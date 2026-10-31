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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.kotcrab.vis.ui.widget.VisTable;

/** 現在の 1 秒あたりのフレーム数を表示する View. */
public class FpsCounterView extends VisTable {

  private final Label fps;
  private final Graphics graphics;

  /** コンストラクタ. */
  public FpsCounterView(Graphics graphics, UiUtil uiUtil) {
    this.graphics = graphics;
    fps = createLabel(uiUtil);
    pad(2 * uiUtil.sclmm);
  }

  private Label createLabel(UiUtil uiUtil) {
    Label fps = this.add("").getActor();
    Label.LabelStyle style = new Label.LabelStyle(fps.getStyle());
    style.font = uiUtil.createFont(" 0123456789.FPS=", 15 * uiUtil.sclpt, Color.WHITE);
    fps.setStyle(style);
    return fps;
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    fps.setText(" FPS = " + graphics.getFramesPerSecond());
    super.draw(batch, parentAlpha);
  }
}
