package com.tybaenergy.client;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.Response;

import java.io.IOException;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Interface for accessing Tyba's forecast data
 */
public class Forecast {
    private final TybaClient client;
    private static final String ROUTE_BASE = "forecasts";

    public Forecast(TybaClient client) {
        this.client = client;
    }

    /**
     * Make a GET request to a forecast endpoint
     */
    private Response get(String route) throws IOException {
        return client.get(ROUTE_BASE + "/" + route);
    }

    /**
     * Make a GET request to a forecast endpoint with parameters
     */
    private Response get(String route, Map<String, Object> params) throws IOException {
        return client.get(ROUTE_BASE + "/" + route, params);
    }

    /**
     * Get the most recent forecast data
     *
     * @param objectName the object name
     * @param product the product type
     * @param startTime start time for the forecast (timezone-aware)
     * @param endTime end time for the forecast (timezone-aware)
     * @param forecastType optional forecast type
     * @param predictionsPerHour optional predictions per hour
     * @param predictionLeadTimeMins optional prediction lead time in minutes
     * @param horizonMins optional horizon in minutes
     * @return JsonNode containing the forecast data
     * @throws IOException if the request fails
     */
    public JsonNode getMostRecent(String objectName, String product, ZonedDateTime startTime, ZonedDateTime endTime,
                                 String forecastType, Integer predictionsPerHour, 
                                 Integer predictionLeadTimeMins, Integer horizonMins) throws IOException {
        Map<String, Object> params = TybaClient.params();
        params.put("object_name", objectName);
        params.put("product", product);
        params.put("start_time", startTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        params.put("end_time", endTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        if (forecastType != null) params.put("forecast_type", forecastType);
        if (predictionsPerHour != null) params.put("predictions_per_hour", predictionsPerHour);
        if (predictionLeadTimeMins != null) params.put("prediction_lead_time_mins", predictionLeadTimeMins);
        if (horizonMins != null) params.put("horizon_mins", horizonMins);
        
        Response response = get("most_recent_forecast", params);
        return client.parseResponse(response, JsonNode.class);
    }

    /**
     * Get the most recent probabilistic forecast data
     *
     * @param objectName the object name
     * @param product the product type
     * @param startTime start time for the forecast (timezone-aware)
     * @param endTime end time for the forecast (timezone-aware)
     * @param quantiles list of quantiles
     * @param forecastType optional forecast type
     * @param predictionsPerHour optional predictions per hour
     * @param predictionLeadTimeMins optional prediction lead time in minutes
     * @param horizonMins optional horizon in minutes
     * @return JsonNode containing the probabilistic forecast data
     * @throws IOException if the request fails
     */
    public JsonNode getMostRecentProbabilistic(String objectName, String product, ZonedDateTime startTime, ZonedDateTime endTime,
                                              List<Double> quantiles, String forecastType, Integer predictionsPerHour,
                                              Integer predictionLeadTimeMins, Integer horizonMins) throws IOException {
        Map<String, Object> params = TybaClient.params();
        params.put("object_name", objectName);
        params.put("product", product);
        params.put("start_time", startTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        params.put("end_time", endTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        params.put("quantiles", quantiles);
        if (forecastType != null) params.put("forecast_type", forecastType);
        if (predictionsPerHour != null) params.put("predictions_per_hour", predictionsPerHour);
        if (predictionLeadTimeMins != null) params.put("prediction_lead_time_mins", predictionLeadTimeMins);
        if (horizonMins != null) params.put("horizon_mins", horizonMins);
        
        Response response = get("most_recent_probabilistic_forecast", params);
        return client.parseResponse(response, JsonNode.class);
    }

    /**
     * Get vintaged forecast data
     *
     * @param objectName the object name
     * @param product the product type
     * @param startTime start time for the forecast (timezone-aware)
     * @param endTime end time for the forecast (timezone-aware)
     * @param daysAgo number of days ago
     * @param beforeTime time before which to get forecast
     * @param exactVintage whether to use exact vintage
     * @param forecastType optional forecast type
     * @param predictionsPerHour optional predictions per hour
     * @param predictionLeadTimeMins optional prediction lead time in minutes
     * @param horizonMins optional horizon in minutes
     * @return JsonNode containing the vintaged forecast data
     * @throws IOException if the request fails
     */
    public JsonNode getVintaged(String objectName, String product, ZonedDateTime startTime, ZonedDateTime endTime,
                               int daysAgo, LocalTime beforeTime, boolean exactVintage,
                               String forecastType, Integer predictionsPerHour,
                               Integer predictionLeadTimeMins, Integer horizonMins) throws IOException {
        Map<String, Object> params = TybaClient.params();
        params.put("object_name", objectName);
        params.put("product", product);
        params.put("start_time", startTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        params.put("end_time", endTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        params.put("days_ago", daysAgo);
        params.put("before_time", beforeTime.toString());
        params.put("exact_vintage", exactVintage);
        if (forecastType != null) params.put("forecast_type", forecastType);
        if (predictionsPerHour != null) params.put("predictions_per_hour", predictionsPerHour);
        if (predictionLeadTimeMins != null) params.put("prediction_lead_time_mins", predictionLeadTimeMins);
        if (horizonMins != null) params.put("horizon_mins", horizonMins);
        
        Response response = get("vintaged_forecast", params);
        return client.parseResponse(response, JsonNode.class);
    }

    /**
     * Get vintaged probabilistic forecast data
     *
     * @param objectName the object name
     * @param product the product type
     * @param startTime start time for the forecast (timezone-aware)
     * @param endTime end time for the forecast (timezone-aware)
     * @param quantiles list of quantiles
     * @param daysAgo number of days ago
     * @param beforeTime time before which to get forecast
     * @param exactVintage whether to use exact vintage
     * @param forecastType optional forecast type
     * @param predictionsPerHour optional predictions per hour
     * @param predictionLeadTimeMins optional prediction lead time in minutes
     * @param horizonMins optional horizon in minutes
     * @return JsonNode containing the vintaged probabilistic forecast data
     * @throws IOException if the request fails
     */
    public JsonNode getVintagedProbabilistic(String objectName, String product, ZonedDateTime startTime, ZonedDateTime endTime,
                                            List<Double> quantiles, int daysAgo, LocalTime beforeTime, boolean exactVintage,
                                            String forecastType, Integer predictionsPerHour,
                                            Integer predictionLeadTimeMins, Integer horizonMins) throws IOException {
        Map<String, Object> params = TybaClient.params();
        params.put("object_name", objectName);
        params.put("product", product);
        params.put("start_time", startTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        params.put("end_time", endTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        params.put("quantiles", quantiles);
        params.put("days_ago", daysAgo);
        params.put("before_time", beforeTime.toString());
        params.put("exact_vintage", exactVintage);
        if (forecastType != null) params.put("forecast_type", forecastType);
        if (predictionsPerHour != null) params.put("predictions_per_hour", predictionsPerHour);
        if (predictionLeadTimeMins != null) params.put("prediction_lead_time_mins", predictionLeadTimeMins);
        if (horizonMins != null) params.put("horizon_mins", horizonMins);
        
        Response response = get("vintaged_probabilistic_forecast", params);
        return client.parseResponse(response, JsonNode.class);
    }

    /**
     * Get forecast data by vintage
     *
     * @param objectName the object name
     * @param product the product type
     * @param vintageStartTime vintage start time (timezone-aware)
     * @param vintageEndTime vintage end time (timezone-aware)
     * @param forecastType optional forecast type
     * @param predictionsPerHour optional predictions per hour
     * @param predictionLeadTimeMins optional prediction lead time in minutes
     * @param horizonMins optional horizon in minutes
     * @return JsonNode containing the forecast data by vintage
     * @throws IOException if the request fails
     */
    public JsonNode getByVintage(String objectName, String product, ZonedDateTime vintageStartTime, ZonedDateTime vintageEndTime,
                                String forecastType, Integer predictionsPerHour,
                                Integer predictionLeadTimeMins, Integer horizonMins) throws IOException {
        Map<String, Object> params = TybaClient.params();
        params.put("object_name", objectName);
        params.put("product", product);
        params.put("start_time", vintageStartTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        params.put("end_time", vintageEndTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        if (forecastType != null) params.put("forecast_type", forecastType);
        if (predictionsPerHour != null) params.put("predictions_per_hour", predictionsPerHour);
        if (predictionLeadTimeMins != null) params.put("prediction_lead_time_mins", predictionLeadTimeMins);
        if (horizonMins != null) params.put("horizon_mins", horizonMins);
        
        Response response = get("forecasts_by_vintage", params);
        return client.parseResponse(response, JsonNode.class);
    }

    /**
     * Get probabilistic forecast data by vintage
     *
     * @param objectName the object name
     * @param product the product type
     * @param quantiles list of quantiles
     * @param vintageStartTime vintage start time (timezone-aware)
     * @param vintageEndTime vintage end time (timezone-aware)
     * @param forecastType optional forecast type
     * @param predictionsPerHour optional predictions per hour
     * @param predictionLeadTimeMins optional prediction lead time in minutes
     * @param horizonMins optional horizon in minutes
     * @return JsonNode containing the probabilistic forecast data by vintage
     * @throws IOException if the request fails
     */
    public JsonNode getByVintageProbabilistic(String objectName, String product, List<Double> quantiles,
                                             ZonedDateTime vintageStartTime, ZonedDateTime vintageEndTime,
                                             String forecastType, Integer predictionsPerHour,
                                             Integer predictionLeadTimeMins, Integer horizonMins) throws IOException {
        Map<String, Object> params = TybaClient.params();
        params.put("object_name", objectName);
        params.put("product", product);
        params.put("quantiles", quantiles);
        params.put("start_time", vintageStartTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        params.put("end_time", vintageEndTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        if (forecastType != null) params.put("forecast_type", forecastType);
        if (predictionsPerHour != null) params.put("predictions_per_hour", predictionsPerHour);
        if (predictionLeadTimeMins != null) params.put("prediction_lead_time_mins", predictionLeadTimeMins);
        if (horizonMins != null) params.put("horizon_mins", horizonMins);
        
        Response response = get("probabilistic_forecasts_by_vintage", params);
        return client.parseResponse(response, JsonNode.class);
    }

    /**
     * Get actual data
     *
     * @param objectName the object name
     * @param product the product type
     * @param startTime start time for the actuals (timezone-aware)
     * @param endTime end time for the actuals (timezone-aware)
     * @param forecastType optional forecast type
     * @param predictionsPerHour optional predictions per hour
     * @param predictionLeadTimeMins optional prediction lead time in minutes
     * @param horizonMins optional horizon in minutes
     * @return JsonNode containing the actual data
     * @throws IOException if the request fails
     */
    public JsonNode getActuals(String objectName, String product, ZonedDateTime startTime, ZonedDateTime endTime,
                              String forecastType, Integer predictionsPerHour, 
                              Integer predictionLeadTimeMins, Integer horizonMins) throws IOException {
        Map<String, Object> params = TybaClient.params();
        params.put("object_name", objectName);
        params.put("product", product);
        params.put("start_time", startTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        params.put("end_time", endTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        if (forecastType != null) params.put("forecast_type", forecastType);
        if (predictionsPerHour != null) params.put("predictions_per_hour", predictionsPerHour);
        if (predictionLeadTimeMins != null) params.put("prediction_lead_time_mins", predictionLeadTimeMins);
        if (horizonMins != null) params.put("horizon_mins", horizonMins);
        
        Response response = get("actuals", params);
        return client.parseResponse(response, JsonNode.class);
    }

    // Convenience methods with default parameters
    public JsonNode getMostRecent(String objectName, String product, ZonedDateTime startTime, ZonedDateTime endTime) throws IOException {
        return getMostRecent(objectName, product, startTime, endTime, null, null, null, null);
    }

    public JsonNode getActuals(String objectName, String product, ZonedDateTime startTime, ZonedDateTime endTime) throws IOException {
        return getActuals(objectName, product, startTime, endTime, null, null, null, null);
    }

    public JsonNode getByVintage(String objectName, String product, ZonedDateTime vintageStartTime, ZonedDateTime vintageEndTime) throws IOException {
        return getByVintage(objectName, product, vintageStartTime, vintageEndTime, null, null, null, null);
    }

    public JsonNode getVintaged(String objectName, String product, ZonedDateTime startTime, ZonedDateTime endTime,
                               int daysAgo, LocalTime beforeTime) throws IOException {
        return getVintaged(objectName, product, startTime, endTime, daysAgo, beforeTime, false, null, null, null, null);
    }

}
