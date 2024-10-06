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
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;

/**
 * メインクラス.
 *
 * @author K.Koike
 */
public class App {

  /** メインメソッド. */
  public static void main(String[] args) throws Exception {
    Lwjgl3WindowListener windowListener = new Lwjgl3WindowAdapter() {
      @Override
      public boolean closeRequested() {
        return true;
      }
    };
    
    var config = new Lwjgl3ApplicationConfiguration();
    config.setWindowListener(windowListener);
    config.setWindowedMode(800, 600);    
    // config.setDecorated(false);
    var app = new Lwjgl3Application(new BhSimulator(), config);
    // Lwjgl3Window window = ((Lwjgl3Graphics) app.getGraphics()).getWindow();
    // window.iconifyWindow(); // iconify the window
    // window.restoreWindow();
  }
}
