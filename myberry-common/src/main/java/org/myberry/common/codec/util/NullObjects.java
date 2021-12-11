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
package org.myberry.common.codec.util;

import java.util.List;
import java.util.Set;
import org.myberry.common.codec.MessageLite;

public final class NullObjects {

  public static final int NULL_INT_DEFAULT = 0;
  public static final long NULL_LONG_DEFAULT = 0L;
  public static final float NULL_FLOAT_DEFAULT = 0F;
  public static final double NULL_DOUBLE_DEFAULT = 0D;
  public static final boolean NULL_BOOLEAN_DEFAULT = false;
  public static final String NULL_STRING_DEFAULT = "";

  public static int getDefaultIntIfAbsent(Integer current) {
    return current == null ? NULL_INT_DEFAULT : current.intValue();
  }

  public static long getDefaultLongIfAbsent(Long current) {
    return current == null ? NULL_LONG_DEFAULT : current.longValue();
  }

  public static float getDefaultFloatIfAbsent(Float current) {
    return current == null ? NULL_FLOAT_DEFAULT : current.floatValue();
  }

  public static double getDefaultDoubleIfAbsent(Double current) {
    return current == null ? NULL_DOUBLE_DEFAULT : current.doubleValue();
  }

  public static boolean getDefaultBooleanIfAbsent(Boolean current) {
    return current == null ? NULL_BOOLEAN_DEFAULT : current.booleanValue();
  }

  public static String getDefaultStringIfAbsent(String current) {
    return current == null ? NULL_STRING_DEFAULT : current;
  }

  public static <T> List<T> getDefaultListIfAbsent(List<T> current) {
    return current == null ? Lists.newArrayList() : current;
  }

  public static <T> Set<T> getDefaultSetIfAbsent(Set<T> current) {
    return current == null ? Sets.newHashSet() : current;
  }

  public static int[] getDefaultArrayIfAbsent(int[] current) {
    return current == null ? (int[]) Arrays.newArray(int.class) : current;
  }

  public static Integer[] getDefaultArrayIfAbsent(Integer[] current) {
    return current == null ? (Integer[]) Arrays.newArray(Integer.class) : current;
  }

  public static long[] getDefaultArrayIfAbsent(long[] current) {
    return current == null ? (long[]) Arrays.newArray(long.class) : current;
  }

  public static Long[] getDefaultArrayIfAbsent(Long[] current) {
    return current == null ? (Long[]) Arrays.newArray(Long.class) : current;
  }

  public static float[] getDefaultArrayIfAbsent(float[] current) {
    return current == null ? (float[]) Arrays.newArray(float.class) : current;
  }

  public static Float[] getDefaultArrayIfAbsent(Float[] current) {
    return current == null ? (Float[]) Arrays.newArray(Float.class) : current;
  }

  public static double[] getDefaultArrayIfAbsent(double[] current) {
    return current == null ? (double[]) Arrays.newArray(double.class) : current;
  }

  public static Double[] getDefaultArrayIfAbsent(Double[] current) {
    return current == null ? (Double[]) Arrays.newArray(Double.class) : current;
  }

  public static boolean[] getDefaultArrayIfAbsent(boolean[] current) {
    return current == null ? (boolean[]) Arrays.newArray(boolean.class) : current;
  }

  public static Boolean[] getDefaultArrayIfAbsent(Boolean[] current) {
    return current == null ? (Boolean[]) Arrays.newArray(Boolean.class) : current;
  }

  public static String[] getDefaultArrayIfAbsent(String[] current) {
    return current == null ? (String[]) Arrays.newArray(String.class) : current;
  }

  public static MessageLite[] getDefaultArrayIfAbsent(MessageLite[] current) {
    return current == null ? (MessageLite[]) Arrays.newArray(MessageLite.class) : current;
  }
}
