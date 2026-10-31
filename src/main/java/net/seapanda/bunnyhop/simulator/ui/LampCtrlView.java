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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import net.seapanda.bunnyhop.simulator.obj.Lamp;

/**
 * {@link Lamp} をコントロールする UI コンポーネントを持つ View.
 *
 * @author K.Koike
 */
public class LampCtrlView extends VisTable {

  private float currentRotationVal = 0;

  /**
   * コンストラクタ.
   *
   * @param model このコントロールビューで操作する {@link Lamp} オブジェクト.
   * @param uiUtil UI 構築のためのユーティリティオブジェクト
   */
  public LampCtrlView(Lamp model, UiUtil uiUtil) {
    addRotationSlider(model, uiUtil);
    row();
    addLightAngleSlider(model, uiUtil);
    row();
    addLightRadiusSlider(model, uiUtil);
    row();
    addLightHeightSlider(model, uiUtil);
    row();
    addLightButtons(model, uiUtil);
  }

  /** Yaw 軸周りに回転させるスライダを追加する. */
  private void addRotationSlider(Lamp model, UiUtil uiUtil) {
    String imgPath = uiUtil.assetDir + "/Images/rotation.png";
    var size = new Vector2(16f * uiUtil.sclmm, 8.47f * uiUtil.sclmm);
    this.add(uiUtil.createUiImage(imgPath, size)).space(2 * uiUtil.sclmm);

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
    this.add(slider).width(30f * uiUtil.sclmm);
  }

  /** ライトの角度を変えるスライダを追加する. */
  private void addLightAngleSlider(Lamp model, UiUtil uiUtil) {
    String imgPath = uiUtil.assetDir + "/Images/changeLightAngle.png";
    var size = new Vector2(16f * uiUtil.sclmm, 10.44f * uiUtil.sclmm);
    this.add(uiUtil.createUiImage(imgPath, size)).space(2 * uiUtil.sclmm);

    VisSlider slider = new VisSlider(-10f, 10f, 1f, false);
    slider.setValue(0f);
    currentRotationVal = slider.getValue();
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        model.setLightAngle(slider.getValue() * 9f);
      }
    };
    slider.addListener(listener);
    this.add(slider).width(30f * uiUtil.sclmm);
  }

  /** ライトの半径を変えるスライダを追加する. */
  private void addLightRadiusSlider(Lamp model, UiUtil uiUtil) {
    String imgPath = uiUtil.assetDir + "/Images/changeLightRadius.png";
    var size = new Vector2(16f * uiUtil.sclmm, 10f * uiUtil.sclmm);
    this.add(uiUtil.createUiImage(imgPath, size)).space(2 * uiUtil.sclmm);

    VisSlider slider = new VisSlider(-10f, 20f, 1f, false);
    slider.setValue(0f);
    currentRotationVal = slider.getValue();
    float defaultRadius = model.getLightRadius();
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        float radius = defaultRadius * (1f + slider.getValue() * 0.08f);
        model.setLightRadius(radius);
      }
    };
    slider.addListener(listener);
    this.add(slider).width(30f * uiUtil.sclmm);
  }

  /** ライトの高さを変えるスライダを追加する. */
  private void addLightHeightSlider(Lamp model, UiUtil uiUtil) {
    String imgPath = uiUtil.assetDir + "/Images/changeLightHeight.png";
    var size = new Vector2(16f * uiUtil.sclmm, 11.75f * uiUtil.sclmm);
    this.add(uiUtil.createUiImage(imgPath, size)).space(2 * uiUtil.sclmm);

    VisSlider slider = new VisSlider(0f, 30f, 1f, false);
    slider.setValue(0f);
    currentRotationVal = slider.getValue();
    float defaultHeight = model.getLightHeight();
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        float height = defaultHeight * (1f + slider.getValue() * 0.08f);
        model.setLightHeight(height);
      }
    };
    slider.addListener(listener);
    this.add(slider).width(30f * uiUtil.sclmm);
  }

  /** ライトの色を変えるボタンを追加する. */
  private void addLightColorButton(Lamp model, Color color, VisTable base, UiUtil uiUtil) {
    var pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(color);
    pixmap.fill();
    var sprite = new Sprite(new Texture(pixmap));
    sprite.setSize(8f * uiUtil.sclmm, 8f * uiUtil.sclmm);
    var drawable = new SpriteDrawable(sprite);
    VisImageButton btn = new VisImageButton(drawable);
    btn.pad(2f * uiUtil.sclmm);
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        model.turnOn();
        model.setLightColor(color);
      }
    };
    btn.addListener(listener);
    base.add(btn).space(2f * uiUtil.sclmm);
  }

  /** ライトを消すボタンを追加する. */
  private void addLightOffButton(Lamp model, VisTable base, UiUtil uiUtil) {
    var pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(Color.BLACK);
    pixmap.fill();
    var sprite = new Sprite(new Texture(pixmap));
    sprite.setSize(8f * uiUtil.sclmm, 8f * uiUtil.sclmm);
    var drawable = new SpriteDrawable(sprite);
    VisImageButton btn = new VisImageButton(drawable);
    btn.pad(2f * uiUtil.sclmm);
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        model.turnOff();
      }
    };
    btn.addListener(listener);
    base.add(btn).space(2f * uiUtil.sclmm);
  }

  private void addLightButtons(Lamp model, UiUtil uiUtil) {
    VisTable buttonBase = new VisTable();
    this.add(buttonBase).colspan(2).padTop(1.5f * uiUtil.sclmm);
    addLightColorButton(model, Color.RED, buttonBase, uiUtil);
    addLightColorButton(model, Color.GREEN, buttonBase, uiUtil);
    addLightColorButton(model, Color.BLUE, buttonBase, uiUtil);
    buttonBase.row();
    addLightColorButton(model, Color.YELLOW, buttonBase, uiUtil);
    addLightColorButton(model, Color.CYAN, buttonBase, uiUtil);
    addLightColorButton(model, Color.MAGENTA, buttonBase, uiUtil);
    buttonBase.row();
    addLightColorButton(model, Color.WHITE, buttonBase, uiUtil);
    addLightOffButton(model, buttonBase, uiUtil);
  }
}
