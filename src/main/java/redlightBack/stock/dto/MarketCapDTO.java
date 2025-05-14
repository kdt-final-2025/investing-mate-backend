package redlightBack.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record MarketCapDTO(
        String symbol,
        @JsonProperty("marketCap") BigDecimal marketCap
) {
}
