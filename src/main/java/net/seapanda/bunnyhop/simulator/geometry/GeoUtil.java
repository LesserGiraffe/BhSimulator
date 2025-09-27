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

package net.seapanda.bunnyhop.simulator.geometry;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import java.util.ArrayList;
import java.util.List;

/**
 * 幾何学的処理を集めたユーティリティクラス.
 *
 * @author K.Koike
 */
public class GeoUtil {

  /** 平面と直線の交点を求める. */
  public static Vector3 calcIntersectionOfPlaneAndLine(
      Vector3 planePos, Vector3 planeNormal, Vector3 linePos, Vector3 lineDirection) {
    float d = 
          planeNormal.x * (planePos.x - linePos.x)
        + planeNormal.y * (planePos.y - linePos.y)
        + planeNormal.z * (planePos.z - linePos.z);
    d /= 
          planeNormal.x * lineDirection.x
        + planeNormal.y * lineDirection.y
        + planeNormal.z * lineDirection.z;
    return new Vector3(lineDirection).scl(d).add(linePos);
  }

  /**
   * {@code modelInstance} から {@code nodeNames} に指定した名前の {@link Node} を探し, 
   * そのバウンディングボックスを算出する.
   * それと同じ大きさと位置を持つ {@link btBoxShape} を {@code container} に追加する.
   *
   * @param container {@link btBoxShape} を追加するオブジェクト.
   * @param modelInstance このオブジェクトから {@code nodeNames} で指定した名前の {@link Node} を探す.
   * @param nodeNames バウンディングボックスを計算する {@link Node} の名前.
   * @return {@code nodeNames} に指定した名前の {@link Node} のリスト.
   */
  public static List<Node> addCollisionBoxes(
      btCompoundShape container,
      ModelInstance modelInstance,
      String... nodeNames) {
    Vector3 scale = new Vector3();
    modelInstance.transform.getScale(scale);
    var nodes = new ArrayList<Node>();
    var bb = new BoundingBox();
    var size = new Vector3();
    var pos = new Vector3();
    for (String nodeName : nodeNames) {
      Node node = modelInstance.getNode(nodeName);
      if (node == null) {
        continue;
      }
      node.calculateBoundingBox(bb);
      size.set(bb.max).sub(bb.min).scl(scale).scl(0.5f);
      pos.set(bb.min).add(bb.max).scl(scale).scl(0.5f);
      var boxShape = new btBoxShape(size);
      boxShape.setMargin(0);
      container.addChildShape(new Matrix4().setTranslation(pos), boxShape);
      nodes.add(node);
    }
    return nodes;
  }
}
