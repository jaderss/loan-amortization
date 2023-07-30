package com.acme.installments.model.api.response;

import java.util.List;
import lombok.Data;

@Data
public class ParcelamentoResponse {
    private List<Parcelamento> dados;
}
