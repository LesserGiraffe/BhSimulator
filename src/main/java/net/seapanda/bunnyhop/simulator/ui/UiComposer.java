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
import net.seapanda.bunnyhop.simulator.BhSimulator;

/**
 * UI コンポーネントを統合して描画するクラス.
 *
 * @author K.Koike
 */
public class UiComposer implements Disposable {

  private final Stage stage = new Stage(new ScreenViewport());
  private final VisTable rhsRoot;
  private final VisTable lhsRoot;
  
  /**
   * コンストラクタ.
   *
   * @param modelCtrlView 3D モデルの制御を行うビュー
   * @param numSimObjectsView 3D モデルの個数を表示するビュー
   */
  public UiComposer(Actor modelCtrlView, Actor numSimObjectsView) {
    rhsRoot = genRhsView(modelCtrlView);
    lhsRoot = genLhsView(numSimObjectsView);
    stage.addActor(rhsRoot);
    stage.addActor(lhsRoot);
    stage.addActor(genCrosshair());
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
  private VisTable genRhsView(Actor view) {
    var table = new VisTable();
    table.top().right();
    table.setFillParent(true);
    table.add(genScrollPane(view)).align(Align.bottomRight);
    return table;
  }

  private VisScrollPane genScrollPane(Actor content) {
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
  private VisTable genLhsView(Actor view) {
    var table = new VisTable();
    table.bottom().left();
    table.setFillParent(true);
    table.add(view).align(Align.topLeft);
    stage.addActor(rhsRoot);
    return table;
  }

  private Container<VisImage> genCrosshair() {
    String imgPath = BhSimulator.ASSET_PATH + "/Images/sight.png";
    Container<VisImage> crosshair = new Container<VisImage>(
        UiUtil.createUiImage(imgPath, new Vector2(5f * UiUtil.sclmm, 5f * UiUtil.sclmm)));
    crosshair.setFillParent(true);
    return crosshair;
  }

  @Override
  public void dispose() {
    stage.dispose();
  }
}
