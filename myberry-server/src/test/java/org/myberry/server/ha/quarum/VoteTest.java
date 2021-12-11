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

import org.junit.Assert;
import org.junit.Test;
import org.myberry.common.codec.LightCodec;
import org.myberry.server.ha.HAState;

public class VoteTest {

  @Test
  public void test() {
    Member member1 = new Member();
    member1.setIp("192.168.1.2");
    member1.setHaPort(10737);
    member1.setSid(1);
    Member member2 = new Member();
    member2.setIp("192.168.1.2");
    member2.setHaPort(10747);
    member2.setSid(2);
    Member[] members = new Member[2];
    members[0] = member1;
    members[1] = member2;
    Precondition mine = new Precondition();
    mine.setMembers(members);

    Vote vote = new Vote();
    vote.setCond(mine);
    vote.setLeader(1);
    vote.setOffset(100L);
    vote.setPeerEpoch(3L);
    vote.setElectEpoch(4L);
    vote.setHaState(HAState.LEADING.getCode());
    vote.setSid(2);

    byte[] bytes = LightCodec.toBytes(vote);
    Vote v = LightCodec.toObj(bytes, Vote.class);

    Assert.assertEquals(mine.getMembers()[0], v.getCond().getMembers()[0]);
    Assert.assertEquals(mine.getMembers()[1], v.getCond().getMembers()[1]);
    Assert.assertEquals(vote.getLeader(), v.getLeader());
    Assert.assertEquals(vote.getOffset(), v.getOffset());
    Assert.assertEquals(vote.getPeerEpoch(), v.getPeerEpoch());
    Assert.assertEquals(vote.getElectEpoch(), v.getElectEpoch());
    Assert.assertEquals(vote.getHaState(), v.getHaState());
    Assert.assertEquals(vote.getSid(), v.getSid());
  }
}
