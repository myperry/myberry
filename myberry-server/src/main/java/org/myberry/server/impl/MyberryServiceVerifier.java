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
package org.myberry.server.impl;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.myberry.common.codec.LightCodec;
import org.myberry.common.expression.exception.ParseExpressionException;
import org.myberry.common.expression.impl.ExpressionString;
import org.myberry.common.expression.impl.ParserManager;
import org.myberry.common.protocol.ResponseCode;
import org.myberry.common.protocol.body.admin.CRComponentData;
import org.myberry.common.protocol.body.admin.NSComponentData;
import org.myberry.common.protocol.body.user.CRPullResultData;
import org.myberry.common.protocol.body.user.NSPullResultData;
import org.myberry.common.security.Verifier;
import org.myberry.common.structure.Structure;
import org.myberry.remoting.common.RemotingHelper;
import org.myberry.remoting.protocol.RemotingSysResponseCode;
import org.myberry.server.converter.ConverterService;
import org.myberry.server.ha.HANotifier;
import org.myberry.store.CRComponent;
import org.myberry.store.MyberryStore;
import org.myberry.store.NSComponent;

public class MyberryServiceVerifier {

  private final MyberryStore myberryStore;
  private final MyberryServiceImpl myberryServiceImpl;
  private final ParserManager parserManager;

  public MyberryServiceVerifier(
      final MyberryStore myberryStore, final ConverterService converterService) {
    this.myberryStore = myberryStore;
    this.myberryServiceImpl = new MyberryServiceImpl(myberryStore, converterService);
    this.parserManager = ParserManager.getInstance();
  }

  public void start() {
    parserManager.registerParser();
  }

  public void shutdown() {
    parserManager.unRegisterParser();
  }

  public DefaultResponse getNewId(String key, Map<String, String> attachments) {
    try {
      CRPullResultData crd = myberryServiceImpl.getNewId(key, attachments);
      DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.SUCCESS, Structure.CR);
      defaultResponse.setExt(key.getBytes(StandardCharsets.UTF_8));
      defaultResponse.setBody(LightCodec.toBytes(crd));
      return defaultResponse;
    } catch (Exception e) {
      DefaultResponse defaultResponse =
          new DefaultResponse(
              RemotingSysResponseCode.SYSTEM_ERROR, RemotingHelper.exceptionSimpleDesc(e));
      defaultResponse.setExt(key.getBytes(StandardCharsets.UTF_8));
      return defaultResponse;
    }
  }

  public DefaultResponse getNewId(String key) {
    try {
      NSPullResultData nsd = myberryServiceImpl.getNewId(key);
      DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.SUCCESS, Structure.NS);
      defaultResponse.setExt(key.getBytes(StandardCharsets.UTF_8));
      defaultResponse.setBody(LightCodec.toBytes(nsd));
      return defaultResponse;
    } catch (Exception e) {
      DefaultResponse defaultResponse =
          new DefaultResponse(
              RemotingSysResponseCode.SYSTEM_ERROR, RemotingHelper.exceptionSimpleDesc(e));
      defaultResponse.setExt(key.getBytes(StandardCharsets.UTF_8));
      return defaultResponse;
    }
  }

  public DefaultResponse addComponent(CRComponentData crcd) {
    if (myberryStore.getComponentMap().containsKey(crcd.getKey())) {
      DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.KEY_EXISTED, Structure.CR);
      defaultResponse.setExt(crcd.getKey().getBytes(StandardCharsets.UTF_8));
      return defaultResponse;
    } else {
      try {
        if (Verifier.crCheck(crcd.getKey(), crcd.getExpression())
            && parserManager.parseExpression(new ExpressionString(crcd.getExpression().trim()))) {
          myberryServiceImpl.addComponent(crcd);
          DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.SUCCESS, Structure.CR);
          defaultResponse.setExt(crcd.getKey().getBytes(StandardCharsets.UTF_8));
          return defaultResponse;
        } else {
          DefaultResponse defaultResponse =
              new DefaultResponse(ResponseCode.PARAMETER_LENGTH_TOO_LONG, Structure.CR);
          defaultResponse.setExt(crcd.getKey().getBytes(StandardCharsets.UTF_8));
          return defaultResponse;
        }
      } catch (ParseExpressionException e) {
        DefaultResponse defaultResponse =
            new DefaultResponse(ResponseCode.INVALID_EXPRESSION, Structure.CR);
        defaultResponse.setExt(crcd.getKey().getBytes(StandardCharsets.UTF_8));
        return defaultResponse;
      } catch (Exception e) {
        return new DefaultResponse(
            RemotingSysResponseCode.SYSTEM_ERROR, RemotingHelper.exceptionSimpleDesc(e));
      }
    }
  }

  public DefaultResponse addComponent(NSComponentData nscd) {
    if (myberryStore.getComponentMap().containsKey(nscd.getKey())) {
      DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.KEY_EXISTED, Structure.NS);
      defaultResponse.setExt(nscd.getKey().getBytes(StandardCharsets.UTF_8));
      return defaultResponse;
    } else {
      try {
        if (Verifier.nsCheck(nscd.getKey())) {
          myberryServiceImpl.addComponent(nscd);
          DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.SUCCESS, Structure.NS);
          defaultResponse.setExt(nscd.getKey().getBytes(StandardCharsets.UTF_8));
          return defaultResponse;
        } else {
          DefaultResponse defaultResponse =
              new DefaultResponse(ResponseCode.PARAMETER_LENGTH_TOO_LONG, Structure.NS);
          defaultResponse.setExt(nscd.getKey().getBytes(StandardCharsets.UTF_8));
          return defaultResponse;
        }
      } catch (Exception e) {
        return new DefaultResponse(
            RemotingSysResponseCode.SYSTEM_ERROR, RemotingHelper.exceptionSimpleDesc(e));
      }
    }
  }

  public DefaultResponse modifyComponent(NSComponentData nscd) {
    if (!myberryStore.getComponentMap().containsKey(nscd.getKey())) {
      DefaultResponse defaultResponse =
          new DefaultResponse(ResponseCode.KEY_NOT_EXISTED, Structure.NS);
      defaultResponse.setExt(nscd.getKey().getBytes(StandardCharsets.UTF_8));
      return defaultResponse;
    } else {
      try {
        myberryServiceImpl.modifyComponent(nscd);
        DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.SUCCESS, Structure.NS);
        defaultResponse.setExt(nscd.getKey().getBytes(StandardCharsets.UTF_8));
        return defaultResponse;
      } catch (Exception e) {
        return new DefaultResponse(
            RemotingSysResponseCode.SYSTEM_ERROR, RemotingHelper.exceptionSimpleDesc(e));
      }
    }
  }

  public DefaultResponse queryComponentSize() {
    DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.SUCCESS);
    defaultResponse.setBody(LightCodec.toBytes(myberryServiceImpl.queryComponentSize()));
    return defaultResponse;
  }

  public DefaultResponse queryComponentByKey(CRComponent crComponent) {
    CRComponentData crComponentData = myberryServiceImpl.queryComponentByKey(crComponent);
    DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.SUCCESS);
    defaultResponse.setStructure(Structure.CR);
    defaultResponse.setExt(crComponent.getKey().getBytes(StandardCharsets.UTF_8));
    defaultResponse.setBody(LightCodec.toBytes(crComponentData));
    return defaultResponse;
  }

  public DefaultResponse queryComponentByKey(NSComponent nsComponent) {
    NSComponentData nsComponentData = myberryServiceImpl.queryComponentByKey(nsComponent);
    DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.SUCCESS);
    defaultResponse.setStructure(Structure.NS);
    defaultResponse.setExt(nsComponent.getKey().getBytes(StandardCharsets.UTF_8));
    defaultResponse.setBody(LightCodec.toBytes(nsComponentData));
    return defaultResponse;
  }

  public HANotifier getHaNotifier() {
    return myberryServiceImpl.getHaNotifier();
  }

  public void setHaNotifier(HANotifier haNotifier) {
    this.myberryServiceImpl.setHaNotifier(haNotifier);
  }
}
