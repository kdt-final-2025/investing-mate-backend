package redlightBack.indicator;

import com.querydsl.core.types.OrderSpecifier;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
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
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
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
            // 1) API에서 받은 이름 리스트
            List<String> apiNames = Arrays.asList(resp.getBody());

            if (apiNames.isEmpty()) {
                return;
            }

            // 2) DB에서 이미 저장된 지표 이름만 한 번에 조회
            Set<String> existingNames = indicatorRepository
                    .findAllByNameIn(apiNames)
                    .stream()
                    .map(Indicator::getName)
                    .collect(Collectors.toSet());

            // 3) 기존에 없던(신규) 이름만 Indicator 객체로 매핑
            List<Indicator> newIndicators = apiNames.stream()
                    .filter(name -> !existingNames.contains(name))
                    .map(Indicator::new)              // nextReleaseDate는 null로 생성
                    .collect(Collectors.toList());

            // 4) 신규 지표가 있으면 한 번에 저장
            if (!newIndicators.isEmpty()) {
                indicatorRepository.saveAll(newIndicators);
            }
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
