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
package org.myberry.client;

import org.myberry.common.MixAll;
import org.myberry.remoting.common.RemotingHelper;

public class ClientConfig {

  private String serverAddr =
      System.getProperty(MixAll.MYBERRY_ADDR_PROPERTY, System.getenv(MixAll.MYBERRY_ADDR_ENV));

  private String clientIP = RemotingHelper.getPhyLocalAddress();
  private String instanceName = System.getProperty("myberry.client.name", "DEFAULT");
  private int heartbeatServerInterval = 1000 * 5;
  private boolean unitMode = false;
  private String unitName;

  public String buildClientId() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getClientIP());

    sb.append("@");
    sb.append(this.getInstanceName());
    if (!MixAll.isBlank(this.unitName)) {
      sb.append("@");
      sb.append(this.unitName);
    }

    return sb.toString();
  }

  public ClientConfig cloneClientConfig() {
    ClientConfig cc = new ClientConfig();
    cc.serverAddr = serverAddr;
    cc.clientIP = clientIP;
    cc.instanceName = instanceName;
    cc.heartbeatServerInterval = heartbeatServerInterval;
    cc.unitMode = unitMode;
    cc.unitName = unitName;
    return cc;
  }

  public String getServerAddr() {
    return serverAddr;
  }

  public void setServerAddr(String serverAddr) {
    this.serverAddr = serverAddr;
  }

  public String getClientIP() {
    return clientIP;
  }

  public void setClientIP(String clientIP) {
    this.clientIP = clientIP;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  public int getHeartbeatServerInterval() {
    return heartbeatServerInterval;
  }

  public void setHeartbeatServerInterval(int heartbeatServerInterval) {
    this.heartbeatServerInterval = heartbeatServerInterval;
  }

  public boolean isUnitMode() {
    return unitMode;
  }

  public void setUnitMode(boolean unitMode) {
    this.unitMode = unitMode;
  }

  public String getUnitName() {
    return unitName;
  }

  public void setUnitName(String unitName) {
    this.unitName = unitName;
  }
}
