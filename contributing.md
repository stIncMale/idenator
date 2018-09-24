# Contributor Guide
## Build-related commands
This project uses [Maven](https://maven.apache.org/) for organizing, managing dependencies and build procedures.

Run from the project root directory:

Command | Description
--- | ---
`mvn clean install -f root.xml` | Install root.xml artifact to your local Maven repository (usually `~/.m2/repository/` directory).
`mvn clean install -f version.xml` |
`mvn clean install -f build.xml` |
`mvn clean test -f idenator/pom.xml -P unitTest` | Run unit tests, results are in `idenator/target/surefire-reports` directory.
`mvn clean test -f idenator/pom.xml -P concurrencyTest` | Run concurrency tests (takes noticeable time).
`mvn clean install -f ratmex/pom.xml -P doc` | Build idenator, generate Javadocs, install these artifacts to local Maven repository. Consider modifying value of `Constants.EXCLUDE_ASSERTIONS_FROM_BYTECODE` to true before building production-ready artifacts.
`mvn clean test -f performance-tests/pom.xml -P performanceTest -Dstincmale.idenator.performance.dryRun=false` | Run performance tests (takes a lot of time), results are in `performance-tests/target/idenator-performance`. Consider using `-Dstincmale.idenator.performance.dryRun=true` for dry runs. Take a look at `JmhOptions` to see/modify settings for performance tests.
`mvn clean install -f root.xml && mvn clean install -f version.xml && mvn clean install -f build.xml && mvn clean test -f idenator/pom.xml -P unitTest && mvn test -f idenator/pom.xml -P concurrencyTest && mvn install -f idenator/pom.xml -P doc && mvn clean test -f performance-tests/pom.xml -P performanceTest -Dstincmale.idenator.performance.dryRun=false` | A single combined command doing all the aforementioned in order.

