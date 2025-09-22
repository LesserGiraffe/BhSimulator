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

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.kotcrab.vis.ui.VisUI;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.seapanda.bunnyhop.simulator.common.BhSimConstants;
import net.seapanda.bunnyhop.simulator.common.BhSimSettings;
import net.seapanda.bunnyhop.simulator.ui.UiComposer;
import net.seapanda.bunnyhop.simulator.ui.UiUtil;
import net.seapanda.bunnyhop.utility.Utility;

/**
 * BunnyHop で作成したプログラムの動作をシミュレーションするためのクラス.
 *
 * @author K.Koike
 */
public class BhSimulator implements ApplicationListener {

  static {
    ASSET_PATH = Utility.execPath + Utility.fs + "SimAssets";
  }

  public static String ASSET_PATH;

  private Camera cam;
  private ModelBatch modelBatch;
  private Environment environment;
  private SimulationObjectManager simObjManager;
  private UiComposer uiComposer;
  private SimulatorCmdProcessorImpl cmdProcessor;
  private CustomInputProcessor inputProcessor;
  private final CountDownLatch latch = new CountDownLatch(1);

  @Override
  public void create() {
    var skinScale = UiUtil.dpi >= BhSimConstants.Ui.X2_SKIN_DPI_THRESHOLD
        || (BhSimSettings.Ui.SCALE > 1)
        ? VisUI.SkinScale.X2 : VisUI.SkinScale.X1;
    VisUI.load(skinScale);
    Bullet.init(true);
    modelBatch = createModelBatch();
    environment = createEnvironment();
    cam = createCamera();
    var camCtrl = new CustomCameraInputController(
        cam, 8f, 85f, -25f, 25f, -25f, 25f, 0.5f, 50f, 0.4f);
    simObjManager = new SimulationObjectManager();
    simObjManager.setCameraTargetGetter(() -> new Vector3(camCtrl.target));
    inputProcessor = new CustomInputProcessor(camCtrl, simObjManager);
    uiComposer = new UiComposer(inputProcessor.getUiView(), simObjManager.getUiView());
    Gdx.input.setInputProcessor(
        new InputMultiplexer(uiComposer.getInputProcessor(), inputProcessor, camCtrl));
    cmdProcessor = new SimulatorCmdProcessorImpl(simObjManager.getRaspiCar());
    latch.countDown();
  }

  private ModelBatch createModelBatch() {
    var config = new Config();
    config.numBones = 16;
    return new ModelBatch(new DefaultShaderProvider(config));
  }

  private Camera createCamera() {
    var cam = new PerspectiveCamera(70, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    cam.position.set(3f, 5.0f, 10f);
    cam.lookAt(0, 0, 0);
    cam.near = 0.1875f;
    cam.far = 120f;
    cam.update();
    return cam;
  }

  private Environment createEnvironment() {
    var env = new Environment();
    env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.9f, 0.9f, 0.9f, 1f));
    env.add(new DirectionalLight().set(0.6f, 0.6f, 0.6f, -1f, -1f, -1f));
    env.add(new DirectionalLight().set(0.6f, 0.6f, 0.6f, 1f, -1f, 1f));
    return env;
  }

  /** シミュレータ制御用コマンドを処理するオブジェクトを取得する. */
  public Optional<SimulatorCmdProcessor> getCmdProcessor() {
    return Optional.ofNullable(cmdProcessor);
  }

  @Override
  public void render() {
    cmdProcessor.executeCmds();
    float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());
    simObjManager.update(delta);
    Gdx.gl.glClearColor(0.3f, 0.5f, 0.8f, 1.f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    Gdx.gl.glLineWidth(1);
    modelBatch.begin(cam);
    modelBatch.render(simObjManager.getRendarableProviders(), environment);
    modelBatch.end();
    uiComposer.draw(delta);
    // simObjManager.drawCollisionObjects(cam); // for debug
  }

  @Override
  public void dispose() {
    simObjManager.dispose();
    modelBatch.dispose();
    uiComposer.dispose();
    UiUtil.dispose();
    VisUI.dispose();
    latch.countDown();
  }

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void resize(int width, int height) {
    cam.viewportHeight = height;
    cam.viewportWidth = width;
    cam.update(true);
    uiComposer.updateViewPortSize(width, height);
  }

  /**
   * シミュレータが初期化されるのを待つ.
   *
   * @param timeout タイムアウト時間 (秒).
   * @return 初期化成功後にコントロールを返した場合 true
   */
  public boolean waitForInitialization(int timeout) {
    try {
      latch.await(timeout, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      return false;
    }
    return true;
  }

  /**
   * シミュレータが初期化されるのを待つ.
   *
   * @return 初期化成功後にコントロールを返した場合 true
   */
  public boolean waitForInitialization() {
    try {
      latch.await();
    } catch (InterruptedException e) {
      return false;
    }
    return true;
  }

  /** キーが押された時のイベントハンドラを設定する. */
  public void setOnKeyPressed(Consumer<Integer> onKeyPressed) {
    inputProcessor.setOnKeyPressed(onKeyPressed);
  }
}
