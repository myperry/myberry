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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import org.myberry.store.common.LoggerName;
import org.myberry.store.config.StoreConfig;
import org.myberry.store.config.StorePathConfigHelper;
import org.myberry.store.impl.FileService;
import org.myberry.store.impl.MappedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultMyberryStore implements MyberryStore {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);

  private volatile boolean shutdown = false;

  private final StoreConfig storeConfig;
  private final FileService fileService;
  private RandomAccessFile lockFile;

  private FileLock lock;

  private volatile long beginTimeInLock = 0;

  public DefaultMyberryStore(final StoreConfig storeConfig) throws IOException {
    this.storeConfig = storeConfig;
    this.fileService = new FileService(storeConfig);
    this.initProcessLock();
  }

  @Override
  public void addComponent(AbstractComponent abstractComponent) {
    fileService.addComponent(abstractComponent);
  }

  @Override
  public void modifyComponent(NSComponent nsc) {
    fileService.modifyComponent(nsc);
  }

  @Override
  public long getLogicOffset() {
    return fileService.getLogicOffset();
  }

  @Override
  public int getMySid(int blockIndex) {
    return fileService.getMySid(blockIndex);
  }

  @Override
  public StoreConfig getStoreConfig() {
    return storeConfig;
  }

  @Override
  public ConcurrentMap<String, AbstractComponent> getComponentMap() {
    return fileService.getComponentMap();
  }

  @Override
  public List<BlockHeader> getBlockHeaderList() {
    return fileService.getBlockHeaderList();
  }

  @Override
  public boolean verifyOffset(int blockIndex, int expectedEndPhyOffset) {
    return fileService.verifyOffset(blockIndex, expectedEndPhyOffset);
  }

  @Override
  public int getMaxBlockIndex() {
    return fileService.getMaxBlockIndex();
  }

  @Override
  public int getSyncLength(int blockIndex, int markOffset, int allowedSyncLength) {
    return fileService.getSyncLength(blockIndex, markOffset, allowedSyncLength);
  }

  @Override
  public byte[] getSyncData(int blockIndex, int markOffset, int length) {
    return fileService.getSyncData(blockIndex, markOffset, length);
  }

  @Override
  public int getLastPosition() {
    return fileService.getBlockFileLastPosition();
  }

  @Override
  public int getLastPosition(int blockIndex) {
    return fileService.getBlockFileLastPosition(blockIndex);
  }

  @Override
  public void updateBufferLong(int blockIndex, int index, long value) {
    fileService.updateBufferLong(blockIndex, index, value);
  }

  @Override
  public void updateBufferInt(int blockIndex, int index, int value) {
    fileService.updateBufferInt(blockIndex, index, value);
  }

  @Override
  public void start() throws Exception {
    this.startProcessLock();
    this.fileService.load();
  }

  @Override
  public void shutdown() {
    if (!this.shutdown) {
      this.shutdown = true;
      this.fileService.unload();
    }

    this.closeProcessLock();
  }

  @Override
  public boolean isOSPageCacheBusy() {
    long diff = System.currentTimeMillis() - beginTimeInLock;

    return diff < 10000000 && diff > this.storeConfig.getOsPageCacheBusyTimeOutMills();
  }

  private void initProcessLock() throws IOException {
    File file = new File(StorePathConfigHelper.getLockFile(storeConfig.getStoreRootDir()));
    MappedFile.ensureDirOK(file.getParent());
    lockFile = new RandomAccessFile(file, "rw");
  }

  private void startProcessLock() throws IOException {
    lock = lockFile.getChannel().tryLock(0, 1, false);

    if (lock == null || lock.isShared() || !lock.isValid()) {
      throw new RuntimeException("Lock failed, Myberry already started");
    }

    lockFile.getChannel().write(ByteBuffer.wrap("lock".getBytes()));
    lockFile.getChannel().force(true);
  }

  private void closeProcessLock() {
    if (lockFile != null && lock != null) {
      try {
        lock.release();
        lockFile.close();
      } catch (IOException e) {
        log.error("Myberry file close fail: ", e);
      }
    }
  }

  @Override
  public void setBeginTimeInLock(long beginTimeInLock) {
    this.beginTimeInLock = beginTimeInLock;
  }
}
