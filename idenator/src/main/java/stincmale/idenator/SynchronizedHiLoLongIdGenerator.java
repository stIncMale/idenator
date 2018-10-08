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

import stincmale.idenator.doc.ThreadSafe;

@ThreadSafe
public final class SynchronizedHiLoLongIdGenerator extends AbstractHiLoLongIdGenerator {
  private final Object mutex;
  private long hi;
  private long lo;

  public SynchronizedHiLoLongIdGenerator(final HiValueGenerator hiValueGenerator, final long loUpperBoundOpen) {
    super(hiValueGenerator, loUpperBoundOpen);
    hi = UNINITIALIZED;
    lo = -1;
    mutex = new Object();
  }

  @Override
  public final long generate() {
    final long loUpperBoundOpen = getLoUpperBoundOpen();
    final long hi;
    final long lo;
    synchronized (mutex) {
      this.lo++;
      if (this.lo >= loUpperBoundOpen) {//lo is too big, we need to reset lo and advance hi
        this.hi = nextHi();
        this.lo = 0;
      } else {//lo is fine
        if (this.hi == UNINITIALIZED) {//initialize hi if needed
          this.hi = nextHi();
        }
      }
      hi = this.hi;
      lo = this.lo;
    }
    return calculateId(hi, lo, loUpperBoundOpen);
  }
}
