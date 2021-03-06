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
package org.apache.camel.component.jt400;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.BaseDataQueue;
import com.ibm.as400.access.DataQueue;
import com.ibm.as400.access.KeyedDataQueue;
import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.component.jt400.Jt400DataQueueEndpoint.Format;
import org.apache.camel.impl.DefaultProducer;

/**
 * {@link Producer} to send data to an AS/400 data queue.
 */
public class Jt400DataQueueProducer extends DefaultProducer {

    private final Jt400DataQueueEndpoint endpoint;

    protected Jt400DataQueueProducer(Jt400DataQueueEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    /**
     * Sends the {@link Exchange}'s in body to the AS/400 data queue. If the
     * endpoint's format is set to {@link Format#binary}, the data queue entry's
     * data will be sent as a <code>byte[]</code>. If the endpoint's format is
     * set to {@link Format#text}, the data queue entry's data will be sent as a
     * <code>String</code>.
     * <p/>
     * If the endpoint is configured to publish to a {@link KeyedDataQueue},
     * then the {@link org.apache.camel.Message} header <code>KEY</code> must be set.
     */
    public void process(Exchange exchange) throws Exception {
        BaseDataQueue queue = endpoint.getDataQueue();
        if (endpoint.isKeyed()) {
            process((KeyedDataQueue) queue, exchange);
        } else {
            process((DataQueue) queue, exchange);
        }
    }

    private void process(DataQueue queue, Exchange exchange) throws Exception {
        if (endpoint.getFormat() == Format.binary) {
            queue.write(exchange.getIn().getBody(byte[].class));
        } else {
            queue.write(exchange.getIn().getBody(String.class));
        }
    }

    private void process(KeyedDataQueue queue, Exchange exchange) throws Exception {
        if (endpoint.getFormat() == Format.binary) {
            queue.write(exchange.getIn().getHeader(Jt400DataQueueEndpoint.KEY, byte[].class), exchange.getIn().getBody(byte[].class));
        } else {
            queue.write(exchange.getIn().getHeader(Jt400DataQueueEndpoint.KEY, String.class), exchange.getIn().getBody(String.class));
        }
    }

    @Override
    protected void doStart() throws Exception {
        if (!endpoint.getSystem().isConnected()) {
            log.info("Connecting to " + endpoint);
            endpoint.getSystem().connectService(AS400.DATAQUEUE);
        }
    }

    @Override
    protected void doStop() throws Exception {
        if (endpoint.getSystem().isConnected()) {
            log.info("Disconnecting from " + endpoint);
            endpoint.getSystem().disconnectAllServices();
        }
    }

}
