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
package org.myberry.server.expression.handler;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.myberry.common.expression.parser.TimeParser;

public class TimeHandler implements PlaceholderHandler {

  public static final String DAY_PATTERN = "yyyyMMdd";
  public static final String MONTH_PATTERN = "yyyyMM";
  public static final String YEAR_PATTERN = "yyyy";

  private DateTimeFormatter format;

  @Override
  public void handle(String placeholder) {
    switch (placeholder) {
      case TimeParser.TIME_DAY:
        this.format = DateTimeFormatter.ofPattern(DAY_PATTERN);
        break;

      case TimeParser.TIME_MONTH:
        this.format = DateTimeFormatter.ofPattern(MONTH_PATTERN);
        break;

      case TimeParser.TIME_YEAR:
        this.format = DateTimeFormatter.ofPattern(YEAR_PATTERN);
        break;

      default:
        this.format = DateTimeFormatter.ofPattern(DAY_PATTERN);
        break;
    }
  }

  @Override
  public String get(String value) {
    long now = System.currentTimeMillis();
    return Instant.ofEpochMilli(now)
        .atZone(ZoneId.of(ZoneId.systemDefault().getId()))
        .toLocalDateTime()
        .format(format);
  }
}
