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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.myberry.common.constant.LoggerName;
import org.myberry.common.loadbalance.Invoker;
import org.myberry.common.route.NodeState;
import org.myberry.common.route.NodeType;
import org.myberry.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectService {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.SERVER_LOGGER_NAME);

  private static final int ROUTE_TIMEOUT = 120 * 1000;

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private final HashMap<Integer /* sid */, NodeAddr> clusterTable = new HashMap<>(64);
  private final HashMap<Integer /* learnerSid */, Invoker> learnerTable = new HashMap<>(64);
  private final HashMap<Integer /* sid */, NodeBlock> blockTable = new HashMap<>(16);

  private int leader;

  public boolean putNodeByLeader(int leader, NodeAddr nodeAddr, NodeBlock nodeBlock) {
    try {
      try {
        this.lock.writeLock().lockInterruptibly();

        long now = System.currentTimeMillis();

        boolean change = false;
        if (this.leader != leader) {
          Iterator<Entry<Integer, NodeAddr>> it = clusterTable.entrySet().iterator();
          while (it.hasNext()) {
            Entry<Integer, NodeAddr> entry = it.next();
            if (entry.getKey().equals(this.leader)) {
              entry.getValue().setNodeState(NodeState.LOST.getCode());
            } else {
              entry.getValue().setLastUpdateTimestamp(now);
            }
          }

          NodeAddr na = clusterTable.get(leader);
          if (null != na) {
            na.setType(NodeType.LEADING_NAME);
          }
          learnerTable.remove(leader);
          blockTable.clear();

          this.leader = leader;
          change = true;
        }

        NodeAddr na = clusterTable.get(nodeAddr.getSid());
        if (null == na) {
          nodeAddr.setLastUpdateTimestamp(now);
          clusterTable.put(nodeAddr.getSid(), nodeAddr);

          if (leader != nodeAddr.getSid()) {
            learnerTable.put(
                nodeAddr.getSid(),
                new Invoker(
                    RemotingHelper.makeStringAddress(nodeAddr.getIp(), nodeAddr.getListenPort()),
                    nodeAddr.getWeight()));
          }

          change = true;
        } else {
          if (na.getNodeState() == NodeState.LOST.getCode()) {
            na.setNodeState(NodeState.NORMAL.getCode());
            learnerTable.put(
                na.getSid(),
                new Invoker(
                    RemotingHelper.makeStringAddress(na.getIp(), na.getListenPort()),
                    na.getWeight()));
            change = true;
          }

          na.setLastUpdateTimestamp(now);
        }

        blockTable.put(nodeBlock.getSid(), nodeBlock);

        return change;
      } finally {
        this.lock.writeLock().unlock();
      }
    } catch (Exception e) {
      log.error("putNodeByLeader Exception: ", e);
    }
    return false;
  }

  public boolean putNodeByLearner(
      int leader, List<NodeAddr> nodeAddrs, List<NodeBlock> nodeBlocks) {
    try {
      try {
        this.lock.writeLock().lockInterruptibly();

        if (this.leader != leader) {
          this.leader = leader;
        }

        long now = System.currentTimeMillis();

        boolean change = false;
        if (nodeAddrs.size() != clusterTable.size()) {
          change = true;
        } else {
          for (NodeAddr nodeAddr : nodeAddrs) {
            if (!clusterTable.containsKey(nodeAddr.getSid())) {
              change = true;
              break;
            } else {
              NodeAddr addr = clusterTable.get(nodeAddr.getSid());
              if (nodeAddr.getNodeState() == NodeState.KICKED_OUT.getCode()
                  && addr.getNodeState() != NodeState.KICKED_OUT.getCode()) {
                change = true;
                break;
              }
              if (nodeAddr.getNodeState() == NodeState.LOST.getCode()
                  && addr.getNodeState() != NodeState.LOST.getCode()) {
                change = true;
                break;
              }
              if (nodeAddr.getNodeState() == NodeState.NORMAL.getCode()
                  && addr.getNodeState() != NodeState.NORMAL.getCode()) {
                change = true;
                break;
              }
              if (nodeAddr.getWeight() != addr.getWeight()) {
                change = true;
                break;
              }
              if (learnerTable.containsKey(nodeAddr.getSid())
                  && now - nodeAddr.getLastUpdateTimestamp() > ROUTE_TIMEOUT) {
                change = true;
                break;
              }
            }
          }
        }

        clusterTable.clear();
        learnerTable.clear();
        blockTable.clear();

        for (NodeAddr nodeAddr : nodeAddrs) {
          clusterTable.put(nodeAddr.getSid(), nodeAddr);
          if (nodeAddr.getLastUpdateTimestamp() + ROUTE_TIMEOUT > now
              && nodeAddr.getNodeState() == NodeState.NORMAL.getCode()) {
            if (leader != nodeAddr.getSid()) {
              learnerTable.put(
                  nodeAddr.getSid(),
                  new Invoker(
                      RemotingHelper.makeStringAddress(nodeAddr.getIp(), nodeAddr.getListenPort()),
                      nodeAddr.getWeight()));
            }
          }
        }

        for (NodeBlock nodeBlock : nodeBlocks) {
          blockTable.put(nodeBlock.getSid(), nodeBlock);
        }

        return change;
      } finally {
        this.lock.writeLock().unlock();
      }
    } catch (Exception e) {
      log.error("putNodeByLearner Exception: ", e);
    }

    return false;
  }

  public void updateBlockByLeader(int mySid, NodeBlock nodeBlock) {
    try {
      try {
        this.lock.writeLock().lockInterruptibly();
        blockTable.put(mySid, nodeBlock);
      } finally {
        this.lock.writeLock().unlock();
      }
    } catch (Exception e) {
      log.error("updateBlockByLeader Exception: ", e);
    }
  }

  public boolean kickOutLearner(int sid) {
    try {
      try {
        this.lock.writeLock().lockInterruptibly();

        if (leader != sid) {
          NodeAddr nodeAddr = clusterTable.get(sid);
          if (null != nodeAddr) {
            nodeAddr.setNodeState(NodeState.KICKED_OUT.getCode());
          }

          learnerTable.remove(sid);

          return true;
        }

      } finally {
        this.lock.writeLock().unlock();
      }
    } catch (Exception e) {
      log.error("kickOutLearner Exception: ", e);
    }
    return false;
  }

  public boolean removeKickedLearner(int sid) {
    try {
      try {
        this.lock.writeLock().lockInterruptibly();

        if (leader != sid) {
          NodeAddr nodeAddr = clusterTable.get(sid);
          if (null != nodeAddr
              && nodeAddr.getNodeState() == NodeState.KICKED_OUT.getCode()
              && nodeAddr.getLastUpdateTimestamp() + ROUTE_TIMEOUT < System.currentTimeMillis()) {
            clusterTable.remove(sid);
            blockTable.remove(sid);
            return true;
          }
        }
      } finally {
        this.lock.writeLock().unlock();
      }
    } catch (Exception e) {
      log.error("removeKickedLearner Exception: ", e);
    }
    return false;
  }

  public boolean updateWeight(int sid, int weight) {
    try {
      try {
        this.lock.writeLock().lockInterruptibly();

        NodeAddr nodeAddr = clusterTable.get(sid);
        if (null != nodeAddr) {
          nodeAddr.setWeight(weight);
        }

        Invoker invoker = learnerTable.get(sid);
        if (null != invoker) {
          invoker.setWeight(weight);
        }

        return true;
      } finally {
        this.lock.writeLock().unlock();
      }
    } catch (Exception e) {
      log.error("updateWeight Exception: ", e);
    }
    return false;
  }

  public boolean expireByLeader() {
    try {
      try {
        this.lock.writeLock().lockInterruptibly();

        long now = System.currentTimeMillis();
        NodeAddr na = clusterTable.get(leader);
        if (null != na) {
          na.setLastUpdateTimestamp(now);
        }

        boolean change = false;
        Iterator<Entry<Integer, NodeAddr>> it = clusterTable.entrySet().iterator();
        while (it.hasNext()) {
          Entry<Integer, NodeAddr> entry = it.next();
          if (entry.getValue().getLastUpdateTimestamp() + ROUTE_TIMEOUT < now) {
            if (learnerTable.containsKey(entry.getKey())) {
              entry.getValue().setNodeState(NodeState.LOST.getCode());
              learnerTable.remove(entry.getKey());
              change = true;
            }
          }
        }

        return change;
      } finally {
        this.lock.writeLock().unlock();
      }
    } catch (Exception e) {
      log.error("expireByLeader Exception: ", e);
    }
    return false;
  }

  public boolean checkSettingsChangeByLeader(Map<Integer, String> memberMap) {
    try {
      try {
        this.lock.readLock().lockInterruptibly();

        Iterator<Entry<Integer, NodeAddr>> it = clusterTable.entrySet().iterator();
        while (it.hasNext()) {
          Entry<Integer, NodeAddr> entry = it.next();
          if (entry.getValue().getNodeState() == NodeState.NORMAL.getCode()
              && !memberMap.containsKey(entry.getKey())) {
            return true;
          }
        }
      } finally {
        this.lock.readLock().unlock();
      }
    } catch (Exception e) {
      log.error("checkSettingsChangeByLeader Exception: ", e);
    }
    return false;
  }

  public boolean checkSettingsChangeByLearner(
      Map<Integer, String> memberMap, int mySid, int weight) {
    try {
      try {
        this.lock.readLock().lockInterruptibly();

        Iterator<Entry<Integer, NodeAddr>> it = clusterTable.entrySet().iterator();
        while (it.hasNext()) {
          Entry<Integer, NodeAddr> entry = it.next();
          if (entry.getValue().getNodeState() == NodeState.KICKED_OUT.getCode()
              && memberMap.containsKey(entry.getKey())) {
            return true;
          }

          if (entry.getValue().getNodeState() == NodeState.NORMAL.getCode()
              && !memberMap.containsKey(entry.getKey())) {
            return true;
          }

          if (entry.getKey().equals(mySid) && entry.getValue().getWeight() != weight) {
            return true;
          }
        }
      } finally {
        this.lock.readLock().unlock();
      }
    } catch (Exception e) {
      log.error("checkSettingsChangeByLearner Exception: ", e);
    }
    return false;
  }

  public String getLeaderAddr() {
    try {
      try {
        this.lock.readLock().lockInterruptibly();

        NodeAddr nodeAddr = clusterTable.get(leader);
        if (null != nodeAddr) {
          return RemotingHelper.makeStringAddress(nodeAddr.getIp(), nodeAddr.getListenPort());
        }
        return null;
      } finally {
        this.lock.readLock().unlock();
      }
    } catch (Exception e) {
      log.error("getLeaderAddr Exception: ", e);
    }
    return null;
  }

  public List<Invoker> getLearnerList() {
    try {
      try {
        this.lock.readLock().lockInterruptibly();
        return new ArrayList<>(this.learnerTable.values());
      } finally {
        this.lock.readLock().unlock();
      }
    } catch (Exception e) {
      log.error("getLearnerList Exception: ", e);
    }
    return new ArrayList<>(0);
  }

  public Set<NodeAddr> getRouteList() {
    try {
      try {
        this.lock.readLock().lockInterruptibly();
        return new HashSet<>(clusterTable.values());
      } finally {
        this.lock.readLock().unlock();
      }
    } catch (Exception e) {
      log.error("getRouteList Exception: ", e);
    }
    return new HashSet<>(0);
  }

  public Map<Integer, NodeAddr> getViewMap() {
    try {
      try {
        this.lock.readLock().lockInterruptibly();

        Map<Integer, NodeAddr> viewMap = new HashMap<>();
        Iterator<Entry<Integer, NodeAddr>> it = clusterTable.entrySet().iterator();
        while (it.hasNext()) {
          Entry<Integer, NodeAddr> entry = it.next();
          if (entry.getValue().getNodeState() != NodeState.KICKED_OUT.getCode()) {
            viewMap.put(entry.getKey(), entry.getValue());
          }
        }
        return viewMap;
      } finally {
        this.lock.readLock().unlock();
      }
    } catch (Exception e) {
      log.error("getViewMap Exception: ", e);
    }
    return new HashMap<>(0);
  }

  public List<NodeBlock> getBlockList() {
    try {
      try {
        this.lock.readLock().lockInterruptibly();
        return new ArrayList<>(blockTable.values());
      } finally {
        this.lock.readLock().unlock();
      }
    } catch (Exception e) {
      log.error("getBlockList Exception: ", e);
    }
    return new ArrayList<>(0);
  }

  public void printAllRouting() {
    try {
      try {
        this.lock.readLock().lockInterruptibly();
        log.info("--------------------------------------------------------");
        {
          log.info("clusterList SIZE: {}", this.clusterTable.size());
          for (NodeAddr nodeAddr : clusterTable.values()) {
            log.info("nodeAddr: {}", nodeAddr);
          }
        }

        {
          log.info("learnerTable SIZE: {}", this.learnerTable.size());
          Iterator<Entry<Integer, Invoker>> it = this.learnerTable.entrySet().iterator();
          while (it.hasNext()) {
            Entry<Integer, Invoker> entry = it.next();
            log.info("learnerTable learnerSid: {} {}", entry.getKey(), entry.getValue());
          }
        }
        log.info("--------------------------------------------------------");
      } finally {
        this.lock.readLock().unlock();
      }
    } catch (Exception e) {
      log.error("printAllTrigger Exception: ", e);
    }
  }
}
