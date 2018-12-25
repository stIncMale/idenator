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

import stincmale.idenator.AbstractTwoPhaseLongIdGenerator;
import static stincmale.idenator.AbstractTwoPhaseLongIdGenerator.UNINITIALIZED;
import stincmale.idenator.LongIdGenerator;
import stincmale.idenator.doc.NotThreadSafe;
import static stincmale.idenator.internal.util.Preconditions.checkArgument;
import static stincmale.idenator.internal.util.Preconditions.checkNotNull;
import static stincmale.idenator.internal.util.Utils.format;

/**
 * An ephemeral strictly increasing (see {@link stincmale.idenator}) implementation of {@link LongIdGenerator}.
 */
@NotThreadSafe
public final class EphemeralStrictlyIncreasingHiGenerator implements LongIdGenerator {
  private final Sleeper sleeper;
  private final long sparseness;
  private long hi;

  /**
   * @param startHi The initial {@code hi} value that will be returned by the first invocation of {@link #next()}.
   * Must not be equal to {@link AbstractTwoPhaseLongIdGenerator#UNINITIALIZED}.
   * @param sparseness 0 if this ID generator is not required to be sparse (see {@link stincmale.idenator}),
   * otherwise the value of a desired sparseness.
   * @param sleeper A {@link Sleeper} allowing to emulate a delay while {@linkplain #next() generating} a {@code hi} value.
   */
  public EphemeralStrictlyIncreasingHiGenerator(final long startHi, final long sparseness, final Sleeper sleeper) {
    checkArgument(startHi != UNINITIALIZED, "startHi", () -> format("Must not be equal to %s", UNINITIALIZED));
    checkArgument(sparseness >= 0, "sparseness", "Must not be be negative");
    hi = startHi;
    this.sparseness = sparseness;
    this.sleeper = checkNotNull(sleeper, "sleeper");
  }

  @Override
  public final long next() {
    sleeper.sleep();
    return getAndAdvanceHi();
  }

  private final long getAndAdvanceHi() {
    final long result = hi;
    hi = hi + sparseness + 1;
    return result;
  }

  @Override
  public final String toString() {
    return getClass().getSimpleName() +
        "{sparseness=" + sparseness +
        ", sleeper=" + sleeper +
        '}';
  }
}
