package redlightBack.stockRecommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redlightBack.stockRecommendation.dto.StockRecommendationResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RequiredArgsConstructor
@Service
public class StockRecommendationService {

    private final StockInfoQueryRepository stockInfoQueryRepository;

    public List<StockRecommendationResponse> getRecommend (Double minDividend, Double maxPriceRatio, int limit){
        List<StockRecommendation> recommend = stockInfoQueryRepository.recommend(minDividend, maxPriceRatio, limit);

        return recommend.stream().map(stock -> {
            Double ratio = stock.generatePriceGapRatio();
            String reason = stock.generateReason();
            String detail = stock.generateDetail();
            String riskLevel = stock.generateRiskLevel();

            return new StockRecommendationResponse(stock.getId(),
                    stock.getTicker(),
                    stock.getName(),
                    stock.getCurrentPrice(),
                    round(stock.getHighPrice1y(), 2),
                    round(stock.getDividendYield(), 2),
                    round(ratio, 3),
                    reason,
                    riskLevel,
                    detail);
        }).toList();
    }

    private Double round (Double value, int places){
        if(value == null)
            return null;
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}
