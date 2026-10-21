package dev.jaderss.amortization.money;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    public Money {
        requireNonNull(amount, "amount");
        requireNonNull(currency, "currency");
        if (!fits(amount, currency)) {
            throw new IllegalArgumentException(
                    "%s has more decimal places than %s allows".formatted(amount.toPlainString(), currency));
        }
        amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.UNNECESSARY);
    }

    public static boolean fits(BigDecimal amount, Currency currency) {
        return amount.stripTrailingZeros().scale() <= currency.getDefaultFractionDigits();
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money plus(Money other) {
        return new Money(amount.add(sameCurrency(other).amount), currency);
    }

    public Money minus(Money other) {
        return new Money(amount.subtract(sameCurrency(other).amount), currency);
    }

    public Money times(BigDecimal factor) {
        return new Money(rounded(amount.multiply(factor)), currency);
    }

    private Money times(int factor) {
        return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
    }

    public List<Money> split(int parts) {
        if (parts < 1) {
            throw new IllegalArgumentException("parts must be positive, was " + parts);
        }
        Money share = new Money(amount.divide(BigDecimal.valueOf(parts), scale(), RoundingMode.DOWN), currency);
        List<Money> shares = new ArrayList<>(Collections.nCopies(parts, share));
        shares.set(0, minus(share.times(parts - 1)));
        return List.copyOf(shares);
    }

    public Money min(Money other) {
        return compareTo(other) <= 0 ? this : other;
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(sameCurrency(other).amount);
    }

    private BigDecimal rounded(BigDecimal value) {
        return value.setScale(scale(), RoundingMode.HALF_EVEN);
    }

    private int scale() {
        return currency.getDefaultFractionDigits();
    }

    private Money sameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot combine %s with %s".formatted(currency, other.currency));
        }
        return other;
    }
}
