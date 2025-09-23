package com.tybaenergy.client;

import okhttp3.Response;

import java.io.IOException;
import java.util.List;

/**
 * Interface for accessing Tyba's historical price data
 */
public class Services {
    final TybaClient client;
    private final Ancillary ancillary;
    private final LMP lmp;
    private static final String ROUTE_BASE = "services";

    public Services(TybaClient client) {
        this.client = client;
        this.ancillary = new Ancillary(this);
        this.lmp = new LMP(this);
    }

    /**
     * Interface for accessing Tyba's historical ancillary price data
     */
    public Ancillary getAncillary() {
        return ancillary;
    }

    /**
     * Interface for accessing Tyba's historical energy price data
     */
    public LMP getLmp() {
        return lmp;
    }

    /**
     * Make a GET request to a services endpoint
     */
    Response get(String route) throws IOException {
        return client.get(ROUTE_BASE + "/" + route);
    }

    /**
     * Make a GET request to a services endpoint with parameters
     */
    Response get(String route, java.util.Map<String, Object> params) throws IOException {
        return client.get(ROUTE_BASE + "/" + route, params);
    }

    /**
     * Make a POST request to a services endpoint
     */
    Response post(String route, Object jsonPayload) throws IOException {
        return client.post(ROUTE_BASE + "/" + route, jsonPayload);
    }

    /**
     * Get list of all independent system operators and regional transmission operators
     * (generally all referred to as ISOs) represented in Tyba's historical price data
     *
     * @return List of available ISO names
     * @throws IOException if the request fails
     */
    public List<String> getAllIsos() throws IOException {
        Response response = get("isos");
        return client.parseResponseList(response, String.class);
    }
}