package redlightBack.stock;

import org.springframework.web.bind.annotation.*;
import redlightBack.loginUtils.LoginMemberId;
import redlightBack.stock.dto.FavoriteStockListResponse;
import redlightBack.stock.dto.FavoriteStockRequest;

@RestController
public class StockRestController {

    private final StockService stockService;

    public StockRestController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/stocks/favorites")
    public void createFavoriteStock(@LoginMemberId String userId, @RequestBody FavoriteStockRequest request) {
        stockService.createFavoriteStock(userId, request);
    }

    @GetMapping("/stocks/favorites")
    public FavoriteStockListResponse getAll(@LoginMemberId String userId,
                                            @RequestParam(required = false, defaultValue = "0") int page,
                                            @RequestParam(required = false, defaultValue = "20") int size,
                                            @RequestParam(required = false, defaultValue = "id") String sortBy,
                                            @RequestParam(required = false, defaultValue = "asc") String order) {
        return stockService.getAll(userId, page, size, sortBy, order);
    }
}
