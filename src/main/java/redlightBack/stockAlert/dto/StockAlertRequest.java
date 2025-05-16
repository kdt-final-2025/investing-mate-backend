package redlightBack.stockAlert.dto;

public record StockAlertRequest(
        double targetPrice,
        String symbol,
        boolean above
) {
}
