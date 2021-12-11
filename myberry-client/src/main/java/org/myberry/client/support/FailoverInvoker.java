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
package org.myberry.client.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import org.myberry.client.exception.MyberryClientException;
import org.myberry.client.exception.MyberryServerException;
import org.myberry.client.impl.CommunicationMode;
import org.myberry.client.impl.user.DefaultUserClientImpl;
import org.myberry.client.router.DefaultRouter;
import org.myberry.client.user.PullCallback;
import org.myberry.client.user.PullResult;
import org.myberry.common.MixAll;
import org.myberry.common.constant.LoggerName;
import org.myberry.common.loadbalance.Invoker;
import org.myberry.common.protocol.header.user.PullIdBackRequestHeader;
import org.myberry.remoting.CommandCustomHeader;
import org.myberry.remoting.exception.RemotingException;
import org.myberry.remoting.exception.RemotingTooMuchRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When invoke fails, log the initial error and retry other invokers (retry n times, which means at
 * most n different invokers will be invoked) Note that retry causes latency.
 *
 * <p><a href="http://en.wikipedia.org/wiki/Failover">Failover</a>
 */
public class FailoverInvoker extends AbstractInvoker {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.CLIENT_LOGGER_NAME);

  public static final String NAME = "failover";

  public FailoverInvoker(final ExecutorService asyncSenderExecutor) {
    super(asyncSenderExecutor);
  }

  @Override
  public PullResult doInvoke(
      final DefaultUserClientImpl defaultUserClientImpl,
      final CommandCustomHeader requestHeader,
      final HashMap<String, String> attachments,
      final long timeoutMillis,
      final int timesRetry,
      final CommunicationMode communicationMode)
      throws RemotingException, InterruptedException, MyberryServerException {
    return retryInvoke(
        defaultUserClientImpl,
        requestHeader,
        attachments,
        timeoutMillis,
        timesRetry,
        communicationMode);
  }

  @Override
  public void doInvoke(
      final DefaultUserClientImpl defaultUserClientImpl,
      final CommandCustomHeader requestHeader,
      final HashMap<String, String> attachments,
      final long timeoutMillis,
      final int timesRetry,
      final CommunicationMode communicationMode,
      final PullCallback pullCallback)
      throws MyberryClientException {
    final long beginStartTime = System.currentTimeMillis();
    try {
      this.asyncSenderExecutor.submit(
          new Runnable() {
            @Override
            public void run() {
              long costTime = System.currentTimeMillis() - beginStartTime;
              if (timeoutMillis > costTime) {
                try {
                  retryInvoke(
                      defaultUserClientImpl,
                      requestHeader,
                      attachments,
                      timeoutMillis,
                      timesRetry,
                      communicationMode,
                      pullCallback,
                      costTime);
                } catch (Exception e) {
                  pullCallback.onException(e);
                }
              } else {
                pullCallback.onException(
                    new RemotingTooMuchRequestException("DEFAULT ASYNC send call timeout"));
              }
            }
          });
    } catch (RejectedExecutionException e) {
      throw new MyberryClientException("executor rejected ", e);
    }
  }

  private Invoker select(
      DefaultRouter defaultRouter, List<Invoker> invokers, List<Invoker> selected) {

    if (MixAll.isEmpty(invokers)) {
      return null;
    }
    if (invokers.size() == 1) {
      return invokers.get(0);
    }

    Invoker invoker = defaultRouter.getInvoker(invokers);
    // If the `invoker` is in the `selected`, then reselect.
    if (selected != null && selected.contains(invoker)) {
      try {
        Invoker reInvoker = reSelect(defaultRouter, invokers, selected);
        if (reInvoker != null) {
          invoker = reInvoker;
        } else {
          // Check the index of current selected invoker, if it's not the last one, choose the one
          // at
          // index+1.
          int index = invokers.indexOf(invoker);
          try {
            // Avoid collision
            invoker = invokers.get((index + 1) % invokers.size());
          } catch (Exception e) {
            log.warn("{} may because invokers list dynamic change, ignore.", e.getMessage(), e);
          }
        }
      } catch (Throwable t) {
        log.error("reselect fail reason is : {}", t);
      }
    }
    return invoker;
  }

  /**
   * Reselect, use invokers not in `selected` first, if all invokers are in `selected`, just pick an
   * invoker using loadbalance policy.
   *
   * @param defaultRouter default router policy
   * @param invokers invoker candidates
   * @param selected exclude selected invokers or not
   * @return the selected server address
   */
  private Invoker reSelect(
      DefaultRouter defaultRouter, List<Invoker> invokers, List<Invoker> selected) {

    // Allocating one in advance, this list is certain to be used.
    List<Invoker> reselectInvokers =
        new ArrayList<>(invokers.size() > 1 ? (invokers.size() - 1) : invokers.size());

    // First, try picking a invoker not in `selected`.
    for (Invoker invoker : invokers) {
      if (selected == null || !selected.contains(invoker)) {
        reselectInvokers.add(invoker);
      }
    }

    if (!reselectInvokers.isEmpty()) {
      return defaultRouter.getInvoker(reselectInvokers);
    }

    // Just pick an available invoker using loadbalance policy
    if (selected != null) {
      for (Invoker invoker : selected) {
        if (!reselectInvokers.contains(invoker)) {
          reselectInvokers.add(invoker);
        }
      }
    }
    if (!reselectInvokers.isEmpty()) {
      return defaultRouter.getInvoker(reselectInvokers);
    }
    return null;
  }

  private PullResult retryInvoke(
      final DefaultUserClientImpl defaultUserClientImpl,
      final CommandCustomHeader requestHeader,
      final HashMap<String, String> attachments,
      final long timeoutMillis,
      final int timesRetry,
      final CommunicationMode communicationMode)
      throws RemotingException {
    return retryInvoke(
        defaultUserClientImpl,
        requestHeader,
        attachments,
        timeoutMillis,
        timesRetry,
        communicationMode,
        null,
        0L);
  }

  private PullResult retryInvoke(
      final DefaultUserClientImpl defaultUserClientImpl,
      final CommandCustomHeader requestHeader,
      final HashMap<String, String> attachments,
      final long timeoutMillis,
      final int timesRetry,
      final CommunicationMode communicationMode,
      final PullCallback pullCallback,
      final long scheduleThreadTime)
      throws RemotingException {
    DefaultRouter defaultRouter = defaultUserClientImpl.getDefaultUserClient().getDefaultRouter();

    List<Invoker> invokers = defaultRouter.getInvokers();
    List<Invoker> invoked = new ArrayList<>(invokers.size()); // invoked invokers.

    // retry loop.
    int timesTotal = timesRetry + 1;
    int times = 0;
    String addr = null;
    for (; times < timesTotal; times++) {
      long beginTimestamp = System.currentTimeMillis();
      long endTimestamp = beginTimestamp;
      Invoker invoker = select(defaultRouter, invokers, invoked);
      invoked.add(invoker);
      addr = invoker.getAddr();

      long costTime = System.currentTimeMillis() - beginTimestamp;
      if (timeoutMillis < costTime) {
        throw new RemotingTooMuchRequestException(
            String.format(
                "retryInvoke call timeout %s for key %s",
                timeoutMillis, ((PullIdBackRequestHeader) requestHeader).getKey()));
      }

      try {
        switch (communicationMode) {
          case ASYNC:
            if (times == 0 && (scheduleThreadTime + costTime) > timeoutMillis) {
              throw new RemotingTooMuchRequestException(
                  String.format(
                      "retryInvoke call timeout %s for key %s",
                      timeoutMillis, ((PullIdBackRequestHeader) requestHeader).getKey()));
            }
            defaultUserClientImpl
                .getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .pull(
                    addr,
                    requestHeader,
                    attachments,
                    times == 0
                        ? (timeoutMillis - scheduleThreadTime - costTime)
                        : (timeoutMillis - costTime),
                    communicationMode,
                    pullCallback);
            return null;
          case ONEWAY:
            return null;
          case SYNC:
            return defaultUserClientImpl
                .getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .pull(
                    addr, requestHeader, attachments, timeoutMillis - costTime, communicationMode);
          default:
            break;
        }

      } catch (RemotingException | InterruptedException | MyberryServerException e) {
        endTimestamp = System.currentTimeMillis();
        log.warn(
            String.format(
                "doInvoke exception, resend at once, timesRetry: %s, costTime: %sms, Server: %s",
                times, endTimestamp - beginTimestamp, addr),
            e);
        continue;
      }
    }

    throw new RemotingException(
        String.format(
            "no route info of this key: %s", ((PullIdBackRequestHeader) requestHeader).getKey()));
  }

  @Override
  public PullResult doInvoke(
      final DefaultUserClientImpl defaultUserClientImpl,
      final CommandCustomHeader requestHeader,
      final HashMap<String, String> attachments,
      final String sessionKey,
      final long timeoutMillis,
      final int timesRetry,
      final CommunicationMode communicationMode)
      throws RemotingException, InterruptedException, MyberryServerException {
    return retryInvoke(
        defaultUserClientImpl,
        requestHeader,
        attachments,
        sessionKey,
        timeoutMillis,
        timesRetry,
        communicationMode);
  }

  @Override
  public void doInvoke(
      final DefaultUserClientImpl defaultUserClientImpl,
      final CommandCustomHeader requestHeader,
      final HashMap<String, String> attachments,
      final String sessionKey,
      final long timeoutMillis,
      final int timesRetry,
      final CommunicationMode communicationMode,
      final PullCallback pullCallback)
      throws MyberryClientException {
    final long beginStartTime = System.currentTimeMillis();
    try {
      this.asyncSenderExecutor.submit(
          new Runnable() {
            @Override
            public void run() {
              long costTime = System.currentTimeMillis() - beginStartTime;
              if (timeoutMillis > costTime) {
                try {
                  retryInvoke(
                      defaultUserClientImpl,
                      requestHeader,
                      attachments,
                      sessionKey,
                      timeoutMillis,
                      timesRetry,
                      communicationMode,
                      pullCallback,
                      costTime);
                } catch (Exception e) {
                  pullCallback.onException(e);
                }
              } else {
                pullCallback.onException(
                    new RemotingTooMuchRequestException("DEFAULT ASYNC send call timeout"));
              }
            }
          });
    } catch (RejectedExecutionException e) {
      throw new MyberryClientException("executor rejected ", e);
    }
  }

  private Invoker select(
      DefaultRouter defaultRouter,
      List<Invoker> invokers,
      List<Invoker> selected,
      String sessionKey) {

    if (MixAll.isEmpty(invokers)) {
      return null;
    }
    if (invokers.size() == 1) {
      return invokers.get(0);
    }

    Invoker invoker = defaultRouter.getInvoker(invokers, sessionKey);
    // If the `invoker` is in the `selected`, then reselect.
    if (selected != null && selected.contains(invoker)) {
      try {
        Invoker reInvoker = reSelect(defaultRouter, invokers, selected, sessionKey);
        if (reInvoker != null) {
          invoker = reInvoker;
        } else {
          // Check the index of current selected invoker, if it's not the last one, choose the one
          // at
          // index+1.
          int index = invokers.indexOf(invoker);
          try {
            // Avoid collision
            invoker = invokers.get((index + 1) % invokers.size());
          } catch (Exception e) {
            log.warn("{} may because invokers list dynamic change, ignore.", e.getMessage(), e);
          }
        }
      } catch (Throwable t) {
        log.error("reselect fail reason is : {}", t);
      }
    }
    return invoker;
  }

  /**
   * Reselect, use invokers not in `selected` first, if all invokers are in `selected`, just pick an
   * invoker using loadbalance policy.
   *
   * @param defaultRouter default router policy
   * @param invokers invoker candidates
   * @param selected exclude selected invokers or not
   * @param sessionKey consistent-hash sessionKey
   * @return the selected server address
   */
  private Invoker reSelect(
      DefaultRouter defaultRouter,
      List<Invoker> invokers,
      List<Invoker> selected,
      String sessionKey) {

    // Allocating one in advance, this list is certain to be used.
    List<Invoker> reselectInvokers =
        new ArrayList<>(invokers.size() > 1 ? (invokers.size() - 1) : invokers.size());

    // First, try picking a invoker not in `selected`.
    for (Invoker invoker : invokers) {
      if (selected == null || !selected.contains(invoker)) {
        reselectInvokers.add(invoker);
      }
    }

    if (!reselectInvokers.isEmpty()) {
      return defaultRouter.getInvoker(reselectInvokers, sessionKey);
    }

    // Just pick an available invoker using loadbalance policy
    if (selected != null) {
      for (Invoker invoker : selected) {
        if (!reselectInvokers.contains(invoker)) {
          reselectInvokers.add(invoker);
        }
      }
    }
    if (!reselectInvokers.isEmpty()) {
      return defaultRouter.getInvoker(reselectInvokers, sessionKey);
    }
    return null;
  }

  private PullResult retryInvoke(
      final DefaultUserClientImpl defaultUserClientImpl,
      final CommandCustomHeader requestHeader,
      final HashMap<String, String> attachments,
      final String sessionKey,
      final long timeoutMillis,
      final int timesRetry,
      final CommunicationMode communicationMode)
      throws RemotingException {
    return retryInvoke(
        defaultUserClientImpl,
        requestHeader,
        attachments,
        sessionKey,
        timeoutMillis,
        timesRetry,
        communicationMode,
        null,
        0L);
  }

  private PullResult retryInvoke(
      final DefaultUserClientImpl defaultUserClientImpl,
      final CommandCustomHeader requestHeader,
      final HashMap<String, String> attachments,
      final String sessionKey,
      final long timeoutMillis,
      final int timesRetry,
      final CommunicationMode communicationMode,
      final PullCallback pullCallback,
      final long scheduleThreadTime)
      throws RemotingException {
    DefaultRouter defaultRouter = defaultUserClientImpl.getDefaultUserClient().getDefaultRouter();

    List<Invoker> invokers = defaultRouter.getInvokers();
    List<Invoker> invoked = new ArrayList<>(invokers.size()); // invoked invokers.

    // retry loop.
    int timesTotal = timesRetry + 1;
    int times = 0;
    String addr = null;
    for (; times < timesTotal; times++) {
      long beginTimestamp = System.currentTimeMillis();
      long endTimestamp = beginTimestamp;
      Invoker invoker = select(defaultRouter, invokers, invoked, sessionKey);
      invoked.add(invoker);
      addr = invoker.getAddr();

      long costTime = System.currentTimeMillis() - beginTimestamp;
      if (timeoutMillis < costTime) {
        throw new RemotingTooMuchRequestException(
            String.format(
                "retryInvoke call timeout %s for key %s",
                timeoutMillis, ((PullIdBackRequestHeader) requestHeader).getKey()));
      }

      try {
        switch (communicationMode) {
          case ASYNC:
            if (times == 0 && (scheduleThreadTime + costTime) > timeoutMillis) {
              throw new RemotingTooMuchRequestException(
                  String.format(
                      "retryInvoke call timeout %s for key %s",
                      timeoutMillis, ((PullIdBackRequestHeader) requestHeader).getKey()));
            }
            defaultUserClientImpl
                .getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .pull(
                    addr,
                    requestHeader,
                    attachments,
                    times == 0
                        ? (timeoutMillis - scheduleThreadTime - costTime)
                        : (timeoutMillis - costTime),
                    communicationMode,
                    pullCallback);
            return null;
          case ONEWAY:
            return null;
          case SYNC:
            return defaultUserClientImpl
                .getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .pull(
                    addr, requestHeader, attachments, timeoutMillis - costTime, communicationMode);
          default:
            break;
        }

      } catch (RemotingException | InterruptedException | MyberryServerException e) {
        endTimestamp = System.currentTimeMillis();
        log.warn(
            String.format(
                "doInvoke exception, resend at once, timesRetry: %s, costTime: %sms, Server: %s",
                times, endTimestamp - beginTimestamp, addr),
            e);
        continue;
      }
    }

    throw new RemotingException(
        String.format(
            "no route info of this key: %s", ((PullIdBackRequestHeader) requestHeader).getKey()));
  }
}
