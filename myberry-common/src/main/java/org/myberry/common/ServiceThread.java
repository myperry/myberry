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
package org.myberry.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.myberry.common.constant.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServiceThread implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);
  private static final long JOIN_TIME = 90 * 1000;

  protected Thread thread;
  protected final CountDownLatch2 waitPoint = new CountDownLatch2(1);
  protected volatile AtomicBoolean hasNotified = new AtomicBoolean(false);
  protected volatile boolean stopped = false;
  protected boolean isDaemon = false;

  // Make it able to restart the thread
  private final AtomicBoolean started = new AtomicBoolean(false);

  public ServiceThread() {}

  public abstract String getServiceName();

  public void start() {
    log.info(
        "Try to start service thread:{} started:{} lastThread:{}",
        getServiceName(),
        started.get(),
        thread);
    if (!started.compareAndSet(false, true)) {
      return;
    }
    stopped = false;
    this.thread = new Thread(this, getServiceName());
    this.thread.setDaemon(isDaemon);
    this.thread.start();
  }

  public void join() throws InterruptedException {
    this.join(0);
  }

  public void join(long millis) throws InterruptedException {
    this.thread.join(millis);
  }

  public void shutdown() {
    this.shutdown(false);
  }

  public void shutdown(final boolean interrupt) {
    log.info(
        "Try to shutdown service thread:{} started:{} lastThread:{}",
        getServiceName(),
        started.get(),
        thread);
    if (!started.compareAndSet(true, false)) {
      return;
    }

    this.stopped = true;
    log.info("shutdown thread {} interrupt {}", this.getServiceName(), interrupt);

    if (hasNotified.compareAndSet(false, true)) {
      waitPoint.countDown(); // notify
    }

    try {
      if (interrupt) {
        this.thread.interrupt();
      }

      long beginTime = System.currentTimeMillis();
      if (!this.thread.isDaemon()) {
        this.thread.join(this.getJointime());
      }
      long eclipseTime = System.currentTimeMillis() - beginTime;
      log.info(
          "join thread {} eclipse time(ms) {} {}",
          this.getServiceName(),
          eclipseTime,
          this.getJointime());
    } catch (InterruptedException e) {
      log.error("Interrupted", e);
    }
  }

  public long getJointime() {
    return JOIN_TIME;
  }

  public void makeStop() {
    if (!started.get()) {
      return;
    }

    this.stopped = true;
    log.debug("makestop thread {}", this.getServiceName());
  }

  public void wakeup() {
    if (hasNotified.compareAndSet(false, true)) {
      waitPoint.countDown(); // notify
    }
  }

  protected void waitForRunning() throws InterruptedException {
    if (hasNotified.compareAndSet(true, false)) {
      this.onWaitEnd();
      return;
    }

    // entry to wait
    waitPoint.reset();

    try {
      waitPoint.await();
    } finally {
      hasNotified.set(false);
      this.onWaitEnd();
    }
  }

  protected void waitForRunning(long interval) throws InterruptedException {
    if (hasNotified.compareAndSet(true, false)) {
      this.onWaitEnd();
      return;
    }

    // entry to wait
    waitPoint.reset();

    try {
      waitPoint.await(interval, TimeUnit.MILLISECONDS);
    } finally {
      hasNotified.set(false);
      this.onWaitEnd();
    }
  }

  protected void onWaitEnd() {}

  public boolean isStopped() {
    return stopped;
  }

  public boolean isDaemon() {
    return isDaemon;
  }

  public void setDaemon(boolean daemon) {
    isDaemon = daemon;
  }
}
