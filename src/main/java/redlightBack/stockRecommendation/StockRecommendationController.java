package redlightBack.stockRecommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redlightBack.stockRecommendation.dto.StockRecommendationResponse;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class StockRecommendationController {

    private final StockRecommendationService stockInfoService;

    @GetMapping("/stockRecommendations")
    public List<StockRecommendationResponse> getRecommendations (@RequestParam(required = false) Double dividendMin,
                                                                 @RequestParam(required = false) Double priceRatioMax,
                                                                 @RequestParam(required = false, defaultValue = "5") Integer limit){

        return stockInfoService.getRecommend(dividendMin, priceRatioMax, limit);
    }

}
