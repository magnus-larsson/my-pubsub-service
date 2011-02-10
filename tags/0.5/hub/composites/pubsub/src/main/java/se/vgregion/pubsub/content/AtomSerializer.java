package se.vgregion.pubsub.content;

import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import se.vgregion.pubsub.Entry;
import se.vgregion.pubsub.Feed;
import se.vgregion.pubsub.Field;
import se.vgregion.pubsub.Namespaces;
import se.vgregion.pubsub.impl.XmlUtil;

public class AtomSerializer extends AbstractSerializer {

    public Document print(Feed feed, EntryFilter entryFilter) {
        Element feedElm = new Element("feed", Namespaces.ATOM);
        
        List<Field> fields = feed.getFields();
        
        for(Field field : fields) {
            feedElm.appendChild(XmlUtil.fieldToXml(field));
        }
        
        for(Entry entry : feed.getEntries()) {
            if(entryFilter == null || entryFilter.include(entry)) {
                feedElm.appendChild(print(entry));
            }
        }
        
        return new Document(feedElm);
    }
    
    private Element print(Entry entry) {
        Element entryElm = new Element("entry", Namespaces.ATOM);
        
        List<Field> fields = entry.getFields();
        
        for(Field field : fields) {
            entryElm.appendChild(XmlUtil.fieldToXml(field));
        }
        
        return entryElm;

    }
}
