package redlightBack.stockAlert.dto;

public record StockAlertRequest(
        double targetPrice,
        Long stockId,
        boolean above
) {
}
