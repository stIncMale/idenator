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

import java.util.concurrent.atomic.AtomicLong;
import stincmale.idenator.doc.ThreadSafe;

/**
 * A {@linkplain ThreadSafe thread-safe} implementation of {@link AbstractTwoPhaseLongIdGenerator}.
 */
@ThreadSafe
public final class ConcurrentTwoPhaseLongIdGenerator extends AbstractTwoPhaseLongIdGenerator {
  private final Object mutex;
  private final AtomicLong lo;
  private volatile long hi;

  /**
   * @param hiGenerator See {@link AbstractTwoPhaseLongIdGenerator#AbstractTwoPhaseLongIdGenerator(LongIdGenerator, long, boolean)}.
   * @param loUpperBoundOpen See
   * {@link AbstractTwoPhaseLongIdGenerator#AbstractTwoPhaseLongIdGenerator(LongIdGenerator, long, boolean)}.
   * @param pooled See {@link AbstractTwoPhaseLongIdGenerator#AbstractTwoPhaseLongIdGenerator(LongIdGenerator, long, boolean)}.
   */
  public ConcurrentTwoPhaseLongIdGenerator(final LongIdGenerator hiGenerator, final long loUpperBoundOpen, final boolean pooled) {
    super(hiGenerator, loUpperBoundOpen, pooled);
    mutex = new Object();
    lo = new AtomicLong(-1);
    hi = UNINITIALIZED;
  }

  @Override
  public final long next() {
    final long loUpperBoundOpen = getLoUpperBoundOpen();
    long hi = UNINITIALIZED;
    long lo = UNINITIALIZED;
    final int maxAttempts = 32;
    for (int attemptIdx = 0; attemptIdx <= maxAttempts; attemptIdx++) {
      final boolean optimisticAttempt = attemptIdx < maxAttempts;
      if (optimisticAttempt) {
        hi = initializedHi();
        lo = this.lo.incrementAndGet();
      }
      if (lo >= loUpperBoundOpen ||//lo is too big, we need to reset lo and advance hi
        !optimisticAttempt) {//no optimistic attempts left, it's time to use locking
        synchronized (mutex) {
          lo = this.lo.incrementAndGet();
          if (lo >= loUpperBoundOpen) {//re-check whether we still need to reset lo and advance hi
            hi = nextId();
            this.hi = hi;
            lo = 0;
            this.lo.set(lo);
          } else {//lo is fine, but we still need to read hi under the exclusive lock to make sure that hi+lo read is atomic
            hi = this.hi;
          }
          break;//hi+lo read was atomic because it was made under the exclusive lock
        }
      } else {//lo is fine, check whether optimistic read succeeded
        long hi2 = this.hi;
        if (hi2 == hi) {//optimistic read succeeded, hence read hi+lo was atomic and we can break the loop
          break;
        }// else continue this while loop because hi was changed while we were reading lo, so we can't guarantee that the hi+lo read is atomic
      }
    }
    return calculateId(hi, lo);
  }

  private final long initializedHi() {
    long hi = this.hi;
    if (hi == UNINITIALIZED) {
      synchronized (mutex) {
        hi = this.hi;
        if (hi == UNINITIALIZED) {
          hi = nextId();
          this.hi = hi;
        }
      }
    }
    return hi;
  }
}
