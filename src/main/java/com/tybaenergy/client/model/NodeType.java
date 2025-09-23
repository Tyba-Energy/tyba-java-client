package com.tybaenergy.client.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Indicator of which type of physical infrastructure is associated with a particular market node
 */
public enum NodeType {
    GENERATOR("GENERATOR"),
    SPTIE("SPTIE"),
    LOAD("LOAD"),
    INTERTIE("INTERTIE"),
    AGGREGATE("AGGREGATE"),
    HUB("HUB"),
    NA("N/A");

    private final String value;

    NodeType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}