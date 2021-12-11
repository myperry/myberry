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
package org.myberry.server.ha.collect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import org.myberry.common.ServiceThread;
import org.myberry.common.route.NodeType;
import org.myberry.remoting.common.RemotingHelper;
import org.myberry.remoting.netty.NettyServerConfig;
import org.myberry.server.common.LoggerName;
import org.myberry.server.config.ServerConfig;
import org.myberry.server.ha.HAContext;
import org.myberry.server.ha.HAHouseKeepService;
import org.myberry.server.ha.HAMessage;
import org.myberry.server.ha.HAMessageDispatcher;
import org.myberry.server.ha.HAService;
import org.myberry.server.ha.HASynchronizer;
import org.myberry.server.ha.HATransfer;
import org.myberry.server.ha.collect.NodeBlock.BlockObject;
import org.myberry.store.BlockHeader;
import org.myberry.store.MyberryStore;
import org.myberry.store.config.StoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaderCollectSynchronizer implements HASynchronizer {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.LEADER_ROUTE_SYNC_NAME);

  private final DefaultSyncCollectAdapter defaultSyncCollectAdapter;
  private final LinkedBlockingQueue<HAMessage> recvQueue;
  private final CollectSyncTrigger collectSyncTrigger;

  public LeaderCollectSynchronizer(final DefaultSyncCollectAdapter defaultSyncCollectAdapter) {
    this.defaultSyncCollectAdapter = defaultSyncCollectAdapter;
    this.recvQueue = defaultSyncCollectAdapter.getRecvQueue();
    this.collectSyncTrigger = new CollectSyncTrigger();
  }

  @Override
  public boolean sync() throws Exception {
    init();
    collectSyncTrigger.start();
    return false;
  }

  @Override
  public void shutdown() {
    collectSyncTrigger.shutdown(true);
  }

  class CollectSyncTrigger extends ServiceThread {

    @Override
    public String getServiceName() {
      return CollectSyncTrigger.class.getSimpleName();
    }

    @Override
    public void run() {
      while (!this.isStopped()) {
        try {
          HAMessage haMessage = recvQueue.poll();
          if (null != haMessage) {
            Collect c = (Collect) haMessage.getHaMessage();
            if (leaderIsMe(c.getLeader())) {
              getHaHouseKeepService().updateLearnersResponseTime(c.getSid());

              boolean needToPrint =
                  getCollectService()
                      .putNodeByLeader(c.getLeader(), c.getNodeAddr(), c.getNodeBlock());
              if (needToPrint) {
                getCollectService().printAllRouting();
              }
              if (getCollectService().checkSettingsChangeByLeader(getHaContext().getMemberMap())) {
                getHaService().writeBack();
              }

              notifyLearner(c.getSid(), createCollect());
            }
          } else {
            this.waitForRunning(3 * 1000);
          }

          boolean needToPrint = getCollectService().expireByLeader();
          if (needToPrint) {
            getCollectService().printAllRouting();
          }
        } catch (InterruptedException e) {
          // Ignore
        } catch (Exception e) {
          LeaderCollectSynchronizer.log.error("CollectSyncTrigger error: ", e);
          try {
            this.waitForRunning(3 * 1000);
          } catch (InterruptedException ex) {
            // Ignore
          }
        }
      }
    }
  }

  private void notifyLearner(int connId, Collect collect) {
    HAMessage haMessage = new HAMessage(HATransfer.COLLECT, connId, collect);
    getHaMessageDispatcher().haMessageDelivery(connId, haMessage);
  }

  private Collect createCollect() {
    Collect collect = new Collect();
    collect.setSid(getStoreConfig().getMySid());
    collect.setLeader(getStoreConfig().getMySid());
    collect.setNodeAddrs(new ArrayList<>(getCollectService().getRouteList()));
    collect.setNodeBlocks(getCollectService().getBlockList());
    return collect;
  }

  private boolean leaderIsMe(int leader) {
    return getStoreConfig().getMySid() == leader;
  }

  private void init() {
    NodeAddr myAddr = makeNodeAddr();
    NodeBlock myNodeBlock = makeNodeBlock();

    boolean needToPrint =
        getCollectService().putNodeByLeader(getStoreConfig().getMySid(), myAddr, myNodeBlock);
    if (needToPrint) {
      getCollectService().printAllRouting();
    }
  }

  private NodeAddr makeNodeAddr() {
    NodeAddr myAddr = new NodeAddr();
    myAddr.setSid(getStoreConfig().getMySid());
    myAddr.setType(NodeType.LEADING_NAME);
    myAddr.setWeight(getServerConfig().getWeight());
    myAddr.setIp(
        RemotingHelper.getAddressIP(
            getHaContext().getMemberMap().get(getStoreConfig().getMySid())));
    myAddr.setListenPort(getNettyServerConfig().getListenPort());
    myAddr.setHaPort(
        RemotingHelper.getAddressPort(
            getHaContext().getMemberMap().get(getStoreConfig().getMySid())));
    return myAddr;
  }

  private NodeBlock makeNodeBlock() {
    List<BlockHeader> blockHeaderList = getMyberryStore().getBlockHeaderList();
    List<BlockObject> blockObjects = new ArrayList<>();
    if (null != blockHeaderList) {
      for (BlockHeader blockHeader : blockHeaderList) {
        BlockObject blockObject = new BlockObject();
        blockObject.setBlockIndex(blockHeader.getBlockIndex());
        blockObject.setComponentCount(blockHeader.getComponentCount());
        blockObject.setBeginPhyOffset(blockHeader.getBeginPhyOffset());
        blockObject.setEndPhyOffset(blockHeader.getEndPhyOffset());
        blockObject.setBeginTimestamp(blockHeader.getBeginTimestamp());
        blockObject.setEndTimestamp(blockHeader.getEndTimestamp());

        blockObjects.add(blockObject);
      }
    }

    NodeBlock nodeBlock = new NodeBlock();
    nodeBlock.setSid(getStoreConfig().getMySid());
    nodeBlock.setBlockObjectList(blockObjects);

    return nodeBlock;
  }

  private HAService getHaService() {
    return defaultSyncCollectAdapter.getHaService();
  }

  private HAContext getHaContext() {
    return getHaService().getHaContext();
  }

  private StoreConfig getStoreConfig() {
    return getHaContext().getStoreConfig();
  }

  private ServerConfig getServerConfig() {
    return getHaContext().getServerConfig();
  }

  private NettyServerConfig getNettyServerConfig() {
    return getHaContext().getNettyServerConfig();
  }

  private HAMessageDispatcher getHaMessageDispatcher() {
    return getHaService().getHaMessageDispatcher();
  }

  private HAHouseKeepService getHaHouseKeepService() {
    return getHaService().getHaHouseKeepService();
  }

  private MyberryStore getMyberryStore() {
    return getHaService().getMyberryStore();
  }

  private CollectService getCollectService() {
    return defaultSyncCollectAdapter.getCollectService();
  }
}
