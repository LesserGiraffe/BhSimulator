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

import net.seapanda.bunnyhop.utility.textdb.JsonTextDatabase;
import net.seapanda.bunnyhop.utility.textdb.TextDatabase;
import net.seapanda.bunnyhop.utility.textdb.TextId;

/**
 * BhSimulator で使用するテキストを集めたクラス.
 *
 * @author K.Koike
 */
public class TextDefs {

  private static volatile TextDatabase db = new JsonTextDatabase("{}");

  /** テキストを取得する際に呼ぶメソッドのインタフェース. */
  @FunctionalInterface
  public interface Getter {
    /**
     * テキストを取得する.
     *
     * @param params 書式付き文字列の中に組み込むデータ
     */
    String get(Object... params);
  }

  /** テキストの取得元となるオブジェクトを設定する. */
  public static void setTextDatabase(TextDatabase db) {
    if (db == null) {
      return;
    }
    TextDefs.db = db;
  }

  /** オブジェクト操作 UI のテキスト. */
  public static class ObjCtrl {

    /** 色の名前. */
    public static class Color {
      public static Getter black = params -> db.get(
          TextId.of("obj-ctrl", "color", "black"), params);

      public static Getter red = params -> db.get(
          TextId.of("obj-ctrl", "color", "red"), params);

      public static Getter green = params -> db.get(
          TextId.of("obj-ctrl", "color", "green"), params);

      public static Getter blue = params -> db.get(
          TextId.of("obj-ctrl", "color", "blue"), params);

      public static Getter magenta = params -> db.get(
          TextId.of("obj-ctrl", "color", "magenta"), params);

      public static Getter cyan = params -> db.get(
          TextId.of("obj-ctrl", "color", "cyan"), params);

      public static Getter yellow = params -> db.get(
          TextId.of("obj-ctrl", "color", "yellow"), params);

      public static Getter white = params -> db.get(
          TextId.of("obj-ctrl", "color", "white"), params);
    }

    /** RaspiCar を操作する UI のテキスト. */
    public static class RaspiCar {
      public static Getter moveSpeed = params -> db.get(
          TextId.of("obj-ctrl", "raspi-car", "movement-speed"), params);

      public static Getter moveTime = params -> db.get(
          TextId.of("obj-ctrl", "raspi-car", "movement-time"), params);

      public static Getter leftEye = params -> db.get(
          TextId.of("obj-ctrl", "raspi-car", "left-eye"), params);

      public static Getter rightEye = params -> db.get(
          TextId.of("obj-ctrl", "raspi-car", "right-eye"), params);

      public static Getter bothEyes = params -> db.get(
          TextId.of("obj-ctrl", "raspi-car", "both-eyes"), params);
    }
  }

  /** カメラ操作説明のテキスト. */
  public static class CameraManual {
    public static Getter moveViewPoint = params -> db.get(
        TextId.of("camera-manual", "move-view-point"), params);
  }
}
