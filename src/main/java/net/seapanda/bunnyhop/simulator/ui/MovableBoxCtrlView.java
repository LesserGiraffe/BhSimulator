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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import net.seapanda.bunnyhop.simulator.BhSimulator;
import net.seapanda.bunnyhop.simulator.obj.Box;

/**
 * {@link Box} をコントロールする UI コンポーネントを持つ View.
 *
 * @author K.Koike
 */
public class MovableBoxCtrlView extends VisTable {
  
  private float currentRotationVal = 0;

  /** コンストラクタ.
   *
   * @param model このコントロールビューで操作する {@link Box} オブジェクト.
   */
  public MovableBoxCtrlView(Box model) {
    addRotationSlider(model);
  }

  /** Yaw 軸周りに回転させるスライダを追加する. */
  private void addRotationSlider(Box model) {
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
}
