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

import java.util.Map;
import org.myberry.remoting.netty.NettyServerConfig;
import org.myberry.server.config.ServerConfig;
import org.myberry.store.MyberryStore;
import org.myberry.store.config.StoreConfig;

public class HAContext {

  private final StoreConfig storeConfig;
  private final ServerConfig serverConfig;
  private final NettyServerConfig nettyServerConfig;
  private final HAService haService;

  private Map<Integer /**/, String /*addr*/> memberMap;
  private Map<Integer /**/, String /*addr*/> otherMemberMap;
  private int haPort;

  public HAContext(
      final StoreConfig storeConfig,
      final ServerConfig serverConfig,
      final NettyServerConfig nettyServerConfig,
      final HAService haService) {
    this.storeConfig = storeConfig;
    this.serverConfig = serverConfig;
    this.nettyServerConfig = nettyServerConfig;
    this.haService = haService;
  }

  public StoreConfig getStoreConfig() {
    return storeConfig;
  }

  public ServerConfig getServerConfig() {
    return serverConfig;
  }

  public NettyServerConfig getNettyServerConfig() {
    return nettyServerConfig;
  }

  public Map<Integer, String> getMemberMap() {
    return memberMap;
  }

  public void setMemberMap(Map<Integer, String> memberMap) {
    this.memberMap = memberMap;
  }

  public Map<Integer, String> getOtherMemberMap() {
    return otherMemberMap;
  }

  public void setOtherMemberMap(Map<Integer, String> otherMemberMap) {
    this.otherMemberMap = otherMemberMap;
  }

  public int getHaPort() {
    return haPort;
  }

  public void setHaPort(int haPort) {
    this.haPort = haPort;
  }

  public HAState getHaState() {
    return haService.getHaState();
  }

  public void setHaState(HAState haState) {
    this.haService.setHaState(haState);
  }

  public MyberryStore getMyberryStore() {
    return haService.getMyberryStore();
  }
}
