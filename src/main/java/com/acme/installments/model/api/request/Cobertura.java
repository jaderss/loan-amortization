package com.acme.installments.model.api.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Cobertura {
    private Integer cobertura;
    private BigDecimal valor;
}
