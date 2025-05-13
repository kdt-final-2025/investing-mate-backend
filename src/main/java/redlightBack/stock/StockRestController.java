package redlightBack.stock;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import redlightBack.loginUtils.LoginMemberId;
import redlightBack.stock.dto.FavoriteStockListResponse;
import redlightBack.stock.dto.FavoriteStockRequest;
import redlightBack.stock.external.TickerSyncService;

@RequiredArgsConstructor
@RestController
public class StockRestController {

    private final StockService stockService;
    private final TickerSyncService tickerSyncService;

    @PostMapping("/stocks/favorites")
    public void createFavoriteStock(@LoginMemberId String userId, @Valid @RequestBody FavoriteStockRequest request) {
        stockService.createFavoriteStock(userId, request);
    }

    @DeleteMapping("/stocks/favorites")
    public void deleteFavoriteStock(@LoginMemberId String userId, @Valid @RequestBody FavoriteStockRequest request) {
        stockService.deleteFavoriteStock(userId, request);
    }

    @GetMapping("/stocks/favorites")
    public FavoriteStockListResponse getFavoriteAll(@LoginMemberId String userId,
                                                    @RequestParam(required = false, defaultValue = "0") int page,
                                                    @RequestParam(required = false, defaultValue = "20") int size,
                                                    @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                    @RequestParam(required = false, defaultValue = "asc") String order) {
        return stockService.getFavoriteAll(userId, page, size, sortBy, order);
    }
//
//    @GetMapping("/stocks")
//    public StockListResponse getAll(@RequestParam(required = false) String symbol,
//                                    @RequestParam(required = false, defaultValue = "0") int page,
//                                    @RequestParam(required = false, defaultValue = "20") int size,
//                                    @RequestParam(required = false, defaultValue = "id") String sortBy,
//                                    @RequestParam(required = false, defaultValue = "asc") String order) {
//        return stockService.getAll(symbol, page, size, sortBy, order);
//    }

    @GetMapping("stocks/symbol")
    public void getAllFromFmp() {
        tickerSyncService.sync();
    }
}
