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

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.myberry.remoting.InvokeCallback;
import org.myberry.remoting.RemotingServer;
import org.myberry.remoting.common.LoggerName;
import org.myberry.remoting.common.Pair;
import org.myberry.remoting.common.RemotingHelper;
import org.myberry.remoting.common.RemotingUtil;
import org.myberry.remoting.exception.RemotingSendRequestException;
import org.myberry.remoting.exception.RemotingTimeoutException;
import org.myberry.remoting.exception.RemotingTooMuchRequestException;
import org.myberry.remoting.protocol.RemotingCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

public class NettyRemotingServer extends NettyRemotingAbstract implements RemotingServer {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.REMOTING_LOGGER_NAME);
  private final ServerBootstrap serverBootstrap;
  private final EventLoopGroup eventLoopGroupSelector;
  private final EventLoopGroup eventLoopGroupBoss;
  private final NettyServerConfig nettyServerConfig;

  private final ExecutorService publicExecutor;

  private final Timer timer = new Timer("ServerHouseKeepingService", true);
  private DefaultEventExecutorGroup defaultEventExecutorGroup;

  private int port = 0;

  public NettyRemotingServer(final NettyServerConfig nettyServerConfig) {
    super(
        nettyServerConfig.getServerOnewaySemaphoreValue(),
        nettyServerConfig.getServerAsyncSemaphoreValue());
    this.serverBootstrap = new ServerBootstrap();
    this.nettyServerConfig = nettyServerConfig;

    int publicThreadNums = nettyServerConfig.getServerCallbackExecutorThreads();
    if (publicThreadNums <= 0) {
      publicThreadNums = 4;
    }

    this.publicExecutor =
        Executors.newFixedThreadPool(
            publicThreadNums,
            new ThreadFactory() {
              private AtomicInteger threadIndex = new AtomicInteger(0);

              @Override
              public Thread newThread(Runnable r) {
                return new Thread(
                    r, "NettyServerPublicExecutor_" + this.threadIndex.incrementAndGet());
              }
            });

    this.eventLoopGroupBoss =
        new NioEventLoopGroup(
            1,
            new ThreadFactory() {
              private AtomicInteger threadIndex = new AtomicInteger(0);

              @Override
              public Thread newThread(Runnable r) {
                return new Thread(
                    r, String.format("NettyBoss_%d", this.threadIndex.incrementAndGet()));
              }
            });

    if (useEpoll()) {
      this.eventLoopGroupSelector =
          new EpollEventLoopGroup(
              nettyServerConfig.getServerSelectorThreads(),
              new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);
                private int threadTotal = nettyServerConfig.getServerSelectorThreads();

                @Override
                public Thread newThread(Runnable r) {
                  return new Thread(
                      r,
                      String.format(
                          "NettyServerEPOLLSelector_%d_%d",
                          threadTotal, this.threadIndex.incrementAndGet()));
                }
              });
    } else {
      this.eventLoopGroupSelector =
          new NioEventLoopGroup(
              nettyServerConfig.getServerSelectorThreads(),
              new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);
                private int threadTotal = nettyServerConfig.getServerSelectorThreads();

                @Override
                public Thread newThread(Runnable r) {
                  return new Thread(
                      r,
                      String.format(
                          "NettyServerNIOSelector_%d_%d",
                          threadTotal, this.threadIndex.incrementAndGet()));
                }
              });
    }
  }

  private boolean useEpoll() {
    return RemotingUtil.isLinuxPlatform()
        && nettyServerConfig.isUseEpollNativeSelector()
        && Epoll.isAvailable();
  }

  @Override
  public void start() {
    this.defaultEventExecutorGroup =
        new DefaultEventExecutorGroup(
            nettyServerConfig.getServerWorkerThreads(),
            new ThreadFactory() {

              private AtomicInteger threadIndex = new AtomicInteger(0);

              @Override
              public Thread newThread(Runnable r) {
                return new Thread(
                    r, "NettyServerCodecThread_" + this.threadIndex.incrementAndGet());
              }
            });

    ServerBootstrap childHandler = //
        this.serverBootstrap
            .group(this.eventLoopGroupBoss, this.eventLoopGroupSelector) //
            .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class) //
            .option(ChannelOption.SO_BACKLOG, 1024) //
            .option(ChannelOption.SO_REUSEADDR, true) //
            .option(ChannelOption.SO_KEEPALIVE, false) //
            .childOption(ChannelOption.TCP_NODELAY, true) //
            .childOption(ChannelOption.SO_SNDBUF, nettyServerConfig.getServerSocketSndBufSize()) //
            .childOption(ChannelOption.SO_RCVBUF, nettyServerConfig.getServerSocketRcvBufSize()) //
            .localAddress(new InetSocketAddress(this.nettyServerConfig.getListenPort())) //
            .childHandler(
                new ChannelInitializer<SocketChannel>() {
                  @Override
                  public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast( //
                            defaultEventExecutorGroup, //
                            new NettyEncoder(), //
                            new NettyDecoder(), //
                            new IdleStateHandler(
                                0, 0, nettyServerConfig.getServerChannelMaxIdleTimeSeconds()), //
                            new NettyConnectManageHandler(), //
                            new NettyServerHandler() //
                            );
                  }
                });

    if (nettyServerConfig.isServerPooledByteBufAllocatorEnable()) {
      childHandler.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }

    try {
      ChannelFuture sync = this.serverBootstrap.bind().sync();
      InetSocketAddress addr = (InetSocketAddress) sync.channel().localAddress();
      this.port = addr.getPort();
    } catch (InterruptedException e1) {
      throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e1);
    }

    this.timer.scheduleAtFixedRate(
        new TimerTask() {

          @Override
          public void run() {
            try {
              NettyRemotingServer.this.scanResponseTable();
            } catch (Exception e) {
              log.error("scanResponseTable exception", e);
            }
          }
        },
        1000 * 3,
        1000);
  }

  class NettyServerHandler extends SimpleChannelInboundHandler<RemotingCommand> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {
      processMessageReceived(ctx, msg);
    }
  }

  class NettyConnectManageHandler extends ChannelDuplexHandler {
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
      final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
      log.debug("NETTY SERVER PIPELINE: channelRegistered {}", remoteAddress);
      super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
      final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
      log.debug("NETTY SERVER PIPELINE: channelUnregistered, the channel[{}]", remoteAddress);
      super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
      log.debug("NETTY SERVER PIPELINE: channelActive, the channel[{}]", remoteAddress);
      super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
      log.debug("NETTY SERVER PIPELINE: channelInactive, the channel[{}]", remoteAddress);
      super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      if (evt instanceof IdleStateEvent) {
        IdleStateEvent event = (IdleStateEvent) evt;
        if (event.state().equals(IdleState.ALL_IDLE)) {
          final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
          log.warn("NETTY SERVER PIPELINE: IDLE exception [{}]", remoteAddress);
          RemotingUtil.closeChannel(ctx.channel());
        }
      }

      ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
      log.warn("NETTY SERVER PIPELINE: exceptionCaught {}", remoteAddress);
      log.debug("NETTY SERVER PIPELINE: exceptionCaught exception.", cause);

      RemotingUtil.closeChannel(ctx.channel());
    }
  }

  @Override
  public void shutdown() {
    try {
      if (this.timer != null) {
        this.timer.cancel();
      }

      this.eventLoopGroupBoss.shutdownGracefully();

      this.eventLoopGroupSelector.shutdownGracefully();

      if (this.defaultEventExecutorGroup != null) {
        this.defaultEventExecutorGroup.shutdownGracefully();
      }
    } catch (Exception e) {
      log.error("NettyRemotingServer shutdown exception, ", e);
    }

    if (this.publicExecutor != null) {
      try {
        this.publicExecutor.shutdown();
      } catch (Exception e) {
        log.error("NettyRemotingServer shutdown exception, ", e);
      }
    }
  }

  @Override
  public int localListenPort() {
    return this.port;
  }

  @Override
  public Pair<NettyRequestProcessor, ExecutorService> getProcessorPair(int requestCode) {
    return processorTable.get(requestCode);
  }

  @Override
  public RemotingCommand invokeSync(
      final Channel channel, final RemotingCommand request, final long timeoutMillis)
      throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException {
    return this.invokeSyncImpl(channel, request, timeoutMillis);
  }

  @Override
  public void invokeAsync(
      Channel channel, RemotingCommand request, long timeoutMillis, InvokeCallback invokeCallback)
      throws InterruptedException, RemotingTooMuchRequestException, RemotingTimeoutException,
          RemotingSendRequestException {
    this.invokeAsyncImpl(channel, request, timeoutMillis, invokeCallback);
  }

  @Override
  public void invokeOneway(Channel channel, RemotingCommand request, long timeoutMillis)
      throws InterruptedException, RemotingTooMuchRequestException, RemotingTimeoutException,
          RemotingSendRequestException {
    this.invokeOnewayImpl(channel, request, timeoutMillis);
  }

  @Override
  public void registerProcessor(
      int requestCode, NettyRequestProcessor processor, ExecutorService executor) {
    ExecutorService executorThis = executor;
    if (null == executor) {
      executorThis = this.publicExecutor;
    }

    Pair<NettyRequestProcessor, ExecutorService> pair =
        new Pair<NettyRequestProcessor, ExecutorService>(processor, executorThis);
    this.processorTable.put(requestCode, pair);
  }

  @Override
  public void registerDefaultProcessor(NettyRequestProcessor processor, ExecutorService executor) {
    this.defaultRequestProcessor =
        new Pair<NettyRequestProcessor, ExecutorService>(processor, executor);
  }

  @Override
  public ExecutorService getCallbackExecutor() {
    return this.publicExecutor;
  }
}
