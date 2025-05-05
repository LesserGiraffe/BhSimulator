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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
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
class SimulatorCmdProcessorImpl implements SimulatorCmdProcessor {
  
  private final RaspiCar raspiCar;
  Queue<Runnable> actions = new ConcurrentLinkedQueue<>();

  /**
   * コンストラクタ.
   *
   * @param raspiCar コマンドで制御する {@link RaspiCar} オブジェクト
   */
  SimulatorCmdProcessorImpl(RaspiCar raspiCar) {
    this.raspiCar = raspiCar;
  }

  /** 未実行のコマンドを処理する. */
  void executeCmds() {
    while (!actions.isEmpty()) {
      actions.remove().run();
    }
  }

  @Override
  public void process(MoveForwardRaspiCarCmd cmd) {
    process(cmd, resp -> {});
  }

  @Override
  public void process(
      MoveForwardRaspiCarCmd cmd,
      Consumer<? super MoveForwardRaspiCarResp> onCmdFinished) {
    var resp = new MoveForwardRaspiCarResp(cmd.getId(), true);
    actions.offer(() -> 
        raspiCar.moveForward(
            cmd.speedLevel,
            cmd.time,
            (oldMotion, newMotion) -> onCmdFinished.accept(resp)));
  }

  @Override
  public void process(MoveBackwardRaspiCarCmd cmd) {
    process(cmd, resp -> {});
  }

  @Override
  public void process(
      MoveBackwardRaspiCarCmd cmd,
      Consumer<? super MoveBackwardRaspiCarResp> onCmdFinished) {
    var resp = new MoveBackwardRaspiCarResp(cmd.getId(), true);
    actions.offer(() -> 
        raspiCar.moveBackward(
            cmd.speedLevel,
            cmd.time,
            (oldMotion, newMotion) -> onCmdFinished.accept(resp)));
  }

  @Override
  public void process(TurnRightRaspiCarCmd cmd) {
    process(cmd, resp -> {});
  }

  @Override
  public void process(
      TurnRightRaspiCarCmd cmd,
      Consumer<? super TurnRightRaspiCarResp> onCmdFinished) {
    var resp = new TurnRightRaspiCarResp(cmd.getId(), true);
    actions.offer(() -> 
        raspiCar.turnRight(
            cmd.speedLevel,
            cmd.time,
            (oldMotion, newMotion) -> onCmdFinished.accept(resp)));
  }

  @Override
  public void process(TurnLeftRaspiCarCmd cmd) {
    process(cmd, resp -> {});
  }

  @Override
  public void process(
      TurnLeftRaspiCarCmd cmd,
      Consumer<? super TurnLeftRaspiCarResp> onCmdFinished) {
    var resp = new TurnLeftRaspiCarResp(cmd.getId(), true);
    actions.offer(() -> 
        raspiCar.turnLeft(
            cmd.speedLevel,
            cmd.time,
            (oldMotion, newMotion) -> onCmdFinished.accept(resp)));
  }

  @Override
  public void process(StopRaspiCarCmd cmd) {
    process(cmd, resp -> {});
  }

  @Override
  public void process(
      StopRaspiCarCmd cmd,
      Consumer<? super StopRaspiCarResp> onCmdFinished) {
    var resp = new StopRaspiCarResp(cmd.getId(), true);
    actions.offer(() -> 
        raspiCar.stopMoving((oldMotion, newMotion) -> onCmdFinished.accept(resp)));
  }

  @Override
  public void process(
      MeasureDistanceCmd cmd,
      Consumer<? super MeasureDistanceResp> onCmdFinished) {
    actions.offer(() -> {
      var resp = new MeasureDistanceResp(cmd.getId(), true, raspiCar.measureDistance());
      onCmdFinished.accept(resp);
    });
  }

  @Override
  public void process(
      DetectColorCmd cmd,
      Consumer<? super DetectColorResp> onCmdFinished) {
    actions.offer(() -> {
      Color color = raspiCar.detectColor();
      var resp = new DetectColorResp(cmd.getId(), true, color.r, color.g, color.b);
      onCmdFinished.accept(resp);
    });
  }

  @Override
  public void process(SetLeftEyeColorCmd cmd) {
    process(cmd, resp -> {});
  }

  @Override
  public void process(
      SetLeftEyeColorCmd cmd,
      Consumer<? super SetLeftEyeColorResp> onCmdFinished) {
    actions.offer(() -> {
      raspiCar.setLeftEyeColor(new Color(cmd.red, cmd.green, cmd.blue, 1f));
      onCmdFinished.accept(new SetLeftEyeColorResp(cmd.getId(), true));
    });
  }

  @Override
  public void process(SetRightEyeColorCmd cmd) {
    process(cmd, resp -> {});
  }

  @Override
  public void process(
      SetRightEyeColorCmd cmd,
      Consumer<? super SetRightEyeColorResp> onCmdFinished) {
    actions.offer(() -> {
      raspiCar.setRightEyeColor(new Color(cmd.red, cmd.green, cmd.blue, 1f));
      onCmdFinished.accept(new SetRightEyeColorResp(cmd.getId(), true));
    });
  }

  @Override
  public void process(SetBothEyesColorCmd cmd) {
    process(cmd, resp -> {});
  }

  @Override
  public void process(
      SetBothEyesColorCmd cmd,
      Consumer<? super SetBothEyesColorResp> onCmdFinished) {
    actions.offer(() -> {
      raspiCar.setBothEyesColor(new Color(cmd.red, cmd.green, cmd.blue, 1f));
      onCmdFinished.accept(new SetBothEyesColorResp(cmd.getId(), true));
    });
  }
}
