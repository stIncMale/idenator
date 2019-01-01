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

import java.time.Duration;
import static java.time.Duration.ofMillis;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import stincmale.idenator.LongIdGenerator;
import stincmale.idenator.internal.Delayer;
import stincmale.idenator.internal.EphemeralStrictlyIncreasingHiGenerator;
import stincmale.idenator.performance.util.GaussianBlackHoleCpuConsumer;
import stincmale.idenator.performance.util.GaussianSleeper;
import stincmale.idenator.performance.util.JmhOptions;

/**
 * Test environment: [single CPU] 3.4 GHz Intel Core i5 (4 cores),
 * [OS] macOS 10.13.6, [JDK] OpenJDK 11.0.1+13 (<a href="https://jdk.java.net/11/">a build from Oracle</a>).
 * <pre>{@code
 * 1 thread
 * Benchmark                                                              (longIdGenerator)  Mode  Cnt   Score   Error  Units
 * EphemeralStrictlyIncreasingHiGeneratorPerformanceTest.next               constantSleeper  avgt   50   9.941 ± 0.048  ms/op
 * EphemeralStrictlyIncreasingHiGeneratorPerformanceTest.next  constantBlackHoleCpuConsumer  avgt   50   9.981 ± 0.011  ms/op
 *
 * 4 threads
 * Benchmark                                                              (longIdGenerator)  Mode  Cnt   Score   Error  Units
 * EphemeralStrictlyIncreasingHiGeneratorPerformanceTest.next               constantSleeper  avgt   50   9.886 ± 0.062  ms/op
 * EphemeralStrictlyIncreasingHiGeneratorPerformanceTest.next  constantBlackHoleCpuConsumer  avgt   50  10.651 ± 0.039  ms/op
 *
 * 32 threads
 * Benchmark                                                              (longIdGenerator)  Mode  Cnt   Score   Error  Units
 * EphemeralStrictlyIncreasingHiGeneratorPerformanceTest.next               constantSleeper  avgt   50   9.759 ± 0.119  ms/op
 * EphemeralStrictlyIncreasingHiGeneratorPerformanceTest.next  constantBlackHoleCpuConsumer  avgt   50  81.673 ± 0.666  ms/op
 * }</pre>
 */
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
  final void throughputThreads1() throws RunnerException {
    runLatencyBenchmarks(1);
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
  public final long next(final BenchmarkState state) {
    return state.longIdGenerator.instance.next();
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    @Param
    private TestableLongIdGenerator longIdGenerator;

    public BenchmarkState() {
    }

    /**
     * A different computer/JDK/JMH will most likely require different constructor arguments to achieve the same delays.
     */
    public enum TestableLongIdGenerator {
      constantSleeper(new GaussianSleeper(ofMillis(8), Duration.ZERO)),
      /**
       * Obviously, the time {@link GaussianBlackHoleCpuConsumer#delay()} takes to return depends on the availability of CPU,
       * hence measuring the delay makes sense only with a single thread.
       */
      constantBlackHoleCpuConsumer(new GaussianBlackHoleCpuConsumer(5_300_000, 0));

      private final LongIdGenerator instance;

      TestableLongIdGenerator(final Delayer delayer) {
        instance = new EphemeralStrictlyIncreasingHiGenerator(0, 5, delayer);
      }
    }
  }
}
