@WorkingCapital
@WorkingCapitalLoanTransactionBusinessEventsFeature
Feature: Working Capital Loan Transaction Business Events

  @TestRailId:C94074
  Scenario: Working Capital loan raises Payout Refund and Goodwill Credit transaction business events
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan creation was successful
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "02 January 2026"
    And Customer makes "PAYOUT_REFUND" transaction on "02 January 2026" with 100.0 transaction amount on Working Capital loan
    Then a Working Capital Loan Payout Refund transaction business event is raised with "100.0" EUR amount
    And a Working Capital Loan Balance Changed business event is raised
    When Customer makes "GOODWILL_CREDIT" transaction on "02 January 2026" with 200.0 transaction amount on Working Capital loan
    Then a Working Capital Loan Goodwill Credit transaction business event is raised with "200.0" EUR amount
    And a Working Capital Loan Balance Changed business event is raised
    Then Admin closes the Working Capital loan with a full repayment on "02 January 2026"

  @TestRailId:C94075
  Scenario: Working Capital loan raises Transaction Reversed business event when a repayment and a payout refund are undone
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan creation was successful
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 100.0 transaction amount on Working Capital loan
    Then a Working Capital Loan Repayment transaction business event is raised with "100.0" EUR amount
    And a Working Capital Loan Balance Changed business event is raised
    And Customer undo "1"th "REPAYMENT" transaction made on "02 January 2026" on Working Capital loan
    Then a Working Capital Loan Transaction Reversed business event is raised for the "repayment" transaction
    And a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "03 January 2026"
    And Customer makes "PAYOUT_REFUND" transaction on "03 January 2026" with 150.0 transaction amount on Working Capital loan
    And a Working Capital Loan Balance Changed business event is raised
    And Customer undo "1"th "PAYOUT_REFUND" transaction made on "03 January 2026" on Working Capital loan
    Then a Working Capital Loan Transaction Reversed business event is raised for the "payoutRefund" transaction
    And a Working Capital Loan Balance Changed business event is raised
    Then Admin closes the Working Capital loan with a full repayment on "03 January 2026"

  @TestRailId:C98191
  Scenario: Working Capital loan raises Transaction Reversed business event when a discount fee adjustment is undone
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 |          |
    Then Working capital loan creation was successful
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Balance Changed business event is raised
    When Admin adds Discount fee with "12" amount on Working Capital loan account for last disbursement
    And Admin adds Discount fee adjustment with "5" amount on Working Capital loan account for last discount
    And Admin undo the last Discount fee adjustment on Working Capital loan account
    Then a Working Capital Loan Transaction Reversed business event is raised for the "discountFeeAdjustment" transaction
    And a Working Capital Loan Balance Changed business event is raised
    And Working Capital Loan has transactions:
      | transactionDate | type                    | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement            | 100.0             | 100.0            | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee            | 12.0              | 12.0             | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee Adjustment | 5.0               | 5.0              | 0.0               | 0.0                   | true     |
    When Admin sets the business date to "02 January 2026"
    Then Admin closes the Working Capital loan with a full repayment on "02 January 2026"

  @TestRailId:C94076
  Scenario: Working Capital loan raises Add Charge business event when a charge is added
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    Then Working capital loan creation was successful
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And a Working Capital Loan Balance Changed business event is raised on approval
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And a Working Capital Loan Balance Changed business event is raised
    When Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "15 January 2026" due date and 50.0 transaction amount
    Then a Working Capital Loan Add Charge business event is raised for charge "Working Capital Loan Fee" with "50.0" EUR amount
    And a Working Capital Loan Balance Changed business event is raised
    When Admin sets the business date to "15 January 2026"
    Then Admin closes the Working Capital loan with a full repayment on "15 January 2026"
