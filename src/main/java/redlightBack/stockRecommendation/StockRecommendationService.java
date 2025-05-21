package redlightBack.stockRecommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redlightBack.openAi.OpenAiService;
import redlightBack.openAi.dto.StockForChatBotDto;
import redlightBack.stockRecommendation.dto.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class StockRecommendationService {

    private final StockInfoQueryRepository stockInfoQueryRepository;
    private final OpenAiService openAiService;
    private final RedisTemplate<String, String> redisTemplate;


    //추천 받은 주식 list return
    public List<StockRecommendationResponse> getRecommend (Double minDividend, Double maxPriceRatio, RiskLevel riskLevel,  int limit){
        List<StockRecommendation> recommend = stockInfoQueryRepository.recommend(minDividend, maxPriceRatio, riskLevel, limit);

        return recommend.stream().map(
                stock -> {
                    String reason = stock.generateReason();
                    String detail = stock.generateDetail();

                    return new StockRecommendationResponse(
                            stock.getId(),
                            stock.getTicker(),
                            stock.getName(),
                            stock.getCurrentPrice(),
                            round(stock.getHighPrice1y(), 2),
                            round(stock.getDividendYield(), 2),
                            round(stock.getCurrentToHighRatio(), 3),
                            riskLevel,
                            reason,
                            detail
                    );
                }).toList();
    }

    //추천 받은 주식 list를 gpt에 넘기는 로직
    public StockRecommendationAndExplanationResponse getRecommendWithExplanation(
            Double minDividend, Double maxPriceRatio, RiskLevel riskLevel, int limit) {

        // 1. DB 조회
        long dbStart = System.currentTimeMillis();
        List<StockRecommendationResponse> stocks = getRecommend(minDividend, maxPriceRatio, riskLevel, limit);
        long dbEnd = System.currentTimeMillis();
        log.info("📊 추천 종목 수: {}", stocks.size());
        log.info("⏰ DB 조회 소요 시간: {} ms", (dbEnd - dbStart));

        // 2. 빈 리스트 처리
        if (stocks.isEmpty()) {
            log.info("⚠️ 추천 종목이 없습니다.");
            return new StockRecommendationAndExplanationResponse(
                    Collections.emptyList(),
                    "추천할 종목이 없습니다. 검색 조건을 조정해보세요."
            );
        }

        // 3. 캐시 키 생성
        String stockKey = stocks.stream()
                .map(StockRecommendationResponse::id)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining("-"));
        String cacheKey = "GPT_EXPLAIN:" + stockKey;

        // 4. 캐시 HIT 체크 및 반환
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
                String cachedExplanation = redisTemplate.opsForValue().get(cacheKey);
                if (cachedExplanation != null && !cachedExplanation.isBlank()) {
                    log.info("✅ GPT 설명 캐시 HIT: {}", cacheKey);
                    return new StockRecommendationAndExplanationResponse(stocks, cachedExplanation);
                }
            }
        } catch (Exception e) {
            log.error("❌ Redis 캐시 확인 중 예외 발생", e);
        }

        // 5. GPT 설명 생성
        List<StockForChatBotDto> forChatBot = stocks.stream()
                .map(stock -> new StockForChatBotDto(
                        stock.name(),
                        stock.recommendReason(),
                        stock.detail(),
                        stock.riskLevel()))
                .toList();

        String message = openAiService.generateUserMessageFromStockList(forChatBot);
        long gptStart = System.currentTimeMillis();
        String explanation = openAiService.askExplanation(message);
        long gptEnd = System.currentTimeMillis();
        log.info("⏰ GPT 설명 생성 소요시간: {} ms", (gptEnd - gptStart));

        // 6. 캐싱
        try {
            if (explanation != null && !explanation.isBlank()) {
                redisTemplate.opsForValue().set(cacheKey, explanation, Duration.ofHours(24));
                log.info("✅ GPT 설명 캐싱 완료: {}", cacheKey);
            }
        } catch (Exception exception) {
            log.error("❌ Redis 캐싱 중 예외 발생", exception);
        }

        // 7. 최종 응답 반환
        return new StockRecommendationAndExplanationResponse(stocks, explanation);
    }

    private Double round (Double value, int places){
        if(value == null)
            return null;
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}
