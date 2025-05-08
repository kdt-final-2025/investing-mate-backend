package redlightBack.stockAlert;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AlertEmitterService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String userId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(()    -> emitters.remove(userId));
        return emitter;
    }

    public void sendAlert(String userId, String symbol, double price) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                Map<String, Object> payload = Map.of(
                        "symbol", symbol,
                        "price", price,
                        "time", Instant.now().toString()
                );
                emitter.send(SseEmitter.event()
                        .name("price-alert")
                        .data(payload));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }
}

