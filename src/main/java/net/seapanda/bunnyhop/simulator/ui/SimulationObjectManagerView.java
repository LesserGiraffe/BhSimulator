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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisTable;
import net.seapanda.bunnyhop.simulator.BhSimulator;
import net.seapanda.bunnyhop.simulator.SimulationObjectManager;

/** {@link SimulationObjectManager} が保持する情報を表示する View. */
public class SimulationObjectManagerView extends VisTable {

  private SimulationObjectManager simObjManager;
  /** [現在の 3D モデルの個数 / 3D モデルの最大数] を示すラベル. */
  private Label labelOfNumObjects;

  /**
   * コンストラクタ.
   *
   * @param simObjManager シミュレーション空間の 3D モデルを管理するオブジェクト.
   */
  public SimulationObjectManagerView(SimulationObjectManager simObjManager) {
    this.simObjManager = simObjManager;
    addImage();
    addLabel();
    this.pad(2 * UiUtil.mm);
  }

  private void addImage() {
    String imgPath = BhSimulator.ASSET_PATH + "/Images/cube.png";
    var size = new Vector2(10 * UiUtil.mm, 10 * UiUtil.mm);
    this.<VisImage>add(UiUtil.createUiImage(imgPath, size)).space(2 * UiUtil.mm);
  }

  private void addLabel() {
    labelOfNumObjects = this.add("").getActor();
    LabelStyle style = new LabelStyle(labelOfNumObjects.getStyle());
    style.font = UiUtil.createFont(" 0123456789/", 17, Color.WHITE);
    labelOfNumObjects.setStyle(style);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    var numObjectsStr = simObjManager.getNumObjects() + " / " + SimulationObjectManager.MAX_OBJECTS;
    labelOfNumObjects.setText(numObjectsStr);
    super.draw(batch, parentAlpha);
  }
}
