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

import org.myberry.common.codec.MessageLite;
import org.myberry.common.codec.annotation.SerialField;

public class Precondition implements MessageLite {

  @SerialField(ordinal = 0)
  private Member[] members;

  @SerialField(ordinal = 1)
  private int blockFileSize;

  @SerialField(ordinal = 2)
  private int maxSyncDataSize;

  /**
   * The number of the same members in the self members and the other members is greater than the
   * number of different members in the other members, and is greater than half of the number of all
   * members in the self members. At this point, the other view is trusted.
   *
   * @param self
   * @param other
   * @return
   */
  public static boolean isAccepted(Precondition self, Precondition other) {
    Member[] myMembers = self.getMembers();
    Member[] anotherMembers = other.getMembers();

    int same = 0;
    int notSame = 0;
    for (int i = 0; i < anotherMembers.length; i++) {
      for (int j = 0; j < myMembers.length; j++) {
        if (j < myMembers.length - 1) {
          if (anotherMembers[i].equals(myMembers[j])) {
            same += 1;
            break;
          }
        } else {
          if (anotherMembers[i].equals(myMembers[j])) {
            same += 1;
          } else {
            notSame += 1;
          }
        }
      }
    }

    if (same > notSame && same > (myMembers.length / 2)) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean isSameSyncSettings(Precondition self, Precondition other) {
    return self.getBlockFileSize() == other.getBlockFileSize()
        && self.getMaxSyncDataSize() == other.getMaxSyncDataSize();
  }

  public static boolean check(Precondition self, Precondition other) {
    return isAccepted(self, other) && isSameSyncSettings(self, other);
  }

  public Member[] getMembers() {
    return members;
  }

  public void setMembers(Member[] members) {
    this.members = members;
  }

  public int getBlockFileSize() {
    return blockFileSize;
  }

  public void setBlockFileSize(int blockFileSize) {
    this.blockFileSize = blockFileSize;
  }

  public int getMaxSyncDataSize() {
    return maxSyncDataSize;
  }

  public void setMaxSyncDataSize(int maxSyncDataSize) {
    this.maxSyncDataSize = maxSyncDataSize;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Precondition [");
    builder.append("members={");

    if (null != members) {
      for (int i = 0; i < members.length; i++) {
        builder.append(members[i]);
        if (i < members.length - 1) {
          builder.append(", ");
        }
      }
    }

    builder.append("}, blockFileSize=");
    builder.append(blockFileSize);
    builder.append(", maxSyncDataSize=");
    builder.append(maxSyncDataSize);
    builder.append(']');
    return builder.toString();
  }
}
