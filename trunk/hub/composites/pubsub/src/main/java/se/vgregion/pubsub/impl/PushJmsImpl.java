package se.vgregion.pubsub.impl;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.collections.BeanMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.vgregion.docpublishing.documentstatusevent.v1.DocumentStatus;
import se.vgregion.docpublishing.documentstatusevent.v1.ObjectFactory;
import se.vgregion.docpublishing.v1.DocumentStatusType;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.Field;
import se.vgregion.pubsub.PushJms;

import javax.jms.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: portaldev
 * Date: 2013-05-15
 * Time: 11:06
 * To change this template use File | Settings | File Templates.
 */
public class PushJmsImpl implements PushJms {

    private final static Logger LOG = LoggerFactory.getLogger(PushJmsImpl.class);

    private String queueName;
    private String consumerRemoteUrl;
    private String producerLocalUrl;

    private String vgrHdrSenderId;
    private String vgrHdrReceiverId;
    private String vgrHdrMessageTypeVersion;

    // Public for testing
    private static final String VGR_HDR_SENDER_ID = "vgrHdr_senderId";
    private static final String VGR_HDR_RECEIVER_ID = "vgrHdr_receiverId";
    private static final String VGR_HDR_MESSAGE_TYPE = "vgrHdr_messageType";
    private static final String PUBLISH_DOCUMENT_EVENT = "PublishDocumentEvent";
    private static final String UNPUBLISH_DOCUMENT_EVENT = "UnpublishDocumentEvent";
    private static final String DOCUMENT_STATUS_EVENT = "DocumentStatusEvent";
    private static final String VGR_HDR_MESSAGE_TYPE_VERSION = "vgrHdr_messageTypeVersion";

    private static final se.vgregion.docpublishing.documentstatusevent.v1.ObjectFactory documentStatusObjectFactory = new se.vgregion.docpublishing.documentstatusevent.v1.ObjectFactory();
    private static final se.vgregion.docpublishing.publishdocumentevent.v1.ObjectFactory documentPublishObjectFactory = new se.vgregion.docpublishing.publishdocumentevent.v1.ObjectFactory();
    private static final se.vgregion.docpublishing.unpublishdocumentevent.v1.ObjectFactory documentUnPublishObjectFactory = new se.vgregion.docpublishing.unpublishdocumentevent.v1.ObjectFactory();
    private static final se.vgregion.docpublishing.v1.ObjectFactory documentStatusTypeObjectFactory = new se.vgregion.docpublishing.v1.ObjectFactory();

    public static String getVgrHdrMessageType() {
        return VGR_HDR_MESSAGE_TYPE;
    }

    public static String getPublishDocumentEvent() {
        return PUBLISH_DOCUMENT_EVENT;
    }

    public static String getUnpublishDocumentEvent() {
        return UNPUBLISH_DOCUMENT_EVENT;
    }

    public static String getDocumentStatusEvent() {
        return DOCUMENT_STATUS_EVENT;
    }

    public static ObjectFactory getDocumentStatusObjectFactory() {
        return documentStatusObjectFactory;
    }

    public static se.vgregion.docpublishing.publishdocumentevent.v1.ObjectFactory getDocumentPublishObjectFactory() {
        return documentPublishObjectFactory;
    }

    public static se.vgregion.docpublishing.unpublishdocumentevent.v1.ObjectFactory getDocumentUnPublishObjectFactory() {
        return documentUnPublishObjectFactory;
    }

    public static se.vgregion.docpublishing.v1.ObjectFactory getDocumentStatusTypeObjectFactory() {
        return documentStatusTypeObjectFactory;
    }

    private String ObjectToXml(Object object) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(object.getClass());

        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        OutputStream os = new ByteArrayOutputStream();

        m.marshal(object, os);

        return os.toString();
    }

    private Field getFieldFromEntry(Entry entries, String key) {
        for (Field field : entries.getFields()) {
            if (key.equals(field.getName())) {
                return field;
            }
        }
        return null;
    }

    private String getValueFromEntry(Entry entry, String key) {
        Field field = getFieldFromEntry(entry, key);
        if (field != null) return field.getContent();
        return null;
    }


    ConnectionFactory connectionFactory;
    Connection connection;
    Session session;
    Destination destination;
    MessageProducer producer;

    private void start() {
        try {
            connectionFactory = new ActiveMQConnectionFactory(consumerRemoteUrl);
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue(queueName);
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void stop() {
        try {
            producer.close();
            session.close();
            connection.close();

            producer = null;
            session = null;
            connection = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean send(Feed feed, String systemMessage, DocumentStatusType type) {
        try {
            start();
            for (Entry entry : feed.getEntries()) {
                TextMessage message = null;

                DocumentStatus documentStatus = documentStatusObjectFactory.createDocumentStatus();

                documentStatus.setRequestId(getValueFromEntry(entry, "requestId"));
                documentStatus.setDocumentId(getValueFromEntry(entry, "identifier.documentid"));

                documentStatus.setStatus(type);

                if (documentStatus.getRequestId() == null || "".equals(documentStatus.getRequestId())) {
                    documentStatus.setStatus(DocumentStatusType.ERROR);
                    systemMessage = "DocumentStatus is missing. " + systemMessage;
                    documentStatus.setRequestId(documentStatus.getDocumentId());
                }

                documentStatus.setIsEnd(false);
                documentStatus.setStatusSource(systemMessage);

                message = session.createTextMessage(ObjectToXml(documentStatus));
                message.setStringProperty(VGR_HDR_SENDER_ID, vgrHdrSenderId);
                message.setStringProperty(VGR_HDR_RECEIVER_ID, vgrHdrReceiverId);


                message.setStringProperty(VGR_HDR_MESSAGE_TYPE, "DocumentStatusEvent");

                message.setStringProperty(VGR_HDR_MESSAGE_TYPE_VERSION, "1.0");
                message.setJMSCorrelationID(documentStatus.getRequestId());

                LOG.info("Sends jms message for " + new TreeMap(new BeanMap(documentStatus)));
                producer.send(destination, message);
                LOG.info("After sends jms message for " + new TreeMap(new BeanMap(documentStatus)));
            }
            stop();
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getConsumerRemoteUrl() {
        return consumerRemoteUrl;
    }

    public void setConsumerRemoteUrl(String consumerRemoteUrl) {
        this.consumerRemoteUrl = consumerRemoteUrl;
    }

    public String getProducerLocalUrl() {
        return producerLocalUrl;
    }

    public void setProducerLocalUrl(String producerLocalUrl) {
        this.producerLocalUrl = producerLocalUrl;
    }

    public String getVgrHdrSenderId() {
        return vgrHdrSenderId;
    }

    public void setVgrHdrSenderId(String vgrHdrSenderId) {
        this.vgrHdrSenderId = vgrHdrSenderId;
    }

    public String getVgrHdrReceiverId() {
        return vgrHdrReceiverId;
    }

    public void setVgrHdrReceiverId(String vgrHdrReceiverId) {
        this.vgrHdrReceiverId = vgrHdrReceiverId;
    }

    public String getVgrHdrMessageTypeVersion() {
        return vgrHdrMessageTypeVersion;
    }

    public void setVgrHdrMessageTypeVersion(String vgrHdrMessageTypeVersion) {
        this.vgrHdrMessageTypeVersion = vgrHdrMessageTypeVersion;
    }


    public static void main(String[] args) {
        PushJmsImpl instance = new PushJmsImpl();
        BeanMap bm = new BeanMap(instance);

        StringBuilder sb = new StringBuilder();
        for (Object key : bm.keySet()) {
            if ("class".equals(key)) continue;
            sb.append("<property name=\"" + key + "\" value=\"${" + key + "}\"/>\n");
        }
        System.out.println(sb + "\n");

        sb = new StringBuilder();
        for (Object key : bm.keySet()) {
            if ("class".equals(key)) continue;
            sb.append(key + "=" + key + "\n");
        }
        System.out.println(sb);
    }


}
