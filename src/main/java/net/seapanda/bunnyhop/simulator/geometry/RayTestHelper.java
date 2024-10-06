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

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.AllHitsRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectConstArray;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.physics.bullet.collision.btTriangleRaycastCallback;
import com.badlogic.gdx.physics.bullet.linearmath.btScalarArray;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.seapanda.bunnyhop.simulator.obj.CollisionGroup;
import net.seapanda.bunnyhop.simulator.obj.interfaces.Collidable;

/**
 * ray test のためのユーティリティメソッドを提供するクラス.
 *
 * @author K.Koike
 */
public class RayTestHelper {

  private btCollisionWorld world;

  public RayTestHelper(btCollisionWorld world) {
    this.world = world;
  }

  /**
   * 引数で指定した線分と交差する {@link Collidable} を取得する.
   * 複数ある場合は交差点が ray の原点に最も近いものを返す.
   *
   * @param ray       線分の始点と方向を格納する {@link Ray}
   * @param len       線分の長さ
   * @param collideTo 交差判定を行う対象のグループ
   * @return ray と交差する {@link Collidable}. 何とも交差しなかった場合は empty.
   */
  public Optional<RayTestResult<Collidable>> getIntersectedCollidable(
      Ray ray, float len, CollisionGroup... collideTo) {
    return getIntersectedCollidable(ray, len, new Config().add(collideTo));
  }

  /**
   * 引数で指定した線分と交差する {@link Collidable} を取得する.
   * 複数ある場合は交差点が ray の原点に最も近いものを返す.
   *
   * @param rayStart  線分の始点の座標
   * @param rayEnd    線分の終点の座標
   * @param collideTo 交差判定を行う対象のグループ
   * @return ray と交差する {@link Collidable}. 何とも交差しなかった場合は empty.
   */
  public Optional<RayTestResult<Collidable>> getIntersectedCollidable(
      Vector3 rayStart, Vector3 rayEnd, CollisionGroup... collideTo) {
    return getIntersectedCollidable(rayStart, rayEnd, new Config().add(collideTo));
  }

  /**
   * 引数で指定した線分と交差する {@link Collidable} を取得する.
   * 複数ある場合は交差点が ray の原点に最も近いものを返す.
   *
   * @param ray    線分の始点と方向を格納する {@link Ray}
   * @param len    線分の長さ
   * @param config ray test の設定を格納したオブジェクト
   * @return ray と交差する {@link Collidable}. 何とも交差しなかった場合は empty.
   */
  public Optional<RayTestResult<Collidable>> getIntersectedCollidable(
      Ray ray, float len, Config config) {
    var rayEnd = new Vector3(ray.origin).add(ray.direction.scl(len));
    return getIntersectedCollidable(ray.origin, rayEnd, config);
  }

  /**
   * 引数で指定した線分と交差する {@link Collidable} を取得する.
   * 複数ある場合は交差点が ray の原点に最も近いものを返す.
   *
   * @param rayStart 線分の始点の座標
   * @param rayEnd   線分の終点の座標
   * @param config   ray test の設定を格納したオブジェクト
   * @return ray と交差する {@link Collidable}. 何とも交差しなかった場合は empty.
   */
  public Optional<RayTestResult<Collidable>> getIntersectedCollidable(
      Vector3 rayStart, Vector3 rayEnd, Config config) {
    var result = new AllHitsRayResultCallback(rayStart, rayEnd);
    result.setFlags(btTriangleRaycastCallback.EFlags.kF_FilterBackfaces);
    result.setCollisionFilterGroup(config.mask);
    result.setCollisionFilterMask(config.mask);
    world.rayTest(rayStart, rayEnd, result);
    return getRayTestResult(result, config);
  }

  private Optional<RayTestResult<Collidable>> getRayTestResult(AllHitsRayResultCallback result,
      Config config) {
    if (!result.hasHit()) {
      return Optional.empty();
    }
    // 配列のインデックスを交点までの距離に応じて並べ替える.
    btScalarArray distanceList = result.getHitFractions();
    List<Integer> indices = IntStream.iterate(0, n -> n + 1).limit(distanceList.size()).boxed()
        .collect(Collectors.toCollection(ArrayList::new));
    indices.sort((a, b) -> (distanceList.atConst(a) > distanceList.atConst(b)) ? 1 : -1);

    btCollisionObjectConstArray collisionObjects = result.getCollisionObjects();
    for (int i : indices) {
      if (collisionObjects.atConst(i).userData instanceof Collidable obj) {
        if (config.classesToExclude.contains(obj.getClass())
            || config.objsToExclude.contains(obj)) {
          continue;
        }
        var pos = new Vector3().set(result.getHitPointWorld().at(i));
        return Optional.of(new RayTestResult<Collidable>(obj, pos));
      }
    }
    return Optional.empty();
  }

  /**
   * ray test の結果を格納するクラス.
   *
   * @param intersected ray と交差した 3D モデル
   * @param pos         交差点
   */
  public record RayTestResult<E extends Collidable>(E intersected, Vector3 pos) {
  }

  /** ray test の設定を格納するクラス. */
  public static class Config {
    private Collection<Class<? extends Collidable>> classesToExclude = new ArrayList<>();
    private Collection<Collidable> objsToExclude = new ArrayList<>();
    private int mask = 0;

    /** ray test の衝突判定から除外するオブジェクトのクラスを追加する. */
    @SuppressWarnings("unchecked")
    public Config add(Class<? extends Collidable>... toExclude) {
      classesToExclude.addAll(Arrays.asList(toExclude));
      return this;
    }

    /** ray test の衝突判定から除外するオブジェクトを追加する. */
    public Config add(Collidable... toExclude) {
      objsToExclude.addAll(Arrays.asList(toExclude));
      return this;
    }

    /** ray test の衝突判定対象にする {@link CollisionGroup} を追加する. */
    public Config add(CollisionGroup... toCollideWith) {
      for (var flag : toCollideWith) {
        mask |= flag.val();
      }
      return this;
    }
  }
}
