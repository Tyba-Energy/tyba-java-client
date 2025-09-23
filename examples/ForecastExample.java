import com.fasterxml.jackson.databind.JsonNode;
import com.tybaenergy.client.TybaClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Example demonstrating usage of the Tyba Java Client for forecast operations.
 * 
 * Shows how to retrieve forecast data for Houston electricity prices with proper timezone handling.
 */
public class ForecastExample {
    
    public static void main(String[] args) throws IOException {
        // Get Personal Access Token from environment variable
        String pat = System.getenv("TYBA_PAT");
        if (pat == null) {
            System.err.println("Please set TYBA_PAT environment variable");
            System.exit(1);
        }
        
        // Get host from environment variable or use default
        String host = System.getenv("HOST");
        if (host == null) {
            host = "https://dev.tybaenergy.com";
        }
        
        // Create client
        System.out.println("Using host: " + host);
        System.out.println("PAT is " + (pat != null ? "set" : "not set"));
        
        TybaClient client = new TybaClient(pat, host);
        
        // Define time range with timezone-aware datetimes
        ZoneId centralTime = ZoneId.of("America/Chicago"); // Houston is in Central Time
        ZonedDateTime startTime = LocalDateTime.of(2024, 2, 5, 0, 0).atZone(centralTime);
        ZonedDateTime endTime = LocalDateTime.of(2024, 2, 10, 0, 0).atZone(centralTime);
        String nodeName = "HB_HOUSTON";
        
        try {
            // Parse command line arguments to determine which endpoints to test
            List<String> endpoints = Arrays.asList(args.length > 0 ? args : new String[]{"most_recent"});
            
            if (endpoints.contains("most_recent")) {
                System.out.println("\nmost recent example\n");
                JsonNode response = client.getForecast().getMostRecent(
                    nodeName, "rt", startTime, endTime, "day-ahead", null, null, null
                );
                System.out.println(response.toPrettyString());
            }
            
            if (endpoints.contains("vintaged")) {
                System.out.println("\nvintaged example\n");
                JsonNode response = client.getForecast().getVintaged(
                    nodeName, "rt", startTime, endTime, 1, LocalTime.of(10, 0)
                );
                System.out.println(response.toPrettyString());
            }
            
            if (endpoints.contains("vintaged_no_results")) {
                System.out.println("\nvintaged (no results)\n");
                try {
                    JsonNode response = client.getForecast().getVintaged(
                        nodeName, "da", startTime, endTime, 2, LocalTime.of(23, 0)
                    );
                    System.out.println(response.toPrettyString());
                } catch (IOException e) {
                    System.out.println("No results found: " + e.getMessage());
                }
            }
            
            if (endpoints.contains("by_vintage")) {
                System.out.println("\nby vintaged\n");
                ZonedDateTime vintageStartTime = startTime.minusDays(1);
                ZonedDateTime vintageEndTime = endTime;
                JsonNode response = client.getForecast().getByVintage(
                    nodeName, "da", vintageStartTime, vintageEndTime, null, null, null, null
                );
                System.out.println(response.toPrettyString());
            }
            
            if (endpoints.contains("actuals")) {
                System.out.println("\nactuals\n");
                JsonNode response = client.getForecast().getActuals(nodeName, "da", startTime, endTime);
                System.out.println(response.toPrettyString());
            }
            
            if (endpoints.contains("most_recent_probabilistic")) {
                System.out.println("\nmost_recent_probabilistic\n");
                List<Double> quantiles = Arrays.asList(0.10, 0.50, 0.90);
                JsonNode response = client.getForecast().getMostRecentProbabilistic(
                    "HB_HOUSTON", "da", startTime, endTime, quantiles, null, null, null, null
                );
                System.out.println(response.toPrettyString());
            }
            
            if (endpoints.contains("vintaged_probabilistic")) {
                System.out.println("\nvintaged_probabilistic\n");
                List<Double> quantiles = Arrays.asList(0.10, 0.50, 0.90);
                JsonNode response = client.getForecast().getVintagedProbabilistic(
                    "HB_HOUSTON", "da", startTime, endTime, quantiles, 1, LocalTime.of(10, 0),
                    false, null, null, null, null
                );
                System.out.println(response.toPrettyString());
            }
            
            if (endpoints.contains("by_vintage_probabilistic")) {
                System.out.println("\nby_vintage_probabilistic\n");
                List<Double> quantiles = Arrays.asList(0.10, 0.50, 0.90);
                ZonedDateTime vintageStartTime = LocalDateTime.of(2024, 2, 5, 6, 0).atZone(centralTime);
                ZonedDateTime vintageEndTime = LocalDateTime.of(2024, 2, 5, 10, 0).atZone(centralTime);
                JsonNode response = client.getForecast().getByVintageProbabilistic(
                    "HB_HOUSTON", "rt", quantiles, vintageStartTime, vintageEndTime, 
                    "day-ahead", null, null, null
                );
                System.out.println(response.toPrettyString());
            }
            
        } finally {
            // Clean up resources
            client.close();
        }
    }
    
    /**
     * Print usage information
     */
    public static void printUsage() {
        System.out.println("Usage: java ForecastExample [endpoints...]");
        System.out.println("Available endpoints:");
        System.out.println("  most_recent");
        System.out.println("  vintaged");
        System.out.println("  vintaged_no_results");
        System.out.println("  by_vintage");
        System.out.println("  actuals");
        System.out.println("  most_recent_probabilistic");
        System.out.println("  vintaged_probabilistic");
        System.out.println("  by_vintage_probabilistic");
        System.out.println();
        System.out.println("Environment variables:");
        System.out.println("  TYBA_PAT - Personal Access Token (required)");
        System.out.println("  HOST - API host (optional, defaults to https://dev.tybaenergy.com)");
    }
}