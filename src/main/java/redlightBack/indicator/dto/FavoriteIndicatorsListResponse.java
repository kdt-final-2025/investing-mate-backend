package redlightBack.indicator.dto;

import java.util.List;

public record FavoriteIndicatorsListResponse(
        List<IndicatorResponse> responses,
        long currentPage,
        long pageSize,
        long totalPages,
        long totalCount
) {
}
