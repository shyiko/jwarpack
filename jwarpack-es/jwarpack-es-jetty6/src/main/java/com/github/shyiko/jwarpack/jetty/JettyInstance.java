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

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.xml.XmlConfiguration;

import java.io.File;
import java.io.InputStream;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
 */
public class JettyInstance {

    private Server server;

    public JettyInstance(EmbeddedServerConfig config) throws Throwable {
        XmlConfiguration serverConfiguration;
        File configurationFile = config.getJettyXMLFile();
        if (configurationFile == null) {
            InputStream inputStream = getClass().getResourceAsStream("/jetty.xml");
            serverConfiguration = new XmlConfiguration(inputStream);
        } else {
            serverConfiguration = new XmlConfiguration(configurationFile.toURI().toURL());
        }
        server = (Server) serverConfiguration.configure();
        Connector[] connectors = this.server.getConnectors();
        if (connectors.length == 1) {
            Connector connector = connectors[0];
            String host = config.getHost();
            if (host != null && !host.isEmpty()) {
                connector.setHost(host);
            }
            int port = config.getPort();
            if (port > 0) {
                connector.setPort(port);
            }
        }
    }

    public void start() throws Exception {
        server.start();
    }

    public void deploy(String context, String path) {
        WebAppContext webAppContext = new WebAppContext(path, context);
        webAppContext.setLogUrlOnStart(true);
        server.addHandler(webAppContext);
    }

    public void undeploy(String context) {
        for (Handler handler : server.getHandlers()) {
            if (handler instanceof WebAppContext) {
                if (context.equals(((WebAppContext) handler).getContextPath())) {
                    server.removeHandler(handler);
                    break;
                }
            }
        }
    }

    public void stop() throws Exception {
        server.stop();
    }
}
