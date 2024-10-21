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
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import java.util.List;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.utils.MaterialConverter;
import net.seapanda.bunnyhop.simulator.BhSimulator;
import net.seapanda.bunnyhop.simulator.geometry.GeoUtil;
import net.seapanda.bunnyhop.simulator.obj.interfaces.Collidable;

/**
 * ステージの 3D モデルを表すクラス.
 *
 * @author K.Koike
 */
public class Stage extends Collidable {
 
  private final btRigidBody body;
  private SceneAsset sceneAsset = new GLBLoader().load(
      Gdx.files.absolute(BhSimulator.ASSET_PATH + "/Models/Stage.glb"));
  private final Scene scene;
  private final Vector3 pos;
  private final Vector3 size = new Vector3(48.09744f, 0f, 48.09744f);
  /** ローカル空間上でのこのオブジェクトの論理的な原点. */
  private final Vector3 logicalOrigin = new Vector3(0, 0, 0);
  /** 選択状態を保持するフラグ. */
  private boolean isSelected = false;
  /** 選択されたときの色. */
  private final Attribute colorAttrOnSelected = 
      ColorAttribute.createEmissive(new Color(0.2f, 0.2f, 0.2f, 1.0f));

  /**
   * コンストラクタ.
   *
   * @param scale 3D モデルのスケール
   * @param pos  地面の上面の中心の位置
   */
  public Stage(float scale, Vector3 pos) {
    this.pos = pos;
    size.scl(scale);
    scene = createScene(scale, pos);
    btCollisionShape shape = createCollisionShape(scene.modelInstance);
    var motionState = new CustomMotionState(scene.modelInstance.transform);
    body = createRigidBody(shape, motionState);
  }

  /** 3次元モデルを作成する. */
  private Scene createScene(float scale, Vector3 pos) {
    var scene = new Scene(sceneAsset.scene);
    scene.modelInstance.transform.scl(scale);
    logicalOrigin.scl(scale);
    scene.modelInstance.transform.setTranslation(new Vector3(pos).sub(logicalOrigin));
    MaterialConverter.makeCompatible(scene);
    return scene;
  }

  private btCollisionShape createCollisionShape(ModelInstance modelInstance) {
    btCompoundShape shape = new btCompoundShape();
    List<Node> nodes = GeoUtil.addCollisionBoxes(
        shape,
        modelInstance,
        "ground-collision",
        "wall+x-collision",
        "wall-x-collision",
        "wall+z-collision",
        "wall-z-collision");
    nodes.forEach(node -> node.parts.get(0).enabled = false);
    return shape;
  }

  private btRigidBody createRigidBody(btCollisionShape shape, btMotionState motionState) {
    var rigidBody = new btRigidBody(0f, motionState, shape, new Vector3());
    rigidBody.setCollisionFlags(
        rigidBody.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_STATIC_OBJECT);
    rigidBody.setActivationState(Collision.DISABLE_DEACTIVATION);
    rigidBody.userData = this;
    rigidBody.setFriction(1.5f);
    return rigidBody;
  }

  /** 引数で指定した x, z 位置をステージの範囲内に収める. */
  public void clampPosXz(Vector3 pos) {
    float maxX = this.pos.x + size.x * 0.5f;
    float minX = this.pos.x - size.x * 0.5f;
    pos.x = Math.clamp(pos.x, minX, maxX);
    float maxZ = this.pos.z + size.z * 0.5f;
    float minZ = this.pos.z - size.z * 0.5f;
    pos.z = Math.clamp(pos.z, minZ, maxZ);
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

  @Override
  public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
    scene.getRenderables(renderables, pool);
  }

  @Override
  public void dispose() {
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
  public void addCollisionObjectsTo(btDynamicsWorld world) {
    world.addRigidBody(
        body,
        CollisionGroup.STAGE.val(),
        CollisionGroup.mask(
            CollisionGroup.PHYSICAL_ENTITY,
            CollisionGroup.PHYSICAL_CONTACT_DETECTOR));
  }

  @Override
  public void removeCollisionObjectsFrom(btDynamicsWorld world) {
    world.removeRigidBody(body);
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
}
