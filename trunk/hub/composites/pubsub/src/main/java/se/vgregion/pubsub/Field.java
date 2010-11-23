package se.vgregion.pubsub;

import nu.xom.Element;
import se.vgregion.dao.domain.patterns.entity.Entity;

public interface Field extends Entity<Long> {

    Element toXml();
    
}
