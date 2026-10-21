package dev.jaderss.amortization.loan;

import dev.jaderss.amortization.money.Money;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Currency;

public record ScheduleRequest(
        @NotNull AmortizationSystem system,
        @NotNull Currency currency,
        @NotNull @DecimalMin("0.01") BigDecimal principal,
        @NotNull @DecimalMin("0") @DecimalMax("1") BigDecimal monthlyRate,
        @NotNull @Min(1) @Max(600) Integer months) {

    @AssertTrue(message = "currency must have a minor unit")
    boolean isCurrencyWithMinorUnit() {
        return currency == null || Money.hasMinorUnit(currency);
    }

    @AssertTrue(message = "principal must not have more decimal places than the currency allows")
    boolean isPrincipalInCurrencyScale() {
        return currency == null
                || !Money.hasMinorUnit(currency)
                || principal == null
                || Money.fits(principal, currency);
    }
}
