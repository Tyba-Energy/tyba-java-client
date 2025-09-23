package com.tybaenergy.client;

import com.tybaenergy.client.model.AncillaryRegionData;
import com.tybaenergy.client.model.AncillaryService;
import com.tybaenergy.client.model.Market;
import com.tybaenergy.client.model.PriceTimeSeries;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface for accessing Tyba's historical ancillary price data
 */
public class Ancillary {
    private final Services services;
    private static final String ROUTE_BASE = "ancillary";

    public Ancillary(Services services) {
        this.services = services;
    }

    /**
     * Make a GET request to an ancillary endpoint
     */
    private Response get(String route) throws IOException {
        return services.get(ROUTE_BASE + "/" + route);
    }

    /**
     * Make a GET request to an ancillary endpoint with parameters
     */
    private Response get(String route, Map<String, Object> params) throws IOException {
        return services.get(ROUTE_BASE + "/" + route, params);
    }

    /**
     * Get the name and available year ranges for all ancillary service pricing regions
     * that meet the ISO, service and market criteria.
     *
     * @param iso ISO name. Possible values can be found by calling Services.getAllIsos()
     * @param service specifies which ancillary service to pull prices for
     * @param market specifies whether to pull day ahead or real time prices for the given service
     * @return List of AncillaryRegionData objects containing region name and year ranges
     * @throws IOException if the request fails
     */
    public List<AncillaryRegionData> getPricingRegions(String iso, AncillaryService service, Market market) throws IOException {
        Map<String, Object> params = TybaClient.params();
        params.put("iso", iso);
        params.put("service", service.getValue());
        params.put("market", market.getValue());
        
        Response response = get("regions", params);
        return services.client.parseResponseList(response, AncillaryRegionData.class);
    }

    /**
     * Get price time series data for a single region/service combination
     *
     * @param iso ISO name. Possible values can be found by calling Services.getAllIsos()
     * @param service specifies which ancillary service to pull prices for
     * @param market specifies whether to pull day ahead or real time prices for the given service
     * @param region specific region within the ISO to pull prices for. Possible values can be found by calling getPricingRegions()
     * @param startYear the year prices should start
     * @param endYear the year prices should end
     * @return PriceTimeSeries object containing prices and datetimes
     * @throws IOException if the request fails
     */
    public PriceTimeSeries getPrices(String iso, AncillaryService service, Market market, 
                                   String region, int startYear, int endYear) throws IOException {
        Map<String, Object> params = TybaClient.params();
        params.put("iso", iso);
        params.put("service", service.getValue());
        params.put("market", market.getValue());
        params.put("region", region);
        params.put("start_year", startYear);
        params.put("end_year", endYear);
        
        Response response = get("prices", params);
        return services.client.parseResponse(response, PriceTimeSeries.class);
    }
}