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

package se.vgregion.pubsub.impl;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import se.vgregion.pubsub.*;

public class DefaultPublicationRetryer implements PublicationRetryer {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultPublicationRetryer.class);
    private final static int DELAY = 60 * 1000;

    private static PushJms pushJms;
    
    private Timer timer = new Timer("publication-retries");
    private TransactionTemplate transactionTemplate;

    public DefaultPublicationRetryer(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }
    
    public void addRetry(Topic topic, Subscriber subscriber, Feed feed) {
        LOG.info("Registring publication retry on {} to {}", topic.getUrl(), subscriber);
        timer.schedule(new PublicationRetryTask(transactionTemplate, (DefaultTopic) topic, subscriber, feed), DELAY, DELAY);
    }

    public PushJms getPushJms() {
        return pushJms;
    }

    public void setPushJms(PushJms pushJms) {
        this.pushJms = pushJms;
    }

    public static class PublicationRetryTask extends TimerTask {
        private TransactionTemplate transactionTemplate;
        private DefaultTopic topic;
        private Subscriber subscriber;
        private Feed feed;
        
        private int attempts = 0;

        public PublicationRetryTask(TransactionTemplate transactionTemplate, DefaultTopic topic, Subscriber subscriber, Feed feed) {
            this.transactionTemplate = transactionTemplate;
            this.topic = topic;
            this.subscriber = subscriber;
            this.feed = feed;
        }

        @Override
        public void run() {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        LOG.info("Attempt " + attempts + " at retrying publication on {} to {}", topic.getUrl(), subscriber);
                        subscriber.publish(feed, pushJms);
                        
                        // success! don't attempt retrying again
                        cancel();
                    } catch (PublicationFailedException e) {
                        attempts++;
                        
                        if(attempts > 10) {
                            // give up
                            LOG.warn("Giving up on retrying publishing to {}", subscriber);
                            cancel();
                        } else {
                            // ignore, this task will run again later
                            LOG.info("Failed publishing to {}, will retry later", subscriber);
                        }
                    }
                }
            });
        }
    }
}
