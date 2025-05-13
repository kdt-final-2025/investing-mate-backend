package redlightBack.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record SymbolDTO(
        String symbol,
        @JsonProperty("companyName") String name,
        String exchange,
        @JsonProperty("marketCap") BigDecimal marketCap
) {}

