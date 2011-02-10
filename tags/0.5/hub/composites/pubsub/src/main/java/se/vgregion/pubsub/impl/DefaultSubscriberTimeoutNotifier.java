package se.vgregion.pubsub.impl;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import se.vgregion.pubsub.Subscriber;
import se.vgregion.pubsub.SubscriberTimeoutNotifier;

public class DefaultSubscriberTimeoutNotifier implements SubscriberTimeoutNotifier {

    private Timer timer = new Timer("subscriber-timeouts");
    private Map<Subscriber, SubscriberTimeoutTask> tasks = new ConcurrentHashMap<Subscriber, SubscriberTimeoutTask>();
    
    public void addSubscriber(Subscriber subscriber) {
        if(subscriber.getTimeout() != null) {
            SubscriberTimeoutTask task = new SubscriberTimeoutTask(subscriber);
            tasks.put(subscriber, task);
            
            timer.schedule(task, subscriber.getTimeout().toDate());
        }
    }

    public void removeSubscriber(Subscriber subscriber) {
        SubscriberTimeoutTask task = tasks.remove(subscriber);
        task.cancel();
    }
    
    public static class SubscriberTimeoutTask extends TimerTask {
        private Subscriber subscriber;
        
        public SubscriberTimeoutTask(Subscriber subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void run() {
            subscriber.timedOut();
        }
    }
}
