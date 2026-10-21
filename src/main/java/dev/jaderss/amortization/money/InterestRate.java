package dev.jaderss.amortization.money;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.math.MathContext;

public record InterestRate(BigDecimal perPeriod) {

    public static final MathContext CONTEXT = MathContext.DECIMAL128;

    public static final InterestRate ZERO = new InterestRate(BigDecimal.ZERO);

    public InterestRate {
        requireNonNull(perPeriod, "perPeriod");
        if (perPeriod.signum() < 0) {
            throw new IllegalArgumentException("Interest rate must not be negative, was " + perPeriod);
        }
    }

    public boolean isZero() {
        return perPeriod.signum() == 0;
    }

    public BigDecimal growth(int periods) {
        return BigDecimal.ONE.add(perPeriod).pow(periods, CONTEXT);
    }

    public Money interestOn(Money balance) {
        return balance.times(perPeriod);
    }

    public Money compound(Money principal, int periods) {
        return principal.times(growth(periods));
    }
}
