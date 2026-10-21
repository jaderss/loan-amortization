package dev.jaderss.amortization.installments;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record InstallmentOption(
        @NotNull @Min(1) @Max(600) Integer minInstallments,
        @NotNull @Min(1) @Max(600) Integer maxInstallments,
        @NotNull @DecimalMin("0") @DecimalMax("1") BigDecimal rate) {

    @AssertTrue(message = "minInstallments must not exceed maxInstallments")
    boolean isOrdered() {
        return minInstallments == null || maxInstallments == null || minInstallments <= maxInstallments;
    }

    boolean covers(int installments) {
        return minInstallments <= installments && installments <= maxInstallments;
    }

    boolean overlaps(InstallmentOption other) {
        return minInstallments <= other.maxInstallments && other.minInstallments <= maxInstallments;
    }
}
