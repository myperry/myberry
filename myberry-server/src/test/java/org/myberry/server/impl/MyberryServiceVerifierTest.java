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
import java.nio.charset.StandardCharsets;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.myberry.common.codec.LightCodec;
import org.myberry.common.protocol.ResponseCode;
import org.myberry.common.protocol.body.admin.CRComponentData;
import org.myberry.common.protocol.body.admin.ComponentSizeData;
import org.myberry.common.protocol.body.admin.NSComponentData;
import org.myberry.common.protocol.body.user.CRPullResultData;
import org.myberry.common.protocol.body.user.NSPullResultData;
import org.myberry.common.structure.Structure;
import org.myberry.server.converter.ConverterService;
import org.myberry.store.CRComponent;
import org.myberry.store.DefaultMyberryStore;
import org.myberry.store.MyberryStore;
import org.myberry.store.NSComponent;
import org.myberry.store.config.StoreConfig;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MyberryServiceVerifierTest {

  private static MyberryStore myberryStore;
  private static ConverterService converterService;
  private static MyberryServiceVerifier myberryServiceVerifier;

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

    myberryServiceVerifier = new MyberryServiceVerifier(myberryStore, converterService);
    myberryServiceVerifier.start();
  }

  @Test
  public void test_a() {
    CRComponentData crcd = new CRComponentData();
    crcd.setKey("key1");
    crcd.setExpression("[2 3 J #sid(0) #sid(1) m z #incr(1) #incr(0)]");

    DefaultResponse resp = myberryServiceVerifier.addComponent(crcd);
    Assert.assertEquals(ResponseCode.SUCCESS, resp.getRespCode());

    DefaultResponse resp1 = myberryServiceVerifier.addComponent(crcd);
    Assert.assertEquals(ResponseCode.KEY_EXISTED, resp1.getRespCode());

    CRComponentData crcd1 = new CRComponentData();
    crcd1.setKey(new String(new byte[Short.MAX_VALUE], StandardCharsets.UTF_8));
    crcd1.setExpression("[2 3 J #sid(0) #sid(1) m z #incr(1) #incr(0)]");

    DefaultResponse resp2 = myberryServiceVerifier.addComponent(crcd1);
    Assert.assertEquals(ResponseCode.PARAMETER_LENGTH_TOO_LONG, resp2.getRespCode());

    CRComponentData crcd2 = new CRComponentData();
    crcd2.setKey("keyx");
    crcd2.setExpression("[2 3 J #sid(0) #sid(1) m z #incr(1) #incr(2)]");

    DefaultResponse resp3 = myberryServiceVerifier.addComponent(crcd2);
    Assert.assertEquals(ResponseCode.INVALID_EXPRESSION, resp3.getRespCode());
  }

  @Test
  public void test_b() {
    NSComponentData nscd = new NSComponentData();
    nscd.setKey("key2");
    nscd.setInitNumber(100);
    nscd.setStepSize(5);
    nscd.setResetType(1);

    DefaultResponse resp = myberryServiceVerifier.addComponent(nscd);
    Assert.assertEquals(ResponseCode.SUCCESS, resp.getRespCode());

    DefaultResponse resp1 = myberryServiceVerifier.addComponent(nscd);
    Assert.assertEquals(ResponseCode.KEY_EXISTED, resp1.getRespCode());

    NSComponentData nscd1 = new NSComponentData();
    nscd1.setKey(new String(new byte[Short.MAX_VALUE], StandardCharsets.UTF_8));
    nscd1.setInitNumber(100);
    nscd1.setStepSize(5);
    nscd1.setResetType(1);

    DefaultResponse resp2 = myberryServiceVerifier.addComponent(nscd1);
    Assert.assertEquals(ResponseCode.PARAMETER_LENGTH_TOO_LONG, resp2.getRespCode());
  }

  @Test
  public void test_c() {
    DefaultResponse resp1 = myberryServiceVerifier.getNewId("key1", null);
    CRPullResultData crPullResultData = LightCodec.toObj(resp1.getBody(), CRPullResultData.class);
    Assert.assertEquals(Structure.CR, resp1.getStructure());
    Assert.assertEquals("key1", new String(resp1.getExt(), StandardCharsets.UTF_8));
    Assert.assertEquals("23J00mz01", crPullResultData.getNewId());
  }

  @Test
  public void test_d() {
    DefaultResponse resp1 = myberryServiceVerifier.getNewId("key2");
    NSPullResultData nsPullResultData = LightCodec.toObj(resp1.getBody(), NSPullResultData.class);
    Assert.assertEquals(Structure.NS, resp1.getStructure());
    Assert.assertEquals("key2", new String(resp1.getExt(), StandardCharsets.UTF_8));
    Assert.assertEquals(100, nsPullResultData.getStart());
    Assert.assertEquals(104, nsPullResultData.getEnd());
    Assert.assertEquals(0, nsPullResultData.getSynergyId());
  }

  @Test
  public void test_e() {
    DefaultResponse resp = myberryServiceVerifier.queryComponentSize();
    ComponentSizeData componentSizeData = LightCodec.toObj(resp.getBody(), ComponentSizeData.class);

    Assert.assertEquals(2, componentSizeData.getSize());
  }

  @Test
  public void test_f() {
    CRComponent crComponent = new CRComponent();
    crComponent.setKey("key1");
    crComponent.setExpression("[2 3 J #sid(0) #sid(1) m z #incr(1) #incr(0)]");
    DefaultResponse resp1 = myberryServiceVerifier.queryComponentByKey(crComponent);
    Assert.assertEquals(Structure.CR, resp1.getStructure());

    CRComponentData crComponentData = LightCodec.toObj(resp1.getBody(), CRComponentData.class);
    Assert.assertEquals("key1", crComponentData.getKey());
    Assert.assertEquals(
        "[2 3 J #sid(0) #sid(1) m z #incr(1) #incr(0)]", crComponentData.getExpression());

    NSComponent nsComponent = new NSComponent();
    nsComponent.setKey("key2");
    nsComponent.setInitNumber(100);
    nsComponent.setStepSize(5);
    nsComponent.setResetType((byte) 1);
    DefaultResponse resp2 = myberryServiceVerifier.queryComponentByKey(nsComponent);
    Assert.assertEquals(Structure.NS, resp2.getStructure());

    NSComponentData nsComponentData = LightCodec.toObj(resp2.getBody(), NSComponentData.class);
    Assert.assertEquals("key2", nsComponentData.getKey());
    Assert.assertEquals(100, nsComponentData.getInitNumber());
    Assert.assertEquals(5, nsComponentData.getStepSize());
    Assert.assertEquals(1, nsComponentData.getResetType());
  }

  @Test
  public void test_g() {
    NSComponentData nscd = new NSComponentData();
    nscd.setKey("key2");
    nscd.setInitNumber(1000);
    nscd.setStepSize(50);
    nscd.setResetType(2);
    DefaultResponse resp1 = myberryServiceVerifier.modifyComponent(nscd);
    Assert.assertEquals(ResponseCode.SUCCESS, resp1.getRespCode());

    nscd = new NSComponentData();
    nscd.setKey("key3");
    DefaultResponse resp2 = myberryServiceVerifier.modifyComponent(nscd);
    Assert.assertEquals(ResponseCode.KEY_NOT_EXISTED, resp2.getRespCode());
  }

  @AfterClass
  public static void destroy() {
    myberryServiceVerifier.shutdown();
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
