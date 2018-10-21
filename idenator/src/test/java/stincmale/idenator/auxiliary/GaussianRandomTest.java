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

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import stincmale.idenator.util.TestTag;

@Tag(TestTag.UNIT)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
final class GaussianRandomTest {
  private GaussianRandomTest() {
  }

  private static final void testNext(final GaussianRandom rnd) {
    final double min = rnd.getMidrange() - rnd.getAbsoluteDeviation();
    final double max = rnd.getMidrange() + rnd.getAbsoluteDeviation();
    for (int i = 0; i < 1000_000; i++) {
      final double value = rnd.next();
      assertTrue(value >= min);
      assertTrue(value <= max);
    }
  }

  @Test
  final void next() {
    testNext(new GaussianRandom(1, 0));
    testNext(new GaussianRandom(1, 1));
    testNext(new GaussianRandom(1234, 111));
    testNext(new GaussianRandom(Double.MAX_VALUE / 2d, Double.MAX_VALUE / 2d));
  }
}
