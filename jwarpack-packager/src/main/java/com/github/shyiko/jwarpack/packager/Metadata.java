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
package com.github.shyiko.jwarpack.packager;

import java.io.File;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
 */
public class Metadata {

    private File serverLauncherJar;
    private File applicationWar;
    private File outputJar;
    private boolean useCompression;

    /**
     * @param serverLauncherJar server launcher JAR provided by jwarpack
     * @param applicationWar application WAR
     * @param outputFile output file or directory
     * @param useCompression true if compression should be used, false otherwise
     */
    public Metadata(String serverLauncherJar, String applicationWar, String outputFile, boolean useCompression) {
        if (serverLauncherJar == null) {
            throw new IllegalArgumentException("Server launcher JAR must be specified");
        }
        File serverLauncherFile = new File(serverLauncherJar);
        if (!serverLauncherFile.isFile()) {
            throw new IllegalArgumentException(
                    String.format("File %s cannot be used as server launcher JAR", serverLauncherFile.getAbsolutePath()));
        }
        this.serverLauncherJar = serverLauncherFile;
        if (applicationWar == null) {
            throw new IllegalArgumentException("Application WAR must be specified");
        }
        File applicationFile = new File(applicationWar);
        if (!applicationFile.isFile()) {
            throw new IllegalArgumentException(
                    String.format("File %s cannot be used as application WAR", applicationFile.getAbsolutePath()));
        }
        this.applicationWar = applicationFile;
        if (outputFile == null) {
            throw new IllegalArgumentException("Output file must be specified");
        }
        File output = new File(outputFile);
        if ((!output.exists() && (outputFile.endsWith("\\") || outputFile.endsWith("/")))
                || output.isDirectory()) {
            output.mkdirs();
            output = new File(output, generateOutputJarName(applicationFile));
        }
        this.outputJar = output;
        this.useCompression = useCompression;
    }

    private String generateOutputJarName(File applicationWar) {
        String result = applicationWar.getName().toLowerCase();
        if (result.endsWith(".war")) {
            result = result.substring(0, result.length() - 4);
        }
        return result + "-standalone.jar";
    }

    public File getServerLauncherJar() {
        return serverLauncherJar;
    }

    public File getApplicationWar() {
        return applicationWar;
    }

    public File getOutputJar() {
        return outputJar;
    }

    public boolean isUseCompression() {
        return useCompression;
    }
}
