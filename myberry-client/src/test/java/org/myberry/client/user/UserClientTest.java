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
package org.myberry.client.user;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class UserClientTest {

  private static DefaultUserClient defaultUserClient;

  @BeforeClass
  public static void setup() throws Exception {
    defaultUserClient = new DefaultUserClient();
    defaultUserClient.setServerAddr("192.168.1.2:8085,192.168.1.2:8086,192.168.1.2:8087");
    defaultUserClient.start();
  }

  @Test
  public void getSyncForCR() throws Exception {
    HashMap<String, String> map = new HashMap<>();
    map.put("hello", "AAA");
    map.put("world", "BBB");
    PullResult pull = defaultUserClient.pull("key1", map);
    System.out.println("Sync: " + pull);
    PullResult pull1 = defaultUserClient.pull("key1", map);
    System.out.println("Sync: " + pull1);
    PullResult pull2 = defaultUserClient.pull("key1", map);
    System.out.println("Sync: " + pull2);
    PullResult pull3 = defaultUserClient.pull("key1", map);
    System.out.println("Sync: " + pull3);
    PullResult pull4 = defaultUserClient.pull("key1", map);
    System.out.println("Sync: " + pull4);
    assertNotNull(pull.getNewId());
  }

  @Test
  public void getSyncTimesRetryForCR() throws Exception {
    PullResult pull = defaultUserClient.pull("key1", 3000, 2);
    System.out.println("Sync6: " + pull);
    assertNotNull(pull.getNewId());
    PullResult pull1 = defaultUserClient.pull("key1", 3000, 2);
    System.out.println("Sync6: " + pull1);
    PullResult pull2 = defaultUserClient.pull("key1", 3000, 2);
    System.out.println("Sync6: " + pull2);
    PullResult pull3 = defaultUserClient.pull("key1", 3000, 2);
    System.out.println("Sync6: " + pull3);
    PullResult pull4 = defaultUserClient.pull("key1", 3000, 2);
    System.out.println("Sync6: " + pull4);
    PullResult pull5 = defaultUserClient.pull("key1", 3000, 2);
    System.out.println("Sync6: " + pull5);
  }

  @Test
  public void beComplicatedByForCR() throws Exception {
    long start = System.currentTimeMillis();

    for (int i = 0; i < 10; i++) {
      // Thread.sleep(50);
      PullResult pull = defaultUserClient.pull("key1");
      System.out.println(pull);
    }
    System.out.println("Total time: " + (System.currentTimeMillis() - start));
  }

  @Test
  public void getSyncWithSessionKeyForCR() throws Exception {
    HashMap<String, String> map = new HashMap<>();
    map.put("hello", "FF");
    PullResult pull = defaultUserClient.pull("key1", map, "abc");
    System.out.println("Sync: " + pull);
    PullResult pull1 = defaultUserClient.pull("key1", map, "abc");
    System.out.println("Sync: " + pull1);
    HashMap<String, String> map1 = new HashMap<>();
    map1.put("hello", "KK");
    PullResult pull2 = defaultUserClient.pull("key1", map1, "def");
    System.out.println("Sync: " + pull2);
    PullResult pull3 = defaultUserClient.pull("key1", map1, "def");
    System.out.println("Sync: " + pull3);
  }

  @Test
  public void getSyncTimesRetryWithSessionKeyForCR() throws Exception {
    HashMap<String, String> map = new HashMap<>();
    map.put("hello", "FF");
    PullResult pull = defaultUserClient.pull("key1", map, "abc", 3000, 2);
    System.out.println("Sync9: " + pull);
    assertNotNull(pull.getNewId());
    PullResult pull1 = defaultUserClient.pull("key1", map, "abc", 3000, 2);
    System.out.println("Sync9: " + pull1);
    PullResult pull2 = defaultUserClient.pull("key1", map, "abc", 3000, 2);
    System.out.println("Sync9: " + pull2);
    HashMap<String, String> map1 = new HashMap<>();
    map1.put("hello", "KK");
    PullResult pull3 = defaultUserClient.pull("key1", map1, "def", 3000, 2);
    System.out.println("Sync9: " + pull3);
    PullResult pull4 = defaultUserClient.pull("key1", map1, "def", 3000, 2);
    System.out.println("Sync9: " + pull4);
    PullResult pull5 = defaultUserClient.pull("key1", map1, "def", 3000, 2);
    System.out.println("Sync9: " + pull5);
  }

  @Test
  public void getAsyncTimesRetryForCR() throws Exception {
    defaultUserClient.pull(
        "key1",
        new PullCallback() {

          @Override
          public void onSuccess(PullResult pullResult) {
            System.out.println("Async: " + pullResult);
            assertNotNull(pullResult.getNewId());
          }

          @Override
          public void onException(Throwable e) {
            e.printStackTrace();
          }
        },
        3000,
        2);

    Thread.sleep(3000);
  }

  @Test
  public void getAsyncTimesRetryWithSessionKeyForCR() throws Exception {
    for (int i = 0; i < 3; i++) {
      HashMap<String, String> map = new HashMap<>();
      map.put("hello", "FF");
      defaultUserClient.pull(
          "key1",
          map,
          new PullCallback() {

            @Override
            public void onSuccess(PullResult pullResult) {
              System.out.println("Async5: " + pullResult);
            }

            @Override
            public void onException(Throwable e) {
              e.printStackTrace();
            }
          },
          "abc",
          3000,
          2);
      HashMap<String, String> map1 = new HashMap<>();
      map1.put("hello", "KK");
      defaultUserClient.pull(
          "key1",
          map1,
          new PullCallback() {

            @Override
            public void onSuccess(PullResult pullResult) {
              System.out.println("Async5: " + pullResult);
              assertNotNull(pullResult.getNewId());
            }

            @Override
            public void onException(Throwable e) {
              e.printStackTrace();
            }
          },
          "def",
          99999,
          2);
    }
    Thread.sleep(3000);
  }

  @Test
  public void getSyncForNS() throws Exception {
    PullResult pull = defaultUserClient.pull("key2");
    System.out.println("Sync: " + pull);
    assertNotNull(pull.getStart());
    assertNotNull(pull.getEnd());
    assertNotNull(pull.getSynergyId());
  }

  @Test
  public void getAsyncTimesRetryForNS() throws Exception {
    defaultUserClient.pull(
        "key2",
        new PullCallback() {

          @Override
          public void onSuccess(PullResult pullResult) {
            System.out.println("Async: " + pullResult);
            assertNotNull(pullResult.getStart());
            assertNotNull(pullResult.getEnd());
            assertNotNull(pullResult.getSynergyId());
          }

          @Override
          public void onException(Throwable e) {
            e.printStackTrace();
          }
        },
        3000,
        2);

    Thread.sleep(3000);
  }

  @Test
  public void getSyncTimesRetryWithSessionKeyForNS() throws Exception {
    PullResult pull = defaultUserClient.pull("key2", "abc", 3000, 2);
    System.out.println("Sync9: " + pull);
    PullResult pull1 = defaultUserClient.pull("key2", "abc", 3000, 2);
    System.out.println("Sync9: " + pull1);
    PullResult pull2 = defaultUserClient.pull("key2", "abc", 3000, 2);
    System.out.println("Sync9: " + pull2);
    PullResult pull3 = defaultUserClient.pull("key2", "def", 3000, 2);
    System.out.println("Sync9: " + pull3);
    PullResult pull4 = defaultUserClient.pull("key2", "def", 3000, 2);
    System.out.println("Sync9: " + pull4);
    PullResult pull5 = defaultUserClient.pull("key2", "def", 3000, 2);
    System.out.println("Sync9: " + pull5);
  }

  @Test
  public void getAsyncTimesRetryWithSessionKeyForNS() throws Exception {
    for (int i = 0; i < 3; i++) {
      defaultUserClient.pull(
          "key2",
          new PullCallback() {

            @Override
            public void onSuccess(PullResult pullResult) {
              System.out.println("Async5: " + pullResult);
            }

            @Override
            public void onException(Throwable e) {
              e.printStackTrace();
            }
          },
          "abc",
          3000,
          2);
      defaultUserClient.pull(
          "key2",
          new PullCallback() {

            @Override
            public void onSuccess(PullResult pullResult) {
              System.out.println("Async5: " + pullResult);
            }

            @Override
            public void onException(Throwable e) {
              e.printStackTrace();
            }
          },
          "def",
          3000,
          2);
    }
    Thread.sleep(3000);
  }

  @AfterClass
  public static void close() {
    defaultUserClient.shutdown();
  }
}
