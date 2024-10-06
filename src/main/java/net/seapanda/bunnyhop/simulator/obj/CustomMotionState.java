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

package net.seapanda.bunnyhop.simulator.obj;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * 3D モデルと衝突判定オブジェクトの姿勢を同期させるためのクラス.
 *
 * @author K.Koike
 */
class CustomMotionState extends btMotionState {
  private Matrix4 transform;
  /** 衝突判定オブジェクトの姿勢と同期させる姿勢行列のリスト. */
  private ArrayList<Consumer<Matrix4>> onSetWorldTransList = new ArrayList<>();
  private Vector3 scale = new Vector3();
  private Vector3 pos = new Vector3();

  public CustomMotionState(Matrix4 transform) {
    this.transform = transform;
  }

  /** 衝突判定オブジェクトの姿勢を 3D モデルの姿勢と同期させるときに呼ぶ関数をセットする. */
  public void addOnWorldTransform(Consumer<Matrix4> onSetWorldTransform) {
    this.onSetWorldTransList.add(onSetWorldTransform);
  }

  public void removeOnWorldTransform(Consumer<Matrix4> onSetWorldTransform) {
    this.onSetWorldTransList.remove(onSetWorldTransform);
  }

  @Override
  public void getWorldTransform(Matrix4 worldTrans) {
    // worldTrans = 衝突判定オブジェクトのアフィン変換行列
    worldTrans.set(transform);
    // スケールはセットしない
    worldTrans.val[Matrix4.M00] = 1;
    worldTrans.val[Matrix4.M11] = 1;
    worldTrans.val[Matrix4.M22] = 1;
  }

  @Override
  public void setWorldTransform(Matrix4 worldTrans) {
    // worldTrans = 衝突判定オブジェクトのアフィン変換行列
    transform.getScale(scale);
    worldTrans.getTranslation(pos);
    transform.idt().scl(scale).mul(worldTrans).setTranslation(pos);
    onSetWorldTransList.forEach(func -> func.accept(worldTrans));
  }
}
