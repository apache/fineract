@LoanChargeback
Feature: LoanChargeback

  @TestRailId:C2441
  Scenario: As an admin I would like to check chargeback function is working properly on a closed loan in case of payment type: REPAYMENT_ADJUSTMENT_CHARGEBACK
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2022"
    And Admin successfully approves the loan on "1 January 2022" with "1000" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "1 February 2022"
    And Customer makes "AUTOPAY" repayment on "1 February 2022" with 1000 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "25 February 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount
    Then Loan has 250 outstanding amount
    Then Loan status will be "ACTIVE"

  @TestRailId:C2442
  Scenario: As an admin I would like to check chargeback function is working properly on a closed loan in case of payment type: REPAYMENT_ADJUSTMENT_REFUND
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2022"
    And Admin successfully approves the loan on "1 January 2022" with "1000" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "1 February 2022"
    And Customer makes "AUTOPAY" repayment on "1 February 2022" with 1000 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "25 February 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 250 EUR transaction amount
    Then Loan has 250 outstanding amount
    Then Loan status will be "ACTIVE"

  @TestRailId:C2443
  Scenario: As an admin I would like to check chargeback function is working properly when chargeback comes after the loan was fully repaid and closed
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
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "25 April 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount
    Then Loan has 250 outstanding amount
    Then Loan status will be "ACTIVE"

  @TestRailId:C2444
  Scenario: As an admin I would like to check chargeback function is working properly when repayment happens again after chargeback
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
    When Admin sets the business date to "25 April 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount
    Then Loan has 250 outstanding amount
    Then Loan status will be "ACTIVE"
    And Customer makes "AUTOPAY" repayment on "25 April 2022" with 250 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C2445
  Scenario: As an admin I would like to check chargeback function is working properly when a second chargeback happens after the repayment
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
    When Admin sets the business date to "25 April 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount
    Then Loan has 250 outstanding amount
    Then Loan status will be "ACTIVE"
    And Customer makes "AUTOPAY" repayment on "25 April 2022" with 250 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "1 May 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 200 EUR transaction amount
    Then Loan has 200 outstanding amount
    Then Loan status will be "ACTIVE"

  @TestRailId:C2446
  Scenario: As an admin I would like to check chargeback function is working properly when chargeback happens after NSF fee was added to the loan then was repaid
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
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Customer makes a repayment undo on "1 April 2022"
    Then Loan has 250 outstanding amount
    Then Loan status will be "ACTIVE"
    And Admin adds an NSF fee because of payment bounce with "1 April 2022" transaction date
    Then Loan has 260 outstanding amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "5 April 2022"
    And Customer makes "AUTOPAY" repayment on "5 April 2022" with 260 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "25 April 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount
    Then Loan has 250 outstanding amount
    Then Loan status will be "ACTIVE"

  @TestRailId:C2447
  Scenario: As an admin I would like to check that the loan goes to ACTIVE when the loan is OVERPAID but the chargeback amount is HIGHER than the overpaid amount
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2022"
    And Admin successfully approves the loan on "1 January 2022" with "1000" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "1 February 2022"
    And Customer makes "AUTOPAY" repayment on "1 February 2022" with 1200 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "OVERPAID"
    When Admin sets the business date to "25 February 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 50 outstanding amount

  @TestRailId:C2448
  Scenario: As an admin I would like to check that the loan remains OVERPAID when the loan is OVERPAID but the chargeback amount is LOWER than the overpaid amount
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2022"
    And Admin successfully approves the loan on "1 January 2022" with "1000" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "1 February 2022"
    And Customer makes "AUTOPAY" repayment on "1 February 2022" with 1300 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "OVERPAID"
    When Admin sets the business date to "25 February 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount

  @TestRailId:C2449
  Scenario: As an admin I would like to check that the loan goes to CLOSED when the loan is OVERPAID but the chargeback amount is EQUALS than the overpaid amount
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2022"
    And Admin successfully approves the loan on "1 January 2022" with "1000" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "1 February 2022"
    And Customer makes "AUTOPAY" repayment on "1 February 2022" with 1250 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "OVERPAID"
    When Admin sets the business date to "25 February 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount

  @TestRailId:C2458
  Scenario: As an admin I would like to check that delinquency and overdue treatment of chargeback works properly
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 January 2022", with Principal: "3000", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2022" with "3000" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "3000" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "01 February 2022"
    And Customer makes "AUTOPAY" repayment on "01 February 2022" with 1000 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "01 March 2022"
    And Customer makes "AUTOPAY" repayment on "01 March 2022" with 1000 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "15 March 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 1000 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "07 April 2022"
    When Admin runs inline COB job for Loan
    Then Admin checks that delinquency range is: "RANGE_3" and has delinquentDate "2022-03-18"
    Then Loan has 2000 total overdue amount

  @TestRailId:C2459
  Scenario: As an admin I would like to check that delinquency and overdue treatment of chargeback works properly when all the chargeback transactions are fully paid
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 January 2022", with Principal: "3000", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2022" with "3000" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "3000" EUR transaction amount
    When Admin sets the business date to "01 February 2022"
    And Customer makes "AUTOPAY" repayment on "01 February 2022" with 1000 EUR transaction amount
    When Admin sets the business date to "01 March 2022"
    And Customer makes "AUTOPAY" repayment on "01 March 2022" with 1000 EUR transaction amount
    When Admin sets the business date to "15 March 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 1000 EUR transaction amount
    When Admin sets the business date to "07 April 2022"
    And Admin runs the Loan Delinquency Classification job
    Then Admin checks that delinquency range is: "RANGE_3" and has delinquentDate "2022-03-18"
    And Customer makes "AUTOPAY" repayment on "20 March 2022" with 1000 EUR transaction amount
    Then Admin checks that delinquency range is: "RANGE_1" and has delinquentDate "2022-04-04"
    Then Loan has 1000 total overdue amount

  @TestRailId:C2460
  Scenario: As an admin I would like to check that delinquency and overdue treatment of chargeback works properly when all the instalments are fully paid
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 January 2022", with Principal: "3000", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2022" with "3000" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "3000" EUR transaction amount
    When Admin sets the business date to "01 February 2022"
    And Customer makes "AUTOPAY" repayment on "01 February 2022" with 1000 EUR transaction amount
    When Admin sets the business date to "01 March 2022"
    And Customer makes "AUTOPAY" repayment on "01 March 2022" with 1000 EUR transaction amount
    When Admin sets the business date to "15 March 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 1000 EUR transaction amount
    When Admin sets the business date to "07 April 2022"
    And Admin runs the Loan Delinquency Classification job
    Then Admin checks that delinquency range is: "RANGE_3" and has delinquentDate "2022-03-18"
    And Customer makes "AUTOPAY" repayment on "20 March 2022" with 1000 EUR transaction amount
    Then Admin checks that delinquency range is: "RANGE_1" and has delinquentDate "2022-04-04"
    Then Loan has 1000 total overdue amount
    And Customer makes "AUTOPAY" repayment on "01 April 2022" with 1000 EUR transaction amount
    Then Admin checks that delinquency range is: "NO_DELINQUENCY" and has delinquentDate ""
    Then Loan has 0 total overdue amount

  @TestRailId:C2486
  Scenario: As an admin I would like to check that Goodwill credit, Payout refund, Merchant Issued refund works properly with chargeback for a fully paid loan
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2022"
    And Admin successfully approves the loan on "1 January 2022" with "1000" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "1 February 2022"
    And Customer makes "AUTOPAY" repayment on "1 February 2022" with 1000 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "25 February 2022"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 1000 EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "15 January 2022" with 1000 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "17 January 2022" with 100 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 100 overpaid amount
    When Admin makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "18 January 2022" with 100 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 200 overpaid amount
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "19 January 2022" with 100 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 300 overpaid amount

  @TestRailId:C2543
  Scenario: When charge backs comes in after the loan is closed, before the maturity date
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 250 EUR transaction amount
    When Admin sets the business date to "30 March 2023"
    And Customer makes "AUTOPAY" repayment on "30 March 2023" with 250 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 2
    Then Loan has 250 outstanding amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 March 2023    | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2023    |                  | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 250.0 | 250.0      | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 750  | 250        | 0    | 250         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "30 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "30 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |


  @TestRailId:C2544
  Scenario: When repayment happens again on the charge backs
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 250 EUR transaction amount
    When Admin sets the business date to "30 March 2023"
    And Customer makes "AUTOPAY" repayment on "30 March 2023" with 250 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 2
    Then Loan has 250 outstanding amount
    When Admin sets the business date to "01 April 2023"
    And Customer makes "AUTOPAY" repayment on "01 April 2023" with 250 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 March 2023    | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2023    | 01 April 2023    | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 500.0 | 250.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 1000 | 250        | 0    | 0           |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "30 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "30 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |

  @TestRailId:C2545
  Scenario: When repayment 1 is reversed
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 250 EUR transaction amount
    When Admin sets the business date to "30 March 2023"
    And Customer makes "AUTOPAY" repayment on "30 March 2023" with 250 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 2
    Then Loan has 250 outstanding amount
    When Admin sets the business date to "01 April 2023"
    And Customer makes "AUTOPAY" repayment on "01 April 2023" with 250 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "1 February 2023"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023  |               | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2023 | 01 March 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 28   | 01 March 2023    | 30 March 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 3  | 31   | 01 April 2023    |               | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 250.0 | 0.0        | 0.0   | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 750  | 0          | 500  | 250         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "30 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "30 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |

  @TestRailId:C2546
  Scenario: When 2 repayments are reversed (repayment 1 & 3)
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 250 EUR transaction amount
    When Admin sets the business date to "30 March 2023"
    And Customer makes "AUTOPAY" repayment on "30 March 2023" with 250 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 2
    Then Loan has 250 outstanding amount
    When Admin sets the business date to "01 April 2023"
    And Customer makes "AUTOPAY" repayment on "01 April 2023" with 250 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "1 February 2023"
    When Customer undo "3"th repayment on "30 March 2023"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023  |               | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2023 | 01 March 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 April 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 3  | 31   | 01 April 2023    |               | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 500  | 0          | 500  | 500         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "30 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "30 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |

  @TestRailId:C2547
  Scenario: When chargeback happens after the charge addition on maturity date for repayment 01-03-2022
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 250 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 250 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "30 March 2023"
    And Customer makes "AUTOPAY" repayment on "30 March 2023" with 250 EUR transaction amount
    When Customer undo "3"th repayment on "30 March 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "30 March 2023" due date and 20 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan has 270 outstanding amount
    When Admin sets the business date to "31 March 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "01 April 2023"
    And Customer makes "AUTOPAY" repayment on "01 April 2023" with 270 EUR transaction amount
    Then Loan has 0 outstanding amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 2
    When Admin runs inline COB job for Loan
    Then Loan has 250 outstanding amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 March 2023    | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2023    |                  | 0.0             | 500.0         | 0.0      | 20.0 | 0.0       | 520.0 | 270.0 | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 770  | 0          | 0    | 250         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "30 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "30 March 2023" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 20.0  |        |
      | INCOME | 404007       | Fee Income              |       | 20.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 20.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 270.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |

  @TestRailId:C2548
  Scenario: When chargeback comes in after the loan overpayment-1
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 250 EUR transaction amount
    When Admin sets the business date to "30 March 2023"
    And Customer makes "AUTOPAY" repayment on "30 March 2023" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "01 April 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 2
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 March 2023    | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2023    | 30 March 2023    | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      | 750           | 0        | 0    | 0         | 750 | 750  | 250        | 0    | 0           |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "30 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | l1           | Overpayment account       |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |

  @TestRailId:C2549
  Scenario: When chargeback comes in after the loan overpayment-2
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 150 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 350 EUR transaction amount
    When Admin sets the business date to "30 March 2023"
    And Customer makes "AUTOPAY" repayment on "30 March 2023" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "01 April 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 150 EUR transaction amount for Payment nr. 1
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023  |               | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2023 | 01 March 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 100.0 | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 March 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 31   | 01 April 2023    | 30 March 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      | 750           | 0        | 0    | 0         | 750 | 750  | 250        | 100  | 0           |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 150.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 150.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 350.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 350.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "30 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | l1           | Overpayment account       |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 150.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 150.0  |

  @TestRailId:C2550
  Scenario: When chargeback comes in after the loan overpayment-3
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 150 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 350 EUR transaction amount
    When Admin sets the business date to "30 March 2023"
    And Customer makes "AUTOPAY" repayment on "30 March 2023" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "01 April 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 350 EUR transaction amount for Payment nr. 2
    Then Loan has 100 outstanding amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023  |               | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2023 | 01 March 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 100.0 | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 March 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 31   | 01 April 2023    |               | 0.0             | 600.0         | 0.0      | 0.0  | 0.0       | 600.0 | 500.0 | 250.0      | 0.0   | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1100          | 0        | 0    | 0         | 1100 | 1000 | 250        | 100  | 100         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 150.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 150.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 350.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 350.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "30 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | l1           | Overpayment account       |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 350.0  |
      | LIABILITY | l1           | Overpayment account       | 250.0 |        |

  @TestRailId:C2551
  Scenario: When chargeback comes in after the loan overpayment-4 with reverse and replay
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 150 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 350 EUR transaction amount
    When Admin sets the business date to "30 March 2023"
    And Customer makes "AUTOPAY" repayment on "30 March 2023" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "01 April 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 350 EUR transaction amount for Payment nr. 2
    Then Loan has 100 outstanding amount
    When Customer undo "1"th repayment on "01 February 2023"
    Then Loan has 250 outstanding amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023  |               | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2023 | 01 March 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 28   | 01 March 2023    | 30 March 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 150.0 | 0.0         |
      | 3  | 31   | 01 April 2023    |               | 0.0             | 600.0         | 0.0      | 0.0  | 0.0       | 600.0 | 350.0 | 250.0      | 0.0   | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1100          | 0        | 0    | 0         | 1100 | 850  | 250        | 400  | 250         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 150.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 150.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 150.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 150.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 350.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 350.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "30 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 400.0  |
      | LIABILITY | l1           | Overpayment account       |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 350.0  |
      | LIABILITY | l1           | Overpayment account       | 100.0 |        |

  @TestRailId:C2608
  Scenario: When charge backs comes in after the loan is closed for the repayment 01-03-2022 (after maturity)
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 April 2023"
    And Customer makes "AUTOPAY" repayment on "1 April 2023" with 250 EUR transaction amount
    When Admin sets the business date to "25 April 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 2
    Then Loan has 250 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 March 2023    | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2023    | 01 April 2023    | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 24   | 25 April 2023    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 750  | 0          | 0    | 250         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "25 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |

  @TestRailId:C2609
  Scenario: When repayment happens again on the charge backs (after maturity)
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 April 2023"
    And Customer makes "AUTOPAY" repayment on "1 April 2023" with 250 EUR transaction amount
    When Admin sets the business date to "25 April 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 2
    Then Loan has 250 outstanding amount
    And Customer makes "AUTOPAY" repayment on "25 April 2023" with 250 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 March 2023    | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2023    | 01 April 2023    | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 24   | 25 April 2023    | 25 April 2023    | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 1000 | 0          | 0    | 0           |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "25 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |

  @TestRailId:C2610
  Scenario: When repayment 1 is reversed (after maturity)
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 April 2023"
    And Customer makes "AUTOPAY" repayment on "01 April 2023" with 250 EUR transaction amount
    When Admin sets the business date to "25 April 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 2
    Then Loan has 250 outstanding amount
    And Customer makes "AUTOPAY" repayment on "25 April 2023" with 250 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "1 February 2023"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023  |               | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2023 | 01 March 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 April 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 3  | 31   | 01 April 2023    | 25 April 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 4  | 24   | 25 April 2023    |               | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 750  | 0          | 750  | 250         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "25 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |

  @TestRailId:C2611
  Scenario: When 2 repayments are reversed (repayment 1 & 3) (after maturity)
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 April 2023"
    And Customer makes "AUTOPAY" repayment on "1 April 2023" with 250 EUR transaction amount
    When Admin sets the business date to "25 April 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 2
    Then Loan has 250 outstanding amount
    And Customer makes "AUTOPAY" repayment on "25 April 2023" with 250 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "1 February 2023"
    When Customer undo "3"th repayment on "1 April 2023"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023  |               | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2023 | 01 March 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 28   | 01 March 2023    | 25 April 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 3  | 31   | 01 April 2023    |               | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 24   | 25 April 2023    |               | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 500  | 0          | 500  | 500         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "25 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |

  @TestRailId:C2623
  Scenario: When second charge back happens for the repayment 01-04-2022 (after maturity)
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 April 2023"
    And Customer makes "AUTOPAY" repayment on "1 April 2023" with 250 EUR transaction amount
    When Admin sets the business date to "25 April 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 2
    Then Loan has 250 outstanding amount
    And Customer makes "AUTOPAY" repayment on "25 April 2023" with 250 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "1 May 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 200 EUR transaction amount for Payment nr. 3
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 March 2023    | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2023    | 01 April 2023    | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2023      |                  | 0.0             | 450.0         | 0.0      | 0.0  | 0.0       | 450.0 | 250.0 | 0.0        | 0.0  | 200.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1200          | 0        | 0    | 0         | 1200 | 1000 | 0          | 0    | 200         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "25 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "25 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 May 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |

  @TestRailId:C2612
  Scenario: When chargeback happens after the charge addition on maturity date for repayment 01-03-2022 (after maturity)
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1       | 01 January 2023   | 750            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2023" with "750" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2023"
    And Customer makes "AUTOPAY" repayment on "01 March 2023" with 250 EUR transaction amount
    When Admin sets the business date to "01 April 2023"
    And Customer makes "AUTOPAY" repayment on "01 April 2023" with 250 EUR transaction amount
    When Customer undo "3"th repayment on "01 April 2023"
    When Admin sets the business date to "05 April 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "05 April 2023" due date and 20 EUR transaction amount
    Then Loan has 270 outstanding amount
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "05 April 2023" with 270 EUR transaction amount
    Then Loan has 0 outstanding amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 2
    Then Loan has 250 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023  |                  | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 March 2023    | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 31   | 01 April 2023    | 05 April 2023    | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 4  | 4    | 05 April 2023    |                  | 0.0             | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 20.0  | 0.0        | 0.0   | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 770  | 0          | 250  | 250         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "05 April 2023" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 20.0  |        |
      | INCOME | 404007       | Fee Income              |       | 20.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 20.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 270.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "05 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |

  @TestRailId:C2613
  Scenario: When chargeback comes in after the loan overpayment-1 (after maturity)
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 250 EUR transaction amount
    When Admin sets the business date to "1 April 2023"
    And Customer makes "AUTOPAY" repayment on "1 April 2023" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "25 April 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 2
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                  | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2023 | 01 February 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 March 2023    | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2023    | 01 April 2023    | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      | 750           | 0        | 0    | 0         | 750 | 750  | 0          | 0    | 0           |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | l1           | Overpayment account       |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "25 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |

  @TestRailId:C2614
  Scenario: When chargeback comes in after the loan overpayment-2 (after maturity)
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 150 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 350 EUR transaction amount
    When Admin sets the business date to "1 April 2023"
    And Customer makes "AUTOPAY" repayment on "1 April 2023" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "25 April 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 150 EUR transaction amount for Payment nr. 1
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023  |               | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2023 | 01 March 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 100.0 | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 March 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 31   | 01 April 2023    | 01 April 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      | 750           | 0        | 0    | 0         | 750 | 750  | 0          | 100  | 0           |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 150.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 150.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 350.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 350.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | l1           | Overpayment account       |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "25 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 150.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 150.0  |

  @TestRailId:C2615
  Scenario: When chargeback comes in after the loan overpayment-3 (after maturity)
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 150 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 350 EUR transaction amount
    When Admin sets the business date to "1 April 2023"
    And Customer makes "AUTOPAY" repayment on "1 April 2023" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "25 April 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 350 EUR transaction amount for Payment nr. 2
    Then Loan has 100 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023  |               | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2023 | 01 March 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 100.0 | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 March 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 31   | 01 April 2023    | 01 April 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 24   | 25 April 2023    |               | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 250.0 | 0.0        | 0.0   | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1100          | 0        | 0    | 0         | 1100 | 1000 | 0          | 100  | 100         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 150.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 150.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 350.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 350.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | l1           | Overpayment account       |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "25 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 350.0  |
      | LIABILITY | l1           | Overpayment account       | 250.0 |        |

  @TestRailId:C2616
  Scenario: When chargeback comes in after the loan overpayment-4 with reverse and replay (after maturity)
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 January 2023", with Principal: "750", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 January 2023" with "750" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "750" EUR transaction amount
    When Admin sets the business date to "1 February 2023"
    And Customer makes "AUTOPAY" repayment on "1 February 2023" with 150 EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    And Customer makes "AUTOPAY" repayment on "1 March 2023" with 350 EUR transaction amount
    When Admin sets the business date to "1 April 2023"
    And Customer makes "AUTOPAY" repayment on "1 April 2023" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "25 April 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 350 EUR transaction amount for Payment nr. 2
    Then Loan has 100 outstanding amount
    When Customer undo "1"th repayment on "01 February 2023"
    Then Loan has 250 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023  |               | 750.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2023 | 01 March 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 28   | 01 March 2023    | 01 April 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 150.0 | 0.0         |
      | 3  | 31   | 01 April 2023    | 01 April 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 24   | 25 April 2023    |               | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 100.0 | 0.0        | 0.0   | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1100          | 0        | 0    | 0         | 1100 | 850  | 0          | 400  | 250         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 750.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 750.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 150.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 150.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 150.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 150.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 350.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 350.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 400.0  |
      | LIABILITY | l1           | Overpayment account       |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "25 April 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 350.0  |
      | LIABILITY | l1           | Overpayment account       | 100.0 |        |

  @TestRailId:C2624
  Scenario: As an admin I would like to verify principal portion for partial chargeback for OVERPAID loan
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "01 February 2023"
    And Customer makes "AUTOPAY" repayment on "01 February 2023" with 500 EUR transaction amount
    Then Loan has 500 outstanding amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "01 March 2023"
    And Customer makes "AUTOPAY" repayment on "01 March 2023" with 300 EUR transaction amount
    Then Loan has 200 outstanding amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "01 April 2023"
    And Customer makes "AUTOPAY" repayment on "01 April 2023" with 300 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "OVERPAID"
    When Admin sets the business date to "04 April 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 300 EUR transaction amount for Payment nr. 2
    Then Loan status will be "ACTIVE"
    Then Loan has 200 outstanding amount
    When Admin sets the business date to "07 April 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 300 EUR transaction amount for Payment nr. 1
    Then Loan Transactions tab has a transaction with date: "07 April 2023", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Chargeback       | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 500.0        |
    Then Loan status will be "ACTIVE"
    Then Loan has 500 outstanding amount

  @TestRailId:C2870 @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation on a closed loan in case of payment type: REPAYMENT_ADJUSTMENT_CHARGEBACK
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 400            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "400" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "400" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 100 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 100 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 100 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "25 October 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 100 EUR transaction amount
    Then Loan has 100 outstanding amount
    Then Loan status will be "ACTIVE"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 September 2023 | 16 September 2023 | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 01 October 2023   | 01 October 2023   | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 16 October 2023   | 16 October 2023   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 5  | 9    | 25 October 2023   |                   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0   | 0.0        | 0.0  | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0        | 0.0  | 0         | 500.0 | 400.0 | 0.0        | 0.0  | 100.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 01 September 2023 | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 16 September 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 200.0        |
      | 01 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 16 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 25 October 2023   | Chargeback       | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |

  @TestRailId:C2871 @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation on a closed loan in case of payment type: REPAYMENT_ADJUSTMENT_REFUND
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 400            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "400" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "400" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 100 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 100 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 100 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "25 October 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 100 EUR transaction amount
    Then Loan has 100 outstanding amount
    Then Loan status will be "ACTIVE"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 September 2023 | 16 September 2023 | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 01 October 2023   | 01 October 2023   | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 16 October 2023   | 16 October 2023   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 5  | 9    | 25 October 2023   |                   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0   | 0.0        | 0.0  | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0        | 0.0  | 0         | 500.0 | 400.0 | 0.0        | 0.0  | 100.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 01 September 2023 | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 16 September 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 200.0        |
      | 01 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 16 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 25 October 2023   | Chargeback       | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |

  @TestRailId:C2872 @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation when full repayment happens after chargeback, on the same business date
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 400            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "400" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "400" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 100 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 100 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 100 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "25 October 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 100 EUR transaction amount
    Then Loan has 100 outstanding amount
    Then Loan status will be "ACTIVE"
    And Customer makes "AUTOPAY" repayment on "25 October 2023" with 100 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 September 2023 | 16 September 2023 | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 01 October 2023   | 01 October 2023   | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 16 October 2023   | 16 October 2023   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 5  | 9    | 25 October 2023   | 25 October 2023   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0        | 0.0  | 0         | 500.0 | 500.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 01 September 2023 | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 16 September 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 200.0        |
      | 01 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 16 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 25 October 2023   | Chargeback       | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 25 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C2873 @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation when partial repayment happens after chargeback, on the same business date
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 400            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "400" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "400" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 100 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 100 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 100 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "25 October 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 100 EUR transaction amount
    Then Loan has 100 outstanding amount
    Then Loan status will be "ACTIVE"
    And Customer makes "AUTOPAY" repayment on "25 October 2023" with 50 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 September 2023 | 16 September 2023 | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 01 October 2023   | 01 October 2023   | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 16 October 2023   | 16 October 2023   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 5  | 9    | 25 October 2023   |                   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 50.0  | 0.0        | 0.0  | 50.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0        | 0.0  | 0         | 500.0 | 450.0 | 0.0        | 0.0  | 50.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 01 September 2023 | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 16 September 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 200.0        |
      | 01 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 16 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 25 October 2023   | Chargeback       | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 25 October 2023   | Repayment        | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 50.0         |

  @TestRailId:C2874 @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation when full repayment happens after chargeback, on a future business date
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 400            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "400" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "400" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 100 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 100 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 100 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "25 October 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 100 EUR transaction amount
    Then Loan has 100 outstanding amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "30 October 2023"
    And Customer makes "AUTOPAY" repayment on "30 October 2023" with 100 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 September 2023 |                   | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 15   | 16 September 2023 | 16 September 2023 | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 01 October 2023   | 01 October 2023   | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 15   | 16 October 2023   | 16 October 2023   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0   | 0.0         |
      | 5  | 9    | 25 October 2023   | 30 October 2023   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 100.0 | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 500.0         | 0        | 0.0  | 0         | 500.0 | 500.0 | 0.0        | 100.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 01 September 2023 | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 16 September 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 200.0        |
      | 01 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 16 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 25 October 2023   | Chargeback       | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 30 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C2875 @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation when partial repayment happens after chargeback, on a future business date
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 400            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "400" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "400" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 100 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 100 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 100 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "25 October 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 100 EUR transaction amount
    Then Loan has 100 outstanding amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "30 October 2023"
    And Customer makes "AUTOPAY" repayment on "30 October 2023" with 50 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 September 2023 | 16 September 2023 | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 01 October 2023   | 01 October 2023   | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 16 October 2023   | 16 October 2023   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 5  | 9    | 25 October 2023   |                   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 50.0  | 0.0        | 50.0 | 50.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0        | 0.0  | 0         | 500.0 | 450.0 | 0.0        | 50.0 | 50.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 01 September 2023 | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 16 September 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 200.0        |
      | 01 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 16 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 25 October 2023   | Chargeback       | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 30 October 2023   | Repayment        | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 50.0         |

  @TestRailId:C2876 @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation when a second chargeback happens after the repayment
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 400            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "400" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "400" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 100 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 100 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 100 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "25 October 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 100 EUR transaction amount
    Then Loan has 100 outstanding amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "30 October 2023"
    And Customer makes "AUTOPAY" repayment on "30 October 2023" with 100 EUR transaction amount
    When Admin sets the business date to "01 November 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 100 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 September 2023 |                   | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 15   | 16 September 2023 | 16 September 2023 | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 01 October 2023   | 01 October 2023   | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 15   | 16 October 2023   | 16 October 2023   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0   | 0.0         |
      | 5  | 16   | 01 November 2023  |                   | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 100.0 | 0.0        | 100.0 | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 600.0         | 0        | 0.0  | 0         | 600.0 | 500.0 | 0.0        | 100.0 | 100.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 01 September 2023 | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 16 September 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 200.0        |
      | 01 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 16 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 25 October 2023   | Chargeback       | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 30 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 01 November 2023  | Chargeback       | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |

  @TestRailId:C2877 @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation -loan goes to ACTIVE when the it is OVERPAID but the chargeback amount is HIGHER than the overpaid amount
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 400            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "400" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "400" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 100 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 100 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 150 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "OVERPAID"
    When Admin sets the business date to "25 October 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 100 EUR transaction amount
    Then Loan has 50 outstanding amount
    Then Loan status will be "ACTIVE"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 September 2023 | 16 September 2023 | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 01 October 2023   | 01 October 2023   | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 16 October 2023   | 16 October 2023   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 5  | 9    | 25 October 2023   |                   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 50.0  | 0.0        | 0.0  | 50.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0        | 0.0  | 0         | 500.0 | 450.0 | 0.0        | 0.0  | 50.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 01 September 2023 | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 16 September 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 200.0        |
      | 01 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 16 October 2023   | Repayment        | 150.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 25 October 2023   | Chargeback       | 100.0  | 50.0      | 0.0      | 0.0  | 0.0       | 50.0         |

  @TestRailId:C2878 @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation -loan remains OVERPAID when the it is OVERPAID but the chargeback amount is LOWER than the overpaid amount
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 400            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "400" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "400" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 100 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 100 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 150 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "OVERPAID"
    When Admin sets the business date to "25 October 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 25 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "OVERPAID"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 September 2023 | 16 September 2023 | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 01 October 2023   | 01 October 2023   | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 16 October 2023   | 16 October 2023   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 400.0         | 0        | 0.0  | 0         | 400.0 | 400.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 01 September 2023 | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 16 September 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 200.0        |
      | 01 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 16 October 2023   | Repayment        | 150.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 25 October 2023   | Chargeback       | 25.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C2879 @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation -loan goes to CLOSED when the loan is OVERPAID but the chargeback amount is EQUALS than the overpaid amount
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 400            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "400" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "400" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 100 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 100 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 150 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "OVERPAID"
    When Admin sets the business date to "25 October 2023"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 50 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 September 2023 | 16 September 2023 | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 01 October 2023   | 01 October 2023   | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 16 October 2023   | 16 October 2023   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 400.0         | 0        | 0.0  | 0         | 400.0 | 400.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 01 September 2023 | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 16 September 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 200.0        |
      | 01 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 16 October 2023   | Repayment        | 150.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 25 October 2023   | Chargeback       | 50.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C2880 @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - chargeback after the loan is closed, before the maturity date
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 400            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "400" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "400" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 100 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 100 EUR transaction amount
    When Admin sets the business date to "10 October 2023"
    And Customer makes "AUTOPAY" repayment on "10 October 2023" with 100 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 100 EUR transaction amount for Payment nr. 2
    Then Loan has 100 outstanding amount
    Then Loan status will be "ACTIVE"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 September 2023 | 16 September 2023 | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 01 October 2023   | 01 October 2023   | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 16 October 2023   |                   | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0 | 100.0 | 100.0      | 0.0  | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0        | 0.0  | 0         | 500.0 | 400.0 | 100.0      | 0.0  | 100.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 01 September 2023 | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 16 September 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 200.0        |
      | 01 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 10 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 10 October 2023   | Chargeback       | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |

  @TestRailId:C2881 @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - repayment 1 is reversed
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 400            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "400" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "400" EUR transaction amount
    When Admin sets the business date to "16 September 2023"
    And Customer makes "AUTOPAY" repayment on "16 September 2023" with 100 EUR transaction amount
    When Admin sets the business date to "01 October 2023"
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 100 EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 100 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "25 October 2023"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 100 EUR transaction amount for Payment nr. 2
    Then Loan has 100 outstanding amount
    When Admin sets the business date to "30 October 2023"
    And Customer makes "AUTOPAY" repayment on "30 October 2023" with 100 EUR transaction amount
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Customer undo "1"th transaction made on "16 September 2023"
    Then On Loan Transactions tab the "Repayment" Transaction with date "16 September 2023" is reverted
    Then Loan has 100 outstanding amount
    Then Loan status will be "ACTIVE"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 September 2023 |                   | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 15   | 16 September 2023 | 01 October 2023   | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 100.0 | 0.0         |
      | 3  | 15   | 01 October 2023   | 16 October 2023   | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 100.0 | 0.0         |
      | 4  | 15   | 16 October 2023   | 30 October 2023   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 100.0 | 0.0         |
      | 5  | 9    | 25 October 2023   |                   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0   | 0.0        | 0.0   | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 500.0         | 0        | 0.0  | 0         | 500.0 | 400.0 | 0.0        | 300.0 | 100.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 400.0        |
      | 01 September 2023 | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 300.0        |
      | 16 September 2023 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 200.0        |
      | 01 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 200.0        |
      | 16 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |
      | 25 October 2023   | Chargeback       | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 200.0        |
      | 30 October 2023   | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        |

  @TestRailId:C3034 @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - overpayment handling
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 September 2023 | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 September 2023" with "1000" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "05 September 2023"
    When Customer makes "AUTOPAY" repayment on "05 September 2023" with 500 EUR transaction amount
    Then Loan has 500 outstanding amount
    When Admin sets the business date to "11 January 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 500 EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "06 September 2023" with 200 EUR transaction amount and system-generated Idempotency key
    Then Loan has 800 outstanding amount
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 200 EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "07 September 2023" with 300 EUR transaction amount and system-generated Idempotency key
    Then Loan has 700 outstanding amount
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 300 EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "08 September 2023" with 100 EUR transaction amount and system-generated Idempotency key
    Then Loan has 900 outstanding amount
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 100 EUR transaction amount
    Then Loan has 1000 outstanding amount

  @TestRailId:C3038 @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - chargeback on downpayment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Downpayment nr. 1
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2024  |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 3  | 15   | 31 January 2024  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2024 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1250.0        | 0        | 0.0  | 0         | 1250.0 | 250.0 | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2024  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 January 2024  | Chargeback       | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1000.0       |

  @TestRailId:C3039 @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - partial chargeback on downpayment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 200 EUR transaction amount for Downpayment nr. 1
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2024  |                 | 500.0           | 450.0         | 0.0      | 0.0  | 0.0       | 450.0 | 0.0   | 0.0        | 0.0  | 450.0       |
      | 3  | 15   | 31 January 2024  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2024 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1200.0        | 0        | 0.0  | 0         | 1200.0 | 250.0 | 0.0        | 0.0  | 950.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2024  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 January 2024  | Chargeback       | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 950.0        |

  @TestRailId:C3243 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "16 January 2024" due date and 20 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "16 January 2024" due date and 50 EUR transaction amount
    When Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    When Customer makes "AUTOPAY" repayment on "16 January 2024" with 320 EUR transaction amount
    When Admin sets the business date to "17 January 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 10 EUR transaction amount

  @TestRailId:C3079 @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - full chargeback on overpaid loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "10 January 2024"
    When Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "10 January 2024" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 125 overpaid amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2024  | 10 January 2024 | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 January 2024  | 10 January 2024 | 125.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 February 2024 | 10 January 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 500.0 | 375.0      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 January 2024  | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 10 January 2024  | Payout Refund    | 500.0  | 375.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 125.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 125.0 |        |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 375.0  |
      | LIABILITY | l1           | Overpayment account       |       | 125.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
#    --- Chargeback transaction added on same day ---
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2024  | 10 January 2024 | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 January 2024  | 10 January 2024 | 125.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 February 2024 | 10 January 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 500.0 | 375.0      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 January 2024  | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 10 January 2024  | Payout Refund    | 500.0  | 375.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 10 January 2024  | Chargeback       | 125.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 125.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 125.0 |        |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 375.0  |
      | LIABILITY | l1           | Overpayment account       |       | 125.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "10 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 125.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 125.0  |

  @TestRailId:C3080 @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - partial chargeback on overpaid loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "10 January 2024"
    When Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "10 January 2024" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 125 overpaid amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2024  | 10 January 2024 | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 January 2024  | 10 January 2024 | 125.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 February 2024 | 10 January 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 500.0 | 375.0      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 January 2024  | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 10 January 2024  | Payout Refund    | 500.0  | 375.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 125.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 125.0 |        |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 375.0  |
      | LIABILITY | l1           | Overpayment account       |       | 125.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
#    --- Chargeback transaction added on same day ---
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 25 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2024  | 10 January 2024 | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
      | 3  | 15   | 31 January 2024  | 10 January 2024 | 125.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
      | 4  | 15   | 15 February 2024 | 10 January 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 500.0 | 375.0      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 January 2024  | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 10 January 2024  | Payout Refund    | 500.0  | 375.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 10 January 2024  | Chargeback       | 25.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 125.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 125.0 |        |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "10 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 375.0  |
      | LIABILITY | l1           | Overpayment account       |       | 125.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "10 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 25.0  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 25.0   |

  @TestRailId:C3081 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC1: partial, prior chargeback
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "20 January 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 125 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 16 January 2024  | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 31 January 2024  | 125.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 625.0         | 0.0      | 0.0  | 0.0       | 625.0 | 625.0 | 0.0        | 125.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 20 January 2024  | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 31 January 2024  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 125.0        |
      | 15 February 2024 | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3082 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC2: partial, prior chargeback with penalty
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2024" due date and 3 EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 128 EUR transaction amount
    When Admin sets the business date to "20 January 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 128 EUR transaction amount
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 253 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 125 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 16 January 2024  | 250.0           | 125.0         | 0.0      | 0.0  | 3.0       | 128.0 | 128.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 31 January 2024  | 125.0           | 250.0         | 0.0      | 0.0  | 3.0       | 253.0 | 253.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 625.0         | 0.0      | 0.0  | 6.0       | 631.0 | 631.0 | 0.0        | 125.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 128.0  | 125.0     | 0.0      | 0.0  | 3.0       | 250.0        |
      | 20 January 2024  | Chargeback       | 128.0  | 125.0     | 0.0      | 0.0  | 3.0       | 375.0        |
      | 31 January 2024  | Repayment        | 253.0  | 250.0     | 0.0      | 0.0  | 3.0       | 125.0        |
      | 15 February 2024 | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 15 February 2024 | Accrual          | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          |

  @TestRailId:C3083 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC3: partial, prior chargeback with penalty (2)
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2024" due date and 3 EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 128 EUR transaction amount
    When Admin sets the business date to "20 January 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 53 EUR transaction amount
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 175 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 128 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 16 January 2024  | 250.0           | 125.0         | 0.0      | 0.0  | 3.0       | 128.0 | 128.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 15 February 2024 | 125.0           | 175.0         | 0.0      | 0.0  | 3.0       | 178.0 | 178.0 | 0.0        | 3.0   | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 550.0         | 0.0      | 0.0  | 6.0       | 556.0 | 556.0 | 0.0        | 128.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 128.0  | 125.0     | 0.0      | 0.0  | 3.0       | 250.0        |
      | 20 January 2024  | Chargeback       | 53.0   | 50.0      | 0.0      | 0.0  | 3.0       | 300.0        |
      | 31 January 2024  | Repayment        | 175.0  | 172.0     | 0.0      | 0.0  | 3.0       | 128.0        |
      | 15 February 2024 | Repayment        | 128.0  | 128.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 15 February 2024 | Accrual          | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          |

  @TestRailId:C3084 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC4: partial, prior chargeback with penalty and fee
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2024" due date and 3 EUR transaction amount
    When Admin sets the business date to "02 January 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 January 2024" due date and 4 EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "20 January 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 50 EUR transaction amount
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 182 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 31 January 2024  | 250.0           | 125.0         | 0.0      | 4.0  | 3.0       | 132.0 | 132.0 | 0.0        | 7.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 15 February 2024 | 125.0           | 168.0         | 0.0      | 4.0  | 3.0       | 175.0 | 175.0 | 0.0        | 57.0  | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 543.0         | 0.0      | 8.0  | 6.0       | 557.0 | 557.0 | 0.0        | 189.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 125.0  | 118.0     | 0.0      | 4.0  | 3.0       | 257.0        |
      | 20 January 2024  | Chargeback       | 50.0   | 43.0      | 0.0      | 4.0  | 3.0       | 300.0        |
      | 31 January 2024  | Repayment        | 125.0  | 118.0     | 0.0      | 4.0  | 3.0       | 182.0        |
      | 15 February 2024 | Repayment        | 182.0  | 182.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 15 February 2024 | Accrual          | 7.0    | 0.0       | 0.0      | 4.0  | 3.0       | 0.0          |

  @TestRailId:C3085 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC5: full, prior chargeback
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "20 January 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount for Payment nr. 1
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount for Payment nr. 2
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 375 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 125 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 16 January 2024  | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 31 January 2024  | 125.0           | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 375.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 750.0         | 0.0      | 0.0  | 0.0       | 750.0 | 750.0 | 0.0        | 125.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 20 January 2024  | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 20 January 2024  | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 31 January 2024  | Repayment        | 375.0  | 375.0     | 0.0      | 0.0  | 0.0       | 125.0        |
      | 15 February 2024 | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3086 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC6: full, after chargeback
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 125 EUR transaction amount
    When Admin sets the business date to "20 February 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount for Payment nr. 1
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount for Payment nr. 2
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount for Payment nr. 3
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount for Payment nr. 4
    When Admin sets the business date to "28 February 2024"
    And Customer makes "AUTOPAY" repayment on "28 February 2024" with 500 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 16 January 2024  | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 31 January 2024  | 125.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 5  | 5    | 20 February 2024 | 28 February 2024 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 500.0 | 0.0        | 500.0 | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 625.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 31 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 125.0        |
      | 15 February 2024 | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 20 February 2024 | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 125.0        |
      | 20 February 2024 | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 20 February 2024 | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 20 February 2024 | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 28 February 2024 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3087 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC7: partial, after chargeback
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 125 EUR transaction amount
    When Admin sets the business date to "20 February 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount for Payment nr. 2
    When Admin sets the business date to "28 February 2024"
    And Customer makes "AUTOPAY" repayment on "28 February 2024" with 125 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 16 January 2024  | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 31 January 2024  | 125.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 5  | 5    | 20 February 2024 | 28 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 625.0         | 0.0      | 0.0  | 0.0       | 625.0 | 625.0 | 0.0        | 250.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 31 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 125.0        |
      | 15 February 2024 | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 20 February 2024 | Chargeback       | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 125.0        |
      | 28 February 2024 | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3088 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation UC8: multiple, prior chargebacks on same payment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 500            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "500" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "500" EUR transaction amount
    When Admin sets the business date to "04 January 2024"
    And Customer makes "AUTOPAY" repayment on "04 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "16 January 2024"
    And Customer makes "AUTOPAY" repayment on "16 January 2024" with 125 EUR transaction amount
    When Admin sets the business date to "20 January 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 75 EUR transaction amount for Payment nr. 1
    When Admin sets the business date to "25 January 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 50 EUR transaction amount for Payment nr. 1
    When Admin sets the business date to "31 January 2024"
    And Customer makes "AUTOPAY" repayment on "31 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "15 February 2024"
    And Customer makes "AUTOPAY" repayment on "15 February 2024" with 125 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024  | 04 January 2024  | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 125.0 | 0.0         |
      | 2  | 15   | 16 January 2024  | 16 January 2024  | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 31 January 2024  | 31 January 2024  | 125.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 4  | 15   | 15 February 2024 | 15 February 2024 | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      | 625.0         | 0.0      | 0.0  | 0.0       | 625.0 | 625.0 | 0.0        | 125.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 04 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 January 2024  | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 20 January 2024  | Chargeback       | 75.0   | 75.0      | 0.0      | 0.0  | 0.0       | 325.0        |
      | 25 January 2024  | Chargeback       | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 375.0        |
      | 31 January 2024  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 125.0        |
      | 15 February 2024 | Repayment        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 0.0          |

  @TestRailId:C3094 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC1: chargeback on principal
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 250 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |

  @TestRailId:C3095 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC2: chargeback on principal and fees
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 March 2024" due date and 30 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 280 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 250 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 280 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 30.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 280.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 280.0  |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 30.0  |        |

  @TestRailId:C3096 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC3: chargeback on principal and penalties
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 March 2024" due date and 30 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 280 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 250 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 280 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 30.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 280.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 280.0  |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 30.0  |        |

  @TestRailId:C3097 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC4: when overpayment amount is smaller than chargeback
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 400 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 150 overpaid amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | l1           | Overpayment account       |       | 150.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 400.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
      | LIABILITY | l1           | Overpayment account       | 150.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |

  @TestRailId:C3098 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC5: with fees- when overpayment amount is bigger than chargeback
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 March 2024" due date and 30 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 400 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 120 overpaid amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 100 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 220.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 30.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 280.0  |
      | LIABILITY | l1           | Overpayment account       |       | 120.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 400.0 |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 April 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 30.0  |        |
      | INCOME | 404007       | Fee Income              |       | 30.0   |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
      | LIABILITY | l1           | Overpayment account       | 100.0 |        |

  @TestRailId:C3099 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC6: when overpayment amount is bigger than chargeback
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 400 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 150 overpaid amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 100 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | l1           | Overpayment account       |       | 150.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 400.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
      | LIABILITY | l1           | Overpayment account       | 100.0 |        |

  @TestRailId:C3100 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC7: chargeback on charge-off loan account with principal
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 250 EUR transaction amount
    When Admin sets the business date to "15 March 2024"
    And Admin does charge-off the loan on "15 March 2024"
    When Admin sets the business date to "01 April 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 March 2024" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 250.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 250.0 |        |

  @TestRailId:C3101 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC8: chargeback on charge-off loan account with principal and Fees
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 March 2024" due date and 30 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 280 EUR transaction amount
    When Admin sets the business date to "15 March 2024"
    And Admin does charge-off the loan on "15 March 2024"
    When Admin sets the business date to "01 April 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 280 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 30.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 280.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 March 2024" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 250.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 280.0  |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 250.0 |        |
      | INCOME    | 404008       | Fee Charge Off            | 30.0  |        |

  @TestRailId:C3102 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC9: chargeback on charge-off loan account with principal and Penalties
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 March 2024" due date and 30 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 280 EUR transaction amount
    When Admin sets the business date to "15 March 2024"
    And Admin does charge-off the loan on "15 March 2024"
    When Admin sets the business date to "01 April 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 280 EUR transaction amount for Payment nr. 3
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 30.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 280.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 March 2024" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 250.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 250.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 280.0  |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      | 250.0 |        |
      | INCOME    | 404008       | Fee Charge Off            | 30.0  |        |

  @TestRailId:C3111 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC10: chargeback on overpaid loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 100            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 January 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 0.0  | 0.0        | 0.0  | 30.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 105 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          |
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "01 March 2024"
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "19 January 2024" with 10 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 19 January 2024  | Merchant Issued Refund | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | 0.0         |
      | 01 February 2024 | Repayment              | 105.0  | 90.0      | 0.0      | 0.0  | 5.0       | 0.0          | 10.0        |
      | 01 February 2024 | Accrual                | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
    Then Loan has 0 outstanding amount
    Then Loan has 10 overpaid amount
    Then Loan status will be "OVERPAID"
    When Admin sets the business date to "15 April 2024"
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 7 EUR transaction amount for Payment nr. 1
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "MERCHANT_ISSUED_REFUND" transaction with date "19 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 90.0   |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 5.0    |
      | LIABILITY | l1           | Overpayment account       |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 105.0 |        |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 5  | 14   | 15 April 2024    | 15 April 2024    | 0.0             | 2.0           | 0.0      | 0.0  | 5.0       | 7.0  | 7.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 19 January 2024  | Merchant Issued Refund | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | 0.0         |
      | 01 February 2024 | Repayment              | 105.0  | 90.0      | 0.0      | 0.0  | 5.0       | 0.0          | 10.0        |
      | 01 February 2024 | Accrual                | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 15 April 2024    | Chargeback             | 7.0    | 2.0       | 0.0      | 0.0  | 5.0       | 0.0          | 7.0         |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "15 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 7.0   |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 7.0    |
    Then Loan has 0 outstanding amount
    Then Loan has 3 overpaid amount
    Then Loan status will be "OVERPAID"
    When Admin sets the business date to "16 April 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 7 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 5  | 15   | 16 April 2024    |                  | 0.0             | 9.0           | 0.0      | 0.0  | 5.0       | 14.0 | 10.0 | 0.0        | 0.0  | 4.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 19 January 2024  | Merchant Issued Refund | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | 0.0         |
      | 01 February 2024 | Repayment              | 105.0  | 90.0      | 0.0      | 0.0  | 5.0       | 0.0          | 10.0        |
      | 01 February 2024 | Accrual                | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 15 April 2024    | Chargeback             | 7.0    | 2.0       | 0.0      | 0.0  | 5.0       | 0.0          | 7.0         |
      | 16 April 2024    | Chargeback             | 7.0    | 7.0       | 0.0      | 0.0  | 0.0       | 4.0          | 3.0         |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "16 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 3.0   |        |
      | ASSET     | 112601       | Loans Receivable          | 4.0   |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 7.0    |
    Then Loan has 4 outstanding amount
    Then Loan status will be "ACTIVE"

  @TestRailId:C3116 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC11: chargeback before maturity on payment with fee
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 100            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 January 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 0.0  | 0.0        | 0.0  | 30.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 105 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          |
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
#    ---Chargeback with amount < fee ---
    When Admin sets the business date to "01 March 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 3 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    |                  | 0.0             | 25.0          | 0.0      | 0.0  | 3.0       | 28.0 | 25.0 | 25.0       | 0.0  | 3.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 8.0       | 108.0 | 105.0 | 50.0       | 25.0 | 3.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 March 2024    | Chargeback       | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          | 0.0         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 105.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 3.0    |
      | ASSET     | 112603       | Interest/Fee Receivable   | 3.0   |        |
    Then Loan has 3 outstanding amount
    Then Loan status will be "ACTIVE"
#    --- Chargeback with amount > fee ---
    When Admin sets the business date to "05 March 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 10 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    |                  | 0.0             | 33.0          | 0.0      | 0.0  | 5.0       | 38.0 | 25.0 | 25.0       | 0.0  | 13.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 108.0         | 0.0      | 0.0  | 10.0      | 118.0 | 105.0 | 50.0       | 25.0 | 13.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 March 2024    | Chargeback       | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          | 0.0         |
      | 05 March 2024    | Chargeback       | 10.0   | 8.0       | 0.0      | 0.0  | 2.0       | 8.0          | 0.0         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 105.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 3.0    |
      | ASSET     | 112603       | Interest/Fee Receivable   | 3.0   |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "05 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 10.0   |
      | ASSET     | 112601       | Loans Receivable          | 8.0   |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 2.0   |        |
    Then Loan has 13 outstanding amount
    Then Loan status will be "ACTIVE"

  @TestRailId:C3117 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC12: chargeback after maturity on payment with fee
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 100            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 January 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 0.0  | 0.0        | 0.0  | 30.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 105 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          |
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
#    ---Chargeback with amount < fee ---
    When Admin sets the business date to "15 April 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 3 EUR transaction amount for Payment nr. 1
    When Admin sets the business date to "16 April 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 5  | 14   | 15 April 2024    |                  | 0.0             | 0.0           | 0.0      | 0.0  | 3.0       | 3.0  | 0.0  | 0.0        | 0.0  | 3.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 8.0       | 108.0 | 105.0 | 50.0       | 25.0 | 3.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 15 April 2024    | Chargeback       | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          | 0.0         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 105.0 |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 February 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 5.0   |        |
      | INCOME | 404007       | Fee Income              |       | 5.0    |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "15 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 3.0    |
      | ASSET     | 112603       | Interest/Fee Receivable   | 3.0   |        |
    Then Loan has 3 outstanding amount
    Then Loan status will be "ACTIVE"
#    --- Chargeback with amount > fee ---
    When Admin sets the business date to "20 April 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 10 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 5  | 19   | 20 April 2024    |                  | 0.0             | 8.0           | 0.0      | 0.0  | 5.0       | 13.0 | 0.0  | 0.0        | 0.0  | 13.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 108.0         | 0.0      | 0.0  | 10.0      | 118.0 | 105.0 | 50.0       | 25.0 | 13.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 15 April 2024    | Chargeback       | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          | 0.0         |
      | 20 April 2024    | Chargeback       | 10.0   | 8.0       | 0.0      | 0.0  | 2.0       | 8.0          | 0.0         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 105.0 |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 February 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 5.0   |        |
      | INCOME | 404007       | Fee Income              |       | 5.0    |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "15 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 3.0    |
      | ASSET     | 112603       | Interest/Fee Receivable   | 3.0   |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "20 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 10.0   |
      | ASSET     | 112601       | Loans Receivable          | 8.0   |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 2.0   |        |
    Then Loan has 13 outstanding amount
    Then Loan status will be "ACTIVE"

  @TestRailId:C3118 @creditAllocation @AdvancedPaymentAllocationChargeback @AdvancedPaymentAllocation
  Scenario: Verify chargeback function for advanced payment allocation - credit allocation accounting entries UC13: loan goes overpaid after maturity then chargeback applied on payment with fee
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 100            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 January 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 0.0  | 0.0        | 0.0  | 30.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 105 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 February 2024 | Repayment        | 105.0  | 100.0     | 0.0      | 0.0  | 5.0       | 0.0          |
      | 01 February 2024 | Accrual          | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          |
    Then Loan has 0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
#    --- Loan goes overpaid after maturity ---
    When Admin sets the business date to "10 April 2024"
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "19 January 2024" with 10 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 5.0       | 105.0 | 105.0 | 50.0       | 25.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 19 January 2024  | Merchant Issued Refund | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | 0.0         |
      | 01 February 2024 | Accrual                | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 February 2024 | Repayment              | 105.0  | 90.0      | 0.0      | 0.0  | 5.0       | 0.0          | 10.0        |
    Then Loan has 0 outstanding amount
    Then Loan has 10 overpaid amount
    Then Loan status will be "OVERPAID"
#    ---Chargeback with amount < overpaid amount and fee amount ---
    When Admin sets the business date to "15 April 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 3 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 5  | 14   | 15 April 2024    | 15 April 2024    | 0.0             | 0.0           | 0.0      | 0.0  | 3.0       | 3.0  | 3.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 8.0       | 108.0 | 108.0 | 50.0       | 25.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 19 January 2024  | Merchant Issued Refund | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | 0.0         |
      | 01 February 2024 | Accrual                | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 01 February 2024 | Repayment              | 105.0  | 90.0      | 0.0      | 0.0  | 5.0       | 0.0          | 10.0        |
      | 15 April 2024    | Chargeback             | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          | 3.0         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "MERCHANT_ISSUED_REFUND" transaction with date "19 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 February 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 5.0   |        |
      | INCOME | 404007       | Fee Income              |       | 5.0    |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 90.0   |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 5.0    |
      | LIABILITY | l1           | Overpayment account       |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 105.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "15 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 3.0    |
      | LIABILITY | l1           | Overpayment account       | 3.0   |        |
    Then Loan has 0 outstanding amount
    Then Loan has 7 overpaid amount
    Then Loan status will be "OVERPAID"
#    --- Chargeback with amount > overpaid amount and fee amount ---
    When Admin sets the business date to "20 April 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 10 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 February 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 | 01 February 2024 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 30.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024    | 01 February 2024 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024    | 01 February 2024 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 25.0       | 0.0  | 0.0         |
      | 5  | 19   | 20 April 2024    |                  | 0.0             | 8.0           | 0.0      | 0.0  | 5.0       | 13.0 | 10.0 | 0.0        | 0.0  | 3.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 108.0         | 0.0      | 0.0  | 10.0      | 118.0 | 115.0 | 50.0       | 25.0 | 3.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Overpayment |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | 0.0         |
      | 19 January 2024  | Merchant Issued Refund | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | 0.0         |
      | 01 February 2024 | Repayment              | 105.0  | 90.0      | 0.0      | 0.0  | 5.0       | 0.0          | 10.0        |
      | 01 February 2024 | Accrual                | 5.0    | 0.0       | 0.0      | 0.0  | 5.0       | 0.0          | 0.0         |
      | 15 April 2024    | Chargeback             | 3.0    | 0.0       | 0.0      | 0.0  | 3.0       | 0.0          | 3.0         |
      | 20 April 2024    | Chargeback             | 10.0   | 8.0       | 0.0      | 0.0  | 2.0       | 3.0          | 7.0         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "MERCHANT_ISSUED_REFUND" transaction with date "19 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 90.0   |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 5.0    |
      | LIABILITY | l1           | Overpayment account       |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 105.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "15 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 3.0    |
      | LIABILITY | l1           | Overpayment account       | 3.0   |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "20 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 10.0   |
      | LIABILITY | l1           | Overpayment account       | 7.0   |        |
      | ASSET     | 112601       | Loans Receivable          | 3.0   |        |
    Then Loan has 3 outstanding amount
    Then Loan status will be "ACTIVE"

  @TestRailId:C3138
  Scenario: Verify chargeback function when reverse replay happens with Advanced payment allocation and set allocation rules for Chargeback transaction
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION | 01 January 2024   | 100            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 January 2024" due date and 5 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 0.0  | 0.0        | 0.0  | 30.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin sets the business date to "05 January 2024"
    And Customer makes "AUTOPAY" repayment on "05 January 2024" with 28 EUR transaction amount
    When Admin sets the business date to "06 January 2024"
    And Customer makes "AUTOPAY" repayment on "06 January 2024" with 10 EUR transaction amount
    When Admin sets the business date to "07 January 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 05 January 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 |                 | 50.0            | 25.0          | 0.0      | 0.0  | 5.0       | 30.0 | 13.0 | 13.0       | 0.0  | 17.0        |
      | 3  | 29   | 01 March 2024    |                 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |                 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 05 January 2024  | Repayment        | 28.0   | 25.0      | 0.0      | 0.0  | 3.0       | 75.0         | false    | false    |
      | 06 January 2024  | Repayment        | 10.0   | 8.0       | 0.0      | 0.0  | 2.0       | 67.0         | false    | false    |
    When Admin makes "REPAYMENT_ADJUSTMENT_REFUND" chargeback with 10 EUR transaction amount for Payment nr. 2
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 05 January 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 |                 | 50.0            | 33.0          | 0.0      | 0.0  | 7.0       | 40.0 | 13.0 | 13.0       | 0.0  | 27.0        |
      | 3  | 29   | 01 March 2024    |                 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |                 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 05 January 2024  | Repayment        | 28.0   | 25.0      | 0.0      | 0.0  | 3.0       | 75.0         | false    | false    |
      | 06 January 2024  | Repayment        | 10.0   | 8.0       | 0.0      | 0.0  | 2.0       | 67.0         | false    | false    |
      | 07 January 2024  | Chargeback       | 10.0   | 8.0       | 0.0      | 0.0  | 2.0       | 75.0         | false    | false    |
    When Customer undo "1"th repayment on "05 January 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 10.0 | 0.0        | 10.0 | 15.0        |
      | 2  | 31   | 01 February 2024 |           | 50.0            | 35.0          | 0.0      | 0.0  | 5.0       | 40.0 | 0.0  | 0.0        | 0.0  | 40.0        |
      | 3  | 29   | 01 March 2024    |           | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |           | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 05 January 2024  | Repayment        | 28.0   | 25.0      | 0.0      | 0.0  | 3.0       | 75.0         | true     | false    |
      | 06 January 2024  | Repayment        | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 90.0         | false    | true     |
      | 07 January 2024  | Chargeback       | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
    And Customer makes "AUTOPAY" repayment on "03 January 2024" with 25 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 03 January 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 25.0 | 0.0        | 25.0 | 0.0         |
      | 2  | 31   | 01 February 2024 |                 | 50.0            | 30.0          | 0.0      | 0.0  | 10.0      | 40.0 | 10.0 | 10.0       | 0.0  | 30.0        |
      | 3  | 29   | 01 March 2024    |                 | 25.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
      | 4  | 31   | 01 April 2024    |                 | 0.0             | 25.0          | 0.0      | 0.0  | 0.0       | 25.0 | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 03 January 2024  | Repayment        | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    | false    |
      | 05 January 2024  | Repayment        | 28.0   | 25.0      | 0.0      | 0.0  | 3.0       | 75.0         | true     | false    |
      | 06 January 2024  | Repayment        | 10.0   | 5.0       | 0.0      | 0.0  | 5.0       | 70.0         | false    | true     |
      | 07 January 2024  | Chargeback       | 10.0   | 5.0       | 0.0      | 0.0  | 5.0       | 75.0         | false    | true     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 105.0         | 0.0      | 0.0  | 10.0       | 115.0 | 35.0 | 10.0       | 25.0 | 80.0        |
