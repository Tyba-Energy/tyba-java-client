# Tyba Java Client

A Java API client for the Tyba Public API, providing access to historical price data, forecast data, and operations data.

## Features

- **Historical Price Data**: Access LMP (energy) and ancillary service pricing data
- **Forecast Data**: Retrieve forecast data with various time windows and probabilistic options
- **Operations Data**: Access asset performance reports, telemetry, and configuration

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Installation

### Maven

This dependency is not currently hosted in Maven Central and must be built locally from source. 

### Building from Source

```bash
git clone https://github.com/Tyba-Energy/tyba-java-client.git
cd tyba-java-client
mvn clean install
```

## Quick Start

```java
import com.tybaenergy.client.TybaClient;
import com.tybaenergy.client.model.Market;
import com.tybaenergy.client.model.NodeData;

import java.io.IOException;
import java.util.List;

public class Example {
    public static void main(String[] args) throws IOException {
        // Create client with your Personal Access Token
        String pat = System.getenv("TYBA_PAT");
        TybaClient client = new TybaClient(pat);
        
        try {
            // Get list of ISOs
            List<String> isos = client.getServices().getAllIsos();
            System.out.println("Available ISOs: " + isos);
            
            // Get all nodes for ERCOT
            List<NodeData> nodes = client.getLmp().getAllNodes("ERCOT");
            System.out.println("Found " + nodes.size() + " nodes in ERCOT");
            
            // Search for nodes in Houston
            var houstonNodes = client.getLmp().searchNodesByLocation("Houston, TX");
            System.out.println("Houston nodes: " + houstonNodes.size());
            
        } finally {
            client.close();
        }
    }
}
```

## Authentication

You need a Personal Access Token to use the Tyba API. Contact Tyba to obtain one.

Set your token as an environment variable:
```bash
export TYBA_PAT="your-personal-access-token-here"
```

Or pass it directly when creating the client:
```java
TybaClient client = new TybaClient("your-personal-access-token-here");
```

## Usage Examples

### Historical Price Data

#### LMP (Energy Prices)

```java
import com.tybaenergy.client.TybaClient;
import com.tybaenergy.client.model.Market;
import com.tybaenergy.client.model.PriceTimeSeries;

import java.util.Arrays;
import java.util.Map;

// Get price data for specific nodes
List<String> nodeIds = Arrays.asList("10000698380", "10000700531");
Map<String, PriceTimeSeries> prices = client.getLmp().getPrices(
    nodeIds, Market.REALTIME, 2023, 2024
);

for (Map.Entry<String, PriceTimeSeries> entry : prices.entrySet()) {
    String nodeId = entry.getKey();
    PriceTimeSeries timeSeries = entry.getValue();
    System.out.println("Node " + nodeId + " has " + timeSeries.getPrices().size() + " price points");
}
```

#### Ancillary Services

```java
import com.tybaenergy.client.model.AncillaryService;
import com.tybaenergy.client.model.AncillaryRegionData;

// Get available regions for regulation up service in ERCOT
List<AncillaryRegionData> regions = client.getAncillary().getPricingRegions(
    "ERCOT", AncillaryService.REGULATION_UP, Market.DAYAHEAD
);

// Get prices for a specific region
PriceTimeSeries prices = client.getAncillary().getPrices(
    "ERCOT", AncillaryService.REGULATION_UP, Market.DAYAHEAD,
    "ERCOT", 2023, 2024
);
```

### Forecast Data

```java
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

// Create timezone-aware datetimes for Houston (Central Time)
ZoneId centralTime = ZoneId.of("America/Chicago");
ZonedDateTime startTime = LocalDateTime.of(2024, 2, 5, 0, 0).atZone(centralTime);
ZonedDateTime endTime = LocalDateTime.of(2024, 2, 10, 0, 0).atZone(centralTime);

// Get most recent forecast
JsonNode forecast = client.getForecast().getMostRecent(
    "HB_HOUSTON", "rt", startTime, endTime
);

// Get actual data
JsonNode actuals = client.getForecast().getActuals(
    "HB_HOUSTON", "da", startTime, endTime
);
```

### Operations Data

```java
import java.time.LocalDate;
import java.util.Arrays;

// Get performance report
String report = client.getOperations().getPerformanceReport(
    LocalDate.of(2024, 1, 1),
    LocalDate.of(2024, 1, 31),
    "my-asset-name"
);

// Get telemetry data
String telemetry = client.getOperations().getTelemetry(
    LocalDate.of(2024, 1, 1),
    LocalDate.of(2024, 1, 2),
    "my-asset-name",
    60, // 60-minute intervals
    Arrays.asList("power", "energy")
);
```

## API Reference

### TybaClient

Main client class for interacting with the Tyba API.

**Constructors:**
- `TybaClient(String personalAccessToken)`
- `TybaClient(String personalAccessToken, String host)`
- `TybaClient(String personalAccessToken, String host, String version)`

**Methods:**
- `getServices()` - Access to historical price data
- `getForecast()` - Access to forecast data  
- `getOperations()` - Access to operations data
- `getLmp()` - Shortcut to services.lmp
- `getAncillary()` - Shortcut to services.ancillary
- `close()` - Clean up resources

### Services

Interface for accessing Tyba's historical price data.

**Methods:**
- `getAllIsos()` - Get list of all available ISOs
- `getLmp()` - Get LMP (energy price) interface
- `getAncillary()` - Get ancillary services interface

### LMP (Energy Prices)

Interface for accessing energy price data.

**Methods:**
- `getAllNodes(String iso)` - Get all nodes for an ISO
- `getPrices(List<String> nodeIds, Market market, int startYear, int endYear)` - Get price data
- `searchNodes(String location, String nodeNameFilter, String isoOverride)` - Search for nodes
- `searchNodesByLocation(String location)` - Search by location only
- `searchNodesByName(String nodeNameFilter)` - Search by name pattern

### Ancillary

Interface for accessing ancillary service price data.

**Methods:**
- `getPricingRegions(String iso, AncillaryService service, Market market)` - Get available regions
- `getPrices(String iso, AncillaryService service, Market market, String region, int startYear, int endYear)` - Get price data

### Forecast

Interface for accessing forecast data.

**Methods:**
- `getMostRecent(...)` - Get most recent forecast
- `getMostRecentProbabilistic(...)` - Get most recent probabilistic forecast
- `getVintaged(...)` - Get vintaged forecast
- `getVintagedProbabilistic(...)` - Get vintaged probabilistic forecast
- `getByVintage(...)` - Get forecasts by vintage
- `getByVintageProbabilistic(...)` - Get probabilistic forecasts by vintage
- `getActuals(...)` - Get actual data

### Operations

Interface for accessing operations data.

**Methods:**
- `getPerformanceReport(...)` - Get performance report for an asset
- `getDaSnapshot(...)` - Get DA snapshot for an asset
- `getTelemetry(...)` - Get telemetry data for an asset
- `getAssetDetails(...)` - Get asset details
- `getAssets(...)` - Get list of assets
- `setAssetOverrides(...)` - Set asset configuration overrides
- `getOverridesSchema()` - Get overrides schema

## Model Classes

### Enums

- `Market` - REALTIME, DAYAHEAD
- `AncillaryService` - REGULATION_UP, REGULATION_DOWN, RESERVES, ECRS
- `NodeType` - GENERATOR, SPTIE, LOAD, etc.

### Data Models

- `NodeData` - Node metadata
- `PriceTimeSeries` - Price and datetime data
- `NodeSearchData` - Node search results
- `AncillaryRegionData` - Ancillary region metadata

## Examples

See the `examples/` directory for complete usage examples with detailed instructions:

- `ForecastExample.java` - Demonstrates forecast API usage with enhanced debugging

For detailed setup and usage instructions, see **[examples/README.md](examples/README.md)**.

Quick start:

```bash
# Set your Personal Access Token
export TYBA_PAT="your-token-here"

# Build the project
mvn clean install

# Compile and run the forecast example
javac -cp "target/tyba-client-0.5.0.jar:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q)" examples/ForecastExample.java
java -cp "target/tyba-client-0.5.0.jar:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q):examples" ForecastExample
```

## Error Handling

The client throws `IOException` for network and API errors. Always wrap API calls in try-catch blocks:

```java
try {
    List<String> isos = client.getServices().getAllIsos();
    // Process results
} catch (IOException e) {
    System.err.println("API call failed: " + e.getMessage());
} finally {
    client.close();
}
```

## Configuration

### Default Settings

- Host: `https://dev.tybaenergy.com`
- API Version: `0.1`
- Connect Timeout: 30 seconds  
- Read/Write Timeout: 60 seconds

### Custom Configuration

```java
// Custom host
TybaClient client = new TybaClient(pat, "https://prod.tybaenergy.com");

// Custom host and version
TybaClient client = new TybaClient(pat, "https://api.tyba.com", "1.0");
```

## Development

### Building

```bash
mvn clean compile
```

### Running Tests

```bash
mvn test
```

### Generating Javadocs

```bash
mvn javadoc:javadoc
```

### Packaging

```bash
mvn clean package
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Add tests for your changes
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For questions or support, please contact Tyba Energy or open an issue on GitHub.

## Changelog

### 0.5.0
- Initial release of Tyba Java Client
- Full API coverage for Services, Forecast, and Operations
