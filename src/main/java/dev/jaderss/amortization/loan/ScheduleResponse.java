package dev.jaderss.amortization.loan;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

public record ScheduleResponse(
        AmortizationSystem system,
        Currency currency,
        BigDecimal totalPaid,
        BigDecimal totalInterest,
        List<Row> installments) {

    static ScheduleResponse of(AmortizationSystem system, Currency currency, Schedule schedule) {
        return new ScheduleResponse(
                system,
                currency,
                schedule.totalPaid().amount(),
                schedule.totalInterest().amount(),
                schedule.installments().stream().map(Row::of).toList());
    }

    public record Row(
            int number, BigDecimal payment, BigDecimal interest, BigDecimal amortization, BigDecimal balance) {

        static Row of(Installment installment) {
            return new Row(
                    installment.number(),
                    installment.payment().amount(),
                    installment.interest().amount(),
                    installment.amortization().amount(),
                    installment.balance().amount());
        }
    }
}
