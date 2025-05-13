package redlightBack.stock.dto;

import java.math.BigDecimal;

public record FavoriteStockResponse(
        String name,
        String code,
        BigDecimal marketCap
) {
}
