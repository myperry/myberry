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

public class NodeBlock implements MessageLite {

  @SerialField(ordinal = 0)
  private int sid;

  @SerialField(ordinal = 1)
  private List<BlockObject> blockObjectList;

  public int getSid() {
    return sid;
  }

  public void setSid(int sid) {
    this.sid = sid;
  }

  public List<BlockObject> getBlockObjectList() {
    return blockObjectList;
  }

  public void setBlockObjectList(List<BlockObject> blockObjectList) {
    this.blockObjectList = blockObjectList;
  }

  public static class BlockObject implements MessageLite {

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
          .append("BlockObject [") //
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
          .append(']') //
          .toString();
    }
  }

  @Override
  public String toString() {
    return new StringBuilder() //
        .append("NodeBlock [") //
        .append("sid=") //
        .append(sid) //
        .append(", blockObjectList=") //
        .append(blockObjectList) //
        .append(']') //
        .toString();
  }
}
