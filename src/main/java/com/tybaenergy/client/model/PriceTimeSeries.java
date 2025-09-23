package com.tybaenergy.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Schema for pricing data associated with a particular energy price node or ancillary pricing region
 */
public class PriceTimeSeries {
    /**
     * Beginning-of-interval datetimes for the hourly pricing given in local time.
     * 
     * - For energy prices, the datetimes are timezone-naive (no timezone identifier) but given in the local timezone
     *   (i.e. including Daylight Savings Time or DST). E.g. The start of the year 2022 in ERCOT is given as
     *   '2022-01-01T00:00:00' as opposed to '2022-01-01T00:00:00-6:00'. Leap days are represented by a single hour,
     *   which should be dropped as a post-processing step.
     * - For ancillary prices, the datetimes are in local standard time (i.e. not including DST) but appear to be in
     *   UTC ("Z" timezone identifier). E.g. The start of the year 2022 in ERCOT is given as '2022-01-01T00:00:00Z' and
     *   not '2022-01-01T00:00:00-6:00'. Leap days are not included.
     */
    @JsonProperty("datetimes")
    private List<String> datetimes;
    
    /**
     * Average hourly settlement prices for hours represented by datetimes.
     */
    @JsonProperty("prices")
    private List<Double> prices;

    // Constructors
    public PriceTimeSeries() {}

    public PriceTimeSeries(List<String> datetimes, List<Double> prices) {
        this.datetimes = datetimes;
        this.prices = prices;
    }

    // Getters and setters
    public List<String> getDatetimes() { return datetimes; }
    public void setDatetimes(List<String> datetimes) { this.datetimes = datetimes; }

    public List<Double> getPrices() { return prices; }
    public void setPrices(List<Double> prices) { this.prices = prices; }
}