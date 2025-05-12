package redlightBack.openAi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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

    private final OpenAiProperties openAiProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    //사용자의 질문 -> 추천 조건 JSON으로 추출
    public String askCondition(String userInput){
        Map<String, Object> systemMsg = Map.of("role", "system", "content", systemMes_extractConditions());
        Map<String, Object> userMsg = Map.of("role", "user", "content", userInput);

        return callOpenAi(List.of(systemMsg, userMsg));
    }

    //추천 결과 JSON -> 사용자에게 자연어로 설명
    public String askExplanation(String stockSummaryText){
        Map<String, Object> systemMsg = Map.of(
                "role", "system", "content", systemMes_generateExplanation()
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
        headers.setBearerAuth(openAiProperties.getApiKey());

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

    //system 프롬프트 : 사용자기 입력한 내용에서 조건 추출
    public String systemMes_extractConditions(){
        return """
                당신은 사용자의 주식 관련 질문을 분석하여, 다음 두 가지 조건을 추출하는 AI입니다.
                사용자의 말투는 초보 투자자일 수 있으며, 전문 용어 대신 감정적/일상적 표현을 사용하는 경우가 많습니다.
                
                추출해야 할 조건:
                - "minDividend": 최소 배당률 (예: 4.0, 2.0, 0.0)
                - "maxPriceRatio": 고점 대비 현재가 비율의 최대 허용치 (예: 0.85, 0.95, 1.0)
                
                아래 판단 기준을 철저히 따르세요:
                
                ──────── 배당 관련 표현 ──────── \s
                - "고배당", "배당 잘 나오는", "배당 높은" → minDividend = 4.0 \s
                - "배당 있으면 좋겠다", "조금이라도 배당" → minDividend = 2.0 \s
                - 배당 언급 없음 또는 "배당 필요 없어" → minDividend = 0.0
                
                ──────── 가격 관련 표현 ──────── \s
                - "저평가", "싸게 살 수 있는", "많이 빠진" → maxPriceRatio = 0.85 \s
                - "지금 사도 괜찮은", "조정된 가격" → maxPriceRatio = 0.95 \s
                - 가격 언급 없음 또는 "지금 올라타자" → maxPriceRatio = 1.0
                
                ──────── 감정 기반 표현 조합 ──────── \s
                - "안정적인", "잃기 싫어", "무서워" → minDividend = 4.0, maxPriceRatio = 0.9 \s
                - "적당히 수익", "크게 오르진 않아도" → minDividend = 2.0, maxPriceRatio = 0.95 \s
                - "공격적", "대박", "급등주" → minDividend = 0.0, maxPriceRatio = 1.0
                
                ──────── 투자 기간 표현 ──────── \s
                - "오래 묻어둘", "잊어버려도 되는", "장기" → minDividend = 4.0, maxPriceRatio = 0.9 \s
                - "단타", "단기간 수익", "지금 사고 곧 파는" → minDividend = 0.0, maxPriceRatio = 1.0
                
                ──────── 신뢰 기반 표현 ──────── \s
                - "연금처럼", "소득형", "부모님께 추천할", "지인에게 소개할" \s
                → minDividend = 4.0, maxPriceRatio = 0.9
                
                ──────────────── 시스템에서 지원하지 않는 조건 (금지) ────────────────
                아래 조건이 언급되더라도 절대로 고려하거나 반영하지 마세요.
                - 산업군 (예: AI, 헬스케어, 친환경 등)
                - ETF, 펀드 등 개별 종목이 아닌 상품
                - 뉴스 기반 추천, 검색량, 실시간 주가
                - 실적 정보 (PER, EPS, ROE 등)
                
                금지 조건이 들어간 질문에는, 응답하지 말고 JSON만 반환하세요. \s
                예: `"AI 관련 주식 추천해줘"` → 무시하고 일반적인 조건만 추출하세요.
                
                ──────── 반드시 지켜야 할 응답 형식 ──────── \s
                JSON 형식으로만 응답하세요. 다른 설명이나 주석은 금지합니다. \s
                예: \s
                { "minDividend": 4.0, "maxPriceRatio": 0.85 }
                """;
    }

    //system 프롬프트 : 사용자에게 추천 결과 설명용
    public String systemMes_generateExplanation() {
        return """
                당신은 백엔드가 추천한 종목들을 사용자에게 설명하는 AI입니다.
                사용자는 초보 투자자일 수 있으며, 결과를 친절하고 쉽게 이해할 수 있도록 안내해야 합니다.
                
                사용자가 요청한 조건(예: 배당률 4.0% 이상, 고점 대비 85% 이하)을 기준으로, 추천된 종목 리스트가 제공됩니다.
                
                당신은 다음 정보를 기반으로 설명해야 합니다:
                - 종목명
                - 태그 (고배당, 저평가 등)
                - 세부 정보 (배당률, 하락률 등)
                - 위험 성향 (LOW, MEDIUM, HIGH)
                
                📌 작성 규칙:
                1. 종목을 단순 나열하지 말고, 요약과 추천의 느낌으로 자연스럽게 문장을 구성하세요.
                2. 숫자는 초보자가 이해할 수 있게 바꾸세요.
                   예: "배당률 4.2%" → "배당이 잘 나오는 편이에요"
                       "고점 대비 -15%" → "지금은 고점보다 많이 저렴한 상태예요"
                3. 위험 성향이 LOW이면 "안정적인 선택", HIGH이면 "리스크가 있을 수 있지만 수익 가능성"처럼 부드럽게 표현
                4. 전체 문장은 부드럽고 신뢰감 있게, 전문 용어는 지양하세요.
                5. 마지막에는 "이 종목들이 현재 조건에 가장 잘 맞는 추천입니다."로 마무리하세요.
                
                ⚠️ 주의: 아래 조건이 언급되더라도 절대 설명에 포함하지 마세요:
                - 산업군 (예: AI, 헬스케어 등)
                - ETF나 펀드
                - 실적 정보 (PER, EPS 등)
                - 뉴스 언급량, 실시간 주가 등 외부 정보 기반 표현
                """;
    }

}
