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

import java.util.concurrent.ThreadLocalRandom;
import stincmale.idenator.doc.ThreadSafe;

/**
 * A pseudorandom generator of Gaussian distributed (but capped) numbers.
 */
@ThreadSafe
final class GaussianRandom {
  private final double midrange;
  private final double absoluteDeviation;

  GaussianRandom(final double midrange, final double absoluteDeviation) {
    this.midrange = midrange;
    this.absoluteDeviation = absoluteDeviation;
  }

  /**
   * @return The next pseudorandom Gaussian distributed number.
   */
  final double next() {
    double rnd = ThreadLocalRandom.current().nextGaussian() * absoluteDeviation / 2d + midrange;
    final double min = midrange - absoluteDeviation;
    if (rnd < min) {
      rnd = min;
    } else {
      final double max = midrange + absoluteDeviation;
      if (rnd > max) {
        rnd = max;
      }
    }
    return rnd;
  }

  final double getMidrange() {
    return midrange;
  }

  final double getAbsoluteDeviation() {
    return absoluteDeviation;
  }

  @Override
  public final String toString() {
    return getClass().getSimpleName() +
        "{midrange=" + midrange +
        ", absoluteDeviation=" + absoluteDeviation +
        '}';
  }
}
