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
package org.myberry.store;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import org.myberry.store.config.StoreConfig;

/**
 * This class defines contracting interfaces to implement, allowing third-party vendor to use
 * customized component store.
 */
public interface MyberryStore {

  /**
   * Launch this component store.
   *
   * @throws Exception if there is any error.
   */
  void start() throws Exception;

  /** Shutdown this component store. */
  void shutdown();

  /** Add a component into store. */
  void addComponent(AbstractComponent abstractComponent);

  /** Modify a component into store. */
  void modifyComponent(NSComponent nsc);

  /** Got logic offset. */
  long getLogicOffset();

  /** Got the stored offset. */
  int getMySid(int blockIndex);

  /** Got Store Config. */
  StoreConfig getStoreConfig();

  /**
   * Got component map.
   *
   * @return
   */
  ConcurrentMap<String, AbstractComponent> getComponentMap();

  /**
   * Got synchronize block header list.
   *
   * @return
   */
  List<BlockHeader> getBlockHeaderList();

  /**
   * Verify offset
   *
   * @param blockIndex
   * @param expectedEndPhyOffset
   * @return
   */
  boolean verifyOffset(int blockIndex, int expectedEndPhyOffset);

  /**
   * Got maxBlockIndex
   *
   * @return
   */
  int getMaxBlockIndex();

  /**
   * Got synchronize length.
   *
   * @param blockIndex
   * @param markOffset
   * @param allowedSyncLength
   * @return
   */
  int getSyncLength(int blockIndex, int markOffset, int allowedSyncLength);

  /**
   * Got synchronize data.
   *
   * @param blockIndex
   * @param markOffset
   * @param length
   * @return
   */
  byte[] getSyncData(int blockIndex, int markOffset, int length);

  /**
   * Got byteBuffer last position in the last block file.
   *
   * @return
   */
  int getLastPosition();

  /**
   * Got byteBuffer last position in the specified block file.
   *
   * @param blockIndex
   * @return
   */
  int getLastPosition(int blockIndex);

  /**
   * Update long buffer
   *
   * @param index
   * @param value
   */
  void updateBufferLong(int blockIndex, int index, long value);

  /**
   * Update int buffer
   *
   * @param index
   * @param value
   */
  void updateBufferInt(int blockIndex, int index, int value);

  /**
   * Check if the operation system page cache is busy or not.
   *
   * @return true if the OS page cache is busy; false otherwise.
   */
  boolean isOSPageCacheBusy();

  /**
   * Set flush disk begin time.
   *
   * @param beginTimeInLock
   */
  void setBeginTimeInLock(long beginTimeInLock);
}
