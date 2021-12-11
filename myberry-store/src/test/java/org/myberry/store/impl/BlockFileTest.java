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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.myberry.common.structure.Structure;
import org.myberry.store.AbstractComponent;
import org.myberry.store.CRComponent;
import org.myberry.store.NSComponent;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BlockFileTest {

  private static int fileSize = 1024;
  private static String testPath = new File("").getAbsolutePath();
  private static String base_dir = testPath + File.separator + ".myberry";
  private static String defaultFileName =
      base_dir + File.separator + "store" + File.separator + "myberry-0";

  private static ConcurrentMap<String /* key */, AbstractComponent> componentMap =
      new ConcurrentHashMap<>();
  private static ConcurrentMap<Integer /* blockIndex */, CopyOnWriteArrayList<String /* key */>>
      syncDataMap = new ConcurrentHashMap<>();

  private static BlockFile blockFile;

  @BeforeClass
  public static void init() throws IOException {
    blockFile = new BlockFile(defaultFileName, fileSize, true, 0, 0, componentMap, syncDataMap);
    blockFile.loadHeader();
  }

  @Test
  public void test_a() {
    String key = "key1";
    String expression = "[#time(day) 2 3 #sid(0) #sid(1) m z #incr(0)]";
    byte[] keyLength = key.getBytes(StandardCharsets.UTF_8);
    byte[] expressionLength = expression.getBytes(StandardCharsets.UTF_8);
    CRComponent crComponent = new CRComponent();
    crComponent.setComponentLength(
        (short)
            (CRComponent.COMPONENT_FIXED_FIELD_LENGTH
                + keyLength.length
                + expressionLength.length));
    crComponent.setStatus((byte) 1);
    crComponent.setCreateTime(1620061126000L);
    crComponent.setUpdateTime(1620061126000L);
    crComponent.setIncrNumber(3L);
    crComponent.setKeyLength((short) keyLength.length);
    crComponent.setKey(key);
    crComponent.setExpressionLength((short) expressionLength.length);
    crComponent.setExpression(expression);
    blockFile.addComponent(crComponent);

    Assert.assertEquals(1, blockFile.getComponentCount());
  }

  @Test
  public void test_b() {
    String key = "key2";
    int incrNumber = 100;
    int stepSize = 5;
    int resetType = 1;
    byte[] keyLength = key.getBytes(StandardCharsets.UTF_8);

    NSComponent nsComponent = new NSComponent();
    nsComponent.setComponentLength(
        (short) (NSComponent.COMPONENT_FIXED_FIELD_LENGTH + keyLength.length));
    nsComponent.setStatus((byte) 1);
    nsComponent.setCreateTime(1620061126000L);
    nsComponent.setUpdateTime(1620061126000L);
    nsComponent.setInitNumber(incrNumber);
    nsComponent.setCurrentNumber(incrNumber);
    nsComponent.setStepSize(stepSize);
    nsComponent.setResetType((byte) resetType);
    nsComponent.setKeyLength((short) keyLength.length);
    nsComponent.setKey(key);
    blockFile.addComponent(nsComponent);

    Assert.assertEquals(2, blockFile.getComponentCount());
  }

  @Test
  public void test_c() {
    String key = "key1";
    String expression = "[#time(day) 2 3 #sid(0) #sid(1) m z #incr(0)]";
    byte[] keyLength = key.getBytes(StandardCharsets.UTF_8);
    byte[] expressionLength = expression.getBytes(StandardCharsets.UTF_8);

    CRComponent crc = (CRComponent) componentMap.get(key);

    Assert.assertEquals(Structure.CR, crc.getStructure());
    Assert.assertEquals(
        (short)
            (CRComponent.COMPONENT_FIXED_FIELD_LENGTH + keyLength.length + expressionLength.length),
        crc.getComponentLength());
    Assert.assertEquals((byte) 1, crc.getStatus());
    Assert.assertEquals(32, crc.getPhyOffset());
    Assert.assertEquals(1620061126000L, crc.getCreateTime());
    Assert.assertEquals(1620061126000L, crc.getUpdateTime());
    Assert.assertEquals(3L, crc.getIncrNumber().get());
    Assert.assertEquals((short) keyLength.length, crc.getKeyLength());
    Assert.assertEquals(key, crc.getKey());
    Assert.assertEquals((short) expressionLength.length, crc.getExpressionLength());
    Assert.assertEquals(expression, crc.getExpression());

    Assert.assertEquals(0, crc.getBlockIndex());
  }

  @Test
  public void test_d() {
    String key = "key2";
    int initNumber = 100;
    int stepSize = 5;
    int resetType = 1;
    byte[] keyLength = key.getBytes(StandardCharsets.UTF_8);

    NSComponent nsc = (NSComponent) componentMap.get(key);

    Assert.assertEquals(Structure.NS, nsc.getStructure());
    Assert.assertEquals(
        (short) (NSComponent.COMPONENT_FIXED_FIELD_LENGTH + keyLength.length),
        nsc.getComponentLength());
    Assert.assertEquals((byte) 1, nsc.getStatus());
    Assert.assertEquals(117, nsc.getPhyOffset());
    Assert.assertEquals(1620061126000L, nsc.getCreateTime());
    Assert.assertEquals(1620061126000L, nsc.getUpdateTime());
    Assert.assertEquals(initNumber, nsc.getInitNumber());
    Assert.assertEquals(initNumber, nsc.getCurrentNumber().get());
    Assert.assertEquals(stepSize, nsc.getStepSize());
    Assert.assertEquals(resetType, nsc.getResetType());
    Assert.assertEquals((short) keyLength.length, nsc.getKeyLength());
    Assert.assertEquals(key, nsc.getKey());

    Assert.assertEquals(0, nsc.getBlockIndex());
  }

  @Test
  public void test_e() {
    blockFile.loadHeader();
    componentMap.clear();
    blockFile.loadComponent();

    CRComponent crc = (CRComponent) componentMap.get("key1");
    Assert.assertEquals(3L, crc.getIncrNumber().get());
    NSComponent nsc = (NSComponent) componentMap.get("key2");
    Assert.assertEquals(100, nsc.getInitNumber());
    Assert.assertEquals(100, nsc.getCurrentNumber().get());

    Assert.assertEquals(2, blockFile.getComponentCount());
  }

  @Test
  public void test_f() {
    int mySid = blockFile.getMySid();
    Assert.assertEquals(0, mySid);

    int blockIndex = blockFile.getBlockIndex();
    Assert.assertEquals(0, blockIndex);

    int componentCount = blockFile.getComponentCount();
    Assert.assertEquals(2, componentCount);

    int endPhyOffset = blockFile.getEndPhyOffset();
    Assert.assertEquals(160, endPhyOffset);

    int lastPosition = blockFile.getLastPosition();
    Assert.assertEquals(160, lastPosition);
  }

  @Test
  public void test_g() {
    int componentLengthByOffset = blockFile.getComponentLengthByOffset(32);
    Assert.assertEquals(85, componentLengthByOffset);

    byte[] syncData = blockFile.getSyncData(32, 85);
    List<AbstractComponent> abstractComponents =
        BlockFile.parseComponent(ByteBuffer.wrap(syncData));
    Assert.assertEquals(1, abstractComponents.size());

    componentLengthByOffset = blockFile.getComponentLengthByOffset(117);
    Assert.assertEquals(43, componentLengthByOffset);

    syncData = blockFile.getSyncData(117, 43);
    abstractComponents = BlockFile.parseComponent(ByteBuffer.wrap(syncData));
    Assert.assertEquals(1, abstractComponents.size());
  }

  @AfterClass
  public static void destroy() {
    blockFile.unload();
    delFile(new File(base_dir));
  }

  public static void delFile(File file) {
    if (file.exists()) {
      if (file.isDirectory()) {
        File[] files = file.listFiles();
        if (files.length > 0) {
          for (File file1 : files) {
            if (file1.isFile()) {
              file1.delete();
            } else {
              delFile(file1);
            }
          }
          file.delete();
        } else {
          file.delete();
        }
      } else {
        file.delete();
      }
    }
  }
}
