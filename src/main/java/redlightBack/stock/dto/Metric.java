package redlightBack.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Metric(
        @JsonProperty("52WeekHigh") Double week52High,
        @JsonProperty("52WeekLow") Double week52Low,
        Long tenDayAverageTradingVolume,
        Double grossMargin,
        Double netMargin,
        Double peBasicExclExtraTTM,
        Double pbAnnual,
        Double roeTTM,
        Double roaTTM
) {
}
