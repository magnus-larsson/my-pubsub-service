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

package se.vgregion.pubsub.twitter;

import java.net.URI;

import org.mockito.Mockito;

import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.PubSubEngine;
import se.vgregion.pubsub.PubSubEventListener;
import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.SubscriberManager;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.twitter.impl.TwitterPublisher;


public class TwitterRunner {

    public static void main(String[] args) throws InterruptedException {
        String username = args[0];
        String password = args[1];
        
        PubSubEngine engine = new PubSubEngine() {
            @Override
            public void publish(URI url, Feed feed) {
                System.out.println(feed);
            }
            
            @Override
            public Topic getTopic(URI url) {return null;}
            
            @Override
            public Topic getOrCreateTopic(URI url) {return null;}
            
            @Override
            public Topic createTopic(URI url) {return null;}

            @Override
            public void subscribe(Subscriber subscriber) {
            }

            @Override
            public void unsubscribe(Subscriber subscriber) {
            }

            @Override
            public void addPubSubEventListener(PubSubEventListener eventListener) {
            }

            @Override
            public void removePubSubEventListener(PubSubEventListener eventListener) {
            }

			@Override
			public void subscribe(SubscriberManager subscriberManager) {
				
			}
        };
        
        TwitterPublisher consumer = new TwitterPublisher(engine, username, password);
        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.when(subscriber.getTopic()).thenReturn(URI.create(TwitterPublisher.FILTER_URL + "#" + "bieber"));
        consumer.onSubscribe(subscriber);
        
        Thread.sleep(50000);
        consumer.stop();
    }
    
}
