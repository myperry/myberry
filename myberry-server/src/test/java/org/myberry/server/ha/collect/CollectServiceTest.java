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
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.myberry.common.loadbalance.Invoker;
import org.myberry.common.route.NodeState;
import org.myberry.server.ha.collect.NodeBlock.BlockObject;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CollectServiceTest {

  private static CollectService collectService;

  @BeforeClass
  public static void init() {
    collectService = new CollectService();
  }

  @Test
  public void test01PutNodeByLeader() {
    NodeAddr leaderAddr = new NodeAddr();
    leaderAddr.setSid(2);
    leaderAddr.setType("Leader");
    leaderAddr.setWeight(1);
    leaderAddr.setIp("192.168.1.2");
    leaderAddr.setListenPort(8085);
    leaderAddr.setHaPort(10757);
    leaderAddr.setNodeState(NodeState.NORMAL.getCode());
    leaderAddr.setLastUpdateTimestamp(System.currentTimeMillis());

    NodeAddr learnerAddr = new NodeAddr();
    learnerAddr.setSid(1);
    learnerAddr.setType("Learner");
    learnerAddr.setWeight(1);
    learnerAddr.setIp("192.168.1.2");
    learnerAddr.setListenPort(8086);
    learnerAddr.setHaPort(10767);
    learnerAddr.setNodeState(NodeState.NORMAL.getCode());
    learnerAddr.setLastUpdateTimestamp(System.currentTimeMillis());

    BlockObject leaderBlockObject = new BlockObject();
    leaderBlockObject.setBlockIndex(0);
    leaderBlockObject.setComponentCount(1);
    leaderBlockObject.setBeginPhyOffset(32);
    leaderBlockObject.setEndPhyOffset(56);
    leaderBlockObject.setBeginTimestamp(System.currentTimeMillis());
    leaderBlockObject.setEndTimestamp(System.currentTimeMillis());

    List<BlockObject> leaderBlockObjects = new ArrayList<>();
    leaderBlockObjects.add(leaderBlockObject);

    NodeBlock leaderNodeBlock = new NodeBlock();
    leaderNodeBlock.setSid(2);
    leaderNodeBlock.setBlockObjectList(leaderBlockObjects);

    BlockObject learnerBlockObject = new BlockObject();
    learnerBlockObject.setBlockIndex(0);
    learnerBlockObject.setComponentCount(1);
    learnerBlockObject.setBeginPhyOffset(32);
    learnerBlockObject.setEndPhyOffset(56);
    learnerBlockObject.setBeginTimestamp(System.currentTimeMillis());
    learnerBlockObject.setEndTimestamp(System.currentTimeMillis());

    List<BlockObject> learnerBlockObjects = new ArrayList<>();
    learnerBlockObjects.add(learnerBlockObject);

    NodeBlock learnerNodeBlock = new NodeBlock();
    learnerNodeBlock.setSid(1);
    learnerNodeBlock.setBlockObjectList(learnerBlockObjects);

    collectService.putNodeByLeader(2, leaderAddr, leaderNodeBlock);
    collectService.putNodeByLeader(2, learnerAddr, learnerNodeBlock);
  }

  @Test
  public void test02RouteList() {
    Set<NodeAddr> routeList = collectService.getRouteList();
    Assert.assertEquals(2, routeList.size());
  }

  @Test
  public void test03LearnerTable() {
    List<Invoker> learnerList = collectService.getLearnerList();
    Assert.assertEquals(1, learnerList.size());
  }

  @Test
  public void test04ViewMap() {
    Map<Integer, NodeAddr> viewMap = collectService.getViewMap();
    Assert.assertEquals(2, viewMap.size());

    collectService.kickOutLearner(1);
    viewMap = collectService.getViewMap();
    Assert.assertEquals(1, viewMap.size());
  }

  @Test
  public void test05BlockList() {
    List<NodeBlock> blockList = collectService.getBlockList();
    Assert.assertEquals(2, blockList.size());
  }

  @Test
  public void test06LeaderAddr() {
    String leaderAddr = collectService.getLeaderAddr();
    Assert.assertNotEquals("", leaderAddr);
  }

  @Test
  public void test07UpdateWeigth() {
    Set<NodeAddr> routeList = collectService.getRouteList();
    for (NodeAddr nodeAddr : routeList) {
      if (nodeAddr.getSid() == 1) {
        Assert.assertEquals(1, nodeAddr.getWeight());
        break;
      }
    }

    List<Invoker> learnerList = collectService.getLearnerList();
    for (Invoker invoker : learnerList) {
      if ("192.168.1.2:8086".equals(invoker.getAddr())) {
        Assert.assertEquals(1, invoker.getWeight());
        break;
      }
    }

    collectService.updateWeight(1, 2);

    routeList = collectService.getRouteList();
    for (NodeAddr nodeAddr : routeList) {
      if (nodeAddr.getSid() == 1) {
        Assert.assertEquals(2, nodeAddr.getWeight());
        break;
      }
    }

    learnerList = collectService.getLearnerList();
    for (Invoker invoker : learnerList) {
      if ("192.168.1.2:8086".equals(invoker.getAddr())) {
        Assert.assertEquals(2, invoker.getWeight());
        break;
      }
    }
  }

  @Test
  public void test08KickOutLearner() {
    collectService.kickOutLearner(1);
    Set<NodeAddr> routeList = collectService.getRouteList();
    for (NodeAddr nodeAddr : routeList) {
      if (nodeAddr.getSid() == 1) {
        Assert.assertEquals(NodeState.KICKED_OUT.getCode(), nodeAddr.getNodeState());
        break;
      }
    }

    boolean removed = false;
    List<Invoker> learnerList = collectService.getLearnerList();
    for (Invoker invoker : learnerList) {
      if ("192.168.1.2:8086".equals(invoker.getAddr())) {
        removed = true;
        break;
      }
    }
    Assert.assertEquals(false, removed);
  }

  @Test
  public void test09PutNodeByLearner() {
    NodeAddr leaderAddr = new NodeAddr();
    leaderAddr.setSid(3);
    leaderAddr.setType("Leader");
    leaderAddr.setWeight(1);
    leaderAddr.setIp("192.168.1.2");
    leaderAddr.setListenPort(8085);
    leaderAddr.setHaPort(10757);
    leaderAddr.setNodeState(NodeState.NORMAL.getCode());
    leaderAddr.setLastUpdateTimestamp(System.currentTimeMillis());

    NodeAddr learnerAddr = new NodeAddr();
    learnerAddr.setSid(4);
    learnerAddr.setType("Learner");
    learnerAddr.setWeight(1);
    learnerAddr.setIp("192.168.1.2");
    learnerAddr.setListenPort(8086);
    learnerAddr.setHaPort(10767);
    learnerAddr.setNodeState(NodeState.NORMAL.getCode());
    learnerAddr.setLastUpdateTimestamp(System.currentTimeMillis());

    List<NodeAddr> addrList = new ArrayList<>();
    addrList.add(leaderAddr);
    addrList.add(learnerAddr);

    BlockObject leaderBlockObject = new BlockObject();
    leaderBlockObject.setBlockIndex(0);
    leaderBlockObject.setComponentCount(1);
    leaderBlockObject.setBeginPhyOffset(32);
    leaderBlockObject.setEndPhyOffset(56);
    leaderBlockObject.setBeginTimestamp(System.currentTimeMillis());
    leaderBlockObject.setEndTimestamp(System.currentTimeMillis());

    List<BlockObject> leaderBlockObjects = new ArrayList<>();
    leaderBlockObjects.add(leaderBlockObject);

    NodeBlock leaderNodeBlock = new NodeBlock();
    leaderNodeBlock.setSid(3);
    leaderNodeBlock.setBlockObjectList(leaderBlockObjects);

    BlockObject learnerBlockObject = new BlockObject();
    learnerBlockObject.setBlockIndex(0);
    learnerBlockObject.setComponentCount(1);
    learnerBlockObject.setBeginPhyOffset(32);
    learnerBlockObject.setEndPhyOffset(56);
    learnerBlockObject.setBeginTimestamp(System.currentTimeMillis());
    learnerBlockObject.setEndTimestamp(System.currentTimeMillis());

    List<BlockObject> learnerBlockObjects = new ArrayList<>();
    learnerBlockObjects.add(learnerBlockObject);

    NodeBlock learnerNodeBlock = new NodeBlock();
    learnerNodeBlock.setSid(4);
    learnerNodeBlock.setBlockObjectList(learnerBlockObjects);

    List<NodeBlock> blockList = new ArrayList<>();
    blockList.add(leaderNodeBlock);
    blockList.add(learnerNodeBlock);

    collectService.putNodeByLearner(3, addrList, blockList);
  }

  @Test
  public void test10RouteList() {
    Set<NodeAddr> routeList = collectService.getRouteList();
    Assert.assertEquals(2, routeList.size());
  }

  @Test
  public void test11LearnerTable() {
    List<Invoker> learnerList = collectService.getLearnerList();
    Assert.assertEquals(1, learnerList.size());
  }

  @Test
  public void test12ViewMap() {
    Map<Integer, NodeAddr> viewMap = collectService.getViewMap();
    Assert.assertEquals(2, viewMap.size());
  }

  @Test
  public void test13BlockList() {
    List<NodeBlock> blockList = collectService.getBlockList();
    Assert.assertEquals(2, blockList.size());
  }
}
