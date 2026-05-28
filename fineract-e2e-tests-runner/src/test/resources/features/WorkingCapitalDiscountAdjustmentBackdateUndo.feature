Feature: Working Capital Discount Adjustment Backdated and Undo

  Scenario: Backdated discount adjustment keeps the principal already repaid before the adjustment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully update discount with "1000" amount on Working Capital loan account
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Admin adds Discount fee adjustment with "1000" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | discount | principal |
      | 0.0      | 8900.0    |
    And Working Capital Loan has transactions:
      | transactionDate | type                    | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement            | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee            | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment               | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment               | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |

  Scenario: Undo of a backdated discount adjustment restores the discount and reverses the adjustment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully update discount with "1000" amount on Working Capital loan account
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    When Admin sets the business date to "08 January 2026"
    And Admin adds Discount fee adjustment with "1000" amount on transaction date "05 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | discount | principal |
      | 0.0      | 9000.0    |
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    And Working capital loan account has the correct data:
      | discount | principal |
      | 1000.0   | 10000.0   |
    And Working Capital Loan has transactions:
      | transactionDate | type                    | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement            | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee            | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Adjustment | 1000.0            | 1000.0           | 0.0               | 0.0                   | true     |

  Scenario: Partial discount adjustment can be backdated
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully update discount with "1000" amount on Working Capital loan account
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    When Admin sets the business date to "05 January 2026"
    And Admin adds Discount fee adjustment with "400" amount on transaction date "03 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | discount | principal |
      | 600.0    | 9600.0    |
    And Working Capital Loan has transactions:
      | transactionDate | type                    | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement            | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee            | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Adjustment | 400.0             | 400.0            | 0.0               | 0.0                   | false    |

  Scenario: Backdated discount adjustment is rejected when dated before the discount fee
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "10 January 2026"
    Then Add Discount fee adjustment with "2" amount and transaction date "31 December 2025" on Working Capital loan account failed due to transaction date before discount fee date

  Scenario: Backdated discount adjustment posts amortization adjustment as of current business date for income recognized up to the previous COB
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully update discount with "1000" amount on Working Capital loan account
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Customer makes repayment on "04 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "06 January 2026"
    And Customer makes repayment on "06 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "07 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Admin adds Discount fee adjustment with "1000" amount on transaction date "05 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | discount | principal |
      | 0.0      | 8750.0    |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 06 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 06 January 2026 | Discount Fee Amortization            | 47.62             |                  |                   |                       | false    |
      | 05 January 2026 | Discount Fee Adjustment              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 07 January 2026 | Discount Fee Amortization Adjustment | 47.62             |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "07 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           | 47.62 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 47.62  |

  Scenario: Undo of a backdated discount adjustment reverses the linked amortization adjustment and posts no new amortization when nothing changed in between
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully update discount with "1000" amount on Working Capital loan account
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Customer makes repayment on "04 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Admin adds Discount fee adjustment with "1000" amount on transaction date "05 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | discount | principal |
      | 0.0      | 8850.0    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           | 28.70 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 28.70  |
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    And Working capital loan account has the correct data:
      | discount | principal |
      | 1000.0   | 9850.0    |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization            | 28.70             |                  |                   |                       | false    |
      | 05 January 2026 | Discount Fee Adjustment              | 1000.0            | 1000.0           | 0.0               | 0.0                   | true     |
      | 05 January 2026 | Discount Fee Amortization Adjustment | 28.70             |                  |                   |                       | true     |
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           | 28.70 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 28.70  |
      | LIABILITY | 240005       | Deferred Interest Revenue | 28.70 |        |
      | INCOME    | 404000       | Interest Income           |       | 28.70  |

  Scenario: Multiple backdated discount fee adjustments are allowed; undo of the last one restores its share of the discount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully update discount with "1000" amount on Working Capital loan account
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    When Admin sets the business date to "05 January 2026"
    And Admin adds Discount fee adjustment with "400" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | discount | principal |
      | 600.0    | 9600.0    |
    When Admin sets the business date to "06 January 2026"
    And Admin adds Discount fee adjustment with "600" amount on transaction date "03 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | discount | principal |
      | 0.0      | 9000.0    |
    And Working Capital Loan has transactions:
      | transactionDate | type                    | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement            | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee            | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment | 400.0             | 400.0            | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Adjustment | 600.0             | 600.0            | 0.0               | 0.0                   | false    |
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    And Working capital loan account has the correct data:
      | discount | principal |
      | 600.0    | 9600.0    |
    And Working Capital Loan has transactions:
      | transactionDate | type                    | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement            | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee            | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment | 400.0             | 400.0            | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Adjustment | 600.0             | 600.0            | 0.0               | 0.0                   | true     |

  Scenario: Undo of a deeply-backdated adjustment reverses only its linked amortization adjustment and preserves pre-existing COB income
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully update discount with "1000" amount on Working Capital loan account
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Customer makes repayment on "04 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Admin adds Discount fee adjustment with "1000" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | discount | principal |
      | 0.0      | 8850.0    |
    When Admin sets the business date to "06 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "07 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    And Working capital loan account has the correct data:
      | discount | principal |
      | 1000.0   | 9850.0    |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization            | 28.70             |                  |                   |                       | false    |
      | 02 January 2026 | Discount Fee Adjustment              | 1000.0            | 1000.0           | 0.0               | 0.0                   | true     |
      | 05 January 2026 | Discount Fee Amortization Adjustment | 28.70             |                  |                   |                       | true     |

  Scenario: Future-dated discount fee adjustment is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100          | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "05 January 2026"
    Then Add Discount fee adjustment with "2" amount and transaction date "10 January 2026" on Working Capital loan account failed due to future transaction date
