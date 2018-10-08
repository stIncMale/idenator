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

import static java.lang.Boolean.parseBoolean;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import static org.openjdk.jmh.runner.options.TimeValue.milliseconds;
import stincmale.idenator.doc.NotThreadSafe;

@NotThreadSafe
public final class JmhOptions {
  private static final boolean DRY_RUN = parseBoolean(System.getProperty("stincmale.idenator.performance.dryRun", "false"));
  private static final boolean JAVA_SERVER = true;
  private static final boolean JAVA_BIASED_LOCKING = false;
  private static final boolean JAVA_ASSERTIONS = DRY_RUN;

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
    result.jvmArgs("-Xms1024m", "-Xmx1024m", "-XX:+UseBiasedLocking")
      .jvmArgsAppend(
        JAVA_SERVER ? "-server" : "-client",
        JAVA_ASSERTIONS ? "-enableassertions" : "-disableassertions",
        JAVA_BIASED_LOCKING ? "-XX:+UseBiasedLocking" : "-XX:-UseBiasedLocking")
      .shouldDoGC(false)
      .syncIterations(true)
      .shouldFailOnError(true)
      .threads(1)
      .timeout(milliseconds(60_000));
    if (DRY_RUN) {
      result.forks(1)
        .warmupTime(milliseconds(50))
        .warmupIterations(1)
        .measurementTime(milliseconds(50))
        .measurementIterations(1);
    } else {
      result.forks(4)
        .warmupTime(milliseconds(300))
        .warmupIterations(7)
        .measurementTime(milliseconds(500))
        .measurementIterations(5);
    }
    return result;
  }
}
