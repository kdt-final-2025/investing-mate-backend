package redlightBack.stock;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import redlightBack.loginUtils.LoginMemberId;
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
}
