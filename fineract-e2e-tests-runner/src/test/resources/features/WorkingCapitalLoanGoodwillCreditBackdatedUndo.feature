@WorkingCapital
@WorkingCapitalLoanGoodwillCreditBackdatedUndoFeature
Feature: Working Capital Loan Goodwill Credit Backdated and Undo

  @TestRailId:C85532
  Scenario: Verify Working Capital Goodwill Credit backdated/undo - UC1: Verify Working Capital Goodwill Credit backdated/undo - UC1: Backdated goodwill credit clears a delinquent period
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | false                 | 270.0            | 1              |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    When Customer makes "GOODWILL_CREDIT" transaction on "15 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Goodwill Credit | 270.0             | 270.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C85533
  Scenario: Verify Working Capital Goodwill Credit backdated/undo - UC2: Backdated goodwill credit and its undo recalculate the delinquency and breach schedules
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | null   |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Customer makes "GOODWILL_CREDIT" transaction on "05 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 0.0               | null       | false  |
    When Customer undo "1"th "GOODWILL_CREDIT" transaction made on "05 January 2026" on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |
    And Working Capital loan breach schedule has the following data:
      | periodNumber | fromDate   | toDate     | numberOfDays | minPaymentAmount | outstandingAmount | nearBreach | breach |
      | 1            | 2026-01-01 | 2026-02-28 | 59           | 110.70           | 110.70            | null       | null   |

  @TestRailId:C85534
  Scenario: Verify Working Capital Goodwill Credit backdated/undo - UC3: Backdated goodwill credit settling the full balance closes the loan and recognizes the discount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin successfully add discount with "1000" amount on Working Capital loan account
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "05 January 2026" with 10000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital loan balance payload contains the following fields:
      | field                | value   |
      | principalOutstanding | 0.0     |
      | overpaymentAmount    | 0.0     |
      | totalPaidPrincipal   | 10000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | false    |
      | 05 January 2026 | Goodwill Credit           | 10000.0           | false    |
      | 05 January 2026 | Discount Fee Amortization | 1000.0            | false    |

  @TestRailId:C85535
  Scenario: Verify Working Capital Goodwill Credit backdated/undo - UC4: Backdated goodwill credit beyond the outstanding balance moves the loan to overpaid
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "05 January 2026" with 9500.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 0.0    |
      | overpaymentAmount    | 500.0  |
      | totalPaidPrincipal   | 9000.0 |

  @TestRailId:C85536
  Scenario: Verify Working Capital Goodwill Credit backdated/undo - UC5: Goodwill credit posted on an already overpaid loan increases the overpayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "05 January 2026" with 9500.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "06 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "06 January 2026" with 300.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 0.0    |
      | overpaymentAmount    | 800.0  |
      | totalPaidPrincipal   | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Goodwill Credit | 9500.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 06 January 2026 | Goodwill Credit | 300.0             | 0.0              | 0.0               | 0.0                   | false    |

  @TestRailId:C85537
  Scenario: Verify Working Capital Goodwill Credit backdated/undo - UC6: Undoing a goodwill credit posts a discount fee amortization adjustment on the next COB
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin successfully add discount with "1000" amount on Working Capital loan account
    When Admin sets the business date to "02 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "02 January 2026" with 3000.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | false    |
      | 02 January 2026 | Goodwill Credit           | 3000.0            | false    |
      | 02 January 2026 | Discount Fee Amortization | 498.67            | false    |
    When Customer undo "1"th "GOODWILL_CREDIT" transaction made on "02 January 2026" on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | false    |
      | 02 January 2026 | Goodwill Credit                      | 3000.0            | true     |
      | 02 January 2026 | Discount Fee Amortization            | 498.67            | false    |
      | 03 January 2026 | Discount Fee Amortization Adjustment | 498.67            | false    |
    And Working Capital loan balance payload contains the following fields:
      | field          | value |
      | realizedIncome | 0.0   |

  @TestRailId:C85538
  Scenario: Verify Working Capital Goodwill Credit backdated/undo - UC7: Undoing the closing goodwill credit re-opens the loan from closed to active
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "05 January 2026" with 9000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "06 January 2026"
    And Customer undo "1"th "GOODWILL_CREDIT" transaction made on "05 January 2026" on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 9000.0 |
      | overpaymentAmount    | 0.0    |
      | totalPaidPrincipal   | 0.0    |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Goodwill Credit | 9000.0            | 9000.0           | 0.0               | 0.0                   | true     |

  @TestRailId:C85539
  Scenario: Verify Working Capital Goodwill Credit backdated/undo - UC8: Undoing a goodwill credit that overpaid a closed loan returns it to closed
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 9000 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "06 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "06 January 2026" with 500.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    When Admin sets the business date to "07 January 2026"
    And Customer undo "1"th "GOODWILL_CREDIT" transaction made on "06 January 2026" on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 0.0    |
      | overpaymentAmount    | 0.0    |
      | totalPaidPrincipal   | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 06 January 2026 | Goodwill Credit | 500.0             | 0.0              | 0.0               | 0.0                   | true     |

  @TestRailId:C85540
  Scenario: Verify Working Capital Goodwill Credit backdated/undo - UC9: Goodwill credit dated before the disbursement date is rejected (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 05 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "05 January 2026"
    When Admin sets the business date to "05 January 2026"
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    Then Initiating a "GOODWILL_CREDIT" transaction on "03 January 2026" with 270.0 transaction amount on Working Capital loan results an error with the following data:
      | httpCode | errorMessage                                                    |
      | 400      | Failed data validation due to: cannot.be.before.disbursal.date. |

  @TestRailId:C85541
  Scenario: Verify Working Capital Goodwill Credit backdated/undo - UC10: Backdated goodwill credit on a loan with a fee re-allocates the later transaction's fee to principal
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with payment allocation order:
      | DUE_PENALTY          |
      | DUE_FEE              |
      | DUE_PRINCIPAL        |
      | IN_ADVANCE_PENALTY   |
      | IN_ADVANCE_FEE       |
      | IN_ADVANCE_PRINCIPAL |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 5.0 transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 30.0 transaction amount on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Goodwill Credit | 30.0              | 25.0             | 5.0               | 0.0                   | false    |
    When Admin sets the business date to "15 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "05 January 2026" with 25.0 transaction amount on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Goodwill Credit | 25.0              | 20.0             | 5.0               | 0.0                   | false    |
      | 10 January 2026 | Goodwill Credit | 30.0              | 30.0             | 0.0               | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Paid | Fee Outstanding |
      | 5.0        | 5.0      | 0.0             |

  @TestRailId:C85542
  Scenario: Verify Working Capital Goodwill Credit backdated/undo - UC11: Undoing a goodwill credit on a loan with a fee re-allocates the remaining transaction's fee portion
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with payment allocation order:
      | DUE_PENALTY          |
      | DUE_FEE              |
      | DUE_PRINCIPAL        |
      | IN_ADVANCE_PENALTY   |
      | IN_ADVANCE_FEE       |
      | IN_ADVANCE_PRINCIPAL |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 5.0 transaction amount
    When Admin sets the business date to "05 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "05 January 2026" with 30.0 transaction amount on Working Capital loan
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 25.0 transaction amount on Working Capital loan
    When Customer undo "1"th "GOODWILL_CREDIT" transaction made on "05 January 2026" on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Goodwill Credit | 30.0              | 25.0             | 5.0               | 0.0                   | true     |
      | 10 January 2026 | Goodwill Credit | 25.0              | 20.0             | 5.0               | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Paid | Fee Outstanding |
      | 5.0        | 5.0      | 0.0             |
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 8980.0 |
      | totalPaidPrincipal   | 20.0   |

  @TestRailId:C85543
  Scenario: Verify Working Capital Goodwill Credit backdated/undo - UC12: Backdated goodwill credit posted on a closed loan reprocesses the payments and moves it to overpaid
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 9000 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "15 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "05 January 2026" with 500.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 0.0    |
      | overpaymentAmount    | 500.0  |
      | totalPaidPrincipal   | 9000.0 |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Goodwill Credit | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment       | 9000.0            | 8500.0           | 0.0               | 0.0                   | false    |

  @TestRailId:C85544
  Scenario: Verify Working Capital Goodwill Credit backdated/undo - UC13: Goodwill credit settling the remaining due fee closes the loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with payment allocation order:
      | DUE_PRINCIPAL        |
      | IN_ADVANCE_PRINCIPAL |
      | DUE_PENALTY          |
      | IN_ADVANCE_PENALTY   |
      | DUE_FEE              |
      | IN_ADVANCE_FEE       |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 35.0 transaction amount
    And Customer makes repayment on "05 January 2026" with 9000 transaction amount on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Paid | Fee Outstanding |
      | 35.0       | 0.0      | 35.0            |
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 35.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 0.0    |
      | overpaymentAmount    | 0.0    |
      | totalPaidPrincipal   | 9000.0 |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Paid | Fee Outstanding |
      | 35.0       | 35.0     | 0.0             |
    And Working Capital Loan has transactions:
      | transactionDate | type            | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Goodwill Credit | 35.0              | 0.0              | 35.0              | 0.0                   | false    |
