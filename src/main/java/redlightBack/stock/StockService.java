package redlightBack.stock;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import redlightBack.stock.dto.*;

import java.util.List;
import java.util.NoSuchElementException;

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

    public FavoriteStockListResponse getFavoriteAll(String userId,
                                                    int page,
                                                    int size,
                                                    String sortBy,
                                                    String order) {
        Sort.Direction direction = Sort.Direction.fromString(order.toUpperCase());
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        List<Stock> stocks = stockQueryRepository.getFavoriteAll(
                userId,
                pageable);
        long totalCount = stockQueryRepository.favoriteTotalCount(userId);
        long totalPages = (totalCount + size - 1) / size;
        List<FavoriteStockResponse> responses = stocks.stream()
                .map(stock -> new FavoriteStockResponse(
                        stock.getName(),
                        stock.getSymbol(),
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

    public StockListResponse getAll(String symbol,
                                    int page,
                                    int size,
                                    String sortBy,
                                    String order) {
        Sort.Direction direction = Sort.Direction.fromString(order.toUpperCase());
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        List<Stock> stocks = stockQueryRepository.getAll(symbol, pageable);
        List<StockResponse> list = stocks.stream()
                .map(stock -> new StockResponse(
                        stock.getName(),
                        stock.getSymbol(),
                        stock.getMarketCap(),
                        stock.getExchange()
                ))
                .toList();
        Long totalCount = stockQueryRepository.totalCount(symbol);
        return new StockListResponse(list, totalCount);
    }
}
