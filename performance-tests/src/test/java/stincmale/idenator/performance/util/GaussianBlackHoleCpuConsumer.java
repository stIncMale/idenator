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
package stincmale.idenator.performance.util;

import static java.lang.Math.round;
import org.openjdk.jmh.infra.Blackhole;
import stincmale.idenator.doc.ThreadSafe;
import stincmale.idenator.internal.Delayer;
import static stincmale.idenator.internal.util.Preconditions.checkArgument;
import static stincmale.idenator.internal.util.Preconditions.checkNotNull;
import static stincmale.idenator.internal.util.Utils.format;

/**
 * A {@link Delayer} that introduces a delay by performing computations with {@link Blackhole#consumeCPU(long)}
 * for a duration roughly proportional to a number picked by a pseudorandom generator of Gaussian distributed (but capped) durations.
 */
@ThreadSafe
public final class GaussianBlackHoleCpuConsumer implements Delayer {
  private final GaussianRandom rnd;

  public GaussianBlackHoleCpuConsumer(final long midrange, final long absoluteDeviation) {
    checkNotNull(midrange, "midrange");
    checkArgument(midrange >= 0, "midrange", "Must not be negative");
    checkNotNull(absoluteDeviation, "absoluteDeviation");
    checkArgument(absoluteDeviation >= 0, "absoluteDeviation", "Must not be negative");
    checkArgument(absoluteDeviation <= midrange, "absoluteDeviation",
        () -> format("%s=%s must be less than or equal to %s=%s", "absoluteDeviation", absoluteDeviation, "midrange", midrange));
    rnd = new GaussianRandom(midrange, absoluteDeviation);
  }

  @Override
  public final void delay() {
    Blackhole.consumeCPU(round(rnd.next()));
  }

  @Override
  public final String toString() {
    return getClass().getSimpleName() +
        "{midrangeMillis=" + rnd.getMidrange() +
        ", absoluteDeviationMillis=" + rnd.getAbsoluteDeviation() +
        '}';
  }
}
