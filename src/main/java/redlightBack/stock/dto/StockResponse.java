package redlightBack.stock.dto;

import java.math.BigDecimal;

public record StockResponse(
        String name,
        String symbol,
        BigDecimal marketCap
) {
}
