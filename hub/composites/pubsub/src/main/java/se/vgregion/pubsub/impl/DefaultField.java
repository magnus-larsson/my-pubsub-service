package se.vgregion.pubsub.impl;

import java.util.UUID;

import nu.xom.Element;
import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.Field;

public class DefaultField extends AbstractEntity<String> implements Field {

    private String id = UUID.randomUUID().toString();
    
    private String namespace;
    private String name;
    private String content;

    
    private static Element createElement(String namespace, String name, String value) {
        Element elm = new Element(name, namespace);
        elm.appendChild(value);
        return elm;
    }

    public DefaultField(String namespace, String name, String value) {
        this(createElement(namespace, name, value));
    }

    public DefaultField(Element elm) {
        this.name = elm.getLocalName();
        this.namespace = elm.getNamespaceURI();
        
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

}
