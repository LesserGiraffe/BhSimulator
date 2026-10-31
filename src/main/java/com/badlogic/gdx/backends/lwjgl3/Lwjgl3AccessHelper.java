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

package com.badlogic.gdx.backends.lwjgl3;

/**
 * {@link Lwjgl3Window} のフィールドにアクセスするためのヘルパークラス.
 *
 * @author K.Koike
 */
public class Lwjgl3AccessHelper {

  private final Lwjgl3Window window;

  public Lwjgl3AccessHelper(Lwjgl3Window window) {
    this.window = window;
  }

  public Lwjgl3Window getWindow() {
    return window;
  }

  public Lwjgl3Graphics getGraphics() {
    return window.getGraphics();
  }

  public Lwjgl3Input getInput() {
    return window.getInput();
  }

  public Lwjgl3ApplicationBase getApplication() {
    return window.application;
  }
}
