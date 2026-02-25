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

package net.seapanda.bunnyhop.simulator.common;

/**
 * BhSimulator の設定一式をまとめたクラス.
 *
 * @author K.Koike
 */
public class BhSimSettings {

  public static volatile String language = "Japanese";

  /** UI 関連のパラメータ. */
  public static class Ui {
    /** UI コンポーネントの大きさの倍率. */
    public static volatile float scale = 1.0f;
    public static volatile Window window = new Window();
  }

  /** ウィンドウ関連のパラメータ. */
  public static class Window {
    /** ウィンドウの幅. */
    public volatile int width = Integer.MIN_VALUE;
    /** ウィンドウの高さ. */
    public volatile int height = Integer.MIN_VALUE;
    /** ウィンドウの X 座標. */
    public volatile int posX = Integer.MIN_VALUE;
    /** ウィンドウの Y 座標. */
    public volatile int posY = Integer.MIN_VALUE;
    /** ウィンドウが最大化されていたかどうか. */
    public volatile boolean maximized = false;
  }
}
