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
package org.myberry.remoting.common;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class RemotingUtil {
  public static final String OS_NAME = System.getProperty("os.name");

  private static final Logger log = LoggerFactory.getLogger(LoggerName.REMOTING_LOGGER_NAME);

  private static boolean isLinuxPlatform = false;
  private static boolean isWindowsPlatform = false;

  static {
    if (OS_NAME != null && OS_NAME.toLowerCase().contains("linux")) {
      isLinuxPlatform = true;
    }

    if (OS_NAME != null && OS_NAME.toLowerCase().contains("windows")) {
      isWindowsPlatform = true;
    }
  }

  public static boolean isWindowsPlatform() {
    return isWindowsPlatform;
  }

  public static Selector openSelector() throws IOException {
    Selector result = null;

    if (isLinuxPlatform()) {
      try {
        final Class<?> providerClazz = Class.forName("sun.nio.ch.EPollSelectorProvider");
        if (providerClazz != null) {
          try {
            final Method method = providerClazz.getMethod("provider");
            if (method != null) {
              final SelectorProvider selectorProvider = (SelectorProvider) method.invoke(null);
              if (selectorProvider != null) {
                result = selectorProvider.openSelector();
              }
            }
          } catch (final Exception e) {
            log.warn("Open ePoll Selector for linux platform exception", e);
          }
        }
      } catch (final Exception e) {
        // ignore
      }
    }

    if (result == null) {
      result = Selector.open();
    }

    return result;
  }

  public static boolean isLinuxPlatform() {
    return isLinuxPlatform;
  }

  public static SocketAddress string2SocketAddress(final String addr) {
    String[] s = addr.split(":");
    InetSocketAddress isa = new InetSocketAddress(s[0], Integer.parseInt(s[1]));
    return isa;
  }

  public static SocketChannel connect(SocketAddress remote) {
    return connect(remote, 1000 * 5);
  }

  public static SocketChannel connect(SocketAddress remote, final int timeoutMillis) {
    SocketChannel sc = null;
    try {
      sc = SocketChannel.open();
      sc.configureBlocking(true);
      sc.socket().setSoLinger(false, -1);
      sc.socket().setTcpNoDelay(true);
      sc.socket().setReceiveBufferSize(1024 * 64);
      sc.socket().setSendBufferSize(1024 * 64);
      sc.socket().connect(remote, timeoutMillis);
      sc.configureBlocking(false);
      return sc;
    } catch (Exception e) {
      if (sc != null) {
        try {
          sc.close();
        } catch (IOException e1) {
          log.error("connect error, ", e1);
        }
      }
    }

    return null;
  }

  public static void closeChannel(Channel channel) {
    final String addrRemote = RemotingHelper.parseChannelRemoteAddr(channel);
    channel
        .close()
        .addListener(
            new ChannelFutureListener() {
              @Override
              public void operationComplete(ChannelFuture future) throws Exception {
                log.info(
                    "closeChannel: close the connection to remote address[{}] result: {}",
                    addrRemote,
                    future.isSuccess());
              }
            });
  }
}
