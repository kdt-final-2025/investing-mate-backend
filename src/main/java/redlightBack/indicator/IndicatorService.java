package redlightBack.indicator;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IndicatorService {

    private final RestTemplate restTemplate;
    private final IndicatorRepository repo;

    public IndicatorService(RestTemplate restTemplate, IndicatorRepository repo) {
        this.restTemplate = restTemplate;
        this.repo = repo;
    }

    public void fetchAndSaveAll() {
        String url = "https://data360api.worldbank.org/data360/indicators?datasetId=WB_WDI";
        ResponseEntity<String[]> resp = restTemplate.getForEntity(url, String[].class);

        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            List<Indicator> indicators = Arrays.stream(resp.getBody())
                    // name: 코드 그대로, nextReleaseDate: null (추후에 업데이트)
                    .map(code -> new Indicator(code, null))
                    .collect(Collectors.toList());

            repo.saveAll(indicators);
        }
    }
}
