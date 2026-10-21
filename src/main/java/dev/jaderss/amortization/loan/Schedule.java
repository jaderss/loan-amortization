package dev.jaderss.amortization.loan;

import dev.jaderss.amortization.money.InterestRate;
import dev.jaderss.amortization.money.Money;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record Schedule(List<Installment> installments) {

    public Schedule {
        if (installments.isEmpty()) {
            throw new IllegalArgumentException("A schedule needs at least one installment");
        }
        installments = List.copyOf(installments);
    }

    public Money totalPaid() {
        return total(Installment::payment);
    }

    public Money totalInterest() {
        return total(Installment::interest);
    }

    static Schedule amortize(Money principal, InterestRate rate, int months, Amortization amortization) {
        List<Installment> installments = new ArrayList<>(months);
        Money balance = principal;
        for (int number = 1; number <= months; number++) {
            Money interest = rate.interestOn(balance);
            Money amortized = amortization.next(number, interest, balance);
            balance = balance.minus(amortized);
            installments.add(new Installment(number, amortized.plus(interest), interest, amortized, balance));
        }
        return new Schedule(installments);
    }

    private Money total(Function<Installment, Money> amount) {
        return installments.stream().map(amount).reduce(Money::plus).orElseThrow();
    }

    @FunctionalInterface
    interface Amortization {
        Money next(int number, Money interest, Money balance);
    }
}
