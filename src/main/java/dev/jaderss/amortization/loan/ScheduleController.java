package dev.jaderss.amortization.loan;

import dev.jaderss.amortization.money.InterestRate;
import dev.jaderss.amortization.money.Money;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScheduleController {

    @PostMapping("/loans/schedule")
    public ScheduleResponse schedule(@Valid @RequestBody ScheduleRequest request) {
        Schedule schedule = request.system()
                .schedule(
                        new Money(request.principal(), request.currency()),
                        new InterestRate(request.monthlyRate()),
                        request.months());
        return ScheduleResponse.of(request.system(), request.currency(), schedule);
    }
}
