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
package org.myberry.remoting.protocol;

import static org.junit.Assert.*;

import org.junit.Test;
import org.myberry.remoting.CommandCustomHeader;
import org.myberry.remoting.SampleCommandCustomHeader;

public class RemotingCommandTest {

  @Test
  public void testCreateRequestCommand() {
    System.setProperty(RemotingCommand.REMOTING_VERSION_KEY, "4753");

    int code = 256;
    CommandCustomHeader header = new SampleCommandCustomHeader();
    RemotingCommand cmd = RemotingCommand.createRequestCommand(code, header);
    assertEquals(code, cmd.getCode());
    assertEquals(4753, cmd.getVersion());
    assertEquals(0, cmd.getFlag());
  }

  @Test
  public void testCreateResponseCommand() {
    System.setProperty(RemotingCommand.REMOTING_VERSION_KEY, "4753");

    int code = RemotingSysResponseCode.SUCCESS;
    String remark = "Sample remark";
    RemotingCommand cmd =
        RemotingCommand.createResponseCommand(code, remark, SampleCommandCustomHeader.class);
    assertEquals(code, cmd.getCode());
    assertEquals(4753, cmd.getVersion());
    assertEquals(remark, cmd.getRemark());
    assertEquals(1, cmd.getFlag());
  }
}
