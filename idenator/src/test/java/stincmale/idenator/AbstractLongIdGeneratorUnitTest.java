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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static stincmale.idenator.internal.util.Utils.format;

/**
 * This test expects strictly increasing {@link AbstractTwoPhaseLongIdGenerator}s
 * which do not skip identifiers when used sequentially unless this is required by the specification.
 */
public abstract class AbstractLongIdGeneratorUnitTest extends AbstractLongIdGeneratorTest {
  protected AbstractLongIdGeneratorUnitTest(final LongIdGeneratorCreatorAndParams... longIdGeneratorCreators) {
    super(longIdGeneratorCreators);
  }

  private final static void testNextPooled(final LongIdGeneratorCreatorAndParams idGenCreator) {
    final LongIdGenerator idGen = idGenCreator.get();
    long id = idGen.next();
    assertEquals(idGenCreator.getStartHi(), id, idGen.toString());
    final int numberOfIterations = 200 * Math.toIntExact(idGenCreator.getLoUpperBoundOpen());
    int numberOfIdGenNextCalls = 1;
    int numberOfTimesLoValuesWereExhausted = 1;
    for (int i = 0; i < numberOfIterations; i++) {
      final boolean exhaustedLoValues = numberOfIdGenNextCalls % idGenCreator.getLoUpperBoundOpen() == 0;
      final long newId;
      if (exhaustedLoValues) {
        numberOfTimesLoValuesWereExhausted++;
        if (numberOfTimesLoValuesWereExhausted % 2 == 0) {
          newId = idGenCreator.getHiGenerator().next();
        } else {
          newId = idGen.next();
          numberOfIdGenNextCalls++;
        }
      } else {
        newId = idGen.next();
        numberOfIdGenNextCalls++;
      }
      assertTrue(newId - id > 0, format("i=%s, numberOfIdGenNextCalls=%s, numberOfTimesLoValuesWereExhausted=%s, newId=%s, id=%s, idGen=%s",
        i, numberOfIdGenNextCalls, numberOfTimesLoValuesWereExhausted, newId, id, idGen.toString()));
      id = newId;
    }
  }

  private final static void testNextHiLo(final LongIdGeneratorCreatorAndParams idGenCreator) {
    final LongIdGenerator idGen = idGenCreator.get();
    long id = idGen.next();
    final int numberOfIterations = 200 * Math.toIntExact(idGenCreator.getLoUpperBoundOpen());
    for (int i = 0; i < numberOfIterations; i++) {
      final long newId = idGen.next();
      assertTrue(newId - id > 0, format("i=%s, newId=%s, id=%s, idGen=%s", i, newId, id, idGen.toString()));
      id = newId;
    }
  }

  @Test
  final void next() {
    getLongIdGeneratorCreators().forEach(idGenCreator -> {
      if (idGenCreator.isPooled()) {
        testNextPooled(idGenCreator);
      } else {
        testNextHiLo(idGenCreator);
      }
    });
  }
}
