package com.tybaenergy.client;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TybaClientTest {

    private MockWebServer mockWebServer;
    private TybaClient client;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        baseUrl = mockWebServer.url("/").toString();
        
        // Remove trailing slash for consistent URL building
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        client = new TybaClient("test-token", baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
        mockWebServer.shutdown();
    }

    @Test
    void testConstructorWithTokenOnly() {
        TybaClient defaultClient = new TybaClient("test-token");
        
        assertNotNull(defaultClient.getServices());
        assertNotNull(defaultClient.getForecast());
        assertNotNull(defaultClient.getOperations());
        assertNotNull(defaultClient.getLmp());
        assertNotNull(defaultClient.getAncillary());
        
        defaultClient.close();
    }

    @Test
    void testConstructorWithTokenAndHost() {
        TybaClient customClient = new TybaClient("test-token", "https://api.custom.com");
        
        assertNotNull(customClient.getServices());
        assertNotNull(customClient.getForecast());
        assertNotNull(customClient.getOperations());
        
        customClient.close();
    }

    @Test
    void testConstructorWithAllParameters() {
        TybaClient fullClient = new TybaClient("test-token", "https://api.custom.com", "1.0");
        
        assertNotNull(fullClient.getServices());
        assertEquals("https://api.custom.com/public/1.0/", fullClient.getBaseUrl());
        
        fullClient.close();
    }

    @Test
    void testGetBaseUrl() {
        assertEquals(baseUrl + "/public/0.1/", client.getBaseUrl());
    }

    @Test
    void testGetRequest() throws IOException, InterruptedException {
        // Enqueue a mock response
        mockWebServer.enqueue(new MockResponse()
            .setBody("[{\"id\":\"ERCOT\",\"name\":\"Electric Reliability Council of Texas\"}]")
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        // Make the request
        var response = client.get("services/isos");

        // Verify the request was made correctly
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/public/0.1/services/isos", recordedRequest.getPath());
        assertEquals("test-token", recordedRequest.getHeader("Authorization"));

        // Verify the response
        assertTrue(response.isSuccessful());
        assertEquals(200, response.code());
        
        response.close();
    }

    @Test
    void testGetRequestWithParams() throws IOException, InterruptedException {
        // Enqueue a mock response
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"status\":\"success\"}")
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        // Prepare parameters
        Map<String, Object> params = new HashMap<>();
        params.put("iso", "ERCOT");
        params.put("year", 2024);
        params.put("null_param", null); // Should be ignored

        // Make the request
        var response = client.get("test/endpoint", params);

        // Verify the request was made correctly
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        
        String path = recordedRequest.getPath();
        assertTrue(path.contains("iso=ERCOT"));
        assertTrue(path.contains("year=2024"));
        assertFalse(path.contains("null_param"));

        response.close();
    }

    @Test
    void testPostRequest() throws IOException, InterruptedException {
        // Enqueue a mock response
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"created\":true}")
            .setResponseCode(201)
            .addHeader("Content-Type", "application/json"));

        // Prepare JSON payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "test");
        payload.put("value", 123);

        // Make the request
        var response = client.post("test/create", payload);

        // Verify the request was made correctly
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/public/0.1/test/create", recordedRequest.getPath());
        assertEquals("test-token", recordedRequest.getHeader("Authorization"));
        assertTrue(recordedRequest.getHeader("Content-Type").startsWith("application/json"));
        
        String requestBody = recordedRequest.getBody().readUtf8();
        assertTrue(requestBody.contains("\"name\":\"test\""));
        assertTrue(requestBody.contains("\"value\":123"));

        assertEquals(201, response.code());
        response.close();
    }

    @Test
    void testParseResponse() throws IOException {
        // Enqueue a mock response with JSON
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"message\":\"success\",\"count\":42}")
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        var response = client.get("test/json");
        JsonNode result = client.parseResponse(response, JsonNode.class);

        assertEquals("success", result.get("message").asText());
        assertEquals(42, result.get("count").asInt());
    }

    @Test
    void testParseResponseError() throws IOException {
        // Enqueue an error response
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"error\":\"Not found\"}")
            .setResponseCode(404)
            .addHeader("Content-Type", "application/json"));

        var response = client.get("test/error");
        
        IOException exception = assertThrows(IOException.class, () -> {
            client.parseResponse(response, JsonNode.class);
        });
        
        assertTrue(exception.getMessage().contains("Request failed with code: 404"));
    }

    @Test
    void testParseResponseList() throws IOException {
        // Enqueue a mock response with JSON array
        mockWebServer.enqueue(new MockResponse()
            .setBody("[{\"id\":\"1\",\"name\":\"first\"},{\"id\":\"2\",\"name\":\"second\"}]")
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        var response = client.get("test/list");
        var result = client.parseResponseList(response, JsonNode.class);

        assertEquals(2, result.size());
        assertEquals("first", result.get(0).get("name").asText());
        assertEquals("second", result.get(1).get("name").asText());
    }

    @Test
    void testParseResponseListError() throws IOException {
        // Enqueue an error response
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"error\":\"Unauthorized\"}")
            .setResponseCode(401)
            .addHeader("Content-Type", "application/json"));

        var response = client.get("test/list-error");
        
        IOException exception = assertThrows(IOException.class, () -> {
            client.parseResponseList(response, JsonNode.class);
        });
        
        assertTrue(exception.getMessage().contains("Request failed with code: 401"));
    }

    @Test
    void testParseResponseEmptyBody() throws IOException {
        // Enqueue a response with empty body
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        var response = client.get("test/empty");
        
        IOException exception = assertThrows(IOException.class, () -> {
            client.parseResponse(response, JsonNode.class);
        });
        
        // Exception should be thrown for empty response body
        assertNotNull(exception.getMessage());
    }

    @Test
    void testShortcutMethods() {
        // Test that shortcuts return the same instances as the full methods
        assertSame(client.getServices().getLmp(), client.getLmp());
        assertSame(client.getServices().getAncillary(), client.getAncillary());
    }

    @Test
    void testClose() throws IOException {
        // This should not throw an exception
        client.close();
        
        // Create a new client since we closed the previous one
        client = new TybaClient("test-token", baseUrl);
    }
}