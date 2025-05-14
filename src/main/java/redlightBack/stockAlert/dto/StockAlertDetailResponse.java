package redlightBack.stockAlert.dto;

public record StockAlertDetailResponse(
        Long id,
        double targetPrice,
        String symbol,
        boolean above
) {
}

