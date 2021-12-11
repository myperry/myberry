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
import org.myberry.store.config.StoreConfig;
import org.myberry.store.impl.StoreHeader;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultMyberryStoreTest {

  private static MyberryStore myberryStore;

  @BeforeClass
  public static void init() throws Exception {
    StoreConfig storeConfig = new StoreConfig();
    storeConfig.setStorePath(new File("").getAbsolutePath());
    Field[] fields = storeConfig.getClass().getDeclaredFields();
    for (Field field : fields) {
      if ("blockFileSize".equals(field.getName())) {
        field.setAccessible(true);
        field.set(storeConfig, 200);
      }
    }
    myberryStore = new DefaultMyberryStore(storeConfig);
    myberryStore.start();
  }

  @Test
  public void test_a() {
    int lastPosition = myberryStore.getLastPosition();
    Assert.assertEquals(StoreHeader.STORE_HEADER_SIZE, lastPosition);

    lastPosition = myberryStore.getLastPosition(0);
    Assert.assertEquals(StoreHeader.STORE_HEADER_SIZE, lastPosition);

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
    myberryStore.addComponent(crComponent);
  }

  @Test
  public void test_b() {
    long logicOffset = myberryStore.getLogicOffset();
    Assert.assertEquals(117, logicOffset);
  }

  @Test
  public void test_c() {
    int mySid = myberryStore.getMySid(0);
    Assert.assertEquals(0, mySid);
  }

  @Test
  public void test_d() {
    ConcurrentMap<String, AbstractComponent> componentMap = myberryStore.getComponentMap();
    Assert.assertEquals(1, componentMap.size());
  }

  @Test
  public void test_e() {
    List<BlockHeader> blockHeaderList = myberryStore.getBlockHeaderList();
    Assert.assertEquals(1, blockHeaderList.size());
  }

  @Test
  public void test_f() {
    boolean verify = myberryStore.verifyOffset(0, 117);
    Assert.assertEquals(true, verify);
  }

  @Test
  public void test_g() {
    int maxBlockIndex = myberryStore.getMaxBlockIndex();
    Assert.assertEquals(0, maxBlockIndex);
  }

  @Test
  public void test_h() {
    int syncLength = myberryStore.getSyncLength(0, 32, 1024);
    Assert.assertEquals(85, syncLength);
  }

  @Test
  public void test_i() {
    byte[] syncData = myberryStore.getSyncData(0, 32, 85);
    Assert.assertEquals(85, syncData.length);
  }

  @Test
  public void test_j() {
    int lastPosition = myberryStore.getLastPosition();
    Assert.assertEquals(117, lastPosition);

    lastPosition = myberryStore.getLastPosition(0);
    Assert.assertEquals(117, lastPosition);
  }

  @AfterClass
  public static void destroy() {
    myberryStore.shutdown();
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
