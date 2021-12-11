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
package org.myberry.store.impl;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import org.myberry.store.AbstractComponent;
import org.myberry.store.common.LoggerName;
import org.myberry.store.config.StoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreComponent {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);

  private final MappedByteBuffer mappedByteBuffer;

  public StoreComponent(final MappedByteBuffer mappedByteBuffer) {
    this.mappedByteBuffer = mappedByteBuffer;
  }

  public void load(final AbstractComponent abstractComponent)
      throws UnsupportedEncodingException, IllegalAccessException {
    load(abstractComponent, mappedByteBuffer);
  }

  public static void load(final AbstractComponent abstractComponent, final ByteBuffer byteBuffer)
      throws IllegalAccessException, UnsupportedEncodingException {
    Field[] abstractComponentFields = getAbstractComponentFields(abstractComponent);

    int preValue = 0;
    for (Field field : abstractComponentFields) {
      if (!Modifier.isStatic(field.getModifiers())
          && !Modifier.isFinal(field.getModifiers())
          && !Modifier.isTransient(field.getModifiers())) {
        String type = field.getType().getSimpleName();
        String name = field.getName();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

        Method method = null;
        try {
          if (type.equals("byte") || type.equals("Byte")) {
            method = abstractComponent.getClass().getMethod("set" + name, byte.class);
            method.invoke(abstractComponent, byteBuffer.get());
          } else if (type.equals("short") || type.equals("Short")) {
            preValue = byteBuffer.getShort();
            method = abstractComponent.getClass().getMethod("set" + name, short.class);
            method.invoke(abstractComponent, (short) preValue);
          }
          if (type.equals("int") || type.equals("Integer") || type.equals("AtomicInteger")) {
            method = abstractComponent.getClass().getMethod("set" + name, int.class);
            method.invoke(abstractComponent, byteBuffer.getInt());
          } else if (type.equals("long") || type.equals("Long") || type.equals("AtomicLong")) {
            method = abstractComponent.getClass().getMethod("set" + name, long.class);
            method.invoke(abstractComponent, byteBuffer.getLong());
          } else if (type.equals("String")) {
            byte[] dest = new byte[preValue];
            for (int i = 0; i < dest.length; i++) {
              dest[i] = byteBuffer.get();
            }
            String str = new String(dest, StoreConfig.MYBERRY_STORE_DEFAULT_CHARSET);
            method = abstractComponent.getClass().getMethod("set" + name, String.class);
            method.invoke(abstractComponent, str);
          }
        } catch (NoSuchMethodException e) {
          log.error("load reflection exception: ", e);
        } catch (SecurityException e) {
          log.error("load exception: ", e);
        } catch (InvocationTargetException e) {
          log.error("load invocation exception: ", e);
        }
      }
    }
  }

  public void write(final AbstractComponent abstractComponent) {
    write(abstractComponent, mappedByteBuffer);
  }

  public static void write(final AbstractComponent abstractComponent, final ByteBuffer byteBuffer) {
    Field[] abstractComponentFields = getAbstractComponentFields(abstractComponent);

    for (Field field : abstractComponentFields) {

      if (!Modifier.isStatic(field.getModifiers())
          && !Modifier.isFinal(field.getModifiers())
          && !Modifier.isTransient(field.getModifiers())) {
        field.setAccessible(true);
        try {
          String value = field.get(abstractComponent).toString();

          String fieldTypeName = field.getType().getSimpleName();
          if (fieldTypeName.equals("String")) {
            byte[] src = value.getBytes(StoreConfig.MYBERRY_STORE_DEFAULT_CHARSET);
            for (int i = 0; i < src.length; i++) {
              byteBuffer.put(src[i]);
            }
          } else if (fieldTypeName.equals("byte") || fieldTypeName.equals("Byte")) {
            byteBuffer.put(Byte.parseByte(value));
          } else if (fieldTypeName.equals("short") || fieldTypeName.equals("Short")) {
            byteBuffer.putShort(Short.parseShort(value));
          } else if (fieldTypeName.equals("int")
              || fieldTypeName.equals("Integer")
              || fieldTypeName.equals("AtomicInteger")) {
            byteBuffer.putInt(Integer.parseInt(value));
          } else if (fieldTypeName.equals("long")
              || fieldTypeName.equals("Long")
              || fieldTypeName.equals("AtomicLong")) {
            byteBuffer.putLong(Long.parseLong(value));
          }
        } catch (IllegalArgumentException e) {
          log.error("load reflection exception: ", e);
        } catch (IllegalAccessException e) {
          log.error("load access exception: ", e);
        } catch (UnsupportedEncodingException e) {
          log.error("load encoding exception: ", e);
        }
      }
    }
  }

  public static Field[] getAbstractComponentFields(final AbstractComponent abstractComponent) {
    Class<? extends AbstractComponent> cls = abstractComponent.getClass();
    return cls.getDeclaredFields();
  }

  public static void put(ByteBuffer byteBuffer, int offset, byte[] src) {
    for (int i = 0; i < src.length; i++) {
      byteBuffer.put(offset + i, src[i]);
    }
  }

  public static byte[] get(ByteBuffer byteBuffer, int offset, byte[] dest) {
    for (int i = 0; i < dest.length; i++) {
      dest[i] = byteBuffer.get(offset + i);
    }
    return dest;
  }
}
