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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
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
import stincmale.idenator.performance.util.JmhOptions;
import stincmale.idenator.performance.util.TestTag;

@Disabled
@Tag(TestTag.PERFORMANCE)
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
    final Lock lock = state.lock;
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
    private Lock lock;
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
