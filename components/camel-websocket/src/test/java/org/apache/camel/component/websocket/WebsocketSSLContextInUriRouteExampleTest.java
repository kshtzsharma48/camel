/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.websocket;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.websocket.WebSocket;
import com.ning.http.client.websocket.WebSocketTextListener;
import com.ning.http.client.websocket.WebSocketUpgradeHandler;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WebsocketSSLContextInUriRouteExampleTest extends CamelTestSupport {

    private static final String NULL_VALUE_MARKER = CamelTestSupport.class.getCanonicalName();
    private static List<String> received = new ArrayList<String>();
    private static CountDownLatch latch = new CountDownLatch(10);
    private Properties originalValues = new Properties();
    private String pwd = "changeit";
    private String uriConsumer;
    private String uriProducer;
    private String server = "127.0.0.1";
    private int port = 8443;

    @Override
    @Before
    public void setUp() throws Exception {

        URL trustStoreUrl = this.getClass().getClassLoader().getResource("jsse/localhost.ks");
        setSystemProp("javax.net.ssl.trustStore", trustStoreUrl.toURI().getPath());
        uriConsumer = "websocket://" + server + ":" + port + "/test?sslContextParametersRef=#sslContextParameters";
        uriProducer = "websocket://" + server + ":" + port + "/test";

        super.setUp();
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        KeyStoreParameters ksp = new KeyStoreParameters();
        ksp.setResource("jsse/localhost.ks");
        ksp.setPassword("changeit");

        KeyManagersParameters kmp = new KeyManagersParameters();
        kmp.setKeyPassword("changeit");
        kmp.setKeyStore(ksp);

        TrustManagersParameters tmp = new TrustManagersParameters();
        tmp.setKeyStore(ksp);

        SSLContextParameters sslContextParameters = new SSLContextParameters();
        sslContextParameters.setKeyManagers(kmp);
        sslContextParameters.setTrustManagers(tmp);

        JndiRegistry registry = super.createRegistry();
        registry.bind("sslContextParameters", sslContextParameters);
        return registry;
    }

    protected void setSystemProp(String key, String value) {
        String originalValue = System.setProperty(key, value);
        originalValues.put(key, originalValue != null ? originalValue : NULL_VALUE_MARKER);
    }

    protected AsyncHttpClient createAsyncHttpSSLClient() throws IOException, GeneralSecurityException {

        AsyncHttpClient c;
        AsyncHttpClientConfig config;

        AsyncHttpClientConfig.Builder builder =
                new AsyncHttpClientConfig.Builder();

        builder.setSSLContext(new SSLContextParameters().createSSLContext());
        config = builder.build();
        c = new AsyncHttpClient(config);

        return c;
    }

    protected SSLContextParameters defineSSLContextParameters() {

        KeyStoreParameters ksp = new KeyStoreParameters();
        // ksp.setResource(this.getClass().getClassLoader().getResource("jsse/localhost.ks").toString());
        ksp.setResource("jsse/localhost.ks");
        ksp.setPassword(pwd);

        KeyManagersParameters kmp = new KeyManagersParameters();
        kmp.setKeyPassword(pwd);
        kmp.setKeyStore(ksp);

        TrustManagersParameters tmp = new TrustManagersParameters();
        tmp.setKeyStore(ksp);

        SSLContextParameters sslContextParameters = new SSLContextParameters();
        sslContextParameters.setKeyManagers(kmp);
        sslContextParameters.setTrustManagers(tmp);

        return sslContextParameters;
    }

    @Test
    public void testWSHttpCall() throws Exception {

        AsyncHttpClient c = createAsyncHttpSSLClient();
        WebSocket websocket = c.prepareGet("wss://127.0.0.1:8443/test").execute(
                new WebSocketUpgradeHandler.Builder()
                        .addWebSocketListener(new WebSocketTextListener() {
                            @Override
                            public void onMessage(String message) {
                                received.add(message);
                                log.info("received --> " + message);
                                latch.countDown();
                            }

                            @Override
                            public void onFragment(String fragment, boolean last) {
                            }

                            @Override
                            public void onOpen(WebSocket websocket) {
                            }

                            @Override
                            public void onClose(WebSocket websocket) {
                            }

                            @Override
                            public void onError(Throwable t) {
                                t.printStackTrace();
                            }
                        }).build()).get();

        getMockEndpoint("mock:client").expectedBodiesReceived("Hello from WS client");

        websocket.sendTextMessage("Hello from WS client");
        assertTrue(latch.await(10, TimeUnit.SECONDS));

        assertMockEndpointsSatisfied();

        assertEquals(10, received.size());
        for (int i = 0; i < 10; i++) {
            assertEquals(">> Welcome on board!", received.get(i));
        }

        websocket.close();
        c.close();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {

                from(uriConsumer)
                     .log(">>> Message received from WebSocket Client : ${body}")
                     .to("mock:client")
                     .loop(10)
                         .setBody().constant(">> Welcome on board!")
                         .to(uriConsumer);
            }
        };
    }
}