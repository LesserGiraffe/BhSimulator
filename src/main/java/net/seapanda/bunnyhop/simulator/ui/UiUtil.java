package net.seapanda.bunnyhop.simulator.ui;

import com.badlogic.gdx.Gdx;
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
import net.seapanda.bunnyhop.simulator.BhSimulator;

/**
 * UI に関係する処理を集めたユーティリティクラス.
 *
 * @author K.Koike
 */
public class UiUtil {

  public static VisImageButton button;

  public static final float dpi = Gdx.graphics.getDensity() * 160f;
  public static final float mm = Gdx.graphics.getDensity() * 160f / 25.4f;
  public static final float ptScale = Gdx.graphics.getDensity() * 160f / 72f;
  private static String fontPath = BhSimulator.ASSET_PATH + "/Images/GenShinGothic-Normal.ttf";
  private static FreeTypeFontGenerator fontGenerator = 
      new FreeTypeFontGenerator(Gdx.files.absolute(fontPath));

  /** UI 部品のボタンを作成する. */
  public static VisImageButton createUiButton(
      String imgPath, Vector2 size, float pad, ChangeListener listener) {
    var sprite = new Sprite(new Texture(imgPath));
    sprite.setSize(size.x, size.y);
    var drawable = new SpriteDrawable(sprite);
    var btn = new VisImageButton(drawable, drawable);
    btn.addListener(listener);
    btn.pad(pad);
    button = btn;
    return btn;
  }

  /** UI 部品の画像ラベルを作成する. */
  public static VisImage createUiImage(String imgPath, Vector2 size) {
    var sprite = new Sprite(new Texture(imgPath));
    sprite.setSize(size.x, size.y);
    var drawable = new SpriteDrawable(sprite);
    return new VisImage(drawable);
  }

  /** UI 部品のテキストラベルを作成する. */
  public static VisLabel createLabel(String text, float fontSize, Color textColor) {
    var label = new VisLabel(text);
    var style = new LabelStyle(label.getStyle());
    style.font = createFont(text, fontSize, textColor);
    label.setStyle(style);
    return label;
  }

  /** UI 部品のフォントを作成する. */
  public static BitmapFont createFont(String text, float fontSize, Color textColor) {
    var parameter = new FreeTypeFontParameter();
    parameter.characters = text;
    parameter.size = toScaledPt(fontSize);
    parameter.color = textColor;
    return fontGenerator.generateFont(parameter);
  }

  public static void dispose() {
    fontGenerator.dispose();
  }

  /** ポイント数 {@code pt} を画面の解像度を考慮したポイントに直す. */
  private static int toScaledPt(float pt) {
    return Math.round(ptScale * pt);
  }
}
