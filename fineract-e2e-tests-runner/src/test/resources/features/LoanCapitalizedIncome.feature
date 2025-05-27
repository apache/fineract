@CapitalizedIncomeFeature
Feature: Capitalized Income

  @TestRailId:C3635
  Scenario: As a user I want to add capitalized income to a progressive loan after disbursement for multidisbursal loan with downpayment - UC1
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | 1 January 2024    | 1000.0         | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                 | 900.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024 | 01 January 2024 | 675.0           | 225.0         | 0.0      | 0.0  | 0.0       | 225.0 | 225.0 | 0.0        | 0.0  | 0.0         |
      |    |      | 02 January 2024 |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 30   | 31 January 2024 |                 | 0.0             | 775.0         | 0.0      | 0.0  | 0.0       | 775.0 | 0.0   | 0.0        | 0.0  | 775.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 225.0 | 0.0        | 0.0  | 775.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 01 January 2024  | Down Payment       | 225.0  | 225.0     | 0.0      | 0.0  | 0.0       | 675.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 775.0        | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |

  @TestRailId:C3637
  Scenario: As a user I want to add capitalized income to a progressive loan after disbursement - UC2
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 900.0           |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      |    |      | 02 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.81     | 0.0  | 0.0       | 1005.81 | 0.0  | 0.0        | 0.0  | 1005.81     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.81     | 0.0  | 0.0       | 1005.81 | 0.0  | 0.0        | 0.0  | 1005.81     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |

  @TestRailId:C3638
  Scenario: Verify capitalized income amount with disbursement amount calculation within approved amount amount for multidisbursal progressive loan - UC3
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSAL_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "600" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin successfully disburse the loan on "2 January 2024" with "200" EUR transaction amount
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "3 January 2024" with "200" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 600.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 02 January 2024  |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 03 January 2024  |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 835.65          | 164.35        | 5.72     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
      | 2  | 29   | 01 March 2024    |           | 670.45          | 165.2         | 4.87     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
      | 3  | 31   | 01 April 2024    |           | 504.29          | 166.16        | 3.91     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
      | 4  | 30   | 01 May 2024      |           | 337.16          | 167.13        | 2.94     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
      | 5  | 31   | 01 June 2024     |           | 169.06          | 168.1         | 1.97     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 169.06        | 0.99     | 0.0  | 0.0       | 170.05 | 0.0  | 0.0        | 0.0  | 170.05      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.4     | 0.0  | 0.0       | 1020.4 | 0.0  | 0.0        | 0.0  | 1020.4      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        | false    |
      | 02 January 2024  | Disbursement       | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0         | false    |
      | 03 January 2024  | Capitalized Income | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 1000.0        | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "03 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 200.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 200.0  |

  @TestRailId:C3639
  Scenario: Verify validation of capitalized income amount with disbursement amount within approved amount for progressive loan - UC4
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    Then Capitalized income with payment type "AUTOPAY" on "02 January 2024" is forbidden with amount "200" while exceed approved amount

  @TestRailId:C3640
  Scenario: Verify validation of capitalized income amount with disbursement amount within approved amount for multidisbursal progressive loan - UC5
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME | 1 January 2024    | 1000.0         | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 30                | DAYS                  | 30             | DAYS                   | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "500" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin successfully disburse the loan on "2 January 2024" with "300" EUR transaction amount
    Then Capitalized income with payment type "AUTOPAY" on "2 January 2024" is forbidden with amount "300" while exceed approved amount

  @TestRailId:C3641
  Scenario: Verify validation of capitalized income amount with disbursement amount within approved amount after undo disbursement - UC6
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 900.0           |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      |    |      | 02 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.81     | 0.0  | 0.0       | 1005.81 | 0.0  | 0.0        | 0.0  | 1005.81     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.81     | 0.0  | 0.0       | 1005.81 | 0.0  | 0.0        | 0.0  | 1005.81     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |
    When Admin successfully undo disbursal
    Then Loan status will be "APPROVED"
    When Admin sets the business date to "3 January 2024"
    And Admin successfully disburse the loan on "3 January 2024" with "800" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "3 January 2024" with "200" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 03 January 2024  |           | 800.0           |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      |    |      | 03 January 2024  |           | 200.0           |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 03 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 03 January 2024  | Disbursement       | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    |
      | 03 January 2024  | Capitalized Income | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "03 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 200.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 200.0  |

  @TestRailId:C3642
  Scenario: Verify capitalized income amount with disbursement amount calculation within approved amount amount for multidisbursal loan after undo disbursement - UC7
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSAL_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "600" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin successfully disburse the loan on "2 January 2024" with "200" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "200" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 600.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 02 January 2024  |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 02 January 2024  |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 835.69          | 164.31        | 5.76     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
      | 2  | 29   | 01 March 2024    |           | 670.49          | 165.2         | 4.87     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
      | 3  | 31   | 01 April 2024    |           | 504.33          | 166.16        | 3.91     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
      | 4  | 30   | 01 May 2024      |           | 337.2           | 167.13        | 2.94     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
      | 5  | 31   | 01 June 2024     |           | 169.1           | 168.1         | 1.97     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 169.1         | 0.99     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.44    | 0.0  | 0.0       | 1020.44 | 0.0  | 0.0        | 0.0  | 1020.44     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        | false    |
      | 02 January 2024  | Disbursement       | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    |
      | 02 January 2024  | Capitalized Income | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 200.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 200.0  |
    When Admin successfully undo disbursal
    Then Loan status will be "APPROVED"
    When Admin sets the business date to "3 January 2024"
    And Admin successfully disburse the loan on "3 January 2024" with "500" EUR transaction amount
    And Admin sets the business date to "4 January 2024"
    And Admin successfully disburse the loan on "4 January 2024" with "200" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "4 January 2024" with "300" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 03 January 2024  |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 04 January 2024  |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 04 January 2024  |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 03 February 2024 |           | 835.67          | 164.33        | 5.74     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
      | 2  | 29   | 03 March 2024    |           | 670.47          | 165.2         | 4.87     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
      | 3  | 31   | 03 April 2024    |           | 504.31          | 166.16        | 3.91     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
      | 4  | 30   | 03 May 2024      |           | 337.18          | 167.13        | 2.94     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
      | 5  | 31   | 03 June 2024     |           | 169.08          | 168.1         | 1.97     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
      | 6  | 30   | 03 July 2024     |           | 0.0             | 169.08        | 0.99     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.42    | 0.0  | 0.0       | 1020.42 | 0.0  | 0.0        | 0.0  | 1020.42     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 03 January 2024  | Disbursement       | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    |
      | 04 January 2024  | Disbursement       | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    |
      | 04 January 2024  | Capitalized Income | 300.0  | 300.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "04 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 300.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 300.0  |

  @TestRailId:C3646
  Scenario: As a user I want to add capitalized income to a progressive loan after disbursement and then make a full repayment - amortization in case of loan close event
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 900.0           |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      |    |      | 02 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.81     | 0.0  | 0.0       | 1005.81 | 0.0  | 0.0        | 0.0  | 1005.81     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.81     | 0.0  | 0.0       | 1005.81 | 0.0  | 0.0        | 0.0  | 1005.81     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "2 January 2024" with 1000.17 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 900.0           |               |          | 0.0  |           | 0.0     | 0.0     |            |      |             |
      |    |      | 02 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0     | 0.0     |            |      |             |
      | 1  | 31   | 01 February 2024 | 02 January 2024 | 0.0             | 1000.0        | 0.17     | 0.0  | 0.0       | 1000.17 | 1000.17 | 1000.17    | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 0.17     | 0.0  | 0.0       | 1000.17 | 1000.17 | 1000.17    | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement                    | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 January 2024  | Repayment                       | 1000.17| 1000.0    | 0.17     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2024  | Accrual                         | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2024  | Capitalized Income Amortization | 100.0  | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              |        | 100.0  |
      | LIABILITY | 145024       | Deferred Capitalized Income  | 100.0  |        |
    And LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent is raised on "02 January 2024"

  @TestRailId:C3647
  Scenario: As a user I want to add capitalized income to a progressive loan after disbursement and then overpay loan - amortization in case of loan close event
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 900.0           |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      |    |      | 02 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.81     | 0.0  | 0.0       | 1005.81 | 0.0  | 0.0        | 0.0  | 1005.81     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.81     | 0.0  | 0.0       | 1005.81 | 0.0  | 0.0        | 0.0  | 1005.81     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "2 January 2024" with 1100 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 900.0           |               |          | 0.0  |           | 0.0     | 0.0     |            |      |             |
      |    |      | 02 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0     | 0.0     |            |      |             |
      | 1  | 31   | 01 February 2024 | 02 January 2024 | 0.0             | 1000.0        | 0.17     | 0.0  | 0.0       | 1000.17 | 1000.17 | 1000.17    | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 0.17     | 0.0  | 0.0       | 1000.17 | 1000.17 | 1000.17    | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement                    | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 January 2024  | Repayment                       | 1100.0 | 1000.0    | 0.17     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2024  | Accrual                         | 0.17   | 0.0       | 0.17     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2024  | Capitalized Income Amortization | 100.0  | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              |        | 100.0  |
      | LIABILITY | 145024       | Deferred Capitalized Income  | 100.0  |        |
    And LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent is raised on "02 January 2024"

  @TestRailId:C3651
  Scenario: As a user I want to add capitalized income to a progressive loan after disbursement and then write off loan - amortization in case of loan close event
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 900.0           |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      |    |      | 02 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.81     | 0.0  | 0.0       | 1005.81 | 0.0  | 0.0        | 0.0  | 1005.81     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.81     | 0.0  | 0.0       | 1005.81 | 0.0  | 0.0        | 0.0  | 1005.81     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |
    And Admin does write-off the loan on "02 January 2024"
    Then Loan status will be "CLOSED_WRITTEN_OFF"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 900.0           |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      |    |      | 02 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 02 January 2024 | 0.0             | 1000.0        | 5.81     | 0.0  | 0.0       | 1005.81 | 0.0  | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.81     | 0.0  | 0.0       | 1005.81 | 0.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement                    | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 02 January 2024  | Close (as written-off)          | 1005.81| 1000.0    | 5.81     | 0.0  | 0.0       | 0.0          | false    |
      | 02 January 2024  | Capitalized Income Amortization | 100.0  | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | EXPENSE   | e4           | Written off                  |        | 100.0  |
      | LIABILITY | 145024       | Deferred Capitalized Income  | 100.0  |        |
    Then LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent is raised on "02 January 2024"

  @TestRailId:C3648
  Scenario: Verify capitalized income: daily amortization - Capitalized Income type: interest
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 200            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 January 2024" with "50" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      |    |      | 01 January 2024  |           | 50.0            |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 100.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 2  | 29   | 01 March 2024    |           | 50.0            | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   |
      | ASSET     | 112601       | Loans Receivable            | 50.0  |        |
    When Admin sets the business date to "02 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      |    |      | 01 January 2024  |           | 50.0            |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 100.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 2  | 29   | 01 March 2024    |           | 50.0            | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      |    |      | 01 January 2024  |           | 50.0            |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 100.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 2  | 29   | 01 March 2024    |           | 50.0            | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    When Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      |    |      | 01 January 2024  |           | 50.0            |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 100.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 2  | 29   | 01 March 2024    |           | 50.0            | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Admin sets the business date to "01 April 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      |    |      | 01 January 2024  |           | 50.0            |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 100.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 2  | 29   | 01 March 2024    |           | 50.0            | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 404000       | Interest Income             |       | 0.55   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.55  |        |
    And Loan Capitalized Income Amortization Transaction Created Business Event is created on "31 March 2024"

  @TestRailId:C3649
  Scenario: Verify capitalized income: daily amortization - Capitalized Income type: fee
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_FEE | 01 January 2024   | 200            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 January 2024" with "50" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      |    |      | 01 January 2024  |           | 50.0            |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 100.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 2  | 29   | 01 March 2024    |           | 50.0            | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   |
      | ASSET     | 112601       | Loans Receivable            | 50.0  |        |
    When Admin sets the business date to "02 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      |    |      | 01 January 2024  |           | 50.0            |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 100.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 2  | 29   | 01 March 2024    |           | 50.0            | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      |    |      | 01 January 2024  |           | 50.0            |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 100.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 2  | 29   | 01 March 2024    |           | 50.0            | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    When Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      |    |      | 01 January 2024  |           | 50.0            |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 100.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 2  | 29   | 01 March 2024    |           | 50.0            | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    And Admin sets the business date to "01 April 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due  | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      |    |      | 01 January 2024  |           | 50.0            |               |          | 0.0  |           | 0.0  | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 100.0           | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 2  | 29   | 01 March 2024    |           | 50.0            | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 50.0          | 0.0      | 0.0  | 0.0       | 50.0 | 0.0  | 0.0        | 0.0  | 50.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |

      | 01 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |

      | 01 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 04 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 05 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 06 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 07 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 08 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 09 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 10 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 11 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 12 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 13 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 14 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 16 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 17 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 18 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 19 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 20 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 21 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 22 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.0     | 0.54  | 0.0       | 0.0          | false    | false    |
      | 23 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.0     | 0.54  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 26 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.0     | 0.54  | 0.0       | 0.0          | false    | false    |
      | 27 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 28 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.0     | 0.54  | 0.0       | 0.0          | false    | false    |
      | 29 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
      | 30 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.0     | 0.54  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.0     | 0.55  | 0.0       | 0.0          | false    | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | INCOME    | 404000       | Interest Income             |       | 0.55   |
      | LIABILITY | 145024       | Deferred Capitalized Income | 0.55  |        |
    And Loan Capitalized Income Amortization Transaction Created Business Event is created on "31 March 2024"

  @TestRailId:C3661
  Scenario: As a user I want to add capitalized income to a progressive loan after disbursement and then charge-off the loan with "delinquent" reason - amortization in case of loan charge-off event
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON_INTEREST_RECALC_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |
    Then Admin can successfully set Fraud flag to the loan
    When Admin sets the business date to "03 February 2024"
    And Admin does charge-off the loan with reason "DELINQUENT" on "03 February 2024"
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 4.58   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 1000.0|        |
      | INCOME  | 404001       | Interest Income Charge Off | 4.58  |        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement                    | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 03 February 2024 | Accrual                         | 4.58   | 0.0       | 4.58     | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2024 | Charge-off                      | 1004.58| 1000.0    | 4.58     | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2024 | Capitalized Income Amortization | 100.0  | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "03 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt         |        | 100.0  |
      | LIABILITY | 145024       | Deferred Capitalized Income  | 100.0  |        |
    Then LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent is raised on "03 February 2024"

  @TestRailId:C3662
  Scenario: As a user I want to add capitalized income to a progressive loan after disbursement and then charge-off a fraud loan - amortization in case of loan charge-off event
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON_INTEREST_RECALC_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |
    Then Admin can successfully set Fraud flag to the loan
    When Admin sets the business date to "03 February 2024"
    And Admin does charge-off the loan on "03 February 2024"
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 4.58   |
      | EXPENSE | 744037       | Credit Loss/Bad Debt-Fraud | 1000.0|        |
      | INCOME  | 404001       | Interest Income Charge Off | 4.58  |        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement                    | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 03 February 2024 | Accrual                         | 4.58   | 0.0       | 4.58     | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2024 | Charge-off                      | 1004.58| 1000.0    | 4.58     | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2024 | Capitalized Income Amortization | 100.0  | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "03 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | EXPENSE   | 744037       | Credit Loss/Bad Debt-Fraud   |        | 100.0  |
      | LIABILITY | 145024       | Deferred Capitalized Income  | 100.0  |        |
    Then LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent is raised on "03 February 2024"

  @TestRailId:C3663
  Scenario: As a user I want to add capitalized income to a progressive loan after disbursement and then charge-off the loan - amortization in case of loan charge-off event
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON_INTEREST_RECALC_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |
    Then Admin can successfully set Fraud flag to the loan
    When Admin sets the business date to "03 February 2024"
    And Admin does charge-off the loan with reason "DELINQUENT" on "03 February 2024"
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 4.58   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 1000.0|        |
      | INCOME  | 404001       | Interest Income Charge Off | 4.58  |        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement                    | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 03 February 2024 | Accrual                         | 4.58   | 0.0       | 4.58     | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2024 | Charge-off                      | 1004.58| 1000.0    | 4.58     | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2024 | Capitalized Income Amortization | 100.0  | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "03 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt         |        | 100.0  |
      | LIABILITY | 145024       | Deferred Capitalized Income  | 100.0  |        |
    Then LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent is raised on "03 February 2024"

  @TestRailId:C3664
  Scenario: As a user I want to add capitalized income to a progressive loan after disbursement and then undo the charge-off transaction with "delinquent" reason - amortization in case of loan charge-off event should also be reversed
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON_INTEREST_RECALC_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "900" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "2 January 2024" with "100" EUR transaction amount
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement       | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "02 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable             | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |
    Then Admin can successfully set Fraud flag to the loan
    When Admin sets the business date to "03 February 2024"
    And Admin does charge-off the loan with reason "DELINQUENT" on "03 February 2024"
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 4.58   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 1000.0|        |
      | INCOME  | 404001       | Interest Income Charge Off | 4.58  |        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement                    | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 03 February 2024 | Accrual                         | 4.58   | 0.0       | 4.58     | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2024 | Charge-off                      | 1004.58| 1000.0    | 4.58     | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2024 | Capitalized Income Amortization | 100.0  | 0.0       | 100.0    | 0.0  | 0.0       | 0.0          | false    |
    Then Loan Transactions tab has a "CAPITALIZED_INCOME_AMORTIZATION" transaction with date "03 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt         |        | 100.0  |
      | LIABILITY | 145024       | Deferred Capitalized Income  | 100.0  |        |
    Then LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent is raised on "03 February 2024"
    Then Admin does a charge-off undo the loan
    Then Loan Transactions tab has a "CHARGE_OFF" transaction with date "03 February 2024" which has the following Journal entries:
      | Type    | Account code | Account name               | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable           |       | 1000.0 |
      | ASSET   | 112603       | Interest/Fee Receivable    |       | 4.58   |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       | 1000.0|        |
      | INCOME  | 404001       | Interest Income Charge Off | 4.58  |        |
      | ASSET   | 112601       | Loans Receivable           | 1000.0|        |
      | ASSET   | 112603       | Interest/Fee Receivable    | 4.58  |        |
      | EXPENSE | 744007       | Credit Loss/Bad Debt       |       | 1000.0 |
      | INCOME  | 404001       | Interest Income Charge Off |       | 4.58   |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement                    | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 02 January 2024  | Capitalized Income              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 03 February 2024 | Accrual                         | 4.58   | 0.0       | 4.58     | 0.0  | 0.0       | 0.0          | false    |
      | 03 February 2024 | Charge-off                      | 1004.58| 1000.0    | 4.58     | 0.0  | 0.0       | 0.0          | true     |
    Then Reversed loan capitalized income amortization transaction has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt         |        | 100.0  |
      | LIABILITY | 145024       | Deferred Capitalized Income  | 100.0  |        |
      | EXPENSE   | 744007       | Credit Loss/Bad Debt         | 100.0  |        |
      | LIABILITY | 145024       | Deferred Capitalized Income  |        | 100.0  |

  @TestRailId:C3673
  Scenario: Verify capitalized income: repayment schedule - UC1: Simple loan - full payment
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 January 2024" with "50" EUR transaction amount
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   |
      | ASSET     | 112601       | Loans Receivable            | 50.0  |        |
    When Admin sets the business date to "02 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 01 January 2024  |           | 50.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 100.29          | 49.71         | 0.87     | 0.0  | 0.0       | 50.58 | 0.0  | 0.0        | 0.0  | 50.58       |
      | 2  | 29   | 01 March 2024    |           | 50.3            | 49.99         | 0.59     | 0.0  | 0.0       | 50.58 | 0.0  | 0.0        | 0.0  | 50.58       |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 50.3          | 0.29     | 0.0  | 0.0       | 50.59 | 0.0  | 0.0        | 0.0  | 50.59       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 150.0         | 1.75     | 0.0  | 0.0       | 151.75 | 0.0  | 0.0        | 0.0  | 151.75      |
#   --- First repayment ---
    When Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 50.58 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 01 January 2024  |                  | 50.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 100.29          | 49.71         | 0.87     | 0.0  | 0.0       | 50.58 | 50.58 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 50.3            | 49.99         | 0.59     | 0.0  | 0.0       | 50.58 | 0.0   | 0.0        | 0.0  | 50.58       |
      | 3  | 31   | 01 April 2024    |                  | 0.0             | 50.3          | 0.29     | 0.0  | 0.0       | 50.59 | 0.0   | 0.0        | 0.0  | 50.59       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 150.0         | 1.75     | 0.0  | 0.0       | 151.75 | 50.58 | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Repayment                       | 50.58  | 49.71     | 0.87     | 0.0  | 0.0       | 100.29       | false    | false    |
#   --- Second repayment ---
    When Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 50.58 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 01 January 2024  |                  | 50.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 100.29          | 49.71         | 0.87     | 0.0  | 0.0       | 50.58 | 50.58 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 50.3            | 49.99         | 0.59     | 0.0  | 0.0       | 50.58 | 50.58 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 0.0             | 50.3          | 0.29     | 0.0  | 0.0       | 50.59 | 0.0   | 0.0        | 0.0  | 50.59       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 150.0         | 1.75     | 0.0  | 0.0       | 151.75 | 101.16 | 0.0        | 0.0  | 50.59       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 February 2024 | Repayment                       | 50.58  | 49.71     | 0.87     | 0.0  | 0.0       | 100.29       | false    | false    |
      | 01 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Repayment                       | 50.58  | 49.99     | 0.59     | 0.0  | 0.0       | 50.3         | false    | false    |
#   --- Third repayment ---
    And Admin sets the business date to "01 April 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 April 2024" with 50.59 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 01 January 2024  |                  | 50.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 100.29          | 49.71         | 0.87     | 0.0  | 0.0       | 50.58 | 50.58 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 50.3            | 49.99         | 0.59     | 0.0  | 0.0       | 50.58 | 50.58 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 April 2024    | 0.0             | 50.3          | 0.29     | 0.0  | 0.0       | 50.59 | 50.59 | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 150.0         | 1.75     | 0.0  | 0.0       | 151.75 | 151.75 | 0.0        | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 February 2024 | Repayment                       | 50.58  | 49.71     | 0.87     | 0.0  | 0.0       | 100.29       | false    | false    |
      | 01 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 March 2024    | Repayment                       | 50.58  | 49.99     | 0.59     | 0.0  | 0.0       | 50.3         | false    | false    |
      | 01 March 2024    | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 March 2024    | Capitalized Income Amortization | 0.54   | 0.0       | 0.54     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2024    | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Repayment                       | 50.59  | 50.3      | 0.29     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual                         | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3674
  Scenario: Verify capitalized income: repayment schedule - UC2: loan - early payoff
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 January 2024" with "50" EUR transaction amount
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   |
      | ASSET     | 112601       | Loans Receivable            | 50.0  |        |
    When Admin sets the business date to "02 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 01 January 2024  |           | 50.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 100.29          | 49.71         | 0.87     | 0.0  | 0.0       | 50.58 | 0.0  | 0.0        | 0.0  | 50.58       |
      | 2  | 29   | 01 March 2024    |           | 50.3            | 49.99         | 0.59     | 0.0  | 0.0       | 50.58 | 0.0  | 0.0        | 0.0  | 50.58       |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 50.3          | 0.29     | 0.0  | 0.0       | 50.59 | 0.0  | 0.0        | 0.0  | 50.59       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 150.0         | 1.75     | 0.0  | 0.0       | 151.75 | 0.0  | 0.0        | 0.0  | 151.75      |
#   --- First repayment ---
    When Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 50.58 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 01 January 2024  |                  | 50.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 100.29          | 49.71         | 0.87     | 0.0  | 0.0       | 50.58 | 50.58 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 50.3            | 49.99         | 0.59     | 0.0  | 0.0       | 50.58 | 0.0   | 0.0        | 0.0  | 50.58       |
      | 3  | 31   | 01 April 2024    |                  | 0.0             | 50.3          | 0.29     | 0.0  | 0.0       | 50.59 | 0.0   | 0.0        | 0.0  | 50.59       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 150.0         | 1.75     | 0.0  | 0.0       | 151.75 | 50.58 | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Repayment                       | 50.58  | 49.71     | 0.87     | 0.0  | 0.0       | 100.29       | false    | false    |
#   --- Early pay-off ---
    When Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    And Loan Pay-off is made on "01 March 2024"
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 01 January 2024  |                  | 50.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 100.29          | 49.71         | 0.87     | 0.0  | 0.0       | 50.58 | 50.58 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 50.3            | 49.99         | 0.59     | 0.0  | 0.0       | 50.58 | 50.58 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    | 01 March 2024    | 0.0             | 50.3          | 0.0      | 0.0  | 0.0       | 50.3  | 50.3  | 50.3       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 150.0         | 1.46     | 0.0  | 0.0       | 151.46 | 151.46 | 50.3       | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 February 2024 | Repayment                       | 50.58  | 49.71     | 0.87     | 0.0  | 0.0       | 100.29       | false    | false    |
      | 01 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Repayment                       | 100.88 | 100.29    | 0.59     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Capitalized Income Amortization | 17.0   | 0.0       | 17.0     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3675
  Scenario: Verify capitalized income: repayment schedule - UC3: loan - charge-off
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "01 January 2024" with "50" EUR transaction amount
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
    And Loan Transactions tab has a "CAPITALIZED_INCOME" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name                | Debit | Credit |
      | LIABILITY | 145024       | Deferred Capitalized Income |       | 50.0   |
      | ASSET     | 112601       | Loans Receivable            | 50.0  |        |
    When Admin sets the business date to "02 January 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      |    |      | 01 January 2024  |           | 50.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 100.29          | 49.71         | 0.87     | 0.0  | 0.0       | 50.58 | 0.0  | 0.0        | 0.0  | 50.58       |
      | 2  | 29   | 01 March 2024    |           | 50.3            | 49.99         | 0.59     | 0.0  | 0.0       | 50.58 | 0.0  | 0.0        | 0.0  | 50.58       |
      | 3  | 31   | 01 April 2024    |           | 0.0             | 50.3          | 0.29     | 0.0  | 0.0       | 50.59 | 0.0  | 0.0        | 0.0  | 50.59       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 150.0         | 1.75     | 0.0  | 0.0       | 151.75 | 0.0  | 0.0        | 0.0  | 151.75      |
#   --- First repayment ---
    When Admin sets the business date to "01 February 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 50.58 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 01 January 2024  |                  | 50.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 100.29          | 49.71         | 0.87     | 0.0  | 0.0       | 50.58 | 50.58 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 50.3            | 49.99         | 0.59     | 0.0  | 0.0       | 50.58 | 0.0   | 0.0        | 0.0  | 50.58       |
      | 3  | 31   | 01 April 2024    |                  | 0.0             | 50.3          | 0.29     | 0.0  | 0.0       | 50.59 | 0.0   | 0.0        | 0.0  | 50.59       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 150.0         | 1.75     | 0.0  | 0.0       | 151.75 | 50.58 | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Repayment                       | 50.58  | 49.71     | 0.87     | 0.0  | 0.0       | 100.29       | false    | false    |
#   --- Charge off ---
    When Admin sets the business date to "01 March 2024"
    And Admin runs inline COB job for Loan
    And Admin does charge-off the loan on "01 March 2024"
    Then Loan status will be "ACTIVE"
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 01 January 2024  |                  | 50.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 100.29          | 49.71         | 0.87     | 0.0  | 0.0       | 50.58 | 50.58 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 50.3            | 49.99         | 0.59     | 0.0  | 0.0       | 50.58 | 0.0   | 0.0        | 0.0  | 50.58       |
      | 3  | 31   | 01 April 2024    |                  | 0.0             | 50.3          | 0.29     | 0.0  | 0.0       | 50.59 | 0.0   | 0.0        | 0.0  | 50.59       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 150.0         | 1.75     | 0.0  | 0.0       | 151.75 | 50.58 | 0.0        | 0.0  | 101.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                    | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Capitalized Income              | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 150.0        | false    | false    |
      | 01 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2024  | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 February 2024 | Repayment                       | 50.58  | 49.71     | 0.87     | 0.0  | 0.0       | 100.29       | false    | false    |
      | 01 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Accrual                         | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 February 2024 | Capitalized Income Amortization | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Accrual                         | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off                      | 101.17 | 100.29    | 0.88     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Capitalized Income Amortization | 17.0   | 0.0       | 17.0     | 0.0  | 0.0       | 0.0          | false    | false    |
