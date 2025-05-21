package redlightBack.stockRecommendation.dto;

import redlightBack.stockRecommendation.RiskLevel;

public record StockRecommendationResponse(Long id,
                                          String ticker,
                                          String name,
                                          Double currentPrice,
                                          Double highPrice1y,
                                          Double dividendYield,
                                          Double currentToHighRatio,
                                          RiskLevel riskLevel,
                                          String recommendReason,
                                          String detail
                                ) {
}
