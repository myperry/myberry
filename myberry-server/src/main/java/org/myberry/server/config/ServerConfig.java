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
package org.myberry.server.config;

import org.myberry.common.MixAll;
import org.myberry.common.annotation.ImportantField;
import org.myberry.common.annotation.WriteBack;

public class ServerConfig {

  @ImportantField
  private String myberryHome =
      System.getProperty(MixAll.MYBERRY_HOME_PROPERTY, System.getenv(MixAll.MYBERRY_HOME_ENV));

  @WriteBack @ImportantField
  private String haServerAddr =
      System.getProperty(MixAll.MYBERRY_ADDR_PROPERTY, System.getenv(MixAll.MYBERRY_ADDR_ENV));

  @WriteBack private int weight = 1;

  private String password = "foobared";
  private int userManageThreadPoolNums = 16 + Runtime.getRuntime().availableProcessors() * 2;
  private int clientManageThreadPoolNums = 32;
  private int adminManageThreadPoolNums = 1;
  private int userManagerThreadPoolQueueCapacity = 100000;
  private int clientManagerThreadPoolQueueCapacity = 1000;
  private int adminManagerThreadPoolQueueCapacity = 10;

  private int haHousekeepingInterval = 1000 * 60 * 3;

  private boolean writeBackEnabled = true;

  public String getMyberryHome() {
    return myberryHome;
  }

  public void setMyberryHome(String myberryHome) {
    this.myberryHome = myberryHome;
  }

  public String getHaServerAddr() {
    return haServerAddr;
  }

  public void setHaServerAddr(String haServerAddr) {
    this.haServerAddr = haServerAddr;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getUserManageThreadPoolNums() {
    return userManageThreadPoolNums;
  }

  public void setUserManageThreadPoolNums(int userManageThreadPoolNums) {
    this.userManageThreadPoolNums = userManageThreadPoolNums;
  }

  public int getClientManageThreadPoolNums() {
    return clientManageThreadPoolNums;
  }

  public void setClientManageThreadPoolNums(int clientManageThreadPoolNums) {
    this.clientManageThreadPoolNums = clientManageThreadPoolNums;
  }

  public int getAdminManageThreadPoolNums() {
    return adminManageThreadPoolNums;
  }

  public void setAdminManageThreadPoolNums(int adminManageThreadPoolNums) {
    this.adminManageThreadPoolNums = adminManageThreadPoolNums;
  }

  public int getUserManagerThreadPoolQueueCapacity() {
    return userManagerThreadPoolQueueCapacity;
  }

  public void setUserManagerThreadPoolQueueCapacity(int userManagerThreadPoolQueueCapacity) {
    this.userManagerThreadPoolQueueCapacity = userManagerThreadPoolQueueCapacity;
  }

  public int getClientManagerThreadPoolQueueCapacity() {
    return clientManagerThreadPoolQueueCapacity;
  }

  public void setClientManagerThreadPoolQueueCapacity(int clientManagerThreadPoolQueueCapacity) {
    this.clientManagerThreadPoolQueueCapacity = clientManagerThreadPoolQueueCapacity;
  }

  public int getAdminManagerThreadPoolQueueCapacity() {
    return adminManagerThreadPoolQueueCapacity;
  }

  public void setAdminManagerThreadPoolQueueCapacity(int adminManagerThreadPoolQueueCapacity) {
    this.adminManagerThreadPoolQueueCapacity = adminManagerThreadPoolQueueCapacity;
  }

  public int getHaHousekeepingInterval() {
    return haHousekeepingInterval;
  }

  public void setHaHousekeepingInterval(int haHousekeepingInterval) {
    this.haHousekeepingInterval = haHousekeepingInterval;
  }

  public boolean isWriteBackEnabled() {
    return writeBackEnabled;
  }

  public void setWriteBackEnabled(boolean writeBackEnabled) {
    this.writeBackEnabled = writeBackEnabled;
  }
}
