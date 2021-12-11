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
package org.myberry.server.processor;

import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.List;
import org.myberry.common.codec.LightCodec;
import org.myberry.common.constant.LoggerName;
import org.myberry.common.loadbalance.Invoker;
import org.myberry.common.protocol.RequestCode;
import org.myberry.common.protocol.ResponseCode;
import org.myberry.common.protocol.body.HeartbeatData;
import org.myberry.common.protocol.header.HeartbeatRequestHeader;
import org.myberry.common.protocol.header.HeartbeatResponseHeader;
import org.myberry.remoting.common.RemotingHelper;
import org.myberry.remoting.netty.NettyRequestProcessor;
import org.myberry.remoting.protocol.RemotingCommand;
import org.myberry.server.ServerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientManageProcessor implements NettyRequestProcessor {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.SERVER_LOGGER_NAME);

  private final ServerController serverController;

  public ClientManageProcessor(final ServerController serverController) {
    this.serverController = serverController;
  }

  @Override
  public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
      throws Exception {
    HeartbeatRequestHeader requestHeader =
        (HeartbeatRequestHeader) request.decodeCommandCustomHeader(HeartbeatRequestHeader.class);
    log.debug("receive [{}] heartbeat.", requestHeader.getClientId());

    RemotingCommand response = RemotingCommand.createResponseCommand(HeartbeatResponseHeader.class);
    switch (request.getCode()) {
      case RequestCode.HEART_BEAT:
        return this.heartBeat(response);
      default:
        break;
    }
    return response;
  }

  public RemotingCommand heartBeat(RemotingCommand response) {
    HeartbeatData heartbeatData = new HeartbeatData();

    if (serverController.getServerConfig().getHaServerAddr() != null
        && !"".equals(serverController.getServerConfig().getHaServerAddr().trim())) {
      heartbeatData.setMaintainer(
          serverController.getHaService().getCollectService().getLeaderAddr());
      heartbeatData.setInvokers(
          serverController.getHaService().getCollectService().getLearnerList());
    } else {
      String phyLocalAddress = RemotingHelper.getPhyLocalAddress();
      int listenPort = serverController.getNettyServerConfig().getListenPort();
      String address = RemotingHelper.makeStringAddress(phyLocalAddress, listenPort);

      List<Invoker> invokers = new ArrayList<>();
      invokers.add(new Invoker(address, 1));

      heartbeatData.setMaintainer(address);
      heartbeatData.setInvokers(invokers);
    }

    response.setCode(ResponseCode.SUCCESS);
    response.setBody(LightCodec.toBytes(heartbeatData));
    response.setRemark(null);
    return response;
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
