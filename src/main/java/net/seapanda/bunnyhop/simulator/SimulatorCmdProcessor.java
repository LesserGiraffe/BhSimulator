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

import com.badlogic.gdx.graphics.Color;
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
import net.seapanda.bunnyhop.simulator.obj.RaspiCar;

/**
 * {@link BhSimulatorCmd} を処理するクラス.
 */
public class SimulatorCmdProcessor {
  
  private final RaspiCar raspiCar;

  /**
   * コンストラクタ.
   *
   * @param raspiCar コマンドで制御する {@link RaspiCar} オブジェクト
   */
  SimulatorCmdProcessor(RaspiCar raspiCar) {
    this.raspiCar = raspiCar;
  }

  /**
   * {@link MoveForwardRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   */
  public void process(MoveForwardRaspiCarCmd cmd) {
    process(cmd, resp -> {});
  }

  /**
   * {@link MoveForwardRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @param onCmdFinished コマンドの処理が終了したときに呼ばれるメソッド
   */
  public synchronized void process(
      MoveForwardRaspiCarCmd cmd,
      Consumer<? super MoveForwardRaspiCarResp> onCmdFinished) {
    var resp = new MoveForwardRaspiCarResp(cmd.getId(), true);
    raspiCar.moveForward(
        cmd.speedLevel,
        cmd.time,
        (oldMotion, newMotion) -> onCmdFinished.accept(resp));
  }

  /**
   * {@link MoveBackwardRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   */
  public void process(MoveBackwardRaspiCarCmd cmd) {
    process(cmd, resp -> {});
  }

  /**
   * {@link MoveBackwardRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @param onCmdFinished コマンドの処理が終了したときに呼ばれるメソッド
   */
  public synchronized void process(
      MoveBackwardRaspiCarCmd cmd,
      Consumer<? super MoveBackwardRaspiCarResp> onCmdFinished) {
    var resp = new MoveBackwardRaspiCarResp(cmd.getId(), true);
    raspiCar.moveBackward(
        cmd.speedLevel,
        cmd.time,
        (oldMotion, newMotion) -> onCmdFinished.accept(resp));
  }

  /**
   * {@link TurnRightRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   */
  public void process(TurnRightRaspiCarCmd cmd) {
    process(cmd, resp -> {});
  }

  /**
   * {@link TurnRightRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @param onCmdFinished コマンドの処理が終了したときに呼ばれるメソッド
   */
  public synchronized void process(
      TurnRightRaspiCarCmd cmd,
      Consumer<? super TurnRightRaspiCarResp> onCmdFinished) {
    var resp = new TurnRightRaspiCarResp(cmd.getId(), true);
    raspiCar.turnRight(
        cmd.speedLevel,
        cmd.time,
        (oldMotion, newMotion) -> onCmdFinished.accept(resp));
  }

  /**
   * {@link TurnLeftRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   */
  public void process(TurnLeftRaspiCarCmd cmd) {
    process(cmd, resp -> {});
  }

  /**
   * {@link TurnLeftRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @param onCmdFinished コマンドの処理が終了したときに呼ばれるメソッド
   */
  public synchronized void process(
      TurnLeftRaspiCarCmd cmd,
      Consumer<? super TurnLeftRaspiCarResp> onCmdFinished) {
    var resp = new TurnLeftRaspiCarResp(cmd.getId(), true);
    raspiCar.turnLeft(
        cmd.speedLevel,
        cmd.time,
        (oldMotion, newMotion) -> onCmdFinished.accept(resp));
  }

  /**
   * {@link StopRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   */
  public void process(StopRaspiCarCmd cmd) {
    process(cmd, resp -> {});
  }

  /**
   * {@link StopRaspiCarCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @param onCmdFinished コマンドの処理が終了したときに呼ばれるメソッド
   */
  public synchronized void process(
      StopRaspiCarCmd cmd,
      Consumer<? super StopRaspiCarResp> onCmdFinished) {
    var resp = new StopRaspiCarResp(cmd.getId(), true);
    raspiCar.stopMoving((oldMotion, newMotion) -> onCmdFinished.accept(resp));
  }

  /**
   * {@link MeasureDistanceCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @return コマンドのレスポンス
   */
  public synchronized MeasureDistanceResp process(MeasureDistanceCmd cmd) {
    return new MeasureDistanceResp(cmd.getId(), true, raspiCar.measureDistance());
  }

  /**
   * {@link DetectColorCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @return コマンドのレスポンス
   */
  public synchronized DetectColorResp process(DetectColorCmd cmd) {
    Color color = raspiCar.detectColor();
    return new DetectColorResp(cmd.getId(), true, color.r, color.g, color.b);
  }

  /**
   * {@link SetLeftEyeColorCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @return コマンドのレスポンス
   */
  public synchronized SetLeftEyeColorResp process(SetLeftEyeColorCmd cmd) {
    raspiCar.setLeftEyeColor(new Color(cmd.red, cmd.green, cmd.blue, 1f));
    return new SetLeftEyeColorResp(cmd.getId(), true);
  }

  /**
   * {@link SetRightEyeColorCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @return コマンドのレスポンス
   */
  public synchronized SetRightEyeColorResp process(SetRightEyeColorCmd cmd) {
    raspiCar.setRightEyeColor(new Color(cmd.red, cmd.green, cmd.blue, 1f));
    return new SetRightEyeColorResp(cmd.getId(), true);
  }

  /**
   * {@link SetBothEyesColorCmd} を処理する.
   *
   * @param cmd 処理するコマンド
   * @return コマンドのレスポンス
   */
  public synchronized SetBothEyesColorResp process(SetBothEyesColorCmd cmd) {
    raspiCar.setBothEyesColor(new Color(cmd.red, cmd.green, cmd.blue, 1f));
    return new SetBothEyesColorResp(cmd.getId(), true);
  }
}
