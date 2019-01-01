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

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import stincmale.idenator.LongIdGenerator;
import stincmale.idenator.internal.EphemeralStrictlyIncreasingHiGenerator;
import stincmale.idenator.internal.NoopDelayer;
import stincmale.idenator.internal.variant.OptimisticTwoPhaseLongIdGenerator1;
import stincmale.idenator.internal.variant.OptimisticTwoPhaseLongIdGenerator2;
import stincmale.idenator.internal.variant.OptimisticTwoPhaseLongIdGenerator3;
import stincmale.idenator.internal.variant.StampedTwoPhaseLongIdGenerator1;
import stincmale.idenator.internal.variant.StampedTwoPhaseLongIdGenerator2;
import stincmale.idenator.internal.variant.SynchronizedTwoPhaseLongIdGenerator1;
import stincmale.idenator.internal.variant.SynchronizedTwoPhaseLongIdGenerator2;
import stincmale.idenator.performance.util.GaussianBlackHoleCpuConsumer;
import stincmale.idenator.performance.util.JmhOptions;

@TestInstance(Lifecycle.PER_CLASS)
public class LongIdGeneratorPerformanceTest {
  public LongIdGeneratorPerformanceTest() {
  }

  private static final void runThroughputBenchmarks(final int numberOfThreads) throws RunnerException {
    new Runner(
        JmhOptions.includingClass(LongIdGeneratorPerformanceTest.class)
            .mode(Mode.Throughput)
            .timeUnit(TimeUnit.MICROSECONDS)
            .threads(numberOfThreads)
            .build())
        .run();
  }

  @Test
  public final void throughputThreads4() throws RunnerException {
    runThroughputBenchmarks(4);
  }

  @Test
  public final void throughputThreads32() throws RunnerException {
    runThroughputBenchmarks(32);
  }

  @Benchmark
  public final long next(final BenchmarkState state) {
    return state.longIdGenerator.instance.next();
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    @Param
    //@Param(value = {"concurrentNoLo", "concurrentSmallLo", "concurrentBigLo", "concurrentSmallLoDelay", "concurrentBigLoDelay"})
    private TestableLongIdGenerator longIdGenerator;

    public BenchmarkState() {
    }
  }

  private static final long smallLo = 10_000;
  private static final long bigLo = 1000_000;

  public enum TestableLongIdGenerator {
    synchronized1SmallLo(SynchronizedTwoPhaseLongIdGenerator1::new, smallLo, false),
    synchronized1BigLo(SynchronizedTwoPhaseLongIdGenerator1::new, bigLo, false),
    synchronized1SmallLoDelay(SynchronizedTwoPhaseLongIdGenerator1::new, smallLo, true),
    synchronized1BigLoDelay(SynchronizedTwoPhaseLongIdGenerator1::new, bigLo, true),

    synchronized2SmallLo(SynchronizedTwoPhaseLongIdGenerator2::new, smallLo, false),
    synchronized2BigLo(SynchronizedTwoPhaseLongIdGenerator2::new, bigLo, false),
    synchronized2SmallLoDelay(SynchronizedTwoPhaseLongIdGenerator2::new, smallLo, true),
    synchronized2BigLoDelay(SynchronizedTwoPhaseLongIdGenerator2::new, bigLo, true),

    stamped1SmallLo(StampedTwoPhaseLongIdGenerator1::new, smallLo, false),
    stamped1BigLo(StampedTwoPhaseLongIdGenerator1::new, bigLo, false),
    stamped1SmallLoDelay(StampedTwoPhaseLongIdGenerator1::new, smallLo, true),
    stamped1BigLoDelay(StampedTwoPhaseLongIdGenerator1::new, bigLo, true),

    stamped2SmallLo(StampedTwoPhaseLongIdGenerator2::new, smallLo, false),
    stamped2BigLo(StampedTwoPhaseLongIdGenerator2::new, bigLo, false),
    stamped2SmallLoDelay(StampedTwoPhaseLongIdGenerator2::new, smallLo, true),
    stamped2BigLoDelay(StampedTwoPhaseLongIdGenerator2::new, bigLo, true),

    optimistic1SmallLo(OptimisticTwoPhaseLongIdGenerator1::new, smallLo, false),
    optimistic1BigLo(OptimisticTwoPhaseLongIdGenerator1::new, bigLo, false),
    optimistic1SmallLoDelay(OptimisticTwoPhaseLongIdGenerator1::new, smallLo, true),
    optimistic1BigLoDelay(OptimisticTwoPhaseLongIdGenerator1::new, bigLo, true),

    optimistic2SmallLo(OptimisticTwoPhaseLongIdGenerator2::new, smallLo, false),
    optimistic2BigLo(OptimisticTwoPhaseLongIdGenerator2::new, bigLo, false),
    optimistic2SmallLoDelay(OptimisticTwoPhaseLongIdGenerator2::new, smallLo, true),
    optimistic2BigLoDelay(OptimisticTwoPhaseLongIdGenerator2::new, bigLo, true),

    optimistic3SmallLo(OptimisticTwoPhaseLongIdGenerator3::new, smallLo, false),
    optimistic3BigLo(OptimisticTwoPhaseLongIdGenerator3::new, bigLo, false),
    optimistic3SmallLoDelay(OptimisticTwoPhaseLongIdGenerator3::new, smallLo, true),
    optimistic3BigLoDelay(OptimisticTwoPhaseLongIdGenerator3::new, bigLo, true);

    //    concurrentSmallLo(ConcurrentTwoPhaseLongIdGenerator::new, smallLo, false),
    //    concurrentBigLo(ConcurrentTwoPhaseLongIdGenerator::new, bigLo, false),
    //    concurrentSmallLoDelay(ConcurrentTwoPhaseLongIdGenerator::new, smallLo, true),
    //    concurrentBigLoDelay(ConcurrentTwoPhaseLongIdGenerator::new, bigLo, true);

    private final LongIdGenerator instance;

    TestableLongIdGenerator(final LongIdGeneratorCreator creator, final long loUpperBoundOpen, final boolean delay) {
      instance = createIdGen(creator, loUpperBoundOpen, delay);
    }

    private static final LongIdGenerator createIdGen(final LongIdGeneratorCreator creator, final long loUpperBoundOpen, final boolean delay) {
      return creator.create(
          new EphemeralStrictlyIncreasingHiGenerator(0, loUpperBoundOpen - 1,
              delay ? new GaussianBlackHoleCpuConsumer(5_300_000, 0) : NoopDelayer.instance()),
          loUpperBoundOpen, true);
    }

    private interface LongIdGeneratorCreator {
      LongIdGenerator create(LongIdGenerator hiGenerator, long loUpperBoundOpen, boolean pooled);
    }
  }
}
