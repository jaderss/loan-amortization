package dev.jaderss.amortization.error;

import static java.util.stream.Collectors.toMap;

import dev.jaderss.amortization.installments.OverlappingOptionsException;
import java.util.TreeMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ProblemDetailsAdvice {

    @ExceptionHandler
    ProblemDetail invalid(MethodArgumentNotValidException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The request is invalid.");
        problem.setProperty(
                "errors",
                e.getBindingResult().getFieldErrors().stream()
                        .collect(toMap(
                                FieldError::getField,
                                error -> String.valueOf(error.getDefaultMessage()),
                                (first, second) -> first,
                                TreeMap::new)));
        return problem;
    }

    @ExceptionHandler
    ProblemDetail overlapping(OverlappingOptionsException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
