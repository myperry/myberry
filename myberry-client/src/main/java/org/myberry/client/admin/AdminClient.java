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
package org.myberry.client.admin;

import org.myberry.client.exception.MyberryServerException;
import org.myberry.common.Component;
import org.myberry.remoting.exception.RemotingException;

public interface AdminClient {

  SendResult createComponent(Component component)
      throws RemotingException, InterruptedException, MyberryServerException;

  SendResult createComponent(Component component, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException;

  SendResult queryComponentSize()
      throws RemotingException, InterruptedException, MyberryServerException;

  SendResult queryComponentSize(long timeout)
      throws RemotingException, InterruptedException, MyberryServerException;

  SendResult queryComponentByKey(String key)
      throws RemotingException, InterruptedException, MyberryServerException;

  SendResult queryComponentByKey(String key, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException;

  SendResult queryClusterList()
      throws RemotingException, InterruptedException, MyberryServerException;

  SendResult queryClusterList(long timeout)
      throws RemotingException, InterruptedException, MyberryServerException;

  SendResult kickOutInvoker(int kickedSid)
      throws RemotingException, InterruptedException, MyberryServerException;

  SendResult kickOutInvoker(int kickedSid, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException;

  SendResult removeInvoker(int kickedSid)
      throws RemotingException, InterruptedException, MyberryServerException;

  SendResult removeInvoker(int kickedSid, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException;

  SendResult updateWeight(int sid, int weight)
      throws RemotingException, InterruptedException, MyberryServerException;

  SendResult updateWeight(int sid, int weight, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException;
}
