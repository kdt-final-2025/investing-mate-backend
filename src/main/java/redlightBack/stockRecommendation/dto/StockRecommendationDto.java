package redlightBack.stockRecommendation.dto;

public record StockRecommendationDto(Long id,
                                     String ticker,
                                     String name,
                                     Double currentPrice,
                                     Double highPrice1y,
                                     Double dividendYield) {
}
