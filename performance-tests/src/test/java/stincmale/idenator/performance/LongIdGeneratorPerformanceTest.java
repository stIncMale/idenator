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
import java.util.function.Supplier;
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
import stincmale.idenator.ConcurrentHiLoLongIdGenerator;
import stincmale.idenator.InMemoryHiValueGenerator;
import stincmale.idenator.auxiliary.GaussianSleeper;
import stincmale.idenator.auxiliary.NoopSleeper;
import stincmale.idenator.evolution.OptimisticHiLoLongIdGenerator;
import stincmale.idenator.evolution.StampedLockHiLoLongIdGenerator;
import stincmale.idenator.evolution.SynchronizedHiLoLongIdGenerator1;
import stincmale.idenator.evolution.SynchronizedHiLoLongIdGenerator2;
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

  //  @Test
  //  public final void throughputThreads1() throws RunnerException {
  //    runThroughputBenchmarks(1);
  //  }

  @Test
  public final void throughputThreads4() throws RunnerException {
    runThroughputBenchmarks(4);
  }

  @Test
  public final void throughputThreads32() throws RunnerException {
    runThroughputBenchmarks(32);
  }

  //  @Benchmark
  //  public final long synchronized1NoSleepBigLo(final BenchmarkState state) {
  //    return state.synchronized1NoSleepBigLo.generate();
  //  }
  //
  //  @Benchmark
  //  public final long synchronized1NoSleepSmallLo(final BenchmarkState state) {
  //    return state.synchronized1NoSleepSmallLo.generate();
  //  }
  //
  //  @Benchmark
  //  public final long synchronized1SleepBigLo(final BenchmarkState state) {
  //    return state.synchronized1SleepBigLo.generate();
  //  }
  //
  //  @Benchmark
  //  public final long synchronized1SleepSmallLo(final BenchmarkState state) {
  //    return state.synchronized1SleepSmallLo.generate();
  //  }
  //
  //  @Benchmark
  //  public final long synchronized2NoSleepBigLo(final BenchmarkState state) {
  //    return state.synchronized2NoSleepBigLo.generate();
  //  }
  //
  //  @Benchmark
  //  public final long synchronized2NoSleepSmallLo(final BenchmarkState state) {
  //    return state.synchronized2NoSleepSmallLo.generate();
  //  }
  //
  //  @Benchmark
  //  public final long synchronized2SleepBigLo(final BenchmarkState state) {
  //    return state.synchronized2SleepBigLo.generate();
  //  }
  //
  //  @Benchmark
  //  public final long synchronized2SleepSmallLo(final BenchmarkState state) {
  //    return state.synchronized1SleepSmallLo.generate();
  //  }

  @Benchmark
  public final long stampedLockNoSleepBigLo(final BenchmarkState state) {
    return state.stampedLockNoSleepBigLo.generate();
  }

  @Benchmark
  public final long stampedLockNoSleepSmallLo(final BenchmarkState state) {
    return state.stampedLockNoSleepSmallLo.generate();
  }

  //  @Benchmark
  //  public final long stampedLockSleepBigLo(final BenchmarkState state) {
  //    return state.stampedLockSleepBigLo.generate();
  //  }
  //
  //  @Benchmark
  //  public final long stampedLockSleepSmallLo(final BenchmarkState state) {
  //    return state.stampedLockSleepSmallLo.generate();
  //  }

  @Benchmark
  public final long optimisticNoSleepBigLo(final BenchmarkState state) {
    return state.optimisticNoSleepBigLo.generate();
  }

  @Benchmark
  public final long optimisticNoSleepSmallLo(final BenchmarkState state) {
    return state.optimisticNoSleepSmallLo.generate();
  }

  //  @Benchmark
  //  public final long optimisticSleepBigLo(final BenchmarkState state) {
  //    return state.optimisticSleepBigLo.generate();
  //  }
  //
  //  @Benchmark
  //  public final long optimisticSleepSmallLo(final BenchmarkState state) {
  //    return state.stampedLock1SleepSmallLo.generate();
  //  }

  @Benchmark
  public final long concurrentNoSleepBigLo(final BenchmarkState state) {
    return state.concurrentNoSleepBigLo.generate();
  }

  @Benchmark
  public final long concurrentNoSleepSmallLo(final BenchmarkState state) {
    return state.concurrentNoSleepSmallLo.generate();
  }

  //  @Benchmark
  //  public final long concurrentSleepBigLo(final BenchmarkState state) {
  //    return state.concurrentSleepBigLo.generate();
  //  }
  //
  //  @Benchmark
  //  public final long concurrentSleepSmallLo(final BenchmarkState state) {
  //    return state.concurrentSleepSmallLo.generate();
  //  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private SynchronizedHiLoLongIdGenerator1 synchronized1NoSleepBigLo;
    private SynchronizedHiLoLongIdGenerator1 synchronized1NoSleepSmallLo;
    private SynchronizedHiLoLongIdGenerator1 synchronized1SleepBigLo;
    private SynchronizedHiLoLongIdGenerator1 synchronized1SleepSmallLo;
    private SynchronizedHiLoLongIdGenerator2 synchronized2NoSleepBigLo;
    private SynchronizedHiLoLongIdGenerator2 synchronized2NoSleepSmallLo;
    private SynchronizedHiLoLongIdGenerator2 synchronized2SleepBigLo;
    private SynchronizedHiLoLongIdGenerator2 synchronized2SleepSmallLo;
    private StampedLockHiLoLongIdGenerator stampedLockNoSleepBigLo;
    private StampedLockHiLoLongIdGenerator stampedLockNoSleepSmallLo;
    private StampedLockHiLoLongIdGenerator stampedLockSleepBigLo;
    private StampedLockHiLoLongIdGenerator stampedLockSleepSmallLo;
    private OptimisticHiLoLongIdGenerator optimisticNoSleepBigLo;
    private OptimisticHiLoLongIdGenerator optimisticNoSleepSmallLo;
    private OptimisticHiLoLongIdGenerator optimisticSleepBigLo;
    private OptimisticHiLoLongIdGenerator optimisticSleepSmallLo;
    private ConcurrentHiLoLongIdGenerator concurrentNoSleepBigLo;
    private ConcurrentHiLoLongIdGenerator concurrentNoSleepSmallLo;
    private ConcurrentHiLoLongIdGenerator concurrentSleepBigLo;
    private ConcurrentHiLoLongIdGenerator concurrentSleepSmallLo;

    public BenchmarkState() {
    }

    @Setup(Level.Trial)
    public final void setup() {
      final Supplier<InMemoryHiValueGenerator> hiValueGenNoSleepCreator = () -> new InMemoryHiValueGenerator(0, NoopSleeper.instance());
      final Supplier<InMemoryHiValueGenerator> hiValueGenSleepCreator = () -> new InMemoryHiValueGenerator(
        0, new GaussianSleeper(ofMillis(10), ofMillis(2)));
      final long smallLo = 10_000;
      final long bigLo = 1000_000;
      synchronized1NoSleepBigLo = new SynchronizedHiLoLongIdGenerator1(hiValueGenNoSleepCreator.get(), bigLo);
      synchronized1NoSleepSmallLo = new SynchronizedHiLoLongIdGenerator1(hiValueGenNoSleepCreator.get(), smallLo);
      synchronized1SleepBigLo = new SynchronizedHiLoLongIdGenerator1(hiValueGenSleepCreator.get(), bigLo);
      synchronized1SleepSmallLo = new SynchronizedHiLoLongIdGenerator1(hiValueGenSleepCreator.get(), smallLo);
      synchronized2NoSleepBigLo = new SynchronizedHiLoLongIdGenerator2(hiValueGenNoSleepCreator.get(), bigLo);
      synchronized2NoSleepSmallLo = new SynchronizedHiLoLongIdGenerator2(hiValueGenNoSleepCreator.get(), smallLo);
      synchronized2SleepBigLo = new SynchronizedHiLoLongIdGenerator2(hiValueGenSleepCreator.get(), bigLo);
      synchronized2SleepSmallLo = new SynchronizedHiLoLongIdGenerator2(hiValueGenSleepCreator.get(), smallLo);
      stampedLockNoSleepBigLo = new StampedLockHiLoLongIdGenerator(hiValueGenNoSleepCreator.get(), bigLo);
      stampedLockNoSleepSmallLo = new StampedLockHiLoLongIdGenerator(hiValueGenNoSleepCreator.get(), smallLo);
      stampedLockSleepBigLo = new StampedLockHiLoLongIdGenerator(hiValueGenSleepCreator.get(), bigLo);
      stampedLockSleepSmallLo = new StampedLockHiLoLongIdGenerator(hiValueGenSleepCreator.get(), smallLo);
      optimisticNoSleepBigLo = new OptimisticHiLoLongIdGenerator(hiValueGenNoSleepCreator.get(), bigLo);
      optimisticNoSleepSmallLo = new OptimisticHiLoLongIdGenerator(hiValueGenNoSleepCreator.get(), smallLo);
      optimisticSleepBigLo = new OptimisticHiLoLongIdGenerator(hiValueGenSleepCreator.get(), bigLo);
      optimisticSleepSmallLo = new OptimisticHiLoLongIdGenerator(hiValueGenSleepCreator.get(), smallLo);
      concurrentNoSleepBigLo = new ConcurrentHiLoLongIdGenerator(hiValueGenNoSleepCreator.get(), bigLo);
      concurrentNoSleepSmallLo = new ConcurrentHiLoLongIdGenerator(hiValueGenNoSleepCreator.get(), smallLo);
      concurrentSleepBigLo = new ConcurrentHiLoLongIdGenerator(hiValueGenSleepCreator.get(), bigLo);
      concurrentSleepSmallLo = new ConcurrentHiLoLongIdGenerator(hiValueGenSleepCreator.get(), smallLo);
    }
  }
}
