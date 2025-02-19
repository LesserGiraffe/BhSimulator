package net.seapanda.bunnyhop.simulator.obj.interfaces;

/** 
 * 物理演算の対象となる 3D モデルの基底クラス.
 *
 * @author K.Koike
 */
public abstract class PhysicalEntity extends Collidable {
  
  /** 姿勢, 速度, 角速度などを初期状態に戻す. */
  public abstract void resetPhysicalState();
}
