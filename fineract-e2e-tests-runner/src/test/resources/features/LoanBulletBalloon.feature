@Emi
Feature: Progressive bullet / balloon loan schedule (grace on principal = N-1)

  Scenario: BL01 - Progressive bullet schedule when grace on principal equals number of repayments minus 1 - N=8
    When Admin sets the business date to "1 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 1 January 2024    | 100            | 30                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 8                 | MONTHS                | 1              | MONTHS                 | 8                  | 7                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    When Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date               | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024    |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024   |           | 100.0           | 0.0           | 2.5      | 0.0  | 0.0       | 2.5   | 0.0  | 0.0        | 0.0  | 2.5         |
      | 2  | 29   | 01 March 2024      |           | 100.0           | 0.0           | 2.5      | 0.0  | 0.0       | 2.5   | 0.0  | 0.0        | 0.0  | 2.5         |
      | 3  | 31   | 01 April 2024      |           | 100.0           | 0.0           | 2.5      | 0.0  | 0.0       | 2.5   | 0.0  | 0.0        | 0.0  | 2.5         |
      | 4  | 30   | 01 May 2024        |           | 100.0           | 0.0           | 2.5      | 0.0  | 0.0       | 2.5   | 0.0  | 0.0        | 0.0  | 2.5         |
      | 5  | 31   | 01 June 2024       |           | 100.0           | 0.0           | 2.5      | 0.0  | 0.0       | 2.5   | 0.0  | 0.0        | 0.0  | 2.5         |
      | 6  | 30   | 01 July 2024       |           | 100.0           | 0.0           | 2.5      | 0.0  | 0.0       | 2.5   | 0.0  | 0.0        | 0.0  | 2.5         |
      | 7  | 31   | 01 August 2024     |           | 100.0           | 0.0           | 2.5      | 0.0  | 0.0       | 2.5   | 0.0  | 0.0        | 0.0  | 2.5         |
      | 8  | 31   | 01 September 2024  |           | 0.0             | 100.0         | 2.5      | 0.0  | 0.0       | 102.5 | 0.0  | 0.0        | 0.0  | 102.5       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 100.0         | 20.0     | 0.0  | 0.0       | 120.0 | 0.0  | 0.0        | 0.0  | 120.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
