@WorkingCapitalDiscountAdjustmentBackdatedUndoFeature
Feature: Working Capital Discount Adjustment Backdated and Undo

  @TestRailId:C83061
  Scenario: Backdated discount adjustment keeps the principal already repaid before the adjustment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    And Admin adds Discount fee adjustment with "1000" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | discount | principal | totalPaidPrincipal |
      | 0.0      | 9000.0    | 100.0              |
    And Working Capital Loan has transactions:
      | transactionDate | type                    | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement            | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee            | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment               | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment               | 50.0              | 50.0             | 0.0               | 0.0                   | false    |

  @TestRailId:C83062
  Scenario: Undo of a backdated discount adjustment restores the discount and reverses the adjustment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
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

  @TestRailId:C83063
  Scenario: Partial discount adjustment can be backdated
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
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

  @TestRailId:C83065
  Scenario: Backdated discount adjustment posts amortization adjustment as of current business date for income recognized up to the previous COB
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
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
      | discount | principal | totalPaidPrincipal |
      | 0.0      | 9000.0    | 250.0              |
    When Admin sets the business date to "08 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Adjustment              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 06 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 06 January 2026 | Discount Fee Amortization            | 47.62             |                  |                   |                       | false    |
      | 07 January 2026 | Discount Fee Amortization Adjustment | 47.62             |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "07 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           | 47.62 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 47.62  |

  @TestRailId:C83066
  Scenario: Full discount adjustment posts an amortization adjustment on the next COB; undo reverses both the discount adjustment and the amortization adjustment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
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
      | discount | principal | totalPaidPrincipal |
      | 0.0      | 9000.0    | 150.0              |
    When Admin sets the business date to "06 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           | 28.70 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 28.70  |
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    And Working capital loan account has the correct data:
      | discount | principal | totalPaidPrincipal |
      | 1000.0   | 10000.0   | 150.0              |
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

  @TestRailId:C83068
  Scenario: Multiple backdated discount fee adjustments are allowed; undo of the last one restores its share of the discount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
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

  @TestRailId:C83069
  Scenario: Undo of a deeply-backdated adjustment reverses its amortization adjustment and preserves pre-existing COB income
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
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
      | discount | principal | totalPaidPrincipal |
      | 0.0      | 9000.0    | 150.0              |
    When Admin sets the business date to "06 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin sets the business date to "07 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    And Working capital loan account has the correct data:
      | discount | principal | totalPaidPrincipal |
      | 1000.0   | 10000.0   | 150.0              |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment              | 1000.0            | 1000.0           | 0.0               | 0.0                   | true     |
      | 03 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization            | 28.70             |                  |                   |                       | false    |
      | 05 January 2026 | Discount Fee Amortization Adjustment | 28.70             |                  |                   |                       | true     |

  @TestRailId:C83071
  Scenario: Discount can be re-adjusted after an undo frees up the remaining discount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    When Admin sets the business date to "05 January 2026"
    And Admin adds Discount fee adjustment with "1000" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | discount | principal |
      | 0.0      | 9000.0    |
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    And Working capital loan account has the correct data:
      | discount | principal |
      | 1000.0   | 10000.0   |
    When Admin sets the business date to "06 January 2026"
    And Admin adds Discount fee adjustment with "600" amount on transaction date "03 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | discount | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome |
      | 400.0    | 9400.0    | 0.0                | 0.0            | 400.0            |
    And Working Capital Loan has transactions:
      | transactionDate | type                    | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement            | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee            | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment | 1000.0            | 1000.0           | 0.0               | 0.0                   | true     |
      | 03 January 2026 | Discount Fee Adjustment | 600.0             | 600.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C83072
  Scenario: Undo of an already-reversed discount fee adjustment is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    When Admin sets the business date to "05 January 2026"
    And Admin adds Discount fee adjustment with "1000" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    And Working capital loan account has the correct data:
      | discount | principal |
      | 1000.0   | 10000.0   |
    Then Undo the last Discount fee adjustment on Working Capital loan account failed due to already reversed transaction with status code 400

  @TestRailId:C83073
  Scenario: Backdated discount adjustment dated exactly on the discount fee date is accepted
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    When Admin sets the business date to "05 January 2026"
    And Admin adds Discount fee adjustment with "1000" amount on transaction date "01 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | discount | principal |
      | 0.0      | 9000.0    |
    And Working Capital Loan has transactions:
      | transactionDate | type                    | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement            | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee            | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee Adjustment | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |

  @TestRailId:C83074
  Scenario: Undo of a non-last discount fee adjustment reverses only that adjustment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    When Admin sets the business date to "05 January 2026"
    And Admin adds Discount fee adjustment with "400" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    When Admin sets the business date to "06 January 2026"
    And Admin adds Discount fee adjustment with "600" amount on transaction date "03 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | discount | principal |
      | 0.0      | 9000.0    |
    When Admin undo the Discount fee adjustment with "400" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | discount | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome |
      | 400.0    | 9400.0    | 0.0                | 0.0            | 400.0            |
    And Working Capital Loan has transactions:
      | transactionDate | type                    | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement            | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee            | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment | 400.0             | 400.0            | 0.0               | 0.0                   | true     |
      | 03 January 2026 | Discount Fee Adjustment | 600.0             | 600.0            | 0.0               | 0.0                   | false    |

  @TestRailId:C83075
  Scenario: Undo referencing a non-adjustment transaction is rejected as invalid type
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    When Admin sets the business date to "05 January 2026"
    Then Undo discount fee adjustment referencing the discount fee transaction on Working Capital loan account failed due to invalid transaction type with status code 400

  @TestRailId:C83076
  Scenario: Undo of a partial backdated adjustment posts a new amortization as of current date for income recognized while adjusted
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Admin successfully add discount with "1000" amount on Working Capital loan account
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    When Admin sets the business date to "02 January 2026"
    And Admin adds Discount fee adjustment with "500" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | discount | principal | realizedIncome | unrealizedIncome |
      | 500.0    | 9500.0    | 0.0            | 500.0            |
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "06 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | discount | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome |
      | 500.0    | 9500.0    | 50.0               | 5.14           | 494.86           |
    When Admin sets the business date to "07 January 2026"
    And Admin undo the Discount fee adjustment with "500" amount on Working Capital loan account
    And Working capital loan account has the correct data:
      | discount | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome |
      | 1000.0   | 10000.0   | 50.0               | 5.14           | 994.86           |
    When Admin sets the business date to "08 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | discount | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome |
      | 1000.0   | 10000.0   | 50.0               | 9.61           | 990.39           |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment   | 500.0             | 500.0            | 0.0               | 0.0                   | true     |
      | 05 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 5.14              |                  |                   |                       | false    |
      | 06 January 2026 | Discount Fee Amortization | 4.47              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "06 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 4.47   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 4.47  |        |

  @TestRailId:C83077
  Scenario: Undo of a discount fee adjustment is rejected when the loan is not active
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 100             | 100                | 18                | 0        |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    And Admin loads discount fee transaction from Working Capital loan for adjustment
    When Admin sets the business date to "02 January 2026"
    And Admin adds Discount fee adjustment with "5" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    And Customer makes repayment on "02 January 2026" with 112.0 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then Working Capital loan status will be "OVERPAID"
    Then Undo the last Discount fee adjustment on Working Capital loan account failed due to non active loan with status code 400

  @TestRailId:C83079
  Scenario: Undo of a discount fee adjustment with a non-existent transaction id is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Undo discount fee adjustment with a non-existent transaction id on Working Capital loan account failed as not found with status code 400
