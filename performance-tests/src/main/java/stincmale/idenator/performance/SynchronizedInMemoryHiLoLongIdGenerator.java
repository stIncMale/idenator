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
package stincmale.idenator.performance;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import stincmale.idenator.LongIdGenerator;
import stincmale.idenator.doc.ThreadSafe;
import static stincmale.idenator.internal.util.Preconditions.checkArgument;
import static stincmale.idenator.internal.util.Preconditions.checkNotNull;

@ThreadSafe
final class SynchronizedInMemoryHiLoLongIdGenerator implements LongIdGenerator {
  private long hi;
  private long lo;
  private final long loUpperBoundOpen;
  private final Object mutex;
  private final long maxNextDelayMillis;

  SynchronizedInMemoryHiLoLongIdGenerator(final long startHi, final long loUpperBoundOpen, final Duration maxNextHiDelay) {
    checkArgument(loUpperBoundOpen > 0, "loUpperBoundOpen", "Must be positive");
    checkNotNull(maxNextHiDelay, "maxNextHiDelay");
    checkArgument(!maxNextHiDelay.isNegative(), "maxNextHiDelay", "Must not be negative");
    hi = startHi;
    lo = -1;
    this.loUpperBoundOpen = loUpperBoundOpen;
    mutex = new Object();
    maxNextDelayMillis = maxNextHiDelay.toMillis();
  }

  @Override
  public final long generate() {
    synchronized (mutex) {
      lo++;
      if (lo >= loUpperBoundOpen) {
        hi = nextHi();
        lo = 0;
      }
      return calculateId(hi, lo, loUpperBoundOpen);
    }
  }

  protected final long getLoUpperBoundOpen() {
    return loUpperBoundOpen;
  }

  private final long nextHi() {
    if (maxNextDelayMillis > 0) {
      try {
        Thread.sleep(ThreadLocalRandom.current().nextLong(maxNextDelayMillis + 1));
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    hi++;
    return hi;
  }

  private static final long calculateId(final long hi, final long lo, final long loUpperBoundOpen) {
    return hi * loUpperBoundOpen + lo;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() +
      "{loUpperBoundOpen=" + loUpperBoundOpen +
      '}';
  }
}
