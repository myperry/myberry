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

import java.util.Date;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.myberry.common.component.ComponentStatus;
import org.myberry.common.protocol.body.admin.CRComponentData;
import org.myberry.common.protocol.body.admin.ComponentSizeData;
import org.myberry.common.protocol.body.admin.NSComponentData;
import org.myberry.common.protocol.body.user.CRPullResultData;
import org.myberry.common.protocol.body.user.NSPullResultData;
import org.myberry.common.strategy.StrategyDate;
import org.myberry.common.structure.Structure;
import org.myberry.server.common.LoggerName;
import org.myberry.server.converter.ConverterService;
import org.myberry.server.ha.HANotifier;
import org.myberry.server.util.DateUtils;
import org.myberry.store.CRComponent;
import org.myberry.store.MyberryStore;
import org.myberry.store.NSComponent;
import org.myberry.store.config.StoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyberryServiceImpl {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.SERVICE_LOGGER_NAME);

  private final Lock lock = new ReentrantLock();

  private final MyberryStore myberryStore;
  private final ConverterService converterService;

  private HANotifier haNotifier;

  public MyberryServiceImpl(
      final MyberryStore myberryStore, final ConverterService converterService) {
    this.myberryStore = myberryStore;
    this.converterService = converterService;
  }

  public CRPullResultData getNewId(String key, Map<String, String> attachments) throws Exception {
    CRComponent crc = (CRComponent) myberryStore.getComponentMap().get(key);
    crc.getLock().lock();
    try {
      if (isReset(crc)) {
        crc.setIncrNumber(0);
      }
      crc.setUpdateTime(System.currentTimeMillis());
      long incr = crc.getIncrNumber().incrementAndGet();

      myberryStore.updateBufferLong(
          crc.getBlockIndex(),
          crc.getPhyOffset() + CRComponent.updateTimeRelativeOffset,
          crc.getUpdateTime());
      myberryStore.updateBufferLong(
          crc.getBlockIndex(), crc.getPhyOffset() + CRComponent.incrNumberRelativeOffset, incr);

      CRPullResultData crd = new CRPullResultData();
      crd.setNewId(converterService.getStruct(key).getResult(crc, attachments));
      return crd;
    } catch (Exception e) {
      log.error("getNewId() error: ", e);
      throw e;
    } finally {
      crc.getLock().unlock();
    }
  }

  public NSPullResultData getNewId(String key) throws Exception {
    NSComponent nsc = (NSComponent) myberryStore.getComponentMap().get(key);
    nsc.getLock().lock();
    try {
      if (isReset(nsc)) {
        nsc.setCurrentNumber(nsc.getInitNumber());
      }

      int start = nsc.getCurrentNumber().getAndAdd(nsc.getStepSize());
      nsc.setUpdateTime(System.currentTimeMillis());

      int current = start + nsc.getStepSize();

      myberryStore.updateBufferLong(
          nsc.getBlockIndex(),
          nsc.getPhyOffset() + NSComponent.updateTimeRelativeOffset,
          nsc.getUpdateTime());
      myberryStore.updateBufferInt(
          nsc.getBlockIndex(),
          nsc.getPhyOffset() + NSComponent.currentNumberRelativeOffset,
          current);

      NSPullResultData nsd = new NSPullResultData();
      nsd.setStart(start);
      nsd.setEnd(current - 1);
      nsd.setSynergyId(myberryStore.getStoreConfig().getMySid());
      return nsd;
    } catch (Exception e) {
      log.error("getNewId() error: ", e);
      throw e;
    } finally {
      nsc.getLock().unlock();
    }
  }

  public boolean addComponent(CRComponentData crcd) throws Exception {
    lock.lock();
    try {
      long currentTime = new Date().getTime();
      int keyLength = crcd.getKey().getBytes(StoreConfig.MYBERRY_STORE_DEFAULT_CHARSET).length;
      int expressionLength =
          crcd.getExpression().getBytes(StoreConfig.MYBERRY_STORE_DEFAULT_CHARSET).length;
      int componentLength = CRComponent.COMPONENT_FIXED_FIELD_LENGTH + keyLength + expressionLength;

      CRComponent crc = new CRComponent();
      crc.setComponentLength((short) componentLength);
      crc.setStructure((byte) Structure.CR);
      crc.setStatus((byte) ComponentStatus.OPEN.getStatus());
      crc.setPhyOffset(myberryStore.getLastPosition());
      crc.setCreateTime(currentTime);
      crc.setUpdateTime(currentTime);
      crc.setKeyLength((short) keyLength);
      crc.setKey(crcd.getKey().trim());
      crc.setExpressionLength((short) expressionLength);
      crc.setExpression(crcd.getExpression().trim());

      myberryStore.addComponent(crc);

      converterService.addStruct(crc.getKey(), crc.getExpression());

      if (null != haNotifier) {
        haNotifier.updateBlockTable();
        haNotifier.notifyDatabaseAppend(
            crc.getBlockIndex(),
            crc.getPhyOffset(),
            myberryStore.getSyncData(
                crc.getBlockIndex(), crc.getPhyOffset(), crc.getComponentLength()));
      }

      log.info(
          "{} ++> add key: [{}], expression: [{}] success.",
          this.getServiceName(),
          crc.getKey(),
          crc.getExpression());
      return true;
    } catch (Exception e) {
      log.error("addComponent() error: ", e);
      throw e;
    } finally {
      lock.unlock();
    }
  }

  public boolean addComponent(NSComponentData nscd) throws Exception {
    lock.lock();
    try {
      long currentTime = new Date().getTime();
      int keyLength = nscd.getKey().getBytes(StoreConfig.MYBERRY_STORE_DEFAULT_CHARSET).length;
      int componentLength = NSComponent.COMPONENT_FIXED_FIELD_LENGTH + keyLength;

      NSComponent nsc = new NSComponent();
      nsc.setComponentLength((short) componentLength);
      nsc.setStructure((byte) Structure.NS);
      nsc.setStatus((byte) ComponentStatus.OPEN.getStatus());
      nsc.setPhyOffset(myberryStore.getLastPosition());
      nsc.setCreateTime(currentTime);
      nsc.setUpdateTime(currentTime);
      nsc.setInitNumber(nscd.getInitNumber());
      nsc.setCurrentNumber(nscd.getInitNumber());
      nsc.setStepSize(nscd.getStepSize());
      nsc.setResetType((byte) nscd.getResetType());
      nsc.setKeyLength((short) keyLength);
      nsc.setKey(nscd.getKey().trim());

      myberryStore.addComponent(nsc);

      if (null != haNotifier) {
        haNotifier.updateBlockTable();
        haNotifier.notifyDatabaseAppend(
            nsc.getBlockIndex(),
            nsc.getPhyOffset(),
            myberryStore.getSyncData(
                nsc.getBlockIndex(), nsc.getPhyOffset(), nsc.getComponentLength()));
      }

      log.info(
          "{} ++> add key: [{}], initNumber: [{}], stepSize: [{}], resetType: [{}] success.",
          this.getServiceName(),
          nsc.getKey(),
          nsc.getInitNumber(),
          nsc.getStepSize(),
          nsc.getResetType());
      return true;
    } catch (Exception e) {
      log.error("addComponent() error: ", e);
      throw e;
    } finally {
      lock.unlock();
    }
  }

  public ComponentSizeData queryComponentSize() {
    ComponentSizeData componentSizeData = new ComponentSizeData();
    componentSizeData.setSize(myberryStore.getComponentMap().size());
    return componentSizeData;
  }

  public CRComponentData queryComponentByKey(CRComponent crc) {
    CRComponentData crcd = new CRComponentData();
    crcd.setKey(crc.getKey());
    crcd.setExpression(crc.getExpression());
    crcd.setStatus(crc.getStatus());
    crcd.setCreateTime(crc.getCreateTime());
    crcd.setUpdateTime(crc.getUpdateTime());
    return crcd;
  }

  public NSComponentData queryComponentByKey(NSComponent nsc) {
    NSComponentData nscd = new NSComponentData();
    nscd.setKey(nsc.getKey());
    nscd.setInitNumber(nsc.getInitNumber());
    nscd.setStepSize(nsc.getStepSize());
    nscd.setResetType(nsc.getResetType());
    nscd.setStatus(nsc.getStatus());
    nscd.setCreateTime(nsc.getCreateTime());
    nscd.setUpdateTime(nsc.getUpdateTime());
    return nscd;
  }

  private boolean isReset(CRComponent crComponent) {
    int type = DateUtils.isIncludeTime(crComponent.getExpression());
    if (type == StrategyDate.NON_TIME) {
      return false;
    }

    long currentTimeMillis = System.currentTimeMillis();
    long updateTime = crComponent.getUpdateTime();
    if (currentTimeMillis > updateTime
        && !DateUtils.convertTime(type, currentTimeMillis)
            .equals(DateUtils.convertTime(type, updateTime))) {
      return true;
    } else {
      return false;
    }
  }

  private boolean isReset(NSComponent nsComponent) {
    long currentTimeMillis = System.currentTimeMillis();

    if (StrategyDate.NON_TIME == nsComponent.getResetType()) {
      return false;
    } else if (StrategyDate.TIME_DAY == nsComponent.getResetType()) {
      return compare(currentTimeMillis, nsComponent.getUpdateTime(), StrategyDate.TIME_DAY);
    } else if (StrategyDate.TIME_MONTH == nsComponent.getResetType()) {
      return compare(currentTimeMillis, nsComponent.getUpdateTime(), StrategyDate.TIME_MONTH);
    } else if (StrategyDate.TIME_YEAR == nsComponent.getResetType()) {
      return compare(currentTimeMillis, nsComponent.getUpdateTime(), StrategyDate.TIME_YEAR);
    } else {
      return false;
    }
  }

  private boolean compare(long currentTime, long updateTime, int type) {
    return currentTime > updateTime
        && !DateUtils.convertTime(type, currentTime)
            .equals(DateUtils.convertTime(type, updateTime));
  }

  public HANotifier getHaNotifier() {
    return haNotifier;
  }

  public void setHaNotifier(HANotifier haNotifier) {
    this.haNotifier = haNotifier;
  }

  public String getServiceName() {
    return MyberryServiceImpl.class.getSimpleName();
  }
}
