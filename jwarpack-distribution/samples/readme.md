How to use examples
---------------

1. build sample WAR (this step is required in order to proceed further)

            cd samples/sample-webapp
            mvn install # this should build target/sample-webapp-$VERSION.war

2. build standalone JAR using maven plugin

            cd samples/sample-webapp-pack-maven
            mvn package # this should build target/sample-webapp-pack-maven-$VERSION.jar

3. build standalone JAR using CLI (example for nix only)

            cd samples/sample-webapp-pack-cli
            sh jwarpack.sh # this should build target/sample-webapp-pack-cli-$VERSION.jar

