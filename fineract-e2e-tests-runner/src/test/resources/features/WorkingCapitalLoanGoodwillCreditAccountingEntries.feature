@WorkingCapital
@WorkingCapitalLoanGoodwillCreditAccountingEntriesFeature
Feature: Working Capital Goodwill Credit Accounting Entries

  @TestRailId:C80942
  Scenario: Verify Working Capital loan Goodwill Credit transaction GL entries - UC1: simple Goodwill Credit transaction
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name              | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account  | 270.0 |        |
      | ASSET   | 112601       | Loans Receivable          |       | 270.0  |

  @TestRailId:C80943
  Scenario: Verify Working Capital loan Goodwill Credit transaction GL entries - UC2: multiple Goodwill Credit transactions same day
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "02 January 2026" with 170.0 transaction amount on Working Capital loan
    And Customer makes "GOODWILL_CREDIT" transaction on "02 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has 2 "GOODWILL_CREDIT" transactions with date "02 January 2026" which have the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 170.0 |        |
      | ASSET   | 112601       | Loans Receivable         |       | 170.0  |
      | EXPENSE | 744003       | Goodwill Expense Account | 100.0 |        |
      | ASSET   | 112601       | Loans Receivable         |       | 100.0  |

  @TestRailId:C80944
  Scenario: Verify Working Capital loan Goodwill Credit transaction GL entries - UC3: multiple Goodwill Credit transactions different days
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "02 January 2026" with 170.0 transaction amount on Working Capital loan
    When Admin sets the business date to "15 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "15 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "02 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 170.0 |        |
      | ASSET   | 112601       | Loans Receivable         |       | 170.0  |
    And Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 100.0 |        |
      | ASSET   | 112601       | Loans Receivable         |       | 100.0  |

  @TestRailId:C80945
  Scenario: Verify Working Capital loan Goodwill Credit transaction GL entries - UC4: Goodwill Credit with overpayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 10000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type       | Account code | Account name             | Debit   | Credit |
      | EXPENSE    | 744003       | Goodwill Expense Account | 10000.0 |        |
      | ASSET      | 112601       | Loans Receivable         |         | 9000.0 |
      | LIABILITY  | 245000       | Other Credit Liability   |         | 1000.0 |

  @TestRailId:C85555
  Scenario: Verify Working Capital loan Goodwill Credit transaction GL entries - UC5: Goodwill Credit allocates to fees
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
#    --- Add fee to Working Capital loan ---
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "01 January 2026" due date and 50.0 transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 320.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 270.0 |        |
      | INCOME  | 404008       | Fee Charge Off           | 50.0  |        |
      | ASSET   | 112601       | Loans Receivable         |       | 270.0  |
      | ASSET   | 112603       | Interest/Fee Receivable  |       | 50.0   |

  @TestRailId:C85556
  Scenario: Verify Working Capital loan Goodwill Credit transaction GL entries - UC6: Goodwill Credit allocates to penalties
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
#   --- Add penalty to Working Capital loan ---
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "01 January 2026" due date and 30.0 transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 300.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 270.0 |        |
      | INCOME  | 404008       | Fee Charge Off           | 30.0  |        |
      | ASSET   | 112601       | Loans Receivable         |       | 270.0  |
      | ASSET   | 112603       | Interest/Fee Receivable  |       | 30.0   |

  @TestRailId:C85557
  Scenario: Verify Working Capital loan Goodwill Credit transaction GL entries - UC7: complex allocation with fees, penalties, and overpayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
#   --- Add fee + penalty to Working Capital loan ---
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "01 January 2026" due date and 50.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "01 January 2026" due date and 30.0 transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 10500.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name             | Debit   | Credit  |
      | EXPENSE   | 744003       | Goodwill Expense Account | 10420.0 |         |
      | INCOME    | 404008       | Fee Charge Off           | 50.0    |         |
      | INCOME    | 404008       | Fee Charge Off           | 30.0    |         |
      | ASSET     | 112601       | Loans Receivable         |         | 9000.0  |
      | ASSET     | 112603       | Interest/Fee Receivable  |         | 50.0    |
      | ASSET     | 112603       | Interest/Fee Receivable  |         | 30.0    |
      | LIABILITY | 245000       | Other Credit Liability   |         | 1420.0  |

  @TestRailId:C80946
  Scenario: Verify Working Capital loan Goodwill Credit transaction GL entries - UC8: partial Goodwill Credit
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 100.0 |        |
      | ASSET   | 112601       | Loans Receivable         |       | 100.0  |

  @TestRailId:C85558
  Scenario: Verify Working Capital loan Goodwill Credit transaction GL entries - UC9: closed loan posts expense debit and overpayment liability credit
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "20 January 2026"
    And Customer makes repayment on "20 January 2026" with 9000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "21 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "21 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance overpaymentAmount is "100.00"
    And Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "21 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name             | Debit | Credit |
      | EXPENSE   | 744003       | Goodwill Expense Account | 100.0 |        |
      | LIABILITY | 245000       | Other Credit Liability   |       | 100.0  |
    And Customer makes credit balance refund on "21 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85559
  Scenario: Verify Working Capital loan Goodwill Credit transaction GL entries - UC10: overpaid loan posts posts expense debit and overpayment liability credit
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "20 January 2026"
    And Customer makes repayment on "20 January 2026" with 9100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance overpaymentAmount is "100.00"
    When Admin sets the business date to "21 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "21 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance overpaymentAmount is "150.00"
    And Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "21 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name             | Debit | Credit |
      | EXPENSE   | 744003       | Goodwill Expense Account | 50.0  |        |
      | LIABILITY | 245000       | Other Credit Liability   |       | 50.0   |
    And Customer makes credit balance refund on "21 January 2026" with 150.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85560
  Scenario: Verify Working Capital loan Goodwill Credit transaction GL entries - UC11: partially repaid active loan, Goodwill Credit covers remainder and excess goes to overpayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 4000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    When Admin sets the business date to "20 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "20 January 2026" with 5200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    And Working Capital loan balance overpaymentAmount is "200.00"
    And Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "20 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name             | Debit  | Credit |
      | EXPENSE   | 744003       | Goodwill Expense Account | 5200.0 |        |
      | ASSET     | 112601       | Loans Receivable         |        | 5000.0 |
      | LIABILITY | 245000       | Other Credit Liability   |        | 200.0  |
    And Customer makes credit balance refund on "20 January 2026" with 200.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C85337
  Scenario: Verify Working Capital loan UNDO Goodwill Credit transaction GL entries - UC1: simple reversal
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 270.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 270.0 |        |
      | ASSET   | 112601       | Loans Receivable         |       | 270.0  |
    When Customer undo "1"th "GOODWILL_CREDIT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 270.0 |        |
      | ASSET   | 112601       | Loans Receivable         |       | 270.0  |
      | EXPENSE | 744003       | Goodwill Expense Account |       | 270.0  |
      | ASSET   | 112601       | Loans Receivable         | 270.0 |        |

  @TestRailId:C85552
  Scenario: Verify Working Capital loan UNDO Goodwill Credit transaction GL entries - UC2: reversal with fees
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 50.0 transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 320.0 transaction amount on Working Capital loan
    When Customer undo "1"th "GOODWILL_CREDIT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 270.0 |        |
      | INCOME  | 404008       | Fee Charge Off           | 50.0  |        |
      | ASSET   | 112601       | Loans Receivable         |       | 270.0  |
      | ASSET   | 112603       | Interest/Fee Receivable  |       | 50.0   |
      | EXPENSE | 744003       | Goodwill Expense Account |       | 270.0  |
      | INCOME  | 404008       | Fee Charge Off           |       | 50.0   |
      | ASSET   | 112601       | Loans Receivable         | 270.0 |        |
      | ASSET   | 112603       | Interest/Fee Receivable  | 50.0  |        |

  @TestRailId:C85553
  Scenario: Verify Working Capital loan UNDO Goodwill Credit transaction GL entries - UC3: reversal with penalties
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "05 January 2026" due date and 30.0 transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 300.0 transaction amount on Working Capital loan
    When Customer undo "1"th "GOODWILL_CREDIT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 270.0 |        |
      | INCOME  | 404008       | Fee Charge Off           | 30.0  |        |
      | ASSET   | 112601       | Loans Receivable         |       | 270.0  |
      | ASSET   | 112603       | Interest/Fee Receivable  |       | 30.0   |
      | EXPENSE | 744003       | Goodwill Expense Account |       | 270.0  |
      | INCOME  | 404008       | Fee Charge Off           |       | 30.0   |
      | ASSET   | 112601       | Loans Receivable         | 270.0 |        |
      | ASSET   | 112603       | Interest/Fee Receivable  | 30.0  |        |

  @TestRailId:C85336
  Scenario: Verify Working Capital loan UNDO Goodwill Credit transaction GL entries - UC4: reversal with overpayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 10000.0 transaction amount on Working Capital loan
    When Customer undo "1"th "GOODWILL_CREDIT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name             | Debit   | Credit  |
      | EXPENSE   | 744003       | Goodwill Expense Account | 10000.0 |         |
      | ASSET     | 112601       | Loans Receivable         |         | 9000.0  |
      | LIABILITY | 245000       | Other Credit Liability   |         | 1000.0  |
      | EXPENSE   | 744003       | Goodwill Expense Account |         | 10000.0 |
      | ASSET     | 112601       | Loans Receivable         | 9000.0  |         |
      | LIABILITY | 245000       | Other Credit Liability   | 1000.0  |         |

  @TestRailId:C85554
  Scenario: Verify Working Capital loan UNDO Goodwill Credit transaction GL entries - UC5: closed loan posts mirror overpayment liability entries
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "20 January 2026"
    And Customer makes repayment on "20 January 2026" with 9000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "21 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "21 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "21 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name             | Debit | Credit |
      | EXPENSE   | 744003       | Goodwill Expense Account | 100.0 |        |
      | LIABILITY | 245000       | Other Credit Liability   |       | 100.0  |
    When Customer undo "1"th "GOODWILL_CREDIT" transaction made on "21 January 2026" on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital Loan Transactions tab has a reversed "GOODWILL_CREDIT" transaction with date "21 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name             | Debit | Credit |
      | EXPENSE   | 744003       | Goodwill Expense Account | 100.0 |        |
      | LIABILITY | 245000       | Other Credit Liability   |       | 100.0  |
      | EXPENSE   | 744003       | Goodwill Expense Account |       | 100.0  |
      | LIABILITY | 245000       | Other Credit Liability   | 100.0 |        |

  @TestRailId:C85336
  Scenario: Verify Working Capital loan UNDO Goodwill Credit transaction GL entries - UC4: reversal with overpayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 10000.0 transaction amount on Working Capital loan
    When Customer undo "1"th "GOODWILL_CREDIT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital Loan Transactions tab has a reversed "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name             | Debit   | Credit  |
      | EXPENSE   | 744003       | Goodwill Expense Account | 10000.0 |         |
      | ASSET     | 112601       | Loans Receivable         |         | 9000.0  |
      | LIABILITY | 245000       | Other Credit Liability   |         | 1000.0  |
      | EXPENSE   | 744003       | Goodwill Expense Account |         | 10000.0 |
      | ASSET     | 112601       | Loans Receivable         | 9000.0  |         |
      | LIABILITY | 245000       | Other Credit Liability   | 1000.0  |         |

  @TestRailId:C85530
  Scenario: Verify Working Capital loan Goodwill Credit transaction GL entries - UC9: GOODWILL_CREDIT specific rule (DUE_PRINCIPAL first) skips the due fee
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct                     | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_GOODWILL_CREDIT_ALLOCATION | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 50.0 transaction amount
# --- the goodwill credit follows the GOODWILL_CREDIT rule (DUE_PRINCIPAL first): the due fee is skipped, so no fee income/receivable legs are posted --- #
    And Customer makes "GOODWILL_CREDIT" transaction on "10 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | EXPENSE | 744003       | Goodwill Expense Account | 100.0 |        |
      | ASSET   | 112601       | Loans Receivable         |       | 100.0  |

