package dev.jaderss.amortization.loan;

import static java.util.Objects.requireNonNull;

import dev.jaderss.amortization.money.InterestRate;
import dev.jaderss.amortization.money.Money;
import java.util.List;

public enum AmortizationSystem {
    PRICE {
        @Override
        Schedule amortize(Money principal, InterestRate rate, int months) {
            Money payment = principal.times(rate.annuityFactor(months));
            return Schedule.amortize(
                    principal,
                    rate,
                    months,
                    (number, interest, balance) ->
                            number == months ? balance : payment.minus(interest).min(balance));
        }
    },
    SAC {
        @Override
        Schedule amortize(Money principal, InterestRate rate, int months) {
            List<Money> amortizations = principal.split(months);
            return Schedule.amortize(
                    principal, rate, months, (number, interest, balance) -> amortizations.get(number - 1));
        }
    };

    public Schedule schedule(Money principal, InterestRate rate, int months) {
        requireNonNull(principal, "principal");
        requireNonNull(rate, "rate");
        if (months < 1) {
            throw new IllegalArgumentException("months must be positive, was " + months);
        }
        if (principal.isNegative() || principal.isZero()) {
            throw new IllegalArgumentException("principal must be positive, was " + principal.amount());
        }
        return amortize(principal, rate, months);
    }

    abstract Schedule amortize(Money principal, InterestRate rate, int months);
}
