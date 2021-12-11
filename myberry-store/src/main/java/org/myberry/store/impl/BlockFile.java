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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.myberry.common.structure.Structure;
import org.myberry.store.AbstractComponent;
import org.myberry.store.CRComponent;
import org.myberry.store.NSComponent;
import org.myberry.store.common.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockFile {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);

  private final MappedFile mappedFile;
  private final MappedByteBuffer mappedByteBuffer;
  private final StoreHeader storeHeader;
  private final StoreComponent storeComponent;
  private final int blockIndex;
  private final ConcurrentMap<String, AbstractComponent> componentMap;
  private final ConcurrentMap<Integer, CopyOnWriteArrayList<String>> syncDataMap;

  public BlockFile(
      final String storeFileName,
      final int fileSize,
      final boolean isCreate,
      final int mySid,
      final int blockIndex,
      final ConcurrentMap<String, AbstractComponent> componentMap,
      final ConcurrentMap<Integer, CopyOnWriteArrayList<String>> syncDataMap)
      throws IOException {
    this.blockIndex = blockIndex;
    this.componentMap = componentMap;
    this.syncDataMap = syncDataMap;
    this.mappedFile = new MappedFile(storeFileName, fileSize);
    this.mappedByteBuffer = this.mappedFile.getMappedByteBuffer();

    ByteBuffer byteBuffer = mappedByteBuffer.slice();
    this.storeHeader = new StoreHeader(byteBuffer);
    this.storeComponent = new StoreComponent(mappedByteBuffer);
    this.init(mySid, isCreate);
  }

  private void init(int mySid, boolean isCreate) {
    if (isCreate) {
      storeHeader.setBeginTimestamp(System.currentTimeMillis());
      storeHeader.setBeginPhyOffset(StoreHeader.STORE_HEADER_SIZE);
      storeHeader.setMySid(mySid);
    }

    this.mappedByteBuffer.position(StoreHeader.STORE_HEADER_SIZE);
  }

  public void load() {
    loadHeader();
    loadComponent();
  }

  public void loadHeader() {
    storeHeader.load();
    mappedByteBuffer.position(StoreHeader.STORE_HEADER_SIZE);
  }

  public void loadComponent() {
    int componentLengthOffset =
        mappedByteBuffer.position() + AbstractComponent.COMPONENT_LENGTH_RELATIVE_OFFSET;
    if (mappedByteBuffer.getShort(componentLengthOffset) > 0) {
      int structureOffset =
          mappedByteBuffer.position() + AbstractComponent.STRUCTURE_RELATIVE_OFFSET;
      AbstractComponent abstractComponent =
          getComponentMapping(mappedByteBuffer.get(structureOffset));
      abstractComponent.setBlockIndex(blockIndex);

      try {
        storeComponent.load(abstractComponent);
        componentMap.put(abstractComponent.getKey(), abstractComponent);
        addSyncDataIndex(abstractComponent.getBlockIndex(), abstractComponent.getKey());
        log.debug("add cache success: key = {}", abstractComponent.getKey());
      } catch (Exception e) {
        log.error("loadComponent error: ", e);
      }
      log.debug("load: {}", abstractComponent);
      if (mappedByteBuffer.position() < storeHeader.getEndPhyOffset()) {
        loadComponent();
      }
    }
  }

  public static List<AbstractComponent> parseComponent(ByteBuffer byteBuffer) {
    List<AbstractComponent> list = new ArrayList<>();
    do {
      try {
        int structureOffset = byteBuffer.position() + AbstractComponent.STRUCTURE_RELATIVE_OFFSET;
        AbstractComponent abstractComponent = getComponentMapping(byteBuffer.get(structureOffset));
        StoreComponent.load(abstractComponent, byteBuffer);

        switch (byteBuffer.get(structureOffset)) {
          case Structure.CR:
            CRComponent crc = (CRComponent) abstractComponent;
            crc.setIncrNumber(0L);
            break;
          case Structure.NS:
            NSComponent nsc = (NSComponent) abstractComponent;
            nsc.setCurrentNumber(nsc.getInitNumber());
            break;
          default:
            throw new RuntimeException("unknown structure: " + byteBuffer.get(structureOffset));
        }

        list.add(abstractComponent);
      } catch (Exception e) {
        log.error("parseComponent error: ", e);
        return new ArrayList<>(0);
      }
    } while (byteBuffer.position() < byteBuffer.capacity());

    return list;
  }

  public void addComponent(final AbstractComponent abstractComponent) {
    abstractComponent.setBlockIndex(blockIndex);
    abstractComponent.setPhyOffset(mappedByteBuffer.position());
    storeComponent.write(abstractComponent);
    componentMap.put(abstractComponent.getKey(), abstractComponent);

    storeHeader.setEndTimestamp(System.currentTimeMillis());
    storeHeader.setEndPhyOffset(mappedByteBuffer.position());
    storeHeader.incrComponentCount();

    addSyncDataIndex(abstractComponent.getBlockIndex(), abstractComponent.getKey());

    mappedFile.flush();
  }

  private void addSyncDataIndex(int blockIndex, String key) {
    CopyOnWriteArrayList<String> keys = syncDataMap.get(blockIndex);
    if (null == keys) {
      keys = new CopyOnWriteArrayList<>();
      syncDataMap.putIfAbsent(blockIndex, keys);
    }
    keys.add(key);
  }

  public void modifyComponent(final NSComponent nsc) {
    mappedByteBuffer.putLong(
        nsc.getPhyOffset() + NSComponent.updateTimeRelativeOffset, nsc.getUpdateTime());
    mappedByteBuffer.putInt(
        nsc.getPhyOffset() + NSComponent.initNumberRelativeOffset, nsc.getInitNumber());
    mappedByteBuffer.putInt(
        nsc.getPhyOffset() + NSComponent.stepSizeRelativeOffset, nsc.getStepSize());
    mappedByteBuffer.put(
        nsc.getPhyOffset() + NSComponent.resetTypeRelativeOffset, nsc.getResetType());

    mappedFile.flush();
  }

  public void flush() {
    mappedFile.flush();
  }

  public void unload() {
    mappedFile.destroy();
  }

  public boolean isWriteFull(int size) {
    if (mappedByteBuffer.capacity() - mappedByteBuffer.position() >= size) {
      return false;
    } else {
      return true;
    }
  }

  public void updateBufferLong(int index, long value) {
    mappedByteBuffer.putLong(index, value);
  }

  public void updateBufferInt(int index, int value) {
    mappedByteBuffer.putInt(index, value);
  }

  public int getLastPosition() {
    return mappedByteBuffer.position();
  }

  public int getBlockIndex() {
    return blockIndex;
  }

  public int getMySid() {
    return storeHeader.getMySid();
  }

  public long getBeginTimestamp() {
    return storeHeader.getBeginTimestamp();
  }

  public long getEndTimestamp() {
    return storeHeader.getEndTimestamp();
  }

  public int getComponentCount() {
    return storeHeader.getComponentCount();
  }

  public int getBeginPhyOffset() {
    return storeHeader.getBeginPhyOffset();
  }

  public int getEndPhyOffset() {
    return storeHeader.getEndPhyOffset();
  }

  public static AbstractComponent getComponentMapping(int structure) {
    if (Structure.CR == structure) {
      return new CRComponent();
    } else if (Structure.NS == structure) {
      return new NSComponent();
    } else {
      throw new RuntimeException("unknown structure: " + structure);
    }
  }

  public int getComponentLengthByOffset(int offset) {
    if (offset <= 0) {
      offset = StoreHeader.STORE_HEADER_SIZE;
    }
    return mappedByteBuffer.getShort(offset);
  }

  public byte[] getSyncData(int offset, int length) {
    if (offset <= 0) {
      offset = StoreHeader.STORE_HEADER_SIZE;
    }
    byte[] bytes = new byte[length];
    for (int i = 0; i < length; i++) {
      bytes[i] = mappedByteBuffer.get(offset + i);
    }
    return bytes;
  }
}
