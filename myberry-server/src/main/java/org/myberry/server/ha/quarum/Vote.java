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
package org.myberry.server.ha.quarum;

import org.myberry.common.codec.MessageLite;
import org.myberry.common.codec.annotation.SerialField;
import org.myberry.server.ha.HAState;

public class Vote implements MessageLite {

  @SerialField(ordinal = 0)
  private Precondition cond;

  /*
   * Proposed leader
   */
  @SerialField(ordinal = 1)
  private int leader;

  /*
   * Logic offset of the proposed leader
   */
  @SerialField(ordinal = 2)
  private long offset;

  /*
   * Epoch of the proposed leader
   */
  @SerialField(ordinal = 3)
  private long peerEpoch;

  /*
   * Epoch of the proposer
   */
  @SerialField(ordinal = 4)
  private long electEpoch;

  /*
   * Current state of the proposer
   */
  @SerialField(ordinal = 5)
  private int haState;

  /*
   * Sid of the proposer
   */
  @SerialField(ordinal = 6)
  private int sid;

  public Vote() {}

  public Vote(int leader, long offset, long peerEpoch, long electEpoch) {
    this.leader = leader;
    this.offset = offset;
    this.peerEpoch = peerEpoch;
    this.electEpoch = electEpoch;
  }

  public Vote(int leader, long offset, long peerEpoch, long electEpoch, int haState) {
    this.leader = leader;
    this.offset = offset;
    this.peerEpoch = peerEpoch;
    this.electEpoch = electEpoch;
    this.haState = haState;
  }

  public Vote(
      Precondition cond,
      int leader,
      long offset,
      long peerEpoch,
      long electEpoch,
      int haState,
      int sid) {
    this.cond = cond;
    this.leader = leader;
    this.offset = offset;
    this.peerEpoch = peerEpoch;
    this.electEpoch = electEpoch;
    this.haState = haState;
    this.sid = sid;
  }

  public Precondition getCond() {
    return cond;
  }

  public void setCond(Precondition cond) {
    this.cond = cond;
  }

  public int getLeader() {
    return leader;
  }

  public void setLeader(int leader) {
    this.leader = leader;
  }

  public long getOffset() {
    return offset;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public long getPeerEpoch() {
    return peerEpoch;
  }

  public void setPeerEpoch(long peerEpoch) {
    this.peerEpoch = peerEpoch;
  }

  public long getElectEpoch() {
    return electEpoch;
  }

  public void setElectEpoch(long electEpoch) {
    this.electEpoch = electEpoch;
  }

  public int getHaState() {
    return haState;
  }

  public void setHaState(int haState) {
    this.haState = haState;
  }

  public int getSid() {
    return sid;
  }

  public void setSid(int sid) {
    this.sid = sid;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Vote)) {
      return false;
    }
    Vote other = (Vote) o;

    if ((haState == HAState.LOOKING.getCode())
        || (other.getHaState() == HAState.LOOKING.getCode())) {
      return (leader == other.getLeader()
          && offset == other.getOffset()
          && electEpoch == other.getElectEpoch()
          && peerEpoch == other.getPeerEpoch());
    } else {
      return (leader == other.getLeader() && peerEpoch == other.getPeerEpoch());
    }
  }

  @Override
  public int hashCode() {
    return (int) (leader & offset);
  }

  @Override
  public String toString() {
    return new StringBuilder() //
        .append("Vote [") //
        .append("cond=") //
        .append(cond) //
        .append(", leader=") //
        .append(leader) //
        .append(", offset=") //
        .append(offset) //
        .append(", peerEpoch=") //
        .append(peerEpoch) //
        .append(", electEpoch=") //
        .append(electEpoch) //
        .append(", haState=") //
        .append(haState) //
        .append(", sid=") //
        .append(sid) //
        .append(']') //
        .toString();
  }
}
