package net.seapanda.bunnyhop.simulator.obj.interfaces;

import net.seapanda.bunnyhop.simulator.obj.ObjectReflection;

/**
 * {@link ObjectReflection} オブジェクトを作成可能なクラスが持つインタフェース.
 *
 * @author K.Koike
 */
public interface ObjectReflectionProvider extends SimulationObject {

  /** この 3D モデルの {@link ObjectReflection} オブジェクトを作成する.*/
  public ObjectReflection createObjectReflection();
  
  /** この 3D モデルのリソースを共有する他の  {@link ObjectReflection} オブジェクト の数. */
  public int getNumShared();
}
