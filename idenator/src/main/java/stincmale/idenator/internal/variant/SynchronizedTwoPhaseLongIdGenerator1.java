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

import stincmale.idenator.AbstractTwoPhaseLongIdGenerator;
import stincmale.idenator.LongIdGenerator;
import stincmale.idenator.doc.ThreadSafe;

@ThreadSafe
public final class SynchronizedTwoPhaseLongIdGenerator1 extends AbstractTwoPhaseLongIdGenerator {
  private final Object mutex;
  private long lo;
  private long hi;

  public SynchronizedTwoPhaseLongIdGenerator1(final LongIdGenerator hiGenerator, final long loUpperBoundOpen, final boolean pooled) {
    super(hiGenerator, loUpperBoundOpen, pooled);
    mutex = new Object();
    lo = -1;
    hi = UNINITIALIZED;
  }

  @Override
  public final long next() {
    synchronized (mutex) {
      final long loUpperBoundOpen = getLoUpperBoundOpen();
      final long hi;
      long lo = ++this.lo;//increment then read
      if (lo >= loUpperBoundOpen) {//lo is too big, we need to reset lo and advance hi
        lo = 0;
        this.lo = lo;
        hi = nextHi();
        this.hi = hi;
      } else {//lo is fine
        hi = initializedHi();
      }
      return calculateId(hi, lo);
    }
  }

  private final long initializedHi() {
    long hi = this.hi;
    if (hi == UNINITIALIZED) {
      hi = nextHi();
      this.hi = hi;
    }
    return hi;
  }
}
