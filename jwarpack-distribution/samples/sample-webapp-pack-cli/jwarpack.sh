#!/bin/bash
VERSION=1.0-SNAPSHOT
MAVEN_HOME=~/.m2/repository
DISTRIBUTION_DIR=../..
SERVER_LAUNCHER_JAR=$DISTRIBUTION_DIR/lib/es/jwarpack-es-jetty6-$VERSION.jar
APPLICATION_WAR=$MAVEN_HOME/com/github/shyiko/jwarpack/distribution/samples/sample-webapp/$VERSION/sample-webapp-$VERSION.war
OUTPUT_JAR=sample-webapp-pack-cli-$VERSION.jar
java -jar $DISTRIBUTION_DIR/jwarpack-cli-$VERSION.jar $SERVER_LAUNCHER_JAR $APPLICATION_WAR target/$OUTPUT_JAR
