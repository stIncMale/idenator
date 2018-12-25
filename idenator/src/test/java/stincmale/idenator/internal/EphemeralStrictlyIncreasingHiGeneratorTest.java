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

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import static stincmale.idenator.AbstractTwoPhaseLongIdGenerator.UNINITIALIZED;
import stincmale.idenator.util.TestTag;

@Tag(TestTag.UNIT)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
final class EphemeralStrictlyIncreasingHiGeneratorTest {
  private EphemeralStrictlyIncreasingHiGeneratorTest() {
  }

  private final static void testNext(final long startHi, final long sparseness) {
    final EphemeralStrictlyIncreasingHiGenerator idGen = new EphemeralStrictlyIncreasingHiGenerator(startHi, sparseness, NoopSleeper.instance());
    long id = idGen.next();
    assertEquals(startHi, id);
    for (int i = 1; i < 100 * sparseness; i++) {
      if (id + i * (sparseness + 1) == UNINITIALIZED) {
        continue;
      }
      id = idGen.next();
      assertEquals(startHi + i * (sparseness + 1), id);
    }
  }

  @Test
  final void next() {
    testNext(0, 0);
    testNext(0, 10);
    testNext(Long.MIN_VALUE + 1, 0);
    testNext(Long.MIN_VALUE + 1, 1);
    testNext(Long.MAX_VALUE, 0);
    testNext(Long.MAX_VALUE, 5);
  }
}
