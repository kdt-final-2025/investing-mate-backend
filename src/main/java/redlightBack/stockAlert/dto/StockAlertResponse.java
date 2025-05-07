package redlightBack.stockAlert.dto;

public record StockAlertResponse(
        Long stockAlertId,
        Long stockId,
        String userId,
        double targetPrice,
        boolean above
) {
}
