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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IllegalFormatFlagsException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.myberry.store.AbstractComponent;
import org.myberry.store.BlockHeader;
import org.myberry.store.NSComponent;
import org.myberry.store.common.LoggerName;
import org.myberry.store.config.StoreConfig;
import org.myberry.store.config.StorePathConfigHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileService {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);

  private final ConcurrentMap<String /* key */, AbstractComponent> componentMap =
      new ConcurrentHashMap<>();
  private final AtomicInteger blockFileIndex = new AtomicInteger(0);

  private final ConcurrentMap<Integer /* blockIndex */, CopyOnWriteArrayList<String /* key */>>
      syncDataMap = new ConcurrentHashMap<>();

  private final StoreConfig storeConfig;
  private ArrayList<BlockFile> blockFileList = new ArrayList<>();
  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  public FileService(final StoreConfig storeConfig) {
    this.storeConfig = storeConfig;
  }

  public boolean load() {
    String storeFilePath = StorePathConfigHelper.getStoreFilePath(storeConfig.getStoreRootDir());
    File dir = new File(storeFilePath);
    File[] files = dir.listFiles();
    if (files != null) {
      FileBlock[] fileBlocks = sort(files);
      for (FileBlock fileBlock : fileBlocks) {
        try {
          BlockFile f =
              new BlockFile(
                  storeFilePath + File.separator + fileBlock.getFile().getName(),
                  storeConfig.getBlockFileSize(),
                  false,
                  storeConfig.getMySid(),
                  fileBlock.getIndex(),
                  componentMap,
                  syncDataMap);
          f.load();

          int blockIndex = blockFileIndex.getAndIncrement();
          if (blockIndex != fileBlock.getIndex()) {
            throw new IllegalFormatFlagsException(
                "blockIndex is not continuous, fileBlockIndex="
                    + fileBlock.getIndex()
                    + ", blockIndex="
                    + blockIndex);
          }

          log.info("load block file OK, " + fileBlock.getFile().getName());
          this.blockFileList.add(f);
        } catch (IOException e) {
          log.error("load file {} error", fileBlock.getFile(), e);
          return false;
        }
      }
    }

    return true;
  }

  private FileBlock[] sort(File[] files) {
    List<FileBlock> fileBlock = new ArrayList<>(files.length);

    for (int i = 0; i < files.length; i++) {
      fileBlock.add(
          new FileBlock(
              Integer.parseInt(
                  files[i].getName().split(StoreConfig.MYBERRY_STORE_FILE_NAME_DELIMITER)[1]),
              files[i]));
    }

    FileBlockComparator fileBlockComparator = new FileBlockComparator();
    fileBlock.sort(fileBlockComparator);

    return fileBlock.toArray(new FileBlock[files.length]);
  }

  private static class FileBlockComparator implements Comparator<FileBlock> {

    @Override
    public int compare(FileBlock o1, FileBlock o2) {
      if (o1.getIndex() < o2.getIndex()) {
        return -1;
      } else if (o1.getIndex() > o2.getIndex()) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  private static class FileBlock {
    private int index;
    private File file;

    public FileBlock(int index, File file) {
      this.index = index;
      this.file = file;
    }

    public int getIndex() {
      return index;
    }

    public File getFile() {
      return file;
    }
  }

  public void unload() {
    this.readWriteLock.writeLock().lock();
    try {
      for (BlockFile f : blockFileList) {
        f.unload();
      }
      this.blockFileList.clear();
    } catch (Exception e) {
      log.error("unload exception", e);
    } finally {
      this.readWriteLock.writeLock().unlock();
    }
  }

  public void updateBufferLong(int blockIndex, int index, long value) {
    this.readWriteLock.readLock().lock();
    try {
      BlockFile blockFile = blockFileList.get(blockIndex);
      blockFile.updateBufferLong(index, value);
    } catch (Exception e) {
      log.error("updateBufferLong exception", e);
    } finally {
      this.readWriteLock.readLock().unlock();
    }
  }

  public void updateBufferInt(int blockIndex, int index, int value) {
    this.readWriteLock.readLock().lock();
    try {
      BlockFile blockFile = blockFileList.get(blockIndex);
      blockFile.updateBufferInt(index, value);
    } catch (Exception e) {
      log.error("updateBufferInt exception", e);
    } finally {
      this.readWriteLock.readLock().unlock();
    }
  }

  public void addComponent(AbstractComponent abstractComponent) {
    try {
      BlockFile blockFile = getAndCreateLastBlockFile(abstractComponent.getComponentLength());
      if (null != blockFile) {
        blockFile.addComponent(abstractComponent);
      }
    } catch (Exception e) {
      log.error("addComponent exception", e);
    }
  }

  public void modifyComponent(NSComponent nsc) {
    try {
      BlockFile blockFile = getBlockFile(nsc.getBlockIndex());
      if (null != blockFile) {
        blockFile.modifyComponent(nsc);
      }
    } catch (Exception e) {
      log.error("modifyComponent exception", e);
    }
  }

  public BlockFile getAndCreateLastBlockFile(int size) {
    BlockFile blockFile = null;
    BlockFile preBlockFile = null;
    boolean needToFlush = false;

    this.readWriteLock.writeLock().lock();
    try {
      if (!blockFileList.isEmpty()) {
        BlockFile tmp = blockFileList.get(blockFileList.size() - 1);
        if (!tmp.isWriteFull(size)) {
          blockFile = tmp;
        } else {
          preBlockFile = tmp;
        }
      }

      if (blockFile == null) {
        int blockIndex = blockFileIndex.getAndIncrement();
        String storeFileName =
            StorePathConfigHelper.getStoreFilePath(storeConfig.getStoreRootDir())
                + File.separator
                + StoreConfig.MYBERRY_STORE_FILE_NAME
                + StoreConfig.MYBERRY_STORE_FILE_NAME_DELIMITER
                + blockIndex;
        blockFile =
            new BlockFile(
                storeFileName,
                storeConfig.getBlockFileSize(),
                true,
                storeConfig.getMySid(),
                blockIndex,
                componentMap,
                syncDataMap);
        this.blockFileList.add(blockFile);

        needToFlush = true;
      }
    } catch (Exception e) {
      log.error("getAndCreateLastBlockFile exception ", e);
    } finally {
      this.readWriteLock.writeLock().unlock();
    }

    if (needToFlush) {
      final BlockFile flushThisFile = preBlockFile;
      Thread flushThread =
          new Thread(
              new Runnable() {
                @Override
                public void run() {
                  FileService.this.flush(flushThisFile);
                }
              },
              "FlushBlockFileThread");

      flushThread.setDaemon(true);
      flushThread.start();
    }

    return blockFile;
  }

  /**
   * this method is thread unsafe.
   *
   * @return
   */
  public int getBlockFileLastPosition() {
    this.readWriteLock.readLock().lock();
    try {
      if (blockFileList.isEmpty()) {
        return StoreHeader.STORE_HEADER_SIZE;
      } else {
        return getBlockFileLastPosition(blockFileList.size() - 1);
      }
    } catch (Exception e) {
      log.error("getBlockFileLastPosition exception ", e);
    } finally {
      this.readWriteLock.readLock().unlock();
    }
    return 0;
  }

  public int getBlockFileLastPosition(int index) {
    BlockFile blockFile = getBlockFile(index);
    if (null == blockFile) {
      return StoreHeader.STORE_HEADER_SIZE;
    } else {
      return blockFile.getLastPosition();
    }
  }

  public BlockFile getBlockFile(int index) {
    BlockFile blockFile = null;

    this.readWriteLock.readLock().lock();
    try {
      if (!blockFileList.isEmpty()) {
        blockFile = blockFileList.get(index);
      }
    } catch (Exception e) {
      log.error("getBlockFile exception ", e);
    } finally {
      this.readWriteLock.readLock().unlock();
    }

    return blockFile;
  }

  public void flush(final BlockFile f) {
    if (null != f) {
      f.flush();
    }
  }

  public long getLogicOffset() {
    long logicOffset = 0L;

    this.readWriteLock.readLock().lock();
    try {
      if (!blockFileList.isEmpty()) {
        for (BlockFile blockFile : blockFileList) {
          logicOffset += blockFile.getEndPhyOffset();
        }
      }
    } catch (Exception e) {
      log.error("getLogicOffset exception ", e);
    } finally {
      this.readWriteLock.readLock().unlock();
    }
    return logicOffset;
  }

  public int getMySid(int index) {
    BlockFile blockFile = getBlockFile(index);
    if (null == blockFile) {
      return -1;
    } else {
      return blockFile.getMySid();
    }
  }

  public ConcurrentMap<String, AbstractComponent> getComponentMap() {
    return componentMap;
  }

  public List<BlockHeader> getBlockHeaderList() {
    this.readWriteLock.readLock().lock();
    try {
      List<BlockHeader> list = new ArrayList<>(blockFileList.size() > 0 ? blockFileList.size() : 1);
      if (blockFileList.isEmpty()) {
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setBlockIndex(0);
        blockHeader.setComponentCount(0);
        blockHeader.setBeginPhyOffset(StoreHeader.STORE_HEADER_SIZE);
        blockHeader.setEndPhyOffset(StoreHeader.STORE_HEADER_SIZE);
        blockHeader.setBeginTimestamp(0L);
        blockHeader.setEndTimestamp(0L);

        list.add(blockHeader);
      } else {
        for (BlockFile f : blockFileList) {
          BlockHeader blockHeader = new BlockHeader();
          blockHeader.setBlockIndex(f.getBlockIndex());
          blockHeader.setComponentCount(f.getComponentCount());
          blockHeader.setBeginPhyOffset(f.getBeginPhyOffset());
          blockHeader.setEndPhyOffset(f.getEndPhyOffset());
          blockHeader.setBeginTimestamp(f.getBeginTimestamp());
          blockHeader.setEndTimestamp(f.getEndTimestamp());

          list.add(blockHeader);
        }
      }
      return list;
    } catch (Exception e) {
      log.error("getBlockHeaderList exception", e);
      return null;
    } finally {
      this.readWriteLock.readLock().unlock();
    }
  }

  public boolean verifyOffset(int blockIndex, int expectedEndPhyOffset) {
    if (blockIndex >= 0 && expectedEndPhyOffset == StoreHeader.STORE_HEADER_SIZE) {
      return true;
    } else {
      CopyOnWriteArrayList<String> keys = syncDataMap.get(blockIndex);
      if (null != keys) {
        for (String key : keys) {
          int currentComponentBeginPhyOffset = componentMap.get(key).getPhyOffset();
          int currentComponentEndPhyOffset =
              currentComponentBeginPhyOffset
                  + getComponentLengthByComponentOffset(blockIndex, currentComponentBeginPhyOffset);
          if (currentComponentEndPhyOffset == expectedEndPhyOffset) {
            return true;
          }
        }
      }
    }

    return false;
  }

  public int getMaxBlockIndex() {
    if (blockFileIndex.get() == 0) {
      return 0;
    } else {
      return blockFileIndex.get() - 1;
    }
  }

  public int getSyncLength(int blockIndex, int markOffset, int allowedSyncLength) {
    CopyOnWriteArrayList<String> keys = syncDataMap.get(blockIndex);
    if (null == keys) {
      return 0;
    }

    int total = 0;
    for (int i = 0; i < keys.size(); i++) {
      AbstractComponent component = componentMap.get(keys.get(i));
      int componentLength =
          getComponentLengthByComponentOffset(blockIndex, component.getPhyOffset());
      int currentComponentEndPhyOffset = component.getPhyOffset() + componentLength;
      // verifyOffset() is executed before getSyncLength(), so there is no need to verify the
      // validity of markOffset
      if (currentComponentEndPhyOffset > markOffset) {
        if (total + componentLength > allowedSyncLength) {
          break;
        } else {
          total += componentLength;
        }
      }
    }

    return total;
  }

  private int getComponentLengthByComponentOffset(int blockIndex, int componentOffset) {
    BlockFile blockFile = getBlockFile(blockIndex);
    if (null == blockFile) {
      return 0;
    } else {
      return blockFile.getComponentLengthByOffset(componentOffset);
    }
  }

  public byte[] getSyncData(int blockIndex, int markOffset, int length) {
    BlockFile blockFile = getBlockFile(blockIndex);
    if (null == blockFile) {
      return new byte[0];
    } else {
      return blockFile.getSyncData(markOffset, length);
    }
  }

  public String getServiceName() {
    return FileService.class.getSimpleName();
  }
}
