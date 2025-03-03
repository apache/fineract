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
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 4.0    |
      | INCOME | 404007       | Fee Income              | 4.0   |        |
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
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 4.0    |
      | INCOME | 404007       | Fee Income       | 4.0   |        |
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
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 4.0    |
      | INCOME | 404007       | Fee Income       | 4.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 5.0    |
      | INCOME | 404007       | Fee Income       | 5.0   |        |
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
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 4.0    |
      | INCOME | 404007       | Fee Income       | 4.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 5.0    |
      | INCOME | 404007       | Fee Income       | 5.0   |        |
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
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 4.0    |
      | INCOME | 404007       | Fee Income       | 4.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 5.0    |
      | INCOME | 404007       | Fee Income       | 5.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 1.0    |
      | INCOME | 404007       | Fee Income       | 1.0   |        |
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
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 4.0    |
      | INCOME | 404007       | Fee Income       | 4.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 5.0    |
      | INCOME | 404007       | Fee Income       | 5.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 1.0    |
      | INCOME | 404007       | Fee Income       | 1.0   |        |
      | ASSET  | 112601       | Loans Receivable | 1.0   |        |
      | INCOME | 404007       | Fee Income       |       | 1.0    |
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
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 9.0   |        |
      | INCOME | 404007       | Fee Income              |       | 9.0    |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 4.0    |
      | INCOME | 404007       | Fee Income       | 4.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 5.0    |
      | INCOME | 404007       | Fee Income       | 5.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 1.0    |
      | INCOME | 404007       | Fee Income       | 1.0   |        |
      | ASSET  | 112601       | Loans Receivable | 1.0   |        |
      | INCOME | 404007       | Fee Income       |       | 1.0    |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 1.0    |
      | INCOME | 404007       | Fee Income       | 1.0   |        |
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
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 4.0    |
      | INCOME | 404007       | Fee Income       | 4.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 5.0    |
      | INCOME | 404007       | Fee Income       | 5.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 1.0    |
      | INCOME | 404007       | Fee Income       | 1.0   |        |
      | ASSET  | 112601       | Loans Receivable | 1.0   |        |
      | INCOME | 404007       | Fee Income       |       | 1.0    |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 1.0    |
      | INCOME | 404007       | Fee Income       | 1.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type      | Account code | Account name        | Debit | Credit |
      | LIABILITY | l1           | Overpayment account |       | 2.0    |
      | INCOME    | 404007       | Fee Income          | 2.0   |        |
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
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 4.0    |
      | INCOME | 404007       | Fee Income       | 4.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 5.0    |
      | INCOME | 404007       | Fee Income       | 5.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 1.0    |
      | INCOME | 404007       | Fee Income       | 1.0   |        |
      | ASSET  | 112601       | Loans Receivable | 1.0   |        |
      | INCOME | 404007       | Fee Income       |       | 1.0    |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 1.0    |
      | INCOME | 404007       | Fee Income       | 1.0   |        |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "04 November 2022" which has the following Journal entries:
      | Type      | Account code | Account name        | Debit | Credit |
      | LIABILITY | l1           | Overpayment account |       | 2.0    |
      | INCOME    | 404007       | Fee Income          | 2.0   |        |
      | LIABILITY | l1           | Overpayment account | 2.0   |        |
      | INCOME    | 404007       | Fee Income          |       | 2.0    |

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
    When Admin adds "LOAN_INSTALLMENT_PERCENTAGE_FEE" charge with 1.5 % of transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 3000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 3000.0       |
    Then Loan Charges tab has the following data:
      | Name                       | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage fee | false     | Installment Fee |           | % Loan Amount + Interest | 46.35 | 0.0  | 0.0    | 46.35       |
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
    When Admin adds "LOAN_INSTALLMENT_PERCENTAGE_FEE" charge with 10 % of transaction amount
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
      | Name                       | isPenalty | Payment due at  | Due as of | Calculation type         | Due   | Paid | Waived | Outstanding |
      | Installment percentage fee | false     | Installment Fee |           | % Loan Amount + Interest | 100.0 | 5.0  | 95.0   | 0.0         |

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
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
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
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
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
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
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
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
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
      | LoanProduct          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
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
      | LoanProduct     | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
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
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is disabled
    Then LoanAccrualTransactionCreatedBusinessEvent is not raised on "05 February 2024"

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
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is disabled
    Then LoanAccrualTransactionCreatedBusinessEvent is not raised on "05 February 2024"

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
      | 05 February 2024 | Accrual          | 30.83  | 0.0       | 5.83     | 0.0  | 25.0      | 0.0          |
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
