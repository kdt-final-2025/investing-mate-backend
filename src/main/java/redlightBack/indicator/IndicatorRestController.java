package redlightBack.indicator;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import redlightBack.indicator.dto.FavoriteIndicatorRequest;
import redlightBack.loginUtils.LoginMemberId;

@RestController
public class IndicatorRestController {

    private final IndicatorService indicatorService;

    public IndicatorRestController(IndicatorService indicatorService) {
        this.indicatorService = indicatorService;
    }

    @PostMapping("/indicators/favorites")
    public void createFavoriteIndicator(@LoginMemberId String userId, FavoriteIndicatorRequest request) {
        indicatorService.createFavoriteIndicator(userId, request);
    }
}
