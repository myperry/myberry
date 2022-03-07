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

import java.util.HashMap;
import org.myberry.client.admin.SendCallback;
import org.myberry.client.admin.SendResult;
import org.myberry.client.admin.SendStatus;
import org.myberry.client.exception.MyberryServerException;
import org.myberry.client.router.RouterInfo;
import org.myberry.client.user.PullCallback;
import org.myberry.client.user.PullResult;
import org.myberry.client.user.PullStatus;
import org.myberry.common.codec.LightCodec;
import org.myberry.common.codec.util.Maps;
import org.myberry.common.protocol.RequestCode;
import org.myberry.common.protocol.ResponseCode;
import org.myberry.common.protocol.body.HeartbeatData;
import org.myberry.common.protocol.body.admin.CRComponentData;
import org.myberry.common.protocol.body.admin.ClusterListData;
import org.myberry.common.protocol.body.admin.ComponentSizeData;
import org.myberry.common.protocol.body.admin.NSComponentData;
import org.myberry.common.protocol.body.admin.RouteData;
import org.myberry.common.protocol.body.user.CRPullResultData;
import org.myberry.common.protocol.body.user.NSPullResultData;
import org.myberry.common.protocol.header.admin.ManageComponentResponseHeader;
import org.myberry.common.protocol.header.user.PullIdBackResponseHeader;
import org.myberry.common.structure.Structure;
import org.myberry.remoting.CommandCustomHeader;
import org.myberry.remoting.InvokeCallback;
import org.myberry.remoting.RemotingClient;
import org.myberry.remoting.exception.RemotingCommandException;
import org.myberry.remoting.exception.RemotingConnectException;
import org.myberry.remoting.exception.RemotingException;
import org.myberry.remoting.exception.RemotingSendRequestException;
import org.myberry.remoting.exception.RemotingTimeoutException;
import org.myberry.remoting.netty.NettyClientConfig;
import org.myberry.remoting.netty.NettyRemotingClient;
import org.myberry.remoting.netty.ResponseFuture;
import org.myberry.remoting.protocol.RemotingCommand;

public class MyberryClientAPIImpl {

  private final RemotingClient remotingClient;

  public MyberryClientAPIImpl(final NettyClientConfig nettyClientConfig) {
    this.remotingClient = new NettyRemotingClient(nettyClientConfig);
  }

  public void start() {
    this.remotingClient.start();
  }

  public void shutdown() {
    this.remotingClient.shutdown();
  }

  public RemotingClient getRemotingClient() {
    return remotingClient;
  }

  public PullResult pull( //
      final String addr, //
      final CommandCustomHeader requestHeader, //
      final HashMap<String, String> attachments, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    return pull(addr, requestHeader, attachments, timeoutMillis, communicationMode, null);
  }

  public PullResult pull( //
      final String addr, //
      final CommandCustomHeader requestHeader, //
      final HashMap<String, String> attachments, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode, //
      final PullCallback pullCallback //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    RemotingCommand request =
        RemotingCommand.createRequestCommand(RequestCode.PULL_ID, requestHeader);
    request.setBody(Maps.serialize(attachments));
    switch (communicationMode) {
      case ONEWAY:
        this.remotingClient.invokeOneway(addr, request, timeoutMillis);
        return null;
      case ASYNC:
        this.pullAsync(addr, request, timeoutMillis, pullCallback);
        return null;
      case SYNC:
        return this.pullSync(addr, request, timeoutMillis);
      default:
        assert false;
        break;
    }
    return null;
  }

  private PullResult pullSync( //
      final String addr, //
      final RemotingCommand request, //
      final long timeoutMillis //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
    assert response != null;
    return this.processPullResponse(response);
  }

  private void pullAsync( //
      final String addr, //
      final RemotingCommand request, //
      final long timeoutMillis, //
      final PullCallback pullCallback //
      ) throws RemotingException, InterruptedException {
    this.remotingClient.invokeAsync(
        addr,
        request,
        timeoutMillis,
        new InvokeCallback() {

          @Override
          public void operationComplete(ResponseFuture responseFuture) {
            PullResult pullResult = null;
            try {
              pullResult =
                  MyberryClientAPIImpl.this.processPullResponse(
                      responseFuture.getResponseCommand());
            } catch (Throwable e) {
              pullCallback.onException(e);
            }
            pullCallback.onSuccess(pullResult);
          }
        });
  }

  private PullResult processPullResponse(final RemotingCommand response)
      throws MyberryServerException, RemotingCommandException {
    PullStatus pullStatus = PullStatus.KEY_NOT_EXISTED;
    switch (response.getCode()) {
      case ResponseCode.SUCCESS:
        pullStatus = PullStatus.PULL_OK;
        break;
      case ResponseCode.KEY_NOT_EXISTED:
        pullStatus = PullStatus.KEY_NOT_EXISTED;
        break;
      default:
        throw new MyberryServerException(response.getCode(), response.getRemark());
    }

    PullIdBackResponseHeader responseHeader =
        (PullIdBackResponseHeader)
            response.decodeCommandCustomHeader(PullIdBackResponseHeader.class);

    switch (pullStatus) {
      case PULL_OK:
        if (Structure.CR == responseHeader.getStructure()) {
          CRPullResultData crPullResultData =
              LightCodec.toObj(response.getBody(), CRPullResultData.class);
          return new PullResult(pullStatus, responseHeader.getKey(), crPullResultData.getNewId());
        } else if (Structure.NS == responseHeader.getStructure()) {
          NSPullResultData nsPullResultData =
              LightCodec.toObj(response.getBody(), NSPullResultData.class);
          return new PullResult(
              pullStatus,
              responseHeader.getKey(),
              nsPullResultData.getStart(),
              nsPullResultData.getEnd(),
              nsPullResultData.getSynergyId());
        }
      default:
        return new PullResult(pullStatus, responseHeader.getKey());
    }
  }

  public SendResult createComponent( //
      final int code, //
      final String addr, //
      byte[] componentData, //
      final CommandCustomHeader requestHeader, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    return this.createComponent(
        code, addr, componentData, requestHeader, timeoutMillis, communicationMode, null);
  }

  public SendResult createComponent( //
      final int code, //
      final String addr, //
      byte[] componentData, //
      final CommandCustomHeader requestHeader, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode, //
      final SendCallback sendCallback //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    RemotingCommand request = RemotingCommand.createRequestCommand(code, requestHeader);
    request.setBody(componentData);
    return this.sendKernelImpl(code, addr, request, timeoutMillis, communicationMode, sendCallback);
  }

  public SendResult queryComponentSize( //
      final int code, //
      final String addr, //
      final CommandCustomHeader requstHeader, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    return this.queryComponentSize(
        code, addr, requstHeader, timeoutMillis, communicationMode, null);
  }

  public SendResult queryComponentSize( //
      final int code, //
      final String addr, //
      final CommandCustomHeader requstHeader, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode, //
      final SendCallback sendCallback //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    RemotingCommand request = RemotingCommand.createRequestCommand(code, requstHeader);
    return this.sendKernelImpl(code, addr, request, timeoutMillis, communicationMode, sendCallback);
  }

  public SendResult queryComponentByKey( //
      final int code, //
      final String addr, //
      byte[] key, //
      final CommandCustomHeader requstHeader, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    return this.queryComponentByKey(
        code, addr, key, requstHeader, timeoutMillis, communicationMode, null);
  }

  public SendResult queryComponentByKey( //
      final int code, //
      final String addr, //
      byte[] key, //
      final CommandCustomHeader requstHeader, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode, //
      final SendCallback sendCallback //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    RemotingCommand request = RemotingCommand.createRequestCommand(code, requstHeader);
    request.setBody(key);
    return this.sendKernelImpl(code, addr, request, timeoutMillis, communicationMode, sendCallback);
  }

  public SendResult queryClusterList( //
      final int code, //
      final String addr, //
      final CommandCustomHeader requstHeader, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    return this.queryComponentSize(
        code, addr, requstHeader, timeoutMillis, communicationMode, null);
  }

  public SendResult queryClusterList( //
      final int code, //
      final String addr, //
      final CommandCustomHeader requstHeader, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode, //
      final SendCallback sendCallback //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    RemotingCommand request = RemotingCommand.createRequestCommand(code, requstHeader);
    return this.sendKernelImpl(code, addr, request, timeoutMillis, communicationMode, sendCallback);
  }

  public SendResult kickOutInvoker(
      final int code, //
      final String addr, //
      byte[] routeData, //
      final CommandCustomHeader requestHeader, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    return this.kickOutInvoker(
        code, addr, routeData, requestHeader, timeoutMillis, communicationMode, null);
  }

  public SendResult kickOutInvoker(
      final int code, //
      final String addr, //
      byte[] routeData, //
      final CommandCustomHeader requestHeader, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode, //
      final SendCallback sendCallback //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    RemotingCommand request = RemotingCommand.createRequestCommand(code, requestHeader);
    request.setBody(routeData);
    return this.sendKernelImpl(code, addr, request, timeoutMillis, communicationMode, sendCallback);
  }

  public SendResult removeInvoker(
      final int code, //
      final String addr, //
      byte[] routeData, //
      final CommandCustomHeader requestHeader, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    return this.removeInvoker(
        code, addr, routeData, requestHeader, timeoutMillis, communicationMode, null);
  }

  public SendResult removeInvoker(
      final int code, //
      final String addr, //
      byte[] routeData, //
      final CommandCustomHeader requestHeader, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode, //
      final SendCallback sendCallback //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    RemotingCommand request = RemotingCommand.createRequestCommand(code, requestHeader);
    request.setBody(routeData);
    return this.sendKernelImpl(code, addr, request, timeoutMillis, communicationMode, sendCallback);
  }

  public SendResult updateWeight(
      final int code, //
      final String addr, //
      byte[] routeData, //
      final CommandCustomHeader requestHeader, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    return this.kickOutInvoker(
        code, addr, routeData, requestHeader, timeoutMillis, communicationMode, null);
  }

  public SendResult updateWeight(
      final int code, //
      final String addr, //
      byte[] routeData, //
      final CommandCustomHeader requestHeader, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode, //
      final SendCallback sendCallback //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    RemotingCommand request = RemotingCommand.createRequestCommand(code, requestHeader);
    request.setBody(routeData);
    return this.sendKernelImpl(code, addr, request, timeoutMillis, communicationMode, sendCallback);
  }

  private SendResult sendKernelImpl( //
      final int code, //
      final String addr, //
      final RemotingCommand request, //
      final long timeoutMillis, //
      final CommunicationMode communicationMode, //
      final SendCallback sendCallback //
      ) throws RemotingException, InterruptedException, MyberryServerException {
    switch (communicationMode) {
      case ONEWAY:
        this.remotingClient.invokeOneway(addr, request, timeoutMillis);
        return null;
      case ASYNC:
        this.sendKernelAsync(code, addr, request, timeoutMillis, sendCallback);
        return null;
      case SYNC:
        return this.sendKernelSync(code, addr, request, timeoutMillis);
      default:
        assert false;
        break;
    }
    return null;
  }

  private SendResult sendKernelSync( //
      final int code, //
      final String addr, //
      final RemotingCommand request, //
      final long timeoutMillis //
      ) throws RemotingException, MyberryServerException, InterruptedException {
    RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
    assert response != null;
    return this.processSendResponse(code, response);
  }

  private void sendKernelAsync( //
      final int code, //
      final String addr, //
      final RemotingCommand request, //
      final long timeoutMillis, //
      final SendCallback sendCallback //
      ) throws RemotingException, InterruptedException {
    this.remotingClient.invokeAsync(
        addr,
        request,
        timeoutMillis,
        new InvokeCallback() {

          @Override
          public void operationComplete(ResponseFuture responseFuture) {
            SendResult pullResult = null;
            try {
              pullResult =
                  MyberryClientAPIImpl.this.processSendResponse(
                      code, responseFuture.getResponseCommand());
            } catch (Throwable e) {
              sendCallback.onException(e);
            }
            sendCallback.onSuccess(pullResult);
          }
        });
  }

  private SendResult processSendResponse(final int code, final RemotingCommand response)
      throws RemotingCommandException, MyberryServerException {
    switch (response.getCode()) {
      case ResponseCode.KEY_EXISTED:
      case ResponseCode.KEY_NOT_EXISTED:
      case ResponseCode.PASSWORD_ERROR:
      case ResponseCode.UNKNOWN_STRUCTURE:
      case ResponseCode.PARAMETER_LENGTH_TOO_LONG:
      case ResponseCode.INVALID_EXPRESSION:
        {
        }
      case ResponseCode.SUCCESS:
        {
          SendStatus sendStatus = SendStatus.SEND_OK;
          switch (response.getCode()) {
            case ResponseCode.KEY_EXISTED:
              sendStatus = SendStatus.KEY_EXISTED;
              break;
            case ResponseCode.KEY_NOT_EXISTED:
              sendStatus = SendStatus.KEY_NOT_EXISTED;
              break;
            case ResponseCode.PASSWORD_ERROR:
              sendStatus = SendStatus.PASSWORD_ERROR;
              break;
            case ResponseCode.UNKNOWN_STRUCTURE:
              sendStatus = SendStatus.UNKNOWN_STRUCTURE;
              break;
            case ResponseCode.PARAMETER_LENGTH_TOO_LONG:
              sendStatus = SendStatus.PARAMETER_LENGTH_TOO_LONG;
              break;
            case ResponseCode.INVALID_EXPRESSION:
              sendStatus = SendStatus.INVALID_EXPRESSION;
              break;
            case ResponseCode.SUCCESS:
              sendStatus = SendStatus.SEND_OK;
              break;
            default:
              assert false;
              break;
          }

          byte[] body = response.getBody();
          if (body == null) {
            body = new byte[0];
          }

          SendResult sendResult = new SendResult(sendStatus);
          switch (code) {
            case RequestCode.CREATE_COMPONENT:
              ManageComponentResponseHeader createResponseHeader =
                  (ManageComponentResponseHeader)
                      response.decodeCommandCustomHeader(ManageComponentResponseHeader.class);
              return new SendResult(sendStatus, createResponseHeader.getKey());
            case RequestCode.QUERY_COMPONENT_SIZE:
              ComponentSizeData componentSizeData = LightCodec.toObj(body, ComponentSizeData.class);
              sendResult.setSize(componentSizeData.getSize());
              return sendResult;
            case RequestCode.QUERY_COMPONENT_BY_KEY:
              ManageComponentResponseHeader queryResponseHeader =
                  (ManageComponentResponseHeader)
                      response.decodeCommandCustomHeader(ManageComponentResponseHeader.class);

              sendResult.setKey(queryResponseHeader.getKey());
              if (Structure.CR == queryResponseHeader.getStructure()) {
                sendResult.setComponent(LightCodec.toObj(body, CRComponentData.class));
                return sendResult;
              } else if (Structure.NS == queryResponseHeader.getStructure()) {
                sendResult.setComponent(LightCodec.toObj(body, NSComponentData.class));
                return sendResult;
              } else {
                return new SendResult(sendStatus);
              }
            case RequestCode.QUERY_CLUSTER_LIST:
              ClusterListData clusterListData = LightCodec.toObj(body, ClusterListData.class);
              sendResult.setClusterList(clusterListData);
              return sendResult;
            case RequestCode.KICK_OUT_INVOKER:
            case RequestCode.REMOVE_INVOKER:
            case RequestCode.UPDATE_WEIGHT:
              sendResult.setRoute(LightCodec.toObj(body, RouteData.class));
              return sendResult;
            default:
              assert false;
              break;
          }
        }
      default:
        break;
    }

    throw new MyberryServerException(response.getCode(), response.getRemark());
  }

  public HeartbeatResult sendHearbeat( //
      final String addr, //
      final CommandCustomHeader requstHeader, //
      final long timeoutMillis //
      )
      throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException,
          InterruptedException, MyberryServerException {
    RemotingCommand request =
        RemotingCommand.createRequestCommand(RequestCode.HEART_BEAT, requstHeader);

    RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
    assert response != null;
    switch (response.getCode()) {
      case ResponseCode.SUCCESS:
        {
          HeartbeatData heartbeatData = LightCodec.toObj(response.getBody(), HeartbeatData.class);
          RouterInfo routerInfo = new RouterInfo();
          routerInfo.setMaintainer(heartbeatData.getMaintainer());
          routerInfo.setInvokers(heartbeatData.getInvokers());

          return new HeartbeatResult(routerInfo);
        }
      default:
        break;
    }
    throw new MyberryServerException(response.getCode(), response.getRemark());
  }
}
