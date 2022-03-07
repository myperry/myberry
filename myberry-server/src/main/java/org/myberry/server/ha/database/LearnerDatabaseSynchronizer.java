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
package org.myberry.server.ha.database;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.myberry.common.ServiceThread;
import org.myberry.server.common.LoggerName;
import org.myberry.server.converter.ConverterService;
import org.myberry.server.ha.HAHouseKeepService;
import org.myberry.server.ha.HAMessage;
import org.myberry.server.ha.HAMessageDispatcher;
import org.myberry.server.ha.HASynchronizer;
import org.myberry.server.ha.HATransfer;
import org.myberry.store.AbstractComponent;
import org.myberry.store.BlockHeader;
import org.myberry.store.CRComponent;
import org.myberry.store.MyberryStore;
import org.myberry.store.SyncParser;
import org.myberry.store.config.StoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LearnerDatabaseSynchronizer extends ServiceThread implements HASynchronizer {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.LEARNER_DATA_SYNC_NAME);

  private static final int INIT_TIMEOUT = 1000;
  private static final int MAX_TIMEOUT = 30000;

  private final DefaultSyncDatabaseAdapter defaultSyncDatabaseAdapter;
  private final LinkedBlockingQueue<HAMessage> recvQueue;
  private final DatabaseSyncSender databaseSyncSender;
  private final DatabaseSyncReceiver databaseSyncReceiver;

  private volatile boolean timedOut = false;

  public LearnerDatabaseSynchronizer(final DefaultSyncDatabaseAdapter defaultSyncDatabaseAdapter) {
    this.defaultSyncDatabaseAdapter = defaultSyncDatabaseAdapter;
    this.recvQueue = defaultSyncDatabaseAdapter.getRecvQueue();
    this.databaseSyncSender = new DatabaseSyncSender();
    this.databaseSyncReceiver = new DatabaseSyncReceiver();
  }

  @Override
  public String getServiceName() {
    return LearnerDatabaseSynchronizer.class.getSimpleName();
  }

  @Override
  public void run() {
    long lastRecvTimestamp = System.currentTimeMillis();
    int notTimeout = INIT_TIMEOUT;

    boolean syncComplete = false;

    askForLeader(createData(getApplyForBlockHeaderList(getMyberryStore().getBlockHeaderList())));

    while (!syncComplete && !this.isStopped()) {
      try {
        HAMessage haMessage = recvQueue.poll(notTimeout, TimeUnit.MILLISECONDS);
        if (null == haMessage) {
          if (getHaMessageDispatcher().haveDelivered(getLeader())) {
            askForLeader(
                createData(getApplyForBlockHeaderList(getMyberryStore().getBlockHeaderList())));
          } else {
            getHaMessageDispatcher().connect(getLeader());
          }

          int tmpTimeout = notTimeout + 1000;
          notTimeout = tmpTimeout < MAX_TIMEOUT ? tmpTimeout : MAX_TIMEOUT;
        } else {
          Database db = (Database) haMessage.getHaMessage();
          if (!authLeader(db.getLeader())) {
            continue;
          }

          switch (SyncDatabaseCommand.transform(db.getCmd())) {
            case APPEND:
              {
                lastRecvTimestamp = System.currentTimeMillis();

                ApplyForBlockAppend remoteLastBlockHeader = db.getApplyForBlockAppend();

                BlockHeader localLastBlockHeader =
                    getLastBlockHeader(getMyberryStore().getBlockHeaderList());
                if (null == localLastBlockHeader) {
                  continue;
                }

                if (verify(localLastBlockHeader, remoteLastBlockHeader)) {
                  if (needToUpdate(haMessage.getData())) {
                    appendAndLoadData(haMessage.getData());
                  } else {
                    syncComplete = true;
                    log.info("Database synchronization is complete.");
                  }
                } else {
                  log.warn(
                      "The leader({}) and my database({}) may be inconsistent, my lastBlockIndex={}, lastEndPhyOffset={}, leader's lastBlockIndex={}, lastEndPhyOffset={}.",
                      getLeader(),
                      getStoreConfig().getMySid(),
                      localLastBlockHeader.getBlockIndex(),
                      localLastBlockHeader.getEndPhyOffset(),
                      remoteLastBlockHeader.getApplyForBlockIndex(),
                      remoteLastBlockHeader.getApplyForEndPhyOffset());
                }
              }
          }
        }

        long now = System.currentTimeMillis();
        if (now - lastRecvTimestamp > 120 * 1000) {
          this.timedOut = true;
          break;
        }
      } catch (InterruptedException e) {
        log.error(
            "A very serious error, the thread was interrupted in the process of synchronizing database.",
            e);
      } catch (Exception e) {
        log.error("checksum error: ", e);
      }
    }
  }

  @Override
  public boolean sync() throws Exception {
    this.start();
    this.join();
    if (timedOut) {
      return true;
    }

    databaseSyncReceiver.start();
    databaseSyncSender.start();

    return false;
  }

  @Override
  public void shutdown() {
    databaseSyncSender.shutdown(true);
    databaseSyncReceiver.shutdown(false);
    this.shutdown(false);
  }

  class DatabaseSyncSender extends ServiceThread {

    @Override
    public String getServiceName() {
      return DatabaseSyncSender.class.getSimpleName();
    }

    @Override
    public void run() {
      while (!this.isStopped()) {
        try {
          ApplyForBlockAppend applyForBlockAppend = initializeApplyForBlockAppend();
          if (null != applyForBlockAppend) {
            if (getHaMessageDispatcher().haveDelivered(getLeader())) {
              askForLeader(createData(applyForBlockAppend));
            } else {
              getHaMessageDispatcher().connect(getLeader());
            }
          }

          this.waitForRunning(30 * 1000);
        } catch (InterruptedException e) {
          // Ignore
        } catch (Exception e) {
          LearnerDatabaseSynchronizer.log.error("DatabaseSyncSender error: ", e);
          try {
            this.waitForRunning(30 * 1000);
          } catch (InterruptedException ex) {
            // Ignore
          }
        }
      }
    }
  }

  class DatabaseSyncReceiver extends ServiceThread {

    @Override
    public String getServiceName() {
      return DatabaseSyncReceiver.class.getSimpleName();
    }

    @Override
    public void run() {
      while (!this.isStopped()) {
        try {
          HAMessage haMessage = recvQueue.poll();
          if (null != haMessage) {
            Database db = (Database) haMessage.getHaMessage();
            if (authLeader(db.getLeader())) {
              getHaHouseKeepService().updateLeaderResponseTime();

              switch (SyncDatabaseCommand.transform(db.getCmd())) {
                case APPEND:
                  {
                    ApplyForBlockAppend remoteLastBlockHeader = db.getApplyForBlockAppend();

                    BlockHeader localLastBlockHeader =
                        getLastBlockHeader(getMyberryStore().getBlockHeaderList());
                    if (null == localLastBlockHeader) {
                      break;
                    }

                    if (verify(localLastBlockHeader, remoteLastBlockHeader)) {
                      if (needToUpdate(haMessage.getData())) {
                        appendAndLoadData(haMessage.getData());
                        LearnerDatabaseSynchronizer.log.info(
                            "Database synchronization is complete.");

                        if (databaseSyncSender != null) {
                          databaseSyncSender.wakeup();
                        }
                      } else {
                        LearnerDatabaseSynchronizer.log.debug(
                            "Database synchronization is complete.");
                      }
                    } else {
                      LearnerDatabaseSynchronizer.log.warn(
                          "The leader({}) and my database({}) may be inconsistent, my lastBlockIndex={}, lastEndPhyOffset={}, leader's lastBlockIndex={}, lastEndPhyOffset={}.",
                          getLeader(),
                          getStoreConfig().getMySid(),
                          localLastBlockHeader.getBlockIndex(),
                          localLastBlockHeader.getEndPhyOffset(),
                          remoteLastBlockHeader.getApplyForBlockIndex(),
                          remoteLastBlockHeader.getApplyForEndPhyOffset());
                    }
                    break;
                  }
              }
            }
          }
          this.waitForRunning(1 * 1000);
        } catch (InterruptedException e) {
          // Ignore
        } catch (Exception e) {
          LearnerDatabaseSynchronizer.log.error("DatabaseSyncReceiver error: ", e);
          try {
            this.waitForRunning(1 * 1000);
          } catch (InterruptedException ex) {
            // Ignore
          }
        }
      }
    }
  }

  private void askForLeader(Database data) {
    HAMessage haMessage = new HAMessage(HATransfer.DATABASE, getLeader(), data);
    getHaMessageDispatcher().haMessageDelivery(getLeader(), haMessage);
  }

  private ApplyForBlockHeader[] getApplyForBlockHeaderList(List<BlockHeader> blockHeaderList) {
    ApplyForBlockHeader[] applyForBlockHeaderList = new ApplyForBlockHeader[blockHeaderList.size()];
    for (int i = 0; i < blockHeaderList.size(); i++) {
      ApplyForBlockHeader applyForBlockHeader = new ApplyForBlockHeader();
      applyForBlockHeader.setBlockIndex(blockHeaderList.get(i).getBlockIndex());
      applyForBlockHeader.setComponentCount(blockHeaderList.get(i).getComponentCount());
      applyForBlockHeader.setEndPhyOffset(blockHeaderList.get(i).getEndPhyOffset());
      applyForBlockHeaderList[i] = applyForBlockHeader;
    }

    return applyForBlockHeaderList;
  }

  private Database createData(ApplyForBlockHeader[] applyForBlockHeaderList) {
    Database db = new Database();
    db.setSid(getStoreConfig().getMySid());
    db.setLeader(getLeader());
    db.setCmd(SyncDatabaseCommand.CHECKSUM.getCode());
    db.setApplyForBlockHeaderList(applyForBlockHeaderList);
    return db;
  }

  private Database createData(ApplyForBlockAppend applyForBlockAppend) {
    Database db = new Database();
    db.setSid(getStoreConfig().getMySid());
    db.setLeader(getLeader());
    db.setCmd(SyncDatabaseCommand.APPEND.getCode());
    db.setApplyForBlockAppend(applyForBlockAppend);
    return db;
  }

  private BlockHeader getLastBlockHeader(List<BlockHeader> blockHeaderList) {
    if (null != blockHeaderList) {
      return blockHeaderList.get(blockHeaderList.size() - 1);
    } else {
      return null;
    }
  }

  private boolean verify(
      BlockHeader localLastBlockHeader, ApplyForBlockAppend remoteLastBlockHeader) {
    if (localLastBlockHeader.getBlockIndex() == remoteLastBlockHeader.getApplyForBlockIndex()
        && localLastBlockHeader.getEndPhyOffset()
            == remoteLastBlockHeader.getApplyForEndPhyOffset()) {
      return true;
    } else {
      return false;
    }
  }

  private boolean needToUpdate(byte[] syncData) {
    if (syncData == null || syncData.length <= 0) {
      return false;
    } else {
      return true;
    }
  }

  private ApplyForBlockAppend initializeApplyForBlockAppend() {
    BlockHeader localLastBlockHeader = getLastBlockHeader(getMyberryStore().getBlockHeaderList());
    if (null != localLastBlockHeader) {
      ApplyForBlockAppend applyForBlockAppend = new ApplyForBlockAppend();
      applyForBlockAppend.setApplyForBlockIndex(localLastBlockHeader.getBlockIndex());
      applyForBlockAppend.setApplyForEndPhyOffset(localLastBlockHeader.getEndPhyOffset());
      return applyForBlockAppend;
    }

    return null;
  }

  private void appendAndLoadData(byte[] syncData) {
    List<AbstractComponent> abstractComponents = SyncParser.parseComponent(syncData);
    for (AbstractComponent component : abstractComponents) {
      getMyberryStore().addComponent(component);
      if (component instanceof CRComponent) {
        CRComponent crc = (CRComponent) component;
        getConverterService().addStruct(crc.getKey(), crc.getExpression());
      }
    }
  }

  private boolean authLeader(int leader) {
    return getLeader() == leader;
  }

  private int getLeader() {
    return defaultSyncDatabaseAdapter.getHaService().getQuorum().getLeader();
  }

  private HAMessageDispatcher getHaMessageDispatcher() {
    return defaultSyncDatabaseAdapter.getHaService().getHaMessageDispatcher();
  }

  private StoreConfig getStoreConfig() {
    return defaultSyncDatabaseAdapter.getHaService().getHaContext().getStoreConfig();
  }

  private MyberryStore getMyberryStore() {
    return defaultSyncDatabaseAdapter.getHaService().getMyberryStore();
  }

  private ConverterService getConverterService() {
    return defaultSyncDatabaseAdapter.getHaService().getConverterService();
  }

  private HAHouseKeepService getHaHouseKeepService() {
    return defaultSyncDatabaseAdapter.getHaService().getHaHouseKeepService();
  }
}
