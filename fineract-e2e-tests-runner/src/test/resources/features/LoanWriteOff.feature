@WriteOffFeature
  Feature: Write-off

    @TestRailId:C2934
    Scenario: As a user I want to do Write-off a loan and verify that undo repayment post write-off results in error
      When Admin sets the business date to "1 January 2023"
      And Admin creates a client with random data
      When Admin creates a fully customized loan with the following data:
        | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
        | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
      And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
      And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
      When Admin sets the business date to "22 January 2023"
      And Customer makes "AUTOPAY" repayment on "22 January 2023" with 100 EUR transaction amount
      When Admin sets the business date to "29 January 2023"
      And Admin does write-off the loan on "29 January 2023"
      Then Loan status will be "CLOSED_WRITTEN_OFF"
      Then Loan Transactions tab has a transaction with date: "29 January 2023", and with the following data:
        | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
        | Close (as written-off) | 650.0  | 650.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      Then Admin fails to undo "1"th transaction made on "22 January 2023"


    @TestRailId:C2935
    Scenario: As a user I want to do Write-off a loan and verify that backdate repayment post write-off results in error
      When Admin sets the business date to "1 January 2023"
      And Admin creates a client with random data
      When Admin creates a fully customized loan with the following data:
        | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
        | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
      And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
      And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
      When Admin sets the business date to "22 January 2023"
      And Customer makes "AUTOPAY" repayment on "22 January 2023" with 100 EUR transaction amount
      When Admin sets the business date to "29 January 2023"
      And Admin does write-off the loan on "29 January 2023"
      Then Loan status will be "CLOSED_WRITTEN_OFF"
      Then Loan Transactions tab has a transaction with date: "29 January 2023", and with the following data:
        | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
        | Close (as written-off) | 650.0  | 650.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      Then Loan "AUTOPAY" repayment transaction on "26 January 2023" with 50 EUR transaction amount results in error


    @TestRailId:C2936
    Scenario: As a user I want to do Write-off a loan and verify that undo write-off results in error
      When Admin sets the business date to "1 January 2023"
      And Admin creates a client with random data
      When Admin creates a fully customized loan with the following data:
        | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
        | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
      And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
      And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
      When Admin sets the business date to "22 January 2023"
      And Customer makes "AUTOPAY" repayment on "22 January 2023" with 100 EUR transaction amount
      When Admin sets the business date to "29 January 2023"
      And Admin does write-off the loan on "29 January 2023"
      Then Loan status will be "CLOSED_WRITTEN_OFF"
      Then Loan Transactions tab has a transaction with date: "29 January 2023", and with the following data:
        | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
        | Close (as written-off) | 650.0  | 650.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      Then Admin fails to undo "1"th transaction made on "29 January 2023"

    @TestRailId:C4006
    Scenario: Verify accounting during Write-off when loan was already charged-off
      When Admin sets the business date to "1 January 2023"
      And Admin creates a client with random data
      When Admin creates a fully customized loan with the following data:
        | LoanProduct       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
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
      When Admin sets the business date to "1 March 2023"
      And Admin does write-off the loan on "1 March 2023"
      Then Loan status will be "CLOSED_WRITTEN_OFF"
      Then Loan Transactions tab has a "WRITE_OFF" transaction with date "01 March 2023" which has the following Journal entries:
        | Type    | Account code | Account name               | Debit  | Credit |
        | EXPENSE | 744007       | Credit Loss/Bad Debt       |        | 1000.0 |
        | INCOME  | 404001       | Interest Income Charge Off |        | 30.0   |
        | INCOME  | 404008       | Fee Charge Off             |        | 113.0  |
        | EXPENSE | e4           | Written off                | 1143.0 |        |

    @TestRailId:C4007
    Scenario: Verify accounting during Write-off when loan was not charged-off before
      When Admin sets the business date to "1 January 2023"
      And Admin creates a client with random data
      When Admin creates a fully customized loan with the following data:
        | LoanProduct       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
        | LP1_INTEREST_FLAT | 1 January 2023    | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
      And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
      And Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
      And Admin adds an NSF fee because of payment bounce with "1 January 2023" transaction date
      When Admin sets the business date to "22 February 2023"
      And Admin adds a 10 % Processing charge to the loan with "en" locale on date: "22 February 2023"
      Then Loan status will be "ACTIVE"
      Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
        | Type      | Account code | Account name              | Debit  | Credit |
        | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
        | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
      When Admin sets the business date to "1 March 2023"
      And Admin does write-off the loan on "1 March 2023"
      Then Loan status will be "CLOSED_WRITTEN_OFF"
      Then Loan Transactions tab has a "WRITE_OFF" transaction with date "01 March 2023" which has the following Journal entries:
        | Type    | Account code | Account name               | Debit  | Credit |
        | ASSET   | 112601       | Loans Receivable           |        | 1000.0 |
        | ASSET   | 112603       | Interest/Fee Receivable    |        | 143.0  |
        | EXPENSE | e4           | Written off                | 1143.0 |        |
