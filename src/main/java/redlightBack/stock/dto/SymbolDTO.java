package redlightBack.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SymbolDTO(
        String symbol,
        String name,
        String exchange
) {
}
