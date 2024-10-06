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
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import net.seapanda.bunnyhop.simulator.obj.interfaces.SimulationObject;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * {@link ModelInstance} を特定のパラメータで描画する機能のみを提供する Simulation Object.
 * {@link #getRenderables(Array, Pool)} の処理中にのみ, {@link ModelInstance} の状態を変更する.
 * 同処理を終えると {@link ModelInstance} の状態は復元される.
 *
 * <p>このクラスのオブジェクトを作成したあとで, 描画対象の {@link ModelInstance} に変更を加えた場合, 
 * {@link #reconstruct()} を呼ぶこと.
 * このクラスの {@link #dispose()} を呼んでオブジェクトを破棄した後に, 
 * 描画対象の {@link ModelInstance} が持つ {@link Model} の {@link Model#dispose()} を呼ぶこと.
 *
 * @author K.Koike
 */
public class ObjectReflection implements SimulationObject {
  
  /** 描画対象. */
  private ModelInstance subject;
  /** 不透明度. */
  private float opacity = 0.5f;
  /** NodePart とその Material のペアを格納する. */
  private final HashMap<NodePart, Material> nodePartToOriginalMaterial = new HashMap<>();
  /** NodePart とその Material のコピーのペアを格納する. */
  private final HashMap<NodePart, Material> nodePartToCopiedMaterial = new HashMap<>();
  /**  3D モデルの位置. */
  private final Vector3 pos = new Vector3();
  /** このオブジェクトを含めた, 現在 {@code subject} を共有する {@link ObjectReflection} の個数. */
  private final MutableInt numShared;
  /** この 3D モデルの論理的な位置から描画位置を算出するときに呼ぶメソッド. */
  private Function<Vector3, Vector3> renderingPosGetter = (pos) -> pos;
  /** 選択状態を保持するフラグ. */
  private boolean isSelected = false;
  /** 選択されたときの色. */
  private final Attribute colorAttrOnSelected = 
      ColorAttribute.createEmissive(new Color(0.2f, 0.2f, 0.2f, 1.0f));
  private final Matrix4 rotation = new Matrix4();

  /**
   * コンストラクタ.
   *
   * @param subject 描画対象となる 3D モデル.
   * @param opacity 描画時の不透明度.
   * @param numShared このオブジェクトを含めた, 現在 {@code subject} を共有する {@link ObjectReflection} の個数.
   */
  public ObjectReflection(ModelInstance subject, float opacity, MutableInt numShared) {
    this.subject = subject;
    this.opacity = opacity;
    this.numShared = numShared;
    subject.transform.getTranslation(pos);
    reconstruct();
  }

  /** 描画対象の ModelInstance の状態をこの 3D モデルの状態に反映させる. */
  public void reconstruct() {
    var nodeParts = new ArrayList<NodePart>();
    for (Node node : subject.nodes) {
      gatherNodePart(node, nodeParts);
    }
    for (NodePart nodePart : nodeParts) {
      if (nodePart.material == null) {
        continue;
      }
      nodePartToOriginalMaterial.put(nodePart, nodePart.material);
      Material copied = nodePart.material.copy();
      nodePartToCopiedMaterial.put(nodePart, copied);
      if (copied.get(BlendingAttribute.Type) instanceof BlendingAttribute attr) {
        attr.opacity *= opacity;
      } else {
        copied.set(new BlendingAttribute(opacity));
      }
    }
  }

  /** 引数で指定した {@link Node} 以下の全ての {@link Node} の {@link NodePart} を集める.*/
  private void gatherNodePart(Node node, List<NodePart> storage) {
    for (Node child : node.getChildren()) {
      gatherNodePart(child, storage);
    }
    node.parts.forEach(storage::add);
  }

  /** 不透明度を返す. */
  public float getOpacity() {
    return opacity;
  }

  /** 不透明度を設定する. */
  public void setOpacity(float opacity) {
    this.opacity = opacity;
    for (Material mat : nodePartToCopiedMaterial.values()) {
      ((BlendingAttribute) mat.get(BlendingAttribute.Type)).opacity = opacity;
    }
  }

  /** この 3D モデルの描画位置を算出するメソッドを設定する. */
  public void setRenderingPosGetter(Function<Vector3, Vector3> renderingPosGetter) {
    this.renderingPosGetter = renderingPosGetter;
  }

  @Override
  public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
    var orgMatrix = new Matrix4(subject.transform);

    // 位置とマテリアルの書き換え.
    Vector3 renderingPos = renderingPosGetter.apply(pos);
    subject.transform.mul(rotation);
    subject.transform.setTranslation(renderingPos);
    nodePartToCopiedMaterial.entrySet()
        .forEach(entry -> entry.getKey().material = entry.getValue());
    // 描画物を提出.
    subject.getRenderables(renderables, pool);
    // 位置とマテリアルを元に戻す.
    subject.transform.set(orgMatrix);
    nodePartToOriginalMaterial.entrySet()
        .forEach(entry -> entry.getKey().material = entry.getValue());
  }

  @Override
  public Vector3 getPosition() {
    return new Vector3(pos);
  }

  @Override
  public void setPosition(Vector3 pos) {
    this.pos.set(pos);
  }

  @Override
  public void rotateEuler(float yaw, float pitch, float roll) {
    var diff = new Matrix4().setFromEulerAngles(yaw, pitch, roll);
    rotation.mul(diff);
  }

  @Override
  public void dispose() {
    numShared.decrement();
  }

  @Override
  public void select() { 
    isSelected = true;
    for (var material : nodePartToCopiedMaterial.values()) {
      material.set(colorAttrOnSelected);
    }
  }

  @Override
  public void deselect() {
    isSelected = false;
    for (var material : nodePartToCopiedMaterial.values()) {
      material.remove(ColorAttribute.Emissive);
    }
  }

  @Override
  public boolean isSelected() {
    return isSelected;
  }
}
