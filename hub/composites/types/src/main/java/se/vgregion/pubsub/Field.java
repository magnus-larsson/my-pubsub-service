package se.vgregion.pubsub;

import se.vgregion.dao.domain.patterns.entity.Entity;

public interface Field extends Entity<Field, Long> {

    String getNamespace();
    String getName();
    FieldType getType();
    String getValue();
    
}
