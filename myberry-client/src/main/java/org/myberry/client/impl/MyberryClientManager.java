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
package org.myberry.client.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.myberry.client.AbstractMyberryClient;
import org.myberry.client.impl.factory.MyberryClientInstance;
import org.myberry.common.constant.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyberryClientManager {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.CLIENT_LOGGER_NAME);
  private AtomicInteger factoryIndexGenerator = new AtomicInteger();
  private ConcurrentMap<String /* clientId */, MyberryClientInstance> factoryTable = //
      new ConcurrentHashMap<String, MyberryClientInstance>();

  private static class SingletonHolder {
    private static final MyberryClientManager INSTANCE = new MyberryClientManager();
  }

  private MyberryClientManager() {}

  public static final MyberryClientManager getInstance() {
    return SingletonHolder.INSTANCE;
  }

  public MyberryClientInstance getAndCreateClientInstance(
      final AbstractMyberryClient abstractMyberryClient) {
    String clientId = abstractMyberryClient.buildClientId();
    MyberryClientInstance instance = this.factoryTable.get(clientId);
    if (null == instance) {
      instance =
          new MyberryClientInstance(
              abstractMyberryClient, this.factoryIndexGenerator.getAndIncrement(), clientId);
      MyberryClientInstance prev = this.factoryTable.putIfAbsent(clientId, instance);
      if (prev != null) {
        instance = prev;
        log.warn("Returned Previous MyberryClientInstance for clientId:[{}]", clientId);
      } else {
        log.info("Created new MyberryClientInstance for clientId:[{}]", clientId);
      }
    }

    return instance;
  }

  public void removeClientFactory(final String clientId) {
    this.factoryTable.remove(clientId);
  }
}
