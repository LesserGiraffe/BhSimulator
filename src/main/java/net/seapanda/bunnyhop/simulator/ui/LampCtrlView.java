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
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import net.seapanda.bunnyhop.simulator.BhSimulator;
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
   */
  public LampCtrlView(Lamp model) {
    addRotationSlider(model);
    row();
    addLightAngleSlider(model);
    row();
    addLightRadiusSlider(model);
    row();
    addLightHeightSlider(model);
    row();
    addLightButtons(model);
  }

  /** Yaw 軸周りに回転させるスライダを追加する. */
  private void addRotationSlider(Lamp model) {
    String imgPath = BhSimulator.ASSET_PATH + "/Images/rotation.png";
    var size = new Vector2(16f * UiUtil.mm, 8.47f * UiUtil.mm);
    this.<VisImage>add(UiUtil.createUiImage(imgPath, size)).space(2 * UiUtil.mm);

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
    this.<VisSlider>add(slider).width(30f * UiUtil.mm);
  }

  /** ライトの角度を変えるスライダを追加する. */
  private void addLightAngleSlider(Lamp model) {
    String imgPath = BhSimulator.ASSET_PATH + "/Images/changeLightAngle.png";
    var size = new Vector2(16f * UiUtil.mm, 10.44f * UiUtil.mm);
    this.<VisImage>add(UiUtil.createUiImage(imgPath, size)).space(2 * UiUtil.mm);

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
    this.<VisSlider>add(slider).width(30f * UiUtil.mm);
  }

  /** ライトの半径を変えるスライダを追加する. */
  private void addLightRadiusSlider(Lamp model) {
    String imgPath = BhSimulator.ASSET_PATH + "/Images/changeLightRadius.png";
    var size = new Vector2(16f * UiUtil.mm, 10f * UiUtil.mm);
    this.<VisImage>add(UiUtil.createUiImage(imgPath, size)).space(2 * UiUtil.mm);

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
    this.<VisSlider>add(slider).width(30f * UiUtil.mm);
  }

  /** ライトの高さを変えるスライダを追加する. */
  private void addLightHeightSlider(Lamp model) {
    String imgPath = BhSimulator.ASSET_PATH + "/Images/changeLightHeight.png";
    var size = new Vector2(16f * UiUtil.mm, 11.75f * UiUtil.mm);
    this.<VisImage>add(UiUtil.createUiImage(imgPath, size)).space(2 * UiUtil.mm);

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
    this.<VisSlider>add(slider).width(30f * UiUtil.mm);
  }

  /** ライトの色を変えるボタンを追加する. */
  private void addLightColorButton(
      Lamp model, Color color, VisTable base) {
    var pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(color);
    pixmap.fill();
    var sprite = new Sprite(new Texture(pixmap));
    sprite.setSize(8f * UiUtil.mm, 8f * UiUtil.mm);
    var drawable = new SpriteDrawable(sprite);
    VisImageButton btn = new VisImageButton(drawable);
    btn.pad(2f * UiUtil.mm);
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        model.turnOn();
        model.setLightColor(color);
      }
    };
    btn.addListener(listener);
    base.<VisImageButton>add(btn).space(2f * UiUtil.mm);
  }

  /** ライトを消すボタンを追加する. */
  private void addLightOffButton(Lamp model, VisTable base) {
    var pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(Color.BLACK);
    pixmap.fill();
    var sprite = new Sprite(new Texture(pixmap));
    sprite.setSize(8f * UiUtil.mm, 8f * UiUtil.mm);
    var drawable = new SpriteDrawable(sprite);
    VisImageButton btn = new VisImageButton(drawable);
    btn.pad(2f * UiUtil.mm);
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        model.turnOff();
      }
    };
    btn.addListener(listener);
    base.<VisImageButton>add(btn).space(2f * UiUtil.mm);
  }

  private void addLightButtons(Lamp model) {
    VisTable buttonBase = new VisTable();
    this.<VisTable>add(buttonBase).colspan(2).padTop(1.5f * UiUtil.mm);
    addLightColorButton(model, Color.RED, buttonBase);
    addLightColorButton(model, Color.GREEN, buttonBase);
    addLightColorButton(model, Color.BLUE, buttonBase);
    buttonBase.row();
    addLightColorButton(model, Color.YELLOW, buttonBase);
    addLightColorButton(model, Color.CYAN, buttonBase);
    addLightColorButton(model, Color.MAGENTA, buttonBase);
    buttonBase.row();
    addLightColorButton(model, Color.WHITE, buttonBase);
    addLightOffButton(model, buttonBase);
  }
}
