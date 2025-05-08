package redlightBack.stockRecommendation.dto;

public record StockRecommendationResponse(Long id,
                                          String ticker,
                                          String name,
                                          Double currentPrice,
                                          Double highPrice1y,
                                          Double dividendYield,
                                          Double priceGapRatio,
                                          String recommendReason,
                                          String riskLevel,
                                          String detail
                                ) {
}
