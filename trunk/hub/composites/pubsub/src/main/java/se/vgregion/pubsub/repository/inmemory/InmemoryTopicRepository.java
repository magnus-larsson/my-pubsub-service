package se.vgregion.pubsub.repository.inmemory;

import java.net.URI;
import java.util.Collection;

import se.vgregion.dao.domain.patterns.repository.inmemory.AbstractInMemoryRepository;
import se.vgregion.pubsub.Topic;
import se.vgregion.pubsub.repository.TopicRepository;

public class InmemoryTopicRepository extends AbstractInMemoryRepository<Topic, Long> implements TopicRepository {

    public Topic findByUrl(URI url) {
        Collection<Topic> topics = findAll();
        for(Topic topic : topics) {
            if(topic.getUrl().equals(url)) return topic;
        }
        
        return null;
    }


}
