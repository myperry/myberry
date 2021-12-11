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

import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ServerUtils {

  public static Options buildCommandlineOptions(final Options options) {
    Option opt = new Option("h", "help", false, "Print help");
    opt.setRequired(false);
    options.addOption(opt);

    opt = new Option("v", "printVersion", false, "Print myberry version");
    opt.setRequired(false);
    options.addOption(opt);

    return options;
  }

  public static CommandLine parseCmdLine(
      final String appName, String[] args, Options options, CommandLineParser parser) {
    HelpFormatter hf = new HelpFormatter();
    hf.setWidth(110);
    CommandLine commandLine = null;
    try {
      commandLine = parser.parse(options, args);
      if (commandLine.hasOption('h')) {
        hf.printHelp(appName, options, true);
        System.exit(0);
      }
    } catch (ParseException e) {
      hf.printHelp(appName, options, true);
      System.exit(1);
    }

    return commandLine;
  }

  public static Properties commandLine2Properties(final CommandLine commandLine) {
    Properties properties = new Properties();
    Option[] opts = commandLine.getOptions();

    if (opts != null) {
      for (Option opt : opts) {
        String name = opt.getLongOpt();
        String value = commandLine.getOptionValue(name);
        if (value != null) {
          properties.setProperty(name, value);
        }
      }
    }

    return properties;
  }
}
