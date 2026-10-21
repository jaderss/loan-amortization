package dev.jaderss.amortization.installments;

import dev.jaderss.amortization.money.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Currency;
import java.util.List;

public record InstallmentRequest(
        @NotNull Currency currency,
        @NotEmpty List<@Valid Coverage> coverages,
        @NotEmpty List<@Valid InstallmentOption> options) {

    @AssertTrue(message = "coverage amounts must not have more decimal places than the currency allows")
    boolean isCoveragesInCurrencyScale() {
        return currency == null
                || coverages == null
                || coverages.stream()
                        .allMatch(coverage -> coverage.amount() == null || Money.fits(coverage.amount(), currency));
    }
}
