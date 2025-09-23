package com.tybaenergy.client.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Indicator for which service to pull pricing data for
 */
public enum AncillaryService {
    /**
     * Indicates pricing data for the Regulation Up service is desired
     */
    REGULATION_UP("Regulation Up"),
    
    /**
     * Indicates pricing data for the Regulation Down service is desired
     */
    REGULATION_DOWN("Regulation Down"),
    
    /**
     * Indicates pricing data for the Reserves service is desired
     */
    RESERVES("Reserves"),
    
    /**
     * Indicates pricing data for the ERCOT Contingency Reserve Service is desired
     */
    ECRS("ECRS");

    private final String value;

    AncillaryService(String value) {
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