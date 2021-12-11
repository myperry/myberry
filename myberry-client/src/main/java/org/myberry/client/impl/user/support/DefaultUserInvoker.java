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
package org.myberry.client.impl.user.support;

import java.util.HashMap;
import java.util.Map;
import org.myberry.client.exception.MyberryClientException;
import org.myberry.client.exception.MyberryServerException;
import org.myberry.client.impl.CommunicationMode;
import org.myberry.client.impl.user.DefaultUserClientImpl;
import org.myberry.client.support.AbstractInvoker;
import org.myberry.client.support.FailfastInvoker;
import org.myberry.client.support.FailoverInvoker;
import org.myberry.client.user.PullCallback;
import org.myberry.client.user.PullResult;
import org.myberry.remoting.CommandCustomHeader;
import org.myberry.remoting.exception.RemotingException;

public class DefaultUserInvoker {

  private final Map<String, AbstractInvoker> invokerTable = new HashMap<>();

  private final DefaultUserClientImpl defaultUserClientImpl;

  public DefaultUserInvoker(final DefaultUserClientImpl defaultUserClientImpl) {
    this.defaultUserClientImpl = defaultUserClientImpl;
    this.invokerTable.put(
        FailfastInvoker.NAME, new FailfastInvoker(defaultUserClientImpl.getAsyncSenderExecutor()));
    this.invokerTable.put(
        FailoverInvoker.NAME, new FailoverInvoker(defaultUserClientImpl.getAsyncSenderExecutor()));
  }

  public PullResult pull( //
      final CommandCustomHeader requestHeader, //
      final HashMap<String, String> attachments, //
      final long timeoutMillis, //
      final int timesRetry, //
      final CommunicationMode communicationMode //
      )
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return this.autoSelect(timesRetry)
        .doInvoke(
            defaultUserClientImpl,
            requestHeader,
            attachments,
            timeoutMillis,
            timesRetry,
            communicationMode);
  }

  public PullResult pull( //
      final CommandCustomHeader requestHeader, //
      final HashMap<String, String> attachments, //
      final long timeoutMillis, //
      final int timesRetry, //
      final CommunicationMode communicationMode, //
      final PullCallback pullCallback //
      )
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    this.autoSelect(timesRetry)
        .doInvoke(
            defaultUserClientImpl,
            requestHeader,
            attachments,
            timeoutMillis,
            timesRetry,
            communicationMode,
            pullCallback);
    return null;
  }

  public PullResult pull( //
      final CommandCustomHeader requestHeader, //
      final HashMap<String, String> attachments, //
      final String sessionKey, //
      final long timeoutMillis, //
      final int timesRetry, //
      final CommunicationMode communicationMode //
      )
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    return this.autoSelect(timesRetry)
        .doInvoke(
            defaultUserClientImpl,
            requestHeader,
            attachments,
            sessionKey,
            timeoutMillis,
            timesRetry,
            communicationMode);
  }

  public PullResult pull( //
      final CommandCustomHeader requestHeader, //
      final HashMap<String, String> attachments, //
      final String sessionKey, //
      final long timeoutMillis, //
      final int timesRetry, //
      final CommunicationMode communicationMode, //
      final PullCallback pullCallback //
      )
      throws RemotingException, InterruptedException, MyberryServerException,
          MyberryClientException {
    this.autoSelect(timesRetry)
        .doInvoke(
            defaultUserClientImpl,
            requestHeader,
            attachments,
            sessionKey,
            timeoutMillis,
            timesRetry,
            communicationMode,
            pullCallback);
    return null;
  }

  private AbstractInvoker autoSelect(int timesRetry) throws MyberryClientException {
    if (timesRetry < 0) {
      throw new MyberryClientException("timesRetry < 0");
    }
    switch (timesRetry) {
      case 0:
        return invokerTable.get(FailfastInvoker.NAME);
      default:
        return invokerTable.get(FailoverInvoker.NAME);
    }
  }
}
