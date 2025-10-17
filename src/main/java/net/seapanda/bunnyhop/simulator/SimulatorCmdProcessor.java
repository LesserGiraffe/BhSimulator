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

import java.util.function.BiConsumer;
import net.seapanda.bunnyhop.utility.event.ConsumerInvoker;

/**
 * シミュレータ制御用コマンドを処理する機能を規定したインタフェース.
 */
public interface SimulatorCmdProcessor {

  /**
   * シミュレータ制御用コマンドを処理する.
   *
   * @param cmd 処理するコマンド
   * @param onCmdFinished コマンドの処理が終了したときに呼ばれるメソッド.
   * 第一引数 : 成否フラグ. (true -> 成功, false -> 失敗) <br>
   * 第二引数 : コマンドレスポンスgig
   */
  void process(String[] cmd, BiConsumer<? super Boolean, ? super String[]> onCmdFinished);

  /**
   * 現在実行中のコマンドの処理を停止する.
   * 未実行のコマンドを破棄する.
   */
  void halt();

  /**
   * このコマンドプロセッサに対するイベントハンドラの追加と削除を行うオブジェクトを返す.
   *
   * @return このコマンドプロセッサに対するイベントハンドラの追加と削除を行うオブジェクト
   */
  CallbackRegistry getCallbackRegistry();

  /** コマンドプロセッサに対するイベントハンドラの登録および削除操作を規定したインタフェース. */
  public interface CallbackRegistry {
    
    /** コマンドを処理する直前に呼ばれるイベントハンドラのレジストリを取得する. */
    ConsumerInvoker<CmdProcessingEvent>.Registry getOnCmdProcessing();
  }
  
  /**
   * コマンドプロセッサがコマンドを処理するときの情報を格納したレコード.
   *
   * @param cmd 処理するコマンド
   */
  public record CmdProcessingEvent(String[] cmd) {}
}
