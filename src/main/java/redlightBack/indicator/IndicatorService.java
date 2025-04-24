package redlightBack.indicator;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redlightBack.indicator.dto.FavoriteIndicatorRequest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class IndicatorService {

    private final RestTemplate restTemplate;
    private final IndicatorRepository indicatorRepository;
    private final FavoriteIndicatorRepository favoriteIndicatorRepository;

    public IndicatorService(RestTemplate restTemplate, IndicatorRepository indicatorRepository, FavoriteIndicatorRepository favoriteIndicatorRepository) {
        this.restTemplate = restTemplate;
        this.indicatorRepository = indicatorRepository;
        this.favoriteIndicatorRepository = favoriteIndicatorRepository;
    }

    public void fetchAndSaveAll() {
        String url = "https://data360api.worldbank.org/data360/indicators?datasetId=WB_WDI";
        ResponseEntity<String[]> resp = restTemplate.getForEntity(url, String[].class);

        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            List<Indicator> indicators = Arrays.stream(resp.getBody())
                    // name: 코드 그대로, nextReleaseDate: null (추후에 업데이트)
                    .map(code -> new Indicator(code, null))
                    .collect(Collectors.toList());

            indicatorRepository.saveAll(indicators);
        }
    }

    public void createFavoriteIndicator(String userId, FavoriteIndicatorRequest request) {
        Indicator indicator = indicatorRepository.findById(request.indicatorId()).orElseThrow(
                () -> new NoSuchElementException("해당하는 경제 지표가 없습니다."));
        favoriteIndicatorRepository.save(new FavoriteIndicator(indicator, userId));
    }
}
