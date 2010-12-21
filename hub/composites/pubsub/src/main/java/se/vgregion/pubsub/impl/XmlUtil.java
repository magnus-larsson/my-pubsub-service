package se.vgregion.pubsub.impl;

import java.io.StringReader;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

public class XmlUtil {

    private static final Builder PARSER = new Builder();;

    public static String xmlToString(Element elm) {
        // TODO ugly hack to retain namespaces
        return new Document((Element) elm.copy()).toXML().replaceFirst("<.+>", "");
    }

    public static Element stringToXml(String xml) {
        if(xml == null) return null;
        
        try {
            Document doc = PARSER.build(new StringReader(xml));
            Element elm = doc.getRootElement();
            return (Element) elm.copy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }

}
