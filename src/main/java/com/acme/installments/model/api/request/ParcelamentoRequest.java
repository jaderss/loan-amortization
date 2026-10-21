package com.acme.installments.model.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ParcelamentoRequest(
        @NotEmpty @Valid List<OpcaoParcelamento> listOpcaoParcelamento,
        @NotEmpty @Valid List<Cobertura> listCobertura) {}
