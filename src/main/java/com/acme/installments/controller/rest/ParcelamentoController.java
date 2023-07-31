package com.acme.installments.controller.rest;

import com.acme.installments.model.api.request.ParcelamentoRequest;
import com.acme.installments.model.api.response.ParcelamentoResponse;
import com.acme.installments.service.ParcelamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/installments")
@RequiredArgsConstructor
public class ParcelamentoController {

    private final ParcelamentoService service;

    @PostMapping
    public ParcelamentoResponse calcularParcelamentos(@Valid @RequestBody ParcelamentoRequest request) {
        ParcelamentoResponse response = new ParcelamentoResponse();
        response.setDados(service.calcularParcelamentos(request.getListOpcaoParcelamento(), request.getListCobertura()));
        return response;
    }
}
