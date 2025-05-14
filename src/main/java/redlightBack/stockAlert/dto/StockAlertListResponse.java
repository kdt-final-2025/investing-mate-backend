package redlightBack.stockAlert.dto;

import java.util.List;

public record StockAlertListResponse(
        List<StockAlertDetailResponse> responses
) {
}
