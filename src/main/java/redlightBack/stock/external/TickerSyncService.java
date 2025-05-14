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
import java.util.List;
import java.util.stream.Stream;


@RequiredArgsConstructor
@Service
public class TickerSyncService {

    private final String apiKey = "VZ23cjUwTpLyaNi16WryRKfQf1vOmAjg";

    private final StockRepository stockRepository;

    // 동기화 엔드포인트를 직접 호출
    @Scheduled(cron = "0 0 3 * * *")
    public void sync() {
        // 1) NASDAQ 상위 1000개 시가총액 대형주
        List<SymbolDTO> nasdaq = WebClient.create()
                .get()
                .uri("https://financialmodelingprep.com/api/v3/stock-screener"
                        + "?exchange=NASDAQ"
                        + "&limit=1000"
                        + "&sort=marketCap"
                        + "&order=desc"
                        + "&marketCapMoreThan=1"
                        + "&apikey=" + apiKey)
                .retrieve()
                .bodyToFlux(SymbolDTO.class)
                .collectList()
                .block();

        // 2) NYSE 상위 1000개
        List<SymbolDTO> nyse = WebClient.create()
                .get()
                .uri("https://financialmodelingprep.com/api/v3/stock-screener"
                        + "?exchange=NYSE"
                        + "&limit=1000"
                        + "&sort=marketCap"
                        + "&order=desc"
                        + "&apikey=" + apiKey)
                .retrieve()
                .bodyToFlux(SymbolDTO.class)
                .collectList()
                .block();

        // 3) 중복 제거 후 심볼 목록 추출
        List<String> symbols = Stream.concat(nasdaq.stream(), nyse.stream())
                .map(SymbolDTO::symbol)
                .distinct()
                .toList();

        // 4) 1000개씩 잘라서 quote API 호출
        for (List<String> chunk : Lists.partition(symbols, 1000)) {
            String joined = String.join(",", chunk);
            List<QuoteDTO> quotes = WebClient.create()
                    .get()
                    .uri("https://financialmodelingprep.com/api/v3/quote/"
                            + joined
                            + "?apikey=" + apiKey)
                    .retrieve()
                    .bodyToFlux(QuoteDTO.class)
                    .collectList()
                    .block();

            if (quotes != null) {
                quotes.stream()
                        .filter(q -> !q.name().contains("Fund"))
                        .filter(q-> !q.name().contains("Vanguard"))
                        .filter(q -> q.marketCap() != null)                                  // null 제거
                        .filter(q -> q.marketCap().compareTo(BigDecimal.ZERO) > 0)            // 0 이하 제거
                        .forEach(q -> {
                            stockRepository.findBySymbol(q.symbol())
                                    .ifPresentOrElse(
                                            existing -> existing.update(q.name(), q.marketCap()),
                                            () -> stockRepository.save(new Stock(q.symbol(), q.name(), q.marketCap()))
                                    );
                        });
            }
        }
    }
}

