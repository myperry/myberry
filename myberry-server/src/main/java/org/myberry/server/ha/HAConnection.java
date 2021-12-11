/*
* MIT License
*
* Copyright (c) 2021 MyBerry. All rights reserved.
* https://myberry.org/
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:

*   * Redistributions of source code must retain the above copyright notice, this
* list of conditions and the following disclaimer.

*   * Redistributions in binary form must reproduce the above copyright notice,
* this list of conditions and the following disclaimer in the documentation
* and/or other materials provided with the distribution.

*   * Neither the name of MyBerry. nor the names of its contributors may be used
* to endorse or promote products derived from this software without specific
* prior written permission.

* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.

* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
package org.myberry.server.ha;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import org.myberry.common.ServiceThread;
import org.myberry.remoting.common.RemotingUtil;
import org.myberry.server.common.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HAConnection {
  private static final Logger log = LoggerFactory.getLogger(LoggerName.HA_CONNECTION_NAME);

  private final HAConnectionManager haConnectionManager;
  private final SocketChannel socketChannel;
  private final String clientAddr;
  private final WriteSocketService writeSocketService;
  private final ReadSocketService readSocketService;

  private boolean originWorker = false;
  private int connId = -1;

  public HAConnection(
      final HAConnectionManager haConnectionManager, final SocketChannel socketChannel)
      throws IOException {
    this.haConnectionManager = haConnectionManager;
    this.socketChannel = socketChannel;
    this.clientAddr = this.socketChannel.socket().getRemoteSocketAddress().toString();
    this.socketChannel.configureBlocking(false);
    this.socketChannel.socket().setSoLinger(false, -1);
    this.socketChannel.socket().setTcpNoDelay(true);
    this.socketChannel.socket().setReceiveBufferSize(1024 * 64);
    this.socketChannel.socket().setSendBufferSize(1024 * 64);
    this.writeSocketService = new WriteSocketService(this.socketChannel);
    this.readSocketService = new ReadSocketService(this.socketChannel);
  }

  public void start() {
    this.readSocketService.start();
    this.writeSocketService.start();
  }

  public void shutdown() {
    this.writeSocketService.shutdown(true);
    this.readSocketService.shutdown(true);
    this.close();
  }

  public void close() {
    if (this.socketChannel != null) {
      try {
        this.socketChannel.close();
      } catch (IOException e) {
        log.error("close socketChannel error: ", e);
      }
    }
  }

  public boolean isOriginWorker() {
    return originWorker;
  }

  public void setOriginWorker(boolean originWorker) {
    this.originWorker = originWorker;
  }

  public void setConnId(int connId) {
    this.connId = connId;
  }

  public String getClientAddr() {
    return clientAddr;
  }

  class ReadSocketService extends ServiceThread {

    private final int MAX_BUFFER_SIZE =
        haConnectionManager.getHaContext().getStoreConfig().getMaxSyncDataSize();
    private ByteBuffer byteBufferRead = ByteBuffer.allocate(MAX_BUFFER_SIZE);
    private int processPosition = 0;
    private long lastReadTimestamp = System.currentTimeMillis();

    private final Selector selector;
    private final SocketChannel socketChannel;

    private boolean sent = false;

    public ReadSocketService(final SocketChannel socketChannel) throws IOException {
      this.selector = RemotingUtil.openSelector();
      this.socketChannel = socketChannel;
      this.socketChannel.register(this.selector, SelectionKey.OP_READ);
      this.setDaemon(true);
    }

    @Override
    public void run() {
      HAConnection.log.debug("{} service started", this.getServiceName());

      while (!this.isStopped()) {
        try {
          this.selector.select(1000);

          boolean ok = this.processReadEvent();
          if (!ok) {
            HAConnection.log.warn("processReadEvent Interrupted");
            break;
          }

          long interval = System.currentTimeMillis() - this.lastReadTimestamp;
          if (interval
              > haConnectionManager.getHaContext().getServerConfig().getHaHousekeepingInterval()) {
            log.warn(
                "ha housekeeping, found this connection["
                    + HAConnection.this.clientAddr
                    + "] expired, "
                    + interval);
            break;
          }
        } catch (InterruptedException e) {
          break;
        } catch (Exception e) {
          HAConnection.log.error("{} service has exception.", this.getServiceName(), e);
          break;
        }
      }

      this.makeStop();

      writeSocketService.makeStop();

      haConnectionManager.removeConnection(HAConnection.this);

      SelectionKey sk = this.socketChannel.keyFor(this.selector);
      if (sk != null) {
        sk.cancel();
      }

      try {
        this.selector.close();
        this.socketChannel.close();
      } catch (IOException e) {
        HAConnection.log.error("", e);
      }

      HAConnection.log.debug("{} service end", this.getServiceName());
    }

    private boolean processReadEvent() throws InterruptedException {
      int readSizeZeroTimes = 0;

      this.byteBufferRead.clear();
      this.processPosition = 0;

      while (this.byteBufferRead.hasRemaining()) {
        try {
          int readSize = this.socketChannel.read(this.byteBufferRead);
          if (readSize > 0) {
            readSizeZeroTimes = 0;
            this.lastReadTimestamp = System.currentTimeMillis();
            int msgLen = HATransfer.headerDecode(this.byteBufferRead, this.processPosition);
            if (readWholeMessage(msgLen)) {
              HAMessage haMessage = HATransfer.decode(this.byteBufferRead, this.processPosition);
              if (!sent) {
                if (!originWorker) {
                  haConnectionManager.addConnectionIfAbsent(
                      haMessage.getConnId(), HAConnection.this);
                  setConnId(haMessage.getConnId());
                }
                this.sent = true;
              }

              this.processPosition += msgLen;
              haConnectionManager.getRecvQueue().put(haMessage);
            }
          } else if (readSize == 0) {
            if (++readSizeZeroTimes >= 3) {
              break;
            }
          } else {
            log.warn("read socket[{}] < 0", HAConnection.this.clientAddr);
            return false;
          }
        } catch (IOException e) {
          log.error("processReadEvent exception", e);
          return false;
        }
      }

      return true;
    }

    private boolean readWholeMessage(int msgLen) {
      if (byteBufferRead.position() - processPosition >= msgLen) {
        return true;
      } else {
        return false;
      }
    }

    @Override
    public String getServiceName() {
      return ReadSocketService.class.getSimpleName();
    }
  }

  class WriteSocketService extends ServiceThread {

    private final Selector selector;
    private final SocketChannel socketChannel;

    private boolean sent = false;

    public WriteSocketService(final SocketChannel socketChannel) throws IOException {
      this.selector = RemotingUtil.openSelector();
      this.socketChannel = socketChannel;
      this.socketChannel.register(this.selector, SelectionKey.OP_WRITE);
      this.setDaemon(true);
    }

    @Override
    public void run() {
      HAConnection.log.debug("{} service started", this.getServiceName());

      while (!this.isStopped()) {
        try {
          this.selector.select(1000);

          if (!sent) {
            this.transferData(HATransfer.encode(haConnectionManager.makeIdentityPacket()));
            if (originWorker) {
              haConnectionManager.addConnectionIfAbsent(connId, HAConnection.this);
            }
            this.sent = true;
          } else {
            if (haConnectionManager.containsConnection(HAConnection.this)) {
              ArrayBlockingQueue<HAMessage> sendQueue = haConnectionManager.getSendQueue(connId);
              if (sendQueue != null) {
                HAMessage haMessage = sendQueue.poll();
                if (haMessage != null) {
                  this.transferData(HATransfer.encode(haMessage));
                }
              }
            }
          }

          this.waitForRunning(1 * 1000);
        } catch (InterruptedException e) {
          break;
        } catch (Exception e) {
          HAConnection.log.error("{} service has exception.", this.getServiceName(), e);
          break;
        }
      }

      this.makeStop();

      readSocketService.makeStop();

      haConnectionManager.removeConnection(HAConnection.this);

      SelectionKey sk = this.socketChannel.keyFor(this.selector);
      if (sk != null) {
        sk.cancel();
      }

      try {
        this.selector.close();
        this.socketChannel.close();
      } catch (IOException e) {
        HAConnection.log.error("", e);
      }

      HAConnection.log.debug("{} service end", this.getServiceName());
    }

    private boolean transferData(ByteBuffer byteBuffer) throws Exception {
      int writeSizeZeroTimes = 0;
      // Write Header
      while (byteBuffer.hasRemaining()) {
        int writeSize = this.socketChannel.write(byteBuffer);
        if (writeSize > 0) {
          writeSizeZeroTimes = 0;
        } else if (writeSize == 0) {
          if (++writeSizeZeroTimes >= 3) {
            break;
          }
        } else {
          throw new Exception("write error < 0");
        }
      }

      return !byteBuffer.hasRemaining();
    }

    @Override
    public String getServiceName() {
      return WriteSocketService.class.getSimpleName();
    }
  }
}
