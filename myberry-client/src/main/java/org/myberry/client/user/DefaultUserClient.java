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
package org.myberry.client.user;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import org.myberry.client.AbstractMyberryClient;
import org.myberry.client.exception.MyberryClientException;
import org.myberry.client.exception.MyberryServerException;
import org.myberry.client.impl.user.DefaultUserClientImpl;
import org.myberry.remoting.exception.RemotingException;
import org.myberry.remoting.netty.NettyRemotingClient;

public class DefaultUserClient extends AbstractMyberryClient implements UserClient {

  protected final transient DefaultUserClientImpl defaultUserClientImpl;
  /** The group has DefaultUserClient and DefaultAdminClient. */
  private String clientGroup;
  /** Timeout for pulling messages. */
  private int pullMsgTimeout = 3000;

  /** Default constructor. */
  public DefaultUserClient() {
    this.defaultUserClientImpl = new DefaultUserClientImpl(this);
  }

  /**
   * Start this UserClient instance. <strong> Much internal initializing procedures are carried out
   * to make this instance prepared, thus, it's a must to invoke this method before pulling or
   * querying information. </strong>
   *
   * @throws MyberryClientException if there is any unexpected error.
   */
  @Override
  public void start() throws MyberryClientException {
    this.defaultUserClientImpl.start();
  }

  /** This method shuts down this UserClient instance and releases related resources. */
  @Override
  public void shutdown() {
    this.defaultUserClientImpl.shutdown();
  }

  /**
   * Use weighted round-robin load balance to pull id in synchronous mode. This method returns only
   * when the pulling procedure totally completes.
   *
   * @param key mandatory.
   * @return {@link PullResult} instance to inform pullers details of the deliverable, say key of
   *     the id, {@link PullStatus} indicating id status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with server.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public PullResult pull(String key)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return this.pull(key, (HashMap<String, String>) null);
  }

  /**
   * Use weighted round-robin load balance to pull id in synchronous mode. This method returns only
   * when the pulling procedure totally completes.
   *
   * @param key mandatory.
   * @param attachments optional.
   * @return {@link PullResult} instance to inform pullers details of the deliverable, say key of
   *     the id, {@link PullStatus} indicating id status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with server.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public PullResult pull(String key, HashMap<String, String> attachments)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return defaultUserClientImpl.pull(key, attachments);
  }

  /**
   * Same to {@link #pull(java.lang.String)} with pull timeout specified in addition.
   *
   * @param key mandatory.
   * @param timeout pull timeout.
   * @return {@link PullResult} instance to inform pullers details of the deliverable, say key of
   *     the id, {@link PullStatus} indicating id status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public PullResult pull(String key, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return this.pull(key, (HashMap<String, String>) null, timeout);
  }

  /**
   * Same to {@link #pull(java.lang.String, HashMap)} with pull timeout specified in addition.
   *
   * @param key mandatory.
   * @param attachments optional.
   * @param timeout pull timeout.
   * @return {@link PullResult} instance to inform pullers details of the deliverable, say key of
   *     the id, {@link PullStatus} indicating id status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public PullResult pull(String key, HashMap<String, String> attachments, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return defaultUserClientImpl.pull(key, attachments, timeout);
  }

  /**
   * Same to {@link #pull(java.lang.String, long)} with pull retry specified in addition.
   *
   * @param key mandatory.
   * @param timeout timeout in each time.
   * @param timesRetry number of retries after request failed.
   * @return {@link PullResult} instance to inform pullers details of the deliverable, say key of
   *     the id, {@link PullStatus} indicating id status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public PullResult pull(String key, long timeout, int timesRetry)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return this.pull(key, (HashMap<String, String>) null, timeout, timesRetry);
  }

  /**
   * Same to {@link #pull(java.lang.String, HashMap, long)} with pull retry specified in addition.
   *
   * @param key mandatory.
   * @param attachments optional.
   * @param timeout timeout in each time.
   * @param timesRetry number of retries after request failed.
   * @return {@link PullResult} instance to inform pullers details of the deliverable, say key of
   *     the id, {@link PullStatus} indicating id status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public PullResult pull(
      String key, HashMap<String, String> attachments, long timeout, int timesRetry)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return defaultUserClientImpl.pull(key, attachments, timeout, timesRetry);
  }

  /**
   * Use consistent-hash load balance to pull id in synchronous mode. This method returns only when
   * the pulling procedure totally completes.
   *
   * @param key mandatory.
   * @param sessionKey mandatory, only for scheduling.
   * @return {@link PullResult} instance to inform pullers details of the deliverable, say key of
   *     the id, {@link PullStatus} indicating id status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with server.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public PullResult pull(String key, String sessionKey)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return this.pull(key, (HashMap) null, sessionKey);
  }

  /**
   * Use consistent-hash load balance to pull id in synchronous mode. This method returns only when
   * the pulling procedure totally completes.
   *
   * @param key mandatory.
   * @param attachments optional.
   * @param sessionKey mandatory, only for scheduling.
   * @return {@link PullResult} instance to inform pullers details of the deliverable, say key of
   *     the id, {@link PullStatus} indicating id status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with server.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public PullResult pull(String key, HashMap<String, String> attachments, String sessionKey)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return defaultUserClientImpl.pull(key, attachments, sessionKey);
  }

  /**
   * Same to {@link #pull(java.lang.String, java.lang.String)} with pull timeout specified in
   * addition.
   *
   * @param key mandatory.
   * @param sessionKey mandatory, only for scheduling.
   * @param timeout pull timeout.
   * @return {@link PullResult} instance to inform pullers details of the deliverable, say key of
   *     the id, {@link PullStatus} indicating id status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public PullResult pull(String key, String sessionKey, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return this.pull(key, (HashMap) null, sessionKey, timeout);
  }

  /**
   * Same to {@link #pull(java.lang.String, HashMap, java.lang.String)} with pull timeout specified
   * in addition.
   *
   * @param key mandatory.
   * @param attachments optional.
   * @param sessionKey mandatory, only for scheduling.
   * @param timeout pull timeout.
   * @return {@link PullResult} instance to inform pullers details of the deliverable, say key of
   *     the id, {@link PullStatus} indicating id status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public PullResult pull(
      String key, HashMap<String, String> attachments, String sessionKey, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return defaultUserClientImpl.pull(key, attachments, sessionKey, timeout);
  }

  /**
   * Same to {@link #pull(java.lang.String, java.lang.String, long)} with pull retry specified in
   * addition.
   *
   * @param key mandatory.
   * @param sessionKey mandatory, only for scheduling.
   * @param timeout timeout in each time.
   * @param timesRetry number of retries after request failed.
   * @return {@link PullResult} instance to inform pullers details of the deliverable, say key of
   *     the id, {@link PullStatus} indicating id status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public PullResult pull(String key, String sessionKey, long timeout, int timesRetry)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return this.pull(key, (HashMap) null, sessionKey, timeout, timesRetry);
  }

  /**
   * Same to {@link #pull(java.lang.String, HashMap, java.lang.String, long)} with pull retry
   * specified in addition.
   *
   * @param key mandatory.
   * @param attachments optional.
   * @param sessionKey mandatory, only for scheduling.
   * @param timeout timeout in each time.
   * @param timesRetry number of retries after request failed.
   * @return {@link PullResult} instance to inform pullers details of the deliverable, say key of
   *     the id, {@link PullStatus} indicating id status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public PullResult pull(
      String key,
      HashMap<String, String> attachments,
      String sessionKey,
      long timeout,
      int timesRetry)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return defaultUserClientImpl.pull(key, attachments, sessionKey, timeout, timesRetry);
  }

  /**
   * Use weighted round-robin load balance to pull id in asynchronously mode. This method returns
   * immediately. On pulling completion, <code>pullCallback</code> will be executed.
   *
   * @param key mandatory.
   * @param pullCallback Callback to execute on pulling completed, either successful or
   *     unsuccessful.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public void pull(String key, PullCallback pullCallback)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    this.pull(key, null, pullCallback);
  }

  /**
   * Use weighted round-robin load balance to pull id in asynchronously mode. This method returns
   * immediately. On pulling completion, <code>pullCallback</code> will be executed.
   *
   * @param key mandatory.
   * @param attachments optional.
   * @param pullCallback Callback to execute on pulling completed, either successful or
   *     unsuccessful.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public void pull(String key, HashMap<String, String> attachments, PullCallback pullCallback)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    defaultUserClientImpl.pull(key, attachments, pullCallback);
  }

  /**
   * Same to {@link #pull(java.lang.String, PullCallback)} with pull timeout specified in addition.
   *
   * @param key mandatory.
   * @param pullCallback Callback to execute on pulling completed, either successful or
   *     unsuccessful.
   * @param timeout pull timeout.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public void pull(String key, PullCallback pullCallback, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    this.pull(key, null, pullCallback, timeout);
  }

  /**
   * Same to {@link #pull(java.lang.String, HashMap, PullCallback)} with pull timeout specified in
   * addition.
   *
   * @param key mandatory.
   * @param attachments optional.
   * @param pullCallback Callback to execute on pulling completed, either successful or
   *     unsuccessful.
   * @param timeout pull timeout.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public void pull(
      String key, HashMap<String, String> attachments, PullCallback pullCallback, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    defaultUserClientImpl.pull(key, attachments, pullCallback, timeout);
  }

  /**
   * Same to {@link #pull(java.lang.String, PullCallback, long)} with pull retry specified in
   * addition.
   *
   * @param key mandatory.
   * @param pullCallback Callback to execute on pulling completed, either successful or
   *     unsuccessful.
   * @param timeout timeout in each time.
   * @param timesRetry number of retries after request failed.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public void pull(String key, PullCallback pullCallback, long timeout, int timesRetry)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    this.pull(key, null, pullCallback, timeout, timesRetry);
  }

  /**
   * Same to {@link #pull(java.lang.String, HashMap, PullCallback, long)} with pull retry specified
   * in addition.
   *
   * @param key mandatory.
   * @param attachments optional.
   * @param pullCallback Callback to execute on pulling completed, either successful or
   *     unsuccessful.
   * @param timeout timeout in each time.
   * @param timesRetry number of retries after request failed.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public void pull(
      String key,
      HashMap<String, String> attachments,
      PullCallback pullCallback,
      long timeout,
      int timesRetry)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    defaultUserClientImpl.pull(key, attachments, pullCallback, timeout, timesRetry);
  }

  /**
   * Use consistent-hash load balance to pull id in asynchronously mode. This method returns
   * immediately. On pulling completion, <code>pullCallback</code> will be executed.
   *
   * @param key mandatory.
   * @param pullCallback Callback to execute on pulling completed, either successful or
   *     unsuccessful.
   * @param sessionKey mandatory, only for scheduling.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public void pull(String key, PullCallback pullCallback, String sessionKey)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    this.pull(key, null, pullCallback, sessionKey);
  }

  /**
   * Use consistent-hash load balance to pull id in asynchronously mode. This method returns
   * immediately. On pulling completion, <code>pullCallback</code> will be executed.
   *
   * @param key mandatory.
   * @param attachments optional.
   * @param pullCallback Callback to execute on pulling completed, either successful or
   *     unsuccessful.
   * @param sessionKey mandatory, only for scheduling.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public void pull(
      String key, HashMap<String, String> attachments, PullCallback pullCallback, String sessionKey)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    defaultUserClientImpl.pull(key, attachments, pullCallback, sessionKey);
  }

  /**
   * Same to {@link #pull(java.lang.String, PullCallback, java.lang.String)} with pull timeout
   * specified in addition.
   *
   * @param key mandatory.
   * @param pullCallback Callback to execute on pulling completed, either successful or
   *     unsuccessful.
   * @param sessionKey mandatory, only for scheduling.
   * @param timeout pull timeout.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public void pull(String key, PullCallback pullCallback, String sessionKey, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    this.pull(key, null, pullCallback, sessionKey, timeout);
  }

  /**
   * Same to {@link #pull(java.lang.String, HashMap, PullCallback, java.lang.String)} with pull
   * timeout specified in addition.
   *
   * @param key mandatory.
   * @param attachments optional.
   * @param pullCallback Callback to execute on pulling completed, either successful or
   *     unsuccessful.
   * @param sessionKey mandatory, only for scheduling.
   * @param timeout pull timeout.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public void pull(
      String key,
      HashMap<String, String> attachments,
      PullCallback pullCallback,
      String sessionKey,
      long timeout)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    defaultUserClientImpl.pull(key, attachments, pullCallback, sessionKey, timeout);
  }

  /**
   * Same to {@link #pull(java.lang.String, PullCallback, java.lang.String, long)} with pull retry
   * specified in addition.
   *
   * @param key mandatory.
   * @param pullCallback Callback to execute on pulling completed, either successful or
   *     unsuccessful.
   * @param sessionKey mandatory, only for scheduling.
   * @param timeout timeout in each time.
   * @param timesRetry number of retries after request failed.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public void pull(
      String key, PullCallback pullCallback, String sessionKey, long timeout, int timesRetry)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    this.pull(key, null, pullCallback, sessionKey, timeout, timesRetry);
  }

  /**
   * Same to {@link #pull(java.lang.String, HashMap, PullCallback, java.lang.String, long)} with
   * pull retry specified in addition.
   *
   * @param key mandatory.
   * @param attachments optional.
   * @param pullCallback Callback to execute on pulling completed, either successful or
   *     unsuccessful.
   * @param sessionKey mandatory, only for scheduling.
   * @param timeout timeout in each time.
   * @param timesRetry number of retries after request failed.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the pulling thread is interrupted.
   * @throws MyberryClientException if there is any error with client.
   */
  @Override
  public void pull(
      String key,
      HashMap<String, String> attachments,
      PullCallback pullCallback,
      String sessionKey,
      long timeout,
      int timesRetry)
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    defaultUserClientImpl.pull(key, attachments, pullCallback, sessionKey, timeout, timesRetry);
  }

  /**
   * Sets an Executor to be used for executing callback methods. If the Executor is not set, {@link
   * NettyRemotingClient#publicExecutor} will be used.
   *
   * @param callbackExecutor the instance of Executor
   */
  public void setCallbackExecutor(final ExecutorService callbackExecutor) {
    this.defaultUserClientImpl.setCallbackExecutor(callbackExecutor);
  }

  /**
   * Sets an Executor to be used for executing asynchronous send. If the Executor is not set, {@link
   * DefaultUserClientImpl#defaultAsyncSenderExecutor} will be used.
   *
   * @param asyncSenderExecutor the instance of Executor
   */
  public void setAsyncSenderExecutor(final ExecutorService asyncSenderExecutor) {
    this.defaultUserClientImpl.setAsyncSenderExecutor(asyncSenderExecutor);
  }

  public DefaultUserClientImpl getDefaultUserClientImpl() {
    return defaultUserClientImpl;
  }

  public void setClientGroup(String clientGroup) {
    this.clientGroup = clientGroup;
  }

  public int getPullMsgTimeout() {
    return pullMsgTimeout;
  }

  public void setPullMsgTimeout(int pullMsgTimeout) {
    this.pullMsgTimeout = pullMsgTimeout;
  }

  @Override
  public String getClientGroup() {
    return clientGroup == null ? DefaultUserClient.class.getSimpleName() : clientGroup;
  }
}
