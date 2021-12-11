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
package org.myberry.common.codec;

import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Test;
import org.myberry.common.codec.util.ElasticBuffer;

public class ElasticBufferTest {

  @Test
  public void testByte() {
    ElasticBuffer elasticBuffer = ElasticBuffer.getOrCreateElasticBuffer(0);
    byte i = 99;
    elasticBuffer.put(i);
    elasticBuffer.flip();
    byte b = elasticBuffer.get();
    Assert.assertEquals(i, b);

    elasticBuffer.clear();

    i = -101;
    elasticBuffer.put(1, i);
    elasticBuffer.position(2);
    elasticBuffer.flip();
    b = elasticBuffer.get(1);
    Assert.assertEquals(i, b);
  }

  @Test
  public void testChar() {
    ElasticBuffer elasticBuffer = ElasticBuffer.getOrCreateElasticBuffer(0);
    char i = 'a';
    elasticBuffer.putChar(i);
    elasticBuffer.flip();
    char b = elasticBuffer.getChar();
    Assert.assertEquals(i, b);

    elasticBuffer.clear();

    i = 'Z';
    elasticBuffer.putChar(2, i);
    elasticBuffer.position(4);
    elasticBuffer.flip();
    b = elasticBuffer.getChar(2);
    Assert.assertEquals(i, b);
  }

  @Test
  public void testShort() {
    ElasticBuffer elasticBuffer = ElasticBuffer.getOrCreateElasticBuffer(0);
    short i = 12;
    elasticBuffer.putShort(i);
    elasticBuffer.flip();
    short b = elasticBuffer.getShort();
    Assert.assertEquals(i, b);

    elasticBuffer.clear();

    i = 'Z';
    elasticBuffer.putShort(2, i);
    elasticBuffer.position(4);
    elasticBuffer.flip();
    b = elasticBuffer.getShort(2);
    Assert.assertEquals(i, b);
  }

  @Test
  public void testInt() {
    ElasticBuffer elasticBuffer = ElasticBuffer.getOrCreateElasticBuffer(0);
    int i = 123;
    elasticBuffer.putInt(i);
    elasticBuffer.flip();
    int b = elasticBuffer.getInt();
    Assert.assertEquals(i, b);

    elasticBuffer.clear();

    i = 234;
    elasticBuffer.putInt(4, i);
    elasticBuffer.position(8);
    elasticBuffer.flip();
    b = elasticBuffer.getInt(4);
    Assert.assertEquals(i, b);
  }

  @Test
  public void testFloat() {
    ElasticBuffer elasticBuffer = ElasticBuffer.getOrCreateElasticBuffer(0);
    float i = 3.141592f;
    elasticBuffer.putFloat(i);
    elasticBuffer.flip();
    float b = elasticBuffer.getFloat();
    Assert.assertEquals(i, b, 0.0f);

    elasticBuffer.clear();

    i = 3.141593f;
    elasticBuffer.putFloat(4, i);
    elasticBuffer.position(8);
    elasticBuffer.flip();
    b = elasticBuffer.getFloat(4);
    Assert.assertEquals(i, b, 0.0f);
  }

  @Test
  public void testLong() {
    ElasticBuffer elasticBuffer = ElasticBuffer.getOrCreateElasticBuffer(0);
    long i = 10000;
    elasticBuffer.putLong(i);
    elasticBuffer.flip();
    long b = elasticBuffer.getLong();
    Assert.assertEquals(i, b);

    elasticBuffer.clear();

    i = 20000;
    elasticBuffer.putLong(8, i);
    elasticBuffer.position(16);
    elasticBuffer.flip();
    b = elasticBuffer.getLong(8);
    Assert.assertEquals(i, b);
  }

  @Test
  public void testDouble() {
    ElasticBuffer elasticBuffer = ElasticBuffer.getOrCreateElasticBuffer(0);
    double i = 3.1415926;
    elasticBuffer.putDouble(i);
    elasticBuffer.flip();
    double b = elasticBuffer.getDouble();
    Assert.assertEquals(i, b, 0.0d);

    elasticBuffer.clear();

    i = 3.1415927;
    elasticBuffer.putDouble(8, i);
    elasticBuffer.position(16);
    elasticBuffer.flip();
    b = elasticBuffer.getDouble(8);
    Assert.assertEquals(i, b, 0.0d);
  }

  @Test
  public void testArray() {
    ElasticBuffer elasticBuffer = ElasticBuffer.getOrCreateElasticBuffer(0);
    String src1 = "春节";
    byte[] i = src1.getBytes(StandardCharsets.UTF_8);
    elasticBuffer.putArray(i);
    elasticBuffer.flip();
    byte[] b = elasticBuffer.getArray(i.length);
    Assert.assertEquals(src1, new String(b, StandardCharsets.UTF_8));

    elasticBuffer.clear();

    String src2 = "元宵节";
    byte[] j = src2.getBytes(StandardCharsets.UTF_8);
    elasticBuffer.putArray(i.length, j);
    elasticBuffer.position(i.length + j.length);
    elasticBuffer.flip();
    b = elasticBuffer.getArray(i.length, j.length);
    Assert.assertEquals(src2, new String(b, StandardCharsets.UTF_8));
  }

  @Test
  public void testExpand() {
    ElasticBuffer elasticBuffer = ElasticBuffer.getOrCreateElasticBuffer(0);
    Assert.assertEquals(1024, elasticBuffer.capacity());
    for (int i = 0; i < 1025; i += 4) {
      elasticBuffer.putInt(5);
    }
    int anInt = elasticBuffer.getInt(1024);
    Assert.assertEquals(5, anInt);

    Assert.assertEquals(1540, elasticBuffer.capacity());
  }
}
