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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.myberry.common.ServiceThread;
import org.myberry.server.common.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HAMessageDispatcher extends ServiceThread {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.HA_MESSAGE_DISPATCHER_NAME);

  private final HAConnectionManager haConnectionManager;
  private LinkedBlockingQueue<HAMessage> voteQueue;
  private LinkedBlockingQueue<HAMessage> databaseQueue;
  private LinkedBlockingQueue<HAMessage> routeQueue;

  public HAMessageDispatcher(final HAConnectionManager haConnectionManager) {
    this.haConnectionManager = haConnectionManager;
  }

  @Override
  public void run() {
    log.info("{} service started", this.getServiceName());

    while (!this.isStopped()) {
      try {
        HAMessage haMessage =
            haConnectionManager.getRecvQueue().poll(1 * 1000, TimeUnit.MILLISECONDS);
        if (null != haMessage) {
          switch (haMessage.getMessageType()) {
            case HATransfer.VOTE:
              voteQueue.offer(haMessage);
              break;
            case HATransfer.DATABASE:
              databaseQueue.offer(haMessage);
              break;
            case HATransfer.COLLECT:
              routeQueue.offer(haMessage);
              break;
          }
        }
      } catch (InterruptedException e) {
        break;
      } catch (Exception e) {
        log.error("HAMessageDispatcher Exception: ", e);
      }
    }

    log.info("{} service end", this.getServiceName());
  }

  public void haMessageDelivery(int connId, HAMessage haMessage) {
    haConnectionManager.deliverHAMessage(connId, haMessage);
  }

  public boolean haveDelivered() {
    return haConnectionManager.haveDelivered();
  }

  public void connectUnbound() {
    haConnectionManager.connectionWorkerWakeup();
  }

  public boolean haveDelivered(int connId) {
    return haConnectionManager.haveDelivered(connId);
  }

  public void connect(int connId) {
    haConnectionManager.connectionWorkerWakeup(connId);
  }

  public void setVoteQueue(LinkedBlockingQueue<HAMessage> voteQueue) {
    this.voteQueue = voteQueue;
  }

  public void setDatabaseQueue(LinkedBlockingQueue<HAMessage> databaseQueue) {
    this.databaseQueue = databaseQueue;
  }

  public void setRouteQueue(LinkedBlockingQueue<HAMessage> routeQueue) {
    this.routeQueue = routeQueue;
  }

  @Override
  public String getServiceName() {
    return HAMessageDispatcher.class.getSimpleName();
  }
}
