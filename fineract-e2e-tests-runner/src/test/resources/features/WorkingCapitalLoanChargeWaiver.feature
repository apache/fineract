@SerialChargeAccrualConfig
@WorkingCapital
@WorkingCapitalLoanChargeWaiverFeature
Feature: Working Capital Loan Charge Waiver

  Scenario: Waive an unpaid fee charge with a past due date
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
# A charge cannot be created with a due date in the past, so the only way into the past-due-date branch is to
# create it with a future due date and let the business date overtake it.
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2026" due date and 100.0 transaction amount
    When Admin sets the business date to "20 January 2026"
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
    And Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Waive loan charges | 100.0             | 0.0              | 0.0               | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 0.0      | 0.0            | 0.0                 | 0.0          |

  Scenario: Waive an unpaid fee charge with a future due date
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 January 2026" due date and 100.0 transaction amount
    When Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
    And Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Waive loan charges | 100.0             | 0.0              | 0.0               | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 0.0      | 0.0            | 0.0                 | 0.0          |

  Scenario: Waive a partially paid charge waives only the remainder
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    And Admin makes a charge adjustment for the last added charge with 40.0 amount on working capital loan
    Then Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 60.0            | 40.0     | 0.0            | 0.0                 | 0.0          |
    When Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "60.0" EUR amount
    And Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Charge Adjustment  | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
      | 10 January 2026 | Waive loan charges | 60.0              | 0.0              | 0.0               | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Amount Paid | Amount Waived | Amount Outstanding |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | 40.0        | 60.0          | 0.0                |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 40.0     | 0.0            | 0.0                 | 0.0          |

  Scenario: Waiving a fully paid charge is rejected
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    And Admin makes a charge adjustment for the last added charge with 100.0 amount on working capital loan
    Then Waiving the last added charge on working capital loan results an error with the following data:
      | httpCode | errorMessage                        |
      | 403      | has no outstanding amount to waive  |

  Scenario: Undo a charge waiver restores the charge to outstanding
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Amount Paid | Amount Waived | Amount Outstanding |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | 0.0         | 100.0         | 0.0                |
    When Admin reverts the last charge waiver on working capital loan
    Then a Working Capital Loan Adjust Transaction business event is raised for the reversed "waiveCharges" transaction
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Amount Paid | Amount Waived | Amount Outstanding |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | 0.0         | 0.0           | 100.0              |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 100.0           | 0.0      | 0.0            | 0.0                 | 0.0          |
# Moved back to outstanding state means back in the allocation pipeline, not only a changed number.
    When Customer makes repayment on "10 January 2026" with 100.0 transaction amount on Working Capital loan
    Then Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 100.0    | 0.0            | 0.0                 | 0.0          |

  Scenario: Re-waiving a charge after its waiver was undone succeeds
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
    When Admin reverts the last charge waiver on working capital loan
    Then a Working Capital Loan Adjust Transaction business event is raised for the reversed "waiveCharges" transaction
    When Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
    And Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Waive loan charges | 100.0             | 0.0              | 0.0               | 0.0                   | true     |
      | 10 January 2026 | Waive loan charges | 100.0             | 0.0              | 0.0               | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Amount Paid | Amount Waived | Amount Outstanding |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | 0.0         | 100.0         | 0.0                |

  Scenario: Waiver closes the loan and accrues the income of the part that was paid
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 1000            | 100000       | 18                | 0        |
    And Global config "charge-accrual-date" value set to "due-date"
    And Customer makes repayment on "01 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "01 June 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "01 June 2026" due date and 100.0 transaction amount
    Then Working Capital loan status will be "ACTIVE"
# Settling only part of the charge keeps the loan open, so the closure - and with it the accrual of the part that
# was paid - can only come from the waiver: the accrual COB step for this date ran before the charge existed.
    When Admin makes a charge adjustment for the last added charge with 40.0 amount on working capital loan
    Then Working Capital loan status will be "ACTIVE"
    When Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "60.0" EUR amount
    And Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 40.0     | 0.0            | 0.0                 | 0.0          |
    And Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Repayment          | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 01 June 2026    | Charge Adjustment  | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
      | 01 June 2026    | Waive loan charges | 60.0              | 0.0              | 0.0               | 0.0                   | false    |
      | 01 June 2026    | Accrual            | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
    And Working Capital Loan Transactions tab has a "ACCRUAL" transaction with date "01 June 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 40.0  |        |
      | INCOME | 404007       | Fee Income              |       | 40.0   |

  Scenario: Waiver on a written-off loan is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    When Admin sets the business date to "15 January 2026"
    And Admin writes off the Working Capital loan on "15 January 2026"
    Then Working Capital loan status will be "CLOSED_WRITTEN_OFF"
    And Waiving the last added charge on working capital loan results an error with the following data:
      | httpCode | errorMessage                                       |
      | 403      | Charge waiver is not supported for the status of   |

  Scenario: A repayment backdated before a charge waiver leaves the waived amount untouched
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2026" due date and 100.0 transaction amount
    When Admin sets the business date to "20 January 2026"
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
# The backdated payment replays the whole history, and the replay resets only the paid distribution - the waived
# bucket has to come through it untouched.
    When Customer makes repayment on "12 January 2026" with 50.0 transaction amount on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 12 January 2026 | Repayment          | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Waive loan charges | 100.0             | 0.0              | 0.0               | 0.0                   | false    |
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Amount Paid | Amount Waived | Amount Outstanding |
      | Working Capital Loan Fee | 15 January 2026 | 100.0  | 0.0         | 100.0         | 0.0                |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 100.0      | 0.0             | 0.0      | 0.0            | 0.0                 | 0.0          |

  Scenario: A waived charge is reported with its waived amount and zero outstanding
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
# The paid flag stays false on a waived charge - only a payment sets it - and the waived amount is what explains
# the zero outstanding.
    And Working Capital Loan has charges with the following data:
      | Charge Name              | Due Date        | Amount | Amount Paid | Amount Waived | Amount Outstanding | Paid  |
      | Working Capital Loan Fee | 10 January 2026 | 100.0  | 0.0         | 100.0         | 0.0                | false |

  Scenario: A waived charge is not accrued afterwards
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2026" due date and 100.0 transaction amount
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
    When Admin sets the business date to "16 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital Loan has transactions:
      | transactionDate | type               | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement       | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Waive loan charges | 100.0             | 0.0              | 0.0               | 0.0                   | false    |

  Scenario: Adjusting a fully waived charge is rejected
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
    And Admin waives the last added charge on working capital loan
    Then a Working Capital Loan Charge Waiver transaction business event is raised with "100.0" EUR amount
# Without the waived part taken out of the amount available for adjustment, this call would pass and then settle
# principal instead of the charge it names, because the allocator caps every charge at its outstanding.
    And Making a charge adjustment with 100.0 amount on working capital loan results an error with the following data:
      | httpCode | errorMessage                                                                        |
      | 403      | Transaction amount cannot be higher than the available charge amount for adjustment |

  Scenario: Passing an amount to the charge waiver request is rejected
    Given Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Global config "charge-accrual-date" value set to "due-date"
    And Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 100.0 transaction amount
# A waiver always clears the whole remainder, so asking for a partial one must not even be expressible.
    Then Waiving the last added charge on working capital loan with an amount results an error with the following data:
      | httpCode | errorMessage                          |
      | 400      | The parameter amount is not supported |
