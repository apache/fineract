@WorkingCapital
@WorkingCapitalLoanPrepaymentFeature
Feature: Working Capital Loan Prepayment

  # TODO: assign TestRailId once the prepayment test cases are created in TestRail
  Scenario: Verify working capital loan prepayment - UC1: payoff template quotes principal only, and the prepayment closes the loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital loan prepayment template on "10 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | transactionAmount |
      | 8000.0           | 0.0               | 0.0                   | 8000.0            |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 0.0 |
      | balance.principalOutstanding | 0.0 |
      | balance.overpaymentAmount    | 0.0 |

  # TODO: assign TestRailId once the prepayment test cases are created in TestRail
  Scenario: Verify working capital loan prepayment - UC2: payoff template splits principal, fee and penalty
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 35.0 transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_PENALTY" specified due date charge to working capital loan with "10 January 2026" due date and 20.0 transaction amount
    Then Working Capital loan prepayment template on "10 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | transactionAmount |
      | 9000.0           | 35.0              | 20.0                  | 9055.0            |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 0.0 |
      | balance.principalOutstanding | 0.0 |
      | balance.overpaymentAmount    | 0.0 |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 35.0       | 0.0             | 35.0     | 20.0           | 0.0                 | 20.0         |

  # TODO: assign TestRailId once the prepayment test cases are created in TestRail
  Scenario: Verify working capital loan prepayment - UC3: payoff template and prepayment by loan external ID
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan prepayment template by loan external ID on "10 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | transactionAmount |
      | 9000.0           | 0.0               | 0.0                   | 9000.0            |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment by loan external ID on "10 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding | 0.0 |

  # TODO: assign TestRailId once the prepayment test cases are created in TestRail
  Scenario: Verify working capital loan prepayment - UC4: omitting the transaction date quotes the payoff as of the business date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 500.0 transaction amount on Working Capital loan
    Then Working Capital loan prepayment template without an explicit transaction date has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | transactionAmount |
      | 8500.0           | 0.0               | 0.0                   | 8500.0            |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  # A Working Capital loan accrues nothing over time, so the payoff quote does not depend on the transaction date: the
  # quote taken on the later business date still closes the loan when it is posted back on the earlier date.
  # TODO: assign TestRailId once the prepayment test cases are created in TestRail
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
    Then Working Capital loan prepayment template on "15 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | transactionAmount |
      | 8000.0           | 0.0               | 0.0                   | 8000.0            |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "15 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding  | 0.0 |
      | balance.overpaymentAmount | 0.0 |

  # A charge dated after the prepayment is still part of the payoff, and the repayment allocation order of the product
  # settles it, so the loan closes rather than landing overpaid.
  # TODO: assign TestRailId once the prepayment test cases are created in TestRail
  Scenario: Verify working capital loan prepayment - UC6: a charge due after the prepayment date is included in the payoff
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 January 2026" due date and 40.0 transaction amount
    Then Working Capital loan prepayment template on "10 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | transactionAmount |
      | 9000.0           | 40.0              | 0.0                   | 9040.0            |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding  | 0.0 |
      | balance.overpaymentAmount | 0.0 |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 40.0       | 0.0             | 40.0     | 0.0            | 0.0                 | 0.0          |

  # TODO: assign TestRailId once the prepayment test cases are created in TestRail
  Scenario: Verify working capital loan prepayment - UC7: reversing a prepayment restores the outstanding balance
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"
    When Customer undo "1"th "REPAYMENT" transaction made on "10 January 2026" on Working Capital loan
    Then Working Capital loan status will be "ACTIVE"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 9000.0 |
      | balance.principalOutstanding | 9000.0 |
    Then Working Capital loan prepayment template on "10 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | transactionAmount |
      | 9000.0           | 0.0               | 0.0                   | 9000.0            |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"

  # TODO: assign TestRailId once the prepayment test cases are created in TestRail
  Scenario: Verify working capital loan prepayment - UC8: an already closed loan is quoted at zero
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "10 January 2026"
    Then Working Capital loan prepayment template on "10 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | transactionAmount |
      | 0.0              | 0.0               | 0.0                   | 0.0               |

  # TODO: assign TestRailId once the prepayment test cases are created in TestRail
  Scenario: Verify working capital loan prepayment - UC9: an unsupported template command results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    Then Fetching the Working Capital loan transaction template with command "notASupportedCommand" results an error with the following data:
      | httpCode | errorMessage                                                                 |
      | 400      | The query parameter command has an unsupported value of: notASupportedCommand |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "01 January 2026"

  # TODO: assign TestRailId once the prepayment test cases are created in TestRail
  Scenario: Verify working capital loan prepayment - UC10: the payoff template for a non-existent loan results an error (Negative)
    When Admin sets the business date to "01 January 2026"
    Then Fetching the Working Capital loan prepayment template for a non-existent loan results an error with the following data:
      | httpCode | errorMessage                                                    |
      | 404      | Working Capital Loan with identifier 999999999 does not exist |

  # The payoff quote is net of what has already been paid, so posting it before an existing payment still adds up to
  # exactly the total due: the loan closes with obligations met and nothing spills into overpayment.
  # TODO: assign TestRailId once the prepayment test cases are created in TestRail
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
    Then Working Capital loan prepayment template on "05 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | transactionAmount |
      | 8000.0           | 0.0               | 0.0                   | 8000.0            |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 0.0 |
      | balance.principalOutstanding | 0.0 |
      | balance.overpaymentAmount    | 0.0 |

  # Same as UC11 but with a charge on the account, which routes the backdated prepayment through a full transaction
  # replay. On the earlier date the charge is not yet due, so the prepayment settles principal and the later repayment
  # picks the charge up once it is due - the buckets are reshuffled, yet the totals still land on zero.
  # TODO: assign TestRailId once the prepayment test cases are created in TestRail
  Scenario: Verify working capital loan prepayment - UC12: prepayment backdated before an existing payment with a charge on the account
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 40.0 transaction amount
    And Customer makes repayment on "10 January 2026" with 1000.0 transaction amount on Working Capital loan
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 40.0       | 0.0             | 40.0     | 0.0            | 0.0                 | 0.0          |
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan prepayment template on "05 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | transactionAmount |
      | 8040.0           | 0.0               | 0.0                   | 8040.0            |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 0.0 |
      | balance.principalOutstanding | 0.0 |
      | balance.overpaymentAmount    | 0.0 |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 40.0       | 0.0             | 40.0     | 0.0            | 0.0                 | 0.0          |

  # The sharp edge of backdating: a charge that is still unpaid, quoted into the payoff, and a prepayment dated before
  # both the charge's due date and the repayment that follows it. Backdating past a later monetary action replays the
  # whole history, so on 05 January the replay allocates the payoff at a point where the 20 January fee is not yet due.
  # Cash-like accounting says the totals must still land on zero and the fee must come out paid: this scenario is what
  # holds that claim to account.
  # TODO: assign TestRailId once the prepayment test cases are created in TestRail
  Scenario: Verify working capital loan prepayment - UC13: prepayment backdated before an unpaid charge closes the loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "10 January 2026" with 1000.0 transaction amount on Working Capital loan
    When Admin sets the business date to "20 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "20 January 2026" due date and 40.0 transaction amount
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 40.0       | 40.0            | 0.0      | 0.0            | 0.0                 | 0.0          |
    Then Working Capital loan prepayment template on "05 January 2026" has the following data:
      | principalPortion | feeChargesPortion | penaltyChargesPortion | transactionAmount |
      | 8000.0           | 40.0              | 0.0                   | 8040.0            |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 January 2026"
    And Working capital loan details has the following field values:
      | balance.totalOutstanding     | 0.0 |
      | balance.principalOutstanding | 0.0 |
      | balance.overpaymentAmount    | 0.0 |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Outstanding | Fee Paid | Penalty Amount | Penalty Outstanding | Penalty Paid |
      | 40.0       | 0.0             | 40.0     | 0.0            | 0.0                 | 0.0          |
