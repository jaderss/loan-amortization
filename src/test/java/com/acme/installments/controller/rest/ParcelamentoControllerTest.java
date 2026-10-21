package com.acme.installments.controller.rest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.acme.installments.service.ParcelamentoService;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@WebMvcTest(ParcelamentoController.class)
@Import(ParcelamentoService.class)
class ParcelamentoControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void returnsEveryInstallmentCountWithInterestApplied() throws IOException {
        assertThat(post("request.json"))
                .hasStatusOk()
                .bodyJson()
                .isEqualTo(new ClassPathResource("response.json"), JsonCompareMode.STRICT);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "request_parcela_conflito.json",
                "request_juros_negativos.json",
                "request_sem_coberturas.json",
                "request_sem_opcoes.json",
                "request_parcelas_invalidas.json"
            })
    void rejectsInvalidRequests(String request) throws IOException {
        assertThat(post(request)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    private MvcTestResult post(String resource) throws IOException {
        return mvc.post()
                .uri("/installments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ClassPathResource(resource).getContentAsString(UTF_8))
                .exchange();
    }
}
