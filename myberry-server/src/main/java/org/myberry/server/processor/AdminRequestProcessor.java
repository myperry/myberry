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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.myberry.common.codec.LightCodec;
import org.myberry.common.constant.LoggerName;
import org.myberry.common.protocol.RequestCode;
import org.myberry.common.protocol.ResponseCode;
import org.myberry.common.protocol.body.admin.ClusterListData;
import org.myberry.common.protocol.body.admin.ClusterListData.ClusterDatabase;
import org.myberry.common.protocol.body.admin.ClusterListData.ClusterDatabase.ClusterBlock;
import org.myberry.common.protocol.body.admin.ClusterListData.ClusterRoute;
import org.myberry.common.protocol.body.admin.RouteData;
import org.myberry.common.protocol.header.admin.ManageComponentRequestHeader;
import org.myberry.common.protocol.header.admin.ManageComponentResponseHeader;
import org.myberry.remoting.netty.NettyRequestProcessor;
import org.myberry.remoting.protocol.RemotingCommand;
import org.myberry.server.ServerController;
import org.myberry.server.ha.HAState;
import org.myberry.server.ha.collect.NodeAddr;
import org.myberry.server.ha.collect.NodeBlock;
import org.myberry.server.ha.collect.NodeBlock.BlockObject;
import org.myberry.server.impl.DefaultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminRequestProcessor implements NettyRequestProcessor {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.SERVER_LOGGER_NAME);
  private final ServerController serverController;

  public AdminRequestProcessor(final ServerController serverController) {
    this.serverController = serverController;
  }

  @Override
  public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
      throws Exception {
    ManageComponentRequestHeader requestHeader = //
        (ManageComponentRequestHeader)
            request.decodeCommandCustomHeader(ManageComponentRequestHeader.class);

    if (!this.serverController
        .getServerConfig()
        .getPassword()
        .equals(requestHeader.getPassword())) {
      RemotingCommand response = RemotingCommand.createResponseCommand(null);
      response.setCode(ResponseCode.PASSWORD_ERROR);
      response.setRemark(null);
      return response;
    }

    DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.REQUEST_CODE_NOT_SUPPORTED);
    switch (request.getCode()) {
      case RequestCode.CREATE_COMPONENT:
        if ((isCluster() && serverController.getHaService().getHaState() == HAState.LEADING)
            || !isCluster()) {
          defaultResponse =
              serverController
                  .getMyberryService()
                  .addComponent(requestHeader.getStructure(), request.getBody());
        }
        break;
      case RequestCode.UPDATE_COMPONENT:
        if ((isCluster() && serverController.getHaService().getHaState() == HAState.LEADING)
            || !isCluster()) {
          defaultResponse =
              serverController
                  .getMyberryService()
                  .modifyComponent(requestHeader.getStructure(), request.getBody());
        }
        break;
      case RequestCode.QUERY_COMPONENT_SIZE:
        defaultResponse = serverController.getMyberryService().queryComponentSize();
        break;
      case RequestCode.QUERY_COMPONENT_BY_KEY:
        defaultResponse =
            serverController.getMyberryService().queryComponentByKey(request.getBody());
        break;
      case RequestCode.QUERY_CLUSTER_LIST:
        if (isCluster() && serverController.getHaService().getHaState() == HAState.LEADING) {
          defaultResponse = queryClusterList();
        }
        break;
      case RequestCode.KICK_OUT_INVOKER:
        if (isCluster() && serverController.getHaService().getHaState() == HAState.LEADING) {
          defaultResponse =
              kickOutNode(LightCodec.toObj(request.getBody(), RouteData.class).getSid());
        }
        break;
      case RequestCode.REMOVE_INVOKER:
        if (isCluster() && serverController.getHaService().getHaState() == HAState.LEADING) {
          defaultResponse =
              removeNode(LightCodec.toObj(request.getBody(), RouteData.class).getSid());
        }
        break;
      case RequestCode.UPDATE_WEIGHT:
        if (isCluster() && serverController.getHaService().getHaState() == HAState.LEADING) {
          RouteData routeData = LightCodec.toObj(request.getBody(), RouteData.class);
          defaultResponse = updateWeight(routeData.getSid(), routeData.getWeight());
        }
        break;
      default:
        break;
    }

    RemotingCommand response =
        RemotingCommand.createResponseCommand(ManageComponentResponseHeader.class);
    ManageComponentResponseHeader responseHeader =
        (ManageComponentResponseHeader) response.readCustomHeader();
    responseHeader.setKey(new String(defaultResponse.getExt(), StandardCharsets.UTF_8));
    responseHeader.setStructure(defaultResponse.getStructure());

    response.setCode(defaultResponse.getRespCode());
    response.setBody(defaultResponse.getBody());
    response.setRemark(defaultResponse.getRemark());
    return response;
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }

  private boolean isCluster() {
    return serverController.getServerConfig().getHaServerAddr() != null
        && !"".equals(serverController.getServerConfig().getHaServerAddr().trim());
  }

  private DefaultResponse queryClusterList() {
    ClusterListData clusterListData = new ClusterListData();

    ArrayList<ClusterRoute> clusterRouteList = new ArrayList<>();

    Set<NodeAddr> routeList = serverController.getHaService().getCollectService().getRouteList();
    if (isCluster()) {
      for (NodeAddr nodeAddr : routeList) {
        ClusterRoute clusterRoute = new ClusterRoute();
        clusterRoute.setSid(nodeAddr.getSid());
        clusterRoute.setType(nodeAddr.getType());
        clusterRoute.setWeight(nodeAddr.getWeight());
        clusterRoute.setIp(nodeAddr.getIp());
        clusterRoute.setListenPort(nodeAddr.getListenPort());
        clusterRoute.setHaPort(nodeAddr.getHaPort());
        clusterRoute.setNodeState(nodeAddr.getNodeState());
        clusterRoute.setLastUpdateTimestamp(nodeAddr.getLastUpdateTimestamp());

        clusterRouteList.add(clusterRoute);
      }
    }

    List<NodeBlock> blockList = serverController.getHaService().getCollectService().getBlockList();

    List<ClusterDatabase> clusterDatabases = new ArrayList<>(blockList.size());
    if (isCluster()) {
      for (NodeBlock nodeBlock : blockList) {
        List<ClusterBlock> clusterBlocks = new ArrayList<>(nodeBlock.getBlockObjectList().size());

        List<BlockObject> blockObjectList = nodeBlock.getBlockObjectList();
        for (BlockObject blockObject : blockObjectList) {
          ClusterBlock clusterBlock = new ClusterBlock();
          clusterBlock.setBlockIndex(blockObject.getBlockIndex());
          clusterBlock.setComponentCount(blockObject.getComponentCount());
          clusterBlock.setBeginPhyOffset(blockObject.getBeginPhyOffset());
          clusterBlock.setEndPhyOffset(blockObject.getEndPhyOffset());
          clusterBlock.setBeginTimestamp(blockObject.getBeginTimestamp());
          clusterBlock.setEndTimestamp(blockObject.getEndTimestamp());

          clusterBlocks.add(clusterBlock);
        }

        ClusterDatabase clusterDatabase = new ClusterDatabase();
        clusterDatabase.setSid(nodeBlock.getSid());
        clusterDatabase.setBlockList(clusterBlocks);

        clusterDatabases.add(clusterDatabase);
      }
    }
    clusterListData.setClusterRouteList(clusterRouteList);
    clusterListData.setClusterDatabaseList(clusterDatabases);

    DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.SUCCESS);
    defaultResponse.setBody(LightCodec.toBytes(clusterListData));
    return defaultResponse;
  }

  private DefaultResponse kickOutNode(int kickedSid) {
    boolean needToPrint =
        serverController.getHaService().getCollectService().kickOutLearner(kickedSid);
    if (needToPrint) {
      serverController.getHaService().getCollectService().printAllRouting();
    }

    if (isCluster()) {
      serverController.getMyberryService().getHaNotifier().notifyCollect();
      serverController.getHaService().writeBack();
    }

    RouteData routeData = new RouteData();
    routeData.setSid(kickedSid);

    DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.SUCCESS);
    defaultResponse.setBody(LightCodec.toBytes(routeData));
    return defaultResponse;
  }

  private DefaultResponse removeNode(int kickedSid) {
    boolean needToPrint =
        serverController.getHaService().getCollectService().removeKickedLearner(kickedSid);
    if (needToPrint) {
      serverController.getHaService().getCollectService().printAllRouting();
    }

    if (isCluster()) {
      serverController.getMyberryService().getHaNotifier().notifyCollect();
    }

    RouteData routeData = new RouteData();
    routeData.setSid(kickedSid);

    DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.SUCCESS);
    defaultResponse.setBody(LightCodec.toBytes(routeData));
    return defaultResponse;
  }

  private DefaultResponse updateWeight(int sid, int weight) {
    boolean needToPrint =
        serverController.getHaService().getCollectService().updateWeight(sid, weight);
    if (needToPrint) {
      serverController.getHaService().getCollectService().printAllRouting();
    }

    if (isCluster()) {
      serverController.getMyberryService().getHaNotifier().notifyCollect();
    }

    if (serverController.getStoreConfig().getMySid() == sid) {
      serverController.getHaService().writeBack();
    }

    RouteData routeData = new RouteData();
    routeData.setSid(sid);
    routeData.setWeight(weight);

    DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.SUCCESS);
    defaultResponse.setBody(LightCodec.toBytes(routeData));
    return defaultResponse;
  }
}
