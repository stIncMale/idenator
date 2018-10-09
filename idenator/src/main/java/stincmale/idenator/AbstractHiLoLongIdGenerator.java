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
import static stincmale.idenator.internal.util.Preconditions.checkNotNull;

/**
 * An abstract <a href="https://vladmihalcea.com/the-hilo-algorithm/">Hi/Lo</a> {@link LongIdGenerator}.
 * {@link AbstractHiLoLongIdGenerator} is:
 * <ul>
 * <li>persistent if the supplied {@link HiValueGenerator} is, and non-persistent otherwise</li>
 * <li>strictly increasing if the supplied {@link HiValueGenerator} is, and non-monotonic otherwise</li>
 * <li>non-consecutive if the supplied {@link HiValueGenerator} is, otherwise this property is defined by implementations</li>
 * </ul>
 */
public abstract class AbstractHiLoLongIdGenerator implements LongIdGenerator {
  /**
   * This value is used to designate an uninitialized (similar to null for objects) <i>hi</i> value.
   */
  public static final long UNINITIALIZED = Long.MIN_VALUE;

  private final HiValueGenerator hiValueGenerator;
  private final long loUpperBoundOpen;

  /**
   * @param hiValueGenerator A <i>hi</i> value generator.
   * @param loUpperBoundOpen This parameter specifies how many identifiers we can {@linkplain #generate() generate}
   * after obtaining a new <i>hi</i> value without retrieving the next <i>hi</i> value again.
   * <p>
   * Generally speaking, {@code loUpperBoundOpen} can be safely increased for the same source of <i>hi</i> values.
   * But decreasing it requires caution and in case of using a database sequence for generating <i>hi</i> values (a persistent generator)
   * requires advancing the sequence value far enough to maintain the guarantee of identifier uniqueness.
   */
  public AbstractHiLoLongIdGenerator(final HiValueGenerator hiValueGenerator, final long loUpperBoundOpen) {
    checkArgument(loUpperBoundOpen > 0, "loUpperBoundOpen", "Must be positive");
    this.hiValueGenerator = checkNotNull(hiValueGenerator, "hiValueGenerator");
    this.loUpperBoundOpen = loUpperBoundOpen;
  }

  /**
   * Calculates an identifier.
   *
   * @param hi A <i>hi</i> value, which in practical cases may represent a value from a database sequence.
   * @param lo A <i>lo</i> value which is a looped in-memory counter.
   * <i>lo</i> ∈ [0; {@code loUpperBoundOpen}).
   * @param loUpperBoundOpen See {@link #AbstractHiLoLongIdGenerator(HiValueGenerator, long)}.
   * @return The identifier uniquely defined by the supplied {@code hi}, {@code lo} and {@code loUpperBoundOpen}.
   */
  protected static final long calculateId(final long hi, final long lo, final long loUpperBoundOpen) {
    checkArgument(hi != UNINITIALIZED, "hi", () -> String.format("Must not be equal to %s", UNINITIALIZED));
    checkArgument(lo >= 0, "lo", "Must not be negative");
    checkArgument(lo < loUpperBoundOpen, "lo", () -> String.format("Must be less than %s=%s", "loUpperBoundOpen", loUpperBoundOpen));
    return hi * loUpperBoundOpen + lo;
  }

  /**
   * @return {@link HiValueGenerator#nextHi()} by using the {@link HiValueGenerator} supplied to the constructor.
   */
  protected final long nextHi() {
    return hiValueGenerator.nextHi();
  }

  /**
   * This method can be called without additional memory synchronization.
   *
   * @return {@code loUpperBoundOpen} supplied to the constructor.
   */
  protected final long getLoUpperBoundOpen() {
    return loUpperBoundOpen;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() +
      "{hiValueGenerator=" + hiValueGenerator +
      ", loUpperBoundOpen=" + loUpperBoundOpen +
      '}';
  }
}
