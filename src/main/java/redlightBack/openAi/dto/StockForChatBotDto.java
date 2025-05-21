package redlightBack.openAi.dto;

import redlightBack.stockRecommendation.RiskLevel;

public record StockForChatBotDto(String name,
                                 String recommendReason,
                                 String detail,
                                 RiskLevel riskLevel) {
}

