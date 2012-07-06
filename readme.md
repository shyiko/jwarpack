#jwarpack#

Overview
---------------

The jwarpack project aims to provide a simple way of turning WAR into standalone application (JAR file) which doesn't require external web container.

This type of artifact can be used in number of cases: 

  - your application is simple enough and you don't want external server to be involved 
  - your application is not that simple but you still want to use standalone bundle as a normal release artifact (just like it does [YouTrack](http://www.jetbrains.com/youtrack/download/index.html))

How does it work
---------------

The JAR file created by jwarpack contains embedded server classes (provided by one of the jwarpack-es modules, jwarpack-es-jetty6 for example) and the original web application. Once launched, main class bootstraps web container, deploys application and opens URL in a browser (if requested).

Usage
---------------

Arbitrary WAR file can be transformed into standalone JAR using one of the methods below.

After that, application can be run using "`java -jar yourapp-standalone.jar start`".

###Maven###
>Note:
>Plugin is not yet available in Maven Central.

            <plugin>
                <groupId>com.github.shyiko.jwarpack</groupId>
                <artifactId>jwarpack-maven-plugin</artifactId>
                <version>1.0</version>
                <dependencies>
                    <dependency>
                        <groupId>com.github.shyiko.jwarpack.es</groupId>
                        <artifactId>jwarpack-es-jetty6</artifactId>
                        <version>1.0</version>
                    </dependency>
                    <dependency>
                        <groupId>yourapp-groupid</groupId>
                        <artifactId>yourapp-artifactid</artifactId>
                        <version>yourapp-version</version>
                        <type>war</type>
                    </dependency>
                </dependencies>
                <executions>
                    <execution>
                        <goals>
                            <goal>pack</goal>
                        </goals>
                        <phase>package</phase>
                    </execution>
                </executions>
            </plugin>

>Tip:
>Use [following link](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.shyiko.jwarpack%22%20AND%20a%3A%22jwarpack-maven-plugin%22) to get the latest version of plugin available in Maven Central.
    
###CLI###

    java -jar jwarpack-cli-1.0.jar jwarpack-distribution-1.0/es/jwarpack-jetty6-1.0.jar yourapp.war yourapp-standalone.jar

History
---------------

Project was developed during the work on [jmxweb](https://github.com/shyiko/jmxweb).

License
---------------

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)