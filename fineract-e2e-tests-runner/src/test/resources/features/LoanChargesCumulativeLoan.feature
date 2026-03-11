@LoanChargesCumulativeLoanFeature
Feature: LoanChargesCumulativeLoan

  @TestRailId:C50
  Scenario: Charge creation functionality with locale EN
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "6000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "6000" amount and expected disbursement date on "1 July 2022"
    When Admin successfully disburse the loan on "1 July 2022" with "6000" EUR transaction amount
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "10 July 2022"
    Then Charge is successfully added to the loan with 600 EUR
    When Loan Pay-off is made on "10 July 2022"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C51
  Scenario: Charge creation functionality with locale DE
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "6000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "6000" amount and expected disbursement date on "1 July 2022"
    When Admin successfully disburse the loan on "1 July 2022" with "6000" EUR transaction amount
    And Admin adds a 10 % Processing charge to the loan with "de_DE" locale on date: "10 Juli 2022"
    Then Charge is successfully added to the loan with 600 EUR
    When Loan Pay-off is made on "10 July 2022"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2450
  Scenario: Due date charge can be successfully applied when it is added on the loan account after the maturity date (NSF scenario of last installment)
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 January 2022", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2022" with "750" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2022"
    And Customer makes "AUTOPAY" repayment on "1 February 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2022"
    And Customer makes "AUTOPAY" repayment on "1 March 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 April 2022"
    And Customer makes "AUTOPAY" repayment on "1 April 2022" with 250 EUR transaction amount
    When Customer makes a repayment undo on "1 April 2022"
    When Admin sets the business date to "5 April 2022"
    And Admin adds an NSF fee because of payment bounce with "5 April 2022" transaction date
    And Customer makes "AUTOPAY" repayment on "1 April 2022" with 260 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2451
  Scenario: Due date charge can be successfully applied when it is added on the loan account which already has a N+1 scenario (by chargeback)
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 January 2022", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2022" with "750" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2022"
    And Customer makes "AUTOPAY" repayment on "1 February 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2022"
    And Customer makes "AUTOPAY" repayment on "1 March 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 April 2022"
    And Customer makes "AUTOPAY" repayment on "1 April 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 May 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "1 May 2022" with 250 EUR transaction amount
    When Customer makes a repayment undo on "1 May 2022"
    When Admin sets the business date to "5 May 2022"
    And Admin adds an NSF fee because of payment bounce with "5 May 2022" transaction date
    When Admin sets the business date to "10 May 2022"
    And Customer makes "AUTOPAY" repayment on "10 May 2022" with 260 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2452
  Scenario: Due date charge can be successfully applied, then waived when it is added on the loan account after the maturity date (NSF scenario of last installment)
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 January 2022", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2022" with "750" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2022"
    And Customer makes "AUTOPAY" repayment on "1 February 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2022"
    And Customer makes "AUTOPAY" repayment on "1 March 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 April 2022"
    And Customer makes "AUTOPAY" repayment on "1 April 2022" with 250 EUR transaction amount
    When Customer makes a repayment undo on "1 April 2022"
    Then Loan status will be "ACTIVE"
    Then Loan has 250 outstanding amount
    When Admin sets the business date to "5 April 2022"
    And Admin adds an NSF fee because of payment bounce with "5 April 2022" transaction date
    Then Loan status will be "ACTIVE"
    Then Loan has 260 outstanding amount
    When Admin sets the business date to "7 April 2022"
    And Admin waives charge
    Then Loan status will be "ACTIVE"
    Then Loan has 250 outstanding amount
    When Loan Pay-off is made on "07 April 2022"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2453
  Scenario: Due date charge can be successfully applied, waived, then waive reversed when it is added on the loan account after the maturity date (NSF scenario of last installment)
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 January 2022", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2022" with "750" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2022"
    And Customer makes "AUTOPAY" repayment on "1 February 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2022"
    And Customer makes "AUTOPAY" repayment on "1 March 2022" with 250 EUR transaction amount
    When Admin sets the business date to "1 April 2022"
    And Customer makes "AUTOPAY" repayment on "1 April 2022" with 250 EUR transaction amount
    When Customer makes a repayment undo on "1 April 2022"
    When Admin sets the business date to "5 April 2022"
    And Admin adds an NSF fee because of payment bounce with "5 April 2022" transaction date
    When Admin sets the business date to "7 April 2022"
    And Admin waives charge
    When Admin sets the business date to "8 April 2022"
    And Admin makes waive undone for charge
    Then Loan status will be "ACTIVE"
    Then Loan has 260 outstanding amount
    When Loan Pay-off is made on "08 April 2022"
    #TDOD - need fix, as loan account has current balance amount equal to NSF fee charge amount and status is Overpaid after pay-off
    #Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2472
  Scenario: Charge adjustment works properly
    When Admin sets the business date to "22 October 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "22 October 2022", with Principal: "1000", a loanTermFrequency: 2 months, and numberOfRepayments: 2
    And Admin successfully approves the loan on "22 October 2022" with "1000" amount and expected disbursement date on "22 October 2022"
    When Admin successfully disburse the loan on "22 October 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "23 October 2022"
    And Admin adds an NSF fee because of payment bounce with "23 October 2022" transaction date
    Then Loan has 1010 outstanding amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 22 October 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 22 November 2022 |           | 500.0           | 500.0         | 0.0      | 0.0  | 10.0      | 510.0 | 0.0  | 0.0        | 0.0  | 510.0       |
      | 2  | 30   | 22 December 2022 |           | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 10.0      | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 22 October 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 23 October 2022  | Accrual          | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "22 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "23 October 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
#    --- charge adjustment for nsf fee with 3 ---
    When Admin sets the business date to "04 November 2022"
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "23 October 2022" with 3 EUR transaction amount and externalId ""
    Then Loan has 1007 outstanding amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 22 October 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 22 November 2022 |           | 500.0           | 500.0         | 0.0      | 0.0  | 10.0      | 510.0 | 3.0  | 3.0        | 0.0  | 507.0       |
      | 2  | 30   | 22 December 2022 |           | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 10.0      | 1010.0 | 3.0  | 3.0        | 0.0  | 1007.0      |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 3.0  | 0.0    | 7.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 22 October 2022  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 23 October 2022  | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          |
      | 04 November 2022 | Charge Adjustment | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 1000.0       |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "22 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "23 October 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 3.0    |
      | INCOME | 404007       | Fee Income              | 3.0   |        |
#   --- Backdated repayment with 8 EUR ---
    And Customer makes "AUTOPAY" repayment on "25 October 2022" with 8 EUR transaction amount
    Then Loan has 999 outstanding amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 22 October 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 22 November 2022 |           | 500.0           | 500.0         | 0.0      | 0.0  | 10.0      | 510.0 | 11.0 | 11.0       | 0.0  | 499.0       |
      | 2  | 30   | 22 December 2022 |           | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 10.0      | 1010.0 | 11.0 | 11.0       | 0.0  | 999.0       |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 22 October 2022  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 23 October 2022  | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          |
      | 25 October 2022  | Repayment         | 8.0    | 0.0       | 0.0      | 0.0  | 8.0       | 1000.0       |
      | 04 November 2022 | Charge Adjustment | 3.0    | 1.0       | 0.0      | 0.0  | 2.0       | 999.0        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "22 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "23 October 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 8.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 8.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable        |       | 1.0    |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 2.0    |
      | INCOME | 404007       | Fee Income              | 3.0   |        |
#  --- charge adjustment with 8 will fail ---
    Then Charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "23 October 2022" with transaction amount 8 which is higher than the available charge amount results an ERROR
#   --- revert last charge adjustment (was amount 3) ---
    When Admin reverts the charge adjustment which was raised on "04 November 2022" with 3 EUR transaction amount
    Then Loan has 1002 outstanding amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 22 October 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 22 November 2022 |           | 500.0           | 500.0         | 0.0      | 0.0  | 10.0      | 510.0 | 8.0  | 8.0        | 0.0  | 502.0       |
      | 2  | 30   | 22 December 2022 |           | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 10.0      | 1010.0 | 8.0  | 8.0        | 0.0  | 1002.0      |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 8.0  | 0.0    | 2.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 October 2022  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 23 October 2022  | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 25 October 2022  | Repayment         | 8.0    | 0.0       | 0.0      | 0.0  | 8.0       | 1000.0       | false    | false    |
      | 04 November 2022 | Charge Adjustment | 3.0    | 1.0       | 0.0      | 0.0  | 2.0       | 999.0        | true     | true     |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "22 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "23 October 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 8.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 8.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable        |       | 1.0    |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 2.0    |
      | INCOME | 404007       | Fee Income              | 3.0   |        |
      | ASSET  | 112601       | Loans Receivable        | 1.0   |        |
      | ASSET  | 112603       | Interest/Fee Receivable | 2.0   |        |
      | INCOME | 404007       | Fee Income              |       | 3.0    |
#   --- Add snooze fee on 10/27/2022 with amount 9 ---
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "27 October 2022" due date and 9 EUR transaction amount
    Then Loan has 1011 outstanding amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 22 October 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 22 November 2022 |           | 500.0           | 500.0         | 0.0      | 9.0  | 10.0      | 519.0 | 8.0  | 8.0        | 0.0  | 511.0       |
      | 2  | 30   | 22 December 2022 |           | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 9.0  | 10.0      | 1019.0 | 8.0  | 8.0        | 0.0  | 1011.0      |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0  | 0.0  | 0.0    | 9.0         |
      | NSF fee    | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 8.0  | 0.0    | 2.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 October 2022  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 23 October 2022  | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 25 October 2022  | Repayment         | 8.0    | 0.0       | 0.0      | 0.0  | 8.0       | 1000.0       | false    | false    |
      | 04 November 2022 | Charge Adjustment | 3.0    | 1.0       | 0.0      | 0.0  | 2.0       | 999.0        | true     | true     |
      | 04 November 2022 | Accrual           | 9.0    | 0.0       | 0.0      | 9.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "22 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "23 October 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 8.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 8.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable        |       | 1.0    |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 2.0    |
      | INCOME | 404007       | Fee Income              | 3.0   |        |
      | ASSET  | 112601       | Loans Receivable        | 1.0   |        |
      | ASSET  | 112603       | Interest/Fee Receivable | 2.0   |        |
      | INCOME | 404007       | Fee Income              |       | 3.0    |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
#   --- charge adjustment for snooze fee with 4 ---
    When Admin makes a charge adjustment for the last "LOAN_SNOOZE_FEE" type charge which is due on "27 October 2022" with 4 EUR transaction amount and externalId ""
    Then Loan has 1007 outstanding amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 22 October 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 22 November 2022 |           | 500.0           | 500.0         | 0.0      | 9.0  | 10.0      | 519.0 | 12.0 | 12.0       | 0.0  | 507.0       |
      | 2  | 30   | 22 December 2022 |           | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 9.0  | 10.0      | 1019.0 | 12.0 | 12.0       | 0.0  | 1007.0      |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0  | 2.0  | 0.0    | 7.0         |
      | NSF fee    | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 October 2022  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 23 October 2022  | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 25 October 2022  | Repayment         | 8.0    | 0.0       | 0.0      | 0.0  | 8.0       | 1000.0       | false    | false    |
      | 04 November 2022 | Charge Adjustment | 3.0    | 1.0       | 0.0      | 0.0  | 2.0       | 999.0        | true     | true     |
      | 04 November 2022 | Accrual           | 9.0    | 0.0       | 0.0      | 9.0  | 0.0       | 0.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 4.0    | 0.0       | 0.0      | 2.0  | 2.0       | 1000.0       | false    | false    |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "22 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "23 October 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 8.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 8.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable        |       | 1.0    |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 2.0    |
      | INCOME | 404007       | Fee Income              | 3.0   |        |
      | ASSET  | 112601       | Loans Receivable        | 1.0   |        |
      | ASSET  | 112603       | Interest/Fee Receivable | 2.0   |        |
      | INCOME | 404007       | Fee Income              |       | 3.0    |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 4.0    |
      | INCOME | 404007       | Fee Income              | 4.0   |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
#   --- Backdated repayment with 507 EUR ---
    And Customer makes "AUTOPAY" repayment on "31 October 2022" with 507 EUR transaction amount
    Then Loan has 500 outstanding amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 22 October 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 22 November 2022 | 04 November 2022 | 500.0           | 500.0         | 0.0      | 9.0  | 10.0      | 519.0 | 519.0 | 519.0      | 0.0  | 0.0         |
      | 2  | 30   | 22 December 2022 |                  | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 9.0  | 10.0      | 1019.0 | 519.0 | 519.0      | 0.0  | 500.0       |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0  | 9.0  | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 October 2022  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 23 October 2022  | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 25 October 2022  | Repayment         | 8.0    | 0.0       | 0.0      | 0.0  | 8.0       | 1000.0       | false    | false    |
      | 31 October 2022  | Repayment         | 507.0  | 496.0     | 0.0      | 9.0  | 2.0       | 504.0        | false    | false    |
      | 04 November 2022 | Charge Adjustment | 3.0    | 1.0       | 0.0      | 0.0  | 2.0       | 999.0        | true     | true     |
      | 04 November 2022 | Accrual           | 9.0    | 0.0       | 0.0      | 9.0  | 0.0       | 0.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 4.0    | 4.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | true     |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "22 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "23 October 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 8.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 8.0   |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "31 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 496.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 11.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 507.0 |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable        |       | 1.0    |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 2.0    |
      | INCOME | 404007       | Fee Income              | 3.0   |        |
      | ASSET  | 112601       | Loans Receivable        | 1.0   |        |
      | ASSET  | 112603       | Interest/Fee Receivable | 2.0   |        |
      | INCOME | 404007       | Fee Income              |       | 3.0    |
      | ASSET  | 112601       | Loans Receivable        |       | 4.0    |
      | INCOME | 404007       | Fee Income              | 4.0   |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
#   --- charge adjustment for nsf fee with 5 ---
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "23 October 2022" with 5 EUR transaction amount and externalId ""
    Then Loan has 495 outstanding amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 22 October 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 22 November 2022 | 04 November 2022 | 500.0           | 500.0         | 0.0      | 9.0  | 10.0      | 519.0 | 519.0 | 519.0      | 0.0  | 0.0         |
      | 2  | 30   | 22 December 2022 |                  | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 5.0   | 5.0        | 0.0  | 495.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 9.0  | 10.0      | 1019.0 | 524.0 | 524.0      | 0.0  | 495.0       |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0  | 9.0  | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 October 2022  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 23 October 2022  | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 25 October 2022  | Repayment         | 8.0    | 0.0       | 0.0      | 0.0  | 8.0       | 1000.0       | false    | false    |
      | 31 October 2022  | Repayment         | 507.0  | 496.0     | 0.0      | 9.0  | 2.0       | 504.0        | false    | false    |
      | 04 November 2022 | Charge Adjustment | 3.0    | 1.0       | 0.0      | 0.0  | 2.0       | 999.0        | true     | true     |
      | 04 November 2022 | Accrual           | 9.0    | 0.0       | 0.0      | 9.0  | 0.0       | 0.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 4.0    | 4.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | true     |
      | 04 November 2022 | Charge Adjustment | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 495.0        | false    | false    |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "22 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "23 October 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 8.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 8.0   |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "31 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 496.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 11.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 507.0 |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable        |       | 1.0    |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 2.0    |
      | INCOME | 404007       | Fee Income              | 3.0   |        |
      | ASSET  | 112601       | Loans Receivable        | 1.0   |        |
      | ASSET  | 112603       | Interest/Fee Receivable | 2.0   |        |
      | INCOME | 404007       | Fee Income              |       | 3.0    |
      | ASSET  | 112601       | Loans Receivable        |       | 4.0    |
      | INCOME | 404007       | Fee Income              | 4.0   |        |
      | ASSET  | 112601       | Loans Receivable        |       | 5.0    |
      | INCOME | 404007       | Fee Income              | 5.0   |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
#  --- Backdated repayment with 494 EUR ---
    And Customer makes "AUTOPAY" repayment on "1 November 2022" with 494 EUR transaction amount
    Then Loan has 1 outstanding amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 22 October 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 22 November 2022 | 01 November 2022 | 500.0           | 500.0         | 0.0      | 9.0  | 10.0      | 519.0 | 519.0 | 519.0      | 0.0  | 0.0         |
      | 2  | 30   | 22 December 2022 |                  | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 499.0 | 499.0      | 0.0  | 1.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 9.0  | 10.0      | 1019.0 | 1018.0 | 1018.0     | 0.0  | 1.0         |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0  | 9.0  | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 October 2022  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 23 October 2022  | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 25 October 2022  | Repayment         | 8.0    | 0.0       | 0.0      | 0.0  | 8.0       | 1000.0       | false    | false    |
      | 31 October 2022  | Repayment         | 507.0  | 496.0     | 0.0      | 9.0  | 2.0       | 504.0        | false    | false    |
      | 01 November 2022 | Repayment         | 494.0  | 494.0     | 0.0      | 0.0  | 0.0       | 10.0         | false    | false    |
      | 04 November 2022 | Charge Adjustment | 3.0    | 1.0       | 0.0      | 0.0  | 2.0       | 999.0        | true     | true     |
      | 04 November 2022 | Accrual           | 9.0    | 0.0       | 0.0      | 9.0  | 0.0       | 0.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 4.0    | 4.0       | 0.0      | 0.0  | 0.0       | 6.0          | false    | true     |
      | 04 November 2022 | Charge Adjustment | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 1.0          | false    | false    |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "22 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "23 October 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 8.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 8.0   |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "31 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 496.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 11.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 507.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 November 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 494.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 494.0 |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable        |       | 1.0    |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 2.0    |
      | INCOME | 404007       | Fee Income              | 3.0   |        |
      | ASSET  | 112601       | Loans Receivable        | 1.0   |        |
      | ASSET  | 112603       | Interest/Fee Receivable | 2.0   |        |
      | INCOME | 404007       | Fee Income              |       | 3.0    |
      | ASSET  | 112601       | Loans Receivable        |       | 4.0    |
      | INCOME | 404007       | Fee Income              | 4.0   |        |
      | ASSET  | 112601       | Loans Receivable        |       | 5.0    |
      | INCOME | 404007       | Fee Income              | 5.0   |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
#   --- charge adjustment for snooze fee with 1 ---
    When Admin makes a charge adjustment for the last "LOAN_SNOOZE_FEE" type charge which is due on "27 October 2022" with 1 EUR transaction amount and externalId ""
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 22 October 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 22 November 2022 | 01 November 2022 | 500.0           | 500.0         | 0.0      | 9.0  | 10.0      | 519.0 | 519.0 | 519.0      | 0.0  | 0.0         |
      | 2  | 30   | 22 December 2022 | 04 November 2022 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 500.0 | 500.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 9.0  | 10.0      | 1019.0 | 1019.0 | 1019.0     | 0.0  | 0.0         |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0  | 9.0  | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 October 2022  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 23 October 2022  | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 25 October 2022  | Repayment         | 8.0    | 0.0       | 0.0      | 0.0  | 8.0       | 1000.0       | false    | false    |
      | 31 October 2022  | Repayment         | 507.0  | 496.0     | 0.0      | 9.0  | 2.0       | 504.0        | false    | false    |
      | 01 November 2022 | Repayment         | 494.0  | 494.0     | 0.0      | 0.0  | 0.0       | 10.0         | false    | false    |
      | 04 November 2022 | Charge Adjustment | 3.0    | 1.0       | 0.0      | 0.0  | 2.0       | 999.0        | true     | true     |
      | 04 November 2022 | Accrual           | 9.0    | 0.0       | 0.0      | 9.0  | 0.0       | 0.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 4.0    | 4.0       | 0.0      | 0.0  | 0.0       | 6.0          | false    | true     |
      | 04 November 2022 | Charge Adjustment | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 1.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 1.0    | 1.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "22 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "23 October 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 8.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 8.0   |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "31 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 496.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 11.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 507.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 November 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 494.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 494.0 |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable        |       | 1.0    |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 2.0    |
      | INCOME | 404007       | Fee Income              | 3.0   |        |
      | ASSET  | 112601       | Loans Receivable        | 1.0   |        |
      | ASSET  | 112603       | Interest/Fee Receivable | 2.0   |        |
      | INCOME | 404007       | Fee Income              |       | 3.0    |
      | ASSET  | 112601       | Loans Receivable        |       | 4.0    |
      | INCOME | 404007       | Fee Income              | 4.0   |        |
      | ASSET  | 112601       | Loans Receivable        |       | 5.0    |
      | INCOME | 404007       | Fee Income              | 5.0   |        |
      | ASSET  | 112601       | Loans Receivable        |       | 1.0    |
      | INCOME | 404007       | Fee Income              | 1.0   |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
#   --- revert last charge adjustment (was amount 1) ---
    When Admin reverts the charge adjustment which was raised on "04 November 2022" with 1 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 1 outstanding amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 22 October 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 22 November 2022 | 01 November 2022 | 500.0           | 500.0         | 0.0      | 9.0  | 10.0      | 519.0 | 519.0 | 519.0      | 0.0  | 0.0         |
      | 2  | 30   | 22 December 2022 |                  | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 499.0 | 499.0      | 0.0  | 1.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 9.0  | 10.0      | 1019.0 | 1018.0 | 1018.0     | 0.0  | 1.0         |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0  | 9.0  | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 October 2022  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 23 October 2022  | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 25 October 2022  | Repayment         | 8.0    | 0.0       | 0.0      | 0.0  | 8.0       | 1000.0       | false    | false    |
      | 31 October 2022  | Repayment         | 507.0  | 496.0     | 0.0      | 9.0  | 2.0       | 504.0        | false    | false    |
      | 01 November 2022 | Repayment         | 494.0  | 494.0     | 0.0      | 0.0  | 0.0       | 10.0         | false    | false    |
      | 04 November 2022 | Charge Adjustment | 3.0    | 1.0       | 0.0      | 0.0  | 2.0       | 999.0        | true     | true     |
      | 04 November 2022 | Accrual           | 9.0    | 0.0       | 0.0      | 9.0  | 0.0       | 0.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 4.0    | 4.0       | 0.0      | 0.0  | 0.0       | 6.0          | false    | true     |
      | 04 November 2022 | Charge Adjustment | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 1.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 1.0    | 1.0       | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "22 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "23 October 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 8.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 8.0   |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "31 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 496.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 11.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 507.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 November 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 494.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 494.0 |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable        |       | 1.0    |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 2.0    |
      | INCOME | 404007       | Fee Income              | 3.0   |        |
      | ASSET  | 112601       | Loans Receivable        | 1.0   |        |
      | ASSET  | 112603       | Interest/Fee Receivable | 2.0   |        |
      | INCOME | 404007       | Fee Income              |       | 3.0    |
      | ASSET  | 112601       | Loans Receivable        |       | 4.0    |
      | INCOME | 404007       | Fee Income              | 4.0   |        |
      | ASSET  | 112601       | Loans Receivable        |       | 5.0    |
      | INCOME | 404007       | Fee Income              | 5.0   |        |
      | ASSET  | 112601       | Loans Receivable        |       | 1.0    |
      | INCOME | 404007       | Fee Income              | 1.0   |        |
      | ASSET  | 112601       | Loans Receivable        | 1.0   |        |
      | INCOME | 404007       | Fee Income              |       | 1.0    |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
#   --- charge adjustment for nsf fee with 1 ---
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "23 October 2022" with 1 EUR transaction amount and externalId ""
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 22 October 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 22 November 2022 | 01 November 2022 | 500.0           | 500.0         | 0.0      | 9.0  | 10.0      | 519.0 | 519.0 | 519.0      | 0.0  | 0.0         |
      | 2  | 30   | 22 December 2022 | 04 November 2022 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 500.0 | 500.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 9.0  | 10.0      | 1019.0 | 1019.0 | 1019.0     | 0.0  | 0.0         |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0  | 9.0  | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 October 2022  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 23 October 2022  | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 25 October 2022  | Repayment         | 8.0    | 0.0       | 0.0      | 0.0  | 8.0       | 1000.0       | false    | false    |
      | 31 October 2022  | Repayment         | 507.0  | 496.0     | 0.0      | 9.0  | 2.0       | 504.0        | false    | false    |
      | 01 November 2022 | Repayment         | 494.0  | 494.0     | 0.0      | 0.0  | 0.0       | 10.0         | false    | false    |
      | 04 November 2022 | Charge Adjustment | 3.0    | 1.0       | 0.0      | 0.0  | 2.0       | 999.0        | true     | true     |
      | 04 November 2022 | Accrual           | 9.0    | 0.0       | 0.0      | 9.0  | 0.0       | 0.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 4.0    | 4.0       | 0.0      | 0.0  | 0.0       | 6.0          | false    | true     |
      | 04 November 2022 | Charge Adjustment | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 1.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 1.0    | 1.0       | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 04 November 2022 | Charge Adjustment | 1.0    | 1.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "22 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "23 October 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 8.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 8.0   |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "31 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 496.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 11.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 507.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 November 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 494.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 494.0 |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable        |       | 1.0    |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 2.0    |
      | INCOME | 404007       | Fee Income              | 3.0   |        |
      | ASSET  | 112601       | Loans Receivable        | 1.0   |        |
      | ASSET  | 112603       | Interest/Fee Receivable | 2.0   |        |
      | INCOME | 404007       | Fee Income              |       | 3.0    |
      | ASSET  | 112601       | Loans Receivable        |       | 4.0    |
      | INCOME | 404007       | Fee Income              | 4.0   |        |
      | ASSET  | 112601       | Loans Receivable        |       | 5.0    |
      | INCOME | 404007       | Fee Income              | 5.0   |        |
      | ASSET  | 112601       | Loans Receivable        |       | 1.0    |
      | INCOME | 404007       | Fee Income              | 1.0   |        |
      | ASSET  | 112601       | Loans Receivable        | 1.0   |        |
      | INCOME | 404007       | Fee Income              |       | 1.0    |
      | ASSET  | 112601       | Loans Receivable        |       | 1.0    |
      | INCOME | 404007       | Fee Income              | 1.0   |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
#   --- charge adjustment for nsf fee with 2 ---
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "23 October 2022" with 2 EUR transaction amount and externalId ""
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 22 October 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 22 November 2022 | 01 November 2022 | 500.0           | 500.0         | 0.0      | 9.0  | 10.0      | 519.0 | 519.0 | 519.0      | 0.0  | 0.0         |
      | 2  | 30   | 22 December 2022 | 04 November 2022 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 500.0 | 500.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 9.0  | 10.0      | 1019.0 | 1019.0 | 1019.0     | 0.0  | 0.0         |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0  | 9.0  | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 October 2022  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 23 October 2022  | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 25 October 2022  | Repayment         | 8.0    | 0.0       | 0.0      | 0.0  | 8.0       | 1000.0       | false    | false    |
      | 31 October 2022  | Repayment         | 507.0  | 496.0     | 0.0      | 9.0  | 2.0       | 504.0        | false    | false    |
      | 01 November 2022 | Repayment         | 494.0  | 494.0     | 0.0      | 0.0  | 0.0       | 10.0         | false    | false    |
      | 04 November 2022 | Charge Adjustment | 3.0    | 1.0       | 0.0      | 0.0  | 2.0       | 999.0        | true     | true     |
      | 04 November 2022 | Accrual           | 9.0    | 0.0       | 0.0      | 9.0  | 0.0       | 0.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 4.0    | 4.0       | 0.0      | 0.0  | 0.0       | 6.0          | false    | true     |
      | 04 November 2022 | Charge Adjustment | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 1.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 1.0    | 1.0       | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 04 November 2022 | Charge Adjustment | 1.0    | 1.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 2.0    | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "22 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "23 October 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 8.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 8.0   |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "31 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 496.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 11.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 507.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 November 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 494.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 494.0 |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type      | Account code | Account name            | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable        |       | 1.0    |
      | ASSET     | 112603       | Interest/Fee Receivable |       | 2.0    |
      | INCOME    | 404007       | Fee Income              | 3.0   |        |
      | ASSET     | 112601       | Loans Receivable        | 1.0   |        |
      | ASSET     | 112603       | Interest/Fee Receivable | 2.0   |        |
      | INCOME    | 404007       | Fee Income              |       | 3.0    |
      | ASSET     | 112601       | Loans Receivable        |       | 4.0    |
      | INCOME    | 404007       | Fee Income              | 4.0   |        |
      | ASSET     | 112601       | Loans Receivable        |       | 5.0    |
      | INCOME    | 404007       | Fee Income              | 5.0   |        |
      | ASSET     | 112601       | Loans Receivable        |       | 1.0    |
      | INCOME    | 404007       | Fee Income              | 1.0   |        |
      | ASSET     | 112601       | Loans Receivable        | 1.0   |        |
      | INCOME    | 404007       | Fee Income              |       | 1.0    |
      | ASSET     | 112601       | Loans Receivable        |       | 1.0    |
      | INCOME    | 404007       | Fee Income              | 1.0   |        |
      | LIABILITY | l1           | Overpayment account     |       | 2.0    |
      | INCOME    | 404007       | Fee Income              | 2.0   |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
#   --- revert last charge adjustment (was amount 2) ---
    When Admin reverts the charge adjustment which was raised on "04 November 2022" with 2 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    And Admin runs the Add Periodic Accrual Transactions job
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 22 October 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 22 November 2022 | 01 November 2022 | 500.0           | 500.0         | 0.0      | 9.0  | 10.0      | 519.0 | 519.0 | 519.0      | 0.0  | 0.0         |
      | 2  | 30   | 22 December 2022 | 04 November 2022 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 500.0 | 500.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 9.0  | 10.0      | 1019.0 | 1019.0 | 1019.0     | 0.0  | 0.0         |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 27 October 2022 | Flat             | 9.0  | 9.0  | 0.0    | 0.0         |
      | NSF fee    | true      | Specified due date | 23 October 2022 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 22 October 2022  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 23 October 2022  | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          | false    | false    |
      | 25 October 2022  | Repayment         | 8.0    | 0.0       | 0.0      | 0.0  | 8.0       | 1000.0       | false    | false    |
      | 31 October 2022  | Repayment         | 507.0  | 496.0     | 0.0      | 9.0  | 2.0       | 504.0        | false    | false    |
      | 01 November 2022 | Repayment         | 494.0  | 494.0     | 0.0      | 0.0  | 0.0       | 10.0         | false    | false    |
      | 04 November 2022 | Charge Adjustment | 3.0    | 1.0       | 0.0      | 0.0  | 2.0       | 999.0        | true     | true     |
      | 04 November 2022 | Accrual           | 9.0    | 0.0       | 0.0      | 9.0  | 0.0       | 0.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 4.0    | 4.0       | 0.0      | 0.0  | 0.0       | 6.0          | false    | true     |
      | 04 November 2022 | Charge Adjustment | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 1.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 1.0    | 1.0       | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 04 November 2022 | Charge Adjustment | 1.0    | 1.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 November 2022 | Charge Adjustment | 2.0    | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "22 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "23 October 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 8.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 8.0   |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "31 October 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 496.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 11.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 507.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 November 2022" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 494.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 494.0 |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type      | Account code | Account name            | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable        |       | 1.0    |
      | ASSET     | 112603       | Interest/Fee Receivable |       | 2.0    |
      | INCOME    | 404007       | Fee Income              | 3.0   |        |
      | ASSET     | 112601       | Loans Receivable        | 1.0   |        |
      | ASSET     | 112603       | Interest/Fee Receivable | 2.0   |        |
      | INCOME    | 404007       | Fee Income              |       | 3.0    |
      | ASSET     | 112601       | Loans Receivable        |       | 4.0    |
      | INCOME    | 404007       | Fee Income              | 4.0   |        |
      | ASSET     | 112601       | Loans Receivable        |       | 5.0    |
      | INCOME    | 404007       | Fee Income              | 5.0   |        |
      | ASSET     | 112601       | Loans Receivable        |       | 1.0    |
      | INCOME    | 404007       | Fee Income              | 1.0   |        |
      | ASSET     | 112601       | Loans Receivable        | 1.0   |        |
      | INCOME    | 404007       | Fee Income              |       | 1.0    |
      | ASSET     | 112601       | Loans Receivable        |       | 1.0    |
      | INCOME    | 404007       | Fee Income              | 1.0   |        |
      | LIABILITY | l1           | Overpayment account     |       | 2.0    |
      | INCOME    | 404007       | Fee Income              | 2.0   |        |
      | LIABILITY | l1           | Overpayment account     | 2.0   |        |
      | INCOME    | 404007       | Fee Income              |       | 2.0    |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |

  @TestRailId:C2532
  Scenario: Verify that charge can be added to loan on disbursement date (loan status is 'active')
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 January 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan's actualMaturityDate is "31 January 2023"
    When Loan Pay-off is made on "01 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2533
  Scenario: Verify that charge can be added to loan after disbursement date (loan status is 'active')
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "10 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan's actualMaturityDate is "31 January 2023"
    When Loan Pay-off is made on "10 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2534
  Scenario: Verify that charge can be added to loan after partial repayment (loan status is 'active')
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 500 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan's actualMaturityDate is "31 January 2023"
    When Loan Pay-off is made on "10 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2535
  Scenario: Verify that charge can be added to loan which is reopened by chargeback transaction after got overpaid by repayment  (loan status is 'active')
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1200 EUR transaction amount
    Then Loan status will be "OVERPAID"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 300 EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan's actualMaturityDate is "31 January 2023"
    When Loan Pay-off is made on "10 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2536
  Scenario: Verify that charge can be added to loan which is reopened by payment undo transaction after got overpaid by repayment  (loan status is 'active')
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "05 January 2023"
    And Customer makes "AUTOPAY" repayment on "05 January 2023" with 700 EUR transaction amount
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 500 EUR transaction amount
    Then Loan status will be "OVERPAID"
    When Customer undo "1"th transaction made on "05 January 2023"
    Then Loan status will be "ACTIVE"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan's actualMaturityDate is "31 January 2023"
    When Loan Pay-off is made on "10 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2537
  Scenario: Verify that charge can be added to loan which is reopened by undo goodwill credit transaction after got overpaid by goodwill credit transaction  (loan status is 'active')
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 900 EUR transaction amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "10 January 2023" with 300 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    When Customer undo "2"th transaction made on "10 January 2023"
    Then Loan status will be "ACTIVE"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan's actualMaturityDate is "31 January 2023"
    When Loan Pay-off is made on "10 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2538
  Scenario: Verify that charge can be added to loan which is reopened by undo repayment after got overpaid by goodwill credit transaction  (loan status is 'active')
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 900 EUR transaction amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "10 January 2023" with 300 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    When Customer undo "1"th transaction made on "10 January 2023"
    Then Loan status will be "ACTIVE"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan's actualMaturityDate is "31 January 2023"
    When Loan Pay-off is made on "10 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2601
  Scenario: Verify that loanChargePaidByList section has the correct data in loanDetails and in LoanTransactionMakeRepaymentPostBusinessEvent
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "03 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "03 January 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "04 January 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "04 January 2023" due date and 20 EUR transaction amount
    When Admin sets the business date to "05 January 2023"
    And Customer makes "AUTOPAY" repayment on "05 January 2023" with 200 EUR transaction amount
    Then Loan details and LoanTransactionMakeRepaymentPostBusinessEvent has the following data in loanChargePaidByList section:
      | amount | name       |
      | 10.0   | Snooze fee |
      | 20.0   | NSF fee    |
    When Loan Pay-off is made on "05 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2606
  Scenario: Verify that after COB job Accrual entry is made when loan has a fee-charge on disbursal date
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
    When Loan Pay-off is made on "01 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2607
  Scenario: Verify that after COB job Accrual entry is made when loan has a penalty-charge on disbursal date
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2023" due date and 10 EUR transaction amount
    When Admin sets the business date to "02 January 2023"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has a transaction with date: "01 January 2023", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Accrual          | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          |
    When Loan Pay-off is made on "01 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2635
  Scenario: Verify that charge can be added to loan which is paid off and overpaid by refund
    When Admin sets the business date to "10 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "10 January 2023"
    And Admin successfully approves the loan on "10 January 2023" with "1000" amount and expected disbursement date on "10 January 2023"
    When Admin successfully disburse the loan on "10 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "10 January 2023"
    And Customer makes "AUTOPAY" repayment on "10 January 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "10 January 2023" with 50 EUR transaction amount
    Then Loan status will be "OVERPAID"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 10 EUR transaction amount
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    And Admin makes Credit Balance Refund transaction on "10 January 2023" with 40 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2672
  Scenario: FEE01 - Verify the loan creation with charge: disbursement percentage fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    When Admin adds "LOAN_DISBURSEMENT_PERCENTAGE_FEE" charge with 1.5 % of transaction amount
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Repayment (at time of disbursement) | 15.0   | 0.0       | 0.0      | 15.0 | 0.0       | 1000.0       |
      | 01 January 2023  | Disbursement                        | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name                        | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement percentage fee | false     | Disbursement   |           | % Amount         | 15.0 | 15.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 15.0 |           | 15.0   | 15.0 |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 15   | 0         | 1015 | 15   | 0          | 0    | 1000        |
    When Loan Pay-off is made on "01 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2673
  Scenario: FEE02 - Verify the loan creation with charge: flat fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "10 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 January 2023" due date and 15 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 January 2023 | Flat             | 15.0 | 0.0  | 0.0    | 15.0        |
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 15.0 | 0.0       | 1015.0 | 0.0  | 0.0        | 0.0  | 1015.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 15   | 0         | 1015 | 0    | 0          | 0    | 1015        |
    When Loan Pay-off is made on "01 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2674
  Scenario: FEE03 - Verify the loan creation with charge: installment percentage fee
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "10 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST" charge with 1.5 % of transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 46.35 | 0.0  | 0.0    | 46.35       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0   |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 15.45 | 0.0       | 1045.45 | 0.0  | 0.0        | 0.0  | 1045.45     |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 15.45 | 0.0       | 1045.45 | 0.0  | 0.0        | 0.0  | 1045.45     |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 15.45 | 0.0       | 1045.45 | 0.0  | 0.0        | 0.0  | 1045.45     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 46.35 | 0         | 3136.35 | 0    | 0          | 0    | 3136.35     |
    When Loan Pay-off is made on "01 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2675
  Scenario: FEE04 - Verify the loan creation with charge: overdue fee on principal
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT | 1 January 2023    | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "10 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 May 2023"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Accrual          | 45.0   | 0.0       | 30.0     | 0.0  | 15.0      | 0.0          |
      | 01 March 2023    | Accrual          | 45.0   | 0.0       | 30.0     | 0.0  | 15.0      | 0.0          |
      | 01 April 2023    | Accrual          | 45.0   | 0.0       | 30.0     | 0.0  | 15.0      | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | % Late fee | true      | Overdue Fees   | 01 February 2023 | % Amount         | 15.0 | 0.0  | 0.0    | 15.0        |
      | % Late fee | true      | Overdue Fees   | 01 March 2023    | % Amount         | 15.0 | 0.0  | 0.0    | 15.0        |
      | % Late fee | true      | Overdue Fees   | 01 April 2023    | % Amount         | 15.0 | 0.0  | 0.0    | 15.0        |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 0.0  | 15.0      | 1045.0 | 0.0  | 0.0        | 0.0  | 1045.0      |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 0.0  | 15.0      | 1045.0 | 0.0  | 0.0        | 0.0  | 1045.0      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 0.0  | 15.0      | 1045.0 | 0.0  | 0.0        | 0.0  | 1045.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 0.0  | 45        | 3135.0 | 0    | 0          | 0    | 3135.0      |
    When Loan Pay-off is made on "01 May 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2676
  Scenario: FEE05 - Verify the loan creation with charge: overdue fee on principal+interest
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT_INTEREST | 1 January 2023    | 3000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2023" with "3000" amount and expected disbursement date on "10 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "3000" EUR transaction amount
    When Admin sets the business date to "01 May 2023"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
      | 01 February 2023 | Accrual          | 45.45  | 0.0       | 30.0     | 0.0  | 15.45     | 0.0          |
      | 01 March 2023    | Accrual          | 45.45  | 0.0       | 30.0     | 0.0  | 15.45     | 0.0          |
      | 01 April 2023    | Accrual          | 45.45  | 0.0       | 30.0     | 0.0  | 15.45     | 0.0          |
    Then Loan Charges tab has the following data:
      | Name                       | isPenalty | Payment due at | Due as of        | Calculation type         | Due   | Paid | Waived | Outstanding |
      | % Late fee amount+interest | true      | Overdue Fees   | 01 February 2023 | % Loan Amount + Interest | 15.45 | 0.0  | 0.0    | 15.45       |
      | % Late fee amount+interest | true      | Overdue Fees   | 01 March 2023    | % Loan Amount + Interest | 15.45 | 0.0  | 0.0    | 15.45       |
      | % Late fee amount+interest | true      | Overdue Fees   | 01 April 2023    | % Loan Amount + Interest | 15.45 | 0.0  | 0.0    | 15.45       |
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 3000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 2000.0          | 1000.0        | 30.0     | 0.0  | 15.45     | 1045.45 | 0.0  | 0.0        | 0.0  | 1045.45     |
      | 2  | 28   | 01 March 2023    |           | 1000.0          | 1000.0        | 30.0     | 0.0  | 15.45     | 1045.45 | 0.0  | 0.0        | 0.0  | 1045.45     |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 1000.0        | 30.0     | 0.0  | 15.45     | 1045.45 | 0.0  | 0.0        | 0.0  | 1045.45     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 3000          | 90       | 0.0  | 46.35     | 3136.35 | 0    | 0          | 0    | 3136.35     |
    When Loan Pay-off is made on "01 May 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2790
  Scenario: Verify that partially waived installment fee applied correctly in reverse-replay logic
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST" charge with 10 % of transaction amount
    When Admin sets the business date to "15 January 2023"
    And Customer makes "AUTOPAY" repayment on "15 January 2023" with 5 EUR transaction amount
    When Admin sets the business date to "20 January 2023"
    And Admin waives due date charge
    And Customer makes "AUTOPAY" repayment on "18 January 2023" with 15 EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0   |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 100.0 | 0.0       | 1100.0 | 20.0 | 20.0       | 0.0  | 985.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 100.0 | 0         | 1100.0 | 20   | 20         | 0    | 985.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 January 2023  | Repayment          | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 1000.0       |
      | 18 January 2023  | Repayment          | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 985.0        |
      | 20 January 2023  | Waive loan charges | 95.0   | 0.0       | 0.0      | 0.0  | 0.0       | 985.0        |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 100.0 | 5.0  | 95.0   | 0.0         |
    When Loan Pay-off is made on "20 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C2909
  Scenario: Verify that adding charge on a closed loan after maturity date is creating an N+1 installment - LP1 product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 October 2023"
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "01 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 31 October 2023  | 31 October 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 1    | 01 November 2023 |                 | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0   | 0.0    | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20.0 | 0         | 1020.0 | 1000 | 0          | 0    | 20.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 31 October 2023  | Repayment        | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    When Loan Pay-off is made on "01 November 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

