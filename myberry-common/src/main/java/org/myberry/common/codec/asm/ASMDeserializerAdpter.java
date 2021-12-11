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

import java.util.HashMap;
import java.util.Map;
import org.myberry.common.codec.asm.deserializer.MessageLiteDeserializer;
import org.myberry.common.codec.util.TypeBean;

public class ASMDeserializerAdpter {

  private static final Map<String /*ClassName*/, Map<Integer /*serialNo*/, TypeBean>> serialNoMap =
      new HashMap<>();

  private static final ASMDeserializerAdpter asmDeserializerAdpter = new ASMDeserializerAdpter();
  private final ASMDeserializerFactory asmDeserializerFactory;

  private ASMDeserializerAdpter() {
    this.asmDeserializerFactory = new ASMDeserializerFactory(serialNoMap);
  }

  public static ASMDeserializerAdpter getInstance() {
    return asmDeserializerAdpter;
  }

  public MessageLiteDeserializer readTo(Class<?> clazz) throws Exception {
    return asmDeserializerFactory.getDeserializer(clazz);
  }

  public TypeBean getClassByFieldSerialNo(String clazzName, int serialNo) {
    return serialNoMap.get(clazzName).get(serialNo);
  }
}
