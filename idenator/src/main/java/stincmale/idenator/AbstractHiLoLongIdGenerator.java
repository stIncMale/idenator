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

import static stincmale.idenator.internal.util.Preconditions.checkArgument;

/**
 * An abstract <a href="https://vladmihalcea.com/the-hilo-algorithm/">Hi/Lo</a> {@link LongIdGenerator}.
 */
public abstract class AbstractHiLoLongIdGenerator implements LongIdGenerator {
  private final long loUpperBoundOpen;

  /**
   * @param loUpperBoundOpen This parameter specifies how many identifiers we can {@linkplain #generate() generate}
   * after obtaining a new <i>hi</i> value without retrieving the next <i>hi</i> value again.
   * <p>
   * Generally speaking, {@code loUpperBoundOpen} can be safely increased for the same source of <i>hi</i> values,
   * but decreasing it requires caution and in case of using a database sequence requires advancing the sequence value far enough
   * to maintain the guarantee of identifier uniqueness.
   */
  protected AbstractHiLoLongIdGenerator(final long loUpperBoundOpen) {
    checkArgument(loUpperBoundOpen > 0, "loUpperBoundOpen", "Must be positive");
    this.loUpperBoundOpen = loUpperBoundOpen;
  }

  /**
   * Calculates a new identifier.
   *
   * @param hi A <i>hi</i> value, which in practical cases may represent a value from a database sequence.
   * @param lo A <i>lo</i> value which is a looped in-memory counter,
   * <i>lo</i> ∈ [0; {@linkplain #AbstractHiLoLongIdGenerator(long) loUpperBoundOpen}).
   * @return The identifier corresponding to the supplied {@code hi} and {@code lo},
   * which uniquely define the identifier for a given {@linkplain #AbstractHiLoLongIdGenerator(long) loUpperBoundOpen}.
   */
  protected final long calculateId(final long hi, final long lo) {
    return hi * loUpperBoundOpen + lo;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() +
      "{loUpperBoundOpen=" + loUpperBoundOpen +
      '}';
  }
}
