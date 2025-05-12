package redlightBack.stockRecommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redlightBack.stockRecommendation.dto.StockRecommendationAndExplanationResponse;
import redlightBack.stockRecommendation.dto.StockRecommendationResponse;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class StockRecommendationController {

    private final StockRecommendationService stockInfoService;

    //기본 주식 추천 List
    @GetMapping("stockRecommendations")
    public List<StockRecommendationResponse> getRecommendations (@RequestParam(required = false) Double dividendMin,
                                                                 @RequestParam(required = false) Double priceRatioMax,
                                                                 @RequestParam(required = false, defaultValue = "3") Integer limit){

        return stockInfoService.getRecommend(dividendMin, priceRatioMax, limit);
    }

    //GPT한테 넘기는 주식 추천 List
    @GetMapping("/stockRecommendationsWithGpt")
    public StockRecommendationAndExplanationResponse getRecommendationsWithGpt (@RequestParam(required = false) Double dividendMin,
                                                                                @RequestParam(required = false) Double priceRatioMax,
                                                                                @RequestParam(required = false, defaultValue = "5") Integer limit){

        return stockInfoService.getRecommendWithExplanation(dividendMin, priceRatioMax, limit);
    }

}
