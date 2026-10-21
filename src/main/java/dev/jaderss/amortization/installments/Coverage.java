package dev.jaderss.amortization.installments;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record Coverage(
        @NotNull Integer id, @NotNull @DecimalMin("0.01") BigDecimal amount) {}
