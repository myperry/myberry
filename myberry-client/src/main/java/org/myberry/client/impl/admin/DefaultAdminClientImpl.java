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
package org.myberry.client.impl.admin;

import org.myberry.client.admin.DefaultAdminClient;
import org.myberry.client.admin.SendCallback;
import org.myberry.client.admin.SendResult;
import org.myberry.client.exception.MyberryServerException;
import org.myberry.client.impl.AbstractClientImpl;
import org.myberry.client.impl.CommunicationMode;
import org.myberry.common.protocol.RequestCode;
import org.myberry.common.protocol.header.admin.ManageComponentRequestHeader;
import org.myberry.remoting.exception.RemotingException;

public class DefaultAdminClientImpl extends AbstractClientImpl {

  private final DefaultAdminClient defaultAdminClient;

  public DefaultAdminClientImpl(DefaultAdminClient defaultAdminClient) {
    super(defaultAdminClient);
    this.defaultAdminClient = defaultAdminClient;
  }

  public SendResult createComponent(String password, int structure, byte[] componentData)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.createComponent(
        password, structure, componentData, defaultAdminClient.getSendMsgTimeout());
  }

  public SendResult createComponent(
      String password, int structure, byte[] componentData, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.createComponentImpl(
        password, structure, componentData, CommunicationMode.SYNC, null, timeout);
  }

  private SendResult createComponentImpl( //
      String password, //
      int structure, //
      byte[] componentData, //
      final CommunicationMode communicationMode, //
      final SendCallback sendCallback, //
      final long timeout //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    ManageComponentRequestHeader manageComponentRequestHeader =
        this.createAdminRequestHeader(password, structure);

    SendResult sendResult = null;
    switch (communicationMode) {
      case ASYNC:
        sendResult =
            this.getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .createComponent(
                    RequestCode.CREATE_COMPONENT,
                    defaultAdminClient.getDefaultRouter().getMaintainerAddr(),
                    componentData,
                    manageComponentRequestHeader,
                    timeout,
                    communicationMode,
                    sendCallback);
        break;
      case ONEWAY:
      case SYNC:
        sendResult =
            this.getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .createComponent(
                    RequestCode.CREATE_COMPONENT,
                    defaultAdminClient.getDefaultRouter().getMaintainerAddr(),
                    componentData,
                    manageComponentRequestHeader,
                    timeout,
                    communicationMode);
        break;
      default:
        assert false;
        break;
    }
    return sendResult;
  }

  public SendResult queryComponentSize(String password)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.queryComponentSize(password, defaultAdminClient.getSendMsgTimeout());
  }

  public SendResult queryComponentSize(String password, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.queryComponentSizeImpl(password, CommunicationMode.SYNC, null, timeout);
  }

  private SendResult queryComponentSizeImpl(
      String password, //
      final CommunicationMode communicationMode, //
      final SendCallback sendCallback, //
      final long timeout //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    ManageComponentRequestHeader manageComponentRequestHeader =
        this.createAdminRequestHeader(password, 0);

    SendResult sendResult = null;
    switch (communicationMode) {
      case ASYNC:
        sendResult =
            this.getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .queryComponentSize(
                    RequestCode.QUERY_COMPONENT_SIZE,
                    defaultAdminClient.getDefaultRouter().getMaintainerAddr(),
                    manageComponentRequestHeader,
                    timeout,
                    communicationMode,
                    sendCallback);
        break;
      case ONEWAY:
      case SYNC:
        sendResult =
            this.getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .queryComponentSize(
                    RequestCode.QUERY_COMPONENT_SIZE,
                    defaultAdminClient.getDefaultRouter().getMaintainerAddr(),
                    manageComponentRequestHeader,
                    timeout,
                    communicationMode);
        break;
      default:
        assert false;
        break;
    }
    return sendResult;
  }

  public SendResult queryComponentByKey(String password, byte[] key)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.queryComponentByKey(password, key, defaultAdminClient.getSendMsgTimeout());
  }

  public SendResult queryComponentByKey(String password, byte[] key, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.queryComponentByKeyImpl(key, password, CommunicationMode.SYNC, null, timeout);
  }

  public SendResult queryComponentByKeyImpl(
      byte[] key, //
      String password, //
      final CommunicationMode communicationMode, //
      final SendCallback sendCallback, //
      final long timeout //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    ManageComponentRequestHeader manageComponentRequestHeader =
        this.createAdminRequestHeader(password, 0);

    SendResult sendResult = null;
    switch (communicationMode) {
      case ASYNC:
        sendResult =
            this.getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .queryComponentByKey(
                    RequestCode.QUERY_COMPONENT_BY_KEY,
                    defaultAdminClient.getDefaultRouter().getMaintainerAddr(),
                    key,
                    manageComponentRequestHeader,
                    timeout,
                    communicationMode,
                    sendCallback);
        break;
      case ONEWAY:
      case SYNC:
        sendResult =
            this.getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .queryComponentByKey(
                    RequestCode.QUERY_COMPONENT_BY_KEY,
                    defaultAdminClient.getDefaultRouter().getMaintainerAddr(),
                    key,
                    manageComponentRequestHeader,
                    timeout,
                    communicationMode);
        break;
      default:
        assert false;
        break;
    }
    return sendResult;
  }

  public SendResult queryClusterList(String password)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.queryClusterList(password, defaultAdminClient.getSendMsgTimeout());
  }

  public SendResult queryClusterList(String password, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.queryClusterListImpl(password, CommunicationMode.SYNC, null, timeout);
  }

  private SendResult queryClusterListImpl(
      String password, //
      final CommunicationMode communicationMode, //
      final SendCallback sendCallback, //
      final long timeout //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    ManageComponentRequestHeader manageComponentRequestHeader =
        this.createAdminRequestHeader(password, 0);

    SendResult sendResult = null;
    switch (communicationMode) {
      case ASYNC:
        sendResult =
            this.getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .queryClusterList(
                    RequestCode.QUERY_CLUSTER_LIST,
                    defaultAdminClient.getDefaultRouter().getMaintainerAddr(),
                    manageComponentRequestHeader,
                    timeout,
                    communicationMode,
                    sendCallback);
        break;
      case ONEWAY:
      case SYNC:
        sendResult =
            this.getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .queryClusterList(
                    RequestCode.QUERY_CLUSTER_LIST,
                    defaultAdminClient.getDefaultRouter().getMaintainerAddr(),
                    manageComponentRequestHeader,
                    timeout,
                    communicationMode);
        break;
      default:
        assert false;
        break;
    }
    return sendResult;
  }

  public SendResult kickOutInvoker(String password, byte[] routeData)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.kickOutInvoker(password, routeData, defaultAdminClient.getSendMsgTimeout());
  }

  public SendResult kickOutInvoker(String password, byte[] routeData, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.kickOutInvokerImpl(password, routeData, CommunicationMode.SYNC, null, timeout);
  }

  private SendResult kickOutInvokerImpl(
      String password, //
      byte[] routeData, //
      final CommunicationMode communicationMode, //
      final SendCallback sendCallback, //
      final long timeout //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    ManageComponentRequestHeader manageComponentRequestHeader =
        this.createAdminRequestHeader(password, 0);

    SendResult sendResult = null;
    switch (communicationMode) {
      case ASYNC:
        sendResult =
            this.getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .kickOutInvoker(
                    RequestCode.KICK_OUT_INVOKER,
                    defaultAdminClient.getDefaultRouter().getMaintainerAddr(),
                    routeData,
                    manageComponentRequestHeader,
                    timeout,
                    communicationMode,
                    sendCallback);
        break;
      case ONEWAY:
      case SYNC:
        sendResult =
            this.getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .kickOutInvoker(
                    RequestCode.KICK_OUT_INVOKER,
                    defaultAdminClient.getDefaultRouter().getMaintainerAddr(),
                    routeData,
                    manageComponentRequestHeader,
                    timeout,
                    communicationMode);
        break;
      default:
        assert false;
        break;
    }
    return sendResult;
  }

  public SendResult removeInvoker(String password, byte[] routeData)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.removeInvoker(password, routeData, defaultAdminClient.getSendMsgTimeout());
  }

  public SendResult removeInvoker(String password, byte[] routeData, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.removeInvokerImpl(password, routeData, CommunicationMode.SYNC, null, timeout);
  }

  private SendResult removeInvokerImpl(
      String password, //
      byte[] routeData, //
      final CommunicationMode communicationMode, //
      final SendCallback sendCallback, //
      final long timeout //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    ManageComponentRequestHeader manageComponentRequestHeader =
        this.createAdminRequestHeader(password, 0);

    SendResult sendResult = null;
    switch (communicationMode) {
      case ASYNC:
        sendResult =
            this.getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .removeInvoker(
                    RequestCode.REMOVE_INVOKER,
                    defaultAdminClient.getDefaultRouter().getMaintainerAddr(),
                    routeData,
                    manageComponentRequestHeader,
                    timeout,
                    communicationMode,
                    sendCallback);
        break;
      case ONEWAY:
      case SYNC:
        sendResult =
            this.getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .removeInvoker(
                    RequestCode.REMOVE_INVOKER,
                    defaultAdminClient.getDefaultRouter().getMaintainerAddr(),
                    routeData,
                    manageComponentRequestHeader,
                    timeout,
                    communicationMode);
        break;
      default:
        assert false;
        break;
    }
    return sendResult;
  }

  public SendResult updateWeight(String password, byte[] routeData)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.updateWeight(password, routeData, defaultAdminClient.getSendMsgTimeout());
  }

  public SendResult updateWeight(String password, byte[] routeData, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.updateWeightImpl(password, routeData, CommunicationMode.SYNC, null, timeout);
  }

  private SendResult updateWeightImpl(
      String password, //
      byte[] routeData, //
      final CommunicationMode communicationMode, //
      final SendCallback sendCallback, //
      final long timeout //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    ManageComponentRequestHeader manageComponentRequestHeader =
        this.createAdminRequestHeader(password, 0);

    SendResult sendResult = null;
    switch (communicationMode) {
      case ASYNC:
        sendResult =
            this.getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .updateWeight(
                    RequestCode.UPDATE_WEIGHT,
                    defaultAdminClient.getDefaultRouter().getMaintainerAddr(),
                    routeData,
                    manageComponentRequestHeader,
                    timeout,
                    communicationMode,
                    sendCallback);
        break;
      case ONEWAY:
      case SYNC:
        sendResult =
            this.getMyberryClientFactory()
                .getMyberryClientAPIImpl()
                .updateWeight(
                    RequestCode.UPDATE_WEIGHT,
                    defaultAdminClient.getDefaultRouter().getMaintainerAddr(),
                    routeData,
                    manageComponentRequestHeader,
                    timeout,
                    communicationMode);
        break;
      default:
        assert false;
        break;
    }
    return sendResult;
  }

  private ManageComponentRequestHeader createAdminRequestHeader(String password, int structure) {
    ManageComponentRequestHeader manageComponentRequestHeader = new ManageComponentRequestHeader();
    manageComponentRequestHeader.setPassword(password);
    manageComponentRequestHeader.setStructure(structure);
    return manageComponentRequestHeader;
  }
}
