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
package org.myberry.store.impl;

import java.nio.ByteBuffer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StoreHeaderTest {

  private static ByteBuffer byteBuffer;
  private static StoreHeader storeHeader;

  private static long beginTimestamp = System.currentTimeMillis();
  private static long endTimestamp = System.currentTimeMillis();
  private static int beginPhyOffset = 100;
  private static int endPhyOffset = 200;
  private static int mySid = 3;
  private static int componentCount = 111;

  @BeforeClass
  public static void init() {
    byteBuffer = ByteBuffer.allocate(StoreHeader.STORE_HEADER_SIZE);
    byteBuffer.putLong(beginTimestamp);
    byteBuffer.putLong(endTimestamp);
    byteBuffer.putInt(beginPhyOffset);
    byteBuffer.putInt(endPhyOffset);
    byteBuffer.putInt(mySid);
    byteBuffer.putInt(componentCount);
    storeHeader = new StoreHeader(byteBuffer);
  }

  @Test
  public void test_a() {
    storeHeader.load();

    Assert.assertEquals(beginTimestamp, storeHeader.getBeginTimestamp());
    Assert.assertEquals(endTimestamp, storeHeader.getEndTimestamp());
    Assert.assertEquals(beginPhyOffset, storeHeader.getBeginPhyOffset());
    Assert.assertEquals(endPhyOffset, storeHeader.getEndPhyOffset());
    Assert.assertEquals(mySid, storeHeader.getMySid());
    Assert.assertEquals(componentCount, storeHeader.getComponentCount());
  }

  @Test
  public void test_b() {
    storeHeader.incrComponentCount();
    Assert.assertEquals(componentCount + 1, storeHeader.getComponentCount());
  }
}
