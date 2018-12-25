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

import static java.time.Duration.ofMillis;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import stincmale.idenator.LongIdGenerator;
import stincmale.idenator.internal.EphemeralStrictlyIncreasingHiGenerator;
import stincmale.idenator.internal.GaussianSleeper;
import stincmale.idenator.internal.NoopSleeper;
import stincmale.idenator.internal.variant.OptimisticTwoPhaseLongIdGenerator1;
import stincmale.idenator.internal.variant.OptimisticTwoPhaseLongIdGenerator2;
import stincmale.idenator.internal.variant.OptimisticTwoPhaseLongIdGenerator3;
import stincmale.idenator.internal.variant.StampedTwoPhaseLongIdGenerator1;
import stincmale.idenator.internal.variant.StampedTwoPhaseLongIdGenerator2;
import stincmale.idenator.internal.variant.SynchronizedTwoPhaseLongIdGenerator1;
import stincmale.idenator.internal.variant.SynchronizedTwoPhaseLongIdGenerator2;
import stincmale.idenator.performance.util.JmhOptions;
import stincmale.idenator.performance.util.TestTag;

@Tag(TestTag.PERFORMANCE)
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
  public final void throughputThreads1() throws RunnerException {
    runThroughputBenchmarks(1);
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
  public final long synchronized1NoLo(final BenchmarkState state) {
    return state.synchronized1NoLo.next();
  }

  @Benchmark
  public final long synchronized1SmallLo(final BenchmarkState state) {
    return state.synchronized1SmallLo.next();
  }

  @Benchmark
  public final long synchronized1BigLo(final BenchmarkState state) {
    return state.synchronized1BigLo.next();
  }

  @Benchmark
  public final long synchronized2NoLo(final BenchmarkState state) {
    return state.synchronized2NoLo.next();
  }

  @Benchmark
  public final long synchronized2SmallLo(final BenchmarkState state) {
    return state.synchronized2SmallLo.next();
  }

  @Benchmark
  public final long synchronized2BigLo(final BenchmarkState state) {
    return state.synchronized2BigLo.next();
  }

  @Benchmark
  public final long synchronized2SmallLoSleep(final BenchmarkState state) {
    return state.synchronized2SmallLoSleep.next();
  }

  @Benchmark
  public final long synchronized2BigLoSleep(final BenchmarkState state) {
    return state.synchronized2BigLoSleep.next();
  }

  @Benchmark
  public final long stamped1NoLo(final BenchmarkState state) {
    return state.stamped1NoLo.next();
  }

  @Benchmark
  public final long stamped1SmallLo(final BenchmarkState state) {
    return state.stamped1SmallLo.next();
  }

  @Benchmark
  public final long stamped1BigLo(final BenchmarkState state) {
    return state.stamped1BigLo.next();
  }

  @Benchmark
  public final long stamped2NoLo(final BenchmarkState state) {
    return state.stamped2NoLo.next();
  }

  @Benchmark
  public final long stamped2SmallLo(final BenchmarkState state) {
    return state.stamped2SmallLo.next();
  }

  @Benchmark
  public final long stamped2BigLo(final BenchmarkState state) {
    return state.stamped2BigLo.next();
  }

  @Benchmark
  public final long stamped2SmallLoSleep(final BenchmarkState state) {
    return state.stamped2SmallLoSleep.next();
  }

  @Benchmark
  public final long stamped2BigLoSleep(final BenchmarkState state) {
    return state.stamped2BigLoSleep.next();
  }

  @Benchmark
  public final long optimistic1NoLo(final BenchmarkState state) {
    return state.optimistic1NoLo.next();
  }

  @Benchmark
  public final long optimistic1SmallLo(final BenchmarkState state) {
    return state.optimistic1SmallLo.next();
  }

  @Benchmark
  public final long optimistic1BigLo(final BenchmarkState state) {
    return state.optimistic1BigLo.next();
  }

  @Benchmark
  public final long optimistic2NoLo(final BenchmarkState state) {
    return state.optimistic2NoLo.next();
  }

  @Benchmark
  public final long optimistic2SmallLo(final BenchmarkState state) {
    return state.optimistic2SmallLo.next();
  }

  @Benchmark
  public final long optimistic2BigLo(final BenchmarkState state) {
    return state.optimistic2BigLo.next();
  }

  @Benchmark
  public final long optimistic3NoLo(final BenchmarkState state) {
    return state.optimistic3NoLo.next();
  }

  @Benchmark
  public final long optimistic3SmallLo(final BenchmarkState state) {
    return state.optimistic3SmallLo.next();
  }

  @Benchmark
  public final long optimistic3BigLo(final BenchmarkState state) {
    return state.optimistic3BigLo.next();
  }

  @Benchmark
  public final long optimistic3SmallLoSleep(final BenchmarkState state) {
    return state.optimistic3SmallLoSleep.next();
  }

  @Benchmark
  public final long optimistic3BigLoSleep(final BenchmarkState state) {
    return state.optimistic3BigLoSleep.next();
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private LongIdGenerator synchronized1NoLo;
    private LongIdGenerator synchronized1SmallLo;
    private LongIdGenerator synchronized1BigLo;

    private LongIdGenerator synchronized2NoLo;
    private LongIdGenerator synchronized2SmallLo;
    private LongIdGenerator synchronized2BigLo;

    private LongIdGenerator synchronized2SmallLoSleep;
    private LongIdGenerator synchronized2BigLoSleep;

    private LongIdGenerator stamped1NoLo;
    private LongIdGenerator stamped1SmallLo;
    private LongIdGenerator stamped1BigLo;

    private LongIdGenerator stamped2NoLo;
    private LongIdGenerator stamped2SmallLo;
    private LongIdGenerator stamped2BigLo;

    private LongIdGenerator stamped2SmallLoSleep;
    private LongIdGenerator stamped2BigLoSleep;

    private LongIdGenerator optimistic1NoLo;
    private LongIdGenerator optimistic1SmallLo;
    private LongIdGenerator optimistic1BigLo;

    private LongIdGenerator optimistic2NoLo;
    private LongIdGenerator optimistic2SmallLo;
    private LongIdGenerator optimistic2BigLo;

    private LongIdGenerator optimistic3NoLo;
    private LongIdGenerator optimistic3SmallLo;
    private LongIdGenerator optimistic3BigLo;

    private LongIdGenerator optimistic3SmallLoSleep;
    private LongIdGenerator optimistic3BigLoSleep;

    public BenchmarkState() {
    }

    @Setup(Level.Trial)
    public final void setup() {
      final long noLo = 1;
      final long smallLo = 10_000;
      final long bigLo = 1000_000;
      synchronized1NoLo = createIdGen(SynchronizedTwoPhaseLongIdGenerator1::new, noLo, false);
      synchronized1SmallLo = createIdGen(SynchronizedTwoPhaseLongIdGenerator1::new, smallLo, false);
      synchronized1BigLo = createIdGen(SynchronizedTwoPhaseLongIdGenerator1::new, bigLo, false);

      synchronized2NoLo = createIdGen(SynchronizedTwoPhaseLongIdGenerator2::new, noLo, false);
      synchronized2SmallLo = createIdGen(SynchronizedTwoPhaseLongIdGenerator2::new, smallLo, false);
      synchronized2BigLo = createIdGen(SynchronizedTwoPhaseLongIdGenerator2::new, bigLo, false);

      synchronized2SmallLoSleep = createIdGen(SynchronizedTwoPhaseLongIdGenerator2::new, smallLo, true);
      synchronized2BigLoSleep = createIdGen(SynchronizedTwoPhaseLongIdGenerator2::new, bigLo, true);

      stamped1NoLo = createIdGen(StampedTwoPhaseLongIdGenerator1::new, noLo, false);
      stamped1SmallLo = createIdGen(StampedTwoPhaseLongIdGenerator1::new, smallLo, false);
      stamped1BigLo = createIdGen(StampedTwoPhaseLongIdGenerator1::new, bigLo, false);

      stamped2NoLo = createIdGen(StampedTwoPhaseLongIdGenerator2::new, noLo, false);
      stamped2SmallLo = createIdGen(StampedTwoPhaseLongIdGenerator2::new, smallLo, false);
      stamped2BigLo = createIdGen(StampedTwoPhaseLongIdGenerator2::new, bigLo, false);

      stamped2SmallLoSleep = createIdGen(StampedTwoPhaseLongIdGenerator2::new, smallLo, true);
      stamped2BigLoSleep = createIdGen(StampedTwoPhaseLongIdGenerator2::new, bigLo, true);

      optimistic1NoLo = createIdGen(OptimisticTwoPhaseLongIdGenerator1::new, noLo, false);
      optimistic1SmallLo = createIdGen(OptimisticTwoPhaseLongIdGenerator1::new, smallLo, false);
      optimistic1BigLo = createIdGen(OptimisticTwoPhaseLongIdGenerator1::new, bigLo, false);

      optimistic2NoLo = createIdGen(OptimisticTwoPhaseLongIdGenerator2::new, noLo, false);
      optimistic2SmallLo = createIdGen(OptimisticTwoPhaseLongIdGenerator2::new, smallLo, false);
      optimistic2BigLo = createIdGen(OptimisticTwoPhaseLongIdGenerator2::new, bigLo, false);

      optimistic3NoLo = createIdGen(OptimisticTwoPhaseLongIdGenerator3::new, noLo, false);
      optimistic3SmallLo = createIdGen(OptimisticTwoPhaseLongIdGenerator3::new, smallLo, false);
      optimistic3BigLo = createIdGen(OptimisticTwoPhaseLongIdGenerator3::new, bigLo, false);

      optimistic3SmallLoSleep = createIdGen(OptimisticTwoPhaseLongIdGenerator3::new, smallLo, true);
      optimistic3BigLoSleep = createIdGen(OptimisticTwoPhaseLongIdGenerator3::new, bigLo, true);
    }
  }

  private static final LongIdGenerator createIdGen(final LongIdGeneratorCreator creator, final long loUpperBoundOpen, boolean sleep) {
    return creator.create(
        new EphemeralStrictlyIncreasingHiGenerator(0, loUpperBoundOpen - 1,
            sleep ? new GaussianSleeper(ofMillis(8), ofMillis(2)) : NoopSleeper.instance()),
        loUpperBoundOpen, true);
  }

  private interface LongIdGeneratorCreator {
    LongIdGenerator create(LongIdGenerator hiGenerator, long loUpperBoundOpen, boolean pooled);
  }
}
