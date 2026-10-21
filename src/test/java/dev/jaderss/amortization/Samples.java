package dev.jaderss.amortization;

import dev.jaderss.amortization.money.InterestRate;
import dev.jaderss.amortization.money.Money;
import java.math.BigDecimal;
import java.util.Currency;

public final class Samples {

    public static final Currency BRL = Currency.getInstance("BRL");

    public static final InterestRate ONE_PERCENT = new InterestRate(new BigDecimal("0.01"));

    private Samples() {}

    public static Money brl(String amount) {
        return new Money(new BigDecimal(amount), BRL);
    }
}
