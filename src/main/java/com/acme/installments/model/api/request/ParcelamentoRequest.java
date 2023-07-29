package com.acme.installments.model.api.request;

import lombok.Data;

import java.util.List;

@Data
public class ParcelamentoRequest {
    private List<OpcaoParcelamento> listOpcaoParcelamento;
    private List<Cobertura> listCobertura;
}
