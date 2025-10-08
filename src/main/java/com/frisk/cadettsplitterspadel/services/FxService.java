package com.frisk.cadettsplitterspadel.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class FxService {

    private final RestClient http;

    public FxService(RestClient.Builder builder,
                     @Value("${fx.base-url:https://api.frankfurter.dev}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
    }

    public BigDecimal sekToEur(BigDecimal amountSek) {
        RatesResponse resp = http.get()
                .uri(uri -> uri.path("/v1/latest")
                        .queryParam("base", "SEK")
                        .queryParam("symbols", "EUR")
                        .build())
                .retrieve()
                .body(RatesResponse.class);

        if (resp == null || resp.rates == null || resp.rates.get("EUR") == null) {
            throw new IllegalStateException("FX response missing EUR rate");
        }
        BigDecimal rate = resp.rates.get("EUR");
        return amountSek.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public static class RatesResponse {
        public String base;
        public String date;
        public Map<String, BigDecimal> rates;
    }
}