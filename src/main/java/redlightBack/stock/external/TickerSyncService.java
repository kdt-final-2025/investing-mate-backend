package redlightBack.stock.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import redlightBack.stock.Stock;
import redlightBack.stock.StockRepository;
import redlightBack.stock.dto.QuoteDTO;
import redlightBack.stock.dto.SymbolDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


@RequiredArgsConstructor
@Service
public class TickerSyncService {

    private final String apiKey = "VZ23cjUwTpLyaNi16WryRKfQf1vOmAjg";

    private final StockRepository stockRepository;

    // 동기화 엔드포인트를 직접 호출
    @Scheduled(cron = "0 0 3 * * *")
    public void sync() {
        // 1) NASDAQ, 2) NYSE 스크리너 호출
        List<SymbolDTO> nasdaq = fetchSymbols("NASDAQ");
        List<SymbolDTO> nyse = fetchSymbols("NYSE");

        // 3) 심볼 → 거래소 맵
        Map<String, String> symbolToExchange = new HashMap<>();
        nasdaq.forEach(s -> symbolToExchange.put(s.symbol(), "NASDAQ"));
        nyse.forEach(s -> symbolToExchange.put(s.symbol(), "NYSE"));

        // 4) 중복 제거된 심볼 리스트
        List<String> symbols = new ArrayList<>(symbolToExchange.keySet());

        // 5) 1000개씩 잘라서 시가총액 조회
        for (List<String> chunk : Lists.partition(symbols, 1000)) {
            List<QuoteDTO> quotes = WebClient.create()
                    .get()
                    .uri("https://financialmodelingprep.com/api/v3/quote/"
                            + String.join(",", chunk)
                            + "?apikey=" + apiKey)
                    .retrieve()
                    .bodyToFlux(QuoteDTO.class)
                    .collectList()
                    .block();

            if (quotes != null) {
                quotes.stream()
                        .filter(q -> !q.name().contains("Fund"))
                        .filter(q-> !q.name().contains("Vanguard"))
                        .filter(q -> q.marketCap() != null && q.marketCap().compareTo(BigDecimal.ZERO) > 0)
                        .forEach(q -> {
                            String exch = q.exchange() != null
                                    ? q.exchange()
                                    : symbolToExchange.getOrDefault(q.symbol(), "UNKNOWN");

                            stockRepository.findBySymbol(q.symbol())
                                    .ifPresentOrElse(
                                            existing -> existing.update(q.name(), q.marketCap(), exch),
                                            () -> stockRepository.save(
                                                    new Stock(q.symbol(), q.name(), q.marketCap(), exch))
                                    );
                        });
            }
        }
    }

    private List<SymbolDTO> fetchSymbols(String exchange) {
        return WebClient.create()
                .get()
                .uri("https://financialmodelingprep.com/api/v3/stock-screener"
                        + "?exchange=" + exchange
                        + "&limit=1000&sort=marketCap&order=desc"
                        + "&apikey=" + apiKey)
                .retrieve()
                .bodyToFlux(SymbolDTO.class)
                .collectList()
                .block();
    }
}

