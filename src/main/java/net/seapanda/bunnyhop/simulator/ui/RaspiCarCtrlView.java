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

package net.seapanda.bunnyhop.simulator.ui;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisTextField.VisTextFieldStyle;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.SimpleFloatSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.seapanda.bunnyhop.simulator.BhSimulator;
import net.seapanda.bunnyhop.simulator.common.TextDefs;
import net.seapanda.bunnyhop.simulator.obj.RaspiCar;
import org.apache.commons.lang3.StringUtils;

/**
 * {@link RaspiCar} をコントロールする UI コンポーネントを持つ View.
 *
 * @author K.Koike
 */
public class RaspiCarCtrlView extends VisTable {

  private float currentRotationVal = 0;
  /** 現在選択されている移動速度レベル. */
  private int speedLevel = 8;
  /** 現在選択されている移動時間. (秒) */
  private float moveTime = 2f;
  /** このコントロールビューで操作する {@link RaspiCar} オブジェクト. */
  private final RaspiCar model;
  /** 目を選択する UI コンポーネント. */
  private VisSelectBox<EyeToSetFunc> eyeSelector;
  private final HashMap<Color, String> colorToName = new HashMap<>() { {
      put(Color.BLACK, TextDefs.ObjCtrl.Color.black.get());
      put(Color.RED, TextDefs.ObjCtrl.Color.red.get());
      put(Color.GREEN, TextDefs.ObjCtrl.Color.green.get());
      put(Color.BLUE, TextDefs.ObjCtrl.Color.blue.get());
      put(Color.MAGENTA, TextDefs.ObjCtrl.Color.magenta.get());
      put(Color.CYAN, TextDefs.ObjCtrl.Color.cyan.get());
      put(Color.YELLOW, TextDefs.ObjCtrl.Color.yellow.get());
      put(Color.WHITE, TextDefs.ObjCtrl.Color.white.get());
    } };

  /**
   * コンストラクタ.
   *
   * @param model このコントロールビューで操作する {@link RaspiCar} オブジェクト.
   */
  public RaspiCarCtrlView(RaspiCar model) {
    this.model = model;
    addRotationSlider();
    row();
    addSpeedLevelSelector();
    row();
    addMoveTimeSelector();
    row();
    addMoveController();
    row();
    addDistanceRuler();
    row();
    addColorPicker();
    row();
    addEyeColorSelector();
  }

  /** Yaw 軸周りに回転させるスライダを追加する. */
  private void addRotationSlider() {
    String imgPath = BhSimulator.ASSET_PATH + "/Images/rotation.png";
    var size = new Vector2(16f * UiUtil.sclmm, 8.47f * UiUtil.sclmm);
    this.<VisImage>add(UiUtil.createUiImage(imgPath, size)).space(2 * UiUtil.sclmm);

    VisSlider slider = new VisSlider(0f, 60f, 1f, false);
    slider.setValue(slider.getMaxValue() / 2);
    currentRotationVal = slider.getValue();
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        float angle = (currentRotationVal - slider.getValue()) * (360 / slider.getMaxValue());
        model.rotateEuler(angle, 0f, 0f);
        currentRotationVal = slider.getValue();
      }
    };
    slider.addListener(listener);
    this.<VisSlider>add(slider).width(30f * UiUtil.sclmm);
  }

  /** 移動操作を行う UI コンポーネントを追加する. */
  private void addMoveController() {
    var movePane = new VisTable();
    // 前進
    VisImageButton button = genMoveButton(model::moveForward, "keyW.png");
    movePane.<VisImageButton>add(button).colspan(3);
    movePane.row();
    // 左回転
    button = genMoveButton(model::turnLeft, "keyA.png");
    movePane.<VisImageButton>add(button);
    // RaspiCar 画像
    String imgPath = BhSimulator.ASSET_PATH + "/Images/" + "raspicarMove.png";
    var size = new Vector2(25 * UiUtil.sclmm, 22f * UiUtil.sclmm);
    VisImage image = UiUtil.createUiImage(imgPath, size);
    movePane.<VisImage>add(image).space(0.5f * UiUtil.sclmm);
    // 右回転
    button = genMoveButton(model::turnRight, "keyD.png");
    movePane.<VisImageButton>add(button);
    movePane.row();
    // 後退
    button = genMoveButton(model::moveBackward, "keyS.png");
    movePane.add("");
    movePane.<VisImageButton>add(button).top();
    // 停止
    movePane.<VisTable>add(genStopController()).padTop(-5 * UiUtil.sclmm);
    // 移動操作を行う UI コンポーネントを親要素に追加
    this.<VisTable>add(movePane).space(2 * UiUtil.sclmm).colspan(2);
  }

  /** 移動ボタンを作成する. */
  private VisImageButton genMoveButton(BiConsumer<Float, Float> fnMove, String imgFileName) {
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        fnMove.accept((float) speedLevel, moveTime);
      }
    };
    var size = new Vector2(10.9f * UiUtil.sclmm, 10.9f * UiUtil.sclmm);
    String imgPath = BhSimulator.ASSET_PATH + "/Images/" + imgFileName;
    return UiUtil.createUiButton(imgPath, size, 0.5f * UiUtil.sclmm, listener);
  }

  /** 移動停止を行う UI 部品を作成する. */
  private VisTable genStopController() {
    var stopPane = new VisTable();
    String imgPath = BhSimulator.ASSET_PATH + "/Images/" + "stopMoving.png";
    var size = new Vector2(10f * UiUtil.sclmm, 10f * UiUtil.sclmm);
    VisImage image = UiUtil.createUiImage(imgPath, size);
    stopPane.<VisImage>add(image).padBottom(1 * UiUtil.sclmm);
    stopPane.row();

    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        model.stopMoving();
      }
    };
    size = new Vector2(10.9f * UiUtil.sclmm, 10.9f * UiUtil.sclmm);
    imgPath = BhSimulator.ASSET_PATH + "/Images/" + "keyF.png";
    VisImageButton button = UiUtil.createUiButton(imgPath, size, 0.5f * UiUtil.sclmm, listener);
    stopPane.<VisImageButton>add(button);
    return stopPane;
  }

  /** 移動速度選択コンポーネントを追加する. */
  private void addSpeedLevelSelector() {
    VisLabel label = UiUtil.createLabel(
        TextDefs.ObjCtrl.RaspiCar.moveSpeed.get(), 13.2f * UiUtil.sclpt, Color.WHITE);
    this.<VisLabel>add(label);
    var intModel = new IntSpinnerModel(speedLevel, 1, 10);
    var speedSel = new Spinner("", intModel);
    speedSel.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        speedLevel = intModel.getValue();  
      }
    });
    this.<Spinner>add(speedSel).space(2 * UiUtil.sclmm); ;
  }

  /** 移動時間選択コンポーネントを追加する. */
  private void addMoveTimeSelector() {
    VisLabel label = UiUtil.createLabel(
        TextDefs.ObjCtrl.RaspiCar.moveTime.get(), 13.2f * UiUtil.sclpt, Color.WHITE);
    this.<VisLabel>add(label);
    var floatModel = new SimpleFloatSpinnerModel(moveTime, 0.5f, 10f, 0.5f);
    var moveTimeSel = new Spinner("", floatModel);
    moveTimeSel.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        moveTime = floatModel.getValue();  
      }
    });
    this.<Spinner>add(moveTimeSel).space(2 * UiUtil.sclmm);
  }

  /** 前方の障害物までの距離を測る UI コンポーネントを追加する. */
  private void addDistanceRuler() {
    VisTextField textField = new VisTextField("");
    textField.setDisabled(true);
    VisTextFieldStyle style = textField.getStyle();
    style.font = UiUtil.createFont("0123456789. cm", 13.2f * UiUtil.sclpt, Color.WHITE);
    style.disabledFontColor = Color.WHITE;
    textField.setStyle(style);

    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        float distance = model.measureDistance() * 100;
        textField.setText(new DecimalFormat("#.#").format(distance) + " cm");
      }
    };
    String imgPath = BhSimulator.ASSET_PATH + "/Images/ruler.png";
    VisImageButton btn = UiUtil.createUiButton(
        imgPath, new Vector2(14f * UiUtil.sclmm, 8f * UiUtil.sclmm), UiUtil.sclmm, listener);
    this.<VisImageButton>add(btn).space(2f * UiUtil.sclmm);
    this.<VisTextField>add(textField).width(25f * UiUtil.sclmm);
  }

  /** 目の色を選択する UI コンポーネントを追加する. */
  private void addEyeColorSelector() {
    addEyeSelector();
    VisTable btnTable = new VisTable();
    Color[] colors = { Color.RED, Color.GREEN, Color.BLUE, Color.CYAN };
    for (var color : colors) {
      btnTable.add(genLightColorButton(color)).space(UiUtil.sclmm);
    }
    btnTable.row();
    colors = new Color[] { Color.MAGENTA, Color.YELLOW, Color.WHITE, Color.BLACK };
    for (var color : colors) {
      btnTable.add(genLightColorButton(color)).space(UiUtil.sclmm);
    }
    this.add(btnTable);
  }

  private void addEyeSelector() {
    eyeSelector = new VisSelectBox<EyeToSetFunc>();
    String[] eyeNames = {
        TextDefs.ObjCtrl.RaspiCar.bothEyes.get(),
        TextDefs.ObjCtrl.RaspiCar.rightEye.get(),
        TextDefs.ObjCtrl.RaspiCar.leftEye.get(),
    };
    eyeSelector.setItems(
      new EyeToSetFunc(eyeNames[0], model::setBothEyesColor),
      new EyeToSetFunc(eyeNames[1], model::setRightEyeColor),
      new EyeToSetFunc(eyeNames[2], model::setLeftEyeColor));
    var style = new SelectBoxStyle(eyeSelector.getStyle());
    style.font = UiUtil.createFont(StringUtils.join(eyeNames), 13.2f * UiUtil.sclpt, Color.WHITE);
    style.listStyle.font = style.font;
    eyeSelector.setStyle(style);
    this.add(eyeSelector).space(2f * UiUtil.sclmm);
  }

  /** 目の色を選択するボタンを追加する. */
  private VisImageButton genLightColorButton(Color color) {
    var pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(color);
    pixmap.fill();
    var sprite = new Sprite(new Texture(pixmap));
    sprite.setSize(5f * UiUtil.sclmm, 5f * UiUtil.sclmm);
    var drawable = new SpriteDrawable(sprite);
    VisImageButton btn = new VisImageButton(drawable);
    btn.pad(UiUtil.sclmm);
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        var col = color.equals(Color.BLACK) ? null : color;
        eyeSelector.getSelected().fnSetEyeColor.accept(col);
      }
    };
    btn.addListener(listener);
    return btn;
  }

  /** {@code keycode} に応じて {@link RaspiCar} を操作する. */
  public boolean keyDown(int keycode)  {
    if (keycode == Keys.W) {
      model.moveForward((float) speedLevel, moveTime);
      return true;
    } else if (keycode == Keys.S) {
      model.moveBackward((float) speedLevel, moveTime);
      return true;
    } else if (keycode == Keys.D) {
      model.turnRight((float) speedLevel, moveTime);
      return true;
    } else if (keycode == Keys.A) {
      model.turnLeft((float) speedLevel, moveTime);
      return true;
    } else if (keycode == Keys.F) {
      model.stopMoving();
      return true;
    }
    return false;
  }

  /** 色センサの値を取得する UI コンポーネントを追加する. */
  private void addColorPicker() {
    VisTextField textField = new VisTextField("");
    textField.setDisabled(true);
    var style = new VisTextFieldStyle(textField.getStyle());
    style.disabledFontColor = Color.WHITE;
    String colorChars = StringUtils.join(colorToName.values().toArray(new String[0]));
    style.font = UiUtil.createFont(colorChars, 13.2f * UiUtil.sclpt, Color.WHITE);
    textField.setStyle(style);

    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        String colorName = colorToName.getOrDefault(model.detectColor(), "");
        textField.setText(colorName);
      }
    };
    String imgPath = BhSimulator.ASSET_PATH + "/Images/colorPicker.png";
    VisImageButton btn = UiUtil.createUiButton(
        imgPath, new Vector2(9f * UiUtil.sclmm, 9f * UiUtil.sclmm), UiUtil.sclmm, listener);
    this.<VisImageButton>add(btn).space(2 * UiUtil.sclmm);
    this.<VisTextField>add(textField).width(25 * UiUtil.sclmm);
  }

  record EyeToSetFunc(String eyeName, Consumer<Color> fnSetEyeColor) {
    @Override
    public String toString() {
      return eyeName;
    }
  }
}
