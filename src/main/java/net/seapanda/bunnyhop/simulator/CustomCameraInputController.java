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

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import net.seapanda.bunnyhop.simulator.geometry.GeoUtil;

/**
 * 注視点が Y = 定数の平面上に固定されるようにカメラを動かすクラス.
 *
 * @author K.Koike
 */
class CustomCameraInputController extends CameraInputController {

  /** カメラの最低仰角. */
  private final float minElevAngle;
  /** カメラの最高仰角. */
  private final float maxElevAngle;
  private final float minPointOfGazeX;
  private final float maxPointOfGazeX;
  private final float minPointOfGazeZ;
  private final float maxPointOfGazeZ;
  private final float minDistanceToGazePoint;
  private final float maxDistanceToGazePoint;
  private final float offsetY;

  /**
   * コンストラクタ.
   *
   * @param camera このクラスが制御するカメラ.
   * @param minElevAngle カメラが取れる最小仰角. (範囲: 5~85) (単位: degree)
   * @param maxElevAngle カメラが取れる最大仰角. (範囲: 5~85) (単位: degree)
   * @param minPointOfGazeX カメラの注視点の X 座標の最小値
   * @param maxPointOfGazeX カメラの注視点の X 座標の最大値
   * @param minPointOfGazeZ カメラの注視点の Z 座標の最小値
   * @param maxPointOfGazeZ カメラの注視点の Z 座標の最大値
   * @param minDistanceToGazePoint カメラと注視点の間の最小距離
   * @param maxDistanceToGazePoint カメラと注視点の間の最大距離
   * @param offsetY 注視点の Y 座標
   */
  public CustomCameraInputController(
      Camera camera,
      float minElevAngle,
      float maxElevAngle,
      float minPointOfGazeX,
      float maxPointOfGazeX,
      float minPointOfGazeZ,
      float maxPointOfGazeZ,
      float minDistanceToGazePoint,
      float maxDistanceToGazePoint,
      float offsetY) {
    super(camera);
    this.minElevAngle = (float) Math.toRadians(Math.clamp(minElevAngle, 0f, 85f));
    this.maxElevAngle = (float) Math.toRadians(Math.clamp(maxElevAngle, 0f, 85f));
    this.minPointOfGazeX = minPointOfGazeX;
    this.maxPointOfGazeX = maxPointOfGazeX;
    this.minPointOfGazeZ = minPointOfGazeZ;
    this.maxPointOfGazeZ = maxPointOfGazeZ;
    this.minDistanceToGazePoint = minDistanceToGazePoint;
    this.maxDistanceToGazePoint = maxDistanceToGazePoint;
    this.offsetY = offsetY;
    if (!isInValidElevAngle()) {
      adjustCameraDirection();
    }
    var pos = GeoUtil.calcIntersectionOfPlaneAndLine(
        new Vector3(0, offsetY, 0), new Vector3(0, 1, 0), camera.position, camera.direction);
    target.set(pos);
    if (!isInValidTargetRange(target.x, target.z)) {
      adjustCameraPos();
    }
    if (!isInValidDistance()) {
      moveForward(0);
    }
    if (autoUpdate) {
      camera.update();
    }
    translateUnits = Math.max(
      maxPointOfGazeX - minPointOfGazeX, maxPointOfGazeZ - minPointOfGazeZ) * 0.5f;
    scrollFactor = -0.08f;
  }

  /**
   * マウスをドラッグした時の処理を行う.
   *
   * @param deltaX 前回呼ばれた時のマウス位置からの横方向のドラッグ量.
   * @param deltaY 前回呼ばれた時のマウス位置からの縦方向のドラッグ量.
   * @param button 押下されているボタン.
   */
  @Override
  protected boolean process(float deltaX, float deltaY, int button) {
    if (button == rotateButton) {
      rotate(deltaX, deltaY);
    } else if (button == translateButton) {
      translate(deltaX, deltaY);
    } else if (button == forwardButton) { 
      moveForward(deltaY * scrollFactor * -80f);
    }
    if (autoUpdate) {
      camera.update();
    }
    return true;
  }

  /** カメラを回転させる. */
  private void rotate(float deltaX, float deltaY) {
    camera.rotateAround(target, Vector3.Y, deltaX * -rotateAngle);
    float deltaAngle = (float) -Math.toRadians(deltaY * rotateAngle);
    if (canChangeElevAngle(deltaAngle)) {
      var tmp = new Vector3(camera.direction).crs(camera.up);
      tmp.y = 0f;
      camera.rotateAround(target, tmp.nor(), deltaY * rotateAngle);
    }
  }

  /** カメラを平行移動させる. */
  private void translate(float deltaX, float deltaY) {
    var tmp = new Vector3(camera.direction).crs(camera.up).nor().scl(-deltaX * translateUnits);
    tmp.add(new Vector3(camera.up.x, 0, camera.up.z).nor().scl(-deltaY * translateUnits));
    camera.translate(tmp.x, 0f, tmp.z);
    tmp = GeoUtil.calcIntersectionOfPlaneAndLine(
      new Vector3(0, offsetY, 0), new Vector3(0, 1, 0), camera.position, camera.direction);
    target.set(tmp);
    if (!isInValidTargetRange(target.x, target.z)) {
      adjustCameraPos();
    }
  }

  /** カメラの仰角を deltaAngle だけ変更可能か調べる. */
  private boolean canChangeElevAngle(float deltaAngle) {
    float elevAngle = (float) Math.asin(-camera.direction.y);
    if (elevAngle < minElevAngle && deltaAngle > 0) {
      return true;
    }
    if (elevAngle > maxElevAngle && deltaAngle < 0) {
      return true;
    }
    elevAngle += deltaAngle;
    return (minElevAngle <= elevAngle) && (elevAngle <= maxElevAngle);
  }

  /** カメラの仰角が正常な範囲にあるか調べる. */
  private boolean isInValidElevAngle() {
    float elevAngle = (float) Math.asin(-camera.direction.y);
    return (minElevAngle <= elevAngle) && (elevAngle <= maxElevAngle);
  }

  /** 引数で指定した位置が, カメラの注視点として正常な範囲にあるか調べる. 
   *
   * @param x 調べる位置の X 座標.
   * @param y 調べる位置の Y 座標.
   */
  private boolean isInValidTargetRange(float x, float z) {
    return (minPointOfGazeX <= x) && (x <= maxPointOfGazeX)
        && (minPointOfGazeZ <= z) && (z <= maxPointOfGazeZ);
  }

  private boolean isInValidDistance() {
    float distance = new Vector3(camera.position).sub(target).len();
    return (minDistanceToGazePoint < distance) && (distance < maxDistanceToGazePoint);
  }

  @Override
  public boolean zoom(float amount) {
    if (!alwaysScroll && activateKey != 0 && !activatePressed) {
      return false;
    }
    moveForward(amount);
    if (autoUpdate) {
      camera.update();
    }
    return true;
  }

  /** カメラと注視点の距離を変更する. */
  public void moveForward(float amount) {
    float distance = new Vector3(camera.position).sub(target).len() - amount;
    if (distance <= minDistanceToGazePoint) {
      camera.position.set(target).add(new Vector3(camera.direction).scl(-minDistanceToGazePoint));
    } else if (maxDistanceToGazePoint <= distance) {
      camera.position.set(target).add(new Vector3(camera.direction).scl(-maxDistanceToGazePoint));
    } else {
      camera.translate(new Vector3(camera.direction).scl(amount));
    }
  }

  /** カメラの視線を調整してカメラの仰角が正常な範囲に収まるようにする. */
  private void adjustCameraDirection() {
    float angle = (float) (this.minElevAngle + this.maxElevAngle) / 2f;
    float x = camera.direction.x + camera.up.x;
    float y = (float) Math.sin(angle);
    float z = camera.direction.z + camera.up.z;
    float a = (1 - y * y) / (x * x + z * z);
    Vector3 lateralVec = new Vector3(camera.direction).crs(camera.up);
    camera.direction.set(a * x, -y, a * z).nor();
    camera.up.set(lateralVec.crs(camera.direction));
  }

  /** カメラの注視点が正常な範囲になるようにカメラの位置を調整する. */
  private void adjustCameraPos() {
    float tx = 0f;
    if (target.x > maxPointOfGazeX) {
      tx = maxPointOfGazeX - target.x;
    } else if (target.x < minPointOfGazeX) {
      tx = minPointOfGazeX - target.x;
    }
    float tz = 0f;
    if (target.z > maxPointOfGazeZ) {
      tz = maxPointOfGazeZ - target.z;
    } else if (target.z < minPointOfGazeZ) {
      tz = minPointOfGazeZ - target.z;
    }
    camera.translate(tx, 0, tz);
    target.add(tx, 0, tz);
  }
}
