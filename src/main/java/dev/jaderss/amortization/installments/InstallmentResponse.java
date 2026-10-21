package dev.jaderss.amortization.installments;

import java.util.Currency;
import java.util.List;

public record InstallmentResponse(Currency currency, List<InstallmentPlan> plans) {}
