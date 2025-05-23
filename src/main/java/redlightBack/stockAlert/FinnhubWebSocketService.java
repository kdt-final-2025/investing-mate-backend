package redlightBack.stockAlert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.Instant;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class FinnhubWebSocketService {

    @Value("${finnhub.ws.base-url}")
    private String baseUrl;

    @Value("${finnhub.api.token}")
    private String token;

    private final StockAlertRepository alertRepository;
    private final AlertEmitterService emitterService;
    private final TaskScheduler scheduler;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private WebSocket webSocket;

    @PostConstruct
    public void init() {
        connect();
    }

    public synchronized void connect() {

        if (webSocket != null) {
            webSocket.close(1000, "reconnect");
        }

        String url = baseUrl + "?token=" + token;

        Request request = new Request.Builder()
                .url(url)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                log.info("✅ Finnhub WebSocket Connected (code: {})", response.code());
                alertRepository.findAllActive().forEach(alert ->
                        subscribeSymbol(alert.getSymbol())
                );
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    // 1) 원시 메시지 로깅
                    log.info("[웹소켓] 수신 원시 메시지: {}", text);

                    // 2) JSON 파싱
                    JsonNode root = mapper.readTree(text);
                    JsonNode dataArray = root.path("data");

                    // 3) 각 데이터 아이템 처리
                    for (JsonNode item : dataArray) {
                        String symbol = item.path("s").asText();
                        double price = item.path("p").asDouble();

                        // 심볼·가격 로깅
                        log.info("[웹소켓] 심볼={}, 가격={}", symbol, price);

                        // 4) 미트리거(triggered=false) 알림 조회
                        List<StockAlert> alerts = alertRepository.findBySymbolAndTriggeredFalse(symbol);
                        log.info("→ 조회된 알림 개수: {} (symbol={})", alerts.size(), symbol);

                        // 5) 조건 체크 및 트리거
                        for (StockAlert alert : alerts) {
                            log.info("   • alert[id={}, above={}, targetPrice={}, triggered={}]",
                                    alert.getId(),
                                    alert.isAbove(),
                                    alert.getTargetPrice(),
                                    alert.isTriggered());

                            boolean shouldTrigger =
                                    (alert.isAbove() && price >= alert.getTargetPrice()) ||
                                            (!alert.isAbove() && price <= alert.getTargetPrice());

                            if (shouldTrigger) {
                                log.info("✔ 조건 만족! alert[id={}] 트리거 실행 (price={} {}, targetPrice={})",
                                        alert.getId(),
                                        price,
                                        alert.isAbove() ? ">=" : "<=",
                                        alert.getTargetPrice());

                                // 6) 상태 업데이트 및 저장
                                alert.markTriggered();
                                alertRepository.save(alert);

                                // 7) SSE로 클라이언트에 알림 전송
                                emitterService.sendAlert(alert.getUserId(), symbol, price);
                            }
                        }
                    }
                } catch (JsonProcessingException e) {
                    log.error("❌ 메시지 파싱 실패: {}", e.getMessage(), e);
                } catch (Exception e) {
                    log.error("❌ onMessage 처리 중 예외 발생: {}", e.getMessage(), e);
                }
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response resp) {
                log.error("❌ WebSocket failure (code={}), will retry in {}s",
                        resp != null ? resp.code() : -1,
                        60,  // ⬅ 변경됨
                        t);
                scheduler.schedule(this::retryConnect,
                        Instant.now().plusSeconds(60)); // ⬅ 변경됨
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                log.warn("⚠️ WebSocket closed (code={}, reason={}), retrying in {}s",
                        code, reason, 60); // ⬅ 변경됨
                scheduler.schedule(this::retryConnect,
                        Instant.now().plusSeconds(60)); // ⬅ 변경됨
            }

            private void retryConnect() {
                log.info("▶ Reconnecting WebSocket…");
                connect();
            }
        });
    }

    public void subscribeSymbol(String symbol) {
        try {
            log.info("▶ Subscribing to symbol: {}", symbol);
            Map<String, String> msg = Map.of(
                    "type", "subscribe",
                    "symbol", symbol
            );
            boolean sent = webSocket.send(mapper.writeValueAsString(msg));
            if (!sent) {
                log.warn("⚠️ Subscribe 메시지 전송 실패: {}", symbol);
            }
        } catch (JsonProcessingException e) {
            log.error("Subscribe 처리 중 오류", e);
        }
    }


    private void handleMessage(String text) throws JsonProcessingException {
        JsonNode node = mapper.readTree(text);
        if (!"trade".equals(node.path("type").asText())) return;

        for (JsonNode item : node.path("data")) {
            String symbol = item.path("s").asText();
            double price = item.path("p").asDouble();

            alertRepository.findBySymbolAndTriggeredFalse(symbol)
                    .forEach(alert -> {
                        if ((alert.isAbove() && price >= alert.getTargetPrice())
                                || (!alert.isAbove() && price <= alert.getTargetPrice())) {
                            alert.markTriggered();
                            alertRepository.save(alert);
                            emitterService.sendAlert(alert.getUserId(), symbol, price);
                        }
                    });
        }
    }
}
