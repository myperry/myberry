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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.myberry.common.ServiceThread;
import org.myberry.remoting.common.RemotingHelper;
import org.myberry.remoting.netty.NettyServerConfig;
import org.myberry.server.common.LoggerName;
import org.myberry.server.config.ServerConfig;
import org.myberry.server.converter.ConverterService;
import org.myberry.server.ha.collect.CollectService;
import org.myberry.server.ha.collect.DefaultSyncCollectAdapter;
import org.myberry.server.ha.collect.NodeAddr;
import org.myberry.server.ha.collect.SyncCollectAdapter;
import org.myberry.server.ha.database.DefaultSyncDatabaseAdapter;
import org.myberry.server.ha.database.SyncDatabaseAdapter;
import org.myberry.server.ha.quarum.DefaultQuorum;
import org.myberry.server.ha.quarum.Quorum;

import org.myberry.server.util.ConfigUtils;
import org.myberry.store.MyberryStore;
import org.myberry.store.config.StoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HAService {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.HASERVICE_LOGGER_NAME);

  private final MyberryStore myberryStore;
  private final ConverterService converterService;
  private final StoreConfig storeConfig;
  private final ServerConfig serverConfig;
  private final NettyServerConfig nettyServerConfig;
  private final HAConnectionManager haConnectionManager;
  private final HAMessageDispatcher haMessageDispatcher;
  private final Quorum quorum;
  private final SyncDatabaseAdapter syncDatabaseAdapter;
  private final SyncCollectAdapter syncCollectAdapter;
  private final HAHouseKeepService haHouseKeepService;
  private final HAWriteBackService haWriteBackService;

  private AtomicReference<HAContext> haContext = new AtomicReference<>();

  private volatile HAState haState = HAState.LOOKING;

  public HAService(
      final MyberryStore myberryStore,
      final ConverterService converterService,
      final StoreConfig storeConfig,
      final ServerConfig serverConfig,
      final NettyServerConfig nettyServerConfig) {
    this.myberryStore = myberryStore;
    this.converterService = converterService;
    this.storeConfig = storeConfig;
    this.serverConfig = serverConfig;
    this.nettyServerConfig = nettyServerConfig;
    this.updateHaContext();
    this.haConnectionManager = new HAConnectionManager(this);
    this.haMessageDispatcher = new HAMessageDispatcher(haConnectionManager);
    this.quorum = new DefaultQuorum(this);
    this.syncDatabaseAdapter = new DefaultSyncDatabaseAdapter(this);
    this.syncCollectAdapter = new DefaultSyncCollectAdapter(this);
    this.haHouseKeepService = new HAHouseKeepService(this);
    this.haWriteBackService = new HAWriteBackService();
  }

  public void updateHaContext() {
    Map<Integer, String> haServerAddrMap =
        ConfigUtils.parseHAServerAddr(serverConfig.getHaServerAddr());

    String myHaAddr = haServerAddrMap.get(storeConfig.getMySid());

    HAContext haContext = new HAContext(storeConfig, serverConfig, nettyServerConfig, this);
    haContext.setHaPort(RemotingHelper.getAddressPort(myHaAddr));

    haContext.setMemberMap(haServerAddrMap);

    Map<Integer, String> duplicateMap = new HashMap<>(haServerAddrMap);

    duplicateMap.remove(storeConfig.getMySid());
    haContext.setOtherMemberMap(duplicateMap);

    this.haContext.set(haContext);
  }

  public void start() throws Exception {
    checkSafe();

    haConnectionManager.start();
    haMessageDispatcher.start();

    quorum.start();

    boolean timedOut = syncDatabaseAdapter.start();
    if (timedOut) {
      restart();
      return;
    }
    timedOut = syncCollectAdapter.start();
    if (timedOut) {
      restart();
      return;
    }

    haHouseKeepService.start();
    haWriteBackService.start();
  }

  public void shutdown() {
    haWriteBackService.shutdown(true);
    haHouseKeepService.shutdown();
    syncCollectAdapter.shutdown();
    syncDatabaseAdapter.shutdown();
    quorum.shutdown();
    haMessageDispatcher.shutdown();
    haConnectionManager.finish();
  }

  public void restart() throws Exception {
    log.info(">>>>>>>>>>>>>>>>>>>>>>>>>> restart begin");
    syncCollectAdapter.shutdown();
    syncDatabaseAdapter.shutdown();
    quorum.shutdown();

    haState = HAState.LOOKING;

    quorum.start();

    boolean timedOut = syncDatabaseAdapter.start();
    if (timedOut) {
      restart();
      return;
    }
    timedOut = syncCollectAdapter.start();
    if (timedOut) {
      restart();
    }
    log.info("<<<<<<<<<<<<<<<<<<<<<<<<<< restart finish.");
  }

  private void checkSafe() throws IllegalArgumentException {
    if (storeConfig.getMySid() < 1) {
      throw new IllegalArgumentException(
          "mySid less than 1 is illegal, current mySid=" + storeConfig.getMySid());
    }

    int mySid = myberryStore.getMySid(0);
    if (mySid != -1 && mySid != storeConfig.getMySid()) {
      throw new IllegalArgumentException(
          "storeConfig mySid=" + storeConfig.getMySid() + ", the stored mySid=" + mySid);
    }
  }

  public Quorum getQuorum() {
    return quorum;
  }

  public HAMessageDispatcher getHaMessageDispatcher() {
    return haMessageDispatcher;
  }

  public HAContext getHaContext() {
    return haContext.get();
  }

  public HAState getHaState() {
    return haState;
  }

  public void setHaState(HAState haState) {
    this.haState = haState;
  }

  public MyberryStore getMyberryStore() {
    return myberryStore;
  }

  public ConverterService getConverterService() {
    return converterService;
  }

  public CollectService getCollectService() {
    return syncCollectAdapter.getCollectService();
  }

  public HAHouseKeepService getHaHouseKeepService() {
    return haHouseKeepService;
  }

  public void writeBack() {
    haWriteBackService.wakeup();
  }

  class HAWriteBackService extends ServiceThread {

    @Override
    public String getServiceName() {
      return HAWriteBackService.class.getSimpleName();
    }

    @Override
    public void run() {
      while (!this.isStopped()) {
        try {
          this.waitForRunning();

          boolean checkResult = false;
          switch (haState) {
            case LEADING:
              checkResult = true;
              break;
            case LEARNING:
              checkResult =
                  syncCollectAdapter
                      .getCollectService()
                      .checkSettingsChangeByLearner(
                          getHaContext().getMemberMap(),
                          storeConfig.getMySid(),
                          serverConfig.getWeight());
              break;
            default:
              break;
          }

          if (checkResult) {
            Map<Integer, NodeAddr> viewMap = HAService.this.getCollectService().getViewMap();
            if (viewMap.containsKey(storeConfig.getMySid())) {
              String oldHaServerAddr = serverConfig.getHaServerAddr();
              int oldWeight = serverConfig.getWeight();

              NodeAddr[] nodeAddrs = viewMap.values().toArray(new NodeAddr[0]);
              serverConfig.setHaServerAddr(ConfigUtils.generateHAServerAddr(nodeAddrs));
              serverConfig.setWeight(viewMap.get(storeConfig.getMySid()).getWeight());

              HAService.log.info(
                  "ServerConfig update. haServerAddr = [NEW : {}, OLD : {}], weight = [NEW : {}, OLD : {}]",
                  serverConfig.getHaServerAddr(),
                  oldHaServerAddr,
                  serverConfig.getWeight(),
                  oldWeight);

              updateHaContext();
              haConnectionManager.connectionWorkerUpdate();

              if (serverConfig.isWriteBackEnabled()) {
                ConfigUtils.writeToPropertiesFile(
                    serverConfig.getMyberryHome() + "/conf/myberry.properties", serverConfig);
                HAService.log.info(
                    "myberry.properties update. haServerAddr = [NEW : {}, OLD : {}], weight = [NEW : {}, OLD : {}]",
                    serverConfig.getHaServerAddr(),
                    oldHaServerAddr,
                    serverConfig.getWeight(),
                    oldWeight);
              }
            }
          }
        } catch (InterruptedException e) {
          // Ignore
        } catch (Exception e) {
          HAService.log.error("HAWriteBackService exception: ", e);
        }
      }
    }
  }
}
