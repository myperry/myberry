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
package org.myberry.client.impl.factory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.myberry.client.AbstractMyberryClient;
import org.myberry.client.ClientConfig;
import org.myberry.client.admin.DefaultAdminClient;
import org.myberry.client.exception.MyberryClientException;
import org.myberry.client.impl.AbstractClientImpl;
import org.myberry.client.impl.HeartbeatResult;
import org.myberry.client.impl.MyberryClientAPIImpl;
import org.myberry.client.impl.MyberryClientManager;
import org.myberry.client.router.DefaultRouter;
import org.myberry.common.MixAll;
import org.myberry.common.ServiceState;
import org.myberry.common.constant.LoggerName;
import org.myberry.common.protocol.header.HeartbeatRequestHeader;
import org.myberry.remoting.netty.NettyClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyberryClientInstance {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.CLIENT_LOGGER_NAME);

  private final AbstractMyberryClient abstractMyberryClient;
  private final ClientConfig clientConfig;
  private final int instanceIndex;
  private final String clientId;
  private final StringBuffer clientInstanceName = new StringBuffer();

  private final ConcurrentMap<String /* group */, AbstractClientImpl> clientTable =
      new ConcurrentHashMap<String, AbstractClientImpl>();
  private final NettyClientConfig nettyClientConfig;
  private final MyberryClientAPIImpl myberryClientAPIImpl;
  private final DefaultRouter defaultRouter;
  private final KeepaliveService keepaliveService;
  private ServiceState serviceState = ServiceState.CREATE_JUST;

  public MyberryClientInstance(
      AbstractMyberryClient abstractMyberryClient, int instanceIndex, String clientId) {
    this.abstractMyberryClient = abstractMyberryClient;
    this.clientConfig = abstractMyberryClient.cloneClientConfig();
    this.instanceIndex = instanceIndex;

    this.nettyClientConfig = new NettyClientConfig();
    this.myberryClientAPIImpl = new MyberryClientAPIImpl(nettyClientConfig);
    this.defaultRouter = new DefaultRouter();
    this.keepaliveService = new KeepaliveService();
    this.clientId = clientId;

    this.clientInstanceName.append(clientConfig.getClientIP());
    this.clientInstanceName.append("-");
    this.clientInstanceName.append(instanceIndex);

    this.abstractMyberryClient.setDefaultRouter(defaultRouter);
  }

  public void start() throws MyberryClientException {
    synchronized (this) {
      switch (this.serviceState) {
        case CREATE_JUST:
          this.serviceState = ServiceState.START_FAILED;
          if (null == this.clientConfig.getServerAddr()) {
            throw new MyberryClientException("server address is null", null);
          }

          this.myberryClientAPIImpl.start();
          this.keepaliveService.startScheduledTask();
          this.serviceState = ServiceState.RUNNING;
          break;
        case START_FAILED:
          throw new MyberryClientException(
              "The Factory object[" + this.getClientId() + "] has been created before, and failed.",
              null);
        default:
          break;
      }
    }
  }

  public void shutdown() {
    synchronized (this) {
      switch (this.serviceState) {
        case CREATE_JUST:
          break;
        case RUNNING:
          this.serviceState = ServiceState.SHUTDOWN_ALREADY;
          this.keepaliveService.stopScheduledTask();
          this.myberryClientAPIImpl.shutdown();
          MyberryClientManager.getInstance().removeClientFactory(this.clientId);
          log.info("the client factory [{}] shutdown OK", this.clientId);
          break;
        case SHUTDOWN_ALREADY:
          break;
        default:
          break;
      }
    }
  }

  public boolean registerClient(final String group, final AbstractClientImpl clientImpl) {
    if (null == group || null == clientImpl) {
      return false;
    }

    AbstractClientImpl prev = this.clientTable.putIfAbsent(group, clientImpl);
    if (prev != null) {
      log.warn("the client group[{}] exist already.", group);
      return false;
    }

    return true;
  }

  public void unregisterClient(final String group) {
    this.clientTable.remove(group);
  }

  private class KeepaliveService {
    private final long HEARTBEAT_TIMEOUT_MILLIS = 3000L;
    private volatile boolean latestHeartbeat = false;

    private final ScheduledExecutorService scheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor(
            new ThreadFactory() {
              @Override
              public Thread newThread(Runnable r) {
                return new Thread(r, "ClientKeepaliveThread");
              }
            });

    void stopScheduledTask() {
      this.scheduledExecutorService.shutdown();
    }

    void startScheduledTask() throws MyberryClientException {
      this.tryConnectServer();

      this.scheduledExecutorService.scheduleAtFixedRate(
          new Runnable() {
            @Override
            public void run() {
              sendHeartbeat(defaultRouter.getHeartbeatServerAddr(latestHeartbeat));
            }
          },
          0,
          clientConfig.getHeartbeatServerInterval(),
          TimeUnit.MILLISECONDS);
    }

    private void sendHeartbeat(String addr) {
      try {
        HeartbeatRequestHeader requestHeader = new HeartbeatRequestHeader();
        requestHeader.setClientId(clientId);

        DefaultAdminClient defaultAdminClient = null;
        if (MyberryClientInstance.this.abstractMyberryClient instanceof DefaultAdminClient) {
          defaultAdminClient =
              (DefaultAdminClient) MyberryClientInstance.this.abstractMyberryClient;
          requestHeader.setPassword(defaultAdminClient.getPassword());
        }

        HeartbeatResult heartbeatResult =
            MyberryClientInstance.this.myberryClientAPIImpl.sendHearbeat(
                addr, requestHeader, HEARTBEAT_TIMEOUT_MILLIS);
        if (heartbeatResult != null) {
          log.debug("recvHeartbeat success from {}", addr);
          if (heartbeatResult.getRouterInfo() != null) {
            defaultRouter.setRouterInfoByHeartbeat(heartbeatResult.getRouterInfo());
          }

          this.latestHeartbeat = true;
        }
      } catch (Exception e) {
        log.error("sendHeartbeat exception: ", e);
        this.latestHeartbeat = false;
      }
    }

    private void tryConnectServer() throws MyberryClientException {
      String serverAddr = MyberryClientInstance.this.clientConfig.getServerAddr();
      if (MixAll.isBlank(serverAddr)) {
        throw new MyberryClientException("serverAddr is null");
      }

      List<String> addrs = Arrays.asList(serverAddr.split(","));
      for (String addr : addrs) {
        log.info("try connect server: {}", addr);
        sendHeartbeat(addr);
        if (latestHeartbeat) {
          break;
        }
      }

      if (!latestHeartbeat) {
        throw new MyberryClientException(
            String.format("can't connect server, serverAddr is [%s]", serverAddr));
      }
    }
  }

  public MyberryClientAPIImpl getMyberryClientAPIImpl() {
    return myberryClientAPIImpl;
  }

  public int getInstanceIndex() {
    return instanceIndex;
  }

  public String getClientId() {
    return clientId;
  }
}
