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

import static net.seapanda.bunnyhop.simulator.common.BhSimSettings.Ui.window;
import static net.seapanda.bunnyhop.simulator.common.BhSimSettings.Window;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import org.lwjgl.glfw.GLFW;

/**
 * シミュレータのウィンドウの状態を管理するクラス.
 *
 * @author K.Koike
 */
class WindowStateManager {

  /** 最大化および最小化されていない状態でのウィンドウの幅. */
  private int width = window.width;
  /** 最大化および最小化されていない状態でのウィンドウの高さ. */
  private int height = window.height;
  /** 最大化および最小化されていない状態でのウィンドウの X 座標. */
  private int posX = window.posX;
  /** 最大化および最小化されていない状態でのウィンドウの Y 座標. */
  private int posY = window.posY;
  /** ウィンドウが最大化されているかどうか. */
  private boolean isMaximized = window.maximized;

  /** コンストラクタ. */
  public WindowStateManager() {}

  /** {@link Window} に格納されたウィンドウの状態を復元する. */
  void restoreWindowState() {
    long wh = getWindowHandle();
    if (!canRestoreWindowState()) {
      return;
    }
    GLFW.glfwSetWindowSize(wh, window.width, window.height);
    GLFW.glfwSetWindowPos(wh, window.posX, window.posY);
    if (window.maximized) {
      GLFW.glfwMaximizeWindow(wh);
    }
  }

  /** ウィンドウの状態を復元できるかどうか調べる. */
  private boolean canRestoreWindowState() {
    return window.width >= 0
        && window.height >= 0
        && window.posX > Integer.MIN_VALUE
        && window.posY > Integer.MIN_VALUE;
  }

  /** ウィンドウの状態を {@link Window} に保存する. */
  void saveWindowState() {
    window.width = width;
    window.height = height;
    window.posX = posX;
    window.posY = posY;
    window.maximized = isMaximized;
  }

  /** ウィンドウの状態を記録する. */
  void updateWindowState() {
    long wh = getWindowHandle();
    isMaximized = isMaximized(wh);
    if (isMaximized || isIconified(wh)) {
      return;
    }
    width = Gdx.graphics.getWidth();
    height = Gdx.graphics.getHeight();
    int[] x = new int[1];
    int[] y = new int[1];
    GLFW.glfwGetWindowPos(wh, x, y);
    posX = x[0];
    posY = y[0];
  }

  private boolean isIconified(long windowHandle) {
    return GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
  }

  private boolean isMaximized(long windowHandle) {
    return GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE;
  }

  private long getWindowHandle() {
    return ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
  }
}
