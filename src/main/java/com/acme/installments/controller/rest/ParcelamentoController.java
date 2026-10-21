package com.acme.installments.controller.rest;

import com.acme.installments.model.api.request.ParcelamentoRequest;
import com.acme.installments.model.api.response.ParcelamentoResponse;
import com.acme.installments.service.ParcelamentoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/installments")
public class ParcelamentoController {

    private final ParcelamentoService service;

    public ParcelamentoController(ParcelamentoService service) {
        this.service = service;
    }

    @PostMapping
    public ParcelamentoResponse calcularParcelamentos(@Valid @RequestBody ParcelamentoRequest request) {
        return new ParcelamentoResponse(
                service.calcularParcelamentos(request.listOpcaoParcelamento(), request.listCobertura()));
    }
}
