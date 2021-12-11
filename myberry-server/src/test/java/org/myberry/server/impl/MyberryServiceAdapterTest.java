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
import org.myberry.common.codec.LightCodec;
import org.myberry.common.protocol.ResponseCode;
import org.myberry.common.protocol.body.admin.CRComponentData;
import org.myberry.common.protocol.body.admin.ComponentKeyData;
import org.myberry.common.protocol.body.admin.NSComponentData;
import org.myberry.server.converter.ConverterService;
import org.myberry.store.DefaultMyberryStore;
import org.myberry.store.MyberryStore;
import org.myberry.store.config.StoreConfig;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MyberryServiceAdapterTest {

  private static MyberryStore myberryStore;
  private static ConverterService converterService;
  private static MyberryServiceAdapter myberryServiceAdapter;

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

    myberryServiceAdapter = new MyberryServiceAdapter(myberryStore, converterService);
    myberryServiceAdapter.start();
  }

  @Test
  public void test_a() {
    CRComponentData crcd = new CRComponentData();
    crcd.setKey("key1");
    crcd.setExpression("[2 3 J #sid(0) #sid(1) m z #incr(1) #incr(0)]");

    DefaultResponse resp1 =
        myberryServiceAdapter.addComponent(crcd.getStructure(), LightCodec.toBytes(crcd));
    Assert.assertEquals(ResponseCode.SUCCESS, resp1.getRespCode());

    NSComponentData nscd = new NSComponentData();
    nscd.setKey("key2");
    nscd.setInitNumber(100);
    nscd.setStepSize(5);
    nscd.setResetType(1);

    DefaultResponse resp2 =
        myberryServiceAdapter.addComponent(nscd.getStructure(), LightCodec.toBytes(nscd));
    Assert.assertEquals(ResponseCode.SUCCESS, resp2.getRespCode());

    DefaultResponse resp3 = myberryServiceAdapter.addComponent(0, null);

    Assert.assertEquals(ResponseCode.UNKNOWN_STRUCTURE, resp3.getRespCode());
  }

  @Test
  public void test_b() {
    DefaultResponse resp1 = myberryServiceAdapter.getNewId("key1", null);
    Assert.assertEquals(ResponseCode.SUCCESS, resp1.getRespCode());
    DefaultResponse resp2 = myberryServiceAdapter.getNewId("key2", null);
    Assert.assertEquals(ResponseCode.SUCCESS, resp2.getRespCode());
    DefaultResponse resp3 = myberryServiceAdapter.getNewId("keyx", null);
    Assert.assertEquals(ResponseCode.KEY_NOT_EXISTED, resp3.getRespCode());
  }

  @Test
  public void test_c() {
    DefaultResponse resp = myberryServiceAdapter.queryComponentSize();
    Assert.assertEquals(ResponseCode.SUCCESS, resp.getRespCode());
  }

  @Test
  public void test_d() {
    ComponentKeyData ckd1 = new ComponentKeyData();
    ckd1.setKey("key1");
    DefaultResponse resp1 = myberryServiceAdapter.queryComponentByKey(LightCodec.toBytes(ckd1));

    Assert.assertEquals(ResponseCode.SUCCESS, resp1.getRespCode());

    ComponentKeyData ckd2 = new ComponentKeyData();
    ckd2.setKey("key2");
    DefaultResponse resp2 = myberryServiceAdapter.queryComponentByKey(LightCodec.toBytes(ckd2));

    Assert.assertEquals(ResponseCode.SUCCESS, resp2.getRespCode());

    ComponentKeyData ckd3 = new ComponentKeyData();
    ckd3.setKey("keyx");
    DefaultResponse resp3 = myberryServiceAdapter.queryComponentByKey(LightCodec.toBytes(ckd3));

    Assert.assertEquals(ResponseCode.KEY_NOT_EXISTED, resp3.getRespCode());
  }

  @Test
  public void test_e() {
    NSComponentData nscd = new NSComponentData();
    nscd.setKey("key2");
    nscd.setInitNumber(1000);
    nscd.setStepSize(50);
    nscd.setResetType(3);
    DefaultResponse resp =
        myberryServiceAdapter.modifyComponent(nscd.getStructure(), LightCodec.toBytes(nscd));

    Assert.assertEquals(ResponseCode.SUCCESS, resp.getRespCode());
  }

  @AfterClass
  public static void destroy() {
    myberryServiceAdapter.shutdown();
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
