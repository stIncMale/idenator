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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import stincmale.idenator.internal.EphemeralStrictlyIncreasingHiGenerator;
import stincmale.idenator.internal.GaussianSleeper;
import stincmale.idenator.performance.util.JmhOptions;
import stincmale.idenator.performance.util.TestTag;

@Disabled
@Tag(TestTag.PERFORMANCE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EphemeralStrictlyIncreasingHiGeneratorPerformanceTest {
  public EphemeralStrictlyIncreasingHiGeneratorPerformanceTest() {
  }

  private static final void runLatencyBenchmarks(final int numberOfThreads) throws RunnerException {
    new Runner(
      JmhOptions.includingClass(EphemeralStrictlyIncreasingHiGeneratorPerformanceTest.class)
        .mode(Mode.AverageTime)
        .timeUnit(TimeUnit.MILLISECONDS)
        .threads(numberOfThreads)
        .build())
      .run();
  }

  @Test
  final void throughputThreads4() throws RunnerException {
    runLatencyBenchmarks(4);
  }

  @Test
  final void throughputThreads32() throws RunnerException {
    runLatencyBenchmarks(32);
  }

  @Benchmark
  public final long next(final EphemeralStrictlyIncreasingHiGeneratorPerformanceTest.BenchmarkState state) {
    return state.ephemeralSleepingIdGen.next();
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private EphemeralStrictlyIncreasingHiGenerator ephemeralSleepingIdGen;

    public BenchmarkState() {
    }

    @Setup(Level.Trial)
    public final void setup() {
      ephemeralSleepingIdGen = new EphemeralStrictlyIncreasingHiGenerator(0, 5, new GaussianSleeper(ofMillis(8), ofMillis(2)));
    }
  }
}
