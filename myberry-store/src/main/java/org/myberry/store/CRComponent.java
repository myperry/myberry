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
package org.myberry.store;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.myberry.common.structure.Structure;

public class CRComponent extends AbstractComponent {

  // COMPONENT_FIXED_FIELD_LENGTH = componentLengthRelativeOffset + structureRelativeOffset +
  // statusRelativeOffset + phyOffsetRelativeOffset + createTimeRelativeOffset +
  // updateTimeRelativeOffset + incrNumberRelativeOffset + keyLengthRelativeOffset +
  // expressionLengthRelativeOffset
  public static final int COMPONENT_FIXED_FIELD_LENGTH = 36;

  public static int componentLengthRelativeOffset =
      AbstractComponent.COMPONENT_LENGTH_RELATIVE_OFFSET;
  public static int structureRelativeOffset = AbstractComponent.STRUCTURE_RELATIVE_OFFSET;
  public static int statusRelativeOffset = 3;
  public static int phyOffsetRelativeOffset = 4;
  public static int createTimeRelativeOffset = 8;
  public static int updateTimeRelativeOffset = 16;
  public static int incrNumberRelativeOffset = 24;

  private short componentLength;

  private byte structure = (byte) Structure.CR;

  private byte status;

  private int phyOffset;

  private long createTime;

  private long updateTime;

  private AtomicLong incrNumber = new AtomicLong(0);

  private short keyLength;

  private String key;

  private short expressionLength;

  private String expression;

  private final Lock lock = new ReentrantLock();

  public CRComponent() {}

  public CRComponent(int blockIndex) {
    this.blockIndex = blockIndex;
  }

  public short getComponentLength() {
    return componentLength;
  }

  public void setComponentLength(short componentLength) {
    this.componentLength = componentLength;
  }

  @Override
  public byte getStructure() {
    return structure;
  }

  public void setStructure(byte structure) {
    this.structure = structure;
  }

  public byte getStatus() {
    return status;
  }

  public void setStatus(byte status) {
    this.status = status;
  }

  public int getPhyOffset() {
    return phyOffset;
  }

  @Override
  public void setPhyOffset(int phyOffset) {
    this.phyOffset = phyOffset;
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

  public AtomicLong getIncrNumber() {
    return incrNumber;
  }

  public void setIncrNumber(long incrNumber) {
    this.incrNumber.set(incrNumber);
  }

  public short getKeyLength() {
    return keyLength;
  }

  public void setKeyLength(short keyLength) {
    this.keyLength = keyLength;
  }

  @Override
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public short getExpressionLength() {
    return expressionLength;
  }

  public void setExpressionLength(short expressionLength) {
    this.expressionLength = expressionLength;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public Lock getLock() {
    return lock;
  }

  @Override
  public String toString() {
    return new StringBuilder() //
        .append("CRComponent [") //
        .append("componentLength=") //
        .append(componentLength) //
        .append(", structure=") //
        .append(structure) //
        .append(", status=") //
        .append(status) //
        .append(", phyOffset=") //
        .append(phyOffset) //
        .append(", createTime=") //
        .append(createTime) //
        .append(", updateTime=") //
        .append(updateTime) //
        .append(", incrNumber=") //
        .append(incrNumber.get()) //
        .append(", keyLength=") //
        .append(keyLength) //
        .append(", key='") //
        .append(key) //
        .append('\'') //
        .append(", expressionLength=") //
        .append(expressionLength) //
        .append(", expression='") //
        .append(expression) //
        .append('\'') //
        .append(']') //
        .toString();
  }
}
