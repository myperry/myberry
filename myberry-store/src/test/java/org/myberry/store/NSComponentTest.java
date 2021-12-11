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
package org.myberry.store;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.myberry.common.structure.Structure;

public class NSComponentTest {

  private static String key = "key2";
  private static int initNumber = 100;
  private static int stepSize = 5;
  private static int resetType = 1;

  private static NSComponent nsComponent;

  @BeforeClass
  public static void init() {
    byte[] keyLength = key.getBytes(StandardCharsets.UTF_8);

    nsComponent = new NSComponent();
    nsComponent.setComponentLength(
        (short) (NSComponent.COMPONENT_FIXED_FIELD_LENGTH + keyLength.length));
    nsComponent.setStatus((byte) 1);
    nsComponent.setPhyOffset(5);
    nsComponent.setCreateTime(System.currentTimeMillis());
    nsComponent.setUpdateTime(System.currentTimeMillis());
    nsComponent.setInitNumber(initNumber);
    nsComponent.setCurrentNumber(initNumber);
    nsComponent.setStepSize(stepSize);
    nsComponent.setResetType((byte) resetType);
    nsComponent.setKeyLength((short) keyLength.length);
    nsComponent.setKey(key);
  }

  @Test
  public void test() {
    Assert.assertEquals(Structure.NS, nsComponent.getStructure());
    Assert.assertEquals(1, nsComponent.getStatus());
    Assert.assertEquals(5, nsComponent.getPhyOffset());
    Assert.assertEquals(initNumber, nsComponent.getInitNumber());
    Assert.assertEquals(initNumber, nsComponent.getCurrentNumber().get());
    Assert.assertEquals(stepSize, nsComponent.getStepSize());
    Assert.assertEquals(resetType, nsComponent.getResetType());
    Assert.assertEquals(key, nsComponent.getKey());
  }

  @Ignore
  @Test
  public void test_CPUCacheLinePerformance() throws Exception {
    final long millis = System.currentTimeMillis();
    int count = 1_0000_0000;
    CountDownLatch countDownLatch = new CountDownLatch(2);
    Thread thread1 =
        new Thread(
            new Runnable() {
              @Override
              public void run() {
                for (int i = 0; i < count; i++) {
                  nsComponent.setUpdateTime(millis);
                  nsComponent.getCurrentNumber().getAndAdd(stepSize);
                }
                countDownLatch.countDown();
              }
            });

    Thread thread2 =
        new Thread(
            new Runnable() {
              @Override
              public void run() {
                for (int i = 0; i < count; i++) {
                  nsComponent.getStepSize();
                  nsComponent.getResetType();
                }
                countDownLatch.countDown();
              }
            });

    long start = System.nanoTime();
    thread1.start();
    thread2.start();
    countDownLatch.await();
    long end = System.nanoTime();

    System.out.println((end - start) / 10000);
  }
}
