package net.seapanda.bunnyhop.simulator.obj.interfaces;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

/** 
 * シミュレーション空間の 3Dモデルが持つ共通のインタフェース.
 *
 * @author K.Koike
 */
public interface SimulationObject extends RenderableProvider, Disposable {

  /** この 3D モデルの位置を取得する. */
  Vector3 getPosition();

  /** この 3D モデルの位置を設定する. */
  void setPosition(Vector3 pos);

  /** この 3D モデルの位置を設定する.  (単位: degrees) */
  void rotateEuler(float yaw, float pitch, float roll);

  /** この 3D モデルが傾きすぎていた場合, その回転をリセットする. */
  default void resetRotationIfTiltingOverly() {}

  /** このオブジェクトを選択状態にする. */
  void select();

  /** このオブジェクトを非選択状態にする. */
  void deselect();

  /** このオブジェクトが選択状態かどうか調べる. */
  boolean isSelected();
}
