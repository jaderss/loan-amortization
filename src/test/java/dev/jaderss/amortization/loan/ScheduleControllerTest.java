package dev.jaderss.amortization.loan;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@WebMvcTest(ScheduleController.class)
class ScheduleControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void returnsTheFullScheduleWithTotals() {
        assertThat(post("PRICE", "BRL", "1000.00", "0.01", "3"))
                .hasStatusOk()
                .bodyJson()
                .isEqualTo(new ClassPathResource("loan/price-response.json"), JsonCompareMode.STRICT);
    }

    @Test
    void schedulesSacLoans() {
        assertThat(post("SAC", "BRL", "120000.00", "0.01", "12"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.totalInterest")
                .isEqualTo(7800.00);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
                    PRICE | BRL | 0.00    | 0.01 | 12  | $.errors.principal                 | must be greater than or equal to 0.01
                    PRICE | BRL | 1000.00 | -0.1 | 12  | $.errors.monthlyRate               | must be greater than or equal to 0
                    PRICE | BRL | 1000.00 | 1.5  | 12  | $.errors.monthlyRate               | must be less than or equal to 1
                    SAC   | BRL | 1000.00 | 0.01 | 0   | $.errors.months                    | must be greater than or equal to 1
                    SAC   | BRL | 1000.00 | 0.01 | 601 | $.errors.months                    | must be less than or equal to 600
                    SAC   | JPY | 1000.50 | 0.01 | 12  | $.errors.principalInCurrencyScale  | principal must not have more decimal places than the currency allows
                    SAC   | XAU | 1230    | 0.01 | 12  | $.errors.currencyWithMinorUnit     | currency must have a minor unit
                    """)
    void rejectsInvalidLoans(
            String system, String currency, String principal, String rate, String months, String path, String message) {
        assertThat(post(system, currency, principal, rate, months))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath(path)
                .isEqualTo(message);
    }

    @Test
    void reportsErrorsInEnglishWhateverLanguageTheClientAsksFor() {
        assertThat(mvc.post()
                        .uri("/loans/schedule")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("SAC", "BRL", "1000.00", "0.01", "601")))
                .bodyJson()
                .extractingPath("$.errors.months")
                .isEqualTo("must be less than or equal to 600");
    }

    @Test
    void rejectsUnknownSystems() {
        assertThat(post("GERMAN", "BRL", "1000.00", "0.01", "12")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    private MvcTestResult post(String system, String currency, String principal, String rate, String months) {
        return mvc.post()
                .uri("/loans/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(system, currency, principal, rate, months))
                .exchange();
    }

    private static String body(String system, String currency, String principal, String rate, String months) {
        return """
                {"system": "%s", "currency": "%s", "principal": %s, "monthlyRate": %s, "months": %s}
                """.formatted(system, currency, principal, rate, months);
    }
}
