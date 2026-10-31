/*
 * Copyright 2017 K.Koike
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

import net.seapanda.bunnyhop.utility.version.AppVersion;

/**
 * BhSimulator 自身の情報を取得する機能を規定したクラス.
 *
 * @author K.Koike
 */
public class BhSimulatorInfo {

  /** BhSimulatorInfo の名前. */
  private static final String NAME = "BhSimulatorImpl";
  /** BhSimulatorInfo のバージョン. */
  private static final AppVersion VERSION = AppVersion.of("bhsim-0.7.7");

  /**
   * BhSimulatorInfo の名前を取得する.
   *
   * @return BhSimulatorInfo の名前
   */
  public static String getName() {
    return NAME;
  }

  /**
   * BhSimulatorInfo のバージョンを取得する.
   *
   * @return BhSimulatorInfo のバージョン
   */
  public static AppVersion getVersion() {
    return VERSION;
  }

  private BhSimulatorInfo() {}
}
