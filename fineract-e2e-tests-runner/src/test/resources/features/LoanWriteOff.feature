@WriteOffFeature
  Feature: Write-off


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






