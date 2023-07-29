package com.acme.installments.model.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Parcelamento {
    private Integer quantidadeParcelas;
    private BigDecimal valorPrimeiraParcela;
    private BigDecimal valorDemaisParcelas;
    private BigDecimal valorParcelamentoTotal;
}
