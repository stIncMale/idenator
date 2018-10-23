/*
 * Copyright 2018 Valiantsin Kavalenka
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
package stincmale.idenator.internal;

import stincmale.idenator.doc.ThreadSafe;

/**
 * An implementation of {@link Sleeper} which never puts a thread into {@link Thread.State#TIMED_WAITING} state.
 */
@ThreadSafe
public final class NoopSleeper implements Sleeper {
  private static final NoopSleeper instance = new NoopSleeper();

  private NoopSleeper() {
  }

  /**
   * Always returns the same instance.
   *
   * @return An instance of {@link NoopSleeper}.
   */
  public static final NoopSleeper instance() {
    return instance;
  }

  /**
   * Does nothing.
   */
  @Override
  public final void sleep() {
  }
}
