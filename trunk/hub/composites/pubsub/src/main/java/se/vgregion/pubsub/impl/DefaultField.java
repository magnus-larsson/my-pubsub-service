package se.vgregion.pubsub.impl;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.Field;
import se.vgregion.pubsub.FieldType;

public class DefaultField extends AbstractEntity<Field, Long> implements Field {

    private Long id;
    private String namespace;
    private String name;
    private FieldType type;
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
