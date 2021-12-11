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

import java.util.List;

public final class SerializedObjectFormat {

  private static final int FIXED8_SIZE = 1;

  private static final int FIXED32_SIZE = 4;
  private static final int FIXED64_SIZE = 8;

  /*
   * Tag Length = SerialNo(short) + FieldType ID(short)
   *
   * @return Tag Length
   */
  public static int getTagLength() {
    return 2 + 2;
  }

  public static int getIntLength() {
    return FIXED32_SIZE;
  }

  public static int getFloatLength() {
    return FIXED32_SIZE;
  }

  public static int getLongLength() {
    return FIXED64_SIZE;
  }

  public static int getDoubleLength() {
    return FIXED64_SIZE;
  }

  public static int getBooleanLength() {
    return FIXED8_SIZE;
  }

  /*
   * String Length = Field Value Size(int) + Field Value(string)
   *
   * @param fieldValue
   * @return
   */
  public static int getStringLength(String fieldValue) {
    return 4 + InOutStream.write(fieldValue).length;
  }

  public static int getMessageLiteLength() {
    return FIXED32_SIZE;
  }

  /*
   * List<Integer> Length = List Capacity(int) + Value Length
   *
   * @param listSize
   * @return
   */
  public static int getIntListLength(int listSize) {
    return 4 + getIntLength() * listSize;
  }

  /*
   * List<Long> Length = List Capacity(int) + Value Length
   *
   * @param listSize
   * @return
   */
  public static int getLongListLength(int listSize) {
    return 4 + getLongLength() * listSize;
  }

  /*
   * List<Float> Length = List Capacity(int) + Value Length
   *
   * @param listSize
   * @return
   */
  public static int getFloatListLength(int listSize) {
    return 4 + getFloatLength() * listSize;
  }

  /*
   * List<Double> Length = List Capacity(int) + Value Length
   *
   * @param listSize
   * @return
   */
  public static int getDoubleListLength(int listSize) {
    return 4 + getDoubleLength() * listSize;
  }

  /*
   * List<Boolean> Length = List Capacity(int) + Value Length
   *
   * @param listSize
   * @return
   */
  public static int getBooleanListLength(int listSize) {
    return 4 + getBooleanLength() * listSize;
  }

  /*
   * List<String> Length = List Capacity(int) + List Length
   *
   * @param fieldValues
   * @return
   */
  public static int getStringListLength(List<String> lists) {
    int listLength = 0;
    for (String fieldValue : lists) {
      listLength += getStringLength(fieldValue);
    }
    return 4 + listLength;
  }

  /*
   * List<Integer> Length = List Capacity(int) + Value Length
   *
   * @param listSize
   * @return
   */
  public static int getMessageLiteListLength(int messageLiteListLength) {
    return 4 + messageLiteListLength;
  }
}
