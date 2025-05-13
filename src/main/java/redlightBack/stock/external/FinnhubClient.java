package redlightBack.stock.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import redlightBack.common.FinnhubProperties;
import redlightBack.stock.dto.CompanyMetric;
import redlightBack.stock.dto.CompanyProfile;

@Service
@RequiredArgsConstructor
public class FinnhubClient {
    private final WebClient webClient;
    private final FinnhubProperties props;

    /**
     * 회사 기본 정보 조회
     */
    public CompanyProfile fetchCompanyProfile(String symbol) {
        return webClient.get()
                .uri(uri -> uri
                        .path("/stock/profile2")
                        .queryParam("symbol", symbol)
                        .queryParam("token", props.getKey())
                        .build())
                .retrieve()
                .bodyToMono(CompanyProfile.class)
                .block();  // 블로킹 호출. 필요에 따라 .subscribe() 등 논블로킹으로도 사용 가능
    }

    /**
     * 재무/밸류에이션 지표 조회
     */
    public CompanyMetric fetchCompanyMetric(String symbol) {
        return webClient.get()
                .uri(uri -> uri
                        .path("/stock/metric")
                        .queryParam("symbol", symbol)
                        .queryParam("metric", "all")
                        .queryParam("token", props.getKey())
                        .build())
                .retrieve()
                .bodyToMono(CompanyMetric.class)
                .block();
    }
}
