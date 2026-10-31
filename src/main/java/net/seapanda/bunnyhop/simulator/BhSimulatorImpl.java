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

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import net.seapanda.bunnyhop.simulator.api.BhSimulator;
import net.seapanda.bunnyhop.simulator.api.BhSimulatorSettings;
import net.seapanda.bunnyhop.simulator.api.KeyboardKey;
import net.seapanda.bunnyhop.simulator.api.SimulatorCmdProcessor;
import net.seapanda.bunnyhop.simulator.api.SimulatorInitializationException;
import net.seapanda.bunnyhop.simulator.api.SimulatorWindow;
import net.seapanda.bunnyhop.utility.concurrent.event.ConcurrentConsumerInvoker;
import net.seapanda.bunnyhop.utility.event.ConsumerInvoker;
import net.seapanda.bunnyhop.utility.reference.WritableBoolean;

/**
 * シミュレータの機能を実装したクラス.
 *
 * <p>アプリケーションのライフサイクルの管理は {@link SimulatorApplicationListener} が行う.
 *
 * @author K.Koike
 */
public class BhSimulatorImpl implements BhSimulator {

  /** シミュレータを実行するスレッドの名前. */
  private static final String SIMULATOR_THREAD_NAME = "BhSimulator";

  private final BhSimulatorSettings settings;
  private final SimulatorWindowImpl windowManager;
  private final ConfigurationImpl configuration;
  private final CallbackRegistryImpl cbRegistry;
  /** シミュレータのアプリケーションのライフサイクルを管理するオブジェクト. */
  private final SimulatorApplicationListener appListener;
  private final CountDownLatch latch = new CountDownLatch(1);
  /** シミュレータの初期化が完了した場合 true. */
  private volatile boolean initialized = false;
  /** シミュレータを実行するスレッドを供給するオブジェクト. */
  private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
    var thread = new Thread(runnable, SIMULATOR_THREAD_NAME);
    thread.setDaemon(true);
    return thread;
  });

  /** コンストラクタ. */
  public BhSimulatorImpl(BhSimulatorSettings settings) {
    this.settings = settings;
    windowManager = new SimulatorWindowImpl(settings);
    configuration = new ConfigurationImpl();
    cbRegistry = new CallbackRegistryImpl();
    appListener = new SimulatorApplicationListener(
        settings,
        windowManager,
        cbRegistry.onKeyPressedInvoker::invoke,
        this::onInitialized);
  }

  /** シミュレータの初期化が完了したときに呼ばれる処理. */
  private void onInitialized() {
    initialized = true;
    latch.countDown();
  }

  @Override
  public void start() {
    var config = new Lwjgl3ApplicationConfiguration();
    config.setWindowListener(windowManager);
    if (canRestoreWindowState()) {
      config.setWindowedMode(settings.window.width.get(), settings.window.height.get());
      config.setWindowPosition(settings.window.posX.get(), settings.window.posY.get());
      config.setMaximized(settings.window.maximized.get());
    } else {
      config.setWindowedMode(1000, 750);
    }
    config.setForegroundFPS(settings.foregroundFps.get());
    new Lwjgl3Application(appListener, config);
  }

  @Override
  public Future<?> startAsync() {
    Future<?> future = executor.submit(() -> {
      try {
        start();
      } finally {
        // 初期化の完了前にシミュレータが終了した場合のために, 待機中のスレッドを解放する.
        latch.countDown();
      }
    });
    executor.shutdown();
    return future;
  }

  @Override
  public Future<?> startAsync(int timeout) throws SimulatorInitializationException {
    Future<?> future = startAsync();
    if (waitForInitialization(timeout)) {
      return future;
    }
    halt();
    throw new SimulatorInitializationException(
        "The simulator failed to start within %s seconds.".formatted(timeout),
        getFailureCause(future));
  }

  /**
   * シミュレータを実行するタスクが例外によって終了している場合, その原因を取得する.
   *
   * @param future シミュレータを実行するタスクの {@link Future} オブジェクト
   * @return シミュレータの起動に失敗した原因. 原因を特定できない場合 null.
   */
  private Throwable getFailureCause(Future<?> future) {
    if (!future.isDone()) {
      return null;
    }
    try {
      future.get();
      return null;
    } catch (ExecutionException e) {
      return e.getCause();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return e;
    }
  }

  /** ウィンドウの状態を復元できるかどうか調べる. */
  private boolean canRestoreWindowState() {
    return settings.window.width.get() > 0
        && settings.window.height.get() > 0
        && settings.window.posX.get() > Integer.MIN_VALUE
        && settings.window.posY.get() > Integer.MIN_VALUE;
  }

  @Override
  public void halt() {
    appListener.requestHalt();
  }

  @Override
  public boolean waitForInitialization(int timeout) {
    try {
      return latch.await(timeout, TimeUnit.SECONDS) && initialized;
    } catch (InterruptedException e) {
      return false;
    }
  }

  @Override
  public boolean waitForInitialization() {
    try {
      latch.await();
    } catch (InterruptedException e) {
      return false;
    }
    return initialized;
  }

  @Override
  public SimulatorWindow getWindow() {
    return windowManager;
  }

  @Override
  public SimulatorCmdProcessor getCmdProcessor() {
    return appListener.getCmdProcessor();
  }

  @Override
  public BhSimulatorSettings getCurrentSettings() {
    return new BhSimulatorSettings(settings);
  }

  @Override
  public Configuration getConfiguration() {
    return configuration;
  }

  @Override
  public CallbackRegistry getCallbackRegistry() {
    return cbRegistry;
  }

  /** シミュレータの設定を変更する機能を提供するクラス. */
  private class ConfigurationImpl implements Configuration {

    @Override
    public WritableBoolean focusOnSimulatorChange() {
      return settings.focusOnSimulatorChange;
    }
  }

  /** イベントハンドラの管理を行うクラス. */
  private static class CallbackRegistryImpl implements CallbackRegistry {

    /** キーが押されたときのイベントハンドラを管理するオブジェクト. */
    private final ConsumerInvoker<KeyboardKey> onKeyPressedInvoker =
        new ConcurrentConsumerInvoker<>();

    @Override
    public ConsumerInvoker<KeyboardKey>.Registry getOnKeyPressed() {
      return onKeyPressedInvoker.getRegistry();
    }
  }
}
