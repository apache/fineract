@CapitalizedIncomeFeature
Feature: Capitalized Income

  @TestRailId:C3635
  Scenario: As a user I want to add capitalized income to a progressive loan after disbursement
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | 1 January 2024    | 1000.0         | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 01 January 2024  | Down Payment       | 225.0  | 225.0     | 0.0      | 0.0  | 0.0       | 675.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 775.0        | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |
