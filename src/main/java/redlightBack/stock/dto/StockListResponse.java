package redlightBack.stock.dto;

import java.util.List;

public record StockListResponse(
        List<StockResponse> stockResponses,
        Long totalCount,
        List<FavoriteStockResponse> favoriteStockResponses
) {
}
