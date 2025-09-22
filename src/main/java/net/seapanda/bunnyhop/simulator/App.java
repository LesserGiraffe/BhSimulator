/*
 * Copyright 2024 K.Koike
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.seapanda.bunnyhop.simulator;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import net.seapanda.bunnyhop.simulator.common.BhSimConstants;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * メインクラス.
 *
 * @author K.Koike
 */
public class App {

  /** メインメソッド. */
  public static void main(String[] args) throws Exception {
    var options = new Options();
    CommandLine cmd = parseCmd(args, options);
    if (cmd.hasOption("help")) {
      HelpFormatter hf = new HelpFormatter();
      hf.printHelp("[opts]", options);
      return;
    }
    if (cmd.hasOption("version")) {
      System.out.println(BhSimConstants.APP_VERSION.toString());
      return;
    }

    Lwjgl3WindowListener windowListener = new Lwjgl3WindowAdapter() {
      @Override
      public boolean closeRequested() {
        return true;
      }
    };
    var config = new Lwjgl3ApplicationConfiguration();
    config.setWindowListener(windowListener);
    config.setWindowedMode(800, 600);    
    new Lwjgl3Application(new BhSimulator(), config);
  }

  /** コマンドライン引数をパースする. */
  private static CommandLine parseCmd(String[] args, Options options) {
    options.addOption(Option.builder()
        .longOpt("version")
        .hasArg(false)
        .desc("Output the version of BhSimulator and exit.")
        .build());

    options.addOption(Option.builder()
        .longOpt("help")
        .hasArg(false)
        .desc("Print help about BhRuntime environment variables and exit.")
        .build());

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      String msg = "Invalid command-line arguments.\n%s".formatted(e);
      System.err.println(msg);
    }
    return cmd;
  }
}
