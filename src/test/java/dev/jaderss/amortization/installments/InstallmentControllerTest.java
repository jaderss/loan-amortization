package dev.jaderss.amortization.installments;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@WebMvcTest(InstallmentController.class)
@Import(InstallmentService.class)
class InstallmentControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void returnsEveryInstallmentCountWithInterestApplied() throws IOException {
        assertThat(post("request.json"))
                .hasStatusOk()
                .bodyJson()
                .isEqualTo(new ClassPathResource("installments/response.json"), JsonCompareMode.STRICT);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
                    negative-rate.json            | $.errors['options[0].rate']            | must be greater than or equal to 0
                    no-coverages.json             | $.errors.coverages                     | must not be empty
                    no-options.json               | $.errors.options                       | must not be empty
                    missing-min-installments.json | $.errors['options[0].minInstallments'] | must not be null
                    reversed-range.json           | $.errors['options[0].ordered']         | minInstallments must not exceed maxInstallments
                    overlapping-options.json      | $.detail                               | Installment options 1-6 and 6-9 cover the same installment count
                    no-currency.json              | $.errors.currency                      | must not be null
                    too-many-decimals.json        | $.errors.coveragesInCurrencyScale      | coverage amounts must not have more decimal places than the currency allows
                    too-many-installments.json    | $.errors['options[2].maxInstallments'] | must be less than or equal to 600
                    rate-above-one.json           | $.errors['options[2].rate']            | must be less than or equal to 1
                    unknown-currency.json         | $.detail                               | Failed to read request
                    """)
    void rejectsInvalidRequestsWithProblemDetails(String request, String path, String message) throws IOException {
        assertThat(post(request))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath(path)
                .isEqualTo(message);
    }

    private MvcTestResult post(String fixture) throws IOException {
        return mvc.post()
                .uri("/installments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ClassPathResource("installments/" + fixture).getContentAsString(UTF_8))
                .exchange();
    }
}
