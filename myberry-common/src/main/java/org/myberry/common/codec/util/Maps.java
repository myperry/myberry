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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class Maps {

  private static final Charset UTF_8 = Charset.forName("UTF-8");

  public static byte[] serialize(HashMap<String, String> map) {
    // keySize+key+valSize+val
    if (null == map || map.isEmpty()) return null;

    int totalLength = 0;
    int kvLength;
    Iterator<Entry<String, String>> it = map.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, String> entry = it.next();
      if (entry.getKey() != null && entry.getValue() != null) {
        kvLength =
            // keySize + Key
            2
                + entry.getKey().getBytes(UTF_8).length
                // valSize + val
                + 4
                + entry.getValue().getBytes(UTF_8).length;
        totalLength += kvLength;
      }
    }

    ByteBuffer content = ByteBuffer.allocate(totalLength);
    byte[] key;
    byte[] val;
    it = map.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, String> entry = it.next();
      if (entry.getKey() != null && entry.getValue() != null) {
        key = entry.getKey().getBytes(UTF_8);
        val = entry.getValue().getBytes(UTF_8);

        content.putShort((short) key.length);
        content.put(key);

        content.putInt(val.length);
        content.put(val);
      }
    }

    return content.array();
  }

  public static HashMap<String, String> deserialize(byte[] bytes) {
    if (bytes == null || bytes.length <= 0) return null;

    HashMap<String, String> map = new HashMap<String, String>();
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

    short keySize;
    byte[] keyContent;
    int valSize;
    byte[] valContent;
    while (byteBuffer.hasRemaining()) {
      keySize = byteBuffer.getShort();
      keyContent = new byte[keySize];
      byteBuffer.get(keyContent);

      valSize = byteBuffer.getInt();
      valContent = new byte[valSize];
      byteBuffer.get(valContent);

      map.put(new String(keyContent, UTF_8), new String(valContent, UTF_8));
    }
    return map;
  }
}
