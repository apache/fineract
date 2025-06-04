@LoanInterestPaymentWaiver
Feature: LoanInterestWaiver

  @TestRailId:C3141
  Scenario: Verify Interest Payment Waiver transaction - UC1: LP1 product, cumulative schedule, flat interest, allocation: interest-principal, interestPaymentWaiver on due date with exact amount
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 01 January 2024   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "01 February 2024" with 260 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 3  | 31   | 01 April 2024    |                  | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 4  | 30   | 01 May 2024      |                  | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 260.0 | 0.0        | 0.0  | 780.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2024 | Interest Payment Waiver | 260.0  | 250.0     | 10.0     | 0.0  | 0.0       | 750.0        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "01 February 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable        |       | 250.0  |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 10.0   |
      | INCOME | 404000       | Interest Income         | 260.0 |        |

  @TestRailId:C3142
  Scenario: Verify Interest Payment Waiver transaction - UC2: LP1 product, cumulative schedule, flat interest, allocation: fee-interest-principal, interestPaymentWaiver on due date with partial amount
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP1_INTEREST_FLAT | 01 January 2024   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "15 January 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 January 2024" due date and 20 EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "01 February 2024" with 40 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 750.0           | 250.0         | 10.0     | 20.0 | 0.0       | 280.0 | 40.0 | 0.0        | 0.0  | 240.0       |
      | 2  | 29   | 01 March 2024    |           | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 3  | 31   | 01 April 2024    |           | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 40.0     | 20.0 | 0.0       | 1060.0 | 40.0 | 0.0        | 0.0  | 1020.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2024 | Interest Payment Waiver | 40.0   | 10.0      | 10.0     | 20.0 | 0.0       | 990.0        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "01 February 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable        |       | 10.0   |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 30.0   |
      | INCOME | 404000       | Interest Income         | 40.0  |        |

  @TestRailId:C3143
  Scenario: Verify Interest Payment Waiver transaction - UC3: LP1 product, cumulative schedule, flat interest, allocation: principal-interest, interestPaymentWaiver on due date with partial amount
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 01 January 2024   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | PRINCIPAL_INTEREST_PENALTIES_FEES_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "01 February 2024" with 10 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 10.0 | 0.0        | 0.0  | 250.0       |
      | 2  | 29   | 01 March 2024    |           | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 3  | 31   | 01 April 2024    |           | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 10.0 | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2024 | Interest Payment Waiver | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 990.0        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "01 February 2024" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 10.0   |
      | INCOME | 404000       | Interest Income  | 10.0  |        |

  @TestRailId:C3144
  Scenario: Verify Interest Payment Waiver transaction - UC4: LP1 product, cumulative schedule, flat interest, allocation: principal-interest, interestPaymentWaiver after maturity date, overpayment
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 01 January 2024   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | PRINCIPAL_INTEREST_PENALTIES_FEES_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 260 EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 260 EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 260 EUR transaction amount
    When Admin sets the business date to "01 May 2024"
    And Customer makes "AUTOPAY" repayment on "01 May 2024" with 250 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 10 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      |                  | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 250.0 | 0.0        | 0.0  | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 1030.0 | 0.0        | 0.0  | 10.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2024 | Repayment        | 260.0  | 250.0     | 10.0     | 0.0  | 0.0       | 750.0        |
      | 01 March 2024    | Repayment        | 260.0  | 250.0     | 10.0     | 0.0  | 0.0       | 500.0        |
      | 01 April 2024    | Repayment        | 260.0  | 250.0     | 10.0     | 0.0  | 0.0       | 250.0        |
      | 01 May 2024      | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          |
#   ---Overpay loan with Interest payment waiver ---
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "01 May 2024" with 20 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 10 overpaid amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 May 2024      | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 1040.0 | 0.0        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2024 | Repayment               | 260.0  | 250.0     | 10.0     | 0.0  | 0.0       | 750.0        |
      | 01 March 2024    | Repayment               | 260.0  | 250.0     | 10.0     | 0.0  | 0.0       | 500.0        |
      | 01 April 2024    | Repayment               | 260.0  | 250.0     | 10.0     | 0.0  | 0.0       | 250.0        |
      | 01 May 2024      | Repayment               | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 01 May 2024      | Interest Payment Waiver | 20.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          |
      | 01 May 2024      | Accrual                 | 40.0   | 0.0       | 40.0     | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 260.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 260.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 April 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 260.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 May 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "01 May 2024" which has the following Journal entries:
      | Type      | Account code | Account name            | Debit | Credit |
      | ASSET     | 112603       | Interest/Fee Receivable |       | 10.0   |
      | LIABILITY | l1           | Overpayment account     |       | 10.0   |
      | INCOME    | 404000       | Interest Income         | 20.0  |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 May 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 40.0  |        |
      | INCOME | 404000       | Interest Income         |       | 40.0   |

  @TestRailId:C3145
  Scenario: Verify Interest Payment Waiver transaction - UC5: LP1 product, cumulative schedule, flat interest, allocation: interest-principal, interestPaymentWaiver before due date, repayment on due date, undo interestPaymentWaiver
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 01 January 2024   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
#    --- Interest Payment Waiver before due date with partial amount  ---
    When Admin sets the business date to "15 January 2024"
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "15 January 2024" with 10 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 10.0 | 10.0       | 0.0  | 250.0       |
      | 2  | 29   | 01 March 2024    |           | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 3  | 31   | 01 April 2024    |           | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 10.0 | 10.0       | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 January 2024  | Interest Payment Waiver | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 1000.0       |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "15 January 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 10.0   |
      | INCOME | 404000       | Interest Income         | 10.0  |        |
#   --- Repayment on due date for the remaining amount ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 10.0       | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 3  | 31   | 01 April 2024    |                  | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 4  | 30   | 01 May 2024      |                  | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 260.0 | 10.0       | 0.0  | 780.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 January 2024  | Interest Payment Waiver | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 1000.0       |
      | 01 February 2024 | Repayment               | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "15 January 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 10.0   |
      | INCOME | 404000       | Interest Income         | 10.0  |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
#   --- Interest Payment waiver revert ---
    When Admin sets the business date to "02 February 2024"
    When Customer undo "1"th "Interest Payment Waiver" transaction made on "15 January 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 250.0 | 0.0        | 0.0  | 10.0        |
      | 2  | 29   | 01 March 2024    |           | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 3  | 31   | 01 April 2024    |           | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 250.0 | 0.0        | 0.0  | 790.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2024  | Interest Payment Waiver | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 1000.0       | true     | false    |
      | 01 February 2024 | Repayment               | 250.0  | 240.0     | 10.0     | 0.0  | 0.0       | 760.0        | false    | true     |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "15 January 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 10.0   |
      | INCOME | 404000       | Interest Income         | 10.0  |        |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404000       | Interest Income         |       | 10.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 240.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |

  @TestRailId:C3146
  Scenario: Verify Interest Payment Waiver transaction - UC6: LP1 product, cumulative schedule, flat interest, allocation: interest-principal, interestPaymentWaiver before due date, repayment on due date, chergeback for interestPaymentWaiver
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 01 January 2024   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
#    --- Interest Payment Waiver before due date with partial amount  ---
    When Admin sets the business date to "15 January 2024"
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "15 January 2024" with 10 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 10.0 | 10.0       | 0.0  | 250.0       |
      | 2  | 29   | 01 March 2024    |           | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 3  | 31   | 01 April 2024    |           | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 10.0 | 10.0       | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 January 2024  | Interest Payment Waiver | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 1000.0       |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "15 January 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 10.0   |
      | INCOME | 404000       | Interest Income         | 10.0  |        |
#   --- Repayment on due date for the remaining amount ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 10.0       | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 3  | 31   | 01 April 2024    |                  | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 4  | 30   | 01 May 2024      |                  | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 260.0 | 10.0       | 0.0  | 780.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 January 2024  | Interest Payment Waiver | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 1000.0       |
      | 01 February 2024 | Repayment               | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "15 January 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 10.0   |
      | INCOME | 404000       | Interest Income         | 10.0  |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
#   --- Chargeback for Interest Payment waiver ---
    And Customer makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" repayment on "01 February 2024" with 10 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 10.0       | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 10.0  | 10.0       | 0.0  | 250.0       |
      | 3  | 31   | 01 April 2024    |                  | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 4  | 30   | 01 May 2024      |                  | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 270.0 | 20.0       | 0.0  | 770.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2024  | Interest Payment Waiver | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2024 | Repayment               | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 01 February 2024 | Repayment               | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 750.0        | false    | false    |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "15 January 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 10.0   |
      | INCOME | 404000       | Interest Income         | 10.0  |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |

  @TestRailId:C3147
  Scenario: Verify Interest Payment Waiver transaction - UC7: LP1 product, cumulative schedule, flat interest, allocation: interest-principal, interestPaymentWaiver puts loan in overpaid status when transaction amount is greater than balance
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 01 January 2024   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "01 February 2024" with 1100 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 60 overpaid amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 February 2024 | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 260.0      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 February 2024 | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 260.0      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 01 February 2024 | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 260.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 1040.0 | 780.0      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2024 | Interest Payment Waiver | 1100.0 | 1000.0    | 40.0     | 0.0  | 0.0       | 0.0          |
      | 01 February 2024 | Accrual                 | 40.0   | 0.0       | 40.0     | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name            | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable        |        | 1000.0 |
      | ASSET     | 112603       | Interest/Fee Receivable |        | 40.0   |
      | LIABILITY | l1           | Overpayment account     |        | 60.0   |
      | INCOME    | 404000       | Interest Income         | 1100.0 |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 February 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 40.0  |        |
      | INCOME | 404000       | Interest Income         |       | 40.0   |

  @TestRailId:C3148
  Scenario: Verify Interest Payment Waiver transaction - UC8: LP1 product, cumulative schedule, flat interest, allocation: interest-principal, interestPaymentWaiver puts loan from closed to overpaid status
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 01 January 2024   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 1040 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "01 February 2024"
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "01 February 2024" with 100 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 100 overpaid amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 260.0      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 January 2024 | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 260.0      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 15 January 2024 | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 260.0      | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2024      | 15 January 2024 | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 260.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 1040.0 | 1040.0     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 January 2024  | Repayment               | 1040.0 | 1000.0    | 40.0     | 0.0  | 0.0       | 0.0          |
      | 15 January 2024  | Accrual                 | 40.0   | 0.0       | 40.0     | 0.0  | 0.0       | 0.0          |
      | 01 February 2024 | Interest Payment Waiver | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | ASSET     | 112603       | Interest/Fee Receivable   |        | 40.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1040.0 |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "15 January 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 40.0  |        |
      | INCOME | 404000       | Interest Income         |       | 40.0   |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name        | Debit | Credit |
      | LIABILITY | l1           | Overpayment account |       | 100.0  |
      | INCOME    | 404000       | Interest Income     | 100.0 |        |

  @TestRailId:C3149
  Scenario: Verify Interest Payment Waiver transaction - UC9: LP1 product, cumulative schedule, flat interest, allocation: interest-principal, loan overdue calculation is updated upon interestPaymentWaiver transaction
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 01 January 2024   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "01 June 2024"
    When Admin runs inline COB job for Loan
    Then Admin checks that delinquency range is: "RANGE_90" and has delinquentDate "2024-02-04"
    Then Loan status will be "ACTIVE"
    Then Loan has 1040 outstanding amount
    When Admin sets the business date to "02 June 2024"
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "02 June 2024" with 1040 EUR transaction amount
    Then Admin checks that delinquency range is: "NO_DELINQUENCY" and has delinquentDate ""
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |              | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 02 June 2024 | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 260.0 | 0.0         |
      | 2  | 29   | 01 March 2024    | 02 June 2024 | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 260.0 | 0.0         |
      | 3  | 31   | 01 April 2024    | 02 June 2024 | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 260.0 | 0.0         |
      | 4  | 30   | 01 May 2024      | 02 June 2024 | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 260.0 | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 1040.0 | 0.0        | 1040.0 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2024 | Accrual                 | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          |
      | 01 March 2024    | Accrual                 | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          |
      | 01 April 2024    | Accrual                 | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          |
      | 01 May 2024      | Accrual                 | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          |
      | 02 June 2024     | Interest Payment Waiver | 1040.0 | 1000.0    | 40.0     | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 February 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404000       | Interest Income         |       | 10.0   |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 March 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404000       | Interest Income         |       | 10.0   |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 April 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404000       | Interest Income         |       | 10.0   |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 May 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404000       | Interest Income         |       | 10.0   |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "02 June 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit  | Credit |
      | ASSET  | 112601       | Loans Receivable        |        | 1000.0 |
      | ASSET  | 112603       | Interest/Fee Receivable |        | 40.0   |
      | INCOME | 404000       | Interest Income         | 1040.0 |        |

  @TestRailId:C3150
  Scenario: Verify Interest Payment Waiver transaction - UC10: LP1 product, cumulative schedule, flat interest, allocation: interest-principal, interestPaymentWaiver and charge-off
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 01 January 2024   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "01 February 2024"
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "01 February 2024" with 260 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 3  | 31   | 01 April 2024    |                  | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 4  | 30   | 01 May 2024      |                  | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 260.0 | 0.0        | 0.0  | 780.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2024 | Interest Payment Waiver | 260.0  | 250.0     | 10.0     | 0.0  | 0.0       | 750.0        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "01 February 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable        |       | 250.0  |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 10.0   |
      | INCOME | 404000       | Interest Income         | 260.0 |        |
    When Admin sets the business date to "02 February 2024"
    And Admin does charge-off the loan on "02 February 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 3  | 31   | 01 April 2024    |                  | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 4  | 30   | 01 May 2024      |                  | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 260.0 | 0.0        | 0.0  | 780.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 February 2024 | Interest Payment Waiver | 260.0  | 250.0     | 10.0     | 0.0  | 0.0       | 750.0        |
      | 02 February 2024 | Accrual                 | 10.34  | 0.0       | 10.34    | 0.0  | 0.0       | 0.0          |
      | 02 February 2024 | Charge-off              | 780.0  | 750.0     | 30.0     | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "01 February 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable        |       | 250.0  |
      | ASSET  | 112603       | Interest/Fee Receivable |       | 10.0   |
      | INCOME | 404000       | Interest Income         | 260.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "02 February 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 750.0  |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 30.0   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 750.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 30.0  |        |

  @TestRailId:C3151
  Scenario: Verify Interest Payment Waiver transaction - UC11: LP1 product, cumulative schedule, flat interest, allocation: interest-principal, interestPaymentWaiver after charge-off
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 01 January 2024   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "15 January 2024"
    And Admin does charge-off the loan on "15 January 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 2  | 29   | 01 March 2024    |           | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 3  | 31   | 01 April 2024    |           | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 0.0  | 0.0        | 0.0  | 1040.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 January 2024  | Accrual          | 4.52   | 0.0       | 4.52     | 0.0  | 0.0       | 0.0          |
      | 15 January 2024  | Charge-off       | 1040.0 | 1000.0    | 40.0     | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 40.0   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 1000.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 40.0   |        |
    When Admin sets the business date to "01 February 2024"
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "01 February 2024" with 260 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 750.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 3  | 31   | 01 April 2024    |                  | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
      | 4  | 30   | 01 May 2024      |                  | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0   | 0.0        | 0.0  | 260.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000          | 40.0     | 0.0  | 0.0       | 1040.0 | 260.0 | 0.0        | 0.0  | 780.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 January 2024  | Accrual                 | 4.52   | 0.0       | 4.52     | 0.0  | 0.0       | 0.0          |
      | 15 January 2024  | Charge-off              | 1040.0 | 1000.0    | 40.0     | 0.0  | 0.0       | 0.0          |
      | 01 February 2024 | Interest Payment Waiver | 260.0  | 250.0     | 10.0     | 0.0  | 0.0       | 750.0        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "15 January 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 40.0   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 1000.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 40.0   |        |
    Then Loan Transactions tab has a "INTEREST_PAYMENT_WAIVER" transaction with date "01 February 2024" which has the following Journal entries:
      | Type   | Account code | Account name               | Debit | Credit |
      | INCOME | 404001       | Interest Income Charge Off |       | 260.0  |
      | INCOME | 404000       | Interest Income            | 260.0 |        |
