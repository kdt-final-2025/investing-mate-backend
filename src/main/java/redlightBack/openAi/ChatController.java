package redlightBack.openAi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import redlightBack.stockRecommendation.StockRecommendationService;
import redlightBack.stockRecommendation.dto.RiskLevel;
import redlightBack.stockRecommendation.dto.StockRecommendationAndExplanationResponse;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;
    private final StockRecommendationService stockRecommendationService;

    //사용자가 챗봇에 입력
    @PostMapping("/chat")
    public String chat (@RequestBody String userMessage){
        return openAiService.askCondition(userMessage);
    }

    @PostMapping("chat/recommend")
    public StockRecommendationAndExplanationResponse recommendFromNaturalLanguage(
            @RequestBody Map<String, String> request) throws JsonProcessingException {

        String userInput = request.get("question");

        //사용자 입력 조건에서 조건 추출하기
        String conditionJson = openAiService.askCondition(userInput);
        Map<String, Object> condition = objectMapper.readValue(conditionJson, Map.class);

        double minDividend = Double.parseDouble(condition.get("minDividend").toString());
        double maxPriceRatio = Double.parseDouble(condition.get("maxPriceRatio").toString());
        RiskLevel riskLevel = RiskLevel.valueOf(condition.get("riskLevel").toString().toUpperCase());

        //조건 기반 추천 로직 + GPT 설명
        return stockRecommendationService.getRecommendWithExplanation(minDividend, maxPriceRatio, riskLevel, 3);
    }
}
