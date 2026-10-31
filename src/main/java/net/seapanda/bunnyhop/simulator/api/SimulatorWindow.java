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

import net.seapanda.bunnyhop.utility.math.Vec2D;

/**
 * シミュレータのウィンドウに対する操作を規定したインタフェース.
 *
 * @author  K.Koike
 */
public interface SimulatorWindow {

  /**
   * ウィンドウの位置を設定する.
   *
   * @param x 設定するウィンドウの左上の X 座標 (スクリーン座標系, ピクセル)
   * @param y 設定するウィンドウの左上の Y 座標 (スクリーン座標系, ピクセル)
   */
  void setPosition(int x, int y);

  /**
   * ウィンドウの X 座標を取得する.
   *
   * @return ウィンドウの左上の X 座標 (スクリーン座標系, ピクセル)
   */
  int getPositionX();

  /**
   * ウィンドウの Y 座標を取得する.
   *
   * @return ウィンドウの左上の Y 座標 (スクリーン座標系, ピクセル)
   */
  int getPositionY();

  /**
   * ウィンドウの位置を取得する.
   *
   * @return ウィンドウの左上の座標 (スクリーン座標系, ピクセル)
   */
  Vec2D getPosition();

  /**
   * ウィンドウの大きさを設定する.
   *
   * @param width 設定するウィンドウの幅 (ピクセル)
   * @param height 設定するウィンドウの高さ (ピクセル)
   */
  void setSize(int width, int height);

  /**
   * ウィンドウの幅を取得する.
   *
   * @return ウィンドウの幅 (ピクセル)
   */
  int getWidth();

  /**
   * ウィンドウの高さを取得する.
   *
   * @return ウィンドウの高さ (ピクセル)
   */
  int getHeight();

  /**
   * ウィンドウの大きさを取得する.
   *
   * @return ウィンドウの大きさ (ピクセル). x が幅, y が高さを表す.
   */
  Vec2D getSize();

  /** ウィンドウをアイコン化する. */
  void iconify();

  /**
   * ウィンドウがアイコン化されているかどうかを調べる.
   *
   * @return ウィンドウがアイコン化されている場合 true
   */
  boolean isIconified();

  /** ウィンドウをアイコン化または最大化されていない状態に戻す. */
  void restore();

  /** ウィンドウを最大化する. */
  void maximize();

  /**
   * ウィンドウが最大化されているかどうかを調べる.
   *
   * @return ウィンドウが最大化されている場合 true
   */
  boolean isMaximized();

  /** ウィンドウにフォーカスを当てる. */
  void focus();

  /**
   * ウィンドウにフォーカスが当たっているかどうかを調べる.
   *
   * @return ウィンドウにフォーカスが当たっている場合 true
   */
  boolean isFocused();
}
