package redlightBack.indicator.dto;

import redlightBack.indicator.Impact;

import java.time.LocalDateTime;

public record IndicatorResponse(
        Long id,
        String name,
        String korName,
        String country,
        LocalDateTime date,
        Double actual,
        Double previous,
        Double estimate,
        Impact impact,
        Boolean isFavorite
) {
}