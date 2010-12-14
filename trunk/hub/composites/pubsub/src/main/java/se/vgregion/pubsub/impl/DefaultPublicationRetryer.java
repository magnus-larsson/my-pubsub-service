package se.vgregion.pubsub.impl;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import se.vgregion.pubsub.PublicationFailedException;
import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.Topic;

public class DefaultPublicationRetryer implements PublicationRetryer {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultPublicationRetryer.class);
    private final static int DELAY = 60 * 1000;

    
    private Timer timer = new Timer("publication-retries");
    private TransactionTemplate transactionTemplate;

    public DefaultPublicationRetryer(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }
    
    public void addRetry(Topic topic, Subscriber subscriber) {
        LOG.info("Registring publication retry on {} to {}", topic.getUrl(), subscriber);
        timer.schedule(new PublicationRetryTask(transactionTemplate, (DefaultTopic) topic, subscriber), DELAY, DELAY);
    }
    
    public static class PublicationRetryTask extends TimerTask {
        private TransactionTemplate transactionTemplate;
        private DefaultTopic topic;
        private Subscriber subscriber;
        
        private int attempts = 0;

        public PublicationRetryTask(TransactionTemplate transactionTemplate, DefaultTopic topic, Subscriber subscriber) {
            this.transactionTemplate = transactionTemplate;
            this.topic = topic;
            this.subscriber = subscriber;
        }

        @Override
        public void run() {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        LOG.info("Retrying publication on {} to {}", topic.getUrl(), subscriber);
                        topic.publish(subscriber);
                        
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
