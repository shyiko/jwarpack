/*
 * Copyright 2012 Stanley Shyiko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.shyiko.jwarpack.cli;

import com.github.shyiko.jwarpack.packager.Metadata;
import com.github.shyiko.jwarpack.packager.Packager;

import java.io.IOException;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
 */
public class EntryPoint {

    public static void main(String[] args) {
        int numberOfArguments = args.length;
        if (numberOfArguments != 3 && numberOfArguments != 4) {
            printUsage();
            System.exit(1);
        }
        String serverLauncherJar = args[0];
        String applicationWar = args[1];
        String outputFile = args[2];
        boolean useCompression = "--compress".equalsIgnoreCase(args.length == 4 ? args[3] : null);
        Metadata metadata = null;
        try {
            metadata = new Metadata(serverLauncherJar, applicationWar, outputFile, useCompression);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        try {
            new Packager().pack(metadata);
        } catch (IOException e) {
            System.err.println("Failed to complete packaging. Error: ");
            e.printStackTrace(System.err);
        }
    }

    private static void printUsage() {
        System.err.println("Usage:");
        System.err.println("java -jar jwarpack-cli.jar <jwarpack-<server-name>.jar location> <your-app.war location> <output dir or file>");
        System.err.println("Adding --compress option to the end of command will enable JAR compression");
    }
}
