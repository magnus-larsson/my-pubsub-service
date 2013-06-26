package se.vgregion.pubsub;

/**
 * Created with IntelliJ IDEA.
 * User: portaldev
 * Date: 2013-05-15
 * Time: 11:04
 * To change this template use File | Settings | File Templates.
 */

import se.vgregion.docpublishing.v1.DocumentStatusType;

/**
 * Service interface for sending push messages to Mule
 * @author Marcus Svensson <marcus.svensson (at) redpill-linpro.com>
 *
 */
public interface PushJms {

    /**
     * Push documents to Mule JMS queue for status updates
     * @return
     */
    public boolean send(Feed feed, String systemMessage, DocumentStatusType type);

    /**
     * Create a
     * @param newConsumerLocation
     * @return
     */
    public PushJms copy(String newConsumerLocation);

}