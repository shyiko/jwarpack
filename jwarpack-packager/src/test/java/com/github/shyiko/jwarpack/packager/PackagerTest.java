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

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertTrue;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
 */
public class PackagerTest {

    @Test
    public void testPack() throws Exception {
        File outputFile = File.createTempFile("jwarpack-", ".jar");
        Metadata metadata = new Metadata(
                "src/test/resources/server-launcher.jar",
                "src/test/resources/application.war",
                outputFile.getAbsolutePath(),
                false
        );
        new Packager().pack(metadata);
        assertTrue(outputFile.exists());
    }
}
