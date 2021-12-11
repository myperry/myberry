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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

public enum FieldType {
  INT(0, Collection.SCALAR, BasicType.INT),
  LONG(1, Collection.SCALAR, BasicType.LONG),
  FLOAT(2, Collection.SCALAR, BasicType.FLOAT),
  DOUBLE(3, Collection.SCALAR, BasicType.DOUBLE),
  BOOLEAN(4, Collection.SCALAR, BasicType.BOOLEAN),
  STRING(5, Collection.SCALAR, BasicType.STRING),
  MESSAGELITE(6, Collection.SCALAR, BasicType.MESSAGELITE),
  INT_PACKED(7, Collection.VECTOR, BasicType.INT),
  LONG_PACKED(8, Collection.VECTOR, BasicType.LONG),
  FLOAT_PACKED(9, Collection.VECTOR, BasicType.FLOAT),
  DOUBLE_PACKED(10, Collection.VECTOR, BasicType.DOUBLE),
  BOOLEAN_PACKED(11, Collection.VECTOR, BasicType.BOOLEAN),
  STRING_PACKED(12, Collection.VECTOR, BasicType.STRING),
  MESSAGELITE_PACKED(13, Collection.VECTOR, BasicType.MESSAGELITE);

  private final BasicType basicType;
  private final int id;
  private final Collection collection;
  private final Class<?> elementType;

  FieldType(int id, Collection collection, BasicType basicType) {
    this.id = id;
    this.collection = collection;
    this.basicType = basicType;

    switch (collection) {
      case VECTOR:
        elementType = basicType.getBoxedType();
        break;
      case SCALAR:
      default:
        elementType = null;
        break;
    }
  }

  public int id() {
    return id;
  }

  public Class<?> getType() {
    return basicType.getType();
  }

  public Class<?> getBoxedType() {
    return basicType.getBoxedType();
  }

  public boolean isDifferentBetweenTypeAndBoxedType() {
    return basicType.isDifferent();
  }

  public boolean isPacked() {
    return collection.isPacked();
  }

  public boolean isValidForField(Field field) {
    Class<?> clazz = field.getType();

    if (Collection.VECTOR.equals(collection)) {
      if (List.class.isAssignableFrom(clazz) || Set.class.isAssignableFrom(clazz)) {
        Type[] realTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        return elementType.isAssignableFrom((Class<?>) realTypes[0]);
      } else if (clazz.isArray()) {
        return basicType.isValidType(clazz.getComponentType());
      }
      return false;
    } else {
      return basicType.isValidType(field.getType());
    }
  }

  public static FieldType forId(int id) {
    if (id < 0 || id >= VALUES.length) {
      return null;
    }
    return VALUES[id];
  }

  private static final FieldType[] VALUES;

  static {
    FieldType[] values = values();
    VALUES = new FieldType[values.length];
    for (FieldType type : values) {
      VALUES[type.id] = type;
    }
  }

  enum Collection {
    SCALAR(false),
    VECTOR(true);

    private final boolean isPacked;

    Collection(boolean isPacked) {
      this.isPacked = isPacked;
    }

    public boolean isPacked() {
      return isPacked;
    }
  }
}
