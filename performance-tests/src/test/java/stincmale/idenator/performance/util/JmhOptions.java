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

package stincmale.idenator.performance.util;

import java.util.ArrayList;
import java.util.Collection;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import static org.openjdk.jmh.runner.options.TimeValue.milliseconds;
import stincmale.idenator.doc.NotThreadSafe;

@NotThreadSafe
public final class JmhOptions {
  private static final boolean JAVA_SERVER = true;
  private static final boolean JAVA_ENABLE_ASSERTIONS = false;
  private static final boolean JAVA_DISABLE_BIASED_LOCKING = false;
  private static final boolean JAVA_DISABLE_GC = true;

  private JmhOptions() {
    throw new UnsupportedOperationException();
  }

  public static final OptionsBuilder includingClass(final Class<?> klass) {
    final OptionsBuilder result = get();
    result.include(klass.getName() + ".*");
    return result;
  }

  public static final OptionsBuilder get() {
    final OptionsBuilder result = new OptionsBuilder();
    final Collection<String> jvmArgs = new ArrayList<>();
    jvmArgs.add("-Xfuture");
    jvmArgs.add("-Xms2096m");
    jvmArgs.add("-Xmx2096m");
    if (JAVA_SERVER) {
      jvmArgs.add("-server");
    } else {
      jvmArgs.add("-client");
    }
    if (JAVA_ENABLE_ASSERTIONS) {
      jvmArgs.add("-enableassertions");
    } else {
      jvmArgs.add("-disableassertions");
    }
    if (JAVA_DISABLE_BIASED_LOCKING) {
      jvmArgs.add("-XX:-UseBiasedLocking");
    }
    if (JAVA_DISABLE_GC) {
      jvmArgs.add("-XX:+UnlockExperimentalVMOptions");
      jvmArgs.add("-XX:+UseEpsilonGC");
    }
    result.jvmArgs(jvmArgs.toArray(new String[jvmArgs.size()]))
      .shouldDoGC(false)
      .syncIterations(true)
      .shouldFailOnError(true)
      .threads(1)
      .timeout(milliseconds(5_000));
    result.forks(20)
      .warmupTime(milliseconds(200))
      .warmupIterations(10)
      .measurementTime(milliseconds(300))
      .measurementIterations(10);
    return result;
  }
}
