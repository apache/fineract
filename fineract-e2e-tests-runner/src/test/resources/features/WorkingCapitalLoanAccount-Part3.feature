@WorkingCapital
@WorkingCapitalLoanAccountFeature
Feature: WorkingCapitalLoanAccount - Part3

  @TestRailId:C110980
  Scenario: Create WC loan account with discount amount that exceeds principal amount results into an error - UC9
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Creating a working capital loan with discount that exceeds principal and the following data will result into an error:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 100             | 100000       | 18                | 150      |

  @TestRailId:C106685
  Scenario: Verify journal entries are not created for Disbursement & undo when accounting is disabled
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin stores the last Working Capital loan disbursement transaction id for later reference
    And Working capital loan account has the correct data:
      | status | principal | totalDiscountFee | unrealizedIncome |
      | Active | 9000.0    | 0.0              | 0.0              |

    Then Working Capital Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |

    And Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan

    And Admin successfully undo Working Capital disbursal

    Then Working Capital Loan Transactions tab has a reversed "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |

  @TestRailId:C106686
  Scenario: Verify journal entries created for Disbursement & undo when accounting is enabled
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
    And Working capital loan account has the correct data:
      | status | principal | discount | totalDiscountFee | unrealizedIncome |
      | Active | 10000.0   | 1000.0   | 1000.0           | 1000.0           |

    Then Working Capital Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 9000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9000.0 |

    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Admin successfully undo Working Capital disbursal

    Then Working Capital Loan Transactions tab has a reversed "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 9000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9000.0 |
      | ASSET     | 112601       | Loans Receivable          |        | 9000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 9000.0 |        |
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |

  @TestRailId:C106825
  Scenario: Verify undo disbursal reverses the journal entries of the discount fee and its adjustment when accounting is enabled
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    And Admin adds Discount fee adjustment with "400" amount on transaction date "01 January 2026" on Working Capital loan account for last discount
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_ADJUSTMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 400.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 400.0  |
    When Admin sets the business date to "03 January 2026"
    And Admin successfully undo Working Capital disbursal
    Then Working Capital Loan has transactions:
      | transactionDate | type                    | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement            | 9000.0            | 9000.0           | 0.0               | 0.0                   | true     |
      | 01 January 2026 | Discount Fee            | 1000.0            | 1000.0           | 0.0               | 0.0                   | true     |
      | 01 January 2026 | Discount Fee Adjustment | 400.0             | 400.0            | 0.0               | 0.0                   | true     |
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_ADJUSTMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 400.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 400.0  |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 400.0  |
      | ASSET     | 112601       | Loans Receivable          | 400.0 |        |

  @TestRailId:C106826
  Scenario: Verify undo disbursal after an undone discount fee adjustment keeps its reversal date and does not reverse its journal entries twice
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin stores the last Working Capital loan disbursement transaction id for later reference
    When Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    And Admin sets the business date to "03 January 2026"
    And Admin adds Discount fee adjustment with "500" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    And Admin sets the business date to "04 January 2026"
    When Admin undo the last Discount fee adjustment on Working Capital loan account
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_ADJUSTMENT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 500.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 500.0  |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
    And Admin sets the business date to "05 January 2026"
    When Admin successfully undo Working Capital disbursal
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_ADJUSTMENT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 500.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 500.0  |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |
    Then Working Capital Loan has transactions:
      | transactionDate | type                    | transactionAmount | reversed | reversedOnDate  |
      | 01 January 2026 | Disbursement            | 9000.0            | true     | 05 January 2026 |
      | 01 January 2026 | Discount Fee            | 1000.0            | true     | 05 January 2026 |
      | 02 January 2026 | Discount Fee Adjustment | 500.0             | true     | 04 January 2026 |

  @TestRailId:C106687
  Scenario: Verify Working Capital loan disbursement GL entries - UC1: ACC_DEF_REV_AM, loan portfolio and fund source net to zero after full repayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 9000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9000.0 |
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 50.0  |        |
      | ASSET     | 112601       | Loans Receivable          |       | 50.0   |
    When Admin sets the business date to "03 January 2026"
    And Customer makes repayment on "03 January 2026" with 8950.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
#   9000 Dr - 50 Cr - 8950 Cr = 0.00 on 112601; 9000 Cr - 50 Dr - 8950 Dr = 0.00 on 145023
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 8950.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 8950.0 |

  @TestRailId:C106688
  Scenario: Verify Working Capital loan disbursement GL entries - UC2: Advanced Accounting, payment channel fund source mapping, undo disbursal on a later date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "MONEY_TRANSFER" payment type
    Then Working Capital Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type  | Account code | Account name     | Debit  | Credit |
      | ASSET | 112601       | Loans Receivable | 9000.0 |        |
      | ASSET | 987654       | Fund Receivables |        | 9000.0 |
    When Admin sets the business date to "05 January 2026"
    And Admin successfully undo Working Capital disbursal
    Then Working Capital Loan Transactions tab has a reversed "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type  | Account code | Account name     | Debit  | Credit |
      | ASSET | 112601       | Loans Receivable | 9000.0 |        |
      | ASSET | 987654       | Fund Receivables |        | 9000.0 |
      | ASSET | 112601       | Loans Receivable |        | 9000.0 |
      | ASSET | 987654       | Fund Receivables | 9000.0 |        |

  @TestRailId:C106689
  Scenario: Verify Working Capital loan disbursement GL entries - UC3: Advanced Accounting, unmapped payment type falls back to the default fund source
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "AUTOPAY" payment type
    Then Working Capital Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 9000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9000.0 |

  @TestRailId:C106690
  Scenario: Verify Working Capital loan disbursement GL entries - UC4: disbursement with discount fee in the same call posts both sets
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    Then Working Capital Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 9000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9000.0 |
    And Working Capital Loan Transactions tab has a "DISCOUNT_FEE" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |

  @TestRailId:C106691
  Scenario: Verify Working Capital loan disbursement GL entries - UC5: partial disbursement, undo, re-disbursement with a different amount
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "8000" EUR transaction amount
    Then Working Capital Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 8000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 8000.0 |
    When Admin successfully undo Working Capital disbursal
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital Loan Transactions tab has a reversed "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 8000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 8000.0 |
      | ASSET     | 112601       | Loans Receivable          |        | 8000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 8000.0 |        |
    And Working Capital Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 9000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9000.0 |

  @TestRailId:C106721
  Scenario: Discount fee reversal and journal entry is correct via undo disbursement after undone repayment - separate discount fee application
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin stores the last Working Capital loan disbursement transaction id for later reference
    And Working capital loan account has the correct data:
      | status | principal | totalDiscountFee | unrealizedIncome |
      | Active | 9000.0    | 0.0              | 0.0              |
    When Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
    And Working capital loan account has the correct data:
      | status | principal | discount | totalDiscountFee | unrealizedIncome |
      | Active | 10000.0   | 1000.0   | 1000.0           | 1000.0           |

    Then Working Capital Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 9000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9000.0 |

    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan

    And Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    When Customer undo "1"th working capital transaction made on "03 January 2026"

    And Admin successfully undo Working Capital disbursal

    Then Working Capital Loan Transactions tab has a reversed "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 9000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9000.0 |
      | ASSET     | 112601       | Loans Receivable          |        | 9000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 9000.0 |        |

    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 50.0  |        |
      | ASSET     | 112601       | Loans Receivable          |       | 50.0   |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 50.0   |
      | ASSET     | 112601       | Loans Receivable          | 50.0  |        |

    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 9.61   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.61  |        |
      | INCOME    | 404000       | Interest Income           | 9.61  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 9.61   |

    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |

  @TestRailId:C106722
  Scenario: Discount fee reversal and journal entry is correct via undo disbursement after undone repayment - discountfee with disbursement
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount

    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |

    And Working capital loan account has the correct data:
      | status | principal | discount | totalDiscountFee | unrealizedIncome |
      | Active | 10000.0   | 1000.0   | 1000.0           | 1000.0           |

    Then Working Capital Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 9000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9000.0 |

    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan

    And Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    When Customer undo "1"th working capital transaction made on "03 January 2026"

    And Admin successfully undo Working Capital disbursal

    Then Working Capital Loan Transactions tab has a reversed "DISBURSEMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 9000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9000.0 |
      | ASSET     | 112601       | Loans Receivable          |        | 9000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 9000.0 |        |

    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 50.0  |        |
      | ASSET     | 112601       | Loans Receivable          |       | 50.0   |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 50.0   |
      | ASSET     | 112601       | Loans Receivable          | 50.0  |        |

    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 9.61   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.61  |        |
      | INCOME    | 404000       | Interest Income           | 9.61  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 9.61   |

    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |

  @TestRailId:C106723
  Scenario: Undo disbursal reverses discount fee adjustment and amortization adjustment journal entries
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin stores the last Working Capital loan disbursement transaction id for later reference
    When Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    And Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Admin adds Discount fee adjustment with "500" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    And Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_ADJUSTMENT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 500.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
    And Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           | 4.47  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 4.47   |
    When Customer undo "1"th working capital transaction made on "03 January 2026"
    When Admin successfully undo Working Capital disbursal
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_ADJUSTMENT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 500.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 500.0  |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
    And Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           |       | 9.61   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.61  |        |
      | INCOME    | 404000       | Interest Income           | 9.61  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 9.61   |
    And Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404000       | Interest Income           | 4.47  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |       | 4.47   |
      | INCOME    | 404000       | Interest Income           |       | 4.47   |
      | LIABILITY | 240005       | Deferred Interest Revenue | 4.47  |        |
    And a Working Capital Loan Adjust Transaction business event is raised for the reversed "discountFee" transaction
    And a Working Capital Loan Adjust Transaction business event is raised for the reversed "discountFeeAdjustment" transaction
    And a Working Capital Loan Adjust Transaction business event is raised for the reversed "discountFeeAmortization" transaction
    And a Working Capital Loan Adjust Transaction business event is raised for the reversed "discountFeeAmortizationAdjustment" transaction
    And Working capital loan account has the correct data:
      | status   | principal | totalDiscountFee | unrealizedIncome |
      | Approved | 0.0       | 0.0              | 0.0              |

  @TestRailId:C106724
  Scenario: Undo disbursal reverses charge accrual, discount fee and amortization journal entries
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    And Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "02 January 2026"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "03 January 2026" due date and 50.0 transaction amount
    And Admin sets the business date to "05 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "03 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 50.0  |        |
      | INCOME | 404007       | Fee Income              |       | 50.0   |
    When Admin successfully undo Working Capital disbursal
    Then Working Capital Loan Transactions tab has a reversed "ACCRUAL" transaction with date "03 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 50.0  |        |
      | INCOME | 404007       | Fee Income              |       | 50.0   |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 50.0   |
      | INCOME | 404007       | Fee Income              | 50.0  |        |
    And a Working Capital Loan Accrual Adjustment transaction business event is raised with "50.0" EUR amount
    Then Working Capital Loan Transactions tab has a reversed "DISCOUNT_FEE" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 240005       | Deferred Interest Revenue | 1000.0 |        |

  @TestRailId:C106725
  Scenario: Undo disbursal keeps the reversal date of a previously undone repayment and does not double-reverse its journal entries
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    And Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    When Customer undo "1"th working capital transaction made on "03 January 2026"
    And Admin sets the business date to "05 January 2026"
    When Admin successfully undo Working Capital disbursal
    Then Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | reversed | reversedOnDate  |
      | 01 January 2026 | Disbursement              | 9000.0            | true     | 05 January 2026 |
      | 01 January 2026 | Discount Fee              | 1000.0            | true     | 05 January 2026 |
      | 03 January 2026 | Repayment                 | 50.0              | true     | 04 January 2026 |
      | 03 January 2026 | Discount Fee Amortization | 9.61              | true     | 05 January 2026 |
    Then Working Capital Loan Transactions tab has a reversed "REPAYMENT" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 50.0  |        |
      | ASSET     | 112601       | Loans Receivable          |       | 50.0   |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 50.0   |
      | ASSET     | 112601       | Loans Receivable          | 50.0  |        |

@TestRailId:C106836
  Scenario: Re-disbursal after undo disbursal starts from a clean discount fee and amortization state
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 1000     |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    And Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    And Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    When Customer undo "1"th working capital transaction made on "03 January 2026"
    And Admin sets the business date to "05 January 2026"
    When Admin successfully undo Working Capital disbursal
    And Admin successfully disburse the Working Capital loan on "05 January 2026" with "9000" EUR transaction amount and "1000" discount amount
    Then Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | true     |
      | 01 January 2026 | Discount Fee              | 1000.0            | true     |
      | 03 January 2026 | Repayment                 | 50.0              | true     |
      | 03 January 2026 | Discount Fee Amortization | 9.61              | true     |
      | 05 January 2026 | Disbursement              | 9000.0            | false    |
      | 05 January 2026 | Discount Fee              | 1000.0            | false    |
    And Working capital loan account has the correct data:
      | status | principal | discount | totalDiscountFee | unrealizedIncome |
      | Active | 10000.0   | 1000.0   | 1000.0           | 1000.0           |
    And Working Capital Loan Transactions tab has a "DISBURSEMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 9000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 9000.0 |
    And Working Capital Loan Transactions tab has a "DISCOUNT_FEE" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 240005       | Deferred Interest Revenue |        | 1000.0 |
    When Admin sets the business date to "07 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "07 January 2026" with 50 transaction amount on Working Capital loan
    And Admin sets the business date to "08 January 2026"
    And Admin runs inline COB job for Working Capital Loan
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "07 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 240005       | Deferred Interest Revenue | 9.61  |        |
      | INCOME    | 404000       | Interest Income           |       | 9.61   |
    And Working capital loan account has the correct data:
      | status | principal | totalDiscountFee | unrealizedIncome |
      | Active | 10000.0   | 1000.0           | 990.39           |

  @TestRailId:C106745
  Scenario: Create Working Capital Loan account rejects non-WORKING_CAPITAL delinquency bucket
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with the following data will result an error "The parameter `delinquencyBucketId` must reference a WORKING_CAPITAL delinquency bucket.":
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount | delinquencyBucketId      |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      | BASIC_DELINQUENCY_BUCKET |

  @TestRailId:C106803
  Scenario: Modify Working Capital Loan account rejects non-WORKING_CAPITAL delinquency bucket
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Admin failed to modify working capital loan with delinquencyBucketId "BASIC_DELINQUENCY_BUCKET" and got an error "The parameter `delinquencyBucketId` must reference a WORKING_CAPITAL delinquency bucket."

  @TestRailId:C106804
  Scenario: Modify Working Capital Loan account with the same WORKING_CAPITAL delinquencyBucketId succeeds
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin modifies the working capital loan resenting its current delinquencyBucketId

  @TestRailId:C115287
  Scenario: Create Working Capital Loan account - UC4.3: With LP overridable disabled, overriding to non-daily frequency is rejected (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with LP overridable disabled and with the following data will result an error:
      | LoanProduct                       | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount | delinquencyBucketId   | repaymentEvery | repaymentFrequencyType |
      | WCLP_DISALLOW_ATTRIBUTES_OVERRIDE | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      | WC_DELINQUENCY_BUCKET | 1              | MONTHS                 |

  @TestRailId:C115392
  Scenario Outline: Verify WC Loan creation fails when delinquency grace/start type set without delinquency bucket
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    Then Creating a working capital loan with "<wcl_field_name>" "<wcl_field_value>" without delinquency bucket on "01 January 2027" will result with error "<wcl_error_message>"

    Examples:
      | wcl_field_name         | wcl_field_value | wcl_error_message                                                                               |
      | delinquencyGraceDays   | 0               | Failed data validation due to: cannot.enable.delinquency.grace.days.without.delinquency.bucket. |
      | delinquencyGraceDays   | 3               | Failed data validation due to: cannot.enable.delinquency.grace.days.without.delinquency.bucket. |
      | delinquencyStartType   | DISBURSEMENT    | Failed data validation due to: cannot.enable.delinquency.start.type.without.delinquency.bucket. |
      | delinquencyStartType   | LOAN_CREATION   | Failed data validation due to: cannot.enable.delinquency.start.type.without.delinquency.bucket. |

  @TestRailId:C115393
  Scenario Outline: Verify WC Loan creation fails when breach grace/start type set without breach
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    Then Creating a working capital loan with "<wcl_field_name>" "<wcl_field_value>" without breach on "01 January 2027" will result with error "<wcl_error_message>"

    Examples:
      | wcl_field_name  | wcl_field_value | wcl_error_message                                                              |
      | breachGraceDays | 0               | Failed data validation due to: cannot.enable.breach.grace.days.without.breach. |
      | breachGraceDays | 5               | Failed data validation due to: cannot.enable.breach.grace.days.without.breach. |
      | breachStartType | DISBURSEMENT    | Failed data validation due to: cannot.enable.breach.start.type.without.breach. |
      | breachStartType | LOAN_CREATION   | Failed data validation due to: cannot.enable.breach.start.type.without.breach. |

  @TestRailId:C115394
  Scenario Outline: Verify WC Loan modify fails when delinquency grace/start type set without delinquency bucket
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product without delinquency bucket for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2027 | 01 January 2027          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    Then Modifying the working capital loan with "<wcl_field_name>" "<wcl_field_value>" will result with error "<wcl_error_message>"

    Examples:
      | wcl_field_name         | wcl_field_value | wcl_error_message                                                                               |
      | delinquencyGraceDays   | 0               | Failed data validation due to: cannot.enable.delinquency.grace.days.without.delinquency.bucket. |
      | delinquencyGraceDays   | 3               | Failed data validation due to: cannot.enable.delinquency.grace.days.without.delinquency.bucket. |
      | delinquencyStartType   | DISBURSEMENT    | Failed data validation due to: cannot.enable.delinquency.start.type.without.delinquency.bucket. |
      | delinquencyStartType   | LOAN_CREATION   | Failed data validation due to: cannot.enable.delinquency.start.type.without.delinquency.bucket. |

  @TestRailId:C115395
  Scenario Outline: Verify WC Loan modify fails when breach grace/start type set without breach
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product without breach for loan test
    And Admin creates a working capital loan with the grace days product and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2027 | 01 January 2027          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    Then Modifying the working capital loan with "<wcl_field_name>" "<wcl_field_value>" will result with error "<wcl_error_message>"

    Examples:
      | wcl_field_name  | wcl_field_value | wcl_error_message                                                              |
      | breachGraceDays | 0               | Failed data validation due to: cannot.enable.breach.grace.days.without.breach. |
      | breachGraceDays | 5               | Failed data validation due to: cannot.enable.breach.grace.days.without.breach. |
      | breachStartType | DISBURSEMENT    | Failed data validation due to: cannot.enable.breach.start.type.without.breach. |
      | breachStartType | LOAN_CREATION   | Failed data validation due to: cannot.enable.breach.start.type.without.breach. |
