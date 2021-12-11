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
import org.myberry.common.ServiceThread;
import org.myberry.common.codec.LightCodec;
import org.myberry.server.common.LoggerName;
import org.myberry.server.ha.HAHouseKeepService;
import org.myberry.server.ha.HAMessage;
import org.myberry.server.ha.HAMessageDispatcher;
import org.myberry.server.ha.HASynchronizer;
import org.myberry.server.ha.HATransfer;
import org.myberry.store.BlockHeader;
import org.myberry.store.MyberryStore;
import org.myberry.store.config.StoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaderDatabaseSynchronizer implements HASynchronizer {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.LEADER_DATA_SYNC_NAME);

  private final DefaultSyncDatabaseAdapter defaultSyncDatabaseAdapter;
  private final LinkedBlockingQueue<HAMessage> recvQueue;
  private final DatabaseSyncTrigger databaseSyncTrigger;

  public LeaderDatabaseSynchronizer(final DefaultSyncDatabaseAdapter defaultSyncDatabaseAdapter) {
    this.defaultSyncDatabaseAdapter = defaultSyncDatabaseAdapter;
    this.recvQueue = defaultSyncDatabaseAdapter.getRecvQueue();
    this.databaseSyncTrigger = new DatabaseSyncTrigger();
  }

  @Override
  public boolean sync() throws Exception {
    databaseSyncTrigger.start();
    return false;
  }

  @Override
  public void shutdown() {
    databaseSyncTrigger.shutdown(true);
  }

  class DatabaseSyncTrigger extends ServiceThread {

    @Override
    public String getServiceName() {
      return DatabaseSyncTrigger.class.getSimpleName();
    }

    @Override
    public void run() {
      while (!this.isStopped()) {
        try {
          HAMessage haMessage = recvQueue.poll();
          if (null != haMessage) {
            Database db = (Database) haMessage.getHaMessage();
            if (leaderIsMe(db.getLeader())) {
              getHaHouseKeepService().updateLearnersResponseTime(db.getSid());

              lead(db);
            }
          } else {
            this.waitForRunning(3 * 1000);
          }
        } catch (InterruptedException e) {
          // Ignore
        } catch (Exception e) {
          LeaderDatabaseSynchronizer.log.error("DataSyncTrigger error: ", e);
          try {
            this.waitForRunning(3 * 1000);
          } catch (InterruptedException ex) {
            // Ignore
          }
        }
      }
    }

    private void lead(Database db) {
      switch (SyncDatabaseCommand.transform(db.getCmd())) {
        case CHECKSUM:
          {
            /*
             *  Check the Learner's file index, component count, end offset
             *
             *  1. All three conditions are met, except for the last file.
             *  2. <1> is established, and the component count and end offset
             *  of the last file are less than Leader.
             *  3. <2> is established, and in the last file, the end offset of
             *  the Learner is the begin offset of a certain component of the Leader.
             */
            List<BlockHeader> localBlockHeaderList = getMyberryStore().getBlockHeaderList();
            if (null == localBlockHeaderList) {
              return;
            }

            ApplyForBlockHeader[] remoteBlockHeaderList = db.getApplyForBlockHeaderList();

            ApplyForBlockHeader remoteLastBlockHeader =
                getApplyForLastBlockHeader(remoteBlockHeaderList);
            if (null == remoteLastBlockHeader) {
              LeaderDatabaseSynchronizer.log.debug("Request information is incomplete: {}", db);
              return;
            }

            if (compareAllBlockFilesValidityExceptLastBlockFile(
                    localBlockHeaderList, remoteBlockHeaderList)
                && compareLastBlockFileValidity(
                    remoteLastBlockHeader.getBlockIndex(),
                    remoteLastBlockHeader.getEndPhyOffset())) {

              ApplyForBlockAppend applyForBlockAppendResponse =
                  createApplyForBlockAppend(
                      remoteLastBlockHeader.getBlockIndex(),
                      remoteLastBlockHeader.getEndPhyOffset());

              Database database = createData(applyForBlockAppendResponse);
              byte[] syncData =
                  getSyncData(
                      remoteLastBlockHeader.getBlockIndex(),
                      remoteLastBlockHeader.getEndPhyOffset(),
                      database);

              if (syncData.length == 0) {
                notifyLearner(db.getSid(), database);
              } else {
                notifyLearner(db.getSid(), database, syncData);
              }
            } else {
              LeaderDatabaseSynchronizer.log.error(
                  "The check fails and the database cannot be synchronized, leader's= {}, learner's={}",
                  localBlockHeaderList,
                  db);
            }

            break;
          }
        case APPEND:
          {
            List<BlockHeader> localBlockHeaderList = getMyberryStore().getBlockHeaderList();
            if (null == localBlockHeaderList) {
              log.info("null == localBlockHeaderList");
              return;
            }
            ApplyForBlockAppend remoteLastBlockHeader = db.getApplyForBlockAppend();
            if (null == remoteLastBlockHeader) {
              log.info("null == remoteLastBlockHeader");
              return;
            }

            if (compareLastBlockFileValidity(
                remoteLastBlockHeader.getApplyForBlockIndex(),
                remoteLastBlockHeader.getApplyForEndPhyOffset())) {

              ApplyForBlockAppend applyForBlockAppendResponse =
                  createApplyForBlockAppend(
                      remoteLastBlockHeader.getApplyForBlockIndex(),
                      remoteLastBlockHeader.getApplyForEndPhyOffset());

              Database database = createData(applyForBlockAppendResponse);
              byte[] syncData =
                  getSyncData(
                      remoteLastBlockHeader.getApplyForBlockIndex(),
                      remoteLastBlockHeader.getApplyForEndPhyOffset(),
                      database);

              if (syncData.length == 0) {
                notifyLearner(db.getSid(), database);
              } else {
                notifyLearner(db.getSid(), database, syncData);
              }
            } else {
              LeaderDatabaseSynchronizer.log.error(
                  "The check fails and the database cannot be synchronized, leader's= {}, learner's={}",
                  localBlockHeaderList,
                  db);
            }

            break;
          }
        default:
          {
            LeaderDatabaseSynchronizer.log.error(
                "Unknow SyncDatabaseCommand: sid={}, cmd={}", db.getSid(), db.getCmd());
          }
      }
    }

    private boolean leaderIsMe(int leader) {
      return getStoreConfig().getMySid() == leader;
    }

    private ApplyForBlockHeader getApplyForLastBlockHeader(
        ApplyForBlockHeader[] remoteBlockHeaderList) {
      if (null == remoteBlockHeaderList || remoteBlockHeaderList.length == 0) {
        return null;
      } else {
        return remoteBlockHeaderList[remoteBlockHeaderList.length - 1];
      }
    }

    private boolean compareAllBlockFilesValidityExceptLastBlockFile(
        List<BlockHeader> localBlockHeaderList, ApplyForBlockHeader[] remoteBlockHeaderList) {
      for (int i = 0; i < remoteBlockHeaderList.length - 1; i++) {
        if (!remoteBlockHeaderList[i].equals(localBlockHeaderList.get(i))) {
          return false;
        }
      }

      return true;
    }

    private boolean compareLastBlockFileValidity(
        int remoteLastBlockIndex, int remoteLastBlockOffset) {
      return getMyberryStore().verifyOffset(remoteLastBlockIndex, remoteLastBlockOffset);
    }

    private void notifyLearner(int connId, Database db) {
      notifyLearner(connId, db, null);
    }

    private void notifyLearner(int connId, Database db, byte[] syncData) {
      HAMessage haMessage = new HAMessage(HATransfer.DATABASE, connId, db);
      haMessage.setData(syncData);
      getHaMessageDispatcher().haMessageDelivery(connId, haMessage);
    }

    // init ApplyForBlockAppend attribute value
    private ApplyForBlockAppend createApplyForBlockAppend(
        int applyForLastBlockIndex, int applyForLastBlockEndPhyOffset) {
      ApplyForBlockAppend applyForBlockAppend = new ApplyForBlockAppend();
      applyForBlockAppend.setApplyForBlockIndex(applyForLastBlockIndex);
      applyForBlockAppend.setApplyForEndPhyOffset(applyForLastBlockEndPhyOffset);
      return applyForBlockAppend;
    }

    private Database createData(ApplyForBlockAppend applyForBlockAppend) {
      Database db = new Database();
      db.setSid(getStoreConfig().getMySid());
      db.setLeader(getStoreConfig().getMySid());
      db.setCmd(SyncDatabaseCommand.APPEND.getCode());
      db.setApplyForBlockAppend(applyForBlockAppend);
      return db;
    }

    private byte[] getSyncData(
        int applyForLastBlockIndex, int applyForLastBlockEndPhyOffset, Database db) {
      int dbFixedLength = LightCodec.toBytes(db).length;

      int remainLength =
          getMyberryStore().getLastPosition(applyForLastBlockIndex) - applyForLastBlockEndPhyOffset;
      if (applyForLastBlockIndex < getMyberryStore().getMaxBlockIndex()) {
        if (remainLength > 0) {
          return getAppendData(
              applyForLastBlockIndex, applyForLastBlockEndPhyOffset, dbFixedLength, remainLength);
        } else if (remainLength == 0) {
          /* StoreHeader.STORE_HEADER_SIZE = 32 */
          remainLength = getMyberryStore().getLastPosition(applyForLastBlockIndex + 1) - 32;
          return getAppendData(applyForLastBlockIndex + 1, 32, dbFixedLength, remainLength);
        }
      } else if (applyForLastBlockIndex == getMyberryStore().getMaxBlockIndex()) {
        if (remainLength > 0) {
          return getAppendData(
              applyForLastBlockIndex, applyForLastBlockEndPhyOffset, dbFixedLength, remainLength);
        }
      }

      return new byte[0];
    }

    private byte[] getAppendData(
        int applyForLastBlockIndex,
        int applyForLastBlockEndPhyOffset,
        int dbFixedLength,
        int remainLength) {
      /*
       * Except syncData, other information is of fixed length
       */
      int allowedSyncLength =
          getHaMessageLength(dbFixedLength, remainLength) <= getStoreConfig().getMaxSyncDataSize()
              ? remainLength
              : getStoreConfig().getMaxSyncDataSize() - getHaMessageLength(dbFixedLength, 0);

      int readLength =
          getMyberryStore()
              .getSyncLength(
                  applyForLastBlockIndex, applyForLastBlockEndPhyOffset, allowedSyncLength);

      return getMyberryStore()
          .getSyncData(applyForLastBlockIndex, applyForLastBlockEndPhyOffset, readLength);
    }

    private int getHaMessageLength(int dbFixedLength, int syncDataLength) {
      return HATransfer.getHaMessageLength(dbFixedLength, syncDataLength);
    }
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

  private HAHouseKeepService getHaHouseKeepService() {
    return defaultSyncDatabaseAdapter.getHaService().getHaHouseKeepService();
  }
}
