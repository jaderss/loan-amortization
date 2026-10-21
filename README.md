# loan-amortization

[![CI](https://github.com/jaderss/loan-amortization/actions/workflows/ci.yml/badge.svg)](https://github.com/jaderss/loan-amortization/actions/workflows/ci.yml)

A loan and installment calculator built on Spring Boot 4.1 and Java 25. It produces full amortization schedules with the two systems used for loans in Brazil, Price and SAC, and splits an insurance premium into installment plans. Every amount is a `BigDecimal` paired with its currency, and every rounding step is explicit.

## What it does

- `POST /loans/schedule` returns every installment of a loan with its payment, interest, amortization and remaining balance, plus the totals.
- Price (the French system) charges a level payment from the annuity formula. SAC (Sistema de Amortização Constante) repays the same principal every month, so the interest and the payment fall over time.
- `POST /installments` splits the sum of a set of coverages into 1 to N installments, compounding the interest of whichever option covers each installment count.
- Rejects invalid input as RFC 9457 Problem Details that name the offending fields.

## Rounding rules

These are the rules the code follows and the tests check.

- An amount always has the scale of its currency: two decimal places for BRL and USD, none for JPY, three for BHD. An input with more decimals than its currency allows is rejected, never rounded.
- Growth factors and the annuity factor are computed with `MathContext.DECIMAL128`, 34 significant digits. Only the resulting amount is rounded, half even, to the currency's scale.
- Interest for a month is the opening balance times the monthly rate, rounded half even.
- Price rounds the level payment once. The last installment repays whatever balance is left, so rounding differences land there and the balance ends at exactly zero.
- SAC splits the principal into equal parts rounded down, and the first part carries the leftover cents.
- Installment plans round the same way: the first installment carries the leftover cents.

## Run it

Java 25 is required. The Maven wrapper downloads Maven.

```bash
./mvnw spring-boot:run
```

```bash
curl -s localhost:8080/loans/schedule -H 'Content-Type: application/json' \
  -d '{"system": "PRICE", "currency": "BRL", "principal": 1000.00, "monthlyRate": 0.01, "months": 3}'
```

```json
{
  "system": "PRICE",
  "currency": "BRL",
  "totalPaid": 1020.07,
  "totalInterest": 20.07,
  "installments": [
    {"number": 1, "payment": 340.02, "interest": 10.00, "amortization": 330.02, "balance": 669.98},
    {"number": 2, "payment": 340.02, "interest": 6.70, "amortization": 333.32, "balance": 336.66},
    {"number": 3, "payment": 340.03, "interest": 3.37, "amortization": 336.66, "balance": 0.00}
  ]
}
```

## API

### POST /loans/schedule

| Field | Type | Rule |
|---|---|---|
| `system` | `PRICE` or `SAC` | required |
| `currency` | ISO 4217 code | required |
| `principal` | number | at least 0.01, no more decimals than the currency allows |
| `monthlyRate` | number | 0 to 1, so `0.01` is 1% a month |
| `months` | integer | 1 to 600 |

### POST /installments

```bash
curl -s localhost:8080/installments -H 'Content-Type: application/json' -d '{
  "currency": "BRL",
  "coverages": [{"id": 1, "amount": 123.12}, {"id": 4, "amount": 345.45}],
  "options": [
    {"minInstallments": 1, "maxInstallments": 6, "rate": 0},
    {"minInstallments": 7, "maxInstallments": 12, "rate": 0.01}
  ]
}'
```

The response has one plan per installment count, from 1 to the largest `maxInstallments`, with the first installment, the other installments and the total. Options must not overlap. An installment count that no option covers is charged no interest.

### Errors

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "The request is invalid.",
  "instance": "/loans/schedule",
  "errors": {"months": "must be less than or equal to 600"}
}
```

## Tests

```bash
./mvnw verify
```

The build runs the tests and checks formatting with Spotless and Palantir Java Format. Alongside example tests with schedules worked out by hand, jqwik property tests generate thousands of loans across currencies, principals up to 100 million, rates up to 20% a month and terms up to 600 months, and check for each one that:

- the amortization sums to the loan amount,
- the balance ends at zero and never rises,
- no payment, interest, amortization or balance is negative,
- each payment is its interest plus its amortization,
- interest is one month on the opening balance,
- Price payments stay level until the last, and SAC payments never rise.

## Stack

Java 25, Spring Boot 4.1, Bean Validation, JUnit 6, AssertJ, jqwik.
