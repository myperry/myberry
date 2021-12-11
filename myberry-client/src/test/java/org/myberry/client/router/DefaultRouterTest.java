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
package org.myberry.client.router;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.myberry.common.loadbalance.Invoker;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultRouterTest {

  private static DefaultRouter defaultRouter;

  @BeforeClass
  public static void setup() {
    defaultRouter = new DefaultRouter();
  }

  @Test
  public void a_testForGetHeartbeatServerAddr() {
    String heartbeatServerAddr = defaultRouter.getHeartbeatServerAddr(true);
    Assert.assertNull(heartbeatServerAddr);
  }

  @Test
  public void b_testForSetRouterInfoByHeartbeat() {
    List<Invoker> invokers = new ArrayList<>();
    invokers.add(new Invoker("127.0.0.1:8086", 1));
    invokers.add(new Invoker("127.0.0.1:8087", 2));

    RouterInfo routerInfo = new RouterInfo();
    routerInfo.setMaintainer("127.0.0.1:8085");
    routerInfo.setInvokers(invokers);

    defaultRouter.setRouterInfoByHeartbeat(routerInfo);
  }

  @Test
  public void c_testForGetMaintainerAddr() {
    String maintainerAddr = defaultRouter.getMaintainerAddr();
    Assert.assertEquals("127.0.0.1:8085", maintainerAddr);
  }

  @Test
  public void d_testForGetInvokerAddr() {
    Invoker invokerAddr1 = defaultRouter.getInvoker(defaultRouter.getInvokers());
    Assert.assertNotNull(invokerAddr1);
    Invoker invokerAddr2 = defaultRouter.getInvoker(defaultRouter.getInvokers());
    Assert.assertNotNull(invokerAddr2);
    Invoker invokerAddr3 = defaultRouter.getInvoker(defaultRouter.getInvokers(), "key1");
    Assert.assertNotNull(invokerAddr3);
    Invoker invokerAddr4 = defaultRouter.getInvoker(defaultRouter.getInvokers(), "key2");
    Assert.assertNotNull(invokerAddr4);
    Invoker invokerAddr5 = defaultRouter.getInvoker(defaultRouter.getInvokers(), "key3");
    Assert.assertNotNull(invokerAddr5);
    Invoker invokerAddr6 = defaultRouter.getInvoker(defaultRouter.getInvokers(), "key4");
    Assert.assertNotNull(invokerAddr6);
  }

  @Test
  public void e_testForGetHeartbeatServerAddr() {
    String heartbeatServerAddr = defaultRouter.getHeartbeatServerAddr(true);
    Assert.assertEquals("127.0.0.1:8085", heartbeatServerAddr);
    heartbeatServerAddr = defaultRouter.getHeartbeatServerAddr(false);
    Assert.assertNotNull(heartbeatServerAddr);
  }

  @Test
  public void f_testForMaintainerChange() throws Exception {
    List<Invoker> invokers = new ArrayList<>();
    invokers.add(new Invoker("127.0.0.1:8086", 1));
    invokers.add(new Invoker("127.0.0.1:8087", 2));

    RouterInfo routerInfo = new RouterInfo();
    routerInfo.setMaintainer("127.0.0.1:8089");
    routerInfo.setInvokers(invokers);

    defaultRouter.setRouterInfoByHeartbeat(routerInfo);
    String addr1 = defaultRouter.getMaintainerAddr();
    Assert.assertEquals("127.0.0.1:8089", addr1);
  }

  @Test
  public void g_testForInvokersChange() {
    // addr change
    List<Invoker> invokers = new ArrayList<>();
    invokers.add(new Invoker("127.0.0.1:8086", 1));
    invokers.add(new Invoker("127.0.0.1:8088", 2));

    RouterInfo routerInfo = new RouterInfo();
    routerInfo.setMaintainer("127.0.0.1:8089");
    routerInfo.setInvokers(invokers);
    defaultRouter.setRouterInfoByHeartbeat(routerInfo);

    List<Invoker> invokerList = defaultRouter.getInvokers();
    boolean updated = false;
    for (Invoker ink : invokerList) {
      if ("127.0.0.1:8088".equals(ink.getAddr())) {
        updated = true;
        break;
      }
    }

    Assert.assertEquals(true, updated);

    // weight change
    List<Invoker> invokers2 = new ArrayList<>();
    invokers2.add(new Invoker("127.0.0.1:8086", 5));
    invokers2.add(new Invoker("127.0.0.1:8088", 2));

    RouterInfo routerInfo2 = new RouterInfo();
    routerInfo2.setMaintainer("127.0.0.1:8089");
    routerInfo2.setInvokers(invokers2);
    defaultRouter.setRouterInfoByHeartbeat(routerInfo2);

    List<Invoker> invokerList2 = defaultRouter.getInvokers();
    boolean updated2 = false;
    for (Invoker ink : invokerList2) {
      if ("127.0.0.1:8086".equals(ink.getAddr()) && 5 == ink.getWeight()) {
        updated2 = true;
        break;
      }
    }

    Assert.assertEquals(true, updated2);
  }

  @Test
  public void h_testForBackupSrvChange() {
    String oldAddr = defaultRouter.getHeartbeatServerAddr(false);
    List<Invoker> invokers = new ArrayList<>();
    invokers.add(new Invoker("127.0.0.1:8083", 1));
    invokers.add(new Invoker("127.0.0.1:8084", 2));

    RouterInfo routerInfo = new RouterInfo();
    routerInfo.setMaintainer("127.0.0.1:8082");
    routerInfo.setInvokers(invokers);

    defaultRouter.setRouterInfoByHeartbeat(routerInfo);

    String newAddr = defaultRouter.getHeartbeatServerAddr(false);
    Assert.assertNotEquals(oldAddr, newAddr);
  }
}
