package redlightBack.stockAlert;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redlightBack.stock.Stock;
import redlightBack.stock.StockRepository;
import redlightBack.stockAlert.dto.DeleteStockAlertRequest;
import redlightBack.stockAlert.dto.StockAlertRequest;
import redlightBack.stockAlert.dto.StockAlertResponse;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class StockAlertService {

    private final StockAlertRepository stockAlertRepository;
    private final StockRepository stockRepository;
    private final FinnhubWebSocketService finnhubWebSocketService;

    @Transactional
    public StockAlertResponse create(String userId, StockAlertRequest request) {
        Stock stock = stockRepository.findById(request.stockId()).orElseThrow(
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

    @Transactional
    public void delete(String userId, DeleteStockAlertRequest request) {
        stockAlertRepository.deleteByUserIdAndSymbolAndTargetPrice(userId, request.stockSymbol(), request.targetPrice());
    }
}
