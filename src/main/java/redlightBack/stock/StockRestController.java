package redlightBack.stock;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import redlightBack.loginUtils.LoginMemberId;
import redlightBack.stock.dto.FavoriteStockListResponse;
import redlightBack.stock.dto.FavoriteStockRequest;

@RequiredArgsConstructor
@RestController
public class StockRestController {

    private final StockService stockService;

    @PostMapping("/stocks/favorites")
    public void createFavoriteStock(@LoginMemberId String userId, @Valid @RequestBody FavoriteStockRequest request) {
        stockService.createFavoriteStock(userId, request);
    }

    @DeleteMapping("/stocks/favorites")
    public void deleteFavoriteStock(@LoginMemberId String userId, @Valid @RequestBody FavoriteStockRequest request) {
        stockService.deleteFavoriteStock(userId, request);
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
