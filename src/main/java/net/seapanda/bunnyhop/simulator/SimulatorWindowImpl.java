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

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3AccessHelper;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import java.util.concurrent.atomic.AtomicBoolean;
import net.seapanda.bunnyhop.simulator.api.BhSimulatorSettings;
import net.seapanda.bunnyhop.simulator.api.SimulatorWindow;
import net.seapanda.bunnyhop.utility.math.Vec2D;
import org.lwjgl.glfw.GLFW;

/**
 * シミュレータのウィンドウを操作するクラス.
 *
 * @author K.Koike
 */
class SimulatorWindowImpl extends Lwjgl3WindowAdapter implements SimulatorWindow {

  /** シミュレータの設定を格納したオブジェクト. */
  private final BhSimulatorSettings settings;
  /**
   * このオブジェクトが管理するウィンドウ.
   *
   * <p>メインスレッドから書き込まれ, 任意のスレッドから読み出されるので volatile とする.
   */
  private volatile Lwjgl3Window window;

  /** {@link #window} の非公開のフィールドにアクセスするためのヘルパーオブジェクト. */
  private volatile Lwjgl3AccessHelper accessHelper;

  private final AtomicBoolean isIconified = new AtomicBoolean(false);
  private final AtomicBoolean isFocused = new AtomicBoolean(false);

  /**
   * コンストラクタ.
   *
   * @param settings シミュレータの設定を格納したオブジェクト.
   */
  public SimulatorWindowImpl(BhSimulatorSettings settings) {
    this.settings = settings;
  }

  /** ウィンドウの状態を記録する. */
  void updateWindowState() {
    long wh = window.getWindowHandle();
    if (window == null || isMaximized(wh) || isIconified(wh)) {
      return;
    }

    int[] width = new int[1];
    int[] height = new int[1];
    GLFW.glfwGetWindowSize(wh, width, height);
    settings.window.width.set(width[0]);
    settings.window.height.set(height[0]);

    settings.window.posX.set(window.getPositionX());
    settings.window.posY.set(window.getPositionY());
  }

  /**
   * このオブジェクトが管理するウィンドウに紐づく {@link Lwjgl3AccessHelper} オブジェクトを取得する.
   *
   * @return {@link Lwjgl3AccessHelper} オブジェクト.
   *         ウィンドウの作成前は null を返す.
   */
  Lwjgl3AccessHelper getAccessHelper() {
    return accessHelper;
  }

  @Override
  public void created(Lwjgl3Window window) {
    this.window = window;
    accessHelper = new Lwjgl3AccessHelper(window);
  }

  @Override
  public void iconified(boolean isIconified) {
    this.isIconified.set(isIconified);
  }

  @Override
  public void maximized(boolean isMaximized) {
    settings.window.maximized.set(isMaximized);
  }

  @Override
  public void focusLost() {
    isFocused.set(false);
  }

  @Override
  public void focusGained() {
    isFocused.set(true);
  }

  @Override
  public boolean closeRequested() {
    return settings.closeOnRequested.get();
  }

  @Override
  public void setPosition(int x, int y) {
    if (window == null) {
      return;
    }
    settings.window.posX.set(x);
    settings.window.posY.set(y);
    window.postRunnable(() -> window.setPosition(x, y));
  }

  @Override
  public int getPositionX() {
    if (window == null) {
      return 0;
    }
    return settings.window.posX.get();
  }

  @Override
  public int getPositionY() {
    if (window == null) {
      return 0;
    }
    return settings.window.posY.get();
  }

  @Override
  public Vec2D getPosition() {
    if (window == null) {
      return new Vec2D(0, 0);
    }
    return new Vec2D(settings.window.posX.get(), settings.window.posY.get());
  }

  @Override
  public void setSize(int width, int height) {
    if (window == null) {
      return;
    }
    settings.window.width.set(width);
    settings.window.height.set(height);
    window.postRunnable(() -> GLFW.glfwSetWindowSize(window.getWindowHandle(), width, height));
  }

  @Override
  public int getWidth() {
    if (window == null) {
      return 0;
    }
    return settings.window.width.get();
  }

  @Override
  public int getHeight() {
    if (window == null) {
      return 0;
    }
    return settings.window.height.get();
  }

  @Override
  public Vec2D getSize() {
    if (window == null) {
      return new Vec2D(0, 0);
    }
    return new Vec2D(settings.window.width.get(), settings.window.height.get());
  }

  @Override
  public void iconify() {
    if (window == null) {
      return;
    }
    isIconified.set(true);
    settings.window.maximized.set(false);
    window.postRunnable(window::iconifyWindow);
  }

  @Override
  public boolean isIconified() {
    return window != null && isIconified.get();
  }

  /**
   * {@code windowHandle} で指定したウィンドウが最小化されているか調べる.
   *
   * <p>{@link #isIconified()} が呼ばれる前に最大化されているかを調べることが可能.
   * 最小化されたとき {@link #isIconified()} が呼ばれる前に {@link #updateWindowState()} が呼ばれる.
   * その際, 正しい状態を取得するためにこの関数を用意する.
   */
  private static boolean isIconified(long windowHandle) {
    return GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
  }

  @Override
  public void restore() {
    if (window == null) {
      return;
    }
    isIconified.set(false);
    settings.window.maximized.set(false);
    window.postRunnable(window::restoreWindow);
  }

  @Override
  public void maximize() {
    if (window == null) {
      return;
    }
    isIconified.set(false);
    settings.window.maximized.set(true);
    window.postRunnable(window::maximizeWindow);
  }

  @Override
  public boolean isMaximized() {
    return window != null && settings.window.maximized.get();
  }

  /**
   * {@code windowHandle} で指定したウィンドウが最大化されているか調べる.
   *
   * <p>{@link #isMaximized()} が呼ばれる前に最大化されているかを調べることが可能.
   * 最大化されたとき {@link #isMaximized()} が呼ばれる前に {@link #updateWindowState()} が呼ばれる.
   * その際, 正しい状態を取得するためにこの関数を用意する.
   */
  private static boolean isMaximized(long windowHandle) {
    return GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE;
  }

  @Override
  public void focus() {
    if (window == null) {
      return;
    }
    isFocused.set(true);
    window.postRunnable(window::focusWindow);
  }

  @Override
  public boolean isFocused() {
    return window != null && isFocused.get();
  }
}
