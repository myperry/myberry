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
package org.myberry.common.codec.asm;

import java.lang.reflect.Field;
import jdk.internal.org.objectweb.asm.Type;
import org.myberry.common.codec.support.FieldType;

public class ASMUtils {

  private static final char C_DOT = '.';
  private static final char C_SLASH = '/';

  private static final String L = "L";
  private static final String LEFT_BRACKET = "(";
  private static final String RIGHT_BRACKET = ")";
  private static final String SEMICOLON = ";";
  private static final String DOT = ".";
  private static final String SLASH = "/";

  private static final String IS = "is";
  private static final String GET = "get";
  private static final String SET = "set";

  public static ASMType generateASMClassTypeName(String packageName, String clazzName) {
    ASMType asmType = new ASMType();
    asmType.setName(generateASMClassName(packageName, clazzName));
    asmType.setSimpleName(clazzName);
    asmType.setNameType(generateASMNameType(packageName, clazzName));
    return asmType;
  }

  public static String generateASMNameType(String packageName, String clazzName) {
    StringBuilder builder = new StringBuilder();
    builder.append(packageName.replace(C_DOT, C_SLASH));
    builder.append(SLASH);
    builder.append(clazzName);
    return builder.toString();
  }

  public static String generateASMClassName(String packageName, String clazzName) {
    StringBuilder builder = new StringBuilder();
    builder.append(packageName);
    builder.append(DOT);
    builder.append(clazzName);
    return builder.toString();
  }

  public static String getMethodDescriptor(String... parameters) {
    return getMethodDescriptor(Type.VOID_TYPE, parameters);
  }

  public static String getMethodDescriptor(Type xReturn, String... parameters) {
    StringBuilder builder = new StringBuilder();
    builder.append(LEFT_BRACKET);
    if (parameters != null && parameters.length != 0) {
      for (int i = 0; i < parameters.length; i++) {
        builder.append(parameters[i]);
      }
    }
    builder.append(RIGHT_BRACKET);
    builder.append(xReturn.getDescriptor());
    return builder.toString();
  }

  public static String getFieldGetterMethodName(Field filed) {
    StringBuilder getBuilder = new StringBuilder();
    if (filed.getType() == FieldType.BOOLEAN.getType()) {
      getBuilder.append(IS);
    } else {
      getBuilder.append(GET);
    }
    getBuilder.append(filed.getName().substring(0, 1).toUpperCase());
    getBuilder.append(filed.getName().substring(1));
    return getBuilder.toString();
  }

  public static String getFieldSetterMethodName(Field filed) {
    StringBuilder setBuilder = new StringBuilder();
    setBuilder.append(SET);
    setBuilder.append(filed.getName().substring(0, 1).toUpperCase());
    setBuilder.append(filed.getName().substring(1));
    return setBuilder.toString();
  }
}
