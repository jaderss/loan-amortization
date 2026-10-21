package dev.jaderss.amortization.installments;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/installments")
public class InstallmentController {

    private final InstallmentService service;

    public InstallmentController(InstallmentService service) {
        this.service = service;
    }

    @PostMapping
    public InstallmentResponse plans(@Valid @RequestBody InstallmentRequest request) {
        return new InstallmentResponse(
                request.currency(), service.plans(request.currency(), request.coverages(), request.options()));
    }
}
