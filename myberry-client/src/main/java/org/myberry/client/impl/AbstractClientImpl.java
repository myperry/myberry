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
package org.myberry.client.impl;

import java.util.concurrent.ExecutorService;
import org.myberry.client.AbstractMyberryClient;
import org.myberry.client.exception.MyberryClientException;
import org.myberry.client.impl.factory.MyberryClientInstance;
import org.myberry.common.ServiceState;

public abstract class AbstractClientImpl {

  private final AbstractMyberryClient abstractMyberryClient;

  protected ExecutorService defaultAsyncSenderExecutor;

  private ServiceState serviceState = ServiceState.CREATE_JUST;
  private MyberryClientInstance myberryClientFactory;

  public AbstractClientImpl(final AbstractMyberryClient abstractMyberryClient) {
    this.abstractMyberryClient = abstractMyberryClient;
  }

  public void start() throws MyberryClientException {
    switch (this.serviceState) {
      case CREATE_JUST:
        this.serviceState = ServiceState.START_FAILED;
        this.myberryClientFactory =
            MyberryClientManager.getInstance()
                .getAndCreateClientInstance(this.abstractMyberryClient);

        boolean registerOK =
            myberryClientFactory.registerClient(abstractMyberryClient.getClientGroup(), this);
        if (!registerOK) {
          this.serviceState = ServiceState.CREATE_JUST;
          throw new MyberryClientException(
              "The client group["
                  + this.abstractMyberryClient.getClientGroup()
                  + "] has been created before.",
              null);
        }

        myberryClientFactory.start();
        this.serviceState = ServiceState.RUNNING;
        break;
      case RUNNING:
      case START_FAILED:
      case SHUTDOWN_ALREADY:
        throw new MyberryClientException("The service state not OK, maybe started once, ", null);
      default:
        break;
    }
  }

  public void shutdown() {
    switch (this.serviceState) {
      case CREATE_JUST:
        break;
      case RUNNING:
        this.myberryClientFactory.unregisterClient(abstractMyberryClient.getClientGroup());
        if (this.defaultAsyncSenderExecutor != null) {
          this.defaultAsyncSenderExecutor.shutdown();
        }
        this.myberryClientFactory.shutdown();
        this.serviceState = ServiceState.SHUTDOWN_ALREADY;
        break;
      case SHUTDOWN_ALREADY:
        break;
      default:
        break;
    }
  }

  public MyberryClientInstance getMyberryClientFactory() {
    return myberryClientFactory;
  }
}
