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
import stincmale.idenator.AbstractTwoPhaseLongIdGenerator;
import stincmale.idenator.LongIdGenerator;
import stincmale.idenator.doc.ThreadSafe;

@ThreadSafe
public final class OptimisticTwoPhaseLongIdGenerator1 extends AbstractTwoPhaseLongIdGenerator {
  private final Object mutex;
  private final AtomicLong lo;
  private volatile long hi;

  public OptimisticTwoPhaseLongIdGenerator1(final LongIdGenerator hiGenerator, final long loUpperBoundOpen, final boolean pooled) {
    super(hiGenerator, loUpperBoundOpen, pooled);
    mutex = new Object();
    lo = new AtomicLong(-1);
    hi = UNINITIALIZED;
  }

  @Override
  public final long next() {
    final long loUpperBoundOpen = getLoUpperBoundOpen();
    long hi;
    long lo;
    while (true) {//each iteration starts with an optimistic read attempt
      hi = initializedHi();
      lo = this.lo.incrementAndGet();
      if (lo >= loUpperBoundOpen) {//lo is too big, we probably need to reset lo and advance hi
        synchronized (mutex) {
          lo = this.lo.incrementAndGet();
          if (lo >= loUpperBoundOpen) {//re-check whether we still need to reset lo and advance hi
            hi = nextHi();
            this.hi = hi;
            lo = 0;
            this.lo.set(lo);
          } else {//lo is fine, but we still need to read hi under the exclusive lock to make sure that hi+lo read is atomic
            hi = this.hi;
          }
          break;//hi+lo read was atomic because it was made under the exclusive lock
        }
      } else {//lo is fine, check whether optimistic read succeeded
        if (this.hi == hi) {//optimistic read succeeded, hence read hi+lo was atomic and we can break the loop
          break;
        }//else continue this while loop because hi was changed while we were reading lo, so we can't guarantee that the hi+lo read is atomic
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
          hi = nextHi();
          this.hi = hi;
        }
      }
    }
    return hi;
  }
}
