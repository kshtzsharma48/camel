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
package org.apache.camel.component.twitter.consumer;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.direct.DirectConsumer;
import org.apache.camel.component.twitter.TwitterEndpoint;

/**
 * Camel DirectConsumer implementation.
 * 
 */
public class TwitterConsumerDirect extends DirectConsumer implements TwitterConsumer {

    private Twitter4JConsumer twitter4jConsumer;

    public TwitterConsumerDirect(TwitterEndpoint endpoint, Processor processor,
                                 Twitter4JConsumer twitter4jConsumer) {
        super(endpoint, processor);

        this.twitter4jConsumer = twitter4jConsumer;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        Iterator<? extends Serializable> i = twitter4jConsumer.directConsume().iterator();
        while (i.hasNext()) {
            Exchange e = getEndpoint().createExchange();
            e.getIn().setBody(i.next());
            getProcessor().process(e);
        }
    }
}
