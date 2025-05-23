package redlightBack.indicator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import redlightBack.indicator.dto.FavoriteIndicatorRequest;
import redlightBack.indicator.dto.FavoriteIndicatorsListResponse;
import redlightBack.indicator.dto.IndicatorListResponse;
import redlightBack.loginUtils.LoginMemberId;

@RequiredArgsConstructor
@RestController
public class IndicatorRestController {

    private final IndicatorService indicatorService;

    @GetMapping("/indicators/fetch")
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

                                                  @RequestParam(defaultValue = "desc") SortType sortType) {
        return indicatorService.getFavoritesAll(userId, page, size, sortType);
    }

    @GetMapping("/indicators")
    public IndicatorListResponse getAll(@LoginMemberId(required = false) String userId,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(defaultValue = "desc") String order) {
        return indicatorService.getAll(userId, page, size, order);
    }

    @DeleteMapping("/indicators/favorites/{indicatorId}")
    public void deleteById(@LoginMemberId String userId,
                           @PathVariable Long indicatorId) {
        indicatorService.deleteById(userId, indicatorId);
    }
}