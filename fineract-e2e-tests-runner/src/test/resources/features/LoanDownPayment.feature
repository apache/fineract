@LoanDownPaymentFeature
Feature: Loan DownPayment


  Scenario: Single disbursement normal flow - auto enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "01 February 2022"
    And Customer makes "AUTOPAY" repayment on "01 February 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    |                  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 500.0 | 0          | 0    | 500         |
    When Admin sets the business date to "15 February 2023"
    And Customer makes "AUTOPAY" repayment on "15 February 2022" with 400 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    | 15 February 2022 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2022    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 150.0 | 150.0      | 0.0  | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 900.0 | 400        | 0    | 100         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 15 February 2022 | Repayment        | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 100.0        |


  Scenario: Single disbursement normal flow - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0.0  | 0          | 0    | 1000        |
    And Customer makes "DOWN_PAYMENT" repayment on "01 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "01 February 2022"
    And Customer makes "AUTOPAY" repayment on "01 February 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    |                  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 500.0 | 0          | 0    | 500         |
    When Admin sets the business date to "15 February 2023"
    And Customer makes "AUTOPAY" repayment on "15 February 2022" with 400 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    | 15 February 2022 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2022    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 150.0 | 150.0      | 0.0  | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 900.0 | 400        | 0    | 100         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 15 February 2022 | Repayment        | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 100.0        |


  Scenario: Single disbursement charge flow - auto enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2022" due date and 50 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 250.0 | 0          | 0    | 800         |
    When Admin sets the business date to "01 February 2022"
    And Customer makes "AUTOPAY" repayment on "01 February 2022" with 300 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 300.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    |                  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 550.0 | 0          | 0    | 500         |
    When Admin sets the business date to "15 February 2023"
    And Customer makes "AUTOPAY" repayment on "15 February 2022" with 400 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 300.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    | 15 February 2022 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2022    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 150.0 | 150.0      | 0.0  | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 950.0 | 400        | 0    | 100         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Repayment        | 300.0  | 250.0     | 0.0      | 0.0  | 50.0      | 500.0        |
      | 15 February 2022 | Repayment        | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 100.0        |


  Scenario: Single disbursement charge flow - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2022" due date and 50 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 0.0  | 0          | 0    | 1050        |
    And Customer makes "DOWN_PAYMENT" repayment on "01 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 250.0 | 0          | 0    | 800         |
    When Admin sets the business date to "01 February 2022"
    And Customer makes "AUTOPAY" repayment on "01 February 2022" with 300 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 300.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    |                  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 550.0 | 0          | 0    | 500         |
    When Admin sets the business date to "15 February 2023"
    And Customer makes "AUTOPAY" repayment on "15 February 2022" with 400 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 300.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    | 15 February 2022 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2022    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 150.0 | 150.0      | 0.0  | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 950.0 | 400        | 0    | 100         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Repayment        | 300.0  | 250.0     | 0.0      | 0.0  | 50.0      | 500.0        |
      | 15 February 2022 | Repayment        | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 100.0        |


  Scenario: Double disbursement different day - auto enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2022" due date and 50 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 250.0 | 0          | 0    | 800         |
    When Admin sets the business date to "10 January 2022"
    When Admin successfully disburse the loan on "10 January 2022" with "400" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 10 January 2022  |                 | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 10 January 2022  | 10 January 2022 | 1050.0          | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                 | 700.0           | 350.0         | 0.0      | 0.0  | 50.0      | 400.0 | 0.0   | 0.0        | 0.0  | 400.0       |
      | 4  | 28   | 01 March 2022    |                 | 350.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1400.0        | 0        | 0    | 50        | 1450.0 | 350.0 | 0          | 0    | 1100        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 10 January 2022  | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1150.0       |
      | 10 January 2022  | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1050.0       |


  Scenario: Double disbursement different day - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2022" due date and 50 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 0.0  | 0          | 0    | 1050        |
    And Customer makes "DOWN_PAYMENT" repayment on "01 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 250.0 | 0          | 0    | 800         |
    When Admin sets the business date to "10 January 2022"
    When Admin successfully disburse the loan on "10 January 2022" with "400" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 10 January 2022  |                 | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 10 January 2022  |                 | 1050.0          | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0   | 0.0        | 0.0  | 100.0       |
      | 3  | 31   | 01 February 2022 |                 | 700.0           | 350.0         | 0.0      | 0.0  | 50.0      | 400.0 | 0.0   | 0.0        | 0.0  | 400.0       |
      | 4  | 28   | 01 March 2022    |                 | 350.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1400.0        | 0        | 0    | 50        | 1450.0 | 250.0 | 0          | 0    | 1200        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 10 January 2022  | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1150.0       |



  Scenario: Double disbursement on installment day - auto enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2022" due date and 50 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 250.0 | 0          | 0    | 800         |
    When Admin sets the business date to "01 February 2022"
    When Admin successfully disburse the loan on "01 February 2022" with "400" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2022 |                  | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 01 February 2022 | 01 February 2022 | 1050.0          | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                  | 700.0           | 350.0         | 0.0      | 0.0  | 50.0      | 400.0 | 0.0   | 0.0        | 0.0  | 400.0       |
      | 4  | 28   | 01 March 2022    |                  | 350.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 5  | 31   | 01 April 2022    |                  | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1400.0        | 0        | 0    | 50        | 1450.0 | 350.0 | 0          | 0    | 1100        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1150.0       |
      | 01 February 2022 | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1050.0       |



  Scenario: Double disbursement on installment day - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2022" due date and 50 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 0.0  | 0          | 0    | 1050        |
    And Customer makes "DOWN_PAYMENT" repayment on "01 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 250.0 | 0          | 0    | 800         |
    When Admin sets the business date to "01 February 2022"
    When Admin successfully disburse the loan on "01 February 2022" with "400" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2022 |                 | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 01 February 2022 |                 | 1050.0          | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0   | 0.0        | 0.0  | 100.0       |
      | 3  | 31   | 01 February 2022 |                 | 700.0           | 350.0         | 0.0      | 0.0  | 50.0      | 400.0 | 0.0   | 0.0        | 0.0  | 400.0       |
      | 4  | 28   | 01 March 2022    |                 | 350.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1400.0        | 0        | 0    | 50        | 1450.0 | 250.0 | 0          | 0    | 1200        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1150.0       |


  Scenario: Downpayment with loan adjustment - auto enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2022" due date and 50 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 250.0 | 0          | 0    | 800         |
    When Admin sets the business date to "10 January 2022"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 February 2022   | 10 January 2022 | 01 April 2022   |                  |                 |            |                 |
    When Admin sets the business date to "01 April 2022"
    When Admin successfully disburse the loan on "01 April 2022" with "400" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022 | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 April 2022   |                 | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 01 April 2022   | 01 April 2022   | 1050.0          | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 90   | 01 April 2022   |                 | 700.0           | 350.0         | 0.0      | 0.0  | 50.0      | 400.0 | 0.0   | 0.0        | 0.0  | 400.0       |
      | 4  | 30   | 01 May 2022     |                 | 350.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 5  | 31   | 01 June 2022    |                 | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1400.0        | 0        | 0    | 50        | 1450.0 | 350.0 | 0          | 0    | 1100        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 April 2022    | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1150.0       |
      | 01 April 2022    | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1050.0       |


  Scenario: Downpayment with loan adjustment - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2022" due date and 50 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 0.0  | 0          | 0    | 1050        |
    And Customer makes "DOWN_PAYMENT" repayment on "01 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 250.0 | 0          | 0    | 800         |
    When Admin sets the business date to "10 January 2022"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 February 2022   | 10 January 2022 | 01 April 2022   |                  |                 |            |                 |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022 | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 90   | 01 April 2022   |                 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 30   | 01 May 2022     |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 June 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 250.0 | 0          | 0    | 800         |
    When Admin sets the business date to "01 April 2022"
    When Admin successfully disburse the loan on "01 April 2022" with "400" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022 | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 April 2022   |                 | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 01 April 2022   |                 | 1050.0          | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0   | 0.0        | 0.0  | 100.0       |
      | 3  | 90   | 01 April 2022   |                 | 700.0           | 350.0         | 0.0      | 0.0  | 50.0      | 400.0 | 0.0   | 0.0        | 0.0  | 400.0       |
      | 4  | 30   | 01 May 2022     |                 | 350.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 5  | 31   | 01 June 2022    |                 | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1400.0        | 0        | 0    | 50        | 1450.0 | 250.0 | 0          | 0    | 1200        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 April 2022    | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1150.0       |


  Scenario: Downpayment creation date - auto enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "10 January 2023"
    When Admin sets the business date to "10 January 2022"
    When Admin successfully disburse the loan on "10 January 2022" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "10 January 2022" due date and 50 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 10 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 10 January 2022  | 10 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 10 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 10 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 10 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 250.0 | 0          | 0    | 800         |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 10 February 2022   | 10 January 2022 | 10 April 2022   |                  |                 |            |                 |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 10 January 2022 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 10 January 2022 | 10 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 90   | 10 April 2022   |                 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 30   | 10 May 2022     |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 10 June 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 250.0 | 0          | 0    | 800         |
    When Admin sets the business date to "01 April 2022"
    When Admin successfully disburse the loan on "01 April 2022" with "400" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 10 January 2022 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 10 January 2022 | 10 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 April 2022   |                 | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 01 April 2022   | 01 April 2022   | 1050.0          | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 90   | 10 April 2022   |                 | 700.0           | 350.0         | 0.0      | 0.0  | 50.0      | 400.0 | 0.0   | 0.0        | 0.0  | 400.0       |
      | 4  | 30   | 10 May 2022     |                 | 350.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 5  | 31   | 10 June 2022    |                 | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1400.0        | 0        | 0    | 50        | 1450.0 | 350.0 | 0          | 0    | 1100        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 10 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 April 2022    | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1150.0       |
      | 01 April 2022    | Down Payment     | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1050.0       |


  Scenario: Downpayment creation date - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "10 January 2023"
    When Admin sets the business date to "10 January 2022"
    When Admin successfully disburse the loan on "10 January 2022" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "10 January 2022" due date and 50 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 10 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 10 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 10 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 10 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 10 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 0.0  | 0          | 0    | 1050        |
    And Customer makes "DOWN_PAYMENT" repayment on "10 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 10 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 10 January 2022  | 10 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 10 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 10 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 10 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 250.0 | 0          | 0    | 800         |
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 10 February 2022   | 10 January 2022 | 10 April 2022   |                  |                 |            |                 |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 10 January 2022 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 10 January 2022 | 10 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 90   | 10 April 2022   |                 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 30   | 10 May 2022     |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 10 June 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 250.0 | 0          | 0    | 800         |
    When Admin sets the business date to "01 April 2022"
    When Admin successfully disburse the loan on "01 April 2022" with "400" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 10 January 2022 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 10 January 2022 | 10 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 April 2022   |                 | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 01 April 2022   |                 | 1050.0          | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0   | 0.0        | 0.0  | 100.0       |
      | 3  | 90   | 10 April 2022   |                 | 700.0           | 350.0         | 0.0      | 0.0  | 50.0      | 400.0 | 0.0   | 0.0        | 0.0  | 400.0       |
      | 4  | 30   | 10 May 2022     |                 | 350.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 5  | 31   | 10 June 2022    |                 | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1400.0        | 0        | 0    | 50        | 1450.0 | 250.0 | 0          | 0    | 1200        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 10 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 10 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 April 2022    | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1150.0       |


  Scenario: Double disbursement normal flow - auto enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "15 January 2022"
    When Admin successfully disburse the loan on "15 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  | 15 January 2022 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 500.0 | 0          | 0    | 1500        |
    When Admin sets the business date to "01 February 2022"
    And Customer makes "AUTOPAY" repayment on "01 February 2022" with 500 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  | 15 January 2022  | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 | 01 February 2022 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 500.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 28   | 01 March 2022    |                  | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                  | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 1000.0 | 0          | 0    | 1000        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 15 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1750.0       |
      | 15 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1500.0       |
      | 01 February 2022 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 1000.0       |


  Scenario: Double disbursement normal flow - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0.0  | 0          | 0    | 1000        |
    And Customer makes "AUTOPAY" repayment on "01 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "15 January 2022"
    When Admin successfully disburse the loan on "15 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  |                 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 250.0 | 0          | 0    | 1750        |
    When Admin sets the business date to "01 February 2022"
    And Customer makes "AUTOPAY" repayment on "01 February 2022" with 500 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      |    |      | 15 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 2  | 0    | 15 January 2022  | 01 February 2022 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 3  | 31   | 01 February 2022 |                  | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 250.0 | 0.0        | 0.0   | 250.0       |
      | 4  | 28   | 01 March 2022    |                  | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
      | 5  | 31   | 01 April 2022    |                  | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 750.0 | 0          | 250  | 1250        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 15 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1750.0       |
      | 01 February 2022 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 1250.0       |


  Scenario: Double disbursement same day - auto enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "15 January 2022"
    When Admin successfully disburse the loan on "15 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  | 15 January 2022 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 500.0 | 0          | 0    | 1500        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 15 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1750.0       |
      | 15 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1500.0       |


  Scenario: Double disbursement same day - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0    | 0          | 0    | 1000        |
    And Customer makes "AUTOPAY" repayment on "01 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "15 January 2022"
    When Admin successfully disburse the loan on "15 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  |                 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 250.0 | 0          | 0    | 1750        |
    And Customer makes "AUTOPAY" repayment on "15 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  | 15 January 2022 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 500.0 | 0          | 0    | 1500        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 15 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1750.0       |
      | 15 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1500.0       |


  Scenario: Double disbursement partial overpayment - auto enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "15 January 2022"
    When Admin successfully disburse the loan on "15 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  | 15 January 2022 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 500.0 | 0          | 0    | 1500        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 15 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1750.0       |
      | 15 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1500.0       |


  Scenario: Double disbursement partial overpayment - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0    | 0          | 0    | 1000        |
    And Customer makes "AUTOPAY" repayment on "01 January 2022" with 400 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 150.0 | 150.0      | 0.0  | 100.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 400.0 | 150        | 0    | 600         |
    When Admin sets the business date to "15 January 2022"
    When Admin successfully disburse the loan on "15 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  |                 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 150.0 | 150.0      | 0.0  | 100.0       |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 400.0 | 150        | 0    | 1600        |
    And Customer makes "AUTOPAY" repayment on "15 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  | 15 January 2022 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 150.0      | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 150.0 | 150.0      | 0.0  | 350.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 650.0 | 300        | 0    | 1350        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 600.0        |
      | 15 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1600.0       |
      | 15 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1350.0       |


  Scenario: Double disbursement full overpayment - auto enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    And Customer makes "AUTOPAY" repayment on "01 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 January 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 500.0 | 250        | 0    | 500         |
    When Admin sets the business date to "15 January 2022"
    When Admin successfully disburse the loan on "15 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  | 01 January 2022 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 250.0 | 250.0      | 0.0  | 250.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 750.0 | 500        | 0    | 1250        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 15 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       |
      | 15 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1250.0       |


  Scenario: Double disbursement full overpayment - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0    | 0          | 0    | 1000        |
    And Customer makes "AUTOPAY" repayment on "01 January 2022" with 500 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 January 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 500.0 | 250        | 0    | 500         |
    When Admin sets the business date to "15 January 2022"
    When Admin successfully disburse the loan on "15 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  | 01 January 2022 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 500.0 | 250        | 0    | 1500        |
    And Customer makes "AUTOPAY" repayment on "15 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  | 01 January 2022 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 250.0 | 250.0      | 0.0  | 250.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 750.0 | 500        | 0    | 1250        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 15 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       |
      | 15 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1250.0       |


  Scenario: Double disbursement after installment overdue - auto enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "15 February 2022"
    When Admin successfully disburse the loan on "15 February 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 01 February 2022 | 15 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      |    |      | 15 February 2022 |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 3  | 0    | 15 February 2022 |                  | 1250.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 28   | 01 March 2022    |                  | 750.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
      | 5  | 31   | 01 April 2022    |                  | 0.0             | 750.0         | 0.0      | 0.0  | 0.0       | 750.0 | 0.0   | 0.0        | 0.0   | 750.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 500.0 | 0          | 250  | 1500        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 15 February 2022 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1750.0       |
      | 15 February 2022 | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1500.0       |


  Scenario: Double disbursement after installment overdue - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0    | 0          | 0    | 1000        |
    And Customer makes "AUTOPAY" repayment on "01 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "15 February 2022"
    When Admin successfully disburse the loan on "15 February 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      |    |      | 15 February 2022 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 0    | 15 February 2022 |                 | 1250.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 28   | 01 March 2022    |                 | 750.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 750.0         | 0.0      | 0.0  | 0.0       | 750.0 | 0.0   | 0.0        | 0.0  | 750.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 250.0 | 0          | 0    | 1750        |
    And Customer makes "AUTOPAY" repayment on "15 February 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 01 February 2022 | 15 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      |    |      | 15 February 2022 |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 3  | 0    | 15 February 2022 |                  | 1250.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 28   | 01 March 2022    |                  | 750.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
      | 5  | 31   | 01 April 2022    |                  | 0.0             | 750.0         | 0.0      | 0.0  | 0.0       | 750.0 | 0.0   | 0.0        | 0.0   | 750.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 500.0 | 0          | 250  | 1500        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 15 February 2022 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1750.0       |
      | 15 February 2022 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1500.0       |


  Scenario: Double disbursement after installment 2 - auto enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "01 February 2022"
    And Customer makes "AUTOPAY" repayment on "01 February 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    |                  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 500.0 | 0          | 0    | 500         |
    When Admin sets the business date to "15 February 2022"
    When Admin successfully disburse the loan on "15 February 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 February 2022 |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 0    | 15 February 2022 | 15 February 2022 | 1250.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 28   | 01 March 2022    |                  | 750.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                  | 0.0             | 750.0         | 0.0      | 0.0  | 0.0       | 750.0 | 0.0   | 0.0        | 0.0  | 750.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 750.0 | 0          | 0    | 1250        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 15 February 2022 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       |
      | 15 February 2022 | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1250.0       |


  Scenario: Double disbursement after installment 2 - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0    | 0          | 0    | 1000        |
    And Customer makes "AUTOPAY" repayment on "01 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "01 February 2022"
    And Customer makes "AUTOPAY" repayment on "01 February 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    |                  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 500.0 | 0          | 0    | 500         |
    When Admin sets the business date to "15 February 2022"
    When Admin successfully disburse the loan on "15 February 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 February 2022 |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 0    | 15 February 2022 |                  | 1250.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 28   | 01 March 2022    |                  | 750.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                  | 0.0             | 750.0         | 0.0      | 0.0  | 0.0       | 750.0 | 0.0   | 0.0        | 0.0  | 750.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 500.0 | 0          | 0    | 1500        |
    And Customer makes "AUTOPAY" repayment on "15 February 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 February 2022 |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 0    | 15 February 2022 | 15 February 2022 | 1250.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 28   | 01 March 2022    |                  | 750.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                  | 0.0             | 750.0         | 0.0      | 0.0  | 0.0       | 750.0 | 0.0   | 0.0        | 0.0  | 750.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 750.0 | 0          | 0    | 1250        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 15 February 2022 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       |
      | 15 February 2022 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1250.0       |


  Scenario: Double disbursement normal overdue - auto enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250  | 0          | 0    | 750         |
    When Admin sets the business date to "15 January 2022"
    When Admin successfully disburse the loan on "15 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  | 15 January 2022 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 500  | 0          | 0    | 1500        |
    And Customer makes "AUTOPAY" repayment on "15 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  | 15 January 2022 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 250.0 | 250.0      | 0.0  | 250.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 750  | 250        | 0    | 1250        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 15 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1750.0       |
      | 15 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1500.0       |
      | 15 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1250.0       |


  Scenario: Double disbursement normal overdue - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0    | 0          | 0    | 1000        |
    When Admin sets the business date to "15 January 2022"
    When Admin successfully disburse the loan on "15 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      |    |      | 15 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 0    | 15 January 2022  |           | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 31   | 01 February 2022 |           | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
      | 4  | 28   | 01 March 2022    |           | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |           | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 0    | 0          | 0    | 2000        |
    And Customer makes "AUTOPAY" repayment on "15 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2022  | 15 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 2  | 0    | 15 January 2022  |                 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 250  | 0          | 250  | 1750        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       |
      | 15 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1750.0       |


  Scenario: Double disbursement on Duedate - auto enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "01 February 2022"
    When Admin successfully disburse the loan on "01 February 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2022 |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 01 February 2022 | 01 February 2022 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                  | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 4  | 28   | 01 March 2022    |                  | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                  | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 500  | 0          | 0    | 1500        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1750.0       |
      | 01 February 2022 | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1500.0       |


  Scenario: Double disbursement on Duedate - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0    | 0          | 0    | 1000        |
    And Customer makes "AUTOPAY" repayment on "01 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "01 February 2022"
    When Admin successfully disburse the loan on "01 February 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2022 |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 01 February 2022 |                 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 250  | 0          | 0    | 1750        |
    And Customer makes "AUTOPAY" repayment on "01 February 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2022 |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 01 February 2022 | 01 February 2022 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                  | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 4  | 28   | 01 March 2022    |                  | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                  | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 500  | 0          | 0    | 1500        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1750.0       |
      | 01 February 2022 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1500.0       |

  #  To recheck after verifying that loan product details are correct
  @Skip
  Scenario: Single disbursement with interest - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct               | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_INTEREST | 01 January 2022   | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 10.0     | 0.0  | 0.0       | 260.0 | 0.0  | 0.0        | 0.0  | 260.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 30       | 0    | 0         | 1030.0 | 0.0  | 0          | 0    | 1030        |

  #  To recheck after verifying that loan product details are correct
  @Skip
  Scenario: Single disbursement delinquency - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct               | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_INTEREST | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0.0  | 0          | 0    | 1000        |
    When Admin sets the business date to "5 January 2022"
    When Admin runs COB job
    Then Admin checks that delinquency range is: "RANGE_1" and has delinquentDate "2022-01-04"


  Scenario: Single disbursement with undo disbursement when auto downpayment is enabled without manual transaction after last downpayment
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"


  Scenario: Single disbursement with undo disbursement when auto downpayment is disabled without manual transaction after last downpayment
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0.0  | 0          | 0    | 1000        |
    And Customer makes "DOWN_PAYMENT" repayment on "01 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"


  Scenario: Single disbursement with undo disbursement when auto downpayment is disabled with manual transaction after last downpayment
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0.0  | 0          | 0    | 1000        |
    And Customer makes "DOWN_PAYMENT" repayment on "01 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "01 February 2022"
    And Customer makes "AUTOPAY" repayment on "01 February 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    |                  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 500.0 | 0          | 0    | 500         |
    When Admin sets the business date to "15 February 2023"
    And Customer makes "AUTOPAY" repayment on "15 February 2022" with 400 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    | 15 February 2022 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2022    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 150.0 | 150.0      | 0.0  | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 900.0 | 400        | 0    | 100         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 15 February 2022 | Repayment        | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"


  Scenario: Single disbursement with undo disbursement when auto downpayment is enabled with manual transaction after last downpayment
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "01 February 2022"
    And Customer makes "AUTOPAY" repayment on "01 February 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    |                  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 500.0 | 0          | 0    | 500         |
    When Admin sets the business date to "15 February 2023"
    And Customer makes "AUTOPAY" repayment on "15 February 2022" with 400 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 | 01 February 2022 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2022    | 15 February 2022 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2022    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 150.0 | 150.0      | 0.0  | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 900.0 | 400        | 0    | 100         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 15 February 2022 | Repayment        | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"



  Scenario: Multi disbursement with undo last disbursement when auto downpayment is enabled without manual transaction after last downpayment
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250  | 0          | 0    | 750         |
    When Admin sets the business date to "15 January 2022"
    When Admin successfully disburse the loan on "15 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  | 15 January 2022 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 500  | 0          | 0    | 1500        |
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250  | 0          | 0    | 750         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |



  Scenario: Multi disbursement with undo last disbursement when auto downpayment is disabled without manual transaction after last downpayment
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "2000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 0.0  | 0          | 0    | 1000        |
    And Customer makes "AUTOPAY" repayment on "01 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    When Admin sets the business date to "15 January 2022"
    When Admin successfully disburse the loan on "15 January 2022" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2022  |                 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 31   | 01 February 2022 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 4  | 28   | 01 March 2022    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 250.0 | 0          | 0    | 1750        |
    And Customer makes "AUTOPAY" repayment on "15 January 2022" with 250 EUR transaction amount
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |



  Scenario: Verify backdated disbursement - after down payment, before first installment due date, installment start date calculated by loan disbursement date
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2023   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "2000" amount and expected disbursement date on "01 January 2023"
    When Admin sets the business date to "05 January 2023"
    When Admin successfully disburse the loan on "05 January 2023" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "05 January 2023" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 05 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 05 January 2023  | 05 January 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 05 February 2023 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 05 March 2023    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 05 April 2023    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 05 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    When Admin sets the business date to "08 January 2023"
    When Admin successfully disburse the loan on "02 January 2023" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 02 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 02 January 2023  | 05 January 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      |    |      | 05 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 2  | 0    | 05 January 2023  |                 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 3  | 31   | 02 February 2023 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
      | 4  | 28   | 02 March 2023    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
      | 5  | 31   | 02 April 2023    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 250.0 | 0          | 250  | 1750        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 02 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       |
      | 05 January 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1750.0       |


  Scenario: Verify backdated disbursement - second disbursement placed on after down payment, after first installment paid on due date, and takes place before first installment due date
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2023   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "2000" amount and expected disbursement date on "01 January 2023"
    When Admin sets the business date to "05 January 2023"
    When Admin successfully disburse the loan on "05 January 2023" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "05 January 2023" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 05 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 05 January 2023  | 05 January 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 05 February 2023 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 05 March 2023    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 05 April 2023    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 05 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    When Admin sets the business date to "05 February 2023"
    And Customer makes "AUTOPAY" repayment on "05 February 2023" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 05 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 05 January 2023  | 05 January 2023  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 05 February 2023 | 05 February 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 05 March 2023    |                  | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 05 April 2023    |                  | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 500.0 | 0          | 0    | 500         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 05 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 05 February 2023 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 500.0        |
    When Admin sets the business date to "10 February 2023"
    When Admin successfully disburse the loan on "01 February 2023" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 05 January 2023  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 05 January 2023  | 05 January 2023  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      |    |      | 01 February 2023 |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 2  | 0    | 01 February 2023 | 05 February 2023 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 3  | 31   | 05 February 2023 |                  | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
      | 4  | 28   | 05 March 2023    |                  | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
      | 5  | 31   | 05 April 2023    |                  | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 500.0 | 0          | 250  | 1500        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 05 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2023 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1750.0       |
      | 05 February 2023 | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1500.0       |


  Scenario: Verify backdated disbursement - after down payment, before first installment due date, installment start date calculated by loan submitted on date
    When Admin sets repaymentStartDateType for "LP2_DOWNPAYMENT" loan product to "SUBMITTED_ON_DATE"
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2023   | 2000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2023" with "2000" amount and expected disbursement date on "01 January 2023"
    When Admin sets the business date to "05 January 2023"
    When Admin successfully disburse the loan on "05 January 2023" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "05 January 2023" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 05 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 05 January 2023  | 05 January 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2023 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2023    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2023    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 0         | 1000.0 | 250.0 | 0          | 0    | 750         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 05 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    When Admin sets the business date to "08 January 2023"
    When Admin successfully disburse the loan on "02 January 2023" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 02 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 02 January 2023  | 05 January 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      |    |      | 05 January 2023  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 2  | 0    | 05 January 2023  |                 | 1500.0          | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 3  | 31   | 01 February 2023 |                 | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
      | 4  | 28   | 01 March 2023    |                 | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
      | 5  | 31   | 01 April 2023    |                 | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0   | 0.0        | 0.0   | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 2000.0        | 0        | 0    | 0         | 2000.0 | 250.0 | 0          | 250  | 1750        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 02 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 January 2023  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       |
      | 05 January 2023  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 1750.0       |
    When Admin sets repaymentStartDateType for "LP2_DOWNPAYMENT" loan product to "DISBURSEMENT_DATE"


  Scenario: Verify correct Loan Repayment EMI Amount is calculated and is rounded off if loan product installmentAmountInMultiplesOf is set
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 1250           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1250" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1250" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1250.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 938.0           | 312.0         | 0.0      | 0.0  | 0.0       | 312.0 | 0.0  | 0.0        | 0.0  | 312.0       |
      | 2  | 31   | 01 February 2022 |           | 625.0           | 313.0         | 0.0      | 0.0  | 0.0       | 313.0 | 0.0  | 0.0        | 0.0  | 313.0       |
      | 3  | 28   | 01 March 2022    |           | 312.0           | 313.0         | 0.0      | 0.0  | 0.0       | 313.0 | 0.0  | 0.0        | 0.0  | 313.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 312.0         | 0.0      | 0.0  | 0.0       | 312.0 | 0.0  | 0.0        | 0.0  | 312.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1250.0        | 0        | 0    | 0         | 1250.0 | 0.0  | 0          | 0    | 1250        |


  Scenario: Verify Loan Repayment Due and Overdue events for downpayment
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Repayment Due Business Event is created
    When Admin sets the business date to "04 January 2022"
    When Admin runs inline COB job for Loan
    Then Loan Repayment Overdue Business Event is created


  Scenario: Verify that with multiple disbursement when last disbursement is reverted, downpayment will be reverted too - autopayment disabled
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 September 2023 | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 September 2023" with "1000" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "05 September 2023"
    And Customer makes "AUTOPAY" repayment on "05 September 2023" with 500 EUR transaction amount
    When Admin sets the business date to "11 September 2023"
    When Admin successfully disburse the loan on "11 September 2023" with "500" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 September 2023 |                   | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 September 2023 | 05 September 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      |    |      | 11 September 2023 |                   | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 2  | 0    | 11 September 2023 | 05 September 2023 | 1125.0          | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0   | 0.0         |
      | 3  | 15   | 16 September 2023 |                   | 750.0           | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 125.0 | 125.0      | 0.0   | 250.0       |
      | 4  | 15   | 01 October 2023   |                   | 375.0           | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0   | 375.0       |
      | 5  | 15   | 16 October 2023   |                   | 0.0             | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0   | 375.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1500.0        | 0        | 0.0  | 0         | 1500.0 | 500.0 | 250.0      | 250.0 | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 September 2023 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
      | 11 September 2023 | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 September 2023 |                   | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 September 2023 | 05 September 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 15   | 16 September 2023 | 05 September 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0   | 0.0         |
      | 3  | 15   | 01 October 2023   |                   | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 15   | 16 October 2023   |                   | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0        | 0.0  | 0         | 1000.0 | 500.0 | 250.0      | 250.0 | 500.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 September 2023 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |


  Scenario: Verify that with multiple disbursement when last disbursement is reverted, downpayment will be reverted too - autopayment enabled
    When Admin sets the business date to "01 September 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 September 2023 | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 September 2023" with "1000" amount and expected disbursement date on "01 September 2023"
    When Admin successfully disburse the loan on "01 September 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "05 September 2023"
    And Customer makes "AUTOPAY" repayment on "05 September 2023" with 500 EUR transaction amount
    When Admin sets the business date to "11 September 2023"
    When Admin successfully disburse the loan on "11 September 2023" with "500" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 11 September 2023 |                   | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 11 September 2023 | 05 September 2023 | 1125.0          | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 125.0      | 0.0  | 0.0         |
      | 3  | 15   | 16 September 2023 | 05 September 2023 | 750.0           | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 375.0 | 375.0      | 0.0  | 0.0         |
      | 4  | 15   | 01 October 2023   |                   | 375.0           | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 125.0 | 125.0      | 0.0  | 250.0       |
      | 5  | 15   | 16 October 2023   |                   | 0.0             | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0  | 375.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1500.0        | 0        | 0.0  | 0         | 1500.0 | 875.0 | 625.0      | 0.0  | 625.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 September 2023 | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 05 September 2023 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        |
      | 11 September 2023 | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 750.0        |
      | 11 September 2023 | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 625.0        |
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |                   | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 September 2023 | 01 September 2023 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 September 2023 | 05 September 2023 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 3  | 15   | 01 October 2023   | 05 September 2023 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 250.0      | 0.0  | 0.0         |
      | 4  | 15   | 16 October 2023   |                   | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0.0  | 0         | 1000.0 | 750.0 | 500.0      | 0.0  | 250.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 September 2023 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 September 2023 | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 05 September 2023 | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 250.0        |


  @AdvancedPaymentAllocation
  Scenario: Verify that down-payment Installments are available in the repayment schedule for CREATED, APPROVED and DISBURSED states of the loan account
    When Admin sets the business date to "01 September 2023"
    And Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 September 2023 | 400            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 September 2023 |           | 400.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 01 September 2023 |           | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 2  | 15   | 16 September 2023 |           | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 3  | 15   | 01 October 2023   |           | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 4  | 15   | 16 October 2023   |           | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 400.0         | 0        | 0.0  | 0         | 400.0 | 0.0  | 0.0        | 0.0  | 400.0       |
    When Admin sets the business date to "02 September 2023"
    And Admin successfully approves the loan on "02 September 2023" with "400" amount and expected disbursement date on "02 September 2023"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 02 September 2023 |           | 400.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 02 September 2023 |           | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 2  | 15   | 17 September 2023 |           | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 3  | 15   | 02 October 2023   |           | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
      | 4  | 15   | 17 October 2023   |           | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 400.0         | 0        | 0.0  | 0         | 400.0 | 0.0  | 0.0        | 0.0  | 400.0       |
    When Admin sets the business date to "03 September 2023"
    And Admin successfully disburse the loan on "03 September 2023" with "400" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 September 2023 |                   | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 03 September 2023 | 03 September 2023 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 18 September 2023 |                   | 200.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0   | 0.0        | 0.0  | 100.0       |
      | 3  | 15   | 03 October 2023   |                   | 100.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0   | 0.0        | 0.0  | 100.0       |
      | 4  | 15   | 18 October 2023   |                   | 0.0             | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0   | 0.0        | 0.0  | 100.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 400.0         | 0        | 0.0  | 0         | 400.0 | 100.0 | 0.0        | 0.0  | 300.0       |
    When Admin sets the business date to "04 September 2023"
    And Admin successfully disburse the loan on "04 September 2023" with "200" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 03 September 2023 |                   | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 03 September 2023 | 03 September 2023 | 300.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 04 September 2023 |                   | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 04 September 2023 | 04 September 2023 | 450.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 50.0  | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 18 September 2023 |                   | 300.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0   | 0.0        | 0.0  | 150.0       |
      | 4  | 15   | 03 October 2023   |                   | 150.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0   | 0.0        | 0.0  | 150.0       |
      | 5  | 15   | 18 October 2023   |                   | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0   | 0.0        | 0.0  | 150.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 600.0         | 0        | 0.0  | 0         | 600.0 | 150.0 | 0.0        | 0.0  | 450.0       |


  Scenario: Multiple disbursements on same day - auto enabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2022" due date and 50 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 250.0 | 0          | 0    | 800         |
    When Admin sets the business date to "01 February 2022"
    When Admin successfully disburse the loan on "01 February 2022" with "200" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2022 |                  | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 01 February 2022 | 01 February 2022 | 900.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 50.0  | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2022 |                  | 600.0           | 300.0         | 0.0      | 0.0  | 50.0      | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 4  | 28   | 01 March 2022    |                  | 300.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 5  | 31   | 01 April 2022    |                  | 0.0             | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1200.0        | 0        | 0    | 50        | 1250.0 | 300.0 | 0          | 0    | 950         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 950.0        |
      | 01 February 2022 | Down Payment     | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 900.0        |
    When Admin successfully disburse the loan on "01 February 2022" with "200" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2022 |                  | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 01 February 2022 |                  | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 01 February 2022 | 01 February 2022 | 1100.0          | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 50.0  | 0.0        | 0.0  | 0.0         |
      | 3  | 0    | 01 February 2022 | 01 February 2022 | 1050.0          | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 50.0  | 0.0        | 0.0  | 0.0         |
      | 4  | 31   | 01 February 2022 |                  | 700.0           | 350.0         | 0.0      | 0.0  | 50.0      | 400.0 | 0.0   | 0.0        | 0.0  | 400.0       |
      | 5  | 28   | 01 March 2022    |                  | 350.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 6  | 31   | 01 April 2022    |                  | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1400.0        | 0        | 0    | 50        | 1450.0 | 350.0 | 0          | 0    | 1100        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 950.0        |
      | 01 February 2022 | Down Payment     | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 900.0        |
      | 01 February 2022 | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1100.0       |
      | 01 February 2022 | Down Payment     | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 1050.0       |



  Scenario: Multiple disbursements on same day - auto disabled
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT | 01 January 2022   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 January 2022" due date and 50 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2022  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2022 |           | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 01 March 2022    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 0.0  | 0          | 0    | 1050        |
    And Customer makes "DOWN_PAYMENT" repayment on "01 January 2022" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2022 |                 | 500.0           | 250.0         | 0.0      | 0.0  | 50.0      | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 3  | 28   | 01 March 2022    |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2022    |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0        | 0    | 50        | 1050.0 | 250.0 | 0          | 0    | 800         |
    When Admin sets the business date to "01 February 2022"
    When Admin successfully disburse the loan on "01 February 2022" with "200" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2022 |                 | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 01 February 2022 |                 | 900.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 0.0   | 0.0        | 0.0  | 50.0        |
      | 3  | 31   | 01 February 2022 |                 | 600.0           | 300.0         | 0.0      | 0.0  | 50.0      | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 4  | 28   | 01 March 2022    |                 | 300.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
      | 5  | 31   | 01 April 2022    |                 | 0.0             | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0   | 0.0        | 0.0  | 300.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1200.0        | 0        | 0    | 50        | 1250.0 | 250.0 | 0          | 0    | 1000        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 950.0        |
    When Admin successfully disburse the loan on "01 February 2022" with "200" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2022  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2022  | 01 January 2022 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2022 |                 | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 01 February 2022 |                 | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 0    | 01 February 2022 |                 | 1100.0          | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 0.0   | 0.0        | 0.0  | 50.0        |
      | 3  | 0    | 01 February 2022 |                 | 1050.0          | 50.0          | 0.0      | 0.0  | 0.0       | 50.0  | 0.0   | 0.0        | 0.0  | 50.0        |
      | 4  | 31   | 01 February 2022 |                 | 700.0           | 350.0         | 0.0      | 0.0  | 50.0      | 400.0 | 0.0   | 0.0        | 0.0  | 400.0       |
      | 5  | 28   | 01 March 2022    |                 | 350.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 6  | 31   | 01 April 2022    |                 | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1400.0        | 0        | 0    | 50        | 1450.0 | 250.0 | 0          | 0    | 1200        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2022  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 January 2022  | Repayment        | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 February 2022 | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 950.0        |
      | 01 February 2022 | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1150.0       |


  Scenario: Verify that repayment schedule is managed properly in case of LP2 product with two disbursement on the same day
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "100" EUR transaction amount
    When Admin successfully disburse the loan on "01 October 2023" with "900" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 October 2023  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 01 October 2023  |                 | 900.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 October 2023  | 01 October 2023 | 975.0           | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 0    | 01 October 2023  | 01 October 2023 | 750.0           | 225.0         | 0.0      | 0.0  | 0.0       | 225.0 | 225.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 16 October 2023  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 31 October 2023  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 5  | 15   | 15 November 2023 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 October 2023  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 October 2023  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         |
      | 01 October 2023  | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 975.0        |
      | 01 October 2023  | Down Payment     | 225.0  | 225.0     | 0.0      | 0.0  | 0.0       | 750.0        |


  Scenario: Verify that in case of auto downpayment Loan details / summary.totalRepaymentTransaction has the downpayment amount
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct           | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                                                             |
      | LP2_DOWNPAYMENT_AUTO | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    Then Loan details has the downpayment amount "250" in summary.totalRepaymentTransaction

   @AdvancedPaymentAllocation
  Scenario: Verify enhanced auto downpayment for overpaid loans - UC1: overpaid amount > second disbursement amount AND overpaid amount > related downpayment amount
    When Admin sets the business date to "01 February 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 February 2024  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 February 2024" with "1000" amount and expected disbursement date on "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "500" EUR transaction amount
    When Admin sets the business date to "16 February 2024"
    And Customer makes "AUTOPAY" repayment on "16 February 2024" with 1000 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 625 overpaid amount
    When Admin sets the business date to "20 February 2024"
    When Admin successfully disburse the loan on "20 February 2024" with "400" EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 225 overpaid amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 February 2024 |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 February 2024 | 01 February 2024 | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 February 2024 | 16 February 2024 | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 20 February 2024 |                  | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 0    | 20 February 2024 | 20 February 2024 | 550.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 02 March 2024    | 20 February 2024 | 275.0           | 275.0         | 0.0      | 0.0  | 0.0       | 275.0 | 275.0 | 275.0      | 0.0  | 0.0         |
      | 5  | 15   | 17 March 2024    | 20 February 2024 | 0.0             | 275.0         | 0.0      | 0.0  | 0.0       | 275.0 | 275.0 | 275.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 900.0         | 0.0      | 0.0  | 0.0       | 900.0 | 900.0 | 550.0      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2024 | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 February 2024 | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 February 2024 | Repayment        | 1000.0 | 375.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 20 February 2024 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |

   @AdvancedPaymentAllocation
  Scenario: Verify enhanced auto downpayment for overpaid loans - UC2: overpaid amount = second disbursement amount AND overpaid amount > related downpayment amount
    When Admin sets the business date to "01 February 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 February 2024  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 February 2024" with "1000" amount and expected disbursement date on "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "500" EUR transaction amount
    When Admin sets the business date to "16 February 2024"
    And Customer makes "AUTOPAY" repayment on "16 February 2024" with 1000 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 625 overpaid amount
    When Admin sets the business date to "20 February 2024"
    When Admin successfully disburse the loan on "20 February 2024" with "625" EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 February 2024 |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 February 2024 | 01 February 2024 | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 February 2024 | 16 February 2024 | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 20 February 2024 |                  | 625.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 0    | 20 February 2024 | 20 February 2024 | 719.0           | 156.0         | 0.0      | 0.0  | 0.0       | 156.0 | 156.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 02 March 2024    | 20 February 2024 | 359.0           | 360.0         | 0.0      | 0.0  | 0.0       | 360.0 | 360.0 | 360.0      | 0.0  | 0.0         |
      | 5  | 15   | 17 March 2024    | 20 February 2024 | 0.0             | 359.0         | 0.0      | 0.0  | 0.0       | 359.0 | 359.0 | 359.0      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 1125.0         | 0.0      | 0.0  | 0.0       | 1125.0 | 1125.0 | 719.0      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2024 | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 February 2024 | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 February 2024 | Repayment        | 1000.0 | 375.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 20 February 2024 | Disbursement     | 625.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          |

   @AdvancedPaymentAllocation
  Scenario: Verify enhanced auto downpayment for overpaid loans - UC3: overpaid amount < second disbursement amount BUT overpaid amount > related downpayment amount
    When Admin sets the business date to "01 February 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 February 2024  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 February 2024" with "1000" amount and expected disbursement date on "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "500" EUR transaction amount
    When Admin sets the business date to "16 February 2024"
    And Customer makes "AUTOPAY" repayment on "16 February 2024" with 575 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 200 overpaid amount
    When Admin sets the business date to "20 February 2024"
    When Admin successfully disburse the loan on "20 February 2024" with "400" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 200 outstanding amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 February 2024 |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 February 2024 | 01 February 2024 | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 February 2024 | 16 February 2024 | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 20 February 2024 |                  | 400.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 0    | 20 February 2024 | 20 February 2024 | 550.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 02 March 2024    |                  | 275.0           | 275.0         | 0.0      | 0.0  | 0.0       | 275.0 | 225.0 | 225.0      | 0.0  | 50.0        |
      | 5  | 15   | 17 March 2024    |                  | 0.0             | 275.0         | 0.0      | 0.0  | 0.0       | 275.0 | 125.0 | 125.0      | 0.0  | 150.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 900.0         | 0.0      | 0.0  | 0.0       | 900.0 | 700.0 | 350.0      | 0.0  | 200.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2024 | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 February 2024 | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 February 2024 | Repayment        | 575.0  | 375.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 20 February 2024 | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        |

   @AdvancedPaymentAllocation
  Scenario: Verify enhanced auto downpayment for overpaid loans - UC4: overpaid amount < second disbursement amount AND overpaid amount < related downpayment amount
    When Admin sets the business date to "01 February 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 February 2024  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 February 2024" with "1000" amount and expected disbursement date on "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "500" EUR transaction amount
    When Admin sets the business date to "16 February 2024"
    And Customer makes "AUTOPAY" repayment on "16 February 2024" with 475 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    When Admin sets the business date to "20 February 2024"
    When Admin successfully disburse the loan on "20 February 2024" with "600" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 450 outstanding amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 February 2024 |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 February 2024 | 01 February 2024 | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 February 2024 | 16 February 2024 | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 20 February 2024 |                  | 600.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 0    | 20 February 2024 | 20 February 2024 | 700.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 150.0 | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 02 March 2024    |                  | 350.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 125.0 | 125.0      | 0.0  | 225.0       |
      | 5  | 15   | 17 March 2024    |                  | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 125.0 | 125.0      | 0.0  | 225.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1100.0        | 0.0      | 0.0  | 0.0       | 1100.0 | 650.0 | 250.0      | 0.0  | 450.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 February 2024 | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 01 February 2024 | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        |
      | 16 February 2024 | Repayment        | 475.0  | 375.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 20 February 2024 | Disbursement     | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        |
      | 20 February 2024 | Down Payment     | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 450.0        |

   @AdvancedPaymentAllocation
  Scenario: Verify enhanced auto downpayment for overpaid loans - UC5: repayment reverted
    When Admin sets the business date to "01 February 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 February 2024  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 February 2024" with "1000" amount and expected disbursement date on "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "500" EUR transaction amount
    When Admin sets the business date to "10 February 2024"
    And Customer makes "AUTOPAY" repayment on "10 February 2024" with 100 EUR transaction amount
    Then Loan has 275 outstanding amount
    When Admin sets the business date to "16 February 2024"
    And Customer makes "AUTOPAY" repayment on "16 February 2024" with 375 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    When Admin sets the business date to "20 February 2024"
    When Admin successfully disburse the loan on "20 February 2024" with "600" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 450 outstanding amount
    When Customer undo "1"th "Repayment" transaction made on "16 February 2024"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 February 2024 |                  | 500.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 February 2024 | 01 February 2024 | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 February 2024 | 20 February 2024 | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 125.0 | 100.0      | 25.0 | 0.0         |
      |    |      | 20 February 2024 |                  | 600.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 3  | 0    | 20 February 2024 |                  | 700.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 25.0  | 0.0        | 0.0  | 125.0       |
      | 4  | 15   | 02 March 2024    |                  | 350.0           | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
      | 5  | 15   | 17 March 2024    |                  | 0.0             | 350.0         | 0.0      | 0.0  | 0.0       | 350.0 | 0.0   | 0.0        | 0.0  | 350.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1100.0        | 0.0      | 0.0  | 0.0       | 1100.0 | 275.0 | 100.0      | 25.0 | 825.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 February 2024 | Disbursement     | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    |
      | 01 February 2024 | Down Payment     | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 375.0        | false    |
      | 10 February 2024 | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 275.0        | false    |
      | 16 February 2024 | Repayment        | 375.0  | 275.0     | 0.0      | 0.0  | 0.0       | 0.0          | true    |
      | 20 February 2024 | Disbursement     | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 875.0        | false    |
      | 20 February 2024 | Down Payment     | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 825.0        | false    |
