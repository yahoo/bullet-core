/*
 *  Copyright 2019, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.pubsub;

import com.yahoo.bullet.common.BulletConfig;
import com.yahoo.bullet.pubsub.rest.RESTPublisher;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class BulletPubSubResponderTest {
    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Publisher.*")
    public void testUncreatablePublisher() {
        BulletConfig config = new BulletConfig();
        config.set(BulletConfig.PUBSUB_CLASS_NAME, "does.not.exist");
        new BulletPubSubResponder(config);
    }

    @Test
    public void testCreation() {
        BulletPubSubResponder responder = new BulletPubSubResponder(new BulletConfig());
        Assert.assertNotNull(responder);
        Assert.assertNotNull(responder.publisher);
        Assert.assertTrue(responder.publisher instanceof RESTPublisher);
    }

    @Test
    public void testResponding() throws Exception {
        BulletConfig config = new BulletConfig("test_config.yaml");
        config.set(MockPubSub.MOCK_MESSAGE_NAME, "test");
        BulletPubSubResponder responder = new BulletPubSubResponder(config);

        PubSubMessage message = new PubSubMessage("id", "");
        responder.respond("id", message);
        verify(responder.publisher).send(eq(message));
    }

    @Test
    public void testRespondingFailure() throws Exception {
        BulletConfig config = new BulletConfig("test_config.yaml");
        config.set(MockPubSub.MOCK_MESSAGE_NAME, "test");
        BulletPubSubResponder responder = new BulletPubSubResponder(config);
        doThrow(new PubSubException("Testing")).when(responder.publisher).send(any());

        PubSubMessage message = new PubSubMessage("id", "");
        responder.respond("id", message);
        verify(responder.publisher).send(eq(message));
    }
}
