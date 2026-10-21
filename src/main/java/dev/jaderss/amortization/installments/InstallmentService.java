package dev.jaderss.amortization.installments;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
public class InstallmentService {

    public List<InstallmentPlan> plans(List<Coverage> coverages, List<InstallmentOption> options) {
        rejectOverlaps(options);
        BigDecimal premium = coverages.stream().map(Coverage::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        int longest = options.stream()
                .mapToInt(InstallmentOption::maxInstallments)
                .max()
                .orElse(1);
        return IntStream.rangeClosed(1, longest)
                .mapToObj(installments -> plan(premium, rateFor(options, installments), installments))
                .toList();
    }

    private static InstallmentPlan plan(BigDecimal premium, BigDecimal rate, int installments) {
        BigDecimal total = rate.signum() > 0
                ? premium.multiply(BigDecimal.ONE.add(rate).pow(installments)).setScale(2, RoundingMode.HALF_EVEN)
                : premium;
        BigDecimal installment = total.divide(BigDecimal.valueOf(installments), 2, RoundingMode.DOWN);
        BigDecimal first = total.subtract(installment.multiply(BigDecimal.valueOf(installments - 1)));
        return new InstallmentPlan(installments, first, installments > 1 ? installment : null, total);
    }

    private static BigDecimal rateFor(List<InstallmentOption> options, int installments) {
        return options.stream()
                .filter(option -> option.covers(installments))
                .map(InstallmentOption::rate)
                .findFirst()
                .orElse(BigDecimal.ZERO);
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
