/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.pubsub.push.impl;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import nu.xom.ParsingException;
import org.junit.Assert;
import org.junit.Test;

import se.vgregion.docpublishing.v1.DocumentStatusType;
import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PublicationFailedException;
import se.vgregion.pubsub.PushJms;
import se.vgregion.pubsub.content.AbstractParser;
import se.vgregion.pubsub.impl.DefaultFeed;
import se.vgregion.pubsub.push.SubscriptionMode;
import se.vgregion.pubsub.push.UnitTestConstants;


public class DefaultPushSubscriberTest {

    @Test(expected=IllegalArgumentException.class)
    public void cstrNullTopic() {
        new DefaultPushSubscriber(null, UnitTestConstants.CALLBACK, 123, "token", UnitTestConstants.SECRET, true);
    }    
    @Test(expected=IllegalArgumentException.class)
    public void cstrNullCallback() {
        new DefaultPushSubscriber(UnitTestConstants.TOPIC, null, 123, "token", UnitTestConstants.SECRET, true);
    }    

    @Test
    public void getTimeout() {
        DefaultPushSubscriber subscriber = new DefaultPushSubscriber(UnitTestConstants.TOPIC, UnitTestConstants.CALLBACK, 0, "token", UnitTestConstants.SECRET, true);
        
        Assert.assertNull(subscriber.getTimeout());
        
    }    

    
    @Test
    public void verificationUrl() throws UnsupportedEncodingException {
        DefaultPushSubscriber subscriber = new DefaultPushSubscriber(UnitTestConstants.TOPIC, UnitTestConstants.CALLBACK, 123, "token", UnitTestConstants.SECRET, true);
        
        Assert.assertEquals(
                URI.create(UnitTestConstants.CALLBACK + "?hub.mode=subscribe&hub.topic=" + URLEncoder.encode(UnitTestConstants.TOPIC.toString(), "UTF-8") + "&hub.challenge=ch&hub.lease_seconds=123&hub.verify_token=token"), 
                subscriber.getVerificationUrl(SubscriptionMode.SUBSCRIBE, "ch"));
    }

    @Test
    public void verificationUrlUnsubscribe() throws UnsupportedEncodingException {
        DefaultPushSubscriber subscriber = new DefaultPushSubscriber(UnitTestConstants.TOPIC, UnitTestConstants.CALLBACK, 123, "token", UnitTestConstants.SECRET, true);
        
        Assert.assertEquals(
                URI.create(UnitTestConstants.CALLBACK + "?hub.mode=unsubscribe&hub.topic=" + URLEncoder.encode(UnitTestConstants.TOPIC.toString(), "UTF-8") + "&hub.challenge=ch&hub.lease_seconds=123&hub.verify_token=token"), 
                subscriber.getVerificationUrl(SubscriptionMode.UNSUBSCRIBE, "ch"));
    }

    
    @Test
    public void verificationUrlNoLeaseSecondsAndToken() throws UnsupportedEncodingException {
        DefaultPushSubscriber subscriber = new DefaultPushSubscriber(UnitTestConstants.TOPIC, UnitTestConstants.CALLBACK, 0, null, UnitTestConstants.SECRET, true);
        
        Assert.assertEquals(
                URI.create(UnitTestConstants.CALLBACK + "?hub.mode=subscribe&hub.topic=" + URLEncoder.encode(UnitTestConstants.TOPIC.toString(), "UTF-8") + "&hub.challenge=ch"), 
                subscriber.getVerificationUrl(SubscriptionMode.SUBSCRIBE, "ch"));
    }

    @Test
    public void verificationUrlCallbackWithQuery() throws UnsupportedEncodingException {
        DefaultPushSubscriber subscriber = new DefaultPushSubscriber(UnitTestConstants.TOPIC, URI.create(UnitTestConstants.CALLBACK.toString() + "?abc=def"), 0, null, UnitTestConstants.SECRET, true);
        
        Assert.assertEquals(
                URI.create(UnitTestConstants.CALLBACK + "?abc=def&hub.mode=subscribe&hub.topic=" + URLEncoder.encode(UnitTestConstants.TOPIC.toString(), "UTF-8") + "&hub.challenge=ch"), 
                subscriber.getVerificationUrl(SubscriptionMode.SUBSCRIBE, "ch"));
    }

    @Test
    public void publish() throws PublicationFailedException, IOException, ParsingException {
        DefaultPushSubscriber subscriber = new DefaultPushSubscriber(UnitTestConstants.TOPIC, URI.create(UnitTestConstants.CALLBACK.toString() + "?abc=def"), 0, null, UnitTestConstants.SECRET, true);

        PushJms pushJms = new PushJms() {
            @Override
            public boolean send(Feed feed, String systemMessage, DocumentStatusType type) {
                return true;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public PushJms copy(String newConsumerLocation) {
                return this;
            }
        };



        //File file =  new File("../../../../../src/test/resources/atom3.xml");
        URL url = getClass().getResource("../../../../../");
        String path = url.getFile();
        File file =  new File(path + File.separator + "atom3.xml");

        FileInputStream xml = new FileInputStream(file);

        Feed feed = AbstractParser.create(ContentType.ATOM).parse(xml, ContentType.ATOM);

        subscriber.publish(feed, pushJms);
    }

}
