package redlightBack.stockAlert.dto;

public record StockAlertResponse(
        Long stockAlertId,
        String stockSymbol,
        String userId,
        double targetPrice,
        boolean above
) {
}
