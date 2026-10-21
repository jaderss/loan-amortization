package dev.jaderss.amortization.loan;

import static dev.jaderss.amortization.loan.AmortizationSystem.PRICE;
import static dev.jaderss.amortization.loan.AmortizationSystem.SAC;
import static org.assertj.core.api.Assertions.assertThat;

import dev.jaderss.amortization.money.InterestRate;
import dev.jaderss.amortization.money.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.stream.IntStream;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

class AmortizationPropertiesTest {

    record Loan(AmortizationSystem system, Money principal, InterestRate rate, int months) {

        Schedule schedule() {
            return system.schedule(principal, rate, months);
        }
    }

    @Provide
    Arbitrary<Loan> loans() {
        return Combinators.combine(Arbitraries.of(AmortizationSystem.class), principals(), rates(), months())
                .as(Loan::new);
    }

    @Provide
    Arbitrary<Loan> priceLoans() {
        return loans().map(loan -> new Loan(PRICE, loan.principal(), loan.rate(), loan.months()));
    }

    @Provide
    Arbitrary<Loan> sacLoans() {
        return loans().map(loan -> new Loan(SAC, loan.principal(), loan.rate(), loan.months()));
    }

    @Property
    void amortizationSumsToTheLoanAmount(@ForAll("loans") Loan loan) {
        assertThat(sum(loan.schedule().installments().stream()
                        .map(Installment::amortization)
                        .toList()))
                .isEqualTo(loan.principal());
    }

    @Property
    void balanceEndsAtZero(@ForAll("loans") Loan loan) {
        assertThat(loan.schedule().installments().getLast().balance().isZero()).isTrue();
    }

    @Property
    void noAmountIsEverNegative(@ForAll("loans") Loan loan) {
        assertThat(loan.schedule().installments())
                .flatExtracting(
                        Installment::payment, Installment::interest, Installment::amortization, Installment::balance)
                .noneMatch(amount -> ((Money) amount).isNegative());
    }

    @Property
    void everyPaymentIsInterestPlusAmortization(@ForAll("loans") Loan loan) {
        assertThat(loan.schedule().installments())
                .allSatisfy(installment -> assertThat(installment.payment())
                        .isEqualTo(installment.interest().plus(installment.amortization())));
    }

    @Property
    void hasOneInstallmentPerMonthWithAFallingBalance(@ForAll("loans") Loan loan) {
        List<Installment> installments = loan.schedule().installments();

        assertThat(installments).extracting(Installment::number).containsExactlyElementsOf(numbers(loan.months()));
        assertThat(installments)
                .extracting(Installment::balance)
                .isSortedAccordingTo((earlier, later) -> later.compareTo(earlier));
    }

    @Property
    void interestIsOneMonthOnTheOpeningBalance(@ForAll("loans") Loan loan) {
        Money opening = loan.principal();
        for (Installment installment : loan.schedule().installments()) {
            assertThat(installment.interest()).isEqualTo(loan.rate().interestOn(opening));
            opening = installment.balance();
        }
    }

    @Property
    void pricePaymentsStayLevelUntilTheLastOrUntilPaidOff(@ForAll("priceLoans") Loan loan) {
        List<Installment> installments = loan.schedule().installments();
        Money level = installments.getFirst().payment();

        assertThat(installments.subList(0, installments.size() - 1))
                .filteredOn(installment -> !installment.balance().isZero())
                .extracting(Installment::payment)
                .isSubsetOf(level);
    }

    @Property
    void sacAmortizesTheSameAmountAfterTheFirstMonthAndPaymentsNeverRise(@ForAll("sacLoans") Loan loan) {
        List<Installment> installments = loan.schedule().installments();

        assertThat(installments.subList(1, installments.size()))
                .extracting(Installment::amortization)
                .isSubsetOf(installments.getLast().amortization());
        assertThat(installments)
                .extracting(Installment::payment)
                .isSortedAccordingTo((earlier, later) -> later.compareTo(earlier));
    }

    private static Arbitrary<Money> principals() {
        return Arbitraries.of("BRL", "USD", "JPY", "BHD")
                .map(Currency::getInstance)
                .flatMap(currency -> Arbitraries.bigDecimals()
                        .between(BigDecimal.ONE, new BigDecimal("100000000"))
                        .ofScale(currency.getDefaultFractionDigits())
                        .map(amount -> new Money(amount, currency)));
    }

    private static Arbitrary<InterestRate> rates() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, new BigDecimal("0.2"))
                .ofScale(6)
                .edgeCases(edges -> edges.add(BigDecimal.ZERO))
                .map(InterestRate::new);
    }

    private static Arbitrary<Integer> months() {
        return Arbitraries.integers().between(1, 600);
    }

    private static Money sum(List<Money> amounts) {
        return amounts.stream().reduce(Money::plus).orElseThrow();
    }

    private static List<Integer> numbers(int months) {
        return IntStream.rangeClosed(1, months).boxed().toList();
    }
}
