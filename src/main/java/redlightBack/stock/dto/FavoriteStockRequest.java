package redlightBack.stock.dto;

import jakarta.validation.constraints.NotNull;

public record FavoriteStockRequest(
        @NotNull Long stockId
) {
}
