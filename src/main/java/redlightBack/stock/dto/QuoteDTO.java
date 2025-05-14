package redlightBack.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record QuoteDTO(
        String symbol,
        String name,
        @JsonProperty("marketCap") BigDecimal marketCap,
        String type
) {

}
