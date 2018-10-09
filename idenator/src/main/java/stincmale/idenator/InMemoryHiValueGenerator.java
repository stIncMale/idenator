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
package stincmale.idenator;

import static stincmale.idenator.AbstractHiLoLongIdGenerator.UNINITIALIZED;
import stincmale.idenator.auxiliary.Sleeper;
import stincmale.idenator.doc.NotThreadSafe;
import static stincmale.idenator.internal.util.Preconditions.checkNotNull;

/**
 * A concurrent non-persistent strictly increasing consecutive implementation of {@link HiValueGenerator}.
 */
@NotThreadSafe
public final class InMemoryHiValueGenerator implements HiValueGenerator {
  private final Sleeper sleeper;
  private long hi;

  /**
   * @param startHi The initial <i>hi</i> value that will be returned by {@link #nextHi()}.
   * @param sleeper A {@link Sleeper} allowing to emulate a delay while {@linkplain #nextHi() generating} a <i>hi</i> value,
   * as if it was actually retrieved from a persistent storage.
   */
  public InMemoryHiValueGenerator(final long startHi, final Sleeper sleeper) {
    hi = startHi;
    this.sleeper = checkNotNull(sleeper, "sleeper");
  }

  @Override
  public final long nextHi() {
    sleeper.sleep();
    final long result = hi++;//return hi then increment
    return result == UNINITIALIZED ? hi++ : result;
  }

  @Override
  public final String toString() {
    return getClass().getSimpleName() +
      "{sleeper=" + sleeper +
      '}';
  }
}
