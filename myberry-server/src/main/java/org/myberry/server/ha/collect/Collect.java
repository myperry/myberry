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

import java.util.List;
import org.myberry.common.codec.MessageLite;
import org.myberry.common.codec.annotation.SerialField;

public class Collect implements MessageLite {

  @SerialField(ordinal = 0)
  private int sid;

  @SerialField(ordinal = 1)
  private int leader;

  /*
   * route request
   */
  @SerialField(ordinal = 2)
  private NodeAddr nodeAddr;

  /*
   * route response
   */
  @SerialField(ordinal = 3)
  private List<NodeAddr> nodeAddrs;

  /*
   * block request
   */
  @SerialField(ordinal = 4)
  private NodeBlock nodeBlock;

  /*
   * block response
   */
  @SerialField(ordinal = 5)
  private List<NodeBlock> nodeBlocks;

  public int getSid() {
    return sid;
  }

  public void setSid(int sid) {
    this.sid = sid;
  }

  public int getLeader() {
    return leader;
  }

  public void setLeader(int leader) {
    this.leader = leader;
  }

  public NodeAddr getNodeAddr() {
    return nodeAddr;
  }

  public void setNodeAddr(NodeAddr nodeAddr) {
    this.nodeAddr = nodeAddr;
  }

  public List<NodeAddr> getNodeAddrs() {
    return nodeAddrs;
  }

  public void setNodeAddrs(List<NodeAddr> nodeAddrs) {
    this.nodeAddrs = nodeAddrs;
  }

  public NodeBlock getNodeBlock() {
    return nodeBlock;
  }

  public void setNodeBlock(NodeBlock nodeBlock) {
    this.nodeBlock = nodeBlock;
  }

  public List<NodeBlock> getNodeBlocks() {
    return nodeBlocks;
  }

  public void setNodeBlocks(List<NodeBlock> nodeBlocks) {
    this.nodeBlocks = nodeBlocks;
  }
}
