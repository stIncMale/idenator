# Contributor Guide
## Build-related commands
This project uses [Maven](https://maven.apache.org/) for build automation.

Run from the project root directory:

&#x23; | Command | Description
--- | --- | ---
1 | `mvn clean test -f idenator/pom.xml -P withTests -Dgroups=unit` | Run only unit tests. Results are in `idenator/target/surefire-reports` directory.
2 | `mvn clean test -f idenator/pom.xml -P withTests -Dgroups=concurrency` | Run only concurrency tests (takes noticeable time).
3 | `mvn clean test -f idenator/pom.xml -P withTests` | Combines 1 and 2.
4 | `mvn clean verify -f idenator/pom.xml` | Build `idenator` and Javadocs artifacts. Consider modifying value of `Constants.EXCLUDE_ASSERTIONS_FROM_BYTECODE` to `true` before building production-ready artifacts.
5 | `mvn clean install -f idenator/pom.xml` | Build `idenator` and Javadocs artifacts, install these artifacts to the local Maven repository (usually `~/.m2/repository/` directory). Consider modifying value of `Constants.EXCLUDE_ASSERTIONS_FROM_BYTECODE` to `true` before building production-ready artifacts.
6 | `mvn clean install -f idenator/pom.xml -P default,withTests` | Combines 3 and 5.
7 | `mvn clean install -f root.xml && mvn clean install -f version.xml && mvn clean install -f build.xml` | Install `root.xml`, `version.xml`, `build.xml` artifacts to the local Maven repository.
8 | `mvn clean test -f performance-tests/pom.xml -P withTests -Dstincmale.idenator.performance.dryRun=false` | Run performance tests (takes significant time), requires `idenator`, `root.xml`, `version.xml`, `build.xml` artifacts to be installed in the local Maven repository in advance. Consider using `-Dstincmale.idenator.performance.dryRun=true` for dry runs. Take a look at `JmhOptions` to see/modify settings for performance tests.
9 | `mvn clean test -f performance-tests/pom.xml -P withTests -Dstincmale.idenator.performance.dryRun=false -Dtest=LongIdGeneratorPerformanceTest` | Similar to 8, but runs a specific test.
10 | `mvn clean install -f idenator/pom.xml -P default,withTests && mvn clean install -f root.xml && mvn clean install -f version.xml && mvn clean install -f build.xml && mvn clean test -f performance-tests/pom.xml -P withTests -Dstincmale.idenator.performance.dryRun=false` | Combines 6, 7, 8.
