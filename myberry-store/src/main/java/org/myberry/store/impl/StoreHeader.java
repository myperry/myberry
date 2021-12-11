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

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StoreHeader {

  public static final int STORE_HEADER_SIZE = 32;
  private static int beginTimestampHeader = 0;
  private static int endTimestampHeader = 8;
  private static int beginPhyOffsetHeader = 16;
  private static int endPhyOffsetHeader = 20;
  private static int mySidHeader = 24;
  private static int componentCountHeader = 28;

  private final ByteBuffer byteBuffer;

  private AtomicLong beginTimestamp = new AtomicLong(0);
  private AtomicLong endTimestamp = new AtomicLong(0);
  private AtomicInteger beginPhyOffset = new AtomicInteger(0);
  private AtomicInteger endPhyOffset = new AtomicInteger(0);
  private AtomicInteger mySid = new AtomicInteger();
  private AtomicInteger componentCount = new AtomicInteger();

  public StoreHeader(final ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
  }

  public void load() {
    this.setBeginTimestamp(byteBuffer.getLong(beginTimestampHeader));
    this.setEndTimestamp(byteBuffer.getLong(endTimestampHeader));
    this.setBeginPhyOffset(byteBuffer.getInt(beginPhyOffsetHeader));
    this.setEndPhyOffset(byteBuffer.getInt(endPhyOffsetHeader));
    this.setMySid(byteBuffer.getInt(mySidHeader));
    this.setComponentCount(byteBuffer.getInt(componentCountHeader));
  }

  public long getBeginTimestamp() {
    return beginTimestamp.get();
  }

  public void setBeginTimestamp(long beginTimestamp) {
    this.beginTimestamp.set(beginTimestamp);
    this.byteBuffer.putLong(beginTimestampHeader, beginTimestamp);
  }

  public long getEndTimestamp() {
    return endTimestamp.get();
  }

  public void setEndTimestamp(long endTimestamp) {
    this.endTimestamp.set(endTimestamp);
    this.byteBuffer.putLong(endTimestampHeader, endTimestamp);
  }

  public int getBeginPhyOffset() {
    return beginPhyOffset.get();
  }

  public void setBeginPhyOffset(int beginPhyOffset) {
    this.beginPhyOffset.set(beginPhyOffset);
    this.byteBuffer.putInt(beginPhyOffsetHeader, beginPhyOffset);
  }

  public int getEndPhyOffset() {
    return endPhyOffset.get();
  }

  public void setEndPhyOffset(int endPhyOffset) {
    this.endPhyOffset.set(endPhyOffset);
    this.byteBuffer.putInt(endPhyOffsetHeader, endPhyOffset);
  }

  public int getMySid() {
    return mySid.get();
  }

  public void setMySid(int mySid) {
    this.mySid.set(mySid);
    this.byteBuffer.putInt(mySidHeader, mySid);
  }

  public int getComponentCount() {
    return componentCount.get();
  }

  public void setComponentCount(int componentCount) {
    this.componentCount.set(componentCount);
    this.byteBuffer.putInt(componentCountHeader, componentCount);
  }

  public void incrComponentCount() {
    int i = this.componentCount.incrementAndGet();
    this.byteBuffer.putInt(componentCountHeader, i);
  }

  public ByteBuffer getByteBuffer() {
    return byteBuffer;
  }
}
