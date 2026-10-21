package dev.jaderss.amortization.installments;

public class OverlappingOptionsException extends RuntimeException {

    OverlappingOptionsException(InstallmentOption first, InstallmentOption second) {
        super("Installment options %d-%d and %d-%d cover the same installment count"
                .formatted(
                        first.minInstallments(),
                        first.maxInstallments(),
                        second.minInstallments(),
                        second.maxInstallments()));
    }
}
