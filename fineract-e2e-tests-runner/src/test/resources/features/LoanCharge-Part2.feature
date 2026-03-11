@ChargeFeature
Feature: LoanCharge - Part2

  @TestRailId:C3260
  Scenario: Verify that there are no payable interest and fee after charge adjustment made on the same date for progressive loan with custom payment allocation order
    When Admin sets the business date to "27 September 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 27 Sep 2024       | 100            | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "27 September 2024" with "40" amount and expected disbursement date on "27 September 2024"
    When Admin successfully disburse the loan on "27 September 2024" with "40" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 27 September 2024 |           | 40.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 27 October 2024   |           | 0.0             | 40.0          | 0.33     | 0.0  | 0.0       | 40.33 | 0.0  | 0.0        | 0.0  | 40.33       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 40.0          | 0.33     | 0.0  | 0.0       | 40.33 | 0.0  | 0.0        | 0.0  | 40.33       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 27 September 2024 | Disbursement     | 40.0   | 0.0       | 0.0      | 0.0  | 0.0       | 40.0         |
    When Admin adds "LOAN_NSF_FEE" due date charge with "27 September 2024" due date and 1 EUR transaction amount
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "27 September 2024" with 1 EUR transaction amount and externalId ""
    Then Loan has 40.32 outstanding amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 27 September 2024 |           | 40.0            |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 27 October 2024   |           | 0.0             | 40.0          | 0.32     | 0.0  | 1.0       | 41.32 | 1.0  | 1.0        | 0.0  | 40.32       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 40.0          | 0.32     | 0.0  | 1.0       | 41.32 | 1.0  | 1.0        | 0.0  | 40.32       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 27 September 2024 | Disbursement      | 40.0   | 0.0       | 0.0      | 0.0  | 0.0       | 40.0         |
      | 27 September 2024 | Charge Adjustment | 1.0    | 1.0       | 0.0      | 0.0  | 0.0       | 39.0         |

  @TestRailId:C3319
  Scenario: Verify enhance the existing implementation to create accruals as part of Charge Creation post maturity with immediate charge accrual and non-zero interest rate
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is enabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "05 February 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 February 2024" due date and 25 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 05 February 2024 | Flat             | 25.0 | 0.0  | 0.0    | 25.0        |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
      | 2  | 4    | 05 February 2024 |           | 0.0             | 0.0           | 0.0      | 0.0  | 25.0      | 25.0    | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 25.0      | 1030.83 | 0.0  | 0.0        | 0.0  | 1030.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 February 2024 | Accrual          | 25.0   | 0.0       | 0.0      | 0.0  | 25.0      | 0.0          |
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is disabled
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 February 2024"

  @TestRailId:C3320
  Scenario: Verify enhance the existing implementation to create accruals as part of Charge Creation post maturity with immediate charge accrual and zero interest rate
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is enabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "05 February 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 February 2024" due date and 25 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 05 February 2024 | Flat             | 25.0 | 0.0  | 0.0    | 25.0        |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 4    | 05 February 2024 |           | 0.0             | 0.0           | 0.0      | 0.0  | 25.0      | 25.0   | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 25.0      | 1025.0 | 0.0  | 0.0        | 0.0  | 1025.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 February 2024 | Accrual          | 25.0   | 0.0       | 0.0      | 0.0  | 25.0      | 0.0          |
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is disabled
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 February 2024"

  @TestRailId:С3335
  Scenario: Verify enhance the existing implementation to create accruals as part of Charge Creation post maturity with inline COB run and non-zero interest rate
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is disabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "05 February 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 February 2024" due date and 25 EUR transaction amount
    When Admin sets the business date to "06 February 2024"
    And Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 05 February 2024 | Flat             | 25.0 | 0.0  | 0.0    | 25.0        |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
      | 2  | 4    | 05 February 2024 |           | 0.0             | 0.0           | 0.0      | 0.0  | 25.0      | 25.0    | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 25.0      | 1030.83 | 0.0  | 0.0        | 0.0  | 1030.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 February 2024 | Accrual          | 30.83  | 0.0       | 5.83     | 0.0  | 25.0      | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 February 2024"

  @TestRailId:C3321
  Scenario: Verify enhance the existing implementation to create accruals as part of Charge Creation post maturity with inline COB run and zero interest rate
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is disabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "05 February 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 February 2024" due date and 25 EUR transaction amount
    When Admin sets the business date to "06 February 2024"
    And Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 05 February 2024 | Flat             | 25.0 | 0.0  | 0.0    | 25.0        |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 4    | 05 February 2024 |           | 0.0             | 0.0           | 0.0      | 0.0  | 25.0      | 25.0   | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 25.0      | 1025.0 | 0.0  | 0.0        | 0.0  | 1025.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 February 2024 | Accrual          | 25.0   | 0.0       | 0.0      | 0.0  | 25.0      | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 February 2024"

  @TestRailId:C3336
  Scenario: Verify enhance the existing implementation to create accruals as part of Charge Creation post maturity with immediate charge accrual with inline COB run and non-zero interest rate
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is enabled
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "05 February 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 February 2024" due date and 25 EUR transaction amount
    When Admin sets the business date to "06 February 2024"
    And Admin runs inline COB job for Loan
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 05 February 2024 | Flat             | 25.0 | 0.0  | 0.0    | 25.0        |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 0.0             | 1000.0        | 5.83     | 0.0  | 0.0       | 1005.83 | 0.0  | 0.0        | 0.0  | 1005.83     |
      | 2  | 4    | 05 February 2024 |           | 0.0             | 0.0           | 0.0      | 0.0  | 25.0      | 25.0    | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 5.83     | 0.0  | 25.0      | 1030.83 | 0.0  | 0.0        | 0.0  | 1030.83     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 05 February 2024 | Accrual          | 25.0   | 0.0       | 0.0      | 0.0  | 25.0      | 0.0          |
      | 05 February 2024 | Accrual          | 5.83   | 0.0       | 5.83     | 0.0  | 0.0       | 0.0          |
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is disabled
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "05 February 2024"

  @TestRailId:C3425
  Scenario: Verify that charge paid is populated for interest bearing products after charge adjustment is being posted
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7.0                    | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    When Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 75.21           | 24.79         | 0.58     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 2  | 29   | 01 March 2024    |           | 50.28           | 24.93         | 0.44     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 3  | 31   | 01 April 2024    |           | 25.2            | 25.08         | 0.29     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 25.2          | 0.15     | 0.0  | 0.0       | 25.35 | 0.0  | 0.0        | 0.0  | 25.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 1.46     | 0.0  | 0.0       | 101.46 | 0.0  | 0.0        | 0.0  | 101.46      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin sets the business date to "01 February 2024"
    When Admin adds "LOAN_NSF_FEE" due date charge with "01 February 2024" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 75.21           | 24.79         | 0.58     | 0.0  | 20.0      | 45.37 | 0.0  | 0.0        | 0.0  | 45.37       |
      | 2  | 29   | 01 March 2024    |           | 50.28           | 24.93         | 0.44     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 3  | 31   | 01 April 2024    |           | 25.2            | 25.08         | 0.29     | 0.0  | 0.0       | 25.37 | 0.0  | 0.0        | 0.0  | 25.37       |
      | 4  | 30   | 01 May 2024      |           | 0.0             | 25.2          | 0.15     | 0.0  | 0.0       | 25.35 | 0.0  | 0.0        | 0.0  | 25.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 1.46     | 0.0  | 20.0      | 121.46 | 0.0  | 0.0        | 0.0  | 121.46      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 01 February 2024 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
  #   --- Backdated repayment with 60 EUR ---
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 60 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |                 | 74.63           | 25.37         | 0.0      | 0.0  | 20.0      | 45.37 | 25.37 | 25.37      | 0.0  | 20.0        |
      | 2  | 29   | 01 March 2024    | 01 January 2024 | 49.26           | 25.37         | 0.0      | 0.0  | 0.0       | 25.37 | 25.37 | 25.37      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                 | 24.58           | 24.68         | 0.69     | 0.0  | 0.0       | 25.37 | 9.26  | 9.26       | 0.0  | 16.11       |
      | 4  | 30   | 01 May 2024      |                 | 0.0             | 24.58         | 0.14     | 0.0  | 0.0       | 24.72 | 0.0   | 0.0        | 0.0  | 24.72       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 0.83     | 0.0  | 20.0      | 120.83 | 60.0 | 60.0       | 0.0  | 60.83       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 January 2024  | Repayment        | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 40.0         |
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 01 February 2024 | Flat             | 20.0 | 0.0  | 0.0    | 20.0        |
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "01 February 2024" with 20 EUR transaction amount and externalId ""
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 74.63           | 25.37         | 0.0      | 0.0  | 20.0      | 45.37 | 45.37 | 25.37      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 January 2024  | 49.26           | 25.37         | 0.0      | 0.0  | 0.0       | 25.37 | 25.37 | 25.37      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 24.58           | 24.68         | 0.69     | 0.0  | 0.0       | 25.37 | 9.26  | 9.26       | 0.0  | 16.11       |
      | 4  | 30   | 01 May 2024      |                  | 0.0             | 24.58         | 0.14     | 0.0  | 0.0       | 24.72 | 0.0   | 0.0        | 0.0  | 24.72       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 0.83     | 0.0  | 20.0      | 120.83 | 80.0 | 60.0       | 0.0  | 40.83       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement      | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 January 2024  | Repayment         | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 40.0         |
      | 01 February 2024 | Charge Adjustment | 20.0   | 0.0       | 0.0      | 0.0  | 20.0      | 40.0         |
    And LoanChargeAdjustmentPostBusinessEvent is raised on "01 February 2024"

  @TestRailId:C3501
  Scenario: Verify repayment schedule amounts after large charge amount added - UC1
    When Admin sets the business date to "20 February 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 20 December 2024  | 800            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "20 December 2024" with "800" amount and expected disbursement date on "20 December 2024"
    When Admin successfully disburse the loan on "20 December 2024" with "800" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      |    |      | 20 December 2024 |           | 800.0           |               |          | 0.0  |           | 0.0      | 0.0  |            |      |             |
      | 1  | 31   | 20 January 2025  |           | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
      | 2  | 31   | 20 February 2025 |           | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
      | 3  | 28   | 20 March 2025    |           | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
      | 4  | 31   | 20 April 2025    |           | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 0.0       | 800.0 | 0.0  | 0.0        | 0.0  | 800.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 December 2024 | Disbursement     | 800.0   | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    And Admin adds "LOAN_NSF_FEE" due date charge with "25 December 2024" due date and 123456789012.12 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties       | Due             | Paid | In advance | Late | Outstanding     |
      |    |      | 20 December 2024 |           | 800.0           |               |          | 0.0  |                 | 0.0             | 0.0  |            |      |                 |
      | 1  | 31   | 20 January 2025  |           | 600.0           | 200.0         | 0.0      | 0.0  | 123456789012.12 | 123456789212.12 | 0.0  | 0.0        | 0.0  | 123456789212.12 |
      | 2  | 31   | 20 February 2025 |           | 400.0           | 200.0         | 0.0      | 0.0  | 0.0             | 200.0           | 0.0  | 0.0        | 0.0  | 200.0           |
      | 3  | 28   | 20 March 2025    |           | 200.0           | 200.0         | 0.0      | 0.0  | 0.0             | 200.0           | 0.0  | 0.0        | 0.0  | 200.0           |
      | 4  | 31   | 20 April 2025    |           | 0.0             | 200.0         | 0.0      | 0.0  | 0.0             | 200.0           | 0.0  | 0.0        | 0.0  | 200.0           |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties       | Due             | Paid | In advance | Late | Outstanding     |
      | 800.0         | 0.0      | 0.0  | 123456789012.12 | 123456789812.12 | 0.0  | 0.0        | 0.0  | 123456789812.12 |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 December 2024 | Disbursement     | 800.0   | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    Then Loan Charges tab has a given charge with the following data:
      | Name    | isPenalty | Payment due at     | Due as of        | Calculation type | Due             | Paid | Waived | Outstanding     |
      | NSF fee | true      | Specified due date | 25 December 2024 | Flat             | 123456789012.12 | 0.0  | 0.0    | 123456789012.12 |

  @TestRailId:C3502
  Scenario: Verify repayment schedule amounts after a few large charges amount added - UC2
    When Admin sets the business date to "20 February 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 20 December 2024  | 800            | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "20 December 2024" with "800" amount and expected disbursement date on "20 December 2024"
    When Admin successfully disburse the loan on "20 December 2024" with "800" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      |    |      | 20 December 2024 |           | 800.0           |               |          | 0.0  |           | 0.0      | 0.0  |            |      |             |
      | 1  | 31   | 20 January 2025  |           | 600.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
      | 2  | 31   | 20 February 2025 |           | 400.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
      | 3  | 28   | 20 March 2025    |           | 200.0           | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
      | 4  | 31   | 20 April 2025    |           | 0.0             | 200.0         | 0.0      | 0.0  | 0.0       | 200.0    | 0.0  | 0.0        | 0.0  | 200.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 800.0         | 0.0      | 0.0  | 0.0       | 800.0 | 0.0  | 0.0        | 0.0  | 800.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 December 2024 | Disbursement     | 800.0   | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    And Admin adds "LOAN_NSF_FEE" due date charge with "25 December 2024" due date and 123456789012.12 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "28 December 2024" due date and 1003456789012.12 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "31 January 2025" due date and 5503456789012.12 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "23 February 2025" due date and 1003456789012.12 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "03 April 2025" due date and 1503456789012.12 EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "09 April 2025" due date and 103456789037.12 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees             | Penalties        | Due              | Paid | In advance | Late | Outstanding      |
      |    |      | 20 December 2024 |           | 800.0           |               |          | 0.0              |                  | 0.0              | 0.0  |            |      |                  |
      | 1  | 31   | 20 January 2025  |           | 600.0           | 200.0         | 0.0      | 1003456789012.12 | 123456789012.12  | 1126913578224.24 | 0.0  | 0.0        | 0.0  | 1126913578224.24 |
      | 2  | 31   | 20 February 2025 |           | 400.0           | 200.0         | 0.0      | 0.0              | 5503456789012.12 | 5503456789212.12 | 0.0  | 0.0        | 0.0  | 5503456789212.12 |
      | 3  | 28   | 20 March 2025    |           | 200.0           | 200.0         | 0.0      | 0.0              | 1003456789012.12 | 1003456789212.12 | 0.0  | 0.0        | 0.0  | 1003456789212.12 |
      | 4  | 31   | 20 April 2025    |           | 0.0             | 200.0         | 0.0      | 103456789037.12  | 1503456789012.12 | 1606913578249.24 | 0.0  | 0.0        | 0.0  | 1606913578249.24 |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees             | Penalties        | Due              | Paid | In advance | Late | Outstanding      |
      | 800.0         | 0.0      | 1106913578049.24 | 8133827156048.48 | 9240740734897.72 | 0.0  | 0.0        | 0.0  | 9240740734897.72 |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 20 December 2024 | Disbursement     | 800.0   | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    Then Loan Charges tab has a given charge with the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due              | Paid | Waived | Outstanding      |
      | NSF fee    | true      | Specified due date | 25 December 2024 | Flat             | 123456789012.12  | 0.0  | 0.0    | 123456789012.12  |
      | Snooze fee | false     | Specified due date | 28 December 2024 | Flat             | 1003456789012.12 | 0.0  | 0.0    | 1003456789012.12 |
      | NSF fee    | true      | Specified due date | 31 January 2025  | Flat             | 5503456789012.12 | 0.0  | 0.0    | 5503456789012.12 |
      | NSF fee    | true      | Specified due date | 23 February 2025 | Flat             | 1003456789012.12 | 0.0  | 0.0    | 1003456789012.12 |
      | NSF fee    | true      | Specified due date | 03 April 2025    | Flat             | 1503456789012.12 | 0.0  | 0.0    | 1503456789012.12 |
      | Snooze fee | false     | Specified due date | 09 April 2025    | Flat             | 103456789037.12  | 0.0  | 0.0    | 103456789037.12  |

  @TestRailId:C3543
  Scenario: Check that subResourceExternalId present in charge adjustment response after loan is charged off, accounting none
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_ACCOUNTING_RULE_NONE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 March 2024" due date and 5 EUR transaction amount
    And Admin does charge-off the loan on "01 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.14           | 16.43         | 0.58     | 5.0  | 0.0       | 22.01 | 0.0  | 0.0        | 0.0  | 22.01       |
      | 3  | 31   | 01 April 2024    |           | 50.52           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0  | 0.0        | 0.0  | 17.09       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Accrual            | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Accrual Adjustment | 0.89   | 0.0       | 0.89     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off         | 107.14 | 100.0     | 2.14     | 5.0  | 0.0       | 0.0          | false    | false    |
    When Admin makes a charge adjustment for the last "LOAN_SNOOZE_FEE" type charge which is due on "01 March 2024" with 5 EUR transaction amount and externalId ""
    Then Charge adjustment response has the subResourceExternalId
    Then Loan has 1 "CHARGE_ADJUSTMENT" transactions on Transactions tab
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Accrual            | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Accrual Adjustment | 0.89   | 0.0       | 0.89     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off         | 107.14 | 100.0     | 2.14     | 5.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge Adjustment  | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 95.0         | false    | false    |

  @TestRailId:C3544
  Scenario: Check that subResourceExternalId present in charge adjustment response after loan is charged off, accrual activity
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    And Admin successfully approves the loan on "1 January 2024" with "100" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "100" EUR transaction amount
    When Admin sets the business date to "01 March 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "01 March 2024" due date and 5 EUR transaction amount
    And Admin does charge-off the loan on "01 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.14           | 16.43         | 0.58     | 5.0  | 0.0       | 22.01 | 0.0  | 0.0        | 0.0  | 22.01       |
      | 3  | 31   | 01 April 2024    |           | 50.52           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0  | 0.0        | 0.0  | 17.09       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 March 2024    | Accrual          | 1.16   | 0.0       | 1.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off       | 107.14 | 100.0     | 2.14     | 5.0  | 0.0       | 0.0          | false    | false    |
    When Admin makes a charge adjustment for the last "LOAN_SNOOZE_FEE" type charge which is due on "01 March 2024" with 5 EUR transaction amount and externalId ""
    Then Charge adjustment response has the subResourceExternalId
    Then Loan has 1 "CHARGE_ADJUSTMENT" transactions on Transactions tab
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement      | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 March 2024    | Accrual           | 1.16   | 0.0       | 1.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge-off        | 107.14 | 100.0     | 2.14     | 5.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Charge Adjustment | 5.0    | 5.0       | 0.0      | 0.0  | 0.0       | 95.0         | false    | false    |

  @TestRailId:C3571
  Scenario: Charge adjustment on account with zero principal balance should not create accrual transactions without journal entries
    When Admin sets the business date to "25 March 2025"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "25 March 2025", with Principal: "800", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "25 March 2025" with "800" amount and expected disbursement date on "25 March 2025"
    When Admin successfully disburse the loan on "25 March 2025" with "800" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "25 March 2025" with 800 EUR transaction amount
    And Admin adds an NSF fee because of payment bounce with "25 March 2025" transaction date
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "25 March 2025" with 10 EUR transaction amount and externalId ""
    Then Loan Transactions tab has a "CHARGE_ADJUSTMENT" transaction with date "25 March 2025" which has the following Journal entries:
      | Type   | Account code | Account name     | Debit | Credit |
      | ASSET  | 112601       | Loans Receivable |       | 10.0   |
      | INCOME | 404007       | Fee Income       | 10.0  |        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 25 March 2025    | Disbursement      | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        |
      | 25 March 2025    | Repayment         | 800.0  | 790.0     | 0.0      | 0.0  | 10.0      | 10.0         |
      | 25 March 2025    | Charge Adjustment | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 0.0          |
      | 25 March 2025    | Accrual           | 10.0   | 0.0       | 0.0      | 0.0  | 10.0      | 0.0          |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "25 March 2025" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 10.0  |        |
      | INCOME | 404007       | Fee Income              |       | 10.0   |

  @TestRailId:C3546
  Scenario: Verify flat disbursement charge for interest bearing progressive loan - UC1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "FLAT" calculation type and 10.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 10.00           |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 10.0 |           | 10.0  | 10.0 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 10.0 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 27.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 44.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |

  @TestRailId:C3547
  Scenario: Verify amount disbursement charge for interest bearing progressive loan - UC2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.0  | 0.0  | 0.0    | 1.0        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.0  |           | 1.0   | 1.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 1.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.0  | 1.0  | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.0   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 18.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 35.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |

  @TestRailId:C3548
  Scenario: Verify amount+interest disbursement charge for interest bearing progressive loan - UC3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.02 | 0.0  | 0.0    | 1.02        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.02 |           | 1.02  | 1.02 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.02 | 0.0       | 103.07 | 1.02 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.02   | 0.0       | 0.0      | 1.02 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.02 | 1.02 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.02  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.02 |           | 1.02  | 1.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.02 | 0.0       | 103.07 | 18.03 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.02   | 0.0       | 0.0      | 1.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.02 |           | 1.02  | 1.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.02 | 0.0       | 103.07 | 35.04 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.02   | 0.0       | 0.0      | 1.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |

  @TestRailId:C3549
  Scenario: Verify interest disbursement charge for interest bearing progressive loan - UC4
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.0  | 0.0    | 0.02       |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.02 |           | 0.02  | 0.02 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 0.02 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.02 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.02  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 17.03 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 34.04 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |

  @TestRailId:C3550
  Scenario: Verify interest disbursement charge with cash based accounting for interest bearing progressive loan - UC5
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CASH_ACCOUNTING_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.0  | 0.0    | 0.02       |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.02 |           | 0.02  | 0.02 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 0.02 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Accrual                             | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.02 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.02  |        |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "01 January 2024" has no the Journal entries
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 17.03 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Accrual                             | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 34.04 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Accrual                             | 2.05   | 0.0       | 2.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |

  @TestRailId:C3551
  Scenario: Verify interest disbursement charge with accrual based accounting for interest bearing progressive loan - UC6.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 0.02            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.0  | 0.0  | 0.0    | 0.0        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.0  | 0.0  | 0.0    | 0.0         |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 34.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |

  @TestRailId:C3552
  Scenario: Verify amount+interest disbursement charge for interest bearing progressive loan - UC6.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.02            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.04 | 0.0  | 0.0    | 1.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.04 |           | 1.04  | 1.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.04 | 0.0       | 103.09 | 1.04 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.04   | 0.0       | 0.0      | 1.04 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.04 | 1.04 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.04 |           | 1.04  | 1.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.04 | 0.0       | 103.09 | 18.05 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.04   | 0.0       | 0.0      | 1.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.04 |           | 1.04  | 1.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.04 | 0.0       | 103.09 | 35.06 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.04   | 0.0       | 0.0      | 1.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |

  @TestRailId:C3553
  Scenario: Verify interest disbursement charge with undo disbursal for interest bearing progressive loan - UC7
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.0  | 0.0    | 0.02       |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.02 |           | 0.02  | 0.02 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 0.02 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.02 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.02  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 17.03 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 34.04 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
#    --- Undo Disbursement  ---
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.02 |           | 0.02  |      |            |      | 0.02         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 0.0  | 0.0        | 0.0  | 102.07      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.02 | 0.0  | 0.0    | 0.02        |

  @TestRailId:C3578
  Scenario: Verify amount disbursement charge for tranche interest bearing progressive loan that expects one tranche with full disbursement - UC8.1.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.0  |           | 2.0   | 2.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 2.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 2.0  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 2.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 2.0   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 2.0  |           | 2.0   | 2.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 19.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 2.0  | 0.0    | 0.0         |

  @TestRailId:C3579
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that expects one tranche with full disbursement - UC8.1.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type          | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest  | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.04 |           | 2.04  | 2.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 2.04 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 2.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 2.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 2.04 |           | 2.04  | 2.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 19.05 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0         |

  @TestRailId:C3580
  Scenario: Verify interest disbursement charge for tranche interest bearing progressive loan that expects one tranche with full disbursement - UC8.1.3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.0  | 0.0    | 0.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.04 |           | 0.04  | 0.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.04 | 0.0       | 102.09 | 0.04 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.04   | 0.0       | 0.0      | 0.04 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.04 | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.04 |           | 0.04  | 0.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.04 | 0.0       | 102.09 | 17.05 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.04   | 0.0       | 0.0      | 0.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.04 | 0.0    | 0.0         |

  @TestRailId:C3581
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with full disbursement - UC8.1.4
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.0  |           | 2.0   | 2.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 2.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 2.0  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 2.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 2.0   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 2.0  |           | 2.0   | 2.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 19.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.0    | 0.0       | 0.0      | 2.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 2.0  | 0.0    | 0.0        |

  @TestRailId:C3582
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with full disbursement - UC8.1.5
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.04 |           | 2.04  | 2.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 2.04 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type          | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           |  % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 2.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 2.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 2.04 |           | 2.04  | 2.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 19.05 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type          | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           |  % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0        |

  @TestRailId:C3583
  Scenario: Verify interest disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with full disbursement - UC8.1.6
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.0  | 0.0    | 0.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.04 |           | 0.04  | 0.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.04 | 0.0       | 102.09 | 0.04 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.04   | 0.0       | 0.0      | 0.04 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.04 | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.04 |           | 0.04  | 0.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.04 | 0.0       | 102.09 | 17.05 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.04   | 0.0       | 0.0      | 0.04 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.04 | 0.0    | 0.0        |

  @TestRailId:C3554
  Scenario: Verify amount disbursement charge for tranche interest bearing progressive loan that expects one tranche - UC8.2.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.4  |           | 1.4   | 1.4  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.84  | 1.4  | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.4  | 1.4  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.4    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.4   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |  70.0         | 1.44     | 1.4  | 0.0       | 72.84  | 13.31 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |

  @TestRailId:C3555
  Scenario: Verify amount disbursement charge for tranche interest bearing progressive loan that expects two tranches with undo last disbursement - UC8.2.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | 1st_tranche_disb_expected_date |1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024                | 70.0                      | 01 February 2024               | 30.0                       |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.4  | 0.0  | 0.0    | 1.4        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.4  |           | 1.4   | 1.4  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      |    |      | 01 February 2024 |           | 30.0            |               |          | 0.0  |           | 0.0   |      |            |      |  0.0        |
      | 2  | 29   | 01 March 2024    |           | 76.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 65.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 53.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 41.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 30.0            | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.84  | 1.4  | 0.0        | 0.0  | 71.44       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type  | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount          | 1.4  | 1.4  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.4    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.4   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   |       |            |      | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 76.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 65.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 53.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 41.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 30.0            | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.84  | 13.31 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
#    --- 2nd disbursement - 1 February, 2024  ---
    When Admin successfully disburse the loan on "01 February 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 71.01           | 17.49         | 0.52     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 3  | 31   | 01 April 2024    |                  | 53.41           | 17.6          | 0.41     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 4  | 30   | 01 May 2024      |                  | 35.71           | 17.7          | 0.31     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.91           | 17.8          | 0.21     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.91         | 0.1      | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.96     | 1.4  | 0.0       | 103.36 | 13.31 | 0.0        | 0.0  | 90.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
      | 01 February 2024 | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 88.5         | false    | false    |
# -- undo last disbursement ----
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |  70.0         | 1.44     | 1.4  | 0.0       | 72.84  | 13.31 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |

  @Skip
  @TestRailId:C3556
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that expects two tranches with undo disbursement - UC8.2.3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | 1st_tranche_disb_expected_date |1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024                | 70.0                      | 01 February 2024               | 30.0                       |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 2.04 |           | 2.04  | 2.04 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      |    |      | 01 February 2024 |           | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 2.04 | 0.0       | 73.48  | 2.04 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 2.04   |
      | LIABILITY | 145023       | Suspense/Clearing account | 2.04  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 2.04 |           | 2.04  | 2.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 76.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 65.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 53.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 41.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 30.0            | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 2.04 | 0.0       | 73.48  | 13.95 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 2.04   | 0.0       | 0.0      | 2.04 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
#    --- 2nd disbursement - 1 February, 2024  ---
    When Admin successfully disburse the loan on "01 February 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 2.04 |           | 2.04  | 2.04  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 71.01           | 17.49         | 0.52     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 3  | 31   | 01 April 2024    |                  | 53.41           | 17.6          | 0.41     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 4  | 30   | 01 May 2024      |                  | 35.71           | 17.7          | 0.31     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.91           | 17.8          | 0.21     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.91         | 0.1      | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.87     | 2.04 | 0.0       | 104.0  | 13.95 | 0.0        | 0.0  | 90.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
      | 15 February 2024 | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 88.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date  | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |            | 70.0            |               |          | 2.04 |           | 2.04  | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |            | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      |    |      | 01 February 2024 |            | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |            | 71.01           | 17.49         | 0.52     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 3  | 31   | 01 April 2024    |            | 53.41           | 17.6          | 0.41     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 4  | 30   | 01 May 2024      |            | 35.71           | 17.7          | 0.31     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 5  | 31   | 01 June 2024     |            | 17.91           | 17.8          | 0.21     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 6  | 30   | 01 July 2024     |            | 0.0             | 17.91         | 0.1      | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.96     | 2.04 | 0.0       | 104.0  | 0.0   | 0.0        | 0.0  | 104.0       |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 2.04 | 0.0    | 0.0         |

  @TestRailId:C3557
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that expects one tranche - UC8.2.4
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.43 |           | 1.43  | 1.43 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44      | 1.43 | 0.0       | 72.87 | 1.43 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.43 | 1.43 | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.43   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.43  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.43 |           | 1.43  | 1.43  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.43 | 0.0       | 72.87 | 13.34 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |

  @TestRailId:C3558
  Scenario: Verify interest disbursement charge for tranche interest bearing progressive loan that expects one tranche with undo disbursement - UC8.2.5
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % | tranche_disb_expected_date | tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               | 01 January 2024            | 100.0                  |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.0  | 0.0    | 0.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 0.03 |           | 0.03  | 0.03 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 0.03 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.03 | 0.03 | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.03   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.03  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 0.03 |           | 0.03  | 0.03  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 11.94 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           |  70.0           |               |          | 0.03 |           | 0.03  |      |            |      | 0.03        |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 0.00 | 0.0        | 0.0  | 71.47        |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.03 | 0.0  | 0.0    | 0.03        |

  @TestRailId:C3560
  Scenario: Verify amount disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with undo disbursements - UC8.2.6
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.4  |           | 1.4   | 1.4  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.84  | 1.4  | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.4  | 1.4  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.4    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.4   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |  70.0         | 1.44     | 1.4  | 0.0       | 72.84  | 13.31 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
#    --- 2nd disbursement - 1 February, 2024  ---
    When Admin successfully disburse the loan on "01 February 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 71.01           | 17.49         | 0.52     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 3  | 31   | 01 April 2024    |                  | 53.41           | 17.6          | 0.41     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 4  | 30   | 01 May 2024      |                  | 35.71           | 17.7          | 0.31     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.91           | 17.8          | 0.21     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.91         | 0.1      | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.96     | 1.4  | 0.0       | 103.36 | 13.31 | 0.0        | 0.0  | 90.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
      | 01 February 2024 | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 88.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.4  |           | 1.4   |      |            |      | 1.4         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.4  | 0.0       | 103.45 | 0.0   | 0.0        | 0.0  | 103.45      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |

  @TestRailId:C3561
  Scenario: Verify amount+interest disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with undo disbursement - UC8.2.7
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.43 |           | 1.43  | 1.43 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44      | 1.43 | 0.0       | 72.87 | 1.43 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.43 | 1.43 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.43   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.43  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.43 |           | 1.43  | 1.43  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.43 | 0.0       | 72.87 | 13.34 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.04 |           |  2.04 |      |            |      |  2.04       |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 0.0   | 0.0        | 0.0  | 104.09      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |

  @Skip
  @TestRailId:C3562
  Scenario: Verify interest disbursement charge for tranche interest bearing progressive loan that doesn't expect tranches with undo last disbursement - UC8.2.8
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.0  | 0.0    | 0.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 0.03 |           | 0.03  | 0.03 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 0.03 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.03 | 0.03 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.03   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.03  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 0.03 |           | 0.03  | 0.03  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 11.94 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
#    --- 2nd disbursement - 1 February, 2024  ---
    When Admin successfully disburse the loan on "01 February 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 0.03 |           | 0.03  | 0.03  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      |    |      | 01 February 2024 |                  | 30.0            |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 2  | 29   | 01 March 2024    |                  | 71.01           | 17.49         | 0.52     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 3  | 31   | 01 April 2024    |                  | 53.41           | 17.6          | 0.41     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 4  | 30   | 01 May 2024      |                  | 35.71           | 17.7          | 0.31     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.91           | 17.8          | 0.21     | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.91         | 0.1      | 0.0  | 0.0       | 18.01 | 0.0   | 0.0        | 0.0  | 18.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.96     | 0.03 | 0.0       | 101.99 | 11.94 | 0.0        | 0.0  | 90.05       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
      | 15 February 2024 | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 88.5         | false    | false    |
# -- undo last disbursement ----
    When Admin successfully undo last disbursal
    Then Loan status has changed to "Active"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 0.03 |           | 0.03  | 0.03 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 0.03 | 0.0        | 0.0  | 71.44       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.03 | 0.03 | 0.0    | 0.0         |

  @TestRailId:C3563
  Scenario: Verify amount disbursement charge for interest bearing progressive with partial disbursal and with undo disbursement - UC2.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.4  |           | 1.4   | 1.4  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.84  | 1.4  | 0.0        | 0.0  | 71.44       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.4  | 1.4  | 0.0    | 0.0        |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.4    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.4   |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.4  |           | 1.4   | 1.4   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.4  | 0.0       | 72.84  | 13.31 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.4    | 0.0       | 0.0      | 1.4  | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.0  |           | 2.0   |      |            |      | 2.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.0  | 0.0       | 104.05 | 0.0   | 0.0        | 0.0  | 104.05      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 2.0  | 0.0  | 0.0    | 2.0         |

  @TestRailId:C3564
  Scenario: Verify amount+interest disbursement charge for interest bearing progressive loan with partial disbursal and with undo disbursement - UC3.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_LOAN_AMOUNT_PLUS_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 1.43 |           | 1.43  | 1.43 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44      | 1.43 | 0.0       | 72.87 | 1.43 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 1.43 | 1.43 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.43   |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.43  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 1.43 |           | 1.43  | 1.43  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 1.43 | 0.0       | 72.87 | 13.34 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.43   | 0.0       | 0.0      | 1.43 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 2.04 |           | 2.04  |      |            |      | 2.04         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 2.04 | 0.0       | 104.09 | 0.0   | 0.0        | 0.0  | 104.09      |
    Then Loan Transactions tab has none transaction
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type         | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Loan Amount + Interest | 2.04 | 0.0  | 0.0    | 2.04        |

  @TestRailId:C3565
  Scenario: Verify interest disbursement charge for interest bearing progressive loan with partial disbursal - UC4.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_INTEREST" calculation type and 2.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 2               |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.04 | 0.0  | 0.0    | 0.04        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "70" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 70.0            |               |          | 0.03 |           | 0.03  | 0.03 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 2  | 29   | 01 March 2024    |           | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |           | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |           | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |           | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0  | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0  | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 0.03 | 0.0        | 0.0  | 71.44        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Interest       | 0.03 | 0.03 | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.03   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.03  |        |
#    --- 1st repayment - 1 February, 2024  ---
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 11.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 70.0            |               |          | 0.03 |           | 0.03  | 0.03  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 58.5            | 11.5          | 0.41     | 0.0  | 0.0       | 11.91 | 11.91 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 46.93           | 11.57         | 0.34     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 3  | 31   | 01 April 2024    |                  | 35.29           | 11.64         | 0.27     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 4  | 30   | 01 May 2024      |                  | 23.59           | 11.7          | 0.21     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 5  | 31   | 01 June 2024     |                  | 11.82           | 11.77         | 0.14     | 0.0  | 0.0       | 11.91 | 0.0   | 0.0        | 0.0  | 11.91       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 11.82         | 0.07     | 0.0  | 0.0       | 11.89 | 0.0   | 0.0        | 0.0  | 11.89       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 70.0          | 1.44     | 0.03 | 0.0       | 71.47  | 11.94 | 0.0        | 0.0  | 59.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 70.0   | 0.0       | 0.0      | 0.0  | 0.0       | 70.0         | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.03   | 0.0       | 0.0      | 0.03 | 0.0       | 70.0         | false    | false    |
      | 01 February 2024 | Repayment                           | 11.91  | 11.5      | 0.41     | 0.0  | 0.0       | 58.5         | false    | false    |

  @TestRailId:C3566
  Scenario: Verify amount disbursement charge with reversed repayment for backdated interest bearing progressive loan - UC9
    When Admin sets the business date to "01 March 2024"
    When Admin creates a client with random data
    When Admin updates charge "LOAN_DISBURSEMENT_CHARGE" with "PERCENTAGE_AMOUNT" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type  | charge amount % |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | LOAN_DISBURSEMENT_CHARGE | 1.00            |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.0  | 0.0  | 0.0    | 1.0        |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.0  |           | 1.0   | 1.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.14           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.52           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.8            | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.99           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.99         | 0.1      | 0.0  | 0.0       | 17.09 | 0.0  | 0.0        | 0.0  | 17.09       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.14     | 1.0  | 0.0       | 103.14 | 1.0  | 0.0        | 0.0  | 102.14      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                | isPenalty | Payment due at | Due as of | Calculation type | Due  | Paid | Waived | Outstanding |
      | Disbursement Charge | false     | Disbursement   |           | % Amount         | 1.0  | 1.0  | 0.0    | 0.0         |
# -- REPAYMENT_AT_DISBURSEMENT journal entries ----
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.0   |        |
#    --- 1st repayment - 1 February, 2024  ---
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 18.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
#    --- 2nd repayment - 1 March, 2024  ---
    When Admin sets the business date to "02 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0   | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0   | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0   | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 35.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
#    --- First repayment reversed ---
    When Admin sets the business date to "03 March 2024"
    When Customer undo "1"th "Repayment" transaction made on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |       |             |
      | 1  | 31   | 01 February 2024 | 01 March 2024    | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 17.01 | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.14           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.53           | 16.61         | 0.4      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.81           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0   | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.1      | 0.0  | 0.0       | 17.1  | 0.0   | 0.0        | 0.0   | 17.1        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 100.0         | 2.15     | 1.0  | 0.0       | 103.15 | 18.01 | 0.0        | 17.01 | 85.14       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | true     | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | true     |

  @TestRailId:C3613
  Scenario: Verify immediate charge accrual post maturity for Progressive loans
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is enabled
    When Admin sets the business date to "25 February 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL | 25 February 2025  | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "25 February 2025" with "1000" amount and expected disbursement date on "25 February 2025"
    When Admin successfully disburse the loan on "25 February 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 25 February 2025 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 28   | 25 March 2025    |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 25 February 2025 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "28 March 2025" due date and 25 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 28 March 2025 | Flat             | 25.0 | 0.0  | 0.0    | 25.0        |
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 25 February 2025 |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 28   | 25 March 2025    |           | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 2  | 3    | 28 March 2025    |           | 0.0             | 0.0           | 0.0      | 25.0 | 0.0       | 25.0   | 0.0  | 0.0        | 0.0  | 25.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 25.0 | 0.0       | 1025.0 | 0.0  | 0.0        | 0.0  | 1025.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 25 February 2025 | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 28 March 2025    | Accrual          | 25.0   | 0.0       | 0.0      | 25.0 | 0.0       | 0.0          |
    Then LoanAccrualTransactionCreatedBusinessEvent is raised on "28 March 2025"
    Given Global configuration "enable-immediate-charge-accrual-post-maturity" is disabled

  @TestRailId:C3650
  Scenario: Tranche disbursement charges - disbursement flat charge
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT" with "FLAT" calculation type and 10.0 EUR amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                        | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 130            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT | 10.0          | 01 January 2024                | 100.0                      | 03 March 2024                  | 30.0                       |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    And Admin successfully approves the loan on "01 January 2024" with "130" amount and expected disbursement date on "01 January 2024"
    When Admin disburses the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 10.0 |           | 10.0  | 10.0 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 10.0 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |
    Then LoanDisbursalTransactionBusinessEvent has changedTerms "false"
    # Add repayment on 01 February 2024
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 27.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    # Add repayment on 01 March 2024
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 44.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # Add additional disbursement on 03 March 2024
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 30 EUR transaction amount
    When Admin disburses the loan on "03 March 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 30.0            |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 72.99           | 24.06         | 0.55     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 4  | 30   | 01 May 2024      |                  | 48.81           | 24.18         | 0.43     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 5  | 31   | 01 June 2024     |                  | 24.48           | 24.33         | 0.28     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 24.48         | 0.14     | 0.0  | 0.0       | 24.62 | 0.0   | 0.0        | 0.0  | 24.62       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 130.0         | 2.47     | 20.0 | 0.0       | 152.47 | 54.02 | 0.0        | 0.0  | 98.45       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 97.05        | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 10.0   | 0.0       | 0.0      | 10.0 | 0.0       | 97.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 03 March 2024   | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 10.0 | 10.0 | 0.0    | 0.0         |
    Then LoanDisbursalTransactionBusinessEvent has changedTerms "false"
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 10.0   |
      | LIABILITY | 145023       | Suspense/Clearing account | 10.0  |        |

  @TestRailId:C3652
  Scenario: Tranche disbursement charges - disbursement percentage charge
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT" with "PERCENTAGE_DISBURSEMENT_AMOUNT" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                         | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 130            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT | 1.0           | 01 January 2024                | 100.0                      | 03 March 2024                  | 30.0                       |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type                | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount           | 1.0 | 0.0  | 0.0    | 1.0         |
    And Admin successfully approves the loan on "01 January 2024" with "130" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.0  |           | 1.0   | 1.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 1.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type       | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount  | 1.0 | 1.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.0   |        |
    # First repayment
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 18.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    # Second repayment
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 35.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # Second disbursement
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 30 EUR transaction amount
    When Admin successfully disburse the loan on "03 March 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 30.0            |               |          | 0.3  |           | 0.3   | 0.3   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 72.99           | 24.06         | 0.55     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 4  | 30   | 01 May 2024      |                  | 48.81           | 24.18         | 0.43     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 5  | 31   | 01 June 2024     |                  | 24.48           | 24.33         | 0.28     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 24.48         | 0.14     | 0.0  | 0.0       | 24.62 | 0.0   | 0.0        | 0.0  | 24.62       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 130.0         | 2.47     | 1.3  | 0.0       | 133.77 | 35.32 | 0.0        | 0.0  | 98.45       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 97.05        | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 0.3    | 0.0       | 0.0      | 0.3  | 0.0       | 97.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type       | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 03 March 2024   | % Disbursement Amount  | 0.3 | 0.3  | 0.0    | 0.0         |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount  | 1.0 | 1.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.3    |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.3   |        |

  @TestRailId:C3653
  Scenario: Tranche disbursement charges - flat and cash based accounting
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT" with "FLAT" calculation type and 0.02 EUR amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                        | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT | 0.02          | 01 January 2024                | 100.0                      | 03 March 2024                  | 100.0                      |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount  | false     | Tranche Disbursement | 01 January 2024 | Flat             | 0.02 | 0.0  | 0.0    | 0.02        |
    And Admin successfully approves the loan on "01 January 2024" with "130" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.02 |           | 0.02  | 0.02 |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 0.02 | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount  | false     | Tranche Disbursement | 01 January 2024 | Flat             | 0.02 | 0.02 | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.02  |        |
    # First repayment
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 17.03 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    # Second repayment
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.02 | 0.0       | 102.07 | 34.04 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # Second disbursement
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 30 EUR transaction amount
    When Admin successfully disburse the loan on "03 March 2024" with "30" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 30.0            |               |          | 0.02 |           | 0.02  | 0.02  |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 72.99           | 24.06         | 0.55     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 4  | 30   | 01 May 2024      |                  | 48.81           | 24.18         | 0.43     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 5  | 31   | 01 June 2024     |                  | 24.48           | 24.33         | 0.28     | 0.0  | 0.0       | 24.61 | 0.0   | 0.0        | 0.0  | 24.61       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 24.48         | 0.14     | 0.0  | 0.0       | 24.62 | 0.0   | 0.0        | 0.0  | 24.62       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 130.0         | 2.47     | 0.04 | 0.0       | 132.51 | 34.06 | 0.0        | 0.0  | 98.45       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 30.0   | 0.0       | 0.0      | 0.0  | 0.0       | 97.05        | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 0.02   | 0.0       | 0.0      | 0.02 | 0.0       | 97.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 03 March 2024   | Flat             | 0.02 | 0.02 | 0.0    | 0.0         |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 0.02 | 0.02 | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 0.02   |
      | LIABILITY | 145023       | Suspense/Clearing account | 0.02  |        |

  @TestRailId:C3654
  Scenario: Tranche disbursement charges - percentage disbursement and accrual based accounting
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT" with "PERCENTAGE_DISBURSEMENT_AMOUNT" calculation type and 1.0 % of transaction amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                         | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_PERCENT | 1.0           | 01 January 2024                | 100.0                      | 03 March 2024                  | 100.0                      |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type                | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount           | 1.0 | 0.0  | 0.0    | 1.0         |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 1.0  |           | 1.0   | 1.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 1.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type       | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount  | 1.0 | 1.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.0   |        |
    # First repayment
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 18.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    # Second repayment
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 1.0  | 0.0       | 103.05 | 35.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # Second disbursement
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 100 EUR transaction amount
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 1.0  |           | 1.0   | 1.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 125.62          | 41.43         | 0.94     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 4  | 30   | 01 May 2024      |                  | 83.98           | 41.64         | 0.73     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 5  | 31   | 01 June 2024     |                  | 42.1            | 41.88         | 0.49     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 42.1          | 0.25     | 0.0  | 0.0       | 42.35 | 0.0   | 0.0        | 0.0  | 42.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 2.0  | 0.0       | 205.48  | 36.02 | 0.0        | 0.0  | 169.46      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type       | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 03 March 2024   | % Disbursement Amount  | 1.0 | 1.0  | 0.0    | 0.0         |
      | Tranche Disbursement Charge Percent | false     | Tranche Disbursement | 01 January 2024 | % Disbursement Amount  | 1.0 | 1.0  | 0.0    | 0.0         |

    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 1.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 1.0   |        |
    # Run income recognition for accrual test
    And Admin runs the Accrual Activity Posting job
    And Admin runs the Add Accrual Transactions job
    And Admin runs the Add Accrual Transactions For Loans With Income Posted As Transactions job
    And Admin runs the Add Periodic Accrual Transactions job
    And Admin runs the Recalculate Interest for Loans job
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 1.0    | 0.0       | 0.0      | 1.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Accrual                             | 1.1    | 0.0       | 1.1      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "03 March 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 1.1   |        |
      | INCOME | 404000       | Interest Income         |       | 1.1    |

  @TestRailId:C3655
  Scenario: Disbursement charge - flat and accrual based accounting - undo disbursement
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT" with "FLAT" calculation type and 5.0 EUR amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                        | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT | 5.0           | 01 January 2024                | 100.0                      | 03 March 2024                  | 100.0                      |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 0.0  | 0.0    | 5.0         |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 5.0  |           | 5.0   | 5.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 5.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 5.0   |        |
    # First repayment
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 22.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    # Second repayment
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 39.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # Second disbursement
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 100 EUR transaction amount
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 125.62          | 41.43         | 0.94     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 4  | 30   | 01 May 2024      |                  | 83.98           | 41.64         | 0.73     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 5  | 31   | 01 June 2024     |                  | 42.1            | 41.88         | 0.49     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 42.1          | 0.25     | 0.0  | 0.0       | 42.35 | 0.0   | 0.0        | 0.0  | 42.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 10.0 | 0.0       | 213.48 | 44.02 | 0.0        | 0.0  | 169.46      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 03 March 2024   | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 5.0   |        |
    # Run income recognition for accrual test
    When Admin sets the business date to "03 March 2024"
    And Admin runs the Accrual Activity Posting job
    And Admin runs the Add Accrual Transactions job
    And Admin runs the Add Accrual Transactions For Loans With Income Posted As Transactions job
    And Admin runs the Add Periodic Accrual Transactions job
    And Admin runs the Recalculate Interest for Loans job
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Accrual                             | 1.1    | 0.0       | 1.1      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "03 March 2024" which has the following Journal entries:
      | Type   | Account code | Account name              | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable   | 1.1   |        |
      | INCOME | 404000       | Interest Income           |       | 1.1    |
    # Undo disbursement
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 5.0  |           | 5.0   |      |            |      | 5.0         |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      |    |      | 03 March 2024    |           | 100.0           |               |          | 5.0  |           | 5.0   |      |            |      | 5.0         |
      | 3  | 31   | 01 April 2024    |           | 125.62          | 41.43         | 0.94     | 0.0  | 0.0       | 42.37 | 0.0  | 0.0        | 0.0  | 42.37       |
      | 4  | 30   | 01 May 2024      |           | 83.98           | 41.64         | 0.73     | 0.0  | 0.0       | 42.37 | 0.0  | 0.0        | 0.0  | 42.37       |
      | 5  | 31   | 01 June 2024     |           | 42.1            | 41.88         | 0.49     | 0.0  | 0.0       | 42.37 | 0.0  | 0.0        | 0.0  | 42.37       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 42.1          | 0.25     | 0.0  | 0.0       | 42.35 | 0.0  | 0.0        | 0.0  | 42.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 10.0 | 0.0       | 213.48 | 0.0  | 0.0        | 0.0  | 213.48      |
    Then Loan Transactions tab has none transaction

  @TestRailId:C3656
  Scenario: Disbursement charge - flat and accrual based accounting - undo last disbursement
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin updates charge "CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT" with "FLAT" calculation type and 5.0 EUR amount
    When Admin creates a fully customized loan with charges and disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | charge calculation type                        | charge amount | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2024   | 200            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | CHARGE_LOAN_TRANCHE_DISBURSEMENT_CHARGE_AMOUNT | 5.0           | 01 January 2024                | 100.0                      | 03 March 2024                  | 100.0                      |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount  | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 0.0  | 0.0    | 5.0         |
    And Admin successfully approves the loan on "01 January 2024" with "200" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 5.0  |           | 5.0   | 5.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 5.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount  | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 5.0   |        |
    # First repayment
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 22.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    # Second repayment
    When Admin sets the business date to "01 March 2024"
    And Customer makes "AUTOPAY" repayment on "01 March 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 5.0  | 0.0       | 107.05 | 39.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
    # Second disbursement
    When Admin sets the business date to "03 March 2024"
    And Admin successfully add disbursement detail to the loan on "03 March 2024" with 100 EUR transaction amount
    When Admin successfully disburse the loan on "03 March 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      |    |      | 03 March 2024    |                  | 100.0           |               |          | 5.0  |           | 5.0   | 5.0   |            |      |             |
      | 3  | 31   | 01 April 2024    |                  | 125.62          | 41.43         | 0.94     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 4  | 30   | 01 May 2024      |                  | 83.98           | 41.64         | 0.73     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 5  | 31   | 01 June 2024     |                  | 42.1            | 41.88         | 0.49     | 0.0  | 0.0       | 42.37 | 0.0   | 0.0        | 0.0  | 42.37       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 42.1          | 0.25     | 0.0  | 0.0       | 42.35 | 0.0   | 0.0        | 0.0  | 42.35       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 200.0         | 3.48     | 10.0 | 0.0       | 213.48  | 44.02 | 0.0        | 0.0  | 169.46      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 167.05       | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 03 March 2024   | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "03 March 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 5.0   |        |
    # Run income recognition for accrual test
    When Admin sets the business date to "03 March 2024"
    And Admin runs the Accrual Activity Posting job
    And Admin runs the Add Accrual Transactions job
    And Admin runs the Add Accrual Transactions For Loans With Income Posted As Transactions job
    And Admin runs the Add Periodic Accrual Transactions job
    And Admin runs the Recalculate Interest for Loans job
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 167.05       | false    | false    |
      | 03 March 2024    | Accrual                             | 1.1    | 0.0       | 1.1      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Transactions tab has a "ACCRUAL" transaction with date "03 March 2024" which has the following Journal entries:
      | Type   | Account code | Account name            | Debit | Credit |
      | ASSET  | 112603       | Interest/Fee Receivable | 1.1   |        |
      | INCOME | 404000       | Interest Income         |       | 1.1    |
    # Undo last disbursement
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 10.0 |           | 10.0  | 10.0  |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 44.02 | 0.0        | 0.0  | 68.03       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type                    | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement                        | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 January 2024  | Repayment (at time of disbursement) | 5.0    | 0.0       | 0.0      | 5.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment                           | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Repayment                           | 17.01  | 16.52     | 0.49     | 0.0  | 0.0       | 67.05        | false    | false    |
      | 03 March 2024    | Accrual                             | 1.1    | 0.0       | 1.1      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                               | isPenalty | Payment due at       | Due as of       | Calculation type | Due | Paid | Waived | Outstanding |
      | Tranche Disbursement Charge Amount | false     | Tranche Disbursement | 01 January 2024 | Flat             | 5.0 | 5.0  | 0.0    | 0.0         |
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 100.0  |
    Then Loan Transactions tab has a "REPAYMENT_AT_DISBURSEMENT" transaction with date "01 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | INCOME    | 404007       | Fee Income                |       | 5.0    |
      | LIABILITY | 145023       | Suspense/Clearing account | 5.0   |        |

  @TestRailId:C3784
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: flat charge type, interestRecalculation = true
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_DAILY_INSTALLMENT_FEE_FLAT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 27.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 27.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0   | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 27.01 | 0.0        | 0.0  | 135.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.43     | 0.58     | 10.0 | 0.0       | 83.57        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 10.0 | 0.0    | 50.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.58  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 27.01  | 16.43     | 0.58     | 10.0 | 0.0       | 83.57        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.43  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 10.58  |
      | LIABILITY | 145023       | Suspense/Clearing account | 27.01 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.43 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 10.58 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 27.01  |

  @TestRailId:C3811
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: flat charge type, interestRecalculation = true, early repayment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_DAILY_INSTALLMENT_FEE_FLAT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 54.02 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 83.25           | 16.75         | 0.26     | 10.0 | 0.0       | 27.01 | 27.01 | 27.01      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 January 2024 | 66.24           | 17.01         | 0.0      | 10.0 | 0.0       | 27.01 | 27.01 | 27.01      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                 | 50.23           | 16.01         | 1.0      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |                 | 33.51           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |                 | 16.7            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 16.7          | 0.1      | 10.0 | 0.0       | 26.8  | 0.0   | 0.0        | 0.0  | 26.8        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.85     | 60.0 | 0.0       | 161.85 | 54.02 | 54.02      | 0.0  | 107.83      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 54.02  | 33.76     | 0.26     | 20.0 | 0.0       | 66.24        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 20.0 | 0.0    | 40.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 33.76  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 20.26  |
      | LIABILITY | 145023       | Suspense/Clearing account | 54.02 |        |
    When Customer makes a repayment undo on "15 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0  | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 10.0 | 0.0       | 27.0  | 0.0  | 0.0        | 0.0  | 27.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 60.0 | 0.0       | 162.05 | 0.0  | 0.0        | 0.0  | 162.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 54.02  | 33.76     | 0.26     | 20.0 | 0.0       | 66.24        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                 | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment flat fee | false     | Installment Fee |           | Flat             | 60.0  | 0.0  | 0.0    | 60.0        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 33.76  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 20.26  |
      | LIABILITY | 145023       | Suspense/Clearing account | 54.02 |        |
      | ASSET     | 112601       | Loans Receivable          | 33.76 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 20.26 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 54.02  |

  @TestRailId:C3785
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: percentage amount charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_AMOUNT_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.16 | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.17 | 0.0       | 17.21 | 0.0  | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 1.01 | 0.0       | 103.05 | 0.0  | 0.0        | 0.0  | 103.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                              | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount fee | false     | Installment Fee |           | % Amount         | 1.01  | 0.0  | 0.0    | 1.01        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.16 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 0.16 | 0.0       | 17.16 | 17.16 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 0.17 | 0.0       | 17.17 | 0.0   | 0.0        | 0.0  | 17.17       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 0.17 | 0.0       | 17.21 | 0.0   | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 1.01 | 0.0       | 103.05 | 17.16 | 0.0        | 0.0  | 85.89       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.16  | 16.41     | 0.59     | 0.16 | 0.0       | 83.59        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                              | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount fee | false     | Installment Fee |           | % Amount         | 1.01  | 0.16 | 0.0    | 0.85        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.75   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.16 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.16 | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.17 | 0.0       | 17.17 | 0.0  | 0.0        | 0.0  | 17.17       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.17 | 0.0       | 17.21 | 0.0  | 0.0        | 0.0  | 17.21       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 1.01 | 0.0       | 103.05 | 0.0  | 0.0        | 0.0  | 103.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.16  | 16.41     | 0.59     | 0.16 | 0.0       | 83.59        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                              | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage amount fee | false     | Installment Fee |           | % Amount         | 1.01  | 0.0  | 0.0    | 1.01        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.75   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.16 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.41 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 0.75  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.16  |

  @TestRailId:C3786
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: percentage interest charge type, interestRecalculation = false
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_INTEREST_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 0.0  | 0.0        | 0.0  | 102.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.03 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 17.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 0.0   | 0.0        | 0.0  | 17.02       |
      | 3  | 31   | 01 April 2024    |                  | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0   | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |                  | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0   | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 17.03 | 0.0        | 0.0  | 85.1        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.03  | 16.41     | 0.59     | 0.03 | 0.0       | 83.59        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.03 | 0.0    | 0.06        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.62   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.03 |        |
    When Customer makes a repayment undo on "01 February 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 0.0  | 0.0        | 0.0  | 102.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.03  | 16.41     | 0.59     | 0.03 | 0.0       | 83.59        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "01 February 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 16.41  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 0.62   |
      | LIABILITY | 145023       | Suspense/Clearing account | 17.03 |        |
      | ASSET     | 112601       | Loans Receivable          | 16.41 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 0.62  |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 17.03  |

  @TestRailId:C3812
  Scenario: Progressive loan - Verify the loan creation with installment fee charge: percentage interest charge type, interestRecalculation = false, early repayment
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_INTEREST_CHARGES | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 0.0  | 0.0        | 0.0  | 102.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    When Admin sets the business date to "15 January 2024"
    And Customer makes "AUTOPAY" repayment on "15 January 2024" with 34.05 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 15 January 2024 | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 17.03 | 17.03      | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 January 2024 | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 17.02 | 17.02      | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                 | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0   | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |                 | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                 | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                 | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0   | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 34.05 | 34.05      | 0.0  | 68.08       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 34.05  | 32.95     | 1.05     | 0.05 | 0.0       | 67.05        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.05 | 0.0    | 0.04        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 32.95  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 1.1    |
      | LIABILITY | 145023       | Suspense/Clearing account | 34.05 |        |
    When Customer makes a repayment undo on "15 January 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.59           | 16.41         | 0.59     | 0.03 | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.54         | 0.46     | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 3  | 31   | 01 April 2024    |           | 50.45           | 16.6          | 0.4      | 0.02 | 0.0       | 17.02 | 0.0  | 0.0        | 0.0  | 17.02       |
      | 4  | 30   | 01 May 2024      |           | 33.74           | 16.71         | 0.29     | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.94           | 16.8          | 0.2      | 0.01 | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.94         | 0.1      | 0.0  | 0.0       | 17.04 | 0.0  | 0.0        | 0.0  | 17.04       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.04     | 0.09 | 0.0       | 102.13 | 0.0  | 0.0        | 0.0  | 102.13      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 15 January 2024  | Repayment        | 34.05  | 32.95     | 1.05     | 0.05 | 0.0       | 67.05        | true     | false    |
    Then Loan Charges tab has the following data:
      | Name                                | isPenalty | Payment due at  | Due as of | Calculation type | Due   | Paid | Waived | Outstanding |
      | Installment percentage interest fee | false     | Installment Fee |           | % Interest       | 0.09  | 0.0  | 0.0    | 0.09        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "15 January 2024" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 32.95  |
      | ASSET     | 112603       | Interest/Fee Receivable   |       | 1.1    |
      | LIABILITY | 145023       | Suspense/Clearing account | 34.05 |        |
      | ASSET     | 112601       | Loans Receivable          | 32.95 |        |
      | ASSET     | 112603       | Interest/Fee Receivable   | 1.1   |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 34.05  |

