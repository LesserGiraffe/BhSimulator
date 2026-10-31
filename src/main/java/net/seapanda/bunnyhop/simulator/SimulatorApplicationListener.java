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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3AccessHelper;
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
import java.nio.file.Paths;
import java.util.function.Consumer;
import net.seapanda.bunnyhop.simulator.api.BhSimulatorSettings;
import net.seapanda.bunnyhop.simulator.api.KeyboardKey;
import net.seapanda.bunnyhop.simulator.ui.UiComposer;
import net.seapanda.bunnyhop.simulator.ui.UiUtil;
import net.seapanda.bunnyhop.utility.Utility;

/**
 * シミュレータのアプリケーションのライフサイクルを管理するクラス.
 *
 * <p>このクラスのメソッドは, シミュレータを実行するスレッドからのみ呼ばれる.
 * ただし, {@link #requestHalt} と {@link #getCmdProcessor} は任意のスレッドから呼ぶことができる.
 *
 * @author K.Koike
 */
class SimulatorApplicationListener implements ApplicationListener {

  /** シミュレータの設定を格納したオブジェクト. */
  private final BhSimulatorSettings settings;
  /** シミュレータのウィンドウの状態を管理するオブジェクト. */
  private final SimulatorWindowImpl window;
  /** キーが押されたときに呼ぶ処理. */
  private final Consumer<? super KeyboardKey> onKeyPressed;
  /** 初期化が完了したときに呼ぶ処理. */
  private final Runnable onInitialized;

  private UiUtil uiUtil;
  private Camera cam;
  private ModelBatch modelBatch;
  private Environment environment;
  private SimulationObjectManager simObjManager;
  private UiComposer uiComposer;
  private Lwjgl3AccessHelper accessHelper;
  /**
   * シミュレータ制御用コマンドを処理するオブジェクト.
   *
   * <p>シミュレータを実行するスレッドから書き込まれ, 任意のスレッドから読み出されるので volatile とする.
   */
  private volatile SimulatorCmdProcessorImpl cmdProcessor;
  /**
   * このオブジェクトが属するアプリケーション.
   *
   * <p>シミュレータを実行するスレッドから書き込まれ, 任意のスレッドから読み出されるので volatile とする.
   */
  private volatile Application application;
  /** シミュレータの停止が要求された場合 true. */
  private volatile boolean haltRequested = false;

  /**
   * コンストラクタ.
   *
   * @param settings シミュレータの設定を格納したオブジェクト
   * @param window シミュレータのウィンドウを操作するためのオブジェクト
   * @param onKeyPressed キーが押されたときに呼ぶ処理
   * @param onInitialized 初期化が完了したときに呼ぶ処理
   */
  SimulatorApplicationListener(
      BhSimulatorSettings settings,
      SimulatorWindowImpl window,
      Consumer<? super KeyboardKey> onKeyPressed,
      Runnable onInitialized) {
    this.settings = settings;
    this.window = window;
    this.onKeyPressed = onKeyPressed;
    this.onInitialized = onInitialized;
  }

  @Override
  public void create() {
    accessHelper = window.getAccessHelper();
    application = accessHelper.getApplication();
    String assetPath = Paths.get(Utility.execPath, "SimAssets").toAbsolutePath().toString();
    uiUtil = new UiUtil(accessHelper.getGraphics(), settings.ui.scale.get(), assetPath);
    VisUI.load(getSkinScale(uiUtil.dpi, (float) settings.ui.x2SkinDpiThreshold.get()));
    Bullet.init(true);

    modelBatch = createModelBatch();
    environment = createEnvironment();
    cam = createCamera(accessHelper.getGraphics());
    var camCtrl = new CustomCameraInputController(
        cam, 8f, 85f, -1.78f, 1.78f, -1.78f, 1.78f, 0.1f, 3.5f, 0.05f);
    simObjManager = new SimulationObjectManager(uiUtil);
    simObjManager.setCameraTargetGetter(() -> new Vector3(camCtrl.target));
    var inputProcessor = new CustomInputProcessor(camCtrl, simObjManager, uiUtil);
    inputProcessor
        .setOnKeyPressed(keyCode -> KeyCodeConverter.convert(keyCode).ifPresent(onKeyPressed));
    uiComposer = new UiComposer(
        inputProcessor.getUiView(), simObjManager.getUiView(), accessHelper.getGraphics(), uiUtil);
    var inputMux = new InputMultiplexer(uiComposer.getInputProcessor(), inputProcessor, camCtrl);
    accessHelper.getInput().setInputProcessor(inputMux);
    cmdProcessor = new SimulatorCmdProcessorImpl(simObjManager.getRaspiCar(), window, settings);
    onInitialized.run();
    if (haltRequested) {
      // 初期化の完了前に停止を要求された場合, ここで停止させる.
      application.exit();
    }
  }

  private VisUI.SkinScale getSkinScale(float dpi, float x2SkinDpiThreshold) {
    return dpi >= x2SkinDpiThreshold || (settings.ui.scale.get() > 1)
        ? VisUI.SkinScale.X2 : VisUI.SkinScale.X1;
  }

  private ModelBatch createModelBatch() {
    var config = new Config();
    config.numBones = 16;
    return new ModelBatch(new DefaultShaderProvider(config));
  }

  private Camera createCamera(Graphics graphics) {
    var cam = new PerspectiveCamera(70, graphics.getWidth(), graphics.getHeight());
    cam.position.set(0.5f, 1f, 0.5f);
    cam.lookAt(0, 0, 0);
    cam.near = 0.04f;
    cam.far = 8.5f;
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

  @Override
  public void render() {
    Graphics graphics = accessHelper.getGraphics();
    cmdProcessor.executeCmds();
    float delta = Math.min(1f / 30f, graphics.getDeltaTime());
    simObjManager.update(delta);
    GL20 gl = graphics.getGL20();
    gl.glClearColor(0.3f, 0.5f, 0.8f, 1.f);
    gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    gl.glLineWidth(1);
    modelBatch.begin(cam);
    modelBatch.render(simObjManager.getRenderableProviders(), environment);
    modelBatch.end();
    uiComposer.draw(delta);
    window.updateWindowState();
    // simObjManager.drawCollisionObjects(cam); // for debug
  }

  @Override
  public void dispose() {
    simObjManager.dispose();
    modelBatch.dispose();
    uiComposer.dispose();
    uiUtil.dispose();
    VisUI.dispose();
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
   * シミュレータの停止を要求する.
   *
   * <p>初期化の完了前に呼ばれた場合, 初期化の完了後に停止する.
   */
  void requestHalt() {
    haltRequested = true;
    Application app = application;
    if (app == null) {
      // 初期化が application の設定まで進んでいないので, create の最後で停止させる.
      return;
    }
    app.exit();
  }

  /**
   * シミュレータ制御用コマンドを処理するオブジェクトを取得する.
   *
   * @return シミュレータ制御用コマンドを処理するオブジェクト.
   *         初期化の完了前は null を返す.
   */
  SimulatorCmdProcessorImpl getCmdProcessor() {
    return cmdProcessor;
  }
}
