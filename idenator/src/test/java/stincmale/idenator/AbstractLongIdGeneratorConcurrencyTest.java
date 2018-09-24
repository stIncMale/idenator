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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractLongIdGeneratorConcurrencyTest extends AbstractLongIdGeneratorTest {
  private final int numberOfThreads;
  private ExecutorService ex;

  protected AbstractLongIdGeneratorConcurrencyTest(
      final Supplier<LongIdGenerator> longIdGeneratorCreator,
      final int numberOfThreads) {
    super(longIdGeneratorCreator);
    this.numberOfThreads = numberOfThreads;
  }

  @Test
  public final void test() {
    final int numberOfTestIterations = 50;
    final int numberOfIdsPerThread = 1000;
    for (int i = 1; i <= numberOfTestIterations; i++) {
      final LongIdGenerator idGen = getLongIdGeneratorCreator().get();
      doTest(idGen, numberOfThreads, numberOfIdsPerThread, ex);
    }
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
            ids[i] = idGen.generate();//collect ids into a local array to avoid additional synchronization introduced by concurrent map
          }
          for (long id : ids) {
            uniqueIds.merge(id, id, (existingId, newId) -> {
              firstDuplicateId.compareAndSet(null, existingId);
              throw new AssertionError(existingId);
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
    assertNull(firstDuplicateId.get(), String.format("Generated id %s more than once", firstDuplicateId.get()));
    assertEquals(numberOfThreads * numberOfIdsPerThread, uniqueIds.size());
    assertTrue(numberOfThreads * numberOfIdsPerThread - 1 <= uniqueIds.lastKey());
  }

  @BeforeEach
  public final void beforeEach() {
    ex = Executors.newFixedThreadPool(numberOfThreads);
  }

  @AfterEach
  public final void afterEach() {
    ex.shutdownNow();
  }
}
