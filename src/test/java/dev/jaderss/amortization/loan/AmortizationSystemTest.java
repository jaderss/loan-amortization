package dev.jaderss.amortization.loan;

import static dev.jaderss.amortization.Samples.ONE_PERCENT;
import static dev.jaderss.amortization.Samples.brl;
import static dev.jaderss.amortization.loan.AmortizationSystem.PRICE;
import static dev.jaderss.amortization.loan.AmortizationSystem.SAC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import dev.jaderss.amortization.money.InterestRate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class AmortizationSystemTest {

    @Test
    void priceChargesEqualPaymentsAndSettlesRoundingInTheLast() {
        Schedule schedule = PRICE.schedule(brl("1000.00"), ONE_PERCENT, 3);

        assertThat(schedule.installments())
                .containsExactly(
                        new Installment(1, brl("340.02"), brl("10.00"), brl("330.02"), brl("669.98")),
                        new Installment(2, brl("340.02"), brl("6.70"), brl("333.32"), brl("336.66")),
                        new Installment(3, brl("340.03"), brl("3.37"), brl("336.66"), brl("0.00")));
        assertThat(schedule.totalPaid()).isEqualTo(brl("1020.07"));
        assertThat(schedule.totalInterest()).isEqualTo(brl("20.07"));
    }

    @Test
    void priceMatchesTheAnnuityFormulaOnALongerLoan() {
        assertThat(PRICE.schedule(brl("100000.00"), ONE_PERCENT, 12)
                        .installments()
                        .getFirst())
                .isEqualTo(new Installment(1, brl("8884.88"), brl("1000.00"), brl("7884.88"), brl("92115.12")));
    }

    @Test
    void sacAmortizesEquallyWithFallingInterest() {
        Schedule schedule = SAC.schedule(brl("120000.00"), ONE_PERCENT, 12);

        assertThat(schedule.installments().getFirst())
                .isEqualTo(new Installment(1, brl("11200.00"), brl("1200.00"), brl("10000.00"), brl("110000.00")));
        assertThat(schedule.installments().getLast())
                .isEqualTo(new Installment(12, brl("10100.00"), brl("100.00"), brl("10000.00"), brl("0.00")));
        assertThat(schedule.totalInterest()).isEqualTo(brl("7800.00"));
    }

    @Test
    void sacPutsTheCentsThatDoNotDivideEvenlyOnTheFirstAmortization() {
        assertThat(SAC.schedule(brl("100.00"), InterestRate.ZERO, 3).installments())
                .extracting(Installment::amortization)
                .containsExactly(brl("33.34"), brl("33.33"), brl("33.33"));
    }

    @ParameterizedTest
    @EnumSource
    void chargesNoInterestAtAZeroRate(AmortizationSystem system) {
        Schedule schedule = system.schedule(brl("100.00"), InterestRate.ZERO, 3);

        assertThat(schedule.totalInterest()).isEqualTo(brl("0.00"));
        assertThat(schedule.totalPaid()).isEqualTo(brl("100.00"));
    }

    @ParameterizedTest
    @EnumSource
    void rejectsLoansWithoutMonthsOrPrincipal(AmortizationSystem system) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> system.schedule(brl("100.00"), ONE_PERCENT, 0))
                .withMessage("months must be positive, was 0");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> system.schedule(brl("0.00"), ONE_PERCENT, 12))
                .withMessage("principal must be positive, was 0.00");
    }
}
