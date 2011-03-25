package se.vgregion.pubsub;

import java.util.List;

public interface Field {

    String getNamespace();
    String getName();
    String getContent();
    
    List<Field> getFields();
    
}
