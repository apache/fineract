@WorkingCapital
@WorkingCapitalLoanPrepaymentFeature
Feature: Working Capital Loan Prepayment

  @TestRailId:C102525
  Scenario: Verify working capital loan prepayment - UC1: payoff template quotes principal only, and the prepayment closes the loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Partial repayment so the payoff quote is net of paid principal ---
    And Customer makes repayment on "10 January 2026" with 1000.0 transaction amount on Working Capital loan
    # --- Prepayment template ---
    Then Working Capital loan prepayment template on "10 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | expectedAmount |
      | 8000.0           | 0.0               | 0.0                   | 8000.0            |
    # --- Full repayment / prepayment ---
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 0.0 |
      | balance.principalOutstanding | 0.0 |
      | balance.overpaymentAmount    | 0.0 |

  @TestRailId:C102526
  Scenario: Verify working capital loan prepayment - UC2: payoff template splits principal, fee and penalty
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Charges ---
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 35.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "10 January 2026" due date and 20.0 transaction amount
    # --- Prepayment template ---
    Then Working Capital loan prepayment template on "10 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | expectedAmount |
      | 9000.0           | 35.0              | 20.0                  | 9055.0            |
    # --- Full repayment / prepayment ---
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 0.0 |
      | balance.principalOutstanding | 0.0 |
      | balance.overpaymentAmount    | 0.0 |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 35.0       | 0.0             | 35.0     | 20.0           | 0.0                 | 20.0         |

  @TestRailId:C102527
  Scenario: Verify working capital loan prepayment - UC3: payoff template and prepayment by loan external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Prepayment template by external ID ---
    Then Working Capital loan prepayment template by loan external ID on "10 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | expectedAmount |
      | 9000.0           | 0.0               | 0.0                   | 9000.0            |
    # --- Full repayment / prepayment by external ID ---
    Then Admin closes the Working Capital loan with all obligations met with a full repayment by loan external ID on "10 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding | 0.0 |

  @TestRailId:C102528
  Scenario: Verify working capital loan prepayment - UC4: omitting the transaction date quotes the payoff as of the business date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Partial repayment so the no-date quote reflects remaining principal ---
    And Customer makes repayment on "10 January 2026" with 500.0 transaction amount on Working Capital loan
    # --- Prepayment template without explicit transaction date ---
    Then Working Capital loan prepayment template without an explicit transaction date has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | expectedAmount |
      | 8500.0           | 0.0               | 0.0                   | 8500.0            |
    # --- Full repayment / prepayment ---
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  # The quote is the balance as of the date asked for. Nothing was disbursed, charged or adjusted between 10 and 15
  # January, so the 15 January quote is the same as today's and still closes the loan when posted back on that date.
  @TestRailId:C102529
  Scenario: Verify working capital loan prepayment - UC5: backdated prepayment closes the loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 1000.0 transaction amount on Working Capital loan
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Backdated prepayment ---
    Then Working Capital loan prepayment template on "15 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | expectedAmount |
      | 8000.0           | 0.0               | 0.0                   | 8000.0            |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "15 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding  | 0.0 |
      | balance.overpaymentAmount | 0.0 |

  # A charge is part of the payoff from the day it is added, not from the day it falls due: this one is added on the
  # date being quoted for, so it counts even though it is not due until the 20th. Contrast UC13, where the charge is
  # added after the quoted date and drops out.
  @TestRailId:C102530
  Scenario: Verify working capital loan prepayment - UC6: a charge due after the prepayment date is included in the payoff
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Charge due after the intended prepayment date ---
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 January 2026" due date and 40.0 transaction amount
    # --- Prepayment template ---
    Then Working Capital loan prepayment template on "10 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | expectedAmount |
      | 9000.0           | 40.0              | 0.0                   | 9040.0            |
    # --- Full repayment / prepayment ---
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding  | 0.0 |
      | balance.overpaymentAmount | 0.0 |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 40.0       | 0.0             | 40.0     | 0.0            | 0.0                 | 0.0          |

  @TestRailId:C102531
  Scenario: Verify working capital loan prepayment - UC7: reversing a prepayment restores the outstanding balance
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Full repayment / prepayment ---
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"
    # --- Reversal ---
    When Customer undo "1"th "REPAYMENT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 9000.0 |
      | balance.principalOutstanding | 9000.0 |
    # --- Prepayment template after reversal ---
    Then Working Capital loan prepayment template on "10 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | expectedAmount |
      | 9000.0           | 0.0               | 0.0                   | 9000.0            |
    # --- Re-close the loan ---
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C102532
  Scenario: Verify working capital loan prepayment - UC8: an already closed loan is quoted at zero
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Full repayment / prepayment ---
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"
    # --- Prepayment template on closed loan ---
    Then Working Capital loan prepayment template on "10 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | expectedAmount |
      | 0.0              | 0.0               | 0.0                   | 0.0               |

  @TestRailId:C102533
  Scenario: Verify working capital loan prepayment - UC9: an unsupported template command results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    # --- Negative: unsupported template command ---
    Then Fetching the Working Capital loan transaction template with command "notASupportedCommand" results an error with the following data:
      | httpCode | errorMessage                                                                 |
      | 400      | The query parameter command has an unsupported value of: notASupportedCommand |
    # --- Full repayment / prepayment ---
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 January 2026"

  @TestRailId:C102534
  Scenario: Verify working capital loan prepayment - UC10: the payoff template for a non-existent loan results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    # --- Negative: non-existent loan ---
    Then Fetching the Working Capital loan prepayment template for a non-existent loan results an error with the following data:
      | httpCode | errorMessage                                                    |
      | 404      | Working Capital Loan with identifier 999999999 does not exist |

  # Payments are deliberately not scoped to the quoted date - only what is owed is. The quote stays net of the
  # 10 January repayment, so posting it back on 05 January adds up to exactly the total due and nothing spills into
  # overpayment. Quoting the un-netted balance here would close the loan and then overpay it by the repayment.
  @TestRailId:C102535
  Scenario: Verify working capital loan prepayment - UC11: prepayment backdated before an existing payment closes the loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 1000.0 transaction amount on Working Capital loan
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Backdated prepayment before existing payment ---
    Then Working Capital loan prepayment template on "05 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | expectedAmount |
      | 8000.0           | 0.0               | 0.0                   | 8000.0            |
    # --- Full repayment / prepayment ---
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 0.0 |
      | balance.principalOutstanding | 0.0 |
      | balance.overpaymentAmount    | 0.0 |

  # Same as UC11 but with a charge on the account, which routes the backdated prepayment through a full transaction
  # replay. The charge was added after the quoted date so it is out of scope, but it had already been paid, and a
  # bucket cannot go negative - the buckets are reshuffled, yet the totals still land on zero.
  @TestRailId:C102536
  Scenario: Verify working capital loan prepayment - UC12: prepayment backdated before an existing payment with a charge on the account
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Charge and repayment before the backdated prepayment ---
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 40.0 transaction amount
    And Customer makes repayment on "10 January 2026" with 1000.0 transaction amount on Working Capital loan
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 40.0       | 0.0             | 40.0     | 0.0            | 0.0                 | 0.0          |
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Backdated prepayment before existing payment ---
    Then Working Capital loan prepayment template on "05 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | expectedAmount |
      | 8040.0           | 0.0               | 0.0                   | 8040.0            |
    # --- Full repayment / prepayment ---
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 0.0 |
      | balance.principalOutstanding | 0.0 |
      | balance.overpaymentAmount    | 0.0 |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 40.0       | 0.0             | 40.0     | 0.0            | 0.0                 | 0.0          |

  # The sharp edge of backdating: a charge added after the date being quoted for. It was not owed on 05 January, so it
  # is not part of that day's payoff - the quote covers the principal alone and leaves the fee behind. The loan stays
  # ACTIVE rather than absorbing an obligation that did not exist yet, and closes once the fee is quoted in too.
  @TestRailId:C102537
  Scenario: Verify working capital loan prepayment - UC13: a charge added after the quoted date is left out of the payoff
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 1000.0 transaction amount on Working Capital loan
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Unpaid charge after the intended backdated prepayment date ---
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 January 2026" due date and 40.0 transaction amount
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 40.0       | 40.0            | 0.0      | 0.0            | 0.0                 | 0.0          |
    # --- Backdated prepayment before the charge was added ---
    Then Working Capital loan prepayment template on "05 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | expectedAmount |
      | 8000.0           | 0.0               | 0.0                   | 8000.0         |
    # --- Posting that payoff clears the principal but not the later fee ---
    And Customer makes repayment on "05 January 2026" with 8000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 40.0 |
      | balance.principalOutstanding | 0.0  |
      | balance.overpaymentAmount    | 0.0  |
    # --- On the business date the fee is in scope, so the quote picks it up and closes the loan ---
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding  | 0.0 |
      | balance.overpaymentAmount | 0.0 |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 40.0       | 0.0             | 40.0     | 0.0            | 0.0                 | 0.0          |

  @TestRailId:C102538
  Scenario: Verify working capital loan prepayment - UC14: prepayment dated in the future is rejected (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Prepayment template ---
    Then Working Capital loan prepayment template on "10 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | expectedAmount |
      | 9000.0           | 0.0               | 0.0                   | 9000.0            |
    # --- Negative: future repayment date ---
    Then Customer fails to make repayment on "15 January 2026" with 9000.0 transaction amount on Working Capital loan due to future date
    # --- Full repayment / prepayment ---
    And Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  @TestRailId:C102539
  Scenario: Verify working capital loan prepayment - UC15: prepayment raises the repayment transaction business event
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Full repayment / prepayment ---
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"
    # --- Business event ---
    And a Working Capital Loan Repayment transaction business event is raised with "9000" EUR amount

  @TestRailId:C102540
  Scenario: Verify working capital loan prepayment - UC16: prepayment creates the same journal entries as a normal repayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct           | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM   | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Full repayment / prepayment ---
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"
    # --- Journal entries ---
    And Working Capital Loan Transactions tab has a "REPAYMENT" transaction with date "10 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account | 9000.0 |        |
      | ASSET     | 112601       | Loans Receivable          |        | 9000.0 |

  @TestRailId:C102541
  Scenario: Verify working capital loan prepayment - UC17: reversing a prepayment raises the adjust transaction business event
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # --- Full repayment / prepayment ---
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"
    # --- Reversal ---
    And Customer undo "1"th "REPAYMENT" transaction made on "10 January 2026" on Working Capital loan
    # --- Business event ---
    Then a Working Capital Loan Adjust Transaction business event is raised for the reversed "repayment" transaction
    # --- Re-close the loan ---
    And Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"
