package dev.jaderss.amortization.loan;

import dev.jaderss.amortization.money.Money;

public record Installment(int number, Money payment, Money interest, Money amortization, Money balance) {}
