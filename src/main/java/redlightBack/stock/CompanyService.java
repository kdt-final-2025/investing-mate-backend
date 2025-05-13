package redlightBack.stock;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import redlightBack.stock.dto.CompanyMetric;
import redlightBack.stock.dto.CompanyProfile;
import redlightBack.stock.external.FinnhubClient;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final FinnhubClient client;
    private final StockRepository stockRepository;

    /**
     * 하루 1회: Company Profile 캐싱
     */
    @Cacheable(value = "companyProfile", key = "#symbol")
    public CompanyProfile getProfile(String symbol) {
        return client.fetchCompanyProfile(symbol);
    }

    /**
     * 하루 1회: Company Metric 캐싱
     */
    @Cacheable(value = "companyMetric", key = "#symbol")
    public CompanyMetric getMetric(String symbol) {
        return client.fetchCompanyMetric(symbol);
    }

}
