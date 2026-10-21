package dev.jaderss.amortization.installments;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.jaderss.amortization.money.Money;
import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InstallmentPlan(
        int installments, BigDecimal firstInstallment, BigDecimal otherInstallments, BigDecimal total) {

    static InstallmentPlan of(Money total, List<Money> installments) {
        return new InstallmentPlan(
                installments.size(),
                installments.getFirst().amount(),
                installments.size() > 1 ? installments.getLast().amount() : null,
                total.amount());
    }
}
