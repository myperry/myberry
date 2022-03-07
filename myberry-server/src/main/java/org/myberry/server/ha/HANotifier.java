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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.myberry.common.codec.MessageLite;
import org.myberry.server.ha.collect.Collect;
import org.myberry.server.ha.collect.CollectService;
import org.myberry.server.ha.collect.NodeAddr;
import org.myberry.server.ha.collect.NodeBlock;
import org.myberry.server.ha.collect.NodeBlock.BlockObject;
import org.myberry.server.ha.database.ApplyForBlockAppend;
import org.myberry.server.ha.database.Database;
import org.myberry.server.ha.database.SyncDatabaseCommand;
import org.myberry.store.BlockHeader;
import org.myberry.store.MyberryStore;
import org.myberry.store.config.StoreConfig;

public class HANotifier {

  private final MyberryStore myberryStore;
  private final HAMessageDispatcher haMessageDispatcher;
  private final CollectService collectService;
  private final StoreConfig storeConfig;

  public HANotifier(
      final MyberryStore myberryStore,
      final HAMessageDispatcher haMessageDispatcher,
      final CollectService collectService,
      final StoreConfig storeConfig) {
    this.myberryStore = myberryStore;
    this.haMessageDispatcher = haMessageDispatcher;
    this.collectService = collectService;
    this.storeConfig = storeConfig;
  }

  public void updateBlockTable() {
    collectService.updateBlockByLeader(storeConfig.getMySid(), makeNodeBlock());
  }

  public void notifyCollect() {
    Collect collect = new Collect();
    collect.setSid(storeConfig.getMySid());
    collect.setLeader(storeConfig.getMySid());
    collect.setNodeAddrs(new ArrayList<>(collectService.getRouteList()));
    collect.setNodeBlocks(collectService.getBlockList());

    delivery(HATransfer.COLLECT, collect);
  }

  public void notifyDatabaseAppend(
      int applyForBlockIndex, int applyForEndPhyOffset, byte[] syncData) {
    ApplyForBlockAppend applyForBlockAppend = new ApplyForBlockAppend();
    applyForBlockAppend.setApplyForBlockIndex(applyForBlockIndex);
    applyForBlockAppend.setApplyForEndPhyOffset(applyForEndPhyOffset);

    Database db = new Database();
    db.setSid(storeConfig.getMySid());
    db.setLeader(storeConfig.getMySid());
    db.setCmd(SyncDatabaseCommand.APPEND.getCode());
    db.setApplyForBlockAppend(applyForBlockAppend);

    delivery(HATransfer.DATABASE, db, syncData);
  }

  private void delivery(int transferType, MessageLite messageLite) {
    delivery(transferType, messageLite, null);
  }

  private void delivery(int transferType, MessageLite messageLite, byte[] syncData) {
    Collection<NodeAddr> viewList = collectService.getViewMap().values();
    for (NodeAddr nodeAddr : viewList) {
      HAMessage haMessage = new HAMessage(transferType, nodeAddr.getSid(), messageLite);
      haMessage.setData(syncData);
      haMessageDispatcher.haMessageDelivery(nodeAddr.getSid(), haMessage);
    }
  }

  private NodeBlock makeNodeBlock() {
    List<BlockHeader> blockHeaderList = myberryStore.getBlockHeaderList();
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
    nodeBlock.setSid(storeConfig.getMySid());
    nodeBlock.setBlockObjectList(blockObjects);

    return nodeBlock;
  }
}
