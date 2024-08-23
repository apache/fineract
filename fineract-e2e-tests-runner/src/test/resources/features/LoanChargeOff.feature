@ChargeOffFeature
Feature: Charge-off


  Scenario: As a user I want to do a Charge-off for non-fraud loan after disbursement
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable     |        | 1000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 1000.0 |        |


  Scenario: As a user I want to do a Charge-off for non-fraud loan after repayment
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 250 EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 750.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 750.0 |        |


  Scenario: As a user I want to do a Repayment undo after Charge-off for non-fraud
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 250 EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    When Customer undo "1"th repayment on "05 January 2023"
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "05 January 2023" is reverted
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable     |        | 1000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 1000.0 |        |


  Scenario: As a user I want to do Charge-off for fraud loan when FEE is added
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable        |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 10.0   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 1000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 10.0   |        |


  Scenario: As a user I want to do a Merchant Refund after charge-off (fee portion)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "22 February 2023" with 100 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable        |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 10.0   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 1000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 10.0   |        |
    Then Loan Transactions tab has a "MERCHANT_ISSUED_REFUND" transaction with date "22 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt      |       | 90.0   |
      | INCOME    | 404008       | Fee Charge Off            |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 100.0 |        |


  Scenario: As a user I want to do a Charge-off for non-fraud loan when FEE and PENALTY added
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable        |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 110.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 1000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 110.0  |        |

  @Skip  @chargeoffOnLoanWithInterest
  Scenario: As a user I want to do Charge-off for non-fraud loan when FEE and PENALTY added (interest portion)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 143.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 1000.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 30.0   |        |
      | INCOME  | 404008       | Fee Charge Off             | 113.0  |        |

  @Skip  @chargeoffOnLoanWithInterest
  Scenario: As a user I want to do a Merchant Refund after charge-off for non fraud loan (interest portion)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "22 February 2023" with 500 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 143.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 1000.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 30.0   |        |
      | INCOME  | 404008       | Fee Charge Off             | 113.0  |        |
    Then Loan Transactions tab has a "MERCHANT_ISSUED_REFUND" transaction with date "22 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name               | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt       |       | 367.0  |
      | INCOME    | 404001       | Interest Income Charge Off |       | 20.0   |
      | INCOME    | 404008       | Fee Charge Off             |       | 113.0  |
      | LIABILITY | 145023       | Suspense/Clearing account  | 500.0 |        |


  @Skip  @chargeoffOnLoanWithInterest
  Scenario: As a user I want to do a Payout refund after charge-off for non fraud loan (interest portion)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    When Admin makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "22 February 2023" with 500 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 143.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 1000.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 30.0   |        |
      | INCOME  | 404008       | Fee Charge Off             | 113.0  |        |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "22 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name               | Debit | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt       |       | 367.0  |
      | INCOME    | 404001       | Interest Income Charge Off |       | 20.0   |
      | INCOME    | 404008       | Fee Charge Off             |       | 113.0  |
      | LIABILITY | 145023       | Suspense/Clearing account  | 500.0 |        |

  @Skip  @chargeoffOnLoanWithInterest
  Scenario:  As a user I want to do a Repayment after Charge-off for fraud loan when FEE and PENALTY added (product with interest)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "22 February 2023" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 143.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 1000.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 30.0   |        |
      | INCOME  | 404008       | Fee Charge Off             | 113.0  |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "22 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 744008       | Recoveries                |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |



  Scenario: As a user I want to do a Charge-off for fraud loan after disbursement
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    Then Admin can successfully set Fraud flag to the loan
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 1000.0 |        |


  Scenario: As a user I want to do a Repayment undo after Charge-off for fraud loan
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 250 EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    Then Admin can successfully set Fraud flag to the loan
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    When Customer undo "1"th repayment on "05 January 2023"
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "05 January 2023" is reverted
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 1000.0 |        |


  Scenario: As a user I want to do a Charge-off for fraud loan when FEE is added
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    Then Admin can successfully set Fraud flag to the loan
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 10.0   |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 1000.0 |        |
      | INCOME  | 404008       | Fee Charge Off             | 10.0   |        |


  Scenario: As a user I want to do a Charge-off for fraud loan when FEE and PENALTY added
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
    Then Admin can successfully set Fraud flag to the loan
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 110.0  |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 1000.0 |        |
      | INCOME  | 404008       | Fee Charge Off             | 110.0  |        |

  @Skip  @chargeoffOnLoanWithInterest
  Scenario: As a user I want to do a Charge-off for fraud loan when FEE and PENALTY added (interest portion)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
    Then Admin can successfully set Fraud flag to the loan
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 143.0  |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 1000.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 30.0   |        |
      | INCOME  | 404008       | Fee Charge Off             | 113.0  |        |

  @Skip  @chargeoffOnLoanWithInterest
  Scenario: As a user I want to do a Merchant issue refund for fraud loan when FEE and PENALTY added (interest portion)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
    Then Admin can successfully set Fraud flag to the loan
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "22 February 2023" with 500 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 143.0  |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 1000.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 30.0   |        |
      | INCOME  | 404008       | Fee Charge Off             | 113.0  |        |
    Then Loan Transactions tab has a "MERCHANT_ISSUED_REFUND" transaction with date "22 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name               | Debit | Credit |
      | EXPENSE   | 744037       | Credit Loss/Bad Debt-Fraud |       | 367.0  |
      | INCOME    | 404001       | Interest Income Charge Off |       | 20.0   |
      | INCOME    | 404008       | Fee Charge Off             |       | 113.0  |
      | LIABILITY | 145023       | Suspense/Clearing account  | 500.0 |        |

  @Skip  @chargeoffOnLoanWithInterest
  Scenario: As a user I want to do Payout refund for fraud loan when FEE and PENALTY added (interest portion)
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
    Then Admin can successfully set Fraud flag to the loan
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    When Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "22 February 2023" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 143.0  |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 1000.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 30.0   |        |
      | INCOME  | 404008       | Fee Charge Off             | 113.0  |        |
    Then Loan Transactions tab has a "PAYOUT_REFUND" transaction with date "22 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name               | Debit | Credit |
      | EXPENSE   | 744037       | Credit Loss/Bad Debt-Fraud |       | 367.0  |
      | INCOME    | 404001       | Interest Income Charge Off |       | 20.0   |
      | INCOME    | 404008       | Fee Charge Off             |       | 113.0  |
      | LIABILITY | 145023       | Suspense/Clearing account  | 500.0 |        |


  @Skip  @chargeoffOnLoanWithInterest
  Scenario: As a user I want to do a Repayment after Charge-off for fraud loan when FEE and PENALTY added
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
    Then Admin can successfully set Fraud flag to the loan
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "22 February 2023" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 143.0  |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 1000.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 30.0   |        |
      | INCOME  | 404008       | Fee Charge Off             | 113.0  |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "22 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 744008       | Recoveries                |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |


  @Skip  @chargeoffOnLoanWithInterest
  Scenario: As a user I want to repay a loan which was charged-off
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
    Then Admin can successfully set Fraud flag to the loan
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "22 February 2023" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "23 February 2023"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "23 February 2023" with 643 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan marked as charged-off on "22 February 2023"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees  | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0   |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2023 | 22 February 2023 | 666.67          | 333.33        | 10.0     | 0.0   | 10.0      | 353.33 | 353.33 | 0.0        | 343.33 | 0.0         |
      | 2  | 28   | 01 March 2023    | 23 February 2023 | 333.34          | 333.33        | 10.0     | 103.0 | 0.0       | 446.33 | 446.33 | 446.33     | 0.0    | 0.0         |
      | 3  | 31   | 01 April 2023    | 23 February 2023 | 0.0             | 333.34        | 10.0     | 0.0   | 0.0       | 343.34 | 343.34 | 343.34     | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late   | Outstanding |
      | 1000          | 30       | 103  | 10        | 1143 | 1143 | 789.67     | 343.33 | 0           |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 143.0  |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 1000.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 30.0   |        |
      | INCOME  | 404008       | Fee Charge Off             | 113.0  |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "22 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 744008       | Recoveries                |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "23 February 2023" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 30.0  |        |
      | INCOME | 404000       | Interest Income         |       | 30.0   |
      | ASSET  | 112603       | Interest/Fee Receivable | 103.0 |        |
      | INCOME | 404007       | Fee Income              |       | 103.0  |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "23 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 744008       | Recoveries                |       | 643.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 643.0 |        |

  @Skip  @chargeoffOnLoanWithInterest
  Scenario: As a user I want to do a charge-off undo before any other transactions on the loan
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
    Then Admin can successfully set Fraud flag to the loan
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0   |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 666.67          | 333.33        | 10.0     | 0.0   | 10.0      | 353.33 | 0.0  | 0.0        | 0.0  | 353.33      |
      | 2  | 28   | 01 March 2023    |           | 333.34          | 333.33        | 10.0     | 103.0 | 0.0       | 446.33 | 0.0  | 0.0        | 0.0  | 446.33      |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 333.34        | 10.0     | 0.0   | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 30       | 103  | 10        | 1143 | 0    | 0          | 0    | 1143        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 143.0  |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 1000.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 30.0   |        |
      | INCOME  | 404008       | Fee Charge Off             | 113.0  |        |
    Then Admin does a charge-off undo the loan
    When Admin sets the business date to "23 February 2023"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "23 February 2023" with 200 EUR transaction amount and system-generated Idempotency key
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |        | 143.0  |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 1000.0 |        |
      | INCOME  | 404001       | Interest Income Charge Off | 30.0   |        |
      | INCOME  | 404008       | Fee Charge Off             | 113.0  |        |
      | ASSET   | 112601       | Loans Receivable           | 1000.0 |        |
      | ASSET   | 112603       | Interest/Fee Receivable    | 143.0  |        |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud |        | 1000.0 |
      | INCOME  | 404001       | Interest Income Charge Off |        | 30.0   |
      | INCOME  | 404008       | Fee Charge Off             |        | 113.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "23 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 180.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 20.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 200.0 |        |


  Scenario: As a user I want to do a Charge-off before the last repayment
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "22 February 2023" with 200 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "23 February 2023"
    Then Charge-off undo is not possible on "21 February 2023"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 200.0 | 0.0        | 200.0 | 800.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 200  | 0          | 200  | 800         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "22 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 200.0 |        |


  Scenario: As a user I want to do a Charge-off between 2 repayments
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "22 February 2023" with 200 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "24 February 2023"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "24 February 2023" with 200 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "25 February 2023"
    Then Charge-off undo is not possible on "23 February 2023"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 400.0 | 0.0        | 400.0 | 600.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 400  | 0          | 400  | 600         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "22 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 200.0 |        |


  Scenario: As a user I want to do a backdated Charge-off when only disbursement transaction happened
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    And Admin does charge-off the loan on "10 February 2023"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 0    | 0          | 0    | 1000        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "10 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable     |        | 1000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 1000.0 |        |


  Scenario: As a user I want to do an undo on a transaction which was created before the Charge-off
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    And Customer makes "AUTOPAY" repayment on "22 January 2023" with 250 EUR transaction amount
    When Admin sets the business date to "23 February 2023"
    And Admin does charge-off the loan on "23 February 2023"
    When Customer undo "1"th repayment on "22 January 2023"
    Then On Loan Transactions tab the "Repayment" Transaction with date "22 January 2023" is reverted
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 0    | 0          | 0    | 1000        |


  Scenario: As a user I want to do an undo on a transaction which was created on the Charge-off day
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    And Customer makes "AUTOPAY" repayment on "22 January 2023" with 250 EUR transaction amount
    And Admin does charge-off the loan on "22 February 2023"
    When Customer undo "1"th repayment on "22 January 2023"
    Then On Loan Transactions tab the "Repayment" Transaction with date "22 January 2023" is reverted
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 0    | 0          | 0    | 1000        |


  Scenario: As a user I want to do a second Charge-off
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Second Charge-off is not possible on "22 February 2023"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 0    | 0          | 0    | 1000        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable     |        | 1000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 1000.0 |        |


  Scenario: As a user I want to do a second Charge-off undo
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    When Admin does a charge-off undo the loan
    And Charge-off undo is not possible as the loan is not charged-off
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 0    | 0          | 0    | 1000        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |


  Scenario: As a user I want to add charge after Charge-off
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    When Admin is not able to add "LOAN_SNOOZE_FEE" due date charge with "22 February 2023" due date and 200 EUR transaction amount because the of charged-off account
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 0    | 0          | 0    | 1000        |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable     |        | 1000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 1000.0 |        |

    # TODO: Add reschedule related tests for "Reschedule" and "Close as Reschedule" actions if the reschedule API fixed



  Scenario: Verify that Charge-off NOT results an error anymore when to be applied on a loan with an interest
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees  | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0   |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 667.0           | 333.0         | 10.0     | 0.0   | 10.0      | 353.0 | 0.0  | 0.0        | 0.0  | 353.0       |
      | 2  | 28   | 01 March 2023    |           | 334.0           | 333.0         | 10.0     | 103.0 | 0.0       | 446.0 | 0.0  | 0.0        | 0.0  | 446.0       |
      | 3  | 31   | 01 April 2023    |           | 0.0             | 334.0         | 10.0     | 0.0   | 0.0       | 344.0 | 0.0  | 0.0        | 0.0  | 344.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 30       | 103  | 10        | 1143 | 0    | 0          | 0    | 1143        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees  | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0   | 0.0       | 1000.0       |
      | 22 February 2023 | Charge-off       | 1143.0 | 1000.0    | 30.0     | 103.0 | 10.0      | 0.0          |


  Scenario: Verify that charge-off is reversed/replayed if Scheduled repayment which was placed on a date before the charge-off is reversed after the charge-off
    When Admin sets the business date to "01 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "25 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "25 January 2023" due date and 20 EUR transaction amount
    When Admin sets the business date to "01 March 2023"
    When Customer makes "REPAYMENT" transaction with "SCHEDULED" payment type on "01 March 2023" with 1000 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "01 June 2023"
    And Admin does charge-off the loan on "01 June 2023"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1000.0 | 0.0        | 1000.0 | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 1000 | 0          | 1000 | 20          |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 March 2023    | Repayment        | 1000.0 | 980.0     | 0.0      | 20.0 | 0.0       | 20.0         |
      | 01 June 2023     | Charge-off       | 20.0   | 20.0      | 0.0      | 0.0  | 0.0       | 0.0          |
    When Admin sets the business date to "05 June 2023"
    When Customer undo "1"th "Repayment" transaction made on "01 March 2023"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 0.0  | 0.0        | 0.0  | 1020.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 0    | 0          | 0    | 1020        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 March 2023    | Repayment        | 1000.0 | 980.0     | 0.0      | 20.0 | 0.0       | 20.0         |
      | 01 June 2023     | Charge-off       | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
    Then On Loan Transactions tab the "Repayment" Transaction with date "01 March 2023" is reverted


  Scenario: Verify that charge-off is reversed/replayed if Real time repayment which was placed on a date before the charge-off is reversed after the charge-off
    When Admin sets the business date to "01 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "25 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "25 January 2023" due date and 20 EUR transaction amount
    When Admin sets the business date to "10 February 2023"
    When Customer makes "REPAYMENT" transaction with "REAL_TIME" payment type on "10 February 2023" with 1020 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "15 February 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 February 2023" due date and 15 EUR transaction amount
    When Admin sets the business date to "01 June 2023"
    And Admin does charge-off the loan on "01 June 2023"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 31 January 2023  | 10 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 0.0        | 1020.0 | 0.0         |
      | 2  | 15   | 15 February 2023 |                  | 0.0             | 0.0           | 0.0      | 0.0  | 15.0      | 15.0   | 0.0    | 0.0        | 0.0    | 15.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 15        | 1035 | 1020 | 0          | 1020 | 15          |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 February 2023 | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 June 2023     | Charge-off       | 15.0   | 0.0       | 0.0      | 0.0  | 15.0      | 0.0          |
    When Admin sets the business date to "10 June 2023"
    When Customer undo "1"th "Repayment" transaction made on "10 February 2023"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023  |           | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 0.0  | 0.0        | 0.0  | 1020.0      |
      | 2  | 15   | 15 February 2023 |           | 0.0             | 0.0           | 0.0      | 0.0  | 15.0      | 15.0   | 0.0  | 0.0        | 0.0  | 15.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 15        | 1035 | 0    | 0          | 0    | 1035        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 February 2023 | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 June 2023     | Charge-off       | 1035.0 | 1000.0    | 0.0      | 20.0 | 15.0      | 0.0          |
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 February 2023" is reverted


  Scenario: Verify that charge-off is reversed/replayed if Autopay repayment which was placed on a date before the charge-off is reversed after the charge-off
    When Admin sets the business date to "01 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "25 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "25 January 2023" due date and 20 EUR transaction amount
    When Admin sets the business date to "10 February 2023"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "10 February 2023" with 1020 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "15 February 2023"
    When Admin adds "LOAN_NSF_FEE" due date charge with "15 February 2023" due date and 15 EUR transaction amount
    When Admin sets the business date to "01 June 2023"
    And Admin does charge-off the loan on "01 June 2023"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 31 January 2023  | 10 February 2023 | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 1020.0 | 0.0        | 1020.0 | 0.0         |
      | 2  | 15   | 15 February 2023 |                  | 0.0             | 0.0           | 0.0      | 0.0  | 15.0      | 15.0   | 0.0    | 0.0        | 0.0    | 15.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 15        | 1035 | 1020 | 0          | 1020 | 15          |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 February 2023 | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 June 2023     | Charge-off       | 15.0   | 0.0       | 0.0      | 0.0  | 15.0      | 0.0          |
    When Admin sets the business date to "10 June 2023"
    When Customer undo "1"th "Repayment" transaction made on "10 February 2023"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023  |           | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 0.0  | 0.0        | 0.0  | 1020.0      |
      | 2  | 15   | 15 February 2023 |           | 0.0             | 0.0           | 0.0      | 0.0  | 15.0      | 15.0   | 0.0  | 0.0        | 0.0  | 15.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 15        | 1035 | 0    | 0          | 0    | 1035        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 February 2023 | Accrual          | 20.0   | 0.0       | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 February 2023 | Repayment        | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 01 June 2023     | Charge-off       | 1035.0 | 1000.0    | 0.0      | 20.0 | 15.0      | 0.0          |
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 February 2023" is reverted


  Scenario: Verify that charge-off is NOT reversed/replayed if OCA repayment which was placed and reversed on a date after the charge-off
    When Admin sets the business date to "01 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "25 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "25 January 2023" due date and 20 EUR transaction amount
    When Admin sets the business date to "01 June 2023"
    And Admin does charge-off the loan on "01 June 2023"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 0.0  | 0.0        | 0.0  | 1020.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 0    | 0          | 0    | 1020        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 June 2023     | Charge-off       | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
    When Admin sets the business date to "10 June 2023"
    When Customer makes "REPAYMENT" transaction with "OCA_PAYMENT" payment type on "10 June 2023" with 1000 EUR transaction amount and system-generated Idempotency key
    When Customer undo "1"th "Repayment" transaction made on "10 June 2023"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 0.0  | 0.0        | 0.0  | 1020.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 0    | 0          | 0    | 1020        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 June 2023     | Charge-off       | 1020.0 | 1000.0    | 0.0      | 20.0 | 0.0       | 0.0          |
      | 10 June 2023     | Repayment        | 1000.0 | 980.0     | 0.0      | 20.0 | 0.0       | 20.0         |
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 June 2023" is reverted


  Scenario: Verify that charge-off is reversed/replayed if Goodwill credit transaction is placed on a date before the charge-off on business date after the charge-off
    When Admin sets the business date to "01 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "25 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "25 January 2023" due date and 20 EUR transaction amount
    When Admin sets the business date to "01 June 2023"
    And Admin does charge-off the loan on "01 June 2023"
    When Admin sets the business date to "10 June 2023"
    When Admin makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "29 January 2023" with 500 EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 500.0 | 500.0      | 0.0  | 520.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 500  | 500        | 0    | 520         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 29 January 2023  | Goodwill Credit  | 500.0  | 480.0     | 0.0      | 20.0 | 0.0       | 520.0        |
      | 01 June 2023     | Charge-off       | 520.0  | 520.0     | 0.0      | 0.0  | 0.0       | 0.0          |


  Scenario: As a user I want to do a undo Charge-off with reversal external Id
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable     |        | 1000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 1000.0 |        |
    When Admin sets the business date to "23 February 2023"
    And Admin does a charge-off undo the loan with reversal external Id
    Then Loan Charge-off undo event has reversed on date "23 February 2023" for charge-off undo


  Scenario: Verify that charge-off is reversed/replayed in case of partial payment, charge-off, second part of payment, reverse 1st payment
    When Admin sets the business date to "01 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 February 2023"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "10 February 2023" with 500 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "01 June 2023"
    And Admin does charge-off the loan on "01 June 2023"
    When Admin sets the business date to "05 June 2023"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "05 June 2023" with 500 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2023 |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 31 January 2023 | 05 June 2023 | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 1000.0 | 0.0        | 1000.0 | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 1000 | 0          | 1000 | 0           |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 February 2023 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 June 2023     | Charge-off       | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 05 June 2023     | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    When Admin sets the business date to "10 June 2023"
    When Customer undo "1"th "Repayment" transaction made on "10 February 2023"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 500.0 | 0.0        | 500.0 | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 500  | 0          | 500  | 500         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 February 2023 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 June 2023     | Charge-off       | 1000.0 | 1000.0    | 0.0      | 0.0  | 0.0       | 0.0          |
      | 05 June 2023     | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
    Then On Loan Transactions tab the "Repayment" Transaction with date "10 February 2023" is reverted
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "10 February 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 500.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 500.0  |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "01 June 2023" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable     |        | 1000.0 |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 1000.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 June 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 744008       | Recoveries                |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |


  Scenario: Verify that charge-off is results an error in case of partial payment, charge-off, fee added after charge-off
    When Admin sets the business date to "01 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "10 February 2023"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "10 February 2023" with 300 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "01 June 2023"
    And Admin does charge-off the loan on "01 June 2023"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 300.0 | 0.0        | 300.0 | 700.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 300  | 0          | 300  | 700         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 February 2023 | Repayment        | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 700.0        |
      | 01 June 2023     | Charge-off       | 700.0  | 700.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    When Admin sets the business date to "10 June 2023"
    Then Loan charge transaction with the following data results a 403 error and "CHARGED_OFF" error message
      | Charge type     | dueDate      | amount |
      | LOAN_SNOOZE_FEE | 10 June 2023 | 20     |


  Scenario: Verify that on interest bearing loans the accrual of interest is stopped when the loan is charged-off
    When Admin sets the business date to "01 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY | 01 January 2023   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "02 January 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "03 January 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "04 January 2023"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 10.19    | 0.0  | 0.0       | 1010.19 | 0.0  | 0.0        | 0.0  | 1010.19     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 10.19    | 0    | 0         | 1010.19 | 0    | 0          | 0    | 1010.19     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2023  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2023  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
    When Admin sets the business date to "05 January 2023"
    And Admin does charge-off the loan on "05 January 2023"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 10.19    | 0.0  | 0.0       | 1010.19 | 0.0  | 0.0        | 0.0  | 1010.19     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 10.19    | 0    | 0         | 1010.19 | 0    | 0          | 0    | 1010.19     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2023  | Accrual          | 0.33    | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 03 January 2023  | Accrual          | 0.33    | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 05 January 2023  | Charge-off       | 1010.19 | 1000.0    | 10.19    | 0.0  | 0.0       | 0.0          |


  Scenario: Verify that on interest bearing loans the accrual of interest is resumed and aggregated when the charge-off is reverted
    When Admin sets the business date to "01 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY | 01 January 2023   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "02 January 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "03 January 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "04 January 2023"
    And Admin does charge-off the loan on "04 January 2023"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 10.19    | 0.0  | 0.0       | 1010.19 | 0.0  | 0.0        | 0.0  | 1010.19     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 10.19    | 0    | 0         | 1010.19 | 0    | 0          | 0    | 1010.19     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2023  | Accrual          | 0.33    | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2023  | Charge-off       | 1010.19 | 1000.0    | 10.19    | 0.0  | 0.0       | 0.0          |
    When Admin sets the business date to "05 January 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "06 January 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "07 January 2023"
    Then Admin does a charge-off undo the loan
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 10.19    | 0.0  | 0.0       | 1010.19 | 0.0  | 0.0        | 0.0  | 1010.19     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 10.19    | 0    | 0         | 1010.19 | 0    | 0          | 0    | 1010.19     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2023  | Accrual          | 0.33    | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2023  | Charge-off       | 1010.19 | 1000.0    | 10.19    | 0.0  | 0.0       | 0.0          |
      | 06 January 2023  | Accrual          | 1.31    | 0.0       | 1.31     | 0.0  | 0.0       | 0.0          |
    Then On Loan Transactions tab the "Charge-off" Transaction with date "04 January 2023" is reverted


  Scenario: Verify that on interest bearing loans the accrual of interest is stopped when the loan is charged-off even if fully paid after the charge-off
    When Admin sets the business date to "01 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY | 01 January 2023   | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "02 January 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "03 January 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "04 January 2023"
    And Admin does charge-off the loan on "04 January 2023"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2023 |           | 0.0             | 1000.0        | 10.19    | 0.0  | 0.0       | 1010.19 | 0.0  | 0.0        | 0.0  | 1010.19     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 10.19    | 0    | 0         | 1010.19 | 0    | 0          | 0    | 1010.19     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2023  | Accrual          | 0.33    | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2023  | Charge-off       | 1010.19 | 1000.0    | 10.19    | 0.0  | 0.0       | 0.0          |
    When Admin sets the business date to "05 January 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "06 January 2023"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "06 January 2023" with 1010.19 EUR transaction amount and system-generated Idempotency key
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0     |            |      |             |
      | 1  | 31   | 01 February 2023 | 06 January 2023 | 0.0             | 1000.0        | 10.19    | 0.0  | 0.0       | 1010.19 | 1010.19 | 1010.19    | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000          | 10.19    | 0    | 0         | 1010.19 | 1010.19 | 1010.19    | 0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2023  | Accrual          | 0.33    | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          |
      | 04 January 2023  | Charge-off       | 1010.19 | 1000.0    | 10.19    | 0.0  | 0.0       | 0.0          |
      | 06 January 2023  | Repayment        | 1010.19 | 1000.0    | 10.19    | 0.0  | 0.0       | 0.0          |


  Scenario: Verify that the accrual of charges is not happened when the loan is charged-off on charge's due date but resumed when the charge-off is reverted
    When Admin sets the business date to "01 January 2023"
    And Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2023"
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "02 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "02 January 2023" due date and 10 EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "03 January 2023"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0.0      | 10   | 0         | 1010.0 | 0    | 0          | 0    | 1010.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2023  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    When Admin sets the business date to "04 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "04 January 2023" due date and 15 EUR transaction amount
    And Admin does charge-off the loan on "04 January 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "05 January 2023"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 25.0 | 0.0       | 1025.0 | 0.0  | 0.0        | 0.0  | 1025.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0.0      | 25   | 0         | 1025.0 | 0    | 0          | 0    | 1025.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2023  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 04 January 2023  | Charge-off       | 1025.0 | 1000.0    | 0.0      | 25.0 | 0.0       | 0.0          |
    When Admin sets the business date to "06 January 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "07 January 2023"
    Then Admin does a charge-off undo the loan
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2023 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 31 January 2023 |           | 0.0             | 1000.0        | 0.0      | 25.0 | 0.0       | 1025.0 | 0.0  | 0.0        | 0.0  | 1025.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0.0      | 25   | 0         | 1025.0 | 0    | 0          | 0    | 1025.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2023  | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 04 January 2023  | Charge-off       | 1025.0 | 1000.0    | 0.0      | 25.0 | 0.0       | 0.0          |
      | 06 January 2023  | Accrual          | 15.0   | 0.0       | 0.0      | 15.0 | 0.0       | 0.0          |
    Then On Loan Transactions tab the "Charge-off" Transaction with date "04 January 2023" is reverted

   @AdvancedPaymentAllocation
  Scenario: Verify that the user is able to do a Charge-off for fraud loan when FEE and PENALTY added - LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION product
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
    When Admin sets the business date to "22 February 2023"
    And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
    Then Admin can successfully set Fraud flag to the loan
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 750.0  |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 110.0  |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 750.0 |        |
      | INCOME  | 404008       | Fee Charge Off             | 110.0 |        |

   @AdvancedPaymentAllocation
  Scenario: Verify that the user is able to do a Charge-off for non-fraud loan after disbursement - LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION product
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 750.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 750.0 |        |

   @AdvancedPaymentAllocation
  Scenario: Verify that the user is able to do a Charge-off for non-fraud loan after repayment - LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION product
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 250 EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    Then Loan status will be "ACTIVE"
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 500.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 500.0 |        |

   @AdvancedPaymentAllocation
  Scenario: Verify that the user is able to do a Repayment undo after Charge-off for non-fraud - LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION product
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 250 EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    And Admin does charge-off the loan on "22 February 2023"
    Then Loan marked as charged-off on "22 February 2023"
    When Customer undo "1"th repayment on "05 January 2023"
    Then Loan status will be "ACTIVE"
    Then On Loan Transactions tab the "Repayment" Transaction with date "05 January 2023" is reverted
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 250.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 250.0  |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "22 February 2023" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 750.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 750.0 |        |

   @AdvancedPaymentAllocation
  Scenario: Verify that the user is able to do a backdated Charge-off when only disbursement transaction happened - LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION product
    When Admin sets the business date to "1 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "22 February 2023"
    And Admin does charge-off the loan on "10 February 2023"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2023  | 01 January 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2023  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 0    | 0         | 1000 | 250  | 0          | 0    | 750         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 10 February 2023 | Charge-off       | 750.0  | 750.0     | 0.0      | 0.0  | 0.0       | 0.0          |

   @AdvancedPaymentAllocation
  Scenario: Verify that charge-off is reversed/replayed if Goodwill credit transaction is placed on a date before the charge-off on business date after the charge-off - - LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION product
    When Admin sets the business date to "01 January 2023"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "25 January 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "25 January 2023" due date and 20 EUR transaction amount
    When Admin sets the business date to "01 June 2023"
    And Admin does charge-off the loan on "01 June 2023"
    When Admin sets the business date to "10 June 2023"
    When Admin makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "29 January 2023" with 500 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2023  | 01 January 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 15   | 16 January 2023  | 29 January 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 3  | 15   | 31 January 2023  |                 | 250.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 0.0   | 0.0        | 0.0   | 270.0       |
      | 4  | 15   | 15 February 2023 | 29 January 2023 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      | 1000          | 0        | 20   | 0         | 1020 | 750  | 250        | 250  | 270         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2023  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 29 January 2023  | Goodwill Credit  | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 01 June 2023     | Charge-off       | 270.0  | 250.0     | 0.0      | 20.0 | 0.0       | 0.0          |

   @AdvancedPaymentAllocation
  Scenario: Verify charge-off GL entries in case of reverse-replay on fraud loan
    When Admin sets the business date to "01 February 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 February 2024  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 February 2024" with "1000" amount and expected disbursement date on "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "02 February 2024"
    And Customer makes "AUTOPAY" repayment on "02 February 2024" with 100 EUR transaction amount
    When Admin sets the business date to "03 February 2024"
    Then Admin can successfully set Fraud flag to the loan
    When Admin sets the business date to "03 February 2024"
    And Admin does charge-off the loan on "03 February 2024"
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 650.0  |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 650.0 |        |
    When Admin sets the business date to "04 February 2024"
    When Customer undo "1"th "Repayment" transaction made on "02 February 2024"
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 750.0  |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 750.0 |        |
    Then In Loan transactions the replayed "CHARGE_OFF" transaction with date "03 February 2024" has a reverted transaction pair with the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 650.0  |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 650.0 |        |
      | ASSET   | 112601       | Loans Receivable           | 650.0 |        |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud |       | 650.0  |

