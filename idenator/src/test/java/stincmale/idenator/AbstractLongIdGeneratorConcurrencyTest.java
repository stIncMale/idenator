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

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Currently, this test expects only strictly increasing {@link LongIdGenerator}s.
 */
//TODO create tests with https://github.com/Devexperts/lin-check
public abstract class AbstractLongIdGeneratorConcurrencyTest extends AbstractLongIdGeneratorTest {
  private final int numberOfThreads;
  private ExecutorService ex;

  @SafeVarargs
  protected AbstractLongIdGeneratorConcurrencyTest(final int numberOfThreads, final Supplier<LongIdGenerator>... longIdGeneratorCreators) {
    super(longIdGeneratorCreators);
    this.numberOfThreads = numberOfThreads;
  }

  private static final void doTest(
    final LongIdGenerator idGen,
    final int numberOfThreads,
    final int numberOfIdsPerThread,
    final ExecutorService ex) {
    final ConcurrentSkipListMap<Long, Long> uniqueIds = new ConcurrentSkipListMap<>();
    final AtomicReference<Long> firstDuplicateId = new AtomicReference<>();
    final AtomicReference<RuntimeException> firstException = new AtomicReference<>();
    final Phaser latch = new Phaser(numberOfThreads + 1);
    for (int tidx = 0; tidx < numberOfThreads; tidx++) {
      ex.submit(() -> {
        try {
          latch.arriveAndAwaitAdvance();
          final long[] ids = new long[numberOfIdsPerThread];
          for (int i = 0; i < numberOfIdsPerThread; i++) {
            ids[i] = idGen.generate();//collect ids into a local array to avoid additional synchronization introduced by a concurrent map
          }
          for (long id : ids) {
            uniqueIds.merge(id, id, (existingId, newId) -> {
              firstDuplicateId.compareAndSet(null, existingId);
              throw new AssertionError();
            });
          }
        } catch (final RuntimeException e) {
          firstException.compareAndSet(null, e);
        } finally {
          latch.arriveAndDeregister();
        }
      });
    }
    latch.arriveAndAwaitAdvance();//release the latch for all tasks
    latch.arriveAndAwaitAdvance();//await for completion of all tasks
    if (firstException.get() != null) {
      throw new RuntimeException(firstException.get());
    }
    assertNull(firstDuplicateId.get(), String.format("Generated id %s more than once from %s", firstDuplicateId.get(), idGen));
    assertEquals(numberOfThreads * numberOfIdsPerThread, uniqueIds.size(), idGen.toString());
    //LongIdGenerator is allowed to generate non-consecutive identifiers, thus we are asserting <=
    assertTrue(numberOfThreads * numberOfIdsPerThread - 1 <= uniqueIds.lastKey(), idGen.toString());
  }

  @Test
  final void test() {
    final int numberOfTestIterations = 4000;
    final int numberOfIdsPerThread = 4000;
    getLongIdGeneratorCreators().forEach(idGenCreator -> {
      for (int i = 1; i <= numberOfTestIterations; i++) {
        doTest(idGenCreator.get(), numberOfThreads, numberOfIdsPerThread, ex);
      }
    });
  }

  @BeforeEach
  final void beforeEach() {
    ex = Executors.newFixedThreadPool(numberOfThreads);
  }

  @AfterEach
  final void afterEach() {
    ex.shutdownNow();
  }
}
