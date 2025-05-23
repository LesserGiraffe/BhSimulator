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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.physics.bullet.collision.btGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btPersistentManifold;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.utils.MaterialConverter;
import net.seapanda.bunnyhop.simulator.BhSimulator;
import net.seapanda.bunnyhop.simulator.geometry.GeoUtil;
import net.seapanda.bunnyhop.simulator.geometry.RayTestHelper;
import net.seapanda.bunnyhop.simulator.geometry.RayTestHelper.RayTestResult;
import net.seapanda.bunnyhop.simulator.obj.interfaces.Collidable;
import net.seapanda.bunnyhop.simulator.obj.interfaces.ObjectReflectionProvider;
import net.seapanda.bunnyhop.simulator.obj.interfaces.PhysicalEntity;
import net.seapanda.bunnyhop.simulator.obj.interfaces.UiViewProvider;
import net.seapanda.bunnyhop.simulator.ui.RaspiCarCtrlView;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * RaspiCar の 3D モデルを作るクラス.
 *
 * @author K.Koike
 */
public class RaspiCar extends PhysicalEntity implements ObjectReflectionProvider, UiViewProvider {

  private final SceneAsset sceneAsset = new GLBLoader().load(
      Gdx.files.absolute(BhSimulator.ASSET_PATH + "/Models/RaspiCar.glb"));
  private final Scene scene;
  /** 本体の衝突判定オブジェクト. */
  private final btRigidBody body;
  /** キャタピラ部分の衝突判定オブジェクト. */
  private final btGhostObject caterpillarCollisionObj;
  /** 色センサ部分の衝突判定オブジェクト. */
  private final btGhostObject colorSensorCollisionObj;
  /** 距離センサのビームを描画するためのオブジェクト. */
  private ModelInstance sensorBeam;
  /** 3D モデルの長さ / 実物の長さ. */
  private float modelScale = 67;
  /** 描画時のスケール. */
  private final float scale;
  /** ローカル空間上でのこのオブジェクトの論理的な原点. */
  private Vector3 logicalOrigin = new Vector3(0f, -1.3f, 0f);
  // private Vector3 logicalOrigin = new Vector3(0, 0, 0);
  /** 実際の距離センサの最大測定距離 (m) . */
  private final float maxMeasurableDistance = 3f;
  /** 3D モデルが定義された空間における距離センサのビームの始点. */
  private final Vector3 beamStartPos = new Vector3(0, 0.93f, -3.634f);
  /** 3D モデルが定義された空間における距離センサのビームの終点. */
  private final Vector3 beamEndPos = new Vector3();
  private List<AnimationController> rhsAnimCtrls = new ArrayList<>();
  private List<AnimationController> lhsAnimCtrls = new ArrayList<>();
  /** 現在の動作. */
  private Motion motion = Motion.IDLE;
  /** 1 つ前の物理シミュレーションステップの動作. */
  private Motion prevMotion = Motion.IDLE;
  /** 1 つ前の物理シミュレーションステップの動作. */
  private boolean isOnGroundPrev = false;
  /** 現在の速度. */
  private float speed = 0;
  /** 現在の角速度. */
  private float rotationSpeed = 0;
  /** 現在の動作の残り時間. */
  private float timeLeft = 0;
  /** 現在の動作が変わった時に呼ぶコールバック関数. (oldMotion, newMotion) -> {} */
  private BiConsumer<Motion, Motion> onMotionSwitched = null;
  /** このオブジェクトのリソースを共有する {@link ObjectReflection} オブジェクトの個数. */
  private final MutableInt numShared = new MutableInt(0);
  /** 選択状態を保持するフラグ. */
  private boolean isSelected = false;
  /** 選択されたときの色. */
  private final Attribute colorAttrOnSelected = 
      ColorAttribute.createEmissive(new Color(0.2f, 0.2f, 0.2f, 1.0f));
  /** この 3D モデルの衝突判定オブジェクトを保持する {@link btCollisionWorld} オブジェクト. */
  private btCollisionWorld world;
  /** 前の物理シミュレーションステップで回転動作で生じた角速度. */
  private Vector3 prevAngularVelocity = new Vector3(0f, 0f, 0f);
  /** 前の物理シミュレーションステップで前進, 後退動作で生じた速度. */
  private Vector3 prevVelocity = new Vector3(0f, 0f, 0f);
  /** 右目の初期色. */
  public final Color defaultRightEyeColor;
  /** 左目の初期色. */
  public final Color defaultLeftEyeColor;
  /** UI のルートコンポーネント. */
  private RaspiCarCtrlView uiComponent;

  /**
   * コンストラクタ.
   *
   * @param scale 直方体のサイズ
   * @param pos 直方体の底面の中心点の位置
   */
  public RaspiCar(float scale, Vector3 pos) {
    this.scale = scale;
    scene = createScene(scale, pos);
    btCollisionShape shape = createCollisionShape(scene.modelInstance);
    CustomMotionState motionState = new CustomMotionState(scene.modelInstance.transform);
    body = createRigidBody(shape, motionState);
    sensorBeam = createSensorBeam(scene.modelInstance.transform);
    caterpillarCollisionObj = createCollisionObject(
        scene.modelInstance, "caterpillar-collision-L", "caterpillar-collision-R");
    colorSensorCollisionObj = createCollisionObject(scene.modelInstance, "color-sensor-collision");
    motionState.addOnWorldTransform(caterpillarCollisionObj::setWorldTransform);
    motionState.addOnWorldTransform(colorSensorCollisionObj::setWorldTransform);
    createAnimDescs();
    defaultRightEyeColor = getRightEyeColor();
    defaultLeftEyeColor = getLeftEyeColor();
    uiComponent = new RaspiCarCtrlView(this);
  }

  /** 
   * モデルが持つ姿勢および物理量を更新する.
   *
   * @param deltaTime 経過時間 (秒)
   */
  public void update(float deltaTime) {
    updateAnimation(deltaTime);
    updatePhysicalState(deltaTime);
    if (timeLeft <= 0) {
      switchMotion(Motion.IDLE, null);
    } else {
      timeLeft -= deltaTime;
    }
  }

  /**
   * 動作を切り替える.
   *
   * @param newMotion 新しい動作
   * @param newOnMotionSwitched {@code newMotion} で指定した動作から他の動作に切り替わったときに呼ばれるコールバック関数
   */
  private void switchMotion(Motion newMotion, BiConsumer<Motion, Motion> newOnMotionSwitched) {
    if (this.onMotionSwitched != null) {
      this.onMotionSwitched.accept(motion, newMotion);
    }
    this.onMotionSwitched = newOnMotionSwitched;
    motion = newMotion;
  }

  /** アニメーションを更新する. */
  private void updateAnimation(float deltaTime) {
    for (int i = 0; i < rhsAnimCtrls.size(); ++i) {
      if (motion != Motion.IDLE) {
        rhsAnimCtrls.get(i).update(deltaTime);
        lhsAnimCtrls.get(i).update(deltaTime);
      } else {
        rhsAnimCtrls.get(i).current.speed = 0;
        lhsAnimCtrls.get(i).current.speed = 0;
      }
    }
  }

  /**
   * 前進, 後退, 右回転, 左回転 の何れかの動作から別の動作に移る場合に, 前の動作によって生じた速度を打ち消す.
   * 摩擦力を大きくすると直進しにくくなるので, 摩擦力を大きくせずに静止力を高めるために導入する.
   */
  private void brake() {
    boolean isTurning = (motion == Motion.TURN_LEFT) || (motion == Motion.TURN_RIGHT);
    boolean isTurningPrev = (prevMotion == Motion.TURN_LEFT) || (prevMotion == Motion.TURN_RIGHT);
    if (isTurningPrev && !isTurning) {
      Vector3 currentAngularVelocity = body.getAngularVelocity();
      float dot = prevAngularVelocity.dot(currentAngularVelocity);
      float len = prevAngularVelocity.len2();
      if (dot < len) {
        prevAngularVelocity.scl(dot / len);
      }
      body.setAngularVelocity(currentAngularVelocity.sub(prevAngularVelocity));
      prevAngularVelocity.set(0f, 0f, 0f);
    }

    boolean isMovingStraight =
        (motion == Motion.MOVE_FORWARD) || (motion == Motion.MOVE_BACKWARD);
    boolean isMovingStraightPrev =
        (prevMotion == Motion.MOVE_FORWARD) || (prevMotion == Motion.MOVE_BACKWARD);
    if (isMovingStraightPrev && !isMovingStraight) {
      Vector3 currentVelocity = body.getLinearVelocity();
      float dot = prevVelocity.dot(currentVelocity);
      float len = prevVelocity.len2();
      if (dot < len) {
        prevVelocity.scl(dot / len);
      }
      body.setLinearVelocity(currentVelocity.sub(prevVelocity));
      prevVelocity.set(0f, 0f, 0f);
    }
  }

  private void updateInvInertiaDiagLocal(float mul) {
    Vector3 invInertia = body.getInvInertiaDiagLocal();
    invInertia.y *= mul;
    body.setInvInertiaDiagLocal(invInertia);
  }

  /** 物理的な状態を更新する. */
  private void updatePhysicalState(float deltaTime) {
    boolean isOnGround = isOnGround();
    if (isOnGround != isOnGroundPrev) {
      // 接地時に Yaw 軸周りに回りにくくする.
      float mul = isOnGround ? (1f / 4f) : 4f;
      updateInvInertiaDiagLocal(mul);
    }
    isOnGroundPrev = isOnGround;
    if (!isOnGround) {
      prevMotion = motion;
      return;
    }
    brake();
    if ((motion == Motion.MOVE_FORWARD) || (motion == Motion.MOVE_BACKWARD)) {
      setVelocity(speed);
    } else if ((motion == Motion.TURN_LEFT) || (motion == Motion.TURN_RIGHT)) {
      setAngularVelocity(rotationSpeed);
    }
    prevMotion = motion;
  }

  /** 3次元モデルを作成する. */
  private Scene createScene(float scale, Vector3 pos) {
    var scene = new Scene(sceneAsset.scene);
    scene.modelInstance.transform.scl(scale);
    logicalOrigin.scl(scale);
    scene.modelInstance.transform.setTranslation(new Vector3(pos).sub(logicalOrigin));
    MaterialConverter.makeCompatible(scene);
    for (var mat : scene.modelInstance.materials) {
      mat.set(ColorAttribute.createSpecular(Color.WHITE));
      mat.set(FloatAttribute.createShininess(10f));
    }
    return scene;
  }

  /**
   * 距離センサのビームのモデルを作成する. 
   *
   * @param transform RaspiCar 本体の姿勢行列
   */
  private ModelInstance createSensorBeam(Matrix4 transform) {
    ModelBuilder builder = new ModelBuilder();
    builder.begin();
    builder.node();
    MeshPartBuilder mpb = builder.part(
        "line",
        GL20.GL_LINES,
        Usage.Position | Usage.Normal,
        new Material(ColorAttribute.createDiffuse(new Color(Color.RED))));
    beamEndPos.set(beamStartPos);
    beamEndPos.z -= maxMeasurableDistance * modelScale;
    mpb.line(beamStartPos, beamEndPos);
    var sensorBeam = new ModelInstance(builder.end());
    sensorBeam.transform = transform;
    return sensorBeam;
  }

  private btCollisionShape createCollisionShape(ModelInstance modelInstance) {
    var shape = new btCompoundShape();
    List<Node> collisionNodes =
        GeoUtil.addCollisionBoxes(shape, modelInstance, "body-collision");
    collisionNodes.forEach(node -> node.parts.get(0).enabled = false);
    return shape;
  }

  private btRigidBody createRigidBody(btCollisionShape shape, btMotionState motionState) {
    var localInertia = new Vector3();
    var mass = 20.0f;
    shape.calculateLocalInertia(mass, localInertia);
    var rigidBody = new btRigidBody(mass, motionState, shape, localInertia);
    rigidBody.setCollisionFlags(
        rigidBody.getCollisionFlags()
        | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
    rigidBody.setActivationState(Collision.DISABLE_DEACTIVATION);
    rigidBody.setFriction(0.5f);
    rigidBody.userData = this;
    return rigidBody;
  }

  /** アニメーション制御用オブジェクトを作成する. */
  private void createAnimDescs() {
    String[] lhsAnims = {"caterpillar-move-L", "arm-move-L", "gears-rotation-L"};    
    for (var anim : lhsAnims) {
      var ctrl = new AnimationController(scene.modelInstance);
      ctrl.setAnimation(anim, -1);
      lhsAnimCtrls.add(ctrl);
    }

    String[] rhsAnims = {"caterpillar-move-R", "arm-move-R", "gears-rotation-R"};
    for (var anim : rhsAnims) {
      var ctrl = new AnimationController(scene.modelInstance);
      ctrl.setAnimation(anim, -1);
      rhsAnimCtrls.add(ctrl);
    }
  }

  /** 
   * {@code collisionNodeNames} で指定した {@link Node} のバウンディングボックスを
   * 衝突範囲として持つ {@link btGhostObject} を作成する.
   */
  private btGhostObject createCollisionObject(
      ModelInstance modelInstance, String... collisionNodeNames) {
    var shape = new btCompoundShape();
    List<Node> collisionNodes = 
        GeoUtil.addCollisionBoxes(shape, scene.modelInstance, collisionNodeNames);
    collisionNodes.forEach(node -> node.parts.get(0).enabled = false);

    var ghostObj = new btGhostObject();
    ghostObj.setCollisionShape(shape);
    ghostObj.setCollisionFlags(
        ghostObj.getCollisionFlags()
        | btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE
        | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
    ghostObj.setActivationState(Collision.DISABLE_DEACTIVATION);
    ghostObj.userData = this;
    return ghostObj;
  }

  /** この 3D モデルの論理的な位置から描画位置を計算する. */
  private Vector3 calcRenderingPos(Vector3 pos) {
    var transformedLogicalOrigin = new Vector3(logicalOrigin);
    Matrix4 mat = body.getWorldTransform();
    transformedLogicalOrigin.mul(mat);
    var transformedOrigin = new Vector3(); 
    mat.getTranslation(transformedOrigin);
    return transformedOrigin.sub(transformedLogicalOrigin).add(pos);
  }

  /** この 3D モデルが接地しているかチェックする. */
  public boolean isOnGround() {
    boolean isCaterpillarCollilded = false;
    for (CollisionObjPair pair : manifoldToCollisionPair.values()) {
      if (pair.obj0() == caterpillarCollisionObj && !(pair.obj1().userData instanceof Lamp)) {
        isCaterpillarCollilded = true;
      }
    }
    return calcTiltAngle() <= (Math.PI / 4) && isCaterpillarCollilded;
  }

  /** 色センサの値を取得する. */
  public Color detectColor() {
    Color detectedColor = new Color(Color.BLACK);
    for (CollisionObjPair pair : manifoldToCollisionPair.values()) {
      if (pair.obj0() == colorSensorCollisionObj && (pair.obj1().userData instanceof Lamp lamp)) {
        lamp.getLightColor().ifPresent(detectedColor::add);
      }
    }
    return detectedColor;
  }

  /**
   * モデルを前進させる.
   *
   * @param speedLevel 速度レベル
   * @param time 前進する時間 (sec)
   */
  public void moveForward(float speedLevel, float time) {
    moveForward(speedLevel, time, (oldMotion, newMotion) -> {});
  }

  /**
   * モデルを前進させる.
   *
   * @param speedLevel 速度レベル
   * @param time 前進する時間 (sec)
   * @param onMotionSwitched 今回のこのメソッドの呼び出しによる前進動作から他の動作に変わったときに呼ばれるコールバック関数.
   */
  public void moveForward(
      float speedLevel, float time, BiConsumer<Motion, Motion> onMotionSwitched) {
    float animSpeed = calcAnimSpeed(speedLevel);
    for (int i = 0; i < rhsAnimCtrls.size(); ++i) {
      rhsAnimCtrls.get(i).current.speed = animSpeed;
      lhsAnimCtrls.get(i).current.speed = animSpeed;
    }
    Motion motion = (time <= 0) ? Motion.IDLE : Motion.MOVE_FORWARD;
    switchMotion(motion, onMotionSwitched);
    speed = calcSpeed(speedLevel);
    timeLeft = time;
  }

  /**
   * モデルを後退させる.
   *
   * @param speedLevel 速度レベル
   * @param time 後退する時間 (sec)
   */
  public void moveBackward(float speedLevel, float time) {
    moveBackward(speedLevel, time, (oldMotion, newMotion) -> {});
  }

  /**
   * モデルを後退させる.
   *
   * @param speedLevel 速度レベル
   * @param time 後退する時間 (sec)
   * @param onMotionSwitched 今回のこのメソッドの呼び出しによる後退動作から他の動作に変わったときに呼ばれるコールバック関数.
   */
  public void moveBackward(
      float speedLevel, float time, BiConsumer<Motion, Motion> onMotionSwitched) {
    float animSpeed = calcAnimSpeed(speedLevel);
    for (int i = 0; i < rhsAnimCtrls.size(); ++i) {
      rhsAnimCtrls.get(i).current.speed = -animSpeed;
      lhsAnimCtrls.get(i).current.speed = -animSpeed;
    }
    Motion motion = (time <= 0) ? Motion.IDLE : Motion.MOVE_BACKWARD;
    switchMotion(motion, onMotionSwitched);
    speed = -calcSpeed(speedLevel);
    timeLeft = time;
  }

  /**
   * モデルを右回転させる.
   *
   * @param speedLevel 回転速度レベル
   * @param time 回転する時間 (sec)
   */
  public void turnRight(float speedLevel, float time) {
    turnRight(speedLevel, time, (oldMotion, newMotion) -> {});
  }

  /**
   * モデルを右回転させる.
   *
   * @param speedLevel 回転速度レベル
   * @param time 回転する時間 (sec)
   * @param onMotionSwitched 今回のこのメソッドの呼び出しによる右回転動作から他の動作に変わったときに呼ばれるコールバック関数.
   */
  public void turnRight(
      float speedLevel, float time, BiConsumer<Motion, Motion> onMotionSwitched) {
    float animSpeed = calcAnimSpeed(speedLevel);
    for (int i = 0; i < rhsAnimCtrls.size(); ++i) {
      rhsAnimCtrls.get(i).current.speed = -animSpeed;
      lhsAnimCtrls.get(i).current.speed = animSpeed;
    }
    Motion motion = (time <= 0) ? Motion.IDLE : Motion.TURN_RIGHT;
    switchMotion(motion, onMotionSwitched);
    rotationSpeed = -calcAngularVelocity(speedLevel);
    timeLeft = time;
  }

  /**
   * モデルを左回転させる.
   *
   * @param speedLevel 回転速度レベル
   * @param time 回転する時間 (sec)
   */
  public void turnLeft(float speedLevel, float time) {
    turnLeft(speedLevel, time, (oldMotion, newMotion) -> {});
  }

  /**
   * モデルを左回転させる.
   *
   * @param speedLevel 回転速度レベル
   * @param time 回転する時間 (sec)
   * @param onMotionSwitched 今回のこのメソッドの呼び出しによる左回転動作から他の動作に変わったときに呼ばれるコールバック関数.
   */
  public void turnLeft(
      float speedLevel, float time, BiConsumer<Motion, Motion> onMotionSwitched) {
    float animSpeed = calcAnimSpeed(speedLevel);
    for (int i = 0; i < rhsAnimCtrls.size(); ++i) {
      rhsAnimCtrls.get(i).current.speed = animSpeed;
      lhsAnimCtrls.get(i).current.speed = -animSpeed;
    }
    Motion motion = (time <= 0) ? Motion.IDLE : Motion.TURN_LEFT;
    switchMotion(motion, onMotionSwitched);
    rotationSpeed = calcAngularVelocity(speedLevel);
    timeLeft = time;
  }

  /** モデルの移動を停止する. */
  public void stopMoving() {
    stopMoving((oldMotion, newMotion) -> {});
  }

  /**
   * モデルの移動を停止する.
   *
   * @param onMotionSwitched 今回のこのメソッドの呼び出しによる停止動作から他の動作に変わったときに呼ばれるコールバック関数.
   */
  public void stopMoving(BiConsumer<Motion, Motion> onMotionSwitched) {
    switchMotion(Motion.IDLE, onMotionSwitched);
    timeLeft = 0;
  }

  /** 速度レベルから速度 [m/s] を求める. */
  private float calcSpeed(float speedLevel) {
    float speed = speedLevel * 0.0146f + 0.0035f;
    return speed * modelScale * scale;
  }

  /** 速度レベルから角速度 [rad/s] を求める. */
  private float calcAngularVelocity(float speedLevel) {
    return speedLevel * 0.1551f;
  }

  /** 速度レベルからアニメーションの再生速度を求める. */
  private float calcAnimSpeed(float speedLevel) {
    final float caterpillarLen = 21.38f; // meter
    final int caterpillarAnimLen = 192; // frames
    final int frameRate = 24; // frames / sec
    // キャタピラ 1 回転にかかる時間 [sec]
    final float catepillerAnimTime = (float) caterpillarAnimLen / frameRate;
    float speed = calcSpeed(speedLevel);
    return catepillerAnimTime * speed / (caterpillarLen * scale);
  }

  /** モデルに速度を与える. */
  private void setVelocity(float speed) {
    float[] elems = body.getWorldTransform().getValues();
    var motionVelocity = new Vector3(
        -elems[Matrix4.M02], -elems[Matrix4.M12], -elems[Matrix4.M22]).scl(speed);
    Vector3 currentVelocity = body.getLinearVelocity();
    float dot = prevVelocity.dot(currentVelocity);
    float len = prevVelocity.len2();
    // 設定した速度未満の場合は, 設定値に近づくように制御したい.
    // 外力計算後の prevVelocity 方向の速度が, 設定した速度より小さければ, 現在の速度から引く分を小さくする.
    if (dot < len) {
      prevVelocity.scl(dot / len);
    }
    var velocity = currentVelocity.sub(prevVelocity).add(motionVelocity);
    body.setLinearVelocity(velocity);
    prevVelocity.set(motionVelocity);
  }

  /** モデルに角速度を与える. */
  private void setAngularVelocity(float rotationSpeed) {
    float[] elems = body.getWorldTransform().getValues();
    var motionAngularVelocity = new Vector3(
        elems[Matrix4.M01], elems[Matrix4.M11], elems[Matrix4.M21]).scl(rotationSpeed);
    
    Vector3 currentAngularVelocity = body.getAngularVelocity();
    float dot = prevAngularVelocity.dot(currentAngularVelocity);
    float len = prevAngularVelocity.len2();
    if (dot < len) {
      prevAngularVelocity.scl(dot / len);
    }
    Vector3 angularVelocity = 
        currentAngularVelocity.sub(prevAngularVelocity).add(motionAngularVelocity);
    body.setAngularVelocity(angularVelocity);
    prevAngularVelocity.set(motionAngularVelocity);
  }

  /** ローカル座標系の Y 軸とワールド座標系の Y 軸のなす角度 (radian) を求める. */
  private float calcTiltAngle() {
    float[] matVals = body.getWorldTransform().getValues();
    var rotatedY = new Vector3(
        matVals[Matrix4.M01], matVals[Matrix4.M11], matVals[Matrix4.M21]);
    return (float) Math.acos(Math.clamp(rotatedY.dot(0, 1, 0), -1, 1));
  }

  /** 
   * この RaspiCar が持つ距離センサの値を取得する.
   *
   * @return この RaspiCar が持つ距離センサの値
   */
  public float measureDistance() {
    if (world == null) {
      return 0f;
    }
    var helper = new RayTestHelper(world);
    var mat = body.getWorldTransform();
    var xfmredStartPos = new Vector3(beamStartPos).scl(scale).mul(mat);
    var xfmredEndPos = new Vector3(beamEndPos).scl(scale).mul(mat);
    Optional<RayTestResult<Collidable>> result = helper.getIntersectedCollidable(
        xfmredStartPos,
        xfmredEndPos,
        CollisionGroup.PHYSICAL_ENTITY,
        CollisionGroup.STAGE);
    return result.map(res -> res.pos().sub(xfmredStartPos).len()).orElse(0f)
        * GeoUtil.SCALE_OF_MODEL;
  }

  /** この RaspiCar の左目の色を取得する. */
  public Color getLeftEyeColor() {
    Material material = scene.modelInstance.getMaterial("eye-L");
    if (material.get(ColorAttribute.Diffuse) instanceof ColorAttribute attr) {
      return new Color(attr.color);
    }
    throw new AssertionError("Left eye color is not set.");
  }

  /** この RaspiCar の右目の色を取得する. */
  public Color getRightEyeColor() {
    Material material = scene.modelInstance.getMaterial("eye-R");
    if (material.get(ColorAttribute.Diffuse) instanceof ColorAttribute attr) {
      return new Color(attr.color);
    }
    throw new AssertionError("Right eye color is not set.");
  }

  /** 
   * この RaspiCar の左目の色を設定する.
   *
   * @param color 設定する目の色.
   */
  public void setLeftEyeColor(Color color) {
    if (color == null) {
      return;
    }
    Material material = scene.modelInstance.getMaterial("eye-L");
    material.set(ColorAttribute.createDiffuse(color));
  }

  /** 
   * この RaspiCar の右目の色を設定する.
   *
   * @param color 設定する目の色.
   */
  public void setRightEyeColor(Color color) {
    if (color == null) {
      return;
    }
    Material material = scene.modelInstance.getMaterial("eye-R");
    material.set(ColorAttribute.createDiffuse(color));
  }

  /** この RaspiCar の両目の色を設定する. */
  public void setBothEyesColor(Color color) {
    setLeftEyeColor(color);
    setRightEyeColor(color);
  }

  /** 回転をリセットする. */
  private void resetRotation() {
    Matrix4 transform = body.getWorldTransform();
    Vector3 translation = transform.getTranslation(new Vector3());
    transform.set(new Matrix3().idt()).setTranslation(translation);
    body.setWorldTransform(transform);
    body.getMotionState().setWorldTransform(transform);
  }

  @Override
  public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
    scene.getRenderables(renderables, pool);
    sensorBeam.getRenderables(renderables, pool);
  }

  @Override
  public void dispose() {
    colorSensorCollisionObj.dispose();
    caterpillarCollisionObj.dispose();
    body.dispose();
    sensorBeam.model.dispose();
    sceneAsset.dispose(); // model も dispose される.
  }

  @Override
  public boolean isDraggable() {
    return true;
  }

  @Override
  public Vector3 getPosition() {
    var pos = new Vector3(logicalOrigin);
    pos.mul(body.getWorldTransform());
    return pos;
  }

  @Override
  public void setPosition(Vector3 pos) {
    Vector3 newPos = calcRenderingPos(pos);
    Matrix4 mat = body.getWorldTransform().setTranslation(newPos);
    body.setWorldTransform(mat);
    body.getMotionState().setWorldTransform(mat);
  }

  @Override
  public void rotateEuler(float yaw, float pitch, float roll) {
    var diff = new Matrix4().setFromEulerAngles(yaw, pitch, roll);
    var mat = body.getWorldTransform().mul(diff);
    body.setWorldTransform(mat);
    body.getMotionState().setWorldTransform(mat);
  }
  
  @Override
  public ObjectReflection createObjectReflection() {
    numShared.increment();
    scene.modelInstance.materials.forEach(material -> material.remove(ColorAttribute.Emissive));
    var reflection = new ObjectReflection(scene.modelInstance, 0.5f, numShared);
    reflection.setRenderingPosGetter(this::calcRenderingPos);
    if (isSelected) {
      scene.modelInstance.materials.forEach(material -> material.set(colorAttrOnSelected));
    }
    return reflection;
  }

  @Override
  public int getNumShared() {
    return numShared.getValue();
  }

  @Override
  public void resetRotationIfTiltingOverly() {
    if (calcTiltAngle() > Math.PI / 3) {
      resetRotation();
    }
  }

  @Override
  public void onContactStarted(
      btPersistentManifold manifold, btCollisionObject self, btCollisionObject theOther) {
    if (theOther.userData == this) {
      return;
    }
    super.onContactStarted(manifold, self, theOther);
  }

  @Override
  public void onContactEnded(
      btPersistentManifold manifold, btCollisionObject self, btCollisionObject theOther) {
    if (theOther.userData == this) {
      return;
    }
    super.onContactEnded(manifold, self, theOther);
  }

  @Override
  public void addCollisionObjectsTo(btDynamicsWorld world) {
    this.world = world;
    world.addRigidBody(
        body,
        CollisionGroup.PHYSICAL_ENTITY.val(),
        CollisionGroup.mask(
            CollisionGroup.PHYSICAL_ENTITY,
            CollisionGroup.STAGE,
            CollisionGroup.PHYSICAL_CONTACT_DETECTOR));
    world.addCollisionObject(
        caterpillarCollisionObj,
        CollisionGroup.PHYSICAL_CONTACT_DETECTOR.val(),
        CollisionGroup.mask(CollisionGroup.STAGE, CollisionGroup.PHYSICAL_ENTITY));
    world.addCollisionObject(
        colorSensorCollisionObj, CollisionGroup.LAMP_LIGHT.val(), CollisionGroup.LAMP_LIGHT.val());
  }

  @Override
  public void removeCollisionObjectsFrom(btDynamicsWorld world) {
    world.removeRigidBody(body);
    world.removeCollisionObject(caterpillarCollisionObj);
    world.removeCollisionObject(colorSensorCollisionObj);
    this.world = null;
  }

  @Override
  public void resetPhysicalState() {
    var zero = new Vector3(0f, 0f, 0f);
    body.clearForces();
    body.setLinearVelocity(zero);
    body.setAngularVelocity(zero);
    resetRotation();
  }

  @Override
  public void select() { 
    isSelected = true;
    scene.modelInstance.materials.forEach(material -> material.set(colorAttrOnSelected));
  }

  @Override
  public void deselect() {
    isSelected = false;
    scene.modelInstance.materials.forEach(material -> material.remove(ColorAttribute.Emissive));
  }

  @Override
  public boolean isSelected() {
    return isSelected;
  }

  @Override
  public RaspiCarCtrlView getUiView() {
    return uiComponent;
  }

  /** モデルの動作を表す列挙型. */
  public enum Motion {
    MOVE_FORWARD,  
    MOVE_BACKWARD,
    TURN_RIGHT,
    TURN_LEFT,
    IDLE,
  }
}
