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
package com.github.shyiko.jwarpack.jetty;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
 */
public class BrowserLauncher {

    public static void open(String url) throws FailedToOpenBrowserException {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("mac")) {
                openBrowserUnderMacOS(url);
            } else
            if (osName.contains("win")) {
                openBrowserUnderWindows(url);
            } else {
                openBrowserUnderLinux(url);
            }
        } catch (Exception e) {
            throw new FailedToOpenBrowserException(e);
        }
    }

    private static void openBrowserUnderMacOS(String url) throws Exception {
        Class fileManager = Class.forName("com.apple.eio.FileManager");
        Method openURLMethod = fileManager.getDeclaredMethod("openURL", new Class[]{String.class});
        openURLMethod.invoke(null, url);
    }

    private static void openBrowserUnderWindows(String url) throws Exception {
        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
    }

    private static void openBrowserUnderLinux(String url) throws Exception {
        String[] browsers = {"google-chrome", "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
        String selectedBrowser = null;
        for (String browser : browsers) {
            if (Runtime.getRuntime().exec(new String[]{"which", browser}).waitFor() == 0) {
                selectedBrowser = browser;
                break;
            }
        }
        if (selectedBrowser == null) {
            throw new Exception("Failed to locate browser.");
        }
        Runtime.getRuntime().exec(new String[]{selectedBrowser, url});
    }
}
