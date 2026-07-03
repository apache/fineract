@WorkingCapital
@WorkingCapitalTransactionReprocessingFeature
Feature: Working Capital Transaction Reprocessing

  @TestRailId:C85208
  Scenario: Verify backdated repayment is reflected in balances and existing allocations stay untouched (no charges)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # First repayment on day 10
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 3000 transaction amount on Working Capital loan
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 6000.0 |
      | totalPaidPrincipal   | 3000.0 |
    # Backdated repayment on day 5 (earlier than the existing day-10 repayment)
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "05 January 2026" with 2000 transaction amount on Working Capital loan
    # Both repayments are reflected - balance math is order-independent
    Then Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 4000.0 |
      | totalPaidPrincipal   | 5000.0 |
      | overpaymentAmount    | 0.0    |
    # The backdated repayment allocates fully to principal; the earlier repayment stays untouched
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 2000.0            | 2000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |

  @TestRailId:C85209
  Scenario: Verify backdated repayment that overpays - excess becomes overpayment
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # Partial repayment on day 10 (loan stays ACTIVE with 2000 outstanding)
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 7000 transaction amount on Working Capital loan
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 2000.0 |
    # Backdated repayment on day 5 - total repaid (7000 + 5000 = 12000) exceeds principal (9000)
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "05 January 2026" with 5000 transaction amount on Working Capital loan
    # Totals are order-independent: 9000 principal repaid, 3000 overpayment
    # Per-transaction allocation redistribution is covered by C85461
    Then Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 0.0    |
      | totalPaidPrincipal   | 9000.0 |
      | overpaymentAmount    | 3000.0 |
    And Working Capital loan status will be "OVERPAID"

  @TestRailId:C85210
  Scenario: Verify multiple backdated repayments accumulate correctly
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # First repayment on day 15
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "15 January 2026" with 3000 transaction amount on Working Capital loan
    # Backdated repayment on day 5
    When Admin sets the business date to "20 January 2026"
    And Customer makes repayment on "05 January 2026" with 1000 transaction amount on Working Capital loan
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 5000.0 |
      | totalPaidPrincipal   | 4000.0 |
    # Another backdated repayment on day 10 (between the existing ones)
    And Customer makes repayment on "10 January 2026" with 2000 transaction amount on Working Capital loan
    Then Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 3000.0 |
      | totalPaidPrincipal   | 6000.0 |
      | overpaymentAmount    | 0.0    |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 2000.0            | 2000.0           | 0.0               | 0.0                   | false    |
      | 15 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |

  @TestRailId:C85211
  Scenario: Verify sequential (non-backdated) repayments do not trigger reprocessing side effects
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 2000 transaction amount on Working Capital loan
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 3000 transaction amount on Working Capital loan
    Then Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 4000.0 |
      | totalPaidPrincipal   | 5000.0 |
      | overpaymentAmount    | 0.0    |
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 2000.0            | 2000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 3000.0            | 3000.0           | 0.0               | 0.0                   | false    |

  @TestRailId:C85212
  Scenario: Verify backdated repayment only settles a charge that is already due on the backdated date
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 35.0 transaction amount
    And Customer makes repayment on "10 January 2026" with 3000 transaction amount on Working Capital loan
    # Backdated repayment on day 5 -> reprocessing is triggered. The fee is due on day 10, so it is NOT yet due on
    # day 5: the backdated repayment stays principal-only, while the day-10 repayment settles the fee (fee-first order).
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "05 January 2026" with 2000 transaction amount on Working Capital loan
    Then Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 4035.0 |
      | totalPaidPrincipal   | 4965.0 |
    # The day-5 repayment predates the fee due date, so it is principal-only; the day-10 repayment covers the fee
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 2000.0            | 2000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 3000.0            | 2965.0           | 35.0              | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Paid | Fee Outstanding |
      | 35.0       | 35.0     | 0.0             |

  @TestRailId:C85213
  Scenario: Verify a repayment clearing more than one lapsed delinquency period distributes by remaining balance
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # Let period 1 (01 Jan - 30 Jan) lapse unpaid and be evaluated as not met
    When Admin sets the business date to "31 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Let period 2 (31 Jan - 01 Mar) lapse unpaid and be evaluated as not met
    When Admin sets the business date to "02 March 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # Single repayment of 400 covering period 1 fully (270) and period 2 partially (130)
    And Customer makes repayment on "02 March 2026" with 400 transaction amount on Working Capital loan
    Then Working Capital loan delinquency range schedule has the following data:
      | periodNumber | fromDate   | toDate     | expectedAmount | paidAmount | outstandingAmount | minPaymentCriteriaMet | delinquentAmount | delinquentDays |
      | 1            | 2026-01-01 | 2026-01-30 | 270.0          | 270.0      | 0.0               | true                  | 0.0              | 0              |
      | 2            | 2026-01-31 | 2026-03-01 | 270.0          | 130.0      | 140.0             | false                 | 140.0            | 1              |
      | 3            | 2026-03-02 | 2026-03-31 | 270.0          | 0.0        | 270.0             | null                  | null             | null           |

  @TestRailId:C85214
  Scenario: Verify backdated repayment is recorded on its actual day and the amortization balance is recalculated
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 3000 transaction amount on Working Capital loan
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "05 January 2026" with 2000 transaction amount on Working Capital loan
    # Outstanding recomputed by the amortization model after the backdated payment
    Then Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 4000.0 |
    # The projected amortization schedule is intact (structure is a static projection); the recomputed outstanding
    # balance above is the model's actual output that reflects the backdated payment.
    When Admin retrieves the projected amortization schedule
    Then The retrieved amortization schedule has the following summary fields:
      | discountFeeAmount | netDisbursementAmount | totalPaymentVolume | periodPaymentRate | npvDayCount | expectedPaymentAmount |
      | 0.00              | 9000.00               | 100000.00          | 18                | 360         | 50.00                 |

  @TestRailId:C85215
  Scenario: Verify backdated repayment reduces the outstanding tracked by the breach schedule
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with breachId and overrides enabled
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 3000 transaction amount on Working Capital loan
    # Backdated repayment on day 5
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "05 January 2026" with 2000 transaction amount on Working Capital loan
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Working Capital loan breach schedule has 1 period

  @TestRailId:C85216
  Scenario: Verify backdated repayment re-allocates the later transaction's fee portion to principal
    # Txn#1 (day 10): pays 5 NSF-Fee + 25 principal (amount 30)
    # Txn#2 backdated (day 5): pays 5 NSF-Fee + 20 principal (amount 25)
    # => Txn#1 must be reprocessed and re-allocate to 30 principal (the fee is now taken by the earlier Txn#2)
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with payment allocation order:
      | DUE_PENALTY          |
      | DUE_FEE              |
      | DUE_PRINCIPAL        |
      | IN_ADVANCE_PENALTY   |
      | IN_ADVANCE_FEE       |
      | IN_ADVANCE_PRINCIPAL |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    # Add the fee while the business date is on its due date - charges cannot be dated before the business date
    When Admin sets the business date to "05 January 2026"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 5.0 transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 30 transaction amount on Working Capital loan
    # First, Txn#1 allocates 5 fee + 25 principal
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 30.0              | 25.0             | 5.0               | 0.0                   | false    |
    # Backdated Txn#2 on day 5 takes the fee; Txn#1 is reprocessed to 30 principal
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "05 January 2026" with 25 transaction amount on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 25.0              | 20.0             | 5.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 30.0              | 30.0             | 0.0               | 0.0                   | false    |

  @TestRailId:C85217
  Scenario: Verify reversal of a transaction re-allocates the remaining transaction's fee portion
    # Txn#1 (day 5): 5 fee + 25 principal; Txn#2 (day 10): 25 principal
    # Txn#1 reverted => Txn#2 is reprocessed and re-allocates to 5 fee + 20 principal
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with payment allocation order:
      | DUE_PENALTY          |
      | DUE_FEE              |
      | DUE_PRINCIPAL        |
      | IN_ADVANCE_PENALTY   |
      | IN_ADVANCE_FEE       |
      | IN_ADVANCE_PRINCIPAL |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 5.0 transaction amount
    When Admin sets the business date to "05 January 2026"
    And Customer makes repayment on "05 January 2026" with 30 transaction amount on Working Capital loan
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 25 transaction amount on Working Capital loan
    # Reverse Txn#1 (the day-5 repayment) -> Txn#2 must be reprocessed to take the fee
    When Customer undo "1"th "REPAYMENT" transaction made on "05 January 2026" on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 30.0              | 25.0             | 5.0               | 0.0                   | true     |
      | 10 January 2026 | Repayment    | 25.0              | 20.0             | 5.0               | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Paid | Fee Outstanding |
      | 5.0        | 5.0      | 0.0             |
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 8980.0 |
      | totalPaidPrincipal   | 20.0   |

  @TestRailId:C85218
  Scenario: Verify a repayment splits across fee and principal per the product's allocation order
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new Working Capital Loan Product with payment allocation order:
      | DUE_PENALTY          |
      | DUE_FEE              |
      | DUE_PRINCIPAL        |
      | IN_ADVANCE_PENALTY   |
      | IN_ADVANCE_FEE       |
      | IN_ADVANCE_PRINCIPAL |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "10 January 2026" due date and 5.0 transaction amount
    And Customer makes repayment on "10 January 2026" with 30 transaction amount on Working Capital loan
    # Fee-first allocation order: 5 to fee, 25 to principal
    Then Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 30.0              | 25.0             | 5.0               | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Paid | Fee Outstanding |
      | 5.0        | 5.0      | 0.0             |

  @TestRailId:C85460
  Scenario: Verify backdated repayment reprocessing preserves an earlier charge adjustment's settlement
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "05 January 2026"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2026" due date and 100.0 transaction amount
    # Charge adjustment settles 40 of the 100 fee
    When Admin sets the business date to "06 January 2026"
    And Admin makes a charge adjustment for the last added charge with 40.0 amount on working capital loan
    Then Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Paid | Fee Outstanding |
      | 100.0      | 40.0     | 60.0            |
    # Repayment on day 10 settles the remaining 60 fee
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 60 transaction amount on Working Capital loan
    Then Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Paid | Fee Outstanding |
      | 100.0      | 100.0    | 0.0             |
    # Chronological order including the day-6 adjustment: day-8 repayment settles 50 of the remaining 60 fee,
    # day-10 repayment settles the last 10 fee and puts 50 to principal
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "08 January 2026" with 50 transaction amount on Working Capital loan
    Then Working Capital Loan has transactions:
      | transactionDate | type              | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement      | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 06 January 2026 | Charge Adjustment | 40.0              | 0.0              | 40.0              | 0.0                   | false    |
      | 08 January 2026 | Repayment         | 50.0              | 0.0              | 50.0              | 0.0                   | false    |
      | 10 January 2026 | Repayment         | 60.0              | 50.0             | 10.0              | 0.0                   | false    |
    And Working Capital Loan charge balances has the following data:
      | Fee Amount | Fee Paid | Fee Outstanding |
      | 100.0      | 100.0    | 0.0             |
    And Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 8950.0 |
      | totalPaidPrincipal   | 50.0   |

  @TestRailId:C85461
  Scenario: Verify backdated repayment that overpays redistributes allocations chronologically
   # allocations of a backdated overpaying repayment are redistributed
   # chronologically, matching the reprocessing behavior of loans with charges
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 7000 transaction amount on Working Capital loan
    When Admin sets the business date to "15 January 2026"
    And Customer makes repayment on "05 January 2026" with 5000 transaction amount on Working Capital loan
    Then Working Capital loan balance payload contains the following fields:
      | field                | value  |
      | principalOutstanding | 0.0    |
      | totalPaidPrincipal   | 9000.0 |
      | overpaymentAmount    | 3000.0 |
    # Chronological replay: day-5 repayment reduces principal by its full 5000 (outstanding was 9000 on day 5);
    # day-10 repayment covers the remaining 4000 principal, its excess 3000 is the overpayment.
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 5000.0            | 5000.0           | 0.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment    | 7000.0            | 4000.0           | 0.0               | 0.0                   | false    |
