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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import org.myberry.common.loadbalance.Invoker;

public class ConsistentHashLoadBalance {

  private final AtomicReference<ConsistentHashSelector> consistentHashSelector =
      new AtomicReference<>();

  public Invoker doSelect(List<Invoker> invokers, String key) {
    int identityHashCode = System.identityHashCode(invokers);
    ConsistentHashSelector selector = consistentHashSelector.get();
    if (selector == null || selector.identityHashCode != identityHashCode) {
      selector = new ConsistentHashSelector(invokers, identityHashCode);
      consistentHashSelector.set(selector);
    }
    return selector.select(key);
  }

  private static final class ConsistentHashSelector {

    private final TreeMap<Long, Invoker> nodes;

    private final int identityHashCode;

    ConsistentHashSelector(List<Invoker> invokers, int identityHashCode) {
      this.nodes = new TreeMap<>();
      this.identityHashCode = identityHashCode;
      for (Invoker invoker : invokers) {
        int virtualNums = getVirtualNums(invoker.getWeight());
        for (int i = 0; i < virtualNums / 4; i++) {
          byte[] digest = md5(invoker.getAddr() + i);
          for (int h = 0; h < 4; h++) {
            long m = hash(digest, h);
            nodes.put(m, invoker);
          }
        }
      }
    }

    public Invoker select(String key) {
      byte[] digest = md5(key);
      return selectForKey(hash(digest, 0));
    }

    private Invoker selectForKey(long hash) {
      Long key = hash;
      SortedMap<Long, Invoker> tailMap = nodes.tailMap(key);
      if (tailMap.isEmpty()) {
        key = nodes.firstKey();
      } else {
        key = tailMap.firstKey();
      }
      return nodes.get(key);
    }

    private int getVirtualNums(int weight) {
      return weight < 160 ? 160 : weight;
    }

    private long hash(byte[] digest, int number) {
      return (((long) (digest[3 + number * 4] & 0xFF) << 24) //
              | ((long) (digest[2 + number * 4] & 0xFF) << 16) //
              | ((long) (digest[1 + number * 4] & 0xFF) << 8) //
              | (digest[number * 4] & 0xFF)) //
          & 0xFFFFFFFFL;
    }

    private byte[] md5(String value) {
      MessageDigest md5;
      try {
        md5 = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException e) {
        throw new IllegalStateException(e.getMessage(), e);
      }
      md5.reset();
      byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
      md5.update(bytes);
      return md5.digest();
    }
  }
}
