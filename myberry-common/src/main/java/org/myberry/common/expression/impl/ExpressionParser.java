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
package org.myberry.common.expression.impl;

import java.util.ArrayList;
import java.util.List;

import org.myberry.common.expression.ParserChain;
import org.myberry.common.expression.exception.ParseExpressionException;
import org.myberry.common.expression.parser.IncrParser;
import org.myberry.common.expression.parser.SidParser;
import org.myberry.common.expression.parser.TimeParser;

public class ExpressionParser {

  public static boolean match(String expression) throws ParseExpressionException {
    if (expression.startsWith("[") && expression.endsWith("]")) {
      return true;
    } else {
      throw new ParseExpressionException("expression must start with '[' and end with ']'");
    }
  }

  public static boolean match(String expression, ParserChain chain)
      throws ParseExpressionException {
    boolean result = false;
    if (match(expression)) {
      boolean flag = true;

      String[] placeholders = split(expression);
      for (int i = 0; i < placeholders.length; i++) {
        boolean doParse = chain.doParse(placeholders[i]);
        flag &= doParse;
      }

      result |= flag;
      if (!result) {
        throw new ParseExpressionException("invalid expression: " + expression);
      }

      boolean incrIndexResult = getAbsCount(placeholders, IncrParser.PREFIX);
      boolean sidIndexResult = getAbsCount(placeholders, SidParser.PREFIX);
      boolean matchTimeResult = getTimeCount(placeholders);

      result &= incrIndexResult;
      result &= sidIndexResult;
      result &= matchTimeResult;
    }

    if (result) {
      return result;
    } else {
      throw new ParseExpressionException("invalid expression: " + expression);
    }
  }

  public static String[] split(String expression) {
    return expression.substring(1, expression.length() - 1).split(" ");
  }

  static boolean getTimeCount(String[] placeholders) {
    int count = 0;
    count = getCount(placeholders, TimeParser.TIME_DAY, count);
    count = getCount(placeholders, TimeParser.TIME_MONTH, count);
    count = getCount(placeholders, TimeParser.TIME_YEAR, count);

    if (count == 0 || count == 1) {
      return true;
    } else {
      return false;
    }
  }

  static int getCount(String[] placeholders, String compareStr, int count) {
    for (int i = 0; i < placeholders.length; i++) {
      if (compareStr.equals(placeholders[i])) {
        count++;
      }
    }
    return count;
  }

  static boolean getAbsCount(String[] placeholders, String compareStr) {
    List<Integer> count = getCount(placeholders, compareStr, new ArrayList<Integer>());
    return macth(count);
  }

  static List<Integer> getCount(String[] placeholders, String compareStr, List<Integer> list) {
    for (int i = 0; i < placeholders.length; i++) {
      if (placeholders[i].startsWith(compareStr)) {
        String num = placeholders[i].substring(compareStr.length(), placeholders[i].length() - 1);
        list.add(Integer.parseInt(num));
      }
    }
    return list;
  }

  static boolean macth(List<Integer> index) {
    if (index.size() <= 0) {
      return false;
    }

    boolean result = true;
    for (int i = 0; i < index.size(); i++) {
      boolean finded = false;
      for (int j = 0; j < index.size(); j++) {
        if (i == index.get(j).intValue()) {
          finded |= true;
          break;
        }
      }

      result &= finded;
      if (!finded) {
        break;
      }
    }
    return result;
  }
}
