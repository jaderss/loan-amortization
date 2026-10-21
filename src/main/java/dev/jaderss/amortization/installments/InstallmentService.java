package dev.jaderss.amortization.installments;

import dev.jaderss.amortization.money.InterestRate;
import dev.jaderss.amortization.money.Money;
import java.util.Currency;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
public class InstallmentService {

    public List<InstallmentPlan> plans(Currency currency, List<Coverage> coverages, List<InstallmentOption> options) {
        rejectOverlaps(options);
        Money premium = coverages.stream()
                .map(coverage -> new Money(coverage.amount(), currency))
                .reduce(Money.zero(currency), Money::plus);
        int longest = options.stream()
                .mapToInt(InstallmentOption::maxInstallments)
                .max()
                .orElse(1);
        return IntStream.rangeClosed(1, longest)
                .mapToObj(installments -> plan(premium, rateFor(options, installments), installments))
                .toList();
    }

    private static InstallmentPlan plan(Money premium, InterestRate rate, int installments) {
        Money total = rate.compound(premium, installments);
        return InstallmentPlan.of(total, total.split(installments));
    }

    private static InterestRate rateFor(List<InstallmentOption> options, int installments) {
        return options.stream()
                .filter(option -> option.covers(installments))
                .map(option -> new InterestRate(option.rate()))
                .findFirst()
                .orElse(InterestRate.ZERO);
    }

    private static void rejectOverlaps(List<InstallmentOption> options) {
        for (int i = 0; i < options.size(); i++) {
            for (int j = i + 1; j < options.size(); j++) {
                if (options.get(i).overlaps(options.get(j))) {
                    throw new OverlappingOptionsException(options.get(i), options.get(j));
                }
            }
        }
    }
}
