@WorkingCapital
@WorkingCapitalDiscountFeature
Feature: Working Capital Discount - Part2

  @TestRailId:C102546
  Scenario: Discount fee referencing a non-existent transaction id is rejected as not found - UC19
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Adding Discount fee with "1000" amount referencing a non-existent transaction id on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                             |
      | 400                | validation.msg.wc.loan.disbursement.transaction.not.found |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
    And Working capital loan account has the correct data:
      | status | principal | discount | totalDiscountFee |
      | Active | 9000.0    | null     | 0.0              |

  @TestRailId:C102547
  Scenario: Second discount fee via discount fee is rejected as the discount is loan-scoped, and the single fee stays consistent through repayments and COB - UC20
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    Then Adding Discount fee with "1000" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                                   |
      | 400                | validation.msg.wc.loan.discount.already.set.before.disbursement |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
    And Working capital loan account has the correct data:
      | status | principal | discount | totalDiscountFee | realizedIncome | unrealizedIncome |
      | Active | 10000.0   | 1000.0   | 1000.0           | 0.0            | 1000.0           |
    When Customer makes repayment on "01 January 2026" with 50 transaction amount on Working Capital loan
    And Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 19.18             |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | status | principal | totalPaidPrincipal | discount | totalDiscountFee | realizedIncome | unrealizedIncome |
      | Active | 10000.0   | 100.0              | 1000.0   | 1000.0           | 19.18          | 980.82           |

  @TestRailId:C110982
  Scenario: Discount fee added by disbursement id in the path on a later business date is backdated to the disbursement date and reversed by undo disbursal
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "08 January 2026"
    And Admin adds Discount fee with "1000" amount by loan id and disbursement id on Working Capital loan account
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
    And Working capital loan account has the correct data:
      | status | principal | discount | totalDiscountFee | realizedIncome | unrealizedIncome |
      | Active | 10000.0   | 1000.0   | 1000.0           | 0.0            | 1000.0           |
    And WorkingCapitalLoanDiscountFeeTransactionBusinessEvent is raised with amount "1000" on "01 January 2026" date
    And Working Capital Loan Transactions tab has a "DISCOUNT_FEE" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
    When Admin successfully undo Working Capital disbursal
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed | reversedOnDate  |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | true     | 08 January 2026 |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | true     | 08 January 2026 |
    And Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |

  @TestRailId:C110983
  Scenario: Discount fee added by loan external-id and disbursement external resource id on a later business date is backdated to the disbursement date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin sets the business date to "08 January 2026"
    And Admin adds Discount fee with "1000" amount by loan external-id and the disbursement external resource id on Working Capital loan account
    Then WorkingCapitalLoanDiscountFeeTransactionBusinessEvent is raised with amount "1000" on "01 January 2026" date
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
    And Working capital loan account has the correct data:
      | status | principal | discount | totalDiscountFee | realizedIncome | unrealizedIncome |
      | Active | 10000.0   | 1000.0   | 1000.0           | 0.0            | 1000.0           |

  @TestRailId:C110984
  Scenario: Discount fee backdated to a disbursement dated before the branch's latest accounting closure date is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "08 January 2026"
    And Admin closes accounting for the last created office on "05 January 2026"
    Then Adding Discount fee with "1000" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                      |
      | 403                | error.msg.glJournalEntry.invalid.accounting.closed |
    Then Adding Discount fee with "1000" amount on transaction date "01 January 2026" on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                      |
      | 403                | error.msg.glJournalEntry.invalid.accounting.closed |
    And Working capital loan account has the correct data:
      | principal | discount |
      | 9000.0    | null     |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |

  @TestRailId:C110985
  Scenario: Discount fee added by date after undo disbursal and re-disbursement on another date belongs to the active disbursement, the reversed disbursement date is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin sets the business date to "08 January 2026"
    And Admin successfully undo Working Capital disbursal
    And Admin successfully disburse the Working Capital loan on "04 January 2026" with "9000" EUR transaction amount
    Then Adding Discount fee with "1000" amount on transaction date "01 January 2026" on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                      |
      | 400                | disbursement.transaction.not.found |
    Then Adding Discount fee with "1000" amount on transaction date "01 January 2026" on Working Capital loan account for last disbursement results an error with the following data:
      | HTTP response code | Error message                                    |
      | 400                | transaction.date.must.be.equal.disbursement.date |
    When Admin adds Discount fee with "1000" amount on transaction date "04 January 2026" on Working Capital loan account
    Then WorkingCapitalLoanDiscountFeeTransactionBusinessEvent is raised with amount "1000" on "04 January 2026" date
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | true     |
      | 04 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "DISCOUNT_FEE" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |

  @TestRailId:C110986
  Scenario: Backdated discount fee on a later business date is rejected on a charged-off and on a closed Working Capital loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin sets the business date to "05 January 2026"
    And Admin charges off the Working Capital loan on "05 January 2026"
    And Admin sets the business date to "08 January 2026"
    Then Adding Discount fee with "1000" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                    |
      | 403                | error.msg.wc.loan.is.charged.off |
    Then Adding Discount fee with "1000" amount on transaction date "01 January 2026" on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                    |
      | 403                | error.msg.wc.loan.is.charged.off |
    And Working capital loan account has the correct data:
      | principal | discount | chargedOff |
      | 9000.0    | null     | true       |
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 9000 transaction amount on Working Capital loan
    And Admin sets the business date to "08 January 2026"
    Then Adding Discount fee with "1000" amount on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                  |
      | 400                | validation.msg.wc.loan.transition.not.allowed |
    Then Adding Discount fee with "1000" amount on transaction date "01 January 2026" on Working Capital loan account results an error with the following data:
      | HTTP response code | Error message                                  |
      | 400                | validation.msg.wc.loan.transition.not.allowed |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |

  @TestRailId:C110987
  Scenario: Discount fee backdated across a month end is dated on the disbursement date and its amortization catches up on the next COB
    When Admin sets the business date to "28 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 28 January 2026 | 28 January 2026          | 9000            | 100000             | 18                | 0        |
    When Admin sets the business date to "30 January 2026"
    And Customer makes repayment on "30 January 2026" with 150 transaction amount on Working Capital loan
    And Admin sets the business date to "03 February 2026"
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    And Admin sets the business date to "04 February 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Working Capital Loan has transactions:
      | transactionDate  | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 28 January 2026  | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 28 January 2026  | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 30 January 2026  | Repayment                 | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
      | 03 February 2026 | Discount Fee Amortization | 28.7              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 150.0              | 28.7           | 971.3            | 0.0               |
    And Working Capital Loan Transactions tab has a "DISCOUNT_FEE" transaction with date "28 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
    And Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "03 February 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 28.7   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 28.7  |        |
