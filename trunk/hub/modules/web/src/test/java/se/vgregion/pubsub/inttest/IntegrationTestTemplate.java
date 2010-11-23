package se.vgregion.pubsub.inttest;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;
import org.junit.Before;

import se.vgregion.pubsub.Feed;


public class IntegrationTestTemplate {

    private Server server;
    protected BlockingQueue<Boolean> verifications = new LinkedBlockingQueue<Boolean>();
    protected BlockingQueue<Feed> publishedFeeds = new LinkedBlockingQueue<Feed>();
    protected Publisher publisher;
    protected URI hubUrl;
    
    @Before
    public void setUpComponents() throws Exception {
        server = new Server(0);
        
        WebAppContext context = new WebAppContext();
        context.setDescriptor("src/main/webapp/WEB-INF/web.xml");
        context.setResourceBase("src/main/webapp");
        context.setContextPath("/");
 
        server.setHandler(context);
        
        server.start();
        
        hubUrl = URI.create("http://localhost:" + server.getConnectors()[0].getLocalPort());
        
        publisher = new Publisher();
        
        Subscriber subscriber = new Subscriber(createSubscriberResult());
        subscriber.addListener(new SubscriberListener() {
            @Override
            public void published(Feed feed) {
                publishedFeeds.add(feed);
            }

            @Override
            public void verified() {
                verifications.add(true);                
            }
        });
        
        subscriber.subscribe(hubUrl, publisher.getUrl());
        
    }
    
    protected SubscriberResult createSubscriberResult() {
        return null;
    }
    
    @After
    public void stopServer() throws Exception {
        server.stop();
    }
}
