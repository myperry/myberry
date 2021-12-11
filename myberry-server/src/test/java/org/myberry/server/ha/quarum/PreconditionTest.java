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

public class PreconditionTest {

  @Test
  public void testSame() {
    Member member1 = new Member();
    member1.setIp("192.168.1.2");
    member1.setHaPort(10737);
    member1.setSid(1);
    Member member2 = new Member();
    member2.setIp("192.168.1.2");
    member2.setHaPort(10747);
    member2.setSid(2);
    Member[] members1 = new Member[2];
    members1[0] = member1;
    members1[1] = member2;
    Precondition mine = new Precondition();
    mine.setMembers(members1);

    Member member3 = new Member();
    member3.setIp("192.168.1.2");
    member3.setHaPort(10737);
    member3.setSid(1);
    Member member4 = new Member();
    member4.setIp("192.168.1.2");
    member4.setHaPort(10747);
    member4.setSid(2);
    Member[] members2 = new Member[2];
    members2[0] = member3;
    members2[1] = member4;
    Precondition another = new Precondition();
    another.setMembers(members2);

    boolean accepted = Precondition.isAccepted(mine, another);

    Assert.assertEquals(true, accepted);
  }

  @Test
  public void testAccepted1() {
    Member member1 = new Member();
    member1.setIp("192.168.1.2");
    member1.setHaPort(10737);
    member1.setSid(1);
    Member member2 = new Member();
    member2.setIp("192.168.1.2");
    member2.setHaPort(10747);
    member2.setSid(2);
    Member member3 = new Member();
    member3.setIp("192.168.1.2");
    member3.setHaPort(10757);
    member3.setSid(3);
    Member[] members1 = new Member[3];
    members1[0] = member1;
    members1[1] = member2;
    members1[2] = member3;
    Precondition mine = new Precondition();
    mine.setMembers(members1);

    Member member4 = new Member();
    member4.setIp("192.168.1.2");
    member4.setHaPort(10737);
    member4.setSid(1);
    Member member5 = new Member();
    member5.setIp("192.168.1.2");
    member5.setHaPort(10747);
    member5.setSid(2);
    Member[] members2 = new Member[2];
    members2[0] = member4;
    members2[1] = member5;
    Precondition another = new Precondition();
    another.setMembers(members2);

    boolean accepted = Precondition.isAccepted(mine, another);

    Assert.assertEquals(true, accepted);
  }

  @Test
  public void testAccepted2() {
    Member member1 = new Member();
    member1.setIp("192.168.1.2");
    member1.setHaPort(10737);
    member1.setSid(1);
    Member member2 = new Member();
    member2.setIp("192.168.1.2");
    member2.setHaPort(10747);
    member2.setSid(2);
    Member[] members1 = new Member[2];
    members1[0] = member1;
    members1[1] = member2;
    Precondition mine = new Precondition();
    mine.setMembers(members1);

    Member member3 = new Member();
    member3.setIp("192.168.1.2");
    member3.setHaPort(10737);
    member3.setSid(1);
    Member member4 = new Member();
    member4.setIp("192.168.1.2");
    member4.setHaPort(10747);
    member4.setSid(2);
    Member member5 = new Member();
    member5.setIp("192.168.1.2");
    member5.setHaPort(10757);
    member5.setSid(3);
    Member[] members2 = new Member[3];
    members2[0] = member3;
    members2[1] = member4;
    members2[2] = member5;
    Precondition another = new Precondition();
    another.setMembers(members2);

    boolean accepted = Precondition.isAccepted(mine, another);

    Assert.assertEquals(true, accepted);
  }

  @Test
  public void testNotSame() {
    Member member1 = new Member();
    member1.setIp("192.168.1.2");
    member1.setHaPort(10737);
    member1.setSid(1);
    Member member2 = new Member();
    member2.setIp("192.168.1.2");
    member2.setHaPort(10747);
    member2.setSid(2);
    Member[] members1 = new Member[2];
    members1[0] = member1;
    members1[1] = member2;
    Precondition mine = new Precondition();
    mine.setMembers(members1);

    Member member3 = new Member();
    member3.setIp("192.168.1.2");
    member3.setHaPort(10737);
    member3.setSid(1);
    Member member4 = new Member();
    member4.setIp("192.168.1.2");
    member4.setHaPort(10747);
    member4.setSid(2);
    Member member5 = new Member();
    member5.setIp("192.168.1.2");
    member5.setHaPort(10757);
    member5.setSid(3);
    Member member6 = new Member();
    member6.setIp("192.168.1.2");
    member6.setHaPort(10767);
    member6.setSid(4);
    Member[] members2 = new Member[4];
    members2[0] = member3;
    members2[1] = member4;
    members2[2] = member5;
    members2[3] = member6;
    Precondition another = new Precondition();
    another.setMembers(members2);

    boolean accepted = Precondition.isAccepted(mine, another);

    Assert.assertEquals(false, accepted);
  }
}
