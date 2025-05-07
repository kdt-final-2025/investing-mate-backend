package redlightBack.stockAlert;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import redlightBack.loginUtils.LoginMemberId;
import redlightBack.stockAlert.dto.DeleteStockAlertRequest;
import redlightBack.stockAlert.dto.StockAlertRequest;
import redlightBack.stockAlert.dto.StockAlertResponse;

@RequiredArgsConstructor
@RestController
public class StockAlertRestController {

    private final StockAlertService stockAlertService;

    @PostMapping("/alerts")
    public StockAlertResponse create(@LoginMemberId String userId, @RequestBody StockAlertRequest request) {
        return stockAlertService.create(userId, request);
    }

    @DeleteMapping("/alerts")
    public void delete(@LoginMemberId String userId, @RequestBody DeleteStockAlertRequest request) {
        stockAlertService.delete(userId, request);
    }
}
