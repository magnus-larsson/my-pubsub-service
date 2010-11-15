package se.vgregion.push.inttest;

import se.vgregion.push.types.Feed;

public interface PublicationListener {

    void published(Feed feed);
}
