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
package stincmale.idenator.fake;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import stincmale.idenator.ConcurrentAbstractHiLoLongIdGenerator;
import static stincmale.idenator.internal.util.Preconditions.checkArgument;
import static stincmale.idenator.internal.util.Preconditions.checkNotNull;

public final class ConcurrentInMemoryHiLoLongIdGenerator extends ConcurrentAbstractHiLoLongIdGenerator {
  private final AtomicLong hi;
  private final long maxNextDelayMillis;

  public ConcurrentInMemoryHiLoLongIdGenerator(final long startHi, final long loUpperBoundOpen, final Duration maxNextHiDelay) {
    super(loUpperBoundOpen);
    checkNotNull(maxNextHiDelay, "maxNextHiDelay");
    checkArgument(!maxNextHiDelay.isNegative(), "maxNextHiDelay", "Must not be negative");
    hi = new AtomicLong(startHi);
    maxNextDelayMillis = maxNextHiDelay.toMillis();
  }

  @Override
  protected final long nextHi() {
    if (maxNextDelayMillis > 0) {
      try {
        Thread.sleep(ThreadLocalRandom.current().nextLong(maxNextDelayMillis + 1));
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    final long result = hi.getAndIncrement();
    return result == UNINITIALIZED ? hi.getAndIncrement() : result;
  }
}
