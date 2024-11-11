@LoanAccrualFeature
Feature: LoanAccrualTransaction

  @TestRailId:C2647
  Scenario: Verify that after COB job Accrual event is raised when loan has a fee-charge on disbursal date
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 January 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "02 January 2023"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has a transaction with date: "01 January 2023", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "01 January 2023"

  @TestRailId:C2648
  Scenario: Verify that after COB job Accrual event is raised when loan has a fee-charge on disbursal date with partial repayment
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 January 2023" due date and 10 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "02 January 2023"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has a transaction with date: "01 January 2023", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "01 January 2023"

  @TestRailId:C2649
  Scenario: Verify that after COB job Accrual event is raised when loan has a fee-charge on disbursal date with full repayment and loan is closed
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 January 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "02 January 2023"
    And Customer makes "AUTOPAY" repayment on "02 January 2023" with 1010 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has a transaction with date: "02 January 2023", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2023"

  @TestRailId:C2650
  Scenario: Verify that after COB job Accrual event is raised when loan has a fee-charge added with chargeback
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2023"
    And Customer makes "AUTOPAY" repayment on "02 January 2023" with 250 EUR transaction amount
    When Admin sets the business date to "03 January 2023"
    And Customer makes "AUTOPAY" repayment on "03 January 2023" with 250 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "04 January 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "04 January 2023"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "05 January 2023"
    And Customer makes "AUTOPAY" repayment on "05 January 2023" with 510 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 January 2023"
    Then Loan Transactions tab has a transaction with date: "05 January 2023", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 2
    Then Loan has 250 outstanding amount

  @TestRailId:C2651
  Scenario: Verify that after periodic accrual transaction job accrual event is raised when loan has a fee-charge added with waive charge and undo waive charge
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "01 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "01 January 2023" with "750" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2023"
    And Customer makes "AUTOPAY" repayment on "01 March 2023" with 250 EUR transaction amount
    When Admin sets the business date to "01 April 2023"
    And Customer makes "AUTOPAY" repayment on "01 April 2023" with 250 EUR transaction amount
    When Customer makes a repayment undo on "01 April 2023"
    When Admin sets the business date to "05 April 2023"
    And Admin adds an NSF fee because of payment bounce with "05 April 2023" transaction date
    When Admin sets the business date to "07 April 2023"
    And Admin waives charge
    When Admin sets the business date to "08 April 2023"
    And Admin makes waive undone for charge
    Then Loan status will be "ACTIVE"
    Then Loan has 260 outstanding amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Transactions tab has a transaction with date: "05 April 2023", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Accrual          | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 April 2023"

  @TestRailId:C2652
  Scenario: Verify that after periodic accrual transaction job accrual event is raised when loan has a fee-charge added when loan is closed
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2023"
    And Customer makes "AUTOPAY" repayment on "02 January 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "02 January 2023" due date and 10 EUR transaction amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Transactions tab has a transaction with date: "02 January 2023", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2023"

  @TestRailId:C2653
  Scenario: Verify that after disbursement and COB job Accrual event is raised when loan has a interest recalculation
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE | 01 January 2023   | 5000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2023" with "5000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "5000" EUR transaction amount
    When Admin sets the business date to "02 January 2023"
    When Admin runs the Add Periodic Accrual Transactions job
    Then Loan Transactions tab has a transaction with date: "02 January 2023", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Accrual          | 1.64   | 0.0       | 1.64     | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2023"

  @TestRailId:C2654
  Scenario: Verify that after loan is closed accrual event is raised when loan has a interest recalculation
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE | 01 January 2023   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2023"
    And Customer makes "AUTOPAY" repayment on "02 January 2023" with 1010.19 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has a transaction with date: "02 January 2023", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Accrual          | 10.19  | 0.0       | 10.19    | 0.0  | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "02 January 2023"

  @TestRailId:C2683
  Scenario: Verify that the final accrual is created when the loan goes to overpaid state
    When Admin sets the business date to "1 July 2023"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2023", with Principal: "5000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 July 2023" with "5000" amount and expected disbursement date on "1 July 2023"
    And Admin successfully disburse the loan on "1 July 2023" with "5000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 July 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "6 July 2023"
    And Customer makes "AUTOPAY" repayment on "6 July 2023" with 5011 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 July 2023   |              | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 August 2023 | 06 July 2023 | 0.0             | 5000.0        | 0.0      | 10.0 | 0.0       | 5010.0 | 5010.0 | 5010.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 5000          | 0        | 10   | 0         | 5010 | 5010 | 5010       | 0    | 0           |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 July 2023     | Disbursement     | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       |
      | 06 July 2023     | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 06 July 2023     | Repayment        | 5011.0 | 5000.0    | 0.0      | 10.0 | 0.0       | 0.0          |

  @TestRailId:C2684
  Scenario: Verify that the accrual transaction correctly created in case a CBR is applied on the loan
    When Admin sets the business date to "1 July 2023"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2023", with Principal: "5000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 July 2023" with "5000" amount and expected disbursement date on "1 July 2023"
    And Admin successfully disburse the loan on "1 July 2023" with "5000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 July 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "6 July 2023"
    And Customer makes "AUTOPAY" repayment on "6 July 2023" with 5011 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 July 2023   |              | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 August 2023 | 06 July 2023 | 0.0             | 5000.0        | 0.0      | 10.0 | 0.0       | 5010.0 | 5010.0 | 5010.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 5000          | 0        | 10   | 0         | 5010 | 5010 | 5010       | 0    | 0           |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 July 2023     | Disbursement     | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       |
      | 06 July 2023     | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 06 July 2023     | Repayment        | 5011.0 | 5000.0    | 0.0      | 10.0 | 0.0       | 0.0          |
    When Admin makes Credit Balance Refund transaction on "06 July 2023" with 1 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 July 2023   |              | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 August 2023 | 06 July 2023 | 0.0             | 5000.0        | 0.0      | 10.0 | 0.0       | 5010.0 | 5010.0 | 5010.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 5000          | 0        | 10   | 0         | 5010 | 5010 | 5010       | 0    | 0           |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type      | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 July 2023     | Disbursement          | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       |
      | 06 July 2023     | Accrual               | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 06 July 2023     | Repayment             | 5011.0 | 5000.0    | 0.0      | 10.0 | 0.0       | 0.0          |
      | 06 July 2023     | Credit Balance Refund | 1.0    | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C2685
  Scenario: Verify that the accrual transaction correctly created (overpay, undo repayment, overpay)
    When Admin sets the business date to "1 July 2023"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2023", with Principal: "5000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 July 2023" with "5000" amount and expected disbursement date on "1 July 2023"
    And Admin successfully disburse the loan on "1 July 2023" with "5000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 July 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "5 July 2023"
    And Customer makes "AUTOPAY" repayment on "5 July 2023" with 1000 EUR transaction amount
    When Admin sets the business date to "6 July 2023"
    And Customer makes "AUTOPAY" repayment on "6 July 2023" with 4011 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 July 2023   |              | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 August 2023 | 06 July 2023 | 0.0             | 5000.0        | 0.0      | 10.0 | 0.0       | 5010.0 | 5010.0 | 5010.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 5000          | 0        | 10   | 0         | 5010 | 5010 | 5010       | 0    | 0           |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 July 2023     | Disbursement     | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       |
      | 05 July 2023     | Repayment        | 1000.0 | 990.0     | 0.0      | 10.0 | 0.0       | 4010.0       |
      | 06 July 2023     | Repayment        | 4011.0 | 4010.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 06 July 2023     | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    And Customer makes a repayment undo on "6 July 2023"
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "7 July 2023"
    And Customer makes "AUTOPAY" repayment on "7 July 2023" with 4011 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 July 2023   |              | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 August 2023 | 07 July 2023 | 0.0             | 5000.0        | 0.0      | 10.0 | 0.0       | 5010.0 | 5010.0 | 5010.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 5000          | 0        | 10   | 0         | 5010 | 5010 | 5010       | 0    | 0           |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 July 2023     | Disbursement     | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       |
      | 05 July 2023     | Repayment        | 1000.0 | 990.0     | 0.0      | 10.0 | 0.0       | 4010.0       |
      | 06 July 2023     | Repayment        | 4011.0 | 4010.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 06 July 2023     | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 07 July 2023     | Repayment        | 4011.0 | 4010.0    | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C2686
  Scenario: Verify that the accrual transaction correctly created (overpay, undo repayment, add charge, overpay)
    When Admin sets the business date to "1 July 2023"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2023", with Principal: "5000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 July 2023" with "5000" amount and expected disbursement date on "1 July 2023"
    And Admin successfully disburse the loan on "1 July 2023" with "5000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 July 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "5 July 2023"
    And Customer makes "AUTOPAY" repayment on "5 July 2023" with 1000 EUR transaction amount
    When Admin sets the business date to "6 July 2023"
    And Customer makes "AUTOPAY" repayment on "6 July 2023" with 4011 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 July 2023   |              | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 August 2023 | 06 July 2023 | 0.0             | 5000.0        | 0.0      | 10.0 | 0.0       | 5010.0 | 5010.0 | 5010.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 5000          | 0        | 10   | 0         | 5010 | 5010 | 5010       | 0    | 0           |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 July 2023     | Disbursement     | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       |
      | 05 July 2023     | Repayment        | 1000.0 | 990.0     | 0.0      | 10.0 | 0.0       | 4010.0       |
      | 06 July 2023     | Repayment        | 4011.0 | 4010.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 06 July 2023     | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    And Customer makes a repayment undo on "6 July 2023"
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "7 July 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 July 2023" due date and 50 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "7 July 2023" with 4061 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date           | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 July 2023   |              | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 August 2023 | 07 July 2023 | 0.0             | 5000.0        | 0.0      | 60.0 | 0.0       | 5060.0 | 5060.0 | 5060.0     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 5000          | 0        | 60   | 0         | 5060 | 5060 | 5060       | 0    | 0           |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 July 2023     | Disbursement     | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       |
      | 05 July 2023     | Repayment        | 1000.0 | 940.0     | 0.0      | 60.0 | 0.0       | 4060.0       |
      | 06 July 2023     | Repayment        | 4011.0 | 4010.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 06 July 2023     | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 07 July 2023     | Repayment        | 4061.0 | 4060.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 07 July 2023     | Accrual          | 50.0   | 0.0       | 0.0      | 50.0 | 0.0       | 0.0          |

  @TestRailId:C2707
  Scenario: Verify that the accrual transaction is not reversed when multi disbursement happens
    When Admin sets the business date to "26 April 2023"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "26 April 2023", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "26 April 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    And Admin successfully disburse the loan on "26 April 2023" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "27 April 2023"
    And Admin successfully disburse the loan on "27 April 2023" with "30" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "27 April 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "28 April 2023"
    And  Admin runs COB job
    And Admin successfully disburse the loan on "28 April 2023" with "20" EUR transaction amount
    When Admin sets the business date to "29 April 2023"
     And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 26 April 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 27 April 2023 |           | 30.0            |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 28 April 2023 |           | 20.0            |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 26 May 2023   |           | 0.0             | 1050.0        | 0.0      | 10.0 | 0.0       | 1060.0 | 0.0  | 0.0        | 0.0  | 1060.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1050          | 0        | 10   | 0         | 1060 | 0    | 0          | 0    | 1060        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 26 April 2023    | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 27 April 2023    | Disbursement     | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 1030.0       |
      | 27 April 2023    | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 28 April 2023    | Disbursement     | 20.0   | 0.0       | 0.0      | 0.0  | 0.0       | 1050.0       |

  @TestRailId:C2708
  Scenario: Verify that the accrual is correct when it is on the installment start date
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 May 2023", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    And Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "1 May 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "2 May 2023"
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "8 May 2023"
    And Admin successfully disburse the loan on "8 May 2023" with "20" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 May 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 08 May 2023  |           | 20.0            |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 June 2023 |           | 0.0             | 1020.0        | 0.0      | 10.0 | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1020          | 0        | 10   | 0         | 1030 | 0    | 0          | 0    | 1030        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 May 2023      | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 May 2023      | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 08 May 2023      | Disbursement     | 20.0   | 0.0       | 0.0      | 0.0  | 0.0       | 1020.0       |

  @TestRailId:C2709
  Scenario:Verify that the accrual transaction is created for disbursement fee
    When Admin sets the business date to "1 May 2023"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 May 2023", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 May 2023" with "1000" amount and expected disbursement date on "1 May 2023"
    When Admin adds "LOAN_DISBURSEMENT_PERCENTAGE_FEE" charge with 1 % of transaction amount
    And Admin successfully disburse the loan on "1 May 2023" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 May 2023      | Disbursement                        | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 May 2023      | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 1000.0       |

  @TestRailId:C2710 @Specific
  Scenario: Verify global config charge-accrual-date function: single installment loan, charge-accrual-date = submitted-date, multiple charges with different submitted date
    When Global config "charge-accrual-date" value set to "submitted-date"
    When Admin sets the business date to "01 February 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 February 2023"
    And Admin successfully approves the loan on "01 February 2023" with "1000" amount and expected disbursement date on "1 February 2023"
    When Admin successfully disburse the loan on "01 February 2023" with "1000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 February 2023" due date and 10 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 February 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "02 February 2023"
    When Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 06 February 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Snooze fee | false     | Specified due date | 16 February 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2023 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 February 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 03 March 2023    |           | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 0.0  | 0.0        | 0.0  | 1020.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 0    | 0          | 0    | 1020        |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C2711
  Scenario: Verify global config charge-accrual-date function: single installment loan, charge-accrual-date = due-date, multiple charges with different submitted date
    When Global config "charge-accrual-date" value set to "due-date"
    When Admin sets the business date to "01 February 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 February 2023"
    And Admin successfully approves the loan on "01 February 2023" with "1000" amount and expected disbursement date on "1 February 2023"
    When Admin successfully disburse the loan on "01 February 2023" with "1000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 February 2023" due date and 10 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 February 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "07 February 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "17 February 2023"
    When Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 06 February 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Snooze fee | false     | Specified due date | 16 February 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2023 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 06 February 2023 | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 16 February 2023 | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 February 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 03 March 2023    |           | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 0.0  | 0.0        | 0.0  | 1020.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 0    | 0          | 0    | 1020        |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C2712 @Specific
  Scenario: Verify global config charge-accrual-date function: multiple installment loan, charge-accrual-date = submitted-date, multiple charges with different submitted date, due dates in same repayment period
    When Global config "charge-accrual-date" value set to "submitted-date"
    When Admin sets the business date to "01 February 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1       | 1 February 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 February 2023" with "3000" amount and expected disbursement date on "1 February 2023"
    When Admin successfully disburse the loan on "01 February 2023" with "3000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 February 2023" due date and 10 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 February 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "02 February 2023"
    When Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 06 February 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Snooze fee | false     | Specified due date | 16 February 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2023 | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 February 2023 |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 28   | 01 March 2023    |           | 2000.0          | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 0.0  | 0.0        | 0.0  | 1020.0      |
      | 2  | 31   | 01 April 2023    |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 3  | 30   | 01 May 2023      |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 20   | 0         | 3020 | 0    | 0          | 0    | 3020        |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C2713
  Scenario: Verify global config charge-accrual-date function: multiple installment loan, charge-accrual-date = due-date, multiple charges with different submitted date, due dates in same repayment period
    When Global config "charge-accrual-date" value set to "due-date"
    When Admin sets the business date to "01 February 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1       | 1 February 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 February 2023" with "3000" amount and expected disbursement date on "1 February 2023"
    When Admin successfully disburse the loan on "01 February 2023" with "3000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 February 2023" due date and 10 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 February 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "07 February 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "17 February 2023"
    When Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 06 February 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Snooze fee | false     | Specified due date | 16 February 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2023 | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 06 February 2023 | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 16 February 2023 | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 February 2023 |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 28   | 01 March 2023    |           | 2000.0          | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 0.0  | 0.0        | 0.0  | 1020.0      |
      | 2  | 31   | 01 April 2023    |           | 1000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 3  | 30   | 01 May 2023      |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 20   | 0         | 3020 | 0    | 0          | 0    | 3020        |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C2714 @Specific
  Scenario: Verify global config charge-accrual-date function: multiple installment loan, charge-accrual-date = submitted-date, multiple charges with different submitted date, due dates in different repayment periods
    When Global config "charge-accrual-date" value set to "submitted-date"
    When Admin sets the business date to "01 February 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1       | 1 February 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 February 2023" with "3000" amount and expected disbursement date on "1 February 2023"
    When Admin successfully disburse the loan on "01 February 2023" with "3000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 February 2023" due date and 10 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 March 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "02 February 2023"
    When Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 06 February 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Snooze fee | false     | Specified due date | 06 March 2023    | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2023 | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 01 February 2023 | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 February 2023 |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 28   | 01 March 2023    |           | 2000.0          | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
      | 2  | 31   | 01 April 2023    |           | 1000.0          | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
      | 3  | 30   | 01 May 2023      |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 20   | 0         | 3020 | 0    | 0          | 0    | 3020        |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C2715
  Scenario: Verify global config charge-accrual-date function: multiple installment loan, charge-accrual-date = due-date, multiple charges with different submitted date, due dates in different repayment periods
    When Global config "charge-accrual-date" value set to "due-date"
    When Admin sets the business date to "01 February 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1       | 1 February 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 February 2023" with "3000" amount and expected disbursement date on "1 February 2023"
    When Admin successfully disburse the loan on "01 February 2023" with "3000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 February 2023" due date and 10 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 March 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "07 February 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "07 March 2023"
    When Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 06 February 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Snooze fee | false     | Specified due date | 06 March 2023    | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2023 | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 06 February 2023 | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 06 March 2023    | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 February 2023 |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 28   | 01 March 2023    |           | 2000.0          | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
      | 2  | 31   | 01 April 2023    |           | 1000.0          | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
      | 3  | 30   | 01 May 2023      |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 20   | 0         | 3020 | 0    | 0          | 0    | 3020        |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C2716 @Specific
  Scenario: Verify global config charge-accrual-date function: single installment loan, charge-accrual-date = submitted-date, multi disbursement
    When Global config "charge-accrual-date" value set to "submitted-date"
    When Admin sets the business date to "01 February 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 February 2023"
    And Admin successfully approves the loan on "01 February 2023" with "1000" amount and expected disbursement date on "1 February 2023"
    When Admin successfully disburse the loan on "01 February 2023" with "500" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 February 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "02 February 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "04 February 2023"
    When Admin successfully disburse the loan on "04 February 2023" with "500" EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 06 February 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2023 | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 February 2023 | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 04 February 2023 | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 04 February 2023 |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 01 February 2023 |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 03 March 2023    |           | 0.0             | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 10   | 0         | 1010 | 0    | 0          | 0    | 1010        |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C2717 @Specific
  Scenario: Verify global config charge-accrual-date function: single installment loan, charge-accrual-date = submitted-date, repayment reversal
    When Global config "charge-accrual-date" value set to "submitted-date"
    When Admin sets the business date to "01 February 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 February 2023"
    And Admin successfully approves the loan on "01 February 2023" with "1000" amount and expected disbursement date on "1 February 2023"
    When Admin successfully disburse the loan on "01 February 2023" with "1000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 February 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "02 February 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "04 February 2023"
    And Customer makes "AUTOPAY" repayment on "04 February 2023" with 500 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "05 February 2023"
    When Customer undo "1"th "Repayment" transaction made on "04 February 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "06 February 2023"
    When Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 06 February 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2023 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2023 | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 04 February 2023 | Repayment        | 500.0  | 490.0     | 0.0      | 10.0 | 0.0       | 510.0        |
    Then On Loan Transactions tab the "Repayment" Transaction with date "04 February 2023" is reverted
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 February 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 03 March 2023    |           | 0.0             | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 10   | 0         | 1010 | 0    | 0          | 0    | 1010        |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C2718 @Specific
  Scenario: Verify global config charge-accrual-date function: single installment loan, charge-accrual-date = submitted-date, waive charge, undo waive
    When Global config "charge-accrual-date" value set to "submitted-date"
    When Admin sets the business date to "01 February 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 February 2023"
    And Admin successfully approves the loan on "01 February 2023" with "1000" amount and expected disbursement date on "1 February 2023"
    When Admin successfully disburse the loan on "01 February 2023" with "1000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 February 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "02 February 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "04 February 2023"
    And Admin waives due date charge
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "05 February 2023"
    And Admin makes waive undone for charge
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "06 February 2023"
    When Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 06 February 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2023 | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2023 | Accrual            | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 04 February 2023 | Waive loan charges | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 1000.0       |
    Then On Loan Transactions tab the "Waive loan charges" Transaction with date "04 February 2023" is reverted
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 February 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 03 March 2023    |           | 0.0             | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 10   | 0         | 1010 | 0    | 0          | 0    | 1010        |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C2719 @Specific
  Scenario: Verify global config charge-accrual-date function: multiple installment loan, charge-accrual-date = submitted-date, multi disbursement
    When Global config "charge-accrual-date" value set to "submitted-date"
    When Admin sets the business date to "01 February 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1       | 1 February 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 February 2023" with "3000" amount and expected disbursement date on "1 February 2023"
    When Admin successfully disburse the loan on "01 February 2023" with "2000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 March 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "02 February 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "04 February 2023"
    When Admin successfully disburse the loan on "04 February 2023" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 06 March 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2023 | Disbursement     | 2000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       |
      | 01 February 2023 | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 04 February 2023 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 February 2023 |           | 2000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 04 February 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 28   | 01 March 2023    |           | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 31   | 01 April 2023    |           | 1000.0          | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
      | 3  | 30   | 01 May 2023      |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 10   | 0         | 3010 | 0    | 0          | 0    | 3010        |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C2720 @Specific
  Scenario: Verify global config charge-accrual-date function: multiple installment loan, charge-accrual-date = submitted-date, repayment reversal
    When Global config "charge-accrual-date" value set to "submitted-date"
    When Admin sets the business date to "01 February 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1       | 1 February 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 February 2023" with "3000" amount and expected disbursement date on "1 February 2023"
    When Admin successfully disburse the loan on "01 February 2023" with "3000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 March 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "02 February 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "04 February 2023"
    And Customer makes "AUTOPAY" repayment on "04 February 2023" with 500 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "05 February 2023"
    When Customer undo "1"th "Repayment" transaction made on "04 February 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "06 February 2023"
    When Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 06 March 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2023 | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 04 February 2023 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 2500.0       |
    Then On Loan Transactions tab the "Repayment" Transaction with date "04 February 2023" is reverted
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 February 2023 |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 28   | 01 March 2023    |           | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 31   | 01 April 2023    |           | 1000.0          | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
      | 3  | 30   | 01 May 2023      |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 10   | 0         | 3010 | 0    | 0          | 0    | 3010        |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C2721 @Specific
  Scenario: Verify global config charge-accrual-date function: multiple installment loan, charge-accrual-date = submitted-date, waive charge, undo waive
    When Global config "charge-accrual-date" value set to "submitted-date"
    When Admin sets the business date to "01 February 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1       | 1 February 2023   | 3000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 February 2023" with "3000" amount and expected disbursement date on "1 February 2023"
    When Admin successfully disburse the loan on "01 February 2023" with "3000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 March 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "02 February 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "04 February 2023"
    And Admin waives due date charge
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "05 February 2023"
    And Admin makes waive undone for charge
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "06 February 2023"
    When Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 06 March 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2023 | Disbursement       | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Accrual            | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 04 February 2023 | Waive loan charges | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 3000.0       |
    Then On Loan Transactions tab the "Waive loan charges" Transaction with date "04 February 2023" is reverted
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 February 2023 |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 28   | 01 March 2023    |           | 2000.0          | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 31   | 01 April 2023    |           | 1000.0          | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
      | 3  | 30   | 01 May 2023      |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 3000          | 0        | 10   | 0         | 3010 | 0    | 0          | 0    | 3010        |
    When Global config "charge-accrual-date" value set to "due-date"


  @TestRailId:C2789 @Specific
  Scenario: Verify accrual transaction for new fee for loan with accrued snooze fee and schedule adjustment
    When Global config "charge-accrual-date" value set to "submitted-date"
    When Admin sets the business date to "19 May 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1       | 19 May 2023       | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "19 May 2023" with "1000" amount and expected disbursement date on "19 May 2023"
    When Admin successfully disburse the loan on "19 May 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "12 June 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "18 July 2023" due date and 10 EUR transaction amount
    When Batch API call with steps: rescheduleLoan from "18 June 2023" to "18 July 2023" submitted on date: "19 May 2023", approveReschedule on date: "19 May 2023" runs with enclosingTransaction: "true"
    When Admin sets the business date to "13 June 2023"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has a transaction with date: "12 June 2023", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    When Admin sets the business date to "18 July 2023"
    And Customer makes "AUTOPAY" repayment on "18 July 2023" with 1010 EUR transaction amount
    When Admin sets the business date to "19 July 2023"
    When Customer makes a repayment undo on "19 July 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "19 July 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "20 July 2023"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has a transaction with date: "19 July 2023", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Accrual          | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date         | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 19 May 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 60   | 18 July 2023 |           | 0.0             | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
      | 2  | 1    | 19 July 2023 |           | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0   | 0.0  | 0.0        | 0.0  | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 10   | 10        | 1020 | 0    | 0          | 0    | 1020        |
    When Global config "charge-accrual-date" value set to "due-date"

  @TestRailId:C3128
  Scenario: Verify that the final accrual calculation is correct when multiple Charges are added and waived
    When Admin sets the business date to "17 April 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 17 April 2024     | 750            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "17 April 2024" with "750" amount and expected disbursement date on "17 April 2024"
    When Admin successfully disburse the loan on "17 April 2024" with "750" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "17 April 2024" due date and 20 EUR transaction amount
    When Admin sets the business date to "18 April 2024"
    When Admin runs inline COB job for Loan
    And Admin waives due date charge
    When Admin sets the business date to "19 April 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "19 April 2024" due date and 55 EUR transaction amount
    When Admin sets the business date to "20 April 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "20 April 2024" with 55 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "20 April 2024" due date and 60 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "20 April 2024" with 810 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 20 April 2024 | Flat             | 60.0 | 60.0 | 0.0    | 0.0         |
      | NSF fee | true      | Specified due date | 19 April 2024 | Flat             | 55.0 | 55.0 | 0.0    | 0.0         |
      | NSF fee | true      | Specified due date | 17 April 2024 | Flat             | 20.0 | 0.0  | 20.0   | 0.0         |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Waived | Outstanding |
      |    |      | 17 April 2024 |               | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |        |             |
      | 1  | 0    | 17 April 2024 | 20 April 2024 | 562.5           | 187.5         | 0.0      | 0.0  | 0.0       | 187.5 | 187.5 | 0.0        | 187.5 | 0.0    | 0.0         |
      | 2  | 15   | 02 May 2024   | 20 April 2024 | 375.0           | 187.5         | 0.0      | 0.0  | 135.0     | 322.5 | 302.5 | 302.5      | 0.0   | 20.0   | 0.0         |
      | 3  | 15   | 17 May 2024   | 20 April 2024 | 187.5           | 187.5         | 0.0      | 0.0  | 0.0       | 187.5 | 187.5 | 187.5      | 0.0   | 0.0    | 0.0         |
      | 4  | 15   | 01 June 2024  | 20 April 2024 | 0.0             | 187.5         | 0.0      | 0.0  | 0.0       | 187.5 | 187.5 | 187.5      | 0.0   | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Waived | Outstanding |
      | 750.0         | 0.0      | 0.0  | 135.0     | 885.0 | 865.0 | 677.5      | 187.5 | 20.0   | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 17 April 2024    | Disbursement       | 750.0  | 0.0       | 0.0      | 0.0  | 0.0       | 750.0        |
      | 17 April 2024    | Accrual            | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 0.0          |
      | 17 April 2024    | Waive loan charges | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 750.0        |
      | 19 April 2024    | Accrual            | 55.0   | 0.0       | 0.0      | 0.0  | 55.0      | 0.0          |
      | 20 April 2024    | Repayment          | 55.0   | 55.0      | 0.0      | 0.0  | 0.0       | 695.0        |
      | 20 April 2024    | Repayment          | 810.0  | 695.0     | 0.0      | 0.0  | 115.0     | 0.0          |
      | 20 April 2024    | Accrual            | 60.0   | 0.0       | 0.0      | 0.0  | 60.0      | 0.0          |

  @TestRailId:C3139
  Scenario: Verify global config charge-accrual-date function: multiple installment loan, charge-accrual-date = submitted-date, multi disbursement, periodic accrual
    When Global config "charge-accrual-date" value set to "submitted-date"
    When Admin sets the business date to "01 April 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_SAR_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE_MULTIDISB | 01 April 2024     | 1000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 April 2024" with "1000" amount and expected disbursement date on "05 April 2024"
    When Admin sets the business date to "05 April 2024"
    When Admin successfully disburse the loan on "05 April 2024" with "500" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 05 April 2024 |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 15   | 20 April 2024 |           | 334.47          | 165.53        | 2.47     | 0.0  | 0.0       | 168.0  | 0.0  | 0.0        | 0.0  | 168.0       |
      | 2  | 15   | 05 May 2024   |           | 168.12          | 166.35        | 1.65     | 0.0  | 0.0       | 168.0  | 0.0  | 0.0        | 0.0  | 168.0       |
      | 3  | 15   | 20 May 2024   |           | 0.0             | 168.12        | 0.83     | 0.0  | 0.0       | 168.95 | 0.0  | 0.0        | 0.0  | 168.95      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 05 April 2024    | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 500.0         | 4.95     | 0.0  | 0.0       | 504.95 | 0.0  | 0.0        | 0.0  | 504.95      |
    When Admin sets the business date to "25 April 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 05 April 2024 |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 15   | 20 April 2024 |           | 334.47          | 165.53        | 2.47     | 0.0  | 0.0       | 168.0  | 0.0  | 0.0        | 0.0  | 168.0       |
      | 2  | 15   | 05 May 2024   |           | 168.94          | 165.53        | 2.47     | 0.0  | 0.0       | 168.0  | 0.0  | 0.0        | 0.0  | 168.0       |
      | 3  | 15   | 20 May 2024   |           | 0.0             | 168.94        | 0.83     | 0.0  | 0.0       | 169.77 | 0.0  | 0.0        | 0.0  | 169.77      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 05 April 2024    | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 20 April 2024    | Accrual          | 2.47   | 0.0       | 2.47     | 0.0  | 0.0       | 0.0          |
      | 24 April 2024    | Accrual          | 0.66   | 0.0       | 0.66     | 0.0  | 0.0       | 0.0          |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 500.0         | 5.77     | 0.0  | 0.0       | 505.77 | 0.0  | 0.0        | 0.0  | 505.77      |
    When Admin sets the business date to "26 April 2024"
    And Admin successfully disburse the loan on "26 April 2024" with "500" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date          | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 05 April 2024 |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 15   | 20 April 2024 |           | 165.47          | 334.53        | 2.47     | 0.0  | 0.0       | 337.0  | 0.0  | 0.0        | 0.0  | 337.0       |
      |    |      | 26 April 2024 |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 15   | 05 May 2024   |           | 332.42          | 333.05        | 3.95     | 0.0  | 0.0       | 337.0  | 0.0  | 0.0        | 0.0  | 337.0       |
      | 3  | 15   | 20 May 2024   |           | 0.0             | 332.42        | 1.64     | 0.0  | 0.0       | 334.06 | 0.0  | 0.0        | 0.0  | 334.06      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 05 April 2024    | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 20 April 2024    | Accrual          | 2.47   | 0.0       | 2.47     | 0.0  | 0.0       | 0.0          |
      | 24 April 2024    | Accrual          | 1.05   | 0.0       | 1.05     | 0.0  | 0.0       | 0.0          |
      | 26 April 2024    | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 8.06     | 0.0  | 0.0       | 1008.06 | 0.0  | 0.0        | 0.0  | 1008.06     |
    When Global config "charge-accrual-date" value set to "due-date"
