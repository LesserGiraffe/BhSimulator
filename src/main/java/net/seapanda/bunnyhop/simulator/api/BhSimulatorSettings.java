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

import net.seapanda.bunnyhop.utility.concurrent.reference.ConcurrentBoolean;
import net.seapanda.bunnyhop.utility.concurrent.reference.ConcurrentDouble;
import net.seapanda.bunnyhop.utility.concurrent.reference.ConcurrentInteger;
import net.seapanda.bunnyhop.utility.concurrent.reference.ConcurrentString;

/**
 * BhSimulator の設定一式をまとめたクラス.
 *
 * @author K.Koike
 */
public class BhSimulatorSettings {

  /** コンストラクタ. */
  public BhSimulatorSettings() {
    language = new ConcurrentString("Japanese");
    ui = new Ui();
    window = new Window();
    closeOnRequested = new ConcurrentBoolean(false);
    foregroundFps = new ConcurrentInteger(60);
    focusOnSimulatorChange = new ConcurrentBoolean(true);
  }

  /** コピーコンストラクタ. */
  public BhSimulatorSettings(BhSimulatorSettings org) {
    language = new ConcurrentString(org.language.get());
    ui = new Ui(org.ui);
    window = new Window(org.window);
    closeOnRequested = new ConcurrentBoolean(org.closeOnRequested.get());
    foregroundFps = new ConcurrentInteger(org.foregroundFps.get());
    focusOnSimulatorChange = new ConcurrentBoolean(org.focusOnSimulatorChange.get());
  }

  public final ConcurrentString language;
  public final Ui ui;
  public final Window window;

  /** 閉じるボタンでシミュレータを終了するかどうか. */
  public final ConcurrentBoolean closeOnRequested;
  /** シミュレータ画面の 1 秒あたりの描画回数. */
  public final ConcurrentInteger foregroundFps;
  /** BhSimulator に変化があったとき BhSimulator をフォーカスするかどうか. */
  public final ConcurrentBoolean focusOnSimulatorChange;


  /** UI 関連のパラメータ. */
  public static class Ui {

    /** コンストラクタ. */
    public Ui() {
      scale = new ConcurrentDouble(1.0f);
      x2SkinDpiThreshold = new ConcurrentDouble(122);
    }

    /** コピーコンストラクタ. */
    public Ui(Ui org) {
      scale = new ConcurrentDouble(org.scale.get());
      x2SkinDpiThreshold = new ConcurrentDouble(org.x2SkinDpiThreshold.get());
    }

    /** UI コンポーネントの大きさの倍率. */
    public final ConcurrentDouble scale;
    /** {@link com.badlogic.gdx.Graphics#getDensity} の戻り値がこの値を超えたとき, 2 倍の大きさの UI スキンを使う. */
    public final ConcurrentDouble x2SkinDpiThreshold;
  }

  /** ウィンドウ関連のパラメータ. */
  public static class Window {

    /** コンストラクタ. */
    public Window() {
      width = new ConcurrentInteger(Integer.MIN_VALUE);
      height = new ConcurrentInteger(Integer.MIN_VALUE);
      posX = new ConcurrentInteger(Integer.MIN_VALUE);
      posY = new ConcurrentInteger(Integer.MIN_VALUE);
      maximized = new ConcurrentBoolean(false);
    }

    /** コピーコンストラクタ. */
    public Window(Window org) {
      width = new ConcurrentInteger(org.width.get());
      height = new ConcurrentInteger(org.height.get());
      posX = new ConcurrentInteger(org.posX.get());
      posY = new ConcurrentInteger(org.posY.get());
      maximized = new ConcurrentBoolean(org.maximized.get());
    }

    /** ウィンドウの幅. */
    public final ConcurrentInteger width;
    /** ウィンドウの高さ. */
    public final ConcurrentInteger height;
    /** ウィンドウの X 座標. */
    public final ConcurrentInteger posX;
    /** ウィンドウの Y 座標. */
    public final ConcurrentInteger posY;
    /** ウィンドウが最大化されていたかどうか. */
    public final ConcurrentBoolean maximized;
  }
}
