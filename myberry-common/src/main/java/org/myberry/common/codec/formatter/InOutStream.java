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
package org.myberry.common.codec.formatter;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import org.myberry.common.codec.util.Arrays;
import org.myberry.common.codec.util.ElasticBuffer;
import org.myberry.common.codec.util.LinkedListStack;
import org.myberry.common.codec.util.Lists;
import org.myberry.common.codec.util.Sets;

public final class InOutStream {

  private static final Charset UTF_8 = Charset.forName("UTF-8");
  public static final int DEFAULT_MESSAGELITE_LENGTH = 0;

  private final ElasticBuffer elasticBuffer;

  private final LinkedListStack<Integer> stack = new LinkedListStack<>();

  public InOutStream() {
    this.elasticBuffer = ElasticBuffer.getOrCreateElasticBuffer(0);
  }

  public static short getSerialNo(ElasticBuffer elasticBuffer) {
    return elasticBuffer.getShort();
  }

  public static short getFieldId(ElasticBuffer elasticBuffer) {
    return elasticBuffer.getShort();
  }
  /**
   * {@link org.myberry.common.codec.formatter.SerializedObjectFormat#getTagLength()}
   *
   * @param serialNo
   * @param fieldId
   * @return
   */
  public InOutStream putTag(int serialNo, int fieldId) {
    elasticBuffer.putShort((short) serialNo);
    elasticBuffer.putShort((short) fieldId);
    return this;
  }

  public void markMessageLiteLength() {
    stack.push(elasticBuffer.position());
    elasticBuffer.putInt(DEFAULT_MESSAGELITE_LENGTH);
  }

  public static int getMessageLiteLength(ElasticBuffer elasticBuffer) {
    return elasticBuffer.getInt();
  }

  /**
   * {@link org.myberry.common.codec.formatter.SerializedObjectFormat#getMessageLiteLength()}
   *
   * @return
   */
  public InOutStream putMessageLiteLength() {
    Integer markedMessageLiteLengthOffset = stack.pop();
    elasticBuffer.putInt(
        markedMessageLiteLengthOffset,
        elasticBuffer.position() - markedMessageLiteLengthOffset - 4);
    return this;
  }

  public static int getInt(ElasticBuffer elasticBuffer) {
    return elasticBuffer.getInt();
  }

  /**
   * {@link org.myberry.common.codec.formatter.SerializedObjectFormat#getIntLength()}
   *
   * @param output
   * @return
   */
  public InOutStream putInt(int output) {
    elasticBuffer.putInt(output);
    return this;
  }

  public static long getLong(ElasticBuffer elasticBuffer) {
    return elasticBuffer.getLong();
  }

  /**
   * {@link org.myberry.common.codec.formatter.SerializedObjectFormat#getLongLength()}
   *
   * @param output
   * @return
   */
  public InOutStream putLong(long output) {
    elasticBuffer.putLong(output);
    return this;
  }

  public static float getFloat(ElasticBuffer elasticBuffer) {
    return elasticBuffer.getFloat();
  }

  /**
   * {@link org.myberry.common.codec.formatter.SerializedObjectFormat#getFloatLength()}
   *
   * @param output
   * @return
   */
  public InOutStream putFloat(float output) {
    elasticBuffer.putFloat(output);
    return this;
  }

  public static double getDouble(ElasticBuffer elasticBuffer) {
    return elasticBuffer.getDouble();
  }

  /**
   * {@link org.myberry.common.codec.formatter.SerializedObjectFormat#getDoubleLength()}
   *
   * @param output
   * @return
   */
  public InOutStream putDouble(double output) {
    elasticBuffer.putDouble(output);
    return this;
  }

  public static boolean getBoolean(ElasticBuffer elasticBuffer) {
    return transformBoolean(elasticBuffer.get());
  }

  /**
   * {@link org.myberry.common.codec.formatter.SerializedObjectFormat#getBooleanLength()}
   *
   * @param output
   * @return
   */
  public InOutStream putBoolean(boolean output) {
    elasticBuffer.put(transformBoolean(output));
    return this;
  }

  public static String getString(ElasticBuffer elasticBuffer) {
    int fieldLength = elasticBuffer.getInt();
    byte[] stringByte = elasticBuffer.getArray(fieldLength);
    return read(stringByte);
  }

  /**
   * {@link org.myberry.common.codec.formatter.SerializedObjectFormat#getStringLength(String)}
   *
   * @param output
   * @return
   */
  public InOutStream putString(String output) {
    byte[] stringByte = InOutStream.write(output);
    elasticBuffer.putInt(stringByte.length);
    elasticBuffer.putArray(stringByte);
    return this;
  }

  public static int getListSize(ElasticBuffer elasticBuffer) {
    return elasticBuffer.getInt();
  }

  public InOutStream putListSize(int size) {
    elasticBuffer.putInt(size);
    return this;
  }

  public static List<Integer> getIntList(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    List<Integer> list = Lists.newArrayList(fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      list.add(getInt(elasticBuffer));
      i++;
    }
    return list;
  }

  public static Set<Integer> getIntSet(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    Set<Integer> set = Sets.newHashSet(fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      set.add(getInt(elasticBuffer));
      i++;
    }
    return set;
  }

  public static int[] getIntArray(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    int[] array = (int[]) Arrays.newArray(int.class, fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      array[i] = getInt(elasticBuffer);
      i++;
    }
    return array;
  }

  public static Integer[] getIntBoxedArray(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    Integer[] array = (Integer[]) Arrays.newArray(Integer.class, fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      array[i] = getInt(elasticBuffer);
      i++;
    }
    return array;
  }

  /**
   * {@link org.myberry.common.codec.formatter.SerializedObjectFormat#getIntListLength(int)}
   *
   * @param outputs
   * @return
   */
  public InOutStream putIntList(List<Integer> outputs) {
    putListSize(outputs.size());
    for (Integer output : outputs) {
      elasticBuffer.putInt(output);
    }
    return this;
  }

  public InOutStream putIntSet(Set<Integer> outputs) {
    putListSize(outputs.size());
    for (Integer output : outputs) {
      elasticBuffer.putInt(output);
    }
    return this;
  }

  public InOutStream putIntArray(int[] outputs) {
    putListSize(outputs.length);
    for (int output : outputs) {
      elasticBuffer.putInt(output);
    }
    return this;
  }

  public InOutStream putIntBoxedArray(Integer[] outputs) {
    putListSize(outputs.length);
    for (Integer output : outputs) {
      elasticBuffer.putInt(output);
    }
    return this;
  }

  public static List<Long> getLongList(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    List<Long> list = Lists.newArrayList(fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      list.add(getLong(elasticBuffer));
      i++;
    }
    return list;
  }

  public static Set<Long> getLongSet(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    Set<Long> set = Sets.newHashSet(fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      set.add(getLong(elasticBuffer));
      i++;
    }
    return set;
  }

  public static long[] getLongArray(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    long[] array = (long[]) Arrays.newArray(long.class, fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      array[i] = getLong(elasticBuffer);
      i++;
    }
    return array;
  }

  public static Long[] getLongBoxedArray(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    Long[] array = (Long[]) Arrays.newArray(Long.class, fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      array[i] = getLong(elasticBuffer);
      i++;
    }
    return array;
  }

  /**
   * {@link org.myberry.common.codec.formatter.SerializedObjectFormat#getLongListLength(int)}
   *
   * @param outputs
   * @return
   */
  public InOutStream putLongList(List<Long> outputs) {
    putListSize(outputs.size());
    for (Long output : outputs) {
      elasticBuffer.putLong(output);
    }
    return this;
  }

  public InOutStream putLongSet(Set<Long> outputs) {
    putListSize(outputs.size());
    for (Long output : outputs) {
      elasticBuffer.putLong(output);
    }
    return this;
  }

  public InOutStream putLongArray(long[] outputs) {
    putListSize(outputs.length);
    for (long output : outputs) {
      elasticBuffer.putLong(output);
    }
    return this;
  }

  public InOutStream putLongBoxedArray(Long[] outputs) {
    putListSize(outputs.length);
    for (Long output : outputs) {
      elasticBuffer.putLong(output);
    }
    return this;
  }

  public static List<Float> getFloatList(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    List<Float> list = Lists.newArrayList(fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      list.add(getFloat(elasticBuffer));
      i++;
    }
    return list;
  }

  public static Set<Float> getFloatSet(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    Set<Float> set = Sets.newHashSet(fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      set.add(getFloat(elasticBuffer));
      i++;
    }
    return set;
  }

  public static float[] getFloatArray(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    float[] array = (float[]) Arrays.newArray(float.class, fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      array[i] = getFloat(elasticBuffer);
      i++;
    }
    return array;
  }

  public static Float[] getFloatBoxedArray(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    Float[] array = (Float[]) Arrays.newArray(Float.class, fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      array[i] = getFloat(elasticBuffer);
      i++;
    }
    return array;
  }

  /**
   * {@link org.myberry.common.codec.formatter.SerializedObjectFormat#getFloatListLength(int)}
   *
   * @param outputs
   * @return
   */
  public InOutStream putFloatList(List<Float> outputs) {
    putListSize(outputs.size());
    for (Float output : outputs) {
      elasticBuffer.putFloat(output);
    }
    return this;
  }

  public InOutStream putFloatSet(Set<Float> outputs) {
    putListSize(outputs.size());
    for (Float output : outputs) {
      elasticBuffer.putFloat(output);
    }
    return this;
  }

  public InOutStream putFloatArray(float[] outputs) {
    putListSize(outputs.length);
    for (float output : outputs) {
      elasticBuffer.putFloat(output);
    }
    return this;
  }

  public InOutStream putFloatBoxedArray(Float[] outputs) {
    putListSize(outputs.length);
    for (Float output : outputs) {
      elasticBuffer.putFloat(output);
    }
    return this;
  }

  public static List<Double> getDoubleList(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    List<Double> list = Lists.newArrayList(fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      list.add(getDouble(elasticBuffer));
      i++;
    }
    return list;
  }

  public static Set<Double> getDoubleSet(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    Set<Double> set = Sets.newHashSet(fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      set.add(getDouble(elasticBuffer));
      i++;
    }
    return set;
  }

  public static double[] getDoubleArray(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    double[] array = (double[]) Arrays.newArray(double.class, fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      array[i] = getDouble(elasticBuffer);
      i++;
    }
    return array;
  }

  public static Double[] getDoubleBoxedArray(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    Double[] array = (Double[]) Arrays.newArray(Double.class, fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      array[i] = getDouble(elasticBuffer);
      i++;
    }
    return array;
  }

  /**
   * {@link org.myberry.common.codec.formatter.SerializedObjectFormat#getDoubleListLength(int)}
   *
   * @param outputs
   * @return
   */
  public InOutStream putDoubleList(List<Double> outputs) {
    putListSize(outputs.size());
    for (Double output : outputs) {
      elasticBuffer.putDouble(output);
    }
    return this;
  }

  public InOutStream putDoubleSet(Set<Double> outputs) {
    putListSize(outputs.size());
    for (Double output : outputs) {
      elasticBuffer.putDouble(output);
    }
    return this;
  }

  public InOutStream putDoubleArray(double[] outputs) {
    putListSize(outputs.length);
    for (double output : outputs) {
      elasticBuffer.putDouble(output);
    }
    return this;
  }

  public InOutStream putDoubleBoxedArray(Double[] outputs) {
    putListSize(outputs.length);
    for (Double output : outputs) {
      elasticBuffer.putDouble(output);
    }
    return this;
  }

  public static List<Boolean> getBooleanList(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    List<Boolean> list = Lists.newArrayList(fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      list.add(getBoolean(elasticBuffer));
      i++;
    }
    return list;
  }

  public static Set<Boolean> getBooleanSet(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    Set<Boolean> set = Sets.newHashSet(fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      set.add(getBoolean(elasticBuffer));
      i++;
    }
    return set;
  }

  public static boolean[] getBooleanArray(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    boolean[] array = (boolean[]) Arrays.newArray(boolean.class, fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      array[i] = getBoolean(elasticBuffer);
      i++;
    }
    return array;
  }

  public static Boolean[] getBooleanBoxedArray(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    Boolean[] array = (Boolean[]) Arrays.newArray(Boolean.class, fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      array[i] = getBoolean(elasticBuffer);
      i++;
    }
    return array;
  }

  /**
   * {@link org.myberry.common.codec.formatter.SerializedObjectFormat#getBooleanListLength(int)}
   *
   * @param outputs
   * @return
   */
  public InOutStream putBooleanList(List<Boolean> outputs) {
    putListSize(outputs.size());
    for (Boolean output : outputs) {
      elasticBuffer.put(transformBoolean(output));
    }
    return this;
  }

  public InOutStream putBooleanSet(Set<Boolean> outputs) {
    putListSize(outputs.size());
    for (Boolean output : outputs) {
      elasticBuffer.put(transformBoolean(output));
    }
    return this;
  }

  public InOutStream putBooleanArray(boolean[] outputs) {
    putListSize(outputs.length);
    for (boolean output : outputs) {
      elasticBuffer.put(transformBoolean(output));
    }
    return this;
  }

  public InOutStream putBooleanBoxedArray(Boolean[] outputs) {
    putListSize(outputs.length);
    for (Boolean output : outputs) {
      elasticBuffer.put(transformBoolean(output));
    }
    return this;
  }

  public static List<String> getStringList(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    List<String> list = Lists.newArrayList(fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      list.add(getString(elasticBuffer));
      i++;
    }
    return list;
  }

  public static Set<String> getStringSet(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    Set<String> set = Sets.newHashSet(fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      set.add(getString(elasticBuffer));
      i++;
    }
    return set;
  }

  public static String[] getStringArray(ElasticBuffer elasticBuffer) {
    int fixedCapacity = getListSize(elasticBuffer);
    String[] array = (String[]) Arrays.newArray(String.class, fixedCapacity);
    int i = 0;
    while (i < fixedCapacity) {
      array[i] = getString(elasticBuffer);
      i++;
    }
    return array;
  }

  /**
   * {@link org.myberry.common.codec.formatter.SerializedObjectFormat#getStringListLength(List)}
   *
   * @param outputs
   * @return
   */
  public InOutStream putStringList(List<String> outputs) {
    putListSize(outputs.size());
    for (String output : outputs) {
      byte[] stringByte = InOutStream.write(output);
      elasticBuffer.putInt(stringByte.length);
      elasticBuffer.putArray(stringByte);
    }
    return this;
  }

  public InOutStream putStringSet(Set<String> outputs) {
    putListSize(outputs.size());
    for (String output : outputs) {
      byte[] stringByte = InOutStream.write(output);
      elasticBuffer.putInt(stringByte.length);
      elasticBuffer.putArray(stringByte);
    }
    return this;
  }

  public InOutStream putStringArray(String[] outputs) {
    putListSize(outputs.length);
    for (String output : outputs) {
      byte[] stringByte = InOutStream.write(output);
      elasticBuffer.putInt(stringByte.length);
      elasticBuffer.putArray(stringByte);
    }
    return this;
  }

  public static byte[] write(String output) {
    return output.getBytes(UTF_8);
  }

  public static String read(byte[] input) {
    return new String(input, UTF_8);
  }

  private byte transformBoolean(boolean outPut) {
    if (outPut) {
      return (byte) 1;
    }
    return (byte) 0;
  }

  private static boolean transformBoolean(byte input) {
    if (input == (byte) 1) {
      return true;
    }
    return false;
  }

  public byte[] getWrittenBuffer() {
    return elasticBuffer.getArray(0, elasticBuffer.position());
  }
}
