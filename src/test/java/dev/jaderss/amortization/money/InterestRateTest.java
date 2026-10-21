package dev.jaderss.amortization.money;

import static dev.jaderss.amortization.Samples.ONE_PERCENT;
import static dev.jaderss.amortization.Samples.brl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class InterestRateTest {

    @Test
    void rejectsNegativeRates() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new InterestRate(new BigDecimal("-0.001")))
                .withMessage("Interest rate must not be negative, was -0.001");
    }

    @Test
    void growthKeepsThirtyFourSignificantDigits() {
        assertThat(new InterestRate(new BigDecimal("0.0123456789")).growth(360).precision())
                .isEqualTo(InterestRate.CONTEXT.getPrecision());
    }

    @Test
    void compoundsBeforeRoundingToTheCent() {
        assertThat(ONE_PERCENT.compound(brl("468.57"), 7)).isEqualTo(brl("502.37"));
        assertThat(InterestRate.ZERO.compound(brl("468.57"), 12)).isEqualTo(brl("468.57"));
    }

    @Test
    void chargesOnePeriodOfInterestOnABalance() {
        assertThat(ONE_PERCENT.interestOn(brl("1000.50"))).isEqualTo(brl("10.00"));
        assertThat(ONE_PERCENT.interestOn(brl("1001.50"))).isEqualTo(brl("10.02"));
    }
}
