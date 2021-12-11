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

import static java.lang.Math.max;

import java.lang.ref.SoftReference;
import java.nio.BufferUnderflowException;
import java.nio.InvalidMarkException;

public class ElasticBuffer {

  private static final int MIN_CACHED_BUFFER_SIZE = 1024;

  private static final int MAX_CACHED_BUFFER_SIZE = 16 * 1024;

  private static final float BUFFER_REALLOCATION_THRESHOLD = 0.5f;

  private static final ThreadLocal<SoftReference<byte[]>> BUFFER = new ThreadLocal<>();

  private byte[] eb;
  private int offset;
  private int mark = -1;
  private int position = 0;
  private int limit;
  private int capacity;

  private ElasticBuffer(byte[] eb) {
    this(-1, 0, eb.length, eb.length);
    this.eb = eb;
  }

  private ElasticBuffer(int mark, int pos, int lim, int cap) {
    if (cap < 0) {
      throw new IllegalArgumentException("Negative capacity: " + cap);
    }
    this.capacity = cap;
    limit(lim);
    position(pos);
    if (mark >= 0) {
      if (mark > pos) {
        throw new IllegalArgumentException("mark > position: (" + mark + " > " + pos + ")");
      }
      this.mark = mark;
    }
  }

  private ElasticBuffer(int mark, int pos, int lim, int cap, byte[] eb, int offset) {
    this(mark, pos, lim, cap);
    this.eb = eb;
    this.offset = offset;
  }

  public final int capacity() {
    return capacity;
  }

  public final int position() {
    return position;
  }

  public final ElasticBuffer position(int newPosition) {
    if ((newPosition > limit) || (newPosition < 0)) {
      throw new IllegalArgumentException();
    }
    position = newPosition;
    if (mark > position) {
      mark = -1;
    }
    return this;
  }

  public final int limit() {
    return limit;
  }

  public final ElasticBuffer limit(int newLimit) {
    if ((newLimit > capacity) || (newLimit < 0)) {
      throw new IllegalArgumentException();
    }
    limit = newLimit;
    if (position > newLimit) {
      position = newLimit;
    }
    if (mark > newLimit) {
      mark = -1;
    }
    return this;
  }

  public final ElasticBuffer mark() {
    mark = position;
    return this;
  }

  public final ElasticBuffer reset() {
    int m = mark;
    if (m < 0) {
      throw new InvalidMarkException();
    }
    position = m;
    return this;
  }

  public final ElasticBuffer clear() {
    position = 0;
    limit = capacity;
    mark = -1;
    return this;
  }

  public final ElasticBuffer flip() {
    limit = position;
    position = 0;
    mark = -1;
    return this;
  }

  public final ElasticBuffer rewind() {
    position = 0;
    mark = -1;
    return this;
  }

  public final int remaining() {
    return limit - position;
  }

  public final boolean hasRemaining() {
    return position < limit;
  }

  public byte[] array() {
    return eb;
  }

  final int nextGetIndex() {
    int p = position;
    if (p >= limit) {
      throw new BufferUnderflowException();
    }
    position = p + 1;
    return p;
  }

  final int nextGetIndex(int nb) {
    int p = position;
    if (limit - p < nb) {
      throw new BufferUnderflowException();
    }
    position = p + nb;
    return p;
  }

  final int nextPutIndex() {
    int p = position;
    if (p >= limit) {
      int newCapacity = expandCapacity(1);
      limit = newCapacity;
      capacity = newCapacity;
    }
    position = p + 1;
    return p;
  }

  final int nextPutIndex(int nb) {
    int p = position;
    if (limit - p < nb) {
      int newCapacity = expandCapacity(nb);
      limit = newCapacity;
      capacity = newCapacity;
    }
    position = p + nb;
    return p;
  }

  final int checkIndex(int i) {
    if ((i < 0) || (i >= limit)) {
      throw new IndexOutOfBoundsException();
    }
    return i;
  }

  final int checkIndex(int i, int nb) {
    if ((i < 0) || (nb > limit - i)) {
      throw new IndexOutOfBoundsException();
    }
    return i;
  }

  // -- get/put --

  byte _get(int i) {
    return eb[i];
  }

  void _put(int i, byte b) {
    eb[i] = b;
  }

  // -- get/put byte --

  public byte get() {
    return eb[nextGetIndex()];
  }

  public byte get(int i) {
    return eb[checkIndex(i)];
  }

  public ElasticBuffer put(byte x) {
    eb[nextPutIndex()] = x;
    return this;
  }

  public ElasticBuffer put(int i, byte x) {
    eb[checkIndex(i)] = x;
    return this;
  }

  // -- get/put char --

  char _getChar(int bi) {
    return (char) ((_get(bi) << 8) | (_get(bi + 1) & 0xff));
  }

  void _putChar(int bi, char x) {
    _put(bi, (byte) (x >> 8));
    _put(bi + 1, (byte) (x));
  }

  public char getChar() {
    return _getChar(nextGetIndex(2));
  }

  public char getChar(int i) {
    return _getChar(checkIndex(i, 2));
  }

  public ElasticBuffer putChar(char x) {
    _putChar(nextPutIndex(2), x);
    return this;
  }

  public ElasticBuffer putChar(int i, char x) {
    _putChar(checkIndex(i, 2), x);
    return this;
  }

  // -- get/put short --

  short _getShort(int bi) {
    return (short) ((_get(bi) << 8) | (_get(bi + 1) & 0xff));
  }

  void _putShort(int bi, short x) {
    _put(bi, (byte) (x >> 8));
    _put(bi + 1, (byte) (x));
  }

  public short getShort() {
    return _getShort(nextGetIndex(2));
  }

  public short getShort(int i) {
    return _getShort(checkIndex(i, 2));
  }

  public ElasticBuffer putShort(short x) {
    _putShort(nextPutIndex(2), x);
    return this;
  }

  public ElasticBuffer putShort(int i, short x) {
    _putShort(checkIndex(i, 2), x);
    return this;
  }

  // -- get/put int --

  int _getInt(int bi) {
    return (((_get(bi)) << 24)
        | ((_get(bi + 1) & 0xff) << 16)
        | ((_get(bi + 2) & 0xff) << 8)
        | ((_get(bi + 3) & 0xff)));
  }

  void _putInt(int bi, int x) {
    _put(bi, (byte) (x >> 24));
    _put(bi + 1, (byte) (x >> 16));
    _put(bi + 2, (byte) (x >> 8));
    _put(bi + 3, (byte) (x));
  }

  public int getInt() {
    return _getInt(nextGetIndex(4));
  }

  public int getInt(int i) {
    return _getInt(checkIndex(i, 4));
  }

  public ElasticBuffer putInt(int x) {
    _putInt(nextPutIndex(4), x);
    return this;
  }

  public ElasticBuffer putInt(int i, int x) {
    _putInt(checkIndex(i, 4), x);
    return this;
  }

  // -- get/put long --

  long _getLong(int bi) {
    return (((long) (_get(bi)) << 56)
        | ((long) (_get(bi + 1) & 0xff) << 48)
        | ((long) (_get(bi + 2) & 0xff) << 40)
        | ((long) (_get(bi + 3) & 0xff) << 32)
        | ((long) (_get(bi + 4) & 0xff) << 24)
        | ((long) (_get(bi + 5) & 0xff) << 16)
        | ((long) (_get(bi + 6) & 0xff) << 8)
        | ((long) (_get(bi + 7) & 0xff)));
  }

  void _putLong(int bi, long x) {
    _put(bi, (byte) (x >> 56));
    _put(bi + 1, (byte) (x >> 48));
    _put(bi + 2, (byte) (x >> 40));
    _put(bi + 3, (byte) (x >> 32));
    _put(bi + 4, (byte) (x >> 24));
    _put(bi + 5, (byte) (x >> 16));
    _put(bi + 6, (byte) (x >> 8));
    _put(bi + 7, (byte) (x));
  }

  public long getLong() {
    return _getLong(nextGetIndex(8));
  }

  public long getLong(int i) {
    return _getLong(checkIndex(i, 8));
  }

  public ElasticBuffer putLong(long x) {
    _putLong(nextPutIndex(8), x);
    return this;
  }

  public ElasticBuffer putLong(int i, long x) {
    _putLong(checkIndex(i, 8), x);
    return this;
  }

  // -- get/put float --

  public float getFloat() {
    return Float.intBitsToFloat(_getInt(nextGetIndex(4)));
  }

  public float getFloat(int i) {
    return Float.intBitsToFloat(_getInt(checkIndex(i, 4)));
  }

  public ElasticBuffer putFloat(float x) {
    _putInt(nextPutIndex(4), Float.floatToRawIntBits(x));
    return this;
  }

  public ElasticBuffer putFloat(int i, float x) {
    _putInt(checkIndex(i, 4), Float.floatToRawIntBits(x));
    return this;
  }

  // -- get/put double --

  public double getDouble() {
    return Double.longBitsToDouble(_getLong(nextGetIndex(8)));
  }

  public double getDouble(int i) {
    return Double.longBitsToDouble(_getLong(checkIndex(i, 8)));
  }

  public ElasticBuffer putDouble(double x) {
    _putLong(nextPutIndex(8), Double.doubleToRawLongBits(x));
    return this;
  }

  public ElasticBuffer putDouble(int i, double x) {
    _putLong(checkIndex(i, 8), Double.doubleToRawLongBits(x));
    return this;
  }

  // -- get/put byte[] --

  public byte[] getArray(int length) {
    byte[] dest = new byte[length];
    System.arraycopy(eb, nextGetIndex(length), dest, 0, length);
    return dest;
  }

  public byte[] getArray(int i, int length) {
    byte[] dest = new byte[length];
    System.arraycopy(eb, checkIndex(i, length), dest, 0, length);
    return dest;
  }

  public ElasticBuffer putArray(byte[] x) {
    System.arraycopy(x, 0, eb, nextPutIndex(x.length), x.length);
    return this;
  }

  public ElasticBuffer putArray(int i, byte[] x) {
    System.arraycopy(x, 0, eb, checkIndex(i, x.length), x.length);
    return this;
  }

  private int expandCapacity(int expandedSize) {
    int expectedSize = eb.length + (eb.length >> 1) + expandedSize;
    byte[] buffer = new byte[expectedSize];
    System.arraycopy(eb, 0, buffer, 0, position);

    if (expectedSize <= MAX_CACHED_BUFFER_SIZE) {
      setBuffer(buffer);
    }
    this.eb = buffer;
    return expectedSize;
  }

  public static ElasticBuffer getOrCreateElasticBuffer(int requestedSize) {
    requestedSize = max(requestedSize, MIN_CACHED_BUFFER_SIZE);

    byte[] buffer = getBuffer();

    if (buffer == null || needToReallocate(requestedSize, buffer.length)) {
      buffer = new byte[requestedSize];

      if (requestedSize <= MAX_CACHED_BUFFER_SIZE) {
        setBuffer(buffer);
      }
    }
    return new ElasticBuffer(buffer);
  }

  private static boolean needToReallocate(int requestedSize, int bufferLength) {
    return bufferLength < requestedSize
        && bufferLength < requestedSize * BUFFER_REALLOCATION_THRESHOLD;
  }

  private static byte[] getBuffer() {
    SoftReference<byte[]> sr = BUFFER.get();
    return sr == null ? null : sr.get();
  }

  private static void setBuffer(byte[] value) {
    BUFFER.set(new SoftReference<byte[]>(value));
  }

  public static ElasticBuffer wrap(byte[] array, int offset, int length) {
    try {
      return new ElasticBuffer(-1, offset, offset + length, array.length, array, 0);
    } catch (IllegalArgumentException x) {
      throw new IndexOutOfBoundsException();
    }
  }

  public static ElasticBuffer wrap(byte[] array) {
    return wrap(array, 0, array.length);
  }
}
