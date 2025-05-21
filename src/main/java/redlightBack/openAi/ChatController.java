package redlightBack.openAi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import redlightBack.stockRecommendation.StockRecommendationService;
import redlightBack.stockRecommendation.RiskLevel;
import redlightBack.stockRecommendation.dto.StockRecommendationAndExplanationResponse;

import java.util.Map;

@Slf4j
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

        long start = System.currentTimeMillis();
        log.info("⏰전체 로직 시작: {}", start);

        String userInput = request.get("question");

        //사용자 입력 조건에서 조건 추출하기
        long conditionStart = System.currentTimeMillis();
        String conditionJson = openAiService.askCondition(userInput);
        long conditionEnd = System.currentTimeMillis();
        log.info("⏰GPT로 조건 추출 소요시간: {} ms", (conditionEnd - conditionStart));

        Map<String, Object> condition = objectMapper.readValue(conditionJson, Map.class);
        double minDividend = Double.parseDouble(condition.get("minDividend").toString());
        double maxPriceRatio = Double.parseDouble(condition.get("maxPriceRatio").toString());
        RiskLevel riskLevel = RiskLevel.valueOf(condition.get("riskLevel").toString().toUpperCase());

        log.info("🔍 conditionJson: {}", conditionJson);
        log.info("🔍 parsed condition map: {}", condition);

        long recommendStart = System.currentTimeMillis();
        //조건 기반 추천 로직 + GPT 설명
        StockRecommendationAndExplanationResponse recommendWithExplanation = stockRecommendationService.getRecommendWithExplanation(minDividend, maxPriceRatio, riskLevel, 3);
        long recommendEnd = System.currentTimeMillis();
        log.info("⏰조건 기반 추천 로직 + GPT 설명 호출 소요시간: {} ms", (recommendEnd - recommendStart));

        long end = System.currentTimeMillis();
        log.info("⏰전체 로직 종료 소요시간: {} ms", (end - start));

        return recommendWithExplanation;
    }
}
