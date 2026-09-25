@WorkingCapital
@WorkingCapitalEffectivePaymentRateFeature
Feature: Working Capital effective payment rate

  Background:
    Given Global configuration "enable-business-date" is enabled

  Scenario: TPV loan details expose effectivePaymentRate equal to the initial paymentRate when no rate change exists
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan details has the following field values:
      | paymentRate          | 1.0 |
      | effectivePaymentRate | 1.0 |
    And Working Capital loan account event schema contains field "effectivePaymentRate"
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  Scenario: After a TPV rate change, paymentRate stays initial while effectivePaymentRate reflects the rate in force
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "12.5" value
    And Working capital loan details has the following field values:
      | paymentRate          | 1.0  |
      | effectivePaymentRate | 12.5 |
    And Working Capital Loan period payment rate in effect is "12.5"
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  Scenario: ANNUAL_EIR loan details have null effectivePaymentRate
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin ensures Annual EIR working capital loan products exist
    And Admin creates a working capital loan with annual EIR and the following data:
      | LoanProduct                         | submittedOnDate | expectedDisbursementDate | principalAmount | annualEir | discount |
      | WCLP_ANNUAL_EIR_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 43.7562   | 1000     |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and "1000" discount amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan details has the following field values:
      | effectivePaymentRate | null |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"
