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

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Creates standalone JAR file based on data provided by {@link Metadata} instance.
 *
 * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
 */
public class Packager {

    /**
     * Merge server launcher jar and application war into one JAR file.
     *
     * @param metadata metadata
     * @return standalone JAR file
     * @throws IOException if anything goes wrong during standalone JAR file creation
     */
    public File pack(Metadata metadata) throws IOException {
        File tempDirectory = new File(System.getProperty("java.io.tmpdir"), "jwarpack-" + System.currentTimeMillis());
        tempDirectory.mkdir();
        extract(metadata.getServerLauncherJar(), tempDirectory, Collections.<String>emptySet());
        extract(metadata.getApplicationWar(), tempDirectory, Arrays.asList("META-INF/MANIFEST.MF"));
        File outputJar = metadata.getOutputJar();
        jar(tempDirectory, outputJar, metadata.isUseCompression());
        delete(tempDirectory);
        return outputJar;
    }
    
    private void extract(File archiveFile, File targetDirectory, Collection<String> filesToSkip) throws IOException {
        ZipFile zipFile = new ZipFile(archiveFile);
        Enumeration zipFileEntries = zipFile.entries();
        while (zipFileEntries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) zipFileEntries.nextElement();
            String zipEntryName = zipEntry.getName();
            if (filesToSkip.contains(zipEntryName)) {
                continue;
            }
            File outputFile = new File(targetDirectory, zipEntryName);
            if (zipEntry.isDirectory()) {
                outputFile.mkdirs();
            } else {
                InputStream inputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                try {
                    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
                    try {
                        copyStream(inputStream, outputStream);
                    } finally {
                        outputStream.close();
                    }
                } finally {
                    inputStream.close();
                }
            }
        }
    }

    private void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int size;
        while ((size = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, size);
        }
    }
    
    private void jar(File sourceDirectory, File archiveFile, boolean useCompression) throws IOException {
        ZipOutputStream outputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(archiveFile)));
        try {
            if (useCompression) {
                outputStream.setMethod(ZipOutputStream.DEFLATED);
            }
            Stack<File> stack = new Stack<File>();
            stack.push(sourceDirectory);
            while (!stack.empty()) {
                File file = stack.pop();
                if (file.isDirectory()) {
                    File[] children = file.listFiles();
                    if (children != null) {
                        for (File child : children) {
                            stack.push(child);
                        }
                    }
                } else {
                    String zipEntryName = extractEntryName(sourceDirectory, file);
                    ZipEntry zipEntry = new ZipEntry(zipEntryName);
                    outputStream.putNextEntry(zipEntry);
                    InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                    try {
                        copyStream(inputStream, outputStream);
                    } finally {
                        inputStream.close();
                    }
                }
            }
        } finally {
            outputStream.close();
        }
    }
    
    private String extractEntryName(File rootDirectory, File file) {
        return file.getAbsolutePath().substring(rootDirectory.getAbsolutePath().length() + 1).replace("\\", "/");
    }

    private void delete(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    delete(child);
                }
            }
        }
        file.delete();
    }
}
