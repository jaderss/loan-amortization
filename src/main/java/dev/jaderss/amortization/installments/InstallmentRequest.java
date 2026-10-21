package dev.jaderss.amortization.installments;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record InstallmentRequest(
        @NotEmpty List<@Valid Coverage> coverages, @NotEmpty List<@Valid InstallmentOption> options) {}
