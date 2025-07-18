@ChargeFeature
Feature: LoanCharge

  @TestRailId:C50
  Scenario: Charge creation functionality with locale EN
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "6000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "6000" amount and expected disbursement date on "1 July 2022"
    When Admin successfully disburse the loan on "1 July 2022" with "6000" EUR transaction amount
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "10 July 2022"
    Then Charge is successfully added to the loan with 600 EUR

  @TestRailId:C51
  Scenario: Charge creation functionality with locale DE
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "6000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "6000" amount and expected disbursement date on "1 July 2022"
    When Admin successfully disburse the loan on "1 July 2022" with "6000" EUR transaction amount
    And Admin adds a 10 % Processing charge to the loan with "de_DE" locale on date: "10 Juli 2022"
    Then Charge is successfully added to the loan with 600 EUR

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

  @TestRailId:C2910
  Scenario: Verify that adding charge on a closed loan after maturity date is creating an N+1 installment - LP2 auto payment enabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "15 November 2023"
    And Customer makes "AUTOPAY" repayment on "15 November 2023" with 250 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023  | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 31 October 2023  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 | 15 November 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 5  | 1    | 16 November 2023 |                  | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 1000.0 | 0          | 0    | 20          |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 31 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 15 November 2023 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

  @TestRailId:C2911
  Scenario: Verify that adding charge on a closed loan after maturity date is creating an N+1 installment - LP2 auto payment disabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "15 November 2023"
    And Customer makes "AUTOPAY" repayment on "15 November 2023" with 250 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023  | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 31 October 2023  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 | 15 November 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 5  | 1    | 16 November 2023 |                  | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 1000.0 | 0          | 0    | 20          |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 31 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 15 November 2023 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

  @TestRailId:C2912 @AdvancedPaymentAllocation
  Scenario: Verify that adding charge on a closed loan after maturity date is creating an N+1 installment - LP2 advanced payment allocation product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "15 November 2023"
    And Customer makes "AUTOPAY" repayment on "15 November 2023" with 250 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023  | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 31 October 2023  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 | 15 November 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 5  | 1    | 16 November 2023 |                  | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 1000.0 | 0          | 0    | 20          |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 31 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 15 November 2023 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

  @TestRailId:C2914
  Scenario: Verify that adding charge on a active loan / partial repayment after maturity date is creating an N+1 installment - LP1 product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 October 2023"
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 800 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 200 outstanding amount
    When Admin sets the business date to "01 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 31 October 2023  |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 800.0 | 0.0        | 0.0  | 200.0       |
      | 2  | 1    | 01 November 2023 |           | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0   | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20.0 | 0         | 1020.0 | 800  | 0          | 0    | 220.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 31 October 2023  | Repayment        | 800.0  | 800.0     | 0.0      | 0.0  | 0.0       | 200.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

  @TestRailId:C2915
  Scenario: Verify that adding charge on a active loan / partial repayment after maturity date is creating an N+1 installment - LP2 auto payment enabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "15 November 2023"
    And Customer makes "AUTOPAY" repayment on "15 November 2023" with 100 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 150 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 31 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 100.0 | 0.0        | 0.0  | 150.0       |
      | 5  | 1    | 16 November 2023 |                 | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 850.0 | 0          | 0    | 170         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 31 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 15 November 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 150.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

  @TestRailId:C2916
  Scenario: Verify that adding charge on a active loan / partial repayment after maturity date is creating an N+1 installment - LP2 auto payment disabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "15 November 2023"
    And Customer makes "AUTOPAY" repayment on "15 November 2023" with 100 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 150 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 31 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 100.0 | 0.0        | 0.0  | 150.0       |
      | 5  | 1    | 16 November 2023 |                 | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 850.0 | 0          | 0    | 170         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 31 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 15 November 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 150.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

  @TestRailId:C2917 @AdvancedPaymentAllocation
  Scenario: Verify that adding charge on an active loan / partial repayment after maturity date is creating an N+1 installment - LP2 advanced payment allocation product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "31 October 2023"
    And Customer makes "AUTOPAY" repayment on "31 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "15 November 2023"
    And Customer makes "AUTOPAY" repayment on "15 November 2023" with 100 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 150 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 31 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 100.0 | 0.0        | 0.0  | 150.0       |
      | 5  | 1    | 16 November 2023 |                 | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 850.0 | 0          | 0    | 170         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 31 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 15 November 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 150.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

  @TestRailId:C2918
  Scenario: Verify that adding charge on a active loan / no repayment made, after maturity date is creating an N+1 installment - LP1 product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 October 2023"
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "01 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 October 2023  |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 1    | 01 November 2023 |           | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0   | 0.0  | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20.0 | 0         | 1020.0 | 0    | 0          | 0    | 1020.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 01 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

  @TestRailId:C2919
  Scenario: Verify that adding charge on a active loan / no repayment made, after maturity date is creating an N+1 installment - LP2 auto payment enabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 750 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 5  | 1    | 16 November 2023 |                 | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 250.0 | 0          | 0    | 770         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

  @TestRailId:C2920
  Scenario: Verify that adding charge on a active loan / no repayment made, after maturity date is creating an N+1 installment - LP2 auto payment disabled
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 October 2023  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 October 2023  |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 October 2023  |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 5  | 1    | 16 November 2023 |           | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0  | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 0.0  | 0          | 0    | 1020        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

  @TestRailId:C2921 @AdvancedPaymentAllocation
  Scenario: Verify that adding charge on an active loan / no repayment made, after maturity date is creating an N+1 installment - LP2 advanced payment allocation product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 750 outstanding amount
    When Admin sets the business date to "16 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 November 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 5  | 1    | 16 November 2023 |                 | 0.0             | 0.0           | 0.0      | 20.0 | 0.0       | 20.0  | 0.0   | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 20   | 0         | 1020.0 | 250.0 | 0          | 0    | 770         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 November 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

  @TestRailId:C2923 @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC1
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 October 2023"
    And Customer makes "AUTOPAY" repayment on "10 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    And Customer makes "AUTOPAY" repayment on "20 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "09 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 10 October 2023 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 270.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 20 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 800.0 | 550.0      | 0.0  | 220.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 10 October 2023  | Repayment        | 300.0  | 280.0     | 0.0      | 20.0 | 0.0       | 470.0        |
      | 20 October 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 220.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 09 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |

  @TestRailId:C2924 @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC2
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 October 2023"
    And Customer makes "AUTOPAY" repayment on "10 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    And Customer makes "AUTOPAY" repayment on "20 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 20 October 2023 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 250.0      | 20.0 | 0.0         |
      | 3  | 15   | 31 October 2023  | 20 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 800.0 | 530.0      | 20.0 | 220.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 10 October 2023  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 450.0        |
      | 20 October 2023  | Repayment        | 250.0  | 230.0     | 0.0      | 20.0 | 0.0       | 220.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |

  @TestRailId:C2925 @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC3
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 October 2023"
    And Customer makes "AUTOPAY" repayment on "10 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    And Customer makes "AUTOPAY" repayment on "20 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "11 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 20 October 2023 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 250.0      | 20.0 | 0.0         |
      | 3  | 15   | 31 October 2023  | 20 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 800.0 | 530.0      | 20.0 | 220.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 10 October 2023  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 450.0        |
      | 20 October 2023  | Repayment        | 250.0  | 230.0     | 0.0      | 20.0 | 0.0       | 220.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 11 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |

  @TestRailId:C2926 @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC4
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 October 2023"
    And Customer makes "AUTOPAY" repayment on "10 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    And Customer makes "AUTOPAY" repayment on "20 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 20 October 2023 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 250.0      | 20.0 | 0.0         |
      | 3  | 15   | 31 October 2023  | 20 October 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 800.0 | 530.0      | 20.0 | 220.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 10 October 2023  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 450.0        |
      | 20 October 2023  | Repayment        | 250.0  | 230.0     | 0.0      | 20.0 | 0.0       | 220.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |

  @TestRailId:C2927 @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC5
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 October 2023"
    And Customer makes "AUTOPAY" repayment on "10 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    And Customer makes "AUTOPAY" repayment on "20 October 2023" with 250 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "17 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 10 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  | 20 October 2023 | 250.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 270.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 800.0 | 550.0      | 0.0  | 220.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 10 October 2023  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 450.0        |
      | 20 October 2023  | Repayment        | 250.0  | 230.0     | 0.0      | 20.0 | 0.0       | 220.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 17 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |

  @TestRailId:C2928 @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC6
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 550.0 | 30.0       | 0.0  | 470.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 300.0  | 280.0     | 0.0      | 20.0 | 0.0       | 470.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |

  @TestRailId:C2929 @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC7
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  |                 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 250.0 | 0.0        | 0.0  | 20.0        |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 50.0  | 50.0       | 0.0  | 200.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 550.0 | 50.0       | 0.0  | 470.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 450.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 October 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

  @TestRailId:C2930 @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC8
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 300 EUR transaction amount
    When Admin sets the business date to "10 November 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "17 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 50.0  | 50.0       | 0.0  | 220.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 550.0 | 50.0       | 0.0  | 470.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 450.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 17 October 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

  @TestRailId:C2931 @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC9
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 300 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 October 2023" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  |                 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 250.0 | 0.0        | 0.0  | 20.0        |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 50.0  | 50.0       | 0.0  | 200.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 550.0 | 50.0       | 0.0  | 470.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 450.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 October 2023 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |

  @TestRailId:C2932 @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC10
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 October 2023" due date and 20 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 300 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 550.0 | 30.0       | 0.0  | 470.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 300.0  | 280.0     | 0.0      | 20.0 | 0.0       | 470.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 16 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |

  @TestRailId:C2933 @AdvancedPaymentAllocation
  Scenario: Verify Loan charge reverse-replaying logic for LP2 advanced payment allocation product - UC11
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "15 October 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 October 2023" due date and 20 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 300 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 October 2023  | 16 October 2023 | 500.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 30.0  | 30.0       | 0.0  | 220.0       |
      | 4  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 550.0 | 30.0       | 0.0  | 470.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 October 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 16 October 2023  | Repayment        | 300.0  | 280.0     | 0.0      | 20.0 | 0.0       | 470.0        |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 15 October 2023 | Flat             | 20.0 | 20.0 | 0.0    | 0.0         |

  @TestRailId:C2993
  Scenario: Waive charge on LP2 cumulative loan product
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2023   | 750            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 05 April 2023 | Flat             | 10.0 | 0.0  | 10.0   | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement       | 750.0  | 0.0       | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 January 2023  | Down Payment       | 188.0  | 188.0     | 0.0      | 0.0  | 0.0       | 562.0        |
      | 01 February 2023 | Repayment          | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 312.0        |
      | 01 March 2023    | Repayment          | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 62.0         |
      | 01 April 2023    | Repayment          | 250.0  | 62.0      | 0.0      | 0.0  | 0.0       | 0.0          |
      | 05 April 2023    | Waive loan charges | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 62.0         |
    Then On Loan Transactions tab the "Repayment" Transaction with date "01 April 2023" is reverted
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Waived | Outstanding |
      |    |      | 01 January 2023  |                  | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |        |             |
      | 1  | 0    | 01 January 2023  | 01 January 2023  | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 188.0 | 0.0        | 0.0   | 0.0    | 0.0         |
      | 2  | 15   | 16 January 2023  | 01 February 2023 | 375.0           | 187.0         | 0.0      | 0.0  | 0.0       | 187.0 | 187.0 | 0.0        | 187.0 | 0.0    | 0.0         |
      | 3  | 15   | 31 January 2023  | 01 March 2023    | 188.0           | 187.0         | 0.0      | 0.0  | 0.0       | 187.0 | 187.0 | 0.0        | 187.0 | 0.0    | 0.0         |
      | 4  | 15   | 15 February 2023 |                  | 0.0             | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 126.0 | 0.0        | 126.0 | 0.0    | 62.0        |
      | 5  | 49   | 05 April 2023    | 05 April 2023    | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0  | 0.0   | 0.0        | 0.0   | 10.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid  | In advance | Late | Waived | Outstanding |
      | 750           | 0        | 0    | 10        | 760 | 688.0 | 0          | 500  | 10     | 62.0        |

  @TestRailId:C2994
  Scenario: Waive charge on LP2 progressive loan
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2023   | 750            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 05 April 2023 | Flat             | 10.0 | 0.0  | 10.0   | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement       | 750.0  | 0.0       | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2023 | Repayment          | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 March 2023    | Repayment          | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 01 April 2023    | Repayment          | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 05 April 2023    | Waive loan charges | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 250.0        |
    Then On Loan Transactions tab the "Repayment" Transaction with date "01 April 2023" is reverted
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Waived | Outstanding |
      |    |      | 01 January 2023  |                  | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |        |             |
      | 1  | 0    | 01 January 2023  | 01 February 2023 | 562.5           | 187.5         | 0.0      | 0.0  | 0.0       | 187.5 | 187.5 | 0.0        | 187.5 | 0.0    | 0.0         |
      | 2  | 15   | 16 January 2023  | 01 March 2023    | 375.0           | 187.5         | 0.0      | 0.0  | 0.0       | 187.5 | 187.5 | 0.0        | 187.5 | 0.0    | 0.0         |
      | 3  | 15   | 31 January 2023  |                  | 187.5           | 187.5         | 0.0      | 0.0  | 0.0       | 187.5 | 125.0 | 0.0        | 125.0 | 0.0    | 62.5        |
      | 4  | 15   | 15 February 2023 |                  | 0.0             | 187.5         | 0.0      | 0.0  | 0.0       | 187.5 | 0.0   | 0.0        | 0.0   | 0.0    | 187.5       |
      | 5  | 49   | 05 April 2023    | 05 April 2023    | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0  | 0.0   | 0.0        | 0.0   | 10.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Waived | Outstanding |
      | 750           | 0        | 0    | 10        | 760 | 500  | 0          | 500  | 10     | 250         |

  @TestRailId:C2995
  Scenario: Verify that when a charge added after maturity had been waived the added N+1 installment will be paid with a paid by date (obligations met date) of the transaction date of the waive charge transaction
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "22 February 2023" due date and 100 EUR transaction amount
    When Admin sets the business date to "31 March 2023"
    And Admin waives due date charge
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Waived | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |        |             |
      | 1  | 0    | 01 January 2023  |                  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 0.0    | 250.0       |
      | 2  | 15   | 16 January 2023  |                  | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 0.0    | 250.0       |
      | 3  | 15   | 31 January 2023  |                  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 0.0    | 250.0       |
      | 4  | 15   | 15 February 2023 |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 0.0    | 250.0       |
      | 5  | 7    | 22 February 2023 | 22 February 2023 | 0.0             | 0.0           | 0.0      | 0.0  | 100.0     | 100.0 | 0.0  | 0.0        | 0.0  | 100.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Waived | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 100.0     | 1100.0 | 0.0  | 0.0        | 0.0  | 100.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 22 February 2023 | Waive loan charges | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due   | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 22 February 2023 | Flat             | 100.0 | 0.0  | 100.0  | 0.0         |

  @TestRailId:C3260
  Scenario: Verify that there are no payable interest and fee after charge adjustment made on the same date for progressive loan with custom payment allocation order
    When Admin sets the business date to "27 September 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 27 Sep 2024       | 100            | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "27 September 2024" with "40" amount and expected disbursement date on "27 September 2024"
    When Admin successfully disburse the loan on "27 September 2024" with "40" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 27 September 2024 |           | 40.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 27 October 2024   |           | 0.0             | 40.0          | 0.33     | 0.0  | 0.0       | 40.33 | 0.0  | 0.0        | 0.0  | 40.33       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 40.0          | 0.33     | 0.0  | 0.0       | 40.33 | 0.0  | 0.0        | 0.0  | 40.33       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 27 September 2024 | Disbursement     | 40.0   | 0.0       | 0.0      | 0.0  | 0.0       | 40.0         |
    When Admin adds "LOAN_NSF_FEE" due date charge with "27 September 2024" due date and 1 EUR transaction amount
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "27 September 2024" with 1 EUR transaction amount and externalId ""
    Then Loan has 40.32 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 27 September 2024 |           | 40.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 27 October 2024   |           | 0.0             | 40.0          | 0.32     | 0.0  | 1.0       | 41.32 | 1.0  | 1.0        | 0.0  | 40.32       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 40.0          | 0.32     | 0.0  | 1.0       | 41.32 | 1.0  | 1.0        | 0.0  | 40.32       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 27 September 2024 | Disbursement      | 40.0   | 0.0       | 0.0      | 0.0  | 0.0       | 40.0         |
      | 27 September 2024 | Charge Adjustment | 1.0    | 1.0       | 0.0      | 0.0  | 0.0       | 39.0         |

  @TestRailId:C3319
  Scenario: Verify enhance the existing implementation to create accruals as part of Charge Creation post maturity with immediate charge accrual and non-zero interest rate
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is enabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "05 February 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 February 2024" due date and 25 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 05 February 2024 | Flat             | 25.0 | 0.0  | 0.0    | 25.0        |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
      | 2  | 4    | 05 February 2024 |           | 0.0             | 0.0           | 0.0      | 0.0  | 25.0      | 25.0    | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 25.0      | 1030.83 | 0.0  | 0.0        | 0.0  | 1030.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 February 2024 | Accrual          | 25.0   | 0.0       | 0.0      | 0.0  | 25.0      | 0.0          |
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is disabled
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 February 2024"

  @TestRailId:C3320
  Scenario: Verify enhance the existing implementation to create accruals as part of Charge Creation post maturity with immediate charge accrual and zero interest rate
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is enabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "05 February 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 February 2024" due date and 25 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 05 February 2024 | Flat             | 25.0 | 0.0  | 0.0    | 25.0        |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 4    | 05 February 2024 |           | 0.0             | 0.0           | 0.0      | 0.0  | 25.0      | 25.0   | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 25.0      | 1025.0 | 0.0  | 0.0        | 0.0  | 1025.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 February 2024 | Accrual          | 25.0   | 0.0       | 0.0      | 0.0  | 25.0      | 0.0          |
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is disabled
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 February 2024"

  @TestRailId:С3335
  Scenario: Verify enhance the existing implementation to create accruals as part of Charge Creation post maturity with inline COB run and non-zero interest rate
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is disabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "05 February 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 February 2024" due date and 25 EUR transaction amount
    When Admin sets the business date to "06 February 2024"
    And Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 05 February 2024 | Flat             | 25.0 | 0.0  | 0.0    | 25.0        |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
      | 2  | 4    | 05 February 2024 |           | 0.0             | 0.0           | 0.0      | 0.0  | 25.0      | 25.0    | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 25.0      | 1030.83 | 0.0  | 0.0        | 0.0  | 1030.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 February 2024 | Accrual          | 30.83  | 0.0       | 5.83     | 0.0  | 25.0      | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 February 2024"

  @TestRailId:C3321
  Scenario: Verify enhance the existing implementation to create accruals as part of Charge Creation post maturity with inline COB run and zero interest rate
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is disabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "05 February 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 February 2024" due date and 25 EUR transaction amount
    When Admin sets the business date to "06 February 2024"
    And Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 05 February 2024 | Flat             | 25.0 | 0.0  | 0.0    | 25.0        |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 4    | 05 February 2024 |           | 0.0             | 0.0           | 0.0      | 0.0  | 25.0      | 25.0   | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 25.0      | 1025.0 | 0.0  | 0.0        | 0.0  | 1025.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 February 2024 | Accrual          | 25.0   | 0.0       | 0.0      | 0.0  | 25.0      | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 February 2024"

  @TestRailId:C3336
  Scenario: Verify enhance the existing implementation to create accruals as part of Charge Creation post maturity with immediate charge accrual with inline COB run and non-zero interest rate
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is enabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "05 February 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 February 2024" due date and 25 EUR transaction amount
    When Admin sets the business date to "06 February 2024"
    And Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 05 February 2024 | Flat             | 25.0 | 0.0  | 0.0    | 25.0        |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
      | 2  | 4    | 05 February 2024 |           | 0.0             | 0.0           | 0.0      | 0.0  | 25.0      | 25.0    | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 25.0      | 1030.83 | 0.0  | 0.0        | 0.0  | 1030.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 February 2024 | Accrual          | 25.0   | 0.0       | 0.0      | 0.0  | 25.0      | 0.0          |
      | 05 February 2024 | Accrual          | 5.83   | 0.0       | 5.83     | 0.0  | 0.0       | 0.0          |
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is disabled
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 February 2024"

  @TestRailId:C3425
  Scenario: Verify that charge paid is populated for interest bearing products after charge adjustment is being posted
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7.0                    | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    When Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 75.21           | 24.79         | 0.58     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 2  | 29   | 01 March 2024    |           | 50.28           | 24.93         | 0.44     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 3  | 31   | 01 April 2024    |           | 25.2            | 25.08         | 0.29     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 25.2          | 0.15     | 0.0  | 0.0       | 25.35 | 0.0  | 0.0        | 0.0  | 25.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 1.46     | 0.0  | 0.0       | 101.46 | 0.0  | 0.0        | 0.0  | 101.46      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin sets the business date to "01 February 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2024" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 75.21           | 24.79         | 0.58     | 0.0  | 20.0      | 45.37 | 0.0  | 0.0        | 0.0  | 45.37       |
      | 2  | 29   | 01 March 2024    |           | 50.28           | 24.93         | 0.44     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 3  | 31   | 01 April 2024    |           | 25.2            | 25.08         | 0.29     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 25.2          | 0.15     | 0.0  | 0.0       | 25.35 | 0.0  | 0.0        | 0.0  | 25.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 1.46     | 0.0  | 20.0      | 121.46 | 0.0  | 0.0        | 0.0  | 121.46      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 01 February 2024 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
  #   --- Backdated repayment with 60 EUR ---
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 60 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |                 | 74.63           | 25.37         | 0.0      | 0.0  | 20.0      | 45.37 | 25.37 | 25.37      | 0.0  | 20.0        |
      | 2  | 29   | 01 March 2024    | 01 January 2024 | 49.26           | 25.37         | 0.0      | 0.0  | 0.0       | 25.37 | 25.37 | 25.37      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                 | 24.58           | 24.68         | 0.69     | 0.0  | 0.0       | 25.37 | 9.26  | 9.26       | 0.0  | 16.11       |
      | 4  | 30   | 01 May 2024      |                 | 0.0             | 24.58         | 0.14     | 0.0  | 0.0       | 24.72 | 0.0   | 0.0        | 0.0  | 24.72       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 0.83     | 0.0  | 20.0      | 120.83 | 60.0 | 60.0       | 0.0  | 60.83       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 January 2024  | Repayment        | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 40.0         |
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 01 February 2024 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "01 February 2024" with 20 EUR transaction amount and externalId ""
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 74.63           | 25.37         | 0.0      | 0.0  | 20.0      | 45.37 | 45.37 | 25.37      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 January 2024  | 49.26           | 25.37         | 0.0      | 0.0  | 0.0       | 25.37 | 25.37 | 25.37      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 24.58           | 24.68         | 0.69     | 0.0  | 0.0       | 25.37 | 9.26  | 9.26       | 0.0  | 16.11       |
      | 4  | 30   | 01 May 2024      |                  | 0.0             | 24.58         | 0.14     | 0.0  | 0.0       | 24.72 | 0.0   | 0.0        | 0.0  | 24.72       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 0.83     | 0.0  | 20.0      | 120.83 | 80.0 | 60.0       | 0.0  | 40.83       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement      | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 January 2024  | Repayment         | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 40.0         |
      | 01 February 2024 | Charge Adjustment | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 40.0         |
    And LoanChargeAdjustmentPostBusinessEvent is raised on "01 February 2024"

  @TestRailId:C3501
  Scenario: Verify repayment schedule amounts after large charge amount added - UC1
    When Admin sets the business date to "20 February 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 20 December 2024  | 800            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "20 December 2024" with "800" amount and expected disbursement date on "20 December 2024"
    When Admin successfully disburse the loan on "20 December 2024" with "800" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      |    |      | 20 December 2024 |           | 800.0           |               |          | 0.0  |           | 0.0      | 0.0  |            |      |             |
      | 1  | 31   | 20 January 2025  |           | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
      | 2  | 31   | 20 February 2025 |           | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
      | 3  | 28   | 20 March 2025    |           | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
      | 4  | 31   | 20 April 2025    |           | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 0.0       | 800.0 | 0.0  | 0.0        | 0.0  | 800.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 December 2024 | Disbursement     | 800.0   | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    And Admin adds "LOAN_NSF_FEE" due date charge with "25 December 2024" due date and 123456789012.12 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties       | Due             | Paid | In advance | Late | Outstanding     |
      |    |      | 20 December 2024 |           | 800.0           |               |          | 0.0  |                 | 0.0             | 0.0  |            |      |                 |
      | 1  | 31   | 20 January 2025  |           | 600.0           | 200.0         | 0.0      | 0.0  | 123456789012.12 | 123456789212.12 | 0.0  | 0.0        | 0.0  | 123456789212.12 |
      | 2  | 31   | 20 February 2025 |           | 400.0           | 200.0         | 0.0      | 0.0  | 0.0             | 200.0           | 0.0  | 0.0        | 0.0  | 200.0           |
      | 3  | 28   | 20 March 2025    |           | 200.0           | 200.0         | 0.0      | 0.0  | 0.0             | 200.0           | 0.0  | 0.0        | 0.0  | 200.0           |
      | 4  | 31   | 20 April 2025    |           | 0.0             | 200.0         | 0.0      | 0.0  | 0.0             | 200.0           | 0.0  | 0.0        | 0.0  | 200.0           |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties       | Due             | Paid | In advance | Late | Outstanding     |
      | 800.0         | 0.0      | 0.0  | 123456789012.12 | 123456789812.12 | 0.0  | 0.0        | 0.0  | 123456789812.12 |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 December 2024 | Disbursement     | 800.0   | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due             | Paid | Waived | Outstanding     |
      | NSF fee | true      | Specified due date | 25 December 2024 | Flat             | 123456789012.12 | 0.0  | 0.0    | 123456789012.12 |

  @TestRailId:C3502
  Scenario: Verify repayment schedule amounts after a few large charges amount added - UC2
    When Admin sets the business date to "20 February 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 20 December 2024  | 800            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "20 December 2024" with "800" amount and expected disbursement date on "20 December 2024"
    When Admin successfully disburse the loan on "20 December 2024" with "800" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      |    |      | 20 December 2024 |           | 800.0           |               |          | 0.0  |           | 0.0      | 0.0  |            |      |             |
      | 1  | 31   | 20 January 2025  |           | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
      | 2  | 31   | 20 February 2025 |           | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
      | 3  | 28   | 20 March 2025    |           | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
      | 4  | 31   | 20 April 2025    |           | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 0.0       | 800.0 | 0.0  | 0.0        | 0.0  | 800.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 December 2024 | Disbursement     | 800.0   | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    And Admin adds "LOAN_NSF_FEE" due date charge with "25 December 2024" due date and 123456789012.12 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "28 December 2024" due date and 1003456789012.12 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "31 January 2025" due date and 5503456789012.12 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "23 February 2025" due date and 1003456789012.12 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "03 April 2025" due date and 1503456789012.12 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "09 April 2025" due date and 103456789037.12 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees             | Penalties        | Due              | Paid | In advance | Late | Outstanding      |
      |    |      | 20 December 2024 |           | 800.0           |               |          | 0.0              |                  | 0.0              | 0.0  |            |      |                  |
      | 1  | 31   | 20 January 2025  |           | 600.0           | 200.0         | 0.0      | 1003456789012.12 | 123456789012.12  | 1126913578224.24 | 0.0  | 0.0        | 0.0  | 1126913578224.24 |
      | 2  | 31   | 20 February 2025 |           | 400.0           | 200.0         | 0.0      | 0.0              | 5503456789012.12 | 5503456789212.12 | 0.0  | 0.0        | 0.0  | 5503456789212.12 |
      | 3  | 28   | 20 March 2025    |           | 200.0           | 200.0         | 0.0      | 0.0              | 1003456789012.12 | 1003456789212.12 | 0.0  | 0.0        | 0.0  | 1003456789212.12 |
      | 4  | 31   | 20 April 2025    |           | 0.0             | 200.0         | 0.0      | 103456789037.12  | 1503456789012.12 | 1606913578249.24 | 0.0  | 0.0        | 0.0  | 1606913578249.24 |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees             | Penalties        | Due              | Paid | In advance | Late | Outstanding      |
      | 800.0         | 0.0      | 1106913578049.24 | 8133827156048.48 | 9240740734897.72 | 0.0  | 0.0        | 0.0  | 9240740734897.72 |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 December 2024 | Disbursement     | 800.0   | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due              | Paid | Waived | Outstanding      |
      | NSF fee    | true      | Specified due date | 25 December 2024 | Flat             | 123456789012.12  | 0.0  | 0.0    | 123456789012.12  |
      | Snooze fee | false     | Specified due date | 28 December 2024 | Flat             | 1003456789012.12 | 0.0  | 0.0    | 1003456789012.12 |
      | NSF fee    | true      | Specified due date | 31 January 2025  | Flat             | 5503456789012.12 | 0.0  | 0.0    | 5503456789012.12 |
      | NSF fee    | true      | Specified due date | 23 February 2025 | Flat             | 1003456789012.12 | 0.0  | 0.0    | 1003456789012.12 |
      | NSF fee    | true      | Specified due date | 03 April 2025    | Flat             | 1503456789012.12 | 0.0  | 0.0    | 1503456789012.12 |
      | Snooze fee | false     | Specified due date | 09 April 2025    | Flat             | 103456789037.12  | 0.0  | 0.0    | 103456789037.12  |

  @TestRailId:C3543
  Scenario: Check that subResourceExternalId present in charge adjustment response after loan is charged off, accounting none
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_ACCOUNTING_RULE_NONE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 March 2024" due date and 5 EUR transaction amount
    And Admin does charge-off the loan on "01 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.14           | 16.43         | 0.58     | 5.0  | 0.0       | 22.01 | 0.0  | 0.0        | 0.0  | 22.01       |
      | 3  | 31   | 01 April 2024    |           | 50.52           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0  | 0.0        | 0.0  | 17.09       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Accrual            | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Accrual Adjustment | 0.89   | 0.0       | 0.89     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off         | 107.14 | 100.0     | 2.14     | 5.0  | 0.0       | 0.0          | false    | false    |
    When Admin makes a charge adjustment for the last "LOAN_SNOOZE_FEE" type charge which is due on "01 March 2024" with 5 EUR transaction amount and externalId ""
    Then Charge adjustment response has the subResourceExternalId
    Then Loan has 1 "CHARGE_ADJUSTMENT" transactions on Transactions tab
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Accrual            | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Accrual Adjustment | 0.89   | 0.0       | 0.89     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off         | 107.14 | 100.0     | 2.14     | 5.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge Adjustment  | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 95.0         | false    | false    |

  @TestRailId:C3544
  Scenario: Check that subResourceExternalId present in charge adjustment response after loan is charged off, accrual activity
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 March 2024" due date and 5 EUR transaction amount
    And Admin does charge-off the loan on "01 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.14           | 16.43         | 0.58     | 5.0  | 0.0       | 22.01 | 0.0  | 0.0        | 0.0  | 22.01       |
      | 3  | 31   | 01 April 2024    |           | 50.52           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0  | 0.0        | 0.0  | 17.09       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 March 2024    | Accrual          | 1.16   | 0.0       | 1.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 107.14 | 100.0     | 2.14     | 5.0  | 0.0       | 0.0          | false    | false    |
    When Admin makes a charge adjustment for the last "LOAN_SNOOZE_FEE" type charge which is due on "01 March 2024" with 5 EUR transaction amount and externalId ""
    Then Charge adjustment response has the subResourceExternalId
    Then Loan has 1 "CHARGE_ADJUSTMENT" transactions on Transactions tab
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement      | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 March 2024    | Accrual           | 1.16   | 0.0       | 1.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off        | 107.14 | 100.0     | 2.14     | 5.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge Adjustment | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 95.0         | false    | false    |

  @TestRailId:C3571
  Scenario: Charge adjustment on account with zero principal balance should not create accrual transactions without journal entries
    When Admin sets the business date to "25 March 2025"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "25 March 2025", with Principal: "800", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "25 March 2025" with "800" amount and expected disbursement date on "25 March 2025"
    When Admin successfully disburse the loan on "25 March 2025" with "800" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "25 March 2025" with 800 EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "25 March 2025" transaction date
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "25 March 2025" with 10 EUR transaction amount and externalId ""
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "25 March 2025" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 10.0   |
      | INCOME | 404007       | Fee Income       | 10.0  |        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 25 March 2025    | Disbursement      | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        |
      | 25 March 2025    | Repayment         | 800.0  | 790.0     | 0.0      | 0.0  | 10.0      | 10.0         |
      | 25 March 2025    | Charge Adjustment | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 0.0          |
      | 25 March 2025    | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "25 March 2025" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |

  @TestRailId:C3546
  Scenario: Verify flat disbursement charge for interest bearing progressive loan - UC1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "FLAT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 10.00           |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 10.0 |           | 10.0  | 10.0 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 10.0 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 27.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 44.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |

  @TestRailId:C3547
  Scenario: Verify amount disbursement charge for interest bearing progressive loan - UC2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.0  | 0.0  | 0.0    | 1.0        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.0  |           | 1.0   | 1.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 1.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.0  | 1.0  | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.0   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 18.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 35.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |

  @TestRailId:C3548
  Scenario: Verify amount+interest disbursement charge for interest bearing progressive loan - UC3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.02 | 0.0  | 0.0    | 1.02        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.02 |           | 1.02  | 1.02 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.02 | 0.0       | 103.07 | 1.02 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.02   | 0.0       | 0.0      | 1.02 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.02 | 1.02 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.02  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.02 |           | 1.02  | 1.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.02 | 0.0       | 103.07 | 18.03 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.02   | 0.0       | 0.0      | 1.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.02 |           | 1.02  | 1.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.02 | 0.0       | 103.07 | 35.04 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.02   | 0.0       | 0.0      | 1.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |

  @TestRailId:C3549
  Scenario: Verify interest disbursement charge for interest bearing progressive loan - UC4
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.0  | 0.0    | 0.02       |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.02 |           | 0.02  | 0.02 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 0.02 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.02 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.02  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 17.03 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 34.04 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |

  @TestRailId:C3550
  Scenario: Verify interest disbursement charge with cash based accounting for interest bearing progressive loan - UC5
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CASH_ACCOUNTING_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.0  | 0.0    | 0.02       |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.02 |           | 0.02  | 0.02 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 0.02 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Accrual                             | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.02 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.02  |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 January 2024" has no the Journal entries
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 17.03 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Accrual                             | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 34.04 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Accrual                             | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |

  @TestRailId:C3551
  Scenario: Verify interest disbursement charge with accrual based accounting for interest bearing progressive loan - UC6.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 0.02            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.0  | 0.0  | 0.0    | 0.0        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.0  | 0.0  | 0.0    | 0.0         |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 34.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |

  @TestRailId:C3552
  Scenario: Verify amount+interest disbursement charge for interest bearing progressive loan - UC6.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.02            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.04 | 0.0  | 0.0    | 1.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.04 |           | 1.04  | 1.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.04 | 0.0       | 103.09 | 1.04 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.04   | 0.0       | 0.0      | 1.04 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.04 | 1.04 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.04 |           | 1.04  | 1.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.04 | 0.0       | 103.09 | 18.05 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.04   | 0.0       | 0.0      | 1.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.04 |           | 1.04  | 1.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.04 | 0.0       | 103.09 | 35.06 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.04   | 0.0       | 0.0      | 1.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |

  @TestRailId:C3553
  Scenario: Verify interest disbursement charge with undo disbursal for interest bearing progressive loan - UC7
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.0  | 0.0    | 0.02       |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.02 |           | 0.02  | 0.02 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 0.02 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.02 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.02  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 17.03 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 34.04 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
#    --- Undo Disbursement  ---
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.02 |           | 0.02  |      |            |      | 0.02         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 0.0  | 0.0        | 0.0  | 102.07      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.0  | 0.0    | 0.02        |

  @TestRailId:C3578
  Scenario: Verify amount disbursement charge for tranche interest bearing progressive loan that expects one tranche with full disbursement - UC8.1.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.0  |           | 2.0   | 2.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 2.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 2.0  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 2.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 2.0   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 2.0  |           | 2.0   | 2.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 19.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 2.0  | 0.0    | 0.0         |

  @TestRailId:C3579
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that expects one tranche with full disbursement - UC8.1.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type          | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest  | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.04 |           | 2.04  | 2.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 2.04 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 2.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 2.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 2.04 |           | 2.04  | 2.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 19.05 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0         |

  @TestRailId:C3580
  Scenario: Verify interest disbursement charge for tranche interest bearing progressive loan that expects one tranche with full disbursement - UC8.1.3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.0  | 0.0    | 0.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.04 |           | 0.04  | 0.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.04 | 0.0       | 102.09 | 0.04 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.04   | 0.0       | 0.0      | 0.04 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.04 | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.04 |           | 0.04  | 0.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.04 | 0.0       | 102.09 | 17.05 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.04   | 0.0       | 0.0      | 0.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.04 | 0.0    | 0.0         |

  @TestRailId:C3581
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with full disbursement - UC8.1.4
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.0  |           | 2.0   | 2.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 2.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 2.0  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 2.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 2.0   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 2.0  |           | 2.0   | 2.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 19.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 2.0  | 0.0    | 0.0        |

  @Skip
  @TestRailId:C3582
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with full disbursement - UC8.1.5
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.04 |           | 2.04  | 2.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 2.04 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type          | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           |  % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 2.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 2.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 2.04 |           | 2.04  | 2.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 19.05 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type          | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           |  % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0        |

  @TestRailId:C3583
  Scenario: Verify interest disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with full disbursement - UC8.1.6
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.0  | 0.0    | 0.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.04 |           | 0.04  | 0.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.04 | 0.0       | 102.09 | 0.04 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.04   | 0.0       | 0.0      | 0.04 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.04 | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.04 |           | 0.04  | 0.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.04 | 0.0       | 102.09 | 17.05 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.04   | 0.0       | 0.0      | 0.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.04 | 0.0    | 0.0        |

  @Skip
  @TestRailId:C3554
  Scenario: Verify amount disbursement charge for tranche interest bearing progressive loan that expects one tranche - UC8.2.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.4  |           | 1.4   | 1.4  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.87  | 1.4  | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.4  | 1.4  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.4    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.4   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.43 |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.67         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.44     | 1.4  | 0.0       | 72.84  | 13.31 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |

  @Skip
  @TestRailId:C3555
  Scenario: Verify amount disbursement charge for tranche interest bearing progressive loan that expects two tranches with undo last disbursement - UC8.2.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | 1st_tranche_disb_expected_date |1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024                | 70.0                      | 01 February 2024               | 30.0                       |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 2.0  |           | 2.0   | 2.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      |    |      | 01 February 2024 |           | 30.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 2.0  | 0.0       | 73.44  | 2.0  | 0.0        | 0.0  | 71.44       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.0  | 2.0  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 2.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 2.0   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 2.0  |           | 2.0   | 2.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 76.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 65.29           | 11.67         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 53.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 41.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 30.0            | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 2.0  | 0.0       | 72.84  | 13.91 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
#    --- 2nd disbursement - 1 February, 2024  ---
    When Admin successfully disburse the loan on "01 February 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 2.0  |           | 2.0   | 2.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 71.01           | 17.49         | 0.52     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 3  | 31   | 01 April 2024    |                  | 53.41           | 17.6          | 0.41     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 4  | 30   | 01 May 2024      |                  | 35.71           | 17.7          | 0.31     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.91           | 17.8          | 0.21     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.91         | 0.1      | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.96     | 2.0  | 0.0       | 103.96 | 13.91 | 0.0        | 0.0  | 90.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
      | 15 February 2024 | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 88.5         | false    | false    |
# -- undo last disbursement ----
    When Admin successfully undo last disbursal
    Then Loan status has changed to "Active"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 2.0  |           | 2.0   | 2.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.67         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.44     | 2.0  | 0.0       | 73.44  | 13.91 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |

  @Skip
  @TestRailId:C3556
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that expects two tranches with undo disbursement - UC8.2.3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | 1st_tranche_disb_expected_date |1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024                | 70.0                      | 01 February 2024               | 30.0                       |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 2.04 |           | 2.04  | 2.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      |    |      | 01 February 2024 |           | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 2.04 | 0.0       | 73.48  | 2.04 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 2.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 2.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 2.04 |           | 2.04  | 2.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 76.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 65.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 53.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 41.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 30.0            | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 2.04 | 0.0       | 73.48  | 13.95 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
#    --- 2nd disbursement - 1 February, 2024  ---
    When Admin successfully disburse the loan on "01 February 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 2.04 |           | 2.04  | 2.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 71.01           | 17.49         | 0.52     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 3  | 31   | 01 April 2024    |                  | 53.41           | 17.6          | 0.41     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 4  | 30   | 01 May 2024      |                  | 35.71           | 17.7          | 0.31     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.91           | 17.8          | 0.21     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.91         | 0.1      | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.87     | 2.04 | 0.0       | 104.0  | 13.95 | 0.0        | 0.0  | 90.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
      | 15 February 2024 | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 88.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date  | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |            | 70.0            |               |          | 2.04 |           | 2.04  | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |            | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      |    |      | 01 February 2024 |            | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |            | 71.01           | 17.49         | 0.52     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 3  | 31   | 01 April 2024    |            | 53.41           | 17.6          | 0.41     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 4  | 30   | 01 May 2024      |            | 35.71           | 17.7          | 0.31     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 5  | 31   | 01 June 2024     |            | 17.91           | 17.8          | 0.21     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 6  | 30   | 01 July 2024     |            | 0.0             | 17.91         | 0.1      | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.96     | 2.04 | 0.0       | 104.0  | 0.0   | 0.0        | 0.0  | 104.0       |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0         |

  @TestRailId:C3557
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that expects one tranche - UC8.2.4
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.43 |           | 1.43  | 1.43 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44      | 1.43 | 0.0       | 72.87 | 1.43 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.43 | 1.43 | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.43   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.43  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.43 |           | 1.43  | 1.43  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.43 | 0.0       | 72.87 | 13.34 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |

  @Skip
  @TestRailId:C3558
  Scenario: Verify interest disbursement charge for tranche interest bearing progressive loan that expects one tranche with undo disbursement - UC8.2.5
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.0  | 0.0    | 0.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 0.03 |           | 0.03  | 0.03 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 0.03 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.03 | 0.03 | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.03   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.03  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 0.03 |           | 0.03  | 0.03  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 11.94 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.04 |           | 0.04  |      |            |      | 0.02         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.04 | 0.0       | 102.09 | 0.0   | 0.0        | 0.0  | 102.09      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.04 | 0.0    | 0.0         |

  @Skip
  @TestRailId:C3560
  Scenario: Verify amount disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with undo disbursements - UC8.2.6
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.4  |           | 1.4   | 1.4  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.87  | 1.4  | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.4  | 1.4  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.4    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.4   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.44     | 1.4  | 0.0       | 72.84  | 13.31 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
#    --- 2nd disbursement - 1 February, 2024  ---
    When Admin successfully disburse the loan on "01 February 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 71.01           | 17.49         | 0.52     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 3  | 31   | 01 April 2024    |                  | 53.41           | 17.6          | 0.41     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 4  | 30   | 01 May 2024      |                  | 35.71           | 17.7          | 0.31     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.91           | 17.8          | 0.21     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.91         | 0.1      | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.96     | 1.4  | 0.0       | 103.36 | 13.31 | 0.0        | 0.0  | 90.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
      | 15 February 2024 | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 88.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.0  |           | 2.0   |      |            |      | 2.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 0.0   | 0.0        | 0.0  | 104.05      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 2.0  | 0.0  | 0.0    | 2.0         |

  @Skip
  @TestRailId:C3561
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with undo disbursement - UC8.2.7
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.43 |           | 1.43  | 1.43 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44      | 1.43 | 0.0       | 72.87 | 1.43 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.43 | 1.43 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.43   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.43  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.43 |           | 1.43  | 1.43  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.43 | 0.0       | 72.87 | 13.34 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.02 |           | 0.02  |      |            |      | 0.02         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 0.0   | 0.0        | 0.0  | 102.07      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |

  @Skip
  @TestRailId:C3562
  Scenario: Verify interest disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with undo last disbursement - UC8.2.8
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.0  | 0.0    | 0.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 0.03 |           | 0.03  | 0.03 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 0.03 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.03 | 0.03 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.03   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.03  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 0.03 |           | 0.03  | 0.03  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 11.94 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
#    --- 2nd disbursement - 1 February, 2024  ---
    When Admin successfully disburse the loan on "01 February 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 0.03 |           | 0.03  | 0.03  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 71.01           | 17.49         | 0.52     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 3  | 31   | 01 April 2024    |                  | 53.41           | 17.6          | 0.41     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 4  | 30   | 01 May 2024      |                  | 35.71           | 17.7          | 0.31     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.91           | 17.8          | 0.21     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.91         | 0.1      | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.96     | 0.03 | 0.0       | 101.99 | 11.94 | 0.0        | 0.0  | 90.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
      | 15 February 2024 | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 88.5         | false    | false    |
# -- undo last disbursement ----
    When Admin successfully undo last disbursal
    Then Loan status has changed to "Active"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 0.03 |           | 0.03  | 0.03 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 0.03 | 0.0        | 0.0  | 71.44       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.03 | 0.03 | 0.0    | 0.0         |

  @TestRailId:C3563
  Scenario: Verify amount disbursement charge for interest bearing progressive with partial disbursal and with undo disbursement - UC2.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.4  |           | 1.4   | 1.4  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.84  | 1.4  | 0.0        | 0.0  | 71.44       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.4  | 1.4  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.4    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.4   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.84  | 13.31 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.0  |           | 2.0   |      |            |      | 2.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 0.0   | 0.0        | 0.0  | 104.05      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |

  @TestRailId:C3564
  Scenario: Verify amount+interest disbursement charge for interest bearing progressive loan with partial disbursal and with undo disbursement - UC3.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.43 |           | 1.43  | 1.43 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44      | 1.43 | 0.0       | 72.87 | 1.43 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.43 | 1.43 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.43   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.43  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.43 |           | 1.43  | 1.43  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.43 | 0.0       | 72.87 | 13.34 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.04 |           | 2.04  |      |            |      | 2.04         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 0.0   | 0.0        | 0.0  | 104.09      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |

  @TestRailId:C3565
  Scenario: Verify interest disbursement charge for interest bearing progressive loan with partial disbursal - UC4.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.0  | 0.0    | 0.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 0.03 |           | 0.03  | 0.03 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 0.03 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.03 | 0.03 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.03   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.03  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 0.03 |           | 0.03  | 0.03  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 11.94 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |

  @TestRailId:C3566
  Scenario: Verify amount disbursement charge with reversed repayment for backdated interest bearing progressive loan - UC9
    When Admin sets the business date to "01 March 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.0  | 0.0  | 0.0    | 1.0        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.0  |           | 1.0   | 1.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.14           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.52           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0  | 0.0        | 0.0  | 17.09       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.14     | 1.0  | 0.0       | 103.14 | 1.0  | 0.0        | 0.0  | 102.14      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.0  | 1.0  | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.0   |        |
#    --- 1st repayment - 1 February, 2024  ---
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 18.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "02 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0   | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0   | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0   | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 35.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
#    --- First repayment reversed ---
    When Admin sets the business date to "03 March 2024"
    When Customer undo "1"th "Repayment" transaction made on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 01 March 2024    | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 17.01 | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.14           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.53           | 16.61         | 0.4      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.81           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.1      | 0.0  | 0.0       | 17.1  | 0.0   | 0.0        | 0.0   | 17.1        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 100.0         | 2.15     | 1.0  | 0.0       | 103.15 | 18.01 | 0.0        | 17.01 | 85.14       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | true     | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | true     |

  @TestRailId:C3613
  Scenario: Verify immediate charge accrual post maturity for Progressive loans
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is enabled
    When Admin sets the business date to "25 February 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL | 25 February 2025  | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "25 February 2025" with "1000" amount and expected disbursement date on "25 February 2025"
    When Admin successfully disburse the loan on "25 February 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 25 February 2025 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 28   | 25 March 2025    |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 25 February 2025 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "28 March 2025" due date and 25 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 28 March 2025 | Flat             | 25.0 | 0.0  | 0.0    | 25.0        |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 25 February 2025 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 28   | 25 March 2025    |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 3    | 28 March 2025    |           | 0.0             | 0.0           | 0.0      | 25.0 | 0.0       | 25.0   | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 25.0 | 0.0       | 1025.0 | 0.0  | 0.0        | 0.0  | 1025.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 25 February 2025 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 28 March 2025    | Accrual          | 25.0   | 0.0       | 0.0      | 25.0 | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "28 March 2025"
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is disabled

  @TestRailId:C3650
  Scenario: Tranche disbursement charges - disbursement flat charge
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                        | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 130            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT | 10.0          | 01 January 2024                | 100.0                      | 03 March 2024                  | 30.0                       |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    And Admin successfully approves the loan on "01 January 2024" with "130" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 10.0 |           | 10.0  | 10.0 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 10.0 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |
    # Add repayment on 01 February 2024
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 27.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    # Add repayment on 01 March 2024
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 44.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # Add additional disbursement on 03 March 2024
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 30 EUR transaction amount
    And Admin successfully disburse the loan on "03 March 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 30.0            |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 72.99           | 24.06         | 0.55     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 4  | 30   | 01 May 2024      |                  | 48.81           | 24.18         | 0.43     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 5  | 31   | 01 June 2024     |                  | 24.48           | 24.33         | 0.28     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 24.48         | 0.14     | 0.0  | 0.0       | 24.62 | 0.0   | 0.0        | 0.0  | 24.62       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 130.0         | 2.47     | 20.0 | 0.0       | 152.47 | 54.02 | 0.0        | 0.0  | 98.45       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 97.05        | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 97.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 03 March 2024   | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |

  @TestRailId:C3652
  Scenario: Tranche disbursement charges - disbursement percentage charge
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT" with "PERCENTAGE_DISBURSEMENT_AMOUNT" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                         | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 130            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT | 1.0           | 01 January 2024                | 100.0                      | 03 March 2024                  | 30.0                       |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type                | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount           | 1.0 | 0.0  | 0.0    | 1.0         |
    And Admin successfully approves the loan on "01 January 2024" with "130" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.0  |           | 1.0   | 1.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 1.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type       | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount  | 1.0 | 1.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.0   |        |
    # First repayment
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 18.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    # Second repayment
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 35.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # Second disbursement
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 30 EUR transaction amount
    When Admin successfully disburse the loan on "03 March 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 30.0            |               |          | 0.3  |           | 0.3   | 0.3   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 72.99           | 24.06         | 0.55     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 4  | 30   | 01 May 2024      |                  | 48.81           | 24.18         | 0.43     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 5  | 31   | 01 June 2024     |                  | 24.48           | 24.33         | 0.28     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 24.48         | 0.14     | 0.0  | 0.0       | 24.62 | 0.0   | 0.0        | 0.0  | 24.62       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 130.0         | 2.47     | 1.3  | 0.0       | 133.77 | 35.32 | 0.0        | 0.0  | 98.45       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 97.05        | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 0.3    | 0.0       | 0.0      | 0.3  | 0.0       | 97.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type       | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 03 March 2024   | % Disbursement Amount  | 0.3 | 0.3  | 0.0    | 0.0         |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount  | 1.0 | 1.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.3    |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.3   |        |

  @TestRailId:C3653
  Scenario: Tranche disbursement charges - flat and cash based accounting
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT" with "FLAT" calculation type and 0.02 EUR amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                        | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT | 0.02          | 01 January 2024                | 100.0                      | 03 March 2024                  | 100.0                      |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount  | false     | Tranche Disbursement | 01 January 2024 | Flat             | 0.02 | 0.0  | 0.0    | 0.02        |
    And Admin successfully approves the loan on "01 January 2024" with "130" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.02 |           | 0.02  | 0.02 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 0.02 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount  | false     | Tranche Disbursement | 01 January 2024 | Flat             | 0.02 | 0.02 | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.02  |        |
    # First repayment
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 17.03 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    # Second repayment
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 34.04 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # Second disbursement
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 30 EUR transaction amount
    When Admin successfully disburse the loan on "03 March 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 30.0            |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 72.99           | 24.06         | 0.55     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 4  | 30   | 01 May 2024      |                  | 48.81           | 24.18         | 0.43     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 5  | 31   | 01 June 2024     |                  | 24.48           | 24.33         | 0.28     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 24.48         | 0.14     | 0.0  | 0.0       | 24.62 | 0.0   | 0.0        | 0.0  | 24.62       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 130.0         | 2.47     | 0.04 | 0.0       | 132.51 | 34.06 | 0.0        | 0.0  | 98.45       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 97.05        | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 97.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 03 March 2024   | Flat             | 0.02 | 0.02 | 0.0    | 0.0         |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 0.02 | 0.02 | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.02  |        |

  @TestRailId:C3654
  Scenario: Tranche disbursement charges - percentage disbursement and accrual based accounting
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT" with "PERCENTAGE_DISBURSEMENT_AMOUNT" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                         | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT | 1.0           | 01 January 2024                | 100.0                      | 03 March 2024                  | 100.0                      |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type                | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount           | 1.0 | 0.0  | 0.0    | 1.0         |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.0  |           | 1.0   | 1.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 1.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type       | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount  | 1.0 | 1.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.0   |        |
    # First repayment
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 18.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    # Second repayment
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 35.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # Second disbursement
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 100 EUR transaction amount
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 125.62          | 41.43         | 0.94     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 4  | 30   | 01 May 2024      |                  | 83.98           | 41.64         | 0.73     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 5  | 31   | 01 June 2024     |                  | 42.1            | 41.88         | 0.49     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 42.1          | 0.25     | 0.0  | 0.0       | 42.35 | 0.0   | 0.0        | 0.0  | 42.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 2.0  | 0.0       | 205.48  | 36.02 | 0.0        | 0.0  | 169.46      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type       | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 03 March 2024   | % Disbursement Amount  | 1.0 | 1.0  | 0.0    | 0.0         |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount  | 1.0 | 1.0  | 0.0    | 0.0         |

    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.0   |        |
    # Run income recognition for accrual test
    And Admin runs the Accrual Activity Posting job
    And Admin runs the Add Accrual Transactions job
    And Admin runs the Add Accrual Transactions For Loans With Income Posted As Transactions job
    And Admin runs the Add Periodic Accrual Transactions job
    And Admin runs the Recalculate Interest for Loans job
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Accrual                             | 1.1    | 0.0       | 1.1      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "03 March 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 1.1   |        |
      | INCOME | 404000       | Interest Income         |       | 1.1    |

  @TestRailId:C3655
  Scenario: Disbursement charge - flat and accrual based accounting - undo disbursement
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT" with "FLAT" calculation type and 5.0 EUR amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                        | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT | 5.0           | 01 January 2024                | 100.0                      | 03 March 2024                  | 100.0                      |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 0.0  | 0.0    | 5.0         |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 5.0  |           | 5.0   | 5.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 5.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 5.0   |        |
    # First repayment
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 22.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    # Second repayment
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 39.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # Second disbursement
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 100 EUR transaction amount
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 125.62          | 41.43         | 0.94     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 4  | 30   | 01 May 2024      |                  | 83.98           | 41.64         | 0.73     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 5  | 31   | 01 June 2024     |                  | 42.1            | 41.88         | 0.49     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 42.1          | 0.25     | 0.0  | 0.0       | 42.35 | 0.0   | 0.0        | 0.0  | 42.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 10.0 | 0.0       | 213.48 | 44.02 | 0.0        | 0.0  | 169.46      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 03 March 2024   | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 5.0   |        |
    # Run income recognition for accrual test
    When Admin sets the business date to "03 March 2024"
    And Admin runs the Accrual Activity Posting job
    And Admin runs the Add Accrual Transactions job
    And Admin runs the Add Accrual Transactions For Loans With Income Posted As Transactions job
    And Admin runs the Add Periodic Accrual Transactions job
    And Admin runs the Recalculate Interest for Loans job
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Accrual                             | 1.1    | 0.0       | 1.1      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "03 March 2024" which has the following Journal entries:
      | Type   | Account code | Account name              | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable   | 1.1   |        |
      | INCOME | 404000       | Interest Income           |       | 1.1    |
    # Undo disbursement
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 5.0  |           | 5.0   |      |            |      | 5.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      |    |      | 03 March 2024    |           | 100.0           |               |          | 5.0  |           | 5.0   |      |            |      | 5.0         |
      | 3  | 31   | 01 April 2024    |           | 125.62          | 41.43         | 0.94     | 0.0  | 0.0       | 42.37 | 0.0  | 0.0        | 0.0  | 42.37       |
      | 4  | 30   | 01 May 2024      |           | 83.98           | 41.64         | 0.73     | 0.0  | 0.0       | 42.37 | 0.0  | 0.0        | 0.0  | 42.37       |
      | 5  | 31   | 01 June 2024     |           | 42.1            | 41.88         | 0.49     | 0.0  | 0.0       | 42.37 | 0.0  | 0.0        | 0.0  | 42.37       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 42.1          | 0.25     | 0.0  | 0.0       | 42.35 | 0.0  | 0.0        | 0.0  | 42.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 10.0 | 0.0       | 213.48 | 0.0  | 0.0        | 0.0  | 213.48      |
    Then Loan Transactions tab has none transaction

  @TestRailId:C3656
  Scenario: Disbursement charge - flat and accrual based accounting - undo last disbursement
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT" with "FLAT" calculation type and 5.0 EUR amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                        | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT | 5.0           | 01 January 2024                | 100.0                      | 03 March 2024                  | 100.0                      |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount  | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 0.0  | 0.0    | 5.0         |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 5.0  |           | 5.0   | 5.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 5.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount  | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 5.0   |        |
    # First repayment
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 22.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    # Second repayment
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 39.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # Second disbursement
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 100 EUR transaction amount
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 125.62          | 41.43         | 0.94     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 4  | 30   | 01 May 2024      |                  | 83.98           | 41.64         | 0.73     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 5  | 31   | 01 June 2024     |                  | 42.1            | 41.88         | 0.49     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 42.1          | 0.25     | 0.0  | 0.0       | 42.35 | 0.0   | 0.0        | 0.0  | 42.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 10.0 | 0.0       | 213.48  | 44.02 | 0.0        | 0.0  | 169.46      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 03 March 2024   | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 5.0   |        |
    # Run income recognition for accrual test
    When Admin sets the business date to "03 March 2024"
    And Admin runs the Accrual Activity Posting job
    And Admin runs the Add Accrual Transactions job
    And Admin runs the Add Accrual Transactions For Loans With Income Posted As Transactions job
    And Admin runs the Add Periodic Accrual Transactions job
    And Admin runs the Recalculate Interest for Loans job
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Accrual                             | 1.1    | 0.0       | 1.1      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "03 March 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 1.1   |        |
      | INCOME | 404000       | Interest Income         |       | 1.1    |
    # Undo last disbursement
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 44.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Accrual                             | 1.1    | 0.0       | 1.1      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 5.0   |        |

  @TestRailId:C3784
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: flat charge type, interestRecalculation = true
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_DAILY_INSTALLMENT_FEE_FLAT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0   | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 27.01 | 0.0        | 0.0  | 135.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.43     | 0.58     | 10.0 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 10.0 | 0.0    | 50.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.58  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.43     | 0.58     | 10.0 | 0.0       | 83.57        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.58  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 10.58 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 27.01  |

  @TestRailId:C3811
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: flat charge type, interestRecalculation = true, early repayment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_DAILY_INSTALLMENT_FEE_FLAT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 54.02 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 83.25           | 16.75         | 0.26     | 10.0 | 0.0       | 27.01 | 27.01 | 27.01      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 January 2024 | 66.24           | 17.01         | 0.0      | 10.0 | 0.0       | 27.01 | 27.01 | 27.01      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                 | 50.23           | 16.01         | 1.0      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |                 | 33.51           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                 | 16.7            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 16.7          | 0.1      | 10.0 | 0.0       | 26.8  | 0.0   | 0.0        | 0.0  | 26.8        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.85     | 60.0 | 0.0       | 161.85 | 54.02 | 54.02      | 0.0  | 107.83      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 54.02  | 33.76     | 0.26     | 20.0 | 0.0       | 66.24        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 20.0 | 0.0    | 40.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 33.76  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 20.26  |
      | LIABILITY | 145023       | Suspense/Clearing account | 54.02 |        |
    When Customer makes a repayment undo on "15 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 54.02  | 33.76     | 0.26     | 20.0 | 0.0       | 66.24        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 33.76  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 20.26  |
      | LIABILITY | 145023       | Suspense/Clearing account | 54.02 |        |
      | ASSET     | 112601       | Loans Receivable          | 33.76 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 20.26 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 54.02  |

  @TestRailId:C3785
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: percentage amount charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_AMOUNT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.16 | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.17 | 0.0       | 17.21 | 0.0  | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 1.01 | 0.0       | 103.05 | 0.0  | 0.0        | 0.0  | 103.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                              | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount fee | false     | Installment Fee |           | % Amount         | 1.01  | 0.0  | 0.0    | 1.01        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.16 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 0.16 | 0.0       | 17.16 | 17.16 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 0.17 | 0.0       | 17.21 | 0.0   | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 1.01 | 0.0       | 103.05 | 17.16 | 0.0        | 0.0  | 85.89       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.16  | 16.41     | 0.59     | 0.16 | 0.0       | 83.59        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                              | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount fee | false     | Installment Fee |           | % Amount         | 1.01  | 0.16 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.75   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.16 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.16 | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.17 | 0.0       | 17.21 | 0.0  | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 1.01 | 0.0       | 103.05 | 0.0  | 0.0        | 0.0  | 103.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.16  | 16.41     | 0.59     | 0.16 | 0.0       | 83.59        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                              | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount fee | false     | Installment Fee |           | % Amount         | 1.01  | 0.0  | 0.0    | 1.01        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.75   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.16 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.41 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 0.75  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.16  |

  @TestRailId:C3786
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: percentage interest charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_INTEREST_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 0.0  | 0.0        | 0.0  | 102.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.03 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 17.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 0.0   | 0.0        | 0.0  | 17.02       |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0   | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0   | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 17.03 | 0.0        | 0.0  | 85.1        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.03  | 16.41     | 0.59     | 0.03 | 0.0       | 83.59        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.03 | 0.0    | 0.06        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.62   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.03 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 0.0  | 0.0        | 0.0  | 102.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.03  | 16.41     | 0.59     | 0.03 | 0.0       | 83.59        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.62   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.03 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.41 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 0.62  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.03  |

  @TestRailId:C3812
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: percentage interest charge type, interestRecalculation = false, early repayment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_INTEREST_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 0.0  | 0.0        | 0.0  | 102.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 34.05 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 17.03 | 17.03      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 January 2024 | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 17.02 | 17.02      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                 | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0   | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |                 | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                 | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0   | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 34.05 | 34.05      | 0.0  | 68.08       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 34.05  | 32.95     | 1.05     | 0.05 | 0.0       | 67.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.05 | 0.0    | 0.04        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 32.95  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 1.1    |
      | LIABILITY | 145023       | Suspense/Clearing account | 34.05 |        |
    When Customer makes a repayment undo on "15 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 0.0  | 0.0        | 0.0  | 102.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 34.05  | 32.95     | 1.05     | 0.05 | 0.0       | 67.05        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 32.95  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 1.1    |
      | LIABILITY | 145023       | Suspense/Clearing account | 34.05 |        |
      | ASSET     | 112601       | Loans Receivable          | 32.95 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 1.1   |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 34.05  |

  @TestRailId:C3787
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: percentage amount + interest charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_AMOUNT_INTEREST_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.17 | 0.0       | 17.21 | 0.0  | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 1.02 | 0.0       | 103.06 | 0.0  | 0.0        | 0.0  | 103.06      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 0.17 | 0.0       | 17.17 | 17.17 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 0.17 | 0.0       | 17.21 | 0.0   | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 1.02 | 0.0       | 103.06 | 17.17 | 0.0        | 0.0  | 85.89       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.17  | 16.41     | 0.59     | 0.17 | 0.0       | 83.59        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.17 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.76   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.17 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.17 | 0.0       | 17.21 | 0.0  | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 1.02 | 0.0       | 103.06 | 0.0  | 0.0        | 0.0  | 103.06      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.17  | 16.41     | 0.59     | 0.17 | 0.0       | 83.59        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.76   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.17 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.41 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 0.76  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.17  |

  @TestRailId:C3788
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: all charge types, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_ALL_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 10.34 | 0.0       | 27.38 | 0.0  | 0.0        | 0.0  | 27.38       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 62.12 | 0.0       | 164.16 | 0.0  | 0.0        | 0.0  | 164.16      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.0  | 0.0    | 1.01        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.36 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 27.36 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 10.36 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 10.35 | 0.0       | 27.35 | 0.0   | 0.0        | 0.0  | 27.35       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 10.35 | 0.0       | 27.35 | 0.0   | 0.0        | 0.0  | 27.35       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 10.34 | 0.0       | 27.38 | 0.0   | 0.0        | 0.0  | 27.38       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 62.12 | 0.0       | 164.16 | 27.36 | 0.0        | 0.0  | 136.8       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.36  | 16.41     | 0.59     | 10.36 | 0.0       | 83.59        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 10.0 | 0.0    | 50.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.16 | 0.0    | 0.85        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.03 | 0.0    | 0.06        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.17 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.95  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.36 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 10.34 | 0.0       | 27.38 | 0.0  | 0.0        | 0.0  | 27.38       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 62.12 | 0.0       | 164.16 | 0.0  | 0.0        | 0.0  | 164.16      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.36  | 16.41     | 0.59     | 10.36 | 0.0       | 83.59        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.0  | 0.0    | 1.01        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.95  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.36 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.41 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 10.95 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 27.36  |

  @TestRailId:C3789
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: flat + % interest charge types, tranche loan, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_FLAT_INTEREST_CHARGES_TRANCHE | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 0.0  | 0.0        | 0.0  | 27.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 10.02 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 10.01 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 10.01 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 10.0  | 0.0       | 27.04 | 0.0  | 0.0        | 0.0  | 27.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 60.09 | 0.0       | 162.13 | 0.0  | 0.0        | 0.0  | 162.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment flat fee                | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.03 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 27.02 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 27.02 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 10.02 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 10.01 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 10.01 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 10.0  | 0.0       | 27.04 | 0.0   | 0.0        | 0.0  | 27.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 60.09 | 0.0       | 162.13 | 54.05 | 0.0        | 0.0  | 108.08      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.03  | 16.41     | 0.59     | 10.03 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Repayment        | 27.02  | 16.54     | 0.46     | 10.02 | 0.0       | 67.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.05 | 0.0    | 0.04        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.62  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.03 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.54  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.48  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.02 |        |
    When Admin sets the business date to "03 March 2024"
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 27.02 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 126.0           | 41.05         | 0.95     | 10.05 | 0.0       | 52.05 | 0.0   | 0.0        | 0.0  | 52.05       |
      | 4  | 30   | 01 May 2024      |                  | 84.72           | 41.28         | 0.72     | 10.04 | 0.0       | 52.04 | 0.0   | 0.0        | 0.0  | 52.04       |
      | 5  | 31   | 01 June 2024     |                  | 43.22           | 41.5          | 0.5      | 10.02 | 0.0       | 52.02 | 0.0   | 0.0        | 0.0  | 52.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 43.22         | 0.25     | 10.01 | 0.0       | 53.48 | 0.0   | 0.0        | 0.0  | 53.48       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.47     | 60.17 | 0.0       | 263.64 | 54.05 | 0.0        | 0.0  | 209.59      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.03  | 16.41     | 0.59     | 10.03 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Repayment        | 27.02  | 16.54     | 0.46     | 10.02 | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.17  | 0.05 | 0.0    | 0.12        |
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 27.02 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 10.02 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 10.01 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 10.01 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 10.0  | 0.0       | 27.04 | 0.0   | 0.0        | 0.0  | 27.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 60.09 | 0.0       | 162.13 | 54.05 | 0.0        | 0.0  | 108.08      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.03  | 16.41     | 0.59     | 10.03 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Repayment        | 27.02  | 16.54     | 0.46     | 10.02 | 0.0       | 67.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.05 | 0.0    | 0.04        |
    Then Admin can successfully undone the loan disbursal

  @TestRailId:C3813
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: flat + % interest charge types, tranche loan, interestRecalculation = false, early repayment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_FLAT_INTEREST_CHARGES_TRANCHE | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 0.0  | 0.0        | 0.0  | 27.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 10.02 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 10.01 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 10.01 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 10.0  | 0.0       | 27.04 | 0.0  | 0.0        | 0.0  | 27.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 60.09 | 0.0       | 162.13 | 0.0  | 0.0        | 0.0  | 162.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment flat fee                | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.03 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 27.02 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 27.02 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 10.02 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 10.01 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 10.01 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 10.0  | 0.0       | 27.04 | 0.0   | 0.0        | 0.0  | 27.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 60.09 | 0.0       | 162.13 | 54.05 | 0.0        | 0.0  | 108.08      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.03  | 16.41     | 0.59     | 10.03 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Repayment        | 27.02  | 16.54     | 0.46     | 10.02 | 0.0       | 67.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.05 | 0.0    | 0.04        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.62  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.03 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.54  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.48  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.02 |        |
    When Admin sets the business date to "03 March 2024"
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 27.02 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 126.0           | 41.05         | 0.95     | 10.05 | 0.0       | 52.05 | 0.0   | 0.0        | 0.0  | 52.05       |
      | 4  | 30   | 01 May 2024      |                  | 84.72           | 41.28         | 0.72     | 10.04 | 0.0       | 52.04 | 0.0   | 0.0        | 0.0  | 52.04       |
      | 5  | 31   | 01 June 2024     |                  | 43.22           | 41.5          | 0.5      | 10.02 | 0.0       | 52.02 | 0.0   | 0.0        | 0.0  | 52.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 43.22         | 0.25     | 10.01 | 0.0       | 53.48 | 0.0   | 0.0        | 0.0  | 53.48       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.47     | 60.17 | 0.0       | 263.64 | 54.05 | 0.0        | 0.0  | 209.59      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.03  | 16.41     | 0.59     | 10.03 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Repayment        | 27.02  | 16.54     | 0.46     | 10.02 | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.17  | 0.05 | 0.0    | 0.12        |
    And Customer makes "AUTOPAY" repayment on "03 March 2024" with 104.09 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.03 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.54         | 0.46     | 10.02 | 0.0       | 27.02 | 27.02 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024    | 03 March 2024    | 126.0           | 41.05         | 0.95     | 10.05 | 0.0       | 52.05 | 52.05 | 52.05      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 03 March 2024    | 84.72           | 41.28         | 0.72     | 10.04 | 0.0       | 52.04 | 52.04 | 52.04      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     |                  | 43.22           | 41.5          | 0.5      | 10.02 | 0.0       | 52.02 | 0.0   | 0.0        | 0.0  | 52.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 43.22         | 0.25     | 10.01 | 0.0       | 53.48 | 0.0   | 0.0        | 0.0  | 53.48       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 200.0         | 3.47     | 60.17 | 0.0       | 263.64 | 158.14 | 104.09     | 0.0  | 105.5       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.03  | 16.41     | 0.59     | 10.03 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Repayment        | 27.02  | 16.54     | 0.46     | 10.02 | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment        | 104.09 | 82.33     | 1.67     | 20.09 | 0.0       | 84.72        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 40.0 | 0.0    | 20.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.17  | 0.14 | 0.0    | 0.03        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 82.33  |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 21.76  |
      | LIABILITY | 145023       | Suspense/Clearing account | 104.09 |        |

  @TestRailId:C3814
  Scenario: Progressive loan - Verify add installment fee charge: flat + % interest charge types, tranche loan, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_MULTIDISBURSE | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 0.0  | 0.0        | 0.0  | 27.04       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 0.0  | 0.0        | 0.0  | 27.03       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.02 | 0.0       | 27.03 | 0.0  | 0.0        | 0.0  | 27.03       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.01 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.01 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0  | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.09 | 0.0       | 162.14 | 0.0  | 0.0        | 0.0  | 162.14      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.0  | 0.0    | 0.09        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.04 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 27.03 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 27.04 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.02 | 0.0       | 27.03 | 0.0   | 0.0        | 0.0  | 27.03       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.0  | 0.0       | 27.0  | 0.0   | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.09 | 0.0       | 162.14 | 54.07 | 0.0        | 0.0  | 108.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.04  | 16.43     | 0.58     | 10.03 | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 27.03  | 16.52     | 0.49     | 10.02 | 0.0       | 67.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.05 | 0.0    | 0.04        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.61  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.04 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.52  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.51  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.03 |        |
    When Admin sets the business date to "03 March 2024"
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 27.04 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 125.62          | 41.43         | 0.94     | 10.05 | 0.0       | 52.42 | 0.0   | 0.0        | 0.0  | 52.42       |
      | 4  | 30   | 01 May 2024      |                  | 83.98           | 41.64         | 0.73     | 10.04 | 0.0       | 52.41 | 0.0   | 0.0        | 0.0  | 52.41       |
      | 5  | 31   | 01 June 2024     |                  | 42.1            | 41.88         | 0.49     | 10.02 | 0.0       | 52.39 | 0.0   | 0.0        | 0.0  | 52.39       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 42.1          | 0.25     | 10.01 | 0.0       | 52.36 | 0.0   | 0.0        | 0.0  | 52.36       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 60.17 | 0.0       | 263.65 | 54.07 | 0.0        | 0.0  | 209.58      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.04  | 16.43     | 0.58     | 10.03 | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 27.03  | 16.52     | 0.49     | 10.02 | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.17  | 0.05 | 0.0    | 0.12        |
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 27.04 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.02 | 0.0       | 27.03 | 0.0   | 0.0        | 0.0  | 27.03       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.0  | 0.0       | 27.0  | 0.0   | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.09 | 0.0       | 162.14 | 54.07 | 0.0        | 0.0  | 108.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.04  | 16.43     | 0.58     | 10.03 | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 27.03  | 16.52     | 0.49     | 10.02 | 0.0       | 67.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.05 | 0.0    | 0.04        |

  @TestRailId:C3820
  Scenario: Progressive loan - Verify add installment fee charge: flat charge type, tranche loan, interestRecalculation = true, early repayment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_INTEREST_RECALCULATION_MULTIDISB | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.58           | 16.42         | 0.59     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.03           | 16.55         | 0.46     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.42           | 16.61         | 0.4      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.7            | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.89           | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.89         | 0.1      | 10.0 | 0.0       | 26.99 | 0.0  | 0.0        | 0.0  | 26.99       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 60.0 | 0.0       | 162.04 | 0.0  | 0.0        | 0.0  | 162.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.01 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 27.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.59     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.03           | 16.55         | 0.46     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.42           | 16.61         | 0.4      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.7            | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.89           | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.89         | 0.1      | 10.0 | 0.0       | 26.99 | 0.0   | 0.0        | 0.0  | 26.99       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 60.0 | 0.0       | 162.04 | 54.02 | 0.0        | 0.0  | 108.02      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.42     | 0.59     | 10.0 | 0.0       | 83.58        | false    | false    |
      | 01 March 2024    | Repayment        | 27.01  | 16.55     | 0.46     | 10.0 | 0.0       | 67.03        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.42  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.59  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.55  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.46  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
    When Admin sets the business date to "03 March 2024"
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.59     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.03           | 16.55         | 0.46     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 125.62          | 41.41         | 0.95     | 10.0 | 0.0       | 52.36 | 0.0   | 0.0        | 0.0  | 52.36       |
      | 4  | 30   | 01 May 2024      |                  | 83.98           | 41.64         | 0.72     | 10.0 | 0.0       | 52.36 | 0.0   | 0.0        | 0.0  | 52.36       |
      | 5  | 31   | 01 June 2024     |                  | 42.12           | 41.86         | 0.5      | 10.0 | 0.0       | 52.36 | 0.0   | 0.0        | 0.0  | 52.36       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 42.12         | 0.24     | 10.0 | 0.0       | 52.36 | 0.0   | 0.0        | 0.0  | 52.36       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.46     | 60.0 | 0.0       | 263.46 | 54.02 | 0.0        | 0.0  | 209.44      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.42     | 0.59     | 10.0 | 0.0       | 83.58        | false    | false    |
      | 01 March 2024    | Repayment        | 27.01  | 16.55     | 0.46     | 10.0 | 0.0       | 67.03        | false    | false    |
      | 03 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.03       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 20.0 | 0.0    | 40.0        |
    And Customer makes "AUTOPAY" repayment on "03 March 2024" with 104.72 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.58           | 16.42         | 0.59     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.03           | 16.55         | 0.46     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024    | 03 March 2024    | 124.7           | 42.33         | 0.03     | 10.0 | 0.0       | 52.36 | 52.36 | 52.36      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 03 March 2024    | 82.34           | 42.36         | 0.0      | 10.0 | 0.0       | 52.36 | 52.36 | 52.36      | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2024     |                  | 41.39           | 40.95         | 1.41     | 10.0 | 0.0       | 52.36 | 0.0   | 0.0        | 0.0  | 52.36       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 41.39         | 0.24     | 10.0 | 0.0       | 51.63 | 0.0   | 0.0        | 0.0  | 51.63       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 200.0         | 2.73     | 60.0 | 0.0       | 262.73 | 158.74 | 104.72     | 0.0  | 103.99      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.42     | 0.59     | 10.0 | 0.0       | 83.58        | false    | false    |
      | 01 March 2024    | Repayment        | 27.01  | 16.55     | 0.46     | 10.0 | 0.0       | 67.03        | false    | false    |
      | 03 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.03       | false    | false    |
      | 03 March 2024    | Repayment        | 104.72 | 84.69     | 0.03     | 20.0 | 0.0       | 82.34        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 40.0 | 0.0    | 20.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 84.69  |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 20.03  |
      | LIABILITY | 145023       | Suspense/Clearing account | 104.72 |        |

  @TestRailId:C3790
  Scenario: Progressive loan - Verify add installment fee charge: flat charge type, interestRecalculation = true
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0   | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 27.01 | 0.0        | 0.0  | 135.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.43     | 0.58     | 10.0 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 10.0 | 0.0    | 50.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.58  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.43     | 0.58     | 10.0 | 0.0       | 83.57        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.58  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 10.58 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 27.01  |

  @TestRailId:C3815
  Scenario: Progressive loan - Verify add installment fee charge: flat charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0   | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 27.01 | 0.0        | 0.0  | 135.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.43     | 0.58     | 10.0 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 10.0 | 0.0    | 50.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.58  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.43     | 0.58     | 10.0 | 0.0       | 83.57        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.58  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 10.58 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 27.01  |

  @TestRailId:C3816
  Scenario: Progressive loan - Verify add installment fee charge: percentage amount charge type is NOT allowed when interestRecalculation = true
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin fails to add "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT" installment charge with 1 amount because of wrong charge calculation type

  @TestRailId:C3791
  Scenario: Progressive loan - Verify add installment fee charge: percentage amount charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT" installment charge with 1 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.16 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.01 | 0.0       | 103.06 | 0.0  | 0.0        | 0.0  | 103.06      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                              | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount fee | false     | Installment Fee |           | % Amount         | 1.01  | 0.0  | 0.0    | 1.01        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.17 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.16 | 0.0       | 17.17 | 17.17 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.01 | 0.0       | 103.06 | 17.17 | 0.0        | 0.0  | 85.89       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.17  | 16.43     | 0.58     | 0.16 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                              | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount fee | false     | Installment Fee |           | % Amount         | 1.01  | 0.16 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.74   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.17 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.16 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.01 | 0.0       | 103.06 | 0.0  | 0.0        | 0.0  | 103.06      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.17  | 16.43     | 0.58     | 0.16 | 0.0       | 83.57        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                              | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount fee | false     | Installment Fee |           | % Amount         | 1.01  | 0.0  | 0.0    | 1.01        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.74   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.17 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 0.74  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.17  |

  @TestRailId:C3792
  Scenario: Progressive loan - Verify add installment fee charge: percentage interest charge type is NOT allowed when interestRecalculation = true
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin fails to add "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount because of wrong charge calculation type

  @TestRailId:C3817
  Scenario: Progressive loan - Verify add installment fee charge: percentage interest charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.03 | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.02 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.02 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.01 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.01 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.09 | 0.0       | 102.14 | 0.0  | 0.0        | 0.0  | 102.14      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.04 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.03 | 0.0       | 17.04 | 17.04 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.02 | 0.0       | 17.03 | 0.0   | 0.0        | 0.0  | 17.03       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.02 | 0.0       | 17.03 | 0.0   | 0.0        | 0.0  | 17.03       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.01 | 0.0       | 17.02 | 0.0   | 0.0        | 0.0  | 17.02       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.01 | 0.0       | 17.02 | 0.0   | 0.0        | 0.0  | 17.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.09 | 0.0       | 102.14 | 17.04 | 0.0        | 0.0  | 85.1        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.04  | 16.43     | 0.58     | 0.03 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.03 | 0.0    | 0.06        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.61   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.04 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.03 | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.02 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.02 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.01 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.01 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.09 | 0.0       | 102.14 | 0.0  | 0.0        | 0.0  | 102.14      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.04  | 16.43     | 0.58     | 0.03 | 0.0       | 83.57        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.61   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.04 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 0.61  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.04  |

  @TestRailId:C3818
  Scenario: Progressive loan - Verify add installment fee charge: percentage amount + interest charge type is NOT allowed when interestRecalculation = true
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin fails to add "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST" installment charge with 1 amount because of wrong charge calculation type

  @TestRailId:C3793
  Scenario: Progressive loan - Verify add installment fee charge: percentage amount + interest charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST" installment charge with 1 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.02 | 0.0       | 103.07 | 0.0  | 0.0        | 0.0  | 103.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.18 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.17 | 0.0       | 17.18 | 17.18 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.17 | 0.0       | 17.18 | 0.0   | 0.0        | 0.0  | 17.18       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.02 | 0.0       | 103.07 | 17.18 | 0.0        | 0.0  | 85.89       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.18  | 16.43     | 0.58     | 0.17 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.17 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.75   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.18 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.17 | 0.0       | 17.18 | 0.0  | 0.0        | 0.0  | 17.18       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.02 | 0.0       | 103.07 | 0.0  | 0.0        | 0.0  | 103.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.18  | 16.43     | 0.58     | 0.17 | 0.0       | 83.57        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.75   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.18 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 0.75  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.18  |

  @TestRailId:C3794
  Scenario: Progressive loan - Verify add installment fee charge: all charge types, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT" installment charge with 1 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST" installment charge with 1 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.34 | 0.0       | 27.34 | 0.0  | 0.0        | 0.0  | 27.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 62.12 | 0.0       | 164.17 | 0.0  | 0.0        | 0.0  | 164.17      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.0  | 0.0    | 1.01        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.37 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.36 | 0.0       | 27.37 | 27.37 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 10.36 | 0.0       | 27.37 | 0.0   | 0.0        | 0.0  | 27.37       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.36 | 0.0       | 27.37 | 0.0   | 0.0        | 0.0  | 27.37       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.35 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.35 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.34 | 0.0       | 27.34 | 0.0   | 0.0        | 0.0  | 27.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 62.12 | 0.0       | 164.17 | 27.37 | 0.0        | 0.0  | 136.8       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.37  | 16.43     | 0.58     | 10.36 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 10.0 | 0.0    | 50.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.16 | 0.0    | 0.85        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.03 | 0.0    | 0.06        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.17 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.94  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.37 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.34 | 0.0       | 27.34 | 0.0  | 0.0        | 0.0  | 27.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 62.12 | 0.0       | 164.17 | 0.0  | 0.0        | 0.0  | 164.17      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.37  | 16.43     | 0.58     | 10.36 | 0.0       | 83.57        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.0  | 0.0    | 1.01        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.94  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.37 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 10.94 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 27.37  |

  @TestRailId:C3795
  Scenario: Progressive loan - Verify add installment fee charge, then make zero-interest charge-off: all charge types, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT" installment charge with 1 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST" installment charge with 1 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 10.34 | 0.0       | 27.38 | 0.0  | 0.0        | 0.0  | 27.38       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 62.12 | 0.0       | 164.16 | 0.0  | 0.0        | 0.0  | 164.16      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.0  | 0.0    | 1.01        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.36 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 27.36 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 10.36 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 10.35 | 0.0       | 27.35 | 0.0   | 0.0        | 0.0  | 27.35       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 10.35 | 0.0       | 27.35 | 0.0   | 0.0        | 0.0  | 27.35       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 10.34 | 0.0       | 27.38 | 0.0   | 0.0        | 0.0  | 27.38       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 62.12 | 0.0       | 164.16 | 27.36 | 0.0        | 0.0  | 136.8       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.36  | 16.41     | 0.59     | 10.36 | 0.0       | 83.59        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 10.0 | 0.0    | 50.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.16 | 0.0    | 0.85        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.03 | 0.0    | 0.06        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.17 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.95  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.36 |        |
    When Admin sets the business date to "1 March 2024"
    And Admin does charge-off the loan on "1 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 27.36 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |                  | 50.05           | 17.0          | 0.0      | 10.34 | 0.0       | 27.34 | 0.0   | 0.0        | 0.0  | 27.34       |
      | 4  | 30   | 01 May 2024      |                  | 33.05           | 17.0          | 0.0      | 10.34 | 0.0       | 27.34 | 0.0   | 0.0        | 0.0  | 27.34       |
      | 5  | 31   | 01 June 2024     |                  | 16.05           | 17.0          | 0.0      | 10.34 | 0.0       | 27.34 | 0.0   | 0.0        | 0.0  | 27.34       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.05         | 0.0      | 10.32 | 0.0       | 26.37 | 0.0   | 0.0        | 0.0  | 26.37       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 1.05     | 62.06 | 0         | 163.11 | 27.36 | 0          | 0    | 135.75      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.36  | 16.41     | 0.59     | 10.36 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Accrual          | 1.05   | 0.0       | 1.05     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 135.75 | 83.59     | 0.46     | 51.7  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "01 March 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 83.59  |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 52.16  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 83.59 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 0.46  |        |
      | INCOME  | 404008       | Fee Charge Off             | 51.7  |        |

  @TestRailId:C3796
  Scenario: Progressive loan - Verify add installment fee charge, then make accelerate maturity date charge-off: all charge types, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT" installment charge with 1 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST" installment charge with 1 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 10.36 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 10.35 | 0.0       | 27.35 | 0.0  | 0.0        | 0.0  | 27.35       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 10.34 | 0.0       | 27.38 | 0.0  | 0.0        | 0.0  | 27.38       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 62.12 | 0.0       | 164.16 | 0.0  | 0.0        | 0.0  | 164.16      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 0.0  | 0.0    | 60.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.0  | 0.0    | 1.01        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.0  | 0.0    | 0.09        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.0  | 0.0    | 1.02        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.36 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 27.36 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 10.36 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 10.36 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 10.35 | 0.0       | 27.35 | 0.0   | 0.0        | 0.0  | 27.35       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 10.35 | 0.0       | 27.35 | 0.0   | 0.0        | 0.0  | 27.35       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 10.34 | 0.0       | 27.38 | 0.0   | 0.0        | 0.0  | 27.38       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 62.12 | 0.0       | 164.16 | 27.36 | 0.0        | 0.0  | 136.8       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.36  | 16.41     | 0.59     | 10.36 | 0.0       | 83.59        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0  | 10.0 | 0.0    | 50.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01  | 0.16 | 0.0    | 0.85        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09  | 0.03 | 0.0    | 0.06        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02  | 0.17 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.95  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.36 |        |
    When Admin sets the business date to "1 March 2024"
    And Admin does charge-off the loan on "1 March 2024"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 10.36 | 0.0       | 27.36 | 27.36 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.59         | 0.46     | 11.7  | 0.0       | 95.75 | 0.0   | 0.0        | 0.0  | 95.75       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 1.05     | 22.06 | 0         | 123.11 | 27.36 | 0          | 0    | 95.75       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.36  | 16.41     | 0.59     | 10.36 | 0.0       | 83.59        | false    | false    |
      | 01 March 2024    | Accrual          | 1.05   | 0.0       | 1.05     | 0.0   | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 95.75  | 83.59     | 0.46     | 11.7  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "01 March 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 83.59  |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 12.16  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 83.59 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 0.46  |        |
      | INCOME  | 404008       | Fee Charge Off             | 11.7  |        |

  @TestRailId:C3797
  Scenario: Verify that partially waived installment fee applied correctly in reverse-replay logic, Progressive loan
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Progressive Loan with date: "01 January 2023"
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
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 100.0 | 0.0       | 1100.0 | 20.0 | 20.0       | 0.0  | 980.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 100.0 | 0         | 1100.0 | 20   | 20         | 0    | 980.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 January 2023  | Repayment          | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 995.0        |
      | 18 January 2023  | Repayment          | 15.0   | 15.0      | 0.0      | 0.0  | 0.0       | 980.0        |
      | 20 January 2023  | Waive loan charges | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 980.0        |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 100.0 | 0.0  | 100.0  | 0.0         |

  @TestRailId:C3823
  Scenario: Progressive loan - Verify non-tranche loan with all installment fee charge types and repayments
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT" installment charge with 1 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST" installment charge with 1 amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.34 | 0.0       | 27.34 | 0.0  | 0.0        | 0.0  | 27.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 62.12 | 0.0       | 164.17 | 0.0  | 0.0        | 0.0  | 164.17      |
    Then Loan Transactions tab has none transaction
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.36 | 0.0       | 27.37 | 0.0  | 0.0        | 0.0  | 27.37       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.35 | 0.0       | 27.36 | 0.0  | 0.0        | 0.0  | 27.36       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.34 | 0.0       | 27.34 | 0.0  | 0.0        | 0.0  | 27.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 62.12 | 0.0       | 164.17 | 0.0  | 0.0        | 0.0  | 164.17      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0 | 0.0  | 0.0    | 60.0        |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02 | 0.0  | 0.0    | 1.02        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09 | 0.0  | 0.0    | 0.09        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01 | 0.0  | 0.0    | 1.01        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.37 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.36 | 0.0       | 27.37 | 27.37 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 10.36 | 0.0       | 27.37 | 0.0   | 0.0        | 0.0  | 27.37       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.36 | 0.0       | 27.37 | 0.0   | 0.0        | 0.0  | 27.37       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.35 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.35 | 0.0       | 27.36 | 0.0   | 0.0        | 0.0  | 27.36       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.34 | 0.0       | 27.34 | 0.0   | 0.0        | 0.0  | 27.34       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 62.12 | 0.0       | 164.17 | 27.37 | 0.0        | 0.0  | 136.8       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.37  | 16.43     | 0.58     | 10.36 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                         | isPenalty | Payment due at  | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Installment percentage amount + interest fee | false     | Installment Fee |           | % Loan Amount + Interest | 1.02 | 0.17 | 0.0    | 0.85        |
      | Installment percentage interest fee          | false     | Installment Fee |           | % Interest               | 0.09 | 0.03 | 0.0    | 0.06        |
      | Installment flat fee                         | false     | Installment Fee |           | Flat                     | 60.0 | 10.0 | 0.0    | 50.0        |
      | Installment percentage amount fee            | false     | Installment Fee |           | % Amount                 | 1.01 | 0.16 | 0.0    | 0.85        |

  @TestRailId:C3824
  Scenario: Progressive loan - Verify tranche loan with installment fee charges, repayments and multiple disbursements
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_MULTIDISBURSE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin adds "LOAN_INSTALLMENT_FEE_FLAT" installment charge with 10 amount
    When Admin adds "LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST" installment charge with 5 amount
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 0.0  | 0.0        | 0.0  | 27.04       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 0.0  | 0.0        | 0.0  | 27.03       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.02 | 0.0       | 27.03 | 0.0  | 0.0        | 0.0  | 27.03       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.01 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.01 | 0.0       | 27.02 | 0.0  | 0.0        | 0.0  | 27.02       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0  | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.09 | 0.0       | 162.14 | 0.0  | 0.0        | 0.0  | 162.14      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09 | 0.0  | 0.0    | 0.09        |
      | Installment flat fee                | false     | Installment Fee |           | Flat             | 60.0 | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.04 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 27.04 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 0.0   | 0.0        | 0.0  | 27.03       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.02 | 0.0       | 27.03 | 0.0   | 0.0        | 0.0  | 27.03       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.0  | 0.0       | 27.0  | 0.0   | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.09 | 0.0       | 162.14 | 27.04 | 0.0        | 0.0  | 135.1       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.04  | 16.43     | 0.58     | 10.03 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09 | 0.03 | 0.0    | 0.06        |
      | Installment flat fee                | false     | Installment Fee |           | Flat             | 60.0 | 10.0 | 0.0    | 50.0        |
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 27.03 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 27.04 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.02 | 0.0       | 27.03 | 0.0   | 0.0        | 0.0  | 27.03       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.01 | 0.0       | 27.02 | 0.0   | 0.0        | 0.0  | 27.02       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.0  | 0.0       | 27.0  | 0.0   | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.09 | 0.0       | 162.14 | 54.07 | 0.0        | 0.0  | 108.07      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.04  | 16.43     | 0.58     | 10.03 | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 27.03  | 16.52     | 0.49     | 10.02 | 0.0       | 67.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09 | 0.05 | 0.0    | 0.04        |
      | Installment flat fee                | false     | Installment Fee |           | Flat             | 60.0 | 20.0 | 0.0    | 40.0        |
    When Admin sets the business date to "03 March 2024"
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.03 | 0.0       | 27.04 | 27.04 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 10.02 | 0.0       | 27.03 | 27.03 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 0.0   |           | 0.0   | 0.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 125.62          | 41.43         | 0.94     | 10.05 | 0.0       | 52.42 | 0.0   | 0.0        | 0.0  | 52.42       |
      | 4  | 30   | 01 May 2024      |                  | 83.98           | 41.64         | 0.73     | 10.04 | 0.0       | 52.41 | 0.0   | 0.0        | 0.0  | 52.41       |
      | 5  | 31   | 01 June 2024     |                  | 42.1            | 41.88         | 0.49     | 10.02 | 0.0       | 52.39 | 0.0   | 0.0        | 0.0  | 52.39       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 42.1          | 0.25     | 10.01 | 0.0       | 52.36 | 0.0   | 0.0        | 0.0  | 52.36       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 60.17 | 0.0       | 263.65 | 54.07 | 0.0        | 0.0  | 209.58      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.04  | 16.43     | 0.58     | 10.03 | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment        | 27.03  | 16.52     | 0.49     | 10.02 | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0   | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Installment flat fee                | false     | Installment Fee |           | Flat             | 60.0 | 20.0 | 0.0    | 40.0        |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.17 | 0.05 | 0.0    | 0.12        |
