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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.myberry.common.codec.bean.InnnerObj;
import org.myberry.common.codec.bean.TestNullObject;
import org.myberry.common.codec.bean.TestObject;
import org.myberry.common.codec.util.NullObjects;

public class LightCodecTest {

  private static TestObject testObject = new TestObject();

  private static TestNullObject testNullObject = new TestNullObject();

  @BeforeClass
  public static void before() {
    testObject.setA(1);
    testObject.setB(2);
    testObject.setC(3L);
    testObject.setD(4L);
    testObject.setE(5F);
    testObject.setF(6F);
    testObject.setG(7D);
    testObject.setH(8D);
    testObject.setI(false);
    testObject.setJ(true);
    testObject.setK("中文 English");

    List<Integer> l1 = new ArrayList<>();
    l1.add(11);
    l1.add(12);
    testObject.setL(l1);

    Set<Integer> s1 = new HashSet<>();
    s1.add(13);
    s1.add(14);
    testObject.setM(s1);

    int[] arr1 = new int[2];
    arr1[0] = 15;
    arr1[1] = 16;
    testObject.setN(arr1);

    Integer[] arry1 = new Integer[2];
    arry1[0] = 17;
    arry1[1] = 18;
    testObject.setO(arry1);

    List<Long> l2 = new ArrayList<>();
    l2.add(21L);
    l2.add(22L);
    testObject.setP(l2);

    Set<Long> s2 = new HashSet<>();
    s2.add(23L);
    s2.add(24L);
    testObject.setQ(s2);

    long[] arr2 = new long[2];
    arr2[0] = 25L;
    arr2[1] = 26L;
    testObject.setR(arr2);

    Long[] arry2 = new Long[2];
    arry2[0] = 27L;
    arry2[1] = 28L;
    testObject.setS(arry2);

    List<Float> l3 = new ArrayList<>();
    l3.add(31F);
    l3.add(32F);
    testObject.setT(l3);

    Set<Float> s3 = new HashSet<>();
    s3.add(33F);
    s3.add(34F);
    testObject.setU(s3);

    float[] arr3 = new float[2];
    arr3[0] = 35F;
    arr3[1] = 36F;
    testObject.setV(arr3);

    Float[] arry3 = new Float[2];
    arry3[0] = 37F;
    arry3[1] = 38F;
    testObject.setW(arry3);

    List<Double> l4 = new ArrayList<>();
    l4.add(41D);
    l4.add(42D);
    testObject.setX(l4);

    Set<Double> s4 = new HashSet<>();
    s4.add(43D);
    s4.add(44D);
    testObject.setY(s4);

    double[] arr4 = new double[2];
    arr4[0] = 45D;
    arr4[1] = 46D;
    testObject.setZ(arr4);

    Double[] arry4 = new Double[2];
    arry4[0] = 47D;
    arry4[1] = 48D;
    testObject.setAa(arry4);

    List<Boolean> l5 = new ArrayList<>();
    l5.add(false);
    l5.add(true);
    testObject.setBb(l5);

    Set<Boolean> s5 = new HashSet<>();
    s5.add(false);
    s5.add(true);
    testObject.setCc(s5);

    boolean[] arr5 = new boolean[2];
    arr5[0] = false;
    arr5[1] = true;
    testObject.setDd(arr5);

    Boolean[] arry5 = new Boolean[2];
    arry5[0] = false;
    arry5[1] = true;
    testObject.setEe(arry5);

    List<String> l6 = new ArrayList<>();
    l6.add("中文");
    l6.add("English");
    testObject.setFf(l6);

    Set<String> s6 = new HashSet<>();
    s6.add("中文");
    s6.add("English");
    testObject.setGg(s6);

    String[] arry6 = new String[2];
    arry6[0] = "中文";
    arry6[1] = "English";
    testObject.setHh(arry6);

    InnnerObj innnerObj = new InnnerObj();
    innnerObj.setName("测试");

    List<Integer> orders = new ArrayList<>();
    orders.add(111);
    orders.add(222);
    innnerObj.setOrder(orders);

    testObject.setInnnerObj(innnerObj);

    InnnerObj innnerObj1 = new InnnerObj();
    innnerObj1.setName("测试1");

    List<Integer> orders1 = new ArrayList<>();
    orders1.add(1111);
    orders1.add(2222);
    innnerObj1.setOrder(orders1);

    InnnerObj innnerObj2 = new InnnerObj();
    innnerObj2.setName("测试2");

    List<Integer> orders2 = new ArrayList<>();
    orders2.add(3333);
    orders2.add(4444);
    innnerObj2.setOrder(orders2);

    List<InnnerObj> innnerObjList = new ArrayList<>();
    innnerObjList.add(innnerObj1);
    innnerObjList.add(innnerObj2);

    testObject.setInnnerObjList(innnerObjList);

    Set<InnnerObj> innnerObjSet = new HashSet<>();
    innnerObjSet.add(innnerObj1);
    innnerObjSet.add(innnerObj2);

    testObject.setInnnerObjSet(innnerObjSet);

    InnnerObj[] innnerObjArray = new InnnerObj[2];
    innnerObjArray[0] = innnerObj1;
    innnerObjArray[1] = innnerObj2;

    testObject.setInnnerObjArray(innnerObjArray);
  }

  @Test
  public void testEncodeDecode() {
    byte[] bytes = LightCodec.toBytes(testObject);
    TestObject obj = LightCodec.toObj(bytes, TestObject.class);

    Assert.assertEquals(1, obj.getA());
    Assert.assertEquals(2, obj.getB().intValue());
    Assert.assertEquals(3L, obj.getC());
    Assert.assertEquals(4L, obj.getD().longValue());
    Assert.assertEquals(5F, obj.getE(), .0);
    Assert.assertEquals(6F, obj.getF().floatValue(), .0);
    Assert.assertEquals(7D, obj.getG(), .0);
    Assert.assertEquals(8D, obj.getH().doubleValue(), .0);
    Assert.assertEquals(false, obj.isI());
    Assert.assertEquals(true, obj.getJ().booleanValue());
    Assert.assertEquals("中文 English", obj.getK());
    Assert.assertEquals(LightCodecTest.testObject.getL(), obj.getL());
    Assert.assertEquals(LightCodecTest.testObject.getM(), obj.getM());
    Assert.assertEquals(LightCodecTest.testObject.getN()[0], obj.getN()[0]);
    Assert.assertEquals(LightCodecTest.testObject.getO()[1], obj.getO()[1]);
    Assert.assertEquals(LightCodecTest.testObject.getP(), obj.getP());
    Assert.assertEquals(LightCodecTest.testObject.getQ(), obj.getQ());
    Assert.assertEquals(LightCodecTest.testObject.getR()[0], obj.getR()[0]);
    Assert.assertEquals(LightCodecTest.testObject.getS()[1], obj.getS()[1]);
    Assert.assertEquals(LightCodecTest.testObject.getT(), obj.getT());
    Assert.assertEquals(LightCodecTest.testObject.getU(), obj.getU());
    Assert.assertEquals(LightCodecTest.testObject.getV()[0], obj.getV()[0], 0.0);
    Assert.assertEquals(LightCodecTest.testObject.getW()[1], obj.getW()[1]);
    Assert.assertEquals(LightCodecTest.testObject.getX(), obj.getX());
    Assert.assertEquals(LightCodecTest.testObject.getY(), obj.getY());
    Assert.assertEquals(LightCodecTest.testObject.getZ()[0], obj.getZ()[0], 0.0);
    Assert.assertEquals(LightCodecTest.testObject.getAa()[1], obj.getAa()[1]);
    Assert.assertEquals(LightCodecTest.testObject.getBb(), obj.getBb());
    Assert.assertEquals(LightCodecTest.testObject.getCc(), obj.getCc());
    Assert.assertEquals(LightCodecTest.testObject.getDd()[0], obj.getDd()[0]);
    Assert.assertEquals(LightCodecTest.testObject.getEe()[1], obj.getEe()[1]);
    Assert.assertEquals(LightCodecTest.testObject.getFf(), obj.getFf());
    Assert.assertEquals(LightCodecTest.testObject.getGg(), obj.getGg());
    Assert.assertEquals(LightCodecTest.testObject.getHh()[0], obj.getHh()[0]);
    Assert.assertEquals(LightCodecTest.testObject.getHh()[1], obj.getHh()[1]);
    Assert.assertEquals(
        LightCodecTest.testObject.getInnnerObj().getName(), obj.getInnnerObj().getName());
    Assert.assertEquals(
        LightCodecTest.testObject.getInnnerObj().getOrder(), obj.getInnnerObj().getOrder());

    Assert.assertEquals(
        LightCodecTest.testObject.getInnnerObjList().get(0).getName(),
        obj.getInnnerObjList().get(0).getName());
    Assert.assertEquals(
        LightCodecTest.testObject.getInnnerObjList().get(0).getOrder().get(0),
        obj.getInnnerObjList().get(0).getOrder().get(0));
    Assert.assertEquals(
        LightCodecTest.testObject.getInnnerObjList().get(0).getOrder().get(1),
        obj.getInnnerObjList().get(0).getOrder().get(1));
    Assert.assertEquals(
        LightCodecTest.testObject.getInnnerObjList().get(1).getName(),
        obj.getInnnerObjList().get(1).getName());
    Assert.assertEquals(
        LightCodecTest.testObject.getInnnerObjList().get(1).getOrder().get(0),
        obj.getInnnerObjList().get(1).getOrder().get(0));
    Assert.assertEquals(
        LightCodecTest.testObject.getInnnerObjList().get(1).getOrder().get(1),
        obj.getInnnerObjList().get(1).getOrder().get(1));

    int i = 0;
    Iterator<InnnerObj> it = testObject.getInnnerObjSet().iterator();
    while (it.hasNext()) {
      i++;
      InnnerObj expected = it.next();

      int j = 0;
      Iterator<InnnerObj> iterator = obj.getInnnerObjSet().iterator();
      while (iterator.hasNext()) {
        j++;
        InnnerObj actual = iterator.next();
        if (i == j) {
          Assert.assertEquals(expected.getName(), actual.getName());
          Assert.assertEquals(expected.getOrder().get(0), actual.getOrder().get(0));
          Assert.assertEquals(expected.getOrder().get(1), actual.getOrder().get(1));
          break;
        }
      }
    }

    Assert.assertEquals(
        LightCodecTest.testObject.getInnnerObjArray()[0].getName(),
        obj.getInnnerObjArray()[0].getName());
    Assert.assertEquals(
        LightCodecTest.testObject.getInnnerObjArray()[0].getOrder().get(0),
        obj.getInnnerObjArray()[0].getOrder().get(0));
    Assert.assertEquals(
        LightCodecTest.testObject.getInnnerObjArray()[0].getOrder().get(1),
        obj.getInnnerObjArray()[0].getOrder().get(1));
    Assert.assertEquals(
        LightCodecTest.testObject.getInnnerObjArray()[1].getName(),
        obj.getInnnerObjArray()[1].getName());
    Assert.assertEquals(
        LightCodecTest.testObject.getInnnerObjArray()[1].getOrder().get(0),
        obj.getInnnerObjArray()[1].getOrder().get(0));
    Assert.assertEquals(
        LightCodecTest.testObject.getInnnerObjArray()[1].getOrder().get(1),
        obj.getInnnerObjArray()[1].getOrder().get(1));
  }

  @Test
  public void testNullDefault() {
    byte[] bytes = LightCodec.toBytes(testNullObject);
    TestNullObject testNullObject = LightCodec.toObj(bytes, TestNullObject.class);

    Assert.assertEquals(5, testNullObject.getAa());
    Assert.assertEquals(NullObjects.NULL_INT_DEFAULT, testNullObject.getBb().intValue());
    Assert.assertEquals(NullObjects.NULL_LONG_DEFAULT, testNullObject.getCc().longValue());
    Assert.assertEquals(NullObjects.NULL_FLOAT_DEFAULT, testNullObject.getDd().floatValue(), .0);
    Assert.assertEquals(NullObjects.NULL_DOUBLE_DEFAULT, testNullObject.getEe().doubleValue(), .0);
    Assert.assertEquals(NullObjects.NULL_DOUBLE_DEFAULT, testNullObject.getFf(), .0);
    Assert.assertEquals(NullObjects.NULL_BOOLEAN_DEFAULT, testNullObject.getGg());
    Assert.assertEquals(NullObjects.NULL_STRING_DEFAULT, testNullObject.getHh());
    Assert.assertEquals(null, testNullObject.getIi());
    Assert.assertEquals(new ArrayList<>(0), testNullObject.getJj());
    Assert.assertEquals(new HashSet<>(0), testNullObject.getKk());
    Assert.assertArrayEquals(new int[0], testNullObject.getLl());
    Assert.assertArrayEquals(new Integer[0], testNullObject.getMm());
    Assert.assertEquals(new ArrayList<>(0), testNullObject.getNn());
    Assert.assertEquals(new ArrayList<>(0), testNullObject.getOo());
    Assert.assertEquals(new ArrayList<>(0), testNullObject.getPp());
    Assert.assertEquals(new ArrayList<>(0), testNullObject.getQq());
    Assert.assertEquals(new ArrayList<>(0), testNullObject.getRr());
    Assert.assertEquals(new HashSet<>(0), testNullObject.getSs());
    Assert.assertArrayEquals(new String[0], testNullObject.getTt());
    Assert.assertEquals(new ArrayList<>(0), testNullObject.getUu());
    Assert.assertEquals(new HashSet<>(0), testNullObject.getVv());
    Assert.assertArrayEquals(new InnnerObj[0], testNullObject.getWw());
  }
}
