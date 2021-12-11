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
package org.myberry.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.myberry.common.ThreadFactoryImpl;
import org.myberry.common.protocol.RequestCode;
import org.myberry.remoting.RemotingServer;
import org.myberry.remoting.netty.NettyRemotingServer;
import org.myberry.remoting.netty.NettyServerConfig;
import org.myberry.server.config.ServerConfig;
import org.myberry.server.converter.ConverterService;
import org.myberry.server.ha.HANotifier;
import org.myberry.server.ha.HAService;
import org.myberry.server.impl.MyberryService;
import org.myberry.server.processor.AdminRequestProcessor;
import org.myberry.server.processor.ClientManageProcessor;
import org.myberry.server.processor.UserRequestProcessor;
import org.myberry.store.DefaultMyberryStore;
import org.myberry.store.MyberryStore;
import org.myberry.store.config.StoreConfig;

public class ServerController {

  private final ServerConfig serverConfig;
  private final NettyServerConfig nettyServerConfig;
  private final StoreConfig storeConfig;
  private final BlockingQueue<Runnable> userManagerThreadPoolQueue;
  private final BlockingQueue<Runnable> clientManagerThreadPoolQueue;
  private final BlockingQueue<Runnable> adminManagerThreadPoolQueue;
  private MyberryStore myberryStore;
  private ConverterService converterService;
  private MyberryService myberryService;
  private HAService haService;
  private RemotingServer remotingServer;
  private ExecutorService userManageExecutor;
  private ExecutorService clientManageExecutor;
  private ExecutorService adminManageExecutor;

  public ServerController(
      final ServerConfig serverConfig,
      final NettyServerConfig nettyServerConfig,
      final StoreConfig storeConfig) {
    this.serverConfig = serverConfig;
    this.nettyServerConfig = nettyServerConfig;
    this.storeConfig = storeConfig;

    this.userManagerThreadPoolQueue =
        new LinkedBlockingQueue<Runnable>(
            this.serverConfig.getUserManagerThreadPoolQueueCapacity());
    this.clientManagerThreadPoolQueue =
        new LinkedBlockingQueue<Runnable>(
            this.serverConfig.getClientManagerThreadPoolQueueCapacity());
    this.adminManagerThreadPoolQueue =
        new LinkedBlockingQueue<Runnable>(
            this.serverConfig.getAdminManagerThreadPoolQueueCapacity());
  }

  public boolean initialize() {
    boolean result = true;
    try {
      this.myberryStore = new DefaultMyberryStore(storeConfig);
      this.converterService = new ConverterService(myberryStore);
      this.myberryService = new MyberryService(myberryStore, converterService);

      if (serverConfig.getHaServerAddr() != null
          && !"".equals(serverConfig.getHaServerAddr().trim())) {
        this.haService =
            new HAService(
                myberryStore, converterService, storeConfig, serverConfig, nettyServerConfig);
        this.myberryService.setHaNotifier(
            new HANotifier(
                myberryStore,
                haService.getHaMessageDispatcher(),
                haService.getCollectService(),
                storeConfig));
      }
    } catch (Exception e) {
      result = false;
      e.printStackTrace();
    }

    if (result) {
      this.remotingServer = new NettyRemotingServer(nettyServerConfig);
      this.userManageExecutor =
          new ThreadPoolExecutor( //
              this.serverConfig.getUserManageThreadPoolNums(), //
              this.serverConfig.getUserManageThreadPoolNums(), //
              1000 * 60, //
              TimeUnit.MILLISECONDS, //
              userManagerThreadPoolQueue, //
              new ThreadFactoryImpl("UserManageThread_") //
              );

      this.clientManageExecutor =
          new ThreadPoolExecutor( //
              this.serverConfig.getClientManageThreadPoolNums(), //
              this.serverConfig.getClientManageThreadPoolNums(), //
              1000 * 60, //
              TimeUnit.MILLISECONDS, //
              this.clientManagerThreadPoolQueue, //
              new ThreadFactoryImpl("ClientManageThread_") //
              );

      this.adminManageExecutor =
          new ThreadPoolExecutor( //
              this.serverConfig.getAdminManageThreadPoolNums(), //
              this.serverConfig.getAdminManageThreadPoolNums(), //
              0L, //
              TimeUnit.MILLISECONDS, //
              this.adminManagerThreadPoolQueue, //
              new ThreadFactoryImpl("AdminManageThread_") //
              );
      this.registerProcessor();
    }

    return result;
  }

  public void registerProcessor() {
    /** UserRequestProcessor */
    UserRequestProcessor userRequestProcessor = new UserRequestProcessor(this);
    this.remotingServer.registerProcessor( //
        RequestCode.PULL_ID, //
        userRequestProcessor, //
        userManageExecutor //
        );

    /** ClientManageProcessor */
    ClientManageProcessor clientManageProcessor = new ClientManageProcessor(this);
    this.remotingServer.registerProcessor( //
        RequestCode.HEART_BEAT, //
        clientManageProcessor, //
        clientManageExecutor //
        );

    /** AdminRequestProcessor */
    AdminRequestProcessor adminRequestProcessor = new AdminRequestProcessor(this);
    /** Default */
    this.remotingServer.registerDefaultProcessor(adminRequestProcessor, adminManageExecutor);
  }

  public void start() throws Exception {
    if (this.myberryStore != null) {
      this.myberryStore.start();
    }

    if (this.converterService != null) {
      this.converterService.start();
    }

    if (this.myberryService != null) {
      this.myberryService.start();
    }

    if (this.haService != null) {
      this.haService.start();
    }

    if (this.remotingServer != null) {
      this.remotingServer.start();
    }
  }

  public void shutdown() {
    if (this.remotingServer != null) {
      this.remotingServer.shutdown();
    }

    if (this.haService != null) {
      this.haService.shutdown();
    }

    if (this.myberryService != null) {
      this.myberryService.shutdown();
    }

    if (this.converterService != null) {
      this.converterService.shutdown();
    }

    if (this.myberryStore != null) {
      this.myberryStore.shutdown();
    }

    if (this.userManageExecutor != null) {
      this.userManageExecutor.shutdown();
    }

    if (this.clientManageExecutor != null) {
      this.clientManageExecutor.shutdown();
    }

    if (this.adminManageExecutor != null) {
      this.adminManageExecutor.shutdown();
    }
  }

  public ServerConfig getServerConfig() {
    return serverConfig;
  }

  public NettyServerConfig getNettyServerConfig() {
    return nettyServerConfig;
  }

  public StoreConfig getStoreConfig() {
    return storeConfig;
  }

  public MyberryStore getMyberryStore() {
    return myberryStore;
  }

  public MyberryService getMyberryService() {
    return myberryService;
  }

  public HAService getHaService() {
    return haService;
  }
}
