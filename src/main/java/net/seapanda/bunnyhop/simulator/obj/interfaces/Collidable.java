package net.seapanda.bunnyhop.simulator.obj.interfaces;

import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btPersistentManifold;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import java.util.HashMap;

/**
 * 衝突判定を持つ 3D モデルの基底クラス.
 *
 * @author K.Koike
 */
public abstract class Collidable implements SimulationObject {

  /** 衝突点を格納するオブジェクトと衝突中のオブジェクトのペアのマップ. */
  protected final HashMap<btPersistentManifold, CollisionObjPair> manifoldToCollisionPair = 
      new HashMap<>();

  /** 引数で指定した {@link btDynamicWorld} オブジェクトに, この 3D モデルが持つ衝突判定オブジェクトを追加する. */
  public abstract void addCollisionObjectsTo(btDynamicsWorld world);

  /** 引数で指定した {@link btDynamicWorld} オブジェクトに, この 3D モデルが持つ衝突判定オブジェクトを追加する. */
  public abstract void removeCollisionObjectsFrom(btDynamicsWorld world);

  /** この 3D モデルをマウスドラッグで移動可能な場合 true を返す. */
  public abstract boolean isDraggable();

  /** 
   * 衝突開始時の処理を行うメソッド. 
   *
   * @param manifold {@code self} と {@code theOther} の衝突点情報を格納するオブジェクト.
   * @param self この 3D モデルに含まれる衝突判定オブジェクト
   * @param theOther この 3D モデルと衝突を開始したオブジェクト
   */
  public void onContactStarted(
      btPersistentManifold manifold, btCollisionObject self, btCollisionObject theOther) {
    manifoldToCollisionPair.put(manifold, new CollisionObjPair(self, theOther));
  }

  /** 
   * 衝突終了時の処理を行うメソッド. 
   *
   * @param manifold {@code self} と {@code theOther} の衝突点情報を格納するオブジェクト.
   * @param self この 3D モデルに含まれる衝突判定オブジェクト
   * @param theOther この 3D モデルと衝突を終了したオブジェクト
   */
  public void onContactEnded(
      btPersistentManifold manifold, btCollisionObject self, btCollisionObject theOther) {
    manifoldToCollisionPair.remove(manifold);
  }

  /** この 3D モデルと交差している {@link Collidable} を返す. */
  public Iterable<Collidable> getIntersectedCollidables() {
    return manifoldToCollisionPair.values().stream()
      .filter(pair -> pair.obj1.userData instanceof Collidable)
      .map(pair -> (Collidable) pair.obj1.userData)
      .toList();
  }

  /** 衝突判定オブジェクトのペア. */
  protected record CollisionObjPair(btCollisionObject obj0, btCollisionObject obj1) { }
}
