package redlightBack.stockAlert.dto;

public record DeleteStockAlertRequest(
        String stockSymbol,
        double targetPrice
) {
}
