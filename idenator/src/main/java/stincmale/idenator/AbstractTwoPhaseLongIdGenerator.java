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

import stincmale.idenator.doc.NotThreadSafe;
import stincmale.idenator.doc.ThreadSafe;
import static stincmale.idenator.internal.util.Preconditions.checkArgument;
import static stincmale.idenator.internal.util.Preconditions.checkNotNull;
import static stincmale.idenator.internal.util.Utils.format;

/**
 * This ID generator can be represented as an optimization around another ID generator (specifically {@linkplain #getHiGenerator() hiGenerator}),
 * which requires noticeable amount of resources (usually time) to produce a new identifier.
 * For example, {@linkplain #getHiGenerator() hiGenerator} may need to access a persistent storage to generate a new identifier.
 * <p>
 * This ID generator maintains two values: {@code hi} and {@code lo},
 * and uses them for {@linkplain #calculateId(long, long) calculation} of a new identifier.
 * {@code Hi} values are identifiers themselves, usually take significant time to be generated and virtually unbounded.
 * {@code Lo} values are usually cheap to produce, bounded by {@linkplain #getLoUpperBoundOpen() loUpperBoundOpen},
 * must be unique only within the same {@code hi} value.
 * <p>
 * This ID generator can work in two modes: Hi/Lo and pooled.
 * <p>
 * <i>Hi/Lo mode</i><br>
 * This mode is suitable to wrap any {@linkplain #getHiGenerator() hiGenerator},
 * but the downside is that {@link AbstractTwoPhaseLongIdGenerator} working in this mode is not compatible (see {@link stincmale.idenator})
 * with the wrapped {@linkplain #getHiGenerator() hiGenerator}.
 * See {@link #calculateId(long, long)} for the formula which is used to calculate identifiers in this mode.
 * <p>
 * This mode is analogous to
 * <a href="http://docs.jboss.org/hibernate/orm/5.3/javadocs/org/hibernate/id/enhanced/HiLoOptimizer.html">HiLoOptimizer</a> in
 * <a href="http://hibernate.org/orm/">Hibernate ORM</a>, and is also described <a href="https://vladmihalcea.com/the-hilo-algorithm/">here</a>.
 * <p>
 * <i>Pooled mode</i> (recommended)<br>
 * This mode is preferable to Hi/Lo, because {@link AbstractTwoPhaseLongIdGenerator} working in this mode
 * is compatible with the wrapped {@linkplain #getHiGenerator() hiGenerator}.
 * However, this mode requires {@linkplain #getHiGenerator() hiGenerator} to be sparse (see {@link stincmale.idenator}).
 * <p>
 * This mode is analogous to
 * <a href="http://docs.jboss.org/hibernate/orm/5.3/javadocs/org/hibernate/id/enhanced/PooledLoOptimizer.html">PooledLoOptimizer</a> in
 * <a href="http://hibernate.org/orm/">Hibernate ORM</a>,
 * and is also described <a href="https://vladmihalcea.com/hibernate-hidden-gem-the-pooled-lo-optimizer/">here</a>.
 * <p>
 * Flavours (see {@link stincmale.idenator}):
 * <ul>
 * <li>
 * This ID generator is persistent if {@linkplain #getHiGenerator() hiGenerator} is persistent
 * and the same {@linkplain #getLoUpperBoundOpen() getLoUpperBoundOpen} is used in different executions of the program.
 * </li>
 * <li>
 * This ID generator is strictly increasing if {@linkplain #getHiGenerator() hiGenerator} is strictly increasing,
 * otherwise this ID generator is nonmonotonic.
 * </li>
 * </ul>
 * <p>
 * This class is {@linkplain ThreadSafe thread-safe}, but does not impose this restriction on its subclasses.
 */
@ThreadSafe
public abstract class AbstractTwoPhaseLongIdGenerator implements LongIdGenerator {
  /**
   * This value is used to designate an uninitialized (similar to null for objects) {@code hi} value.
   */
  public static final long UNINITIALIZED = Long.MIN_VALUE;

  private final LongIdGenerator hiGenerator;
  private final long loUpperBoundOpen;
  private final boolean pooled;

  /**
   * @param hiGenerator A {@code hi} value generator (the ID generator which we are optimizing).
   * {@code hiGenerator} is allowed to be {@linkplain NotThreadSafe not thread-safe}.
   * @param loUpperBoundOpen This parameter specifies how many identifiers we can {@linkplain #next() generate}
   * after obtaining a new {@code hi} value without retrieving another {@code hi} value again. Must be positive.
   * {@code lo} ∈ [0; {@code loUpperBoundOpen}).
   * <p>
   * If persistent behaviour (see {@link stincmale.idenator}) is required,
   * then this parameter should be the same for different executions of the program.
   * Using different values in different executions can sometimes be possible, but this is beyond of scope of this documentation.
   * <p>
   * If {@code pooled} is specified as true,
   * then this argument must have a value from the interval [0; {@code (sparseness + 1)}] (see {@link stincmale.idenator}).
   * The recommended value is {@code (sparseness + 1)} because it minimizes the frequency of accesses to {@code hiGenerator}.
   * @param pooled Defines whether this ID generator will work in Hi/Lo or in pooled (recommended) mode
   * (this affects the behaviour of {@link #calculateId(long, long)}).
   * <p>
   * If {@code hiGenerator} is sparse (see {@link stincmale.idenator}),
   * then specify true and specify {@code (sparseness + 1)} as {@code loUpperBoundOpen}.
   * Otherwise, or if you don't know anything about the behaviour of {@code hiGenerator}, specify false.
   */
  protected AbstractTwoPhaseLongIdGenerator(final LongIdGenerator hiGenerator, final long loUpperBoundOpen, final boolean pooled) {
    checkArgument(loUpperBoundOpen > 0, "loUpperBoundOpen", "Must be positive");
    this.hiGenerator = checkNotNull(hiGenerator, "hiGenerator");
    this.loUpperBoundOpen = loUpperBoundOpen;
    this.pooled = pooled;
  }

  /**
   * Calculates an identifier as {@code hi + lo} if this ID generator {@link #isPooled()}, otherwise as {@code hi * loUpperBoundOpen + lo}.
   * <p>
   * Pooled and not pooled (we will be calling it Hi/Lo) ID generators are simpler to explain
   * with the assumption that {@linkplain #getHiGenerator() hiGenerator} is strictly increasing (see {@link stincmale.idenator}).
   *
   * @param hi A {@code hi} value. Must not be equal to {@link AbstractTwoPhaseLongIdGenerator#UNINITIALIZED}.
   * @param lo A {@code lo} value. {@code lo} ∈ [0; {@linkplain #getLoUpperBoundOpen() loUpperBoundOpen}).
   * @return The identifier uniquely defined by the supplied {@code hi}, {@code lo} and {@code numberOfIdsPerHi} values.
   */
  protected final long calculateId(final long hi, final long lo) {
    checkArgument(hi != UNINITIALIZED, "hi", () -> format("Must not be equal to %s", UNINITIALIZED));
    checkArgument(lo >= 0, "lo", "Must not be negative");
    checkArgument(lo < loUpperBoundOpen, "lo", () -> format("Must be less than %s=%s", "loUpperBoundOpen", loUpperBoundOpen));
    return pooled
        ? hi + lo
        : hi * loUpperBoundOpen + lo;
  }

  /**
   * @return The {@code hi} value generator specified via {@link #AbstractTwoPhaseLongIdGenerator(LongIdGenerator, long, boolean)}.
   */
  protected final LongIdGenerator getHiGenerator() {
    return hiGenerator;
  }

  /**
   * @return The next {@code hi} value by using {@link #getHiGenerator() hiGenerator}.
   * Never returns {@link AbstractTwoPhaseLongIdGenerator#UNINITIALIZED}.
   */
  protected final long nextId() {
    final long id = hiGenerator.next();
    return id == UNINITIALIZED ? hiGenerator.next() : id;
  }

  /**
   * @return {@code loUpperBoundOpen} specified via {@link #AbstractTwoPhaseLongIdGenerator(LongIdGenerator, long, boolean)}.
   */
  protected final long getLoUpperBoundOpen() {
    return loUpperBoundOpen;
  }

  /**
   * @return Whether this generator works in pooled mode or in Hi/Lo mode.
   */
  private boolean isPooled() {
    return pooled;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() +
        "{hiGenerator=" + hiGenerator +
        ", loUpperBoundOpen=" + loUpperBoundOpen +
        ", pooled=" + pooled +
        '}';
  }
}
