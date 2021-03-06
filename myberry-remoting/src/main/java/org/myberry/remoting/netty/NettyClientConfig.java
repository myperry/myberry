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

public class NettyClientConfig {

  /** Worker thread number */
  private int clientWorkerThreads = 4;

  private int connectTimeoutMillis = 3000;
  private int clientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();
  private int clientOnewaySemaphoreValue = NettySystemConfig.CLIENT_ONEWAY_SEMAPHORE_VALUE;
  private int clientAsyncSemaphoreValue = NettySystemConfig.CLIENT_ASYNC_SEMAPHORE_VALUE;

  private int clientChannelMaxIdleTimeSeconds = 120;

  private int clientSocketSndBufSize = NettySystemConfig.socketSndbufSize;
  private int clientSocketRcvBufSize = NettySystemConfig.socketRcvbufSize;

  private boolean clientCloseSocketIfTimeout = false;

  public boolean isClientCloseSocketIfTimeout() {
    return clientCloseSocketIfTimeout;
  }

  public void setClientCloseSocketIfTimeout(final boolean clientCloseSocketIfTimeout) {
    this.clientCloseSocketIfTimeout = clientCloseSocketIfTimeout;
  }

  public int getClientWorkerThreads() {
    return clientWorkerThreads;
  }

  public void setClientWorkerThreads(int clientWorkerThreads) {
    this.clientWorkerThreads = clientWorkerThreads;
  }

  public int getConnectTimeoutMillis() {
    return connectTimeoutMillis;
  }

  public void setConnectTimeoutMillis(int connectTimeoutMillis) {
    this.connectTimeoutMillis = connectTimeoutMillis;
  }

  public int getClientCallbackExecutorThreads() {
    return clientCallbackExecutorThreads;
  }

  public void setClientCallbackExecutorThreads(int clientCallbackExecutorThreads) {
    this.clientCallbackExecutorThreads = clientCallbackExecutorThreads;
  }

  public int getClientOnewaySemaphoreValue() {
    return clientOnewaySemaphoreValue;
  }

  public void setClientOnewaySemaphoreValue(int clientOnewaySemaphoreValue) {
    this.clientOnewaySemaphoreValue = clientOnewaySemaphoreValue;
  }

  public int getClientAsyncSemaphoreValue() {
    return clientAsyncSemaphoreValue;
  }

  public void setClientAsyncSemaphoreValue(int clientAsyncSemaphoreValue) {
    this.clientAsyncSemaphoreValue = clientAsyncSemaphoreValue;
  }

  public int getClientChannelMaxIdleTimeSeconds() {
    return clientChannelMaxIdleTimeSeconds;
  }

  public void setClientChannelMaxIdleTimeSeconds(int clientChannelMaxIdleTimeSeconds) {
    this.clientChannelMaxIdleTimeSeconds = clientChannelMaxIdleTimeSeconds;
  }

  public int getClientSocketSndBufSize() {
    return clientSocketSndBufSize;
  }

  public void setClientSocketSndBufSize(int clientSocketSndBufSize) {
    this.clientSocketSndBufSize = clientSocketSndBufSize;
  }

  public int getClientSocketRcvBufSize() {
    return clientSocketRcvBufSize;
  }

  public void setClientSocketRcvBufSize(int clientSocketRcvBufSize) {
    this.clientSocketRcvBufSize = clientSocketRcvBufSize;
  }
}
