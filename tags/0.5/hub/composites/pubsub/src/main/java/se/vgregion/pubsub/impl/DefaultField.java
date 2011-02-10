package se.vgregion.pubsub.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import nu.xom.Attribute;
import nu.xom.Element;
import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.Field;

public class DefaultField extends AbstractEntity<String> implements Field {

    private String id = UUID.randomUUID().toString();
    
    private String namespace;
    private String name;
    private String content;
    private List<Field> fields = new ArrayList<Field>();

    public DefaultField(String namespace, String name, String value) {
        this.namespace = namespace;
        this.name = name;
        this.content = value;
    }

    public DefaultField(String namespace, String name, String value, List<Field> fields) {
        this.namespace = namespace;
        this.name = name;
        this.content = value;
        this.fields = fields;
    }

    
    public DefaultField(Element elm) {
        this.name = elm.getLocalName();
        this.namespace = elm.getNamespaceURI();
        
        for(int i = 0; i<elm.getAttributeCount(); i++) {
            Attribute attribute = elm.getAttribute(i);
            fields.add(new DefaultField(attribute.getNamespaceURI(), attribute.getLocalName(), attribute.getValue()));
        }
        
        this.content = XmlUtil.innerToString(elm);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Field> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(namespace != null && namespace.length() > 0) {
            sb.append("{");
            sb.append(namespace);
            sb.append("}");
        }
        sb.append(name);
        sb.append("=");
        sb.append(content);
        
        return sb.toString();
    }

    
}
