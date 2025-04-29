package redlightBack.stock;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import redlightBack.stock.dto.FavoriteStockListResponse;
import redlightBack.stock.dto.FavoriteStockRequest;
import redlightBack.stock.dto.FavoriteStockResponse;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final FavoriteStockRepository favoriteStockRepository;
    private final StockQueryRepository stockQueryRepository;

    public StockService(StockRepository stockRepository, FavoriteStockRepository favoriteStockRepository, StockQueryRepository stockQueryRepository) {
        this.stockRepository = stockRepository;
        this.favoriteStockRepository = favoriteStockRepository;
        this.stockQueryRepository = stockQueryRepository;
    }

    @Transactional
    public void createFavoriteStock(String userId, FavoriteStockRequest request) {
        Stock stock = stockRepository.findById(request.stockId()).orElseThrow(
                () -> new NoSuchElementException("해당하는 주식이 없습니다."));
        favoriteStockRepository.save(new FavoriteStock(stock, userId));
    }

    public FavoriteStockListResponse getAll(String userId,
                                            int page,
                                            int size,
                                            String sortBy,
                                            String order) {
        List<Stock> stocks = stockQueryRepository.getAll(
                userId,
                sortBy,
                order,
                page,
                size);
        long totalCount = stockQueryRepository.totalCount(userId);
        long totalPages = (totalCount + size - 1) / size;
        List<FavoriteStockResponse> responses = stocks.stream()
                .map(stock -> new FavoriteStockResponse(
                        stock.getName(),
                        stock.getCode(),
                        stock.getMarketCap()))
                .toList();
        return new FavoriteStockListResponse(
                responses,
                page,
                size,
                totalPages,
                totalCount
        );
    }
}
