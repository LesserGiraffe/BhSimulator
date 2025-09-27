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
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;
import java.util.ArrayList;
import java.util.function.Supplier;
import javax.naming.LimitExceededException;
import net.seapanda.bunnyhop.simulator.geometry.CustomContactListener;
import net.seapanda.bunnyhop.simulator.geometry.RayTestHelper;
import net.seapanda.bunnyhop.simulator.obj.Box;
import net.seapanda.bunnyhop.simulator.obj.Lamp;
import net.seapanda.bunnyhop.simulator.obj.ObjectReflection;
import net.seapanda.bunnyhop.simulator.obj.RaspiCar;
import net.seapanda.bunnyhop.simulator.obj.Stage;
import net.seapanda.bunnyhop.simulator.obj.interfaces.Collidable;
import net.seapanda.bunnyhop.simulator.obj.interfaces.ObjectReflectionProvider;
import net.seapanda.bunnyhop.simulator.obj.interfaces.PhysicalEntity;
import net.seapanda.bunnyhop.simulator.obj.interfaces.SimulationObject;
import net.seapanda.bunnyhop.simulator.obj.interfaces.UiViewProvider;
import net.seapanda.bunnyhop.simulator.ui.SimulationObjectManagerView;

/**
 * シミュレーション空間の 3D モデルを管理するクラス.
 *
 * @author K.Koike
 */
public class SimulationObjectManager implements Disposable, UiViewProvider {
  
  /** シミュレーション空間に追加可能な 3D モデルの最大数. */
  public static final int MAX_OBJECTS = 30;
  /** シミュレーション空間に存在する 3D モデルを格納するリスト. */
  private final ArrayList<SimulationObject> instances = new ArrayList<>();
  private final Stage stage = new Stage(1f, new Vector3(0f, 0f, 0f));
  private final RaspiCar car = new RaspiCar(1f / 67f, new Vector3(0f, 0.3f, 0.3f));
  private final RayTestHelper rayTestHelper;
  private final btDiscreteDynamicsWorld dynamicsWorld;
  private final ArrayList<Disposable> disposables = new ArrayList<>();
  private final DebugDrawer debugDrawer = new DebugDrawer();
  /** オブジェクトがステージから落ちたと判断する鉛直方向の位置の閾値. */
  private final float verticalPosThreshold = -10f;
  /** カメラの注視点を取得する関数のオブジェクト. */
  private Supplier<Vector3> cameraTargetGetter = () -> new Vector3(0f, 3f, 0f);
  /** シミュレーション空間に, 現在追加されている 3D モデルの個数. */
  private int numObjects = 0;
  /** UI のルートコンポーネント. */
  private final Actor uiComponent = new SimulationObjectManagerView(this);
  /**
   * 次の物理シミュレーションの更新で経過する時間を計算するためのオブジェクト.
   *
   * <p>Box と Lamp の AdditionalDamping を有効にしているので, シミュレーション間隔を 1 / 120 秒から減らさないこと.
   */
  private final SimulationStepTimeCalculator simStepTimeCalc =
      new SimulationStepTimeCalculator(1f / 120f, 5);

  /** コンストラクタ. */
  public SimulationObjectManager() {
    instances.add(stage);
    instances.add(car);
    dynamicsWorld = createDynamicWorld();
    rayTestHelper = new RayTestHelper(dynamicsWorld);
    stage.addCollisionObjectsTo(dynamicsWorld);
    car.addCollisionObjectsTo(dynamicsWorld);
  }

  private btDiscreteDynamicsWorld createDynamicWorld() {
    var collisionConfig = new btDefaultCollisionConfiguration();
    var dispatcher = new btCollisionDispatcher(collisionConfig);
    var broadphase = new btDbvtBroadphase();
    var constraintSolver = new btSequentialImpulseConstraintSolver();
    var dynamicsWorld = new btDiscreteDynamicsWorld(
        dispatcher, broadphase, constraintSolver, collisionConfig);
    dynamicsWorld.setGravity(new Vector3(0, -9.8f, 0));
    var contactListener = new CustomContactListener();
    dynamicsWorld.setDebugDrawer(debugDrawer);
    debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);
    disposables.add(contactListener);
    disposables.add(dynamicsWorld);
    disposables.add(constraintSolver);
    disposables.add(broadphase);
    disposables.add(dispatcher);
    disposables.add(collisionConfig);
    return dynamicsWorld;
  }

  /** シミュレーション空間の Stage を返す. */
  public Stage getStage() {
    return stage;
  }

  /** シミュレーション空間の RaspiCar を返す. */
  public RaspiCar getRaspiCar() {
    return car;
  }

  /** シミュレーション空間の 3D モデルの状態を更新する. */
  public void update(float deltaTime) {
    teleportObjectsDroppedOutOfStage();
    simStepTimeCalc.advanceTime(deltaTime);
    car.update(deltaTime, simStepTimeCalc.getNextTimeStep());
    // Box の AdditionalDamping を有効にしているので, シミュレーション間隔を 1 / 90 秒から減らさないこと.
    dynamicsWorld.stepSimulation(deltaTime, simStepTimeCalc.maxSteps, simStepTimeCalc.timeStep);
  }

  /** シミュレーション空間の 3D モデルを描画するためのインタフェースを取得する. */
  public Iterable<? extends RenderableProvider> getRenderableProviders() {
    return instances;
  }

  /** 
   * 引数で指定した位置に箱の 3D モデルを作成する. 
   *
   * @param pos 箱の底面の中心の位置.
   * @param isHeavy 重い箱を作る場合 true.
   * @return 作成した箱の 3D モデル.
   * @throws MaxObjectsExceededException 既にシミュレーション空間内に作成可能な 3D モデルの最大個数に達している.
   */
  public Box createBox(Vector3 pos, boolean isHeavy) throws MaxObjectsExceededException {
    if (numObjects == MAX_OBJECTS) {
      throw new MaxObjectsExceededException("No more 3D models can be added.");
    }
    float size = isHeavy ? 0.2f : 0.1f;
    var box = new Box(new Vector3(size, size, size), pos, isHeavy);
    instances.add(box);
    box.addCollisionObjectsTo(dynamicsWorld);
    ++numObjects;
    return box;
  }

  /** 
   * 引数で指定した位置に電灯の 3D モデルを作成する. 
   *
   * @param pos 箱の底面の中心の位置.
   * @return 作成した箱の 3D モデル.
   * @throws MaxObjectsExceededException 既にシミュレーション空間内に作成可能な 3D モデルの最大個数に達している.
   */
  public Lamp createLamp(Vector3 pos) throws MaxObjectsExceededException {
    if (numObjects == MAX_OBJECTS) {
      throw new MaxObjectsExceededException("No more 3D models can be added.");
    }
    var lamp = new Lamp(1f, pos);
    instances.add(lamp);
    lamp.addCollisionObjectsTo(dynamicsWorld);
    ++numObjects;
    return lamp;
  }

  /** {@code provider} で指定した 3D モデルの {@link ObjectReflection} を作成する. */
  public ObjectReflection createObjectReflection(ObjectReflectionProvider provider) {
    ObjectReflection obj = provider.createObjectReflection();
    instances.add(obj);
    return obj;
  }

  /** このオブジェクトが管理するシミュレーション空間上での ray test を行うためのオブジェクトを取得する. */
  public RayTestHelper getRayTestHelper() {
    return rayTestHelper;
  }

  /** {@code obj} で指定した 3D モデルをシミュレーション空間から削除する. */
  public void delete(SimulationObject obj) {
    instances.remove(obj);
    if (obj instanceof Collidable collidable) {
      collidable.removeCollisionObjectsFrom(dynamicsWorld);
    }
    obj.dispose();
    if (obj instanceof Lamp || obj instanceof Box) {
      --numObjects;
    }
  }

  /** 衝突判定オブジェクトを描画する. */
  public void drawCollisionObjects(Camera camera) {
    debugDrawer.begin(camera);
    dynamicsWorld.debugDrawWorld();
    debugDrawer.end();
  }

  /** シミュレーション空間を映すカメラの注視点を取得するメソッドを設定する. */
  public void setCameraTargetGetter(Supplier<Vector3> cameraTargetGetter) {
    this.cameraTargetGetter = cameraTargetGetter;
  }

  /** ステージから落ちたオブジェクトをステージ上に転移させる. */
  private void teleportObjectsDroppedOutOfStage() {
    for (SimulationObject obj : instances) {
      if (obj.getPosition().y >= verticalPosThreshold || obj instanceof Stage) {
        continue;
      }
      if (obj instanceof PhysicalEntity pe) {
        pe.resetPhysicalState();
      }
      Vector3 newObjPos = cameraTargetGetter.get();
      newObjPos.y = 1f;
      stage.clampPosXz(newObjPos);
      obj.setPosition(newObjPos);
    }
  }

  /** 現在シミュレーション空間内に追加されている 3D モデルの数を取得する. */
  public int getNumObjects() {
    return numObjects;
  }

  @Override
  public void dispose() {
    for (var disposable : disposables) {
      disposable.dispose();
    }
    for (SimulationObject instance : instances) {
      instance.dispose();
    }
  }

  @Override
  public Actor getUiView() {
    return uiComponent;
  }

  /** 追加可能な 3D モデルの最大個数を超えたときに投げられる例外. */
  public  static class MaxObjectsExceededException extends LimitExceededException {
    public MaxObjectsExceededException(String msg) {
      super(msg);
    }
  }
}
