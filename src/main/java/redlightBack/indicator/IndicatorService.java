package redlightBack.indicator;

import com.querydsl.core.types.OrderSpecifier;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redlightBack.indicator.dto.FavoriteIndicatorRequest;
import redlightBack.indicator.dto.FavoriteIndicatorResponse;
import redlightBack.indicator.dto.FavoriteIndicatorsListResponse;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class IndicatorService {

    private final RestTemplate restTemplate;
    private final IndicatorRepository indicatorRepository;
    private final FavoriteIndicatorRepository favoriteIndicatorRepository;
    private final IndicatorQueryRepository indicatorQueryRepository;

    @Scheduled(cron = "0 0 0 1 * *")
    public void fetchAndSaveAll() {
        String url = "https://data360api.worldbank.org/data360/indicators?datasetId=WB_WDI";
        ResponseEntity<String[]> resp = restTemplate.getForEntity(url, String[].class);

        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            List<Indicator> indicators = Arrays.stream(resp.getBody())
                    // name: 코드 그대로, nextReleaseDate: null (추후에 업데이트)
                    .map(Indicator::new)
                    .collect(Collectors.toList());

            indicatorRepository.saveAll(indicators);
        }
    }

    @Transactional
    public void createFavoriteIndicator(String userId, FavoriteIndicatorRequest request) {
        Indicator indicator = indicatorRepository.findById(request.indicatorId()).orElseThrow(
                () -> new NoSuchElementException("해당하는 경제 지표가 없습니다."));
        favoriteIndicatorRepository.save(new FavoriteIndicator(indicator, userId));
    }

    public FavoriteIndicatorsListResponse getFavoritesAll(String userId,
                                                          int page,
                                                          int size,
                                                          SortType sortType) {
        OrderSpecifier<?> orderSpecifier = sortType.getOrder(QFavoriteIndicator.favoriteIndicator);
        long totalCount = indicatorQueryRepository.totalCount(userId);
        long offset = (long) (page - 1) * size;
        List<FavoriteIndicator> favoriteIndicators = indicatorQueryRepository.getAll(
                userId,
                orderSpecifier,
                offset,
                size);
        List<FavoriteIndicatorResponse> responses = favoriteIndicators.stream()
                .map(favoriteIndicator -> new FavoriteIndicatorResponse(
                        favoriteIndicator.getId(),
                        favoriteIndicator.getIndicator().getName()))
                .toList();
        int totalPages = (int) Math.ceil((double) totalCount / size);
        return new FavoriteIndicatorsListResponse(
                responses,
                page,
                size,
                totalPages,
                totalCount);
    }
}
