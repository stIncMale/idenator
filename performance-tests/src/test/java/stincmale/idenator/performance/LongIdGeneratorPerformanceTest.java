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
import stincmale.idenator.SynchronizedHiLoLongIdGenerator;
import stincmale.idenator.auxiliary.GaussianSleeper;
import stincmale.idenator.auxiliary.NoopSleeper;
import stincmale.idenator.performance.util.JmhOptions;
import stincmale.idenator.performance.util.TestTag;

@Tag(TestTag.PERFORMANCE)
@TestInstance(Lifecycle.PER_CLASS)
public class LongIdGeneratorPerformanceTest {
  public LongIdGeneratorPerformanceTest() {
  }

  private static final void runThroughputBenchmarks(final int numberOfThreads) throws RunnerException {
    new Runner(JmhOptions.includingClass(LongIdGeneratorPerformanceTest.class)
      .mode(Mode.Throughput)
      .timeUnit(TimeUnit.MICROSECONDS)
      .threads(numberOfThreads)
      .build())
      .run();
  }

  private static final void runLatencyBenchmarks(final int numberOfThreads) throws RunnerException {
    if (numberOfThreads <= Runtime.getRuntime().availableProcessors()) {//no sense in measuring latency of a system oversaturated with threads
      new Runner(JmhOptions.includingClass(LongIdGeneratorPerformanceTest.class)
        .mode(Mode.AverageTime)
        .timeUnit(TimeUnit.NANOSECONDS)
        .threads(numberOfThreads)
        .build())
        .run();
    }
  }

  @Test
  public final void throughputThreads1() throws RunnerException {
    runThroughputBenchmarks(1);
  }

  @Test
  public final void latencyThreads1() throws RunnerException {
    runLatencyBenchmarks(1);
  }

  @Test
  public final void throughputThreads4() throws RunnerException {
    runThroughputBenchmarks(4);
  }

  @Test
  public final void latencyThreads4() throws RunnerException {
    runLatencyBenchmarks(4);
  }

  @Test
  public final void throughputThreads32() throws RunnerException {
    runThroughputBenchmarks(32);
  }

  @Test
  public final void latencyThreads32() throws RunnerException {
    runLatencyBenchmarks(32);
  }

  @Benchmark
  public final long concurrentNoSleepBigLo(final BenchmarkState state) {
    return state.concurrentNoSleepBigLo.generate();
  }

  @Benchmark
  public final long concurrentNoSleepSmallLo(final BenchmarkState state) {
    return state.concurrentNoSleepSmallLo.generate();
  }

  @Benchmark
  public final long concurrentSleepBigLo(final BenchmarkState state) {
    return state.concurrentSleepBigLo.generate();
  }

  @Benchmark
  public final long concurrentSleepSmallLo(final BenchmarkState state) {
    return state.concurrentSleepSmallLo.generate();
  }

  @Benchmark
  public final long synchronizedNoSleepBigLo(final BenchmarkState state) {
    return state.synchronizedNoSleepBigLo.generate();
  }

  @Benchmark
  public final long synchronizedNoSleepSmallLo(final BenchmarkState state) {
    return state.synchronizedNoSleepSmallLo.generate();
  }

  @Benchmark
  public final long synchronizedSleepBigLo(final BenchmarkState state) {
    return state.synchronizedSleepBigLo.generate();
  }

  @Benchmark
  public final long synchronizedSleepSmallLo(final BenchmarkState state) {
    return state.synchronizedSleepSmallLo.generate();
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private ConcurrentHiLoLongIdGenerator concurrentNoSleepBigLo;
    private ConcurrentHiLoLongIdGenerator concurrentNoSleepSmallLo;
    private ConcurrentHiLoLongIdGenerator concurrentSleepBigLo;
    private ConcurrentHiLoLongIdGenerator concurrentSleepSmallLo;
    private SynchronizedHiLoLongIdGenerator synchronizedNoSleepBigLo;
    private SynchronizedHiLoLongIdGenerator synchronizedNoSleepSmallLo;
    private SynchronizedHiLoLongIdGenerator synchronizedSleepBigLo;
    private SynchronizedHiLoLongIdGenerator synchronizedSleepSmallLo;

    public BenchmarkState() {
    }

    @Setup(Level.Trial)
    public final void setup() {
      final Supplier<InMemoryHiValueGenerator> hiValueGenNoSleepCreator = () -> new InMemoryHiValueGenerator(0, NoopSleeper.instance());
      final Supplier<InMemoryHiValueGenerator> hiValueGenSleepCreator = () -> new InMemoryHiValueGenerator(
        0, new GaussianSleeper(ofMillis(10), ofMillis(2)));
      final long smallLo = 10_000;
      final long bigLo = 1000_000;
      concurrentNoSleepBigLo = new ConcurrentHiLoLongIdGenerator(hiValueGenNoSleepCreator.get(), bigLo);
      concurrentNoSleepSmallLo = new ConcurrentHiLoLongIdGenerator(hiValueGenNoSleepCreator.get(), smallLo);
      concurrentSleepBigLo = new ConcurrentHiLoLongIdGenerator(hiValueGenSleepCreator.get(), bigLo);
      concurrentSleepSmallLo = new ConcurrentHiLoLongIdGenerator(hiValueGenSleepCreator.get(), smallLo);
      synchronizedNoSleepBigLo = new SynchronizedHiLoLongIdGenerator(hiValueGenNoSleepCreator.get(), bigLo);
      synchronizedNoSleepSmallLo = new SynchronizedHiLoLongIdGenerator(hiValueGenNoSleepCreator.get(), smallLo);
      synchronizedSleepBigLo = new SynchronizedHiLoLongIdGenerator(hiValueGenSleepCreator.get(), bigLo);
      synchronizedSleepSmallLo = new SynchronizedHiLoLongIdGenerator(hiValueGenSleepCreator.get(), smallLo);
    }
  }
}
