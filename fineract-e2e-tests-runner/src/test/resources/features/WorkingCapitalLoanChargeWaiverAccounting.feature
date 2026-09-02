@SerialChargeAccrualConfig
@WorkingCapital
@WorkingCapitalLoanChargeWaiverAccountingFeature
Feature: Working Capital Loan Charge Waiver Accounting

  Scenario: Waiving an accrued fee charge books Write-off Expense against Fees Receivable
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2026" due date and 100.0 transaction amount
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "15 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
    When Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
    And Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Accrual            | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
      | 15 January 2026 | Waive loan charges | 100.0             | 0.0              | 100.0             | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "WAIVE_CHARGES" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit | Credit |
      | EXPENSE | e4           | Written off             | 100.0 |        |
      | ASSET   | 112603       | Interest/Fee Receivable |       | 100.0  |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 0.0      | 0.0            | 0.0                 | 0.0          |

  Scenario: Waiving an accrued penalty charge books against Penalties Receivable
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "15 January 2026" due date and 50.0 transaction amount
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "50.0" EUR amount
    And Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Accrual            | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
      | 15 January 2026 | Waive loan charges | 50.0              | 0.0              | 0.0               | 50.0                  | false    |
    And Working Capital Loan Transactions tab has a "WAIVE_CHARGES" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit | Credit |
      | EXPENSE | e4           | Written off             | 50.0  |        |
      | ASSET   | 112603       | Interest/Fee Receivable |       | 50.0   |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 0.0        | 0.0             | 0.0      | 50.0           | 0.0                 | 0.0          |

  Scenario: Waiving a charge that was never accrued books no journal entries
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 January 2026" due date and 100.0 transaction amount
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
# There is no receivable behind income that was never recognized, so crediting one would drive it negative.
    And Working Capital Loan Transactions tab has a "WAIVE_CHARGES" transaction with date "10 January 2026" which has the following Journal entries:
      | Type | Account code | Account name | Debit | Credit |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 0.0      | 0.0            | 0.0                 | 0.0          |

  Scenario: Undoing a charge waiver reverses its journal entries
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2026" due date and 100.0 transaction amount
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
    When Admin reverts the last charge waiver on working capital loan
    Then a Working Capital Loan Adjust Transaction business event is raised for the reversed "waiveCharges" transaction
    And Working Capital Loan Transactions tab has a reversed "WAIVE_CHARGES" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit | Credit |
      | EXPENSE | e4           | Written off             | 100.0 |        |
      | ASSET   | 112603       | Interest/Fee Receivable |       | 100.0  |
      | EXPENSE | e4           | Written off             |       | 100.0  |
      | ASSET   | 112603       | Interest/Fee Receivable | 100.0 |        |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 0.0            | 0.0                 | 0.0          |

  Scenario: Charge waiver on a charged-off loan books against charge-off fee income
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2026" due date and 100.0 transaction amount
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "15 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 100.0 |        |
      | INCOME | 404007       | Fee Income              |       | 100.0  |
# The waiver is dated on the charge due date, which precedes the charge-off, so it sorts before it - the only order
# that proves the credit follows the charge-off flag rather than the transaction order.
    When Admin sets the business date to "18 January 2026"
    And Admin charges off the Working Capital loan on "18 January 2026" with charge-off reason "Fraud"
    When Admin sets the business date to "20 January 2026"
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
    And Working Capital Loan Transactions tab has a "WAIVE_CHARGES" transaction with date "15 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name   | Debit | Credit |
      | EXPENSE | e4           | Written off    | 100.0 |        |
      | INCOME  | 404008       | Fee Charge Off |       | 100.0  |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 0.0      | 0.0            | 0.0                 | 0.0          |

  Scenario: Waiving the remainder of a partially paid charge before it is accrued accrues only the paid portion
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
# The charge payment credits the receivable whether or not an accrual preceded it, leaving it at -40. The later
# accrual has to recognize exactly the paid part to net that back to zero.
    And Admin makes a charge adjustment for the last added charge with 40.0 amount on working capital loan
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "60.0" EUR amount
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge Adjustment  | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
      | 10 January 2026 | Waive loan charges | 60.0              | 0.0              | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Accrual            | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "10 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 40.0  |        |
      | INCOME | 404007       | Fee Income              |       | 40.0   |

  Scenario: Undoing a charge waiver tops the reduced accrual back up to the full charge amount
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    And Admin makes a charge adjustment for the last added charge with 40.0 amount on working capital loan
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "60.0" EUR amount
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin reverts the last charge waiver on working capital loan
    Then a Working Capital Loan Adjust Transaction business event is raised for the reversed "waiveCharges" transaction
# The restored part of the charge must still be able to reach the income statement. A boolean "already accrued"
# guard would silence every later accrual and leave it unrecognized forever.
    When Admin sets the business date to "12 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge Adjustment  | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
      | 10 January 2026 | Waive loan charges | 60.0              | 0.0              | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Accrual            | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
      | 10 January 2026 | Accrual            | 60.0              | 0.0              | 60.0              | 0.0                   | false    |
    And Working Capital Loan Transactions tab has 2 "ACCRUAL" transactions with date "10 January 2026" which have the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 40.0  |        |
      | INCOME | 404007       | Fee Income              |       | 40.0   |
      | ASSET  | 112603       | Interest/Fee Receivable | 60.0  |        |
      | INCOME | 404007       | Fee Income              |       | 60.0   |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 60.0            | 40.0     | 0.0            | 0.0                 | 0.0          |

  Scenario: Re-waiving a charge before the accrual catches up books no journal entries
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    And Admin makes a charge adjustment for the last added charge with 40.0 amount on working capital loan
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "60.0" EUR amount
    When Admin sets the business date to "11 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin reverts the last charge waiver on working capital loan
    Then a Working Capital Loan Adjust Transaction business event is raised for the reversed "waiveCharges" transaction
# Only 40 was ever accrued, and the payment already consumed all of it, so the 60 being waived again has no
# receivable behind it - a flat "accrued or not" rule would credit Fees Receivable against nothing.
    When Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "60.0" EUR amount
    And Working Capital Loan Transactions tab has a "WAIVE_CHARGES" transaction with date "10 January 2026" which has the following Journal entries:
      | Type | Account code | Account name | Debit | Credit |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 40.0     | 0.0            | 0.0                 | 0.0          |
