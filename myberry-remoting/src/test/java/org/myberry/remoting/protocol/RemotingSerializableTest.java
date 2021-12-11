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

import java.nio.ByteBuffer;
import org.junit.Assert;
import org.junit.Test;
import org.myberry.remoting.SampleCommandCustomHeader;

public class RemotingSerializableTest {

  @Test
  public void testEncode2Decode() throws Exception {
    // case
    int code = 111;

    int id = 15;
    String name = "John";
    boolean flag = true;
    SampleCommandCustomHeader header = new SampleCommandCustomHeader();
    header.setId(id);
    header.setName(name);
    header.setFlag(flag);

    RemotingCommand requestCommand = RemotingCommand.createRequestCommand(code, header);
    ByteBuffer byteBuffer = requestCommand.encode();

    // offset 4 due to LengthFieldBasedFrameDecoder
    byte[] bytes = new byte[byteBuffer.capacity() - 4];
    for (int i = 0; i < byteBuffer.capacity() - 4; i++) {
      bytes[i] = byteBuffer.get(4 + i);
    }

    RemotingCommand command = RemotingCommand.decode(ByteBuffer.wrap(bytes));
    Assert.assertEquals(code, command.getCode());

    SampleCommandCustomHeader commandHeader =
        (SampleCommandCustomHeader)
            command.decodeCommandCustomHeader(SampleCommandCustomHeader.class);
    Assert.assertEquals(id, commandHeader.getId());
    Assert.assertEquals(name, commandHeader.getName());
    Assert.assertEquals(flag, commandHeader.isFlag());
  }
}
