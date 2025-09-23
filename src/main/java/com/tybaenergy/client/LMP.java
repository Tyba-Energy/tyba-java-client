package com.tybaenergy.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tybaenergy.client.model.Market;
import com.tybaenergy.client.model.NodeData;
import com.tybaenergy.client.model.NodeSearchData;
import com.tybaenergy.client.model.PriceTimeSeries;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface for accessing Tyba's historical energy price data
 */
public class LMP {
    private final Services services;
    private static final String ROUTE_BASE = "lmp";

    public LMP(Services services) {
        this.services = services;
    }

    /**
     * Make a GET request to an LMP endpoint
     */
    private Response get(String route) throws IOException {
        return services.get(ROUTE_BASE + "/" + route);
    }

    /**
     * Make a GET request to an LMP endpoint with parameters
     */
    private Response get(String route, Map<String, Object> params) throws IOException {
        return services.get(ROUTE_BASE + "/" + route, params);
    }

    /**
     * Make a POST request to an LMP endpoint
     */
    private Response post(String route, Object jsonPayload) throws IOException {
        return services.post(ROUTE_BASE + "/" + route, jsonPayload);
    }

    /**
     * Get node names, IDs and other metadata for all nodes within the given ISO territory.
     *
     * @param iso ISO name. Possible values can be found by calling Services.getAllIsos()
     * @return List of NodeData objects containing node metadata
     * @throws IOException if the request fails
     */
    public List<NodeData> getAllNodes(String iso) throws IOException {
        Map<String, Object> params = TybaClient.params("iso", iso);
        Response response = get("nodes", params);
        return services.client.parseResponseList(response, NodeData.class);
    }

    /**
     * Get price time series data for a list of node IDs
     *
     * @param nodeIds list of IDs for which prices are desired (Maximum length is 8 IDS)
     * @param market specifies whether to pull day ahead or real time market prices
     * @param startYear the year prices should start
     * @param endYear the year prices should end
     * @return Map where keys are node IDs and values are PriceTimeSeries objects
     * @throws IOException if the request fails
     */
    public Map<String, PriceTimeSeries> getPrices(List<String> nodeIds, Market market, 
                                                 int startYear, int endYear) throws IOException {
        if (nodeIds.size() > 8) {
            throw new IllegalArgumentException("Maximum of 8 node IDs allowed");
        }
        
        Map<String, Object> params = TybaClient.params();
        params.put("node_ids", String.join(",", nodeIds));  // Convert list to comma-separated string
        params.put("market", market.getValue());
        params.put("start_year", startYear);
        params.put("end_year", endYear);
        
        Response response = get("prices", params);
        
        if (!response.isSuccessful()) {
            throw new IOException("Request failed with code: " + response.code() + ", message: " + response.message());
        }
        
        try (ResponseBody responseBody = response.body()) {
            if (responseBody == null) {
                throw new IOException("Empty response body");
            }
            
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<Map<String, PriceTimeSeries>> typeRef = new TypeReference<Map<String, PriceTimeSeries>>() {};
            return mapper.readValue(responseBody.string(), typeRef);
        }
    }

    /**
     * Get a list of matching nodes based on search criteria. Multiple search criteria can be applied in a single request.
     *
     * @param location location information. There are 3 possible forms:
     *                - city/state, e.g. 'dallas, tx'
     *                - address, e.g. '12345 Anywhere Street, Anywhere, TX 12345'
     *                - latitude and longitude, e.g. '29.760427, -95.369804'
     * @param nodeNameFilter partial node name with which to perform a pattern-match, e.g. 'HB_'
     * @param isoOverride ISO signifier, used to constrain search to a single ISO. When null, all ISOs
     *                   are searched based on other criteria. Possible values can be found by calling Services.getAllIsos()
     * @return List of NodeSearchData objects containing matching nodes
     * @throws IOException if the request fails
     */
    public List<NodeSearchData> searchNodes(String location, String nodeNameFilter, String isoOverride) throws IOException {
        Map<String, Object> params = TybaClient.params();
        if (location != null) params.put("location", location);
        if (nodeNameFilter != null) params.put("node_name_filter", nodeNameFilter);
        if (isoOverride != null) params.put("iso_override", isoOverride);
        
        Response response = get("search-nodes", params);
        
        if (!response.isSuccessful()) {
            throw new IOException("Request failed with code: " + response.code() + ", message: " + response.message());
        }
        
        try (ResponseBody responseBody = response.body()) {
            if (responseBody == null) {
                throw new IOException("Empty response body");
            }
            
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.readValue(responseBody.string(), Map.class);
            
            if (result.containsKey("nodes")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> nodesList = (List<Map<String, Object>>) result.get("nodes");
                return nodesList.stream()
                        .map(nodeMap -> {
                            NodeSearchData node = new NodeSearchData();
                            node.setNodeName((String) nodeMap.get("node/name"));
                            node.setNodeId((String) nodeMap.get("node/id"));
                            node.setNodeIso((String) nodeMap.get("node/iso"));
                            if (nodeMap.get("node/lat") instanceof Number) {
                                node.setNodeLatitude(((Number) nodeMap.get("node/lat")).doubleValue());
                            }
                            if (nodeMap.get("node/lng") instanceof Number) {
                                node.setNodeLongitude(((Number) nodeMap.get("node/lng")).doubleValue());
                            }
                            if (nodeMap.get("node/distance-meters") instanceof Number) {
                                node.setNodeDistanceMeters(((Number) nodeMap.get("node/distance-meters")).doubleValue());
                            }
                            return node;
                        })
                        .toList();
            } else {
                throw new IOException("No nodes found or error in response");
            }
        }
    }

    /**
     * Convenience method to search nodes by location only
     */
    public List<NodeSearchData> searchNodesByLocation(String location) throws IOException {
        return searchNodes(location, null, null);
    }

    /**
     * Convenience method to search nodes by name filter only
     */
    public List<NodeSearchData> searchNodesByName(String nodeNameFilter) throws IOException {
        return searchNodes(null, nodeNameFilter, null);
    }
}