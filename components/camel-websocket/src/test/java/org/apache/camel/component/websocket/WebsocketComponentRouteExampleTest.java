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
import com.ning.http.client.websocket.WebSocket;
import com.ning.http.client.websocket.WebSocketTextListener;
import com.ning.http.client.websocket.WebSocketUpgradeHandler;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WebsocketComponentRouteExampleTest extends CamelTestSupport {

    private static List<String> received = new ArrayList<String>();
    private static CountDownLatch latch = new CountDownLatch(1);

    @Test
    public void testWSHttpCall() throws Exception {
        AsyncHttpClient c = new AsyncHttpClient();

        WebSocket websocket = c.prepareGet("ws://localhost:9494/echo").execute(
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

        websocket.sendTextMessage("Beer");
        assertTrue(latch.await(10, TimeUnit.SECONDS));

        assertEquals(1, received.size());
        assertEquals("BeerBeer", received.get(0));

        websocket.close();
        c.close();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {

                WebsocketComponent websocketComponent = getContext().getComponent("websocket", WebsocketComponent.class);
                websocketComponent.setHost("localhost");
                websocketComponent.setPort(9494);

                from("websocket://echo")
                    .log(">>> Message received from WebSocket Client : ${body}")
                    .transform().simple("${body}${body}")
                    .to("websocket://echo");

            }
        };
    }
}
