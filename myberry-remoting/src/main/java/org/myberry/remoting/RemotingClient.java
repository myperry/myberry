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
package org.myberry.remoting;

import java.util.concurrent.ExecutorService;
import org.myberry.remoting.exception.RemotingConnectException;
import org.myberry.remoting.exception.RemotingSendRequestException;
import org.myberry.remoting.exception.RemotingTimeoutException;
import org.myberry.remoting.exception.RemotingTooMuchRequestException;
import org.myberry.remoting.protocol.RemotingCommand;

public interface RemotingClient extends RemotingService {

  RemotingCommand invokeSync(
      final String addr, final RemotingCommand request, final long timeoutMillis)
      throws InterruptedException, RemotingConnectException, RemotingSendRequestException,
          RemotingTimeoutException;

  void invokeAsync(
      final String addr,
      final RemotingCommand request,
      final long timeoutMillis,
      final InvokeCallback invokeCallback)
      throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
          RemotingTimeoutException, RemotingSendRequestException;

  void invokeOneway(final String addr, final RemotingCommand request, final long timeoutMillis)
      throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
          RemotingTimeoutException, RemotingSendRequestException;

  void setCallbackExecutor(final ExecutorService callbackExecutor);

  ExecutorService getCallbackExecutor();

  boolean isChannelWriteable(final String addr);
}
