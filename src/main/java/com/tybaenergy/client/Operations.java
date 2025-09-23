package com.tybaenergy.client;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.Response;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interface for accessing Tyba's operations data
 */
public class Operations {
    private final TybaClient client;
    private static final String ROUTE_BASE = "operations";

    public Operations(TybaClient client) {
        this.client = client;
    }

    /**
     * Make a GET request to an operations endpoint
     */
    private Response get(String route) throws IOException {
        return client.get(ROUTE_BASE + "/" + route);
    }

    /**
     * Make a GET request to an operations endpoint with parameters
     */
    private Response get(String route, Map<String, Object> params) throws IOException {
        return client.get(ROUTE_BASE + "/" + route, params);
    }

    /**
     * Make a POST request to an operations endpoint
     */
    private Response post(String route, Object jsonPayload) throws IOException {
        return client.post(ROUTE_BASE + "/" + route, jsonPayload);
    }

    /**
     * Get performance report for an asset
     *
     * @param startDate start date for the report
     * @param endDate end date for the report
     * @param assetName asset name (either this or displayName must be provided)
     * @param displayName display name (either this or assetName must be provided)
     * @return String containing the performance report
     * @throws IOException if the request fails
     * @throws IllegalArgumentException if neither assetName nor displayName is provided
     */
    public String getPerformanceReport(LocalDate startDate, LocalDate endDate, String assetName, String displayName) throws IOException {
        if (assetName == null && displayName == null) {
            throw new IllegalArgumentException("Must provide either 'assetName' or 'displayName'.");
        }

        Map<String, Object> params = TybaClient.params();
        params.put("start_date", startDate.toString());
        params.put("end_date", endDate.toString());

        if (assetName != null) {
            params.put("asset_name", assetName);
        } else {
            params.put("asset_display_name", displayName);
        }

        Response response = get("internal_api/performance_report", params);
        return client.parseResponseString(response);
    }

    /**
     * Get DA snapshot for an asset
     *
     * @param startDate start date for the snapshot
     * @param endDate end date for the snapshot
     * @param assetName asset name (either this or displayName must be provided)
     * @param displayName display name (either this or assetName must be provided)
     * @return String containing the DA snapshot
     * @throws IOException if the request fails
     * @throws IllegalArgumentException if neither assetName nor displayName is provided
     */
    public String getDaSnapshot(LocalDate startDate, LocalDate endDate, String assetName, String displayName) throws IOException {
        if (assetName == null && displayName == null) {
            throw new IllegalArgumentException("Must provide either 'assetName' or 'displayName'.");
        }

        Map<String, Object> params = TybaClient.params();
        params.put("start_date", startDate.toString());
        params.put("end_date", endDate.toString());

        if (assetName != null) {
            params.put("asset_name", assetName);
        } else {
            params.put("asset_display_name", displayName);
        }

        Response response = get("internal_api/da_snapshot", params);
        return client.parseResponseString(response);
    }

    /**
     * Get telemetry data for an asset
     *
     * @param startDate start date for the telemetry
     * @param endDate end date for the telemetry
     * @param assetName asset name (either this or displayName must be provided)
     * @param intervalMins interval in minutes
     * @param metrics list of metrics to retrieve
     * @param solarAssetTelemetry whether to include solar asset telemetry
     * @param displayName display name (either this or assetName must be provided)
     * @return String containing the telemetry data
     * @throws IOException if the request fails
     * @throws IllegalArgumentException if neither assetName nor displayName is provided
     */
    public String getTelemetry(LocalDate startDate, LocalDate endDate, String assetName, int intervalMins, 
                              List<String> metrics, boolean solarAssetTelemetry, String displayName) throws IOException {
        if (assetName == null && displayName == null) {
            throw new IllegalArgumentException("Must provide either 'assetName' or 'displayName'.");
        }

        Map<String, Object> params = TybaClient.params();
        params.put("start_date", startDate.toString());
        params.put("end_date", endDate.toString());
        params.put("interval_mins", intervalMins);
        params.put("metrics", metrics);
        params.put("solar_asset_telemetry", solarAssetTelemetry);

        if (assetName != null) {
            params.put("asset_name", assetName);
        } else {
            params.put("asset_display_name", displayName);
        }

        Response response = get("internal_api/telemetry", params);
        return client.parseResponseString(response);
    }

    /**
     * Get asset details
     *
     * @param assetName asset name (either this or displayName must be provided)
     * @param date optional date for the details
     * @param displayName display name (either this or assetName must be provided)
     * @return String containing the asset details
     * @throws IOException if the request fails
     * @throws IllegalArgumentException if neither assetName nor displayName is provided
     */
    public String getAssetDetails(String assetName, LocalDate date, String displayName) throws IOException {
        if (assetName == null && displayName == null) {
            throw new IllegalArgumentException("Must provide either 'assetName' or 'displayName'.");
        }

        Map<String, Object> params = TybaClient.params();

        if (assetName != null) {
            params.put("asset_name", assetName);
        } else {
            params.put("asset_display_name", displayName);
        }

        if (date != null) {
            params.put("date", date.toString());
        }

        Response response = get("internal_api/asset_details", params);
        return client.parseResponseString(response);
    }

    /**
     * Get list of assets
     *
     * @param orgId optional organization ID
     * @param includeDisabled whether to include disabled assets
     * @return String containing the assets list
     * @throws IOException if the request fails
     */
    public String getAssets(String orgId, boolean includeDisabled) throws IOException {
        Map<String, Object> params = TybaClient.params("include_disabled", includeDisabled);
        if (orgId != null) {
            params.put("org_id", orgId);
        }

        Response response = get("internal_api/assets", params);
        return client.parseResponseString(response);
    }

    /**
     * Set asset overrides
     *
     * @param assetNames list of asset names
     * @param field the field to override
     * @param aggregation aggregation type ("global", "single_day", "single_day_hourly", "12x24")
     * @param values the values to set
     * @param service optional service name
     * @param date optional date (required for some aggregation types)
     * @return JsonNode containing the result
     * @throws IOException if the request fails
     */
    public JsonNode setAssetOverrides(List<String> assetNames, String field, String aggregation, Object values, 
                                     String service, LocalDate date) throws IOException {
        Map<String, Object> assumption = TybaClient.params();
        assumption.put("field", field);
        
        Map<String, Object> data = TybaClient.params("aggregation", aggregation);
        
        switch (aggregation) {
            case "12x24":
                if (values instanceof Map) {
                    data.putAll((Map<String, Object>) values);
                }
                break;
            case "single_day_hourly":
                data.put("date", date != null ? date.toString() : null);
                data.put("values", values);
                break;
            case "single_day":
                data.put("values", values);
                data.put("date", date != null ? date.toString() : null);
                break;
            case "global":
                data.put("value", values);
                break;
        }
        
        assumption.put("data", data);
        
        if (service != null) {
            assumption.put("service", service);
        }
        
        Map<String, Object> requestData = TybaClient.params();
        requestData.put("asset_names", assetNames);
        requestData.put("assumption", assumption);

        Response response = post("internal_api/assets/override/", requestData);
        
        if (!response.isSuccessful()) {
            Map<String, Object> errorResponse = TybaClient.params();
            errorResponse.put("status_code", response.code());
            errorResponse.put("reason", response.message());
            errorResponse.put("message", client.parseResponseString(response));
            return client.objectMapper.valueToTree(errorResponse);
        } else {
            return client.parseResponse(response, JsonNode.class);
        }
    }

    /**
     * Get overrides schema
     *
     * @return JsonNode containing the overrides schema
     * @throws IOException if the request fails
     */
    public JsonNode getOverridesSchema() throws IOException {
        Response response = get("internal_api/overrides_schema");
        return client.parseResponse(response, JsonNode.class);
    }

    // Convenience methods with default parameters
    public String getPerformanceReport(LocalDate startDate, LocalDate endDate, String assetName) throws IOException {
        return getPerformanceReport(startDate, endDate, assetName, null);
    }

    public String getDaSnapshot(LocalDate startDate, LocalDate endDate, String assetName) throws IOException {
        return getDaSnapshot(startDate, endDate, assetName, null);
    }

    public String getTelemetry(LocalDate startDate, LocalDate endDate, String assetName, int intervalMins, List<String> metrics) throws IOException {
        return getTelemetry(startDate, endDate, assetName, intervalMins, metrics, false, null);
    }

    public String getAssetDetails(String assetName) throws IOException {
        return getAssetDetails(assetName, null, null);
    }

    public String getAssets() throws IOException {
        return getAssets(null, false);
    }
}