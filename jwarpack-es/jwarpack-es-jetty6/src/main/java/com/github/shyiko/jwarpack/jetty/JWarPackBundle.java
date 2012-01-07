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

import java.util.ResourceBundle;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
 */
public class JWarPackBundle {

    private static ResourceBundle bundle = ResourceBundle.getBundle("jwarpack");

    private JWarPackBundle() {}

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        String result = bundle.containsKey(key) ? bundle.getString(key) : System.getProperty(key);
        return result == null ? defaultValue : result;
    }
}
