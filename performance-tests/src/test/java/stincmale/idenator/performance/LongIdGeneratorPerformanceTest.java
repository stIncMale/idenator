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
import stincmale.idenator.InMemoryHiValueGenerator;
import stincmale.idenator.auxiliary.GaussianSleeper;
import stincmale.idenator.auxiliary.NoopSleeper;
import stincmale.idenator.evolution.OptimisticHiLoLongIdGenerator1;
import stincmale.idenator.evolution.OptimisticHiLoLongIdGenerator2;
import stincmale.idenator.evolution.StampedLockHiLoLongIdGenerator1;
import stincmale.idenator.evolution.StampedLockHiLoLongIdGenerator2;
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

  @Test
  public final void throughputThreads4() throws RunnerException {
    runThroughputBenchmarks(4);
  }

  @Test
  public final void throughputThreads32() throws RunnerException {
    runThroughputBenchmarks(32);
  }

  @Benchmark
  public final long synchronized1NoSleepBigLo(final BenchmarkState state) {
    return state.synchronized1NoSleepBigLo.generate();
  }

  @Benchmark
  public final long synchronized1NoSleepSmallLo(final BenchmarkState state) {
    return state.synchronized1NoSleepSmallLo.generate();
  }

  @Benchmark
  public final long synchronized1SleepBigLo(final BenchmarkState state) {
    return state.synchronized1SleepBigLo.generate();
  }

  @Benchmark
  public final long synchronized1SleepSmallLo(final BenchmarkState state) {
    return state.synchronized1SleepSmallLo.generate();
  }

  @Benchmark
  public final long synchronized2NoSleepBigLo(final BenchmarkState state) {
    return state.synchronized2NoSleepBigLo.generate();
  }

  @Benchmark
  public final long synchronized2NoSleepSmallLo(final BenchmarkState state) {
    return state.synchronized2NoSleepSmallLo.generate();
  }

  @Benchmark
  public final long synchronized2SleepBigLo(final BenchmarkState state) {
    return state.synchronized2SleepBigLo.generate();
  }

  @Benchmark
  public final long synchronized2SleepSmallLo(final BenchmarkState state) {
    return state.synchronized2SleepSmallLo.generate();
  }

  @Benchmark
  public final long stampedLock1NoSleepBigLo(final BenchmarkState state) {
    return state.stampedLock1NoSleepBigLo.generate();
  }

  @Benchmark
  public final long stampedLock1NoSleepSmallLo(final BenchmarkState state) {
    return state.stampedLock1NoSleepSmallLo.generate();
  }

  @Benchmark
  public final long stampedLock1SleepBigLo(final BenchmarkState state) {
    return state.stampedLock1SleepBigLo.generate();
  }

  @Benchmark
  public final long stampedLock1SleepSmallLo(final BenchmarkState state) {
    return state.stampedLock1SleepSmallLo.generate();
  }

  @Benchmark
  public final long stampedLock2NoSleepBigLo(final BenchmarkState state) {
    return state.stampedLock2NoSleepBigLo.generate();
  }

  @Benchmark
  public final long stampedLock2NoSleepSmallLo(final BenchmarkState state) {
    return state.stampedLock2NoSleepSmallLo.generate();
  }

  @Benchmark
  public final long stampedLock2SleepBigLo(final BenchmarkState state) {
    return state.stampedLock2SleepBigLo.generate();
  }

  @Benchmark
  public final long stampedLock2SleepSmallLo(final BenchmarkState state) {
    return state.stampedLock2SleepSmallLo.generate();
  }

  @Benchmark
  public final long optimistic1NoSleepBigLo(final BenchmarkState state) {
    return state.optimistic1NoSleepBigLo.generate();
  }

  @Benchmark
  public final long optimistic1NoSleepSmallLo(final BenchmarkState state) {
    return state.optimistic1NoSleepSmallLo.generate();
  }

  @Benchmark
  public final long optimistic1SleepBigLo(final BenchmarkState state) {
    return state.optimistic1SleepBigLo.generate();
  }

  @Benchmark
  public final long optimistic1SleepSmallLo(final BenchmarkState state) {
    return state.optimistic1SleepSmallLo.generate();
  }

  @Benchmark
  public final long optimistic2NoSleepBigLo(final BenchmarkState state) {
    return state.optimistic2NoSleepBigLo.generate();
  }

  @Benchmark
  public final long optimistic2NoSleepSmallLo(final BenchmarkState state) {
    return state.optimistic2NoSleepSmallLo.generate();
  }

  @Benchmark
  public final long optimistic2SleepBigLo(final BenchmarkState state) {
    return state.optimistic2SleepBigLo.generate();
  }

  @Benchmark
  public final long optimistic2SleepSmallLo(final BenchmarkState state) {
    return state.optimistic2SleepSmallLo.generate();
  }

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
    private StampedLockHiLoLongIdGenerator1 stampedLock1NoSleepBigLo;
    private StampedLockHiLoLongIdGenerator1 stampedLock1NoSleepSmallLo;
    private StampedLockHiLoLongIdGenerator1 stampedLock1SleepBigLo;
    private StampedLockHiLoLongIdGenerator1 stampedLock1SleepSmallLo;
    private StampedLockHiLoLongIdGenerator2 stampedLock2NoSleepBigLo;
    private StampedLockHiLoLongIdGenerator2 stampedLock2NoSleepSmallLo;
    private StampedLockHiLoLongIdGenerator2 stampedLock2SleepBigLo;
    private StampedLockHiLoLongIdGenerator2 stampedLock2SleepSmallLo;
    private OptimisticHiLoLongIdGenerator1 optimistic1NoSleepBigLo;
    private OptimisticHiLoLongIdGenerator1 optimistic1NoSleepSmallLo;
    private OptimisticHiLoLongIdGenerator1 optimistic1SleepBigLo;
    private OptimisticHiLoLongIdGenerator1 optimistic1SleepSmallLo;
    private OptimisticHiLoLongIdGenerator2 optimistic2NoSleepBigLo;
    private OptimisticHiLoLongIdGenerator2 optimistic2NoSleepSmallLo;
    private OptimisticHiLoLongIdGenerator2 optimistic2SleepBigLo;
    private OptimisticHiLoLongIdGenerator2 optimistic2SleepSmallLo;

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
      stampedLock1NoSleepBigLo = new StampedLockHiLoLongIdGenerator1(hiValueGenNoSleepCreator.get(), bigLo);
      stampedLock1NoSleepSmallLo = new StampedLockHiLoLongIdGenerator1(hiValueGenNoSleepCreator.get(), smallLo);
      stampedLock1SleepBigLo = new StampedLockHiLoLongIdGenerator1(hiValueGenSleepCreator.get(), bigLo);
      stampedLock1SleepSmallLo = new StampedLockHiLoLongIdGenerator1(hiValueGenSleepCreator.get(), smallLo);
      stampedLock2NoSleepBigLo = new StampedLockHiLoLongIdGenerator2(hiValueGenNoSleepCreator.get(), bigLo);
      stampedLock2NoSleepSmallLo = new StampedLockHiLoLongIdGenerator2(hiValueGenNoSleepCreator.get(), smallLo);
      stampedLock2SleepBigLo = new StampedLockHiLoLongIdGenerator2(hiValueGenSleepCreator.get(), bigLo);
      stampedLock2SleepSmallLo = new StampedLockHiLoLongIdGenerator2(hiValueGenSleepCreator.get(), smallLo);
      optimistic1NoSleepBigLo = new OptimisticHiLoLongIdGenerator1(hiValueGenNoSleepCreator.get(), bigLo);
      optimistic1NoSleepSmallLo = new OptimisticHiLoLongIdGenerator1(hiValueGenNoSleepCreator.get(), smallLo);
      optimistic1SleepBigLo = new OptimisticHiLoLongIdGenerator1(hiValueGenSleepCreator.get(), bigLo);
      optimistic1SleepSmallLo = new OptimisticHiLoLongIdGenerator1(hiValueGenSleepCreator.get(), smallLo);
      optimistic2NoSleepBigLo = new OptimisticHiLoLongIdGenerator2(hiValueGenNoSleepCreator.get(), bigLo);
      optimistic2NoSleepSmallLo = new OptimisticHiLoLongIdGenerator2(hiValueGenNoSleepCreator.get(), smallLo);
      optimistic2SleepBigLo = new OptimisticHiLoLongIdGenerator2(hiValueGenSleepCreator.get(), bigLo);
      optimistic2SleepSmallLo = new OptimisticHiLoLongIdGenerator2(hiValueGenSleepCreator.get(), smallLo);
    }
  }
}
