package se.vgregion.pubsub;

import se.vgregion.dao.domain.patterns.entity.Entity;

public interface Field extends Entity<String> {

    String getNamespace();
    String getName();
    String getContent();
    
}
