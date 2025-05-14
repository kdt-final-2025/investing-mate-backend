package redlightBack.stockRecommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redlightBack.openAi.OpenAiService;
import redlightBack.openAi.dto.StockForChatBotDto;
import redlightBack.stockRecommendation.dto.SortBy;
import redlightBack.stockRecommendation.dto.SortDirection;
import redlightBack.stockRecommendation.dto.StockRecommendationAndExplanationResponse;
import redlightBack.stockRecommendation.dto.StockRecommendationResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RequiredArgsConstructor
@Service
public class StockRecommendationService {

    private final StockInfoQueryRepository stockInfoQueryRepository;
    private final OpenAiService openAiService;


    //추천 받은 주식 list return
    public List<StockRecommendationResponse> getRecommend (Double minDividend, Double maxPriceRatio, SortBy sortBy, SortDirection sortDirection, int limit){
        List<StockRecommendation> recommend = stockInfoQueryRepository.recommend(minDividend, maxPriceRatio, sortBy, sortDirection, limit);

        return recommend.stream().map(
                stock -> {
                    Double ratio = stock.generatePriceGapRatio();
                    String reason = stock.generateReason();
                    String detail = stock.generateDetail();
                    String riskLevel = stock.generateRiskLevel().name();

                    return new StockRecommendationResponse(
                            stock.getId(),
                            stock.getTicker(),
                            stock.getName(),
                            stock.getCurrentPrice(),
                            round(stock.getHighPrice1y(), 2),
                            round(stock.getDividendYield(), 2),
                            round(ratio, 3),
                            reason,
                            riskLevel,
                            detail
                    );
                }).toList();
    }

    //추천 받은 주식 list를 gpt에 넘기는 로직
    public StockRecommendationAndExplanationResponse getRecommendWithExplanation(Double minDividend, Double maxPriceRatio, SortBy sortBy, SortDirection sortDirection, int limit){
        List<StockRecommendationResponse> stocks = getRecommend(minDividend, maxPriceRatio, sortBy, sortDirection, limit);

        List<StockForChatBotDto> forChatBot = stocks.stream().map(stock -> new StockForChatBotDto(
                stock.name(),
                stock.recommendReason(),
                stock.detail(),
                stock.riskLevel()
        )).toList();

        String message = openAiService.generateUserMessageFromStockList(forChatBot);
        String explanation = openAiService.askExplanation(message);

        return new StockRecommendationAndExplanationResponse(stocks, explanation);
    }

    private Double round (Double value, int places){
        if(value == null)
            return null;
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}
