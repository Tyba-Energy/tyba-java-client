# Tyba Java Client Examples

This directory contains working examples demonstrating how to use the Tyba Java Client API.

## Prerequisites

1. **Build the project** first:
   ```bash
   mvn clean install
   ```

2. **Set your Personal Access Token**:
   ```bash
   export TYBA_PAT="your-personal-access-token-here"
   ```
   
   Contact Tyba Energy to obtain a Personal Access Token.

3. **Optional: Set custom host** (defaults to `https://dev.tybaenergy.com`):
   ```bash
   export HOST="https://your-custom-host.com"
   ```

## Available Examples

### ForecastExample.java

Demonstrates usage of the Tyba Forecast API with enhanced debugging and error handling.

**Features:**
- Retrieves forecast data for Houston electricity prices
- Shows detailed request/response debugging information
- Handles timezone-aware datetime formatting (required by API)
- Tests multiple forecast endpoints
- Includes comprehensive error reporting

**Quick Run:**

```bash
# Compile the example
javac -cp "target/tyba-client-0.5.0.jar:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q)" examples/ForecastExample.java

# Run with default endpoint (most_recent)
java -cp "target/tyba-client-0.5.0.jar:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q):examples" ForecastExample
```

**Available Endpoints:**

You can specify which endpoints to test by passing them as arguments:

```bash
# Test specific endpoints
java -cp "target/tyba-client-0.5.0.jar:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q):examples" ForecastExample most_recent

java -cp "target/tyba-client-0.5.0.jar:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q):examples" ForecastExample actuals

java -cp "target/tyba-client-0.5.0.jar:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q):examples" ForecastExample vintaged

# Test multiple endpoints
java -cp "target/tyba-client-0.5.0.jar:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q):examples" ForecastExample most_recent actuals vintaged
```

**Available endpoint options:**
- `most_recent` - Get most recent forecast data
- `vintaged` - Get vintaged forecast data
- `vintaged_no_results` - Test vintaged with parameters that return no results
- `by_vintage` - Get forecast data by vintage time range
- `actuals` - Get actual (historical) data
- `most_recent_probabilistic` - Get most recent probabilistic forecast
- `vintaged_probabilistic` - Get vintaged probabilistic forecast  
- `by_vintage_probabilistic` - Get probabilistic forecast by vintage

## Simplified Run Script

For convenience, you can create an alias in your shell profile (`.zshrc` or `.bashrc`):

```bash
# Add this to your ~/.zshrc or ~/.bashrc
alias run-forecast='java -cp "target/tyba-client-0.5.0.jar:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q):examples" ForecastExample'
```

Then reload your shell and run:
```bash
run-forecast most_recent
run-forecast actuals
```

## Example Output

When running successfully, you'll see output like:

```
Using host: https://dev.tybaenergy.com
PAT is set

most recent example

Making request with params: {start_time=2024-02-05T00:00:00-06:00, product=rt, object_name=HB_HOUSTON, end_time=2024-02-10T00:00:00-06:00, forecast_type=day-ahead}
API route: forecasts/most_recent_forecast
Response code: 200
Response message: 
Response body: [{"datetime":"2024-02-05T00:00:00-06:00","forecasted_at":"2024-02-04T06:00:00-06:00","forecast_type":"day-ahead","value":7.68}, ...]
Parsed JSON: [ {
  "datetime" : "2024-02-05T00:00:00-06:00",
  "forecasted_at" : "2024-02-04T06:00:00-06:00",
  "forecast_type" : "day-ahead",
  "value" : 7.68
}, ... ]
```

## Troubleshooting

### Common Issues

1. **422 Validation Error**: This usually means invalid parameters. The enhanced example shows detailed error messages from the API.

2. **Missing TYBA_PAT**: Make sure your Personal Access Token environment variable is set.

3. **Compilation Errors**: Ensure you've run `mvn clean install` first to build the dependencies.

4. **Classpath Issues**: The Maven command `mvn dependency:build-classpath` automatically resolves all dependencies. Make sure Maven is installed and working.

### Debug Output

The ForecastExample includes detailed debugging that shows:
- Request parameters being sent to the API
- HTTP response codes and messages  
- Full API response body (especially helpful for error messages)
- Parsed JSON when successful

This makes it easy to diagnose issues with API calls.

## Adding New Examples

When creating new examples:

1. Place them in this `examples/` directory
2. Use the same compilation pattern:
   ```bash
   javac -cp "target/tyba-client-0.5.0.jar:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q)" examples/YourExample.java
   ```
3. Include error handling and debugging output like ForecastExample
4. Update this README with usage instructions

## Architecture & Design

### Timezone Handling

The Tyba Java Client has been designed with proper timezone handling as a core principle:

- **ZonedDateTime API**: All forecast methods accept `ZonedDateTime` parameters to ensure timezone awareness
- **No hardcoded timezones**: The SDK respects user-provided timezone information without making assumptions about node locations
- **ISO-8601 compliance**: Datetimes are automatically formatted with proper timezone offsets (e.g., `2024-02-05T00:00:00-06:00`)

### Example Usage

```java
// Create timezone-aware datetimes for Houston (Central Time)
ZoneId centralTime = ZoneId.of("America/Chicago");
ZonedDateTime startTime = LocalDateTime.of(2024, 2, 5, 0, 0).atZone(centralTime);
ZonedDateTime endTime = LocalDateTime.of(2024, 2, 10, 0, 0).atZone(centralTime);

// SDK will format as: "2024-02-05T00:00:00-06:00" (proper ISO-8601 with offset)
client.getForecast().getMostRecent(nodeName, "rt", startTime, endTime, ...);
```

### API Design Philosophy

- **Explicit over implicit**: Users must specify timezones explicitly, preventing ambiguity
- **User control**: The SDK doesn't assume or hardcode timezone mappings for nodes
- **Type safety**: `ZonedDateTime` prevents common timezone-related bugs that occur with naive `LocalDateTime`

This design ensures that physical location-based APIs (like energy forecasts) handle timezones correctly without hidden assumptions.

## Notes

- Examples use timezone-aware datetime formatting (ISO-8601 with offset) as required by the Tyba API
- Houston electricity prices are used as example data (HB_HOUSTON node in Central Time)
- The examples include proper resource cleanup with `client.close()`
- Enhanced error handling shows both HTTP status codes and detailed API error messages
