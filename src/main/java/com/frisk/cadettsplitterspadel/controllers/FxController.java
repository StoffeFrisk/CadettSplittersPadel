package com.frisk.cadettsplitterspadel.controllers;

import com.frisk.cadettsplitterspadel.services.FxService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@RestController
@RequestMapping("/api/wigellpadel/fx")
public class FxController {

    private final FxService fx;

    public FxController(FxService fx) {
        this.fx = fx;
    }

    @GetMapping("/sek-to-eur")
    public Map<String, Object> sekToEur(@RequestParam("amount") BigDecimal amountSek) {
        if (amountSek == null || amountSek.signum() < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        var eur = fx.sekToEur(amountSek).setScale(2, RoundingMode.HALF_UP);
        return Map.of(
                "amountSek", amountSek,
                "amountEur", eur,
                "provider", "frankfurter"
        );
    }
}