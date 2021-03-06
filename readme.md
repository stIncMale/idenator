# idenator
<p align="right">
<a href="https://docs.oracle.com/en/java/javase/11/"><img src="https://img.shields.io/badge/Java-11-blue.svg" alt="Java requirement"></a>
<a href="https://github.com/stIncMale/idenator/wiki"><img src="https://img.shields.io/badge/documentation-current-blue.svg" alt="Docs link"></a>
<a href="https://stincmale.github.io/idenator/apidocs/current/index.html"><img src="https://img.shields.io/badge/javadocs-current-blue.svg" alt="API docs"></a>
</p>

## About
An [open source](https://opensource.org/osd) Java library that supplies tools for generating identifiers.

All you need to get started is to read how it works [here](https://stincmale.github.io/idenator/apidocs/current/stincmale.idenator/stincmale/idenator/AbstractTwoPhaseLongIdGenerator.html), wrap your persistent ID generator (which is likely a database sequence generator) into [`ConcurrentTwoPhaseLongIdGenerator`](https://stincmale.github.io/idenator/apidocs/current/stincmale.idenator/stincmale/idenator/ConcurrentTwoPhaseLongIdGenerator.html) and get a [high-performance](https://github.com/stIncMale/idenator/wiki/Performance) persistent ID generator.

You can also read the article ["Implementing a concurrent two-phase ID generator in Java"](https://sites.google.com/site/aboutmale/techblog/twophaseidgen) which explains the implementation of [AbstractTwoPhaseLongIdGenerator](https://stincmale.github.io/idenator/apidocs/current/stincmale.idenator/stincmale/idenator/AbstractTwoPhaseLongIdGenerator.html).

## Versioning
This project uses [semantic versioning](https://semver.org). The current version is `0.1.0-SNAPSHOT`.

---

Copyright 2018 [Valiantsin Kavalenka](https://sites.google.com/site/aboutmale/)

Licensed under the Apache License, Version 2.0 (the "License") (except where another license is explicitly specified);
you may not use this project except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
