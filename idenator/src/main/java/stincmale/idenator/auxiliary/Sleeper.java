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
package stincmale.idenator.auxiliary;

import stincmale.idenator.doc.NotThreadSafe;

/**
 * An object allowing to put a thread calling {@link #sleep()} into {@link Thread.State#TIMED_WAITING} state
 * for a duration decided be the implementation.
 */
@NotThreadSafe
public interface Sleeper {
  /**
   * Puts a thread calling this method into {@link Thread.State#TIMED_WAITING} for a duration decided be the implementation.
   */
  void sleep();
}