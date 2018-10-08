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
import static stincmale.idenator.internal.util.Constants.EXCLUDE_ASSERTIONS_FROM_BYTECODE;

/**
 * A concurrent implementation of {@link AbstractHiLoLongIdGenerator}.
 */
@ThreadSafe
public final class ConcurrentHiLoLongIdGenerator extends AbstractHiLoLongIdGenerator {
  private static final int MAX_ATTEMPTS_BEFORE_LOCKING = 5;
  private final AtomicLong lo;
  private final Object mutex;
  private volatile long hi;

  /**
   * @param hiValueGenerator See {@link AbstractHiLoLongIdGenerator#AbstractHiLoLongIdGenerator(HiValueGenerator, long)}.
   * @param loUpperBoundOpen This parameter specifies how many identifiers we can {@linkplain #generate() generate}
   * after calling {@link #nextHi()} without calling {@link #nextHi()} again.
   * <i>lo</i> ∈ [0; {@code loUpperBoundOpen}).
   */
  public ConcurrentHiLoLongIdGenerator(final HiValueGenerator hiValueGenerator, final long loUpperBoundOpen) {
    super(hiValueGenerator, loUpperBoundOpen);
    hi = UNINITIALIZED;
    lo = new AtomicLong(-1);
    mutex = new Object();
  }

  @Override
  public final long generate() {
    final long loUpperBoundOpen = getLoUpperBoundOpen();
    long hi = UNINITIALIZED;
    long lo = UNINITIALIZED;
    for (int attemptIdx = 0; attemptIdx <= MAX_ATTEMPTS_BEFORE_LOCKING; attemptIdx++) {
      hi = initializedHi();
      lo = this.lo.incrementAndGet();
      if (lo >= loUpperBoundOpen ||//lo is too big, we need to reset lo and advance hi
        attemptIdx == MAX_ATTEMPTS_BEFORE_LOCKING) {//we were unlucky for too many attempts, it's time to use locking to avoid a live-lock
        synchronized (mutex) {
          lo = this.lo.incrementAndGet();
          if (lo >= loUpperBoundOpen) {//re-check that we still need to reset lo and advance hi
            hi = nextHi();
            this.hi = hi;
            lo = 0;
            this.lo.set(lo);
          } else {//we still need to read hi inside this synchronized block to make sure that hi+lo read is atomic
            hi = initializedHi();
          }
          break;//hi+lo read was atomic because it was made inside this synchronized block
        }
      } else {//lo is fine, check that hi is still the same
        long hi2 = initializedHi();
        if (hi2 == hi) {
          break;//hi is the same, hence read hi+lo was atomic and we can break the loop
        }// else continue this while loop because hi was changed while we were reading lo, so we can't guarantee that the hi+lo read is atomic
      }
    }
    assert EXCLUDE_ASSERTIONS_FROM_BYTECODE || hi != UNINITIALIZED;
    assert EXCLUDE_ASSERTIONS_FROM_BYTECODE || lo != UNINITIALIZED;
    assert EXCLUDE_ASSERTIONS_FROM_BYTECODE || lo < loUpperBoundOpen;
    return calculateId(hi, lo, loUpperBoundOpen);
  }

  /**
   * Initializes <i>hi</i> by assigning it to the result of {@link #nextHi()}.
   *
   * @return An initialized <i>hi</i>.
   */
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
