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
package org.myberry.common.protocol.body.admin;

import org.myberry.common.codec.LightCodec;
import org.myberry.common.codec.MessageLite;
import org.myberry.common.codec.annotation.SerialField;

public class RouteData implements MessageLite {

  @SerialField(ordinal = 0)
  private int sid;

  @SerialField(ordinal = 1)
  private int weight;

  public RouteData() {}

  public RouteData(int sid) {
    this.sid = sid;
  }

  public RouteData(int sid, int weight) {
    this.sid = sid;
    this.weight = weight;
  }

  public byte[] encode() {
    return LightCodec.toBytes(this);
  }

  public int getSid() {
    return sid;
  }

  public void setSid(int sid) {
    this.sid = sid;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("RouteData{");
    builder.append("sid=");
    builder.append(sid);
    if (weight != 0) {
      builder.append(", weight=");
      builder.append(weight);
    }
    builder.append('}');
    return builder.toString();
  }
}