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

import java.util.concurrent.Future;
import net.seapanda.bunnyhop.utility.event.ConsumerInvoker;
import net.seapanda.bunnyhop.utility.reference.WritableBoolean;

/**
 * シミュレータの機能を規定したインタフェース.
 *
 * @author K.Koike
 */
public interface BhSimulator {

  /**
   * シミュレータを起動する.
   *
   * <p>シミュレータの起動完了は {@link #waitForInitialization} で確認できる.
   */
  void start();

  /**
   * シミュレータを別のスレッドで起動する.
   *
   * @return シミュレータを実行するタスクの {@link Future} オブジェクト.
   */
  Future<?> startAsync();

  /**
   * シミュレータを別のスレッドで起動し, 起動が完了するのを待つ.
   *
   * @param timeout 起動完了を待つタイムアウト時間 (秒).
   * @return シミュレータを実行するタスクの {@link Future} オブジェクト
   * @throws SimulatorInitializationException
   *     起動完了前にタイムアウトした場合, または起動に失敗した場合.
   *     この例外を投げる前にシミュレータを停止するので, 呼び出し側での後処理は不要.
   */
  Future<?> startAsync(int timeout) throws SimulatorInitializationException;

  /**
   * シミュレータを停止させる.
   *
   * <p>シミュレータの起動完了前に呼んだ場合, 起動完了後に停止する.
   */
  void halt();

  /**
   * シミュレータの起動が完了するを待つ.
   *
   * @param timeout タイムアウト時間 (秒).
   * @return 起動完了後にコントロールを返した場合 true
   */
  boolean waitForInitialization(int timeout);

  /**
   * シミュレータの起動が完了するを待つ.
   *
   * @return 起動完了後にコントロールを返した場合 true
   */
  boolean waitForInitialization();

  /**
   * シミュレータのウィンドウを操作するためのオブジェクトを取得する.
   *
   * <p>シミュレータの起動完了前に呼んだ場合の動作は不定.
   *
   * @return シミュレータのウィンドウを操作するためのオブジェクト
   */
  SimulatorWindow getWindow();

  /**
   * シミュレータ制御用コマンドを処理するオブジェクトを取得する.
   *
   * <p>シミュレータの起動完了前に呼んだ場合の動作は不定.
   *
   * @return シミュレータのウィンドウに対する操作を提供するオブジェクト
   */
  SimulatorCmdProcessor getCmdProcessor();

  /**
   * このシミュレータの現在の設定を返す.
   *
   * <p>シミュレータの起動完了前に呼んだ場合の動作は不定.
   *
   * @return このシミュレータの現在の設定
   */
  BhSimulatorSettings getCurrentSettings();

  /** シミュレータの設定を行うためのオブジェクトを取得する. */
  Configuration getConfiguration();

  /**
   * このシミュレータに対するイベントハンドラの追加と削除を行うオブジェクトを返す.
   *
   * @return このシミュレータに対するイベントハンドラの追加と削除を行うオブジェクト
   */
  CallbackRegistry getCallbackRegistry();


  /** シミュレータの設定を行うための機能を規定したインタフェース. */
  interface Configuration {

    /** シミュレータに変更があったとき, ウィンドウにフォーカスを当てるかどうかを設定するプロパティを取得する. */
    WritableBoolean focusOnSimulatorChange();
  }


  /** シミュレータに対するイベントハンドラの登録および削除操作を規定したインタフェース. */
  interface CallbackRegistry {

    /** キーが押されたときのイベントハンドラのレジストリを取得する. */
    ConsumerInvoker<KeyboardKey>.Registry getOnKeyPressed();
  }
}
