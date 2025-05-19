package redlightBack.openAi.dto;

import redlightBack.stockRecommendation.dto.RiskLevel;

public record StockForChatBotDto(String name,
                                 String recommendReason,
                                 String detail,
                                 RiskLevel riskLevel) {
}

