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
package org.myberry.common.protocol.body.admin;

import java.util.List;
import org.myberry.common.codec.MessageLite;
import org.myberry.common.codec.annotation.SerialField;
import org.myberry.common.route.NodeState;

public class ClusterListData implements MessageLite {

  @SerialField(ordinal = 0)
  private List<ClusterRoute> clusterRouteList;

  @SerialField(ordinal = 1)
  private List<ClusterDatabase> clusterDatabaseList;

  public List<ClusterRoute> getClusterRouteList() {
    return clusterRouteList;
  }

  public void setClusterRouteList(List<ClusterRoute> clusterRouteList) {
    this.clusterRouteList = clusterRouteList;
  }

  public List<ClusterDatabase> getClusterDatabaseList() {
    return clusterDatabaseList;
  }

  public void setClusterDatabaseList(List<ClusterDatabase> clusterDatabaseList) {
    this.clusterDatabaseList = clusterDatabaseList;
  }

  public static class ClusterRoute implements MessageLite {

    @SerialField(ordinal = 0)
    private int sid;

    @SerialField(ordinal = 1)
    private String type;

    @SerialField(ordinal = 2)
    private int weight;

    @SerialField(ordinal = 3)
    private String ip;

    @SerialField(ordinal = 4)
    private int listenPort;

    @SerialField(ordinal = 5)
    private int haPort;

    /** {@link NodeState} */
    @SerialField(ordinal = 6)
    private int nodeState;

    @SerialField(ordinal = 7)
    private long lastUpdateTimestamp;

    public int getSid() {
      return sid;
    }

    public void setSid(int sid) {
      this.sid = sid;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public int getWeight() {
      return weight;
    }

    public void setWeight(int weight) {
      this.weight = weight;
    }

    public String getIp() {
      return ip;
    }

    public void setIp(String ip) {
      this.ip = ip;
    }

    public int getListenPort() {
      return listenPort;
    }

    public void setListenPort(int listenPort) {
      this.listenPort = listenPort;
    }

    public int getHaPort() {
      return haPort;
    }

    public void setHaPort(int haPort) {
      this.haPort = haPort;
    }

    public int getNodeState() {
      return nodeState;
    }

    public void setNodeState(int nodeState) {
      this.nodeState = nodeState;
    }

    public long getLastUpdateTimestamp() {
      return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
      this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    @Override
    public String toString() {
      return new StringBuilder() //
          .append("ClusterRoute{") //
          .append("sid=") //
          .append(sid) //
          .append(", type=") //
          .append(type) //
          .append(", weight=") //
          .append(weight) //
          .append(", ip=") //
          .append(ip) //
          .append(", listenPort=") //
          .append(listenPort) //
          .append(", haPort=") //
          .append(haPort) //
          .append(", nodeState=") //
          .append(nodeState) //
          .append(", lastUpdateTimestamp=") //
          .append(lastUpdateTimestamp) //
          .append('}') //
          .toString();
    }
  }

  public static class ClusterDatabase implements MessageLite {

    @SerialField(ordinal = 0)
    private int sid;

    @SerialField(ordinal = 1)
    private List<ClusterBlock> blockList;

    public int getSid() {
      return sid;
    }

    public void setSid(int sid) {
      this.sid = sid;
    }

    public List<ClusterBlock> getBlockList() {
      return blockList;
    }

    public void setBlockList(List<ClusterBlock> blockList) {
      this.blockList = blockList;
    }

    public static class ClusterBlock implements MessageLite {
      @SerialField(ordinal = 0)
      private int blockIndex;

      @SerialField(ordinal = 1)
      private int componentCount;

      @SerialField(ordinal = 2)
      private int beginPhyOffset;

      @SerialField(ordinal = 3)
      private int endPhyOffset;

      @SerialField(ordinal = 4)
      private long beginTimestamp;

      @SerialField(ordinal = 5)
      private long endTimestamp;

      public int getBlockIndex() {
        return blockIndex;
      }

      public void setBlockIndex(int blockIndex) {
        this.blockIndex = blockIndex;
      }

      public int getComponentCount() {
        return componentCount;
      }

      public void setComponentCount(int componentCount) {
        this.componentCount = componentCount;
      }

      public int getBeginPhyOffset() {
        return beginPhyOffset;
      }

      public void setBeginPhyOffset(int beginPhyOffset) {
        this.beginPhyOffset = beginPhyOffset;
      }

      public int getEndPhyOffset() {
        return endPhyOffset;
      }

      public void setEndPhyOffset(int endPhyOffset) {
        this.endPhyOffset = endPhyOffset;
      }

      public long getBeginTimestamp() {
        return beginTimestamp;
      }

      public void setBeginTimestamp(long beginTimestamp) {
        this.beginTimestamp = beginTimestamp;
      }

      public long getEndTimestamp() {
        return endTimestamp;
      }

      public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
      }

      @Override
      public String toString() {
        return new StringBuilder() //
            .append("ClusterBlock{") //
            .append("blockIndex=") //
            .append(blockIndex) //
            .append(", componentCount=") //
            .append(componentCount) //
            .append(", beginPhyOffset=") //
            .append(beginPhyOffset) //
            .append(", endPhyOffset=") //
            .append(endPhyOffset) //
            .append(", beginTimestamp=") //
            .append(beginTimestamp) //
            .append(", endTimestamp=") //
            .append(endTimestamp) //
            .append('}') //
            .toString();
      }
    }

    @Override
    public String toString() {
      return new StringBuilder() //
          .append("ClusterDatabase{") //
          .append("sid=") //
          .append(sid) //
          .append(", blockList=") //
          .append(blockList) //
          .append('}') //
          .toString();
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ClusterListData{");
    builder.append("clusterRouteList=");
    builder.append(clusterRouteList);
    builder.append(", clusterDatabaseList=");
    builder.append(clusterDatabaseList);
    builder.append('}');
    return builder.toString();
  }
}
