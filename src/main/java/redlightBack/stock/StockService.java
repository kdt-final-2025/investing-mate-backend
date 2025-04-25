package redlightBack.stock;

import org.springframework.stereotype.Service;
import redlightBack.stock.dto.FavoriteStockRequest;

import java.util.NoSuchElementException;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final FavoriteStockRepository favoriteStockRepository;

    public StockService(StockRepository stockRepository, FavoriteStockRepository favoriteStockRepository) {
        this.stockRepository = stockRepository;
        this.favoriteStockRepository = favoriteStockRepository;
    }

    public void createFavoriteStock(String userId, FavoriteStockRequest request) {
        Stock stock = stockRepository.findById(request.stockId()).orElseThrow(
                () -> new NoSuchElementException("해당하는 주식이 없습니다."));
        favoriteStockRepository.save(new FavoriteStock(stock, userId));
    }
}
