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
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.myberry.common.structure.Structure;
import org.myberry.store.CRComponent;
import org.myberry.store.NSComponent;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StoreComponentTest {

  private static StoreComponent storeComponent;

  @BeforeClass
  public static void init() {
    storeComponent = new StoreComponent(null);
  }

  @Test
  public void test_a() throws Exception {
    String key = "key1";
    String expression = "[#time(day) 2 3 #sid(0) #sid(1) m z #incr(0)]";
    byte[] keyLength = key.getBytes(StandardCharsets.UTF_8);
    byte[] expressionLength = expression.getBytes(StandardCharsets.UTF_8);
    int componentLength =
        (CRComponent.COMPONENT_FIXED_FIELD_LENGTH + keyLength.length + expressionLength.length);
    long millis = System.currentTimeMillis();

    CRComponent crComponent = new CRComponent();
    crComponent.setComponentLength((short) componentLength);
    crComponent.setStatus((byte) 1);
    crComponent.setPhyOffset(5);
    crComponent.setCreateTime(millis);
    crComponent.setUpdateTime(millis);
    crComponent.setIncrNumber(3L);
    crComponent.setKeyLength((short) keyLength.length);
    crComponent.setKey(key);
    crComponent.setExpressionLength((short) expressionLength.length);
    crComponent.setExpression(expression);
    crComponent.setBlockIndex(1);

    ByteBuffer byteBuffer = ByteBuffer.allocate(120);
    storeComponent.write(crComponent, byteBuffer);

    byteBuffer.flip();
    CRComponent crc = new CRComponent(1);
    storeComponent.load(crc, byteBuffer);

    Assert.assertEquals(componentLength, crc.getComponentLength());
    Assert.assertEquals(Structure.CR, crc.getStructure());
    Assert.assertEquals((byte) 1, crc.getStatus());
    Assert.assertEquals(5, crc.getPhyOffset());
    Assert.assertEquals(millis, crc.getCreateTime());
    Assert.assertEquals(millis, crc.getUpdateTime());
    Assert.assertEquals(3L, crc.getIncrNumber().get());
    Assert.assertEquals((short) keyLength.length, crc.getKeyLength());
    Assert.assertEquals(key, crc.getKey());
    Assert.assertEquals((short) expressionLength.length, crc.getExpressionLength());
    Assert.assertEquals(expression, crc.getExpression());
    Assert.assertEquals(1, crc.getBlockIndex());
  }

  @Test
  public void test_b() throws Exception {
    String key = "key2";
    byte[] keyLength = key.getBytes(StandardCharsets.UTF_8);
    int componentLength = (CRComponent.COMPONENT_FIXED_FIELD_LENGTH + keyLength.length);
    long millis = System.currentTimeMillis();

    NSComponent nsComponent = new NSComponent();
    nsComponent.setComponentLength((short) componentLength);
    nsComponent.setStatus((byte) 1);
    nsComponent.setPhyOffset(5);
    nsComponent.setCreateTime(millis);
    nsComponent.setUpdateTime(millis);
    nsComponent.setInitNumber(3);
    nsComponent.setCurrentNumber(7);
    nsComponent.setStepSize(4);
    nsComponent.setResetType((byte) 2);
    nsComponent.setKeyLength((short) keyLength.length);
    nsComponent.setKey(key);
    nsComponent.setBlockIndex(2);

    ByteBuffer byteBuffer = ByteBuffer.allocate(120);
    storeComponent.write(nsComponent, byteBuffer);

    byteBuffer.flip();
    NSComponent nsc = new NSComponent();
    nsc.setBlockIndex(2);
    storeComponent.load(nsc, byteBuffer);

    Assert.assertEquals(componentLength, nsc.getComponentLength());
    Assert.assertEquals(Structure.NS, nsc.getStructure());
    Assert.assertEquals((byte) 1, nsc.getStatus());
    Assert.assertEquals(5, nsc.getPhyOffset());
    Assert.assertEquals(millis, nsc.getCreateTime());
    Assert.assertEquals(millis, nsc.getUpdateTime());
    Assert.assertEquals(3, nsc.getInitNumber());
    Assert.assertEquals(7, nsc.getCurrentNumber().get());
    Assert.assertEquals(4, nsc.getStepSize());
    Assert.assertEquals((byte) 2, nsc.getResetType());
    Assert.assertEquals((short) keyLength.length, nsc.getKeyLength());
    Assert.assertEquals(key, nsc.getKey());
    Assert.assertEquals(2, nsc.getBlockIndex());
  }
}
