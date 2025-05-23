package redlightBack.indicator.dto;

import java.util.List;

public record IndicatorListResponse(
        List<IndicatorResponse> indicatorResponses,
        Long totalCount
) {
}
