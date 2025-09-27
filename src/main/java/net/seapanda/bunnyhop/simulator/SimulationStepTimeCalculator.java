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

/**
 * 物理シミュレーション時間が次にどれだけ進むか計算するクラス.
 *
 * @author K.Koike
 */
public class SimulationStepTimeCalculator {

  /** シミュレータが 1 度のステップで進める時間. */
  public final float timeStep;
  /** シミュレータの一度の更新で実行する最大ステップ数. */
  public final int maxSteps;

  private float totalTime = 0;
  private int nextSteps = 0;

  /** コンストラクタ. */
  public SimulationStepTimeCalculator(float timeStep, int maxSteps) {
    if (timeStep <= 0) {
      throw new AssertionError("timeStep must be greater than zero.");
    }
    if (maxSteps < 1) {
      throw new AssertionError("maxSteps must be 1 or greater");
    }
    this.timeStep = timeStep;
    this.maxSteps = maxSteps;
  }

  /** シミュレーション時間を {@code deltaTime} 数だけ進める. */
  public void advanceTime(float deltaTime) {
    totalTime += deltaTime;
    nextSteps = 0;
    if (totalTime >= timeStep) {
      nextSteps = (int) (totalTime / timeStep);
      // 引く時間は制限をかける前のステップを使う. (btDiscreteDynamicsWorld::stepSimulation より)
      totalTime -= timeStep * nextSteps;
      nextSteps = Math.min(nextSteps, maxSteps);
    }
  }

  /** 次のシミュレータの更新で進む時間 (秒) を取得する. */
  public float getNextTimeStep() {
    return timeStep * nextSteps;
  }

  /** 次のシミュレータの更新で実行されるステップ数を取得する. */
  public int getNextSteps() {
    return nextSteps;
  }
}
