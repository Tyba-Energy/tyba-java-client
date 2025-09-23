package com.tybaenergy.client;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ForecastTest {

    private MockWebServer mockWebServer;
    private TybaClient client;
    private Forecast forecast;
    
    // Test data
    private final ZoneId centralTime = ZoneId.of("America/Chicago");
    private final ZonedDateTime startTime = LocalDateTime.of(2024, 2, 5, 0, 0).atZone(centralTime);
    private final ZonedDateTime endTime = LocalDateTime.of(2024, 2, 5, 23, 59).atZone(centralTime);
    private final String nodeName = "HB_HOUSTON";

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString();
        
        // Remove trailing slash for consistent URL building
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        client = new TybaClient("test-token", baseUrl);
        forecast = client.getForecast();
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
        mockWebServer.shutdown();
    }

    @Test
    void testGetMostRecent() throws IOException, InterruptedException {
        // Mock response data
        String mockResponse = """
            [{
                "datetime": "2024-02-05T00:00:00-06:00",
                "forecasted_at": "2024-02-04T06:00:00-06:00",
                "forecast_type": "day-ahead",
                "value": 25.5
            }]
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        // Make the request
        JsonNode result = forecast.getMostRecent(
            nodeName, "rt", startTime, endTime, "day-ahead", null, null, null
        );

        // Verify the request was made correctly
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertTrue(recordedRequest.getPath().startsWith("/public/0.1/forecasts/most_recent_forecast"));
        
        String path = recordedRequest.getPath();
        assertTrue(path.contains("object_name=HB_HOUSTON"));
        assertTrue(path.contains("product=rt"));
        assertTrue(path.contains("start_time=2024-02-05T00%3A00%3A00-06%3A00")); // URL encoded
        assertTrue(path.contains("end_time=2024-02-05T23%3A59%3A00-06%3A00")); // URL encoded
        assertTrue(path.contains("forecast_type=day-ahead"));

        // Verify the response
        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(1, result.size());
        assertEquals("2024-02-05T00:00:00-06:00", result.get(0).get("datetime").asText());
        assertEquals(25.5, result.get(0).get("value").asDouble());
    }

    @Test
    void testGetMostRecentConvenienceMethod() throws IOException, InterruptedException {
        String mockResponse = """
            [{
                "datetime": "2024-02-05T00:00:00-06:00",
                "value": 30.0
            }]
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        // Test convenience method (with defaults)
        JsonNode result = forecast.getMostRecent(nodeName, "rt", startTime, endTime);

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertTrue(path.contains("object_name=HB_HOUSTON"));
        assertFalse(path.contains("forecast_type")); // Should be null and not included
        
        assertNotNull(result);
        assertTrue(result.isArray());
    }

    @Test
    void testGetMostRecentProbabilistic() throws IOException, InterruptedException {
        String mockResponse = """
            [{
                "datetime": "2024-02-05T00:00:00-06:00",
                "quantile_0.1": 20.0,
                "quantile_0.5": 25.0,
                "quantile_0.9": 30.0
            }]
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        List<Double> quantiles = Arrays.asList(0.1, 0.5, 0.9);
        JsonNode result = forecast.getMostRecentProbabilistic(
            nodeName, "da", startTime, endTime, quantiles, "day-ahead", null, null, null
        );

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertTrue(path.contains("object_name=HB_HOUSTON"));
        assertTrue(path.contains("product=da"));
        assertTrue(path.contains("quantiles=%5B0.1%2C%200.5%2C%200.9%5D")); // JSON array, URL encoded

        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(20.0, result.get(0).get("quantile_0.1").asDouble());
    }

    @Test
    void testGetVintaged() throws IOException, InterruptedException {
        String mockResponse = """
            [{
                "datetime": "2024-02-05T00:00:00-06:00",
                "forecasted_at": "2024-02-03T10:00:00-06:00",
                "value": 28.5
            }]
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        JsonNode result = forecast.getVintaged(
            nodeName, "rt", startTime, endTime, 1, LocalTime.of(10, 0), false, 
            "day-ahead", null, null, null
        );

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertTrue(path.contains("object_name=HB_HOUSTON"));
        assertTrue(path.contains("days_ago=1"));
        assertTrue(path.contains("before_time=10%3A00")); // URL encoded
        assertTrue(path.contains("exact_vintage=false"));

        assertNotNull(result);
        assertTrue(result.isArray());
    }

    @Test
    void testGetVintagedConvenienceMethod() throws IOException, InterruptedException {
        String mockResponse = """
            [{
                "datetime": "2024-02-05T00:00:00-06:00",
                "value": 28.5
            }]
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        JsonNode result = forecast.getVintaged(
            nodeName, "rt", startTime, endTime, 1, LocalTime.of(10, 0)
        );

        // Verify convenience method works
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertTrue(path.contains("exact_vintage=false")); // Default value
        
        assertNotNull(result);
    }

    @Test
    void testGetByVintage() throws IOException, InterruptedException {
        String mockResponse = """
            [{
                "datetime": "2024-02-05T00:00:00-06:00",
                "forecasted_at": "2024-02-04T06:00:00-06:00",
                "value": 27.0
            }]
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        ZonedDateTime vintageStart = startTime.minusDays(1);
        ZonedDateTime vintageEnd = startTime;

        JsonNode result = forecast.getByVintage(
            nodeName, "da", vintageStart, vintageEnd, "day-ahead", null, null, null
        );

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertTrue(path.contains("object_name=HB_HOUSTON"));
        assertTrue(path.contains("product=da"));
        assertTrue(path.contains("start_time=2024-02-04T00%3A00%3A00-06%3A00")); // URL encoded vintage start
        assertTrue(path.contains("end_time=2024-02-05T00%3A00%3A00-06%3A00")); // URL encoded vintage end

        assertNotNull(result);
        assertTrue(result.isArray());
    }

    @Test
    void testGetByVintageConvenienceMethod() throws IOException, InterruptedException {
        String mockResponse = """
            [{
                "datetime": "2024-02-05T00:00:00-06:00",
                "value": 27.0
            }]
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        ZonedDateTime vintageStart = startTime.minusDays(1);
        ZonedDateTime vintageEnd = startTime;

        JsonNode result = forecast.getByVintage(nodeName, "da", vintageStart, vintageEnd);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertNotNull(result);
        assertTrue(recordedRequest.getPath().contains("object_name=HB_HOUSTON"));
    }

    @Test
    void testGetActuals() throws IOException, InterruptedException {
        String mockResponse = """
            [{
                "datetime": "2024-02-05T00:00:00-06:00",
                "value": 29.5
            }]
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        JsonNode result = forecast.getActuals(
            nodeName, "da", startTime, endTime, "day-ahead", null, null, null
        );

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertTrue(path.contains("object_name=HB_HOUSTON"));
        assertTrue(path.contains("product=da"));
        assertTrue(path.contains("forecast_type=day-ahead"));

        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(29.5, result.get(0).get("value").asDouble());
    }

    @Test
    void testGetActualsConvenienceMethod() throws IOException, InterruptedException {
        String mockResponse = """
            [{
                "datetime": "2024-02-05T00:00:00-06:00",
                "value": 29.5
            }]
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        JsonNode result = forecast.getActuals(nodeName, "da", startTime, endTime);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertNotNull(result);
        assertTrue(recordedRequest.getPath().contains("object_name=HB_HOUSTON"));
    }

    @Test
    void testGetVintagedProbabilistic() throws IOException, InterruptedException {
        String mockResponse = """
            [{
                "datetime": "2024-02-05T00:00:00-06:00",
                "quantile_0.1": 18.0,
                "quantile_0.5": 25.5,
                "quantile_0.9": 32.0
            }]
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        List<Double> quantiles = Arrays.asList(0.1, 0.5, 0.9);
        JsonNode result = forecast.getVintagedProbabilistic(
            nodeName, "da", startTime, endTime, quantiles, 1, LocalTime.of(12, 0), 
            true, "day-ahead", null, null, null
        );

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertTrue(path.contains("object_name=HB_HOUSTON"));
        assertTrue(path.contains("exact_vintage=true"));
        assertTrue(path.contains("before_time=12%3A00"));

        assertNotNull(result);
        assertTrue(result.isArray());
    }

    @Test
    void testGetByVintageProbabilistic() throws IOException, InterruptedException {
        String mockResponse = """
            [{
                "datetime": "2024-02-05T00:00:00-06:00",
                "quantile_0.1": 15.0,
                "quantile_0.5": 22.5,
                "quantile_0.9": 28.0
            }]
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        List<Double> quantiles = Arrays.asList(0.1, 0.5, 0.9);
        ZonedDateTime vintageStart = LocalDateTime.of(2024, 2, 4, 6, 0).atZone(centralTime);
        ZonedDateTime vintageEnd = LocalDateTime.of(2024, 2, 4, 10, 0).atZone(centralTime);

        JsonNode result = forecast.getByVintageProbabilistic(
            nodeName, "rt", quantiles, vintageStart, vintageEnd, "day-ahead", null, null, null
        );

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertTrue(path.contains("object_name=HB_HOUSTON"));
        assertTrue(path.contains("product=rt"));
        assertTrue(path.contains("quantiles=%5B0.1%2C%200.5%2C%200.9%5D")); // JSON array, URL encoded

        assertNotNull(result);
        assertTrue(result.isArray());
    }

    @Test
    void testTimezoneFormatting() throws IOException, InterruptedException {
        // Test different timezones to ensure proper formatting
        ZoneId easternTime = ZoneId.of("America/New_York");
        ZonedDateTime easternStart = LocalDateTime.of(2024, 2, 5, 0, 0).atZone(easternTime);
        ZonedDateTime easternEnd = LocalDateTime.of(2024, 2, 5, 23, 59).atZone(easternTime);
        
        mockWebServer.enqueue(new MockResponse()
            .setBody("[]")
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        forecast.getMostRecent(nodeName, "rt", easternStart, easternEnd);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        
        // Should contain Eastern Time offset (-05:00 in February)
        assertTrue(path.contains("start_time=2024-02-05T00%3A00%3A00-05%3A00"));
        assertTrue(path.contains("end_time=2024-02-05T23%3A59%3A00-05%3A00"));
    }

    @Test
    void testErrorHandling() throws IOException {
        // Test API error response
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"error\":\"Invalid node name\"}")
            .setResponseCode(400)
            .addHeader("Content-Type", "application/json"));

        IOException exception = assertThrows(IOException.class, () -> {
            forecast.getMostRecent("INVALID_NODE", "rt", startTime, endTime);
        });

        assertTrue(exception.getMessage().contains("Request failed with code: 400"));
    }

    @Test
    void testNullParametersHandledCorrectly() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse()
            .setBody("[]")
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        // Test with null optional parameters
        forecast.getMostRecent(nodeName, "rt", startTime, endTime, null, null, null, null);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        
        // Null parameters should not appear in query string
        assertFalse(path.contains("forecast_type"));
        assertFalse(path.contains("predictions_per_hour"));
        assertFalse(path.contains("prediction_lead_time_mins"));
        assertFalse(path.contains("horizon_mins"));
    }
}