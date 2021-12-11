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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.myberry.common.ServiceThread;
import org.myberry.remoting.common.RemotingUtil;
import org.myberry.server.common.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HAConnectionManager {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.HA_CONNECTION_MANAGER_NAME);

  private final Map<Integer, ArrayBlockingQueue<HAMessage>> queueSendMap;
  private final ArrayBlockingQueue<HAMessage> recvQueue;
  private final AcceptSocketService acceptSocketService;

  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final Map<Integer /*sid*/, HAConnection> haConnectionMap;
  private final Map<Integer /*sid*/, ConnectionWorker> connectionWorkerMap;

  private final HAService haService;

  public HAConnectionManager(final HAService haService) {
    this.haService = haService;
    this.acceptSocketService = new AcceptSocketService(getHaContext().getHaPort());
    this.queueSendMap = new HashMap<>(17);
    this.recvQueue = new ArrayBlockingQueue<>(101);
    this.haConnectionMap = new HashMap<>();
    this.connectionWorkerMap = new HashMap<>();
  }

  public void start() throws Exception {
    this.acceptSocketService.beginAccept();
    this.acceptSocketService.start();
    this.connectionWorkerStart();
  }

  public void finish() {
    this.acceptSocketService.shutdown();
    this.connectionWorkerStop();
    this.destroyConnections();
  }

  public void connectionWorkerStart() {
    lock.writeLock().lock();
    try {
      Iterator<Entry<Integer, String>> it =
          getHaContext().getOtherMemberMap().entrySet().iterator();
      while (it.hasNext()) {
        Entry<Integer, String> entry = it.next();

        ConnectionWorker connectionWorker = new ConnectionWorker(entry.getKey(), entry.getValue());
        connectionWorker.start();

        connectionWorkerMap.put(entry.getKey(), connectionWorker);
      }
    } catch (Exception e) {
      log.error("connectionWorkerStart Exception: ", e);
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void connectionWorkerStop() {
    lock.writeLock().lock();
    try {
      Iterator<Entry<Integer, ConnectionWorker>> it = connectionWorkerMap.entrySet().iterator();
      while (it.hasNext()) {
        Entry<Integer, ConnectionWorker> entry = it.next();
        entry.getValue().shutdown(true);
      }
      connectionWorkerMap.clear();
    } catch (Exception e) {
      log.error("connectionWorkerStop Exception: ", e);
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void connectionWorkerWakeup() {
    lock.readLock().lock();
    try {
      Iterator<Entry<Integer, ConnectionWorker>> it = connectionWorkerMap.entrySet().iterator();
      while (it.hasNext()) {
        Entry<Integer, ConnectionWorker> entry = it.next();
        if (haConnectionMap.containsKey(entry.getKey())) {
          continue;
        }

        ConnectionWorker connectionWorker = entry.getValue();
        connectionWorker.wakeup();
      }
    } catch (Exception e) {
      log.error("connectionWorkerWakeup Exception: ", e);
    } finally {
      lock.readLock().unlock();
    }
  }

  public void connectionWorkerUpdate() {
    lock.writeLock().lock();
    try {
      Iterator<Entry<Integer, ConnectionWorker>> it = connectionWorkerMap.entrySet().iterator();
      while (it.hasNext()) {
        Entry<Integer, ConnectionWorker> entry = it.next();
        if (!getHaContext().getOtherMemberMap().containsKey(entry.getKey())) {
          entry.getValue().shutdown(true);
          it.remove();
        }
      }

      Iterator<Entry<Integer, String>> iterator =
          getHaContext().getOtherMemberMap().entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<Integer, String> entry = iterator.next();
        if (!connectionWorkerMap.containsKey(entry.getKey())) {
          ConnectionWorker connectionWorker =
              new ConnectionWorker(entry.getKey(), entry.getValue());
          connectionWorkerMap.put(entry.getKey(), connectionWorker);
        }
      }
    } catch (Exception e) {
      log.error("connectionWorkerUpdate Exception: ", e);
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void connectionWorkerWakeup(int connId) {
    lock.readLock().lock();
    try {
      ConnectionWorker connectionWorker = connectionWorkerMap.get(connId);
      if (null != connectionWorker && !haConnectionMap.containsKey(connId)) {
        connectionWorker.wakeup();
      }
    } catch (Exception e) {
      log.error("connectionWorkerWakeup Exception: ", e);
    } finally {
      lock.readLock().unlock();
    }
  }

  public void destroyConnections() {
    lock.writeLock().lock();
    try {
      Iterator<Entry<Integer, HAConnection>> it = haConnectionMap.entrySet().iterator();
      while (it.hasNext()) {
        Entry<Integer, HAConnection> entry = it.next();
        entry.getValue().shutdown();
      }
      haConnectionMap.clear();
    } catch (Exception e) {
      log.error("destroyConnections Exception: ", e);
    } finally {
      lock.writeLock().unlock();
    }
  }

  public boolean containsConnection(final HAConnection haConnection) {
    lock.readLock().lock();
    try {
      return haConnectionMap.containsValue(haConnection);
    } catch (Exception e) {
      log.error("containsConnection Exception: ", e);
    } finally {
      lock.readLock().unlock();
    }
    return false;
  }

  public void addConnectionIfAbsent(final int connId, final HAConnection haConnection) {
    lock.writeLock().lock();
    try {
      if (!haConnectionMap.containsKey(connId)) {
        queueSendMap.putIfAbsent(connId, new ArrayBlockingQueue<>(1));
        if (getHaContext().getStoreConfig().getMySid() > connId) {
          if (haConnection.isOriginWorker()) {
            haConnectionMap.put(connId, haConnection);
          } else {
            if (getHaContext().getOtherMemberMap().containsKey(connId)) {
              haConnection.shutdown();
            } else {
              haConnectionMap.put(connId, haConnection);
            }
          }
        } else if (getHaContext().getStoreConfig().getMySid() < connId) {
          haConnectionMap.put(connId, haConnection);
        } else {
          // Ignore
        }
      } else {
        if (haConnectionMap.get(connId) != haConnection) {
          if (getHaContext().getStoreConfig().getMySid() > connId) {
            if (!haConnection.isOriginWorker()) {
              haConnection.shutdown();
            } else {
              if (!haConnectionMap.get(connId).isOriginWorker() && haConnection.isOriginWorker()) {
                HAConnection haConn = haConnectionMap.put(connId, haConnection);
                haConn.shutdown();
              }
            }
          } else if (getHaContext().getStoreConfig().getMySid() < connId) {
            if (haConnectionMap.get(connId).isOriginWorker() && !haConnection.isOriginWorker()) {
              haConnectionMap.put(connId, haConnection);
            }
          } else {
            // Ignore
          }
        }
      }

      if (log.isInfoEnabled()) {
        printAliveHAConnection();
      }
    } catch (Exception e) {
      log.error("addConnectionIfAbsent Exception: ", e);
    } finally {
      lock.writeLock().unlock();
    }
  }

  private void printAliveHAConnection() {
    StringBuilder builder = new StringBuilder();
    Iterator<Entry<Integer, HAConnection>> iterator = haConnectionMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<Integer, HAConnection> entry = iterator.next();
      builder.append("{connId=");
      builder.append(entry.getKey());
      builder.append(", originWorker=");
      builder.append(entry.getValue().isOriginWorker());
      builder.append("}");
      if (iterator.hasNext()) {
        builder.append(", ");
      }
    }
    log.info(
        "---> The currently alive connections in the mySid={} have: [{}]",
        haService.getHaContext().getStoreConfig().getMySid(),
        builder.toString());
  }

  public void removeConnection(final HAConnection haConnection) {
    lock.writeLock().lock();
    try {
      Iterator<Entry<Integer, HAConnection>> it = haConnectionMap.entrySet().iterator();
      while (it.hasNext()) {
        Entry<Integer, HAConnection> entry = it.next();
        if (entry.getValue() == haConnection) {
          it.remove();
          queueSendMap.remove(entry.getKey());

          log.info(
              "---> The current connection at mySid={} is removed: connId={}, originWorker={}",
              haService.getHaContext().getStoreConfig().getMySid(),
              entry.getKey(),
              haConnection.isOriginWorker());

          if (log.isInfoEnabled()) {
            printAliveHAConnection();
          }
          break;
        }
      }
    } catch (Exception e) {
      log.error("removeConnection Exception: ", e);
    } finally {
      lock.writeLock().unlock();
    }
  }

  public ArrayBlockingQueue<HAMessage> getSendQueue(final int connId) {
    lock.readLock().lock();
    try {
      return queueSendMap.get(connId);
    } catch (Exception e) {
      log.error("getSendQueue Exception: ", e);
    } finally {
      lock.readLock().unlock();
    }

    return null;
  }

  public void deliverHAMessage(int connId, HAMessage haMessage) {
    lock.readLock().lock();
    try {
      ArrayBlockingQueue<HAMessage> sendQueue = queueSendMap.get(connId);
      if (sendQueue == null) {
        return;
      }

      sendQueue.offer(haMessage, 3 * 1000, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      log.error("deliverHAMessage Exception: ", e);
    } finally {
      lock.readLock().unlock();
    }
  }

  public boolean haveDelivered() {
    lock.readLock().lock();
    try {
      for (ArrayBlockingQueue<HAMessage> queue : queueSendMap.values()) {
        if (queue.size() == 0) {
          return true;
        }
      }
    } catch (Exception e) {
      log.error("haveDelivered Exception: ", e);
    } finally {
      lock.readLock().unlock();
    }
    return false;
  }

  public boolean haveDelivered(int connId) {
    lock.readLock().lock();
    try {
      ArrayBlockingQueue<HAMessage> queue = queueSendMap.get(connId);
      if (null != queue && queue.size() == 0) {
        return true;
      }
    } catch (Exception e) {
      log.error("haveDelivered Exception: ", e);
    } finally {
      lock.readLock().unlock();
    }
    return false;
  }

  public HAMessage makeIdentityPacket() {
    HAMessage haMessage = new HAMessage();
    haMessage.setMessageType(HATransfer.IDENTITY_PACKET);
    haMessage.setConnId(getHaContext().getStoreConfig().getMySid());
    return haMessage;
  }

  public ArrayBlockingQueue<HAMessage> getRecvQueue() {
    return recvQueue;
  }

  public HAContext getHaContext() {
    return haService.getHaContext();
  }

  class AcceptSocketService extends ServiceThread {

    private final SocketAddress socketAddressListen;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public AcceptSocketService(final int port) {
      this.socketAddressListen = new InetSocketAddress(port);
    }

    public void beginAccept() throws Exception {
      this.serverSocketChannel = ServerSocketChannel.open();
      this.selector = RemotingUtil.openSelector();
      this.serverSocketChannel.socket().setReuseAddress(true);
      this.serverSocketChannel.socket().bind(this.socketAddressListen);
      this.serverSocketChannel.configureBlocking(false);
      this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void shutdown(final boolean interrupt) {
      super.shutdown(interrupt);
      try {
        if (serverSocketChannel != null) {
          this.serverSocketChannel.close();
        }
        if (selector != null) {
          this.selector.close();
        }
      } catch (IOException e) {
        log.error("AcceptSocketService shutdown exception", e);
      }
    }

    @Override
    public void run() {
      log.info("{} service started", this.getServiceName());

      while (!this.isStopped()) {
        try {
          this.selector.select(1000);
          Set<SelectionKey> selected = this.selector.selectedKeys();

          if (selected != null) {
            for (SelectionKey k : selected) {
              if ((k.readyOps() & SelectionKey.OP_ACCEPT) != 0) {
                SocketChannel sc = ((ServerSocketChannel) k.channel()).accept();

                if (sc != null) {
                  HAConnectionManager.log.info(
                      "{} create a connection {}",
                      this.getServiceName(),
                      sc.socket().getRemoteSocketAddress());

                  try {
                    HAConnection qc = new HAConnection(HAConnectionManager.this, sc);
                    qc.setOriginWorker(false);
                    qc.start();
                  } catch (Exception e) {
                    log.error("new connection exception", e);
                    sc.close();
                  }
                }
              } else {
                log.warn("Unexpected ops in select {}", k.readyOps());
              }
            }

            selected.clear();
          }
        } catch (Exception e) {
          log.error("{} service has exception.", this.getServiceName(), e);
        }
      }

      log.info("{} service end", this.getServiceName());
    }

    @Override
    public String getServiceName() {
      return AcceptSocketService.class.getSimpleName();
    }
  }

  class ConnectionWorker extends ServiceThread {

    private final int connId;
    private final String remoteAddr;
    private final SocketAddress socketAddress;

    private SocketChannel socketChannel;

    public ConnectionWorker(final int connId, final String remoteAddr) {
      this.connId = connId;
      this.remoteAddr = remoteAddr;
      this.socketAddress = RemotingUtil.string2SocketAddress(remoteAddr);
    }

    @Override
    public void run() {
      log.debug(
          "{}: [{}] started", this.getServiceName(), String.format("%d@%s", connId, remoteAddr));
      while (!isStopped()) {
        try {
          if (connect(socketAddress)) {
            log.info(
                "{} create a connection [{}]",
                this.getServiceName(),
                String.format("%d@%s", connId, remoteAddr));

            HAConnection haConn = new HAConnection(HAConnectionManager.this, socketChannel);
            haConn.setOriginWorker(true);
            haConn.setConnId(connId);

            haConn.start();

            this.waitForRunning();
          } else {
            this.waitForRunning(1 * 1000);
          }
        } catch (InterruptedException e) {
          break;
        } catch (Exception e) {
          log.error("{} has exception. ", this.getServiceName(), e);
          try {
            this.waitForRunning(5 * 1000);
          } catch (InterruptedException ie) {
            break;
          }
        }
      }
      log.debug(
          "{}: [{}] started", this.getServiceName(), String.format("%d@%s", connId, remoteAddr));
    }

    private boolean connect(final SocketAddress socketAddress) {
      if (socketAddress != null) {
        this.socketChannel = RemotingUtil.connect(socketAddress);
        if (socketChannel != null) {
          return true;
        }
      }
      return false;
    }

    @Override
    public String getServiceName() {
      return ConnectionWorker.class.getSimpleName();
    }
  }
}
