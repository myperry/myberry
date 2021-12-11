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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.myberry.common.expression.parser.TimeParser;
import org.myberry.common.strategy.StrategyDate;
import org.myberry.server.expression.handler.TimeHandler;

public class DateUtils {

  public static int isIncludeTime(String expression) {
    int indexOf = expression.indexOf(TimeParser.TIME_DAY);
    if (indexOf > -1) {
      return StrategyDate.TIME_DAY;
    }

    indexOf = expression.indexOf(TimeParser.TIME_MONTH);
    if (indexOf > -1) {
      return StrategyDate.TIME_MONTH;
    }

    indexOf = expression.indexOf(TimeParser.TIME_YEAR);
    if (indexOf > -1) {
      return StrategyDate.TIME_YEAR;
    }

    return StrategyDate.NON_TIME;
  }

  public static String convertTime(int type, long time) {

    DateTimeFormatter format;
    switch (type) {
      case StrategyDate.TIME_DAY:
        format = DateTimeFormatter.ofPattern(TimeHandler.DAY_PATTERN);
        break;

      case StrategyDate.TIME_MONTH:
        format = DateTimeFormatter.ofPattern(TimeHandler.MONTH_PATTERN);
        break;

      case StrategyDate.TIME_YEAR:
        format = DateTimeFormatter.ofPattern(TimeHandler.YEAR_PATTERN);
        break;

      default:
        return "";
    }

    return Instant.ofEpochMilli(time)
        .atZone(ZoneId.of(ZoneId.systemDefault().getId()))
        .toLocalDateTime()
        .format(format);
  }
}
