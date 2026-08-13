@WorkingCapital
@WorkingCapitalPeriodPaymentRateFeature
Feature: Working Capital Period Payment Rate

  @TestRailId:C78817
  Scenario: Verify Working Capital period payment rate added successfully on loan account - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | 0.0              |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null     |
#--- update period payment rate ---#
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "12.5" value
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 15 January 2026 | 1.0           | 12.5     | false    |
    When Admin sets the business date to "15 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null     |
    And Working Capital Loan period payment rate in effect is "12.5"
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C78818
  Scenario: Verify Working Capital period payment rate added on first day of disbursement successfully on loan account - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0                |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null     |
#--- update period payment rate ---#
    And Admin update Working Capital period payment rate with "12.5" value
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 01 January 2026 | 1.0           | 12.5     | false    |
    When Admin sets the business date to "15 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null     |
    And Working Capital Loan period payment rate in effect is "12.5"
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C78819
  Scenario: Verify Working Capital period payment rate added successfully a few times a day on loan account - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0                |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null     |
#--- update period payment rate ---#
    And Admin update Working Capital period payment rate with "12.5" value
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 01 January 2026 | 1.0           | 12.5     | false    |
#--- update period payment rate ---#
    And Admin update Working Capital period payment rate with "19.38" value
# The correcting change succeeds the rate that was in force before the mistaken one, not the mistake itself: 12.5 never
# stood, so the surviving row runs from the original 1.0. The reversed row keeps what it was written with.
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 01 January 2026 | 1.0           | 12.5     | true     |
      | 01 January 2026 | 1.0           | 19.38    | false    |
    When Admin sets the business date to "15 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null     |
    And Working Capital Loan period payment rate in effect is "19.38"
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C78820
  Scenario: Verify Working Capital period payment rate added successfully a few times on different dates on loan account - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0                |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null     |
#--- update period payment rate ---#
    And Admin update Working Capital period payment rate with "12.5" value
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 01 January 2026 | 1.0           | 12.5     | false    |
#--- update period payment rate ---#
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "19.38" value
# Changes on distinct dates each keep their own segment of the schedule: only a change sharing an effective date is
# overwritten, so nothing here is reversed.
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 01 January 2026 | 1.0           | 12.5     | false    |
      | 15 January 2026 | 12.5          | 19.38    | false    |
#--- update period payment rate ---#
    When Admin sets the business date to "25 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "18.09" value
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date   | Previous Rate | New Rate | Reversed |
      | 01 January 2026  | 1.0           | 12.5     | false    |
      | 15 January 2026  | 12.5          | 19.38    | false    |
      | 25 February 2026 | 19.38         | 18.09    | false    |
    When Admin sets the business date to "15 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null     |
    And Working Capital Loan period payment rate in effect is "18.09"
    Then Admin closes the Working Capital loan with a full repayment on "25 February 2026"

  @TestRailId:C78821
  Scenario: Verify Working Capital period payment rate added successfully on different date on loan account - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 12.5              | 0                |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 12.5              | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 12.5              | null     |
#--- update period payment rate ---#
    When Admin sets the business date to "25 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "15" value
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date | Previous Rate | New Rate | Reversed |
      | 25 April 2026  | 12.5          | 15.0     | false    |
    When Admin sets the business date to "28 April 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 12.5              | null     |
    And Working Capital Loan period payment rate in effect is "15.0"
    Then Admin closes the Working Capital loan with a full repayment on "28 April 2026"

  @TestRailId:C89816
  Scenario: Verify a rate change followed by backdated-repayment reprocessing preserves the pre-rate-change schedule segment
    # A later repayment plus a backdated one trigger the full reset+replay reprocessing that rebuilds the projected
    # amortization schedule from scratch. The periods before the rate change are a pure function of the untouched
    # opening balance and the rate history, so reconstruction must reproduce them exactly rather than flattening the
    # whole schedule to the current rate.
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin retrieves the projected amortization schedule
    And Admin remembers the retrieved amortization schedule payments before "2026-01-10"
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "11" value
    When Admin sets the business date to "25 January 2026"
    And Customer makes repayment on "25 January 2026" with 500 transaction amount on Working Capital loan
    When Admin sets the business date to "30 January 2026"
    And Customer makes repayment on "15 January 2026" with 200 transaction amount on Working Capital loan
    And Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule payments before "2026-01-10" match the previously remembered ones
    Then Admin closes the Working Capital loan with a full repayment on "30 January 2026"

  @TestRailId:C78822
  Scenario Outline: Verify update Working Capital period payment rate failed with outranged rate change value within loan product level defined range - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP_PERIOD_PAYMENT_RATE | 01 January 2026 | 01 January 2026          | 100             | 100                | 15                | 0                |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_PERIOD_PAYMENT_RATE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 15.0              | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_PERIOD_PAYMENT_RATE | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 15.0              | null     |
#--- update period payment rate  with invalid value that is out of allowed min/max values defined on loan product level ---#
    And Admin update Working Capital period payment rate failed with "<rate_change_value>" value with <rate_change_error_message> error message
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_PERIOD_PAYMENT_RATE | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 15.0              | null     |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

    Examples:
      | rate_change_value | rate_change_error_message                                    |
      | 0.5               | Failed data validation due to: rate.below.product.minimum.   |
      | 99.5              | Failed data validation due to: rate.exceeds.product.maximum. |

  @TestRailId:C78823
  Scenario: Verify update Working Capital period payment rate update failed within non active loan - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP_PERIOD_PAYMENT_RATE | 01 January 2026 | 01 January 2026          | 100             | 100                | 15                | 0                |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_PERIOD_PAYMENT_RATE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 15.0              | null     |
    Then Admin update Working Capital period payment rate failed with "18" value on non active loan
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin update Working Capital period payment rate failed with "18" value on non active loan
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_PERIOD_PAYMENT_RATE | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 15.0              | null     |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

  @TestRailId:C78824
  Scenario Outline: Verify update Working Capital period payment rate failed with invalid rate change value - UC8
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_PERIOD_PAYMENT_RATE | 01 January 2026 | 01 January 2026          | 100             | 100                | 15                | 0        |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_PERIOD_PAYMENT_RATE | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 15.0              | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_PERIOD_PAYMENT_RATE | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 15.0              | null     |
#--- update period payment rate with invalid or already set up value ---#
    And Admin update Working Capital period payment rate failed with "<rate_change_value>" value with <rate_change_error_message> error message
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_PERIOD_PAYMENT_RATE | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 15.0              | null     |
    Then Admin closes the Working Capital loan with a full repayment on "01 January 2026"

    Examples:
      | rate_change_value | rate_change_error_message                                 |
      | 0                 | The parameter `periodPaymentRate` must be greater than 0. |
      | 15                | rate.must.differ.from.current                             |

  @TestRailId:C78825
  Scenario: Verify Working Capital period payment rate added successfully by externalId on loan account - UC9
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discountProposed |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0                |
    Then Working capital loan creation was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status                         | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Submitted and pending approval | 100.0             | 0.0               | 100.0              | 1.0               | null     |
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null     |
#--- update period payment rate by externalId ---#
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "12.5" value by externalId
    Then Working Capital Loan Period Payment Rate changes history by externalId contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 15 January 2026 | 1.0           | 12.5     | false    |
    When Admin sets the business date to "15 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 1.0               | null     |
    And Working Capital Loan period payment rate in effect is "12.5"
    Then Admin closes the Working Capital loan with a full repayment on "15 March 2026"

  @TestRailId:C93984
  Scenario: Verify a backdated period payment rate change takes effect on its own date and keeps later changes - UC10
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
#--- before any rate change: a flat schedule at the original 18, 50.00 a day throughout ---#
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 8         | 2026-01-09 | 50.00                 | 8675.67         | 9.31                       | 924.33                     |
      | 9         | 2026-01-10 | 50.00                 | 8634.94         | 9.26                       | 915.07                     |
      | 18        | 2026-01-19 | 50.00                 | 8266.35         | 8.87                       | 833.66                     |
      | 19        | 2026-01-20 | 50.00                 | 8225.18         | 8.83                       | 824.83                     |
#--- first change, effective today: everything up to 19 January is left exactly as it was ---#
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "20" value
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 18        | 2026-01-19 | 50.00                 | 8266.35         | 8.87                       | 833.66                     |
      | 19        | 2026-01-20 | 55.56                 | 8220.59         | 9.80                       | 823.86                     |
      | 20        | 2026-01-21 | 55.56                 | 8174.78         | 9.75                       | 814.11                     |
#--- second change, backdated ten days behind the first ---#
    And Admin update Working Capital period payment rate with "11" value effective from "10 January 2026"
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 10 January 2026 | 18.0          | 11.0     | false    |
      | 20 January 2026 | 11.0          | 20.0     | false    |
# The rate in force today is the latest change effective on or before it, which the backdated one sits behind.
    And Working Capital Loan period payment rate in effect is "20"
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 9.61                       | 990.39                     |
      | 8         | 2026-01-09 | 50.00                 | 8675.67         | 9.31                       | 924.33                     |
      | 9         | 2026-01-10 | 30.56                 | 8650.78         | 5.67                       | 918.66                     |
      | 10        | 2026-01-11 | 30.56                 | 8625.88         | 5.66                       | 913.00                     |
      | 18        | 2026-01-19 | 30.56                 | 8426.06         | 5.53                       | 868.33                     |
      | 19        | 2026-01-20 | 55.56                 | 8380.49         | 9.99                       | 858.34                     |
      | 20        | 2026-01-21 | 55.56                 | 8334.87         | 9.94                       | 848.40                     |
      | 185       | 2026-07-05 | 55.56                 | 15.86           | 0.08                       | 0.03                       |
      | 186       | 2026-07-06 | 15.88                 | 0.00            | 0.03                       | 0.00                       |
    Then Admin closes the Working Capital loan with a full repayment on "20 January 2026"

  @TestRailId:C93985
  Scenario: Verify a backdated period payment rate change leaves the periods before it untouched - UC11
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin retrieves the projected amortization schedule
    And Admin remembers the retrieved amortization schedule payments before "2026-01-10"
#--- before the change: a flat schedule at the original 18 across the effective date ---#
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 8         | 2026-01-09 | 50.00                 | 8675.67         | 9.31                       | 924.33                     |
      | 9         | 2026-01-10 | 50.00                 | 8634.94         | 9.26                       | 915.07                     |
      | 10        | 2026-01-11 | 50.00                 | 8594.16         | 9.22                       | 905.85                     |
#--- a repayment lands before the change, then the change is backdated to a date after it ---#
    When Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 January 2026" with 500 transaction amount on Working Capital loan
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "11" value effective from "10 January 2026"
    And Admin retrieves the projected amortization schedule
# Rebuilding the whole schedule must not disturb what came before the change: the periods up to 09 January are a pure
# function of the opening balance and the original rate, and the repayment on 05 January belongs to them.
    Then The retrieved amortization schedule payments before "2026-01-10" match the previously remembered ones
# Side by side with the snapshot above: period 8 is unchanged down to the cent, period 9 onwards is repriced at 11.
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 8         | 2026-01-09 | 50.00                 | 8675.67         | 9.31                       | 924.33                     |
      | 9         | 2026-01-10 | 30.56                 | 8650.78         | 5.67                       | 918.66                     |
      | 10        | 2026-01-11 | 30.56                 | 8625.88         | 5.66                       | 913.00                     |
    Then Admin closes the Working Capital loan with a full repayment on "20 January 2026"

  @TestRailId:C93986
  Scenario: Verify a future-dated period payment rate change leaves the current rate untouched until its date - UC12
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin retrieves the projected amortization schedule
    And Admin remembers the retrieved amortization schedule payments before "2026-02-01"
#--- before the change: a flat schedule at the original 18 across the future effective date ---#
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 30        | 2026-01-31 | 50.00                 | 7769.36         | 8.34                       | 730.65                     |
      | 31        | 2026-02-01 | 50.00                 | 7727.66         | 8.30                       | 722.35                     |
      | 32        | 2026-02-02 | 50.00                 | 7685.91         | 8.25                       | 714.10                     |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "11" value effective from "01 February 2026"
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date   | Previous Rate | New Rate | Reversed |
      | 01 February 2026 | 18.0          | 11.0     | false    |
#--- Recorded but not yet in force: the change is effective 01 February, so 18 is still the rate being billed.
    And Working Capital Loan period payment rate in effect is "18"
    And Admin retrieves the projected amortization schedule
#--- everything up to the effective date is untouched, everything from it is repriced ---#
    Then The retrieved amortization schedule payments before "2026-02-01" match the previously remembered ones
# From 01 February the daily payment drops from 50.00 to 30.56 and the amortization drops with it, stretching the term
# from 200 periods to 309 while the balance and deferred discount fee still close at 0.00.
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 29        | 2026-01-30 | 50.00                 | 7811.02         | 8.39                       | 738.99                     |
      | 30        | 2026-01-31 | 50.00                 | 7769.36         | 8.34                       | 730.65                     |
      | 31        | 2026-02-01 | 30.56                 | 7743.88         | 5.08                       | 725.57                     |
      | 32        | 2026-02-02 | 30.56                 | 7718.39         | 5.06                       | 720.51                     |
      | 308       | 2026-11-05 | 30.56                 | 4.32            | 0.02                       | 0.05                       |
      | 309       | 2026-11-06 | 4.32                  | 0.00            | 0.05                       | 0.00                       |
#--- the effective date arrives and the change is in force at once. The assertion sits before the COB run
#--- deliberately: the rate in force is derived from the change history, so no job has to bring it up to date.
    When Admin sets the business date to "01 February 2026"
    Then Working Capital Loan period payment rate in effect is "11"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Admin closes the Working Capital loan with a full repayment on "01 February 2026"

  @TestRailId:C93987
  Scenario: Verify a same-date period payment rate change overwrites the earlier one for that date only - UC13
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "11" value effective from "10 January 2026"
    And Admin update Working Capital period payment rate with "20" value
#--- the mistaken 11 is in force from 10 January, with the 20 running from 20 January ---#
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 8         | 2026-01-09 | 50.00                 | 8675.67         | 9.31                       | 924.33                     |
      | 9         | 2026-01-10 | 30.56                 | 8650.78         | 5.67                       | 918.66                     |
      | 19        | 2026-01-20 | 55.56                 | 8380.49         | 9.99                       | 858.34                     |
#--- correcting the 10 January change: same effective date, so the mistaken one is reversed ---#
    And Admin update Working Capital period payment rate with "17" value effective from "10 January 2026"
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 10 January 2026 | 18.0          | 11.0     | true     |
      | 10 January 2026 | 18.0          | 17.0     | false    |
      | 20 January 2026 | 17.0          | 20.0     | false    |
    And Working Capital Loan period payment rate in effect is "20"
# The correction is scoped to 10 January onwards: period 8 is untouched, period 9 moves from the mistaken 30.56 to
# 47.22, and the 20 January segment keeps its rate but is re-derived from the balance the corrected segment leaves.
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 8         | 2026-01-09 | 50.00                 | 8675.67         | 9.31                       | 924.33                     |
      | 9         | 2026-01-10 | 47.22                 | 8637.20         | 8.75                       | 915.58                     |
      | 19        | 2026-01-20 | 55.56                 | 8243.50         | 9.83                       | 828.74                     |
    Then Admin closes the Working Capital loan with a full repayment on "20 January 2026"

  @TestRailId:C93988
  Scenario: Verify period payment rate changes with different dates overwrite amortization schedule with corresponding period calculations - UC14
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin retrieves the projected amortization schedule
    And Admin remembers the retrieved amortization schedule payments before "2026-01-10"
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "11" value effective from "10 January 2026"
    And Admin update Working Capital period payment rate with "20" value
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 10 January 2026 | 18.0          | 11.0     | false    |
      | 20 January 2026 | 11.0          | 20.0     | false    |
    And Working Capital Loan period payment rate in effect is "20"
    And Admin update Working Capital period payment rate with "15" value effective from "31 March 2026"
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 10 January 2026 | 18.0          | 11.0     | false    |
      | 20 January 2026 | 11.0          | 20.0     | false    |
      | 31 March 2026   | 20.0          | 15.0     | false    |
    And Admin update Working Capital period payment rate with "19" value effective from "15 January 2026"
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 10 January 2026 | 18.0          | 11.0     | false    |
      | 15 January 2026 | 11.0          | 19.0     | false    |
      | 20 January 2026 | 19.0          | 20.0     | false    |
      | 31 March 2026   | 20.0          | 15.0     | false    |
    And Admin update Working Capital period payment rate with "13" value effective from "10 January 2026"
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 10 January 2026 | 18.0          | 11.0     | true     |
      | 10 January 2026 | 18.0          | 13.0     | false    |
      | 15 January 2026 | 13.0          | 19.0     | false    |
      | 20 January 2026 | 19.0          | 20.0     | false    |
      | 31 March 2026   | 20.0          | 15.0     | false    |
    And Admin update Working Capital period payment rate with "25" value effective from "31 March 2026"
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 10 January 2026 | 18.0          | 11.0     | true     |
      | 10 January 2026 | 18.0          | 13.0     | false    |
      | 15 January 2026 | 13.0          | 19.0     | false    |
      | 20 January 2026 | 19.0          | 20.0     | false    |
      | 31 March 2026   | 20.0          | 15.0     | true     |
      | 31 March 2026   | 20.0          | 25.0     | false    |
#--- The 31 March correction is recorded but still ahead of the business date, so the rate in force on 20 January is
#--- the 20 that took effect that day.
    And Working Capital Loan period payment rate in effect is "20"
    And Admin retrieves the projected amortization schedule
#--- everything up to the effective date is untouched, everything from it is repriced ---#
    Then The retrieved amortization schedule payments before "2026-01-10" match the previously remembered ones
# amortization schedule contains diff values based omn period payment rate start date
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 9.61                       | 990.39                     |
      | 8         | 2026-01-09 | 50.00                 | 8675.67         | 9.31                       | 924.33                     |
      | 9         | 2026-01-10 | 36.11                 | 8646.26         | 6.70                       | 917.63                     |
      | 13        | 2026-01-14 | 36.11                 | 8528.39         | 6.61                       | 891.06                     |
      | 14        | 2026-01-15 | 52.78                 | 8485.22         | 9.61                       | 881.45                     |
      | 18        | 2026-01-19 | 52.78                 | 8312.05         | 9.42                       | 843.50                     |
      | 19        | 2026-01-20 | 55.56                 | 8266.35         | 9.86                       | 833.64                     |
      | 88        | 2026-03-30 | 55.56                 | 4978.36         | 5.96                       | 288.00                     |
      | 89        | 2026-03-31 | 69.44                 | 4916.28         | 7.36                       | 280.64                     |
      | 164       | 2026-06-14 | 58.35                 | 0.00            | 0.10                       | 0.00                       |
    Then Admin closes the Working Capital loan with a full repayment on "20 January 2026"

  @TestRailId:C93989
  Scenario: Verify backdated and then future-dated period payment rate change from current biz date close to last date changes amortization schedule calculations - UC15
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 17                | 0        |
    Then Admin successfully add discount with "12" amount on Working Capital loan account
    When Admin sets the business date to "06 August 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "15" value effective from "01 August 2026"
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 01 August 2026  | 17.0          | 15.0     | false    |
    And Admin update Working Capital period payment rate with "19" value effective from "10 August 2026"
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 01 August 2026  | 17.0          | 15.0     | false    |
      | 10 August 2026  | 15.0          | 19.0     | false    |
#--- On 06 August the 01 August change is in force at 15; the 10 August one is recorded but still in the future.
    And Working Capital Loan period payment rate in effect is "15"
    And Admin retrieves the projected amortization schedule
# amortization schedule contains diff values based omn period payment rate start date
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 1         | 2026-01-02 | 47.22                 | 8952.91         | 0.13                       | 11.87                      |
      | 221       | 2026-08-10 | 52.78                 | 7700.33         | 0.00                       | 0.00                       |
      | 368       | 2027-01-04 | 3.24                  | 0.00            | 0.00                       | 0.00                       |
    Then Admin closes the Working Capital loan with a full repayment on "06 August 2026"

  @TestRailId:C93990
  Scenario: Verify backdated period payment rate change after repayment outcomes with valid amortization schedule calculations - UC16
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
#--- before any rate change: a flat schedule at the original 18, 50.00 a day throughout ---#
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 8         | 2026-01-09 | 50.00                 | 8675.67         | 9.31                       | 924.33                     |
      | 9         | 2026-01-10 | 50.00                 | 8634.94         | 9.26                       | 915.07                     |
      | 18        | 2026-01-19 | 50.00                 | 8266.35         | 8.87                       | 833.66                     |
      | 19        | 2026-01-20 | 50.00                 | 8225.18         | 8.83                       | 824.83                     |
#--- first change, effective today: everything up to 19 January is left exactly as it was ---#
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "20" value
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 18        | 2026-01-19 | 50.00                 | 8266.35         | 8.87                       | 833.66                     |
      | 19        | 2026-01-20 | 55.56                 | 8220.59         | 9.80                       | 823.86                     |
      | 20        | 2026-01-21 | 55.56                 | 8174.78         | 9.75                       | 814.11                     |
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 20 January 2026 | 18.0          | 20.0     | false    |
# --- add repayment on current biz date --- #
    And Customer makes repayment on "20 January 2026" with 100 transaction amount on Working Capital loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 20 January 2026 | Repayment                 | 100.0             | 100.0             | 0.0               | 0.0                   | false    |
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 18        | 2026-01-19 | 50.00                 | 8266.35         | 8.87                       | 833.66                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 19        | 2026-01-20 | 55.56                 | 8220.59         | 9.80                       | 823.86                     | 100.00              | 8183.61       | 17.26                    | 982.74                   |
      | 20        | 2026-01-21 | 55.56                 | 8174.78         | 9.75                       | 814.11                     |                     |               |                          |                          |
#--- second change, backdated ten days behind the first ---#
    And Admin update Working Capital period payment rate with "11" value effective from "10 January 2026"
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 10 January 2026 | 18.0          | 11.0     | false    |
      | 20 January 2026 | 11.0          | 20.0     | false    |
# The rate in force today is the latest change effective on or before it, which the backdated one sits behind.
    And Working Capital Loan period payment rate in effect is "20"
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 2026-01-01 | -9000.00              | 9000.00         |                            | 1000.00                    |                     | 9000.00       |                          | 1000.00                  |
      | 1         | 2026-01-02 | 50.00                 | 8959.61         | 9.61                       | 990.39                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 8         | 2026-01-09 | 50.00                 | 8675.67         | 9.31                       | 924.33                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 9         | 2026-01-10 | 30.56                 | 8650.78         | 5.67                       | 918.66                     | 0.00                | 8675.67       | 0.00                     | 1000.00                  |
      | 10        | 2026-01-11 | 30.56                 | 8625.88         | 5.66                       | 913.00                     | 0.00                | 8675.67       | 0.00                     | 1000.00                  |
      | 18        | 2026-01-19 | 30.56                 | 8426.06         | 5.53                       | 868.33                     | 0.00                | 8675.67       | 0.00                     | 1000.00                  |
      | 19        | 2026-01-20 | 55.56                 | 8380.49         | 9.99                       | 858.34                     | 100.00              | 8343.32       | 17.26                    | 982.74                   |
      | 20        | 2026-01-21 | 55.56                 | 8334.87         | 9.94                       | 848.40                     |                     |               |                          |                          |
      | 185       | 2026-07-05 | 55.56                 | 15.86           | 0.08                       | 0.03                       |                     |               |                          |                          |
      | 186       | 2026-07-06 | 15.88                 | 0.00            | 0.03                       | 0.00                       |                     |               |                          |                          |
      | 198       | 2026-07-18 | 50.00                 | 0.00            | 0.00                       | 0.00                       |                     |               |                          |                          |
    Then Admin closes the Working Capital loan with a full repayment on "20 January 2026"

  @TestRailId:C93991
  Scenario: Verify period payment rate change with backdated repayment with charge fee outcomes with valid amortization schedule calculations - UC17
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin retrieves the projected amortization schedule
    And Admin remembers the retrieved amortization schedule payments before "2026-01-10"
#--- before the change: a flat schedule at the original 18 across the effective date ---#
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 8         | 2026-01-09 | 50.00                 | 8600.00         | 0.00                       | 0.00                       |
      | 9         | 2026-01-10 | 50.00                 | 8550.00         | 0.00                       | 0.00                       |
      | 10        | 2026-01-11 | 50.00                 | 8500.00         | 0.00                       | 0.00                       |
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 35.0 transaction amount
#--- a repayment lands before the change, then the change is backdated to a date after it ---#
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "15" value effective from "20 January 2026"
    And Customer makes repayment on "10 January 2026" with 500 transaction amount on Working Capital loan
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 20 January 2026 | 18.0          | 15.0     | false    |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment                 | 500.0             | 465.0            | 35.0              | 0.0                   | false    |
    And Admin retrieves the projected amortization schedule
# Rebuilding the whole schedule must not disturb what came before the change: the periods up to 09 January are a pure
# function of the opening balance and the original rate, and the repayment on 05 January belongs to them.
    Then The retrieved amortization schedule payments before "2026-01-10" match the previously remembered ones
# Side by side with the snapshot above: period 8 is unchanged down to the cent, period 9 onwards is repriced at 11.
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 8         | 2026-01-09 | 50.00                 | 8600.00         | 0.00                       | 0.00                       | 0.00                | 9000.00       | 0.00                     | 0.00                     |
      | 9         | 2026-01-10 | 50.00                 | 8550.00         | 0.00                       | 0.00                       | 465.00              | 8535.00       | 0.00                     | 0.00                     |
      | 10        | 2026-01-11 | 50.00                 | 8500.00         | 0.00                       | 0.00                       | 0.00                | 8535.00       | 0.00                     | 0.00                     |
      | 18        | 2026-01-19 | 50.00                 | 8100.00         | 0.00                       | 0.00                       | 0.00                | 8535.00       | 0.00                     | 0.00                     |
      | 19        | 2026-01-20 | 41.67                 | 8058.33         | 0.00                       | 0.00                       |                     |               |                          |                          |
      | 20        | 2026-01-21 | 41.67                 | 8016.66         | 0.00                       | 0.00                       |                     |               |                          |                          |
      | 225       | 2026-08-14 | 18.30                 | 0.00            | 0.00                       | 0.00                       |                     |               |                          |                          |
    When Admin sets the business date to "25 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital Loan period payment rate in effect is "15"
    Then Admin closes the Working Capital loan with a full repayment on "25 January 2026"

  @TestRailId:C93992
  Scenario: Verify period payment rate change backdated repayment reversal outcomes with valid amortization schedule calculations - UC18
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin retrieves the projected amortization schedule
    And Admin remembers the retrieved amortization schedule payments before "2026-01-10"
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "11" value effective from "01 February 2026"
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date   | Previous Rate | New Rate | Reversed |
      | 01 February 2026 | 18.0          | 11.0     | false    |
# --- add  backdated repayment on current biz date --- #
    And Customer makes repayment on "15 January 2026" with 100 transaction amount on Working Capital loan
    And Admin update Working Capital period payment rate with "21" value effective from "10 January 2026"
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date   | Previous Rate | New Rate | Reversed |
      | 10 January 2026  | 18.0          | 21.0     | false    |
      | 01 February 2026 | 21.0          | 11.0     | false    |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Repayment                 | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 8         | 2026-01-09 | 50.00                 | 8675.67         | 9.31                       | 924.33                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 9         | 2026-01-10 | 58.33                 | 8628.14         | 10.80                      | 913.53                     | 0.00                | 8675.67       | 0.00                     | 1000.00                  |
      | 14        | 2026-01-15 | 58.33                 | 8389.59         | 10.50                      | 860.43                     | 100.00              | 8592.12       | 16.45                    | 983.55                    |
      | 15        | 2026-01-16 | 58.33                 | 8341.71         | 10.44                      | 849.99                     | 0.00                | 8592.12       | 0.00                     | 983.55                   |
      | 18        | 2026-01-19 | 58.33                 | 8197.69         | 10.26                      | 819.03                     | 0.00                | 8592.12       | 0.00                     | 983.55                   |
      | 19        | 2026-01-20 | 58.33                 | 8149.56         | 10.20                      | 808.83                     |                     |               |                          |                          |
# - undo repayment trn --- #
    When Admin sets the business date to "02 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer undo "1"th "REPAYMENT" transaction made on "15 January 2026" on Working Capital loan
#--- On 02 February the 01 February change is the newest in force, so the backdated 10 January one sits behind it.
    And Working Capital Loan period payment rate in effect is "11"
    And Working Capital Loan has transactions:
      | transactionDate  | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026  | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026  | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026  | Repayment                 | 100.0             | 100.0            | 0.0               | 0.0                   | true     |
      | 20 January 2026  | Discount Fee Amortization | 16.45             |                  |                   |                       | false    |
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 8         | 2026-01-09 | 50.00                 | 8675.67         | 9.31                       | 924.33                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 9         | 2026-01-10 | 58.33                 | 8628.14         | 10.80                      | 913.53                     | 0.00                | 8675.67       | 0.00                     | 1000.00                  |
      | 14        | 2026-01-15 | 58.33                 | 8389.59         | 10.50                      | 860.43                     | 0.00                | 8675.67       | 0.00                     | 1000.00                   |
      | 15        | 2026-01-16 | 58.33                 | 8341.71         | 10.44                      | 849.99                     | 0.00                | 8675.67       | 0.00                     | 1000.00                  |
      | 18        | 2026-01-19 | 58.33                 | 8197.69         | 10.26                      | 819.03                     | 0.00                | 8675.67       | 0.00                     | 1000.00                  |
      | 19        | 2026-01-20 | 58.33                 | 8149.56         | 10.20                      | 808.83                     |                     |               |                          |                          |
#--- the effective date arrives and the change is in force at once. The assertion sits before the COB run
#--- deliberately: the rate in force is derived from the change history, so no job has to bring it up to date.
    When Admin sets the business date to "03 February 2026"
    Then Working Capital Loan period payment rate in effect is "11"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Admin closes the Working Capital loan with a full repayment on "03 February 2026"

  @TestRailId:C93993
  Scenario: Verify Working Capital amortization schedule with period payment rate change after repayment with amount less then expected - UC19
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment by loan external ID on "10 January 2026" with 30.0 transaction amount on Working Capital loan
    Then Working Capital loan amortization schedule has 190 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | actualPaymentAmount | expectedBalance | actualBalance | expectedAmortizationAmount | actualAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 01 January 2026  | -9000.00              |                     | 9000.00         | 9000.00       |                            |                          | 0.00                       |
      | 1         | 02 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 2         | 03 January 2026  | 50.00                 | 0.00                | 8900.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 3         | 04 January 2026  | 50.00                 | 0.00                | 8850.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 4         | 05 January 2026  | 50.00                 | 0.00                | 8800.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 5         | 06 January 2026  | 50.00                 | 0.00                | 8750.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 6         | 07 January 2026  | 50.00                 | 0.00                | 8700.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 7         | 08 January 2026  | 50.00                 | 0.00                | 8650.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 8         | 09 January 2026  | 50.00                 | 0.00                | 8600.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 9         | 10 January 2026  | 50.00                 | 30.00               | 8550.00         | 8970.00       | 0.00                       | 0.00                     | 0.00                       |
      | 10        | 11 January 2026  | 50.00                 |                     | 8500.00         |               | 0.00                       |                          | 0.00                       |
      | 11        | 12 January 2026  | 50.00                 |                     | 8450.00         |               | 0.00                       |                          | 0.00                       |
      | 12        | 13 January 2026  | 50.00                 |                     | 8400.00         |               | 0.00                       |                          | 0.00                       |
      | 13        | 14 January 2026  | 50.00                 |                     | 8350.00         |               | 0.00                       |                          | 0.00                       |
      | 14        | 15 January 2026  | 50.00                 |                     | 8300.00         |               | 0.00                       |                          | 0.00                       |
      | 15        | 16 January 2026  | 50.00                 |                     | 8250.00         |               | 0.00                       |                          | 0.00                       |
      | 16        | 17 January 2026  | 50.00                 |                     | 8200.00         |               | 0.00                       |                          | 0.00                       |
      | 17        | 18 January 2026  | 50.00                 |                     | 8150.00         |               | 0.00                       |                          | 0.00                       |
      | 18        | 19 January 2026  | 50.00                 |                     | 8100.00         |               | 0.00                       |                          | 0.00                       |
      | 19        | 20 January 2026  | 50.00                 |                     | 8050.00         |               | 0.00                       |                          | 0.00                       |
      | 20        | 21 January 2026  | 50.00                 |                     | 8000.00         |               | 0.00                       |                          | 0.00                       |
      | 21        | 22 January 2026  | 50.00                 |                     | 7950.00         |               | 0.00                       |                          | 0.00                       |
      | 22        | 23 January 2026  | 50.00                 |                     | 7900.00         |               | 0.00                       |                          | 0.00                       |
      | 23        | 24 January 2026  | 50.00                 |                     | 7850.00         |               | 0.00                       |                          | 0.00                       |
      | 24        | 25 January 2026  | 50.00                 |                     | 7800.00         |               | 0.00                       |                          | 0.00                       |
      | 25        | 26 January 2026  | 50.00                 |                     | 7750.00         |               | 0.00                       |                          | 0.00                       |
      | 26        | 27 January 2026  | 50.00                 |                     | 7700.00         |               | 0.00                       |                          | 0.00                       |
      | 27        | 28 January 2026  | 50.00                 |                     | 7650.00         |               | 0.00                       |                          | 0.00                       |
      | 28        | 29 January 2026  | 50.00                 |                     | 7600.00         |               | 0.00                       |                          | 0.00                       |
      | 29        | 30 January 2026  | 50.00                 |                     | 7550.00         |               | 0.00                       |                          | 0.00                       |
      | 30        | 31 January 2026  | 50.00                 |                     | 7500.00         |               | 0.00                       |                          | 0.00                       |
      | 31        | 01 February 2026 | 50.00                 |                     | 7450.00         |               | 0.00                       |                          | 0.00                       |
      | 32        | 02 February 2026 | 50.00                 |                     | 7400.00         |               | 0.00                       |                          | 0.00                       |
      | 33        | 03 February 2026 | 50.00                 |                     | 7350.00         |               | 0.00                       |                          | 0.00                       |
      | 34        | 04 February 2026 | 50.00                 |                     | 7300.00         |               | 0.00                       |                          | 0.00                       |
      | 35        | 05 February 2026 | 50.00                 |                     | 7250.00         |               | 0.00                       |                          | 0.00                       |
      | 36        | 06 February 2026 | 50.00                 |                     | 7200.00         |               | 0.00                       |                          | 0.00                       |
      | 37        | 07 February 2026 | 50.00                 |                     | 7150.00         |               | 0.00                       |                          | 0.00                       |
      | 38        | 08 February 2026 | 50.00                 |                     | 7100.00         |               | 0.00                       |                          | 0.00                       |
      | 39        | 09 February 2026 | 50.00                 |                     | 7050.00         |               | 0.00                       |                          | 0.00                       |
      | 40        | 10 February 2026 | 50.00                 |                     | 7000.00         |               | 0.00                       |                          | 0.00                       |
      | 41        | 11 February 2026 | 50.00                 |                     | 6950.00         |               | 0.00                       |                          | 0.00                       |
      | 42        | 12 February 2026 | 50.00                 |                     | 6900.00         |               | 0.00                       |                          | 0.00                       |
      | 43        | 13 February 2026 | 50.00                 |                     | 6850.00         |               | 0.00                       |                          | 0.00                       |
      | 44        | 14 February 2026 | 50.00                 |                     | 6800.00         |               | 0.00                       |                          | 0.00                       |
      | 45        | 15 February 2026 | 50.00                 |                     | 6750.00         |               | 0.00                       |                          | 0.00                       |
      | 46        | 16 February 2026 | 50.00                 |                     | 6700.00         |               | 0.00                       |                          | 0.00                       |
      | 47        | 17 February 2026 | 50.00                 |                     | 6650.00         |               | 0.00                       |                          | 0.00                       |
      | 48        | 18 February 2026 | 50.00                 |                     | 6600.00         |               | 0.00                       |                          | 0.00                       |
      | 49        | 19 February 2026 | 50.00                 |                     | 6550.00         |               | 0.00                       |                          | 0.00                       |
      | 50        | 20 February 2026 | 50.00                 |                     | 6500.00         |               | 0.00                       |                          | 0.00                       |
      | 51        | 21 February 2026 | 50.00                 |                     | 6450.00         |               | 0.00                       |                          | 0.00                       |
      | 52        | 22 February 2026 | 50.00                 |                     | 6400.00         |               | 0.00                       |                          | 0.00                       |
      | 53        | 23 February 2026 | 50.00                 |                     | 6350.00         |               | 0.00                       |                          | 0.00                       |
      | 54        | 24 February 2026 | 50.00                 |                     | 6300.00         |               | 0.00                       |                          | 0.00                       |
      | 55        | 25 February 2026 | 50.00                 |                     | 6250.00         |               | 0.00                       |                          | 0.00                       |
      | 56        | 26 February 2026 | 50.00                 |                     | 6200.00         |               | 0.00                       |                          | 0.00                       |
      | 57        | 27 February 2026 | 50.00                 |                     | 6150.00         |               | 0.00                       |                          | 0.00                       |
      | 58        | 28 February 2026 | 50.00                 |                     | 6100.00         |               | 0.00                       |                          | 0.00                       |
      | 59        | 01 March 2026    | 50.00                 |                     | 6050.00         |               | 0.00                       |                          | 0.00                       |
      | 60        | 02 March 2026    | 50.00                 |                     | 6000.00         |               | 0.00                       |                          | 0.00                       |
      | 61        | 03 March 2026    | 50.00                 |                     | 5950.00         |               | 0.00                       |                          | 0.00                       |
      | 62        | 04 March 2026    | 50.00                 |                     | 5900.00         |               | 0.00                       |                          | 0.00                       |
      | 63        | 05 March 2026    | 50.00                 |                     | 5850.00         |               | 0.00                       |                          | 0.00                       |
      | 64        | 06 March 2026    | 50.00                 |                     | 5800.00         |               | 0.00                       |                          | 0.00                       |
      | 65        | 07 March 2026    | 50.00                 |                     | 5750.00         |               | 0.00                       |                          | 0.00                       |
      | 66        | 08 March 2026    | 50.00                 |                     | 5700.00         |               | 0.00                       |                          | 0.00                       |
      | 67        | 09 March 2026    | 50.00                 |                     | 5650.00         |               | 0.00                       |                          | 0.00                       |
      | 68        | 10 March 2026    | 50.00                 |                     | 5600.00         |               | 0.00                       |                          | 0.00                       |
      | 69        | 11 March 2026    | 50.00                 |                     | 5550.00         |               | 0.00                       |                          | 0.00                       |
      | 70        | 12 March 2026    | 50.00                 |                     | 5500.00         |               | 0.00                       |                          | 0.00                       |
      | 71        | 13 March 2026    | 50.00                 |                     | 5450.00         |               | 0.00                       |                          | 0.00                       |
      | 72        | 14 March 2026    | 50.00                 |                     | 5400.00         |               | 0.00                       |                          | 0.00                       |
      | 73        | 15 March 2026    | 50.00                 |                     | 5350.00         |               | 0.00                       |                          | 0.00                       |
      | 74        | 16 March 2026    | 50.00                 |                     | 5300.00         |               | 0.00                       |                          | 0.00                       |
      | 75        | 17 March 2026    | 50.00                 |                     | 5250.00         |               | 0.00                       |                          | 0.00                       |
      | 76        | 18 March 2026    | 50.00                 |                     | 5200.00         |               | 0.00                       |                          | 0.00                       |
      | 77        | 19 March 2026    | 50.00                 |                     | 5150.00         |               | 0.00                       |                          | 0.00                       |
      | 78        | 20 March 2026    | 50.00                 |                     | 5100.00         |               | 0.00                       |                          | 0.00                       |
      | 79        | 21 March 2026    | 50.00                 |                     | 5050.00         |               | 0.00                       |                          | 0.00                       |
      | 80        | 22 March 2026    | 50.00                 |                     | 5000.00         |               | 0.00                       |                          | 0.00                       |
      | 81        | 23 March 2026    | 50.00                 |                     | 4950.00         |               | 0.00                       |                          | 0.00                       |
      | 82        | 24 March 2026    | 50.00                 |                     | 4900.00         |               | 0.00                       |                          | 0.00                       |
      | 83        | 25 March 2026    | 50.00                 |                     | 4850.00         |               | 0.00                       |                          | 0.00                       |
      | 84        | 26 March 2026    | 50.00                 |                     | 4800.00         |               | 0.00                       |                          | 0.00                       |
      | 85        | 27 March 2026    | 50.00                 |                     | 4750.00         |               | 0.00                       |                          | 0.00                       |
      | 86        | 28 March 2026    | 50.00                 |                     | 4700.00         |               | 0.00                       |                          | 0.00                       |
      | 87        | 29 March 2026    | 50.00                 |                     | 4650.00         |               | 0.00                       |                          | 0.00                       |
      | 88        | 30 March 2026    | 50.00                 |                     | 4600.00         |               | 0.00                       |                          | 0.00                       |
      | 89        | 31 March 2026    | 50.00                 |                     | 4550.00         |               | 0.00                       |                          | 0.00                       |
      | 90        | 01 April 2026    | 50.00                 |                     | 4500.00         |               | 0.00                       |                          | 0.00                       |
      | 91        | 02 April 2026    | 50.00                 |                     | 4450.00         |               | 0.00                       |                          | 0.00                       |
      | 92        | 03 April 2026    | 50.00                 |                     | 4400.00         |               | 0.00                       |                          | 0.00                       |
      | 93        | 04 April 2026    | 50.00                 |                     | 4350.00         |               | 0.00                       |                          | 0.00                       |
      | 94        | 05 April 2026    | 50.00                 |                     | 4300.00         |               | 0.00                       |                          | 0.00                       |
      | 95        | 06 April 2026    | 50.00                 |                     | 4250.00         |               | 0.00                       |                          | 0.00                       |
      | 96        | 07 April 2026    | 50.00                 |                     | 4200.00         |               | 0.00                       |                          | 0.00                       |
      | 97        | 08 April 2026    | 50.00                 |                     | 4150.00         |               | 0.00                       |                          | 0.00                       |
      | 98        | 09 April 2026    | 50.00                 |                     | 4100.00         |               | 0.00                       |                          | 0.00                       |
      | 99        | 10 April 2026    | 50.00                 |                     | 4050.00         |               | 0.00                       |                          | 0.00                       |
      | 100       | 11 April 2026    | 50.00                 |                     | 4000.00         |               | 0.00                       |                          | 0.00                       |
      | 101       | 12 April 2026    | 50.00                 |                     | 3950.00         |               | 0.00                       |                          | 0.00                       |
      | 102       | 13 April 2026    | 50.00                 |                     | 3900.00         |               | 0.00                       |                          | 0.00                       |
      | 103       | 14 April 2026    | 50.00                 |                     | 3850.00         |               | 0.00                       |                          | 0.00                       |
      | 104       | 15 April 2026    | 50.00                 |                     | 3800.00         |               | 0.00                       |                          | 0.00                       |
      | 105       | 16 April 2026    | 50.00                 |                     | 3750.00         |               | 0.00                       |                          | 0.00                       |
      | 106       | 17 April 2026    | 50.00                 |                     | 3700.00         |               | 0.00                       |                          | 0.00                       |
      | 107       | 18 April 2026    | 50.00                 |                     | 3650.00         |               | 0.00                       |                          | 0.00                       |
      | 108       | 19 April 2026    | 50.00                 |                     | 3600.00         |               | 0.00                       |                          | 0.00                       |
      | 109       | 20 April 2026    | 50.00                 |                     | 3550.00         |               | 0.00                       |                          | 0.00                       |
      | 110       | 21 April 2026    | 50.00                 |                     | 3500.00         |               | 0.00                       |                          | 0.00                       |
      | 111       | 22 April 2026    | 50.00                 |                     | 3450.00         |               | 0.00                       |                          | 0.00                       |
      | 112       | 23 April 2026    | 50.00                 |                     | 3400.00         |               | 0.00                       |                          | 0.00                       |
      | 113       | 24 April 2026    | 50.00                 |                     | 3350.00         |               | 0.00                       |                          | 0.00                       |
      | 114       | 25 April 2026    | 50.00                 |                     | 3300.00         |               | 0.00                       |                          | 0.00                       |
      | 115       | 26 April 2026    | 50.00                 |                     | 3250.00         |               | 0.00                       |                          | 0.00                       |
      | 116       | 27 April 2026    | 50.00                 |                     | 3200.00         |               | 0.00                       |                          | 0.00                       |
      | 117       | 28 April 2026    | 50.00                 |                     | 3150.00         |               | 0.00                       |                          | 0.00                       |
      | 118       | 29 April 2026    | 50.00                 |                     | 3100.00         |               | 0.00                       |                          | 0.00                       |
      | 119       | 30 April 2026    | 50.00                 |                     | 3050.00         |               | 0.00                       |                          | 0.00                       |
      | 120       | 01 May 2026      | 50.00                 |                     | 3000.00         |               | 0.00                       |                          | 0.00                       |
      | 121       | 02 May 2026      | 50.00                 |                     | 2950.00         |               | 0.00                       |                          | 0.00                       |
      | 122       | 03 May 2026      | 50.00                 |                     | 2900.00         |               | 0.00                       |                          | 0.00                       |
      | 123       | 04 May 2026      | 50.00                 |                     | 2850.00         |               | 0.00                       |                          | 0.00                       |
      | 124       | 05 May 2026      | 50.00                 |                     | 2800.00         |               | 0.00                       |                          | 0.00                       |
      | 125       | 06 May 2026      | 50.00                 |                     | 2750.00         |               | 0.00                       |                          | 0.00                       |
      | 126       | 07 May 2026      | 50.00                 |                     | 2700.00         |               | 0.00                       |                          | 0.00                       |
      | 127       | 08 May 2026      | 50.00                 |                     | 2650.00         |               | 0.00                       |                          | 0.00                       |
      | 128       | 09 May 2026      | 50.00                 |                     | 2600.00         |               | 0.00                       |                          | 0.00                       |
      | 129       | 10 May 2026      | 50.00                 |                     | 2550.00         |               | 0.00                       |                          | 0.00                       |
      | 130       | 11 May 2026      | 50.00                 |                     | 2500.00         |               | 0.00                       |                          | 0.00                       |
      | 131       | 12 May 2026      | 50.00                 |                     | 2450.00         |               | 0.00                       |                          | 0.00                       |
      | 132       | 13 May 2026      | 50.00                 |                     | 2400.00         |               | 0.00                       |                          | 0.00                       |
      | 133       | 14 May 2026      | 50.00                 |                     | 2350.00         |               | 0.00                       |                          | 0.00                       |
      | 134       | 15 May 2026      | 50.00                 |                     | 2300.00         |               | 0.00                       |                          | 0.00                       |
      | 135       | 16 May 2026      | 50.00                 |                     | 2250.00         |               | 0.00                       |                          | 0.00                       |
      | 136       | 17 May 2026      | 50.00                 |                     | 2200.00         |               | 0.00                       |                          | 0.00                       |
      | 137       | 18 May 2026      | 50.00                 |                     | 2150.00         |               | 0.00                       |                          | 0.00                       |
      | 138       | 19 May 2026      | 50.00                 |                     | 2100.00         |               | 0.00                       |                          | 0.00                       |
      | 139       | 20 May 2026      | 50.00                 |                     | 2050.00         |               | 0.00                       |                          | 0.00                       |
      | 140       | 21 May 2026      | 50.00                 |                     | 2000.00         |               | 0.00                       |                          | 0.00                       |
      | 141       | 22 May 2026      | 50.00                 |                     | 1950.00         |               | 0.00                       |                          | 0.00                       |
      | 142       | 23 May 2026      | 50.00                 |                     | 1900.00         |               | 0.00                       |                          | 0.00                       |
      | 143       | 24 May 2026      | 50.00                 |                     | 1850.00         |               | 0.00                       |                          | 0.00                       |
      | 144       | 25 May 2026      | 50.00                 |                     | 1800.00         |               | 0.00                       |                          | 0.00                       |
      | 145       | 26 May 2026      | 50.00                 |                     | 1750.00         |               | 0.00                       |                          | 0.00                       |
      | 146       | 27 May 2026      | 50.00                 |                     | 1700.00         |               | 0.00                       |                          | 0.00                       |
      | 147       | 28 May 2026      | 50.00                 |                     | 1650.00         |               | 0.00                       |                          | 0.00                       |
      | 148       | 29 May 2026      | 50.00                 |                     | 1600.00         |               | 0.00                       |                          | 0.00                       |
      | 149       | 30 May 2026      | 50.00                 |                     | 1550.00         |               | 0.00                       |                          | 0.00                       |
      | 150       | 31 May 2026      | 50.00                 |                     | 1500.00         |               | 0.00                       |                          | 0.00                       |
      | 151       | 01 June 2026     | 50.00                 |                     | 1450.00         |               | 0.00                       |                          | 0.00                       |
      | 152       | 02 June 2026     | 50.00                 |                     | 1400.00         |               | 0.00                       |                          | 0.00                       |
      | 153       | 03 June 2026     | 50.00                 |                     | 1350.00         |               | 0.00                       |                          | 0.00                       |
      | 154       | 04 June 2026     | 50.00                 |                     | 1300.00         |               | 0.00                       |                          | 0.00                       |
      | 155       | 05 June 2026     | 50.00                 |                     | 1250.00         |               | 0.00                       |                          | 0.00                       |
      | 156       | 06 June 2026     | 50.00                 |                     | 1200.00         |               | 0.00                       |                          | 0.00                       |
      | 157       | 07 June 2026     | 50.00                 |                     | 1150.00         |               | 0.00                       |                          | 0.00                       |
      | 158       | 08 June 2026     | 50.00                 |                     | 1100.00         |               | 0.00                       |                          | 0.00                       |
      | 159       | 09 June 2026     | 50.00                 |                     | 1050.00         |               | 0.00                       |                          | 0.00                       |
      | 160       | 10 June 2026     | 50.00                 |                     | 1000.00         |               | 0.00                       |                          | 0.00                       |
      | 161       | 11 June 2026     | 50.00                 |                     | 950.00          |               | 0.00                       |                          | 0.00                       |
      | 162       | 12 June 2026     | 50.00                 |                     | 900.00          |               | 0.00                       |                          | 0.00                       |
      | 163       | 13 June 2026     | 50.00                 |                     | 850.00          |               | 0.00                       |                          | 0.00                       |
      | 164       | 14 June 2026     | 50.00                 |                     | 800.00          |               | 0.00                       |                          | 0.00                       |
      | 165       | 15 June 2026     | 50.00                 |                     | 750.00          |               | 0.00                       |                          | 0.00                       |
      | 166       | 16 June 2026     | 50.00                 |                     | 700.00          |               | 0.00                       |                          | 0.00                       |
      | 167       | 17 June 2026     | 50.00                 |                     | 650.00          |               | 0.00                       |                          | 0.00                       |
      | 168       | 18 June 2026     | 50.00                 |                     | 600.00          |               | 0.00                       |                          | 0.00                       |
      | 169       | 19 June 2026     | 50.00                 |                     | 550.00          |               | 0.00                       |                          | 0.00                       |
      | 170       | 20 June 2026     | 50.00                 |                     | 500.00          |               | 0.00                       |                          | 0.00                       |
      | 171       | 21 June 2026     | 50.00                 |                     | 450.00          |               | 0.00                       |                          | 0.00                       |
      | 172       | 22 June 2026     | 50.00                 |                     | 400.00          |               | 0.00                       |                          | 0.00                       |
      | 173       | 23 June 2026     | 50.00                 |                     | 350.00          |               | 0.00                       |                          | 0.00                       |
      | 174       | 24 June 2026     | 50.00                 |                     | 300.00          |               | 0.00                       |                          | 0.00                       |
      | 175       | 25 June 2026     | 50.00                 |                     | 250.00          |               | 0.00                       |                          | 0.00                       |
      | 176       | 26 June 2026     | 50.00                 |                     | 200.00          |               | 0.00                       |                          | 0.00                       |
      | 177       | 27 June 2026     | 50.00                 |                     | 150.00          |               | 0.00                       |                          | 0.00                       |
      | 178       | 28 June 2026     | 50.00                 |                     | 100.00          |               | 0.00                       |                          | 0.00                       |
      | 179       | 29 June 2026     | 50.00                 |                     | 50.00           |               | 0.00                       |                          | 0.00                       |
      | 180       | 30 June 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 181       | 01 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 182       | 02 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 183       | 03 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 184       | 04 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 185       | 05 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 186       | 06 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 187       | 07 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 188       | 08 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 189       | 09 July 2026     | 20.00                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
#--- update period payment rate by externalId ---#
    And Admin update Working Capital period payment rate with "17" value by externalId
    Then Working Capital Loan Period Payment Rate changes history by externalId contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 10 January 2026 | 18.0          | 17.0     | false    |
    Then Working Capital loan amortization schedule has 201 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | actualPaymentAmount | expectedBalance | actualBalance | expectedAmortizationAmount | actualAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 01 January 2026  | -9000.00              |                     | 9000.00         | 9000.00       |                            |                          | 0.00                       |
      | 1         | 02 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 2         | 03 January 2026  | 50.00                 | 0.00                | 8900.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 3         | 04 January 2026  | 50.00                 | 0.00                | 8850.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 4         | 05 January 2026  | 50.00                 | 0.00                | 8800.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 5         | 06 January 2026  | 50.00                 | 0.00                | 8750.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 6         | 07 January 2026  | 50.00                 | 0.00                | 8700.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 7         | 08 January 2026  | 50.00                 | 0.00                | 8650.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 8         | 09 January 2026  | 50.00                 | 0.00                | 8600.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 9         | 10 January 2026  | 47.22                 | 30.00               | 8552.78         | 8570.00       | 0.00                       | 0.00                     | 0.00                       |
      | 10        | 11 January 2026  | 47.22                 |                     | 8505.56         |               | 0.00                       |                          | 0.00                       |
      | 11        | 12 January 2026  | 47.22                 |                     | 8458.34         |               | 0.00                       |                          | 0.00                       |
      | 12        | 13 January 2026  | 47.22                 |                     | 8411.12         |               | 0.00                       |                          | 0.00                       |
      | 13        | 14 January 2026  | 47.22                 |                     | 8363.90         |               | 0.00                       |                          | 0.00                       |
      | 14        | 15 January 2026  | 47.22                 |                     | 8316.68         |               | 0.00                       |                          | 0.00                       |
      | 15        | 16 January 2026  | 47.22                 |                     | 8269.46         |               | 0.00                       |                          | 0.00                       |
      | 16        | 17 January 2026  | 47.22                 |                     | 8222.24         |               | 0.00                       |                          | 0.00                       |
      | 17        | 18 January 2026  | 47.22                 |                     | 8175.02         |               | 0.00                       |                          | 0.00                       |
      | 18        | 19 January 2026  | 47.22                 |                     | 8127.80         |               | 0.00                       |                          | 0.00                       |
      | 19        | 20 January 2026  | 47.22                 |                     | 8080.58         |               | 0.00                       |                          | 0.00                       |
      | 20        | 21 January 2026  | 47.22                 |                     | 8033.36         |               | 0.00                       |                          | 0.00                       |
      | 21        | 22 January 2026  | 47.22                 |                     | 7986.14         |               | 0.00                       |                          | 0.00                       |
      | 22        | 23 January 2026  | 47.22                 |                     | 7938.92         |               | 0.00                       |                          | 0.00                       |
      | 23        | 24 January 2026  | 47.22                 |                     | 7891.70         |               | 0.00                       |                          | 0.00                       |
      | 24        | 25 January 2026  | 47.22                 |                     | 7844.48         |               | 0.00                       |                          | 0.00                       |
      | 25        | 26 January 2026  | 47.22                 |                     | 7797.26         |               | 0.00                       |                          | 0.00                       |
      | 26        | 27 January 2026  | 47.22                 |                     | 7750.04         |               | 0.00                       |                          | 0.00                       |
      | 27        | 28 January 2026  | 47.22                 |                     | 7702.82         |               | 0.00                       |                          | 0.00                       |
      | 28        | 29 January 2026  | 47.22                 |                     | 7655.60         |               | 0.00                       |                          | 0.00                       |
      | 29        | 30 January 2026  | 47.22                 |                     | 7608.38         |               | 0.00                       |                          | 0.00                       |
      | 30        | 31 January 2026  | 47.22                 |                     | 7561.16         |               | 0.00                       |                          | 0.00                       |
      | 31        | 01 February 2026 | 47.22                 |                     | 7513.94         |               | 0.00                       |                          | 0.00                       |
      | 32        | 02 February 2026 | 47.22                 |                     | 7466.72         |               | 0.00                       |                          | 0.00                       |
      | 33        | 03 February 2026 | 47.22                 |                     | 7419.50         |               | 0.00                       |                          | 0.00                       |
      | 34        | 04 February 2026 | 47.22                 |                     | 7372.28         |               | 0.00                       |                          | 0.00                       |
      | 35        | 05 February 2026 | 47.22                 |                     | 7325.06         |               | 0.00                       |                          | 0.00                       |
      | 36        | 06 February 2026 | 47.22                 |                     | 7277.84         |               | 0.00                       |                          | 0.00                       |
      | 37        | 07 February 2026 | 47.22                 |                     | 7230.62         |               | 0.00                       |                          | 0.00                       |
      | 38        | 08 February 2026 | 47.22                 |                     | 7183.40         |               | 0.00                       |                          | 0.00                       |
      | 39        | 09 February 2026 | 47.22                 |                     | 7136.18         |               | 0.00                       |                          | 0.00                       |
      | 40        | 10 February 2026 | 47.22                 |                     | 7088.96         |               | 0.00                       |                          | 0.00                       |
      | 41        | 11 February 2026 | 47.22                 |                     | 7041.74         |               | 0.00                       |                          | 0.00                       |
      | 42        | 12 February 2026 | 47.22                 |                     | 6994.52         |               | 0.00                       |                          | 0.00                       |
      | 43        | 13 February 2026 | 47.22                 |                     | 6947.30         |               | 0.00                       |                          | 0.00                       |
      | 44        | 14 February 2026 | 47.22                 |                     | 6900.08         |               | 0.00                       |                          | 0.00                       |
      | 45        | 15 February 2026 | 47.22                 |                     | 6852.86         |               | 0.00                       |                          | 0.00                       |
      | 46        | 16 February 2026 | 47.22                 |                     | 6805.64         |               | 0.00                       |                          | 0.00                       |
      | 47        | 17 February 2026 | 47.22                 |                     | 6758.42         |               | 0.00                       |                          | 0.00                       |
      | 48        | 18 February 2026 | 47.22                 |                     | 6711.20         |               | 0.00                       |                          | 0.00                       |
      | 49        | 19 February 2026 | 47.22                 |                     | 6663.98         |               | 0.00                       |                          | 0.00                       |
      | 50        | 20 February 2026 | 47.22                 |                     | 6616.76         |               | 0.00                       |                          | 0.00                       |
      | 51        | 21 February 2026 | 47.22                 |                     | 6569.54         |               | 0.00                       |                          | 0.00                       |
      | 52        | 22 February 2026 | 47.22                 |                     | 6522.32         |               | 0.00                       |                          | 0.00                       |
      | 53        | 23 February 2026 | 47.22                 |                     | 6475.10         |               | 0.00                       |                          | 0.00                       |
      | 54        | 24 February 2026 | 47.22                 |                     | 6427.88         |               | 0.00                       |                          | 0.00                       |
      | 55        | 25 February 2026 | 47.22                 |                     | 6380.66         |               | 0.00                       |                          | 0.00                       |
      | 56        | 26 February 2026 | 47.22                 |                     | 6333.44         |               | 0.00                       |                          | 0.00                       |
      | 57        | 27 February 2026 | 47.22                 |                     | 6286.22         |               | 0.00                       |                          | 0.00                       |
      | 58        | 28 February 2026 | 47.22                 |                     | 6239.00         |               | 0.00                       |                          | 0.00                       |
      | 59        | 01 March 2026    | 47.22                 |                     | 6191.78         |               | 0.00                       |                          | 0.00                       |
      | 60        | 02 March 2026    | 47.22                 |                     | 6144.56         |               | 0.00                       |                          | 0.00                       |
      | 61        | 03 March 2026    | 47.22                 |                     | 6097.34         |               | 0.00                       |                          | 0.00                       |
      | 62        | 04 March 2026    | 47.22                 |                     | 6050.12         |               | 0.00                       |                          | 0.00                       |
      | 63        | 05 March 2026    | 47.22                 |                     | 6002.90         |               | 0.00                       |                          | 0.00                       |
      | 64        | 06 March 2026    | 47.22                 |                     | 5955.68         |               | 0.00                       |                          | 0.00                       |
      | 65        | 07 March 2026    | 47.22                 |                     | 5908.46         |               | 0.00                       |                          | 0.00                       |
      | 66        | 08 March 2026    | 47.22                 |                     | 5861.24         |               | 0.00                       |                          | 0.00                       |
      | 67        | 09 March 2026    | 47.22                 |                     | 5814.02         |               | 0.00                       |                          | 0.00                       |
      | 68        | 10 March 2026    | 47.22                 |                     | 5766.80         |               | 0.00                       |                          | 0.00                       |
      | 69        | 11 March 2026    | 47.22                 |                     | 5719.58         |               | 0.00                       |                          | 0.00                       |
      | 70        | 12 March 2026    | 47.22                 |                     | 5672.36         |               | 0.00                       |                          | 0.00                       |
      | 71        | 13 March 2026    | 47.22                 |                     | 5625.14         |               | 0.00                       |                          | 0.00                       |
      | 72        | 14 March 2026    | 47.22                 |                     | 5577.92         |               | 0.00                       |                          | 0.00                       |
      | 73        | 15 March 2026    | 47.22                 |                     | 5530.70         |               | 0.00                       |                          | 0.00                       |
      | 74        | 16 March 2026    | 47.22                 |                     | 5483.48         |               | 0.00                       |                          | 0.00                       |
      | 75        | 17 March 2026    | 47.22                 |                     | 5436.26         |               | 0.00                       |                          | 0.00                       |
      | 76        | 18 March 2026    | 47.22                 |                     | 5389.04         |               | 0.00                       |                          | 0.00                       |
      | 77        | 19 March 2026    | 47.22                 |                     | 5341.82         |               | 0.00                       |                          | 0.00                       |
      | 78        | 20 March 2026    | 47.22                 |                     | 5294.60         |               | 0.00                       |                          | 0.00                       |
      | 79        | 21 March 2026    | 47.22                 |                     | 5247.38         |               | 0.00                       |                          | 0.00                       |
      | 80        | 22 March 2026    | 47.22                 |                     | 5200.16         |               | 0.00                       |                          | 0.00                       |
      | 81        | 23 March 2026    | 47.22                 |                     | 5152.94         |               | 0.00                       |                          | 0.00                       |
      | 82        | 24 March 2026    | 47.22                 |                     | 5105.72         |               | 0.00                       |                          | 0.00                       |
      | 83        | 25 March 2026    | 47.22                 |                     | 5058.50         |               | 0.00                       |                          | 0.00                       |
      | 84        | 26 March 2026    | 47.22                 |                     | 5011.28         |               | 0.00                       |                          | 0.00                       |
      | 85        | 27 March 2026    | 47.22                 |                     | 4964.06         |               | 0.00                       |                          | 0.00                       |
      | 86        | 28 March 2026    | 47.22                 |                     | 4916.84         |               | 0.00                       |                          | 0.00                       |
      | 87        | 29 March 2026    | 47.22                 |                     | 4869.62         |               | 0.00                       |                          | 0.00                       |
      | 88        | 30 March 2026    | 47.22                 |                     | 4822.40         |               | 0.00                       |                          | 0.00                       |
      | 89        | 31 March 2026    | 47.22                 |                     | 4775.18         |               | 0.00                       |                          | 0.00                       |
      | 90        | 01 April 2026    | 47.22                 |                     | 4727.96         |               | 0.00                       |                          | 0.00                       |
      | 91        | 02 April 2026    | 47.22                 |                     | 4680.74         |               | 0.00                       |                          | 0.00                       |
      | 92        | 03 April 2026    | 47.22                 |                     | 4633.52         |               | 0.00                       |                          | 0.00                       |
      | 93        | 04 April 2026    | 47.22                 |                     | 4586.30         |               | 0.00                       |                          | 0.00                       |
      | 94        | 05 April 2026    | 47.22                 |                     | 4539.08         |               | 0.00                       |                          | 0.00                       |
      | 95        | 06 April 2026    | 47.22                 |                     | 4491.86         |               | 0.00                       |                          | 0.00                       |
      | 96        | 07 April 2026    | 47.22                 |                     | 4444.64         |               | 0.00                       |                          | 0.00                       |
      | 97        | 08 April 2026    | 47.22                 |                     | 4397.42         |               | 0.00                       |                          | 0.00                       |
      | 98        | 09 April 2026    | 47.22                 |                     | 4350.20         |               | 0.00                       |                          | 0.00                       |
      | 99        | 10 April 2026    | 47.22                 |                     | 4302.98         |               | 0.00                       |                          | 0.00                       |
      | 100       | 11 April 2026    | 47.22                 |                     | 4255.76         |               | 0.00                       |                          | 0.00                       |
      | 101       | 12 April 2026    | 47.22                 |                     | 4208.54         |               | 0.00                       |                          | 0.00                       |
      | 102       | 13 April 2026    | 47.22                 |                     | 4161.32         |               | 0.00                       |                          | 0.00                       |
      | 103       | 14 April 2026    | 47.22                 |                     | 4114.10         |               | 0.00                       |                          | 0.00                       |
      | 104       | 15 April 2026    | 47.22                 |                     | 4066.88         |               | 0.00                       |                          | 0.00                       |
      | 105       | 16 April 2026    | 47.22                 |                     | 4019.66         |               | 0.00                       |                          | 0.00                       |
      | 106       | 17 April 2026    | 47.22                 |                     | 3972.44         |               | 0.00                       |                          | 0.00                       |
      | 107       | 18 April 2026    | 47.22                 |                     | 3925.22         |               | 0.00                       |                          | 0.00                       |
      | 108       | 19 April 2026    | 47.22                 |                     | 3878.00         |               | 0.00                       |                          | 0.00                       |
      | 109       | 20 April 2026    | 47.22                 |                     | 3830.78         |               | 0.00                       |                          | 0.00                       |
      | 110       | 21 April 2026    | 47.22                 |                     | 3783.56         |               | 0.00                       |                          | 0.00                       |
      | 111       | 22 April 2026    | 47.22                 |                     | 3736.34         |               | 0.00                       |                          | 0.00                       |
      | 112       | 23 April 2026    | 47.22                 |                     | 3689.12         |               | 0.00                       |                          | 0.00                       |
      | 113       | 24 April 2026    | 47.22                 |                     | 3641.90         |               | 0.00                       |                          | 0.00                       |
      | 114       | 25 April 2026    | 47.22                 |                     | 3594.68         |               | 0.00                       |                          | 0.00                       |
      | 115       | 26 April 2026    | 47.22                 |                     | 3547.46         |               | 0.00                       |                          | 0.00                       |
      | 116       | 27 April 2026    | 47.22                 |                     | 3500.24         |               | 0.00                       |                          | 0.00                       |
      | 117       | 28 April 2026    | 47.22                 |                     | 3453.02         |               | 0.00                       |                          | 0.00                       |
      | 118       | 29 April 2026    | 47.22                 |                     | 3405.80         |               | 0.00                       |                          | 0.00                       |
      | 119       | 30 April 2026    | 47.22                 |                     | 3358.58         |               | 0.00                       |                          | 0.00                       |
      | 120       | 01 May 2026      | 47.22                 |                     | 3311.36         |               | 0.00                       |                          | 0.00                       |
      | 121       | 02 May 2026      | 47.22                 |                     | 3264.14         |               | 0.00                       |                          | 0.00                       |
      | 122       | 03 May 2026      | 47.22                 |                     | 3216.92         |               | 0.00                       |                          | 0.00                       |
      | 123       | 04 May 2026      | 47.22                 |                     | 3169.70         |               | 0.00                       |                          | 0.00                       |
      | 124       | 05 May 2026      | 47.22                 |                     | 3122.48         |               | 0.00                       |                          | 0.00                       |
      | 125       | 06 May 2026      | 47.22                 |                     | 3075.26         |               | 0.00                       |                          | 0.00                       |
      | 126       | 07 May 2026      | 47.22                 |                     | 3028.04         |               | 0.00                       |                          | 0.00                       |
      | 127       | 08 May 2026      | 47.22                 |                     | 2980.82         |               | 0.00                       |                          | 0.00                       |
      | 128       | 09 May 2026      | 47.22                 |                     | 2933.60         |               | 0.00                       |                          | 0.00                       |
      | 129       | 10 May 2026      | 47.22                 |                     | 2886.38         |               | 0.00                       |                          | 0.00                       |
      | 130       | 11 May 2026      | 47.22                 |                     | 2839.16         |               | 0.00                       |                          | 0.00                       |
      | 131       | 12 May 2026      | 47.22                 |                     | 2791.94         |               | 0.00                       |                          | 0.00                       |
      | 132       | 13 May 2026      | 47.22                 |                     | 2744.72         |               | 0.00                       |                          | 0.00                       |
      | 133       | 14 May 2026      | 47.22                 |                     | 2697.50         |               | 0.00                       |                          | 0.00                       |
      | 134       | 15 May 2026      | 47.22                 |                     | 2650.28         |               | 0.00                       |                          | 0.00                       |
      | 135       | 16 May 2026      | 47.22                 |                     | 2603.06         |               | 0.00                       |                          | 0.00                       |
      | 136       | 17 May 2026      | 47.22                 |                     | 2555.84         |               | 0.00                       |                          | 0.00                       |
      | 137       | 18 May 2026      | 47.22                 |                     | 2508.62         |               | 0.00                       |                          | 0.00                       |
      | 138       | 19 May 2026      | 47.22                 |                     | 2461.40         |               | 0.00                       |                          | 0.00                       |
      | 139       | 20 May 2026      | 47.22                 |                     | 2414.18         |               | 0.00                       |                          | 0.00                       |
      | 140       | 21 May 2026      | 47.22                 |                     | 2366.96         |               | 0.00                       |                          | 0.00                       |
      | 141       | 22 May 2026      | 47.22                 |                     | 2319.74         |               | 0.00                       |                          | 0.00                       |
      | 142       | 23 May 2026      | 47.22                 |                     | 2272.52         |               | 0.00                       |                          | 0.00                       |
      | 143       | 24 May 2026      | 47.22                 |                     | 2225.30         |               | 0.00                       |                          | 0.00                       |
      | 144       | 25 May 2026      | 47.22                 |                     | 2178.08         |               | 0.00                       |                          | 0.00                       |
      | 145       | 26 May 2026      | 47.22                 |                     | 2130.86         |               | 0.00                       |                          | 0.00                       |
      | 146       | 27 May 2026      | 47.22                 |                     | 2083.64         |               | 0.00                       |                          | 0.00                       |
      | 147       | 28 May 2026      | 47.22                 |                     | 2036.42         |               | 0.00                       |                          | 0.00                       |
      | 148       | 29 May 2026      | 47.22                 |                     | 1989.20         |               | 0.00                       |                          | 0.00                       |
      | 149       | 30 May 2026      | 47.22                 |                     | 1941.98         |               | 0.00                       |                          | 0.00                       |
      | 150       | 31 May 2026      | 47.22                 |                     | 1894.76         |               | 0.00                       |                          | 0.00                       |
      | 151       | 01 June 2026     | 47.22                 |                     | 1847.54         |               | 0.00                       |                          | 0.00                       |
      | 152       | 02 June 2026     | 47.22                 |                     | 1800.32         |               | 0.00                       |                          | 0.00                       |
      | 153       | 03 June 2026     | 47.22                 |                     | 1753.10         |               | 0.00                       |                          | 0.00                       |
      | 154       | 04 June 2026     | 47.22                 |                     | 1705.88         |               | 0.00                       |                          | 0.00                       |
      | 155       | 05 June 2026     | 47.22                 |                     | 1658.66         |               | 0.00                       |                          | 0.00                       |
      | 156       | 06 June 2026     | 47.22                 |                     | 1611.44         |               | 0.00                       |                          | 0.00                       |
      | 157       | 07 June 2026     | 47.22                 |                     | 1564.22         |               | 0.00                       |                          | 0.00                       |
      | 158       | 08 June 2026     | 47.22                 |                     | 1517.00         |               | 0.00                       |                          | 0.00                       |
      | 159       | 09 June 2026     | 47.22                 |                     | 1469.78         |               | 0.00                       |                          | 0.00                       |
      | 160       | 10 June 2026     | 47.22                 |                     | 1422.56         |               | 0.00                       |                          | 0.00                       |
      | 161       | 11 June 2026     | 47.22                 |                     | 1375.34         |               | 0.00                       |                          | 0.00                       |
      | 162       | 12 June 2026     | 47.22                 |                     | 1328.12         |               | 0.00                       |                          | 0.00                       |
      | 163       | 13 June 2026     | 47.22                 |                     | 1280.90         |               | 0.00                       |                          | 0.00                       |
      | 164       | 14 June 2026     | 47.22                 |                     | 1233.68         |               | 0.00                       |                          | 0.00                       |
      | 165       | 15 June 2026     | 47.22                 |                     | 1186.46         |               | 0.00                       |                          | 0.00                       |
      | 166       | 16 June 2026     | 47.22                 |                     | 1139.24         |               | 0.00                       |                          | 0.00                       |
      | 167       | 17 June 2026     | 47.22                 |                     | 1092.02         |               | 0.00                       |                          | 0.00                       |
      | 168       | 18 June 2026     | 47.22                 |                     | 1044.80         |               | 0.00                       |                          | 0.00                       |
      | 169       | 19 June 2026     | 47.22                 |                     | 997.58          |               | 0.00                       |                          | 0.00                       |
      | 170       | 20 June 2026     | 47.22                 |                     | 950.36          |               | 0.00                       |                          | 0.00                       |
      | 171       | 21 June 2026     | 47.22                 |                     | 903.14          |               | 0.00                       |                          | 0.00                       |
      | 172       | 22 June 2026     | 47.22                 |                     | 855.92          |               | 0.00                       |                          | 0.00                       |
      | 173       | 23 June 2026     | 47.22                 |                     | 808.70          |               | 0.00                       |                          | 0.00                       |
      | 174       | 24 June 2026     | 47.22                 |                     | 761.48          |               | 0.00                       |                          | 0.00                       |
      | 175       | 25 June 2026     | 47.22                 |                     | 714.26          |               | 0.00                       |                          | 0.00                       |
      | 176       | 26 June 2026     | 47.22                 |                     | 667.04          |               | 0.00                       |                          | 0.00                       |
      | 177       | 27 June 2026     | 47.22                 |                     | 619.82          |               | 0.00                       |                          | 0.00                       |
      | 178       | 28 June 2026     | 47.22                 |                     | 572.60          |               | 0.00                       |                          | 0.00                       |
      | 179       | 29 June 2026     | 47.22                 |                     | 525.38          |               | 0.00                       |                          | 0.00                       |
      | 180       | 30 June 2026     | 47.22                 |                     | 478.16          |               | 0.00                       |                          | 0.00                       |
      | 181       | 01 July 2026     | 47.22                 |                     | 430.94          |               | 0.00                       |                          | 0.00                       |
      | 182       | 02 July 2026     | 47.22                 |                     | 383.72          |               | 0.00                       |                          | 0.00                       |
      | 183       | 03 July 2026     | 47.22                 |                     | 336.50          |               | 0.00                       |                          | 0.00                       |
      | 184       | 04 July 2026     | 47.22                 |                     | 289.28          |               | 0.00                       |                          | 0.00                       |
      | 185       | 05 July 2026     | 47.22                 |                     | 242.06          |               | 0.00                       |                          | 0.00                       |
      | 186       | 06 July 2026     | 47.22                 |                     | 194.84          |               | 0.00                       |                          | 0.00                       |
      | 187       | 07 July 2026     | 47.22                 |                     | 147.62          |               | 0.00                       |                          | 0.00                       |
      | 188       | 08 July 2026     | 47.22                 |                     | 100.40          |               | 0.00                       |                          | 0.00                       |
      | 189       | 09 July 2026     | 47.22                 |                     | 53.18           |               | 0.00                       |                          | 0.00                       |
      | 190       | 10 July 2026     | 47.22                 |                     | 5.96            |               | 0.00                       |                          | 0.00                       |
      | 191       | 11 July 2026     | 5.96                  |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 192       | 12 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 193       | 13 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 194       | 14 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 195       | 15 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 196       | 16 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 197       | 17 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 198       | 18 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 199       | 19 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 200       | 20 July 2026     | 39.46                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C93994
  Scenario: Verify Working Capital amortization schedule with period payment rate change after repayment with amount as expected - UC20
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 17                | 0        |
    Then Working Capital loan amortization schedule has 192 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | actualPaymentAmount | expectedBalance | actualBalance | expectedAmortizationAmount | actualAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 01 January 2026  | -9000.00              |                     | 9000.00         | 9000.00       |                            |                          | 0.00                       |
      | 1         | 02 January 2026  | 47.22                 |                     | 8952.78         |               | 0.00                       |                          | 0.00                       |
      | 2         | 03 January 2026  | 47.22                 |                     | 8905.56         |               | 0.00                       |                          | 0.00                       |
      | 3         | 04 January 2026  | 47.22                 |                     | 8858.34         |               | 0.00                       |                          | 0.00                       |
      | 4         | 05 January 2026  | 47.22                 |                     | 8811.12         |               | 0.00                       |                          | 0.00                       |
      | 5         | 06 January 2026  | 47.22                 |                     | 8763.90         |               | 0.00                       |                          | 0.00                       |
      | 6         | 07 January 2026  | 47.22                 |                     | 8716.68         |               | 0.00                       |                          | 0.00                       |
      | 7         | 08 January 2026  | 47.22                 |                     | 8669.46         |               | 0.00                       |                          | 0.00                       |
      | 8         | 09 January 2026  | 47.22                 |                     | 8622.24         |               | 0.00                       |                          | 0.00                       |
      | 9         | 10 January 2026  | 47.22                 |                     | 8575.02         |               | 0.00                       |                          | 0.00                       |
      | 10        | 11 January 2026  | 47.22                 |                     | 8527.80         |               | 0.00                       |                          | 0.00                       |
      | 11        | 12 January 2026  | 47.22                 |                     | 8480.58         |               | 0.00                       |                          | 0.00                       |
      | 12        | 13 January 2026  | 47.22                 |                     | 8433.36         |               | 0.00                       |                          | 0.00                       |
      | 13        | 14 January 2026  | 47.22                 |                     | 8386.14         |               | 0.00                       |                          | 0.00                       |
      | 14        | 15 January 2026  | 47.22                 |                     | 8338.92         |               | 0.00                       |                          | 0.00                       |
      | 15        | 16 January 2026  | 47.22                 |                     | 8291.70         |               | 0.00                       |                          | 0.00                       |
      | 16        | 17 January 2026  | 47.22                 |                     | 8244.48         |               | 0.00                       |                          | 0.00                       |
      | 17        | 18 January 2026  | 47.22                 |                     | 8197.26         |               | 0.00                       |                          | 0.00                       |
      | 18        | 19 January 2026  | 47.22                 |                     | 8150.04         |               | 0.00                       |                          | 0.00                       |
      | 19        | 20 January 2026  | 47.22                 |                     | 8102.82         |               | 0.00                       |                          | 0.00                       |
      | 20        | 21 January 2026  | 47.22                 |                     | 8055.60         |               | 0.00                       |                          | 0.00                       |
      | 21        | 22 January 2026  | 47.22                 |                     | 8008.38         |               | 0.00                       |                          | 0.00                       |
      | 22        | 23 January 2026  | 47.22                 |                     | 7961.16         |               | 0.00                       |                          | 0.00                       |
      | 23        | 24 January 2026  | 47.22                 |                     | 7913.94         |               | 0.00                       |                          | 0.00                       |
      | 24        | 25 January 2026  | 47.22                 |                     | 7866.72         |               | 0.00                       |                          | 0.00                       |
      | 25        | 26 January 2026  | 47.22                 |                     | 7819.50         |               | 0.00                       |                          | 0.00                       |
      | 26        | 27 January 2026  | 47.22                 |                     | 7772.28         |               | 0.00                       |                          | 0.00                       |
      | 27        | 28 January 2026  | 47.22                 |                     | 7725.06         |               | 0.00                       |                          | 0.00                       |
      | 28        | 29 January 2026  | 47.22                 |                     | 7677.84         |               | 0.00                       |                          | 0.00                       |
      | 29        | 30 January 2026  | 47.22                 |                     | 7630.62         |               | 0.00                       |                          | 0.00                       |
      | 30        | 31 January 2026  | 47.22                 |                     | 7583.40         |               | 0.00                       |                          | 0.00                       |
      | 31        | 01 February 2026 | 47.22                 |                     | 7536.18         |               | 0.00                       |                          | 0.00                       |
      | 32        | 02 February 2026 | 47.22                 |                     | 7488.96         |               | 0.00                       |                          | 0.00                       |
      | 33        | 03 February 2026 | 47.22                 |                     | 7441.74         |               | 0.00                       |                          | 0.00                       |
      | 34        | 04 February 2026 | 47.22                 |                     | 7394.52         |               | 0.00                       |                          | 0.00                       |
      | 35        | 05 February 2026 | 47.22                 |                     | 7347.30         |               | 0.00                       |                          | 0.00                       |
      | 36        | 06 February 2026 | 47.22                 |                     | 7300.08         |               | 0.00                       |                          | 0.00                       |
      | 37        | 07 February 2026 | 47.22                 |                     | 7252.86         |               | 0.00                       |                          | 0.00                       |
      | 38        | 08 February 2026 | 47.22                 |                     | 7205.64         |               | 0.00                       |                          | 0.00                       |
      | 39        | 09 February 2026 | 47.22                 |                     | 7158.42         |               | 0.00                       |                          | 0.00                       |
      | 40        | 10 February 2026 | 47.22                 |                     | 7111.20         |               | 0.00                       |                          | 0.00                       |
      | 41        | 11 February 2026 | 47.22                 |                     | 7063.98         |               | 0.00                       |                          | 0.00                       |
      | 42        | 12 February 2026 | 47.22                 |                     | 7016.76         |               | 0.00                       |                          | 0.00                       |
      | 43        | 13 February 2026 | 47.22                 |                     | 6969.54         |               | 0.00                       |                          | 0.00                       |
      | 44        | 14 February 2026 | 47.22                 |                     | 6922.32         |               | 0.00                       |                          | 0.00                       |
      | 45        | 15 February 2026 | 47.22                 |                     | 6875.10         |               | 0.00                       |                          | 0.00                       |
      | 46        | 16 February 2026 | 47.22                 |                     | 6827.88         |               | 0.00                       |                          | 0.00                       |
      | 47        | 17 February 2026 | 47.22                 |                     | 6780.66         |               | 0.00                       |                          | 0.00                       |
      | 48        | 18 February 2026 | 47.22                 |                     | 6733.44         |               | 0.00                       |                          | 0.00                       |
      | 49        | 19 February 2026 | 47.22                 |                     | 6686.22         |               | 0.00                       |                          | 0.00                       |
      | 50        | 20 February 2026 | 47.22                 |                     | 6639.00         |               | 0.00                       |                          | 0.00                       |
      | 51        | 21 February 2026 | 47.22                 |                     | 6591.78         |               | 0.00                       |                          | 0.00                       |
      | 52        | 22 February 2026 | 47.22                 |                     | 6544.56         |               | 0.00                       |                          | 0.00                       |
      | 53        | 23 February 2026 | 47.22                 |                     | 6497.34         |               | 0.00                       |                          | 0.00                       |
      | 54        | 24 February 2026 | 47.22                 |                     | 6450.12         |               | 0.00                       |                          | 0.00                       |
      | 55        | 25 February 2026 | 47.22                 |                     | 6402.90         |               | 0.00                       |                          | 0.00                       |
      | 56        | 26 February 2026 | 47.22                 |                     | 6355.68         |               | 0.00                       |                          | 0.00                       |
      | 57        | 27 February 2026 | 47.22                 |                     | 6308.46         |               | 0.00                       |                          | 0.00                       |
      | 58        | 28 February 2026 | 47.22                 |                     | 6261.24         |               | 0.00                       |                          | 0.00                       |
      | 59        | 01 March 2026    | 47.22                 |                     | 6214.02         |               | 0.00                       |                          | 0.00                       |
      | 60        | 02 March 2026    | 47.22                 |                     | 6166.80         |               | 0.00                       |                          | 0.00                       |
      | 61        | 03 March 2026    | 47.22                 |                     | 6119.58         |               | 0.00                       |                          | 0.00                       |
      | 62        | 04 March 2026    | 47.22                 |                     | 6072.36         |               | 0.00                       |                          | 0.00                       |
      | 63        | 05 March 2026    | 47.22                 |                     | 6025.14         |               | 0.00                       |                          | 0.00                       |
      | 64        | 06 March 2026    | 47.22                 |                     | 5977.92         |               | 0.00                       |                          | 0.00                       |
      | 65        | 07 March 2026    | 47.22                 |                     | 5930.70         |               | 0.00                       |                          | 0.00                       |
      | 66        | 08 March 2026    | 47.22                 |                     | 5883.48         |               | 0.00                       |                          | 0.00                       |
      | 67        | 09 March 2026    | 47.22                 |                     | 5836.26         |               | 0.00                       |                          | 0.00                       |
      | 68        | 10 March 2026    | 47.22                 |                     | 5789.04         |               | 0.00                       |                          | 0.00                       |
      | 69        | 11 March 2026    | 47.22                 |                     | 5741.82         |               | 0.00                       |                          | 0.00                       |
      | 70        | 12 March 2026    | 47.22                 |                     | 5694.60         |               | 0.00                       |                          | 0.00                       |
      | 71        | 13 March 2026    | 47.22                 |                     | 5647.38         |               | 0.00                       |                          | 0.00                       |
      | 72        | 14 March 2026    | 47.22                 |                     | 5600.16         |               | 0.00                       |                          | 0.00                       |
      | 73        | 15 March 2026    | 47.22                 |                     | 5552.94         |               | 0.00                       |                          | 0.00                       |
      | 74        | 16 March 2026    | 47.22                 |                     | 5505.72         |               | 0.00                       |                          | 0.00                       |
      | 75        | 17 March 2026    | 47.22                 |                     | 5458.50         |               | 0.00                       |                          | 0.00                       |
      | 76        | 18 March 2026    | 47.22                 |                     | 5411.28         |               | 0.00                       |                          | 0.00                       |
      | 77        | 19 March 2026    | 47.22                 |                     | 5364.06         |               | 0.00                       |                          | 0.00                       |
      | 78        | 20 March 2026    | 47.22                 |                     | 5316.84         |               | 0.00                       |                          | 0.00                       |
      | 79        | 21 March 2026    | 47.22                 |                     | 5269.62         |               | 0.00                       |                          | 0.00                       |
      | 80        | 22 March 2026    | 47.22                 |                     | 5222.40         |               | 0.00                       |                          | 0.00                       |
      | 81        | 23 March 2026    | 47.22                 |                     | 5175.18         |               | 0.00                       |                          | 0.00                       |
      | 82        | 24 March 2026    | 47.22                 |                     | 5127.96         |               | 0.00                       |                          | 0.00                       |
      | 83        | 25 March 2026    | 47.22                 |                     | 5080.74         |               | 0.00                       |                          | 0.00                       |
      | 84        | 26 March 2026    | 47.22                 |                     | 5033.52         |               | 0.00                       |                          | 0.00                       |
      | 85        | 27 March 2026    | 47.22                 |                     | 4986.30         |               | 0.00                       |                          | 0.00                       |
      | 86        | 28 March 2026    | 47.22                 |                     | 4939.08         |               | 0.00                       |                          | 0.00                       |
      | 87        | 29 March 2026    | 47.22                 |                     | 4891.86         |               | 0.00                       |                          | 0.00                       |
      | 88        | 30 March 2026    | 47.22                 |                     | 4844.64         |               | 0.00                       |                          | 0.00                       |
      | 89        | 31 March 2026    | 47.22                 |                     | 4797.42         |               | 0.00                       |                          | 0.00                       |
      | 90        | 01 April 2026    | 47.22                 |                     | 4750.20         |               | 0.00                       |                          | 0.00                       |
      | 91        | 02 April 2026    | 47.22                 |                     | 4702.98         |               | 0.00                       |                          | 0.00                       |
      | 92        | 03 April 2026    | 47.22                 |                     | 4655.76         |               | 0.00                       |                          | 0.00                       |
      | 93        | 04 April 2026    | 47.22                 |                     | 4608.54         |               | 0.00                       |                          | 0.00                       |
      | 94        | 05 April 2026    | 47.22                 |                     | 4561.32         |               | 0.00                       |                          | 0.00                       |
      | 95        | 06 April 2026    | 47.22                 |                     | 4514.10         |               | 0.00                       |                          | 0.00                       |
      | 96        | 07 April 2026    | 47.22                 |                     | 4466.88         |               | 0.00                       |                          | 0.00                       |
      | 97        | 08 April 2026    | 47.22                 |                     | 4419.66         |               | 0.00                       |                          | 0.00                       |
      | 98        | 09 April 2026    | 47.22                 |                     | 4372.44         |               | 0.00                       |                          | 0.00                       |
      | 99        | 10 April 2026    | 47.22                 |                     | 4325.22         |               | 0.00                       |                          | 0.00                       |
      | 100       | 11 April 2026    | 47.22                 |                     | 4278.00         |               | 0.00                       |                          | 0.00                       |
      | 101       | 12 April 2026    | 47.22                 |                     | 4230.78         |               | 0.00                       |                          | 0.00                       |
      | 102       | 13 April 2026    | 47.22                 |                     | 4183.56         |               | 0.00                       |                          | 0.00                       |
      | 103       | 14 April 2026    | 47.22                 |                     | 4136.34         |               | 0.00                       |                          | 0.00                       |
      | 104       | 15 April 2026    | 47.22                 |                     | 4089.12         |               | 0.00                       |                          | 0.00                       |
      | 105       | 16 April 2026    | 47.22                 |                     | 4041.90         |               | 0.00                       |                          | 0.00                       |
      | 106       | 17 April 2026    | 47.22                 |                     | 3994.68         |               | 0.00                       |                          | 0.00                       |
      | 107       | 18 April 2026    | 47.22                 |                     | 3947.46         |               | 0.00                       |                          | 0.00                       |
      | 108       | 19 April 2026    | 47.22                 |                     | 3900.24         |               | 0.00                       |                          | 0.00                       |
      | 109       | 20 April 2026    | 47.22                 |                     | 3853.02         |               | 0.00                       |                          | 0.00                       |
      | 110       | 21 April 2026    | 47.22                 |                     | 3805.80         |               | 0.00                       |                          | 0.00                       |
      | 111       | 22 April 2026    | 47.22                 |                     | 3758.58         |               | 0.00                       |                          | 0.00                       |
      | 112       | 23 April 2026    | 47.22                 |                     | 3711.36         |               | 0.00                       |                          | 0.00                       |
      | 113       | 24 April 2026    | 47.22                 |                     | 3664.14         |               | 0.00                       |                          | 0.00                       |
      | 114       | 25 April 2026    | 47.22                 |                     | 3616.92         |               | 0.00                       |                          | 0.00                       |
      | 115       | 26 April 2026    | 47.22                 |                     | 3569.70         |               | 0.00                       |                          | 0.00                       |
      | 116       | 27 April 2026    | 47.22                 |                     | 3522.48         |               | 0.00                       |                          | 0.00                       |
      | 117       | 28 April 2026    | 47.22                 |                     | 3475.26         |               | 0.00                       |                          | 0.00                       |
      | 118       | 29 April 2026    | 47.22                 |                     | 3428.04         |               | 0.00                       |                          | 0.00                       |
      | 119       | 30 April 2026    | 47.22                 |                     | 3380.82         |               | 0.00                       |                          | 0.00                       |
      | 120       | 01 May 2026      | 47.22                 |                     | 3333.60         |               | 0.00                       |                          | 0.00                       |
      | 121       | 02 May 2026      | 47.22                 |                     | 3286.38         |               | 0.00                       |                          | 0.00                       |
      | 122       | 03 May 2026      | 47.22                 |                     | 3239.16         |               | 0.00                       |                          | 0.00                       |
      | 123       | 04 May 2026      | 47.22                 |                     | 3191.94         |               | 0.00                       |                          | 0.00                       |
      | 124       | 05 May 2026      | 47.22                 |                     | 3144.72         |               | 0.00                       |                          | 0.00                       |
      | 125       | 06 May 2026      | 47.22                 |                     | 3097.50         |               | 0.00                       |                          | 0.00                       |
      | 126       | 07 May 2026      | 47.22                 |                     | 3050.28         |               | 0.00                       |                          | 0.00                       |
      | 127       | 08 May 2026      | 47.22                 |                     | 3003.06         |               | 0.00                       |                          | 0.00                       |
      | 128       | 09 May 2026      | 47.22                 |                     | 2955.84         |               | 0.00                       |                          | 0.00                       |
      | 129       | 10 May 2026      | 47.22                 |                     | 2908.62         |               | 0.00                       |                          | 0.00                       |
      | 130       | 11 May 2026      | 47.22                 |                     | 2861.40         |               | 0.00                       |                          | 0.00                       |
      | 131       | 12 May 2026      | 47.22                 |                     | 2814.18         |               | 0.00                       |                          | 0.00                       |
      | 132       | 13 May 2026      | 47.22                 |                     | 2766.96         |               | 0.00                       |                          | 0.00                       |
      | 133       | 14 May 2026      | 47.22                 |                     | 2719.74         |               | 0.00                       |                          | 0.00                       |
      | 134       | 15 May 2026      | 47.22                 |                     | 2672.52         |               | 0.00                       |                          | 0.00                       |
      | 135       | 16 May 2026      | 47.22                 |                     | 2625.30         |               | 0.00                       |                          | 0.00                       |
      | 136       | 17 May 2026      | 47.22                 |                     | 2578.08         |               | 0.00                       |                          | 0.00                       |
      | 137       | 18 May 2026      | 47.22                 |                     | 2530.86         |               | 0.00                       |                          | 0.00                       |
      | 138       | 19 May 2026      | 47.22                 |                     | 2483.64         |               | 0.00                       |                          | 0.00                       |
      | 139       | 20 May 2026      | 47.22                 |                     | 2436.42         |               | 0.00                       |                          | 0.00                       |
      | 140       | 21 May 2026      | 47.22                 |                     | 2389.20         |               | 0.00                       |                          | 0.00                       |
      | 141       | 22 May 2026      | 47.22                 |                     | 2341.98         |               | 0.00                       |                          | 0.00                       |
      | 142       | 23 May 2026      | 47.22                 |                     | 2294.76         |               | 0.00                       |                          | 0.00                       |
      | 143       | 24 May 2026      | 47.22                 |                     | 2247.54         |               | 0.00                       |                          | 0.00                       |
      | 144       | 25 May 2026      | 47.22                 |                     | 2200.32         |               | 0.00                       |                          | 0.00                       |
      | 145       | 26 May 2026      | 47.22                 |                     | 2153.10         |               | 0.00                       |                          | 0.00                       |
      | 146       | 27 May 2026      | 47.22                 |                     | 2105.88         |               | 0.00                       |                          | 0.00                       |
      | 147       | 28 May 2026      | 47.22                 |                     | 2058.66         |               | 0.00                       |                          | 0.00                       |
      | 148       | 29 May 2026      | 47.22                 |                     | 2011.44         |               | 0.00                       |                          | 0.00                       |
      | 149       | 30 May 2026      | 47.22                 |                     | 1964.22         |               | 0.00                       |                          | 0.00                       |
      | 150       | 31 May 2026      | 47.22                 |                     | 1917.00         |               | 0.00                       |                          | 0.00                       |
      | 151       | 01 June 2026     | 47.22                 |                     | 1869.78         |               | 0.00                       |                          | 0.00                       |
      | 152       | 02 June 2026     | 47.22                 |                     | 1822.56         |               | 0.00                       |                          | 0.00                       |
      | 153       | 03 June 2026     | 47.22                 |                     | 1775.34         |               | 0.00                       |                          | 0.00                       |
      | 154       | 04 June 2026     | 47.22                 |                     | 1728.12         |               | 0.00                       |                          | 0.00                       |
      | 155       | 05 June 2026     | 47.22                 |                     | 1680.90         |               | 0.00                       |                          | 0.00                       |
      | 156       | 06 June 2026     | 47.22                 |                     | 1633.68         |               | 0.00                       |                          | 0.00                       |
      | 157       | 07 June 2026     | 47.22                 |                     | 1586.46         |               | 0.00                       |                          | 0.00                       |
      | 158       | 08 June 2026     | 47.22                 |                     | 1539.24         |               | 0.00                       |                          | 0.00                       |
      | 159       | 09 June 2026     | 47.22                 |                     | 1492.02         |               | 0.00                       |                          | 0.00                       |
      | 160       | 10 June 2026     | 47.22                 |                     | 1444.80         |               | 0.00                       |                          | 0.00                       |
      | 161       | 11 June 2026     | 47.22                 |                     | 1397.58         |               | 0.00                       |                          | 0.00                       |
      | 162       | 12 June 2026     | 47.22                 |                     | 1350.36         |               | 0.00                       |                          | 0.00                       |
      | 163       | 13 June 2026     | 47.22                 |                     | 1303.14         |               | 0.00                       |                          | 0.00                       |
      | 164       | 14 June 2026     | 47.22                 |                     | 1255.92         |               | 0.00                       |                          | 0.00                       |
      | 165       | 15 June 2026     | 47.22                 |                     | 1208.70         |               | 0.00                       |                          | 0.00                       |
      | 166       | 16 June 2026     | 47.22                 |                     | 1161.48         |               | 0.00                       |                          | 0.00                       |
      | 167       | 17 June 2026     | 47.22                 |                     | 1114.26         |               | 0.00                       |                          | 0.00                       |
      | 168       | 18 June 2026     | 47.22                 |                     | 1067.04         |               | 0.00                       |                          | 0.00                       |
      | 169       | 19 June 2026     | 47.22                 |                     | 1019.82         |               | 0.00                       |                          | 0.00                       |
      | 170       | 20 June 2026     | 47.22                 |                     | 972.60          |               | 0.00                       |                          | 0.00                       |
      | 171       | 21 June 2026     | 47.22                 |                     | 925.38          |               | 0.00                       |                          | 0.00                       |
      | 172       | 22 June 2026     | 47.22                 |                     | 878.16          |               | 0.00                       |                          | 0.00                       |
      | 173       | 23 June 2026     | 47.22                 |                     | 830.94          |               | 0.00                       |                          | 0.00                       |
      | 174       | 24 June 2026     | 47.22                 |                     | 783.72          |               | 0.00                       |                          | 0.00                       |
      | 175       | 25 June 2026     | 47.22                 |                     | 736.50          |               | 0.00                       |                          | 0.00                       |
      | 176       | 26 June 2026     | 47.22                 |                     | 689.28          |               | 0.00                       |                          | 0.00                       |
      | 177       | 27 June 2026     | 47.22                 |                     | 642.06          |               | 0.00                       |                          | 0.00                       |
      | 178       | 28 June 2026     | 47.22                 |                     | 594.84          |               | 0.00                       |                          | 0.00                       |
      | 179       | 29 June 2026     | 47.22                 |                     | 547.62          |               | 0.00                       |                          | 0.00                       |
      | 180       | 30 June 2026     | 47.22                 |                     | 500.40          |               | 0.00                       |                          | 0.00                       |
      | 181       | 01 July 2026     | 47.22                 |                     | 453.18          |               | 0.00                       |                          | 0.00                       |
      | 182       | 02 July 2026     | 47.22                 |                     | 405.96          |               | 0.00                       |                          | 0.00                       |
      | 183       | 03 July 2026     | 47.22                 |                     | 358.74          |               | 0.00                       |                          | 0.00                       |
      | 184       | 04 July 2026     | 47.22                 |                     | 311.52          |               | 0.00                       |                          | 0.00                       |
      | 185       | 05 July 2026     | 47.22                 |                     | 264.30          |               | 0.00                       |                          | 0.00                       |
      | 186       | 06 July 2026     | 47.22                 |                     | 217.08          |               | 0.00                       |                          | 0.00                       |
      | 187       | 07 July 2026     | 47.22                 |                     | 169.86          |               | 0.00                       |                          | 0.00                       |
      | 188       | 08 July 2026     | 47.22                 |                     | 122.64          |               | 0.00                       |                          | 0.00                       |
      | 189       | 09 July 2026     | 47.22                 |                     | 75.42           |               | 0.00                       |                          | 0.00                       |
      | 190       | 10 July 2026     | 47.22                 |                     | 28.20           |               | 0.00                       |                          | 0.00                       |
      | 191       | 11 July 2026     | 28.20                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment by loan external ID on "10 January 2026" with 30.0 transaction amount on Working Capital loan
    Then Working Capital loan amortization schedule has 201 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | actualPaymentAmount | expectedBalance | actualBalance | expectedAmortizationAmount | actualAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 01 January 2026  | -9000.00              |                     | 9000.00         | 9000.00       |                            |                          | 0.00                       |
      | 1         | 02 January 2026  | 47.22                 | 0.00                | 8952.78         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 2         | 03 January 2026  | 47.22                 | 0.00                | 8905.56         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 3         | 04 January 2026  | 47.22                 | 0.00                | 8858.34         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 4         | 05 January 2026  | 47.22                 | 0.00                | 8811.12         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 5         | 06 January 2026  | 47.22                 | 0.00                | 8763.90         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 6         | 07 January 2026  | 47.22                 | 0.00                | 8716.68         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 7         | 08 January 2026  | 47.22                 | 0.00                | 8669.46         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 8         | 09 January 2026  | 47.22                 | 0.00                | 8622.24         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 9         | 10 January 2026  | 47.22                 | 30.00               | 8575.02         | 8970.00       | 0.00                       | 0.00                     | 0.00                       |
      | 10        | 11 January 2026  | 47.22                 |                     | 8527.80         |               | 0.00                       |                          | 0.00                       |
      | 11        | 12 January 2026  | 47.22                 |                     | 8480.58         |               | 0.00                       |                          | 0.00                       |
      | 12        | 13 January 2026  | 47.22                 |                     | 8433.36         |               | 0.00                       |                          | 0.00                       |
      | 13        | 14 January 2026  | 47.22                 |                     | 8386.14         |               | 0.00                       |                          | 0.00                       |
      | 14        | 15 January 2026  | 47.22                 |                     | 8338.92         |               | 0.00                       |                          | 0.00                       |
      | 15        | 16 January 2026  | 47.22                 |                     | 8291.70         |               | 0.00                       |                          | 0.00                       |
      | 16        | 17 January 2026  | 47.22                 |                     | 8244.48         |               | 0.00                       |                          | 0.00                       |
      | 17        | 18 January 2026  | 47.22                 |                     | 8197.26         |               | 0.00                       |                          | 0.00                       |
      | 18        | 19 January 2026  | 47.22                 |                     | 8150.04         |               | 0.00                       |                          | 0.00                       |
      | 19        | 20 January 2026  | 47.22                 |                     | 8102.82         |               | 0.00                       |                          | 0.00                       |
      | 20        | 21 January 2026  | 47.22                 |                     | 8055.60         |               | 0.00                       |                          | 0.00                       |
      | 21        | 22 January 2026  | 47.22                 |                     | 8008.38         |               | 0.00                       |                          | 0.00                       |
      | 22        | 23 January 2026  | 47.22                 |                     | 7961.16         |               | 0.00                       |                          | 0.00                       |
      | 23        | 24 January 2026  | 47.22                 |                     | 7913.94         |               | 0.00                       |                          | 0.00                       |
      | 24        | 25 January 2026  | 47.22                 |                     | 7866.72         |               | 0.00                       |                          | 0.00                       |
      | 25        | 26 January 2026  | 47.22                 |                     | 7819.50         |               | 0.00                       |                          | 0.00                       |
      | 26        | 27 January 2026  | 47.22                 |                     | 7772.28         |               | 0.00                       |                          | 0.00                       |
      | 27        | 28 January 2026  | 47.22                 |                     | 7725.06         |               | 0.00                       |                          | 0.00                       |
      | 28        | 29 January 2026  | 47.22                 |                     | 7677.84         |               | 0.00                       |                          | 0.00                       |
      | 29        | 30 January 2026  | 47.22                 |                     | 7630.62         |               | 0.00                       |                          | 0.00                       |
      | 30        | 31 January 2026  | 47.22                 |                     | 7583.40         |               | 0.00                       |                          | 0.00                       |
      | 31        | 01 February 2026 | 47.22                 |                     | 7536.18         |               | 0.00                       |                          | 0.00                       |
      | 32        | 02 February 2026 | 47.22                 |                     | 7488.96         |               | 0.00                       |                          | 0.00                       |
      | 33        | 03 February 2026 | 47.22                 |                     | 7441.74         |               | 0.00                       |                          | 0.00                       |
      | 34        | 04 February 2026 | 47.22                 |                     | 7394.52         |               | 0.00                       |                          | 0.00                       |
      | 35        | 05 February 2026 | 47.22                 |                     | 7347.30         |               | 0.00                       |                          | 0.00                       |
      | 36        | 06 February 2026 | 47.22                 |                     | 7300.08         |               | 0.00                       |                          | 0.00                       |
      | 37        | 07 February 2026 | 47.22                 |                     | 7252.86         |               | 0.00                       |                          | 0.00                       |
      | 38        | 08 February 2026 | 47.22                 |                     | 7205.64         |               | 0.00                       |                          | 0.00                       |
      | 39        | 09 February 2026 | 47.22                 |                     | 7158.42         |               | 0.00                       |                          | 0.00                       |
      | 40        | 10 February 2026 | 47.22                 |                     | 7111.20         |               | 0.00                       |                          | 0.00                       |
      | 41        | 11 February 2026 | 47.22                 |                     | 7063.98         |               | 0.00                       |                          | 0.00                       |
      | 42        | 12 February 2026 | 47.22                 |                     | 7016.76         |               | 0.00                       |                          | 0.00                       |
      | 43        | 13 February 2026 | 47.22                 |                     | 6969.54         |               | 0.00                       |                          | 0.00                       |
      | 44        | 14 February 2026 | 47.22                 |                     | 6922.32         |               | 0.00                       |                          | 0.00                       |
      | 45        | 15 February 2026 | 47.22                 |                     | 6875.10         |               | 0.00                       |                          | 0.00                       |
      | 46        | 16 February 2026 | 47.22                 |                     | 6827.88         |               | 0.00                       |                          | 0.00                       |
      | 47        | 17 February 2026 | 47.22                 |                     | 6780.66         |               | 0.00                       |                          | 0.00                       |
      | 48        | 18 February 2026 | 47.22                 |                     | 6733.44         |               | 0.00                       |                          | 0.00                       |
      | 49        | 19 February 2026 | 47.22                 |                     | 6686.22         |               | 0.00                       |                          | 0.00                       |
      | 50        | 20 February 2026 | 47.22                 |                     | 6639.00         |               | 0.00                       |                          | 0.00                       |
      | 51        | 21 February 2026 | 47.22                 |                     | 6591.78         |               | 0.00                       |                          | 0.00                       |
      | 52        | 22 February 2026 | 47.22                 |                     | 6544.56         |               | 0.00                       |                          | 0.00                       |
      | 53        | 23 February 2026 | 47.22                 |                     | 6497.34         |               | 0.00                       |                          | 0.00                       |
      | 54        | 24 February 2026 | 47.22                 |                     | 6450.12         |               | 0.00                       |                          | 0.00                       |
      | 55        | 25 February 2026 | 47.22                 |                     | 6402.90         |               | 0.00                       |                          | 0.00                       |
      | 56        | 26 February 2026 | 47.22                 |                     | 6355.68         |               | 0.00                       |                          | 0.00                       |
      | 57        | 27 February 2026 | 47.22                 |                     | 6308.46         |               | 0.00                       |                          | 0.00                       |
      | 58        | 28 February 2026 | 47.22                 |                     | 6261.24         |               | 0.00                       |                          | 0.00                       |
      | 59        | 01 March 2026    | 47.22                 |                     | 6214.02         |               | 0.00                       |                          | 0.00                       |
      | 60        | 02 March 2026    | 47.22                 |                     | 6166.80         |               | 0.00                       |                          | 0.00                       |
      | 61        | 03 March 2026    | 47.22                 |                     | 6119.58         |               | 0.00                       |                          | 0.00                       |
      | 62        | 04 March 2026    | 47.22                 |                     | 6072.36         |               | 0.00                       |                          | 0.00                       |
      | 63        | 05 March 2026    | 47.22                 |                     | 6025.14         |               | 0.00                       |                          | 0.00                       |
      | 64        | 06 March 2026    | 47.22                 |                     | 5977.92         |               | 0.00                       |                          | 0.00                       |
      | 65        | 07 March 2026    | 47.22                 |                     | 5930.70         |               | 0.00                       |                          | 0.00                       |
      | 66        | 08 March 2026    | 47.22                 |                     | 5883.48         |               | 0.00                       |                          | 0.00                       |
      | 67        | 09 March 2026    | 47.22                 |                     | 5836.26         |               | 0.00                       |                          | 0.00                       |
      | 68        | 10 March 2026    | 47.22                 |                     | 5789.04         |               | 0.00                       |                          | 0.00                       |
      | 69        | 11 March 2026    | 47.22                 |                     | 5741.82         |               | 0.00                       |                          | 0.00                       |
      | 70        | 12 March 2026    | 47.22                 |                     | 5694.60         |               | 0.00                       |                          | 0.00                       |
      | 71        | 13 March 2026    | 47.22                 |                     | 5647.38         |               | 0.00                       |                          | 0.00                       |
      | 72        | 14 March 2026    | 47.22                 |                     | 5600.16         |               | 0.00                       |                          | 0.00                       |
      | 73        | 15 March 2026    | 47.22                 |                     | 5552.94         |               | 0.00                       |                          | 0.00                       |
      | 74        | 16 March 2026    | 47.22                 |                     | 5505.72         |               | 0.00                       |                          | 0.00                       |
      | 75        | 17 March 2026    | 47.22                 |                     | 5458.50         |               | 0.00                       |                          | 0.00                       |
      | 76        | 18 March 2026    | 47.22                 |                     | 5411.28         |               | 0.00                       |                          | 0.00                       |
      | 77        | 19 March 2026    | 47.22                 |                     | 5364.06         |               | 0.00                       |                          | 0.00                       |
      | 78        | 20 March 2026    | 47.22                 |                     | 5316.84         |               | 0.00                       |                          | 0.00                       |
      | 79        | 21 March 2026    | 47.22                 |                     | 5269.62         |               | 0.00                       |                          | 0.00                       |
      | 80        | 22 March 2026    | 47.22                 |                     | 5222.40         |               | 0.00                       |                          | 0.00                       |
      | 81        | 23 March 2026    | 47.22                 |                     | 5175.18         |               | 0.00                       |                          | 0.00                       |
      | 82        | 24 March 2026    | 47.22                 |                     | 5127.96         |               | 0.00                       |                          | 0.00                       |
      | 83        | 25 March 2026    | 47.22                 |                     | 5080.74         |               | 0.00                       |                          | 0.00                       |
      | 84        | 26 March 2026    | 47.22                 |                     | 5033.52         |               | 0.00                       |                          | 0.00                       |
      | 85        | 27 March 2026    | 47.22                 |                     | 4986.30         |               | 0.00                       |                          | 0.00                       |
      | 86        | 28 March 2026    | 47.22                 |                     | 4939.08         |               | 0.00                       |                          | 0.00                       |
      | 87        | 29 March 2026    | 47.22                 |                     | 4891.86         |               | 0.00                       |                          | 0.00                       |
      | 88        | 30 March 2026    | 47.22                 |                     | 4844.64         |               | 0.00                       |                          | 0.00                       |
      | 89        | 31 March 2026    | 47.22                 |                     | 4797.42         |               | 0.00                       |                          | 0.00                       |
      | 90        | 01 April 2026    | 47.22                 |                     | 4750.20         |               | 0.00                       |                          | 0.00                       |
      | 91        | 02 April 2026    | 47.22                 |                     | 4702.98         |               | 0.00                       |                          | 0.00                       |
      | 92        | 03 April 2026    | 47.22                 |                     | 4655.76         |               | 0.00                       |                          | 0.00                       |
      | 93        | 04 April 2026    | 47.22                 |                     | 4608.54         |               | 0.00                       |                          | 0.00                       |
      | 94        | 05 April 2026    | 47.22                 |                     | 4561.32         |               | 0.00                       |                          | 0.00                       |
      | 95        | 06 April 2026    | 47.22                 |                     | 4514.10         |               | 0.00                       |                          | 0.00                       |
      | 96        | 07 April 2026    | 47.22                 |                     | 4466.88         |               | 0.00                       |                          | 0.00                       |
      | 97        | 08 April 2026    | 47.22                 |                     | 4419.66         |               | 0.00                       |                          | 0.00                       |
      | 98        | 09 April 2026    | 47.22                 |                     | 4372.44         |               | 0.00                       |                          | 0.00                       |
      | 99        | 10 April 2026    | 47.22                 |                     | 4325.22         |               | 0.00                       |                          | 0.00                       |
      | 100       | 11 April 2026    | 47.22                 |                     | 4278.00         |               | 0.00                       |                          | 0.00                       |
      | 101       | 12 April 2026    | 47.22                 |                     | 4230.78         |               | 0.00                       |                          | 0.00                       |
      | 102       | 13 April 2026    | 47.22                 |                     | 4183.56         |               | 0.00                       |                          | 0.00                       |
      | 103       | 14 April 2026    | 47.22                 |                     | 4136.34         |               | 0.00                       |                          | 0.00                       |
      | 104       | 15 April 2026    | 47.22                 |                     | 4089.12         |               | 0.00                       |                          | 0.00                       |
      | 105       | 16 April 2026    | 47.22                 |                     | 4041.90         |               | 0.00                       |                          | 0.00                       |
      | 106       | 17 April 2026    | 47.22                 |                     | 3994.68         |               | 0.00                       |                          | 0.00                       |
      | 107       | 18 April 2026    | 47.22                 |                     | 3947.46         |               | 0.00                       |                          | 0.00                       |
      | 108       | 19 April 2026    | 47.22                 |                     | 3900.24         |               | 0.00                       |                          | 0.00                       |
      | 109       | 20 April 2026    | 47.22                 |                     | 3853.02         |               | 0.00                       |                          | 0.00                       |
      | 110       | 21 April 2026    | 47.22                 |                     | 3805.80         |               | 0.00                       |                          | 0.00                       |
      | 111       | 22 April 2026    | 47.22                 |                     | 3758.58         |               | 0.00                       |                          | 0.00                       |
      | 112       | 23 April 2026    | 47.22                 |                     | 3711.36         |               | 0.00                       |                          | 0.00                       |
      | 113       | 24 April 2026    | 47.22                 |                     | 3664.14         |               | 0.00                       |                          | 0.00                       |
      | 114       | 25 April 2026    | 47.22                 |                     | 3616.92         |               | 0.00                       |                          | 0.00                       |
      | 115       | 26 April 2026    | 47.22                 |                     | 3569.70         |               | 0.00                       |                          | 0.00                       |
      | 116       | 27 April 2026    | 47.22                 |                     | 3522.48         |               | 0.00                       |                          | 0.00                       |
      | 117       | 28 April 2026    | 47.22                 |                     | 3475.26         |               | 0.00                       |                          | 0.00                       |
      | 118       | 29 April 2026    | 47.22                 |                     | 3428.04         |               | 0.00                       |                          | 0.00                       |
      | 119       | 30 April 2026    | 47.22                 |                     | 3380.82         |               | 0.00                       |                          | 0.00                       |
      | 120       | 01 May 2026      | 47.22                 |                     | 3333.60         |               | 0.00                       |                          | 0.00                       |
      | 121       | 02 May 2026      | 47.22                 |                     | 3286.38         |               | 0.00                       |                          | 0.00                       |
      | 122       | 03 May 2026      | 47.22                 |                     | 3239.16         |               | 0.00                       |                          | 0.00                       |
      | 123       | 04 May 2026      | 47.22                 |                     | 3191.94         |               | 0.00                       |                          | 0.00                       |
      | 124       | 05 May 2026      | 47.22                 |                     | 3144.72         |               | 0.00                       |                          | 0.00                       |
      | 125       | 06 May 2026      | 47.22                 |                     | 3097.50         |               | 0.00                       |                          | 0.00                       |
      | 126       | 07 May 2026      | 47.22                 |                     | 3050.28         |               | 0.00                       |                          | 0.00                       |
      | 127       | 08 May 2026      | 47.22                 |                     | 3003.06         |               | 0.00                       |                          | 0.00                       |
      | 128       | 09 May 2026      | 47.22                 |                     | 2955.84         |               | 0.00                       |                          | 0.00                       |
      | 129       | 10 May 2026      | 47.22                 |                     | 2908.62         |               | 0.00                       |                          | 0.00                       |
      | 130       | 11 May 2026      | 47.22                 |                     | 2861.40         |               | 0.00                       |                          | 0.00                       |
      | 131       | 12 May 2026      | 47.22                 |                     | 2814.18         |               | 0.00                       |                          | 0.00                       |
      | 132       | 13 May 2026      | 47.22                 |                     | 2766.96         |               | 0.00                       |                          | 0.00                       |
      | 133       | 14 May 2026      | 47.22                 |                     | 2719.74         |               | 0.00                       |                          | 0.00                       |
      | 134       | 15 May 2026      | 47.22                 |                     | 2672.52         |               | 0.00                       |                          | 0.00                       |
      | 135       | 16 May 2026      | 47.22                 |                     | 2625.30         |               | 0.00                       |                          | 0.00                       |
      | 136       | 17 May 2026      | 47.22                 |                     | 2578.08         |               | 0.00                       |                          | 0.00                       |
      | 137       | 18 May 2026      | 47.22                 |                     | 2530.86         |               | 0.00                       |                          | 0.00                       |
      | 138       | 19 May 2026      | 47.22                 |                     | 2483.64         |               | 0.00                       |                          | 0.00                       |
      | 139       | 20 May 2026      | 47.22                 |                     | 2436.42         |               | 0.00                       |                          | 0.00                       |
      | 140       | 21 May 2026      | 47.22                 |                     | 2389.20         |               | 0.00                       |                          | 0.00                       |
      | 141       | 22 May 2026      | 47.22                 |                     | 2341.98         |               | 0.00                       |                          | 0.00                       |
      | 142       | 23 May 2026      | 47.22                 |                     | 2294.76         |               | 0.00                       |                          | 0.00                       |
      | 143       | 24 May 2026      | 47.22                 |                     | 2247.54         |               | 0.00                       |                          | 0.00                       |
      | 144       | 25 May 2026      | 47.22                 |                     | 2200.32         |               | 0.00                       |                          | 0.00                       |
      | 145       | 26 May 2026      | 47.22                 |                     | 2153.10         |               | 0.00                       |                          | 0.00                       |
      | 146       | 27 May 2026      | 47.22                 |                     | 2105.88         |               | 0.00                       |                          | 0.00                       |
      | 147       | 28 May 2026      | 47.22                 |                     | 2058.66         |               | 0.00                       |                          | 0.00                       |
      | 148       | 29 May 2026      | 47.22                 |                     | 2011.44         |               | 0.00                       |                          | 0.00                       |
      | 149       | 30 May 2026      | 47.22                 |                     | 1964.22         |               | 0.00                       |                          | 0.00                       |
      | 150       | 31 May 2026      | 47.22                 |                     | 1917.00         |               | 0.00                       |                          | 0.00                       |
      | 151       | 01 June 2026     | 47.22                 |                     | 1869.78         |               | 0.00                       |                          | 0.00                       |
      | 152       | 02 June 2026     | 47.22                 |                     | 1822.56         |               | 0.00                       |                          | 0.00                       |
      | 153       | 03 June 2026     | 47.22                 |                     | 1775.34         |               | 0.00                       |                          | 0.00                       |
      | 154       | 04 June 2026     | 47.22                 |                     | 1728.12         |               | 0.00                       |                          | 0.00                       |
      | 155       | 05 June 2026     | 47.22                 |                     | 1680.90         |               | 0.00                       |                          | 0.00                       |
      | 156       | 06 June 2026     | 47.22                 |                     | 1633.68         |               | 0.00                       |                          | 0.00                       |
      | 157       | 07 June 2026     | 47.22                 |                     | 1586.46         |               | 0.00                       |                          | 0.00                       |
      | 158       | 08 June 2026     | 47.22                 |                     | 1539.24         |               | 0.00                       |                          | 0.00                       |
      | 159       | 09 June 2026     | 47.22                 |                     | 1492.02         |               | 0.00                       |                          | 0.00                       |
      | 160       | 10 June 2026     | 47.22                 |                     | 1444.80         |               | 0.00                       |                          | 0.00                       |
      | 161       | 11 June 2026     | 47.22                 |                     | 1397.58         |               | 0.00                       |                          | 0.00                       |
      | 162       | 12 June 2026     | 47.22                 |                     | 1350.36         |               | 0.00                       |                          | 0.00                       |
      | 163       | 13 June 2026     | 47.22                 |                     | 1303.14         |               | 0.00                       |                          | 0.00                       |
      | 164       | 14 June 2026     | 47.22                 |                     | 1255.92         |               | 0.00                       |                          | 0.00                       |
      | 165       | 15 June 2026     | 47.22                 |                     | 1208.70         |               | 0.00                       |                          | 0.00                       |
      | 166       | 16 June 2026     | 47.22                 |                     | 1161.48         |               | 0.00                       |                          | 0.00                       |
      | 167       | 17 June 2026     | 47.22                 |                     | 1114.26         |               | 0.00                       |                          | 0.00                       |
      | 168       | 18 June 2026     | 47.22                 |                     | 1067.04         |               | 0.00                       |                          | 0.00                       |
      | 169       | 19 June 2026     | 47.22                 |                     | 1019.82         |               | 0.00                       |                          | 0.00                       |
      | 170       | 20 June 2026     | 47.22                 |                     | 972.60          |               | 0.00                       |                          | 0.00                       |
      | 171       | 21 June 2026     | 47.22                 |                     | 925.38          |               | 0.00                       |                          | 0.00                       |
      | 172       | 22 June 2026     | 47.22                 |                     | 878.16          |               | 0.00                       |                          | 0.00                       |
      | 173       | 23 June 2026     | 47.22                 |                     | 830.94          |               | 0.00                       |                          | 0.00                       |
      | 174       | 24 June 2026     | 47.22                 |                     | 783.72          |               | 0.00                       |                          | 0.00                       |
      | 175       | 25 June 2026     | 47.22                 |                     | 736.50          |               | 0.00                       |                          | 0.00                       |
      | 176       | 26 June 2026     | 47.22                 |                     | 689.28          |               | 0.00                       |                          | 0.00                       |
      | 177       | 27 June 2026     | 47.22                 |                     | 642.06          |               | 0.00                       |                          | 0.00                       |
      | 178       | 28 June 2026     | 47.22                 |                     | 594.84          |               | 0.00                       |                          | 0.00                       |
      | 179       | 29 June 2026     | 47.22                 |                     | 547.62          |               | 0.00                       |                          | 0.00                       |
      | 180       | 30 June 2026     | 47.22                 |                     | 500.40          |               | 0.00                       |                          | 0.00                       |
      | 181       | 01 July 2026     | 47.22                 |                     | 453.18          |               | 0.00                       |                          | 0.00                       |
      | 182       | 02 July 2026     | 47.22                 |                     | 405.96          |               | 0.00                       |                          | 0.00                       |
      | 183       | 03 July 2026     | 47.22                 |                     | 358.74          |               | 0.00                       |                          | 0.00                       |
      | 184       | 04 July 2026     | 47.22                 |                     | 311.52          |               | 0.00                       |                          | 0.00                       |
      | 185       | 05 July 2026     | 47.22                 |                     | 264.30          |               | 0.00                       |                          | 0.00                       |
      | 186       | 06 July 2026     | 47.22                 |                     | 217.08          |               | 0.00                       |                          | 0.00                       |
      | 187       | 07 July 2026     | 47.22                 |                     | 169.86          |               | 0.00                       |                          | 0.00                       |
      | 188       | 08 July 2026     | 47.22                 |                     | 122.64          |               | 0.00                       |                          | 0.00                       |
      | 189       | 09 July 2026     | 47.22                 |                     | 75.42           |               | 0.00                       |                          | 0.00                       |
      | 190       | 10 July 2026     | 47.22                 |                     | 28.20           |               | 0.00                       |                          | 0.00                       |
      | 191       | 11 July 2026     | 28.20                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 192       | 12 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 193       | 13 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 194       | 14 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 195       | 15 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 196       | 16 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 197       | 17 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 198       | 18 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 199       | 19 July 2026     | 47.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
      | 200       | 20 July 2026     | 17.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"
