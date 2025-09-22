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

package net.seapanda.bunnyhop.simulator;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.seapanda.bunnyhop.simulator.geometry.RayTestHelper;
import net.seapanda.bunnyhop.simulator.geometry.RayTestHelper.RayTestResult;
import net.seapanda.bunnyhop.simulator.obj.Box;
import net.seapanda.bunnyhop.simulator.obj.CollisionGroup;
import net.seapanda.bunnyhop.simulator.obj.Lamp;
import net.seapanda.bunnyhop.simulator.obj.ObjectReflection;
import net.seapanda.bunnyhop.simulator.obj.RaspiCar;
import net.seapanda.bunnyhop.simulator.obj.interfaces.Collidable;
import net.seapanda.bunnyhop.simulator.obj.interfaces.ObjectReflectionProvider;
import net.seapanda.bunnyhop.simulator.obj.interfaces.UiViewProvider;
import net.seapanda.bunnyhop.simulator.ui.ModelCtrlView;

/**
 * マウスとキーの入力操作を処理するクラス.
 *
 * @author K.Koike
 */
public class CustomInputProcessor extends InputAdapter implements UiViewProvider {

  private final CameraInputController cameraCtrl;
  private final SimulationObjectManager simObjManager;

  /** Simulation Object 選択時の交差点. */
  private Vector3 pointOfReflection = new Vector3();
  /** 選択中の 3D モデル. */
  private final List<Collidable> selectedModels = new ArrayList<>();
  /** ドラッグ中の 3D モデル. */
  private ObjectReflection dragged;
  /** 押されたマウスボタン. */
  private int buttonPressed = -1;
  /** 押されたキー. */
  private int keyPressed = -1;
  /** UI のルートコンポーネント. */
  private final Actor uiComponent = new ModelCtrlView(new AccessHelper());
  /** キーが押されたときに呼び出すコールバックメソッド. */
  private final AtomicReference<Consumer<Integer>> onKeyPressed =
      new AtomicReference<>(keyCode -> {});

  /**
   * コンストラクタ.
   *
   * @param camCtrl       シミュレーション空間のカメラを制御するオブジェクト.
   * @param simObjManager シミュレーション空間の 3D モデルを管理するオブジェクト.
   */
  public CustomInputProcessor(
        CameraInputController camCtrl, SimulationObjectManager simObjManager) {
    this.cameraCtrl = camCtrl;
    this.simObjManager = simObjManager;
  }

  @Override
  public boolean keyDown(int keycode) {
    onKeyPressed.get().accept(keycode);
    keyPressed = keycode;
    RaspiCar car = simObjManager.getRaspiCar();
    if (car != null && car.getUiView().keyDown(keycode)) {
      return true;
    }
    if (keycode == Keys.FORWARD_DEL || keycode == Keys.DEL) {
      deleteSelectedObjects();
    }
    return false;
  }

  @Override
  public boolean keyUp(int keycode) {
    keyPressed = -1;
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    buttonPressed = button;
    if (button == Buttons.LEFT) {
      return onLeftButtonPressed(screenX, screenY);
    }
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (button == Buttons.LEFT) {
      onLeftButtonReleased();
    }
    buttonPressed = -1;
    return false;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    if (buttonPressed != Buttons.LEFT || selectedModels.isEmpty()
        || !selectedModels.getLast().isDraggable()) {
      return false;
    }
    return dragReflection(screenX, screenY);
  }

  @Override
  public Actor getUiView() {
    return uiComponent;
  }

  /** 左マウスボタンを押した時の処理を行う. */
  private boolean onLeftButtonPressed(int screenX, int screenY) {
    var cameraRay = new Ray().set(cameraCtrl.camera.getPickRay(screenX, screenY));
    Optional<RayTestResult<Collidable>> result = simObjManager.getRayTestHelper()
        .getIntersectedCollidable(cameraRay, cameraCtrl.camera.far, CollisionGroup.PHYSICAL_ENTITY);
    if (result.isEmpty()) {
      deselectAll();
      return false;
    }
    Collidable newSelected = result.get().intersected();
    if (keyPressed != Keys.SHIFT_LEFT && keyPressed != Keys.SHIFT_RIGHT) {
      deselectAll();
    } else if (selectedModels.contains(newSelected)) {
      selectedModels.remove(newSelected);
      newSelected.deselect();
      return true;
    }
    pointOfReflection = result.get().pos();
    select(newSelected);
    return true;
  }

  /** 最後に選択されたオブジェクトの {@link ObjectReflection} をドラッグする. */
  private boolean dragReflection(int screenX, int screenY) {
    Collidable lastSelected = selectedModels.getLast();
    if (dragged != null) {
      setPosOfDraggedObj(screenX, screenY);
      return true;
    } else if (lastSelected instanceof ObjectReflectionProvider provider) {
      lastSelected.resetRotationIfTiltingOverly();
      dragged = simObjManager.createObjectReflection(provider);
      setPosOfDraggedObj(screenX, screenY);
      return true;
    }
    return false;
  }

  /** ドラッグ中の 3D モデルの位置を設定する. */
  private void setPosOfDraggedObj(int screenX, int screenY) {
    Ray ray = new Ray().set(cameraCtrl.camera.getPickRay(screenX, screenY));
    RayTestHelper.Config config = new RayTestHelper.Config()
        .add(CollisionGroup.PHYSICAL_ENTITY, CollisionGroup.STAGE).add(selectedModels.getLast());
    Optional<RayTestResult<Collidable>> result = simObjManager.getRayTestHelper()
        .getIntersectedCollidable(ray, cameraCtrl.camera.far, config);
    result.ifPresent(res -> pointOfReflection = res.pos());
    dragged.setPosition(pointOfReflection);
  }

  /** 左マウスボタンを離した時の処理を行う. */
  private boolean onLeftButtonReleased() {
    if (dragged == null) {
      return false;
    }
    if (!selectedModels.isEmpty()) {
      selectedModels.getLast().setPosition(dragged.getPosition());
    }
    simObjManager.delete(dragged);
    dragged = null;
    return false;
  }

  /** 選択中の全てのオブジェクトを削除する. */
  private void deleteSelectedObjects() {
    if (dragged != null || buttonPressed != -1) {
      return;
    }
    for (Collidable selected : selectedModels) {
      selected.deselect();
      if (!(selected instanceof RaspiCar)) {
        simObjManager.delete(selected);
      }
    }
    selectedModels.clear();
  }

  /** 選択中の全ての 3D モデルの選択を解除する. */
  private void deselectAll() {
    selectedModels.forEach(selected -> selected.deselect());
    selectedModels.clear();
  }

  /** {@code } を選択された状態にする. */
  private void select(Collidable target) {
    target.select();
    selectedModels.add(target);
  }

  private void createLamp() {
    try {
      Lamp lamp = simObjManager.createLamp(cameraCtrl.target);
      deselectAll();
      select(lamp);
    } catch (SimulationObjectManager.MaxObjectsExceededException e) {
      /* Do nothing. */
    }
  }

  private void createBox(boolean isHeavy) {
    try {
      Box box = simObjManager.createBox(cameraCtrl.target, isHeavy);
      deselectAll();
      select(box);
    } catch (SimulationObjectManager.MaxObjectsExceededException e) {
      /* Do nothing. */
    }
  }

  /** キーが押された時のイベントハンドラを設定する. */
  public void setOnKeyPressed(Consumer<Integer> onKeyPressed) {
    if (onKeyPressed != null) {
      this.onKeyPressed.set(onKeyPressed);
    }
  }

  /** {@link CustomInputProcessor} のメンバにアクセスするためのヘルパークラス. */
  public class AccessHelper {

    public Runnable fnDeleteSelectedObjects = CustomInputProcessor.this::deleteSelectedObjects;
    public Consumer<Boolean> fnCreateBox = CustomInputProcessor.this::createBox;
    public Runnable fnCreateLamp = CustomInputProcessor.this::createLamp;
    public List<Collidable> selectedModels = CustomInputProcessor.this.selectedModels;

    private AccessHelper() {}
  }
}
