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

import static java.lang.Math.round;
import java.time.Duration;
import stincmale.idenator.doc.ThreadSafe;
import static stincmale.idenator.internal.util.Preconditions.checkArgument;
import static stincmale.idenator.internal.util.Preconditions.checkNotNull;

/**
 * A {@link Sleeper} that {@linkplain #sleep() sleeps} for a duration
 * picked by a pseudorandom generator of Gaussian distributed (but capped) durations.
 */
@ThreadSafe
public final class GaussianSleeper implements Sleeper {
  private final GaussianRandom rnd;

  /**
   * @param midrange The mean of min duration and max duration, i.e. (min + max) / 2. Must not be negative.
   * @param absoluteDeviation The max absolute deviation of sleep durations from the {@code midrange}.
   * Must not be negative. Must not be greater than {@code midrange}.
   */
  public GaussianSleeper(final Duration midrange, final Duration absoluteDeviation) {
    checkNotNull(midrange, "midrange");
    checkArgument(!midrange.isNegative(), "midrange", "Must not be negative");
    checkNotNull(absoluteDeviation, "absoluteDeviation");
    checkArgument(!absoluteDeviation.isNegative(), "absoluteDeviation", "Must not be negative");
    checkArgument(absoluteDeviation.compareTo(midrange) <= 0, "absoluteDeviation",
      () -> String.format("%s=%s must be less than or equal to %s=%s", "absoluteDeviation", absoluteDeviation, "midrange", midrange));
    rnd = new GaussianRandom(midrange.toMillis(), absoluteDeviation.toMillis());
  }

  @Override
  public final void sleep() {
    try {
      Thread.sleep(round(rnd.next()));
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public final String toString() {
    return getClass().getSimpleName() +
      "{midrangeMillis=" + rnd.getMidrange() +
      ", absoluteDeviationMillis=" + rnd.getAbsoluteDeviation() +
      '}';
  }
}
