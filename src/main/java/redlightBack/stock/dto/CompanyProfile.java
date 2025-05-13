package redlightBack.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CompanyProfile(
        String country,
        String currency,
        String exchange,
        String name,
        String ticker,
        @JsonProperty("marketCapitalization")
        Long marketCap,
        @JsonProperty("shareOutstanding")
        Double sharesOutstanding,
        String finnhubIndustry,
        String ipo,
        String logo,
        String weburl,
        String phone
) {}

