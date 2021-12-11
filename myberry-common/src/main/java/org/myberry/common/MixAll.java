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
package org.myberry.common;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Properties;

public class MixAll {

  private MixAll() {}

  public static final String MYBERRY_HOME_ENV = "MYBERRY_HOME";
  public static final String MYBERRY_HOME_PROPERTY = "myberry.home.dir";
  public static final String MYBERRY_ADDR_ENV = "HA_SERVER_ADDR";
  public static final String MYBERRY_ADDR_PROPERTY = "myberry.ha.server.addr";

  public static void properties2Object(final Properties p, final Object object) {
    Method[] methods = object.getClass().getMethods();
    for (Method method : methods) {
      String mn = method.getName();
      if (mn.startsWith("set")) {
        try {
          String tmp = mn.substring(4);
          String first = mn.substring(3, 4);

          String key = first.toLowerCase() + tmp;
          String property = p.getProperty(key);
          if (property != null) {
            Class<?>[] pt = method.getParameterTypes();
            if (pt != null && pt.length > 0) {
              String cn = pt[0].getSimpleName();
              Object arg = null;
              if (cn.equals("int") || cn.equals("Integer")) {
                arg = Integer.parseInt(property);
              } else if (cn.equals("long") || cn.equals("Long")) {
                arg = Long.parseLong(property);
              } else if (cn.equals("double") || cn.equals("Double")) {
                arg = Double.parseDouble(property);
              } else if (cn.equals("boolean") || cn.equals("Boolean")) {
                arg = Boolean.parseBoolean(property);
              } else if (cn.equals("float") || cn.equals("Float")) {
                arg = Float.parseFloat(property);
              } else if (cn.equals("String")) {
                arg = property;
              } else {
                continue;
              }
              method.invoke(object, arg);
            }
          }
        } catch (Throwable ignored) {
        }
      }
    }
  }

  public static boolean isBlank(String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static boolean isEmpty(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  public static String responseCode2String(final int code) {
    return Integer.toString(code);
  }
}
