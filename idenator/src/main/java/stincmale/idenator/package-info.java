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

/**
 * Contains tools for generating identifiers.
 * <p>
 * <b>Glossary</b><br>
 * <i>Identifier (ID)</i><br>
 * A piece of data which is unique within a defined scope. Such a piece of data can be used to identify something within the scope.
 * <p>
 * <i>ID generator</i><br>
 * A tool which generates identifiers. All ID generators manifest the following flavours:
 * ephemeral or persistent, monotonic (strictly increasing, strictly decreasing) or nonmonotonic, sparse or condensed (see below).
 * <p>
 * <i>ID generator range</i><br>
 * Range of an ID generator is a complete set of all identifiers which can be generated by the ID generator.
 * We will denote it by {@code G}. {@code G} is totally ordered if and only if all its elements are comparable
 * (e.i. all elements are ordered by the same lower than or equal/greater than or equal binary relation).
 * <p>
 * <i>Ephemeral/persistent flavour</i><br>
 * Any identifier generated by the same instance of an ID generator is always unique among all identifiers generated by this instance.
 * If this uniqueness guarantee holds among all identifiers generated by different instances of the ID generator,
 * even those created at different executions of the program, then the ID generator is persistent, otherwise it is ephemeral.
 * <p>
 * <i>Monotonic/nonmonotonic flavour</i><br>
 * If the range {@code G} of an ID generator is totally ordered,
 * and the ID generator produces identifiers in the order corresponding with such a total order,
 * then the ID generator is monotonic, otherwise it is nonmonotonic.
 * A monotonic ID generator can be either strictly increasing or strictly decreasing
 * depending on the definition of lower than or equal/greater than or equal binary relation.
 * <p>
 * Note that monotonicity does not guarantee that the generated identifiers are consecutive.
 * Actually, there is no sense in talking about consecutiveness/nonconsecutiveness alone with regard to ID generators,
 * but there is a sense in talking about sparseness/condenseness.
 * <p>
 * <i>Sparse/condense flavour</i><br>
 * An ID generator is sparse if and only if all of the following statements are true:
 * <ul>
 * <li>
 * Its range {@code G} is a set of integral numbers.
 * </li>
 * <li>
 * There exists an integral number {@code sparseness > 0} such that for any identifier {@code id ∈ G} the following holds
 * {@code ((id - (sparseness + 1); id) ∪ (id; id + (sparseness + 1))) ∩ G = ∅} and there exist at least one {@code id' ∈ G} such that
 * {@code ((id' - (sparseness + 2); id') ∪ (id'; id' + (sparseness + 2))) ∩ G ≠ ∅}.
 * </li>
 * </ul>
 * In other words {@code (sparseness + 1)} is the minimal value of {@code |id1 - id2|} among all {@code id1, id2 ∈ G} such that {@code id1 ≠ id2}.
 * <p>
 * <i>Compatible identifiers</i><br>
 * If all identifiers that can be produced by each of two or more ID generators are unique among all identifiers
 * that can be generated by all these ID generators, then such ID generators are called compatible.
 * E.g. if one of two ID generators always produce even integral numbers
 * and the other one always produce odd integral numbers, then these ID generators are compatible.
 */
package stincmale.idenator;
