package se.vgregion.pubsub.impl;

import java.io.StringReader;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import se.vgregion.pubsub.Field;

public class XmlUtil {

    private static final Builder PARSER = new Builder();;

    public static String xmlToString(Element elm) {
        // TODO ugly hack to retain namespaces
        return new Document((Element) elm.copy()).toXML().replaceFirst("<.+>", "");
    }

    public static String innerToString(Element elm) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i<elm.getChildCount(); i++) {
            Node child = elm.getChild(i);
            sb.append(child.toXML());
        }
        return sb.toString();
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

    public static Element fieldToXml(Field field) {
        try {
            StringBuilder xml = new StringBuilder();
            xml.append("<");
            xml.append(field.getName());
            if(field.getNamespace() != null) {
                xml.append(" xmlns='");
                xml.append(field.getNamespace());
            }
            xml.append("'>");
            xml.append(field.getContent());
            xml.append("</");
            xml.append(field.getName());
            xml.append(">");
            
            Document doc = PARSER.build(new StringReader(xml.toString()));
            Element elm = (Element) doc.getRootElement().copy();
            for(Field attrField : field.getFields()) {
                Attribute attr = new Attribute(attrField.getName(), attrField.getNamespace(), attrField.getContent());
                elm.addAttribute(attr);
            }
            
            return elm;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }

    
}
