package redlightBack.stockAlert;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import redlightBack.loginUtils.LoginMemberId;
import redlightBack.stockAlert.dto.*;

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

    @GetMapping("/alerts/{alertId}")
    public StockAlertDetailResponse findByAlertId(@LoginMemberId String userId, Long alertId) {
        return stockAlertService.findByAlertId(userId, alertId);
    }

    @GetMapping("/alerts")
    public StockAlertListResponse findAll(@LoginMemberId String userId) {
        return stockAlertService.findAll(userId);
    }
}
