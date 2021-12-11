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
package org.myberry.server.ha;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.myberry.common.codec.LightCodec;
import org.myberry.common.codec.MessageLite;
import org.myberry.server.ha.collect.Collect;
import org.myberry.server.ha.database.Database;
import org.myberry.server.ha.quarum.Vote;

public class HATransfer {

  /*IDENTITY_PACKET is not listed in MESSAGE_TYPE_MAP*/
  public static final int IDENTITY_PACKET = 0x00;
  public static final int VOTE = 0x01;
  public static final int DATABASE = 0x02;
  public static final int COLLECT = 0x03;

  private static final Map<Integer, Class<? extends MessageLite>> MESSAGE_TYPE_MAP =
      new HashMap<>();

  static {
    MESSAGE_TYPE_MAP.put(VOTE, Vote.class);
    MESSAGE_TYPE_MAP.put(DATABASE, Database.class);
    MESSAGE_TYPE_MAP.put(COLLECT, Collect.class);
  }

  /**
   * @param byteBuffer
   * @param pos
   * @return message length
   */
  public static int headerDecode(final ByteBuffer byteBuffer, final int pos) {
    int header = byteBuffer.getInt(pos);
    return (header >>> 4) + 4;
  }

  public static HAMessage decode(final ByteBuffer byteBuffer, final int pos) {
    int header = byteBuffer.getInt(pos);
    if (header < 0) {
      return null;
    }

    int type = header & 0x0F;

    return bodyDecode(type, byteBuffer, pos);
  }

  private static HAMessage bodyDecode(int messageType, ByteBuffer byteBuffer, int pos) {
    HAMessage haMessage = new HAMessage();
    haMessage.setMessageType(messageType);

    int connId = byteBuffer.getInt(pos + 4);
    haMessage.setConnId(connId);

    int haMessageLength = byteBuffer.getInt(pos + 4 + 4);
    byte[] haMessageBytes = new byte[haMessageLength];
    System.arraycopy(byteBuffer.array(), pos + 4 + 4 + 4, haMessageBytes, 0, haMessageLength);
    MessageLite messageLite = LightCodec.toObj(haMessageBytes, MESSAGE_TYPE_MAP.get(messageType));
    haMessage.setHaMessage(messageLite);

    int dataLength = byteBuffer.getInt(pos + 4 + 4 + 4 + haMessageLength);
    byte[] data = new byte[dataLength];
    System.arraycopy(
        byteBuffer.array(), pos + 4 + 4 + 4 + haMessageLength + 4, data, 0, dataLength);
    haMessage.setData(data);

    return haMessage;
  }

  public static ByteBuffer encode(HAMessage haMessage) {
    byte[] bytes = LightCodec.toBytes(haMessage.getHaMessage());
    int haMessageLength = bytes == null ? 0 : bytes.length;
    int dataLength = haMessage.getData() == null ? 0 : haMessage.getData().length;
    int bodyLength =
        // sid
        4
            +
            // haMessage length
            4
            +
            // haMessage
            haMessageLength
            +
            // data length
            4
            +
            // data
            dataLength;
    byte[] body =
        bodyEncode(
            bodyLength,
            haMessage.getConnId(),
            haMessageLength,
            bytes,
            dataLength,
            haMessage.getData());

    int mt =
        getMessageTypeByClass(
            haMessage.getHaMessage() == null ? null : haMessage.getHaMessage().getClass());
    int header = headerEncode(bodyLength, mt);

    ByteBuffer byteBuffer = ByteBuffer.allocate(4 + bodyLength);
    byteBuffer.putInt(header);
    byteBuffer.put(body);
    byteBuffer.flip();
    return byteBuffer;
  }

  public static int getHaMessageLength(int haMessageLength, int dataLength) {
    return
    // header length
    4
        +
        // sid
        4
        +
        // haMessage length
        4
        +
        // haMessage
        haMessageLength
        +
        // data length
        4
        +
        // data
        dataLength;
  }

  private static int getMessageTypeByClass(Class clz) {
    int mt = IDENTITY_PACKET;
    if (null == clz) {
      return mt;
    }
    Iterator<Entry<Integer, Class<? extends MessageLite>>> it =
        MESSAGE_TYPE_MAP.entrySet().iterator();
    while (it.hasNext()) {
      Entry<Integer, Class<? extends MessageLite>> entry = it.next();
      if (entry.getValue() == clz) {
        mt = entry.getKey();
        break;
      }
    }
    return mt;
  }

  private static byte[] bodyEncode(
      int bodyLength, int sid, int haMessageLength, byte[] haMessage, int dataLength, byte[] data) {
    ByteBuffer bodyBuffer = ByteBuffer.allocate(bodyLength);
    bodyBuffer.putInt(sid);
    bodyBuffer.putInt(haMessageLength);
    if (haMessageLength > 0) {
      bodyBuffer.put(haMessage);
    }
    bodyBuffer.putInt(dataLength);
    if (dataLength > 0) {
      bodyBuffer.put(data);
    }
    return bodyBuffer.array();
  }

  /**
   * type max value is 7
   *
   * @param bodyLength
   * @param type
   * @return header value
   */
  private static int headerEncode(int bodyLength, int type) {
    return (bodyLength << 4) | type;
  }
}
