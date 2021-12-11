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
package org.myberry.server.impl;

import java.io.File;
import java.lang.reflect.Field;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.myberry.common.protocol.body.admin.CRComponentData;
import org.myberry.common.protocol.body.admin.ComponentSizeData;
import org.myberry.common.protocol.body.admin.NSComponentData;
import org.myberry.common.protocol.body.user.CRPullResultData;
import org.myberry.common.protocol.body.user.NSPullResultData;
import org.myberry.server.converter.ConverterService;
import org.myberry.store.AbstractComponent;
import org.myberry.store.CRComponent;
import org.myberry.store.DefaultMyberryStore;
import org.myberry.store.MyberryStore;
import org.myberry.store.NSComponent;
import org.myberry.store.config.StoreConfig;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MyberryServiceImplTest {

  private static MyberryStore myberryStore;
  private static ConverterService converterService;
  private static MyberryServiceImpl myberryServiceImpl;

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

    converterService = new ConverterService(myberryStore);
    converterService.start();

    myberryServiceImpl = new MyberryServiceImpl(myberryStore, converterService);
  }

  @Test
  public void test_a() throws Exception {
    CRComponentData crcd = new CRComponentData();
    crcd.setKey("key1");
    crcd.setExpression("[2 3 J #sid(0) #sid(1) m z #incr(1) #incr(0)]");
    boolean b = myberryServiceImpl.addComponent(crcd);

    Assert.assertEquals(true, b);
  }

  @Test
  public void test_b() throws Exception {
    NSComponentData nscd = new NSComponentData();
    nscd.setKey("key2");
    nscd.setInitNumber(100);
    nscd.setStepSize(5);
    nscd.setResetType(1);
    boolean b = myberryServiceImpl.addComponent(nscd);

    Assert.assertEquals(true, b);
  }

  @Test
  public void test_c() throws Exception {
    CRPullResultData crd1 = myberryServiceImpl.getNewId("key1", null);
    Assert.assertEquals("23J00mz01", crd1.getNewId());
    CRPullResultData crd2 = myberryServiceImpl.getNewId("key1", null);
    Assert.assertEquals("23J00mz02", crd2.getNewId());
  }

  @Test
  public void test_d() throws Exception {
    NSPullResultData nsd1 = myberryServiceImpl.getNewId("key2");
    Assert.assertEquals(100, nsd1.getStart());
    Assert.assertEquals(104, nsd1.getEnd());
    Assert.assertEquals(0, nsd1.getSynergyId());
    NSPullResultData nsd2 = myberryServiceImpl.getNewId("key2");
    Assert.assertEquals(105, nsd2.getStart());
    Assert.assertEquals(109, nsd2.getEnd());
    Assert.assertEquals(0, nsd2.getSynergyId());
  }

  @Test
  public void test_e() {
    ComponentSizeData componentSizeData = myberryServiceImpl.queryComponentSize();

    Assert.assertEquals(2, componentSizeData.getSize());
  }

  @Test
  public void test_f() {
    AbstractComponent component = myberryStore.getComponentMap().get("key1");
    CRComponentData crcd = myberryServiceImpl.queryComponentByKey((CRComponent) component);

    Assert.assertEquals("key1", crcd.getKey());
    Assert.assertEquals("[2 3 J #sid(0) #sid(1) m z #incr(1) #incr(0)]", crcd.getExpression());
  }

  @Test
  public void test_g() {
    AbstractComponent component = myberryStore.getComponentMap().get("key2");
    NSComponentData nscd = myberryServiceImpl.queryComponentByKey((NSComponent) component);

    Assert.assertEquals("key2", nscd.getKey());
    Assert.assertEquals(100, nscd.getInitNumber());
    Assert.assertEquals(5, nscd.getStepSize());
    Assert.assertEquals(1, nscd.getResetType());
  }

  @Test
  public void test_h() throws Exception {
    NSComponentData nscd = new NSComponentData();
    nscd.setKey("key2");
    nscd.setInitNumber(200);
    nscd.setStepSize(10);
    nscd.setResetType(3);

    myberryServiceImpl.modifyComponent(nscd);

    AbstractComponent component = myberryStore.getComponentMap().get("key2");
    nscd = myberryServiceImpl.queryComponentByKey((NSComponent) component);

    Assert.assertEquals("key2", nscd.getKey());
    Assert.assertEquals(200, nscd.getInitNumber());
    Assert.assertEquals(10, nscd.getStepSize());
    Assert.assertEquals(3, nscd.getResetType());
  }

  @AfterClass
  public static void destroy() {
    converterService.shutdown();
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
