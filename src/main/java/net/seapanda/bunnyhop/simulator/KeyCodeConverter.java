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

import com.badlogic.gdx.Input.Keys;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.seapanda.bunnyhop.simulator.api.KeyboardKey;

public class KeyCodeConverter {

  public static Map<Integer, KeyboardKey> gdxToSimKey = new HashMap<>();

  static {
    gdxToSimKey.put(Keys.NUM_0, KeyboardKey.DIGIT_0);
    gdxToSimKey.put(Keys.NUMPAD_0, KeyboardKey.DIGIT_0);
    gdxToSimKey.put(Keys.NUM_1, KeyboardKey.DIGIT_1);
    gdxToSimKey.put(Keys.NUMPAD_1, KeyboardKey.DIGIT_1);
    gdxToSimKey.put(Keys.NUM_2, KeyboardKey.DIGIT_2);
    gdxToSimKey.put(Keys.NUMPAD_2, KeyboardKey.DIGIT_2);
    gdxToSimKey.put(Keys.NUM_3, KeyboardKey.DIGIT_3);
    gdxToSimKey.put(Keys.NUMPAD_3, KeyboardKey.DIGIT_3);
    gdxToSimKey.put(Keys.NUM_4, KeyboardKey.DIGIT_4);
    gdxToSimKey.put(Keys.NUMPAD_4, KeyboardKey.DIGIT_4);
    gdxToSimKey.put(Keys.NUM_5, KeyboardKey.DIGIT_5);
    gdxToSimKey.put(Keys.NUMPAD_5, KeyboardKey.DIGIT_5);
    gdxToSimKey.put(Keys.NUM_6, KeyboardKey.DIGIT_6);
    gdxToSimKey.put(Keys.NUMPAD_6, KeyboardKey.DIGIT_6);
    gdxToSimKey.put(Keys.NUM_7, KeyboardKey.DIGIT_7);
    gdxToSimKey.put(Keys.NUMPAD_7, KeyboardKey.DIGIT_7);
    gdxToSimKey.put(Keys.NUM_8, KeyboardKey.DIGIT_8);
    gdxToSimKey.put(Keys.NUMPAD_8, KeyboardKey.DIGIT_8);
    gdxToSimKey.put(Keys.NUM_9, KeyboardKey.DIGIT_9);
    gdxToSimKey.put(Keys.NUMPAD_9, KeyboardKey.DIGIT_9);
    gdxToSimKey.put(Keys.UP, KeyboardKey.UP);
    gdxToSimKey.put(Keys.DPAD_UP, KeyboardKey.UP);
    gdxToSimKey.put(Keys.DOWN, KeyboardKey.DOWN);
    gdxToSimKey.put(Keys.DPAD_DOWN, KeyboardKey.DOWN);
    gdxToSimKey.put(Keys.RIGHT, KeyboardKey.RIGHT);
    gdxToSimKey.put(Keys.DPAD_RIGHT, KeyboardKey.RIGHT);
    gdxToSimKey.put(Keys.LEFT, KeyboardKey.LEFT);
    gdxToSimKey.put(Keys.DPAD_LEFT, KeyboardKey.LEFT);
    gdxToSimKey.put(Keys.SHIFT_LEFT, KeyboardKey.SHIFT);
    gdxToSimKey.put(Keys.SHIFT_RIGHT, KeyboardKey.SHIFT);
    gdxToSimKey.put(Keys.CONTROL_LEFT, KeyboardKey.CONTROL);
    gdxToSimKey.put(Keys.CONTROL_RIGHT, KeyboardKey.CONTROL);
    gdxToSimKey.put(Keys.SPACE, KeyboardKey.SPACE);
    gdxToSimKey.put(Keys.ENTER, KeyboardKey.ENTER);
    gdxToSimKey.put(Keys.NUMPAD_ENTER, KeyboardKey.ENTER);
    gdxToSimKey.put(Keys.A, KeyboardKey.A);
    gdxToSimKey.put(Keys.B, KeyboardKey.B);
    gdxToSimKey.put(Keys.C, KeyboardKey.C);
    gdxToSimKey.put(Keys.D, KeyboardKey.D);
    gdxToSimKey.put(Keys.E, KeyboardKey.E);
    gdxToSimKey.put(Keys.F, KeyboardKey.F);
    gdxToSimKey.put(Keys.G, KeyboardKey.G);
    gdxToSimKey.put(Keys.H, KeyboardKey.H);
    gdxToSimKey.put(Keys.I, KeyboardKey.I);
    gdxToSimKey.put(Keys.J, KeyboardKey.J);
    gdxToSimKey.put(Keys.K, KeyboardKey.K);
    gdxToSimKey.put(Keys.L, KeyboardKey.L);
    gdxToSimKey.put(Keys.M, KeyboardKey.M);
    gdxToSimKey.put(Keys.N, KeyboardKey.N);
    gdxToSimKey.put(Keys.O, KeyboardKey.O);
    gdxToSimKey.put(Keys.P, KeyboardKey.P);
    gdxToSimKey.put(Keys.Q, KeyboardKey.Q);
    gdxToSimKey.put(Keys.R, KeyboardKey.R);
    gdxToSimKey.put(Keys.S, KeyboardKey.S);
    gdxToSimKey.put(Keys.T, KeyboardKey.T);
    gdxToSimKey.put(Keys.U, KeyboardKey.U);
    gdxToSimKey.put(Keys.V, KeyboardKey.V);
    gdxToSimKey.put(Keys.W, KeyboardKey.W);
    gdxToSimKey.put(Keys.X, KeyboardKey.X);
    gdxToSimKey.put(Keys.Y, KeyboardKey.Y);
    gdxToSimKey.put(Keys.Z, KeyboardKey.Z);
  }

  public static Optional<KeyboardKey> convert(int keyCode) {
    return Optional.ofNullable(gdxToSimKey.get(keyCode));
  }
}
