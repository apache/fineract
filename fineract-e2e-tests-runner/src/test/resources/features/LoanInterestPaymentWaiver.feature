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

  @TestRailId:C4200
  Scenario: Verify Interest Payment Waiver transaction - UC12: IPW after Charge-off - UC1
    When Admin sets the business date to "23 October 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                   | submitted on date | with Principal   | ANNUAL interest rate %     | interest type              | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType  | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_ZERO_CHARGE_OFF | 25 October 2021   | 678.03           | 9.5129                     | DECLINING_BALANCE          | DAILY                       | EQUAL_INSTALLMENTS | 24                 | MONTHS                | 1              | MONTHS                 | 24                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "25 October 2021" with "678.03" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "25 October 2021" with "678.03" EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "29 October 2021" with 10 EUR transaction amount and self-generated Idempotency key
    And Customer makes "AUTOPAY" repayment on "26 August 2022" with 186.84 EUR transaction amount
    And Admin does charge-off the loan on "24 September 2022"
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "24 September 2022" with 46.56 EUR transaction amount and self-generated external-id
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance  | Late  | Outstanding |
      |    |      | 25 October 2021   |                   | 678.03          |               |          | 0.0  |           | 0.0   | 0.0   |             |       |             |
      | 1  | 31   | 25 November 2021  | 26 August 2022    | 652.2           | 25.83         | 5.31     | 0.0  | 0.0       | 31.14 | 31.14 |  0.01       | 31.13 | 0.0         |
      | 2  | 30   | 25 December 2021  | 26 August 2022    | 626.36          | 25.84         | 5.3      | 0.0  | 0.0       | 31.14 | 31.14 |  0.0        | 31.14 | 0.0         |
      | 3  | 31   | 25 January 2022   | 26 August 2022    | 600.52          | 25.84         | 5.3      | 0.0  | 0.0       | 31.14 | 31.14 |  0.0        | 31.14 | 0.0         |
      | 4  | 31   | 25 February 2022  | 26 August 2022    | 574.68          | 25.84         | 5.3      | 0.0  | 0.0       | 31.14 | 31.14 |  0.0        | 31.14 | 0.0         |
      | 5  | 28   | 25 March 2022     | 26 August 2022    | 548.84          | 25.84         | 5.3      | 0.0  | 0.0       | 31.14 | 31.14 |  0.0        | 31.14 | 0.0         |
      | 6  | 31   | 25 April 2022     | 26 August 2022    | 523.0           | 25.84         | 5.3      | 0.0  | 0.0       | 31.14 | 31.14 |  0.0        | 31.14 | 0.0         |
      | 7  | 30   | 25 May 2022       | 24 September 2022 | 497.16          | 25.84         | 5.3      | 0.0  | 0.0       | 31.14 | 31.14 |  0.0        | 31.14 | 0.0         |
      | 8  | 31   | 25 June 2022      |                   | 471.32          | 25.84         | 5.3      | 0.0  | 0.0       | 31.14 | 15.43 |  0.0        | 15.43 | 15.71       |
      | 9  | 30   | 25 July 2022      |                   | 445.48          | 25.84         | 5.3      | 0.0  | 0.0       | 31.14 |  0.0  |  0.0        | 0.0   | 31.14       |
      | 10 | 31   | 25 August 2022    |                   | 419.64          | 25.84         | 5.3      | 0.0  | 0.0       | 31.14 |  0.0  |  0.0        | 0.0   | 31.14       |
      | 11 | 31   | 25 September 2022 |                   | 392.48          | 27.16         | 3.98     | 0.0  | 0.0       | 31.14 |  0.0  |  0.0        | 0.0   | 31.14       |
      | 12 | 30   | 25 October 2022   |                   | 361.34          | 31.14         | 0.0      | 0.0  | 0.0       | 31.14 |  0.0  |  0.0        | 0.0   | 31.14       |
      | 13 | 31   | 25 November 2022  |                   | 330.2           | 31.14         | 0.0      | 0.0  | 0.0       | 31.14 |  0.0  |  0.0        | 0.0   | 31.14       |
      | 14 | 30   | 25 December 2022  |                   | 299.06          | 31.14         | 0.0      | 0.0  | 0.0       | 31.14 |  0.0  |  0.0        | 0.0   | 31.14       |
      | 15 | 31   | 25 January 2023   |                   | 267.92          | 31.14         | 0.0      | 0.0  | 0.0       | 31.14 |  0.0  |  0.0        | 0.0   | 31.14       |
      | 16 | 31   | 25 February 2023  |                   | 236.78          | 31.14         | 0.0      | 0.0  | 0.0       | 31.14 |  0.0  |  0.0        | 0.0   | 31.14       |
      | 17 | 28   | 25 March 2023     |                   | 205.64          | 31.14         | 0.0      | 0.0  | 0.0       | 31.14 |  0.0  |  0.0        | 0.0   | 31.14       |
      | 18 | 31   | 25 April 2023     |                   | 174.5           | 31.14         | 0.0      | 0.0  | 0.0       | 31.14 |  0.0  |  0.0        | 0.0   | 31.14       |
      | 19 | 30   | 25 May 2023       |                   | 143.36          | 31.14         | 0.0      | 0.0  | 0.0       | 31.14 |  0.0  |  0.0        | 0.0   | 31.14       |
      | 20 | 31   | 25 June 2023      |                   | 112.22          | 31.14         | 0.0      | 0.0  | 0.0       | 31.14 |  0.0  |  0.0        | 0.0   | 31.14       |
      | 21 | 30   | 25 July 2023      |                   |  81.08          | 31.14         | 0.0      | 0.0  | 0.0       | 31.14 |  0.0  |  0.0        | 0.0   | 31.14       |
      | 22 | 31   | 25 August 2023    |                   | 49.94           | 31.14         | 0.0      | 0.0  | 0.0       | 31.14 |  0.0  |  0.0        | 0.0   | 31.14       |
      | 23 | 31   | 25 September 2023 |                   | 18.8            | 31.14         | 0.0      | 0.0  | 0.0       | 31.14 |  0.0  |  0.0        | 0.0   | 31.14       |
      | 24 | 30   | 25 October 2023   |                   | 0.0             | 18.8          | 0.0      | 0.0  | 0.0       | 18.8  | 10.0  | 10.0        | 0.0   | 8.8         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late    | Outstanding |
      | 678.03        | 56.99    | 0.0  | 0.0       | 735.02 | 243.41 | 10.01      | 233.40  | 491.61      |
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance |
      | 25 October 2021    | Disbursement            | 678.03  |   0.0      |  0.0     | 0.0  | 0.0       | 678.03       |
      | 29 October 2021    | Merchant Issued Refund  | 10.0    |  10.0      |  0.0     | 0.0  | 0.0       | 668.03       |
      | 29 October 2021    | Interest Refund         |  0.01   |   0.0      |  0.01    | 0.0  | 0.0       | 668.03       |
      | 26 August 2022     | Repayment               | 186.84  | 155.03     | 31.81    | 0.0  | 0.0       | 513.0        |
      | 24 September 2022  | Accrual                 |  56.99  |   0.0      | 56.99    | 0.0  | 0.0       |   0.0        |
      | 24 September 2022  | Charge-off              | 538.17  | 513.0      | 25.17    | 0.0  | 0.0       |   0.0        |
      | 24 September 2022  | Interest Payment Waiver |  46.56  |  35.97     | 10.59    | 0.0  | 0.0       | 477.03       |
    Then In Loan Transactions all transactions have non-null external-id
    And Customer makes "AUTOPAY" repayment on "24 September 2022" with 491.61 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4204
  Scenario: Verify Interest Payment Waiver transaction - UC12: IPW after Charge-off - UC2
    When Admin sets the business date to "23 October 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                     | submitted on date | with Principal   | ANNUAL interest rate %     | interest type              | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType  | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP2_ADV_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALC_ZERO_CHARGE_OF_ACCRUAL | 18 January 2022   | 431.98           | 9.99                       | DECLINING_BALANCE          | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                  | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "18 January 2022" with "431.98" amount and expected disbursement date on "18 January 2022"
    And Admin successfully disburse the loan on "18 January 2022" with "431.98" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance  | Late  | Outstanding |
      |    |      | 18 January 2022   |                   | 431.98          |               |          | 0.0  |           | 0.0   |  0.0  |             |       |             |
      | 1  | 31   | 18 February 2022  |                   | 397.68          | 34.3          | 3.67     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 2  | 28   | 18 March 2022     |                   | 363.02          | 34.66         | 3.31     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 3  | 31   | 18 April 2022     |                   | 328.72          | 34.3          | 3.67     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 4  | 30   | 18 May 2022       |                   | 294.3           | 34.42         | 3.55     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 5  | 31   | 18 June 2022      |                   | 260.0           | 34.3          | 3.67     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 6  | 30   | 18 July 2022      |                   | 225.58          | 34.42         | 3.55     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 7  | 31   | 18 August 2022    |                   | 191.28          | 34.3          | 3.67     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 8  | 31   | 18 September 2022 |                   | 156.98          | 34.3          | 3.67     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 9  | 30   | 18 October 2022   |                   | 122.56          | 34.42         | 3.55     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 10 | 31   | 18 November 2022  |                   |  88.26          | 34.3          | 3.67     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 11 | 30   | 18 December 2022  |                   |  53.84          | 34.42         | 3.55     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 12 | 31   | 18 January 2023   |                   |   0.0           | 53.84         | 3.67     | 0.0  | 0.0       | 57.51 |  0.0  |  0.0        | 0.0   | 57.51       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late    | Outstanding |
      | 431.98        | 43.2     | 0.0  | 0.0       | 475.18 | 0.0    | 0.0        | 0.0     | 475.18      |
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       |
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "20 January 2022" with 349.99 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance  | Late  | Outstanding |
      |    |      | 18 January 2022   |                   | 431.98          |               |          | 0.0  |           | 0.0   |  0.0  |             |       |             |
      | 1  | 31   | 18 February 2022  | 20 January 2022   | 394.25          | 37.73         | 0.24     | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 2  | 28   | 18 March 2022     | 20 January 2022   | 356.28          | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 3  | 31   | 18 April 2022     | 20 January 2022   | 318.31          | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 4  | 30   | 18 May 2022       | 20 January 2022   | 280.34          | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 5  | 31   | 18 June 2022      | 20 January 2022   | 242.37          | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 6  | 30   | 18 July 2022      | 20 January 2022   | 204.4           | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 7  | 31   | 18 August 2022    | 20 January 2022   | 166.43          | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 8  | 31   | 18 September 2022 | 20 January 2022   | 128.46          | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 9  | 30   | 18 October 2022   | 20 January 2022   |  90.49          | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 10 | 31   | 18 November 2022  |                   |  59.31          | 31.18         | 6.79     | 0.0  | 0.0       | 37.97 |  8.46 |  8.46       | 0.0   | 29.51       |
      | 11 | 30   | 18 December 2022  |                   |  22.01          | 37.3          | 0.67     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 12 | 31   | 18 January 2023   |                   |   0.0           | 22.01         | 0.7      | 0.0  | 0.0       | 22.71 |  0.0  |  0.0        | 0.0   | 22.71       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late    | Outstanding |
      | 431.98        | 8.4      | 0.0  | 0.0       | 440.38 | 350.19 | 350.19     | 0.0     | 90.19       |
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "18 February 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 February 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | true     | false    |
    And Customer makes "AUTOPAY" repayment on "28 February 2022" with 19.83 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | false    | false    |
    And Customer makes "AUTOPAY" repayment on "18 March 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 March 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  18.65     |  1.18    | 0.0  | 0.0       |  43.55       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "31 March 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "31 March 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  18.65     |  1.18    | 0.0  | 0.0       |  43.55       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  18.43     |  1.4     | 0.0  | 0.0       |  43.77       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "18 April 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 April 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  18.65     |  1.18    | 0.0  | 0.0       |  43.55       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  18.43     |  1.4     | 0.0  | 0.0       |  43.77       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.12     |  1.71    | 0.0  | 0.0       |  44.08       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "18 May 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 May 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  18.65     |  1.18    | 0.0  | 0.0       |  43.55       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  18.43     |  1.4     | 0.0  | 0.0       |  43.77       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.12     |  1.71    | 0.0  | 0.0       |  44.08       | true     | false    |
      | 18 May 2022        | Repayment               | 19.83   |  17.61     |  2.22    | 0.0  | 0.0       |  44.59       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "18 June 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 June 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  18.65     |  1.18    | 0.0  | 0.0       |  43.55       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  18.43     |  1.4     | 0.0  | 0.0       |  43.77       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.12     |  1.71    | 0.0  | 0.0       |  44.08       | true     | false    |
      | 18 May 2022        | Repayment               | 19.83   |  17.61     |  2.22    | 0.0  | 0.0       |  44.59       | true     | false    |
      | 18 June 2022       | Repayment               | 19.83   |  17.08     |  2.75    | 0.0  | 0.0       |  45.12       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "18 July 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 July 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  18.65     |  1.18    | 0.0  | 0.0       |  43.55       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  18.43     |  1.4     | 0.0  | 0.0       |  43.77       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.12     |  1.71    | 0.0  | 0.0       |  44.08       | true     | false    |
      | 18 May 2022        | Repayment               | 19.83   |  17.61     |  2.22    | 0.0  | 0.0       |  44.59       | true     | false    |
      | 18 June 2022       | Repayment               | 19.83   |  17.08     |  2.75    | 0.0  | 0.0       |  45.12       | true     | false    |
      | 18 July 2022       | Repayment               | 19.83   |  16.57     |  3.26    | 0.0  | 0.0       |  45.63       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "18 August 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 August 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  18.65     |  1.18    | 0.0  | 0.0       |  43.55       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  18.43     |  1.4     | 0.0  | 0.0       |  43.77       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.12     |  1.71    | 0.0  | 0.0       |  44.08       | true     | false    |
      | 18 May 2022        | Repayment               | 19.83   |  17.61     |  2.22    | 0.0  | 0.0       |  44.59       | true     | false    |
      | 18 June 2022       | Repayment               | 19.83   |  17.08     |  2.75    | 0.0  | 0.0       |  45.12       | true     | false    |
      | 18 July 2022       | Repayment               | 19.83   |  16.57     |  3.26    | 0.0  | 0.0       |  45.63       | true     | false    |
      | 18 August 2022     | Repayment               | 19.83   |  16.04     |  3.79    | 0.0  | 0.0       |  46.16       | true     | false    |
    And Admin does charge-off the loan on "16 September 2022"
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "16 September 2022" with 46.56 EUR transaction amount and self-generated external-id
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance  | Late  | Outstanding |
      |    |      | 18 January 2022   |                   | 431.98          |               |          | 0.0  |           | 0.0   |  0.0  |             |       |             |
      | 1  | 31   | 18 February 2022  | 20 January 2022   | 394.25          | 37.73         | 0.24     | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 2  | 28   | 18 March 2022     | 20 January 2022   | 356.28          | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 3  | 31   | 18 April 2022     | 20 January 2022   | 318.31          | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 4  | 30   | 18 May 2022       | 20 January 2022   | 280.34          | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 5  | 31   | 18 June 2022      | 20 January 2022   | 242.37          | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 6  | 30   | 18 July 2022      | 20 January 2022   | 204.4           | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 7  | 31   | 18 August 2022    | 20 January 2022   | 166.43          | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 8  | 31   | 18 September 2022 | 20 January 2022   | 132.74          | 33.69         | 4.28     | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 9  | 30   | 18 October 2022   | 20 January 2022   |  94.77          | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 10 | 31   | 18 November 2022  | 16 September 2022 |  56.8           | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 11 | 30   | 18 December 2022  |                   |  18.83          | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 36.88 | 36.88       | 0.0   | 1.09        |
      | 12 | 31   | 18 January 2023   |                   |   0.0           | 18.83         | 0.0      | 0.0  | 0.0       | 18.83 |  0.0  |  0.0        | 0.0   | 18.83       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late    | Outstanding |
      | 431.98        | 4.52     | 0.0  | 0.0       | 436.5  | 416.58 | 416.58     | 0.0     | 19.92       |
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  19.83     |  0.0     | 0.0  | 0.0       |  62.2        | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  18.65     |  1.18    | 0.0  | 0.0       |  43.55       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  18.43     |  1.4     | 0.0  | 0.0       |  43.77       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.12     |  1.71    | 0.0  | 0.0       |  44.08       | true     | false    |
      | 18 May 2022        | Repayment               | 19.83   |  17.61     |  2.22    | 0.0  | 0.0       |  44.59       | true     | false    |
      | 18 June 2022       | Repayment               | 19.83   |  17.08     |  2.75    | 0.0  | 0.0       |  45.12       | true     | false    |
      | 18 July 2022       | Repayment               | 19.83   |  16.57     |  3.26    | 0.0  | 0.0       |  45.63       | true     | false    |
      | 18 August 2022     | Repayment               | 19.83   |  16.04     |  3.79    | 0.0  | 0.0       |  46.16       | true     | false    |
      | 16 September 2022  | Accrual                 | 4.52    |   0.0      |  4.52    | 0.0  | 0.0       |   0.0        | false    | false    |
      | 16 September 2022  | Charge-off              | 66.48   |  62.2      |  4.28    | 0.0  | 0.0       |   0.0        | false    | false    |
      | 16 September 2022  | Interest Payment Waiver | 46.56   |  46.56     |  0.0     | 0.0  | 0.0       |  15.64       | false    | false    |
    Then In Loan Transactions all transactions have non-null external-id
    And Customer makes "AUTOPAY" repayment on "16 September 2022" with 19.92 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4205
  Scenario: Verify Interest Payment Waiver transaction - UC12: Payout Refund after Charge-off - UC3
    When Admin sets the business date to "23 October 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                     | submitted on date | with Principal   | ANNUAL interest rate %     | interest type              | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType  | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP2_ADV_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALC_ZERO_CHARGE_OF_ACCRUAL | 18 January 2022   | 431.98           | 9.99                       | DECLINING_BALANCE          | DAILY                       | EQUAL_INSTALLMENTS | 24                | MONTHS                | 1              | MONTHS                  | 24                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "18 January 2022" with "431.98" amount and expected disbursement date on "18 January 2022"
    And Admin successfully disburse the loan on "18 January 2022" with "431.98" EUR transaction amount
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance  | Late  | Outstanding |
      |    |      | 18 January 2022   |                   | 431.98          |               |          | 0.0  |           | 0.0   |  0.0  |             |       |             |
      | 1  | 31   | 18 February 2022  |                   | 415.72          | 16.26         | 3.67     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 2  | 28   | 18 March 2022     |                   | 399.1           | 16.62         | 3.31     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 3  | 31   | 18 April 2022     |                   | 382.84          | 16.26         | 3.67     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 4  | 30   | 18 May 2022       |                   | 366.46          | 16.38         | 3.55     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 5  | 31   | 18 June 2022      |                   | 350.2           | 16.26         | 3.67     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 6  | 30   | 18 July 2022      |                   | 333.82          | 16.38         | 3.55     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 7  | 31   | 18 August 2022    |                   | 317.56          | 16.26         | 3.67     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 8  | 31   | 18 September 2022 |                   | 301.3           | 16.26         | 3.67     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 9  | 30   | 18 October 2022   |                   | 284.92          | 16.38         | 3.55     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 10 | 31   | 18 November 2022  |                   | 268.66          | 16.26         | 3.67     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 11 | 30   | 18 December 2022  |                   | 252.28          | 16.38         | 3.55     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 12 | 31   | 18 January 2023   |                   | 236.02          | 16.26         | 3.67     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 13 | 31   | 18 February 2023  |                   | 219.76          | 16.26         | 3.67     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 14 | 28   | 18 March 2023     |                   | 203.14          | 16.62         | 3.31     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 15 | 31   | 18 April 2023     |                   | 186.88          | 16.26         | 3.67     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 16 | 30   | 18 May 2023       |                   | 170.5           | 16.38         | 3.55     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 17 | 31   | 18 June 2023      |                   | 154.24          | 16.26         | 3.67     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 18 | 30   | 18 July 2023      |                   | 137.86          | 16.38         | 3.55     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 19 | 31   | 18 August 2023    |                   | 121.6           | 16.26         | 3.67     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 20 | 31   | 18 September 2023 |                   | 105.34          | 16.26         | 3.67     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 21 | 30   | 18 October 2023   |                   |  88.96          | 16.38         | 3.55     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 22 | 31   | 18 November 2023  |                   |  72.7           | 16.26         | 3.67     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 23 | 30   | 18 December 2023  |                   |  56.32          | 16.38         | 3.55     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 24 | 31   | 18 January 2024   |                   |   0.0           | 56.32         | 3.67     | 0.0  | 0.0       | 59.99 |  0.0  |  0.0        | 0.0   | 59.99       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late    | Outstanding |
      | 431.98        | 86.4     | 0.0  | 0.0       | 518.38 | 0.0    | 0.0        | 0.0     | 518.38      |
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       |
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "20 January 2022" with 349.99 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance  | Late  | Outstanding |
      |    |      | 18 January 2022   |                   | 431.98          |               |          | 0.0  |           | 0.0   |  0.0  |             |       |             |
      | 1  | 31   | 18 February 2022  | 20 January 2022   | 412.29          | 19.69         | 0.24     | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 2  | 28   | 18 March 2022     | 20 January 2022   | 392.36          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 3  | 31   | 18 April 2022     | 20 January 2022   | 372.43          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 4  | 30   | 18 May 2022       | 20 January 2022   | 352.5           | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 5  | 31   | 18 June 2022      | 20 January 2022   | 332.57          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 6  | 30   | 18 July 2022      | 20 January 2022   | 312.64          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 7  | 31   | 18 August 2022    | 20 January 2022   | 292.71          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 8  | 31   | 18 September 2022 | 20 January 2022   | 272.78          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 9  | 30   | 18 October 2022   | 20 January 2022   | 252.85          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 10 | 31   | 18 November 2022  | 20 January 2022   | 232.92          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 11 | 30   | 18 December 2022  | 20 January 2022   | 212.99          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 12 | 31   | 18 January 2023   | 20 January 2022   | 193.06          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 13 | 31   | 18 February 2023  | 20 January 2022   | 173.13          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 14 | 28   | 18 March 2023     | 20 January 2022   | 153.2           | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 15 | 31   | 18 April 2023     | 20 January 2022   | 133.27          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 16 | 30   | 18 May 2023       | 20 January 2022   | 113.34          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 17 | 31   | 18 June 2023      | 20 January 2022   |  93.41          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 18 | 30   | 18 July 2023      |                   |  73.48          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 11.38 | 11.38       | 0.0   |  8.55       |
      | 19 | 31   | 18 August 2023    |                   |  66.48          |  7.0          | 12.93    | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 20 | 31   | 18 September 2023 |                   |  47.25          | 19.23         | 0.7      | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 21 | 30   | 18 October 2023   |                   |  27.99          | 19.26         | 0.67     | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 22 | 31   | 18 November 2023  |                   |   8.76          | 19.23         | 0.7      | 0.0  | 0.0       | 19.93 |  0.0  |  0.0        | 0.0   | 19.93       |
      | 23 | 30   | 18 December 2023  |                   |   0.0           | 8.76          | 0.67     | 0.0  | 0.0       |  9.43 |  0.0  |  0.0        | 0.0   |  9.43       |
      | 24 | 31   | 18 January 2024   | 20 January 2022   |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0  |  0.0  |  0.0        | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late    | Outstanding |
      | 431.98        | 15.91    | 0.0  | 0.0       | 447.89 | 350.19 | 350.19     | 0.0     | 97.7        |
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "18 February 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 February 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.18     |  0.65    | 0.0  | 0.0       |  62.85       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "28 February 2022" with 19.83 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.18     |  0.65    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.96     |  0.87    | 0.0  | 0.0       |  63.07       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "18 March 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 March 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.18     |  0.65    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.96     |  0.87    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.51     |  0.32    | 0.0  | 0.0       |  43.56       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "31 March 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "31 March 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.18     |  0.65    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.96     |  0.87    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.51     |  0.32    | 0.0  | 0.0       |  43.56       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  19.29     |  0.54    | 0.0  | 0.0       |  43.78       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "18 April 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 April 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.18     |  0.65    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.96     |  0.87    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.51     |  0.32    | 0.0  | 0.0       |  43.56       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  19.29     |  0.54    | 0.0  | 0.0       |  43.78       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.97     |  0.86    | 0.0  | 0.0       |  44.1        | true     | false    |
    And Customer makes "AUTOPAY" repayment on "18 May 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 May 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.18     |  0.65    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.96     |  0.87    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.51     |  0.32    | 0.0  | 0.0       |  43.56       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  19.29     |  0.54    | 0.0  | 0.0       |  43.78       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.97     |  0.86    | 0.0  | 0.0       |  44.1        | true     | false    |
      | 18 May 2022        | Repayment               | 19.83   |  18.45     |  1.38    | 0.0  | 0.0       |  44.62       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "18 June 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 June 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.18     |  0.65    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.96     |  0.87    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.51     |  0.32    | 0.0  | 0.0       |  43.56       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  19.29     |  0.54    | 0.0  | 0.0       |  43.78       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.97     |  0.86    | 0.0  | 0.0       |  44.1        | true     | false    |
      | 18 May 2022        | Repayment               | 19.83   |  18.45     |  1.38    | 0.0  | 0.0       |  44.62       | true     | false    |
      | 18 June 2022       | Repayment               | 19.83   |  17.91     |  1.92    | 0.0  | 0.0       |  45.16       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "18 July 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 July 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.18     |  0.65    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.96     |  0.87    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.51     |  0.32    | 0.0  | 0.0       |  43.56       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  19.29     |  0.54    | 0.0  | 0.0       |  43.78       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.97     |  0.86    | 0.0  | 0.0       |  44.1        | true     | false    |
      | 18 May 2022        | Repayment               | 19.83   |  18.45     |  1.38    | 0.0  | 0.0       |  44.62       | true     | false    |
      | 18 June 2022       | Repayment               | 19.83   |  17.91     |  1.92    | 0.0  | 0.0       |  45.16       | true     | false    |
      | 18 July 2022       | Repayment               | 19.83   |  17.39     |  2.44    | 0.0  | 0.0       |  45.68       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "18 August 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 August 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.18     |  0.65    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.96     |  0.87    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.51     |  0.32    | 0.0  | 0.0       |  43.56       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  19.29     |  0.54    | 0.0  | 0.0       |  43.78       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.97     |  0.86    | 0.0  | 0.0       |  44.1        | true     | false    |
      | 18 May 2022        | Repayment               | 19.83   |  18.45     |  1.38    | 0.0  | 0.0       |  44.62       | true     | false    |
      | 18 June 2022       | Repayment               | 19.83   |  17.91     |  1.92    | 0.0  | 0.0       |  45.16       | true     | false    |
      | 18 July 2022       | Repayment               | 19.83   |  17.39     |  2.44    | 0.0  | 0.0       |  45.68       | true     | false    |
      | 18 August 2022     | Repayment               | 19.83   |  16.85     |  2.98    | 0.0  | 0.0       |  46.22       | true     | false    |
    And Admin does charge-off the loan on "16 September 2022"
    When Admin makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "16 September 2022" with 67.42 EUR transaction amount and self-generated external-id
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance  | Late  | Outstanding |
      |    |      | 18 January 2022   |                   | 431.98          |               |          | 0.0  |           | 0.0   |  0.0  |             |       |             |
      | 1  | 31   | 18 February 2022  | 20 January 2022   | 412.29          | 19.69         | 0.24     | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 2  | 28   | 18 March 2022     | 20 January 2022   | 392.36          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 3  | 31   | 18 April 2022     | 20 January 2022   | 372.43          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 4  | 30   | 18 May 2022       | 20 January 2022   | 352.5           | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 5  | 31   | 18 June 2022      | 20 January 2022   | 332.57          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 6  | 30   | 18 July 2022      | 20 January 2022   | 312.64          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 7  | 31   | 18 August 2022    | 20 January 2022   | 292.71          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 8  | 31   | 18 September 2022 | 20 January 2022   | 277.13          | 15.58         | 4.35     | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 9  | 30   | 18 October 2022   | 20 January 2022   | 257.2           | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 10 | 31   | 18 November 2022  | 20 January 2022   | 237.27          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 11 | 30   | 18 December 2022  | 20 January 2022   | 217.34          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 12 | 31   | 18 January 2023   | 20 January 2022   | 197.41          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 13 | 31   | 18 February 2023  | 20 January 2022   | 177.48          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 14 | 28   | 18 March 2023     | 20 January 2022   | 157.55          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 15 | 31   | 18 April 2023     | 20 January 2022   | 137.62          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 16 | 30   | 18 May 2023       | 20 January 2022   | 117.69          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 17 | 31   | 18 June 2023      | 20 January 2022   |  97.76          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 18 | 30   | 18 July 2023      | 28 February 2022  |  78.7           | 19.06         | 0.87     | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 19 | 31   | 18 August 2023    | 16 September 2022 |  58.77          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 20 | 31   | 18 September 2023 | 16 September 2022 |  38.84          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 21 | 30   | 18 October 2023   | 16 September 2022 |  18.91          | 19.93         | 0.0      | 0.0  | 0.0       | 19.93 | 19.93 | 19.93       | 0.0   |  0.0        |
      | 22 | 31   | 18 November 2023  | 16 September 2022 |   0.0           | 18.91         | 0.0      | 0.0  | 0.0       | 18.91 | 18.91 | 18.91       | 0.0   |  0.0        |
      | 23 | 30   | 18 December 2023  | 16 September 2022 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0  |  0.0  |  0.0        | 0.0   |  0.0        |
      | 24 | 31   | 18 January 2024   | 20 January 2022   |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0  |  0.0  |  0.0        | 0.0   |  0.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late    | Outstanding |
      | 431.98        | 5.46     | 0.0  | 0.0       | 437.44  | 437.44  | 437.44     | 0.0     | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.75    |  0.24    | 0.0  | 0.0       |  82.23       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  82.03       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  19.18     |  0.65    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 18 February 2022   | Accrual Activity        |  0.24   |   0.0      |  0.24    | 0.0  | 0.0       |   0.0        | false    | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.96     |  0.87    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.51     |  0.32    | 0.0  | 0.0       |  43.56       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  19.29     |  0.54    | 0.0  | 0.0       |  43.78       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.97     |  0.86    | 0.0  | 0.0       |  44.1        | true     | false    |
      | 18 May 2022        | Repayment               | 19.83   |  18.45     |  1.38    | 0.0  | 0.0       |  44.62       | true     | false    |
      | 18 June 2022       | Repayment               | 19.83   |  17.91     |  1.92    | 0.0  | 0.0       |  45.16       | true     | false    |
      | 18 July 2022       | Repayment               | 19.83   |  17.39     |  2.44    | 0.0  | 0.0       |  45.68       | true     | false    |
      | 18 August 2022     | Repayment               | 19.83   |  16.85     |  2.98    | 0.0  | 0.0       |  46.22       | true     | false    |
      | 16 September 2022  | Accrual                 | 4.59    |   0.0      |  4.59    | 0.0  | 0.0       |   0.0        | false    | false    |
      | 16 September 2022  | Charge-off              | 67.42   |  63.07     |  4.35    | 0.0  | 0.0       |   0.0        | false    | false    |
      | 16 September 2022  | Payout Refund           | 67.42   |  67.42     |  0.0     | 0.0  | 0.0       |   0.0        | false    | false    |
      | 16 September 2022  | Interest Refund         |  4.59   |   0.0      |  0.0     | 0.0  | 0.0       |   0.0        | false    | false    |
      | 16 September 2022  | Accrual Activity        |  5.22   |   0.0      |  5.22    | 0.0  | 0.0       |   0.0        | false    | false    |
    Then In Loan Transactions all transactions have non-null external-id
    When Admin makes Credit Balance Refund transaction on "16 September 2022" with 4.59 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4206
  Scenario: Verify Interest Payment Waiver transaction - UC12: Goodwill Credit after Charge-off - UC4
    When Admin sets the business date to "23 October 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal   | ANNUAL interest rate %     | interest type              | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType  | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP2_ADV_CUSTOM_PMT_ALLOC_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALC_ZERO_CHARGE_OFF_ACCRUAL | 18 January 2022   | 431.98           | 9.99                       | DECLINING_BALANCE          | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                  | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "18 January 2022" with "431.98" amount and expected disbursement date on "18 January 2022"
    And Admin successfully disburse the loan on "18 January 2022" with "431.98" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance  | Late  | Outstanding |
      |    |      | 18 January 2022   |                   | 431.98          |               |          | 0.0  |           | 0.0   |  0.0  |             |       |             |
      | 1  | 31   | 18 February 2022  |                   | 397.68          | 34.3          | 3.67     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 2  | 28   | 18 March 2022     |                   | 363.02          | 34.66         | 3.31     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 3  | 31   | 18 April 2022     |                   | 328.72          | 34.3          | 3.67     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 4  | 30   | 18 May 2022       |                   | 294.3           | 34.42         | 3.55     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 5  | 31   | 18 June 2022      |                   | 260.0           | 34.3          | 3.67     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 6  | 30   | 18 July 2022      |                   | 225.58          | 34.42         | 3.55     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 7  | 31   | 18 August 2022    |                   | 191.28          | 34.3          | 3.67     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 8  | 31   | 18 September 2022 |                   | 156.98          | 34.3          | 3.67     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 9  | 30   | 18 October 2022   |                   | 122.56          | 34.42         | 3.55     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 10 | 31   | 18 November 2022  |                   |  88.26          | 34.3          | 3.67     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 11 | 30   | 18 December 2022  |                   |  53.84          | 34.42         | 3.55     | 0.0  | 0.0       | 37.97 |  0.0  |  0.0        | 0.0   | 37.97       |
      | 12 | 31   | 18 January 2023   |                   |   0.0           | 53.84         | 3.67     | 0.0  | 0.0       | 57.51 |  0.0  |  0.0        | 0.0   | 57.51       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late    | Outstanding |
      | 431.98        | 43.2     | 0.0  | 0.0       | 475.18 | 0.0    | 0.0        | 0.0     | 475.18      |
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       |
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "20 January 2022" with 349.99 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance  | Late  | Outstanding |
      |    |      | 18 January 2022   |                   | 431.98          |               |          | 0.0  |           | 0.0   |  0.0  |             |       |             |
      | 1  | 31   | 18 February 2022  |                   | 394.9           | 37.08         | 0.89     | 0.0  | 0.0       | 37.97 | 29.37 | 29.37       | 0.0   | 8.6         |
      | 2  | 28   | 18 March 2022     |                   | 357.56          | 37.34         | 0.63     | 0.0  | 0.0       | 37.97 | 29.17 | 29.17       | 0.0   | 8.8         |
      | 3  | 31   | 18 April 2022     |                   | 320.28          | 37.28         | 0.69     | 0.0  | 0.0       | 37.97 | 29.17 | 29.17       | 0.0   | 8.8         |
      | 4  | 30   | 18 May 2022       |                   | 282.98          | 37.3          | 0.67     | 0.0  | 0.0       | 37.97 | 29.17 | 29.17       | 0.0   | 8.8         |
      | 5  | 31   | 18 June 2022      |                   | 245.7           | 37.28         | 0.69     | 0.0  | 0.0       | 37.97 | 29.17 | 29.17       | 0.0   | 8.8         |
      | 6  | 30   | 18 July 2022      |                   | 208.4           | 37.3          | 0.67     | 0.0  | 0.0       | 37.97 | 29.17 | 29.17       | 0.0   | 8.8         |
      | 7  | 31   | 18 August 2022    |                   | 171.12          | 37.28         | 0.69     | 0.0  | 0.0       | 37.97 | 29.17 | 29.17       | 0.0   | 8.8         |
      | 8  | 31   | 18 September 2022 |                   | 133.84          | 37.28         | 0.69     | 0.0  | 0.0       | 37.97 | 29.17 | 29.17       | 0.0   | 8.8         |
      | 9  | 30   | 18 October 2022   |                   |  96.54          | 37.3          | 0.67     | 0.0  | 0.0       | 37.97 | 29.17 | 29.17       | 0.0   | 8.8         |
      | 10 | 31   | 18 November 2022  |                   |  58.29          | 38.25         | 0.69     | 0.0  | 0.0       | 38.94 | 29.17 | 29.17       | 0.0   | 9.77        |
      | 11 | 30   | 18 December 2022  | 20 January 2022   |  29.12          | 29.17         | 0.0      | 0.0  | 0.0       | 29.17 | 29.17 | 29.17       | 0.0   | 0.0         |
      | 12 | 31   | 18 January 2023   | 20 January 2022   |   0.0           | 29.12         | 0.0      | 0.0  | 0.0       | 29.12 | 29.12 | 29.12       | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late    | Outstanding |
      | 431.98        | 6.98     | 0.0  | 0.0       | 438.96 | 350.19 | 350.19     | 0.0     | 88.77       |
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.99    |  0.0     | 0.0  | 0.0       |  81.99       |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  81.79       |
    And Customer makes "AUTOPAY" repayment on "18 February 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 February 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.99    |  0.0     | 0.0  | 0.0       |  81.99       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  81.79       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  18.94     |  0.89    | 0.0  | 0.0       |  62.85       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "28 February 2022" with 19.83 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.99    |  0.0     | 0.0  | 0.0       |  81.99       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  81.79       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  18.94     |  0.89    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.72     |  1.11    | 0.0  | 0.0       |  63.07       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "18 March 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 March 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.99    |  0.0     | 0.0  | 0.0       |  81.99       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  81.79       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  18.94     |  0.89    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.72     |  1.11    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.52     |  0.31    | 0.0  | 0.0       |  43.55       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "31 March 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "31 March 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.99    |  0.0     | 0.0  | 0.0       |  81.99       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  81.79       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  18.94     |  0.89    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.72     |  1.11    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.52     |  0.31    | 0.0  | 0.0       |  43.55       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  19.3      |  0.53    | 0.0  | 0.0       |  43.77       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "18 April 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 April 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.99    |  0.0     | 0.0  | 0.0       |  81.99       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  81.79       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  18.94     |  0.89    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.72     |  1.11    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.52     |  0.31    | 0.0  | 0.0       |  43.55       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  19.3      |  0.53    | 0.0  | 0.0       |  43.77       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.98     |  0.85    | 0.0  | 0.0       |  44.09       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "18 May 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 May 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.99    |  0.0     | 0.0  | 0.0       |  81.99       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  81.79       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  18.94     |  0.89    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.72     |  1.11    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.52     |  0.31    | 0.0  | 0.0       |  43.55       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  19.3      |  0.53    | 0.0  | 0.0       |  43.77       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.98     |  0.85    | 0.0  | 0.0       |  44.09       | true     | false    |
      | 18 May 2022        | Repayment               | 19.83   |  18.46     |  1.37    | 0.0  | 0.0       |  44.61       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "18 June 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 June 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.99    |  0.0     | 0.0  | 0.0       |  81.99       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  81.79       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  18.94     |  0.89    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.72     |  1.11    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.52     |  0.31    | 0.0  | 0.0       |  43.55       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  19.3      |  0.53    | 0.0  | 0.0       |  43.77       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.98     |  0.85    | 0.0  | 0.0       |  44.09       | true     | false    |
      | 18 May 2022        | Repayment               | 19.83   |  18.46     |  1.37    | 0.0  | 0.0       |  44.61       | true     | false    |
      | 18 June 2022       | Repayment               | 19.83   |  18.46     |  1.37    | 0.0  | 0.0       |  44.61       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "18 July 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 July 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.99    |  0.0     | 0.0  | 0.0       |  81.99       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  81.79       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  18.94     |  0.89    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.72     |  1.11    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.52     |  0.31    | 0.0  | 0.0       |  43.55       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  19.3      |  0.53    | 0.0  | 0.0       |  43.77       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.98     |  0.85    | 0.0  | 0.0       |  44.09       | true     | false    |
      | 18 May 2022        | Repayment               | 19.83   |  18.46     |  1.37    | 0.0  | 0.0       |  44.61       | true     | false    |
      | 18 June 2022       | Repayment               | 19.83   |  18.46     |  1.37    | 0.0  | 0.0       |  44.61       | true     | false    |
      | 18 July 2022       | Repayment               | 19.83   |  18.46     |  1.37    | 0.0  | 0.0       |  44.61       | true     | false    |
    And Customer makes "AUTOPAY" repayment on "18 August 2022" with 19.83 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "18 August 2022"
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.99    |  0.0     | 0.0  | 0.0       |  81.99       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  81.79       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  18.94     |  0.89    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.72     |  1.11    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.52     |  0.31    | 0.0  | 0.0       |  43.55       | true     | false    |
      | 31 March 2022      | Repayment               | 19.83   |  19.3      |  0.53    | 0.0  | 0.0       |  43.77       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.98     |  0.85    | 0.0  | 0.0       |  44.09       | true     | false    |
      | 18 May 2022        | Repayment               | 19.83   |  18.46     |  1.37    | 0.0  | 0.0       |  44.61       | true     | false    |
      | 18 June 2022       | Repayment               | 19.83   |  18.46     |  1.37    | 0.0  | 0.0       |  44.61       | true     | false    |
      | 18 July 2022       | Repayment               | 19.83   |  18.46     |  1.37    | 0.0  | 0.0       |  44.61       | true     | false    |
      | 18 August 2022     | Repayment               | 19.83   |  18.46     |  1.37    | 0.0  | 0.0       |  44.61       | true     | false    |
    And Admin does charge-off the loan on "16 September 2022"
    When Admin makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "16 September 2022" with 66.54 EUR transaction amount and self-generated external-id
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance  | Late  | Outstanding |
      |    |      | 18 January 2022   |                   | 431.98          |               |          | 0.0  |           | 0.0   |  0.0  |             |       |             |
      | 1  | 31   | 18 February 2022  | 28 February 2022  | 394.9           | 37.08         | 0.89     | 0.0  | 0.0       | 37.97 | 37.97 | 29.37       | 8.6   | 0.0         |
      | 2  | 28   | 18 March 2022     | 28 February 2022  | 357.15          | 37.75         | 0.22     | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 3  | 31   | 18 April 2022     | 16 September 2022 | 320.03          | 37.12         | 0.85     | 0.0  | 0.0       | 37.97 | 37.97 | 31.6        | 6.37  | 0.0         |
      | 4  | 30   | 18 May 2022       | 16 September 2022 | 282.58          | 37.45         | 0.52     | 0.0  | 0.0       | 37.97 | 37.97 | 29.17       | 8.8   | 0.0         |
      | 5  | 31   | 18 June 2022      | 16 September 2022 | 245.15          | 37.43         | 0.54     | 0.0  | 0.0       | 37.97 | 37.97 | 29.17       | 8.8   | 0.0         |
      | 6  | 30   | 18 July 2022      | 16 September 2022 | 207.7           | 37.45         | 0.52     | 0.0  | 0.0       | 37.97 | 37.97 | 29.17       | 8.8   | 0.0         |
      | 7  | 31   | 18 August 2022    | 16 September 2022 | 170.27          | 37.43         | 0.54     | 0.0  | 0.0       | 37.97 | 37.97 | 29.17       | 8.8   | 0.0         |
      | 8  | 31   | 18 September 2022 | 16 September 2022 | 132.8           | 37.47         | 0.5      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 9  | 30   | 18 October 2022   | 16 September 2022 |  94.83          | 37.97         | 0.0      | 0.0  | 0.0       | 37.97 | 37.97 | 37.97       | 0.0   | 0.0         |
      | 10 | 31   | 18 November 2022  | 16 September 2022 |  58.29          | 36.54         | 0.0      | 0.0  | 0.0       | 36.54 | 36.54 | 36.54       | 0.0   | 0.0         |
      | 11 | 30   | 18 December 2022  | 20 January 2022   |  29.12          | 29.17         | 0.0      | 0.0  | 0.0       | 29.17 | 29.17 | 29.17       | 0.0   | 0.0         |
      | 12 | 31   | 18 January 2023   | 20 January 2022   |   0.0           | 29.12         | 0.0      | 0.0  | 0.0       | 29.12 | 29.12 | 29.12       | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late    | Outstanding |
      | 431.98        | 4.58     | 0.0  | 0.0       | 436.56 | 436.56 | 386.39     | 50.17   | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type        | Amount  | Principal  | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 18 January 2022    | Disbursement            | 431.98  |   0.0      |  0.0     | 0.0  | 0.0       | 431.98       | false    | false    |
      | 20 January 2022    | Merchant Issued Refund  | 349.99  |  349.99    |  0.0     | 0.0  | 0.0       |  81.99       | false    | false    |
      | 20 January 2022    | Interest Refund         |  0.2    |   0.2      |  0.0     | 0.0  | 0.0       |  81.79       | false    | false    |
      | 18 February 2022   | Repayment               | 19.83   |  18.94     |  0.89    | 0.0  | 0.0       |  62.85       | true     | false    |
      | 18 February 2022   | Accrual Activity        |  0.89   |   0.0      |  0.89    | 0.0  | 0.0       |   0.0        | false    | false    |
      | 28 February 2022   | Repayment               | 19.83   |  18.72     |  1.11    | 0.0  | 0.0       |  63.07       | false    | false    |
      | 18 March 2022      | Repayment               | 19.83   |  19.52     |  0.31    | 0.0  | 0.0       |  43.55       | true     | false    |
      | 18 March 2022      | Accrual Activity        |  0.22   |   0.0      |  0.22    | 0.0  | 0.0       |   0.0        | false    | false    |
      | 31 March 2022      | Repayment               | 19.83   |  19.3      |  0.53    | 0.0  | 0.0       |  43.77       | true     | false    |
      | 18 April 2022      | Repayment               | 19.83   |  18.98     |  0.85    | 0.0  | 0.0       |  44.09       | true     | false    |
      | 18 April 2022      | Accrual Activity        |  0.85   |   0.0      |  0.85    | 0.0  | 0.0       |   0.0        | false    | false    |
      | 18 May 2022        | Repayment               | 19.83   |  18.46     |  1.37    | 0.0  | 0.0       |  44.61       | true     | false    |
      | 18 May 2022        | Accrual Activity        |  0.52   |   0.0      |  0.52    | 0.0  | 0.0       |   0.0        | false    | false    |
      | 18 June 2022       | Repayment               | 19.83   |  18.46     |  1.37    | 0.0  | 0.0       |  44.61       | true     | false    |
      | 18 June 2022       | Accrual Activity        |  0.54   |   0.0      |  0.54    | 0.0  | 0.0       |   0.0        | false    | false    |
      | 18 July 2022       | Repayment               | 19.83   |  18.46     |  1.37    | 0.0  | 0.0       |  44.61       | true     | false    |
      | 18 July 2022       | Accrual Activity        |  0.52   |   0.0      |  0.52    | 0.0  | 0.0       |   0.0        | false    | false    |
      | 18 August 2022     | Repayment               | 19.83   |  18.46     |  1.37    | 0.0  | 0.0       |  44.61       | true     | false    |
      | 18 August 2022     | Accrual Activity        |  0.54   |   0.0      |  0.54    | 0.0  | 0.0       |   0.0        | false    | false    |
      | 16 September 2022  | Accrual                 | 4.58    |   0.0      |  4.58    | 0.0  | 0.0       |   0.0        | false    | false    |
      | 16 September 2022  | Charge-off              | 66.54   |  63.07     |  3.47    | 0.0  | 0.0       |   0.0        | false    | false    |
      | 16 September 2022  | Goodwill Credit         | 66.54   |  63.07     |  3.47    | 0.0  | 0.0       |   0.0        | false    | false    |
      | 16 September 2022  | Accrual Activity        |  0.5    |   0.0      |  0.5     | 0.0  | 0.0       |   0.0        | false    | false    |
    Then In Loan Transactions all transactions have non-null external-id
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
