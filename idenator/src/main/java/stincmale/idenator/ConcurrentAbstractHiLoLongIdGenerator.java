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
import static stincmale.idenator.internal.util.Preconditions.checkArgument;


@ThreadSafe
public abstract class ConcurrentAbstractHiLoLongIdGenerator implements LongIdGenerator {
  /**
   * This value is used internally to designate an uninitialized <i>hi</i> value.
   */
  protected static final long UNINITIALIZED = Long.MIN_VALUE;
  private static final int MAX_ATTEMPTS_BEFORE_LOCKING = 5;

  private volatile long hi;
  private final AtomicLong lo;
  private final long loUpperBoundOpen;//VAKOTODO move to a different class; this calue can be safely increased, but can't be easily reduced
  private final Object mutex;

  /**
   * @param loUpperBoundOpen This parameter specifies how many identifiers we can {@linkplain #generate() generate}
   * after calling {@link #nextHi()} without calling {@link #nextHi()} again.
   * <i>lo</i> ∈ [0; {@code loUpperBoundOpen}).
   */
  protected ConcurrentAbstractHiLoLongIdGenerator(final long loUpperBoundOpen) {
    checkArgument(loUpperBoundOpen > 0, "loUpperBoundOpen", "Must be positive");
    hi = UNINITIALIZED;
    lo = new AtomicLong(-1);
    this.loUpperBoundOpen = loUpperBoundOpen;
    mutex = new Object();
  }

  @Override
  public final long generate() {
    long hi = UNINITIALIZED;
    long lo = UNINITIALIZED;
    for (int attemptIdx = 0; attemptIdx <= MAX_ATTEMPTS_BEFORE_LOCKING; attemptIdx++) {
      hi = initializedHi();
      lo = this.lo.incrementAndGet();
      if (lo >= loUpperBoundOpen ||//lo is too big, we need to reset it and advance hi
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
          break;//hi is the same, hence read hi+lo was atomic
        }// else continue this while loop because hi was changed while we were reading lo, so we can't guarantee that the hi+lo read is atomic
      }
    }
    assert EXCLUDE_ASSERTIONS_FROM_BYTECODE || hi != UNINITIALIZED;
    assert EXCLUDE_ASSERTIONS_FROM_BYTECODE || lo != UNINITIALIZED;
    assert EXCLUDE_ASSERTIONS_FROM_BYTECODE || lo < loUpperBoundOpen;
    return calculateId(hi, lo, loUpperBoundOpen);
  }

  protected final long getLoUpperBoundOpen() {
    return loUpperBoundOpen;
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

  private static final long calculateId(final long hi, final long lo, final long loUpperBoundOpen) {
    return hi * loUpperBoundOpen + lo;
  }

  /**
   * Generates the next <i>hi</i> value and returns it.
   * In practical cases the next value from a database sequence may be returned.
   * <i>hi</i> must not be equal to {@link #UNINITIALIZED} because this value is reserved for internal purposes.
   *
   * @return The next <i>hi</i> value.
   */
  protected abstract long nextHi();

  @Override
  public String toString() {
    return getClass().getSimpleName() +
      "{loUpperBoundOpen=" + loUpperBoundOpen +
      '}';
  }
}
