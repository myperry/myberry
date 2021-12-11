/*
 * Copyright (c) 2021 MyBerry. All rights reserved.
 * https://myberry.org/
 *
 * Modified by Apache RocketMQ.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.myberry.remoting.common;

import io.netty.channel.Channel;

import java.net.*;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemotingHelper {

  private static final Logger log = LoggerFactory.getLogger(LoggerName.REMOTING_LOGGER_NAME);

  public static String exceptionSimpleDesc(final Throwable e) {
    StringBuffer sb = new StringBuffer();
    if (e != null) {
      sb.append(e.toString());

      StackTraceElement[] stackTrace = e.getStackTrace();
      if (stackTrace != null && stackTrace.length > 0) {
        StackTraceElement elment = stackTrace[0];
        sb.append(", ");
        sb.append(elment.toString());
      }
    }

    return sb.toString();
  }

  public static String getPhyLocalAddress() {
    try {
      Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
      InetAddress ip = null;
      while (allNetInterfaces.hasMoreElements()) {
        NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
        if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
          continue;
        } else {
          Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
          while (addresses.hasMoreElements()) {
            ip = addresses.nextElement();
            if (ip != null && ip instanceof Inet4Address) {
              return ip.getHostAddress();
            }

            /*
             * if (ip != null && ip instanceof Inet6Address) { return ip.getHostAddress(); }
             */
          }
        }
      }
    } catch (Exception e) {
      log.error("get physical address exception: ", e);
    }
    return "";
  }

  public static SocketAddress string2SocketAddress(final String addr) {
    String[] s = addr.split(":");
    InetSocketAddress isa = new InetSocketAddress(s[0], Integer.parseInt(s[1]));
    return isa;
  }

  public static String makeStringAddress(final String ip, final int port) {
    StringBuilder addr = new StringBuilder();
    addr.append(ip);
    addr.append(":");
    addr.append(port);
    return addr.toString();
  }

  public static String getAddressIP(final String addr) {
    if (addr == null || "".equals(addr)) {
      return "";
    }
    return addr.split(":")[0];
  }

  public static int getAddressPort(final String addr) {
    if (addr == null || "".equals(addr)) {
      return -1;
    }
    return Integer.parseInt(addr.split(":")[1]);
  }

  public static String parseChannelRemoteAddr(final Channel channel) {
    if (null == channel) {
      return "";
    }
    SocketAddress remote = channel.remoteAddress();
    final String addr = remote != null ? remote.toString() : "";

    if (addr.length() > 0) {
      int index = addr.lastIndexOf("/");
      if (index >= 0) {
        return addr.substring(index + 1);
      }

      return addr;
    }

    return "";
  }

  public static String parseSocketAddressAddr(SocketAddress socketAddress) {
    if (socketAddress != null) {
      final String addr = socketAddress.toString();

      if (addr.length() > 0) {
        return addr.substring(1);
      }
    }
    return "";
  }
}
