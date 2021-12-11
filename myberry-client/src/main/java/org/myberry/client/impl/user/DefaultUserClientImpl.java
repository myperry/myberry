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
package org.myberry.client.impl.user;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.myberry.client.exception.MyberryClientException;
import org.myberry.client.exception.MyberryServerException;
import org.myberry.client.impl.AbstractClientImpl;
import org.myberry.client.impl.CommunicationMode;
import org.myberry.client.impl.user.support.DefaultUserInvoker;
import org.myberry.client.user.DefaultUserClient;
import org.myberry.client.user.PullCallback;
import org.myberry.client.user.PullResult;
import org.myberry.common.ThreadFactoryImpl;
import org.myberry.common.protocol.header.user.PullIdBackRequestHeader;
import org.myberry.remoting.exception.RemotingException;

public class DefaultUserClientImpl extends AbstractClientImpl {

  private final DefaultUserClient defaultUserClient;
  private final DefaultUserInvoker defaultUserInvoker;
  private final BlockingQueue<Runnable> asyncSenderThreadPoolQueue;

  private ExecutorService asyncSenderExecutor;

  public DefaultUserClientImpl(final DefaultUserClient defaultUserClient) {
    super(defaultUserClient);
    this.defaultUserClient = defaultUserClient;

    this.asyncSenderThreadPoolQueue = new LinkedBlockingQueue<Runnable>(50000);
    this.defaultAsyncSenderExecutor =
        new ThreadPoolExecutor( //
            Runtime.getRuntime().availableProcessors(), //
            Runtime.getRuntime().availableProcessors(), //
            1000 * 60, //
            TimeUnit.MILLISECONDS, //
            this.asyncSenderThreadPoolQueue, //
            new ThreadFactoryImpl("AsyncSenderExecutor_"));

    this.defaultUserInvoker = new DefaultUserInvoker(this);
  }

  public DefaultUserClient getDefaultUserClient() {
    return defaultUserClient;
  }

  public void setCallbackExecutor(final ExecutorService callbackExecutor) {
    this.getMyberryClientFactory()
        .getMyberryClientAPIImpl()
        .getRemotingClient()
        .setCallbackExecutor(callbackExecutor);
  }

  public ExecutorService getAsyncSenderExecutor() {
    return null == asyncSenderExecutor ? defaultAsyncSenderExecutor : asyncSenderExecutor;
  }

  public void setAsyncSenderExecutor(ExecutorService asyncSenderExecutor) {
    this.asyncSenderExecutor = asyncSenderExecutor;
  }

  public PullResult pull(String key, HashMap<String, String> attachments)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return this.pull(key, attachments, defaultUserClient.getPullMsgTimeout());
  }

  public PullResult pull(String key, HashMap<String, String> attachments, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return this.pull(key, attachments, timeout, 0);
  }

  public PullResult pull(
      String key, HashMap<String, String> attachments, long timeout, int timesRetry)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return this.pullWithoutStatusImpl(
        key, attachments, CommunicationMode.SYNC, null, timeout, timesRetry);
  }

  public void pull(String key, HashMap<String, String> attachments, PullCallback pullCallback)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    this.pull(key, attachments, pullCallback, defaultUserClient.getPullMsgTimeout());
  }

  public void pull(
      String key, HashMap<String, String> attachments, PullCallback pullCallback, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    this.pull(key, attachments, pullCallback, timeout, 0);
  }

  public void pull(
      String key,
      HashMap<String, String> attachments,
      PullCallback pullCallback,
      long timeout,
      int timesRetry)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    this.pullWithoutStatusImpl(
        key, attachments, CommunicationMode.ASYNC, pullCallback, timeout, timesRetry);
  }

  private PullResult pullWithoutStatusImpl( //
      String key, //
      HashMap<String, String> attachments, //
      final CommunicationMode communicationMode, //
      final PullCallback pullCallback, //
      final long timeout, //
      final int timesRetry //
      )
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    PullIdBackRequestHeader pullIdBackRequestHeader = new PullIdBackRequestHeader();
    pullIdBackRequestHeader.setKey(key);

    PullResult pullResult = null;
    switch (communicationMode) {
      case ASYNC:
        pullResult =
            defaultUserInvoker.pull(
                pullIdBackRequestHeader,
                attachments,
                timeout,
                timesRetry,
                communicationMode,
                pullCallback);
        break;
      case ONEWAY:
      case SYNC:
        pullResult =
            defaultUserInvoker.pull(
                pullIdBackRequestHeader, attachments, timeout, timesRetry, communicationMode);
        break;
      default:
        assert false;
        break;
    }
    return pullResult;
  }

  public PullResult pull(String key, HashMap<String, String> attachments, String sessionKey)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return this.pull(key, attachments, sessionKey, defaultUserClient.getPullMsgTimeout());
  }

  public PullResult pull(
      String key, HashMap<String, String> attachments, String sessionKey, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return this.pull(key, attachments, sessionKey, timeout, 0);
  }

  public PullResult pull(
      String key,
      HashMap<String, String> attachments,
      String sessionKey,
      long timeout,
      int timesRetry)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return this.pullWithStatusImpl(
        key, attachments, CommunicationMode.SYNC, null, sessionKey, timeout, timesRetry);
  }

  public void pull(
      String key, HashMap<String, String> attachments, PullCallback pullCallback, String sessionKey)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    this.pull(key, attachments, pullCallback, sessionKey, defaultUserClient.getPullMsgTimeout());
  }

  public void pull(
      String key,
      HashMap<String, String> attachments,
      PullCallback pullCallback,
      String sessionKey,
      long timeout)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    this.pull(key, attachments, pullCallback, sessionKey, timeout, 0);
  }

  public void pull(
      String key,
      HashMap<String, String> attachments,
      PullCallback pullCallback,
      String sessionKey,
      long timeout,
      int timesRetry)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    this.pullWithStatusImpl(
        key, attachments, CommunicationMode.ASYNC, pullCallback, sessionKey, timeout, timesRetry);
  }

  private PullResult pullWithStatusImpl( //
      String key, //
      HashMap<String, String> attachments, //
      final CommunicationMode communicationMode, //
      final PullCallback pullCallback, //
      final String sessionKey, //
      final long timeout, //
      final int timesRetry //
      )
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    PullIdBackRequestHeader pullIdBackRequestHeader = new PullIdBackRequestHeader();
    pullIdBackRequestHeader.setKey(key);

    PullResult pullResult = null;
    switch (communicationMode) {
      case ASYNC:
        pullResult =
            defaultUserInvoker.pull(
                pullIdBackRequestHeader,
                attachments,
                sessionKey,
                timeout,
                timesRetry,
                communicationMode,
                pullCallback);
        break;
      case ONEWAY:
      case SYNC:
        pullResult =
            defaultUserInvoker.pull(
                pullIdBackRequestHeader,
                attachments,
                sessionKey,
                timeout,
                timesRetry,
                communicationMode);
        break;
      default:
        assert false;
        break;
    }
    return pullResult;
  }
}
