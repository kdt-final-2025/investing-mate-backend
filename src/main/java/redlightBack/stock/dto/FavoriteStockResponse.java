package redlightBack.stock.dto;

public record FavoriteStockResponse(
        String name,
        String code,
        Long marketCap
) {
}
