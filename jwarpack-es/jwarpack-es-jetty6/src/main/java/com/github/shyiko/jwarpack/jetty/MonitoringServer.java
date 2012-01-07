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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
 */
public class MonitoringServer {

    private static final String REQUEST_STATUS = "status";
    private static final String REQUEST_SHUTDOWN = "stop";

    private int port;
    private String key = "jwarpack";

    public void setPort(int port) {
        this.port = port;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void startListeningThread() {
        new ListeningThread().start();
    }

    public void sendShutdownRequest() {
        sendRequest(REQUEST_SHUTDOWN + ":" + key);
    }

    public void sendStatusRequest() {
        sendRequest(REQUEST_STATUS);
    }

    private void sendRequest(String request) {
        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), port);
            try {
                writeToSocket(socket, request);
            } finally {
                socket.close();
            }
            System.out.println("Connection established");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void writeToSocket(Socket socket, String request) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        try {
            outputStream.write(request.getBytes());
        } finally {
            outputStream.close();
        }
    }

    private class ListeningThread extends Thread {

        private ServerSocket socket;

        private ListeningThread() {
            setName("MonitoringServer");
            setDaemon(true);
        }

        public void run() {
            try {
                socket = new ServerSocket(port, 1, InetAddress.getLocalHost());
                port = socket.getLocalPort();
                try {
                    loop:
                    while (true) {
                        Socket socket = this.socket.accept();
                        try {
                            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            try {
                                String line;
                                while ((line = inputStream.readLine()) != null) {
                                    if (REQUEST_STATUS.equalsIgnoreCase(line)) {
                                        OutputStream outputStream = socket.getOutputStream();
                                        outputStream.write("Running...\n".getBytes());
                                        outputStream.flush();
                                    } else if ((REQUEST_SHUTDOWN + ":" + key).equalsIgnoreCase(line)) {
                                        break loop;
                                    }
                                }
                            } finally {
                                inputStream.close();
                            }
                        } finally {
                            socket.close();
                        }
                    }
                } finally {
                    socket.close();
                }
                System.exit(0);
            } catch (Exception e) {
                System.err.println("Monitoring thread failed.");
                e.printStackTrace(System.err);
            }
        }
    }
}
