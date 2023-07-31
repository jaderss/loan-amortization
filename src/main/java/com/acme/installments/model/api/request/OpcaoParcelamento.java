package com.acme.installments.model.api.request;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class OpcaoParcelamento {
    @NotNull
    @Min(value = 1, message = "Quantidade de parcelas não pode ser menor que 1.")
    private Integer quantidadeMinimaParcelas;
    @NotNull
    @Min(value = 1, message = "Quantidade de parcelas não pode ser menor que 1.")
    private Integer quantidadeMaximaParcelas;
    @NotNull
    @DecimalMin(value = "0", message = "É obrigatório informar uma taxa de juros")
    private BigDecimal juros;
}
