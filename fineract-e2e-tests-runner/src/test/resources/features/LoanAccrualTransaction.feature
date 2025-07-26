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
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
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
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE | 01 January 2023   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2023"
    And Customer makes "AUTOPAY" repayment on "02 January 2023" with 1000.33 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Transactions tab has a transaction with date: "02 January 2023", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
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
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1         | 1 February 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
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
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1         | 1 February 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
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
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1         | 1 February 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
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
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1         | 1 February 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
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
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1         | 1 February 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
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
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1         | 1 February 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
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
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1         | 1 February 2023   | 3000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
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
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1         | 19 May 2023       | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
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
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
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
      | LoanProduct                                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
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

  @TestRailId:C3416
  Scenario: Verify the accrual activity creation in case of full repayment on maturity date
    When Admin sets the business date to "09 August 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_AUTO_DOWNPAYMENT_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 09 August 2024    | 200            | 9.9                    | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "09 August 2024" with "200" amount and expected disbursement date on "09 August 2024"
    And Admin successfully disburse the loan on "09 August 2024" with "200" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 09 August 2024    |                | 200.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 09 August 2024    | 09 August 2024 | 150.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 50.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 09 September 2024 |                | 112.98          | 37.02         | 1.26     | 0.0  | 0.0       | 38.28 | 0.0  | 0.0        | 0.0  | 38.28       |
      | 3  | 30   | 09 October 2024   |                | 75.62           | 37.36         | 0.92     | 0.0  | 0.0       | 38.28 | 0.0  | 0.0        | 0.0  | 38.28       |
      | 4  | 31   | 09 November 2024  |                | 37.97           | 37.65         | 0.63     | 0.0  | 0.0       | 38.28 | 0.0  | 0.0        | 0.0  | 38.28       |
      | 5  | 30   | 09 December 2024  |                | 0.0             | 37.97         | 0.31     | 0.0  | 0.0       | 38.28 | 0.0  | 0.0        | 0.0  | 38.28       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 3.12     | 0.0  | 0.0       | 203.12 | 50.0 | 0.0        | 0.0  | 153.12      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 August 2024   | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 09 August 2024   | Down Payment     | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
    When Admin sets the business date to "09 December 2024"
    And Customer makes "AUTOPAY" repayment on "09 December 2024" with 153.12 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 09 August 2024    |                  | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 09 August 2024    | 09 August 2024   | 150.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 50.0  | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 09 September 2024 | 09 December 2024 | 112.98          | 37.02         | 1.26     | 0.0  | 0.0       | 38.28 | 38.28 | 0.0        | 38.28 | 0.0         |
      | 3  | 30   | 09 October 2024   | 09 December 2024 | 75.62           | 37.36         | 0.92     | 0.0  | 0.0       | 38.28 | 38.28 | 0.0        | 38.28 | 0.0         |
      | 4  | 31   | 09 November 2024  | 09 December 2024 | 37.97           | 37.65         | 0.63     | 0.0  | 0.0       | 38.28 | 38.28 | 0.0        | 38.28 | 0.0         |
      | 5  | 30   | 09 December 2024  | 09 December 2024 | 0.0             | 37.97         | 0.31     | 0.0  | 0.0       | 38.28 | 38.28 | 0.0        | 0.0   | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 200.0         | 3.12     | 0.0  | 0.0       | 203.12 | 203.12 | 0.0        | 114.84 | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 09 August 2024    | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 09 August 2024    | Down Payment     | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 09 September 2024 | Accrual Activity | 1.26   | 0.0       | 1.26     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 October 2024   | Accrual Activity | 0.92   | 0.0       | 0.92     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 November 2024  | Accrual Activity | 0.63   | 0.0       | 0.63     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 December 2024  | Repayment        | 153.12 | 150.0     | 3.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 December 2024  | Accrual          | 3.12   | 0.0       | 3.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 December 2024  | Accrual Activity | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3531
  Scenario: Verify the accrual activity creation in case of full repayment on maturity date - absence of negative numbers  with mid range interest rate and small principal
    When Admin sets the business date to "19 February 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_AUTO_DOWNPAYMENT_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 19 February 2025  | 50             | 19.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "19 February 2025" with "50" amount and expected disbursement date on "19 February 2025"
    And Admin successfully disburse the loan on "19 February 2025" with "50" EUR transaction amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 50.0            |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 37.5            | 12.5          | 0.0      | 0.0  | 0.0       | 12.5 | 12.5 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     |                  | 34.61           | 2.89          | 0.58     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 3  | 31   | 19 April 2025     |                  | 31.73           | 2.88          | 0.59     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 4  | 30   | 19 May 2025       |                  | 28.78           | 2.95          | 0.52     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 5  | 31   | 19 June 2025      |                  | 25.8            | 2.98          | 0.49     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 6  | 30   | 19 July 2025      |                  | 22.75           | 3.05          | 0.42     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 7  | 31   | 19 August 2025    |                  | 19.67           | 3.08          | 0.39     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 8  | 31   | 19 September 2025 |                  | 16.53           | 3.14          | 0.33     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 9  | 30   | 19 October 2025   |                  | 13.33           | 3.2           | 0.27     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 10 | 31   | 19 November 2025  |                  | 10.09           | 3.24          | 0.23     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 11 | 30   | 19 December 2025  |                  | 6.79            | 3.3           | 0.17     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 12 | 31   | 19 January 2026   |                  | 3.44            | 3.35          | 0.12     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 3.44          | 0.06     | 0.0  | 0.0       | 3.5  | 0.0  | 0.0        | 0.0  | 3.5         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 50.0          | 4.17     | 0.0  | 0.0       | 54.17 | 12.5 | 0.0        | 0.0  | 41.67       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement     | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 50.0         | false    | false    |
      | 19 February 2025 | Down Payment     | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 37.5         | false    | false    |
    And Customer makes "AUTOPAY" repayment on "19 February 2025" with 3.47 EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 50.0            |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 37.5            | 12.5          | 0.0      | 0.0  | 0.0       | 12.5 | 12.5 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 34.61           | 2.89          | 0.58     | 0.0  | 0.0       | 3.47 | 3.47 | 3.47       | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 31.73           | 2.88          | 0.59     | 0.0  | 0.0       | 3.47 | 0.0  |  0.0       | 0.0  | 3.47        |
      | 4  | 30   | 19 May 2025       |                  | 28.78           | 2.95          | 0.52     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 5  | 31   | 19 June 2025      |                  | 25.8            | 2.98          | 0.49     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 6  | 30   | 19 July 2025      |                  | 22.75           | 3.05          | 0.42     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 7  | 31   | 19 August 2025    |                  | 19.67           | 3.08          | 0.39     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 8  | 31   | 19 September 2025 |                  | 16.53           | 3.14          | 0.33     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 9  | 30   | 19 October 2025   |                  | 13.33           | 3.2           | 0.27     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 10 | 31   | 19 November 2025  |                  | 10.09           | 3.24          | 0.23     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 11 | 30   | 19 December 2025  |                  | 6.79            | 3.3           | 0.17     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 12 | 31   | 19 January 2026   |                  | 3.44            | 3.35          | 0.12     | 0.0  | 0.0       | 3.47 | 0.0  | 0.0        | 0.0  | 3.47        |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 3.44          | 0.06     | 0.0  | 0.0       | 3.5  | 0.0  | 0.0        | 0.0  | 3.5         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 50.0          | 4.17     | 0.0  | 0.0       | 54.17 | 15.97 | 3.47       | 0.0  | 38.2        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement      | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 50.0         | false    | false    |
      | 19 February 2025 | Down Payment      | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 37.5         | false    | false    |
      | 19 February 2025 | Repayment         | 3.47   | 2.89      | 0.58     | 0.0  | 0.0       | 34.61        | false    | false    |
    When Admin sets the business date to "20 February 2025"
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "20 February 2025" with 17.35 EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 50.0            |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 37.5            | 12.5          | 0.0      | 0.0  | 0.0       | 12.5 | 12.5 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 34.61           | 2.89          | 0.58     | 0.0  | 0.0       | 3.47 | 3.47 | 3.47       | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 31.73           | 2.88          | 0.59     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 4  | 30   | 19 May 2025       |                  | 28.78           | 2.95          | 0.52     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 5  | 31   | 19 June 2025      |                  | 25.8            | 2.98          | 0.49     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 6  | 30   | 19 July 2025      |                  | 22.75           | 3.05          | 0.42     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 7  | 31   | 19 August 2025    |                  | 19.67           | 3.08          | 0.39     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 8  | 31   | 19 September 2025 |                  | 16.53           | 3.14          | 0.33     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 9  | 30   | 19 October 2025   |                  | 13.33           | 3.2           | 0.27     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 10 | 31   | 19 November 2025  |                  | 10.09           | 3.24          | 0.23     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 11 | 30   | 19 December 2025  |                  | 6.79            | 3.3           | 0.17     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 12 | 31   | 19 January 2026   |                  | 3.44            | 3.35          | 0.12     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 3.44          | 0.06     | 0.0  | 0.0       | 3.5  | 1.55 | 1.55       | 0.0  | 1.95        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 50.00         | 4.17     | 0.0  | 0.0       | 54.17 | 33.32 | 20.82      | 0.0  | 20.85       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement            | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 50.0         | false    | false    |
      | 19 February 2025 | Down Payment            | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 37.5         | false    | false    |
      | 19 February 2025 | Repayment               | 3.47   | 2.89      | 0.58     | 0.0  | 0.0       | 34.61        | false    | false    |
      | 20 February 2025 | Merchant Issued Refund  | 17.35  | 17.35     | 0.0      | 0.0  | 0.0       | 17.26        | false    | false    |
    When Admin sets the business date to "21 February 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 50.0            |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 37.5            | 12.5          | 0.0      | 0.0  | 0.0       | 12.5 | 12.5 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 34.61           | 2.89          | 0.58     | 0.0  | 0.0       | 3.47 | 3.47 | 3.47       | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 31.73           | 2.88          | 0.59     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 4  | 30   | 19 May 2025       |                  | 28.78           | 2.95          | 0.52     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 5  | 31   | 19 June 2025      |                  | 25.8            | 2.98          | 0.49     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 6  | 30   | 19 July 2025      |                  | 22.75           | 3.05          | 0.42     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 7  | 31   | 19 August 2025    |                  | 19.67           | 3.08          | 0.39     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 8  | 31   | 19 September 2025 |                  | 16.53           | 3.14          | 0.33     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 9  | 30   | 19 October 2025   |                  | 13.33           | 3.2           | 0.27     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 10 | 31   | 19 November 2025  |                  | 10.09           | 3.24          | 0.23     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 11 | 30   | 19 December 2025  |                  | 6.79            | 3.3           | 0.17     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 12 | 31   | 19 January 2026   |                  | 3.44            | 3.35          | 0.12     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 3.44          | 0.06     | 0.0  | 0.0       | 3.5  | 1.55 | 1.55       | 0.0  | 1.95        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 50.00         | 4.17     | 0.0  | 0.0       | 54.17 | 33.32 | 20.82      | 0.0  | 20.85       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement            | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 50.0         | false    | false    |
      | 19 February 2025 | Down Payment            | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 37.5         | false    | false    |
      | 19 February 2025 | Repayment               | 3.47   | 2.89      | 0.58     | 0.0  | 0.0       | 34.61        | false    | false    |
      | 20 February 2025 | Merchant Issued Refund  | 17.35  | 17.35     | 0.0      | 0.0  | 0.0       | 17.26        | false    | false    |
      | 20 February 2025 | Accrual                 | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "22 February 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 50.0            |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 37.5            | 12.5          | 0.0      | 0.0  | 0.0       | 12.5 | 12.5 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 34.61           | 2.89          | 0.58     | 0.0  | 0.0       | 3.47 | 3.47 | 3.47       | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 31.73           | 2.88          | 0.59     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 4  | 30   | 19 May 2025       |                  | 28.78           | 2.95          | 0.52     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 5  | 31   | 19 June 2025      |                  | 25.8            | 2.98          | 0.49     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 6  | 30   | 19 July 2025      |                  | 22.75           | 3.05          | 0.42     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 7  | 31   | 19 August 2025    |                  | 19.67           | 3.08          | 0.39     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 8  | 31   | 19 September 2025 |                  | 16.53           | 3.14          | 0.33     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 9  | 30   | 19 October 2025   |                  | 13.33           | 3.2           | 0.27     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 10 | 31   | 19 November 2025  |                  | 10.09           | 3.24          | 0.23     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 11 | 30   | 19 December 2025  |                  | 6.79            | 3.3           | 0.17     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 12 | 31   | 19 January 2026   |                  | 3.44            | 3.35          | 0.12     | 0.0  | 0.0       | 3.47 | 1.58 | 1.58       | 0.0  | 1.89        |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 3.44          | 0.06     | 0.0  | 0.0       | 3.5  | 1.55 | 1.55       | 0.0  | 1.95        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 50.00         | 4.17     | 0.0  | 0.0       | 54.17 | 33.32 | 20.82      | 0.0  | 20.85       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement            | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 50.0         | false    | false    |
      | 19 February 2025 | Down Payment            | 12.5   | 12.5      | 0.0      | 0.0  | 0.0       | 37.5         | false    | false    |
      | 19 February 2025 | Repayment               | 3.47   | 2.89      | 0.58     | 0.0  | 0.0       | 34.61        | false    | false    |
      | 20 February 2025 | Merchant Issued Refund  | 17.35  | 17.35     | 0.0      | 0.0  | 0.0       | 17.26        | false    | false    |
      | 20 February 2025 | Accrual                 | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2025 | Accrual                 | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3535
  Scenario: Verify the accrual activity creation in case of full repayment on maturity date - absence of negative numbers with low interest rate and small principal
    When Admin sets the business date to "19 February 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_AUTO_DOWNPAYMENT_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 19 February 2025  | 10.01          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "19 February 2025" with "10.01" amount and expected disbursement date on "19 February 2025"
    And Admin successfully disburse the loan on "19 February 2025" with "10.01" EUR transaction amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 10.01           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 7.51            | 2.5           | 0.0      | 0.0  | 0.0       | 2.5  | 2.5  | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     |                  | 6.91            | 0.6           | 0.07     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 3  | 31   | 19 April 2025     |                  | 6.31            | 0.6           | 0.07     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 4  | 30   | 19 May 2025       |                  | 5.7             | 0.61          | 0.06     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 5  | 31   | 19 June 2025      |                  | 5.09            | 0.61          | 0.06     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 6  | 30   | 19 July 2025      |                  | 4.47            | 0.62          | 0.05     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 7  | 31   | 19 August 2025    |                  | 3.85            | 0.62          | 0.05     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 8  | 31   | 19 September 2025 |                  | 3.22            | 0.63          | 0.04     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 9  | 30   | 19 October 2025   |                  | 2.58            | 0.64          | 0.03     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 10 | 31   | 19 November 2025  |                  | 1.94            | 0.64          | 0.03     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 11 | 30   | 19 December 2025  |                  | 1.29            | 0.65          | 0.02     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 12 | 31   | 19 January 2026   |                  | 0.63            | 0.66          | 0.01     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 0.63          | 0.01     | 0.0  | 0.0       | 0.64 | 0.0  | 0.0        | 0.0  | 0.64        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 10.01         | 0.5      | 0.0  | 0.0       | 10.51 | 2.5  | 0.0        | 0.0  | 8.01        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement     | 10.01  | 0.0       | 0.0      | 0.0  | 0.0       | 10.01        | false    | false    |
      | 19 February 2025 | Down Payment     | 2.5    | 2.5       | 0.0      | 0.0  | 0.0       | 7.51         | false    | false    |
    And Customer makes "AUTOPAY" repayment on "19 February 2025" with 0.67 EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 10.01           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 7.51            | 2.5           | 0.0      | 0.0  | 0.0       | 2.5  | 2.5  | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 6.91            | 0.6           | 0.07     | 0.0  | 0.0       | 0.67 | 0.67 | 0.67       | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 6.31            | 0.6           | 0.07     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 4  | 30   | 19 May 2025       |                  | 5.7             | 0.61          | 0.06     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 5  | 31   | 19 June 2025      |                  | 5.09            | 0.61          | 0.06     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 6  | 30   | 19 July 2025      |                  | 4.47            | 0.62          | 0.05     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 7  | 31   | 19 August 2025    |                  | 3.85            | 0.62          | 0.05     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 8  | 31   | 19 September 2025 |                  | 3.22            | 0.63          | 0.04     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 9  | 30   | 19 October 2025   |                  | 2.58            | 0.64          | 0.03     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 10 | 31   | 19 November 2025  |                  | 1.94            | 0.64          | 0.03     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 11 | 30   | 19 December 2025  |                  | 1.29            | 0.65          | 0.02     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 12 | 31   | 19 January 2026   |                  | 0.63            | 0.66          | 0.01     | 0.0  | 0.0       | 0.67 | 0.0  | 0.0        | 0.0  | 0.67        |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 0.63          | 0.01     | 0.0  | 0.0       | 0.64 | 0.0  | 0.0        | 0.0  | 0.64        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 10.01         | 0.5      | 0.0  | 0.0       | 10.51 | 3.17 | 0.67       | 0.0  | 7.34        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement     | 10.01  | 0.0       | 0.0      | 0.0  | 0.0       | 10.01        | false    | false    |
      | 19 February 2025 | Down Payment     | 2.5    | 2.5       | 0.0      | 0.0  | 0.0       | 7.51         | false    | false    |
      | 19 February 2025 | Repayment        | 0.67   | 0.6       | 0.07     | 0.0  | 0.0       | 6.91         | false    | false    |
    When Admin sets the business date to "20 February 2025"
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "20 February 2025" with 3.35 EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 10.01           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 7.51            | 2.5           | 0.0      | 0.0  | 0.0       | 2.5  | 2.5  | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 6.91            | 0.6           | 0.07     | 0.0  | 0.0       | 0.67 | 0.67 | 0.67       | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 6.31            | 0.6           | 0.07     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 4  | 30   | 19 May 2025       |                  | 5.7             | 0.61          | 0.06     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 5  | 31   | 19 June 2025      |                  | 5.09            | 0.61          | 0.06     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 6  | 30   | 19 July 2025      |                  | 4.47            | 0.62          | 0.05     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 7  | 31   | 19 August 2025    |                  | 3.85            | 0.62          | 0.05     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 8  | 31   | 19 September 2025 |                  | 3.22            | 0.63          | 0.04     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 9  | 30   | 19 October 2025   |                  | 2.58            | 0.64          | 0.03     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 10 | 31   | 19 November 2025  |                  | 1.94            | 0.64          | 0.03     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 11 | 30   | 19 December 2025  |                  | 1.29            | 0.65          | 0.02     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 12 | 31   | 19 January 2026   |                  | 0.63            | 0.66          | 0.01     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 0.63          | 0.01     | 0.0  | 0.0       | 0.64 | 0.35 | 0.35       | 0.0  | 0.29        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 10.01         | 0.5      | 0.0  | 0.0       | 10.51 | 6.52 | 4.02       | 0.0  | 3.99        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement           | 10.01  | 0.0       | 0.0      | 0.0  | 0.0       | 10.01        | false    | false    |
      | 19 February 2025 | Down Payment           | 2.5    | 2.5       | 0.0      | 0.0  | 0.0       | 7.51         | false    | false    |
      | 19 February 2025 | Repayment              | 0.67   | 0.6       | 0.07     | 0.0  | 0.0       | 6.91         | false    | false    |
      | 20 February 2025 | Merchant Issued Refund | 3.35   | 3.35      | 0.0      | 0.0  | 0.0       | 3.56         | false    | false    |
    When Admin sets the business date to "21 February 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 10.01           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 7.51            | 2.5           | 0.0      | 0.0  | 0.0       | 2.5  | 2.5  | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 6.91            | 0.6           | 0.07     | 0.0  | 0.0       | 0.67 | 0.67 | 0.67       | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 6.31            | 0.6           | 0.07     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 4  | 30   | 19 May 2025       |                  | 5.7             | 0.61          | 0.06     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 5  | 31   | 19 June 2025      |                  | 5.09            | 0.61          | 0.06     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 6  | 30   | 19 July 2025      |                  | 4.47            | 0.62          | 0.05     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 7  | 31   | 19 August 2025    |                  | 3.85            | 0.62          | 0.05     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 8  | 31   | 19 September 2025 |                  | 3.22            | 0.63          | 0.04     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 9  | 30   | 19 October 2025   |                  | 2.58            | 0.64          | 0.03     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 10 | 31   | 19 November 2025  |                  | 1.94            | 0.64          | 0.03     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 11 | 30   | 19 December 2025  |                  | 1.29            | 0.65          | 0.02     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 12 | 31   | 19 January 2026   |                  | 0.63            | 0.66          | 0.01     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 0.63          | 0.01     | 0.0  | 0.0       | 0.64 | 0.35 | 0.35       | 0.0  | 0.29        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 10.01         | 0.5      | 0.0  | 0.0       | 10.51 | 6.52 | 4.02       | 0.0  | 3.99        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement           | 10.01  | 0.0       | 0.0      | 0.0  | 0.0       | 10.01        | false    | false    |
      | 19 February 2025 | Down Payment           | 2.5    | 2.5       | 0.0      | 0.0  | 0.0       | 7.51         | false    | false    |
      | 19 February 2025 | Repayment              | 0.67   | 0.6       | 0.07     | 0.0  | 0.0       | 6.91         | false    | false    |
      | 20 February 2025 | Merchant Issued Refund | 3.35   | 3.35      | 0.0      | 0.0  | 0.0       | 3.56         | false    | false    |
    When Admin sets the business date to "22 February 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 10.01           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 7.51            | 2.5           | 0.0      | 0.0  | 0.0       | 2.5  | 2.5  | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 6.91            | 0.6           | 0.07     | 0.0  | 0.0       | 0.67 | 0.67 | 0.67       | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 6.31            | 0.6           | 0.07     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 4  | 30   | 19 May 2025       |                  | 5.7             | 0.61          | 0.06     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 5  | 31   | 19 June 2025      |                  | 5.09            | 0.61          | 0.06     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 6  | 30   | 19 July 2025      |                  | 4.47            | 0.62          | 0.05     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 7  | 31   | 19 August 2025    |                  | 3.85            | 0.62          | 0.05     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 8  | 31   | 19 September 2025 |                  | 3.22            | 0.63          | 0.04     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 9  | 30   | 19 October 2025   |                  | 2.58            | 0.64          | 0.03     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 10 | 31   | 19 November 2025  |                  | 1.94            | 0.64          | 0.03     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 11 | 30   | 19 December 2025  |                  | 1.29            | 0.65          | 0.02     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 12 | 31   | 19 January 2026   |                  | 0.63            | 0.66          | 0.01     | 0.0  | 0.0       | 0.67 | 0.3  | 0.3        | 0.0  | 0.37        |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 0.63          | 0.01     | 0.0  | 0.0       | 0.64 | 0.35 | 0.35       | 0.0  | 0.29        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 10.01         | 0.5      | 0.0  | 0.0       | 10.51 | 6.52 | 4.02       | 0.0  | 3.99        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement           | 10.01  | 0.0       | 0.0      | 0.0  | 0.0       | 10.01        | false    | false    |
      | 19 February 2025 | Down Payment           | 2.5    | 2.5       | 0.0      | 0.0  | 0.0       | 7.51         | false    | false    |
      | 19 February 2025 | Repayment              | 0.67   | 0.6       | 0.07     | 0.0  | 0.0       | 6.91         | false    | false    |
      | 20 February 2025 | Merchant Issued Refund | 3.35   | 3.35      | 0.0      | 0.0  | 0.0       | 3.56         | false    | false    |

  @TestRailId:C3536
  Scenario: Verify the accrual activity creation in case of full repayment on maturity date - absence of negative numbers with high interest rate and large principal
    When Admin sets the business date to "19 February 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_AUTO_DOWNPAYMENT_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 19 February 2025  | 9999.99        | 60                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "19 February 2025" with "9999.99" amount and expected disbursement date on "19 February 2025"
    And Admin successfully disburse the loan on "19 February 2025" with "9999.99" EUR transaction amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 9999.99         |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 7499.99         | 2500.0        | 0.0      | 0.0  | 0.0       | 2500.0 | 2500.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     |                  | 7000.98         | 499.01        | 345.21   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 3  | 31   | 19 April 2025     |                  | 6513.52         | 487.46        | 356.76   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 4  | 30   | 19 May 2025       |                  | 5990.51         | 523.01        | 321.21   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 5  | 31   | 19 June 2025      |                  | 5451.56         | 538.95        | 305.27   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 6  | 30   | 19 July 2025      |                  | 4876.18         | 575.38        | 268.84   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 7  | 31   | 19 August 2025    |                  | 4280.44         | 595.74        | 248.48   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 8  | 31   | 19 September 2025 |                  | 3654.35         | 626.09        | 218.13   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 9  | 30   | 19 October 2025   |                  | 2990.34         | 664.01        | 180.21   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 10 | 31   | 19 November 2025  |                  | 2298.5          | 691.84        | 152.38   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 11 | 30   | 19 December 2025  |                  | 1567.63         | 730.87        | 113.35   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 12 | 31   | 19 January 2026   |                  | 803.29          | 764.34        | 79.88    | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 803.29        | 40.93    | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid    | In advance | Late | Outstanding |
      | 9999.99       | 2630.65  | 0.0  | 0.0       | 12630.64 | 2500.00 | 0.0        | 0.0  | 10130.64    |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement     | 9999.99 | 0.0       | 0.0      | 0.0  | 0.0       | 9999.99      | false    | false    |
      | 19 February 2025 | Down Payment     | 2500.0  | 2500.0    | 0.0      | 0.0  | 0.0       | 7499.99      | false    | false    |
    And Customer makes "AUTOPAY" repayment on "19 February 2025" with 844.22 EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 9999.99         |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 7499.99         | 2500.0        | 0.0      | 0.0  | 0.0       | 2500.0 | 2500.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 7000.98         | 499.01        | 345.21   | 0.0  | 0.0       | 844.22 | 844.22 | 844.22     | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 6513.52         | 487.46        | 356.76   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 4  | 30   | 19 May 2025       |                  | 5990.51         | 523.01        | 321.21   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 5  | 31   | 19 June 2025      |                  | 5451.56         | 538.95        | 305.27   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 6  | 30   | 19 July 2025      |                  | 4876.18         | 575.38        | 268.84   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 7  | 31   | 19 August 2025    |                  | 4280.44         | 595.74        | 248.48   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 8  | 31   | 19 September 2025 |                  | 3654.35         | 626.09        | 218.13   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 9  | 30   | 19 October 2025   |                  | 2990.34         | 664.01        | 180.21   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 10 | 31   | 19 November 2025  |                  | 2298.5          | 691.84        | 152.38   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 11 | 30   | 19 December 2025  |                  | 1567.63         | 730.87        | 113.35   | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 12 | 31   | 19 January 2026   |                  | 803.29          | 764.34        | 79.88    | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 803.29        | 40.93    | 0.0  | 0.0       | 844.22 | 0.0    | 0.0        | 0.0  | 844.22      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid    | In advance | Late | Outstanding |
      | 9999.99       | 2630.65  | 0.0  | 0.0       | 12630.64 | 3344.22 | 844.22     | 0.0  | 9286.42    |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement     | 9999.99 | 0.0       | 0.0      | 0.0  | 0.0       | 9999.99      | false    | false    |
      | 19 February 2025 | Down Payment     | 2500.0  | 2500.0    | 0.0      | 0.0  | 0.0       | 7499.99      | false    | false    |
      | 19 February 2025 | Repayment        | 844.22  | 499.01    | 345.21   | 0.0  | 0.0       | 7000.98      | false    | false    |
    When Admin sets the business date to "20 February 2025"
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "20 February 2025" with 4221.1 EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 9999.99         |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 7499.99         | 2500.0        | 0.0      | 0.0  | 0.0       | 2500.0 | 2500.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 7000.98         | 499.01        | 345.21   | 0.0  | 0.0       | 844.22 | 844.22 | 844.22     | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 6513.52         | 487.46        | 356.76   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 4  | 30   | 19 May 2025       |                  | 5990.51         | 523.01        | 321.21   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 5  | 31   | 19 June 2025      |                  | 5451.56         | 538.95        | 305.27   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 6  | 30   | 19 July 2025      |                  | 4876.18         | 575.38        | 268.84   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 7  | 31   | 19 August 2025    |                  | 4280.44         | 595.74        | 248.48   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 8  | 31   | 19 September 2025 |                  | 3654.35         | 626.09        | 218.13   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 9  | 30   | 19 October 2025   |                  | 2990.34         | 664.01        | 180.21   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 10 | 31   | 19 November 2025  |                  | 2298.5          | 691.84        | 152.38   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 11 | 30   | 19 December 2025  |                  | 1567.63         | 730.87        | 113.35   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 12 | 31   | 19 January 2026   |                  | 803.29          | 764.34        | 79.88    | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 803.29        | 40.93    | 0.0  | 0.0       | 844.22 | 383.7  | 383.7      | 0.0  | 460.52      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid    | In advance | Late | Outstanding |
      | 9999.99       | 2630.65  | 0.0  | 0.0       | 12630.64 | 7565.32 | 5065.32    | 0.0  | 5065.32     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement           | 9999.99 | 0.0       | 0.0      | 0.0  | 0.0       | 9999.99      | false    | false    |
      | 19 February 2025 | Down Payment           | 2500.0  | 2500.0    | 0.0      | 0.0  | 0.0       | 7499.99      | false    | false    |
      | 19 February 2025 | Repayment              | 844.22  | 499.01    | 345.21   | 0.0  | 0.0       | 7000.98      | false    | false    |
      | 20 February 2025 | Merchant Issued Refund | 4221.1  | 4221.1    | 0.0      | 0.0  | 0.0       | 2779.88      | false    | false    |
    When Admin sets the business date to "21 February 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 9999.99         |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 7499.99         | 2500.0        | 0.0      | 0.0  | 0.0       | 2500.0 | 2500.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 7000.98         | 499.01        | 345.21   | 0.0  | 0.0       | 844.22 | 844.22 | 844.22     | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 6513.52         | 487.46        | 356.76   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 4  | 30   | 19 May 2025       |                  | 5990.51         | 523.01        | 321.21   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 5  | 31   | 19 June 2025      |                  | 5451.56         | 538.95        | 305.27   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 6  | 30   | 19 July 2025      |                  | 4876.18         | 575.38        | 268.84   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 7  | 31   | 19 August 2025    |                  | 4280.44         | 595.74        | 248.48   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 8  | 31   | 19 September 2025 |                  | 3654.35         | 626.09        | 218.13   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 9  | 30   | 19 October 2025   |                  | 2990.34         | 664.01        | 180.21   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 10 | 31   | 19 November 2025  |                  | 2298.5          | 691.84        | 152.38   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 11 | 30   | 19 December 2025  |                  | 1567.63         | 730.87        | 113.35   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 12 | 31   | 19 January 2026   |                  | 803.29          | 764.34        | 79.88    | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 803.29        | 40.93    | 0.0  | 0.0       | 844.22 | 383.7  | 383.7      | 0.0  | 460.52      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid    | In advance | Late | Outstanding |
      | 9999.99       | 2630.65  | 0.0  | 0.0       | 12630.64 | 7565.32 | 5065.32    | 0.0  | 5065.32     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement           | 9999.99 | 0.0       | 0.0      | 0.0  | 0.0       | 9999.99      | false    | false    |
      | 19 February 2025 | Down Payment           | 2500.0  | 2500.0    | 0.0      | 0.0  | 0.0       | 7499.99      | false    | false    |
      | 19 February 2025 | Repayment              | 844.22  | 499.01    | 345.21   | 0.0  | 0.0       | 7000.98      | false    | false    |
      | 20 February 2025 | Merchant Issued Refund | 4221.1  | 4221.1    | 0.0      | 0.0  | 0.0       | 2779.88      | false    | false    |
      | 20 February 2025 | Accrual                | 12.33   | 0.0       | 12.33    | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "22 February 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 9999.99         |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 7499.99         | 2500.0        | 0.0      | 0.0  | 0.0       | 2500.0 | 2500.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 7000.98         | 499.01        | 345.21   | 0.0  | 0.0       | 844.22 | 844.22 | 844.22     | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 6513.52         | 487.46        | 356.76   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 4  | 30   | 19 May 2025       |                  | 5990.51         | 523.01        | 321.21   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 5  | 31   | 19 June 2025      |                  | 5451.56         | 538.95        | 305.27   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 6  | 30   | 19 July 2025      |                  | 4876.18         | 575.38        | 268.84   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 7  | 31   | 19 August 2025    |                  | 4280.44         | 595.74        | 248.48   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 8  | 31   | 19 September 2025 |                  | 3654.35         | 626.09        | 218.13   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 9  | 30   | 19 October 2025   |                  | 2990.34         | 664.01        | 180.21   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 10 | 31   | 19 November 2025  |                  | 2298.5          | 691.84        | 152.38   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 11 | 30   | 19 December 2025  |                  | 1567.63         | 730.87        | 113.35   | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 12 | 31   | 19 January 2026   |                  | 803.29          | 764.34        | 79.88    | 0.0  | 0.0       | 844.22 | 383.74 | 383.74     | 0.0  | 460.48      |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 803.29        | 40.93    | 0.0  | 0.0       | 844.22 | 383.7  | 383.7      | 0.0  | 460.52      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid    | In advance | Late | Outstanding |
      | 9999.99       | 2630.65  | 0.0  | 0.0       | 12630.64 | 7565.32 | 5065.32    | 0.0  | 5065.32     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement           | 9999.99 | 0.0       | 0.0      | 0.0  | 0.0       | 9999.99      | false    | false    |
      | 19 February 2025 | Down Payment           | 2500.0  | 2500.0    | 0.0      | 0.0  | 0.0       | 7499.99      | false    | false    |
      | 19 February 2025 | Repayment              | 844.22  | 499.01    | 345.21   | 0.0  | 0.0       | 7000.98      | false    | false    |
      | 20 February 2025 | Merchant Issued Refund | 4221.1  | 4221.1    | 0.0      | 0.0  | 0.0       | 2779.88      | false    | false    |
      | 20 February 2025 | Accrual                | 12.33   | 0.0       | 12.33    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2025 | Accrual                | 12.33   | 0.0       | 12.33    | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3537
  Scenario: Verify the accrual activity creation in case of full repayment on maturity date - absence of negative numbers with mid-range interest rate and mid-range principal
    When Admin sets the business date to "19 February 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_AUTO_DOWNPAYMENT_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY | 19 February 2025  | 5000           | 36                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "19 February 2025" with "5000" amount and expected disbursement date on "19 February 2025"
    And Admin successfully disburse the loan on "19 February 2025" with "5000" EUR transaction amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 3750.0          | 1250.0        | 0.0      | 0.0  | 0.0       | 1250.0 | 1250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     |                  | 3477.35         | 272.65        | 103.56   | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 3  | 31   | 19 April 2025     |                  | 3207.46         | 269.89        | 106.32   | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 4  | 30   | 19 May 2025       |                  | 2926.16         | 281.3         | 94.91    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 5  | 31   | 19 June 2025      |                  | 2639.42         | 286.74        | 89.47    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 6  | 30   | 19 July 2025      |                  | 2341.31         | 298.11        | 78.1     | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 7  | 31   | 19 August 2025    |                  | 2036.69         | 304.62        | 71.59    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 8  | 31   | 19 September 2025 |                  | 1722.75         | 313.94        | 62.27    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 9  | 30   | 19 October 2025   |                  | 1397.51         | 325.24        | 50.97    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 10 | 31   | 19 November 2025  |                  | 1064.03         | 333.48        | 42.73    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 11 | 30   | 19 December 2025  |                  | 719.3           | 344.73        | 31.48    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 12 | 31   | 19 January 2026   |                  | 365.08          | 354.22        | 21.99    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 365.08        | 11.16    | 0.0  | 0.0       | 376.24 | 0.0    | 0.0        | 0.0  | 376.24      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 5000.0        | 764.55   | 0.0  | 0.0       | 5764.55 | 1250.0 | 0.0        | 0.0  | 4514.55     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement     | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       | false    | false    |
      | 19 February 2025 | Down Payment     | 1250.0 | 1250.0    | 0.0      | 0.0  | 0.0       | 3750.0       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "19 February 2025" with 376.21 EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 3750.0          | 1250.0        | 0.0      | 0.0  | 0.0       | 1250.0 | 1250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 3477.35         | 272.65        | 103.56   | 0.0  | 0.0       | 376.21 | 376.21 | 376.21     | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 3207.46         | 269.89        | 106.32   | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 4  | 30   | 19 May 2025       |                  | 2926.16         | 281.3         | 94.91    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 5  | 31   | 19 June 2025      |                  | 2639.42         | 286.74        | 89.47    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 6  | 30   | 19 July 2025      |                  | 2341.31         | 298.11        | 78.1     | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 7  | 31   | 19 August 2025    |                  | 2036.69         | 304.62        | 71.59    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 8  | 31   | 19 September 2025 |                  | 1722.75         | 313.94        | 62.27    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 9  | 30   | 19 October 2025   |                  | 1397.51         | 325.24        | 50.97    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 10 | 31   | 19 November 2025  |                  | 1064.03         | 333.48        | 42.73    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 11 | 30   | 19 December 2025  |                  | 719.3           | 344.73        | 31.48    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 12 | 31   | 19 January 2026   |                  | 365.08          | 354.22        | 21.99    | 0.0  | 0.0       | 376.21 | 0.0    | 0.0        | 0.0  | 376.21      |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 365.08        | 11.16    | 0.0  | 0.0       | 376.24 | 0.0    | 0.0        | 0.0  | 376.24      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 5000.0        | 764.55   | 0.0  | 0.0       | 5764.55 | 1626.21 | 376.21     | 0.0  | 4138.34     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement     | 5000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       | false    | false    |
      | 19 February 2025 | Down Payment     | 1250.0 | 1250.0    | 0.0      | 0.0  | 0.0       | 3750.0       | false    | false    |
      | 19 February 2025 | Repayment        | 376.21 | 272.65    | 103.56   | 0.0  | 0.0       | 3477.35      | false    | false    |
    When Admin sets the business date to "20 February 2025"
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "20 February 2025" with 1881.05 EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 3750.0          | 1250.0        | 0.0      | 0.0  | 0.0       | 1250.0 | 1250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 3477.35         | 272.65        | 103.56   | 0.0  | 0.0       | 376.21 | 376.21 | 376.21     | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 3207.46         | 269.89        | 106.32   | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 4  | 30   | 19 May 2025       |                  | 2926.16         | 281.3         | 94.91    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 5  | 31   | 19 June 2025      |                  | 2639.42         | 286.74        | 89.47    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 6  | 30   | 19 July 2025      |                  | 2341.31         | 298.11        | 78.1     | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 7  | 31   | 19 August 2025    |                  | 2036.69         | 304.62        | 71.59    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 8  | 31   | 19 September 2025 |                  | 1722.75         | 313.94        | 62.27    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 9  | 30   | 19 October 2025   |                  | 1397.51         | 325.24        | 50.97    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 10 | 31   | 19 November 2025  |                  | 1064.03         | 333.48        | 42.73    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 11 | 30   | 19 December 2025  |                  | 719.3           | 344.73        | 31.48    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 12 | 31   | 19 January 2026   |                  | 365.08          | 354.22        | 21.99    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 365.08        | 11.16    | 0.0  | 0.0       | 376.24 | 171.05 | 171.05     | 0.0  | 205.19      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 5000.0        | 764.55   | 0.0  | 0.0       | 5764.55 | 3507.26 | 2257.26    | 0.0  | 2257.29     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement           | 5000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       | false    | false    |
      | 19 February 2025 | Down Payment           | 1250.0  | 1250.0    | 0.0      | 0.0  | 0.0       | 3750.0       | false    | false    |
      | 19 February 2025 | Repayment              | 376.21  | 272.65    | 103.56   | 0.0  | 0.0       | 3477.35      | false    | false    |
      | 20 February 2025 | Merchant Issued Refund | 1881.05 | 1881.05   | 0.0      | 0.0  | 0.0       | 1596.3       | false    | false    |
    When Admin sets the business date to "21 February 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 3750.0          | 1250.0        | 0.0      | 0.0  | 0.0       | 1250.0 | 1250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 3477.35         | 272.65        | 103.56   | 0.0  | 0.0       | 376.21 | 376.21 | 376.21     | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 3207.46         | 269.89        | 106.32   | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 4  | 30   | 19 May 2025       |                  | 2926.16         | 281.3         | 94.91    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 5  | 31   | 19 June 2025      |                  | 2639.42         | 286.74        | 89.47    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 6  | 30   | 19 July 2025      |                  | 2341.31         | 298.11        | 78.1     | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 7  | 31   | 19 August 2025    |                  | 2036.69         | 304.62        | 71.59    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 8  | 31   | 19 September 2025 |                  | 1722.75         | 313.94        | 62.27    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 9  | 30   | 19 October 2025   |                  | 1397.51         | 325.24        | 50.97    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 10 | 31   | 19 November 2025  |                  | 1064.03         | 333.48        | 42.73    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 11 | 30   | 19 December 2025  |                  | 719.3           | 344.73        | 31.48    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 12 | 31   | 19 January 2026   |                  | 365.08          | 354.22        | 21.99    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 365.08        | 11.16    | 0.0  | 0.0       | 376.24 | 171.05 | 171.05     | 0.0  | 205.19      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 5000.0        | 764.55   | 0.0  | 0.0       | 5764.55 | 3507.26 | 2257.26    | 0.0  | 2257.29     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement           | 5000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       | false    | false    |
      | 19 February 2025 | Down Payment           | 1250.0  | 1250.0    | 0.0      | 0.0  | 0.0       | 3750.0       | false    | false    |
      | 19 February 2025 | Repayment              | 376.21  | 272.65    | 103.56   | 0.0  | 0.0       | 3477.35      | false    | false    |
      | 20 February 2025 | Merchant Issued Refund | 1881.05 | 1881.05   | 0.0      | 0.0  | 0.0       | 1596.3       | false    | false    |
      | 20 February 2025 | Accrual                | 3.7     | 0.0       | 3.7      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "22 February 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 19 February 2025  |                  | 5000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 19 February 2025  | 19 February 2025 | 3750.0          | 1250.0        | 0.0      | 0.0  | 0.0       | 1250.0 | 1250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 19 March 2025     | 19 February 2025 | 3477.35         | 272.65        | 103.56   | 0.0  | 0.0       | 376.21 | 376.21 | 376.21     | 0.0  | 0.0         |
      | 3  | 31   | 19 April 2025     |                  | 3207.46         | 269.89        | 106.32   | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 4  | 30   | 19 May 2025       |                  | 2926.16         | 281.3         | 94.91    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 5  | 31   | 19 June 2025      |                  | 2639.42         | 286.74        | 89.47    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 6  | 30   | 19 July 2025      |                  | 2341.31         | 298.11        | 78.1     | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 7  | 31   | 19 August 2025    |                  | 2036.69         | 304.62        | 71.59    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 8  | 31   | 19 September 2025 |                  | 1722.75         | 313.94        | 62.27    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 9  | 30   | 19 October 2025   |                  | 1397.51         | 325.24        | 50.97    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 10 | 31   | 19 November 2025  |                  | 1064.03         | 333.48        | 42.73    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 11 | 30   | 19 December 2025  |                  | 719.3           | 344.73        | 31.48    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 12 | 31   | 19 January 2026   |                  | 365.08          | 354.22        | 21.99    | 0.0  | 0.0       | 376.21 | 171.0  | 171.0      | 0.0  | 205.21      |
      | 13 | 31   | 19 February 2026  |                  | 0.0             | 365.08        | 11.16    | 0.0  | 0.0       | 376.24 | 171.05 | 171.05     | 0.0  | 205.19      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 5000.0        | 764.55   | 0.0  | 0.0       | 5764.55 | 3507.26 | 2257.26    | 0.0  | 2257.29     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 19 February 2025 | Disbursement           | 5000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 5000.0       | false    | false    |
      | 19 February 2025 | Down Payment           | 1250.0  | 1250.0    | 0.0      | 0.0  | 0.0       | 3750.0       | false    | false    |
      | 19 February 2025 | Repayment              | 376.21  | 272.65    | 103.56   | 0.0  | 0.0       | 3477.35      | false    | false    |
      | 20 February 2025 | Merchant Issued Refund | 1881.05 | 1881.05   | 0.0      | 0.0  | 0.0       | 1596.3       | false    | false    |
      | 20 February 2025 | Accrual                | 3.7     | 0.0       | 3.7      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2025 | Accrual                | 3.7     | 0.0       | 3.7      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3733
  Scenario: Accruals on accounts where charge adjustments and refunds are done on the same day should not cause duplicate journal entries
    When Admin sets the business date to "12 May 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_360_30_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF_ACCRUAL_ACTIVITY | 12 May 2025       | 50             | 12.19                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "12 May 2025" with "50" amount and expected disbursement date on "12 May 2025"
    And Admin successfully disburse the loan on "12 May 2025" with "48.25" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "12 May 2025" due date and 1.30 EUR transaction amount
    And Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "12 May 2025" with 1.30 EUR transaction amount and externalId ""
    When Admin makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "12 May 2025" with 48.25 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 12 May 2025      | Disbursement       | 48.25  | 0.0       | 0.0      | 0.0  | 0.0       | 48.25        |
      | 12 May 2025      | Charge Adjustment  | 1.3    | 0.0       | 0.0      | 0.0  | 1.3       | 48.25        |
      | 12 May 2025      | Payout Refund      | 48.25  | 48.25     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 12 May 2025      | Accrual            | 1.3    | 0.0       | 0.0      | 0.0  | 1.3       | 0.0          |
      | 12 May 2025      | Accrual Activity   | 1.3    | 0.0       | 0.0      | 0.0  | 1.3       | 0.0          |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "12 May 2025" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 1.3   |        |
      | INCOME | 404007       | Fee Income              |       | 1.3    |