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

package stincmale.idenator.evolution;

import stincmale.idenator.AbstractHiLoLongIdGenerator;
import stincmale.idenator.HiValueGenerator;
import stincmale.idenator.doc.ThreadSafe;

/**
 * A synchronized implementation of {@link AbstractHiLoLongIdGenerator}.
 * {@link SynchronizedHiLoLongIdGenerator2} is consecutive if the supplied {@link HiValueGenerator} is consecutive.
 */
@ThreadSafe
public final class SynchronizedHiLoLongIdGenerator2 extends AbstractHiLoLongIdGenerator {
  private final Object mutex;
  private long lo;
  private long hi;

  public SynchronizedHiLoLongIdGenerator2(final HiValueGenerator hiValueGenerator, final long loUpperBoundOpen) {
    super(hiValueGenerator, loUpperBoundOpen);
    mutex = new Object();
    lo = -1;
    hi = UNINITIALIZED;
  }

  @Override
  public final long generate() {
    final long loUpperBoundOpen = getLoUpperBoundOpen();
    final long hi;
    long lo;
    synchronized (mutex) {
      lo = ++this.lo;//increment then assign
      if (lo >= loUpperBoundOpen) {//lo is too big, we need to reset lo and advance hi
        lo = 0;
        this.lo = lo;
        hi = nextHi();
        this.hi = hi;
      } else {//lo is fine
        hi = initializedHi();
      }
    }
    return calculateId(hi, lo, loUpperBoundOpen);
  }

  private final long initializedHi() {
    if (hi == UNINITIALIZED) {
      hi = nextHi();
    }
    return hi;
  }
}
