package redlightBack.stock.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import redlightBack.stock.dto.QuoteDTO;
import redlightBack.stock.dto.SymbolDTO;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class FmpClient {

    private final WebClient webClient;
    private final FmpProperties fmpProperties;

    public List<SymbolDTO> fetchTopByMarketCap(String exchange) {
        return webClient.get()
                .uri(uri -> uri
                        .scheme("https")
                        .host("financialmodelingprep.com")
                        .path("/api/v3/stock-screener")
                        .queryParam("exchange", exchange)
                        .queryParam("limit", "1000")
                        .queryParam("sort", "marketCap")
                        .queryParam("order", "desc")
                        .queryParam("apikey", fmpProperties.getKey())
                        .build())
                .retrieve()
                .bodyToFlux(SymbolDTO.class)
                .collectList()
                .block();
    }

    public List<SymbolDTO> fetchAllTopLargeCaps() {
        List<SymbolDTO> nasdaq = fetchTopByMarketCap("NASDAQ");
        List<SymbolDTO> nyse = fetchTopByMarketCap("NYSE");
        return Stream.concat(nasdaq.stream(), nyse.stream())
                .distinct()
                .toList();
    }


    public List<QuoteDTO> fetchQuotes(List<String> symbols) {
        String joined = String.join(",", symbols);
        return webClient.get()
                .uri("https://financialmodelingprep.com/api/v3/quote/{syms}?apikey={key}",
                        joined, fmpProperties.getKey())
                .retrieve()
                .bodyToFlux(QuoteDTO.class)
                .collectList()
                .block();
    }
}
