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
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ConeShapeBuilder;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.kotcrab.vis.ui.widget.VisTable;
import java.util.List;
import java.util.Optional;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.utils.MaterialConverter;
import net.seapanda.bunnyhop.simulator.BhSimulator;
import net.seapanda.bunnyhop.simulator.geometry.GeoUtil;
import net.seapanda.bunnyhop.simulator.obj.interfaces.ObjectReflectionProvider;
import net.seapanda.bunnyhop.simulator.obj.interfaces.PhysicalEntity;
import net.seapanda.bunnyhop.simulator.obj.interfaces.UiViewProvider;
import net.seapanda.bunnyhop.simulator.ui.LampCtrlView;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * 電灯の 3D モデルを作るクラス.
 *
 * @author K.Koike
 */
public class Lamp extends PhysicalEntity implements ObjectReflectionProvider, UiViewProvider {
 
  private SceneAsset sceneAsset = new GLBLoader().load(
      Gdx.files.absolute(BhSimulator.ASSET_PATH + "/Models/Lamp.glb"));
  private final Scene scene;
  private final float scale;
  /** ローカル空間上でのこのオブジェクトの論理的な原点. */
  private final Vector3 logicalOrigin = new Vector3(0, -0.03f, 0);
  /** light 部分のモデル. */
  private Model lightModel;
  /** 本体の衝突判定オブジェクト. */
  private final btRigidBody body;
  /** ライト部分の衝突判定オブジェクト. */
  private final btGhostObject lightCollisionObj;
  /** この 3D モデルのリソースを共有する {@link ObjectReflection} オブジェクトの個数. */
  private final MutableInt numShared = new MutableInt(0);
  /** ライトの円錐の半径. (単位: meters) */
  private float lightRadius = 0.24f;
  /** ライトの円錐の高さ. (単位: meters) */
  private float lightHeight = 0.24f;
  /** ライトの角度 (degrees). 0: 下向き,  90,-90: 水平*/
  private float lightAngle = 0;
  /** ローカル座標での光源の位置. */
  private final Vector3 lightSourcePos;
  /** ライト部分の {@link Node} の ID. */
  private final String lightNodeId = "light";
  /** ライトの回転軸. */
  private final Vector3 lightRotAxis = new Vector3(1f, 0f, 0f);
  /** 選択状態を保持するフラグ. */
  private boolean isSelected = false;
  /** 選択されたときの色. */
  private final Attribute colorAttrOnSelected = 
      ColorAttribute.createEmissive(new Color(0.2f, 0.2f, 0.2f, 1.0f));
  /** UI のルートコンポーネント. */
  private final VisTable uiComponent;

  /**
   * コンストラクタ.
   *
   * @param scale 3D モデルのスケール
   * @param pos 3D モデルの底面の中心点の位置
   */
  public Lamp(float scale, Vector3 pos) {
    this.scale = scale;
    scene = createScene(scale, pos);
    lightSourcePos = calcLightSourcePos(scene.modelInstance);
    btCollisionShape shape = createCollisionShape(scene.modelInstance);
    CustomMotionState motionState = new CustomMotionState(scene.modelInstance.transform);
    body = createRigidBody(shape, motionState);
    addLightNode(scene.modelInstance);
    lightCollisionObj = createLightCollisionObject();
    motionState.addOnWorldTransform(worldTrans -> {
      var trans = new Matrix4()
          .rotate(lightRotAxis, lightAngle)
          .setTranslation(lightSourcePos)
          .mulLeft(worldTrans);
      lightCollisionObj.setWorldTransform(trans);
    });
    uiComponent = new LampCtrlView(this);
  }

  /** 3D モデルを作成する. */
  private Scene createScene(float scale, Vector3 pos) {
    var scene = new Scene(sceneAsset.scene);
    scene.modelInstance.transform.scl(scale);
    logicalOrigin.scl(scale);
    scene.modelInstance.transform.setTranslation(new Vector3(pos).sub(logicalOrigin));
    MaterialConverter.makeCompatible(scene);
    for (var material : scene.modelInstance.materials) {
      material.set(ColorAttribute.createSpecular(Color.WHITE));
      material.set(FloatAttribute.createShininess(10f));
    }
    return scene;
  }

  /** ローカル座標系での光源の位置を算出する. */
  private Vector3 calcLightSourcePos(ModelInstance modelInstance) {
    var pos = new Vector3();
    var scl = new Vector3();
    modelInstance.getNode("bulb").globalTransform.getTranslation(pos);
    pos.scl(modelInstance.transform.getScale(scl));
    return pos;
  }

  /** ライト部分の {@link Node} を作成して, {@param modelInstance} に追加する. */
  private Node addLightNode(ModelInstance modelInstance) {
    ModelBuilder builder = new ModelBuilder();
    builder.begin();
    var material = new Material();
    material.set(ColorAttribute.createDiffuse(new Color(Color.WHITE)));
    material.set(new BlendingAttribute(0.3f));
    MeshPartBuilder mpb = builder.part(
        "cone",
        GL20.GL_TRIANGLES,
        Usage.Position | Usage.Normal,
        material);
    // 底面の半径 1 m, 高さ 1 m の円錐を作成する
    ConeShapeBuilder.build(mpb, 2, 1, 2, 20);
    lightModel = builder.end();
    Node coneNode = lightModel.nodes.get(0);
    coneNode.id = lightNodeId;
    modelInstance.getNode("bulb").addChild(coneNode);
    calcLightTransform();
    return coneNode;
  }

  /** ライト部分の 3D モデルの描画位置と姿勢を計算する. */
  private void calcLightTransform() {
    Node lightNode = scene.modelInstance.getNode(lightNodeId);
    var rotate = new Matrix4().rotate(lightRotAxis, lightAngle);
    lightNode.localTransform.idt()
        .setToScaling(lightRadius, lightHeight, lightRadius)
        .setTranslation(0f, -lightHeight / 2f, 0f)
        .mulLeft(rotate);    
    lightNode.calculateWorldTransform();
  }
 
  private btCollisionShape createCollisionShape(ModelInstance modelInstance) {
    btCompoundShape shape = new btCompoundShape();
    List<Node> nodes = 
        GeoUtil.addCollisionBoxes(shape, modelInstance, "base-collision", "pole-collision");
    nodes.forEach(node -> node.parts.get(0).enabled = false);
    return shape;
  }

  private btRigidBody createRigidBody(btCollisionShape shape, btMotionState motionState) {
    var localInertia = new Vector3();
    var mass = 0.1f;
    shape.calculateLocalInertia(mass, localInertia);
    var info = new btRigidBody.btRigidBodyConstructionInfo(mass, motionState, shape, localInertia);
    info.setAdditionalDamping(true);
    var rigidBody = new btRigidBody(info);
    info.dispose();
    rigidBody.setCollisionFlags(
        rigidBody.getCollisionFlags()
        | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
    rigidBody.setActivationState(Collision.DISABLE_DEACTIVATION);
    rigidBody.setFriction(0.5f);
    rigidBody.userData = this;
    return rigidBody;
  }

  /** ライト部分の衝突判定オブジェクトを作成する. */
  private btGhostObject createLightCollisionObject() {
    var shape = createLightCollisionShape();
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

  /** ライト部分の衝突判定形状を作成する. */
  private btCollisionShape createLightCollisionShape() {
    // 衝突判定オブジェクトが見た目より若干大きくなるので,
    // モデルの大きさが等倍の時に正確に衝突判定できるように補正係数を設ける.
    var coneShape = new btConeShape(1f, 1f);
    coneShape.setMargin(0);
    var shape = new btCompoundShape();
    var coneApexPosOffset = new Vector3(0, -1f / 2f, 0);
    shape.addChildShape(new Matrix4().setTranslation(coneApexPosOffset), coneShape);
    shape.setLocalScaling(new Vector3(lightRadius, lightHeight, lightRadius).scl(scale));
    return shape;
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

  /**  ローカル座標系の Y 軸とワールド座標系の Y 軸のなす角度 (radian) を求める. */
  private float calcTiltAngle() {
    float[] matVals = body.getWorldTransform().getValues();
    var rotatedY = new Vector3(
        matVals[Matrix4.M01], matVals[Matrix4.M11], matVals[Matrix4.M21]);
    return (float) Math.acos(Math.clamp(rotatedY.dot(0, 1, 0), -1, 1));
  }

  /** ライトの角度を設定する. */
  public void setLightAngle(float degrees) {
    this.lightAngle = degrees;
    calcLightTransform();
  }

  /** ライトの角度を取得する. (単位: degrees) */
  public float getLightAngle() {
    return lightAngle;
  }

  /** ライトの 3D モデルの円錐の半径を設定する. (単位: meters) */
  public void setLightRadius(float radius) {
    if (radius <= 0) {
      return;
    }
    lightRadius = radius;
    lightCollisionObj.getCollisionShape()
        .setLocalScaling(new Vector3(lightRadius, lightHeight, lightRadius).scl(scale));
    calcLightTransform();
  }

  /** ライトの 3D モデルの円錐の半径を取得する. (単位: meters) */
  public float getLightRadius() {
    return lightRadius;
  }

  /** ライトの 3D モデルの高さを設定する. (単位: meters) */
  public void setLightHeight(float height) {
    if (height <= 0) {
      return;
    }
    lightHeight = height;
    lightCollisionObj.getCollisionShape()
        .setLocalScaling(new Vector3(lightRadius, lightHeight, lightRadius).scl(scale));
    calcLightTransform();
  }

  /** ライトの 3D モデルの円錐の高さを取得する. (単位: meters) */
  public float getLightHeight() {
    return lightHeight;
  }

  /**
   * ライトの色を取得する.
   *
   * @return ライトの色. ライトが消えている場合 empty を返す.
   */
  public Optional<Color> getLightColor() {
    if (!isLightOn()) {
      return Optional.empty();
    }
    Material material = scene.modelInstance.getNode(lightNodeId).parts.get(0).material;
    if (material.get(ColorAttribute.Diffuse) instanceof ColorAttribute attr) {
      return Optional.of(new Color(attr.color));
    }
    throw new AssertionError("Lamp light color is not set.");
  }

  /** ライトの色を設定する. */
  public void setLightColor(Color color) {
    Material material = scene.modelInstance.getNode(lightNodeId).parts.get(0).material;
    material.set(ColorAttribute.createDiffuse(color));
  }

  /** ライトをつける. */
  public void turnOn() {
    scene.modelInstance.getNode(lightNodeId).parts.get(0).enabled = true;
  }

  /** ライトを消す. */
  public void turnOff() {
    scene.modelInstance.getNode(lightNodeId).parts.get(0).enabled = false;
  }

  /** ライトが点いている場合 true を返す. */
  public boolean isLightOn() {
    return scene.modelInstance.getNode(lightNodeId).parts.get(0).enabled;
  }

  /** 回転をリセットする. */
  private void resetRotation() {
    Matrix4 mat = body.getWorldTransform();
    Vector3 translation = mat.getTranslation(new Vector3());
    mat.set(new Matrix3().idt()).setTranslation(translation);
    body.setWorldTransform(mat);
    body.getMotionState().setWorldTransform(mat);
  }

  @Override
  public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
    scene.getRenderables(renderables, pool);
  }

  @Override
  public void dispose() {
    lightModel.dispose();
    body.dispose();
    sceneAsset.dispose();
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
    var reflection =  new ObjectReflection(scene.modelInstance, 0.5f, numShared);
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
  public void addCollisionObjectsTo(btDynamicsWorld world) {
    world.addRigidBody(
        body,
        CollisionGroup.PHYSICAL_ENTITY.val(),
        CollisionGroup.mask(
            CollisionGroup.PHYSICAL_ENTITY,
            CollisionGroup.STAGE,
            CollisionGroup.PHYSICAL_CONTACT_DETECTOR));
    world.addCollisionObject(
        lightCollisionObj, CollisionGroup.LAMP_LIGHT.val(), CollisionGroup.LAMP_LIGHT.val());
  }

  @Override
  public void removeCollisionObjectsFrom(btDynamicsWorld world) {
    world.removeRigidBody(body);
    world.removeCollisionObject(lightCollisionObj);
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
  public Actor getUiView() {
    return uiComponent;
  }
}
