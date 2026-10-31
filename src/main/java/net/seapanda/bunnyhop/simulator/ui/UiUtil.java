package net.seapanda.bunnyhop.simulator.ui;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import java.nio.file.Paths;

/**
 * UI に関係する処理を集めたユーティリティクラス.
 *
 * @author K.Koike
 */
public class UiUtil {

  /** 物理 DPI. */
  public final float dpi;
  /** mm からピクセル数に変換するときに掛ける値. */
  public final float mm;
  /** mm に UI スケールを掛けた値. */
  public final float sclmm;
  /** ポイント数にかけるとモニタの物理解像度によらないポイント数になる値. */
  public final float pt;
  /* pt に UI スケールを掛けた値. */
  public final float sclpt;
  /** シミュレータのアセットのあるディレクトリのパス. */
  public final String assetDir;
  private final FreeTypeFontGenerator fontGenerator;

  /**
   * コンストラクタ.
   *
   * @param graphics 解像度の取得に使用する {@link Graphics} オブジェクト
   * @param uiScale UI のサイズの倍率
   * @param assetDirPath シミュレータのアセットが格納されたディレクトリのパス
   */
  public UiUtil(Graphics graphics, double uiScale, String assetDirPath) {
    float density = graphics.getDensity();
    dpi = density * 160f;
    mm =  density * 160f / 25.4f;
    sclmm =  (float) (mm * uiScale);
    pt = density * 160f / 72f;
    sclpt = (float) (pt * uiScale);

    this.assetDir = assetDirPath;
    String fontFilePath = assetDirPath + "/Images/GenShinGothic-Normal.ttf";
    var fileHandle = new FileHandle(Paths.get(fontFilePath).toAbsolutePath().toString());
    fontGenerator = new FreeTypeFontGenerator(fileHandle);
  }

  /** UI 部品のボタンを作成する. */
  public VisImageButton createUiButton(
      String imgPath, Vector2 size, float pad, ChangeListener listener) {
    var sprite = new Sprite(new Texture(imgPath));
    sprite.setSize(size.x, size.y);
    var drawable = new SpriteDrawable(sprite);
    var btn = new VisImageButton(drawable, drawable);
    btn.addListener(listener);
    btn.pad(pad);
    return btn;
  }

  /** UI 部品の画像ラベルを作成する. */
  public VisImage createUiImage(String imgPath, Vector2 size) {
    var sprite = new Sprite(new Texture(imgPath));
    sprite.setSize(size.x, size.y);
    var drawable = new SpriteDrawable(sprite);
    return new VisImage(drawable);
  }

  /** UI 部品のテキストラベルを作成する. */
  public VisLabel createLabel(String text, float fontSize, Color textColor) {
    var label = new VisLabel(text);
    var style = new LabelStyle(label.getStyle());
    style.font = createFont(text, fontSize, textColor);
    label.setStyle(style);
    return label;
  }

  /** UI 部品のフォントを作成する. */
  public BitmapFont createFont(String text, float fontSize, Color textColor) {
    var parameter = new FreeTypeFontParameter();
    parameter.characters = text;
    parameter.size = Math.round(fontSize);
    parameter.color = textColor;
    return fontGenerator.generateFont(parameter);
  }

  public void dispose() {
    fontGenerator.dispose();
  }
}
