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

package stincmale.idenator;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import static stincmale.idenator.internal.util.Preconditions.checkArgument;
import static stincmale.idenator.internal.util.Preconditions.checkNotNull;

abstract class AbstractLongIdGeneratorTest {
  private final Collection<Supplier<LongIdGenerator>> idGenCreators;

  @SafeVarargs
  protected AbstractLongIdGeneratorTest(final Supplier<LongIdGenerator>... longIdGeneratorCreators) {
    checkNotNull(longIdGeneratorCreators, "longIdGeneratorCreators");
    checkArgument(longIdGeneratorCreators.length > 0, "longIdGeneratorCreators", "Must not be empty");
    @SuppressWarnings("varargs")
    final Collection<Supplier<LongIdGenerator>> idGenCreators = List.of(longIdGeneratorCreators);
    this.idGenCreators = idGenCreators;
  }

  protected final Collection<Supplier<LongIdGenerator>> getLongIdGeneratorCreators() {
    return idGenCreators;
  }
}
