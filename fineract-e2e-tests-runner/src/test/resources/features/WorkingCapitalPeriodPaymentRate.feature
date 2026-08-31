@WorkingCapital
@WorkingCapitalPeriodPaymentRateFeature
Feature: Working Capital Period Payment Rate

  Background:
    Given Global configuration "enable-business-date" is enabled

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
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "11" value
#--- Snapshot taken here, once those nine days have gone by: elapsing is itself what settles a period, so the earlier
#--- rows legitimately move as the calendar advances. What must not move them is the reprocessing below.
    And Admin retrieves the projected amortization schedule
    And Admin remembers the retrieved amortization schedule payments before "2026-01-10"
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
#--- first change, effective today. Nineteen days went by unpaid, so the projection made no progress across
#--- them: every elapsed period bills its 50.00 against the balance still owed and lands back on 8959.61.
#--- Only from the change onwards does the schedule move again, at the raised rate. ---#
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "20" value
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 18        | 2026-01-19 | 50.00                 | 8959.61         | 9.61                       | 990.39                     |
      | 19        | 2026-01-20 | 55.56                 | 8955.11         | 10.67                      | 989.33                     |
      | 20        | 2026-01-21 | 55.56                 | 8910.17         | 10.62                      | 978.71                     |
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
      | 8         | 2026-01-09 | 50.00                 | 8959.61         | 9.61                       | 990.39                     |
      | 9         | 2026-01-10 | 30.56                 | 8975.32         | 5.88                       | 994.12                     |
      | 10        | 2026-01-11 | 30.56                 | 8975.32         | 5.88                       | 994.12                     |
      | 18        | 2026-01-19 | 30.56                 | 8975.32         | 5.88                       | 994.12                     |
      | 19        | 2026-01-20 | 55.56                 | 8955.11         | 10.67                      | 989.33                     |
      | 20        | 2026-01-21 | 55.56                 | 8910.17         | 10.62                      | 978.71                     |
      | 197       | 2026-07-17 | 55.56                 | 54.70           | 0.13                       | 0.10                       |
      | 198       | 2026-07-18 | 54.76                 | 0.00            | 0.10                       | 0.00                       |
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
#--- before anything has elapsed: a flat schedule at the original 18 across the effective date ---#
    And Admin retrieves the projected amortization schedule
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
#--- what the periods before the change look like with those days gone by, and before the change is made ---#
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 5         | 2026-01-06 | 50.00                 | 8553.33         | 9.18                       | 896.66                     |
      | 8         | 2026-01-09 | 50.00                 | 8553.33         | 9.18                       | 896.66                     |
    And Admin update Working Capital period payment rate with "11" value effective from "10 January 2026"
    And Admin retrieves the projected amortization schedule
# The change does not re-rate what came before it: periods 5 and 8 still bill 50.00, and their balance is where it was
# before the change was made. Their deferred fee shifts by a couple of cents, and legitimately so - the 05 January
# repayment reaches past 10 January, so re-rating the instalments it covers changes a little of how much fee that
# repayment earned. This is the same behaviour a backdated interest change has on a cumulative or progressive loan,
# where it alters the principal and interest split of the repayments that follow it. What it does not do is move those
# earlier periods wholesale: a repayment earns the fee of the periods it actually covers, priced at what each of those
# periods bills, so re-rating later periods cannot re-price the ones already paid for.
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 5         | 2026-01-06 | 50.00                 | 8553.54         | 9.18                       | 896.46                     |
      | 8         | 2026-01-09 | 50.00                 | 8553.54         | 9.18                       | 896.46                     |
      | 9         | 2026-01-10 | 30.56                 | 8550.84         | 5.73                       | 899.91                     |
      | 10        | 2026-01-11 | 30.56                 | 8525.99         | 5.71                       | 894.20                     |
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
#--- before the change: a flat schedule at the original 18 across the future effective date ---#
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 30        | 2026-01-31 | 50.00                 | 7769.36         | 8.34                       | 730.65                     |
      | 31        | 2026-02-01 | 50.00                 | 7727.66         | 8.30                       | 722.35                     |
      | 32        | 2026-02-02 | 50.00                 | 7685.91         | 8.25                       | 714.10                     |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
#--- Snapshot taken once those days have gone by: elapsing is itself what settles a period, so the earlier
#--- rows legitimately restate as the calendar advances. What must not move them is the change below.
    And Admin retrieves the projected amortization schedule
    And Admin remembers the retrieved amortization schedule payments before "2026-02-01"
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
      | 29        | 2026-01-30 | 50.00                 | 8142.70         | 8.74                       | 807.31                     |
      | 30        | 2026-01-31 | 50.00                 | 8101.39         | 8.69                       | 798.62                     |
      | 31        | 2026-02-01 | 30.56                 | 8076.13         | 5.30                       | 793.32                     |
      | 32        | 2026-02-02 | 30.56                 | 8050.85         | 5.28                       | 788.04                     |
      | 308       | 2026-11-05 | 30.56                 | 402.45          | 0.28                       | 1.88                       |
      | 309       | 2026-11-06 | 30.56                 | 372.16          | 0.26                       | 1.62                       |
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
      | 8         | 2026-01-09 | 50.00                 | 8959.61         | 9.61                       | 990.39                     |
      | 9         | 2026-01-10 | 30.56                 | 8975.32         | 5.88                       | 994.12                     |
      | 19        | 2026-01-20 | 55.56                 | 8955.11         | 10.67                      | 989.33                     |
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
      | 8         | 2026-01-09 | 50.00                 | 8959.61         | 9.61                       | 990.39                     |
      | 9         | 2026-01-10 | 47.22                 | 8961.86         | 9.08                       | 990.92                     |
      | 19        | 2026-01-20 | 55.56                 | 8955.11         | 10.67                      | 989.33                     |
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
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
#--- Snapshot taken once those days have gone by: elapsing is itself what settles a period, so the earlier
#--- rows legitimately restate as the calendar advances. What must not move them is the change below.
    And Admin retrieves the projected amortization schedule
    And Admin remembers the retrieved amortization schedule payments before "2026-01-10"
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
      | 8         | 2026-01-09 | 50.00                 | 8959.61         | 9.61                       | 990.39                     |
      | 9         | 2026-01-10 | 36.11                 | 8970.84         | 6.95                       | 993.05                     |
      | 13        | 2026-01-14 | 36.11                 | 8970.84         | 6.95                       | 993.05                     |
      | 14        | 2026-01-15 | 52.78                 | 8957.36         | 10.14                      | 989.86                     |
      | 18        | 2026-01-19 | 52.78                 | 8957.36         | 10.14                      | 989.86                     |
      | 19        | 2026-01-20 | 55.56                 | 8955.11         | 10.67                      | 989.33                     |
      | 88        | 2026-03-30 | 55.56                 | 5725.86         | 6.85                       | 384.94                     |
      | 89        | 2026-03-31 | 69.44                 | 5664.89         | 8.47                       | 376.47                     |
      | 164       | 2026-06-14 | 69.44                 | 825.40          | 1.32                       | 7.96                       |
    Then Admin closes the Working Capital loan with a full repayment on "20 January 2026"

  @TestRailId:C93989
  Scenario: Verify backdated and then future-dated period payment rate changes are both accepted on an overdue loan - UC15
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
#--- Every missed instalment pushes the last period out by a day, so a loan this far behind still matures well in the
#--- future and both changes have periods left to re-rate.
    And Admin update Working Capital period payment rate with "19" value effective from "10 August 2026"
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 01 August 2026  | 17.0          | 15.0     | false    |
      | 10 August 2026  | 15.0          | 19.0     | false    |
#--- On 06 August the 01 August change is in force at 15; the 10 August one is recorded but still in the future.
    And Working Capital Loan period payment rate in effect is "15"
    And Admin retrieves the projected amortization schedule
# amortization schedule contains diff values based omn period payment rate start date
# The periods that elapsed unpaid are zero-filled rather than dropped, so the term stretches past the 368 it ran to
# before and the balance closes on period 388 instead.
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 1         | 2026-01-02 | 47.22                 | 8952.91         | 0.13                       | 11.87                      |
      | 221       | 2026-08-10 | 52.78                 | 8781.12         | 0.14                       | 11.42                      |
      | 388       | 2027-01-24 | 31.06                 | 0.00            | 0.00                       | 0.00                       |
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
      | 18        | 2026-01-19 | 50.00                 | 8959.61         | 9.61                       | 990.39                     |
      | 19        | 2026-01-20 | 55.56                 | 8955.11         | 10.67                      | 989.33                     |
      | 20        | 2026-01-21 | 55.56                 | 8910.17         | 10.62                      | 978.71                     |
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
      | 18        | 2026-01-19 | 50.00                 | 8959.61         | 9.61                       | 990.39                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 19        | 2026-01-20 | 55.56                 | 8955.11         | 10.67                      | 989.33                     | 100.00              | 8919.18       | 19.18                    | 980.82                   |
      | 20        | 2026-01-21 | 55.56                 | 8874.20         | 10.58                      | 970.24                     |                     |               |                          |                          |
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
      | 8         | 2026-01-09 | 50.00                 | 8959.61         | 9.61                       | 990.39                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 9         | 2026-01-10 | 30.56                 | 8975.32         | 5.88                       | 994.12                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 10        | 2026-01-11 | 30.56                 | 8975.32         | 5.88                       | 994.12                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 18        | 2026-01-19 | 30.56                 | 8975.32         | 5.88                       | 994.12                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 19        | 2026-01-20 | 55.56                 | 8955.11         | 10.67                      | 989.33                     | 100.00              | 8919.18       | 19.18                    | 980.82                   |
      | 20        | 2026-01-21 | 55.56                 | 8874.20         | 10.58                      | 970.24                     |                     |               |                          |                          |
      | 185       | 2026-07-05 | 55.56                 | 671.78          | 0.86                       | 5.25                       |                     |               |                          |                          |
      | 186       | 2026-07-06 | 55.56                 | 617.02          | 0.80                       | 4.45                       |                     |               |                          |                          |
      | 198       | 2026-07-18 | 10.33                 | 0.00            | 0.00                       | 0.00                       |                     |               |                          |                          |
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
#--- before the change: a flat schedule at the original 18 across the effective date ---#
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 8         | 2026-01-09 | 50.00                 | 8600.00         | 0.00                       | 0.00                       |
      | 9         | 2026-01-10 | 50.00                 | 8550.00         | 0.00                       | 0.00                       |
      | 10        | 2026-01-11 | 50.00                 | 8500.00         | 0.00                       | 0.00                       |
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 35.0 transaction amount
#--- a repayment lands before the change, then the change is backdated to a date after it ---#
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
#--- Snapshot taken once those days have gone by: elapsing is itself what settles a period, so the earlier
#--- rows legitimately restate as the calendar advances. What must not move them is the change below.
    And Admin retrieves the projected amortization schedule
    And Admin remembers the retrieved amortization schedule payments before "2026-01-10"
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
      | 8         | 2026-01-09 | 50.00                 | 8950.00         | 0.00                       | 0.00                       | 0.00                | 9000.00       | 0.00                     | 0.00                     |
      | 9         | 2026-01-10 | 50.00                 | 8950.00         | 0.00                       | 0.00                       | 465.00              | 8535.00       | 0.00                     | 0.00                     |
      | 10        | 2026-01-11 | 50.00                 | 8485.00         | 0.00                       | 0.00                       | 0.00                | 8535.00       | 0.00                     | 0.00                     |
      | 18        | 2026-01-19 | 50.00                 | 8485.00         | 0.00                       | 0.00                       | 0.00                | 8535.00       | 0.00                     | 0.00                     |
      | 19        | 2026-01-20 | 41.67                 | 8958.33         | 0.00                       | 0.00                       |                     |               |                          |                          |
      | 20        | 2026-01-21 | 41.67                 | 8916.66         | 0.00                       | 0.00                       |                     |               |                          |                          |
      | 225       | 2026-08-14 | 41.67                 | 374.31          | 0.00                       | 0.00                       |                     |               |                          |                          |
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
      | 8         | 2026-01-09 | 50.00                 | 8959.61         | 9.61                       | 990.39                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 9         | 2026-01-10 | 58.33                 | 8952.87         | 11.20                      | 988.80                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 14        | 2026-01-15 | 58.33                 | 8952.87         | 11.20                      | 988.80                     | 100.00              | 8919.18       | 19.18                    | 980.82                   |
      | 15        | 2026-01-16 | 58.33                 | 8871.95         | 11.10                      | 969.72                     | 0.00                | 8919.18       | 0.00                     | 980.82                   |
      | 18        | 2026-01-19 | 58.33                 | 8871.95         | 11.10                      | 969.72                     | 0.00                | 8919.18       | 0.00                     | 980.82                   |
      | 19        | 2026-01-20 | 58.33                 | 8871.95         | 11.10                      | 969.72                     |                     |               |                          |                          |
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
      | 20 January 2026  | Discount Fee Amortization | 19.18             |                  |                   |                       | false    |
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance | actualAmortizationAmount | actualDiscountFeeBalance |
      | 8         | 2026-01-09 | 50.00                 | 8959.61         | 9.61                       | 990.39                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 9         | 2026-01-10 | 58.33                 | 8952.87         | 11.20                      | 988.80                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 14        | 2026-01-15 | 58.33                 | 8952.87         | 11.20                      | 988.80                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 15        | 2026-01-16 | 58.33                 | 8952.87         | 11.20                      | 988.80                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 18        | 2026-01-19 | 58.33                 | 8952.87         | 11.20                      | 988.80                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
      | 19        | 2026-01-20 | 58.33                 | 8952.87         | 11.20                      | 988.80                     | 0.00                | 9000.00       | 0.00                     | 1000.00                  |
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
      | 2         | 03 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 3         | 04 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 4         | 05 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 5         | 06 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 6         | 07 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 7         | 08 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 8         | 09 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 9         | 10 January 2026  | 50.00                 | 30.00               | 8950.00         | 8970.00       | 0.00                       | 0.00                     | 0.00                       |
      | 10        | 11 January 2026  | 50.00                 |                     | 8920.00         |               | 0.00                       |                          | 0.00                       |
      | 11        | 12 January 2026  | 50.00                 |                     | 8870.00         |               | 0.00                       |                          | 0.00                       |
      | 12        | 13 January 2026  | 50.00                 |                     | 8820.00         |               | 0.00                       |                          | 0.00                       |
      | 13        | 14 January 2026  | 50.00                 |                     | 8770.00         |               | 0.00                       |                          | 0.00                       |
      | 14        | 15 January 2026  | 50.00                 |                     | 8720.00         |               | 0.00                       |                          | 0.00                       |
      | 15        | 16 January 2026  | 50.00                 |                     | 8670.00         |               | 0.00                       |                          | 0.00                       |
      | 16        | 17 January 2026  | 50.00                 |                     | 8620.00         |               | 0.00                       |                          | 0.00                       |
      | 17        | 18 January 2026  | 50.00                 |                     | 8570.00         |               | 0.00                       |                          | 0.00                       |
      | 18        | 19 January 2026  | 50.00                 |                     | 8520.00         |               | 0.00                       |                          | 0.00                       |
      | 19        | 20 January 2026  | 50.00                 |                     | 8470.00         |               | 0.00                       |                          | 0.00                       |
      | 20        | 21 January 2026  | 50.00                 |                     | 8420.00         |               | 0.00                       |                          | 0.00                       |
      | 21        | 22 January 2026  | 50.00                 |                     | 8370.00         |               | 0.00                       |                          | 0.00                       |
      | 22        | 23 January 2026  | 50.00                 |                     | 8320.00         |               | 0.00                       |                          | 0.00                       |
      | 23        | 24 January 2026  | 50.00                 |                     | 8270.00         |               | 0.00                       |                          | 0.00                       |
      | 24        | 25 January 2026  | 50.00                 |                     | 8220.00         |               | 0.00                       |                          | 0.00                       |
      | 25        | 26 January 2026  | 50.00                 |                     | 8170.00         |               | 0.00                       |                          | 0.00                       |
      | 26        | 27 January 2026  | 50.00                 |                     | 8120.00         |               | 0.00                       |                          | 0.00                       |
      | 27        | 28 January 2026  | 50.00                 |                     | 8070.00         |               | 0.00                       |                          | 0.00                       |
      | 28        | 29 January 2026  | 50.00                 |                     | 8020.00         |               | 0.00                       |                          | 0.00                       |
      | 29        | 30 January 2026  | 50.00                 |                     | 7970.00         |               | 0.00                       |                          | 0.00                       |
      | 30        | 31 January 2026  | 50.00                 |                     | 7920.00         |               | 0.00                       |                          | 0.00                       |
      | 31        | 01 February 2026 | 50.00                 |                     | 7870.00         |               | 0.00                       |                          | 0.00                       |
      | 32        | 02 February 2026 | 50.00                 |                     | 7820.00         |               | 0.00                       |                          | 0.00                       |
      | 33        | 03 February 2026 | 50.00                 |                     | 7770.00         |               | 0.00                       |                          | 0.00                       |
      | 34        | 04 February 2026 | 50.00                 |                     | 7720.00         |               | 0.00                       |                          | 0.00                       |
      | 35        | 05 February 2026 | 50.00                 |                     | 7670.00         |               | 0.00                       |                          | 0.00                       |
      | 36        | 06 February 2026 | 50.00                 |                     | 7620.00         |               | 0.00                       |                          | 0.00                       |
      | 37        | 07 February 2026 | 50.00                 |                     | 7570.00         |               | 0.00                       |                          | 0.00                       |
      | 38        | 08 February 2026 | 50.00                 |                     | 7520.00         |               | 0.00                       |                          | 0.00                       |
      | 39        | 09 February 2026 | 50.00                 |                     | 7470.00         |               | 0.00                       |                          | 0.00                       |
      | 40        | 10 February 2026 | 50.00                 |                     | 7420.00         |               | 0.00                       |                          | 0.00                       |
      | 41        | 11 February 2026 | 50.00                 |                     | 7370.00         |               | 0.00                       |                          | 0.00                       |
      | 42        | 12 February 2026 | 50.00                 |                     | 7320.00         |               | 0.00                       |                          | 0.00                       |
      | 43        | 13 February 2026 | 50.00                 |                     | 7270.00         |               | 0.00                       |                          | 0.00                       |
      | 44        | 14 February 2026 | 50.00                 |                     | 7220.00         |               | 0.00                       |                          | 0.00                       |
      | 45        | 15 February 2026 | 50.00                 |                     | 7170.00         |               | 0.00                       |                          | 0.00                       |
      | 46        | 16 February 2026 | 50.00                 |                     | 7120.00         |               | 0.00                       |                          | 0.00                       |
      | 47        | 17 February 2026 | 50.00                 |                     | 7070.00         |               | 0.00                       |                          | 0.00                       |
      | 48        | 18 February 2026 | 50.00                 |                     | 7020.00         |               | 0.00                       |                          | 0.00                       |
      | 49        | 19 February 2026 | 50.00                 |                     | 6970.00         |               | 0.00                       |                          | 0.00                       |
      | 50        | 20 February 2026 | 50.00                 |                     | 6920.00         |               | 0.00                       |                          | 0.00                       |
      | 51        | 21 February 2026 | 50.00                 |                     | 6870.00         |               | 0.00                       |                          | 0.00                       |
      | 52        | 22 February 2026 | 50.00                 |                     | 6820.00         |               | 0.00                       |                          | 0.00                       |
      | 53        | 23 February 2026 | 50.00                 |                     | 6770.00         |               | 0.00                       |                          | 0.00                       |
      | 54        | 24 February 2026 | 50.00                 |                     | 6720.00         |               | 0.00                       |                          | 0.00                       |
      | 55        | 25 February 2026 | 50.00                 |                     | 6670.00         |               | 0.00                       |                          | 0.00                       |
      | 56        | 26 February 2026 | 50.00                 |                     | 6620.00         |               | 0.00                       |                          | 0.00                       |
      | 57        | 27 February 2026 | 50.00                 |                     | 6570.00         |               | 0.00                       |                          | 0.00                       |
      | 58        | 28 February 2026 | 50.00                 |                     | 6520.00         |               | 0.00                       |                          | 0.00                       |
      | 59        | 01 March 2026    | 50.00                 |                     | 6470.00         |               | 0.00                       |                          | 0.00                       |
      | 60        | 02 March 2026    | 50.00                 |                     | 6420.00         |               | 0.00                       |                          | 0.00                       |
      | 61        | 03 March 2026    | 50.00                 |                     | 6370.00         |               | 0.00                       |                          | 0.00                       |
      | 62        | 04 March 2026    | 50.00                 |                     | 6320.00         |               | 0.00                       |                          | 0.00                       |
      | 63        | 05 March 2026    | 50.00                 |                     | 6270.00         |               | 0.00                       |                          | 0.00                       |
      | 64        | 06 March 2026    | 50.00                 |                     | 6220.00         |               | 0.00                       |                          | 0.00                       |
      | 65        | 07 March 2026    | 50.00                 |                     | 6170.00         |               | 0.00                       |                          | 0.00                       |
      | 66        | 08 March 2026    | 50.00                 |                     | 6120.00         |               | 0.00                       |                          | 0.00                       |
      | 67        | 09 March 2026    | 50.00                 |                     | 6070.00         |               | 0.00                       |                          | 0.00                       |
      | 68        | 10 March 2026    | 50.00                 |                     | 6020.00         |               | 0.00                       |                          | 0.00                       |
      | 69        | 11 March 2026    | 50.00                 |                     | 5970.00         |               | 0.00                       |                          | 0.00                       |
      | 70        | 12 March 2026    | 50.00                 |                     | 5920.00         |               | 0.00                       |                          | 0.00                       |
      | 71        | 13 March 2026    | 50.00                 |                     | 5870.00         |               | 0.00                       |                          | 0.00                       |
      | 72        | 14 March 2026    | 50.00                 |                     | 5820.00         |               | 0.00                       |                          | 0.00                       |
      | 73        | 15 March 2026    | 50.00                 |                     | 5770.00         |               | 0.00                       |                          | 0.00                       |
      | 74        | 16 March 2026    | 50.00                 |                     | 5720.00         |               | 0.00                       |                          | 0.00                       |
      | 75        | 17 March 2026    | 50.00                 |                     | 5670.00         |               | 0.00                       |                          | 0.00                       |
      | 76        | 18 March 2026    | 50.00                 |                     | 5620.00         |               | 0.00                       |                          | 0.00                       |
      | 77        | 19 March 2026    | 50.00                 |                     | 5570.00         |               | 0.00                       |                          | 0.00                       |
      | 78        | 20 March 2026    | 50.00                 |                     | 5520.00         |               | 0.00                       |                          | 0.00                       |
      | 79        | 21 March 2026    | 50.00                 |                     | 5470.00         |               | 0.00                       |                          | 0.00                       |
      | 80        | 22 March 2026    | 50.00                 |                     | 5420.00         |               | 0.00                       |                          | 0.00                       |
      | 81        | 23 March 2026    | 50.00                 |                     | 5370.00         |               | 0.00                       |                          | 0.00                       |
      | 82        | 24 March 2026    | 50.00                 |                     | 5320.00         |               | 0.00                       |                          | 0.00                       |
      | 83        | 25 March 2026    | 50.00                 |                     | 5270.00         |               | 0.00                       |                          | 0.00                       |
      | 84        | 26 March 2026    | 50.00                 |                     | 5220.00         |               | 0.00                       |                          | 0.00                       |
      | 85        | 27 March 2026    | 50.00                 |                     | 5170.00         |               | 0.00                       |                          | 0.00                       |
      | 86        | 28 March 2026    | 50.00                 |                     | 5120.00         |               | 0.00                       |                          | 0.00                       |
      | 87        | 29 March 2026    | 50.00                 |                     | 5070.00         |               | 0.00                       |                          | 0.00                       |
      | 88        | 30 March 2026    | 50.00                 |                     | 5020.00         |               | 0.00                       |                          | 0.00                       |
      | 89        | 31 March 2026    | 50.00                 |                     | 4970.00         |               | 0.00                       |                          | 0.00                       |
      | 90        | 01 April 2026    | 50.00                 |                     | 4920.00         |               | 0.00                       |                          | 0.00                       |
      | 91        | 02 April 2026    | 50.00                 |                     | 4870.00         |               | 0.00                       |                          | 0.00                       |
      | 92        | 03 April 2026    | 50.00                 |                     | 4820.00         |               | 0.00                       |                          | 0.00                       |
      | 93        | 04 April 2026    | 50.00                 |                     | 4770.00         |               | 0.00                       |                          | 0.00                       |
      | 94        | 05 April 2026    | 50.00                 |                     | 4720.00         |               | 0.00                       |                          | 0.00                       |
      | 95        | 06 April 2026    | 50.00                 |                     | 4670.00         |               | 0.00                       |                          | 0.00                       |
      | 96        | 07 April 2026    | 50.00                 |                     | 4620.00         |               | 0.00                       |                          | 0.00                       |
      | 97        | 08 April 2026    | 50.00                 |                     | 4570.00         |               | 0.00                       |                          | 0.00                       |
      | 98        | 09 April 2026    | 50.00                 |                     | 4520.00         |               | 0.00                       |                          | 0.00                       |
      | 99        | 10 April 2026    | 50.00                 |                     | 4470.00         |               | 0.00                       |                          | 0.00                       |
      | 100       | 11 April 2026    | 50.00                 |                     | 4420.00         |               | 0.00                       |                          | 0.00                       |
      | 101       | 12 April 2026    | 50.00                 |                     | 4370.00         |               | 0.00                       |                          | 0.00                       |
      | 102       | 13 April 2026    | 50.00                 |                     | 4320.00         |               | 0.00                       |                          | 0.00                       |
      | 103       | 14 April 2026    | 50.00                 |                     | 4270.00         |               | 0.00                       |                          | 0.00                       |
      | 104       | 15 April 2026    | 50.00                 |                     | 4220.00         |               | 0.00                       |                          | 0.00                       |
      | 105       | 16 April 2026    | 50.00                 |                     | 4170.00         |               | 0.00                       |                          | 0.00                       |
      | 106       | 17 April 2026    | 50.00                 |                     | 4120.00         |               | 0.00                       |                          | 0.00                       |
      | 107       | 18 April 2026    | 50.00                 |                     | 4070.00         |               | 0.00                       |                          | 0.00                       |
      | 108       | 19 April 2026    | 50.00                 |                     | 4020.00         |               | 0.00                       |                          | 0.00                       |
      | 109       | 20 April 2026    | 50.00                 |                     | 3970.00         |               | 0.00                       |                          | 0.00                       |
      | 110       | 21 April 2026    | 50.00                 |                     | 3920.00         |               | 0.00                       |                          | 0.00                       |
      | 111       | 22 April 2026    | 50.00                 |                     | 3870.00         |               | 0.00                       |                          | 0.00                       |
      | 112       | 23 April 2026    | 50.00                 |                     | 3820.00         |               | 0.00                       |                          | 0.00                       |
      | 113       | 24 April 2026    | 50.00                 |                     | 3770.00         |               | 0.00                       |                          | 0.00                       |
      | 114       | 25 April 2026    | 50.00                 |                     | 3720.00         |               | 0.00                       |                          | 0.00                       |
      | 115       | 26 April 2026    | 50.00                 |                     | 3670.00         |               | 0.00                       |                          | 0.00                       |
      | 116       | 27 April 2026    | 50.00                 |                     | 3620.00         |               | 0.00                       |                          | 0.00                       |
      | 117       | 28 April 2026    | 50.00                 |                     | 3570.00         |               | 0.00                       |                          | 0.00                       |
      | 118       | 29 April 2026    | 50.00                 |                     | 3520.00         |               | 0.00                       |                          | 0.00                       |
      | 119       | 30 April 2026    | 50.00                 |                     | 3470.00         |               | 0.00                       |                          | 0.00                       |
      | 120       | 01 May 2026      | 50.00                 |                     | 3420.00         |               | 0.00                       |                          | 0.00                       |
      | 121       | 02 May 2026      | 50.00                 |                     | 3370.00         |               | 0.00                       |                          | 0.00                       |
      | 122       | 03 May 2026      | 50.00                 |                     | 3320.00         |               | 0.00                       |                          | 0.00                       |
      | 123       | 04 May 2026      | 50.00                 |                     | 3270.00         |               | 0.00                       |                          | 0.00                       |
      | 124       | 05 May 2026      | 50.00                 |                     | 3220.00         |               | 0.00                       |                          | 0.00                       |
      | 125       | 06 May 2026      | 50.00                 |                     | 3170.00         |               | 0.00                       |                          | 0.00                       |
      | 126       | 07 May 2026      | 50.00                 |                     | 3120.00         |               | 0.00                       |                          | 0.00                       |
      | 127       | 08 May 2026      | 50.00                 |                     | 3070.00         |               | 0.00                       |                          | 0.00                       |
      | 128       | 09 May 2026      | 50.00                 |                     | 3020.00         |               | 0.00                       |                          | 0.00                       |
      | 129       | 10 May 2026      | 50.00                 |                     | 2970.00         |               | 0.00                       |                          | 0.00                       |
      | 130       | 11 May 2026      | 50.00                 |                     | 2920.00         |               | 0.00                       |                          | 0.00                       |
      | 131       | 12 May 2026      | 50.00                 |                     | 2870.00         |               | 0.00                       |                          | 0.00                       |
      | 132       | 13 May 2026      | 50.00                 |                     | 2820.00         |               | 0.00                       |                          | 0.00                       |
      | 133       | 14 May 2026      | 50.00                 |                     | 2770.00         |               | 0.00                       |                          | 0.00                       |
      | 134       | 15 May 2026      | 50.00                 |                     | 2720.00         |               | 0.00                       |                          | 0.00                       |
      | 135       | 16 May 2026      | 50.00                 |                     | 2670.00         |               | 0.00                       |                          | 0.00                       |
      | 136       | 17 May 2026      | 50.00                 |                     | 2620.00         |               | 0.00                       |                          | 0.00                       |
      | 137       | 18 May 2026      | 50.00                 |                     | 2570.00         |               | 0.00                       |                          | 0.00                       |
      | 138       | 19 May 2026      | 50.00                 |                     | 2520.00         |               | 0.00                       |                          | 0.00                       |
      | 139       | 20 May 2026      | 50.00                 |                     | 2470.00         |               | 0.00                       |                          | 0.00                       |
      | 140       | 21 May 2026      | 50.00                 |                     | 2420.00         |               | 0.00                       |                          | 0.00                       |
      | 141       | 22 May 2026      | 50.00                 |                     | 2370.00         |               | 0.00                       |                          | 0.00                       |
      | 142       | 23 May 2026      | 50.00                 |                     | 2320.00         |               | 0.00                       |                          | 0.00                       |
      | 143       | 24 May 2026      | 50.00                 |                     | 2270.00         |               | 0.00                       |                          | 0.00                       |
      | 144       | 25 May 2026      | 50.00                 |                     | 2220.00         |               | 0.00                       |                          | 0.00                       |
      | 145       | 26 May 2026      | 50.00                 |                     | 2170.00         |               | 0.00                       |                          | 0.00                       |
      | 146       | 27 May 2026      | 50.00                 |                     | 2120.00         |               | 0.00                       |                          | 0.00                       |
      | 147       | 28 May 2026      | 50.00                 |                     | 2070.00         |               | 0.00                       |                          | 0.00                       |
      | 148       | 29 May 2026      | 50.00                 |                     | 2020.00         |               | 0.00                       |                          | 0.00                       |
      | 149       | 30 May 2026      | 50.00                 |                     | 1970.00         |               | 0.00                       |                          | 0.00                       |
      | 150       | 31 May 2026      | 50.00                 |                     | 1920.00         |               | 0.00                       |                          | 0.00                       |
      | 151       | 01 June 2026     | 50.00                 |                     | 1870.00         |               | 0.00                       |                          | 0.00                       |
      | 152       | 02 June 2026     | 50.00                 |                     | 1820.00         |               | 0.00                       |                          | 0.00                       |
      | 153       | 03 June 2026     | 50.00                 |                     | 1770.00         |               | 0.00                       |                          | 0.00                       |
      | 154       | 04 June 2026     | 50.00                 |                     | 1720.00         |               | 0.00                       |                          | 0.00                       |
      | 155       | 05 June 2026     | 50.00                 |                     | 1670.00         |               | 0.00                       |                          | 0.00                       |
      | 156       | 06 June 2026     | 50.00                 |                     | 1620.00         |               | 0.00                       |                          | 0.00                       |
      | 157       | 07 June 2026     | 50.00                 |                     | 1570.00         |               | 0.00                       |                          | 0.00                       |
      | 158       | 08 June 2026     | 50.00                 |                     | 1520.00         |               | 0.00                       |                          | 0.00                       |
      | 159       | 09 June 2026     | 50.00                 |                     | 1470.00         |               | 0.00                       |                          | 0.00                       |
      | 160       | 10 June 2026     | 50.00                 |                     | 1420.00         |               | 0.00                       |                          | 0.00                       |
      | 161       | 11 June 2026     | 50.00                 |                     | 1370.00         |               | 0.00                       |                          | 0.00                       |
      | 162       | 12 June 2026     | 50.00                 |                     | 1320.00         |               | 0.00                       |                          | 0.00                       |
      | 163       | 13 June 2026     | 50.00                 |                     | 1270.00         |               | 0.00                       |                          | 0.00                       |
      | 164       | 14 June 2026     | 50.00                 |                     | 1220.00         |               | 0.00                       |                          | 0.00                       |
      | 165       | 15 June 2026     | 50.00                 |                     | 1170.00         |               | 0.00                       |                          | 0.00                       |
      | 166       | 16 June 2026     | 50.00                 |                     | 1120.00         |               | 0.00                       |                          | 0.00                       |
      | 167       | 17 June 2026     | 50.00                 |                     | 1070.00         |               | 0.00                       |                          | 0.00                       |
      | 168       | 18 June 2026     | 50.00                 |                     | 1020.00         |               | 0.00                       |                          | 0.00                       |
      | 169       | 19 June 2026     | 50.00                 |                     | 970.00          |               | 0.00                       |                          | 0.00                       |
      | 170       | 20 June 2026     | 50.00                 |                     | 920.00          |               | 0.00                       |                          | 0.00                       |
      | 171       | 21 June 2026     | 50.00                 |                     | 870.00          |               | 0.00                       |                          | 0.00                       |
      | 172       | 22 June 2026     | 50.00                 |                     | 820.00          |               | 0.00                       |                          | 0.00                       |
      | 173       | 23 June 2026     | 50.00                 |                     | 770.00          |               | 0.00                       |                          | 0.00                       |
      | 174       | 24 June 2026     | 50.00                 |                     | 720.00          |               | 0.00                       |                          | 0.00                       |
      | 175       | 25 June 2026     | 50.00                 |                     | 670.00          |               | 0.00                       |                          | 0.00                       |
      | 176       | 26 June 2026     | 50.00                 |                     | 620.00          |               | 0.00                       |                          | 0.00                       |
      | 177       | 27 June 2026     | 50.00                 |                     | 570.00          |               | 0.00                       |                          | 0.00                       |
      | 178       | 28 June 2026     | 50.00                 |                     | 520.00          |               | 0.00                       |                          | 0.00                       |
      | 179       | 29 June 2026     | 50.00                 |                     | 470.00          |               | 0.00                       |                          | 0.00                       |
      | 180       | 30 June 2026     | 50.00                 |                     | 420.00          |               | 0.00                       |                          | 0.00                       |
      | 181       | 01 July 2026     | 50.00                 |                     | 370.00          |               | 0.00                       |                          | 0.00                       |
      | 182       | 02 July 2026     | 50.00                 |                     | 320.00          |               | 0.00                       |                          | 0.00                       |
      | 183       | 03 July 2026     | 50.00                 |                     | 270.00          |               | 0.00                       |                          | 0.00                       |
      | 184       | 04 July 2026     | 50.00                 |                     | 220.00          |               | 0.00                       |                          | 0.00                       |
      | 185       | 05 July 2026     | 50.00                 |                     | 170.00          |               | 0.00                       |                          | 0.00                       |
      | 186       | 06 July 2026     | 50.00                 |                     | 120.00          |               | 0.00                       |                          | 0.00                       |
      | 187       | 07 July 2026     | 50.00                 |                     | 70.00           |               | 0.00                       |                          | 0.00                       |
      | 188       | 08 July 2026     | 50.00                 |                     | 20.00           |               | 0.00                       |                          | 0.00                       |
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
      | 2         | 03 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 3         | 04 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 4         | 05 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 5         | 06 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 6         | 07 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 7         | 08 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 8         | 09 January 2026  | 50.00                 | 0.00                | 8950.00         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 9         | 10 January 2026  | 47.22                 | 30.00               | 8952.78         | 8970.00       | 0.00                       | 0.00                     | 0.00                       |
      | 10        | 11 January 2026  | 47.22                 |                     | 8922.78         |               | 0.00                       |                          | 0.00                       |
      | 11        | 12 January 2026  | 47.22                 |                     | 8875.56         |               | 0.00                       |                          | 0.00                       |
      | 12        | 13 January 2026  | 47.22                 |                     | 8828.34         |               | 0.00                       |                          | 0.00                       |
      | 13        | 14 January 2026  | 47.22                 |                     | 8781.12         |               | 0.00                       |                          | 0.00                       |
      | 14        | 15 January 2026  | 47.22                 |                     | 8733.90         |               | 0.00                       |                          | 0.00                       |
      | 15        | 16 January 2026  | 47.22                 |                     | 8686.68         |               | 0.00                       |                          | 0.00                       |
      | 16        | 17 January 2026  | 47.22                 |                     | 8639.46         |               | 0.00                       |                          | 0.00                       |
      | 17        | 18 January 2026  | 47.22                 |                     | 8592.24         |               | 0.00                       |                          | 0.00                       |
      | 18        | 19 January 2026  | 47.22                 |                     | 8545.02         |               | 0.00                       |                          | 0.00                       |
      | 19        | 20 January 2026  | 47.22                 |                     | 8497.80         |               | 0.00                       |                          | 0.00                       |
      | 20        | 21 January 2026  | 47.22                 |                     | 8450.58         |               | 0.00                       |                          | 0.00                       |
      | 21        | 22 January 2026  | 47.22                 |                     | 8403.36         |               | 0.00                       |                          | 0.00                       |
      | 22        | 23 January 2026  | 47.22                 |                     | 8356.14         |               | 0.00                       |                          | 0.00                       |
      | 23        | 24 January 2026  | 47.22                 |                     | 8308.92         |               | 0.00                       |                          | 0.00                       |
      | 24        | 25 January 2026  | 47.22                 |                     | 8261.70         |               | 0.00                       |                          | 0.00                       |
      | 25        | 26 January 2026  | 47.22                 |                     | 8214.48         |               | 0.00                       |                          | 0.00                       |
      | 26        | 27 January 2026  | 47.22                 |                     | 8167.26         |               | 0.00                       |                          | 0.00                       |
      | 27        | 28 January 2026  | 47.22                 |                     | 8120.04         |               | 0.00                       |                          | 0.00                       |
      | 28        | 29 January 2026  | 47.22                 |                     | 8072.82         |               | 0.00                       |                          | 0.00                       |
      | 29        | 30 January 2026  | 47.22                 |                     | 8025.60         |               | 0.00                       |                          | 0.00                       |
      | 30        | 31 January 2026  | 47.22                 |                     | 7978.38         |               | 0.00                       |                          | 0.00                       |
      | 31        | 01 February 2026 | 47.22                 |                     | 7931.16         |               | 0.00                       |                          | 0.00                       |
      | 32        | 02 February 2026 | 47.22                 |                     | 7883.94         |               | 0.00                       |                          | 0.00                       |
      | 33        | 03 February 2026 | 47.22                 |                     | 7836.72         |               | 0.00                       |                          | 0.00                       |
      | 34        | 04 February 2026 | 47.22                 |                     | 7789.50         |               | 0.00                       |                          | 0.00                       |
      | 35        | 05 February 2026 | 47.22                 |                     | 7742.28         |               | 0.00                       |                          | 0.00                       |
      | 36        | 06 February 2026 | 47.22                 |                     | 7695.06         |               | 0.00                       |                          | 0.00                       |
      | 37        | 07 February 2026 | 47.22                 |                     | 7647.84         |               | 0.00                       |                          | 0.00                       |
      | 38        | 08 February 2026 | 47.22                 |                     | 7600.62         |               | 0.00                       |                          | 0.00                       |
      | 39        | 09 February 2026 | 47.22                 |                     | 7553.40         |               | 0.00                       |                          | 0.00                       |
      | 40        | 10 February 2026 | 47.22                 |                     | 7506.18         |               | 0.00                       |                          | 0.00                       |
      | 41        | 11 February 2026 | 47.22                 |                     | 7458.96         |               | 0.00                       |                          | 0.00                       |
      | 42        | 12 February 2026 | 47.22                 |                     | 7411.74         |               | 0.00                       |                          | 0.00                       |
      | 43        | 13 February 2026 | 47.22                 |                     | 7364.52         |               | 0.00                       |                          | 0.00                       |
      | 44        | 14 February 2026 | 47.22                 |                     | 7317.30         |               | 0.00                       |                          | 0.00                       |
      | 45        | 15 February 2026 | 47.22                 |                     | 7270.08         |               | 0.00                       |                          | 0.00                       |
      | 46        | 16 February 2026 | 47.22                 |                     | 7222.86         |               | 0.00                       |                          | 0.00                       |
      | 47        | 17 February 2026 | 47.22                 |                     | 7175.64         |               | 0.00                       |                          | 0.00                       |
      | 48        | 18 February 2026 | 47.22                 |                     | 7128.42         |               | 0.00                       |                          | 0.00                       |
      | 49        | 19 February 2026 | 47.22                 |                     | 7081.20         |               | 0.00                       |                          | 0.00                       |
      | 50        | 20 February 2026 | 47.22                 |                     | 7033.98         |               | 0.00                       |                          | 0.00                       |
      | 51        | 21 February 2026 | 47.22                 |                     | 6986.76         |               | 0.00                       |                          | 0.00                       |
      | 52        | 22 February 2026 | 47.22                 |                     | 6939.54         |               | 0.00                       |                          | 0.00                       |
      | 53        | 23 February 2026 | 47.22                 |                     | 6892.32         |               | 0.00                       |                          | 0.00                       |
      | 54        | 24 February 2026 | 47.22                 |                     | 6845.10         |               | 0.00                       |                          | 0.00                       |
      | 55        | 25 February 2026 | 47.22                 |                     | 6797.88         |               | 0.00                       |                          | 0.00                       |
      | 56        | 26 February 2026 | 47.22                 |                     | 6750.66         |               | 0.00                       |                          | 0.00                       |
      | 57        | 27 February 2026 | 47.22                 |                     | 6703.44         |               | 0.00                       |                          | 0.00                       |
      | 58        | 28 February 2026 | 47.22                 |                     | 6656.22         |               | 0.00                       |                          | 0.00                       |
      | 59        | 01 March 2026    | 47.22                 |                     | 6609.00         |               | 0.00                       |                          | 0.00                       |
      | 60        | 02 March 2026    | 47.22                 |                     | 6561.78         |               | 0.00                       |                          | 0.00                       |
      | 61        | 03 March 2026    | 47.22                 |                     | 6514.56         |               | 0.00                       |                          | 0.00                       |
      | 62        | 04 March 2026    | 47.22                 |                     | 6467.34         |               | 0.00                       |                          | 0.00                       |
      | 63        | 05 March 2026    | 47.22                 |                     | 6420.12         |               | 0.00                       |                          | 0.00                       |
      | 64        | 06 March 2026    | 47.22                 |                     | 6372.90         |               | 0.00                       |                          | 0.00                       |
      | 65        | 07 March 2026    | 47.22                 |                     | 6325.68         |               | 0.00                       |                          | 0.00                       |
      | 66        | 08 March 2026    | 47.22                 |                     | 6278.46         |               | 0.00                       |                          | 0.00                       |
      | 67        | 09 March 2026    | 47.22                 |                     | 6231.24         |               | 0.00                       |                          | 0.00                       |
      | 68        | 10 March 2026    | 47.22                 |                     | 6184.02         |               | 0.00                       |                          | 0.00                       |
      | 69        | 11 March 2026    | 47.22                 |                     | 6136.80         |               | 0.00                       |                          | 0.00                       |
      | 70        | 12 March 2026    | 47.22                 |                     | 6089.58         |               | 0.00                       |                          | 0.00                       |
      | 71        | 13 March 2026    | 47.22                 |                     | 6042.36         |               | 0.00                       |                          | 0.00                       |
      | 72        | 14 March 2026    | 47.22                 |                     | 5995.14         |               | 0.00                       |                          | 0.00                       |
      | 73        | 15 March 2026    | 47.22                 |                     | 5947.92         |               | 0.00                       |                          | 0.00                       |
      | 74        | 16 March 2026    | 47.22                 |                     | 5900.70         |               | 0.00                       |                          | 0.00                       |
      | 75        | 17 March 2026    | 47.22                 |                     | 5853.48         |               | 0.00                       |                          | 0.00                       |
      | 76        | 18 March 2026    | 47.22                 |                     | 5806.26         |               | 0.00                       |                          | 0.00                       |
      | 77        | 19 March 2026    | 47.22                 |                     | 5759.04         |               | 0.00                       |                          | 0.00                       |
      | 78        | 20 March 2026    | 47.22                 |                     | 5711.82         |               | 0.00                       |                          | 0.00                       |
      | 79        | 21 March 2026    | 47.22                 |                     | 5664.60         |               | 0.00                       |                          | 0.00                       |
      | 80        | 22 March 2026    | 47.22                 |                     | 5617.38         |               | 0.00                       |                          | 0.00                       |
      | 81        | 23 March 2026    | 47.22                 |                     | 5570.16         |               | 0.00                       |                          | 0.00                       |
      | 82        | 24 March 2026    | 47.22                 |                     | 5522.94         |               | 0.00                       |                          | 0.00                       |
      | 83        | 25 March 2026    | 47.22                 |                     | 5475.72         |               | 0.00                       |                          | 0.00                       |
      | 84        | 26 March 2026    | 47.22                 |                     | 5428.50         |               | 0.00                       |                          | 0.00                       |
      | 85        | 27 March 2026    | 47.22                 |                     | 5381.28         |               | 0.00                       |                          | 0.00                       |
      | 86        | 28 March 2026    | 47.22                 |                     | 5334.06         |               | 0.00                       |                          | 0.00                       |
      | 87        | 29 March 2026    | 47.22                 |                     | 5286.84         |               | 0.00                       |                          | 0.00                       |
      | 88        | 30 March 2026    | 47.22                 |                     | 5239.62         |               | 0.00                       |                          | 0.00                       |
      | 89        | 31 March 2026    | 47.22                 |                     | 5192.40         |               | 0.00                       |                          | 0.00                       |
      | 90        | 01 April 2026    | 47.22                 |                     | 5145.18         |               | 0.00                       |                          | 0.00                       |
      | 91        | 02 April 2026    | 47.22                 |                     | 5097.96         |               | 0.00                       |                          | 0.00                       |
      | 92        | 03 April 2026    | 47.22                 |                     | 5050.74         |               | 0.00                       |                          | 0.00                       |
      | 93        | 04 April 2026    | 47.22                 |                     | 5003.52         |               | 0.00                       |                          | 0.00                       |
      | 94        | 05 April 2026    | 47.22                 |                     | 4956.30         |               | 0.00                       |                          | 0.00                       |
      | 95        | 06 April 2026    | 47.22                 |                     | 4909.08         |               | 0.00                       |                          | 0.00                       |
      | 96        | 07 April 2026    | 47.22                 |                     | 4861.86         |               | 0.00                       |                          | 0.00                       |
      | 97        | 08 April 2026    | 47.22                 |                     | 4814.64         |               | 0.00                       |                          | 0.00                       |
      | 98        | 09 April 2026    | 47.22                 |                     | 4767.42         |               | 0.00                       |                          | 0.00                       |
      | 99        | 10 April 2026    | 47.22                 |                     | 4720.20         |               | 0.00                       |                          | 0.00                       |
      | 100       | 11 April 2026    | 47.22                 |                     | 4672.98         |               | 0.00                       |                          | 0.00                       |
      | 101       | 12 April 2026    | 47.22                 |                     | 4625.76         |               | 0.00                       |                          | 0.00                       |
      | 102       | 13 April 2026    | 47.22                 |                     | 4578.54         |               | 0.00                       |                          | 0.00                       |
      | 103       | 14 April 2026    | 47.22                 |                     | 4531.32         |               | 0.00                       |                          | 0.00                       |
      | 104       | 15 April 2026    | 47.22                 |                     | 4484.10         |               | 0.00                       |                          | 0.00                       |
      | 105       | 16 April 2026    | 47.22                 |                     | 4436.88         |               | 0.00                       |                          | 0.00                       |
      | 106       | 17 April 2026    | 47.22                 |                     | 4389.66         |               | 0.00                       |                          | 0.00                       |
      | 107       | 18 April 2026    | 47.22                 |                     | 4342.44         |               | 0.00                       |                          | 0.00                       |
      | 108       | 19 April 2026    | 47.22                 |                     | 4295.22         |               | 0.00                       |                          | 0.00                       |
      | 109       | 20 April 2026    | 47.22                 |                     | 4248.00         |               | 0.00                       |                          | 0.00                       |
      | 110       | 21 April 2026    | 47.22                 |                     | 4200.78         |               | 0.00                       |                          | 0.00                       |
      | 111       | 22 April 2026    | 47.22                 |                     | 4153.56         |               | 0.00                       |                          | 0.00                       |
      | 112       | 23 April 2026    | 47.22                 |                     | 4106.34         |               | 0.00                       |                          | 0.00                       |
      | 113       | 24 April 2026    | 47.22                 |                     | 4059.12         |               | 0.00                       |                          | 0.00                       |
      | 114       | 25 April 2026    | 47.22                 |                     | 4011.90         |               | 0.00                       |                          | 0.00                       |
      | 115       | 26 April 2026    | 47.22                 |                     | 3964.68         |               | 0.00                       |                          | 0.00                       |
      | 116       | 27 April 2026    | 47.22                 |                     | 3917.46         |               | 0.00                       |                          | 0.00                       |
      | 117       | 28 April 2026    | 47.22                 |                     | 3870.24         |               | 0.00                       |                          | 0.00                       |
      | 118       | 29 April 2026    | 47.22                 |                     | 3823.02         |               | 0.00                       |                          | 0.00                       |
      | 119       | 30 April 2026    | 47.22                 |                     | 3775.80         |               | 0.00                       |                          | 0.00                       |
      | 120       | 01 May 2026      | 47.22                 |                     | 3728.58         |               | 0.00                       |                          | 0.00                       |
      | 121       | 02 May 2026      | 47.22                 |                     | 3681.36         |               | 0.00                       |                          | 0.00                       |
      | 122       | 03 May 2026      | 47.22                 |                     | 3634.14         |               | 0.00                       |                          | 0.00                       |
      | 123       | 04 May 2026      | 47.22                 |                     | 3586.92         |               | 0.00                       |                          | 0.00                       |
      | 124       | 05 May 2026      | 47.22                 |                     | 3539.70         |               | 0.00                       |                          | 0.00                       |
      | 125       | 06 May 2026      | 47.22                 |                     | 3492.48         |               | 0.00                       |                          | 0.00                       |
      | 126       | 07 May 2026      | 47.22                 |                     | 3445.26         |               | 0.00                       |                          | 0.00                       |
      | 127       | 08 May 2026      | 47.22                 |                     | 3398.04         |               | 0.00                       |                          | 0.00                       |
      | 128       | 09 May 2026      | 47.22                 |                     | 3350.82         |               | 0.00                       |                          | 0.00                       |
      | 129       | 10 May 2026      | 47.22                 |                     | 3303.60         |               | 0.00                       |                          | 0.00                       |
      | 130       | 11 May 2026      | 47.22                 |                     | 3256.38         |               | 0.00                       |                          | 0.00                       |
      | 131       | 12 May 2026      | 47.22                 |                     | 3209.16         |               | 0.00                       |                          | 0.00                       |
      | 132       | 13 May 2026      | 47.22                 |                     | 3161.94         |               | 0.00                       |                          | 0.00                       |
      | 133       | 14 May 2026      | 47.22                 |                     | 3114.72         |               | 0.00                       |                          | 0.00                       |
      | 134       | 15 May 2026      | 47.22                 |                     | 3067.50         |               | 0.00                       |                          | 0.00                       |
      | 135       | 16 May 2026      | 47.22                 |                     | 3020.28         |               | 0.00                       |                          | 0.00                       |
      | 136       | 17 May 2026      | 47.22                 |                     | 2973.06         |               | 0.00                       |                          | 0.00                       |
      | 137       | 18 May 2026      | 47.22                 |                     | 2925.84         |               | 0.00                       |                          | 0.00                       |
      | 138       | 19 May 2026      | 47.22                 |                     | 2878.62         |               | 0.00                       |                          | 0.00                       |
      | 139       | 20 May 2026      | 47.22                 |                     | 2831.40         |               | 0.00                       |                          | 0.00                       |
      | 140       | 21 May 2026      | 47.22                 |                     | 2784.18         |               | 0.00                       |                          | 0.00                       |
      | 141       | 22 May 2026      | 47.22                 |                     | 2736.96         |               | 0.00                       |                          | 0.00                       |
      | 142       | 23 May 2026      | 47.22                 |                     | 2689.74         |               | 0.00                       |                          | 0.00                       |
      | 143       | 24 May 2026      | 47.22                 |                     | 2642.52         |               | 0.00                       |                          | 0.00                       |
      | 144       | 25 May 2026      | 47.22                 |                     | 2595.30         |               | 0.00                       |                          | 0.00                       |
      | 145       | 26 May 2026      | 47.22                 |                     | 2548.08         |               | 0.00                       |                          | 0.00                       |
      | 146       | 27 May 2026      | 47.22                 |                     | 2500.86         |               | 0.00                       |                          | 0.00                       |
      | 147       | 28 May 2026      | 47.22                 |                     | 2453.64         |               | 0.00                       |                          | 0.00                       |
      | 148       | 29 May 2026      | 47.22                 |                     | 2406.42         |               | 0.00                       |                          | 0.00                       |
      | 149       | 30 May 2026      | 47.22                 |                     | 2359.20         |               | 0.00                       |                          | 0.00                       |
      | 150       | 31 May 2026      | 47.22                 |                     | 2311.98         |               | 0.00                       |                          | 0.00                       |
      | 151       | 01 June 2026     | 47.22                 |                     | 2264.76         |               | 0.00                       |                          | 0.00                       |
      | 152       | 02 June 2026     | 47.22                 |                     | 2217.54         |               | 0.00                       |                          | 0.00                       |
      | 153       | 03 June 2026     | 47.22                 |                     | 2170.32         |               | 0.00                       |                          | 0.00                       |
      | 154       | 04 June 2026     | 47.22                 |                     | 2123.10         |               | 0.00                       |                          | 0.00                       |
      | 155       | 05 June 2026     | 47.22                 |                     | 2075.88         |               | 0.00                       |                          | 0.00                       |
      | 156       | 06 June 2026     | 47.22                 |                     | 2028.66         |               | 0.00                       |                          | 0.00                       |
      | 157       | 07 June 2026     | 47.22                 |                     | 1981.44         |               | 0.00                       |                          | 0.00                       |
      | 158       | 08 June 2026     | 47.22                 |                     | 1934.22         |               | 0.00                       |                          | 0.00                       |
      | 159       | 09 June 2026     | 47.22                 |                     | 1887.00         |               | 0.00                       |                          | 0.00                       |
      | 160       | 10 June 2026     | 47.22                 |                     | 1839.78         |               | 0.00                       |                          | 0.00                       |
      | 161       | 11 June 2026     | 47.22                 |                     | 1792.56         |               | 0.00                       |                          | 0.00                       |
      | 162       | 12 June 2026     | 47.22                 |                     | 1745.34         |               | 0.00                       |                          | 0.00                       |
      | 163       | 13 June 2026     | 47.22                 |                     | 1698.12         |               | 0.00                       |                          | 0.00                       |
      | 164       | 14 June 2026     | 47.22                 |                     | 1650.90         |               | 0.00                       |                          | 0.00                       |
      | 165       | 15 June 2026     | 47.22                 |                     | 1603.68         |               | 0.00                       |                          | 0.00                       |
      | 166       | 16 June 2026     | 47.22                 |                     | 1556.46         |               | 0.00                       |                          | 0.00                       |
      | 167       | 17 June 2026     | 47.22                 |                     | 1509.24         |               | 0.00                       |                          | 0.00                       |
      | 168       | 18 June 2026     | 47.22                 |                     | 1462.02         |               | 0.00                       |                          | 0.00                       |
      | 169       | 19 June 2026     | 47.22                 |                     | 1414.80         |               | 0.00                       |                          | 0.00                       |
      | 170       | 20 June 2026     | 47.22                 |                     | 1367.58         |               | 0.00                       |                          | 0.00                       |
      | 171       | 21 June 2026     | 47.22                 |                     | 1320.36         |               | 0.00                       |                          | 0.00                       |
      | 172       | 22 June 2026     | 47.22                 |                     | 1273.14         |               | 0.00                       |                          | 0.00                       |
      | 173       | 23 June 2026     | 47.22                 |                     | 1225.92         |               | 0.00                       |                          | 0.00                       |
      | 174       | 24 June 2026     | 47.22                 |                     | 1178.70         |               | 0.00                       |                          | 0.00                       |
      | 175       | 25 June 2026     | 47.22                 |                     | 1131.48         |               | 0.00                       |                          | 0.00                       |
      | 176       | 26 June 2026     | 47.22                 |                     | 1084.26         |               | 0.00                       |                          | 0.00                       |
      | 177       | 27 June 2026     | 47.22                 |                     | 1037.04         |               | 0.00                       |                          | 0.00                       |
      | 178       | 28 June 2026     | 47.22                 |                     | 989.82          |               | 0.00                       |                          | 0.00                       |
      | 179       | 29 June 2026     | 47.22                 |                     | 942.60          |               | 0.00                       |                          | 0.00                       |
      | 180       | 30 June 2026     | 47.22                 |                     | 895.38          |               | 0.00                       |                          | 0.00                       |
      | 181       | 01 July 2026     | 47.22                 |                     | 848.16          |               | 0.00                       |                          | 0.00                       |
      | 182       | 02 July 2026     | 47.22                 |                     | 800.94          |               | 0.00                       |                          | 0.00                       |
      | 183       | 03 July 2026     | 47.22                 |                     | 753.72          |               | 0.00                       |                          | 0.00                       |
      | 184       | 04 July 2026     | 47.22                 |                     | 706.50          |               | 0.00                       |                          | 0.00                       |
      | 185       | 05 July 2026     | 47.22                 |                     | 659.28          |               | 0.00                       |                          | 0.00                       |
      | 186       | 06 July 2026     | 47.22                 |                     | 612.06          |               | 0.00                       |                          | 0.00                       |
      | 187       | 07 July 2026     | 47.22                 |                     | 564.84          |               | 0.00                       |                          | 0.00                       |
      | 188       | 08 July 2026     | 47.22                 |                     | 517.62          |               | 0.00                       |                          | 0.00                       |
      | 189       | 09 July 2026     | 47.22                 |                     | 470.40          |               | 0.00                       |                          | 0.00                       |
      | 190       | 10 July 2026     | 47.22                 |                     | 423.18          |               | 0.00                       |                          | 0.00                       |
      | 191       | 11 July 2026     | 47.22                 |                     | 375.96          |               | 0.00                       |                          | 0.00                       |
      | 192       | 12 July 2026     | 47.22                 |                     | 328.74          |               | 0.00                       |                          | 0.00                       |
      | 193       | 13 July 2026     | 47.22                 |                     | 281.52          |               | 0.00                       |                          | 0.00                       |
      | 194       | 14 July 2026     | 47.22                 |                     | 234.30          |               | 0.00                       |                          | 0.00                       |
      | 195       | 15 July 2026     | 47.22                 |                     | 187.08          |               | 0.00                       |                          | 0.00                       |
      | 196       | 16 July 2026     | 47.22                 |                     | 139.86          |               | 0.00                       |                          | 0.00                       |
      | 197       | 17 July 2026     | 47.22                 |                     | 92.64           |               | 0.00                       |                          | 0.00                       |
      | 198       | 18 July 2026     | 47.22                 |                     | 45.42           |               | 0.00                       |                          | 0.00                       |
      | 199       | 19 July 2026     | 28.20                 |                     | 17.22           |               | 0.00                       |                          | 0.00                       |
      | 200       | 20 July 2026     | 17.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
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
      | 2         | 03 January 2026  | 47.22                 | 0.00                | 8952.78         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 3         | 04 January 2026  | 47.22                 | 0.00                | 8952.78         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 4         | 05 January 2026  | 47.22                 | 0.00                | 8952.78         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 5         | 06 January 2026  | 47.22                 | 0.00                | 8952.78         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 6         | 07 January 2026  | 47.22                 | 0.00                | 8952.78         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 7         | 08 January 2026  | 47.22                 | 0.00                | 8952.78         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 8         | 09 January 2026  | 47.22                 | 0.00                | 8952.78         | 9000.00       | 0.00                       | 0.00                     | 0.00                       |
      | 9         | 10 January 2026  | 47.22                 | 30.00               | 8952.78         | 8970.00       | 0.00                       | 0.00                     | 0.00                       |
      | 10        | 11 January 2026  | 47.22                 |                     | 8922.78         |               | 0.00                       |                          | 0.00                       |
      | 11        | 12 January 2026  | 47.22                 |                     | 8875.56         |               | 0.00                       |                          | 0.00                       |
      | 12        | 13 January 2026  | 47.22                 |                     | 8828.34         |               | 0.00                       |                          | 0.00                       |
      | 13        | 14 January 2026  | 47.22                 |                     | 8781.12         |               | 0.00                       |                          | 0.00                       |
      | 14        | 15 January 2026  | 47.22                 |                     | 8733.90         |               | 0.00                       |                          | 0.00                       |
      | 15        | 16 January 2026  | 47.22                 |                     | 8686.68         |               | 0.00                       |                          | 0.00                       |
      | 16        | 17 January 2026  | 47.22                 |                     | 8639.46         |               | 0.00                       |                          | 0.00                       |
      | 17        | 18 January 2026  | 47.22                 |                     | 8592.24         |               | 0.00                       |                          | 0.00                       |
      | 18        | 19 January 2026  | 47.22                 |                     | 8545.02         |               | 0.00                       |                          | 0.00                       |
      | 19        | 20 January 2026  | 47.22                 |                     | 8497.80         |               | 0.00                       |                          | 0.00                       |
      | 20        | 21 January 2026  | 47.22                 |                     | 8450.58         |               | 0.00                       |                          | 0.00                       |
      | 21        | 22 January 2026  | 47.22                 |                     | 8403.36         |               | 0.00                       |                          | 0.00                       |
      | 22        | 23 January 2026  | 47.22                 |                     | 8356.14         |               | 0.00                       |                          | 0.00                       |
      | 23        | 24 January 2026  | 47.22                 |                     | 8308.92         |               | 0.00                       |                          | 0.00                       |
      | 24        | 25 January 2026  | 47.22                 |                     | 8261.70         |               | 0.00                       |                          | 0.00                       |
      | 25        | 26 January 2026  | 47.22                 |                     | 8214.48         |               | 0.00                       |                          | 0.00                       |
      | 26        | 27 January 2026  | 47.22                 |                     | 8167.26         |               | 0.00                       |                          | 0.00                       |
      | 27        | 28 January 2026  | 47.22                 |                     | 8120.04         |               | 0.00                       |                          | 0.00                       |
      | 28        | 29 January 2026  | 47.22                 |                     | 8072.82         |               | 0.00                       |                          | 0.00                       |
      | 29        | 30 January 2026  | 47.22                 |                     | 8025.60         |               | 0.00                       |                          | 0.00                       |
      | 30        | 31 January 2026  | 47.22                 |                     | 7978.38         |               | 0.00                       |                          | 0.00                       |
      | 31        | 01 February 2026 | 47.22                 |                     | 7931.16         |               | 0.00                       |                          | 0.00                       |
      | 32        | 02 February 2026 | 47.22                 |                     | 7883.94         |               | 0.00                       |                          | 0.00                       |
      | 33        | 03 February 2026 | 47.22                 |                     | 7836.72         |               | 0.00                       |                          | 0.00                       |
      | 34        | 04 February 2026 | 47.22                 |                     | 7789.50         |               | 0.00                       |                          | 0.00                       |
      | 35        | 05 February 2026 | 47.22                 |                     | 7742.28         |               | 0.00                       |                          | 0.00                       |
      | 36        | 06 February 2026 | 47.22                 |                     | 7695.06         |               | 0.00                       |                          | 0.00                       |
      | 37        | 07 February 2026 | 47.22                 |                     | 7647.84         |               | 0.00                       |                          | 0.00                       |
      | 38        | 08 February 2026 | 47.22                 |                     | 7600.62         |               | 0.00                       |                          | 0.00                       |
      | 39        | 09 February 2026 | 47.22                 |                     | 7553.40         |               | 0.00                       |                          | 0.00                       |
      | 40        | 10 February 2026 | 47.22                 |                     | 7506.18         |               | 0.00                       |                          | 0.00                       |
      | 41        | 11 February 2026 | 47.22                 |                     | 7458.96         |               | 0.00                       |                          | 0.00                       |
      | 42        | 12 February 2026 | 47.22                 |                     | 7411.74         |               | 0.00                       |                          | 0.00                       |
      | 43        | 13 February 2026 | 47.22                 |                     | 7364.52         |               | 0.00                       |                          | 0.00                       |
      | 44        | 14 February 2026 | 47.22                 |                     | 7317.30         |               | 0.00                       |                          | 0.00                       |
      | 45        | 15 February 2026 | 47.22                 |                     | 7270.08         |               | 0.00                       |                          | 0.00                       |
      | 46        | 16 February 2026 | 47.22                 |                     | 7222.86         |               | 0.00                       |                          | 0.00                       |
      | 47        | 17 February 2026 | 47.22                 |                     | 7175.64         |               | 0.00                       |                          | 0.00                       |
      | 48        | 18 February 2026 | 47.22                 |                     | 7128.42         |               | 0.00                       |                          | 0.00                       |
      | 49        | 19 February 2026 | 47.22                 |                     | 7081.20         |               | 0.00                       |                          | 0.00                       |
      | 50        | 20 February 2026 | 47.22                 |                     | 7033.98         |               | 0.00                       |                          | 0.00                       |
      | 51        | 21 February 2026 | 47.22                 |                     | 6986.76         |               | 0.00                       |                          | 0.00                       |
      | 52        | 22 February 2026 | 47.22                 |                     | 6939.54         |               | 0.00                       |                          | 0.00                       |
      | 53        | 23 February 2026 | 47.22                 |                     | 6892.32         |               | 0.00                       |                          | 0.00                       |
      | 54        | 24 February 2026 | 47.22                 |                     | 6845.10         |               | 0.00                       |                          | 0.00                       |
      | 55        | 25 February 2026 | 47.22                 |                     | 6797.88         |               | 0.00                       |                          | 0.00                       |
      | 56        | 26 February 2026 | 47.22                 |                     | 6750.66         |               | 0.00                       |                          | 0.00                       |
      | 57        | 27 February 2026 | 47.22                 |                     | 6703.44         |               | 0.00                       |                          | 0.00                       |
      | 58        | 28 February 2026 | 47.22                 |                     | 6656.22         |               | 0.00                       |                          | 0.00                       |
      | 59        | 01 March 2026    | 47.22                 |                     | 6609.00         |               | 0.00                       |                          | 0.00                       |
      | 60        | 02 March 2026    | 47.22                 |                     | 6561.78         |               | 0.00                       |                          | 0.00                       |
      | 61        | 03 March 2026    | 47.22                 |                     | 6514.56         |               | 0.00                       |                          | 0.00                       |
      | 62        | 04 March 2026    | 47.22                 |                     | 6467.34         |               | 0.00                       |                          | 0.00                       |
      | 63        | 05 March 2026    | 47.22                 |                     | 6420.12         |               | 0.00                       |                          | 0.00                       |
      | 64        | 06 March 2026    | 47.22                 |                     | 6372.90         |               | 0.00                       |                          | 0.00                       |
      | 65        | 07 March 2026    | 47.22                 |                     | 6325.68         |               | 0.00                       |                          | 0.00                       |
      | 66        | 08 March 2026    | 47.22                 |                     | 6278.46         |               | 0.00                       |                          | 0.00                       |
      | 67        | 09 March 2026    | 47.22                 |                     | 6231.24         |               | 0.00                       |                          | 0.00                       |
      | 68        | 10 March 2026    | 47.22                 |                     | 6184.02         |               | 0.00                       |                          | 0.00                       |
      | 69        | 11 March 2026    | 47.22                 |                     | 6136.80         |               | 0.00                       |                          | 0.00                       |
      | 70        | 12 March 2026    | 47.22                 |                     | 6089.58         |               | 0.00                       |                          | 0.00                       |
      | 71        | 13 March 2026    | 47.22                 |                     | 6042.36         |               | 0.00                       |                          | 0.00                       |
      | 72        | 14 March 2026    | 47.22                 |                     | 5995.14         |               | 0.00                       |                          | 0.00                       |
      | 73        | 15 March 2026    | 47.22                 |                     | 5947.92         |               | 0.00                       |                          | 0.00                       |
      | 74        | 16 March 2026    | 47.22                 |                     | 5900.70         |               | 0.00                       |                          | 0.00                       |
      | 75        | 17 March 2026    | 47.22                 |                     | 5853.48         |               | 0.00                       |                          | 0.00                       |
      | 76        | 18 March 2026    | 47.22                 |                     | 5806.26         |               | 0.00                       |                          | 0.00                       |
      | 77        | 19 March 2026    | 47.22                 |                     | 5759.04         |               | 0.00                       |                          | 0.00                       |
      | 78        | 20 March 2026    | 47.22                 |                     | 5711.82         |               | 0.00                       |                          | 0.00                       |
      | 79        | 21 March 2026    | 47.22                 |                     | 5664.60         |               | 0.00                       |                          | 0.00                       |
      | 80        | 22 March 2026    | 47.22                 |                     | 5617.38         |               | 0.00                       |                          | 0.00                       |
      | 81        | 23 March 2026    | 47.22                 |                     | 5570.16         |               | 0.00                       |                          | 0.00                       |
      | 82        | 24 March 2026    | 47.22                 |                     | 5522.94         |               | 0.00                       |                          | 0.00                       |
      | 83        | 25 March 2026    | 47.22                 |                     | 5475.72         |               | 0.00                       |                          | 0.00                       |
      | 84        | 26 March 2026    | 47.22                 |                     | 5428.50         |               | 0.00                       |                          | 0.00                       |
      | 85        | 27 March 2026    | 47.22                 |                     | 5381.28         |               | 0.00                       |                          | 0.00                       |
      | 86        | 28 March 2026    | 47.22                 |                     | 5334.06         |               | 0.00                       |                          | 0.00                       |
      | 87        | 29 March 2026    | 47.22                 |                     | 5286.84         |               | 0.00                       |                          | 0.00                       |
      | 88        | 30 March 2026    | 47.22                 |                     | 5239.62         |               | 0.00                       |                          | 0.00                       |
      | 89        | 31 March 2026    | 47.22                 |                     | 5192.40         |               | 0.00                       |                          | 0.00                       |
      | 90        | 01 April 2026    | 47.22                 |                     | 5145.18         |               | 0.00                       |                          | 0.00                       |
      | 91        | 02 April 2026    | 47.22                 |                     | 5097.96         |               | 0.00                       |                          | 0.00                       |
      | 92        | 03 April 2026    | 47.22                 |                     | 5050.74         |               | 0.00                       |                          | 0.00                       |
      | 93        | 04 April 2026    | 47.22                 |                     | 5003.52         |               | 0.00                       |                          | 0.00                       |
      | 94        | 05 April 2026    | 47.22                 |                     | 4956.30         |               | 0.00                       |                          | 0.00                       |
      | 95        | 06 April 2026    | 47.22                 |                     | 4909.08         |               | 0.00                       |                          | 0.00                       |
      | 96        | 07 April 2026    | 47.22                 |                     | 4861.86         |               | 0.00                       |                          | 0.00                       |
      | 97        | 08 April 2026    | 47.22                 |                     | 4814.64         |               | 0.00                       |                          | 0.00                       |
      | 98        | 09 April 2026    | 47.22                 |                     | 4767.42         |               | 0.00                       |                          | 0.00                       |
      | 99        | 10 April 2026    | 47.22                 |                     | 4720.20         |               | 0.00                       |                          | 0.00                       |
      | 100       | 11 April 2026    | 47.22                 |                     | 4672.98         |               | 0.00                       |                          | 0.00                       |
      | 101       | 12 April 2026    | 47.22                 |                     | 4625.76         |               | 0.00                       |                          | 0.00                       |
      | 102       | 13 April 2026    | 47.22                 |                     | 4578.54         |               | 0.00                       |                          | 0.00                       |
      | 103       | 14 April 2026    | 47.22                 |                     | 4531.32         |               | 0.00                       |                          | 0.00                       |
      | 104       | 15 April 2026    | 47.22                 |                     | 4484.10         |               | 0.00                       |                          | 0.00                       |
      | 105       | 16 April 2026    | 47.22                 |                     | 4436.88         |               | 0.00                       |                          | 0.00                       |
      | 106       | 17 April 2026    | 47.22                 |                     | 4389.66         |               | 0.00                       |                          | 0.00                       |
      | 107       | 18 April 2026    | 47.22                 |                     | 4342.44         |               | 0.00                       |                          | 0.00                       |
      | 108       | 19 April 2026    | 47.22                 |                     | 4295.22         |               | 0.00                       |                          | 0.00                       |
      | 109       | 20 April 2026    | 47.22                 |                     | 4248.00         |               | 0.00                       |                          | 0.00                       |
      | 110       | 21 April 2026    | 47.22                 |                     | 4200.78         |               | 0.00                       |                          | 0.00                       |
      | 111       | 22 April 2026    | 47.22                 |                     | 4153.56         |               | 0.00                       |                          | 0.00                       |
      | 112       | 23 April 2026    | 47.22                 |                     | 4106.34         |               | 0.00                       |                          | 0.00                       |
      | 113       | 24 April 2026    | 47.22                 |                     | 4059.12         |               | 0.00                       |                          | 0.00                       |
      | 114       | 25 April 2026    | 47.22                 |                     | 4011.90         |               | 0.00                       |                          | 0.00                       |
      | 115       | 26 April 2026    | 47.22                 |                     | 3964.68         |               | 0.00                       |                          | 0.00                       |
      | 116       | 27 April 2026    | 47.22                 |                     | 3917.46         |               | 0.00                       |                          | 0.00                       |
      | 117       | 28 April 2026    | 47.22                 |                     | 3870.24         |               | 0.00                       |                          | 0.00                       |
      | 118       | 29 April 2026    | 47.22                 |                     | 3823.02         |               | 0.00                       |                          | 0.00                       |
      | 119       | 30 April 2026    | 47.22                 |                     | 3775.80         |               | 0.00                       |                          | 0.00                       |
      | 120       | 01 May 2026      | 47.22                 |                     | 3728.58         |               | 0.00                       |                          | 0.00                       |
      | 121       | 02 May 2026      | 47.22                 |                     | 3681.36         |               | 0.00                       |                          | 0.00                       |
      | 122       | 03 May 2026      | 47.22                 |                     | 3634.14         |               | 0.00                       |                          | 0.00                       |
      | 123       | 04 May 2026      | 47.22                 |                     | 3586.92         |               | 0.00                       |                          | 0.00                       |
      | 124       | 05 May 2026      | 47.22                 |                     | 3539.70         |               | 0.00                       |                          | 0.00                       |
      | 125       | 06 May 2026      | 47.22                 |                     | 3492.48         |               | 0.00                       |                          | 0.00                       |
      | 126       | 07 May 2026      | 47.22                 |                     | 3445.26         |               | 0.00                       |                          | 0.00                       |
      | 127       | 08 May 2026      | 47.22                 |                     | 3398.04         |               | 0.00                       |                          | 0.00                       |
      | 128       | 09 May 2026      | 47.22                 |                     | 3350.82         |               | 0.00                       |                          | 0.00                       |
      | 129       | 10 May 2026      | 47.22                 |                     | 3303.60         |               | 0.00                       |                          | 0.00                       |
      | 130       | 11 May 2026      | 47.22                 |                     | 3256.38         |               | 0.00                       |                          | 0.00                       |
      | 131       | 12 May 2026      | 47.22                 |                     | 3209.16         |               | 0.00                       |                          | 0.00                       |
      | 132       | 13 May 2026      | 47.22                 |                     | 3161.94         |               | 0.00                       |                          | 0.00                       |
      | 133       | 14 May 2026      | 47.22                 |                     | 3114.72         |               | 0.00                       |                          | 0.00                       |
      | 134       | 15 May 2026      | 47.22                 |                     | 3067.50         |               | 0.00                       |                          | 0.00                       |
      | 135       | 16 May 2026      | 47.22                 |                     | 3020.28         |               | 0.00                       |                          | 0.00                       |
      | 136       | 17 May 2026      | 47.22                 |                     | 2973.06         |               | 0.00                       |                          | 0.00                       |
      | 137       | 18 May 2026      | 47.22                 |                     | 2925.84         |               | 0.00                       |                          | 0.00                       |
      | 138       | 19 May 2026      | 47.22                 |                     | 2878.62         |               | 0.00                       |                          | 0.00                       |
      | 139       | 20 May 2026      | 47.22                 |                     | 2831.40         |               | 0.00                       |                          | 0.00                       |
      | 140       | 21 May 2026      | 47.22                 |                     | 2784.18         |               | 0.00                       |                          | 0.00                       |
      | 141       | 22 May 2026      | 47.22                 |                     | 2736.96         |               | 0.00                       |                          | 0.00                       |
      | 142       | 23 May 2026      | 47.22                 |                     | 2689.74         |               | 0.00                       |                          | 0.00                       |
      | 143       | 24 May 2026      | 47.22                 |                     | 2642.52         |               | 0.00                       |                          | 0.00                       |
      | 144       | 25 May 2026      | 47.22                 |                     | 2595.30         |               | 0.00                       |                          | 0.00                       |
      | 145       | 26 May 2026      | 47.22                 |                     | 2548.08         |               | 0.00                       |                          | 0.00                       |
      | 146       | 27 May 2026      | 47.22                 |                     | 2500.86         |               | 0.00                       |                          | 0.00                       |
      | 147       | 28 May 2026      | 47.22                 |                     | 2453.64         |               | 0.00                       |                          | 0.00                       |
      | 148       | 29 May 2026      | 47.22                 |                     | 2406.42         |               | 0.00                       |                          | 0.00                       |
      | 149       | 30 May 2026      | 47.22                 |                     | 2359.20         |               | 0.00                       |                          | 0.00                       |
      | 150       | 31 May 2026      | 47.22                 |                     | 2311.98         |               | 0.00                       |                          | 0.00                       |
      | 151       | 01 June 2026     | 47.22                 |                     | 2264.76         |               | 0.00                       |                          | 0.00                       |
      | 152       | 02 June 2026     | 47.22                 |                     | 2217.54         |               | 0.00                       |                          | 0.00                       |
      | 153       | 03 June 2026     | 47.22                 |                     | 2170.32         |               | 0.00                       |                          | 0.00                       |
      | 154       | 04 June 2026     | 47.22                 |                     | 2123.10         |               | 0.00                       |                          | 0.00                       |
      | 155       | 05 June 2026     | 47.22                 |                     | 2075.88         |               | 0.00                       |                          | 0.00                       |
      | 156       | 06 June 2026     | 47.22                 |                     | 2028.66         |               | 0.00                       |                          | 0.00                       |
      | 157       | 07 June 2026     | 47.22                 |                     | 1981.44         |               | 0.00                       |                          | 0.00                       |
      | 158       | 08 June 2026     | 47.22                 |                     | 1934.22         |               | 0.00                       |                          | 0.00                       |
      | 159       | 09 June 2026     | 47.22                 |                     | 1887.00         |               | 0.00                       |                          | 0.00                       |
      | 160       | 10 June 2026     | 47.22                 |                     | 1839.78         |               | 0.00                       |                          | 0.00                       |
      | 161       | 11 June 2026     | 47.22                 |                     | 1792.56         |               | 0.00                       |                          | 0.00                       |
      | 162       | 12 June 2026     | 47.22                 |                     | 1745.34         |               | 0.00                       |                          | 0.00                       |
      | 163       | 13 June 2026     | 47.22                 |                     | 1698.12         |               | 0.00                       |                          | 0.00                       |
      | 164       | 14 June 2026     | 47.22                 |                     | 1650.90         |               | 0.00                       |                          | 0.00                       |
      | 165       | 15 June 2026     | 47.22                 |                     | 1603.68         |               | 0.00                       |                          | 0.00                       |
      | 166       | 16 June 2026     | 47.22                 |                     | 1556.46         |               | 0.00                       |                          | 0.00                       |
      | 167       | 17 June 2026     | 47.22                 |                     | 1509.24         |               | 0.00                       |                          | 0.00                       |
      | 168       | 18 June 2026     | 47.22                 |                     | 1462.02         |               | 0.00                       |                          | 0.00                       |
      | 169       | 19 June 2026     | 47.22                 |                     | 1414.80         |               | 0.00                       |                          | 0.00                       |
      | 170       | 20 June 2026     | 47.22                 |                     | 1367.58         |               | 0.00                       |                          | 0.00                       |
      | 171       | 21 June 2026     | 47.22                 |                     | 1320.36         |               | 0.00                       |                          | 0.00                       |
      | 172       | 22 June 2026     | 47.22                 |                     | 1273.14         |               | 0.00                       |                          | 0.00                       |
      | 173       | 23 June 2026     | 47.22                 |                     | 1225.92         |               | 0.00                       |                          | 0.00                       |
      | 174       | 24 June 2026     | 47.22                 |                     | 1178.70         |               | 0.00                       |                          | 0.00                       |
      | 175       | 25 June 2026     | 47.22                 |                     | 1131.48         |               | 0.00                       |                          | 0.00                       |
      | 176       | 26 June 2026     | 47.22                 |                     | 1084.26         |               | 0.00                       |                          | 0.00                       |
      | 177       | 27 June 2026     | 47.22                 |                     | 1037.04         |               | 0.00                       |                          | 0.00                       |
      | 178       | 28 June 2026     | 47.22                 |                     | 989.82          |               | 0.00                       |                          | 0.00                       |
      | 179       | 29 June 2026     | 47.22                 |                     | 942.60          |               | 0.00                       |                          | 0.00                       |
      | 180       | 30 June 2026     | 47.22                 |                     | 895.38          |               | 0.00                       |                          | 0.00                       |
      | 181       | 01 July 2026     | 47.22                 |                     | 848.16          |               | 0.00                       |                          | 0.00                       |
      | 182       | 02 July 2026     | 47.22                 |                     | 800.94          |               | 0.00                       |                          | 0.00                       |
      | 183       | 03 July 2026     | 47.22                 |                     | 753.72          |               | 0.00                       |                          | 0.00                       |
      | 184       | 04 July 2026     | 47.22                 |                     | 706.50          |               | 0.00                       |                          | 0.00                       |
      | 185       | 05 July 2026     | 47.22                 |                     | 659.28          |               | 0.00                       |                          | 0.00                       |
      | 186       | 06 July 2026     | 47.22                 |                     | 612.06          |               | 0.00                       |                          | 0.00                       |
      | 187       | 07 July 2026     | 47.22                 |                     | 564.84          |               | 0.00                       |                          | 0.00                       |
      | 188       | 08 July 2026     | 47.22                 |                     | 517.62          |               | 0.00                       |                          | 0.00                       |
      | 189       | 09 July 2026     | 47.22                 |                     | 470.40          |               | 0.00                       |                          | 0.00                       |
      | 190       | 10 July 2026     | 47.22                 |                     | 423.18          |               | 0.00                       |                          | 0.00                       |
      | 191       | 11 July 2026     | 28.20                 |                     | 394.98          |               | 0.00                       |                          | 0.00                       |
      | 192       | 12 July 2026     | 47.22                 |                     | 347.76          |               | 0.00                       |                          | 0.00                       |
      | 193       | 13 July 2026     | 47.22                 |                     | 300.54          |               | 0.00                       |                          | 0.00                       |
      | 194       | 14 July 2026     | 47.22                 |                     | 253.32          |               | 0.00                       |                          | 0.00                       |
      | 195       | 15 July 2026     | 47.22                 |                     | 206.10          |               | 0.00                       |                          | 0.00                       |
      | 196       | 16 July 2026     | 47.22                 |                     | 158.88          |               | 0.00                       |                          | 0.00                       |
      | 197       | 17 July 2026     | 47.22                 |                     | 111.66          |               | 0.00                       |                          | 0.00                       |
      | 198       | 18 July 2026     | 47.22                 |                     | 64.44           |               | 0.00                       |                          | 0.00                       |
      | 199       | 19 July 2026     | 47.22                 |                     | 17.22           |               | 0.00                       |                          | 0.00                       |
      | 200       | 20 July 2026     | 17.22                 |                     | 0.00            |               | 0.00                       |                          | 0.00                       |
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C94030
  Scenario: Verify Working Capital amortization schedule with period payment rate change after repayment and discount with WC COB run - UC21
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 100      |
    Then Working Capital loan amortization schedule has 23 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance   | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 01 January 2026  | -1000.00              | 1000.00         |                            | 100.00                     |                     | 1000.00         |                          | 100.00                   |
      | 1         | 02 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      |                     |                 |                          |                          |
      | 2         | 03 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      |                     |                 |                          |                          |
      | 3         | 04 January 2026  | 50.00                 | 874.29          | 7.74                       | 75.71                      |                     |                 |                          |                          |
      | 4         | 05 January 2026  | 50.00                 | 831.67          | 7.39                       | 68.32                      |                     |                 |                          |                          |
      | 5         | 06 January 2026  | 50.00                 | 788.70          | 7.03                       | 61.29                      |                     |                 |                          |                          |
      | 6         | 07 January 2026  | 50.00                 | 745.36          | 6.66                       | 54.63                      |                     |                 |                          |                          |
      | 7         | 08 January 2026  | 50.00                 | 701.65          | 6.30                       | 48.33                      |                     |                 |                          |                          |
      | 8         | 09 January 2026  | 50.00                 | 657.58          | 5.93                       | 42.40                      |                     |                 |                          |                          |
      | 9         | 10 January 2026  | 50.00                 | 613.14          | 5.55                       | 36.85                      |                     |                 |                          |                          |
      | 10        | 11 January 2026  | 50.00                 | 568.31          | 5.18                       | 31.67                      |                     |                 |                          |                          |
      | 11        | 12 January 2026  | 50.00                 | 523.12          | 4.80                       | 26.87                      |                     |                 |                          |                          |
      | 12        | 13 January 2026  | 50.00                 | 477.53          | 4.42                       | 22.45                      |                     |                 |                          |                          |
      | 13        | 14 January 2026  | 50.00                 | 431.57          | 4.03                       | 18.42                      |                     |                 |                          |                          |
      | 14        | 15 January 2026  | 50.00                 | 385.21          | 3.65                       | 14.77                      |                     |                 |                          |                          |
      | 15        | 16 January 2026  | 50.00                 | 338.47          | 3.25                       | 11.52                      |                     |                 |                          |                          |
      | 16        | 17 January 2026  | 50.00                 | 291.33          | 2.86                       | 8.66                       |                     |                 |                          |                          |
      | 17        | 18 January 2026  | 50.00                 | 243.79          | 2.46                       | 6.20                       |                     |                 |                          |                          |
      | 18        | 19 January 2026  | 50.00                 | 195.85          | 2.06                       | 4.14                       |                     |                 |                          |                          |
      | 19        | 20 January 2026  | 50.00                 | 147.50          | 1.65                       | 2.49                       |                     |                 |                          |                          |
      | 20        | 21 January 2026  | 50.00                 | 98.75           | 1.25                       | 1.24                       |                     |                 |                          |                          |
      | 21        | 22 January 2026  | 50.00                 | 49.58           | 0.83                       | 0.41                       |                     |                 |                          |                          |
      | 22        | 23 January 2026  | 50.00                 | 0.00            | 0.41                       | 0.00                       |                     |                 |                          |                          |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan amortization schedule has 25 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance   | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 01 January 2026  | -1000.00              | 1000.00         |                            | 100.00                     |                     | 1000.00         |                          | 100.00                   |
      | 1         | 02 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 2         | 03 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 3         | 04 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      |                     |                 |                          |                          |
      | 4         | 05 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      |                     |                 |                          |                          |
      | 5         | 06 January 2026  | 50.00                 | 874.29          | 7.74                       | 75.71                      |                     |                 |                          |                          |
      | 6         | 07 January 2026  | 50.00                 | 831.67          | 7.39                       | 68.32                      |                     |                 |                          |                          |
      | 7         | 08 January 2026  | 50.00                 | 788.70          | 7.03                       | 61.29                      |                     |                 |                          |                          |
      | 8         | 09 January 2026  | 50.00                 | 745.36          | 6.66                       | 54.63                      |                     |                 |                          |                          |
      | 9         | 10 January 2026  | 50.00                 | 701.65          | 6.30                       | 48.33                      |                     |                 |                          |                          |
      | 10        | 11 January 2026  | 50.00                 | 657.58          | 5.93                       | 42.40                      |                     |                 |                          |                          |
      | 11        | 12 January 2026  | 50.00                 | 613.14          | 5.55                       | 36.85                      |                     |                 |                          |                          |
      | 12        | 13 January 2026  | 50.00                 | 568.31          | 5.18                       | 31.67                      |                     |                 |                          |                          |
      | 13        | 14 January 2026  | 50.00                 | 523.12          | 4.80                       | 26.87                      |                     |                 |                          |                          |
      | 14        | 15 January 2026  | 50.00                 | 477.53          | 4.42                       | 22.45                      |                     |                 |                          |                          |
      | 15        | 16 January 2026  | 50.00                 | 431.57          | 4.03                       | 18.42                      |                     |                 |                          |                          |
      | 16        | 17 January 2026  | 50.00                 | 385.21          | 3.65                       | 14.77                      |                     |                 |                          |                          |
      | 17        | 18 January 2026  | 50.00                 | 338.47          | 3.25                       | 11.52                      |                     |                 |                          |                          |
      | 18        | 19 January 2026  | 50.00                 | 291.33          | 2.86                       | 8.66                       |                     |                 |                          |                          |
      | 19        | 20 January 2026  | 50.00                 | 243.79          | 2.46                       | 6.20                       |                     |                 |                          |                          |
      | 20        | 21 January 2026  | 50.00                 | 195.85          | 2.06                       | 4.14                       |                     |                 |                          |                          |
      | 21        | 22 January 2026  | 50.00                 | 147.50          | 1.65                       | 2.49                       |                     |                 |                          |                          |
      | 22        | 23 January 2026  | 50.00                 | 98.75           | 1.24                       | 1.25                       |                     |                 |                          |                          |
      | 23        | 24 January 2026  | 50.00                 | 49.58           | 0.83                       | 0.42                       |                     |                 |                          |                          |
      | 24        | 25 January 2026  | 50.00                 | 0.00            | 0.42                       | 0.00                       |                     |                 |                          |                          |
    And Working Capital Loan has transactions:
      | transactionDate | type           | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement   | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee   | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
# ---- make repayment --- #
    And Customer makes repayment by loan external ID on "04 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital loan amortization schedule has 25 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance   | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 01 January 2026  | -1000.00              | 1000.00         |                            | 100.00                     |                     | 1000.00         |                          | 100.00                   |
      | 1         | 02 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 2         | 03 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 3         | 04 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 50.00               | 958.45          | 8.45                     | 91.55                    |
      | 4         | 05 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      |                     |                 |                          |                          |
      | 5         | 06 January 2026  | 50.00                 | 874.29          | 7.74                       | 75.71                      |                     |                 |                          |                          |
      | 6         | 07 January 2026  | 50.00                 | 831.67          | 7.39                       | 68.32                      |                     |                 |                          |                          |
      | 7         | 08 January 2026  | 50.00                 | 788.70          | 7.03                       | 61.29                      |                     |                 |                          |                          |
      | 8         | 09 January 2026  | 50.00                 | 745.36          | 6.66                       | 54.63                      |                     |                 |                          |                          |
      | 9         | 10 January 2026  | 50.00                 | 701.65          | 6.30                       | 48.33                      |                     |                 |                          |                          |
      | 10        | 11 January 2026  | 50.00                 | 657.58          | 5.93                       | 42.40                      |                     |                 |                          |                          |
      | 11        | 12 January 2026  | 50.00                 | 613.14          | 5.55                       | 36.85                      |                     |                 |                          |                          |
      | 12        | 13 January 2026  | 50.00                 | 568.31          | 5.18                       | 31.67                      |                     |                 |                          |                          |
      | 13        | 14 January 2026  | 50.00                 | 523.12          | 4.80                       | 26.87                      |                     |                 |                          |                          |
      | 14        | 15 January 2026  | 50.00                 | 477.53          | 4.42                       | 22.45                      |                     |                 |                          |                          |
      | 15        | 16 January 2026  | 50.00                 | 431.57          | 4.03                       | 18.42                      |                     |                 |                          |                          |
      | 16        | 17 January 2026  | 50.00                 | 385.21          | 3.65                       | 14.77                      |                     |                 |                          |                          |
      | 17        | 18 January 2026  | 50.00                 | 338.47          | 3.25                       | 11.52                      |                     |                 |                          |                          |
      | 18        | 19 January 2026  | 50.00                 | 291.33          | 2.86                       | 8.66                       |                     |                 |                          |                          |
      | 19        | 20 January 2026  | 50.00                 | 243.79          | 2.46                       | 6.20                       |                     |                 |                          |                          |
      | 20        | 21 January 2026  | 50.00                 | 195.85          | 2.06                       | 4.14                       |                     |                 |                          |                          |
      | 21        | 22 January 2026  | 50.00                 | 147.50          | 1.65                       | 2.49                       |                     |                 |                          |                          |
      | 22        | 23 January 2026  | 50.00                 | 98.75           | 1.24                       | 1.25                       |                     |                 |                          |                          |
      | 23        | 24 January 2026  | 50.00                 | 49.58           | 0.83                       | 0.42                       |                     |                 |                          |                          |
      | 24        | 25 January 2026  | 50.00                 | 0.00            | 0.42                       | 0.00                       |                     |                 |                          |                          |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
# --- update period payment rate --- #
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "20" value effective from "20 January 2026"
    Then Working Capital loan amortization schedule has 38 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance   | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 01 January 2026  | -1000.00              | 1000.00         |                            | 100.00                     |                     | 1000.00         |                          | 100.00                   |
      | 1         | 02 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 2         | 03 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 3         | 04 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 50.00               | 958.45          | 8.45                     | 91.55                    |
      | 4         | 05 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 5         | 06 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 6         | 07 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 7         | 08 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 8         | 09 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 9         | 10 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 10        | 11 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 11        | 12 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 12        | 13 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 13        | 14 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 14        | 15 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 15        | 16 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 16        | 17 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 17        | 18 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 18        | 19 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 19        | 20 January 2026  | 55.56                 | 911.84          | 8.95                       | 82.60                      |                     |                 |                          |                          |
      | 20        | 21 January 2026  | 55.56                 | 864.80          | 8.52                       | 74.08                      |                     |                 |                          |                          |
      | 21        | 22 January 2026  | 55.56                 | 817.31          | 8.08                       | 66.00                      |                     |                 |                          |                          |
      | 22        | 23 January 2026  | 55.56                 | 769.39          | 7.63                       | 58.37                      |                     |                 |                          |                          |
      | 23        | 24 January 2026  | 55.56                 | 721.01          | 7.19                       | 51.18                      |                     |                 |                          |                          |
      | 24        | 25 January 2026  | 55.56                 | 672.19          | 6.73                       | 44.45                      |                     |                 |                          |                          |
      | 25        | 26 January 2026  | 55.56                 | 622.91          | 6.28                       | 38.17                      |                     |                 |                          |                          |
      | 26        | 27 January 2026  | 55.56                 | 573.16          | 5.82                       | 32.35                      |                     |                 |                          |                          |
      | 27        | 28 January 2026  | 55.56                 | 522.96          | 5.35                       | 27.00                      |                     |                 |                          |                          |
      | 28        | 29 January 2026  | 55.56                 | 472.28          | 4.88                       | 22.12                      |                     |                 |                          |                          |
      | 29        | 30 January 2026  | 55.56                 | 421.13          | 4.41                       | 17.71                      |                     |                 |                          |                          |
      | 30        | 31 January 2026  | 55.56                 | 369.50          | 3.93                       | 13.78                      |                     |                 |                          |                          |
      | 31        | 01 February 2026 | 55.56                 | 317.40          | 3.45                       | 10.33                      |                     |                 |                          |                          |
      | 32        | 02 February 2026 | 55.56                 | 264.80          | 2.96                       | 7.37                       |                     |                 |                          |                          |
      | 33        | 03 February 2026 | 55.56                 | 211.71          | 2.47                       | 4.90                       |                     |                 |                          |                          |
      | 34        | 04 February 2026 | 55.56                 | 158.13          | 1.98                       | 2.92                       |                     |                 |                          |                          |
      | 35        | 05 February 2026 | 55.56                 | 104.05          | 1.48                       | 1.44                       |                     |                 |                          |                          |
      | 36        | 06 February 2026 | 55.56                 | 49.46           | 0.97                       | 0.47                       |                     |                 |                          |                          |
      | 37        | 07 February 2026 | 49.92                 | 0.00            | 0.47                       | 0.00                       |                     |                 |                          |                          |
# ---- make repayment after updated period payment rate --- #
    When Admin sets the business date to "21 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment by loan external ID on "21 January 2026" with 55.56 transaction amount on Working Capital loan
    Then Working Capital loan amortization schedule has 40 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance   | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 01 January 2026  | -1000.00              | 1000.00         |                            | 100.00                     |                     | 1000.00         |                          | 100.00                   |
      | 1         | 02 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 2         | 03 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 3         | 04 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 50.00               | 958.45          | 8.45                     | 91.55                    |
      | 4         | 05 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 5         | 06 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 6         | 07 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 7         | 08 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 8         | 09 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 9         | 10 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 10        | 11 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 11        | 12 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 12        | 13 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 13        | 14 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 14        | 15 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 15        | 16 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 16        | 17 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 17        | 18 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 18        | 19 January 2026  | 50.00                 | 916.54          | 8.10                       | 83.45                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 19        | 20 January 2026  | 55.56                 | 911.84          | 8.95                       | 82.60                      | 0.00                | 958.45          | 0.00                     | 91.55                    |
      | 20        | 21 January 2026  | 55.56                 | 911.84          | 8.95                       | 82.60                      | 55.56               | 911.84          | 8.95                     | 82.60                    |
      | 21        | 22 January 2026  | 55.56                 | 864.80          | 8.52                       | 74.07                      |                     |                 |                          |                          |
      | 22        | 23 January 2026  | 55.56                 | 817.32          | 8.08                       | 65.99                      |                     |                 |                          |                          |
      | 23        | 24 January 2026  | 55.56                 | 769.39          | 7.63                       | 58.36                      |                     |                 |                          |                          |
      | 24        | 25 January 2026  | 55.56                 | 721.02          | 7.19                       | 51.17                      |                     |                 |                          |                          |
      | 25        | 26 January 2026  | 55.56                 | 672.19          | 6.73                       | 44.44                      |                     |                 |                          |                          |
      | 26        | 27 January 2026  | 55.56                 | 622.91          | 6.28                       | 38.16                      |                     |                 |                          |                          |
      | 27        | 28 January 2026  | 55.56                 | 573.17          | 5.82                       | 32.34                      |                     |                 |                          |                          |
      | 28        | 29 January 2026  | 55.56                 | 522.96          | 5.35                       | 26.99                      |                     |                 |                          |                          |
      | 29        | 30 January 2026  | 55.56                 | 472.28          | 4.88                       | 22.11                      |                     |                 |                          |                          |
      | 30        | 31 January 2026  | 55.56                 | 421.13          | 4.41                       | 17.70                      |                     |                 |                          |                          |
      | 31        | 01 February 2026 | 55.56                 | 369.51          | 3.93                       | 13.77                      |                     |                 |                          |                          |
      | 32        | 02 February 2026 | 55.56                 | 317.40          | 3.45                       | 10.32                      |                     |                 |                          |                          |
      | 33        | 03 February 2026 | 55.56                 | 264.80          | 2.96                       | 7.36                       |                     |                 |                          |                          |
      | 34        | 04 February 2026 | 55.56                 | 211.72          | 2.47                       | 4.89                       |                     |                 |                          |                          |
      | 35        | 05 February 2026 | 55.56                 | 158.13          | 1.98                       | 2.91                       |                     |                 |                          |                          |
      | 36        | 06 February 2026 | 55.56                 | 104.05          | 1.48                       | 1.43                       |                     |                 |                          |                          |
      | 37        | 07 February 2026 | 49.92                 | 55.10           | 0.91                       | 0.52                       |                     |                 |                          |                          |
      | 38        | 08 February 2026 | 55.56                 | 0.05            | 0.51                       | 0.01                       |                     |                 |                          |                          |
      | 39        | 09 February 2026 | 0.06                  | 0.00            | 0.01                       | 0.00                       |                     |                 |                          |                          |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization | 8.45              |                  |                   |                       | false    |
      | 21 January 2026 | Repayment                 | 55.56             | 55.56            | 0.0               | 0.0                   | false    |
    Then Admin closes the Working Capital loan with a full repayment on "21 January 2026"

  @TestRailId:C94031
  Scenario: Verify Working Capital amortization schedule with period payment rate change with repayment afterwards with WC COB run - UC22
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 100      |
# --- update period payment rate --- #
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "20" value effective from "20 January 2026"
    Then Working Capital loan amortization schedule has 39 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance   | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 01 January 2026  | -1000.00              | 1000.00         |                            | 100.00                     |                     | 1000.00         |                          | 100.00                   |
      | 1         | 02 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 2         | 03 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 3         | 04 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 4         | 05 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 5         | 06 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 6         | 07 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 7         | 08 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 8         | 09 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 9         | 10 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 10        | 11 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 11        | 12 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 12        | 13 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 13        | 14 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 14        | 15 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 15        | 16 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 16        | 17 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 17        | 18 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 18        | 19 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 19        | 20 January 2026  | 55.56                 | 953.78          | 9.34                       | 90.66                      |                     |                 |                          |                          |
      | 20        | 21 January 2026  | 55.56                 | 907.13          | 8.91                       | 81.75                      |                     |                 |                          |                          |
      | 21        | 22 January 2026  | 55.56                 | 860.04          | 8.47                       | 73.28                      |                     |                 |                          |                          |
      | 22        | 23 January 2026  | 55.56                 | 812.52          | 8.03                       | 65.25                      |                     |                 |                          |                          |
      | 23        | 24 January 2026  | 55.56                 | 764.55          | 7.59                       | 57.66                      |                     |                 |                          |                          |
      | 24        | 25 January 2026  | 55.56                 | 716.13          | 7.14                       | 50.52                      |                     |                 |                          |                          |
      | 25        | 26 January 2026  | 55.56                 | 667.26          | 6.69                       | 43.83                      |                     |                 |                          |                          |
      | 26        | 27 January 2026  | 55.56                 | 617.93          | 6.23                       | 37.60                      |                     |                 |                          |                          |
      | 27        | 28 January 2026  | 55.56                 | 568.14          | 5.77                       | 31.83                      |                     |                 |                          |                          |
      | 28        | 29 January 2026  | 55.56                 | 517.89          | 5.31                       | 26.52                      |                     |                 |                          |                          |
      | 29        | 30 January 2026  | 55.56                 | 467.16          | 4.84                       | 21.68                      |                     |                 |                          |                          |
      | 30        | 31 January 2026  | 55.56                 | 415.97          | 4.36                       | 17.32                      |                     |                 |                          |                          |
      | 31        | 01 February 2026 | 55.56                 | 364.29          | 3.89                       | 13.43                      |                     |                 |                          |                          |
      | 32        | 02 February 2026 | 55.56                 | 312.14          | 3.40                       | 10.03                      |                     |                 |                          |                          |
      | 33        | 03 February 2026 | 55.56                 | 259.49          | 2.92                       | 7.11                       |                     |                 |                          |                          |
      | 34        | 04 February 2026 | 55.56                 | 206.35          | 2.42                       | 4.69                       |                     |                 |                          |                          |
      | 35        | 05 February 2026 | 55.56                 | 152.72          | 1.93                       | 2.76                       |                     |                 |                          |                          |
      | 36        | 06 February 2026 | 55.56                 | 98.59           | 1.43                       | 1.33                       |                     |                 |                          |                          |
      | 37        | 07 February 2026 | 55.56                 | 43.95           | 0.92                       | 0.41                       |                     |                 |                          |                          |
      | 38        | 08 February 2026 | 44.36                 | 0.00            | 0.41                       | 0.00                       |                     |                 |                          |                          |
# ---- make repayment after updated period payment rate --- #
    When Admin sets the business date to "21 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment by loan external ID on "21 January 2026" with 55.56 transaction amount on Working Capital loan
    Then Working Capital loan amortization schedule has 41 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance | actualPaymentAmount | actualBalance   | actualAmortizationAmount | actualDiscountFeeBalance |
      | 0         | 01 January 2026  | -1000.00              | 1000.00         |                            | 100.00                     |                     | 1000.00         |                          | 100.00                   |
      | 1         | 02 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 2         | 03 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 3         | 04 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 4         | 05 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 5         | 06 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 6         | 07 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 7         | 08 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 8         | 09 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 9         | 10 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 10        | 11 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 11        | 12 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 12        | 13 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 13        | 14 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 14        | 15 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 15        | 16 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 16        | 17 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 17        | 18 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 18        | 19 January 2026  | 50.00                 | 958.45          | 8.45                       | 91.55                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 19        | 20 January 2026  | 55.56                 | 953.78          | 9.34                       | 90.66                      | 0.00                | 1000.00         | 0.00                     | 100.00                   |
      | 20        | 21 January 2026  | 55.56                 | 953.78          | 9.34                       | 90.66                      | 55.56               | 953.79          | 9.35                     | 90.65                    |
      | 21        | 22 January 2026  | 55.56                 | 907.14          | 8.91                       | 81.74                      |                     |                 |                          |                          |
      | 22        | 23 January 2026  | 55.56                 | 860.05          | 8.47                       | 73.27                      |                     |                 |                          |                          |
      | 23        | 24 January 2026  | 55.56                 | 812.52          | 8.03                       | 65.24                      |                     |                 |                          |                          |
      | 24        | 25 January 2026  | 55.56                 | 764.55          | 7.59                       | 57.65                      |                     |                 |                          |                          |
      | 25        | 26 January 2026  | 55.56                 | 716.13          | 7.14                       | 50.51                      |                     |                 |                          |                          |
      | 26        | 27 January 2026  | 55.56                 | 667.26          | 6.69                       | 43.82                      |                     |                 |                          |                          |
      | 27        | 28 January 2026  | 55.56                 | 617.94          | 6.23                       | 37.59                      |                     |                 |                          |                          |
      | 28        | 29 January 2026  | 55.56                 | 568.15          | 5.77                       | 31.82                      |                     |                 |                          |                          |
      | 29        | 30 January 2026  | 55.56                 | 517.89          | 5.31                       | 26.51                      |                     |                 |                          |                          |
      | 30        | 31 January 2026  | 55.56                 | 467.17          | 4.84                       | 21.67                      |                     |                 |                          |                          |
      | 31        | 01 February 2026 | 55.56                 | 415.97          | 4.36                       | 17.31                      |                     |                 |                          |                          |
      | 32        | 02 February 2026 | 55.56                 | 364.30          | 3.89                       | 13.42                      |                     |                 |                          |                          |
      | 33        | 03 February 2026 | 55.56                 | 312.14          | 3.40                       | 10.02                      |                     |                 |                          |                          |
      | 34        | 04 February 2026 | 55.56                 | 259.50          | 2.92                       | 7.10                       |                     |                 |                          |                          |
      | 35        | 05 February 2026 | 55.56                 | 206.36          | 2.42                       | 4.68                       |                     |                 |                          |                          |
      | 36        | 06 February 2026 | 55.56                 | 152.73          | 1.93                       | 2.75                       |                     |                 |                          |                          |
      | 37        | 07 February 2026 | 55.56                 | 98.60           | 1.43                       | 1.32                       |                     |                 |                          |                          |
      | 38        | 08 February 2026 | 44.36                 | 55.16           | 0.80                       | 0.52                       |                     |                 |                          |                          |
      | 39        | 09 February 2026 | 55.56                 | 0.12            | 0.52                       | 0.00                       |                     |                 |                          |                          |
      | 40        | 10 February 2026 | 0.12                  | 0.00            | 0.00                       | 0.00                       |                     |                 |                          |                          |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 21 January 2026 | Repayment                 | 55.56             | 55.56            | 0.0               | 0.0                   | false    |
    Then Admin closes the Working Capital loan with a full repayment on "21 January 2026"

  @TestRailId:C94032
  Scenario: Verify Working Capital period payment rate change forbidden after maturity date  - UC23
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 100      |
    When Admin sets the business date to "01 February 2026"
    And Working Capital Loan has transactions:
      | transactionDate | type           | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement   | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee   | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
    And Admin update Working Capital period payment rate failed with "20" value on "01 February 2026" date cause after maturity date
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 1100.0    | 1000.0            | 100000.0           | 18.0              | 100.0    |
    Then Admin closes the Working Capital loan with a full repayment on "01 February 2026"

  @TestRailId:C98201
  Scenario: Verify Working Capital period payment rate change submitted on date follows the business date and stays immutable - UC24
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    #--- submitted on date is stamped with the business date in force at creation ---#
    When Admin sets the business date to "10 January 2026"
    And Admin update Working Capital period payment rate with "12.5" value
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed | Submitted On Date |
      | 10 January 2026 | 1.0           | 12.5     | false    | 10 January 2026   |
    #--- a later business date stamps only the new record, the earlier one is immutable ---#
    When Admin sets the business date to "20 January 2026"
    And Admin update Working Capital period payment rate with "15" value
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed | Submitted On Date |
      | 10 January 2026 | 1.0           | 12.5     | false    | 10 January 2026   |
      | 20 January 2026 | 12.5          | 15.0     | false    | 20 January 2026   |
    Then Admin closes the Working Capital loan with a full repayment on "20 January 2026"
