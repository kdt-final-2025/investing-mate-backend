package redlightBack.indicator;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import redlightBack.indicator.dto.FavoriteIndicatorRequest;
import redlightBack.indicator.dto.FavoriteIndicatorsListResponse;
import redlightBack.loginUtils.LoginMemberId;

@RequiredArgsConstructor
@RestController
public class IndicatorRestController {

    private final IndicatorService indicatorService;

    @GetMapping("/indicators")
    public void fetchAndSaveAll() {
        indicatorService.fetchAndSaveAll();
    }

    @PostMapping("/indicators/favorites")
    public void createFavoriteIndicator(@LoginMemberId String userId, @Valid @RequestBody FavoriteIndicatorRequest request) {
        indicatorService.createFavoriteIndicator(userId, request);
    }

    @GetMapping("/indicators/favorites")
    public FavoriteIndicatorsListResponse findAll(@LoginMemberId String userId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(defaultValue = "LATEST") SortType sortType) {
        return indicatorService.getFavoritesAll(userId, page, size, sortType);
    }
}
