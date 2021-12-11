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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.myberry.common.ServiceThread;
import org.myberry.server.common.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HAHouseKeepService {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.HA_HOUSE_KEEP_SERVICE_NAME);

  public static final int TIME_OUT = 120 * 1000;
  private static final int ALL_UNRESPONSIVE = -1;

  private final HAService haService;
  private final TimeoutCheckTask timeoutCheckTask;

  private long leaderResponseTime = System.currentTimeMillis();
  private long recentLearnersResponseTime = 0L;
  private Map<Integer /*learner sid*/, Long /*time millis*/> learnersResponseTimeTable =
      new HashMap<>();

  public HAHouseKeepService(final HAService haService) {
    this.haService = haService;
    this.timeoutCheckTask = new TimeoutCheckTask();
  }

  public void start() throws Exception {
    timeoutCheckTask.start();
  }

  public void shutdown() {
    timeoutCheckTask.shutdown(true);
  }

  public void reset() {
    this.leaderResponseTime = System.currentTimeMillis();
    this.recentLearnersResponseTime = 0L;
    this.learnersResponseTimeTable.clear();
  }

  class TimeoutCheckTask extends ServiceThread {

    @Override
    public String getServiceName() {
      return TimeoutCheckTask.class.getSimpleName();
    }

    @Override
    public void run() {
      while (!this.isStopped()) {
        try {
          switch (haService.getHaState()) {
            case LEADING:
              List<Integer> learners = checkLearnerResponseTimeout();
              if (haService.getHaContext().getMemberMap().size() - 1 == learners.size()) {
                reset();

                haService.restart();
              } else {
                for (Integer learner : learners) {
                  if (learner.equals(ALL_UNRESPONSIVE)) {
                    reset();

                    haService.restart();
                  }
                }
              }

              break;
            case LEARNING:
              if (checkLeaderResponseTimeout()) {
                reset();

                haService.restart();
              }

              break;
            default:
              // Ignore
              break;
          }

          this.waitForRunning(10 * 1000);
        } catch (InterruptedException e) {
          break;
        } catch (Exception e) {
          HAHouseKeepService.log.error("TimeoutCheckTask error: ", e);
        }
      }
    }
  }

  private boolean checkLeaderResponseTimeout() {
    if (System.currentTimeMillis() - leaderResponseTime > TIME_OUT) {
      return true;
    } else {
      return false;
    }
  }

  /*
   * There are only four cases for learner timeout, and re-election will
   * only be made when all learners timeout
   *
   * 1.none of them timed out
   * 2.part of the learner timed out
   * 3.received the learner's response, all learners timed out
   * 4.no learner response was received, all learners timed out
   *
   * @return timed out learner
   */
  private List<Integer> checkLearnerResponseTimeout() {
    long now = System.currentTimeMillis();
    List<Integer> learnerTimeoutList = new ArrayList<>();

    Iterator<Entry<Integer, Long>> it = learnersResponseTimeTable.entrySet().iterator();
    while (it.hasNext()) {
      Entry<Integer, Long> entry = it.next();
      if (now - entry.getValue() > TIME_OUT
          && haService.getHaContext().getMemberMap().containsKey(entry.getKey())) {
        learnerTimeoutList.add(entry.getKey());
      }
    }

    if (learnersResponseTimeTable.size() == 0) {
      if (recentLearnersResponseTime == 0L) {
        this.recentLearnersResponseTime = now;
      }

      if (now - recentLearnersResponseTime > TIME_OUT) {
        learnerTimeoutList.add(ALL_UNRESPONSIVE);
      }
    }
    return learnerTimeoutList;
  }

  public void updateLeaderResponseTime() {
    this.leaderResponseTime = System.currentTimeMillis();
  }

  public void updateLearnersResponseTime(int sid) {
    learnersResponseTimeTable.put(sid, System.currentTimeMillis());
  }
}
