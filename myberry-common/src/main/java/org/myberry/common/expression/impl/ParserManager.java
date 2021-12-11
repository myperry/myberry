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

import org.myberry.common.expression.ParserChain;
import org.myberry.common.expression.exception.ParseExpressionException;
import org.myberry.common.expression.parser.DynamicParser;
import org.myberry.common.expression.parser.IncrParser;
import org.myberry.common.expression.parser.LetterParser;
import org.myberry.common.expression.parser.NumberParser;
import org.myberry.common.expression.parser.RandomParser;
import org.myberry.common.expression.parser.SidParser;
import org.myberry.common.expression.parser.TimeParser;

public class ParserManager {

  private final ParserChain chain;

  private static class SingletonHolder {
    private static final ParserManager INSTANCE = new ParserManager();
  }

  private ParserManager() {
    this.chain = new ParserChain();
  }

  public static ParserManager getInstance() {
    return SingletonHolder.INSTANCE;
  }

  public void registerParser() {
    chain
        .addParser(new NumberParser()) //
        .addParser(new LetterParser()) //
        .addParser(new TimeParser()) //
        .addParser(new RandomParser()) //
        .addParser(new SidParser()) //
        .addParser(new IncrParser()) //
        .addParser(new DynamicParser());
  }

  public void unRegisterParser() {
    chain.removeAllParser();
  }

  public boolean parseExpression(ExpressionString expressionString)
      throws ParseExpressionException {
    return ExpressionParser.match(expressionString.toString(), chain);
  }

  public boolean parseExpression(ExpressionObject expressionObject)
      throws ParseExpressionException {
    return ExpressionParser.match(expressionObject.toString(), chain);
  }
}
