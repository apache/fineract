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
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 01 January 2026 | 1.0           | 12.5     | true     |
      | 01 January 2026 | 12.5          | 19.38    | false    |
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
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date  | Previous Rate | New Rate | Reversed |
      | 01 January 2026 | 1.0           | 12.5     | true     |
      | 15 January 2026 | 12.5          | 19.38    | false    |
#--- update period payment rate ---#
    When Admin sets the business date to "25 February 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin update Working Capital period payment rate with "18.09" value
    Then Working Capital Loan Period Payment Rate changes history contains the following data:
      | Effective Date   | Previous Rate | New Rate | Reversed |
      | 01 January 2026  | 1.0           | 12.5     | true     |
      | 15 January 2026  | 12.5          | 19.38    | true     |
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
