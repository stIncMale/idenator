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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
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
import stincmale.idenator.performance.util.JmhOptions;

/**
 * Test environment: [single CPU] 3.4 GHz Intel Core i5 (4 cores),
 * [OS] macOS 10.13.6 x86_64, [JDK] OpenJDK 11.0.1+13 (<a href="https://jdk.java.net/11/">a build from Oracle</a>).
 * <pre>{@code
 * 1 thread
 * Benchmark                                          Mode  Cnt   Score   Error   Units
 * BaselinePerformanceTest.synchronizedIncrement     thrpt   50   55.081 ± 0.084  ops/us (-XX:-UseBiasedLocking)
 * BaselinePerformanceTest.synchronizedIncrement     thrpt   50  242.180 ± 0.757  ops/us
 * BaselinePerformanceTest.reentrantRWLockIncrement  thrpt   50   57.720 ± 0.058  ops/us
 * BaselinePerformanceTest.stampedLockIncrement      thrpt   50   60.422 ± 0.188  ops/us
 * BaselinePerformanceTest.atomicIncrement           thrpt   50  144.278 ± 0.147  ops/us
 *
 * 4 threads
 * Benchmark                                          Mode  Cnt   Score   Error   Units
 * BaselinePerformanceTest.synchronizedIncrement     thrpt   50  18.194 ± 0.111  ops/us (-XX:-UseBiasedLocking)
 * BaselinePerformanceTest.synchronizedIncrement     thrpt   50  20.493 ± 0.099  ops/us
 * BaselinePerformanceTest.reentrantRWLockIncrement  thrpt   50  33.816 ± 0.519  ops/us
 * BaselinePerformanceTest.stampedLockIncrement      thrpt   50  42.399 ± 0.668  ops/us
 * BaselinePerformanceTest.atomicIncrement           thrpt   50  50.996 ± 0.082  ops/us
 *
 * 32 threads
 * Benchmark                                          Mode  Cnt   Score   Error   Units
 * BaselinePerformanceTest.synchronizedIncrement     thrpt   50  18.186 ± 0.106  ops/us (-XX:-UseBiasedLocking)
 * BaselinePerformanceTest.synchronizedIncrement     thrpt   50  20.421 ± 0.128  ops/us
 * BaselinePerformanceTest.reentrantRWLockIncrement  thrpt   50  35.563 ± 0.703  ops/us
 * BaselinePerformanceTest.stampedLockIncrement      thrpt   50  42.527 ± 1.081  ops/us
 * BaselinePerformanceTest.atomicIncrement           thrpt   50  51.488 ± 0.525  ops/us
 * }</pre>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaselinePerformanceTest {
  public BaselinePerformanceTest() {
  }

  private static final void runThroughputBenchmarks(final int numberOfThreads) throws RunnerException {
    new Runner(
        JmhOptions.includingClass(BaselinePerformanceTest.class)
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
  public final long synchronizedIncrement(final BaselinePerformanceTest.BenchmarkState state) {
    synchronized (state.mutex) {
      return state.plainLong++;
    }
  }

  @Benchmark
  public final long stampedLockIncrement(final BaselinePerformanceTest.BenchmarkState state) {
    final StampedLock lock = state.stampedLock;
    final long stamp = lock.writeLock();
    try {
      return state.plainLong++;
    } finally {
      lock.unlockWrite(stamp);
    }
  }

  @Benchmark
  public final long reentrantRWLockIncrement(final BaselinePerformanceTest.BenchmarkState state) {
    final ReentrantReadWriteLock.WriteLock lock = state.lock;
    lock.lock();
    try {
      return state.plainLong++;
    } finally {
      lock.unlock();
    }
  }

  @Benchmark
  public final long atomicIncrement(final BaselinePerformanceTest.BenchmarkState state) {
    return state.atomicLong.getAndIncrement();
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private Object mutex;
    private StampedLock stampedLock;
    private ReentrantReadWriteLock.WriteLock lock;
    private long plainLong;
    private AtomicLong atomicLong;

    public BenchmarkState() {
    }

    @Setup(Level.Trial)
    public final void setup() {
      mutex = new Object();
      stampedLock = new StampedLock();
      lock = new ReentrantReadWriteLock().writeLock();
      plainLong = 0;
      atomicLong = new AtomicLong();
    }
  }
}
