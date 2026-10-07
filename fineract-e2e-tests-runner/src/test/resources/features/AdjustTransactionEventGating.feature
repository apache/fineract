@ExternalEventConfigurationScenario
@AdjustTransactionEventGatingFeature
Feature: Adjust transaction business event gating by the transaction type event

  @TestRailId:C111037 @AdvancedPaymentAllocation
  Scenario: Verify that disabled re-age transaction event suppresses the adjust event of a replayed re-age in bulk event while adjust events of other transaction types are still raised
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2024"
    And Admin adds "LOAN_NSF_FEE" due date charge with "10 January 2024" due date and 150 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "10 January 2024" with 100 EUR transaction amount
    When Admin sets the business date to "11 January 2024"
    And Customer makes "AUTOPAY" repayment on "11 January 2024" with 100 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2024  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    |
      | 10 January 2024  | Repayment        | 100.0  | 0.0       | 0.0      | 0.0  | 100.0     | 750.0        | false    |
      | 11 January 2024  | Repayment        | 100.0  | 50.0      | 0.0      | 0.0  | 50.0      | 700.0        | false    |
    When Admin sets the business date to "20 February 2024"
    # re-age transaction event disabled: neither the re-age event nor the adjust event of a replayed re-age may be posted
    When Admin disables external business event "LoanReAgeTransactionBusinessEvent"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 01 March 2024 | 6                    |
    Then LoanReAgeBusinessEvent is created
    # down payment reversal replays both repayments (allocation changes) and the re-age (amount changes):
    # the bulk event carries the adjust events of the repayments only
    When Customer undo "1"th "Down Payment" transaction made on "01 January 2024"
    Then "Repayment" transaction on "10 January 2024" got reverse-replayed on "20 February 2024"
    And LoanAdjustTransactionBusinessEvent is not raised for any "Re-age" transaction of the loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2024  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | true     |
      | 10 January 2024  | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 11 January 2024  | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 800.0        | false    |
      | 20 February 2024 | Re-age           | 800.0  | 800.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
    # re-age transaction event enabled again: the same kind of replay posts the adjust event of the re-age too
    When Admin enables external business event "LoanReAgeTransactionBusinessEvent"
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    Then "Re-age" transaction on "20 February 2024" got reverse-replayed on "20 February 2024"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2024  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | true     |
      | 01 January 2024  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    |
      | 10 January 2024  | Repayment        | 100.0  | 0.0       | 0.0      | 0.0  | 100.0     | 750.0        | false    |
      | 11 January 2024  | Repayment        | 100.0  | 50.0      | 0.0      | 0.0  | 50.0      | 700.0        | false    |
      | 20 February 2024 | Re-age           | 700.0  | 700.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C111038 @AdvancedPaymentAllocation
  Scenario: Verify that disabled adjust transaction event suppresses the adjust events of every transaction type even if the transaction type events are enabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2024"
    And Customer makes "AUTOPAY" repayment on "10 January 2024" with 100 EUR transaction amount
    When Admin sets the business date to "20 February 2024"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 01 March 2024 | 6                    |
    When Admin disables external business event "LoanAdjustTransactionBusinessEvent"
    # repayment reversal: direct adjust event of the repayment + adjust event of the replayed re-age
    When Customer makes a repayment undo on "10 January 2024" without event check
    Then LoanAdjustTransactionBusinessEvent is not raised for any "Repayment" transaction of the loan
    And LoanAdjustTransactionBusinessEvent is not raised for any "Re-age" transaction of the loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2024  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    |
      | 10 January 2024  | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 650.0        | true     |
      | 20 February 2024 | Re-age           | 750.0  | 750.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C111039 @AdvancedPaymentAllocation
  Scenario: Verify that disabled repayment transaction event suppresses the adjust event of a reversed repayment while the adjust event of a reversed goodwill credit is still raised
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "10 January 2024"
    And Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "10 January 2024" with 50 EUR transaction amount and system-generated Idempotency key
    And Customer makes "AUTOPAY" repayment on "10 January 2024" with 100 EUR transaction amount
    When Admin disables external business event "LoanTransactionMakeRepaymentPostBusinessEvent"
    When Customer makes a repayment undo on "10 January 2024" without event check
    Then LoanAdjustTransactionBusinessEvent is not raised for any "Repayment" transaction of the loan
    # the goodwill credit event is enabled: its reversal still posts the adjust event (asserted by the undo step)
    When Customer undo "1"th "Goodwill Credit" transaction made on "10 January 2024"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 January 2024  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    |
      | 10 January 2024  | Goodwill Credit  | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 700.0        | true     |
      | 10 January 2024  | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 600.0        | true     |

  @TestRailId:C111040 @WorkingCapital
  Scenario: Verify that disabled Working Capital transaction type event suppresses the adjust event of that transaction type on reversal and on reprocessing while adjust events of other transaction types are still raised
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
    And Customer makes "PAYOUT_REFUND" transaction on "05 January 2026" with 30.0 transaction amount on Working Capital loan
    When Admin sets the business date to "10 January 2026"
    And Customer makes repayment on "10 January 2026" with 25 transaction amount on Working Capital loan
    # payout refund event disabled: its reversal posts no adjust event, the reprocessed repayment still does
    When Admin disables external business event "WorkingCapitalLoanPayoutRefundTransactionBusinessEvent"
    And Customer undo "1"th "PAYOUT_REFUND" transaction made on "05 January 2026" on Working Capital loan
    Then a Working Capital Loan Adjust Transaction business event is raised for the "repayment" transaction on "10 January 2026" with principal portion changed from "25.0" to "20.0" and fee portion changed from "0.0" to "5.0"
    And a Working Capital Loan Adjust Transaction business event is not raised for any "payoutRefund" transaction
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 30.0              | 25.0             | 5.0               | 0.0                   | true     |
      | 10 January 2026 | Repayment     | 25.0              | 20.0             | 5.0               | 0.0                   | false    |
    # repayment event disabled: the repayment reprocessed by a backdated repayment posts no adjust event
    When Admin enables external business event "WorkingCapitalLoanPayoutRefundTransactionBusinessEvent"
    And Admin disables external business event "WorkingCapitalLoanRepaymentTransactionBusinessEvent"
    And Customer makes repayment on "05 January 2026" with 25 transaction amount on Working Capital loan
    Then a Working Capital Loan Adjust Transaction business event is not raised for any "repayment" transaction
    And Working Capital Loan has transactions:
      | transactionDate | type          | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement  | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Payout Refund | 30.0              | 25.0             | 5.0               | 0.0                   | true     |
      | 05 January 2026 | Repayment     | 25.0              | 20.0             | 5.0               | 0.0                   | false    |
      | 10 January 2026 | Repayment     | 25.0              | 25.0             | 0.0               | 0.0                   | false    |
    When Admin enables external business event "WorkingCapitalLoanRepaymentTransactionBusinessEvent"
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"

  @TestRailId:C111041 @WorkingCapital
  Scenario: Verify that disabled Working Capital adjust transaction event suppresses the adjust events of reversed and reprocessed transactions even if the transaction type events are enabled
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
    When Admin disables external business event "WorkingCapitalLoanAdjustTransactionBusinessEvent"
    And Customer undo "1"th "REPAYMENT" transaction made on "05 January 2026" on Working Capital loan
    Then a Working Capital Loan Adjust Transaction business event is not raised for any "repayment" transaction
    And Working Capital Loan has transactions:
      | transactionDate | type         | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment    | 30.0              | 25.0             | 5.0               | 0.0                   | true     |
      | 10 January 2026 | Repayment    | 25.0              | 20.0             | 5.0               | 0.0                   | false    |
    When Admin enables external business event "WorkingCapitalLoanAdjustTransactionBusinessEvent"
    Then Admin closes the Working Capital loan with a full repayment on "10 January 2026"
