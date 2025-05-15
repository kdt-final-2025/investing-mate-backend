package redlightBack.stockRecommendation.dto;

import java.util.List;

public record StockRecommendationAndExplanationResponse(List<StockRecommendationResponse> stocks,
                                                        String Explanation) {
}
