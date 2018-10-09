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

import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 * Currently, this test expects only strictly increasing {@link LongIdGenerator}s
 * which start with 0 identifier and generate consecutive identifiers when used sequentially.
 */
public abstract class AbstractLongIdGeneratorUnitTest extends AbstractLongIdGeneratorTest {
  @SafeVarargs
  protected AbstractLongIdGeneratorUnitTest(final Supplier<LongIdGenerator>... longIdGeneratorCreators) {
    super(longIdGeneratorCreators);
  }

  @Test
  final void generate() {
    getLongIdGeneratorCreators().forEach(idGenCreator -> {
      final LongIdGenerator idGen = idGenCreator.get();
      assertEquals(0, idGen.generate(), idGen.toString());
      final int iterations = 15;
      for (int i = 0; i < iterations; i++) {
        idGen.generate();
      }
      assertEquals(iterations + 1, idGen.generate(), idGen.toString());
    });
  }
}
