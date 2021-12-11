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
package org.myberry.common.codec.support;

import org.myberry.common.codec.MessageLite;

enum BasicType {
  INT(int.class, Integer.class, true),
  LONG(long.class, Long.class, true),
  FLOAT(float.class, Float.class, true),
  DOUBLE(double.class, Double.class, true),
  BOOLEAN(boolean.class, Boolean.class, true),
  STRING(String.class, String.class, false),
  MESSAGELITE(MessageLite.class, MessageLite.class, false);

  private final Class<?> type;
  private final Class<?> boxedType;
  private final boolean different;

  BasicType(Class<?> type, Class<?> boxedType, boolean different) {
    this.type = type;
    this.boxedType = boxedType;
    this.different = different;
  }

  public Class<?> getType() {
    return type;
  }

  public Class<?> getBoxedType() {
    return boxedType;
  }

  public boolean isDifferent() {
    return different;
  }

  public boolean isValidType(Class<?> t) {
    return type.isAssignableFrom(t) || boxedType.isAssignableFrom(t);
  }
}
