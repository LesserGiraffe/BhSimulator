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
import java.util.function.BiConsumer;
import net.seapanda.bunnyhop.simulator.obj.RaspiCar;
import net.seapanda.bunnyhop.simulator.obj.RaspiCar.Motion;
import net.seapanda.bunnyhop.utility.function.ConsumerInvoker;

/**
 * {@link BhSimulatorCmd} を処理するクラス.
 */
class SimulatorCmdProcessorImpl implements SimulatorCmdProcessor {
  
  private final RaspiCar raspiCar;
  private final Queue<Runnable> actions = new ConcurrentLinkedQueue<>();
  private final CallbackRegistryImpl cbRegistry = new CallbackRegistryImpl();

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
  public void process(String[] cmd, BiConsumer<? super Boolean, ? super String[]> onCmdFinished) {
    try {
      String opcode = cmd[0];
      if (opcode.equals(Opcode.MOVE.name)) {
        cbRegistry.onCmdProcessingInvoker.invoke(new CmdProcessingEvent(cmd));
        move(cmd, onCmdFinished);
      } else if (opcode.equals(Opcode.DETECT_COLOR.name)) {
        cbRegistry.onCmdProcessingInvoker.invoke(new CmdProcessingEvent(cmd));
        detectColor(onCmdFinished);
      } else if (opcode.equals(Opcode.MEASURE_DISTANCE.name)) {
        cbRegistry.onCmdProcessingInvoker.invoke(new CmdProcessingEvent(cmd));
        measureDistance(onCmdFinished);
      } else if (opcode.equals(Opcode.LIGHT_EYE.name)) {
        cbRegistry.onCmdProcessingInvoker.invoke(new CmdProcessingEvent(cmd));
        lightEye(cmd, onCmdFinished);
      } else {
        onCmdFinished.accept(false, new String[] {"Unknown Command"});  
      }
    } catch (Throwable e) {
      onCmdFinished.accept(false, new String[] {e.toString()});
    }
  }

  /** RasPiCar を移動させるコマンドを処理する. */
  private void move(String[] cmd, BiConsumer<? super Boolean, ? super String[]> onCmdFinished) {
    String motion = cmd[1];
    BiConsumer<Motion, Motion> onMoveFinished =
        (oldMotion, newMotion) -> onCmdFinished.accept(true, new String[] {});

    if (motion.equals(MoveMotion.STOP.name)) {
      actions.add(() -> raspiCar.stopMoving(onMoveFinished));
      return;
    }

    float speed = Float.parseFloat(cmd[2]);
    float time = Float.parseFloat(cmd[3]);
    if (motion.equals(MoveMotion.FORWARD.name)) {
      actions.add(() -> raspiCar.moveForward(speed, time, onMoveFinished));
    } else if (motion.equals(MoveMotion.BACKWARD.name)) {
      actions.add(() -> raspiCar.moveBackward(speed, time, onMoveFinished));
    } else if (motion.equals(MoveMotion.CLOCKWISE.name)) {
      actions.add(() -> raspiCar.turnRight(speed, time, onMoveFinished));
    } else if (motion.equals(MoveMotion.COUNTER_CLOCKWISE.name)) {
      actions.add(() -> raspiCar.turnLeft(speed, time, onMoveFinished));
    } else {
      onCmdFinished.accept(false, new String[] {"Invalid Move Command"});
    }
  }

  /** 色を取得するコマンドを処理する. */
  private void detectColor(BiConsumer<? super Boolean, ? super String[]> onCmdFinished) {
    actions.offer(() -> {
      Color color = raspiCar.detectColor();
      int red = Math.clamp((int) (color.r * 255.0f), 0, 255);
      int green = Math.clamp((int) (color.g * 255.0f), 0, 255);
      int blue = Math.clamp((int) (color.b * 255.0f), 0, 255);
      onCmdFinished.accept(
          true, new String[] {String.valueOf(red), String.valueOf(green), String.valueOf(blue)});
    });
  }

  /** 距離を計測するコマンドを処理する. */
  private void measureDistance(BiConsumer<? super Boolean, ? super String[]> onCmdFinished) {
    actions.offer(() -> {
      float distance = raspiCar.measureDistance();
      onCmdFinished.accept(true, new String[] {String.valueOf(distance)});
    });
  }  

  /** RasPiCar の目を光らせるコマンドを処理する. */
  private void lightEye(String[] cmd, BiConsumer<? super Boolean, ? super String[]> onCmdFinished) {
    String eye = cmd[1];
    int red = Integer.parseInt(cmd[2]);
    int green = Integer.parseInt(cmd[3]);
    int blue = Integer.parseInt(cmd[4]);
    EyeColors eyeColors = getEyeColors(red, green, blue);
    
    if (eye.equals(Eye.LEFT.name)) {
      actions.add(() -> {
        raspiCar.setLeftEyeColor(eyeColors.left);
        onCmdFinished.accept(true, new String[] {});
      });
    } else if (eye.equals(Eye.RIGHT.name)) {
      actions.add(() -> {
        raspiCar.setRightEyeColor(eyeColors.right);
        onCmdFinished.accept(true, new String[] {});
      });
    } else if (eye.equals(Eye.BOTH.name)) {
      actions.add(() -> {
        raspiCar.setLeftEyeColor(eyeColors.left);
        raspiCar.setRightEyeColor(eyeColors.right);
        onCmdFinished.accept(true, new String[] {});
      });
    } else {
      onCmdFinished.accept(false, new String[] {"Invalid Eye Option"});
    }
  }

  /** 目の色を取得する. */
  private EyeColors getEyeColors(int red, int green, int blue) {
    Color left = raspiCar.defaultLeftEyeColor;
    Color right = raspiCar.defaultRightEyeColor;
    if (!(red == -1 && green == -1 && blue == -1)) {
      left = new Color(red / 255f, green / 255f, blue / 255f, 1.0f);
      right = left;
    }
    return new EyeColors(left, right);
  }

  @Override
  public void halt() {
    actions.clear();
    actions.add(() -> raspiCar.setLeftEyeColor(raspiCar.defaultLeftEyeColor));
    actions.add(() -> raspiCar.setRightEyeColor(raspiCar.defaultRightEyeColor));
    actions.add(() -> raspiCar.stopMoving());
  }

  @Override
  public CallbackRegistry getCallbackRegistry() {
    return cbRegistry;
  }

  /** コマンドのオペコード. */
  private enum Opcode {
    MOVE("move"),
    DETECT_COLOR("detect-color"),
    MEASURE_DISTANCE("measure-distance"),
    LIGHT_EYE("light-eye");

    public final String name;

    private Opcode(String name) {
      this.name = name;
    }
  
    @Override
    public String toString() {
      return name;
    }
  }

  /** 移動の種類. */
  private enum MoveMotion {
    FORWARD("fwd"),
    BACKWARD("bwd"),
    CLOCKWISE("cw"),
    COUNTER_CLOCKWISE("ccw"),
    STOP("stop");

    public final String name;

    private MoveMotion(String name) {
      this.name = name;
    }
  
    @Override
    public String toString() {
      return name;
    }
  }

  /** 目の種類. */
  private enum Eye {
    LEFT("left"),
    RIGHT("right"),
    BOTH("both");

    public final String name;

    private Eye(String name) {
      this.name = name;
    }
  
    @Override
    public String toString() {
      return name;
    }
  }

  /** 目の色を格納するレコード. */
  private record EyeColors(Color left, Color right) {}

  /** コマンドプロセッサに対するイベントハンドラの登録および削除操作を提供するクラス. */
  public class CallbackRegistryImpl implements CallbackRegistry {
    
    /** コマンドを処理する直前に呼ばれるイベントハンドラを管理するオブジェクト. */
    private ConsumerInvoker<CmdProcessingEvent> onCmdProcessingInvoker = new ConsumerInvoker<>();

    @Override
    public ConsumerInvoker<CmdProcessingEvent>.Registry getOnCmdProcessing() {
      return onCmdProcessingInvoker.getRegistry();
    }
  }
}
