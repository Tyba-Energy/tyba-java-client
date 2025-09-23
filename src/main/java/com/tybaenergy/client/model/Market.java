package com.tybaenergy.client.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Indicator for which market to pull pricing data for
 */
public enum Market {
    /**
     * Indicates pricing data for the Real Time (RT) Market is desired
     */
    REALTIME("realtime"),
    
    /**
     * Indicates pricing data for the Day Ahead (DA) Market is desired
     */
    DAYAHEAD("dayahead");

    private final String value;

    Market(String value) {
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