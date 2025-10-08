package com.frisk.cadettsplitterspadel.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class FxService {

    private final RestClient http;

    public FxService(RestClient.Builder builder,
                     @Value("${fx.base-url:https://api.frankfurter.app}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
    }

    public BigDecimal sekToEur(BigDecimal amountSek) {
        FxResponse resp = http.get()
                .uri(uri -> uri.path("/latest")
                        .queryParam("amount", amountSek)
                        .queryParam("from", "SEK")
                        .queryParam("to", "EUR")
                        .build())
                .retrieve()
                .body(FxResponse.class);

        if (resp == null || resp.rates == null || resp.rates.get("EUR") == null) {
            throw new IllegalStateException("FX response missing EUR rate");
        }
        return resp.rates.get("EUR");
    }

    public static class FxResponse {
        public BigDecimal amount;
        public String base;
        public String date;
        public Map<String, BigDecimal> rates;
    }
}