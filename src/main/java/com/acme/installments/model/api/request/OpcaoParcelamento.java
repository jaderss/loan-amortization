package com.acme.installments.model.api.request;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Data
public class OpcaoParcelamento {
    @Min(value = 1, message = "Quantidade de parcelas não pode ser menor que 1.")
    private Integer quantidadeMinimaParcelas;
    @Min(value = 1, message = "Quantidade de parcelas não pode ser menor que 1.")
    private Integer quantidadeMaximaParcelas;
    @DecimalMin(value = "0", message = "É obrigatório informar uma taxa de juros")
    private BigDecimal juros;
}
