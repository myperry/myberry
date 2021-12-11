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
package org.myberry.store.config;

import java.io.File;
import org.myberry.common.annotation.ImportantField;

public class StoreConfig {

  public static final String MYBERRY_STORE_DEFAULT_CHARSET = "UTF-8";
  public static final String MYBERRY_STORE_FILE_NAME = "myberry";
  public static final String MYBERRY_STORE_FILE_NAME_DELIMITER = "-";

  @ImportantField private int mySid;

  private long osPageCacheBusyTimeOutMills = 1000;

  @ImportantField private int blockFileSize = 1024 * 1024 * 8;
  private int maxSyncDataSize = 1024 * 1024 * 1;

  @ImportantField private String storePath = System.getProperty("user.home");

  public int getMySid() {
    return mySid;
  }

  public void setMySid(int mySid) {
    this.mySid = mySid;
  }

  public long getOsPageCacheBusyTimeOutMills() {
    return osPageCacheBusyTimeOutMills;
  }

  public void setOsPageCacheBusyTimeOutMills(final long osPageCacheBusyTimeOutMills) {
    this.osPageCacheBusyTimeOutMills = osPageCacheBusyTimeOutMills;
  }

  public String getStorePath() {
    return storePath;
  }

  public int getBlockFileSize() {
    return blockFileSize;
  }

  public void setBlockFileSize(int blockFileSize) {
    this.blockFileSize = blockFileSize;
  }

  public int getMaxSyncDataSize() {
    return maxSyncDataSize;
  }

  public void setMaxSyncDataSize(int maxSyncDataSize) {
    this.maxSyncDataSize = maxSyncDataSize;
  }

  public void setStorePath(String storePath) {
    this.storePath = storePath;
  }

  public String getStoreRootDir() {
    return storePath + File.separator + ".myberry";
  }
}
