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

package net.seapanda.bunnyhop.simulator.api;

import java.nio.file.Paths;
import net.seapanda.bunnyhop.simulator.BhSimulatorImpl;
import net.seapanda.bunnyhop.simulator.common.TextDefs;
import net.seapanda.bunnyhop.utility.textdb.JsonTextDatabase;

/**
 * シミュレータを作成する機能を提供するクラス.
 *
 * <p>シミュレータが使用するテキストはプロセス全体で共有するので,
 * シミュレータを作成する前に {@link #initializeTextDb} を呼んでテキストデータベースを初期化すること.
 *
 * @author K.Koike
 */
public class BhSimulatorFactory {

  /**
   * テキストデータをファイルから読み込んで,
   * シミュレータ全体で共有するテキストデータベースを初期化する.
   *
   * @param textDataFilePath テキストデータを格納したファイルのパス.
   * @throws Exception テキストデータベースの構築に失敗したとき
   */
  public static void initializeTextDb(String textDataFilePath) throws Exception {
    final var textDb = new JsonTextDatabase(Paths.get(textDataFilePath));
    TextDefs.setTextDatabase(textDb);
  }

  /**
   * シミュレータを作成する.
   *
   * @param settings シミュレータの初期設定.
   *
   * @return 作成したシミュレータ
   * @throws SimulatorInitializationException シミュレータの作成に失敗した場合
   */
  public static BhSimulator create(BhSimulatorSettings settings)
      throws SimulatorInitializationException {
    try {
      return new BhSimulatorImpl(new BhSimulatorSettings(settings));
    } catch (Exception e) {
      throw new SimulatorInitializationException("Failed to create a simulator.", e);
    }
  }
}
