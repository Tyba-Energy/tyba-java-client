package com.tybaenergy.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Schema for ancillary region metadata
 */
public class AncillaryRegionData {
    /**
     * Name of the region
     */
    @JsonProperty("region")
    private String region;
    
    /**
     * First year in price dataset for the region and specified service
     */
    @JsonProperty("start_year")
    private Integer startYear;
    
    /**
     * Final year in price dataset for the region and specified service
     */
    @JsonProperty("end_year")
    private Integer endYear;

    // Constructors
    public AncillaryRegionData() {}

    public AncillaryRegionData(String region, Integer startYear, Integer endYear) {
        this.region = region;
        this.startYear = startYear;
        this.endYear = endYear;
    }

    // Getters and setters
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Integer getStartYear() { return startYear; }
    public void setStartYear(Integer startYear) { this.startYear = startYear; }

    public Integer getEndYear() { return endYear; }
    public void setEndYear(Integer endYear) { this.endYear = endYear; }
}