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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static stincmale.idenator.internal.util.Utils.format;

//TODO create tests with https://github.com/Devexperts/lin-check
public abstract class AbstractLongIdGeneratorConcurrencyTest extends AbstractLongIdGeneratorTest {
  private final int numberOfThreads;
  private ExecutorService ex;

  protected AbstractLongIdGeneratorConcurrencyTest(final int numberOfThreads, final LongIdGeneratorCreatorAndParams... longIdGeneratorCreators) {
    super(longIdGeneratorCreators);
    this.numberOfThreads = numberOfThreads;
  }

  private static final void doTest(
    final LongIdGeneratorCreatorAndParams idGenCreator,
    final long[][] threadLocalIdHolders,
    final ExecutorService ex) {
    final AtomicReference<Long> firstDuplicateId = new AtomicReference<>();
    final AtomicReference<RuntimeException> firstException = new AtomicReference<>();
    final int numberOfThreads = threadLocalIdHolders.length;
    final int numberOfIdsPerThread = threadLocalIdHolders[0].length;
    final ConcurrentMap<Long, Long> uniqueIds = new ConcurrentHashMap<>(numberOfThreads * numberOfIdsPerThread);
    final LongIdGenerator idGen = idGenCreator.get();
    final Phaser latch = new Phaser(numberOfThreads + 1);
    for (long[] threadLocalIds : threadLocalIdHolders) {
      ex.submit(() -> {
        try {
          latch.arriveAndAwaitAdvance();
          for (int i = 0; i < threadLocalIds.length; i++) {
            threadLocalIds[i] = idGen.next();//collect ids into a thread-local array to avoid synchronization introduced by a concurrent map
          }
          for (long id : threadLocalIds) {
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
    assertNull(firstDuplicateId.get(), format("Generated id %s more than once from %s", firstDuplicateId.get(), idGen));
    assertEquals(numberOfThreads * numberOfIdsPerThread, uniqueIds.size(), idGen.toString());
  }

  @Test
  final void test() {
    final int numberOfTestIterations = 1000;
    final int numberOfIdsPerThread = 4000;
    final long[][] threadLocalIds = new long[numberOfThreads][numberOfIdsPerThread];
    getLongIdGeneratorCreators().forEach(idGenCreator -> {
      for (int i = 1; i <= numberOfTestIterations; i++) {
        doTest(idGenCreator, threadLocalIds, ex);
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
