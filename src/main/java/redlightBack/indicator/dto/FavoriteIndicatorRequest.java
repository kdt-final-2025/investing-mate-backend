package redlightBack.indicator.dto;

import jakarta.validation.constraints.NotNull;

public record FavoriteIndicatorRequest(
        @NotNull Long indicatorId
) {
}
