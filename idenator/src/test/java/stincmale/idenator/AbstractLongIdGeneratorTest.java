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

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import stincmale.idenator.auxiliary.EphemeralStrictlyIncreasingHiGenerator;
import stincmale.idenator.auxiliary.NoopSleeper;
import static stincmale.idenator.internal.util.Preconditions.checkArgument;
import static stincmale.idenator.internal.util.Preconditions.checkNotNull;

public abstract class AbstractLongIdGeneratorTest {
  private final Collection<LongIdGeneratorCreatorAndParams> idGenCreators;

  protected AbstractLongIdGeneratorTest(final LongIdGeneratorCreatorAndParams... longIdGeneratorCreators) {
    checkNotNull(longIdGeneratorCreators, "longIdGeneratorCreators");
    checkArgument(longIdGeneratorCreators.length > 0, "longIdGeneratorCreators", "Must not be empty");
    this.idGenCreators = List.of(longIdGeneratorCreators);
  }

  protected final Collection<LongIdGeneratorCreatorAndParams> getLongIdGeneratorCreators() {
    return idGenCreators;
  }

  protected interface LongIdGeneratorCreator {
    AbstractTwoPhaseLongIdGenerator create(LongIdGenerator hiGenerator, long loUpperBoundOpen, boolean pooled);
  }

  protected static final class LongIdGeneratorCreatorAndParams implements Supplier<AbstractTwoPhaseLongIdGenerator> {
    private final LongIdGeneratorCreator creator;
    private final LongIdGenerator hiGenerator;
    private final long startHi;
    private final long loUpperBoundOpen;
    private final boolean pooled;

    public LongIdGeneratorCreatorAndParams(
      final LongIdGeneratorCreator creator,
      final long startHi,
      final long loUpperBoundOpen,
      final boolean pooled) {
      checkArgument(loUpperBoundOpen > 0, "loUpperBoundOpen", "Must be positive");
      this.creator = checkNotNull(creator, "creator");
      this.hiGenerator = new EphemeralStrictlyIncreasingHiGenerator(startHi, pooled ? loUpperBoundOpen - 1 : 0, NoopSleeper.instance());
      this.startHi = startHi;
      this.loUpperBoundOpen = loUpperBoundOpen;
      this.pooled = pooled;
    }

    @Override
    public AbstractTwoPhaseLongIdGenerator get() {
      return creator.create(hiGenerator, loUpperBoundOpen, pooled);
    }

    public final LongIdGeneratorCreator getCreator() {
      return creator;
    }

    public final LongIdGenerator getHiGenerator() {
      return hiGenerator;
    }

    public final long getStartHi() {
      return startHi;
    }

    public final long getLoUpperBoundOpen() {
      return loUpperBoundOpen;
    }

    public final boolean isPooled() {
      return pooled;
    }
  }
}