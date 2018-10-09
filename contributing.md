# Contributor Guide
## Build-related commands
This project uses [Maven](https://maven.apache.org/) for organizing, managing dependencies and build procedures.

Run from the project root directory:

&#x23; | Command | Description
--- | --- | ---
1 | `mvn clean install -f root.xml` | Install root.xml artifact to your local Maven repository (usually `~/.m2/repository/` directory).
2 | `mvn clean install -f version.xml` |
3 | `mvn clean install -f build.xml` |
4 | `mvn clean test -f idenator/pom.xml -P withTests -Dgroups=unit` | Run only unit tests, results are in `idenator/target/surefire-reports` directory.
5 | `mvn clean test -f idenator/pom.xml -P withTests -Dgroups=concurrency` | Run only concurrency tests (takes noticeable time).
6 | `mvn clean test -f idenator/pom.xml -P withTests` | Combines 4 and 5.
7 | `mvn clean package -f idenator/pom.xml` | Build idenator and Javadocs artifacts. Consider modifying value of `Constants.EXCLUDE_ASSERTIONS_FROM_BYTECODE` to `true` before building production-ready artifacts.
8 | `mvn clean install -f idenator/pom.xml` | Build idenator and Javadocs artifacts, install these artifacts to local Maven repository. Consider modifying value of `Constants.EXCLUDE_ASSERTIONS_FROM_BYTECODE` to `true` before building production-ready artifacts.
9 | `mvn clean install -f idenator/pom.xml -P default,withTests` | Combines 6 and 8.
10 | `mvn clean test -f performance-tests/pom.xml -P withTests` | Run performance tests (takes a lot of time), results are in `performance-tests/target/idenator-performance`. Take a look at `JmhOptions` to see/modify settings for performance tests.
11 | `mvn clean install -f root.xml && mvn clean install -f version.xml && mvn clean install -f build.xml && mvn clean install -f idenator/pom.xml -P default,withTests && mvn clean test -f performance-tests/pom.xml -P withTests` | Combines everything (specifically 1, 2, 3, 9, 10).
