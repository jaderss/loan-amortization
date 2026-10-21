package com.acme.installments.model.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OpcaoParcelamento(
        @NotNull @Min(value = 1, message = "Quantidade de parcelas não pode ser menor que 1.")
        Integer quantidadeMinimaParcelas,

        @NotNull @Min(value = 1, message = "Quantidade de parcelas não pode ser menor que 1.")
        Integer quantidadeMaximaParcelas,

        @NotNull @DecimalMin(value = "0", message = "É obrigatório informar uma taxa de juros")
        BigDecimal juros) {}
