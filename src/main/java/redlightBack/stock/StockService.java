package redlightBack.stock;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redlightBack.stock.dto.FavoriteStockListResponse;
import redlightBack.stock.dto.FavoriteStockRequest;
import redlightBack.stock.dto.FavoriteStockResponse;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class StockService {

    private final StockRepository stockRepository;
    private final FavoriteStockRepository favoriteStockRepository;
    private final StockQueryRepository stockQueryRepository;

    @Transactional
    public void createFavoriteStock(String userId, FavoriteStockRequest request) {
        Stock stock = stockRepository.findById(request.stockId()).orElseThrow(
                () -> new NoSuchElementException("해당하는 주식이 없습니다."));
        if (favoriteStockRepository.existsByStock_IdAndUserId(stock.getId(), userId)) {
            throw new IllegalStateException("이미 등록된 주식입니다.");
        }
        favoriteStockRepository.save(new FavoriteStock(stock, userId));

    }

    @Transactional
    public void deleteFavoriteStock(String userId, FavoriteStockRequest request) {
        Stock stock = stockRepository.findById(request.stockId()).orElseThrow(
                () -> new NoSuchElementException("해당하는 주식이 없습니다."));
        if (!favoriteStockRepository.existsByStock_IdAndUserId(stock.getId(), userId)) {
            throw new NoSuchElementException("즐겨찾기가 존재하지 않습니다.");
        }
        favoriteStockRepository.deleteByStock_IdAndUserId(stock.getId(), userId);
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
