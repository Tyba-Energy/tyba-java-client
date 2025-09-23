package com.tybaenergy.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Schema for search-specific node metadata
 */
public class NodeSearchData {
    /**
     * Name of the node
     */
    @JsonProperty("node/name")
    private String nodeName;
    
    /**
     * ID of the node
     */
    @JsonProperty("node/id")
    private String nodeId;
    
    /**
     * ISO that the node belongs to
     */
    @JsonProperty("node/iso")
    private String nodeIso;
    
    /**
     * Longitude of the point on the electrical grid associated with the node
     */
    @JsonProperty("node/lng")
    private Double nodeLongitude;
    
    /**
     * Latitude of the point on the electrical grid associated with the node
     */
    @JsonProperty("node/lat")
    private Double nodeLatitude;
    
    /**
     * Distance from the node to the location parameter passed to search_nodes. Not present if location is not given.
     */
    @JsonProperty("node/distance-meters")
    private Double nodeDistanceMeters;

    // Constructors
    public NodeSearchData() {}

    // Getters and setters
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public String getNodeIso() { return nodeIso; }
    public void setNodeIso(String nodeIso) { this.nodeIso = nodeIso; }

    public Double getNodeLongitude() { return nodeLongitude; }
    public void setNodeLongitude(Double nodeLongitude) { this.nodeLongitude = nodeLongitude; }

    public Double getNodeLatitude() { return nodeLatitude; }
    public void setNodeLatitude(Double nodeLatitude) { this.nodeLatitude = nodeLatitude; }

    public Double getNodeDistanceMeters() { return nodeDistanceMeters; }
    public void setNodeDistanceMeters(Double nodeDistanceMeters) { this.nodeDistanceMeters = nodeDistanceMeters; }
}