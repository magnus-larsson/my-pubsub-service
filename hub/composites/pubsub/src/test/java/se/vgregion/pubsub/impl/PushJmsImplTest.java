package se.vgregion.pubsub.impl;

import junit.framework.Assert;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.MemoryUsage;
import org.apache.activemq.usage.StoreUsage;
import org.apache.activemq.usage.SystemUsage;
import org.apache.activemq.usage.TempUsage;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.vgregion.docpublishing.v1.DocumentStatusType;
import se.vgregion.pubsub.ContentType;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;

import javax.jms.*;

/**
 * Created with IntelliJ IDEA.
 * User: portaldev
 * Date: 2013-05-15
 * Time: 13:04
 * To change this template use File | Settings | File Templates.
 */
public class PushJmsImplTest {

    private BrokerService consumerBroker;

    public void testPushToJms() throws Exception {

    }


    /**
     * The URLs used by the producer and consumer to connect to their local
     * brokers
     */
    final static String PRODUCER_LOCAL_URL = "vm://producer", CONSUMER_LOCAL_URL = "vm://consumer";
    /**
     * The URL used by a remote broker to connect to the consumer broker
     */
    final static String CONSUMER_REMOTE_URL = "tcp://localhost:12345";
    //final static String CONSUMER_REMOTE_URL = "failover:(tcp://vgms0141:61616)";


    //final static String DEV_CONSUMER_REMOTE_URL = "failover:(tcp://vgms0141:61616)";

    final static String QUEUE_NAME = "DOCPUBLISHING.PUBLISHINGSTATUS.IN";

    final static String SENDER_ID = "Alfresco";
    final static String RECEIVER_ID = "Docpublishing";
    final static String MESSAGE_TYPE_VERSION = "1.0";

    Mockery context;

    private static final String WORKSPACE_AND_STORE = "workspace://SpacesStore/";
    private static final String DUMMY_NODE_ID_PUBLISH = "cafebabe-cafe-babe-cafe-babecafebab1";
    private static final String DUMMY_NODE_ID_UNPUBLISH = "cafebabe-cafe-babe-cafe-babecafebab2";

    /**
     * Setup the producer and consumer broker
     *
     * @throws Exception
     */
    @Before
    public void setup() throws Exception {

        context = new JUnit4Mockery() {
            {
                setThreadingPolicy(new Synchroniser());
            }
        };

        /*
        context.checking(new Expectations() {
            {
                // siteService.getSite
                allowing(nodeService).getProperty(with(any(NodeRef.class)), with(equal(VgrModel.PROP_SOURCE_ORIGIN)));
                will(returnValue("Alfresco"));
                allowing(nodeService).getProperty(with(any(NodeRef.class)), with(equal(VgrModel.PROP_SOURCE_DOCUMENTID)));
                will(returnValue(WORKSPACE_AND_STORE + DUMMY_NODE_ID_PUBLISH));
            }
        });*/
        //this.logger.info("Starting consumer broker");

        // Set memory usage, should be configured in activemq.xml
        // <systemUsage>
        // <systemUsage>
        // <memoryUsage>
        // <memoryUsage limit="20 mb"/>
        // </memoryUsage>
        // <storeUsage>
        // <storeUsage limit="1 gb"/>
        // </storeUsage>
        // <tempUsage>
        // <tempUsage limit="100 mb"/>
        // </tempUsage>
        // </systemUsage>
        // </systemUsage>
        SystemUsage memoryManager = new SystemUsage();

        MemoryUsage memoryUsage = new MemoryUsage();
        memoryUsage.setLimit(20 * 1024 * 1024); // 20mb
        memoryManager.setMemoryUsage(memoryUsage);

        StoreUsage storeUsage = new StoreUsage();
        storeUsage.setLimit(500 * 1024 * 1024); // 500mb
        memoryManager.setStoreUsage(storeUsage);

        TempUsage tempUsage = new TempUsage();
        tempUsage.setLimit(100 * 1024 * 1024); // 100mb
        memoryManager.setTempUsage(tempUsage);

        this.consumerBroker = new BrokerService();

        this.consumerBroker.setSystemUsage(memoryManager);

        this.consumerBroker.setBrokerName("consumer");
        this.consumerBroker.addConnector(CONSUMER_LOCAL_URL);
    /* Explicitly add the remote URL so the broker is reachable via TCP */
        this.consumerBroker.addConnector(CONSUMER_REMOTE_URL);
        this.consumerBroker.start();
    }

    @After
    public void tearDown() throws Exception {
        this.consumerBroker.stop();
    }

    @Test
    public void testLocal() throws Exception {

        class ConsumerThread extends Thread {

            //final Logger LOG = Logger.getLogger(ConsumerThread.class);
            /**
             * Since threads cannot throw exceptions (including AssertionError) use
             * this flag to determine if the test was successful
             */
            public boolean successPublish = false;
            public boolean successUnpublish = false;

            @Override
            public void run() {
                try {
                    ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(CONSUMER_LOCAL_URL);
                    Connection connection = connectionFactory.createConnection();
                    connection.start();

                    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    Destination destination = session.createQueue(QUEUE_NAME);

                    MessageConsumer consumer = session.createConsumer(destination);

                    for (int i = 0; i < 2; i++) {
                        System.out.println(i);
                        Message message = consumer.receive();

                        Assert.assertNotNull("Did not receive message in time", message);
                        Assert.assertTrue(message instanceof TextMessage);
                        TextMessage textMessage = (TextMessage) message;
                        String result = textMessage.getText();
                        Assert.assertTrue(result.length() > 0);

                        if (result.indexOf("<PublishDocument") > 0) {
                            successPublish = true;
                        } else if (result.indexOf("<UnpublishDocument") > 0) {
                            successUnpublish = true;
                        }
                        System.out.println(i + " " + message);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        class ProducerThread extends Thread {

            //final Logger LOG = Logger.getLogger(ProducerThread.class);

            @Override
            public void run() {
                PushJmsImpl pushJmsService = new PushJmsImpl();
                pushJmsService.setQueueName(QUEUE_NAME);
                /*pushJmsService.setMemoryUsageLimit(20 * 1024 * 1024);
                pushJmsService.setStoreUsageLimit(500 * 1024 * 1024);
                pushJmsService.setTempUsageLimit(100 * 1024 * 1024);*/
                pushJmsService.setProducerLocalUrl(PRODUCER_LOCAL_URL);
                pushJmsService.setVgrHdrSenderId(SENDER_ID);
                pushJmsService.setVgrHdrReceiverId(RECEIVER_ID);
                pushJmsService.setConsumerRemoteUrl("failover:(" + CONSUMER_REMOTE_URL + ")");

                pushJmsService.setVgrHdrMessageTypeVersion(MESSAGE_TYPE_VERSION);
                /*
                try {
                    pushJmsService.afterPropertiesSet();
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.assertTrue(false);
                }
                */
                // Send some messages
                // Send publish event
                /*
                List<NodeRef> list = new ArrayList<NodeRef>();
                list.add(new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_PUBLISH));
                pushJmsService.send(list, VgrModel.PROP_PUSHED_FOR_PUBLISH);*/
                // Send unpublish event
                /*
                list = new ArrayList<NodeRef>();
                list.add(new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_UNPUBLISH));*/
                //pushJmsService.send(list, VgrModel.PROP_PUSHED_FOR_UNPUBLISH);


                DefaultEntry.EntryBuilder entryBuilder = new DefaultEntry.EntryBuilder();
                Entry entry = entryBuilder.id("id").field("published", "true").field("requestId", "foo").build();

                DefaultFeed.FeedBuilder feedBuilder = new DefaultFeed.FeedBuilder(ContentType.ATOM);
                Feed feed = feedBuilder.entry(entry).build();

                pushJmsService.send(feed, "system-message", DocumentStatusType.OK);
            }
        }

        ConsumerThread consumer = new ConsumerThread();
        consumer.start();

        Thread producer = new ProducerThread();
        producer.start();
        producer.join();

        consumer.join(5000);

        //Assert.assertTrue("Test failed", consumer.successPublish);
        //Assert.assertTrue("Test failed", consumer.successUnpublish);
    }
/*
 * Uncomment to test against dev jms queue
  @Test
  public void testRemoteDev() throws Exception {
    PushJmsServiceImpl pushJmsService = new PushJmsServiceImpl();
    pushJmsService.setQueueName(QUEUE_NAME);
    pushJmsService.setMemoryUsageLimit(20 * 1024 * 1024);
    pushJmsService.setStoreUsageLimit(500 * 1024 * 1024);
    pushJmsService.setTempUsageLimit(100 * 1024 * 1024);
    pushJmsService.setProducerLocalUrl(PRODUCER_LOCAL_URL);
    pushJmsService.setVgrHdrSenderId(SENDER_ID);
    pushJmsService.setVgrHdrReceiverId(RECEIVER_ID);
    pushJmsService.setConsumerRemoteUrl(DEV_CONSUMER_REMOTE_URL);
    pushJmsService.setNodeService(nodeService);
    pushJmsService.setVgrHdrMessageTypeVersion(MESSAGE_TYPE_VERSION);

    try {
      pushJmsService.afterPropertiesSet();
    } catch (Exception e) {
      e.printStackTrace();
      Assert.assertTrue(false);
    }
    // Send some messages
    // Send publish event
    List<NodeRef> list = new ArrayList<NodeRef>();
    list.add(new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_PUBLISH));
    pushJmsService.send(list, VgrModel.PROP_PUSHED_FOR_PUBLISH);
    // Send unpublish event
    list = new ArrayList<NodeRef>();
    list.add(new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_UNPUBLISH));
    pushJmsService.send(list, VgrModel.PROP_PUSHED_FOR_UNPUBLISH);
  }
  */


}
