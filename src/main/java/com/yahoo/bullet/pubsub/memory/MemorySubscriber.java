/*
 *  Copyright 2017, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.pubsub.memory;

import com.yahoo.bullet.common.BulletConfig;
import com.yahoo.bullet.pubsub.BufferingSubscriber;
import com.yahoo.bullet.pubsub.PubSubException;
import com.yahoo.bullet.pubsub.PubSubMessage;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;

@Slf4j
public class MemorySubscriber extends BufferingSubscriber {
    protected MemoryPubSubConfig config;
    protected AsyncHttpClient client;
    List<String> uris;

    /**
     * Create a MemorySubscriber from a {@link BulletConfig}.
     *
     * @param config The config.
     * @param maxUncommittedMessages The maximum number of records that will be buffered before commit() must be called.
     */
    public MemorySubscriber(MemoryPubSubConfig config, int maxUncommittedMessages, List<String> uris, AsyncHttpClient client) {
        super(maxUncommittedMessages);
        this.config = config;
        this.client = client;
        this.uris = uris;
    }

    @Override
    public List<PubSubMessage> getMessages() throws PubSubException {
        List<PubSubMessage> messages = new ArrayList<>();
        for (String uri : uris) {
            try {
                log.debug("Getting messages from uri: " + uri);
                Response response = client.prepareGet(uri).execute().get();
                int statusCode = response.getStatusCode();
                if (statusCode == Status.OK.getStatusCode()) {
                    messages.add(PubSubMessage.fromJSON(response.getResponseBody()));
                } else if (statusCode != Status.NO_CONTENT.getStatusCode()) {
                    // NO_CONTENT (204) indicates there are no new messages - anything else indicates a problem
                    log.error("Http call failed with status code {} and response {}.", statusCode, response);
                }
            } catch (Exception e) {
                log.error("Http call failed with error: " + e);
            }
        }
        return messages;
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            log.warn("Caught exception when closing AsyncHttpClient: " + e);
        }
    }
}
