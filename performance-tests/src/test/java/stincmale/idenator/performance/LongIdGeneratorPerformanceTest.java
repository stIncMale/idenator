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
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
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
import stincmale.idenator.LongIdGenerator;
import stincmale.idenator.performance.util.JmhOptions;
import stincmale.idenator.performance.util.PerformanceTestTag;
import static stincmale.idenator.performance.util.JmhOptions.jvmArgsAppend;
import static stincmale.idenator.performance.util.Utils.format;

@Tag(PerformanceTestTag.VALUE)
@TestInstance(Lifecycle.PER_CLASS)
public class LongIdGeneratorPerformanceTest {
  private static final long ACCEPTABLE_INCORRECTLY_REGISTERED_TICKS_EVENTS_COUNT_PER_TRIAL = 0;
  private static final String SYSTEM_PROPERTY_GROUP_OF_RUNS_DESCRIPTOR = "stincmale.idenator.performance.groupOfRunsDescriptor";
  private static final long LO_UPPER_BOUND_OPEN = 1000;
  private static final Duration maxNextHiDelay = Duration.of(10, ChronoUnit.MILLIS);

  private enum GroupOfRunsDescriptor {
    A_SYNCHRONIZED_IN_MEMORY_HILO_LONG_ID_GENERATOR(
        format("%s, max next hi delay: %sms", SynchronizedInMemoryHiLoLongIdGenerator.class.getSimpleName(), maxNextHiDelay.toMillis()),
        JmhOptions.numbersOfThreads,
        () -> new SynchronizedInMemoryHiLoLongIdGenerator(0, LO_UPPER_BOUND_OPEN, maxNextHiDelay)),
    B_CONCURRENT_IN_MEMORY_HILO_LONG_ID_GENERATOR(
        format("%s, max next hi delay: %sms", SynchronizedInMemoryHiLoLongIdGenerator.class.getSimpleName(), maxNextHiDelay.toMillis()),
        JmhOptions.numbersOfThreads,
        () -> new SynchronizedInMemoryHiLoLongIdGenerator(0, LO_UPPER_BOUND_OPEN, maxNextHiDelay));

    private final String description;
    private final Set<Integer> numbersOfThreads;
    private final Supplier<LongIdGenerator> longIdGeneratorCreator;

    GroupOfRunsDescriptor(
        final String name,
        final Collection<Integer> numbersOfThreads,
        final Supplier<LongIdGenerator> longIdGeneratorCreator) {
      this.description = name;
      this.numbersOfThreads = Collections.unmodifiableSet(new TreeSet<>(numbersOfThreads));
      this.longIdGeneratorCreator = longIdGeneratorCreator;
    }
  }

  public LongIdGeneratorPerformanceTest() {
  }

  @Test
  public final void synchronizedInMemoryHiLoLongIdGeneratorThroughput() {
    runThroughput(GroupOfRunsDescriptor.A_SYNCHRONIZED_IN_MEMORY_HILO_LONG_ID_GENERATOR);
  }

  @Test
  public final void synchronizedInMemoryHiLoLongIdGeneratorLatency() {
    runLatency(GroupOfRunsDescriptor.A_SYNCHRONIZED_IN_MEMORY_HILO_LONG_ID_GENERATOR);
  }

  @Test
  public final void concurrentInMemoryHiLoLongIdGeneratorThroughput() {
    runThroughput(GroupOfRunsDescriptor.B_CONCURRENT_IN_MEMORY_HILO_LONG_ID_GENERATOR);
  }

  @Test
  public final void concurrentInMemoryHiLoLongIdGeneratorLatency() {
    runLatency(GroupOfRunsDescriptor.B_CONCURRENT_IN_MEMORY_HILO_LONG_ID_GENERATOR);
  }

  @Benchmark
  public long generate(final BenchmarkState benchmarkState) {
    return benchmarkState.longIdGenerator.generate();
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private LongIdGenerator longIdGenerator;

    public BenchmarkState() {
    }

    @Setup(Level.Trial)
    public final void setup() {
      final GroupOfRunsDescriptor groupOfRunsDescriptor = GroupOfRunsDescriptor.valueOf(System.getProperty(SYSTEM_PROPERTY_GROUP_OF_RUNS_DESCRIPTOR));
      longIdGenerator = groupOfRunsDescriptor.longIdGeneratorCreator.get();
    }
  }

  private final void runThroughput(final GroupOfRunsDescriptor groupOfRunsDescriptor) {
    final int numberOfAvailableProcessors = Runtime.getRuntime().availableProcessors();
    for (int numberOfThreads : groupOfRunsDescriptor.numbersOfThreads) {
      runThroughput(groupOfRunsDescriptor, numberOfThreads);
    }
  }

  private final void runLatency(final GroupOfRunsDescriptor groupOfRunsDescriptor) {
    final int numberOfAvailableProcessors = Runtime.getRuntime().availableProcessors();
    for (int numberOfThreads : groupOfRunsDescriptor.numbersOfThreads) {
      if (numberOfThreads <= numberOfAvailableProcessors) {//there is no sense in measuring latency of a system oversaturated with threads
        runLatency(groupOfRunsDescriptor, numberOfThreads);
      }
    }
  }

  private final void runThroughput(final GroupOfRunsDescriptor groupOfRunsDescriptor, final int numberOfThreads) {
    try {
      new Runner(jvmArgsAppend(
          JmhOptions.includingClass(LongIdGeneratorPerformanceTest.class),
          format("-D%s=%s", SYSTEM_PROPERTY_GROUP_OF_RUNS_DESCRIPTOR, groupOfRunsDescriptor.name()))
          .mode(Mode.Throughput)
          .timeUnit(TimeUnit.MILLISECONDS)
          .threads(numberOfThreads)
          .build())
          .run();
    } catch (final RunnerException e) {
      throw new RuntimeException(e);
    }
  }

  private final void runLatency(final GroupOfRunsDescriptor groupOfRunsDescriptor, final int numberOfThreads) {
    try {
      new Runner(jvmArgsAppend(
          JmhOptions.includingClass(LongIdGeneratorPerformanceTest.class),
          format("-D%s=%s", SYSTEM_PROPERTY_GROUP_OF_RUNS_DESCRIPTOR, groupOfRunsDescriptor.name()))
          .mode(Mode.AverageTime)
          .timeUnit(TimeUnit.MICROSECONDS)
          .threads(numberOfThreads)
          .build())
          .run();
    } catch (final RunnerException e) {
      throw new RuntimeException(e);
    }
  }
}
