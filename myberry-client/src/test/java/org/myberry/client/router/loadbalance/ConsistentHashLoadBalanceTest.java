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
package org.myberry.client.router.loadbalance;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.myberry.common.loadbalance.Invoker;

public class ConsistentHashLoadBalanceTest {

  private static ConsistentHashLoadBalance loadBalance;

  @BeforeClass
  public static void setup() {
    loadBalance = new ConsistentHashLoadBalance();
  }

  @Test
  public void test1ConsistentHashLoadBalance() {
    Invoker invoker1 = new Invoker("192.168.1.1:8080", 4);
    Invoker invoker2 = new Invoker("192.168.1.2:8080", 7);
    Invoker invoker3 = new Invoker("192.168.1.3:8080", 11);
    List<Invoker> addrs = new ArrayList<>();
    addrs.add(invoker1);
    addrs.add(invoker2);
    addrs.add(invoker3);

    Assert.assertNotNull(loadBalance.doSelect(addrs, "key1"));
    Assert.assertNotNull(loadBalance.doSelect(addrs, "key1"));
    Assert.assertNotNull(loadBalance.doSelect(addrs, "key1"));
    Assert.assertNotNull(loadBalance.doSelect(addrs, "key1"));
    Assert.assertNotNull(loadBalance.doSelect(addrs, "key1"));
    Assert.assertNotNull(loadBalance.doSelect(addrs, "key1"));
    Assert.assertNotNull(loadBalance.doSelect(addrs, "key1"));
    Assert.assertNotNull(loadBalance.doSelect(addrs, "key1"));
    Assert.assertNotNull(loadBalance.doSelect(addrs, "key1"));
    Assert.assertNotNull(loadBalance.doSelect(addrs, "key1"));
  }

  @Test
  public void test2ConsistentHashLoadBalance() {
    Invoker invoker1 = new Invoker("192.168.1.1:8080", 4);
    Invoker invoker2 = new Invoker("192.168.1.2:8080", 7);
    Invoker invoker3 = new Invoker("192.168.1.3:8080", 11);
    List<Invoker> addrs = new ArrayList<>();
    addrs.add(invoker1);
    addrs.add(invoker2);
    addrs.add(invoker3);

    Assert.assertNotNull(loadBalance.doSelect(addrs, "key1"));
  }

  @Test
  public void test3ConsistentHashLoadBalance() {
    Invoker invoker1 = new Invoker("192.168.1.1:8080", 6);
    Invoker invoker2 = new Invoker("192.168.1.2:8080", 8);
    Invoker invoker3 = new Invoker("192.168.1.3:8080", 3);
    List<Invoker> addrs = new ArrayList<>();
    addrs.add(invoker1);
    addrs.add(invoker2);
    addrs.add(invoker3);

    Assert.assertNotNull(loadBalance.doSelect(addrs, "key1"));
    Assert.assertNotNull(loadBalance.doSelect(addrs, "key1"));
  }
}
