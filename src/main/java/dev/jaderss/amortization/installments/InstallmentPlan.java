package dev.jaderss.amortization.installments;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InstallmentPlan(
        int installments, BigDecimal firstInstallment, BigDecimal otherInstallments, BigDecimal total) {}
