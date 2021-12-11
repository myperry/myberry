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
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.myberry.client.exception.MyberryRuntimeException;
import org.myberry.client.router.loadbalance.ConsistentHashLoadBalance;
import org.myberry.client.router.loadbalance.RoundRobinLoadBalance;
import org.myberry.common.MixAll;
import org.myberry.common.constant.LoggerName;
import org.myberry.common.loadbalance.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRouter {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.CLIENT_LOGGER_NAME);

  private static final RoundRobinLoadBalance roundRobin = new RoundRobinLoadBalance();
  private static final ConsistentHashLoadBalance consistentHash = new ConsistentHashLoadBalance();

  private final AtomicReference<String> maintainer = new AtomicReference<>();
  private final AtomicReference<List<Invoker>> invokers = new AtomicReference<>();

  private final AtomicReference<List<String>> backupSrv = new AtomicReference<>();
  private final AtomicInteger backupSrvIndex = new AtomicInteger(initValueIndex());

  private static int initValueIndex() {
    Random r = new Random();

    return Math.abs(r.nextInt() % 999) % 999;
  }

  public String getHeartbeatServerAddr(boolean latestHeartbeat) {
    if (latestHeartbeat) {
      return maintainer.get();
    } else {
      int index = this.backupSrvIndex.incrementAndGet();
      index = Math.abs(index);
      index = index % backupSrv.get().size();
      return backupSrv.get().get(index);
    }
  }

  public void setRouterInfoByHeartbeat(RouterInfo routerInfo) {
    boolean maintainerChange = false;
    if (routerInfo.getMaintainer() != null
        && !"".equals(routerInfo.getMaintainer())
        && !routerInfo.getMaintainer().equals(maintainer.get())) {
      maintainerChange = true;
      log.info(
          "remoting maintainer address updated. NEW : {} , OLD: {}",
          routerInfo.getMaintainer(),
          maintainer.get());
      maintainer.set(routerInfo.getMaintainer());
    }

    boolean invokerChange = false;
    if (routerInfo.getInvokers() != null && routerInfo.getInvokers().size() != 0) {
      if (null == invokers.get() || routerInfo.getInvokers().size() != invokers.get().size()) {
        invokerChange = true;
      } else {
        for (Invoker invokerRemote : routerInfo.getInvokers()) {
          for (Invoker invokerLocal : invokers.get()) {
            if (invokerRemote.equals(invokerLocal)) {
              invokerChange = false;
              break;
            } else {
              invokerChange = true;
            }
          }
          if (invokerChange) {
            break;
          }
        }
      }

      if (invokerChange) {
        log.info(
            "remoting invokers address updated. NEW : {} , OLD: {}",
            routerInfo.getInvokers(),
            invokers.get());
        invokers.set(routerInfo.getInvokers());
      }
    }

    boolean backupChange = maintainerChange | invokerChange;
    if (backupChange) {
      List<String> backup = new ArrayList<>();
      backup.add(routerInfo.getMaintainer());

      for (Invoker newInvoker : routerInfo.getInvokers()) {
        if (!backup.contains(newInvoker.getAddr())) {
          backup.add(newInvoker.getAddr());
        }
      }
      backupSrv.set(backup);
    }
  }

  public String getMaintainerAddr() {
    return maintainer.get();
  }

  public List<Invoker> getInvokers() {
    return invokers.get();
  }

  public Invoker getInvoker(List<Invoker> invokers) {
    if (null == invokers) {
      throw new MyberryRuntimeException("invokers is null");
    }

    return roundRobin.doSelect(invokers);
  }

  public Invoker getInvoker(List<Invoker> invokers, String sessionKey) {
    if (null == invokers) {
      throw new MyberryRuntimeException("invokers is null");
    }
    if (MixAll.isBlank(sessionKey)) {
      throw new MyberryRuntimeException("sessionKey is blank");
    }

    return consistentHash.doSelect(invokers, sessionKey);
  }
}
