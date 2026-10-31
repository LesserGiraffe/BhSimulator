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

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * UI コンポーネントを統合して描画するクラス.
 *
 * @author K.Koike
 */
public class UiComposer implements Disposable {

  private final Stage stage = new Stage(new ScreenViewport());
  private final VisTable rhsRoot;

  /**
   * コンストラクタ.
   *
   * @param modelCtrlView 3D モデルの制御を行うビュー
   * @param numSimObjectsView 3D モデルの個数を表示するビュー
   * @param graphics UI コンポーネントを描画するウィンドウに紐づく {@link Graphics} オブジェクト
   * @param uiUtil UI 構築のためのユーティリティオブジェクト
   */
  public UiComposer(
      Actor modelCtrlView, Actor numSimObjectsView, Graphics graphics, UiUtil uiUtil) {
    rhsRoot = createRhsView(modelCtrlView);
    VisTable lhsRoot = createLhsView(numSimObjectsView);
    stage.addActor(createFpsCounterView(graphics, uiUtil));
    stage.addActor(rhsRoot);
    stage.addActor(lhsRoot);
    stage.addActor(createCrosshair(uiUtil));
  }

  /** UI コンポーネントを描画する. */
  public void draw(float deltaTime) {
    stage.act(deltaTime);
    stage.draw();
  }

  /** UI を描画するビューポートのサイズを変更する. */
  public void updateViewPortSize(int width, int height) {
    stage.getViewport().update(width, height, true);
    rhsRoot.setOrigin(width, height);
  }

  /** このオブジェクトが管理する UI に対する入力を処理するオブジェクトを返す. */
  public InputProcessor getInputProcessor() {
    return stage;
  }

  /** 画面右側に表示するビューを作成する. */
  private static VisTable createRhsView(Actor view) {
    var table = new VisTable();
    table.top().right();
    table.setFillParent(true);
    table.add(createScrollPane(view)).align(Align.bottomRight);
    return table;
  }

  private static VisScrollPane createScrollPane(Actor content) {
    VisScrollPane scrollPane = new VisScrollPane(content);
    scrollPane.setFadeScrollBars(false);
    scrollPane.setTouchable(Touchable.childrenOnly);
    scrollPane.setFlickScroll(false);
    InputListener listener = new InputListener() {
      @Override
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        scrollPane.getStage().setScrollFocus(scrollPane);
      }

      @Override
      public void exit(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        scrollPane.getStage().setScrollFocus(null);
      }
    };
    scrollPane.addListener(listener);
    return scrollPane;
  }

  /** 画面左に表示するビューを作成する. */
  private static VisTable createLhsView(Actor view) {
    var table = new VisTable();
    table.bottom().left();
    table.setFillParent(true);
    table.add(view).align(Align.topLeft);
    return table;
  }

  private static Container<VisImage> createCrosshair(UiUtil uiUtil) {
    String imgPath = uiUtil.assetDir + "/Images/sight.png";
    Container<VisImage> crosshair = new Container<>(
        uiUtil.createUiImage(imgPath, new Vector2(5f * uiUtil.sclmm, 5f * uiUtil.sclmm)));
    crosshair.setFillParent(true);
    return crosshair;
  }

  private static FpsCounterView createFpsCounterView(Graphics graphics, UiUtil uiUtil) {
    var fpsCounterView = new FpsCounterView(graphics, uiUtil);
    fpsCounterView.left().top().setFillParent(true);
    return fpsCounterView;
  }

  @Override
  public void dispose() {
    stage.dispose();
  }
}
