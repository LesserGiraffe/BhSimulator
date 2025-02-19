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

import java.util.function.Consumer;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.DetectColorCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.MeasureDistanceCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.MoveBackwardRaspiCarCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.MoveForwardRaspiCarCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.SetBothEyesColorCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.SetLeftEyeColorCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.SetRightEyeColorCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.StopRaspiCarCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.TurnLeftRaspiCarCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorCmd.TurnRightRaspiCarCmd;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.DetectColorResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.MeasureDistanceResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.MoveBackwardRaspiCarResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.MoveForwardRaspiCarResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.SetBothEyesColorResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.SetLeftEyeColorResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.SetRightEyeColorResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.StopRaspiCarResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.TurnLeftRaspiCarResp;
import net.seapanda.bunnyhop.bhprogram.common.message.BhSimulatorResp.TurnRightRaspiCarResp;

/**
 * {@link BhSimulatorCmd} を処理する機能を規定したインタフェース.
 */
public interface SimulatorCmdProcessor {
  
  /**
   * {@link MoveForwardRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   */
  void process(MoveForwardRaspiCarCmd cmd);

  /**
   * {@link MoveForwardRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @param onCmdFinished コマンドの処理が終了したときに呼ばれるメソッド
   */
  void process(
      MoveForwardRaspiCarCmd cmd,
      Consumer<? super MoveForwardRaspiCarResp> onCmdFinished);

  /**
   * {@link MoveBackwardRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   */
  void process(MoveBackwardRaspiCarCmd cmd);

  /**
   * {@link MoveBackwardRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @param onCmdFinished コマンドの処理が終了したときに呼ばれるメソッド
   */
  void process(
      MoveBackwardRaspiCarCmd cmd,
      Consumer<? super MoveBackwardRaspiCarResp> onCmdFinished);

  /**
   * {@link TurnRightRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   */
  void process(TurnRightRaspiCarCmd cmd);

  /**
   * {@link TurnRightRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @param onCmdFinished コマンドの処理が終了したときに呼ばれるメソッド
   */
  void process(
      TurnRightRaspiCarCmd cmd,
      Consumer<? super TurnRightRaspiCarResp> onCmdFinished);

  /**
   * {@link TurnLeftRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   */
  void process(TurnLeftRaspiCarCmd cmd);

  /**
   * {@link TurnLeftRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @param onCmdFinished コマンドの処理が終了したときに呼ばれるメソッド
   */
  void process(
      TurnLeftRaspiCarCmd cmd,
      Consumer<? super TurnLeftRaspiCarResp> onCmdFinished);

  /**
   * {@link StopRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   */
  void process(StopRaspiCarCmd cmd);

  /**
   * {@link StopRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @param onCmdFinished コマンドの処理が終了したときに呼ばれるメソッド
   */
  void process(
      StopRaspiCarCmd cmd,
      Consumer<? super StopRaspiCarResp> onCmdFinished);

  /**
   * {@link MeasureDistanceCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @return コマンドのレスポンス
   */
  MeasureDistanceResp process(MeasureDistanceCmd cmd);

  /**
   * {@link DetectColorCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @return コマンドのレスポンス
   */
  DetectColorResp process(DetectColorCmd cmd);

  /**
   * {@link SetLeftEyeColorCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @return コマンドのレスポンス
   */
  SetLeftEyeColorResp process(SetLeftEyeColorCmd cmd);

  /**
   * {@link SetRightEyeColorCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @return コマンドのレスポンス
   */
  SetRightEyeColorResp process(SetRightEyeColorCmd cmd);

  /**
   * {@link SetBothEyesColorCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @return コマンドのレスポンス
   */
  SetBothEyesColorResp process(SetBothEyesColorCmd cmd);
}
