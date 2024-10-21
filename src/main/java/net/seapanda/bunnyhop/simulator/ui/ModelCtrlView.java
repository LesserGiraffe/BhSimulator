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
import net.seapanda.bunnyhop.simulator.BhSimulator;
import net.seapanda.bunnyhop.simulator.CustomInputProcessor;
import net.seapanda.bunnyhop.simulator.obj.interfaces.Collidable;
import net.seapanda.bunnyhop.simulator.obj.interfaces.UiViewProvider;

/**
 * 3D モデルをコントロールする UI コンポーネントを持つ View.
 *
 * @author K.Koike
 */
public class ModelCtrlView extends VerticalGroup {

  private final CustomInputProcessor.AccessHelper accessor;
  /** 選択されたモデルの UI View を子に持つ UI View. */
  private Container<Actor> viewOfSelectedModel = new Container<>();

  /**
   * コンストラクタ.
   *
   * @param accessor {@link CustomInputProcessor} のメンバにアクセスするためのヘルパークラス.
   */
  public ModelCtrlView(CustomInputProcessor.AccessHelper accessor) {
    space(2 * UiUtil.mm);
    top().right();
    columnRight();
    this.accessor = accessor;
    addActor(new CameraManualView().top().right());
    addActor(genModelCreationView());
    addActor(viewOfSelectedModel);
    viewOfSelectedModel.pad(2 * UiUtil.mm);
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
  private VisTable genModelCreationView() {
    VisTable table = new VisTable();
    table.top().right();
    table.pad(2 * UiUtil.mm);
    setBackgroundColorTo(table, viewOfSelectedModel);
    table.<VisImageButton>add(gencreateBoxButton(accessor, false)).space(2 * UiUtil.mm);
    table.<VisImageButton>add(gencreateBoxButton(accessor, true)).space(2 * UiUtil.mm);
    table.row();
    table.<VisImageButton>add(genCreateLampButton(accessor)).space(2 * UiUtil.mm);
    table.<VisImageButton>add(genDeleteButton(accessor)).space(2 * UiUtil.mm);
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
  private VisImageButton genDeleteButton(CustomInputProcessor.AccessHelper accessor) {
    String imgPath = BhSimulator.ASSET_PATH + "/Images/closedTrashbox.png";
    var size = new Vector2(9 * UiUtil.mm, 16 * UiUtil.mm);
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        accessor.fnDeleteSelectedObjects.run();
      }
    };
    return UiUtil.createUiButton(imgPath, size, UiUtil.mm, listener);
  }

  /**
   * 箱を作成するボタンを作成する.
   *
   * @param isHeavy 重い箱を作成するボタンを作る場合 true
   */
  private VisImageButton gencreateBoxButton(
      CustomInputProcessor.AccessHelper accessor, boolean isHeavy) {
    String iconName = isHeavy ? "/Images/heavyBox.png" : "/Images/dice.png";
    String imgPath = BhSimulator.ASSET_PATH + iconName;
    var size = new Vector2(15.26f * UiUtil.mm, 16.5f * UiUtil.mm);
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        accessor.fnCreateBox.accept(isHeavy);
      }
    };
    return UiUtil.createUiButton(imgPath, size, UiUtil.mm, listener);
  }

  /** ランプを作成するボタンを作成する. */
  private VisImageButton genCreateLampButton(CustomInputProcessor.AccessHelper accessor) {
    String imgPath = BhSimulator.ASSET_PATH + "/Images/lamp.png";
    var size = new Vector2(12 * UiUtil.mm, 12 * UiUtil.mm);
    ChangeListener listener = new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        accessor.fnCreateLamp.run();
      }
    };
    return UiUtil.createUiButton(imgPath, size, UiUtil.mm, listener);
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
