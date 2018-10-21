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
import stincmale.idenator.auxiliary.EphemeralStrictlyIncreasingHiGenerator;
import stincmale.idenator.auxiliary.NoopSleeper;
import stincmale.idenator.evolution.SynchronizedHiLoLongIdGenerator1;
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
  public final long var1NoLo(final BenchmarkState state) {
    return state.var1NoLo.next();
  }

  @Benchmark
  public final long var1SmallLo(final BenchmarkState state) {
    return state.var1SmallLo.next();
  }

  @Benchmark
  public final long var2NoLo(final BenchmarkState state) {
    return state.var2NoLo.next();
  }

  @Benchmark
  public final long var2SmallLo(final BenchmarkState state) {
    return state.var2SmallLo.next();
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private LongIdGenerator var1NoLo;
    private LongIdGenerator var1SmallLo;
    private LongIdGenerator var2NoLo;
    private LongIdGenerator var2SmallLo;

    public BenchmarkState() {
    }

    @Setup(Level.Trial)
    public final void setup() {
      final long noLo = 1;
      final long smallLo = 10_000;
      final long bigLo = 1000_000;
      var1NoLo = new SynchronizedHiLoLongIdGenerator1(
        new EphemeralStrictlyIncreasingHiGenerator(0, noLo - 1, NoopSleeper.instance()), noLo, true);
      var1SmallLo = new SynchronizedHiLoLongIdGenerator1(
        new EphemeralStrictlyIncreasingHiGenerator(0, smallLo - 1, NoopSleeper.instance()), smallLo, true);
      var2NoLo = new SynchronizedHiLoLongIdGenerator1(
        new EphemeralStrictlyIncreasingHiGenerator(0, noLo - 1, NoopSleeper.instance()), noLo, true);
      var2SmallLo = new SynchronizedHiLoLongIdGenerator1(
        new EphemeralStrictlyIncreasingHiGenerator(0, smallLo - 1, NoopSleeper.instance()), smallLo, true);
    }
  }
}
