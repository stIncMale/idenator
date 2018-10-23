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

package stincmale.idenator.internal.variant;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.StampedLock;
import stincmale.idenator.AbstractTwoPhaseLongIdGenerator;
import stincmale.idenator.LongIdGenerator;
import stincmale.idenator.doc.ThreadSafe;

@ThreadSafe
public final class StampedTwoPhaseLongIdGenerator2 extends AbstractTwoPhaseLongIdGenerator {
  private final StampedLock lock;
  private final AtomicLong lo;
  private long hi;

  public StampedTwoPhaseLongIdGenerator2(final LongIdGenerator hiGenerator, final long loUpperBoundOpen, final boolean pooled) {
    super(hiGenerator, loUpperBoundOpen, pooled);
    lock = new StampedLock();
    lo = new AtomicLong(-1);
    hi = UNINITIALIZED;
  }

  @Override
  public final long next() {
    final long loUpperBoundOpen = getLoUpperBoundOpen();
    long hi = UNINITIALIZED;
    long lo = -1;
    final int maxAttempts = 4;
    for (int attemptIdx = 0; attemptIdx <= maxAttempts; attemptIdx++) {
      final boolean optimisticAttempt = attemptIdx < maxAttempts;
      final long optimisticStamp;
      if (optimisticAttempt) {
        optimisticStamp = initializeHi(lock.tryOptimisticRead());
        if (optimisticStamp == 0) {//failed to start optimistic read
          continue;
        }
        hi = this.hi;
        lo = this.lo.incrementAndGet();
      } else {
        optimisticStamp = 0;
      }
      if (lo >= loUpperBoundOpen ||//lo is too big, we probably need to reset lo and advance hi
        !optimisticAttempt) {//no optimistic attempts left, it's time to use locking
        final long exclusiveStamp = lock.writeLock();
        try {
          lo = this.lo.incrementAndGet();
          if (lo >= loUpperBoundOpen) {//re-check whether we still need to reset lo and advance hi
            lo = 0;
            this.lo.set(lo);
            hi = nextId();
            this.hi = hi;
          } else {//lo is fine, but we still need to read hi under the exclusive lock to make sure that hi+lo read is atomic
            hi = this.hi;
          }
          break;//hi+lo read was atomic because it was made under the exclusive lock
        } finally {
          lock.unlockWrite(exclusiveStamp);
        }
      } else {//lo is fine, check whether optimistic read succeeded
        if (lock.validate(optimisticStamp)) {//optimistic read succeeded, hence read hi+lo was atomic and we can break the loop
          break;
        }//else continue this while loop because hi was changed while we were reading lo, so we can't guarantee that the hi+lo read is atomic
      }
    }
    return calculateId(hi, lo);
  }

  private final long initializeHi(long optimisticStamp) {
    if (hi == UNINITIALIZED) {
      long exclusiveStamp = lock.writeLock();
      try {
        if (hi == UNINITIALIZED) {
          hi = nextId();
        }
      } finally {
        lock.unlockWrite(exclusiveStamp);
        optimisticStamp = lock.tryOptimisticRead();
      }
    }
    return optimisticStamp;
  }
}
