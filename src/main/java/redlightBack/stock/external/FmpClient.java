package redlightBack.stock.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import redlightBack.stock.dto.QuoteDTO;
import redlightBack.stock.dto.SymbolDTO;

import java.util.List;

@RequiredArgsConstructor
@Service
public class FmpClient {

    private final WebClient webClient;
    private final FmpProperties fmpProperties;

    public List<SymbolDTO> fetchAllSymbols() {
        return webClient.get()
                .uri("https://financialmodelingprep.com/api/v3/stock/list?apikey={key}", fmpProperties.getKey())
                .retrieve().bodyToFlux(SymbolDTO.class)
                .collectList().block();
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
