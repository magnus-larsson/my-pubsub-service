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
