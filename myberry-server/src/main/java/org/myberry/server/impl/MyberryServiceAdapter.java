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
import org.myberry.common.codec.LightCodec;
import org.myberry.common.codec.util.Maps;
import org.myberry.common.protocol.ResponseCode;
import org.myberry.common.protocol.body.admin.CRComponentData;
import org.myberry.common.protocol.body.admin.ComponentKeyData;
import org.myberry.common.protocol.body.admin.NSComponentData;
import org.myberry.common.structure.Structure;
import org.myberry.server.converter.ConverterService;
import org.myberry.server.ha.HANotifier;
import org.myberry.store.AbstractComponent;
import org.myberry.store.CRComponent;
import org.myberry.store.MyberryStore;
import org.myberry.store.NSComponent;

public class MyberryServiceAdapter {

  private final MyberryStore myberryStore;
  private final MyberryServiceVerifier myberryServiceVerifier;

  public MyberryServiceAdapter(
      final MyberryStore myberryStore, final ConverterService converterService) {
    this.myberryStore = myberryStore;
    this.myberryServiceVerifier = new MyberryServiceVerifier(myberryStore, converterService);
  }

  public void start() {
    myberryServiceVerifier.start();
  }

  public void shutdown() {
    myberryServiceVerifier.shutdown();
  }

  public DefaultResponse getNewId(String key, byte[] attachments) {
    AbstractComponent abstractComponent = myberryStore.getComponentMap().get(key);
    if (null != abstractComponent) {

      switch (abstractComponent.getStructure()) {
        case Structure.CR:
          return myberryServiceVerifier.getNewId(key, Maps.deserialize(attachments));
        case Structure.NS:
          return myberryServiceVerifier.getNewId(key);
      }

      return new DefaultResponse(
          ResponseCode.UNKNOWN_STRUCTURE,
          abstractComponent.getStructure(),
          String.format(
              "this %d structure is unknown, only support %d, %d",
              abstractComponent.getStructure(), Structure.CR, Structure.NS));
    } else {
      DefaultResponse defaultResponse =
          new DefaultResponse(
              ResponseCode.KEY_NOT_EXISTED, String.format("this key: '%s' does not exist", key));
      defaultResponse.setExt(key.getBytes(StandardCharsets.UTF_8));
      return defaultResponse;
    }
  }

  public DefaultResponse addComponent(int structure, byte[] component) {
    switch (structure) {
      case Structure.CR:
        return myberryServiceVerifier.addComponent(
            LightCodec.toObj(component, CRComponentData.class));
      case Structure.NS:
        return myberryServiceVerifier.addComponent(
            LightCodec.toObj(component, NSComponentData.class));
    }

    return new DefaultResponse(
        ResponseCode.UNKNOWN_STRUCTURE,
        structure,
        String.format(
            "this %d structure is unknown, only support %d, %d",
            structure, Structure.CR, Structure.NS));
  }

  public DefaultResponse queryComponentSize() {
    return myberryServiceVerifier.queryComponentSize();
  }

  public DefaultResponse queryComponentByKey(byte[] key) {
    ComponentKeyData componentKeyData = LightCodec.toObj(key, ComponentKeyData.class);
    if (myberryStore.getComponentMap().containsKey(componentKeyData.getKey())) {
      AbstractComponent abstractComponent =
          myberryStore.getComponentMap().get(componentKeyData.getKey());
      switch (abstractComponent.getStructure()) {
        case Structure.CR:
          return myberryServiceVerifier.queryComponentByKey((CRComponent) abstractComponent);
        case Structure.NS:
          return myberryServiceVerifier.queryComponentByKey((NSComponent) abstractComponent);
      }

      return new DefaultResponse(
          ResponseCode.UNKNOWN_STRUCTURE,
          abstractComponent.getStructure(),
          String.format(
              "this %d structure is unknown, only support %d, %d",
              abstractComponent.getStructure(), Structure.CR, Structure.NS));
    } else {
      DefaultResponse defaultResponse = new DefaultResponse(ResponseCode.KEY_NOT_EXISTED);
      defaultResponse.setExt(componentKeyData.getKey().getBytes(StandardCharsets.UTF_8));
      return defaultResponse;
    }
  }

  public HANotifier getHaNotifier() {
    return myberryServiceVerifier.getHaNotifier();
  }

  public void setHaNotifier(HANotifier haNotifier) {
    this.myberryServiceVerifier.setHaNotifier(haNotifier);
  }
}
