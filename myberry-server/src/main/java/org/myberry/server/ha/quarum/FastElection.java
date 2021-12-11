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
package org.myberry.server.ha.quarum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.myberry.common.ServiceThread;
import org.myberry.remoting.common.RemotingHelper;
import org.myberry.server.common.LoggerName;
import org.myberry.server.ha.HAContext;
import org.myberry.server.ha.HAMessage;
import org.myberry.server.ha.HAMessageDispatcher;
import org.myberry.server.ha.HAState;
import org.myberry.server.ha.HATransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastElection extends ServiceThread {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.FAST_ELECTION_NAME);

  private static final int INIT_TIMEOUT = 200;
  private static final int MAX_TIMEOUT = 60000;

  private final DefaultQuorum defaultQuorum;
  private final Messenger messenger;
  private final LinkedBlockingQueue<HAMessage> recvQueue;
  private final LinkedBlockingQueue<Vote> lookupQueue = new LinkedBlockingQueue<>();

  private AtomicLong logicalclock;
  private int proposedLeader;
  private long proposedOffset;
  private long proposedEpoch;

  private volatile Vote currentVote;

  public FastElection(final DefaultQuorum defaultQuorum) {
    this.defaultQuorum = defaultQuorum;
    this.messenger = new Messenger();
    this.recvQueue = defaultQuorum.getRecvQueue();

    this.proposedLeader = -1;
    this.proposedOffset = -1;
  }

  @Override
  public String getServiceName() {
    return FastElection.class.getSimpleName();
  }

  @Override
  public void run() {
    try {
      HashMap<Integer, Vote> recvset = new HashMap<>();

      HashMap<Integer, Vote> outofelection = new HashMap<>();

      int notTimeout = INIT_TIMEOUT;

      long peerEpoch = logicalclock.get();

      logicalclock.incrementAndGet();
      updateProposal(getInitSid(), getInitOffset(), peerEpoch);

      sendVotes();

      while (getHaContext().getHaState() == HAState.LOOKING && !this.isStopped()) {
        Vote recVote = lookupQueue.poll(notTimeout, TimeUnit.MILLISECONDS);
        if (null == recVote) {
          if (getHaMessageDispatcher().haveDelivered()) {
            sendVotes();
          } else {
            getHaMessageDispatcher().connectUnbound();
          }

          int tmpTimeout = notTimeout * 2;
          notTimeout = tmpTimeout < MAX_TIMEOUT ? tmpTimeout : MAX_TIMEOUT;
        } else if (validVoter(recVote.getSid()) && validVoter(recVote.getLeader())) {
          switch (HAState.transform(recVote.getHaState())) {
            case LOOKING:
              if (recVote.getElectEpoch() > logicalclock.get()) {
                logicalclock.set(recVote.getElectEpoch());
                recvset.clear();
                if (totalOrderPredicate(
                    recVote.getLeader(),
                    recVote.getOffset(),
                    recVote.getPeerEpoch(),
                    getInitSid(),
                    getInitOffset(),
                    peerEpoch)) {
                  updateProposal(recVote.getLeader(), recVote.getOffset(), recVote.getPeerEpoch());
                } else {
                  updateProposal(getInitSid(), getInitOffset(), peerEpoch);
                }

                sendVotes();
              } else if (recVote.getElectEpoch() < logicalclock.get()) {
                break;
              } else if (totalOrderPredicate(
                  recVote.getLeader(),
                  recVote.getOffset(),
                  recVote.getPeerEpoch(),
                  proposedLeader,
                  proposedOffset,
                  proposedEpoch)) {
                updateProposal(recVote.getLeader(), recVote.getOffset(), recVote.getPeerEpoch());
                sendVotes();
              }

              recvset.put(
                  recVote.getSid(),
                  new Vote(
                      recVote.getLeader(),
                      recVote.getOffset(),
                      recVote.getPeerEpoch(),
                      recVote.getElectEpoch()));
              log.debug(
                  "<recvset> addding vote: from {}, proposed leader={}, proposed offset=0x{}, proposed election epoch=0x{}",
                  recVote.getSid(),
                  recVote.getLeader(),
                  Long.toHexString(recVote.getOffset()),
                  Long.toHexString(recVote.getElectEpoch()));

              if (termPredicate(
                  recvset,
                  new Vote(proposedLeader, proposedOffset, proposedEpoch, logicalclock.get()))) {
                // Verify if there is any change in the proposed leader
                while ((recVote = lookupQueue.poll(INIT_TIMEOUT, TimeUnit.MILLISECONDS)) != null) {
                  if (totalOrderPredicate(
                      recVote.getLeader(),
                      recVote.getOffset(),
                      recVote.getPeerEpoch(),
                      proposedLeader,
                      proposedOffset,
                      proposedEpoch)) {
                    lookupQueue.put(recVote);
                    break;
                  }
                }

                /*
                 * This predicate is true once we don't read any new
                 * relevant message from the reception queue
                 */
                if (recVote == null) {
                  getHaContext()
                      .setHaState(
                          (proposedLeader == getHaContext().getStoreConfig().getMySid())
                              ? HAState.LEADING
                              : HAState.LEARNING);
                  Vote endVote =
                      new Vote(proposedLeader, proposedOffset, proposedEpoch, logicalclock.get());
                  leaveInstance(endVote);
                  return;
                }
              }
              break;
            case LEARNING:
            case LEADING:
              /*
               * Consider all votes from the same epoch together.
               */
              if (recVote.getElectEpoch() == logicalclock.get()) {
                recvset.put(
                    recVote.getSid(),
                    new Vote(
                        recVote.getLeader(),
                        recVote.getOffset(),
                        recVote.getPeerEpoch(),
                        recVote.getElectEpoch()));
                log.debug(
                    "<recvset> addding vote: from {}, proposed leader={}, proposed offset=0x{}, proposed election epoch=0x{}",
                    recVote.getSid(),
                    recVote.getLeader(),
                    Long.toHexString(recVote.getOffset()),
                    Long.toHexString(recVote.getElectEpoch()));
                if (termPredicate(
                        recvset,
                        new Vote(
                            recVote.getLeader(),
                            recVote.getOffset(),
                            recVote.getPeerEpoch(),
                            recVote.getElectEpoch(),
                            recVote.getHaState()))
                    && checkLeader(outofelection, recVote.getLeader(), recVote.getElectEpoch())) {
                  getHaContext()
                      .setHaState(
                          (recVote.getLeader() == getHaContext().getStoreConfig().getMySid())
                              ? HAState.LEADING
                              : HAState.LEARNING);
                  Vote endVote =
                      new Vote(
                          recVote.getLeader(),
                          recVote.getOffset(),
                          recVote.getPeerEpoch(),
                          recVote.getElectEpoch());
                  leaveInstance(endVote);
                  return;
                }
              }

              /*
               * Before joining an established ensemble, verify that
               * a majority are following the same leader.
               */
              outofelection.put(
                  recVote.getSid(),
                  new Vote(
                      recVote.getLeader(),
                      recVote.getOffset(),
                      recVote.getPeerEpoch(),
                      recVote.getElectEpoch(),
                      recVote.getHaState()));
              log.debug(
                  "<outofelection> addding vote: from {}, proposed leader={}, proposed peer epoch=0x{}",
                  recVote.getSid(),
                  recVote.getLeader(),
                  Long.toHexString(recVote.getPeerEpoch()));
              if (termPredicate(
                      outofelection,
                      new Vote(
                          recVote.getLeader(),
                          recVote.getOffset(),
                          recVote.getPeerEpoch(),
                          recVote.getElectEpoch(),
                          recVote.getHaState()))
                  && checkLeader(outofelection, recVote.getLeader(), recVote.getElectEpoch())) {

                logicalclock.set(recVote.getElectEpoch());
                getHaContext()
                    .setHaState(
                        (recVote.getLeader() == getHaContext().getStoreConfig().getMySid())
                            ? HAState.LEADING
                            : HAState.LEARNING);
                Vote endVote =
                    new Vote(
                        recVote.getLeader(),
                        recVote.getOffset(),
                        recVote.getPeerEpoch(),
                        recVote.getElectEpoch());
                leaveInstance(endVote);
                return;
              }
              break;
            default:
              log.warn(
                  "Vote state unrecoginized: haState={}, sid={}",
                  recVote.getHaState(),
                  recVote.getSid());
              break;
          }
        } else {
          if (!validVoter(recVote.getLeader())) {
            log.warn(
                "Ignoring vote for non-cluster member sid {} from sid {}",
                recVote.getLeader(),
                recVote.getSid());
          }
          if (!validVoter(recVote.getSid())) {
            log.warn(
                "Ignoring vote for sid {} from non-quorum member sid {}",
                recVote.getLeader(),
                recVote.getSid());
          }
        }
      }
    } catch (InterruptedException e) {
      log.warn("{} Interrupted.", this.getServiceName());
    }
  }

  public void half() throws InterruptedException {
    messenger.start();
    this.start();
    this.join();
  }

  public void shutdown() {
    this.shutdown(true);
    messenger.shutdown(true);
  }

  class Messenger extends ServiceThread {

    @Override
    public void run() {
      while (!this.isStopped()) {
        try {
          HAMessage haMessage = recvQueue.poll(1 * 3000, TimeUnit.MILLISECONDS);
          if (null == haMessage) {
            continue;
          }
          Vote recVote = (Vote) haMessage.getHaMessage();

          Precondition cond = makePrecondition(getHaContext().getMemberMap());
          if (Precondition.check(cond, recVote.getCond())) {
            if (getHaContext().getHaState() == HAState.LOOKING) {
              lookupQueue.offer(recVote);
              if (recVote.getHaState() == HAState.LOOKING.getCode()
                  && recVote.getElectEpoch() < logicalclock.get()) {
                Vote selfVote =
                    new Vote(
                        cond,
                        proposedLeader,
                        proposedOffset,
                        proposedEpoch,
                        logicalclock.get(),
                        getHaContext().getHaState().getCode(),
                        getHaContext().getStoreConfig().getMySid());

                HAMessage selfMessage = new HAMessage(HATransfer.VOTE, recVote.getSid(), selfVote);
                getHaMessageDispatcher().haMessageDelivery(recVote.getSid(), selfMessage);
              }
            } else {
              if (recVote.getHaState() == HAState.LOOKING.getCode()) {
                Vote currVote =
                    new Vote(
                        cond,
                        currentVote.getLeader(),
                        currentVote.getOffset(),
                        currentVote.getPeerEpoch(),
                        currentVote.getElectEpoch(),
                        getHaContext().getHaState().getCode(),
                        getHaContext().getStoreConfig().getMySid());

                HAMessage selfMessage = new HAMessage(HATransfer.VOTE, recVote.getSid(), currVote);
                getHaMessageDispatcher().haMessageDelivery(recVote.getSid(), selfMessage);
              }
            }
          } else {
            FastElection.log.warn(
                "{} receive an invalid vote: [{}], self precondition [{}]",
                this.getServiceName(),
                recVote,
                cond);
          }
        } catch (InterruptedException e) {
          break;
        } catch (Exception e) {
          FastElection.log.error("Messenger error: ", e);
        }
      }
    }

    @Override
    public String getServiceName() {
      return Messenger.class.getSimpleName();
    }
  }

  public void setLogicalclock(AtomicLong logicalclock) {
    this.logicalclock = logicalclock;
  }

  public Vote getCurrentVote() {
    return currentVote;
  }

  private Precondition makePrecondition(Map<Integer, String> viewMap) {
    int i = 0;
    Member[] nodes = new Member[viewMap.size()];

    Iterator<Entry<Integer, String>> it = viewMap.entrySet().iterator();
    while (it.hasNext()) {
      Entry<Integer, String> next = it.next();
      Integer sid = next.getKey();
      String ip = RemotingHelper.getAddressIP(next.getValue());
      int haPort = RemotingHelper.getAddressPort(next.getValue());
      nodes[i] = new Member(ip, haPort, sid);
      i++;
    }
    Precondition precondition = new Precondition();
    precondition.setMembers(nodes);
    precondition.setBlockFileSize(getHaContext().getStoreConfig().getBlockFileSize());
    precondition.setMaxSyncDataSize(getHaContext().getStoreConfig().getMaxSyncDataSize());
    return precondition;
  }

  private void sendVotes() throws InterruptedException {
    Vote vote =
        new Vote(
            makePrecondition(getHaContext().getMemberMap()),
            proposedLeader,
            proposedOffset,
            proposedEpoch,
            logicalclock.get(),
            getHaContext().getHaState().getCode(),
            getHaContext().getStoreConfig().getMySid());
    Iterator<Integer> it = getHaContext().getMemberMap().keySet().iterator();
    while (it.hasNext()) {
      Integer sid = it.next();
      if (sid.intValue() == getHaContext().getStoreConfig().getMySid()) {
        lookupQueue.put(vote);
      } else {
        HAMessage selfMessage = new HAMessage(HATransfer.VOTE, sid, vote);
        getHaMessageDispatcher().haMessageDelivery(sid, selfMessage);
      }
    }
  }

  private boolean totalOrderPredicate(
      int newSid, long newOffset, long newEpoch, int currSid, long currOffset, long currEpoch) {
    /*
     * We return true if one of the following three cases hold:
     * 1- New epoch is higher
     * 2- New epoch is the same as current epoch, but new offset is higher
     * 3- New epoch is the same as current epoch, new offset is the same
     *  as current offset, but server id is higher.
     */

    return ((newEpoch > currEpoch)
        || ((newEpoch == currEpoch)
            && ((newOffset > currOffset) || ((newOffset == currOffset) && (newSid > currSid)))));
  }

  private boolean validVoter(int sid) {
    return getHaContext().getMemberMap().containsKey(sid);
  }

  private int getInitSid() {
    if (getHaContext().getMemberMap().containsKey(getHaContext().getStoreConfig().getMySid())) {
      return getHaContext().getStoreConfig().getMySid();
    } else {
      return Integer.MIN_VALUE;
    }
  }

  private long getInitOffset() {
    return getHaContext().getMyberryStore().getLogicOffset();
  }

  private boolean termPredicate(Map<Integer, Vote> votes, Vote vote) {
    Set<Integer> verifySet = new HashSet<>();
    for (Map.Entry<Integer, Vote> entry : votes.entrySet()) {
      if (vote.equals(entry.getValue())
          && getHaContext().getMemberMap().containsKey(entry.getKey())) {
        verifySet.add(entry.getKey());
      }
    }

    return verifySet.size() > (getHaContext().getMemberMap().size() / 2);
  }

  private void leaveInstance(Vote v) {
    currentVote = v;
    lookupQueue.clear();
    log.info("===================================================================================");
    log.info(
        "About to leave FE instance: leader={}, offset=0x{}, my sid={}, my state={}",
        v.getLeader(),
        Long.toHexString(v.getOffset()),
        getHaContext().getStoreConfig().getMySid(),
        getHaContext().getHaState());
    log.info("===================================================================================");
  }

  /**
   * In the case there is a leader elected, and a quorum supporting this leader, we have to check if
   * the leader has voted and acked that it is leading. We need this check to avoid that peers keep
   * electing over and over a peer that has crashed and it is no longer leading.
   *
   * @param votes set of votes
   * @param leader leader id
   * @param electionEpoch epoch id
   */
  private boolean checkLeader(Map<Integer, Vote> votes, int leader, long electionEpoch) {

    boolean predicate = true;

    /*
     * If everyone else thinks I'm the leader, I must be the leader.
     * The other two checks are just for the case in which I'm not the
     * leader. If I'm not the leader and I haven't received a message
     * from leader stating that it is leading, then predicate is false.
     */

    if (leader != getHaContext().getStoreConfig().getMySid()) {
      if (votes.get(leader) == null) {
        predicate = false;
      } else if (votes.get(leader).getHaState() != HAState.LEADING.getCode()) {
        predicate = false;
      }
    } else if (logicalclock.get() != electionEpoch) {
      predicate = false;
    }

    return predicate;
  }

  private void updateProposal(int leader, long offset, long epoch) {
    proposedLeader = leader;
    proposedOffset = offset;
    proposedEpoch = epoch;
  }

  private HAContext getHaContext() {
    return defaultQuorum.getHaService().getHaContext();
  }

  private HAMessageDispatcher getHaMessageDispatcher() {
    return defaultQuorum.getHaService().getHaMessageDispatcher();
  }
}
