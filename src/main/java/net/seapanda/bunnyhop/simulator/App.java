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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.seapanda.bunnyhop.simulator.api.BhSimulator;
import net.seapanda.bunnyhop.simulator.api.BhSimulatorFactory;
import net.seapanda.bunnyhop.simulator.api.BhSimulatorInfo;
import net.seapanda.bunnyhop.simulator.api.BhSimulatorSettings;
import net.seapanda.bunnyhop.simulator.api.SimulatorInitializationException;
import net.seapanda.bunnyhop.utility.Utility;
import org.apache.commons.cli.CommandLine;
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
      System.out.println(BhSimulatorInfo.getVersion());
      return;
    }
    initializeTextDb();
    Path settingFilePath = Paths.get(Utility.execPath, "settings", "BhSimSettings.json");
    BhSimulatorSettings settings = importSettings(settingFilePath.toAbsolutePath().toString());
    BhSimulator simulator = BhSimulatorFactory.create(settings);
    simulator.start();
    exportSettings(settingFilePath.toAbsolutePath().toString(), simulator.getCurrentSettings());
  }

  private static void initializeTextDb() throws Exception {
    Path filePath = Paths.get(Utility.execPath, "language", "Japanese", "BhSimulator.json");
    BhSimulatorFactory.initializeTextDb(filePath.toAbsolutePath().toString());
  }

  private static BhSimulatorSettings importSettings(String configFilePath)
      throws SimulatorInitializationException {
    var gson = new Gson();
    try (var jr = gson.newJsonReader(new FileReader(configFilePath))) {
      return gson.fromJson(jr, BhSimulatorSettings.class);
    } catch (Exception e) {
      throw new SimulatorInitializationException(
          "Failed to import simulator settings.\n(%s)".formatted(configFilePath), e);
    }
  }

  private static void exportSettings(String filePath, BhSimulatorSettings settings) {
    var gson = new GsonBuilder().setPrettyPrinting().create();
    try {
      Path path = Paths.get(filePath).toAbsolutePath();
      Files.createDirectories(path.getParent());
      try (var jw = gson.newJsonWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
        gson.toJson(settings, new TypeToken<BhSimulatorSettings>(){}.getType(), jw);
      }
    } catch (IOException e) { /* Do nothing.*/ }
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

    var parser = new DefaultParser();
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
