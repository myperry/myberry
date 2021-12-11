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
package org.myberry.client.admin;

import org.myberry.client.AbstractMyberryClient;
import org.myberry.client.exception.MyberryClientException;
import org.myberry.client.exception.MyberryServerException;
import org.myberry.client.impl.admin.DefaultAdminClientImpl;
import org.myberry.common.Component;
import org.myberry.common.protocol.body.admin.ComponentKeyData;
import org.myberry.common.protocol.body.admin.NSComponentData;
import org.myberry.common.protocol.body.admin.RouteData;
import org.myberry.remoting.exception.RemotingException;

/** This class is the entry point for applications intending to manage component. */
public class DefaultAdminClient extends AbstractMyberryClient implements AdminClient {

  protected final transient DefaultAdminClientImpl defaultAdminClientImpl;

  /** Administrator client authentication */
  private String password;
  /** The group has DefaultAdminClient and DefaultUserClient. */
  private String clientGroup;
  /** Timeout for sending messages. */
  private int sendMsgTimeout = 3000;

  /** Default constructor. */
  public DefaultAdminClient() {
    this.defaultAdminClientImpl = new DefaultAdminClientImpl(this);
  }

  /**
   * Start this AdminClient instance. <strong> Much internal initializing procedures are carried out
   * to make this instance prepared, thus, it's a must to invoke this method before sending or
   * querying information. </strong>
   *
   * @throws MyberryClientException if there is any unexpected error.
   */
  @Override
  public void start() throws MyberryClientException {
    this.defaultAdminClientImpl.start();
  }

  /** This method shuts down this AdminClient instance and releases related resources. */
  @Override
  public void shutdown() {
    defaultAdminClientImpl.shutdown();
  }

  /**
   * Create component.
   *
   * @param component component to send.
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the component, {@link SendStatus} indicating component status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult createComponent(Component component)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.createComponent(
        password, component.getStructure(), component.encode());
  }

  /**
   * Same to {@link #createComponent(Component)} with send timeout specified in addition.
   *
   * @param component component to send.
   * @param timeout send timeout.
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the component, {@link SendStatus} indicating component status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult createComponent(Component component, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.createComponent(
        password, component.getStructure(), component.encode(), timeout);
  }

  /**
   * Update NS component.
   *
   * @param component NS component to be sent.
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the component, {@link SendStatus} indicating component status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult updateComponent(NSComponentData component)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.updateComponent(
        password, component.getStructure(), component.encode());
  }

  /**
   * Same to {@link #updateComponent(NSComponentData)} with send timeout specified in addition.
   *
   * @param component NS component to be sent.
   * @param timeout send timeout.
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the component, {@link SendStatus} indicating component status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult updateComponent(NSComponentData component, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.updateComponent(
        password, component.getStructure(), component.encode(), timeout);
  }

  /**
   * Query component size.
   *
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the component, {@link SendStatus} indicating component status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult queryComponentSize()
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.queryComponentSize(password);
  }

  /**
   * Same to {@link #queryComponentSize()} with send timeout specified in addition.
   *
   * @param timeout send timeout.
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the component, {@link SendStatus} indicating component status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult queryComponentSize(long timeout)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.queryComponentSize(password, timeout);
  }

  /**
   * Query component by key.
   *
   * @param key component key.
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the component, {@link SendStatus} indicating component status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult queryComponentByKey(String key)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.queryComponentByKey(
        password, new ComponentKeyData(key).encode());
  }

  /**
   * Same to {@link #queryComponentByKey(String)} with send timeout specified in addition.
   *
   * @param key component key.
   * @param timeout send timeout.
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the component, {@link SendStatus} indicating component status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult queryComponentByKey(String key, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.queryComponentByKey(
        password, new ComponentKeyData(key).encode(), timeout);
  }

  /**
   * Query cluster list.
   *
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the routing, {@link SendStatus} indicating cluster status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult queryClusterList()
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.queryClusterList(password);
  }

  /**
   * Same to {@link #queryClusterList()} with send timeout specified in addition.
   *
   * @param timeout send timeout.
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the routing, {@link SendStatus} indicating cluster status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult queryClusterList(long timeout)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.queryClusterList(password, timeout);
  }

  /**
   * Kick out invoker.
   *
   * @param kickedSid kicked sid.
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the routing, {@link SendStatus} indicating cluster status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult kickOutInvoker(int kickedSid)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.kickOutInvoker(password, new RouteData(kickedSid).encode());
  }

  /**
   * Same to {@link #kickOutInvoker(int)} with send timeout specified in addition.
   *
   * @param kickedSid kicked sid.
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the routing, {@link SendStatus} indicating cluster status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult kickOutInvoker(int kickedSid, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.kickOutInvoker(
        password, new RouteData(kickedSid).encode(), timeout);
  }

  /**
   * Remove the kicked invoker from the cluster list.
   *
   * @param kickedSid kicked sid.
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the routing, {@link SendStatus} indicating cluster status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult removeInvoker(int kickedSid)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.removeInvoker(password, new RouteData(kickedSid).encode());
  }

  /**
   * Same to {@link #removeInvoker(int)} with send timeout specified in addition.
   *
   * @param kickedSid kicked sid.
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the routing, {@link SendStatus} indicating cluster status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult removeInvoker(int kickedSid, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.removeInvoker(
        password, new RouteData(kickedSid).encode(), timeout);
  }

  /**
   * Update invoker weight.
   *
   * @param sid sid.
   * @param weight routing weight.
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the routing, {@link SendStatus} indicating cluster status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult updateWeight(int sid, int weight)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.updateWeight(password, new RouteData(sid, weight).encode());
  }

  /**
   * Same to {@link #updateWeight(int, int)} with send timeout specified in addition.
   *
   * @param sid sid.
   * @param weight routing weight.
   * @return {@link SendResult} instance to inform senders details of the deliverable, say result of
   *     the routing, {@link SendStatus} indicating cluster status, etc.
   * @throws RemotingException if there is any network-tier error.
   * @throws MyberryServerException if there is any error with broker.
   * @throws InterruptedException if the sending thread is interrupted.
   */
  @Override
  public SendResult updateWeight(int sid, int weight, long timeout)
      throws RemotingException, InterruptedException, MyberryServerException {
    return this.defaultAdminClientImpl.updateWeight(
        password, new RouteData(sid, weight).encode(), timeout);
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setClientGroup(String clientGroup) {
    this.clientGroup = clientGroup;
  }

  public int getSendMsgTimeout() {
    return sendMsgTimeout;
  }

  public void setSendMsgTimeout(int sendMsgTimeout) {
    this.sendMsgTimeout = sendMsgTimeout;
  }

  @Override
  public String getClientGroup() {
    return clientGroup == null ? DefaultAdminClient.class.getSimpleName() : clientGroup;
  }
}
