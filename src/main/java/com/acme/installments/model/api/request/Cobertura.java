package com.acme.installments.model.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record Cobertura(
        @NotNull Integer cobertura,

        @NotNull @DecimalMin(value = "0.01", message = "É obrigatório informar o valor da cobertura.")
        BigDecimal valor) {}
