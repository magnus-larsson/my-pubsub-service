package se.vgregion.pubsub;


public interface FeedMerger {

    Feed merge(Feed oldFeed, Feed newFeed);
}
