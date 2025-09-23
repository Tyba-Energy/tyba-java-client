package com.tybaenergy.client;

import com.tybaenergy.client.model.Market;
import com.tybaenergy.client.model.NodeData;
import com.tybaenergy.client.model.NodeSearchData;
import com.tybaenergy.client.model.PriceTimeSeries;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ServicesTest {

    private MockWebServer mockWebServer;
    private TybaClient client;
    private Services services;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString();
        
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        client = new TybaClient("test-token", baseUrl);
        services = client.getServices();
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
        mockWebServer.shutdown();
    }

    @Test
    void testGetAllIsos() throws IOException, InterruptedException {
        String mockResponse = """
            ["ERCOT", "CAISO", "PJM"]
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        List<String> result = services.getAllIsos();

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/public/0.1/services/isos", recordedRequest.getPath());
        assertEquals("test-token", recordedRequest.getHeader("Authorization"));

        // Verify the response
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("ERCOT"));
        assertTrue(result.contains("CAISO"));
        assertTrue(result.contains("PJM"));
    }

    @Test
    void testGetAllIsosEmpty() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse()
            .setBody("[]")
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        List<String> result = services.getAllIsos();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetAllIsosError() throws IOException {
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"error\":\"Service unavailable\"}")
            .setResponseCode(503)
            .addHeader("Content-Type", "application/json"));

        IOException exception = assertThrows(IOException.class, () -> {
            services.getAllIsos();
        });

        assertTrue(exception.getMessage().contains("Request failed with code: 503"));
    }

    @Test
    void testGetLmpInstance() {
        LMP lmp = services.getLmp();
        
        assertNotNull(lmp);
        assertSame(lmp, services.getLmp()); // Should return same instance
    }

    @Test
    void testGetAncillaryInstance() {
        Ancillary ancillary = services.getAncillary();
        
        assertNotNull(ancillary);
        assertSame(ancillary, services.getAncillary()); // Should return same instance
    }
}

class LMPTest {

    private MockWebServer mockWebServer;
    private TybaClient client;
    private LMP lmp;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString();
        
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        client = new TybaClient("test-token", baseUrl);
        lmp = client.getLmp();
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
        mockWebServer.shutdown();
    }

    @Test
    void testGetAllNodes() throws IOException, InterruptedException {
        String mockResponse = """
            [
                {
                    "id": "10000698380",
                    "name": "HB_HOUSTON",
                    "type": "HUB",
                    "zone": "ERCOT",
                    "substation": "Houston, TX"
                },
                {
                    "id": "10000700531", 
                    "name": "HB_NORTH",
                    "type": "HUB",
                    "zone": "ERCOT",
                    "substation": "Dallas, TX"
                }
            ]
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        List<NodeData> result = lmp.getAllNodes("ERCOT");

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertTrue(recordedRequest.getPath().startsWith("/public/0.1/services/lmp/nodes"));
        assertTrue(recordedRequest.getPath().contains("iso=ERCOT"));

        // Verify the response
        assertNotNull(result);
        assertEquals(2, result.size());
        
        NodeData firstNode = result.get(0);
        assertEquals("10000698380", firstNode.getId());
        assertEquals("HB_HOUSTON", firstNode.getName());
    }

    @Test
    void testGetPrices() throws IOException, InterruptedException {
        String mockResponse = """
            {
                "10000698380": {
                    "datetimes": [
                        "2024-01-01T00:00:00Z",
                        "2024-01-01T01:00:00Z"
                    ],
                    "prices": [25.5, 24.8]
                }
            }
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        List<String> nodeIds = Arrays.asList("10000698380");
        Map<String, PriceTimeSeries> result = lmp.getPrices(nodeIds, Market.REALTIME, 2024, 2024);

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertTrue(recordedRequest.getPath().contains("node_ids=10000698380"));
        assertTrue(recordedRequest.getPath().contains("market=realtime"));
        assertTrue(recordedRequest.getPath().contains("start_year=2024"));
        assertTrue(recordedRequest.getPath().contains("end_year=2024"));

        // Verify the response
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("10000698380"));
        
        PriceTimeSeries timeSeries = result.get("10000698380");
        assertEquals(2, timeSeries.getDatetimes().size());
        assertEquals(2, timeSeries.getPrices().size());
    }

    @Test
    void testGetPricesMultipleNodes() throws IOException, InterruptedException {
        String mockResponse = """
            {
                "10000698380": {
                    "datetimes": [],
                    "prices": []
                },
                "10000700531": {
                    "datetimes": [],
                    "prices": []
                }
            }
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        List<String> nodeIds = Arrays.asList("10000698380", "10000700531");
        Map<String, PriceTimeSeries> result = lmp.getPrices(nodeIds, Market.DAYAHEAD, 2023, 2024);

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertTrue(path.contains("node_ids=10000698380%2C10000700531")); // Comma-separated, URL encoded
        assertTrue(path.contains("market=dayahead"));

        // Verify the response
        assertEquals(2, result.size());
        assertTrue(result.containsKey("10000698380"));
        assertTrue(result.containsKey("10000700531"));
    }

    @Test
    void testSearchNodes() throws IOException, InterruptedException {
        String mockResponse = """
            {
                "nodes": [
                    {
                        "node/id": "10000698380",
                        "node/name": "HB_HOUSTON",
                        "node/iso": "ERCOT",
                        "node/lat": 29.760427,
                        "node/lng": -95.369803,
                        "node/distance-meters": 1000.0
                    }
                ]
            }
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        List<NodeSearchData> result = lmp.searchNodes("Houston, TX", "HB_*", "ERCOT");

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertTrue(path.contains("location=Houston%2C%20TX")); // URL encoded
        assertTrue(path.contains("node_name_filter=HB_*")); // Asterisk not encoded
        assertTrue(path.contains("iso_override=ERCOT"));

        // Verify the response
        assertNotNull(result);
        assertEquals(1, result.size());
        
        NodeSearchData foundNode = result.get(0);
        assertEquals("HB_HOUSTON", foundNode.getNodeName());
    }

    @Test
    void testSearchNodesByLocation() throws IOException, InterruptedException {
        String mockResponse = """
            {
                "nodes": [
                    {
                        "node/id": "10000698380",
                        "node/name": "HB_HOUSTON",
                        "node/iso": "ERCOT",
                        "node/lat": 29.760427,
                        "node/lng": -95.369803
                    }
                ]
            }
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        List<NodeSearchData> result = lmp.searchNodesByLocation("Houston, TX");

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertTrue(path.contains("location=Houston%2C%20TX"));
        assertFalse(path.contains("node_name_filter"));
        assertFalse(path.contains("iso_override"));

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testSearchNodesByName() throws IOException, InterruptedException {
        String mockResponse = """
            {
                "nodes": [
                    {
                        "node/id": "10000698380",
                        "node/name": "HB_HOUSTON",
                        "node/iso": "ERCOT"
                    }
                ]
            }
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        List<NodeSearchData> result = lmp.searchNodesByName("HB_*");

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertTrue(path.contains("node_name_filter=HB_*")); // Asterisk not encoded
        assertFalse(path.contains("location"));
        assertFalse(path.contains("iso_override"));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("HB_HOUSTON", result.get(0).getNodeName());
    }

    @Test
    void testSearchNodesWithNullParameters() throws IOException, InterruptedException {
        String mockResponse = """
            {
                "nodes": []
            }
            """;
        
        mockWebServer.enqueue(new MockResponse()
            .setBody(mockResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        // Test with null parameters - should not include them in request
        List<NodeSearchData> result = lmp.searchNodes(null, null, null);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        
        // Null parameters should not appear in query string
        assertFalse(path.contains("location"));
        assertFalse(path.contains("node_name_filter"));
        assertFalse(path.contains("iso_override"));

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetPricesError() throws IOException {
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"error\":\"Invalid node ID\"}")
            .setResponseCode(400)
            .addHeader("Content-Type", "application/json"));

        List<String> nodeIds = Arrays.asList("INVALID_NODE");
        
        IOException exception = assertThrows(IOException.class, () -> {
            lmp.getPrices(nodeIds, Market.REALTIME, 2024, 2024);
        });

        assertTrue(exception.getMessage().contains("Request failed with code: 400"));
    }
}