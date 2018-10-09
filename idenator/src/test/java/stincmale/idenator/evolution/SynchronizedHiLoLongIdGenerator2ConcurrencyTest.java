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

package stincmale.idenator.evolution;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import stincmale.idenator.AbstractLongIdGeneratorConcurrencyTest;
import stincmale.idenator.InMemoryHiValueGenerator;
import stincmale.idenator.auxiliary.NoopSleeper;
import stincmale.idenator.util.TestTag;

@Tag(TestTag.CONCURRENCY)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
final class SynchronizedHiLoLongIdGenerator2ConcurrencyTest extends AbstractLongIdGeneratorConcurrencyTest {
  private SynchronizedHiLoLongIdGenerator2ConcurrencyTest() {
    super(2 * Math.max(2, Runtime.getRuntime().availableProcessors()),
      () -> new SynchronizedHiLoLongIdGenerator2(new InMemoryHiValueGenerator(0, NoopSleeper.instance()), 1),
      () -> new SynchronizedHiLoLongIdGenerator2(new InMemoryHiValueGenerator(0, NoopSleeper.instance()), 10));
  }
}
