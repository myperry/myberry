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
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.myberry.store.AbstractComponent;
import org.myberry.store.BlockHeader;
import org.myberry.store.CRComponent;
import org.myberry.store.NSComponent;
import org.myberry.store.config.StoreConfig;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FileServiceTest {

  private static FileService fileService;

  @BeforeClass
  public static void init() throws IllegalAccessException {
    StoreConfig storeConfig = new StoreConfig();
    storeConfig.setStorePath(new File("").getAbsolutePath());
    Field[] fields = storeConfig.getClass().getDeclaredFields();
    for (Field field : fields) {
      if ("blockFileSize".equals(field.getName())) {
        field.setAccessible(true);
        field.set(storeConfig, 200);
      }
    }

    fileService = new FileService(storeConfig);
  }

  @Test
  public void test_a() {
    String key1 = "key1";
    String expression1 = "[#time(day) 2 3 #sid(0) #sid(1) m z #incr(0)]";
    byte[] keyLength1 = key1.getBytes(StandardCharsets.UTF_8);
    byte[] expressionLength1 = expression1.getBytes(StandardCharsets.UTF_8);
    CRComponent crComponent1 = new CRComponent();
    crComponent1.setComponentLength(
        (short)
            (CRComponent.COMPONENT_FIXED_FIELD_LENGTH
                + keyLength1.length
                + expressionLength1.length));
    crComponent1.setStatus((byte) 1);
    crComponent1.setCreateTime(1620061126000L);
    crComponent1.setUpdateTime(1620061126000L);
    crComponent1.setIncrNumber(3L);
    crComponent1.setKeyLength((short) keyLength1.length);
    crComponent1.setKey(key1);
    crComponent1.setExpressionLength((short) expressionLength1.length);
    crComponent1.setExpression(expression1);
    fileService.addComponent(crComponent1);

    String key2 = "key2";
    String expression2 = "[#time(day) 7 9 #sid(0) #sid(1) m z #incr(0)]";
    byte[] keyLength2 = key2.getBytes(StandardCharsets.UTF_8);
    byte[] expressionLength2 = expression2.getBytes(StandardCharsets.UTF_8);
    CRComponent crComponent2 = new CRComponent();
    crComponent2.setComponentLength(
        (short)
            (CRComponent.COMPONENT_FIXED_FIELD_LENGTH
                + keyLength2.length
                + expressionLength2.length));
    crComponent2.setStatus((byte) 1);
    crComponent2.setCreateTime(1620061126000L);
    crComponent2.setUpdateTime(1620061126000L);
    crComponent2.setIncrNumber(6L);
    crComponent2.setKeyLength((short) keyLength2.length);
    crComponent2.setKey(key2);
    crComponent2.setExpressionLength((short) expressionLength2.length);
    crComponent2.setExpression(expression2);
    fileService.addComponent(crComponent2);

    String key = "key3";
    int initNumber = 100;
    int stepSize = 5;
    int resetType = 1;
    byte[] keyLength = key.getBytes(StandardCharsets.UTF_8);

    NSComponent nsComponent = new NSComponent();
    nsComponent.setComponentLength(
        (short) (NSComponent.COMPONENT_FIXED_FIELD_LENGTH + keyLength.length));
    nsComponent.setStatus((byte) 1);
    nsComponent.setCreateTime(System.currentTimeMillis());
    nsComponent.setUpdateTime(System.currentTimeMillis());
    nsComponent.setInitNumber(initNumber);
    nsComponent.setCurrentNumber(initNumber);
    nsComponent.setStepSize(stepSize);
    nsComponent.setResetType((byte) resetType);
    nsComponent.setKeyLength((short) keyLength.length);
    nsComponent.setKey(key);
    fileService.addComponent(nsComponent);
  }

  @Test
  public void test_b() {
    AbstractComponent crComponent1 = fileService.getComponentMap().get("key1");
    Assert.assertEquals(0, crComponent1.getBlockIndex());
    AbstractComponent crComponent2 = fileService.getComponentMap().get("key2");
    Assert.assertEquals(1, crComponent2.getBlockIndex());
    AbstractComponent nsComponent = fileService.getComponentMap().get("key3");
    Assert.assertEquals(1, nsComponent.getBlockIndex());
  }

  @Test
  public void test_c() {
    int blockFileLastPosition = fileService.getBlockFileLastPosition();
    Assert.assertEquals(160, blockFileLastPosition);

    blockFileLastPosition = fileService.getBlockFileLastPosition(0);
    Assert.assertEquals(117, blockFileLastPosition);
  }

  @Test
  public void test_d() {
    long logicOffset = fileService.getLogicOffset();
    Assert.assertEquals(277, logicOffset);
  }

  @Test
  public void test_e() {
    int mySid = fileService.getMySid(0);
    Assert.assertEquals(0, mySid);
  }

  @Test
  public void test_f() {
    ConcurrentMap<String, AbstractComponent> componentMap = fileService.getComponentMap();
    Assert.assertEquals(3, componentMap.size());
  }

  @Test
  public void test_g() {
    List<BlockHeader> blockHeaderList = fileService.getBlockHeaderList();
    Assert.assertEquals(2, blockHeaderList.size());
  }

  @Test
  public void test_h() {
    boolean verify = fileService.verifyOffset(0, 117);
    Assert.assertEquals(true, verify);
  }

  @Test
  public void test_i() {
    int maxBlockIndex = fileService.getMaxBlockIndex();
    Assert.assertEquals(1, maxBlockIndex);
  }

  @Test
  public void test_j() {
    int syncLength = fileService.getSyncLength(0, 32, 1024);
    Assert.assertEquals(85, syncLength);
  }

  @Test
  public void test_k() {
    byte[] syncData = fileService.getSyncData(0, 32, 85);
    Assert.assertEquals(85, syncData.length);

    syncData = fileService.getSyncData(1, 32, 85);
    Assert.assertEquals(85, syncData.length);

    syncData = fileService.getSyncData(1, 117, 43);
    Assert.assertEquals(43, syncData.length);
  }

  @AfterClass
  public static void destroy() {
    fileService.unload();
    StoreConfig storeConfig = new StoreConfig();
    storeConfig.setStorePath(new File("").getAbsolutePath());
    delFile(new File(storeConfig.getStoreRootDir()));
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
