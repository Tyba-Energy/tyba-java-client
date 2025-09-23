package com.tybaenergy.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Schema for node metadata
 */
public class NodeData {
    /**
     * Name of the node
     */
    @JsonProperty("name")
    private String name;
    
    /**
     * ID of the node
     */
    @JsonProperty("id")
    private String id;
    
    /**
     * Zone where the node is located within the ISO territory
     */
    @JsonProperty("zone")
    private String zone;
    
    /**
     * Identifier that indicates physical infrastructure associated with this node
     */
    @JsonProperty("type")
    private NodeType type;
    
    /**
     * First year in the Day Ahead (DA) market price dataset for this node
     */
    @JsonProperty("da_start_year")
    private Double daStartYear;
    
    /**
     * Final year in the Day Ahead (DA) market price dataset for this node
     */
    @JsonProperty("da_end_year")
    private Double daEndYear;
    
    /**
     * First year in the Real Time (RT) market price dataset for this node
     */
    @JsonProperty("rt_start_year")
    private Integer rtStartYear;
    
    /**
     * Final year in the Real Time (RT) market price dataset for this node
     */
    @JsonProperty("rt_end_year")
    private Integer rtEndYear;
    
    /**
     * Indicator of the grid substation associated with this node (not always present)
     */
    @JsonProperty("substation")
    private String substation;

    // Constructors
    public NodeData() {}

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public NodeType getType() { return type; }
    public void setType(NodeType type) { this.type = type; }

    public Double getDaStartYear() { return daStartYear; }
    public void setDaStartYear(Double daStartYear) { this.daStartYear = daStartYear; }

    public Double getDaEndYear() { return daEndYear; }
    public void setDaEndYear(Double daEndYear) { this.daEndYear = daEndYear; }

    public Integer getRtStartYear() { return rtStartYear; }
    public void setRtStartYear(Integer rtStartYear) { this.rtStartYear = rtStartYear; }

    public Integer getRtEndYear() { return rtEndYear; }
    public void setRtEndYear(Integer rtEndYear) { this.rtEndYear = rtEndYear; }

    public String getSubstation() { return substation; }
    public void setSubstation(String substation) { this.substation = substation; }
}