package redlightBack.stockAlert;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import redlightBack.stock.Stock;
import redlightBack.stock.StockRepository;
import redlightBack.stockAlert.dto.*;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Service
public class StockAlertService {

    private final StockAlertRepository stockAlertRepository;
    private final StockRepository stockRepository;
    private final FinnhubWebSocketService finnhubWebSocketService;
    private final AlertEmitterService alertEmitterService;

    @Transactional
    public StockAlertResponse create(String userId, StockAlertRequest request) {
        Stock stock = stockRepository.findBySymbol(request.symbol()).orElseThrow(
                () -> new NoSuchElementException("해당하는 주식이 없습니다."));
        if (stockAlertRepository.existsByUserIdAndSymbolAndTargetPrice(userId, stock.getSymbol(), request.targetPrice())) {
            throw new IllegalArgumentException("해당하는 알람이 이미 존재합니다.");
        }
        StockAlert stockAlert = stockAlertRepository.save(new StockAlert(userId,
                request.targetPrice(),
                stock.getSymbol(),
                request.above()
        ));
        finnhubWebSocketService.subscribeSymbol(stockAlert.getSymbol());
        return new StockAlertResponse(
                stockAlert.getId(),
                stock.getSymbol(),
                stockAlert.getUserId(),
                stockAlert.getTargetPrice(),
                stockAlert.isAbove()
        );
    }

    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = alertEmitterService.createEmitter(userId);
        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("SSE 연결 성공"));
        } catch (IOException e) {
            log.error("SSE 초기 이벤트 전송 실패 - userId={}", userId, e);
            emitter.completeWithError(e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "SSE 연결 초기화에 실패했습니다."
            );
        }
        return emitter;
    }

    @Transactional
    public void delete(String userId, DeleteStockAlertRequest request) {
        stockAlertRepository.deleteByUserIdAndSymbolAndTargetPrice(userId, request.stockSymbol(), request.targetPrice());
    }

    public StockAlertDetailResponse findByAlertId(String userId, Long alertId) {
        StockAlert stockAlert = stockAlertRepository.findById(alertId).orElseThrow(
                () -> new NoSuchElementException("해당하는 알림이 없습니다."));
        return new StockAlertDetailResponse(
                stockAlert.getId(),
                stockAlert.getTargetPrice(),
                stockAlert.getSymbol(),
                stockAlert.isAbove()
        );
    }

    public StockAlertListResponse findAll(String userId) {
        List<StockAlertDetailResponse> stockAlertDetailResponses = stockAlertRepository.findAllByUserId(userId).stream()
                .map(stockAlert -> new StockAlertDetailResponse(
                        stockAlert.getId(),
                        stockAlert.getTargetPrice(),
                        stockAlert.getSymbol(),
                        stockAlert.isAbove()
                ))
                .toList();
        return new StockAlertListResponse(stockAlertDetailResponses);
    }
}
