@LoanChargesDownPaymentFeature
Feature: LoanChargesDownPayment

  @TestRailId:C119543
  Scenario: Verify flat down payment fee with auto down payment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026  | 750.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026   |                  | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |                  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 260.0 | 0.0        | 0.0  | 750.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 260.0  | 250.0     | 0.0      | 10.0 | 0.0       | 750.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 260.0 |        |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119544
  Scenario: Verify percentage down payment fee with auto down payment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" with "PERCENTAGE_AMOUNT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type          | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_PERCENTAGE_FEE | 10.00           |
    Then Loan Charges tab has the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 25.0 | 0.0  | 0.0    | 25.0        |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "01 January 2026"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026  | 750.0           | 250.0         | 0.0      | 25.0 | 0.0       | 275.0 | 275.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026   |                  | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |                  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 25.0 | 0.0       | 1025.0 | 275.0 | 0.0        | 0.0  | 750.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 25.0 | 25.0 | 0.0    | 0.0         |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119545
  Scenario: Verify skipDownPaymentFee deactivates fee on auto down payment disbursement
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount skipping down payment fee
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026   |                  | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |                  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
    When Admin adds "LOAN_DOWN_PAYMENT_FEE" due date charge with "01 January 2026" due date and 10.0 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment     | 260.0  | 250.0     | 0.0      | 10.0 | 0.0       | 750.0        |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026 | 750.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026   |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 260.0 | 0.0        | 0.0  | 750.0       |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119546
  Scenario: Verify flat down payment fee with manual down payment repayment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2026   |           | 750.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 2  | 15   | 16 January 2026   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    And Customer makes "DOWN_PAYMENT" repayment on "01 January 2026" with 260 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026 | 750.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026   |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 260.0 | 0.0        | 0.0  | 750.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Repayment        | 260.0  | 250.0     | 0.0      | 10.0 | 0.0       | 750.0        |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119547
  Scenario: Verify waive flat down payment fee without auto down payment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2026   |           | 750.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 2  | 15   | 16 January 2026   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    And Admin waives the last unpaid "LOAN_DOWN_PAYMENT_FEE" type charge which is due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 10.0   | 0.0         |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2026   |           | 750.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 January 2026   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Waive loan charges | 10.0   | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119548
  Scenario: Verify multiple flat down payment fees on same DP period with manual repayment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin adds "LOAN_DOWN_PAYMENT_FEE" due date charge with "01 January 2026" due date and 10.0 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2026   |           | 750.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 0.0  | 0.0        | 0.0  | 270.0       |
      | 2  | 15   | 16 January 2026   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    And Customer makes "DOWN_PAYMENT" repayment on "01 January 2026" with 270 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026 | 750.0           | 250.0         | 0.0      | 20.0 | 0.0       | 270.0 | 270.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026   |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Repayment        | 270.0  | 250.0     | 0.0      | 20.0 | 0.0       | 750.0        |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119549
  Scenario: Verify multi-disburse with auto down payment and flat fee
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 January 2026  | Down Payment     | 135.0  | 125.0     | 0.0      | 10.0 | 0.0       | 375.0        |
    When Admin sets the business date to "02 January 2026"
    When Admin successfully disburse the loan on "02 January 2026" with "500" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "02 January 2026"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 02 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 January 2026  | Down Payment     | 135.0  | 125.0     | 0.0      | 10.0 | 0.0       | 375.0        |
      | 02 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 875.0        |
      | 02 January 2026  | Down Payment     | 135.0  | 125.0     | 0.0      | 10.0 | 0.0       | 750.0        |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |                 | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026 | 375.0           | 125.0         | 0.0      | 10.0 | 0.0       | 135.0 | 135.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 02 January 2026   |                 | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 02 January 2026   | 02 January 2026 | 750.0           | 125.0         | 0.0      | 10.0 | 0.0       | 135.0 | 135.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 16 January 2026   |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 31 January 2026   |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 5  | 15   | 15 February 2026  |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 270.0 | 0.0        | 0.0  | 750.0       |
    When Loan Pay-off is made on "02 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119550
  Scenario: Verify undo disbursal with auto down payment fee
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment     | 260.0  | 250.0     | 0.0      | 10.0 | 0.0       | 750.0        |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026 | 750.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026   |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |

  @TestRailId:C119551
  Scenario: Verify undo Down Payment transaction leaves fee unpaid
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    When Customer undo "1"th "Down Payment" transaction made on "01 January 2026"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 260.0  | 250.0     | 0.0      | 10.0 | 0.0       | 750.0        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2026   |           | 750.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 2  | 15   | 16 January 2026   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119552
  Scenario: Verify backdated reverse-replay after undo Down Payment with fee
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    When Admin sets the business date to "16 January 2026"
    And Customer makes "AUTOPAY" repayment on "16 January 2026" with 250 EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 260.0  | 250.0     | 0.0      | 10.0 | 0.0       | 750.0        | false    | false    |
      | 16 January 2026  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026 | 750.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026   | 16 January 2026 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 January 2026   |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    When Customer undo "1"th "Down Payment" transaction made on "01 January 2026"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 260.0  | 250.0     | 0.0      | 10.0 | 0.0       | 750.0        | true     | false    |
      | 16 January 2026  | Repayment        | 250.0  | 240.0     | 0.0      | 10.0 | 0.0       | 760.0        | false    | true     |
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2026   |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2026   |                 | 750.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 250.0 | 0.0        | 250.0 | 10.0        |
      | 2  | 15   | 16 January 2026   |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 3  | 15   | 31 January 2026   |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 15   | 15 February 2026  |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
    When Loan Pay-off is made on "16 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119553
  Scenario: Verify charge-off after auto down payment fee paid
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment     | 260.0  | 250.0     | 0.0      | 10.0 | 0.0       | 750.0        |
    When Admin sets the business date to "10 January 2026"
    And Admin does charge-off the loan on "10 January 2026"
    Then Loan marked as charged-off on "10 January 2026"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment     | 260.0  | 250.0     | 0.0      | 10.0 | 0.0       | 750.0        |
      | 10 January 2026  | Charge-off       | 750.0  | 750.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name         | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable     |       | 750.0  |
      | EXPENSE | 744007       | Credit Loss/Bad Debt | 750.0 |        |

  @TestRailId:C119554
  Scenario: Verify accrual for unpaid down payment fee via inline COB
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2026   |           | 750.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 2  | 15   | 16 January 2026   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "02 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has a transaction with date: "01 January 2026", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Accrual          | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    When Loan Pay-off is made on "02 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119555
  Scenario: Verify charge adjustment after auto down payment fee paid
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    When Admin makes a charge adjustment for the last "LOAN_DOWN_PAYMENT_FEE" type charge which is due on "01 January 2026" with 5 EUR transaction amount and externalId ""
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment      | 260.0  | 250.0     | 0.0      | 10.0 | 0.0       | 750.0        |
      | 01 January 2026  | Charge Adjustment | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 745.0        |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026 | 750.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 260.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026   |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 5.0   | 5.0        | 0.0  | 245.0       |
      | 3  | 15   | 31 January 2026   |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 265.0 | 5.0        | 0.0  | 745.0       |
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 5.0    |
      | INCOME | 404007       | Fee Income       | 5.0   |        |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119556
  Scenario: Verify skipDownPaymentFee with percentage fee then manual add adjusts auto down payment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" with "PERCENTAGE_AMOUNT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type          | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_PERCENTAGE_FEE | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount skipping down payment fee
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026   |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    When Admin adds "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" due date charge with "01 January 2026" due date and 10.0 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 25.0 | 25.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment     | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026 | 750.0           | 250.0         | 0.0      | 25.0 | 0.0       | 275.0 | 275.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026   |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119557
  Scenario: Verify percentage down payment fee with manual down payment repayment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" with "PERCENTAGE_AMOUNT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type          | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_PERCENTAGE_FEE | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 25.0 | 0.0  | 0.0    | 25.0        |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2026   |           | 750.0           | 250.0         | 0.0      | 25.0 | 0.0       | 275.0 | 0.0  | 0.0        | 0.0  | 275.0       |
      | 2  | 15   | 16 January 2026   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    And Customer makes "DOWN_PAYMENT" repayment on "01 January 2026" with 275 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 25.0 | 25.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026 | 750.0           | 250.0         | 0.0      | 25.0 | 0.0       | 275.0 | 275.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2026   |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Repayment        | 275.0  | 250.0     | 0.0      | 25.0 | 0.0       | 750.0        |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119558
  Scenario: Verify multi-disburse with auto down payment and percentage fee
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" with "PERCENTAGE_AMOUNT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type          | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_PERCENTAGE_FEE | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 12.5 | 12.5 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 January 2026  | Down Payment     | 137.5  | 125.0     | 0.0      | 12.5 | 0.0       | 375.0        |
    When Admin sets the business date to "02 January 2026"
    When Admin successfully disburse the loan on "02 January 2026" with "500" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "02 January 2026"
    Then Loan Charges tab has the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 12.5 | 12.5 | 0.0    | 0.0         |
      | Down Payment percentage fee | false     | Down Payment   | 02 January 2026 | % Amount         | 12.5 | 12.5 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 January 2026  | Down Payment     | 137.5  | 125.0     | 0.0      | 12.5 | 0.0       | 375.0        |
      | 02 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 875.0        |
      | 02 January 2026  | Down Payment     | 137.5  | 125.0     | 0.0      | 12.5 | 0.0       | 750.0        |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |                 | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026 | 375.0           | 125.0         | 0.0      | 12.5 | 0.0       | 137.5 | 137.5 | 0.0        | 0.0  | 0.0         |
      |    |      | 02 January 2026   |                 | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 02 January 2026   | 02 January 2026 | 750.0           | 125.0         | 0.0      | 12.5 | 0.0       | 137.5 | 137.5 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 16 January 2026   |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 31 January 2026   |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 5  | 15   | 15 February 2026  |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 25.0 | 0.0       | 1025.0 | 275.0 | 0.0        | 0.0  | 750.0       |
    When Loan Pay-off is made on "02 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119559
  Scenario: Verify waive percentage down payment fee without auto down payment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" with "PERCENTAGE_AMOUNT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type          | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_PERCENTAGE_FEE | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment percentage fee" charge due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 25.0 | 0.0  | 0.0    | 25.0        |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2026   |           | 750.0           | 250.0         | 0.0      | 25.0 | 0.0       | 275.0 | 0.0  | 0.0        | 0.0  | 275.0       |
      | 2  | 15   | 16 January 2026   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    And Admin waives the last unpaid "LOAN_DOWN_PAYMENT_PERCENTAGE_FEE" type charge which is due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name                        | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment percentage fee | false     | Down Payment   | 01 January 2026 | % Amount         | 25.0 | 0.0  | 25.0   | 0.0         |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2026   |           | 750.0           | 250.0         | 0.0      | 25.0 | 0.0       | 275.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 January 2026   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 25.0 | 0.0       | 1025.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Waive loan charges | 25.0   | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Loan Pay-off is made on "01 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119560
  Scenario: Verify waive flat down payment fee after accrual
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    When Admin sets the business date to "02 January 2026"
    When Admin runs inline COB job for Loan
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 January 2026" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |
    And Admin waives the last unpaid "LOAN_DOWN_PAYMENT_FEE" type charge which is due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 10.0   | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement       | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Accrual            | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 0.0          |
      | 01 January 2026  | Waive loan charges | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 1000.0       |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2026   |           | 750.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 January 2026   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has a "WAIVE_CHARGES" transaction with date "01 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit | Credit |
      | EXPENSE | e4           | Written off             | 10.0  |        |
      | ASSET   | 112603       | Interest/Fee Receivable |       | 10.0   |
    When Loan Pay-off is made on "02 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119561
  Scenario: Verify charge-off with unpaid flat down payment fee
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Loan Charges tab has the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2026   |           | 750.0           | 250.0         | 0.0      | 10.0 | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 2  | 15   | 16 January 2026   |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2026   |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2026  |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "10 January 2026"
    And Admin does charge-off the loan on "10 January 2026"
    Then Loan marked as charged-off on "10 January 2026"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2026  | Charge-off       | 1010.0 | 1000.0    | 0.0      | 10.0 | 0.0       | 0.0          |
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "10 January 2026" which has the following Journal entries:
      | Type    | Account code | Account name            | Debit  | Credit |
      | ASSET   | 112601       | Loans Receivable        |        | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable |        | 10.0   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt    | 1000.0 |        |
      | INCOME  | 404008       | Fee Charge Off          | 10.0   |        |

  @TestRailId:C119562
  Scenario Outline: Verify creating Down payment charge with unsupported calculation type fails
    Then Creating loan charge with "DOWN_PAYMENT" chargeTimeType and "<calc_type>" chargeCalculationType results an error with the following data:
      | httpCode | errorMessage                                                    |
      | 400      | The parameter `chargeCalculationType` must be one of [ 1, 2 ] . |

    Examples:
      | calc_type                             |
      | PERCENTAGE_DISBURSEMENT_AMOUNT        |
      | PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST  |
      | PERCENTAGE_INTEREST                   |

  @TestRailId:C119563
  Scenario Outline: Verify creating Down payment charge on non-loan product fails
    Then Creating "<applies_to>" charge with "DOWN_PAYMENT" chargeTimeType and "FLAT" chargeCalculationType results an error with the following data:
      | httpCode | errorMessage                                                                 |
      | 400      | <error_message>                                                              |

    Examples:
      | applies_to | error_message                                                                    |
      | SAVINGS    | The parameter `chargeTimeType` must be one of [ 2, 3, 4, 5, 6, 7, 10, 11, 16 ] . |
      | CLIENT     | The parameter `chargeTimeType` must be one of [ 2 ] .                            |

  @TestRailId:C119564
  Scenario: Verify add Down payment fee fails when loan product has no down payment
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Admin failed to add "LOAN_DOWN_PAYMENT_FEE" due date charge with "01 January 2026" due date and 10.0 EUR transaction amount and got an error "Down payment charge cannot be added because down payment is not enabled."

  @TestRailId:C119565
  Scenario: Verify add Down payment fee fails when due date has no matching down payment period
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "01 January 2026"
    Then Admin failed to add "LOAN_DOWN_PAYMENT_FEE" due date charge with "15 January 2026" due date and 10.0 EUR transaction amount and got an error "Down payment charge due date must match an existing down payment installment."

  @TestRailId:C119566
  Scenario: Verify down payment fee is created only for the actual disbursement date when the loan is disbursed later than expected
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin sets the business date to "02 January 2026"
    When Admin successfully disburse the loan on "02 January 2026" with "1000" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 02 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 02 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2026  | Down Payment     | 260.0  | 250.0     | 0.0      | 10.0 | 0.0       | 750.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 260.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C119567
  Scenario: Verify undo last disbursal removes the down payment fee of the undone tranche and re-disbursement creates a single new fee
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    When Admin successfully disburse the loan on "02 January 2026" with "500" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 02 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    When Admin successfully undo last disbursal
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 01 January 2026  | Down Payment     | 135.0  | 125.0     | 0.0      | 10.0 | 0.0       | 375.0        | false    | false    |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0.0      | 10.0 | 0.0       | 510.0 | 135.0 | 0.0        | 0.0  | 375.0       |
    When Admin sets the business date to "03 January 2026"
    When Admin successfully disburse the loan on "03 January 2026" with "500" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 03 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 270.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C119568
  Scenario: Verify skipDownPaymentFee on a second tranche disbursed on a later day creates no fee for that tranche
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    When Admin sets the business date to "02 January 2026"
    When Admin successfully disburse the loan on "02 January 2026" with "500" EUR transaction amount skipping down payment fee
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 January 2026  | Down Payment     | 135.0  | 125.0     | 0.0      | 10.0 | 0.0       | 375.0        |
      | 02 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 875.0        |
      | 02 January 2026  | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 260.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C119569
  Scenario: Verify loan application with two expected tranches and a down payment fee is accepted with auto-generated external ids
    Given Global configuration "enable-auto-generated-external-id" is enabled
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_EXPECTED_TRANCHES" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and disbursements details and following data:
      | LoanProduct                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_EXPECTED_TRANCHES | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.0          | 01 January 2026                | 600.0                      | 20 January 2026                | 400.0                      |
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Down Payment Fee | false     | Down Payment   | 20 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |

  @TestRailId:C119570 @RestoreAutoGeneratedExternalId
  Scenario: Verify loan application with two expected tranches and a down payment fee can be modified resending and moving its disbursement details
    Given Global configuration "enable-auto-generated-external-id" is disabled
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_EXPECTED_TRANCHES" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and disbursements details and following data:
      | LoanProduct                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_EXPECTED_TRANCHES | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.0          | 01 January 2026                | 600.0                      | 15 January 2026                | 400.0                      |
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Down Payment Fee | false     | Down Payment   | 15 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    When Admin modifies the loan resending its expected disbursement details and charges
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Down Payment Fee | false     | Down Payment   | 15 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    When Admin modifies the loan resending its charges and moving the expected disbursement date of its last tranche to "20 January 2026"
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Down Payment Fee | false     | Down Payment   | 20 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Global configuration "enable-auto-generated-external-id" is enabled

  @TestRailId:C119571
  Scenario: Verify each tranche disbursed on the same day gets its own down payment fee
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 January 2026  | Down Payment     | 135.0  | 125.0     | 0.0      | 10.0 | 0.0       | 375.0        |
      | 01 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 875.0        |
      | 01 January 2026  | Down Payment     | 135.0  | 125.0     | 0.0      | 10.0 | 0.0       | 750.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 270.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C119572
  Scenario: Verify the original auto down payment transaction keeps its posted amount after a down payment fee is added and reverse-replayed
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount skipping down payment fee
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    Then Store "Down Payment" transaction created on "01 January 2026" date as "1"th transaction
    When Admin adds "LOAN_DOWN_PAYMENT_FEE" due date charge with "01 January 2026" due date and 10.0 EUR transaction amount
    Then "Down Payment" transaction on "01 January 2026" got reverse-replayed on "01 January 2026"
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment     | 260.0  | 250.0     | 0.0      | 10.0 | 0.0       | 750.0        |
    Then Loan Transactions tab has a "DOWN_PAYMENT" transaction with date "01 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 260.0 |        |
    Then The stored "1"th transaction, retrieved by id, has the following data:
      | Amount | Principal | Interest | Fees |
      | 250.0  | 250.0     | 0.0      | 0.0  |

  @TestRailId:C119573 @RestoreAutoGeneratedExternalId
  Scenario: Verify the fee of the first expected tranche is paid by its automatic down payment while the second tranche fee stays outstanding
    Given Global configuration "enable-auto-generated-external-id" is disabled
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_EXPECTED_TRANCHES" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and disbursements details and following data:
      | LoanProduct                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_EXPECTED_TRANCHES | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.0          | 01 January 2026                | 600.0                      | 20 January 2026                | 400.0                      |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "600" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        |
      | 01 January 2026  | Down Payment     | 160.0  | 150.0     | 0.0      | 10.0 | 0.0       | 450.0        |
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 20 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Global configuration "enable-auto-generated-external-id" is enabled

  @TestRailId:C119576
  Scenario: Verify a down payment fee attached only to the loan product is not charged when the loan application does not include it
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_DP_FEE_ON_PRODUCT" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADV_PMT_ALLOC_DP_FEE_ON_PRODUCT | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Charges tab has exactly the following data:
      | Name | isPenalty | Payment due at | Due as of | Calculation type | Due | Paid | Waived | Outstanding |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    Then Loan Charges tab has exactly the following data:
      | Name | isPenalty | Payment due at | Due as of | Calculation type | Due | Paid | Waived | Outstanding |
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name | isPenalty | Payment due at | Due as of | Calculation type | Due | Paid | Waived | Outstanding |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C119577
  Scenario: Verify LoanAddChargeBusinessEvent is raised for the down payment fee created automatically for a later tranche
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    When Admin successfully disburse the loan on "02 January 2026" with "500" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Down Payment Fee | false     | Down Payment   | 02 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then LoanAddChargeBusinessEvent is raised for the "Down Payment Fee" charge due on "02 January 2026"

  @TestRailId:C119594
  Scenario: Verify a loan whose last disbursal was undone closes when its outstanding balance is repaid
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    When Admin successfully disburse the loan on "02 January 2026" with "500" EUR transaction amount
    When Admin successfully undo last disbursal
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan has 375.0 outstanding amount
    When Admin sets the business date to "03 January 2026"
    And Customer makes "AUTOPAY" repayment on "03 January 2026" with 375 EUR transaction amount
    Then Loan has 0.0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C119595
  Scenario: Verify re-disbursement with skipDownPaymentFee after undo last disbursal leaves no down payment fee for the undone tranche
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    When Admin sets the business date to "02 January 2026"
    When Admin successfully disburse the loan on "02 January 2026" with "500" EUR transaction amount
    When Admin successfully undo last disbursal
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan has 375.0 outstanding amount
    When Admin sets the business date to "03 January 2026"
    When Admin successfully disburse the loan on "03 January 2026" with "500" EUR transaction amount skipping down payment fee
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 260.0 | 0.0        | 0.0  | 750.0       |
    And Customer makes "AUTOPAY" repayment on "03 January 2026" with 750 EUR transaction amount
    Then Loan has 0.0 outstanding amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C119596
  Scenario: Verify skipDownPaymentFee is honoured on a partial first disbursement
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount skipping down payment fee
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 January 2026  | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 125.0 | 0.0        | 0.0  | 375.0       |

  @TestRailId:C119597
  Scenario: Verify a second tranche creates its own down payment fee while the fee of the first tranche is still unpaid
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    When Admin sets the business date to "02 January 2026"
    When Admin successfully disburse the loan on "02 January 2026" with "500" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
      | Down Payment Fee | false     | Down Payment   | 02 January 2026 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 20.0 | 0.0       | 1020.0 | 0.0  | 0.0        | 0.0  | 1020.0      |

  @TestRailId:C119598
  Scenario: Verify undo last disbursal of a same-day tranche disbursed with skipDownPaymentFee keeps the fee of the earlier tranche
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount
    When Admin successfully disburse the loan on "01 January 2026" with "500" EUR transaction amount skipping down payment fee
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    When Admin successfully undo last disbursal
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 01 January 2026  | Down Payment     | 135.0  | 125.0     | 0.0      | 10.0 | 0.0       | 375.0        | false    | false    |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 500.0         | 0.0      | 10.0 | 0.0       | 510.0 | 135.0 | 0.0        | 0.0  | 375.0       |

  @TestRailId:C119599
  Scenario: Verify re-disbursement on another day after undo disbursal creates a single down payment fee
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    When Admin sets the business date to "02 January 2026"
    When Admin successfully disburse the loan on "02 January 2026" with "1000" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 02 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 02 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 02 January 2026  | Down Payment     | 260.0  | 250.0     | 0.0      | 10.0 | 0.0       | 750.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 260.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:C119600
  Scenario: Verify re-disbursement on the same day after undo disbursal does not charge the down payment fee twice
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin updates charge "LOAN_DOWN_PAYMENT_FEE" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type | charge amount % |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2026   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DOWN_PAYMENT_FEE   | 10.00           |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan Charges tab has exactly the following data:
      | Name             | isPenalty | Payment due at | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Down Payment Fee | false     | Down Payment   | 01 January 2026 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2026  | Down Payment     | 260.0  | 250.0     | 0.0      | 10.0 | 0.0       | 750.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 10.0 | 0.0       | 1010.0 | 260.0 | 0.0        | 0.0  | 750.0       |
