package se.vgregion.pubsub.impl;

import se.vgregion.pubsub.Feed;

public interface FeedMerger {

    Feed merge(Feed oldFeed, Feed newFeed);
}
