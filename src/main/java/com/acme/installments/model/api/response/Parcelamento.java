package com.acme.installments.model.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Parcelamento(
        int quantidadeParcelas,
        BigDecimal valorPrimeiraParcela,
        BigDecimal valorDemaisParcelas,
        BigDecimal valorParcelamentoTotal) {}
