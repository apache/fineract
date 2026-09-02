@WorkingCapital
@WorkingCapitalLoanChargeOffFeature
Feature: Working Capital Loan Charge-off

  @TestRailId:C93923
  Scenario: Verify Working Capital Charge-off transactions - UC1: simple charge-off transaction
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status   | proposedPrincipal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | chargedOff | chargedOffOnDate | chargeOffReason.name |
      | WCLP         | 2026-01-01      | 2026-01-01               | Approved | 100.0             | 100.0             | 1000.0             | 18.0              | null     | false      | null             | null                 |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | chargedOff | chargedOffOnDate | chargeOffReason.name |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 1000.0             | 18.0              | null     | false      | null             | null                 |
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin charges off the Working Capital loan on "15 January 2026"
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | chargedOff | chargedOffOnDate | chargeOffReason.name |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 1000.0             | 18.0              | null     | true       | 2026-01-15       | null                 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
    And In Working Capital Loan Transactions all transactions have non-blank external-id

  @TestRailId:C93924
  Scenario: Verify Working Capital Charge-off transactions - UC2: charge-off with a reason
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Given A code value "Fraud" exists for code name "ChargeOffReasons"
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin charges off the Working Capital loan on "15 January 2026" with charge-off reason "Fraud"
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | chargedOff | chargedOffOnDate | chargeOffReason.name |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 1000.0             | 18.0              | null     | true       | 2026-01-15       | Fraud                |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 100.0             | 100.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C93925
  Scenario: Verify Working Capital Charge-off transactions - UC3: charge-off with a note
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Given A code value "Fraud" exists for code name "ChargeOffReasons"
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin charges off the Working Capital loan on "15 January 2026" with charge-off reason "Fraud" and note "Customer defaulted"
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | chargedOff | chargedOffOnDate | chargeOffReason.name |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 1000.0             | 18.0              | null     | true       | 2026-01-15       | Fraud                |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 100.0             | 100.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C93926
  Scenario: Verify Working Capital Charge-off transactions - UC4: charge-off date cannot be before the last transaction (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Customer makes "REPAYMENT" transaction on "10 January 2026" with 30.0 transaction amount on Working Capital loan
    Then Initiating a charge-off on the Working Capital loan on "05 January 2026" results an error with the following data:
      | httpCode | message                                |
      | 400      | cannot.be.before.last.transaction.date |
    When Admin charges off the Working Capital loan on "10 January 2026"
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | chargedOff | chargedOffOnDate | chargeOffReason.name |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 1000.0             | 18.0              | null     | true       | 2026-01-10       | null                 |
    And Working Capital loan balance principalOutstanding is "70.0"
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 30.0              | 30.0             | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge-off   | 70.0              | 70.0             | 0.0               | 0.0                   | false    |

  @TestRailId:C93927
  Scenario: Verify Working Capital Charge-off transactions - UC5: charge-off cannot be a future date (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Initiating a charge-off on the Working Capital loan on "02 January 2026" results an error with the following data:
      | httpCode | message                 |
      | 400      | cannot.be.a.future.date |

  @TestRailId:C93928
  Scenario: Verify Working Capital Charge-off transactions - UC6: cannot charge-off an already charged-off loan (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin charges off the Working Capital loan on "15 January 2026"
    And Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating a charge-off on the Working Capital loan on "16 January 2026" results an error with the following data:
      | httpCode | message                                  |
      | 400      | error.msg.wc.loan.is.already.charged.off |

  @TestRailId:C93929
  Scenario: Verify Working Capital Charge-off transactions - UC7: undo charge-off transaction
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin charges off the Working Capital loan on "15 January 2026"
    And Admin undoes the charge-off on the Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | chargedOff | chargedOffOnDate | chargeOffReason.name |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 1000.0             | 18.0              | null     | false      | null             | null                 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 100.0             | 100.0            | 0.0               | 0.0                   | true     |

  @TestRailId:C93930
  Scenario: Verify Working Capital Charge-off transactions - UC8: cannot undo charge-off when a repayment is posted after the charge-off (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin charges off the Working Capital loan on "10 January 2026"
    And Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "REPAYMENT" transaction on "11 January 2026" with 30.0 transaction amount on Working Capital loan
    Then Initiating an undo of the charge-off on the Working Capital loan results an error with the following data:
      | httpCode | message                                                       |
      | 403      | error.msg.wc.loan.charge.off.is.not.the.last.user.transaction |

  @TestRailId:C93931
  Scenario: Verify Working Capital Charge-off transactions - UC9: charge-off with externalId
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin charges off the Working Capital loan on "15 January 2026" with a random externalId
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | chargedOff | chargedOffOnDate | chargeOffReason.name |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 1000.0             | 18.0              | null     | true       | 2026-01-15       | null                 |
    And Working Capital Loan transaction of type "Charge-off" on "15 January 2026" has a non-blank externalId

  @TestRailId:C93932
  Scenario: Verify Working Capital Charge-off transactions - UC10: backdated charge-off success
    When Admin sets the business date to "15 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin charges off the Working Capital loan on "10 January 2026"
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | chargedOff | chargedOffOnDate | chargeOffReason.name |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 1000.0             | 18.0              | null     | true       | 2026-01-10       | null                 |

  @TestRailId:C93933
  Scenario: Verify Working Capital Charge-off transactions - UC11: adding charges after charge-off isn't allowed (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin charges off the Working Capital loan on "15 January 2026"
    And Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Initiating adding "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 January 2026" due date and 10.0 transaction amount results an error with the following data:
      | httpCode | message                          |
      | 403      | error.msg.wc.loan.is.charged.off |

  @TestRailId:C93934
  Scenario: Verify Working Capital Charge-off transactions - UC12: undo charge-off on a not charged off loan returns validation error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Initiating an undo of the charge-off on the Working Capital loan results an error with the following data:
      | httpCode | message                              |
      | 400      | error.msg.wc.loan.is.not.charged.off |

  @TestRailId:C93935
  Scenario: Verify Working Capital Charge-off transactions - UC13: repayment after charge-off works and reduces balance
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin charges off the Working Capital loan on "15 January 2026"
    And Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "REPAYMENT" transaction on "16 January 2026" with 30.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | chargedOff | chargedOffOnDate | chargeOffReason.name |
      | WCLP         | 2026-01-01      | 2026-01-01               | Active | 100.0     | 100.0             | 1000.0             | 18.0              | null     | true       | 2026-01-15       | null                 |
    And Working Capital loan balance principalOutstanding is "70.0"
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 16 January 2026 | Repayment    | 30.0              | 30.0             | 0.0               | 0.0                   | false    |

  @TestRailId:C93936
  Scenario: Verify Working Capital Charge-off transactions - UC14: charged-off flag persists after full repayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin charges off the Working Capital loan on "15 January 2026"
    And Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "REPAYMENT" transaction on "16 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | chargedOff | chargedOffOnDate | chargeOffReason.name |
      | WCLP         | 2026-01-01      | 2026-01-01               | 100.0     | 100.0             | 1000.0             | 18.0              | null     | true       | 2026-01-15       | null                 |
    And Working Capital loan balance principalOutstanding is "0.0"
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 16 January 2026 | Repayment    | 100.0             | 100.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C93937
  Scenario: Verify Working Capital Charge-off transactions - UC15: post-charge-off repayment posts recovery income GL entries
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin charges off the Working Capital loan on "15 January 2026"
    And Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes "REPAYMENT" transaction on "16 January 2026" with 100.0 transaction amount on Working Capital loan
    # While the loan is charged off the repayment credit is recognized as recovery income (Cr Recoveries) instead of
    # reducing the portfolio (Cr Loans Receivable); the debit stays on the fund source (Suspense/Clearing account).
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "16 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |
      | INCOME    | 744008       | Recoveries                |       | 100.0  |

  @TestRailId:C93938
  Scenario: Verify Working Capital Charge-off transactions - UC16: cannot undo charge-off when a repayment is posted on the same day after the charge-off (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin charges off the Working Capital loan on "15 January 2026"
    # Same-day activity: the repayment shares the charge-off date but sorts after it by id, so the charge-off is no
    # longer the last user transaction. A plain date comparison would miss this; ordering by (date, id) catches it.
    And Customer makes "REPAYMENT" transaction on "15 January 2026" with 30.0 transaction amount on Working Capital loan
    Then Initiating an undo of the charge-off on the Working Capital loan results an error with the following data:
      | httpCode | message                                                       |
      | 403      | error.msg.wc.loan.charge.off.is.not.the.last.user.transaction |

  @TestRailId:C93939
  Scenario: Verify Working Capital Charge-off transactions - UC17: charge-off template returns outstanding amount, business date, currency and reason options
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Given A code value "Fraud" exists for code name "ChargeOffReasons"
    And Admin sets the business date to "10 January 2026"
    When Admin retrieves the working capital loan action template with templateType "chargeOff"
    # chargeOffAmount is the current outstanding balance; chargeOffDate defaults to the business date.
    Then The working capital loan charge-off template has the following data:
      | chargeOffAmount | chargeOffDate | currency | chargeOffReasonOptionsPresent |
      | 100.0           | 2026-01-10    | EUR      | true                          |

  @TestRailId:C93940
  Scenario: Verify Working Capital Charge-off transactions - UC18: cannot undo charge-off when a charge adjustment is posted on the same day after the charge-off (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2026" due date and 10.0 transaction amount
    When Admin charges off the Working Capital loan on "15 January 2026"
    And Admin makes a charge adjustment for the last added fee charge with 10.0 amount on working capital loan
    Then Initiating an undo of the charge-off on the Working Capital loan results an error with the following data:
      | httpCode | message                                                       |
      | 403      | error.msg.wc.loan.charge.off.is.not.the.last.user.transaction |

  @TestRailId:C94064
  Scenario: Working Capital loan raises Charge Off account and transaction business events on charge-off
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin charges off the Working Capital loan on "15 January 2026" with charge-off reason "Fraud"
    Then a Working Capital Loan Charge Off business event is raised with "15 January 2026" charge off date
    And a Working Capital Loan Charge Off transaction business event is raised with "100.0" EUR amount
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:C98192
  Scenario: Working Capital loan raises an Undo Charge Off business event when the charge-off is reverted
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin charges off the Working Capital loan on "15 January 2026" with charge-off reason "Fraud"
    Then a Working Capital Loan Charge Off business event is raised with "15 January 2026" charge off date
    When Admin undoes the charge-off on the Working Capital loan
    Then a Working Capital Loan Undo Charge Off business event is raised
    And a Working Capital Loan Adjust Transaction business event is raised for the reversed "chargeOff" transaction
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"

  @TestRailId:C98193
  Scenario: Working Capital loan raises an Undo Charge Off business event when a backdated repayment lifts the charge-off
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 1000               | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Balance Changed business event is raised
    Given A code value "Fraud" exists for code name "ChargeOffReasons"
    When Admin sets the business date to "15 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin charges off the Working Capital loan on "15 January 2026" with charge-off reason "Fraud"
    Then a Working Capital Loan Charge Off business event is raised with "15 January 2026" charge off date
    When Customer makes "REPAYMENT" transaction on "10 January 2026" with 40.0 transaction amount on Working Capital loan
    Then a Working Capital Loan Adjust Transaction business event is raised for the "chargeOff" transaction on "15 January 2026" with principal portion changed from "100.0" to "60.0" and fee portion changed from "0.0" to "0.0"
    When Customer makes "REPAYMENT" transaction on "10 January 2026" with 60.0 transaction amount on Working Capital loan
    Then a Working Capital Loan Undo Charge Off business event is raised
    And a Working Capital Loan Adjust Transaction business event is raised for the reversed "chargeOff" transaction
    And Working capital loan account has the correct data:
      | product.name | submittedOnDate | expectedDisbursementDate | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount | chargedOff | chargedOffOnDate | chargeOffReason.name |
      | WCLP         | 2026-01-01      | 2026-01-01               | 100.0     | 100.0             | 1000.0             | 18.0              | null     | false      | null             | null                 |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 40.0              | 40.0             | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 60.0              | 60.0             | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Charge-off   | 60.0              | 60.0             | 0.0               | 0.0                   | true     |
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
