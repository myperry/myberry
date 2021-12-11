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
package org.myberry.client.admin;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.myberry.client.exception.MyberryClientException;
import org.myberry.client.exception.MyberryServerException;
import org.myberry.common.protocol.body.admin.CRComponentData;
import org.myberry.common.protocol.body.admin.ClusterListData;
import org.myberry.common.protocol.body.admin.ClusterListData.ClusterRoute;
import org.myberry.common.protocol.body.admin.NSComponentData;
import org.myberry.common.strategy.StrategyDate;
import org.myberry.remoting.exception.RemotingException;

@Ignore
public class AdminClientTest {

  private static DefaultAdminClient defaultAdminClient;

  @BeforeClass
  public static void setup() throws MyberryClientException {
    defaultAdminClient = new DefaultAdminClient();
    defaultAdminClient.setPassword("foobared");
    defaultAdminClient.setServerAddr("192.168.1.2:8085,192.168.1.2:8086,192.168.1.2:8087");
    defaultAdminClient.start();
  }

  @Test
  public void createComponentForCR()
      throws RemotingException, InterruptedException, MyberryServerException {
    CRComponentData cr = new CRComponentData();
    cr.setKey("key1");
    cr.setExpression(
        "[#time(day) 9 #sid(2) #sid(1) #sid(0) m #incr(4) #incr(3) #incr(2) #incr(1) #incr(0) $dynamic(hello) #rand(3)]");

    SendResult sendResult = defaultAdminClient.createComponent(cr);
    assertEquals(SendStatus.SEND_OK, sendResult.getSendStatus());
  }

  @Test
  public void createComponentForNS()
      throws RemotingException, InterruptedException, MyberryServerException {
    NSComponentData ns = new NSComponentData();
    ns.setKey("key2");
    ns.setInitNumber(100);
    ns.setStepSize(5);
    ns.setResetType(StrategyDate.TIME_DAY);

    SendResult sendResult = defaultAdminClient.createComponent(ns);
    assertEquals(SendStatus.SEND_OK, sendResult.getSendStatus());
  }

  @Test
  public void queryComponentSize()
      throws RemotingException, InterruptedException, MyberryServerException {
    SendResult sendResult = defaultAdminClient.queryComponentSize();
    assertEquals(1, sendResult.getSize().intValue());
  }

  @Test
  public void queryComponentByKeyForCR()
      throws RemotingException, InterruptedException, MyberryServerException {
    SendResult sendResult = defaultAdminClient.queryComponentByKey("key1");
    CRComponentData crcd = (CRComponentData) sendResult.getComponent();
    assertEquals(
        "[#time(day) 9 #sid(2) #sid(1) #sid(0) m #incr(4) #incr(3) #incr(2) #incr(1) #incr(0) $dynamic(hello) #rand(3)]",
        crcd.getExpression());
  }

  @Test
  public void queryComponentByKeyForNS()
      throws RemotingException, InterruptedException, MyberryServerException {
    SendResult sendResult = defaultAdminClient.queryComponentByKey("key2");
    NSComponentData nscd = (NSComponentData) sendResult.getComponent();
    assertEquals(100, nscd.getInitNumber());
    assertEquals(5, nscd.getStepSize());
    assertEquals(StrategyDate.TIME_DAY, nscd.getResetType());
  }

  @Test
  public void queryClusterList()
      throws RemotingException, InterruptedException, MyberryServerException {
    SendResult sendResult = defaultAdminClient.queryClusterList();
    ClusterListData clusterList = sendResult.getClusterList();
    List<ClusterRoute> clusterRouteList = clusterList.getClusterRouteList();
    assertEquals(3, clusterRouteList.size());
  }

  @AfterClass
  public static void close() {
    defaultAdminClient.shutdown();
  }
}
