/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

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
    private String prefix;
    private String name;
    private String content;
    private List<Field> fields = new ArrayList<Field>();

    public DefaultField(String namespace, String prefix, String name, String value) {
        this.namespace = namespace;
        this.prefix = prefix;
        this.name = name;
        this.content = value;
    }

    public DefaultField(String namespace, String prefix, String name, String value, List<Field> fields) {
        this.namespace = namespace;
        this.prefix = prefix;
        this.name = name;
        this.content = value;
        this.fields = fields;
    }

    
    public DefaultField(Element elm) {
        this.name = elm.getLocalName();
        this.namespace = elm.getNamespaceURI();
        this.prefix = elm.getNamespacePrefix();
        
        for(int i = 0; i<elm.getAttributeCount(); i++) {
            Attribute attribute = elm.getAttribute(i);
            fields.add(new DefaultField(attribute.getNamespaceURI(), attribute.getNamespacePrefix(), attribute.getLocalName(), attribute.getValue()));
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
    public String getPrefix() {
    	return prefix;
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
