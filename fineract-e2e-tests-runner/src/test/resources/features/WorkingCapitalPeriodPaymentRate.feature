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
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 12.5              | null     |

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
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 12.5              | null     |

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
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 19.38             | null     |

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
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 18.09             | null     |

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
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 15.0              | null     |

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
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 100.0              | 12.5              | null     |

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
# The loan still bills the latest rate in force today, which the backdated change sits behind.
    And Working Capital Loan period payment rate is "20"
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

  Scenario: Verify a backdated period payment rate change leaves the periods before it untouched - UC13
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

  Scenario: Verify a future-dated period payment rate change leaves the current rate untouched until its date - UC11
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
#--- Recorded but not yet in force: the loan's rate stays 18 while the change is still in the future.
    And Working Capital Loan period payment rate is "18"
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
#--- the effective date arrives: COB brings the loan's rate up to date ---#
    When Admin sets the business date to "01 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan period payment rate is "11"

  Scenario: Verify a same-date period payment rate change overwrites the earlier one for that date only - UC12
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
    And Working Capital Loan period payment rate is "20"
# The correction is scoped to 10 January onwards: period 8 is untouched, period 9 moves from the mistaken 30.56 to
# 47.22, and the 20 January segment keeps its rate but is re-derived from the balance the corrected segment leaves.
    And Admin retrieves the projected amortization schedule
    And The retrieved amortization schedule has payments with the following details for the listed payment numbers:
      | paymentNo | date       | expectedPaymentAmount | expectedBalance | expectedAmortizationAmount | expectedDiscountFeeBalance |
      | 8         | 2026-01-09 | 50.00                 | 8675.67         | 9.31                       | 924.33                     |
      | 9         | 2026-01-10 | 47.22                 | 8637.20         | 8.75                       | 915.58                     |
      | 19        | 2026-01-20 | 55.56                 | 8243.50         | 9.83                       | 828.74                     |
