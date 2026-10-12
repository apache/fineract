@WorkingCapitalDiscountFeeAmortizationRecalculationFeature
Feature: Working Capital Discount Fee Amortization Recalculation

  @TestRailId:C85360
  Scenario: Multiple COB runs accumulate discount fee amortization correctly - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 03 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 9.57              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 100.0              | 19.18          | 980.82           | 0.0               |
    And Customer makes repayment on "04 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 03 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 9.57              |                  |                   |                       | false    |
      | 04 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization | 9.52              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 150.0              | 28.7           | 971.3            | 0.0               |

  @TestRailId:C85361
  Scenario: Second COB run without new repayment does not produce a duplicate amortization transaction - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    When Admin sets the business date to "04 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |

  @TestRailId:C85362
  Scenario: Undo of discount fee adjustment recalculates realized income from net amortization transactions - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    When Admin sets the business date to "04 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Admin adds Discount fee adjustment with "500" amount on transaction date "04 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 50.0               | 9.61           | 490.39           | 0.0               |
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 04 January 2026 | Discount Fee Adjustment              | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization Adjustment | 4.47              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           | 4.47  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 4.47   |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 50.0               | 5.14           | 494.86           | 0.0               |
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 04 January 2026 | Discount Fee Adjustment              | 500.0             | 500.0            | 0.0               | 0.0                   | true     |
      | 04 January 2026 | Discount Fee Amortization Adjustment | 4.47              |                  |                   |                       | true     |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |

  @TestRailId:C85363
  Scenario: Inline full discount recognition on loan close uses net amortization as base to compute remaining amount - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 9950 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 05 January 2026 | Repayment                 | 9950.0            | 9950.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 990.39            |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | INCOME    | 404000       | Interest Income           |        | 990.39 |
      | LIABILITY | 240005       | Deferred Interest Revenue | 990.39 |        |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 10000.0            | 1000.0         | 0.0              | 0.0               |

  @TestRailId:C85364
  Scenario: Inline recognition on close produces amortization adjustment when discount was reduced below already-posted amount - UC4b
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    When Admin sets the business date to "04 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Admin adds Discount fee adjustment with "995" amount on transaction date "04 January 2026" on Working Capital loan account for last discount
    And Customer makes repayment on "04 January 2026" with 8955 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 04 January 2026 | Discount Fee Adjustment              | 995.0             | 995.0            | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Repayment                            | 8955.0            | 8955.0           | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization Adjustment | 4.61              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           | 4.61  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 4.61   |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9005.0    | 9005.0             | 5.0            | 0.0              | 0.0               |

  @TestRailId:C85365
  Scenario: Second COB after amortization adjustment does not produce a duplicate transaction - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    When Admin sets the business date to "04 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Admin adds Discount fee adjustment with "500" amount on transaction date "04 January 2026" on Working Capital loan account for last discount
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 04 January 2026 | Discount Fee Adjustment              | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization Adjustment | 4.47              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 50.0               | 5.14           | 494.86           | 0.0               |
    When Admin sets the business date to "06 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 04 January 2026 | Discount Fee Adjustment              | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization Adjustment | 4.47              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 50.0               | 5.14           | 494.86           | 0.0               |

  @TestRailId:C85366
  Scenario: Catch-up of COB does not produce amortization transactions before transaction date - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    When Admin sets the business date to "04 January 2026"
    And Admin adds Discount fee adjustment with "500" amount on transaction date "04 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 50.0               | 9.61           | 490.39           | 0.0               |
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 04 January 2026 | Discount Fee Adjustment              | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization Adjustment | 4.47              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           | 4.47  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 4.47   |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 50.0               | 5.14           | 494.86           | 0.0               |

  @TestRailId:C85367
  Scenario: Verify discount fee amortization transaction on working capital loan after repayment overpays loan - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 0.0       | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | 1000.0    |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 10200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 10000.0            | 1000.0         | 0.0              | 200.0             |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 10200.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 10000.0            | 1000.0         | 0.0              | 200.0             |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 10200.0           | 10000.0          | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 1000.0            |                  |                   |                       | false    |

  @TestRailId:C85368
  Scenario: Verify none discount fee amortization nor discount fee adjustment amortization if discount fee adjustment pays off discount fee - UC8
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 0.0       | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | 1000.0     |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 50.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 0.0            | 1000.0           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
    And Admin adds Discount fee adjustment with "1000" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 50.0               | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment   | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 50.0               | 0.0            | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment   | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |

