package com.tybaenergy.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * High level interface for interacting with Tyba's API.
 */
public class TybaClient {
    private static final Logger logger = LoggerFactory.getLogger(TybaClient.class);
    
    private static final String DEFAULT_HOST = "https://dev.tybaenergy.com";
    private static final String DEFAULT_VERSION = "0.1";
    
    private final String personalAccessToken;
    private final String host;
    private final String version;
    private final OkHttpClient httpClient;
    final ObjectMapper objectMapper;
    
    private final Services services;
    private final Forecast forecast;
    private final Operations operations;

    /**
     * Create a new TybaClient with the given personal access token.
     *
     * @param personalAccessToken required for using the Java client/API, contact Tyba to obtain
     */
    public TybaClient(String personalAccessToken) {
        this(personalAccessToken, DEFAULT_HOST, DEFAULT_VERSION);
    }

    /**
     * Create a new TybaClient with custom host and version.
     *
     * @param personalAccessToken required for using the Java client/API, contact Tyba to obtain
     * @param host the API host URL
     */
    public TybaClient(String personalAccessToken, String host) {
        this(personalAccessToken, host, DEFAULT_VERSION);
    }

    /**
     * Create a new TybaClient with custom host and version.
     *
     * @param personalAccessToken required for using the Java client/API, contact Tyba to obtain
     * @param host the API host URL
     * @param version the API version
     */
    public TybaClient(String personalAccessToken, String host, String version) {
        this.personalAccessToken = personalAccessToken;
        this.host = host;
        this.version = version;
        
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        this.services = new Services(this);
        this.forecast = new Forecast(this);
        this.operations = new Operations(this);
    }

    /**
     * Interface for accessing Tyba's historical price data
     */
    public Services getServices() {
        return services;
    }

    /**
     * Interface for accessing Tyba's forecast data
     */
    public Forecast getForecast() {
        return forecast;
    }

    /**
     * Interface for accessing Tyba's operations data
     */
    public Operations getOperations() {
        return operations;
    }

    /**
     * Shortcut to services.ancillary
     */
    public Ancillary getAncillary() {
        return services.getAncillary();
    }

    /**
     * Shortcut to services.lmp
     */
    public LMP getLmp() {
        return services.getLmp();
    }

    /**
     * Get the base URL for API requests
     */
    public String getBaseUrl() {
        return host + "/public/" + version + "/";
    }

    /**
     * Make a GET request to the API
     */
    public Response get(String route) throws IOException {
        return get(route, null);
    }

    /**
     * Make a GET request to the API with query parameters
     */
    public Response get(String route, Map<String, Object> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(getBaseUrl() + route).newBuilder();
        
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() != null) {
                    urlBuilder.addQueryParameter(entry.getKey(), entry.getValue().toString());
                }
            }
        }
        
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Authorization", personalAccessToken)
                .get()
                .build();
        
        logger.debug("GET {}", request.url());
        return httpClient.newCall(request).execute();
    }

    /**
     * Make a POST request to the API
     */
    public Response post(String route, Object jsonPayload) throws IOException {
        String json = objectMapper.writeValueAsString(jsonPayload);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        
        Request request = new Request.Builder()
                .url(getBaseUrl() + route)
                .addHeader("Authorization", personalAccessToken)
                .post(body)
                .build();
        
        logger.debug("POST {} with body: {}", request.url(), json);
        return httpClient.newCall(request).execute();
    }

    /**
     * Parse JSON response into a specific type
     */
    public <T> T parseResponse(Response response, Class<T> clazz) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Request failed with code: " + response.code() + ", message: " + response.message());
        }
        
        try (ResponseBody responseBody = response.body()) {
            if (responseBody == null) {
                throw new IOException("Empty response body");
            }
            return objectMapper.readValue(responseBody.string(), clazz);
        }
    }

    /**
     * Parse JSON response into a List of specific type
     */
    public <T> List<T> parseResponseList(Response response, Class<T> clazz) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Request failed with code: " + response.code() + ", message: " + response.message());
        }
        
        try (ResponseBody responseBody = response.body()) {
            if (responseBody == null) {
                throw new IOException("Empty response body");
            }
            CollectionType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(responseBody.string(), listType);
        }
    }

    /**
     * Parse JSON response as raw string
     */
    public String parseResponseString(Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Request failed with code: " + response.code() + ", message: " + response.message());
        }
        
        try (ResponseBody responseBody = response.body()) {
            if (responseBody == null) {
                throw new IOException("Empty response body");
            }
            return responseBody.string();
        }
    }

    /**
     * Helper method to create parameter maps
     */
    public static Map<String, Object> params() {
        return new HashMap<>();
    }

    /**
     * Helper method to create parameter maps with initial values
     */
    public static Map<String, Object> params(String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }

    /**
     * Close the HTTP client
     */
    public void close() {
        httpClient.connectionPool().evictAll();
        if (httpClient.dispatcher().executorService() != null) {
            httpClient.dispatcher().executorService().shutdown();
        }
    }
}