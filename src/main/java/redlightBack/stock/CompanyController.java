package redlightBack.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import redlightBack.stock.dto.CompanyMetric;
import redlightBack.stock.dto.CompanyProfile;

@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/api/company/{symbol}")
    public ResponseEntity<CompanyProfile> getProfile(@PathVariable String symbol) {
        CompanyProfile profile = companyService.getProfile(symbol);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/api/company/{symbol}/metrics")
    public ResponseEntity<CompanyMetric> getMetrics(@PathVariable String symbol) {
        CompanyMetric metric = companyService.getMetric(symbol);
        return ResponseEntity.ok(metric);
    }
}

