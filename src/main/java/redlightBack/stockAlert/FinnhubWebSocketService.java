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
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;


import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class FinnhubWebSocketService {

    // ① 토큰을 직접 박아넣기
    private static final String WS_URL = "wss://ws.finnhub.io?token=d0a2df9r01qus8rg7d8gd0a2df9r01qus8rg7d90";

    private final StockAlertRepository alertRepository;
    private final AlertEmitterService emitterService;

    private OkHttpClient client;
    private WebSocket webSocket;
    private ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        client = new OkHttpClient();
        // ② replace 불필요, WS_URL 그대로 사용
        Request request = new Request.Builder()
                .url(WS_URL)
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
            public void onMessage(WebSocket ws, String text) {
                try {
                    handleMessage(text);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response resp) {
                t.printStackTrace();
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
