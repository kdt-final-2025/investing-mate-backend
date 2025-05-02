package redlightBack.stock.dto;

import java.util.List;

public record FavoriteStockListResponse(
        List<FavoriteStockResponse> responses,
        long currentPage,
        long pageSize,
        long totalPages,
        long totalCount
) {
}
