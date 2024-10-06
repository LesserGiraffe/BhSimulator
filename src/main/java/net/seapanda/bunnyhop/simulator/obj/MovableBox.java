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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.kotcrab.vis.ui.widget.VisTable;
import net.seapanda.bunnyhop.simulator.obj.interfaces.ObjectReflectionProvider;
import net.seapanda.bunnyhop.simulator.obj.interfaces.PhysicalEntity;
import net.seapanda.bunnyhop.simulator.obj.interfaces.UiViewProvider;
import net.seapanda.bunnyhop.simulator.ui.MovableBoxCtrlView;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * 直方体の 3D モデルを作るクラス.
 *
 * @author K.Koike
 */
public class MovableBox
    extends PhysicalEntity implements ObjectReflectionProvider, UiViewProvider {
  
  private final ModelInstance modelInstance;
  private final btRigidBody body;
  /** この 3D モデルのリソースを共有する {@link ObjectReflection} オブジェクトの個数. */
  private final MutableInt numShared = new MutableInt(0);
  private Vector3 size;
  /** 選択状態を保持するフラグ. */
  private boolean isSelected = false;
  /** 選択されたときの色. */
  private final Attribute colorAttrOnSelected = 
      ColorAttribute.createEmissive(new Color(0.2f, 0.2f, 0.2f, 1.0f));
  /** UI のルートコンポーネント. */
  private VisTable uiComponent;

  /**
   * コンストラクタ.
   *
   * @param size 直方体のサイズ
   * @param pos 直方体の底面の中心点の位置
   */
  public MovableBox(Vector3 size, Vector3 pos) {
    this.size = size;
    Model model = createModel(size);
    modelInstance = new ModelInstance(model);
    modelInstance.transform.translate(pos.x, pos.y + size.y * 0.5f, pos.z);
    var motionState = new CustomMotionState(modelInstance.transform);
    btCollisionShape shape = new btBoxShape(new Vector3(size).scl(0.5f));
    body = createRigidBody(shape, motionState);
    uiComponent = new MovableBoxCtrlView(this);
  }

  
  private Model createModel(Vector3 size) {
    ModelBuilder builder = new ModelBuilder();
    builder.begin();
    builder.node();
    MeshPartBuilder mpb = builder.part(
        "box",
        GL20.GL_TRIANGLES,
        Usage.Position | Usage.Normal,
        new Material(ColorAttribute.createDiffuse(new Color(Color.BLUE).mul(0.5f))));
    BoxShapeBuilder.build(mpb, size.x, size.y, size.z);
    return builder.end();
  }


  private btRigidBody createRigidBody(btCollisionShape shape, btMotionState motionState) {
    var localInertia = new Vector3();
    var mass = 0.5f;
    shape.calculateLocalInertia(mass, localInertia);
    var rigidBody = new btRigidBody(mass, motionState, shape, localInertia.scl(2f));
    rigidBody.setCollisionFlags(
        rigidBody.getCollisionFlags()
        | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
    rigidBody.setActivationState(Collision.DISABLE_DEACTIVATION);
    rigidBody.setFriction(1.0f);
    rigidBody.userData = this;
    return rigidBody;
  }

  /** この 3D モデルの論理的な位置から描画位置を計算する. */
  private Vector3 calcRenderingPos(Vector3 pos) {
    float[] matVals = body.getWorldTransform().getValues();
    var newPos = new Vector3(
        matVals[Matrix4.M01], matVals[Matrix4.M11], matVals[Matrix4.M21]); // rotated Y
    return newPos.scl(size.y * 0.5f).add(pos);
  }

  /**  ローカル座標系の Y 軸とワールド座標系の Y 軸のなす角度 (radian) を求める. */
  private float calcTiltAngle() {
    float[] matVals = body.getWorldTransform().getValues();
    var rotatedY = new Vector3(
        matVals[Matrix4.M01], matVals[Matrix4.M11], matVals[Matrix4.M21]);
    return (float) Math.acos(Math.clamp(rotatedY.dot(0, 1, 0), -1, 1));
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
    modelInstance.getRenderables(renderables, pool);
  }

  @Override
  public void dispose() {
    body.dispose();
    modelInstance.model.dispose();
  }

  @Override
  public boolean isDraggable() {
    return true;
  }

  @Override
  public Vector3 getPosition() {
    var pos = new Vector3(0, -size.y * 0.5f, 0);
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
    modelInstance.materials.forEach(material -> material.remove(ColorAttribute.Emissive));
    var reflection =  new ObjectReflection(modelInstance, 0.5f, numShared);
    reflection.setRenderingPosGetter(this::calcRenderingPos);
    if (isSelected) {
      modelInstance.materials.forEach(material -> material.set(colorAttrOnSelected));
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
  }

  @Override
  public void removeCollisionObjectsFrom(btDynamicsWorld world) {
    world.removeRigidBody(body);
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
    modelInstance.materials.forEach(material -> material.set(colorAttrOnSelected));
  }

  @Override
  public void deselect() {
    isSelected = false;
    modelInstance.materials.forEach(material -> material.remove(ColorAttribute.Emissive));
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
