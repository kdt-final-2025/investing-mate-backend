package redlightBack.stock.external;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import redlightBack.stock.Stock;
import redlightBack.stock.StockRepository;
import redlightBack.stock.dto.QuoteDTO;
import redlightBack.stock.dto.SymbolDTO;

import java.util.List;


@RequiredArgsConstructor
@Service
public class TickerSyncService {

    private final FmpClient fmpClient;
    private final StockRepository stockRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void sync() {
        // 1) 심볼 리스트 가져와서 NYSE/NASDAQ 필터
        List<String> symbols = fmpClient.fetchAllSymbols().stream()
                .filter(s -> {
                    String ex = s.exchange();
                    return ex != null
                            && (ex.equals("NYSE") || ex.equals("NASDAQ"));
                })
                .map(SymbolDTO::symbol)
                .toList();

        // 2) 1,000개씩 잘라서 quote API 호출
        for (List<String> chunk : Lists.partition(symbols, 1000)) {
            List<QuoteDTO> quotes = fmpClient.fetchQuotes(chunk);
            // 3) DB 저장/업데이트
            quotes.forEach(q -> {
                stockRepository.findBySymbol(q.symbol())
                        .ifPresentOrElse(
                                t -> t.update(q.name(), q.marketCap()),
                                () -> stockRepository.save(new Stock(q.symbol(), q.name(), q.marketCap()))
                        );
            });
        }
    }
}
