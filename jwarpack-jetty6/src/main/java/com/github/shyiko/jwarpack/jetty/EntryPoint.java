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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
 */
public class EntryPoint {

    private static final String APPLICATION_NAME = "application.name";
    private static final String APPLICATION_CONTEXT = "application.context";

    public static void main(String[] args) throws Throwable {
        @Parameters(separators = "=", optionPrefixes = "")
        class CLIParameters {
            @Parameter(names = {"--host", "-h"}, arity = 1,
                       description = "Hostname or IP Address, Jetty will be bounded to (default localhost)")
            private String host = "localhost";
            @Parameter(names = {"--port", "-p"}, arity = 1,
                       description = "Port, Jetty will listen to (default 8080)")
            private Integer port = 8080;
            @Parameter(names = {"--monitoringPort", "-m"}, arity = 1,
                       description = "Monitoring port (default 8888, -1 turns it of)")
            private Integer monitoringPort = 8888;
            @Parameter(names = {"--config", "-c"}, arity = 1,
                       description = "Custom jetty.xml")
            private File config;
            @Parameter(names = {"--browser", "-b"}, arity = 1,
                       description = "Start browser (default false)")
            private boolean startBrowser;
            @Parameter(names = {"--browserURL", "-u"}, arity = 1,
                    description = "URL to open (applies only if --browser option is on) " +
                                  "(default http://<host>:<port>/<application.context>)")
            private String browserUrl;
            @Parameter(description = "Target <start|stop|status>", required = true)
            private List<String> mode;
        }
        CLIParameters parameters = new CLIParameters();
        JCommander commander = new JCommander(parameters);
        try {
            commander.parse(args);
            if (parameters.mode == null || parameters.mode.size() != 1 ||
                !Arrays.asList("start", "stop", "status").contains(parameters.mode.get(0))) {
                throw new ParameterException("Target is invalid.");
            }
        } catch (ParameterException e) {
            System.err.println("Error: " + e.getMessage());
            commander.usage();
            System.exit(1);
        }
        String mode = parameters.mode.get(0);
        EmbeddedServerConfig configuration = new EmbeddedServerConfig(parameters.host, parameters.port, parameters.config);
        if ("start".equals(mode)) {
            startServer(configuration, parameters.monitoringPort);
            if (parameters.startBrowser) {
                String url = parameters.browserUrl;
                if (url == null) {
                    String context = JWarPackBundle.get(APPLICATION_CONTEXT, "/");
                    url = String.format("http://%s:%s%s", configuration.getHost(), configuration.getPort(), context);
                }
                BrowserLauncher.open(url);
            }
        } else {
            MonitoringServer monitoringServer = new MonitoringServer();
            monitoringServer.setPort(parameters.monitoringPort);
            if ("stop".equalsIgnoreCase(mode)) {
                monitoringServer.sendShutdownRequest();
            } else 
            if ("status".equalsIgnoreCase(mode)) {
                monitoringServer.sendStatusRequest();
            }
        }
    }

    private static void startServer(EmbeddedServerConfig configuration, int monitoringPort) throws Throwable {
        prepareEnvironment();
        final JettyInstance jettyInstance = new JettyInstance(configuration);
        deployApplication(jettyInstance);
        jettyInstance.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    jettyInstance.stop();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        });
        startMonitoringServer(monitoringPort);
    }

    private static void prepareEnvironment() {
        System.setProperty("java.awt.headless", "true");
        String jettyHome = JWarPackBundle.get("jetty.home");
        if (jettyHome == null) {
            jettyHome = System.getProperty("user.home") + File.separator + "." + JWarPackBundle.get(APPLICATION_NAME);
            System.setProperty("jetty.home", jettyHome);
        }
        File jettyWorkDirectory = new File(jettyHome, "work");
        jettyWorkDirectory.mkdirs();
        deleteDirectoryContent(jettyWorkDirectory);
    }

    private static void deployApplication(JettyInstance jettyInstance) {
        String context = JWarPackBundle.get(APPLICATION_CONTEXT, "/");
        String path = getJarFileLocation();
        jettyInstance.deploy(context, path);
    }

    private static String getJarFileLocation() {
        String path = EntryPoint.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            return URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void startMonitoringServer(int monitoringPort) {
        if (monitoringPort > -1) {
            MonitoringServer monitoringServer = new MonitoringServer();
            monitoringServer.setPort(monitoringPort);
            monitoringServer.startListeningThread();
        }
    }

    private static void deleteDirectoryContent(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectoryContent(file);
                }
                file.delete();
            }
        }
    }
}
