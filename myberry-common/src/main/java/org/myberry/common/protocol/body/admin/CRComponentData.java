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

import java.lang.reflect.Field;
import org.myberry.common.Component;
import org.myberry.common.codec.LightCodec;
import org.myberry.common.codec.MessageLite;
import org.myberry.common.codec.annotation.SerialField;
import org.myberry.common.component.ComponentStatus;
import org.myberry.common.structure.Structure;

public class CRComponentData implements MessageLite, Component {

  @SerialField(ordinal = 0)
  private String key;

  @SerialField(ordinal = 1)
  private String expression;

  /** {@link ComponentStatus} */
  @SerialField(ordinal = 2)
  private int status;

  @SerialField(ordinal = 3)
  private long createTime;

  @SerialField(ordinal = 4)
  private long updateTime;

  @SerialField(ordinal = 5)
  private int structure;

  public CRComponentData() {
    this.structure = Structure.CR;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(long createTime) {
    this.createTime = createTime;
  }

  public long getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(long updateTime) {
    this.updateTime = updateTime;
  }

  public int getStructure() {
    return structure;
  }

  public void setStructure(int structure) {
    this.structure = structure;
  }

  @Override
  public byte[] encode() {
    return LightCodec.toBytes(this);
  }

  @Override
  public String toString() {
    Class<?> clz = this.getClass();
    Field[] fields = clz.getDeclaredFields();
    StringBuilder builder = new StringBuilder();
    builder.append("CRComponentData [");
    for (int i = 0; i < fields.length; i++) {
      fields[i].setAccessible(true);
      Object obj = null;
      try {
        obj = fields[i].get(this);
      } catch (IllegalAccessException e) {
      }
      if (obj == null) {
        continue;
      }

      if (i != 0) {
        builder.append(", ");
      }
      builder.append(fields[i].getName());
      builder.append("=");
      builder.append(obj);
    }
    return builder.append("]").toString();
  }
}
