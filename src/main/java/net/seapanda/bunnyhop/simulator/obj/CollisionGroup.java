package net.seapanda.bunnyhop.simulator.obj;

/**
 * 衝突判定フィルタリング用のフラグ.
 *
 * @author K.Koike
 */
public enum CollisionGroup {
  PHYSICAL_ENTITY,
  PHYSICAL_CONTACT_DETECTOR,
  STAGE,
  LAMP_LIGHT;

  public int val() {
    return 1 << this.ordinal();
  }

  /** {@code flags} に指定した値からビットフィールドを作成する. */
  public static int mask(CollisionGroup... flags) {
    int ret = 0;
    for (CollisionGroup flag : flags) {
      ret |= flag.val();
    }
    return ret;
  }

  /** {@code flags} に指定した値からビットフィールドを作成する. */
  public static int mask(Iterable<CollisionGroup> flags) {
    int ret = 0;
    for (CollisionGroup flag : flags) {
      ret |= flag.val();
    }
    return ret;
  }
}
