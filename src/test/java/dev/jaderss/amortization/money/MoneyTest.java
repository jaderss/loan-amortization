package dev.jaderss.amortization.money;

import static dev.jaderss.amortization.Samples.BRL;
import static dev.jaderss.amortization.Samples.brl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MoneyTest {

    private static final Currency JPY = Currency.getInstance("JPY");

    @Test
    void holdsAmountAtCurrencyScale() {
        assertThat(brl("10").amount().scale()).isEqualTo(2);
        assertThat(new Money(new BigDecimal("1500"), JPY).amount().scale()).isZero();
    }

    @Test
    void valuesWithDifferentTrailingZerosAreEqual() {
        assertThat(brl("10.5")).isEqualTo(brl("10.50"));
    }

    @Test
    void rejectsMoreDecimalPlacesThanCurrencyAllows() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> brl("10.005"))
                .withMessage("10.005 has more decimal places than BRL allows");
        assertThatIllegalArgumentException().isThrownBy(() -> new Money(new BigDecimal("1.5"), JPY));
    }

    @Test
    void rejectsCurrenciesWithoutAMinorUnit() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Money(new BigDecimal("1230"), Currency.getInstance("XAU")))
                .withMessage("XAU has no minor unit");
    }

    @Test
    void rejectsArithmeticAcrossCurrencies() {
        Money yen = new Money(BigDecimal.ONE, JPY);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> brl("1").plus(yen))
                .withMessage("Cannot combine BRL with JPY");
        assertThatIllegalArgumentException().isThrownBy(() -> brl("1").compareTo(yen));
    }

    @Test
    void addsAndSubtracts() {
        assertThat(brl("10.25").plus(brl("0.80"))).isEqualTo(brl("11.05"));
        assertThat(brl("10.25").minus(brl("10.30"))).isEqualTo(brl("-0.05"));
    }

    @ParameterizedTest
    @CsvSource({"10.00, 0.0125, 0.12", "10.00, 0.0135, 0.14", "10.00, 0.0145, 0.14", "0.01, 0.5, 0.00"})
    void multipliesRoundingHalfEven(String amount, String factor, String expected) {
        assertThat(brl(amount).times(new BigDecimal(factor))).isEqualTo(brl(expected));
    }

    @ParameterizedTest
    @CsvSource({"100.00, 3, 33.34, 33.33", "468.57, 2, 234.29, 234.28", "0.05, 4, 0.02, 0.01", "9.99, 1, 9.99, 9.99"})
    void splitsWithTheRemainderOnTheFirstShare(String amount, int parts, String first, String rest) {
        List<Money> shares = brl(amount).split(parts);

        assertThat(shares).hasSize(parts).startsWith(brl(first)).endsWith(brl(rest));
        assertThat(shares.stream().reduce(Money.zero(BRL), Money::plus)).isEqualTo(brl(amount));
    }

    @Test
    void rejectsSplittingIntoNoParts() {
        assertThatIllegalArgumentException().isThrownBy(() -> brl("1").split(0));
    }

    @Test
    void ordersByAmount() {
        assertThat(brl("1.10")).isGreaterThan(brl("1.09")).isEqualByComparingTo(brl("1.1"));
        assertThat(brl("-0.01").isNegative()).isTrue();
        assertThat(Money.zero(BRL).isZero()).isTrue();
    }
}
