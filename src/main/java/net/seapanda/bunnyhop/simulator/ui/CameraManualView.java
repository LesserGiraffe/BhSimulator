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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.CollapsibleWidget;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import net.seapanda.bunnyhop.simulator.BhSimulator;
import net.seapanda.bunnyhop.simulator.common.TextDefs;

/**
 * 視点の操作方法を表示する UI コンポーネントを持つ View.
 *
 * @author K.Koike
 */
public class CameraManualView extends VisTable {
  
  /** コンストラクタ. */
  public CameraManualView() {
    VisCheckBox collapseButton = genCollapseButton();
    add(collapseButton).align(Align.topLeft);
    row();  
    add(genCameraManual(collapseButton)).align(Align.topLeft);
    setBackgroundColor();
    pad(UiUtil.sclmm);
  }

  /** この UI コンポーネントの背景色を設定する. */
  private void setBackgroundColor() {
    var pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(new Color(0, 0, 0, 1.0f));
    pixmap.fill();
    var sprite = new Sprite(new Texture(pixmap));
    var drawable = new SpriteDrawable(sprite);
    background(drawable);
  }

  /** 視点の操作方法をの表示/非表示を切り替えるボタンを作成する. */
  private VisCheckBox genCollapseButton() {
    VisCheckBox collapseButton = new VisCheckBox("");
    VisLabel label = UiUtil.createLabel(
        TextDefs.CameraManual.moveViewPoint.get(), 13.2f * UiUtil.sclpt, Color.WHITE);
    collapseButton.add(label);
    return collapseButton;
  }

  /** 視点の操作方法を表示する UI コンポーネントを作成する. */
  private CollapsibleWidget genCameraManual(VisCheckBox collapseButton) {
    String imgPath = BhSimulator.ASSET_PATH + "/Images/povCtrl.png";
    VisImage manualImg =
        UiUtil.createUiImage(imgPath, new Vector2(42.35f * UiUtil.sclmm, 53.57f * UiUtil.sclmm));
    Table table = new Table();
    table.add(manualImg);
    CollapsibleWidget cw = new CollapsibleWidget(table);
    cw.setCollapsed(true);

    collapseButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        cw.setCollapsed(!cw.isCollapsed());
      }
    });
    return cw;
  }
}
