package redlightBack.indicator.dto;

import java.util.List;

public record FavoriteIndicatorsListResponse(
        List<FavoriteIndicatorResponse> responses,
        long currentPage,
        long pageSize,
        long totalPages,
        long totalCount
) {
}
