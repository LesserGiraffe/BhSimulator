package net.seapanda.bunnyhop.simulator.common;

import net.seapanda.bunnyhop.utility.version.AppVersion;

/**
 * BhSimulator の定数一式をまとめたクラス.
 *
 * @author K.Koike
 */
public class BhSimConstants {
  /** アプリケーションのバージョン. */
  public static final AppVersion APP_VERSION = AppVersion.of("bhsim-0.7.3");

  /** UI 関連のパラメータ. */
  public static class Ui {
    /** {@link com.badlogic.gdx.Graphics#getDensity} の戻り値がこの値を超えたとき, 2 倍の大きさの UI スキンを使う. */
    public static final float X2_SKIN_DPI_THRESHOLD = 122;
  }

  /** ファイルパス関連のパラメータ. */
  public static class Path {
    /** ディレクトリ名のリスト. */
    public static class Dir {
      /** 言語ファイルが格納されたディレクトリ. */
      public static final String LANGUAGE = "Language";
    }

    /** ファイル名のリスト. */
    public static class File {
      /** 言語ファイルの名前. */
      public static final String LANGUAGE_FILE = "BhSimulator.json";
    }
  }
}
