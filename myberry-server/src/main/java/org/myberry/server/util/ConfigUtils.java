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
package org.myberry.server.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.myberry.common.CommentedProperties;
import org.myberry.common.annotation.ImportantField;
import org.myberry.common.annotation.WriteBack;
import org.myberry.server.ha.collect.NodeAddr;

public class ConfigUtils {

  public static void printConfig(boolean isImportant, Object... object) {
    for (Object obj : object) {
      Class<? extends Object> cls = obj.getClass();
      Field[] fields = cls.getDeclaredFields();
      Field.setAccessible(fields, true);
      for (Field field : fields) {
        if (isImportant) {
          boolean fieldHasAnno = field.isAnnotationPresent(ImportantField.class);
          if (fieldHasAnno) {
            try {
              System.out.println(field.getName() + ":" + field.get(obj));
            } catch (Exception e) {
            }
          }
        } else {
          try {
            System.out.println(field.getName() + ":" + field.get(obj));
          } catch (Exception e) {
          }
        }
      }
    }
  }

  public static void printVersion(Properties properties) {
    System.out.println("myberry version " + properties.getProperty("version"));
  }

  public static void writeToPropertiesFile(String fileName, Object... object) {
    CommentedProperties cp = new CommentedProperties();
    InputStream fis = null;
    OutputStream fos = null;
    try {
      fis = new FileInputStream(fileName);
      cp.load(fis);

      fos = new FileOutputStream(fileName);

      for (Object obj : object) {
        Class<? extends Object> cls = obj.getClass();
        Field[] fields = cls.getDeclaredFields();
        Field.setAccessible(fields, true);
        for (Field field : fields) {
          boolean annotationPresent = field.isAnnotationPresent(WriteBack.class);
          if (annotationPresent) {
            cp.setProperty(field.getName(), String.valueOf(field.get(obj)));
          }
        }
      }
      cp.store(fos);
    } catch (IOException e) {
    } catch (IllegalAccessException e) {
    } finally {
      if (null != fos) {
        try {
          fos.close();
        } catch (IOException e) {
        }
      }
      if (null != fis) {
        try {
          fis.close();
        } catch (IOException e) {
        }
      }
    }
  }

  public static Map<Integer, String> parseHAServerAddr(String haServerAddr) {
    String[] peers = haServerAddr.trim().split(",");
    Map<Integer, String> map = new HashMap<>(peers.length);
    for (String peer : peers) {
      String[] element = peer.split("@");
      map.put(Integer.parseInt(element[0].trim()), element[1].trim());
    }
    return map;
  }

  public static String generateHAServerAddr(NodeAddr[] clusterList) {
    Arrays.sort(clusterList);

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < clusterList.length; i++) {
      builder.append(clusterList[i].getSid());
      builder.append("@");
      builder.append(clusterList[i].getIp());
      builder.append(":");
      builder.append(clusterList[i].getHaPort());
      if (i < clusterList.length - 1) {
        builder.append(",");
      }
    }

    return builder.toString();
  }
}
