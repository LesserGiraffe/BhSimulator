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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisTable;
import net.seapanda.bunnyhop.simulator.CustomInputProcessor;
import net.seapanda.bunnyhop.simulator.CustomInputProcessor.AccessHelper;
import net.seapanda.bunnyhop.simulator.obj.interfaces.Collidable;
import net.seapanda.bunnyhop.simulator.obj.interfaces.UiViewProvider;

/**
 * 3D モデルをコントロールする UI コンポーネントを持つ View.
 *
 * @author K.Koike
 */
public class ModelCtrlView extends VerticalGroup {

  private final AccessHelper accessor;
  /** 選択されたモデルの UI View を子に持つ UI View. */
  private final Container<Actor> viewOfSelectedModel = new Container<>();

  /**
   * コンストラクタ.
   *
   * @param accessor {@link CustomInputProcessor} のメンバにアクセスするためのヘルパークラス.
   * @param uiUtil UI 構築のためのユーティリティオブジェクト
   */
  public ModelCtrlView(AccessHelper accessor, UiUtil uiUtil) {
    space(2 * uiUtil.sclmm);
    top().right();
    columnRight();
    this.accessor = accessor;
    addActor(new CameraManualView(uiUtil).top().right());
    addActor(createModelCreationView(uiUtil));
    addActor(viewOfSelectedModel);
    viewOfSelectedModel.pad(2 * uiUtil.sclmm);
    ClickListener listener = new ClickListener() {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        return true;
      }
    };
    viewOfSelectedModel.addListener(listener);
    viewOfSelectedModel.setTouchable(Touchable.enabled);
  }

  /** モデルの生成と削除機能を提供する UI View を作成する. */
  private VisTable createModelCreationView(UiUtil uiUtil) {
    VisTable table = new VisTable();
    table.top().right();
    table.pad(2 * uiUtil.sclmm);
    setBackgroundColorTo(table, viewOfSelectedModel);
    table.add(createBoxButton(accessor, false, uiUtil)).space(2 * uiUtil.sclmm);
    table.add(createBoxButton(accessor, true, uiUtil)).space(2 * uiUtil.sclmm);
    table.row();
    table.add(createLampButton(accessor, uiUtil)).space(2 * uiUtil.sclmm);
    table.add(createDeleteButton(accessor, uiUtil)).space(2 * uiUtil.sclmm);
    table.row();
    table.addListener(event -> true);
    table.setTouchable(Touchable.enabled);
    return table;
  }

  /** 引数の {@code table} と {@code container} に背景色を設定する. */
  private void setBackgroundColorTo(Table table, Container<Actor> container) {
    var pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(new Color(0, 0, 0, 1.0f));
    pixmap.fill();
    var sprite = new Sprite(new Texture(pixmap));
    var drawable = new SpriteDrawable(sprite);
    table.background(drawable);
    container.background(drawable);
  }

  /** オブジェクトを削除するボタンを作成する. */
  private VisImageButton createDeleteButton(AccessHelper accessor, UiUtil uiUtil) {
    String imgPath = uiUtil.assetDir + "/Images/closedTrashbox.png";
    var size = new Vector2(11 * uiUtil.sclmm, 16 * uiUtil.sclmm);
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        accessor.fnDeleteSelectedObjects.run();
      }
    };
    return uiUtil.createUiButton(imgPath, size, 0.5f * uiUtil.sclmm, listener);
  }

  /**
   * 箱を作成するボタンを作成する.
   *
   * @param isHeavy 重い箱を作成するボタンを作る場合 true
   */
  private VisImageButton createBoxButton(AccessHelper accessor, boolean isHeavy, UiUtil uiUtil) {
    String iconName = isHeavy ? "/Images/heavyBox.png" : "/Images/dice.png";
    String imgPath = uiUtil.assetDir + iconName;
    var size = new Vector2(12 * uiUtil.sclmm, 12.5f * uiUtil.sclmm);
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        accessor.fnCreateBox.accept(isHeavy);
      }
    };
    return uiUtil.createUiButton(imgPath, size, 0.5f * uiUtil.sclmm, listener);
  }

  /** ランプを作成するボタンを作成する. */
  private VisImageButton createLampButton(AccessHelper accessor, UiUtil uiUtil) {
    String imgPath = uiUtil.assetDir + "/Images/lamp.png";
    var size = new Vector2(12 * uiUtil.sclmm, 12 * uiUtil.sclmm);
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        accessor.fnCreateLamp.run();
      }
    };
    return uiUtil.createUiButton(imgPath, size, 0.5f * uiUtil.sclmm, listener);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    setUiViewOfLastSelectedModel();
    super.draw(batch, parentAlpha);
  }

  /** 最後に選択されたオブジェクトの UI View をこの UI View に追加する. */
  private void setUiViewOfLastSelectedModel() {
    if (accessor.selectedModels.isEmpty()) {
      viewOfSelectedModel.setVisible(false);
      viewOfSelectedModel.setLayoutEnabled(true);
      viewOfSelectedModel.setActor(null);
      return;
    }
    Collidable lastSelected = accessor.selectedModels.getLast();
    if (lastSelected instanceof UiViewProvider provider) {
      viewOfSelectedModel.setVisible(true);
      viewOfSelectedModel.setLayoutEnabled(true);
      viewOfSelectedModel.setActor(provider.getUiView());
    } else {
      viewOfSelectedModel.setVisible(false);
      viewOfSelectedModel.setLayoutEnabled(true);
      viewOfSelectedModel.setActor(null);
    }
  }
}
