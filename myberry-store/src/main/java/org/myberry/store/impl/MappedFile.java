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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import jdk.internal.ref.Cleaner;
import org.myberry.store.common.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.DirectBuffer;

public class MappedFile {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);

  protected RandomAccessFile raf;

  protected FileChannel fileChannel;

  private MappedByteBuffer mappedByteBuffer;

  public MappedFile() {}

  public MappedFile(final String fileName, final int fileSize) throws IOException {
    init(fileName, fileSize);
  }

  private void init(final String fileName, final int fileSize) throws IOException {
    File file = new File(fileName);
    ensureDirOK(file.getParent());
    isCreatedFile(file);

    boolean ok = false;
    try {

      this.raf = new RandomAccessFile(file, "rw");
      this.fileChannel = raf.getChannel();
      this.mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, 0, fileSize);
      ok = true;
    } catch (FileNotFoundException e) {
      log.error("create file channel [{}] Failed. ", file.getAbsoluteFile(), e);
      throw e;
    } catch (IOException e) {
      log.error("map file [{}] Failed. ", file.getAbsoluteFile(), e);
      throw e;
    } finally {
      if (!ok && fileChannel != null) {
        fileChannel.close();
      }
    }
  }

  public void flush() {
    this.mappedByteBuffer.force();
  }

  public static void ensureDirOK(final String dirName) {
    if (dirName != null) {
      File f = new File(dirName);
      if (!f.exists()) {
        boolean result = f.mkdirs();
        log.info("{} mkdir {}", dirName, (result ? "OK" : "Failed"));
      }
    }
  }

  public static void isCreatedFile(final File file) throws IOException {
    if (file.exists()) {
      return;
    } else {
      file.createNewFile();
    }
  }

  public void destroy() {
    if (fileChannel != null) {
      try {
        fileChannel.close();
      } catch (IOException e) {
        log.error("close fileChannel Exception: ", e);
      }
    }
    if (raf != null) {
      try {
        raf.close();
      } catch (IOException e) {
        log.error("close randomAccessFile Exception: ", e);
      }
    }
    if (mappedByteBuffer != null) {
      try {
        unmap(mappedByteBuffer);
      } catch (Exception e) {
        log.error("clean mappedByteBuffer Exception: ", e);
      }
    }
  }

  public static void unmap(ByteBuffer byteBuffer) {
    if (byteBuffer == null || !byteBuffer.isDirect() || byteBuffer.capacity() == 0) {
      return;
    }
    DirectBuffer directBuffer = (DirectBuffer) byteBuffer;

    Cleaner cleaner = directBuffer.cleaner();
    if (cleaner != null) {
      cleaner.clean();
    }
  }

  public FileChannel getFileChannel() {
    return fileChannel;
  }

  public MappedByteBuffer getMappedByteBuffer() {
    return mappedByteBuffer;
  }
}
