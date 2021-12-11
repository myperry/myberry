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

import org.myberry.common.codec.MessageLite;
import org.myberry.common.codec.annotation.SerialField;

public class NodeAddr implements MessageLite, Comparable<NodeAddr> {

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
  public int compareTo(NodeAddr o) {
    if (this.sid > o.getSid()) {
      return 1;
    } else {
      return -1;
    }
  }

  @Override
  public String toString() {
    return new StringBuilder() //
        .append("NodeAddr [") //
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
        .append(']') //
        .toString();
  }
}
