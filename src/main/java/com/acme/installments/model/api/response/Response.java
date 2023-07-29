package com.acme.installments.model.api.response;

import java.util.List;
import lombok.Data;

@Data
public class Response{
    private List<Parcelamento> dados;
}
