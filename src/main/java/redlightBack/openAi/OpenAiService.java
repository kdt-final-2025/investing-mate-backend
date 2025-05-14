package redlightBack.openAi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redlightBack.openAi.dto.StockForChatBotDto;



import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    @Value("${openai.api-key}")
    private String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    //사용자의 질문 -> 추천 조건 JSON으로 추출
    public String askCondition(String userInput){
        Map<String, Object> systemMsg = Map.of("role", "system", "content", ChatBotPrompt.systemMes_extractConditions);
        Map<String, Object> userMsg = Map.of("role", "user", "content", userInput);

        return callOpenAi(List.of(systemMsg, userMsg));
    }

    //추천 결과 JSON -> 사용자에게 자연어로 설명
    public String askExplanation(String stockSummaryText){
        Map<String, Object> systemMsg = Map.of(
                "role", "system", "content", ChatBotPrompt.systemMes_generateExplanation
        );

        Map<String, Object> userMsg = Map.of(
                "role", "user", "content", stockSummaryText
        );
        return callOpenAi(List.of(systemMsg, userMsg));
    }

    //AIP로 gpt 호출
    private String callOpenAi(List<Map<String, Object>> messages){
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", messages,
                "temperature", 0.7,
                "max_tokens", 500
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                entity,
                Map.class
        );

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return message.get("content").toString();
    }

    public String generateUserMessageFromStockList(List<StockForChatBotDto> stocks ){
        String message = "아래는 추천 종목입니다. 사용자에게 자연스럽게 설명해 주세요:\n\n";

        int index = 1;
        for (StockForChatBotDto stock : stocks){
            message += index + ". " + stock.name() + "/n";
            message += "태그: " + stock.recommendReason() + "/n";
            message += "세부 정보: " + stock.detail() + "/n";
            message += "위험 성향: " + stock.riskLevel() + "/n";
            index++;
        }
        return message.trim();
    }


}
