/*
 * Copyright (c) 2021 MyBerry. All rights reserved.
 * https://myberry.org/
 *
 * Modified by Apache RocketMQ.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.myberry.remoting.netty;

import java.nio.ByteBuffer;

import org.myberry.remoting.common.LoggerName;
import org.myberry.remoting.common.RemotingHelper;
import org.myberry.remoting.common.RemotingUtil;
import org.myberry.remoting.protocol.RemotingCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyEncoder extends MessageToByteEncoder<RemotingCommand> {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.REMOTING_LOGGER_NAME);

  @Override
  protected void encode(ChannelHandlerContext ctx, RemotingCommand remotingCommand, ByteBuf out)
      throws Exception {
    try {
      ByteBuffer header = remotingCommand.encodeHeader();
      out.writeBytes(header);
      byte[] body = remotingCommand.getBody();
      if (body != null) {
        out.writeBytes(body);
      }
    } catch (Exception e) {
      log.error("encode exception, " + RemotingHelper.parseChannelRemoteAddr(ctx.channel()), e);
      if (remotingCommand != null) {
        log.error(remotingCommand.toString());
      }
      RemotingUtil.closeChannel(ctx.channel());
    }
  }
}
