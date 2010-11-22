package se.vgregion.pubsub.impl;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.Field;
import se.vgregion.pubsub.FieldType;

@Entity
@Table(name="FIELDS")
public class DefaultField extends AbstractEntity<Long> implements Field {

    @Id
    @GeneratedValue
    private Long id;
    
    @Basic
    private String namespace;
    
    @Basic(optional=false)
    private String name;
    
    @Basic(optional=false)
    private FieldType type;
    
    @Basic(optional=false)
    private String value;

    public DefaultField(String namespace, String name, String value) {
        this(namespace, name, FieldType.ELEMENT, value);
    }

    public DefaultField(String namespace, String name, FieldType type, String value) {
        this.namespace = namespace;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public Long getId() {
        return id;
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
    public FieldType getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }
}
