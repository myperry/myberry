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
package org.myberry.server.ha.database;

import org.myberry.common.codec.MessageLite;
import org.myberry.common.codec.annotation.SerialField;

public class Database implements MessageLite {

  @SerialField(ordinal = 0)
  private int cmd;

  @SerialField(ordinal = 1)
  private int sid;

  @SerialField(ordinal = 2)
  private int leader;

  /*
   * for checksum
   */
  @SerialField(ordinal = 3)
  private ApplyForBlockHeader[] applyForBlockHeaderList;

  /*
   * for append
   */
  @SerialField(ordinal = 4)
  private ApplyForBlockAppend applyForBlockAppend;

  public int getCmd() {
    return cmd;
  }

  public void setCmd(int cmd) {
    this.cmd = cmd;
  }

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

  public ApplyForBlockHeader[] getApplyForBlockHeaderList() {
    return applyForBlockHeaderList;
  }

  public void setApplyForBlockHeaderList(ApplyForBlockHeader[] applyForBlockHeaderList) {
    this.applyForBlockHeaderList = applyForBlockHeaderList;
  }

  public ApplyForBlockAppend getApplyForBlockAppend() {
    return applyForBlockAppend;
  }

  public void setApplyForBlockAppend(ApplyForBlockAppend applyForBlockAppend) {
    this.applyForBlockAppend = applyForBlockAppend;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Database ["); //
    builder.append("cmd="); //
    builder.append(cmd); //
    builder.append(", sid="); //
    builder.append(sid); //
    builder.append(", leader="); //
    builder.append(leader); //
    builder.append(", applyForBlockHeaderList={"); //

    if (null != applyForBlockHeaderList) {
      for (int i = 0; i < applyForBlockHeaderList.length; i++) {
        builder.append(applyForBlockHeaderList[i]);
        if (i < applyForBlockHeaderList.length - 1) {
          builder.append(", ");
        }
      }
    }

    builder.append("}, applyForBlockAppend="); //
    builder.append(applyForBlockAppend); //
    builder.append(']'); //
    return builder.toString();
  }
}
