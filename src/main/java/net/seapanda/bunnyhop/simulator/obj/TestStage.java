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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import net.seapanda.bunnyhop.simulator.obj.interfaces.Collidable;

/**
 * ステージの 3D モデルを表すクラス.
 *
 * @author K.Koike
 */
public class TestStage extends Collidable {

  private final ModelInstance modelInstance;
  private final btRigidBody body;
  private final float thickness = 2;
  private final float height = 2;
  private final Vector3 size;
  private final Vector3 pos;
  /** 選択状態を保持するフラグ. */
  private boolean isSelected = false;
  /** 選択されたときの色. */
  private final Attribute colorAttrOnSelected = 
      ColorAttribute.createEmissive(new Color(0.2f, 0.2f, 0.2f, 1.0f));

  /**
   * コンストラクタ.
   *
   * @param size 地面のサイズ
   * @param pos  地面の上面の中心の位置
   */
  public TestStage(Vector3 size, Vector3 pos) {
    this.size = size;
    this.pos = pos;
    Model model = createModel(size);
    modelInstance = new ModelInstance(model);
    modelInstance.transform.setTranslation(pos.x, pos.y - size.y * 0.5f, pos.z);
    var motionState = new CustomMotionState(modelInstance.transform);
    btCollisionShape shape = createCollisionShape(size);
    body = createRigidBody(shape, motionState);
  }


  private Model createModel(Vector3 size) {
    var material = new Material(ColorAttribute.createDiffuse(Color.BROWN));
    var mb = new ModelBuilder();
    mb.begin();

    var partSize = new Vector3(size.x, size.y, size.z);
    var translation = new Vector3(0f, 0f, 0f);
    createModelPart(mb, "ground", material, partSize, translation);

    material = new Material(ColorAttribute.createDiffuse(new Color(Color.BROWN).mul(0.8f)));
    partSize.set(thickness, height + size.y, size.z + 2 * thickness);
    translation.set((thickness + size.x) * -0.5f, height * 0.5f, 0f);
    createModelPart(mb, "wall-x", material, partSize, translation);

    translation.set((thickness + size.x) * 0.5f, height * 0.5f, 0f);
    createModelPart(mb, "wall+x", material, partSize, translation);

    partSize.set(size.x, height + size.y, thickness);
    translation.set(0f, height * 0.5f, (thickness + size.z) * -0.5f);
    createModelPart(mb, "wall-z", material, partSize, translation);

    translation.set(0f, height * 0.5f, (thickness + size.z) * 0.5f);
    createModelPart(mb, "wall+z", material, partSize, translation);
    return mb.end();
  }

  
  private void createModelPart(
      ModelBuilder mb, String id, Material material, Vector3 size, Vector3 translation) {
    mb.node().id = id;
    MeshPartBuilder mpb = mb.part(id, GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, material);
    BoxShapeBuilder.build(
        mpb, translation.x, translation.y, translation.z, size.x, size.y, size.z);
  }

  
  private btCollisionShape createCollisionShape(Vector3 size) {
    btCompoundShape shape = new btCompoundShape();
    var partSize = new Vector3(size.x, size.y, size.z);
    var translation = new Vector3(0f, 0f, 0f);
    var ground = new btBoxShape(partSize.scl(0.5f));
    shape.addChildShape(new Matrix4(), ground);

    var mat = new Matrix4();
    partSize.set(thickness, height + size.y, size.z + 2 * thickness).scl(0.5f);
    translation.set((thickness + size.x) * -0.5f, height * 0.5f, 0f);
    shape.addChildShape(mat.translate(translation), new btBoxShape(partSize));

    translation.set((thickness + size.x) * 0.5f, height * 0.5f, 0f);
    shape.addChildShape(mat.idt().translate(translation), new btBoxShape(partSize));

    partSize.set(size.x, height + size.y, thickness).scl(0.5f);
    translation.set(0f, height * 0.5f, (thickness + size.z) * -0.5f);
    shape.addChildShape(mat.idt().translate(translation), new btBoxShape(partSize));

    translation.set(0f, height * 0.5f, (thickness + size.z) * 0.5f);
    shape.addChildShape(mat.idt().translate(translation), new btBoxShape(partSize));

    return shape;
  }

  /** この 3D モデルの物理演算に使用する {@link btRigidBody} を作成する. */
  private btRigidBody createRigidBody(btCollisionShape shape, btMotionState motionState) {
    var localInertia = new Vector3();
    var rigidBody = new btRigidBody(0f, motionState, shape, localInertia);
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

  @Override
  public void dispose() {
    body.dispose(); // btMotionState, btCollisionShape も dispose される.
    modelInstance.model.dispose();
  }

  @Override
  public boolean isDraggable() {
    return false;
  }

  @Override
  public Vector3 getPosition() {
    var pos = new Vector3();
    body.getWorldTransform().getTranslation(pos);
    return pos;
  }

  @Override
  public void setPosition(Vector3 pos) {
    Matrix4 mat = body.getWorldTransform().setTranslation(pos);
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
  public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
    modelInstance.getRenderables(renderables, pool);
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
}
