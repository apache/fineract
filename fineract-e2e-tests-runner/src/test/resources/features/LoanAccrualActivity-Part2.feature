@LoanAccrualActivityFeature
Feature: LoanAccrualActivity - Part2

  @TestRailId:C3527
  Scenario: Verify accrual activity behavior in case repayment reversal before the installment date - UC3
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2025   | 800            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "800" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "800" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.6           | 131.4         | 4.67     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 2  | 28   | 01 March 2025    |           | 536.43          | 132.17        | 3.9      | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.49          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.77          | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 135.27          | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 135.27        | 0.79     | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.00        | 16.41    | 0.0  | 0.0       | 816.41 | 0.0  | 0.0        | 0.0  | 816.41      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    When Admin sets the business date to "27 January 2025"
    And Customer makes "AUTOPAY" repayment on "27 January 2025" with 803.91 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 27 January 2025 | 667.84          | 132.16        | 3.91     | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    | 27 January 2025 | 531.77          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025    | 27 January 2025 | 395.7           | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025      | 27 January 2025 | 259.63          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2025     | 27 January 2025 | 123.56          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2025     | 27 January 2025 | 0.0             | 123.56        | 0.0      | 0.0  | 0.0       | 123.56 | 123.56 | 123.56     | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 800.00        | 3.91     | 0.0  | 0.0       | 803.91 | 803.91 | 803.91     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 27 January 2025  | Repayment        | 803.91 | 800.0     | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual          | 3.91   | 0.0       | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual Activity | 3.91   | 0.0       | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 January 2025"
    And Customer undo "1"th "Repayment" transaction made on "27 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.6           | 131.4         | 4.67     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 2  | 28   | 01 March 2025    |           | 536.43          | 132.17        | 3.9      | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.49          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.77          | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 135.27          | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 135.27        | 0.79     | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.00        | 16.41    | 0.0  | 0.0       | 816.41 | 0.0  | 0.0        | 0.0  | 816.41      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 27 January 2025  | Repayment        | 803.91 | 800.0     | 3.91     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 27 January 2025  | Accrual          | 3.91   | 0.0       | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3528
  Scenario: Verify accrual activity behavior in case repayment reversal after the installment date - UC4
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2025   | 800            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "800" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "800" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.6           | 131.4         | 4.67     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 2  | 28   | 01 March 2025    |           | 536.43          | 132.17        | 3.9      | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.49          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.77          | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 135.27          | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 135.27        | 0.79     | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.00        | 16.41    | 0.0  | 0.0       | 816.41 | 0.0  | 0.0        | 0.0  | 816.41      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    When Admin sets the business date to "27 January 2025"
    And Customer makes "AUTOPAY" repayment on "27 January 2025" with 816.46 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 27 January 2025 | 667.84          | 132.16        | 3.91     | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    | 27 January 2025 | 531.77          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025    | 27 January 2025 | 395.7           | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025      | 27 January 2025 | 259.63          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2025     | 27 January 2025 | 123.56          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2025     | 27 January 2025 | 0.0             | 123.56        | 0.0      | 0.0  | 0.0       | 123.56 | 123.56 | 123.56     | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 800.00        | 3.91     | 0.0  | 0.0       | 803.91 | 803.91 | 803.91     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 27 January 2025  | Repayment        | 816.46 | 800.0     | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual          | 3.91   | 0.0       | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual Activity | 3.91   | 0.0       | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Save external ID of "Accrual Activity" transaction made on "27 January 2025" as "saved-external-id"
    When Admin sets the business date to "03 February 2025"
    And Customer undo "1"th "Repayment" transaction made on "27 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.6           | 131.4         | 4.67     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 2  | 28   | 01 March 2025    |           | 536.48          | 132.12        | 3.95     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.54          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.82          | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 135.32          | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 135.32        | 0.79     | 0.0  | 0.0       | 136.11 | 0.0  | 0.0        | 0.0  | 136.11      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.00        | 16.46    | 0.0  | 0.0       | 816.46 | 0.0  | 0.0        | 0.0  | 816.46      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 27 January 2025  | Repayment        | 816.46 | 800.0     | 3.91     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 27 January 2025  | Accrual          | 3.91   | 0.0       | 3.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual Activity | 4.67   | 0.0       | 4.67     | 0.0  | 0.0       | 0.0          | false    | true     |
    And "Accrual Activity" transaction on "01 February 2025" got reverse-replayed on "03 February 2025"
    And External ID of replayed "Accrual Activity" on "01 February 2025" is matching with "saved-external-id"

  @TestRailId:C3529
  Scenario: Verify accrual activity behavior when COB runs on installment date and backdated repayment happens - UC5
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2025   | 800            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "800" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "800" EUR transaction amount
    When Admin sets the business date to "02 February 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.6           | 131.4         | 4.67     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 2  | 28   | 01 March 2025    |           | 536.46          | 132.14        | 3.93     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.52          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.8           | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 135.3           | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 135.3         | 0.79     | 0.0  | 0.0       | 136.09 | 0.0  | 0.0        | 0.0  | 136.09      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.00        | 16.44    | 0.0  | 0.0       | 816.44 | 0.0  | 0.0        | 0.0  | 816.44      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 01 February 2025 | Accrual          | 4.67   | 0.0       | 4.67     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual Activity | 4.67   | 0.0       | 4.67     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Save external ID of "Accrual Activity" transaction made on "01 February 2025" as "saved-external-id"
    When Customer makes "AUTOPAY" repayment on "27 January 2025" with 800 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 800.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 27 January 2025 | 667.84          | 132.16        | 3.91     | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    | 27 January 2025 | 531.77          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025    | 27 January 2025 | 395.7           | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 4  | 30   | 01 May 2025      | 27 January 2025 | 259.63          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 5  | 31   | 01 June 2025     | 27 January 2025 | 123.56          | 136.07        | 0.0      | 0.0  | 0.0       | 136.07 | 136.07 | 136.07     | 0.0  | 0.0         |
      | 6  | 30   | 01 July 2025     |                 | 0.0             | 123.56        | 0.11     | 0.0  | 0.0       | 123.67 | 119.65 | 119.65     | 0.0  | 4.02        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 800.00        | 4.02     | 0.0  | 0.0       | 804.02 | 800.0 | 800.0      | 0.0  | 4.02        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 27 January 2025  | Repayment        | 800.0  | 796.09    | 3.91     | 0.0  | 0.0       | 3.91         | false    | false    |
      | 01 February 2025 | Accrual          | 4.67   | 0.0       | 4.67     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual Activity | 3.91   | 0.0       | 3.91     | 0.0  | 0.0       | 0.0          | false    | true     |
    And "Accrual Activity" transaction on "01 February 2025" got reverse-replayed on "02 February 2025"
    And External ID of replayed "Accrual Activity" on "01 February 2025" is matching with "saved-external-id"

  @TestRailId:C3530
  Scenario: Verify accrual activity behavior in case of partial payment which is reversed after the installment date - UC6
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2025   | 800            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "800" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "800" EUR transaction amount
    And Admin sets the business date to "15 January 2025"
    And Customer makes "AUTOPAY" repayment on "15 January 2025" with 100 EUR transaction amount
    And Admin sets the business date to "02 February 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.28          | 131.72        | 4.35     | 0.0  | 0.0       | 136.07 | 100.0 | 100.0      | 0.0  | 36.07       |
      | 2  | 28   | 01 March 2025    |           | 536.11          | 132.17        | 3.9      | 0.0  | 0.0       | 136.07 | 0.0   | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.17          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0   | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.45          | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0   | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 134.95          | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0   | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 134.95        | 0.79     | 0.0  | 0.0       | 135.74 | 0.0   | 0.0        | 0.0  | 135.74      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 800.00        | 16.09    | 0.0  | 0.0       | 816.09 | 100.0 | 100.0      | 0.0  | 716.09      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 15 January 2025  | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 01 February 2025 | Accrual          | 4.35   | 0.0       | 4.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual Activity | 4.35   | 0.0       | 4.35     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Save external ID of "Accrual Activity" transaction made on "01 February 2025" as "saved-external-id"
    When Customer undo "1"th "Repayment" transaction made on "15 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.6           | 131.4         | 4.67     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 2  | 28   | 01 March 2025    |           | 536.46          | 132.14        | 3.93     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.52          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.8           | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 135.3           | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 135.3         | 0.79     | 0.0  | 0.0       | 136.09 | 0.0  | 0.0        | 0.0  | 136.09      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 800.00        | 16.44    | 0.0  | 0.0       | 816.44 | 0.0  | 0.0        | 0.0  | 816.44      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 15 January 2025  | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 700.0        | true     | false    |
      | 01 February 2025 | Accrual          | 4.35   | 0.0       | 4.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual Activity | 4.67   | 0.0       | 4.67     | 0.0  | 0.0       | 0.0          | false    | true     |
    And "Accrual Activity" transaction on "01 February 2025" got reverse-replayed on "02 February 2025"
    And External ID of replayed "Accrual Activity" on "01 February 2025" is matching with "saved-external-id"

  @TestRailId:C3533
  Scenario: Logging out transaction list, excluded given transaction types
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECOGNITION_DISBURSEMENT_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2025   | 2000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "2000" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "2000" EUR transaction amount
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "05 January 2025"
    And Customer makes "AUTOPAY" repayment on "05 January 2025" with 200 EUR transaction amount
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 2000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 2000.0       | false    | false    |
      | 01 January 2025  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 January 2025  | Accrual          | 0.37   | 0.0       | 0.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Repayment        | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 1800.0       | false    | false    |
    Then Log out transaction list by loanId, filtered out the following transaction types: "disbursement, accrual"
    Then Log out transaction list by loanExternalId, filtered out the following transaction types: "accrual"
    Then Filtered out transactions list contains the the following entries when filtered out by loanId for transaction types: "accrual"
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2025  | disbursement     | 2000.0 |           |          |      |           | 2000.0       |
      | 05 January 2025  | repayment        | 200.0  | 200.0     |          |      |           | 1800.0       |
    Then Filtered out transactions list contains the the following entries when filtered out by loanExternalId for transaction types: "accrual"
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2025  | disbursement     | 2000.0 |           |          |      |           | 2000.0       |
      | 05 January 2025  | repayment        | 200.0  | 200.0     |          |      |           | 1800.0       |
    Then Filtered out transactions list contains the the following entries when filtered out by loanId for transaction types: ""
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2025  | disbursement     | 2000.0 |           |          |      |           | 2000.0       |
      | 01 January 2025  | accrual          | 0.38   |           | 0.38     |      |           |              |
      | 02 January 2025  | accrual          | 0.37   |           | 0.37     |      |           |              |
      | 03 January 2025  | accrual          | 0.38   |           | 0.38     |      |           |              |
      | 04 January 2025  | accrual          | 0.38   |           | 0.38     |      |           |              |
      | 05 January 2025  | repayment        | 200.0  | 200.0     |          |      |           | 1800.0       |
    Then Filtered out transactions list contains the the following entries when filtered out by loanId for transaction types: "merchant_issued_refund"
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2025  | disbursement     | 2000.0 |           |          |      |           | 2000.0       |
      | 01 January 2025  | accrual          | 0.38   |           | 0.38     |      |           |              |
      | 02 January 2025  | accrual          | 0.37   |           | 0.37     |      |           |              |
      | 03 January 2025  | accrual          | 0.38   |           | 0.38     |      |           |              |
      | 04 January 2025  | accrual          | 0.38   |           | 0.38     |      |           |              |
      | 05 January 2025  | repayment        | 200.0  | 200.0     |          |      |           | 1800.0       |
    Then Filtered out transactions list has 4 pages in case of size set to 1 and transactions are filtered out for transaction types: "disbursement, repayment"

  @TestRailId:C3692
  Scenario: Verify that accruals are added in case of reversed repayment made before MIR and CBR for progressive loan with downpayment - UC1
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_DOWNPAYMENT | 21 March 2025     | 242.46         | 29.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "242.46" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "242.46" EUR transaction amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     |               | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  |  0.0  | 0.0        | 0.0  | 60.62       |
      | 2  | 31   | 21 April 2025     |               | 168.65          | 13.19         | 4.54     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 3  | 30   | 21 May 2025       |               | 155.13          | 13.52         | 4.21     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 4  | 31   | 21 June 2025      |               | 141.28          | 13.85         | 3.88     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 5  | 30   | 21 July 2025      |               | 127.08          | 14.2          | 3.53     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 6  | 31   | 21 August 2025    |               | 112.53          | 14.55         | 3.18     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 7  | 31   | 21 September 2025 |               |  97.61          | 14.92         | 2.81     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 8  | 30   | 21 October 2025   |               |  82.32          | 15.29         | 2.44     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 9  | 31   | 21 November 2025  |               |  66.65          | 15.67         | 2.06     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 10 | 30   | 21 December 2025  |               |  50.59          | 16.06         | 1.67     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 11 | 31   | 21 January 2026   |               |  34.12          | 16.47         | 1.26     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 12 | 31   | 21 February 2026  |               |  17.24          | 16.88         | 0.85     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 13 | 28   | 21 March 2026     |               |   0.0           | 17.24         | 0.43     | 0.0  | 0.0       | 17.67  |  0.0  | 0.0        | 0.0  | 17.67       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 30.86    | 0.0  | 0.0       | 273.32 | 0.0    | 0.0        | 0.0  | 273.32      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 100 EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 March 2025" with 242.46 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     | 21 March 2025 | 164.11          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 146.38          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 128.65          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 110.92          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 |  93.19          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  75.46          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  57.73          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  40.0           | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  22.27          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |   4.54          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  4.54         | 0.0      | 0.0  | 0.0       |  4.54  |  4.54 |  4.54      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 181.84      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 March 2025" with 100 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     | 21 March 2025 | 164.11          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 146.38          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 128.65          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 110.92          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 |  93.19          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  75.46          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  57.73          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  40.0           | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  22.27          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |   4.54          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  4.54         | 0.0      | 0.0  | 0.0       |  4.54  |  4.54 |  4.54      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 181.84       | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "02 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 March 2025"
    When Admin sets the business date to "15 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     |               | 166.69          | 115.15        | 1.93     | 0.0  | 0.0       | 117.08 | 15.15 | 15.15      | 0.0  | 101.93      |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 151.54          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 136.39          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 121.24          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 | 106.09          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  90.94          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  75.79          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  60.64          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  45.49          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |  30.34          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |  15.19          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           | 15.19         | 0.0      | 0.0  | 0.0       | 15.19  | 15.19 | 15.19      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 1.93     | 0.0  | 0.0       | 344.39 | 242.46 | 181.84     | 0.0  | 101.93      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 14 April 2025    | Accrual                | 1.37   | 0.0       | 1.37     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true    |
      | 14 April 2025    | Accrual                | 1.37   | 0.0       | 1.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |

    When Loan Pay-off is made on "21 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3693
  Scenario: Verify that accruals are added in case of reversed repayment made before MIR and CBR for progressive loan with auto downpayment - UC2
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_AUTO_DOWNPAYMENT | 21 March 2025     | 242.46         | 29.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "242.46" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "242.46" EUR transaction amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  |  0.0        |
      | 2  | 31   | 21 April 2025     |               | 168.65          | 13.19         | 4.54     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 3  | 30   | 21 May 2025       |               | 155.13          | 13.52         | 4.21     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 4  | 31   | 21 June 2025      |               | 141.28          | 13.85         | 3.88     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 5  | 30   | 21 July 2025      |               | 127.08          | 14.2          | 3.53     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 6  | 31   | 21 August 2025    |               | 112.53          | 14.55         | 3.18     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 7  | 31   | 21 September 2025 |               |  97.61          | 14.92         | 2.81     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 8  | 30   | 21 October 2025   |               |  82.32          | 15.29         | 2.44     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 9  | 31   | 21 November 2025  |               |  66.65          | 15.67         | 2.06     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 10 | 30   | 21 December 2025  |               |  50.59          | 16.06         | 1.67     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 11 | 31   | 21 January 2026   |               |  34.12          | 16.47         | 1.26     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 12 | 31   | 21 February 2026  |               |  17.24          | 16.88         | 0.85     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 13 | 28   | 21 March 2026     |               |   0.0           | 17.24         | 0.43     | 0.0  | 0.0       | 17.67  |  0.0  | 0.0        | 0.0  | 17.67       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 30.86    | 0.0  | 0.0       | 273.32 | 60.62  | 0.0        | 0.0  | 212.7       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Down Payment           | 60.62  | 60.62     | 0.0      | 0.0  | 0.0       | 181.84       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 100 EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 March 2025" with 242.46 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 160.62 overpaid amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     | 21 March 2025 | 164.11          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 146.38          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 128.65          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 110.92          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 |  93.19          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  75.46          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  57.73          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  40.0           | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  22.27          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |   4.54          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  4.54         | 0.0      | 0.0  | 0.0       |  4.54  |  4.54 |  4.54      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 181.84      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Down Payment           | 60.62  | 60.62     | 0.0      | 0.0  | 0.0       | 181.84       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 81.84        | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 81.84     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 March 2025" with 160.62 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     | 21 March 2025 | 164.11          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 146.38          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 128.65          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 110.92          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 |  93.19          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  75.46          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  57.73          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  40.0           | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  22.27          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |   4.54          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  4.54         | 0.0      | 0.0  | 0.0       |  4.54  |  4.54 |  4.54      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 181.84       | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Down Payment           | 60.62  | 60.62     | 0.0      | 0.0  | 0.0       | 181.84       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 81.84        | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 81.84     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2025    | Credit Balance Refund  | 160.62 | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "02 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 March 2025"
    When Admin sets the business date to "15 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     |               | 164.11          | 178.35        | 1.93     | 0.0  | 0.0       | 180.28 | 78.35 | 78.35      | 0.0  | 101.93        |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 146.38          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 128.65          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 110.92          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 |  93.19          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  75.46          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  57.73          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  40.0           | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  22.27          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |   4.54          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  4.54         | 0.0      | 0.0  | 0.0       |  4.54  |  4.54 |  4.54      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 403.08        | 1.93     | 0.0  | 0.0       | 405.01 | 303.08 | 242.46     | 0.0  | 101.93        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Down Payment           | 60.62  | 60.62     | 0.0      | 0.0  | 0.0       | 181.84       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 81.84        | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 181.84    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 160.62 | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 14 April 2025    | Accrual                | 1.37   | 0.0       | 1.37     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Down Payment           | 60.62  | 60.62     | 0.0      | 0.0  | 0.0       | 181.84       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 81.84        | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 181.84    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 160.62 | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 14 April 2025    | Accrual                | 1.37   | 0.0       | 1.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |

    When Loan Pay-off is made on "21 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3694
  Scenario: Verify repayment and accruals are added after reversed repayment made before MIR and CBR for progressive loan - UC3
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 21 March 2025     | 242.46         | 29.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "242.46" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "242.46" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 224.88          | 17.58         | 6.06     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 2  | 30   | 21 May 2025       |               | 206.86          | 18.02         | 5.62     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 3  | 31   | 21 June 2025      |               | 188.39          | 18.47         | 5.17     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 4  | 30   | 21 July 2025      |               | 169.46          | 18.93         | 4.71     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 5  | 31   | 21 August 2025    |               | 150.06          | 19.4          | 4.24     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 6  | 31   | 21 September 2025 |               | 130.17          | 19.89         | 3.75     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 7  | 30   | 21 October 2025   |               | 109.78          | 20.39         | 3.25     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 8  | 31   | 21 November 2025  |               |  88.88          | 20.9          | 2.74     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 9  | 30   | 21 December 2025  |               |  67.46          | 21.42         | 2.22     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 10 | 31   | 21 January 2026   |               |  45.51          | 21.95         | 1.69     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 11 | 31   | 21 February 2026  |               |  23.01          | 22.5          | 1.14     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 12 | 28   | 21 March 2026     |               |   0.0           | 23.01         | 0.58     | 0.0  | 0.0       | 23.59  |  0.0  | 0.0        | 0.0  | 23.59       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 41.17    | 0.0  | 0.0       | 283.63 | 0.0    | 0.0        | 0.0  | 283.63     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 40 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 60 EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 March 2025" with 242.46 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 March 2025 | 218.82          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 195.18          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 171.54          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 147.9           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 124.26          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 100.62          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  76.98          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  53.34          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  29.7           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |   6.06          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  6.06         | 0.0      | 0.0  | 0.0       |  6.06  |  6.06 |  6.06      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 242.46     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 40.0   | 40.0      | 0.0      | 0.0  | 0.0       | 202.46       | false    | false    |
      | 21 March 2025    | Repayment              | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 March 2025" with 100 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 March 2025 | 218.82          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 195.18          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 171.54          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 147.9           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 124.26          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 100.62          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  76.98          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  53.34          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  29.7           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |   6.06          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  6.06         | 0.0      | 0.0  | 0.0       |  6.06  |  6.06 |  6.06      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 242.46     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 40.0   | 40.0      | 0.0      | 0.0  | 0.0       | 202.46       | false    | false    |
      | 21 March 2025    | Repayment              | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "02 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 March 2025"
    When Admin sets the business date to "15 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 218.82          | 123.64        | 0.77     | 0.0  | 0.0       | 124.41 | 83.64 | 83.64      | 0.0  | 40.77       |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 195.18          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 171.54          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 147.9           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 124.26          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 100.62          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  76.98          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  53.34          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  29.7           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |   6.06          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  6.06         | 0.0      | 0.0  | 0.0       |  6.06  |  6.06 |  6.06      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 0.77     | 0.0  | 0.0       | 343.23 | 302.46 | 302.46     | 0.0  | 40.77       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 40.0   | 40.0      | 0.0      | 0.0  | 0.0       | 202.46       | true     | false    |
      | 21 March 2025    | Repayment              | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 182.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 182.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 40.0      | 0.0      | 0.0  | 0.0       | 40.0         | false    | true     |
      | 14 April 2025    | Accrual                | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "21 April 2025" with 80 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0   |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 218.82          | 123.64        | 0.77     | 0.0  | 0.0       | 124.41 | 124.41 | 83.64      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 195.18          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 171.54          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 147.9           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 124.26          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 100.62          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  76.98          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  53.34          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  29.7           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |   6.06          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64  | 23.64      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  6.06         | 0.0      | 0.0  | 0.0       |  6.06  |  6.06  |  6.06      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0   |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 0.77     | 0.0  | 0.0       | 343.23 | 343.23 | 302.46     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 40.0   | 40.0      | 0.0      | 0.0  | 0.0       | 202.46       | true     | false    |
      | 21 March 2025    | Repayment              | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 182.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 182.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 40.0      | 0.0      | 0.0  | 0.0       | 40.0         | false    | true     |
      | 14 April 2025    | Accrual                | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Repayment              | 80.0   | 40.0      | 0.77     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "23 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 April 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 218.82          | 123.64        | 0.84     | 0.0  | 0.0       | 124.48 | 83.64 | 83.64      | 0.0  | 40.84       |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 195.18          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 171.54          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 147.9           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 124.26          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 100.62          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  76.98          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  53.34          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  29.7           | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |   6.06          | 23.64         | 0.0      | 0.0  | 0.0       | 23.64  | 23.64 | 23.64      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  6.06         | 0.0      | 0.0  | 0.0       |  6.06  |  6.06 |  6.06      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 0.84     | 0.0  | 0.0       | 343.3  | 302.46 | 302.46     | 0.0  | 40.84       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 40.0   | 40.0      | 0.0      | 0.0  | 0.0       | 202.46       | true     | false    |
      | 21 March 2025    | Repayment              | 60.0   | 60.0      | 0.0      | 0.0  | 0.0       | 182.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 182.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 40.0      | 0.0      | 0.0  | 0.0       | 40.0         | false    | true     |
      | 14 April 2025    | Accrual                | 0.55   | 0.0       | 0.55     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Repayment              | 80.0   | 40.0      | 0.77     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 21 April 2025    | Accrual                | 0.03   | 0.0       | 0.03     | 0.0  | 0.0       | 0.0          | false    | false    |

    When Loan Pay-off is made on "21 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3695
  Scenario: Verify accrual activity of overpaid loan in case of reversed repayment made before MIR and CBR for loan with interest refund - UC4
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_FULL | 21 March 2025     | 242.46         | 29.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "242.46" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "242.46" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 224.98          | 17.48         | 6.18     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 2  | 30   | 21 May 2025       |               | 206.87          | 18.11         | 5.55     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 3  | 31   | 21 June 2025      |               | 188.48          | 18.39         | 5.27     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 4  | 30   | 21 July 2025      |               | 169.47          | 19.01         | 4.65     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 5  | 31   | 21 August 2025    |               | 150.13          | 19.34         | 4.32     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 6  | 31   | 21 September 2025 |               | 130.29          | 19.84         | 3.82     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 7  | 30   | 21 October 2025   |               | 109.84          | 20.45         | 3.21     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 8  | 31   | 21 November 2025  |               |  88.98          | 20.86         | 2.8      | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 9  | 30   | 21 December 2025  |               |  67.51          | 21.47         | 2.19     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 10 | 31   | 21 January 2026   |               |  45.57          | 21.94         | 1.72     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 11 | 31   | 21 February 2026  |               |  23.07          | 22.5          | 1.16     | 0.0  | 0.0       | 23.66  |  0.0  | 0.0        | 0.0  | 23.66       |
      | 12 | 28   | 21 March 2026     |               |   0.0           | 23.07         | 0.53     | 0.0  | 0.0       | 23.6   |  0.0  | 0.0        | 0.0  | 23.6        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 41.4     | 0.0  | 0.0       | 283.86 | 0.0    | 0.0        | 0.0  | 283.86      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 March 2025" with 142.46 EUR transaction amount and system-generated Idempotency key
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 200 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 March 2025 | 218.8           | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 195.14          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 171.48          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 147.82          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 124.16          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 100.5           | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  76.84          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  53.18          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  29.52          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |   5.86          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  5.86         | 0.0      | 0.0  | 0.0       |  5.86  |  5.86 |  5.86      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 242.46     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 21 March 2025    | Repayment              | 200.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 March 2025" with 100 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin sets the business date to "02 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Merchant Issued Refund" transaction made on "21 March 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 221.85          | 120.61        | 3.05     | 0.0  | 0.0       | 123.66 | 23.66 | 23.66      | 0.0  | 100.0       |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 198.19          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 174.53          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 150.87          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 127.21          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 103.55          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  79.89          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  56.23          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  |               |  41.69          | 14.54         | 9.12     | 0.0  | 0.0       | 23.66  | 10.72 | 10.72      | 0.0  | 12.94       |
      | 10 | 31   | 21 January 2026   |               |  19.09          | 22.6          | 1.06     | 0.0  | 0.0       | 23.66  |  0.0  |  0.0       | 0.0  | 23.66       |
      | 11 | 31   | 21 February 2026  |               |   0.0           | 19.09         | 0.49     | 0.0  | 0.0       | 19.58  |  0.0  |  0.0       | 0.0  | 19.58       |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  |  0.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 13.72    | 0.0  | 0.0       | 356.18 | 200.0  | 200.0      | 0.0  | 156.18      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
      | 21 March 2025    | Repayment              | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 42.46        | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | true     |
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 April 2025" with 100 EUR transaction amount and system-generated Idempotency key
    And Customer makes "AUTOPAY" repayment on "21 April 2025" with 200 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 156.7 overpaid amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 221.85          | 120.61        | 3.05     | 0.0  | 0.0       | 123.66 | 123.66| 23.66      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 198.19          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 174.53          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 150.87          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 127.21          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 103.55          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  79.89          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  56.23          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 April 2025 |  32.57          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 April 2025 |   8.91          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 April 2025 |   0.0           |  8.91         | 0.0      | 0.0  | 0.0       |  8.91  |  8.91 |  8.91      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 3.05     | 0.0  | 0.0       | 345.51 | 345.51 | 245.51     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
      | 21 March 2025    | Repayment              | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 42.46        | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | true     |
      | 20 April 2025    | Accrual                | 2.94   | 0.0       | 2.94     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Merchant Issued Refund | 100.0  | 96.95     | 3.05     | 0.0  | 0.0       | 45.51        | false    | false    |
      | 21 April 2025    | Interest Refund        | 2.21   | 2.21      | 0.0      | 0.0  | 0.0       | 43.3         | false    | false    |
      | 21 April 2025    | Repayment              | 200.0  | 43.3      | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "23 April 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "23 April 2025" with 156.7 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0   |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 221.85          | 120.61        | 3.05     | 0.0  | 0.0       | 123.66 | 123.66 | 23.66      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 198.19          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 174.53          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 150.87          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 127.21          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 103.55          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  79.89          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  56.23          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 April 2025 |  32.57          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 April 2025 |   8.91          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66  | 23.66      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 April 2025 |   0.0           |  8.91         | 0.0      | 0.0  | 0.0       |  8.91  |  8.91  |  8.91      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0   |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 3.05     | 0.0  | 0.0       | 345.51 | 345.51 | 245.51     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
      | 21 March 2025    | Repayment              | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 42.46        | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | true     |
      | 20 April 2025    | Accrual                | 2.94   | 0.0       | 2.94     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Merchant Issued Refund | 100.0  | 96.95     | 3.05     | 0.0  | 0.0       | 45.51        | false    | false    |
      | 21 April 2025    | Interest Refund        | 2.21   | 2.21      | 0.0      | 0.0  | 0.0       | 43.3         | false    | false    |
      | 21 April 2025    | Repayment              | 200.0  | 43.3      | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Credit Balance Refund  | 156.7  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "25 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Merchant Issued Refund" transaction made on "21 April 2025"
    Then Loan has 104.56 outstanding amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 221.85          | 120.61        | 3.05     | 0.0  | 0.0       | 123.66 | 123.66| 23.66      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       |               | 198.19          | 180.36        | 2.35     | 0.0  | 0.0       | 182.71 | 78.15 | 78.15      | 0.0  | 104.56      |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 174.53          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 150.87          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 127.21          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 103.55          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  79.89          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  56.23          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 April 2025 |  32.57          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 April 2025 |   8.91          | 23.66         | 0.0      | 0.0  | 0.0       | 23.66  | 23.66 | 23.66      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 April 2025 |   0.0           |  8.91         | 0.0      | 0.0  | 0.0       |  8.91  |  8.91 |  8.91      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 499.16        | 5.4      | 0.0  | 0.0       | 504.56 | 400.0  | 300.0      | 0.0  | 104.56      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
      | 21 March 2025    | Repayment              | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 42.46        | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | true     |
      | 20 April 2025    | Accrual                | 2.94   | 0.0       | 2.94     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Merchant Issued Refund | 100.0  | 96.95     | 3.05     | 0.0  | 0.0       | 45.51        | true     | false    |
      | 21 April 2025    | Interest Refund        | 2.21   | 2.21      | 0.0      | 0.0  | 0.0       | 43.3         | true     | false    |
      | 21 April 2025    | Accrual                | 0.11   | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Repayment              | 200.0  | 142.46    | 3.05     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 23 April 2025    | Credit Balance Refund  | 156.7  | 102.21    | 0.0      | 0.0  | 0.0       | 102.21       | false    | true     |
# - CBR on active loan - with outstanding amount is forbidden -#
    Then Credit Balance Refund transaction on active loan "25 April 2025" with 100 EUR transaction amount will result an error

    When Loan Pay-off is made on "25 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3696
  Scenario: Verify accrual activity of overpaid loan in case of reversed repayment made before MIR and CBR for progressive multidisbursal loan - UC5
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 21 March 2025     | 242.46         | 29.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "242.46" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "142.46" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 142.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 132.13          | 10.33         | 3.56     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 2  | 30   | 21 May 2025       |               | 121.54          | 10.59         | 3.3      | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 3  | 31   | 21 June 2025      |               | 110.69          | 10.85         | 3.04     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 4  | 30   | 21 July 2025      |               |  99.57          | 11.12         | 2.77     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 5  | 31   | 21 August 2025    |               |  88.17          | 11.4          | 2.49     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 6  | 31   | 21 September 2025 |               |  76.48          | 11.69         | 2.2      | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 7  | 30   | 21 October 2025   |               |  64.5           | 11.98         | 1.91     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 8  | 31   | 21 November 2025  |               |  52.22          | 12.28         | 1.61     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 9  | 30   | 21 December 2025  |               |  39.64          | 12.58         | 1.31     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 10 | 31   | 21 January 2026   |               |  26.74          | 12.9          | 0.99     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 11 | 31   | 21 February 2026  |               |  13.52          | 13.22         | 0.67     | 0.0  | 0.0       | 13.89  |  0.0  | 0.0        | 0.0  | 13.89       |
      | 12 | 28   | 21 March 2026     |               |   0.0           | 13.52         | 0.34     | 0.0  | 0.0       | 13.86  |  0.0  | 0.0        | 0.0  | 13.86       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 142.46        | 24.19    | 0.0  | 0.0       | 166.65 | 0.0    | 0.0        | 0.0  | 166.65      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 142.46 | 0.0       | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 100 EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 March 2025" with 142.46 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 142.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 March 2025 | 128.57          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 114.68          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 100.79          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 |  86.9           | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 |  73.01          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 |  59.12          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  45.23          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  31.34          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  17.45          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |   3.56          | 13.89         | 0.0      | 0.0  | 0.0       | 13.89  | 13.89 | 13.89      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  3.56         | 0.0      | 0.0  | 0.0       |  3.56  |  3.56 |  3.56      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 142.46        | 0.0      | 0.0  | 0.0       | 142.46 | 142.46 | 142.46     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 142.46 | 0.0       | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 42.46        | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 42.46     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 March 2025" with 100 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin sets the business date to "02 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 March 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 142.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 130.59          | 111.87        | 1.93     | 0.0  | 0.0       | 113.8  | 11.87 | 11.87      | 0.0  | 101.93      |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 118.72          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 106.85          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 |  94.98          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 |  83.11          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 |  71.24          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 |  59.37          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  47.5           | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  35.63          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |  23.76          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |  11.89          | 11.87         | 0.0      | 0.0  | 0.0       | 11.87  | 11.87 | 11.87      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           | 11.89         | 0.0      | 0.0  | 0.0       | 11.89  | 11.89 | 11.89      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 1.93     | 0.0  | 0.0       | 244.39 | 142.46 | 142.46     | 0.0  | 101.93      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 142.46 | 0.0       | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 42.46        | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    And Admin successfully disburse the loan on "21 April 2025" with "100" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "21 April 2025" with 100 EUR transaction amount
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "21 April 2025" with 150 EUR transaction amount and system-generated Idempotency key
    Then Loan has 48.07 overpaid amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 142.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 130.59          | 111.87        | 1.93     | 0.0  | 0.0       | 113.8  | 113.8 | 11.87      | 0.0  | 0.0         |
      |    |      | 21 April 2025     |               | 100.0           |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 2  | 30   | 21 May 2025       | 21 April 2025 | 208.32          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 April 2025 | 186.05          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 April 2025 | 163.78          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 April 2025 | 141.51          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 April 2025 | 119.24          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 April 2025 |  96.97          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 April 2025 |  74.7           | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 April 2025 |  52.43          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 April 2025 |  30.16          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 April 2025 |  11.89          | 18.27         | 0.0      | 0.0  | 0.0       | 18.27  | 18.27 | 18.27      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 April 2025 |   0.0           | 11.89         | 0.0      | 0.0  | 0.0       | 11.89  | 11.89 | 11.89      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 1.93     | 0.0  | 0.0       | 344.39 | 344.39 | 242.46     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 142.46 | 0.0       | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 42.46        | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 21 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 21 April 2025    | Payout Refund          | 150.0  | 100.0     | 1.93     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 April 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 April 2025" with 48.07 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
# - CBR on closed loan is forbidden - #
    Then Credit Balance Refund transaction on active loan "28 April 2025" with 100 EUR transaction amount will result an error
    When Admin sets the business date to "06 May 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 April 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 142.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 130.59          | 111.87        | 1.93     | 0.0  | 0.0       | 113.8  | 113.8 | 11.87      | 0.0  | 0.0         |
      |    |      | 21 April 2025     |               | 100.0           |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 2  | 30   | 21 May 2025       |               | 210.54          | 68.12         | 2.22     | 0.0  | 0.0       | 70.34  | 22.27 | 22.27      | 0.0  | 48.07       |
      | 3  | 31   | 21 June 2025      | 21 April 2025 | 188.27          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 April 2025 | 166.0           | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 April 2025 | 143.73          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 22.27 | 22.27      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 |               | 121.46          | 22.27         | 0.0      | 0.0  | 0.0       | 22.27  | 18.34 | 18.34      | 0.0  | 3.93        |
      | 7  | 30   | 21 October 2025   |               | 105.85          | 15.61         | 6.66     | 0.0  | 0.0       | 22.27  | 11.87 | 11.87      | 0.0  | 10.4        |
      | 8  | 31   | 21 November 2025  |               |  84.74          | 21.11         | 1.16     | 0.0  | 0.0       | 22.27  | 11.87 | 11.87      | 0.0  | 10.4        |
      | 9  | 30   | 21 December 2025  |               |  63.4           | 21.34         | 0.93     | 0.0  | 0.0       | 22.27  | 11.87 | 11.87      | 0.0  | 10.4        |
      | 10 | 31   | 21 January 2026   |               |  41.82          | 21.58         | 0.69     | 0.0  | 0.0       | 22.27  | 11.87 | 11.87      | 0.0  | 10.4        |
      | 11 | 31   | 21 February 2026  |               |  20.0           | 21.82         | 0.45     | 0.0  | 0.0       | 22.27  | 11.87 | 11.87      | 0.0  | 10.4        |
      | 12 | 28   | 21 March 2026     |               |   0.0           | 20.0          | 0.2      | 0.0  | 0.0       | 20.2   | 11.89 | 11.89      | 0.0  | 8.31        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 390.53        | 14.24    | 0.0  | 0.0       | 404.77 | 292.46 | 190.53     | 0.0  | 112.31      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 142.46 | 0.0       | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 42.46        | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 21 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Payout Refund          | 150.0  | 148.07    | 1.93     | 0.0  | 0.0       | 51.93        | false    | true     |
      | 28 April 2025    | Credit Balance Refund  | 48.07  | 48.07     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
    When Admin sets the business date to "01 May 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Merchant Issued Refund" transaction made on "21 March 2025"
    Then Loan has 285.65 outstanding amount
 # - CBR on active loan is forbidden - #
    Then Credit Balance Refund transaction on active loan "01 May 2025" with 100 EUR transaction amount will result an error
    And Customer makes "AUTOPAY" repayment on "01 May 2025" with 330 EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 142.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 134.07          | 108.39        | 5.5      | 0.0  | 0.0       | 113.89 | 113.89| 0.0        | 0.0  | 0.0         |
      |    |      | 21 April 2025     |               | 100.0           |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 2  | 30   | 21 May 2025       | 01 May 2025   | 213.58          | 68.56         | 1.77     | 0.0  | 0.0       | 70.33  | 70.33 | 70.33      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 01 May 2025   | 191.32          | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 01 May 2025   | 169.06          | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 01 May 2025   | 146.8           | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 01 May 2025   | 124.54          | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 01 May 2025   | 102.28          | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 01 May 2025   |  80.02          | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 01 May 2025   |  57.76          | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 01 May 2025   |  35.5           | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 01 May 2025   |  13.24          | 22.26         | 0.0      | 0.0  | 0.0       | 22.26  | 22.26 | 22.26      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 01 May 2025   |   0.0           | 13.24         | 0.0      | 0.0  | 0.0       | 13.24  | 13.24 | 13.24      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 390.53        | 7.27     | 0.0  | 0.0       | 397.8  | 397.8  | 283.91     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 142.46 | 0.0       | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 42.46        | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | true     | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 242.46       | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 342.46       | false    | false    |
      | 21 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Payout Refund          | 150.0  | 144.5     | 5.5      | 0.0  | 0.0       | 197.96       | false    | true     |
      | 22 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Credit Balance Refund  | 48.07  | 48.07     | 0.0      | 0.0  | 0.0       | 246.03       | false    | true     |
      | 28 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Repayment              | 330.0  | 246.03    | 1.77     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Accrual                | 4.87   | 0.0       | 4.87     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "03 May 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "01 May 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 142.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     | 21 April 2025 | 134.07          | 108.39        | 5.5      | 0.0  | 0.0       | 113.89 | 113.89| 0.0        | 0.0  | 0.0         |
      |    |      | 21 April 2025     |               | 100.0           |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 2  | 30   | 21 May 2025       |               | 217.68          | 64.46         | 5.87     | 0.0  | 0.0       | 70.33  | 22.26 | 22.26      | 0.0  | 48.07       |
      | 3  | 31   | 21 June 2025      |               | 200.51          | 17.17         | 5.09     | 0.0  | 0.0       | 22.26  | 13.85 | 13.85      | 0.0  | 8.41        |
      | 4  | 30   | 21 July 2025      |               | 183.26          | 17.25         | 5.01     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 5  | 31   | 21 August 2025    |               | 165.58          | 17.68         | 4.58     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 6  | 31   | 21 September 2025 |               | 147.46          | 18.12         | 4.14     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 7  | 30   | 21 October 2025   |               | 128.89          | 18.57         | 3.69     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 8  | 31   | 21 November 2025  |               | 109.85          | 19.04         | 3.22     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 9  | 30   | 21 December 2025  |               |  90.34          | 19.51         | 2.75     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 10 | 31   | 21 January 2026   |               |  70.34          | 20.0          | 2.26     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 11 | 31   | 21 February 2026  |               |  49.84          | 20.5          | 1.76     | 0.0  | 0.0       | 22.26  | 0.0   | 0.0        | 0.0  | 22.26       |
      | 12 | 28   | 21 March 2026     |               |   0.0           | 49.84         | 1.25     | 0.0  | 0.0       | 51.09  | 0.0   | 0.0        | 0.0  | 51.09       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 390.53        | 45.12    | 0.0  | 0.0       | 435.65 | 150.0  | 36.11      | 0.0  | 285.65       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 142.46 | 0.0       | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 42.46        | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 142.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | true     | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 242.46       | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 342.46       | false    | false    |
      | 21 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | true     | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Payout Refund          | 150.0  | 144.5     | 5.5      | 0.0  | 0.0       | 197.96       | false    | true     |
      | 22 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Credit Balance Refund  | 48.07  | 48.07     | 0.0      | 0.0  | 0.0       | 246.03       | false    | true     |
      | 28 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Repayment              | 330.0  | 246.03    | 1.77     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 01 May 2025      | Accrual                | 4.87   | 0.0       | 4.87     | 0.0  | 0.0       | 0.0          | false    | false    |
# - CBR on closed loan is forbidden - #
    Then Credit Balance Refund transaction on active loan "03 May 2025" with 100 EUR transaction amount will result an error

    When Loan Pay-off is made on "03 May 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3697
  Scenario: Verify accrual activity of overpaid loan in case of reversed MIR made before MIR and CBR for progressive loan - UC6
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 21 March 2025     | 242.46         | 29.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "242.46" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "242.46" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 224.88          | 17.58         | 6.06     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 2  | 30   | 21 May 2025       |               | 206.86          | 18.02         | 5.62     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 3  | 31   | 21 June 2025      |               | 188.39          | 18.47         | 5.17     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 4  | 30   | 21 July 2025      |               | 169.46          | 18.93         | 4.71     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 5  | 31   | 21 August 2025    |               | 150.06          | 19.4          | 4.24     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 6  | 31   | 21 September 2025 |               | 130.17          | 19.89         | 3.75     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 7  | 30   | 21 October 2025   |               | 109.78          | 20.39         | 3.25     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 8  | 31   | 21 November 2025  |               |  88.88          | 20.9          | 2.74     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 9  | 30   | 21 December 2025  |               |  67.46          | 21.42         | 2.22     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 10 | 31   | 21 January 2026   |               |  45.51          | 21.95         | 1.69     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 11 | 31   | 21 February 2026  |               |  23.01          | 22.5          | 1.14     | 0.0  | 0.0       | 23.64  |  0.0  | 0.0        | 0.0  | 23.64       |
      | 12 | 28   | 21 March 2026     |               |   0.0           | 23.01         | 0.58     | 0.0  | 0.0       | 23.59  |  0.0  | 0.0        | 0.0  | 23.59       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 41.17    | 0.0  | 0.0       | 283.63 | 0.0    | 0.0        | 0.0  | 283.63     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 100 EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 March 2025" with 242.46 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 March 2025" with 100 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin sets the business date to "02 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 March 2025"
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 222.26          | 120.2         | 1.93     | 0.0  | 0.0       | 122.13 | 20.2  |  20.2      | 0.0  | 101.93      |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 202.06          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 181.86          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 161.66          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 141.46          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 121.26          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 | 101.06          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  80.86          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  60.66          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |  40.46          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |  20.26          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  20.26        | 0.0      | 0.0  | 0.0       |  20.26 | 20.26 |  20.26     | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 1.93     | 0.0  | 0.0       | 344.39 | 242.46 | 242.46     | 0.0  | 101.93       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Credit Balance Refund transaction on active loan "21 April 2025" with 100 EUR transaction amount will result an error
    When Admin sets the business date to "29 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "29 April 2025" with 100 EUR transaction amount
    Then Loan has 2.6 outstanding amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 222.26          | 120.2         | 2.6      | 0.0  | 0.0       | 122.8  |120.2  |  20.2      | 100.0| 2.6         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 202.06          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 181.86          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 161.66          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 141.46          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 121.26          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 | 101.06          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  80.86          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  60.66          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |  40.46          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |  20.26          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0  | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  20.26        | 0.0      | 0.0  | 0.0       |  20.26 | 20.26 |  20.26     | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      | 342.46        | 2.6      | 0.0  | 0.0       | 345.06 | 342.46 | 242.46     | 100.0 | 2.6         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
# - CBR on active loan is forbidden - #
    Then Credit Balance Refund transaction on active loan "29 April 2025" with 100 EUR transaction amount will result an error
    When Admin sets the business date to "06 May 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "06 May 2025" with 2.6 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    Then Loan has 0 outstanding amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late   | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |        |             |
      | 1  | 31   | 21 April 2025     | 06 May 2025   | 222.26          | 120.2         | 2.6      | 0.0  | 0.0       | 122.8  |122.8  |  20.2      | 102.6  | 0.0         |
      | 2  | 30   | 21 May 2025       | 21 March 2025 | 202.06          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 3  | 31   | 21 June 2025      | 21 March 2025 | 181.86          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 4  | 30   | 21 July 2025      | 21 March 2025 | 161.66          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 5  | 31   | 21 August 2025    | 21 March 2025 | 141.46          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 6  | 31   | 21 September 2025 | 21 March 2025 | 121.26          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 7  | 30   | 21 October 2025   | 21 March 2025 | 101.06          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 8  | 31   | 21 November 2025  | 21 March 2025 |  80.86          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 9  | 30   | 21 December 2025  | 21 March 2025 |  60.66          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 10 | 31   | 21 January 2026   | 21 March 2025 |  40.46          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 11 | 31   | 21 February 2026  | 21 March 2025 |  20.26          |  20.2         | 0.0      | 0.0  | 0.0       |  20.2  | 20.2  |  20.2      | 0.0    | 0.0         |
      | 12 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  20.26        | 0.0      | 0.0  | 0.0       |  20.26 | 20.26 |  20.26     | 0.0    | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 342.46        | 2.6      | 0.0  | 0.0       | 345.06 | 345.06 | 242.46     | 102.6  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 May 2025      | Repayment              | 2.6    | 0.0       | 2.6      | 0.0  | 0.0       | 0.0          | false    | false    |
# - CBR on closed loan is forbidden - #
    Then Credit Balance Refund transaction on active loan "06 May 2025" with 100 EUR transaction amount will result an error

  @TestRailId:C3698
  Scenario: Verify that interest activities are added in case of reversed repayment made before MIR and CBR for loan with accrual activity - UC7
    When Admin sets the business date to "07 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_ACCRUAL_ACTIVITY | 07 April 2025     | 72.3           | 29.99                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "07 April 2025" with "72.3" amount and expected disbursement date on "07 April 2025"
    And Admin successfully disburse the loan on "07 April 2025" with "72.3" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 07 May 2025       |               |  67.06          | 5.24          | 1.81     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 2  | 31   | 07 June 2025      |               |  61.69          | 5.37          | 1.68     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 3  | 30   | 07 July 2025      |               |  56.18          | 5.51          | 1.54     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 4  | 31   | 07 August 2025    |               |  50.53          | 5.65          | 1.4      | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 5  | 31   | 07 September 2025 |               |  44.74          | 5.79          | 1.26     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 6  | 30   | 07 October 2025   |               |  38.81          | 5.93          | 1.12     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 7  | 31   | 07 November 2025  |               |  32.73          | 6.08          | 0.97     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 8  | 30   | 07 December 2025  |               |  26.5           | 6.23          | 0.82     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 9  | 31   | 07 January 2026   |               |  20.11          | 6.39          | 0.66     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 10 | 31   | 07 February 2026  |               |  13.56          | 6.55          | 0.5      | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 11 | 28   | 07 March 2026     |               |  6.85           | 6.71          | 0.34     | 0.0  | 0.0       | 7.05  | 0.0  | 0.0        | 0.0  | 7.05        |
      | 12 | 31   | 07 April 2026     |               |  0.0            | 6.85          | 0.17     | 0.0  | 0.0       | 7.02  | 0.0  | 0.0        | 0.0  | 7.02        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 72.3          | 12.27    | 0.0  | 0.0       | 84.57  | 0.0    | 0.0        | 0.0  | 84.57       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
    When Admin sets the business date to "08 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "08 April 2025" with 72.35 EUR transaction amount
    When Admin sets the business date to "11 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "11 April 2025" with 72.3 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 72.35 overpaid amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 30   | 07 May 2025       | 08 April 2025 |  65.31          | 6.99          | 0.06     | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 2  | 31   | 07 June 2025      | 08 April 2025 |  58.26          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 3  | 30   | 07 July 2025      | 08 April 2025 |  51.21          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 4  | 31   | 07 August 2025    | 08 April 2025 |  44.16          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 5  | 31   | 07 September 2025 | 08 April 2025 |  37.11          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 6  | 30   | 07 October 2025   | 08 April 2025 |  30.06          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 7  | 31   | 07 November 2025  | 08 April 2025 |  23.01          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 8  | 30   | 07 December 2025  | 08 April 2025 |  15.96          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 9  | 31   | 07 January 2026   | 08 April 2025 |   8.91          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 10 | 31   | 07 February 2026  | 08 April 2025 |   1.86          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 11 | 28   | 07 March 2026     | 11 April 2025 |   0.0           | 1.86          | 0.0      | 0.0  | 0.0       | 1.86   |  1.86 | 1.86       | 0.0  | 0.0         |
      | 12 | 31   | 07 April 2026     | 08 April 2025 |   0.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    |  0.0  | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 72.3          | 0.06     | 0.0  | 0.0       | 72.36  | 72.36  | 72.36      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
      | 08 April 2025    | Repayment              | 72.35  | 72.29     | 0.06     | 0.0  | 0.0       | 0.01         | false    | false    |
      | 08 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Merchant Issued Refund | 72.3   | 0.01      | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Interest Refund        | 0.06   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual Activity       | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "15 April 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "15 April 2025" with 72.35 EUR transaction amount
    When Admin sets the business date to "18 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "08 April 2025"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 30   | 07 May 2025       |               |  65.49          | 79.16         | 1.57     | 0.0  | 0.0       | 80.73  |  7.05 | 7.05       | 0.0  | 73.68       |
      | 2  | 31   | 07 June 2025      | 11 April 2025 |  58.44          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 3  | 30   | 07 July 2025      | 11 April 2025 |  51.39          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 4  | 31   | 07 August 2025    | 11 April 2025 |  44.34          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 5  | 31   | 07 September 2025 | 11 April 2025 |  37.29          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 6  | 30   | 07 October 2025   | 11 April 2025 |  30.24          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 7  | 31   | 07 November 2025  | 11 April 2025 |  23.19          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 8  | 30   | 07 December 2025  | 11 April 2025 |  16.14          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 9  | 31   | 07 January 2026   | 11 April 2025 |   9.09          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 10 | 31   | 07 February 2026  | 11 April 2025 |   2.04          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 11 | 28   | 07 March 2026     | 11 April 2025 |   0.0           | 2.04          | 0.0      | 0.0  | 0.0       | 2.04   |  2.04 | 2.04       | 0.0  | 0.0         |
      | 12 | 31   | 07 April 2026     | 11 April 2025 |   0.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    |  0.0  | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 144.65        | 1.57     | 0.0  | 0.0       | 146.22 | 72.54  | 72.54      | 0.0  | 73.68       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
      | 08 April 2025    | Repayment              | 72.35  | 72.29     | 0.06     | 0.0  | 0.0       | 0.01         | true     | false    |
      | 08 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Merchant Issued Refund | 72.3   | 72.06     | 0.24     | 0.0  | 0.0       | 0.24         | false    | true     |
      | 11 April 2025    | Interest Refund        | 0.24   | 0.24      | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 15 April 2025    | Credit Balance Refund  | 72.35  | 72.35     | 0.0      | 0.0  | 0.0       | 72.35        | false    | true     |
    When Admin sets the business date to "07 May 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "07 May 2025" with 73.68 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin sets the business date to "08 May 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 30   | 07 May 2025       | 07 May 2025   |  65.49          | 79.16         | 1.57     | 0.0  | 0.0       | 80.73  | 80.73 | 7.05       | 0.0  | 0.0         |
      | 2  | 31   | 07 June 2025      | 11 April 2025 |  58.44          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 3  | 30   | 07 July 2025      | 11 April 2025 |  51.39          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 4  | 31   | 07 August 2025    | 11 April 2025 |  44.34          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 5  | 31   | 07 September 2025 | 11 April 2025 |  37.29          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 6  | 30   | 07 October 2025   | 11 April 2025 |  30.24          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 7  | 31   | 07 November 2025  | 11 April 2025 |  23.19          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 8  | 30   | 07 December 2025  | 11 April 2025 |  16.14          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 9  | 31   | 07 January 2026   | 11 April 2025 |   9.09          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 10 | 31   | 07 February 2026  | 11 April 2025 |   2.04          | 7.05          | 0.0      | 0.0  | 0.0       | 7.05   |  7.05 | 7.05       | 0.0  | 0.0         |
      | 11 | 28   | 07 March 2026     | 11 April 2025 |   0.0           | 2.04          | 0.0      | 0.0  | 0.0       | 2.04   |  2.04 | 2.04       | 0.0  | 0.0         |
      | 12 | 31   | 07 April 2026     | 11 April 2025 |   0.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    |  0.0  | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 144.65        | 1.57     | 0.0  | 0.0       | 146.22 | 146.22  | 72.54     | 0.0  | 0.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
      | 08 April 2025    | Repayment              | 72.35  | 72.29     | 0.06     | 0.0  | 0.0       | 0.01         | true     | false    |
      | 08 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Merchant Issued Refund | 72.3   | 72.06     | 0.24     | 0.0  | 0.0       | 0.24         | false    | true     |
      | 11 April 2025    | Interest Refund        | 0.24   | 0.24      | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 11 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Credit Balance Refund  | 72.35  | 72.35     | 0.0      | 0.0  | 0.0       | 72.35        | false    | true     |
      | 16 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Accrual                | 0.07   | 0.0       | 0.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 May 2025      | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 May 2025      | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 May 2025      | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 May 2025      | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 May 2025      | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Repayment              | 73.68  | 72.35     | 1.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Accrual Activity       | 1.57   | 0.0       | 1.57     | 0.0  | 0.0       | 0.0          | false    | false    |
# - CBR on closed loan is forbidden - #
    Then Credit Balance Refund transaction on active loan "07 May 2025" with 72.35 EUR transaction amount will result an error

  @TestRailId:C3699
  Scenario: Verify that interest activities are added in case of reversed repayment made before MIR and CBR for progressive loan with auto downpayment and accrual activity - UC8
    When Admin sets the business date to "07 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY | 07 April 2025     | 72.3           | 29.99                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "07 April 2025" with "72.3" amount and expected disbursement date on "07 April 2025"
    And Admin successfully disburse the loan on "07 April 2025" with "72.3" EUR transaction amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  |  0   | 07 April 2025     | 07 April 2025 |  54.22          | 18.08         | 0.0      | 0.0  | 0.0       | 18.08 | 18.08| 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 07 May 2025       |               |  50.29          | 3.93          | 1.36     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 3  | 31   | 07 June 2025      |               |  46.26          | 4.03          | 1.26     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 4  | 30   | 07 July 2025      |               |  42.13          | 4.13          | 1.16     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 5  | 31   | 07 August 2025    |               |  37.89          | 4.24          | 1.05     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 6  | 31   | 07 September 2025 |               |  33.55          | 4.34          | 0.95     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 7  | 30   | 07 October 2025   |               |  29.1           | 4.45          | 0.84     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 8  | 31   | 07 November 2025  |               |  24.54          | 4.56          | 0.73     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 9  | 30   | 07 December 2025  |               |  19.86          | 4.68          | 0.61     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 10 | 31   | 07 January 2026   |               |  15.07          | 4.79          | 0.5      | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 11 | 31   | 07 February 2026  |               |  10.16          | 4.91          | 0.38     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 12 | 28   | 07 March 2026     |               |   5.12          | 5.04          | 0.25     | 0.0  | 0.0       | 5.29  | 0.0  | 0.0        | 0.0  | 5.29        |
      | 13 | 31   | 07 April 2026     |               |   0.0           | 5.12          | 0.13     | 0.0  | 0.0       | 5.25  | 0.0  | 0.0        | 0.0  | 5.25        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 72.3          | 9.22     | 0.0  | 0.0       | 81.52  | 18.08  | 0.0        | 0.0  | 63.44       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
      | 07 April 2025    | Down Payment           | 18.08  | 18.08     | 0.0      | 0.0  | 0.0       | 54.22        | false    | false    |
    When Admin sets the business date to "08 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "08 April 2025" with 54.27 EUR transaction amount
    When Admin sets the business date to "11 April 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "11 April 2025" with 72.3 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 72.35 overpaid amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  |  0   | 07 April 2025     | 07 April 2025 |  54.22          | 18.08         | 0.0      | 0.0  | 0.0       | 18.08 | 18.08| 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 07 May 2025       | 08 April 2025 |  48.98          | 5.24          | 0.05     | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 3  | 31   | 07 June 2025      | 08 April 2025 |  43.69          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 4  | 30   | 07 July 2025      | 08 April 2025 |  38.4           | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 5  | 31   | 07 August 2025    | 08 April 2025 |  33.11          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 6  | 31   | 07 September 2025 | 08 April 2025 |  27.82          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 7  | 30   | 07 October 2025   | 08 April 2025 |  22.53          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 8  | 31   | 07 November 2025  | 08 April 2025 |  17.24          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 9  | 30   | 07 December 2025  | 08 April 2025 |  11.95          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 10 | 31   | 07 January 2026   | 08 April 2025 |   6.66          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 11 | 31   | 07 February 2026  | 08 April 2025 |   1.37          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29 | 5.29       | 0.0  | 0.0         |
      | 12 | 28   | 07 March 2026     | 08 April 2025 |   0.0           | 1.37          | 0.0      | 0.0  | 0.0       | 1.37  | 1.37 | 1.37       | 0.0  | 0.0         |
      | 13 | 31   | 07 April 2026     | 08 April 2025 |   0.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 72.3          | 0.05     | 0.0  | 0.0       | 72.35  | 72.35  | 54.27      | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
      | 07 April 2025    | Down Payment           | 18.08  | 18.08     | 0.0      | 0.0  | 0.0       | 54.22        | false    | false    |
      | 08 April 2025    | Repayment              | 54.27  | 54.22     | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual Activity       | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Merchant Issued Refund | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Interest Refund        | 0.05   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "15 April 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "15 April 2025" with 72.35 EUR transaction amount
    When Admin sets the business date to "18 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "08 April 2025"
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  |  0   | 07 April 2025     | 07 April 2025 |  54.22          | 18.08         | 0.0      | 0.0  | 0.0       | 18.08 | 18.08 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 07 May 2025       |               |  49.11          | 77.46         | 1.18     | 0.0  | 0.0       | 78.64 | 23.37 | 23.37      | 0.0  | 55.27       |
      | 3  | 31   | 07 June 2025      | 11 April 2025 |  43.82          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 4  | 30   | 07 July 2025      | 11 April 2025 |  38.53          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 5  | 31   | 07 August 2025    | 11 April 2025 |  33.24          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 6  | 31   | 07 September 2025 | 11 April 2025 |  27.95          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 7  | 30   | 07 October 2025   | 11 April 2025 |  22.66          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 8  | 31   | 07 November 2025  | 11 April 2025 |  17.37          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 9  | 30   | 07 December 2025  | 11 April 2025 |  12.08          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 10 | 31   | 07 January 2026   | 11 April 2025 |   6.79          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 11 | 31   | 07 February 2026  | 11 April 2025 |   1.5           | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 12 | 28   | 07 March 2026     | 11 April 2025 |   0.0           | 1.5           | 0.0      | 0.0  | 0.0       | 1.5   | 1.5   | 1.5        | 0.0  | 0.0         |
      | 13 | 31   | 07 April 2026     | 11 April 2025 |   0.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 144.65        | 1.18     | 0.0  | 0.0       | 145.83 | 90.56  | 72.48      | 0.0  | 55.27       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
      | 07 April 2025    | Down Payment           | 18.08  | 18.08     | 0.0      | 0.0  | 0.0       | 54.22        | false    | false    |
      | 08 April 2025    | Repayment              | 54.27  | 54.22     | 0.05     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 08 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Merchant Issued Refund | 72.3   | 54.22     | 0.18     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 11 April 2025    | Interest Refund        | 0.18   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 15 April 2025    | Credit Balance Refund  | 72.35  | 54.27     | 0.0      | 0.0  | 0.0       | 54.27        | false    | true     |
    When Admin sets the business date to "07 May 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "07 May 2025" with 55.27 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 07 April 2025     |               |  72.3           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  |  0   | 07 April 2025     | 07 April 2025 |  54.22          | 18.08         | 0.0      | 0.0  | 0.0       | 18.08 | 18.08 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 07 May 2025       | 07 May 2025   |  49.11          | 77.46         | 1.18     | 0.0  | 0.0       | 78.64 | 78.64 | 23.37      | 0.0  | 0.0        |
      | 3  | 31   | 07 June 2025      | 11 April 2025 |  43.82          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 4  | 30   | 07 July 2025      | 11 April 2025 |  38.53          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 5  | 31   | 07 August 2025    | 11 April 2025 |  33.24          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 6  | 31   | 07 September 2025 | 11 April 2025 |  27.95          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 7  | 30   | 07 October 2025   | 11 April 2025 |  22.66          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 8  | 31   | 07 November 2025  | 11 April 2025 |  17.37          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 9  | 30   | 07 December 2025  | 11 April 2025 |  12.08          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 10 | 31   | 07 January 2026   | 11 April 2025 |   6.79          | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 11 | 31   | 07 February 2026  | 11 April 2025 |   1.5           | 5.29          | 0.0      | 0.0  | 0.0       | 5.29  | 5.29  | 5.29       | 0.0  | 0.0         |
      | 12 | 28   | 07 March 2026     | 11 April 2025 |   0.0           | 1.5           | 0.0      | 0.0  | 0.0       | 1.5   | 1.5   | 1.5        | 0.0  | 0.0         |
      | 13 | 31   | 07 April 2026     | 11 April 2025 |   0.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 144.65        | 1.18     | 0.0  | 0.0       | 145.83 | 145.83 | 72.48      | 0.0  | 0.0        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 07 April 2025    | Disbursement           | 72.3   | 0.0       | 0.0      | 0.0  | 0.0       | 72.3         | false    | false    |
      | 07 April 2025    | Down Payment           | 18.08  | 18.08     | 0.0      | 0.0  | 0.0       | 54.22        | false    | false    |
      | 08 April 2025    | Repayment              | 54.27  | 54.22     | 0.05     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 08 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Merchant Issued Refund | 72.3   | 54.22     | 0.18     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 11 April 2025    | Interest Refund        | 0.18   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 11 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Credit Balance Refund  | 72.35  | 54.27     | 0.0      | 0.0  | 0.0       | 54.27        | false    | true     |
      | 16 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 May 2025      | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 May 2025      | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 May 2025      | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 May 2025      | Accrual                | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 May 2025      | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Repayment              | 55.27  | 54.27     | 1.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Accrual                | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Accrual Activity       | 1.18   | 0.0       | 1.18     | 0.0  | 0.0       | 0.0          | false    | false    |
# - CBR on closed loan is forbidden - #
    Then Credit Balance Refund transaction on active loan "07 May 2025" with 72.35 EUR transaction amount will result an error

  @TestRailId:C3736
  Scenario: Verify that interest is calculated after last unpaid period in case of reversed repayment made before MIR and CBR for progressive loan with downpayment
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_DOWNPAYMENT | 21 March 2025     | 242.46         | 29.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "242.46" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "242.46" EUR transaction amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     |               | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  |  0.0  | 0.0        | 0.0  | 60.62       |
      | 2  | 31   | 21 April 2025     |               | 168.65          | 13.19         | 4.54     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 3  | 30   | 21 May 2025       |               | 155.13          | 13.52         | 4.21     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 4  | 31   | 21 June 2025      |               | 141.28          | 13.85         | 3.88     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 5  | 30   | 21 July 2025      |               | 127.08          | 14.2          | 3.53     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 6  | 31   | 21 August 2025    |               | 112.53          | 14.55         | 3.18     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 7  | 31   | 21 September 2025 |               |  97.61          | 14.92         | 2.81     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 8  | 30   | 21 October 2025   |               |  82.32          | 15.29         | 2.44     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 9  | 31   | 21 November 2025  |               |  66.65          | 15.67         | 2.06     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 10 | 30   | 21 December 2025  |               |  50.59          | 16.06         | 1.67     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 11 | 31   | 21 January 2026   |               |  34.12          | 16.47         | 1.26     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 12 | 31   | 21 February 2026  |               |  17.24          | 16.88         | 0.85     | 0.0  | 0.0       | 17.73  |  0.0  | 0.0        | 0.0  | 17.73       |
      | 13 | 28   | 21 March 2026     |               |   0.0           | 17.24         | 0.43     | 0.0  | 0.0       | 17.67  |  0.0  | 0.0        | 0.0  | 17.67       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 30.86    | 0.0  | 0.0       | 273.32 | 0.0    | 0.0        | 0.0  | 273.32      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
    And Customer makes "AUTOPAY" repayment on "21 March 2025" with 100 EUR transaction amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "21 March 2025" with 242.46 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 100 overpaid amount
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     | 21 March 2025 | 164.11          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 146.38          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 128.65          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 110.92          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 |  93.19          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  75.46          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  57.73          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  40.0           | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  22.27          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |   4.54          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  4.54         | 0.0      | 0.0  | 0.0       |  4.54  |  4.54 |  4.54      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 181.84     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "28 March 2025"
    And Admin runs inline COB job for Loan
    When Admin makes Credit Balance Refund transaction on "28 March 2025" with 100 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     | 21 March 2025 | 164.11          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 146.38          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 128.65          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 110.92          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 |  93.19          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  75.46          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  57.73          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  40.0           | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  22.27          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |   4.54          | 17.73         | 0.0      | 0.0  | 0.0       | 17.73  | 17.73 | 17.73      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |   0.0           |  4.54         | 0.0      | 0.0  | 0.0       |  4.54  |  4.54 |  4.54      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           |  0.0          | 0.0      | 0.0  | 0.0       |  0.0   |  0.0  |  0.0       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 242.46        | 0.0      | 0.0  | 0.0       | 242.46 | 242.46 | 181.84     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | false    | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 142.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "02 April 2025"
    And Admin runs inline COB job for Loan
    When Customer undo "1"th "Repayment" transaction made on "21 March 2025"
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     |               | 166.69          | 115.15        | 1.93     | 0.0  | 0.0       | 117.08 | 15.15 | 15.15      | 0.0  | 101.93      |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 151.54          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 136.39          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 121.24          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 | 106.09          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 | 90.94           | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 | 75.79           | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 | 60.64           | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 | 45.49           | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 | 30.34           | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 | 15.19           | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 | 0.0             | 15.19         | 0.0      | 0.0  | 0.0       | 15.19  | 15.19 | 15.19      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 1.93     | 0.0  | 0.0       | 344.39 | 242.46 | 181.84     | 0.0  | 101.93      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
    When Admin sets the business date to "21 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     |               | 166.69          | 115.15        | 1.93     | 0.0  | 0.0       | 117.08 | 15.15 | 15.15      | 0.0  | 101.93      |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 151.54          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 136.39          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 121.24          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 | 106.09          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  90.94          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  75.79          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  60.64          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  45.49          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |  30.34          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |  15.19          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           | 15.19         | 0.0      | 0.0  | 0.0       | 15.19  | 15.19 | 15.19      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 1.93     | 0.0  | 0.0       | 344.39 | 242.46 | 181.84     | 0.0  | 101.93      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "22 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     |               | 166.69          | 115.15        | 2.01     | 0.0  | 0.0       | 117.16 | 15.15 | 15.15      | 0.0  | 102.01      |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 151.54          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 136.39          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 121.24          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 | 106.09          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  90.94          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  75.79          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  60.64          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  45.49          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |  30.34          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |  15.19          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           | 15.19         | 0.0      | 0.0  | 0.0       | 15.19  | 15.19 | 15.19      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 2.01     | 0.0  | 0.0       | 344.47 | 242.46 | 181.84     | 0.0  | 102.01      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "23 April 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 13 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 242.46          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  |  0   | 21 March 2025     | 21 March 2025 | 181.84          | 60.62         | 0.0      | 0.0  | 0.0       | 60.62  | 60.62 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 21 April 2025     |               | 166.69          | 115.15        | 2.1      | 0.0  | 0.0       | 117.25 | 15.15 | 15.15      | 0.0  | 102.1       |
      | 3  | 30   | 21 May 2025       | 21 March 2025 | 151.54          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 4  | 31   | 21 June 2025      | 21 March 2025 | 136.39          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 5  | 30   | 21 July 2025      | 21 March 2025 | 121.24          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 6  | 31   | 21 August 2025    | 21 March 2025 | 106.09          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 7  | 31   | 21 September 2025 | 21 March 2025 |  90.94          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 8  | 30   | 21 October 2025   | 21 March 2025 |  75.79          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 9  | 31   | 21 November 2025  | 21 March 2025 |  60.64          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 10 | 30   | 21 December 2025  | 21 March 2025 |  45.49          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 11 | 31   | 21 January 2026   | 21 March 2025 |  30.34          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 12 | 31   | 21 February 2026  | 21 March 2025 |  15.19          | 15.15         | 0.0      | 0.0  | 0.0       | 15.15  | 15.15 | 15.15      | 0.0  | 0.0         |
      | 13 | 28   | 21 March 2026     | 21 March 2025 |   0.0           | 15.19         | 0.0      | 0.0  | 0.0       | 15.19  | 15.19 | 15.19      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 342.46        | 2.1      | 0.0  | 0.0       | 344.56 | 242.46 | 181.84     | 0.0  | 102.1       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Customer makes "AUTOPAY" repayment on "23 April 2025" with 102.1 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 242.46 | 0.0       | 0.0      | 0.0  | 0.0       | 242.46       | false    | false    |
      | 21 March 2025    | Repayment              | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 142.46       | true     | false    |
      | 21 March 2025    | Merchant Issued Refund | 242.46 | 242.46    | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 28 March 2025    | Credit Balance Refund  | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 100.0        | false    | true     |
      | 20 April 2025    | Accrual                | 1.85   | 0.0       | 1.85     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.08   | 0.0       | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Repayment              | 102.1  | 100.0     | 2.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3773
  Scenario: Verify that interest is calculated after last unpaid period in case of MIR partially covering later periods
    When Admin sets the business date to "21 March 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_IR_DAILY_TILL_PRECLOSE_LAST_INSTALLMENT_STRATEGY | 21 March 2025     | 186.38         | 35.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 March 2025" with "186.38" amount and expected disbursement date on "21 March 2025"
    And Admin successfully disburse the loan on "21 March 2025" with "186.38" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 186.38          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 126.08          | 60.3          | 5.59     | 0.0  | 0.0       | 65.89  |  0.0  | 0.0        | 0.0  | 65.89       |
      | 2  | 30   | 21 May 2025       |               | 63.97           | 62.11         | 3.78     | 0.0  | 0.0       | 65.89  |  0.0  | 0.0        | 0.0  | 65.89       |
      | 3  | 31   | 21 June 2025      |               | 0.0             | 63.97         | 1.92     | 0.0  | 0.0       | 65.89  |  0.0  | 0.0        | 0.0  | 65.89       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 186.38        | 11.29    | 0.0  | 0.0       | 197.67 | 0.0    | 0.0        | 0.0  | 197.67      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 186.38 | 0.0       | 0.0      | 0.0  | 0.0       | 186.38       | false    | false    |
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "17 April 2025"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "17 April 2025" with 87.33 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 21 March 2025     |               | 186.38          |               |          | 0.0  |           | 0.0    |  0.0  |            |      |             |
      | 1  | 31   | 21 April 2025     |               | 125.74          | 60.64         | 5.25     | 0.0  | 0.0       | 65.89  |  0.0  | 0.0        | 0.0  | 65.89       |
      | 2  | 30   | 21 May 2025       |               | 65.89           | 59.85         | 1.15     | 0.0  | 0.0       | 61.0   |  21.44| 21.44      | 0.0  | 39.56       |
      | 3  | 31   | 21 June 2025      | 17 April 2025 | 0.0             | 65.89         | 0.0      | 0.0  | 0.0       | 65.89  |  65.89| 65.89      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 186.38        | 6.4      | 0.0  | 0.0       | 192.78 | 87.33  | 87.33      | 0.0  | 105.45      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 186.38 | 0.0       | 0.0      | 0.0  | 0.0       | 186.38       | false    | false    |
      | 17 April 2025    | Merchant Issued Refund | 87.33  | 87.33     | 0.0      | 0.0  | 0.0       | 99.05        | false    | false    |
    When Admin sets the business date to "21 May 2025"
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 186.38 | 0.0       | 0.0      | 0.0  | 0.0       | 186.38       | false    | false    |
      | 22 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual                | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Merchant Issued Refund | 87.33  | 87.33     | 0.0      | 0.0  | 0.0       | 99.05        | false    | false    |
      | 17 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 May 2025      | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan has 8.22 total unpaid payable due interest
    When Admin sets the business date to "26 May 2025"
    And Admin runs inline COB job for Loan
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 March 2025    | Disbursement           | 186.38 | 0.0       | 0.0      | 0.0  | 0.0       | 186.38       | false    | false    |
      | 22 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 March 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 April 2025    | Accrual                | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 April 2025    | Merchant Issued Refund | 87.33  | 87.33     | 0.0      | 0.0  | 0.0       | 99.05        | false    | false    |
      | 17 April 2025    | Accrual                | 0.18   | 0.0       | 0.18     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 April 2025    | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 April 2025    | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 May 2025      | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 May 2025      | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 May 2025      | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 May 2025      | Accrual                | 0.1    | 0.0       | 0.1      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 May 2025      | Accrual                | 0.09   | 0.0       | 0.09     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan has 8.7 total unpaid payable due interest
    And Customer makes "AUTOPAY" repayment on "26 May 2025" with 107.75 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3802
  Scenario: Correct Accrual Activity event publishing for backdated loans when the loan re-opens after reversing a goodwill credit transaction  - UC1
    Given Admin sets the business date to "05 May 2023"
    And Admin creates a client with random data
    When Admin sets the business date to "24 June 2025"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY | 05 May 2023       | 359.79         | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "05 May 2023" with "359.79" amount and expected disbursement date on "05 May 2023"
    And Admin successfully disburse the loan on "05 May 2023" with "359.79" EUR transaction amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "19 May 2023" with 270.85 EUR transaction amount and system-generated Idempotency key
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "19 May 2023"
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Customer undo "1"th transaction made on "19 May 2023"
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date    | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 May 2023         | Disbursement       | 359.79 | 0.0       | 0.0      | 0.0  | 0.0       | 359.79       | false    | false    |
      | 05 May 2023         | Down Payment       | 89.95  | 89.95     | 0.0      | 0.0  | 0.0       | 269.84       | false    | false    |
      | 19 May 2023         | Goodwill Credit    | 270.85 | 269.84    | 1.01     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 05 June 2023        | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 05 July 2023        | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2023      | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2023   | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2023     | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual            | 1.01   | 0.0       | 1.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 July 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 August 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 September 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 October 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 November 2023"
    And "Accrual Activity" transaction on "05 June 2023" got reverse-replayed on "24 June 2025"

    When Loan Pay-off is made on "24 June 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3803
  Scenario: Correct Accrual Activity event publishing for backdated loans when the overpaid loan re-opens after reversing a goodwill credit transaction - UC2
    Given Admin sets the business date to "05 May 2023"
    And Admin creates a client with random data
    When Admin sets the business date to "24 June 2025"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY | 05 May 2023       | 359.79         | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "05 May 2023" with "359.79" amount and expected disbursement date on "05 May 2023"
    And Admin successfully disburse the loan on "05 May 2023" with "359.79" EUR transaction amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "19 May 2023" with 359.79 EUR transaction amount and system-generated Idempotency key
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "19 May 2023"
    Then Loan status will be "OVERPAID"
    When Customer undo "1"th transaction made on "19 May 2023"
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date    | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 May 2023         | Disbursement       | 359.79 | 0.0       | 0.0      | 0.0  | 0.0       | 359.79       | false    | false    |
      | 05 May 2023         | Down Payment       | 89.95  | 89.95     | 0.0      | 0.0  | 0.0       | 269.84       | false    | false    |
      | 19 May 2023         | Goodwill Credit    | 359.79 | 269.84    | 1.01     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 05 June 2023        | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 05 July 2023        | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2023      | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2023   | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2023     | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual            | 1.01   | 0.0       | 1.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 July 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 August 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 September 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 October 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 November 2023"
    And "Accrual Activity" transaction on "05 June 2023" got reverse-replayed on "24 June 2025"

    When Loan Pay-off is made on "24 June 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3805
  Scenario: Correct Accrual Activity event publishing for backdated loans when the loan re-opens after reversing a payout refund transaction - UC3
    Given Admin sets the business date to "05 May 2023"
    And Admin creates a client with random data
    When Admin sets the business date to "24 June 2025"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_360_30_INTEREST_RECALC_AUTO_DOWNPAYMENT_ZERO_INTEREST_CHARGE_OFF_ACCRUAL_ACTIVITY | 05 May 2023       | 359.79         | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "05 May 2023" with "359.79" amount and expected disbursement date on "05 May 2023"
    And Admin successfully disburse the loan on "05 May 2023" with "359.79" EUR transaction amount
    And Admin makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "19 May 2023" with 270.85 EUR transaction amount
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "19 May 2023"
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Customer undo "1"th transaction made on "19 May 2023"
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date    | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 May 2023         | Disbursement     | 359.79 | 0.0       | 0.0      | 0.0  | 0.0       | 359.79       | false    | false    |
      | 05 May 2023         | Down Payment     | 89.95  | 89.95     | 0.0      | 0.0  | 0.0       | 269.84       | false    | false    |
      | 19 May 2023         | Payout Refund    | 270.85 | 269.84    | 1.01     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 05 June 2023        | Accrual Activity | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 05 July 2023        | Accrual Activity | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2023      | Accrual Activity | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2023   | Accrual Activity | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2023     | Accrual Activity | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual          | 1.01   | 0.0       | 1.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual Activity | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 July 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 August 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 September 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 October 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 November 2023"
    And "Accrual Activity" transaction on "05 June 2023" got reverse-replayed on "24 June 2025"

    When Loan Pay-off is made on "24 June 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3806
  Scenario: Correct Accrual Activity event publishing for backdated loans when the loan re-opens after reversing a merchant issue refund transaction  - UC4
    Given Admin sets the business date to "05 May 2023"
    And Admin creates a client with random data
    When Admin sets the business date to "24 June 2025"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY | 05 May 2023       | 359.79         | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "05 May 2023" with "359.79" amount and expected disbursement date on "05 May 2023"
    And Admin successfully disburse the loan on "05 May 2023" with "359.79" EUR transaction amount
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "19 May 2023" with 359.79 EUR transaction amount
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "19 May 2023"
    Then Loan status will be "OVERPAID"
    When Customer undo "1"th transaction made on "19 May 2023"
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date    | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 May 2023         | Disbursement           | 359.79 | 0.0       | 0.0      | 0.0  | 0.0       | 359.79       | false    | false    |
      | 05 May 2023         | Down Payment           | 89.95  | 89.95     | 0.0      | 0.0  | 0.0       | 269.84       | false    | false    |
      | 19 May 2023         | Merchant Issued Refund | 359.79 | 269.84    | 1.01     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 19 May 2023         | Interest Refund        | 1.01   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | true     | false    |
      | 05 June 2023        | Accrual Activity       | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 05 July 2023        | Accrual Activity       | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2023      | Accrual Activity       | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2023   | Accrual Activity       | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2023     | Accrual Activity       | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual                | 1.01   | 0.0       | 1.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual Activity       | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 July 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 August 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 September 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 October 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 November 2023"
    And "Accrual Activity" transaction on "05 June 2023" got reverse-replayed on "24 June 2025"

    When Loan Pay-off is made on "24 June 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3807
  Scenario: Correct Accrual Activity event publishing for backdated loans when the loan re-opens after reversing a interest payment waiver transaction - UC5
    Given Admin sets the business date to "05 May 2023"
    And Admin creates a client with random data
    When Admin sets the business date to "24 June 2025"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY | 05 May 2023       | 359.79         | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "05 May 2023" with "359.79" amount and expected disbursement date on "05 May 2023"
    And Admin successfully disburse the loan on "05 May 2023" with "359.79" EUR transaction amount
    When Admin makes "INTEREST_PAYMENT_WAIVER" transaction with "AUTOPAY" payment type on "19 May 2023" with 270.85 EUR transaction amount
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "19 May 2023"
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Customer undo "1"th transaction made on "19 May 2023"
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date    | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 May 2023         | Disbursement            | 359.79 | 0.0       | 0.0      | 0.0  | 0.0       | 359.79       | false    | false    |
      | 05 May 2023         | Down Payment            | 89.95  | 89.95     | 0.0      | 0.0  | 0.0       | 269.84       | false    | false    |
      | 19 May 2023         | Interest Payment Waiver | 270.85 | 269.84    | 1.01     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 05 June 2023        | Accrual Activity        | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 05 July 2023        | Accrual Activity        | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2023      | Accrual Activity        | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2023   | Accrual Activity        | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2023     | Accrual Activity        | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual                 | 1.01   | 0.0       | 1.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual Activity        | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 July 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 August 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 September 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 October 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 November 2023"
    And "Accrual Activity" transaction on "05 June 2023" got reverse-replayed on "24 June 2025"

    When Loan Pay-off is made on "24 June 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3808
  Scenario: Correct Accrual Activity event publishing for backdated loans when the loan re-opens after reversing a repayment transaction  - UC6
    Given Admin sets the business date to "05 May 2023"
    And Admin creates a client with random data
    When Admin sets the business date to "24 June 2025"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY | 05 May 2023       | 359.79         | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "05 May 2023" with "359.79" amount and expected disbursement date on "05 May 2023"
    And Admin successfully disburse the loan on "05 May 2023" with "359.79" EUR transaction amount
    When Customer makes "AUTOPAY" repayment on "19 May 2023" with 359.79 EUR transaction amount
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "19 May 2023"
    Then Loan status will be "OVERPAID"
    When Customer undo "1"th transaction made on "19 May 2023"
    Then Loan status will be "ACTIVE"
    And Loan Transactions tab has the following data:
      | Transaction date    | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 05 May 2023         | Disbursement       | 359.79 | 0.0       | 0.0      | 0.0  | 0.0       | 359.79       | false    | false    |
      | 05 May 2023         | Down Payment       | 89.95  | 89.95     | 0.0      | 0.0  | 0.0       | 269.84       | false    | false    |
      | 19 May 2023         | Repayment          | 359.79 | 269.84    | 1.01     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 05 June 2023        | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 05 July 2023        | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2023      | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2023   | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2023     | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual            | 1.01   | 0.0       | 1.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 November 2023    | Accrual Activity   | 2.25   | 0.0       | 2.25     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 July 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 August 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 September 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 October 2023"
    Then LoanTransactionAccrualActivityPostBusinessEvent is raised on "05 November 2023"
    And "Accrual Activity" transaction on "05 June 2023" got reverse-replayed on "24 June 2025"

    When Loan Pay-off is made on "24 June 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4052
  Scenario: Verify that no extra accrual activity will be created upon loan reprocessing with merchant issued refund and NSF penalty
    When Admin sets the business date to "13 June 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_ACCRUAL_ACTIVITY | 13 June 2025      | 135.94         | 11.32                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                | MONTHS                  | 1             | MONTHS                   | 6                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "13 June 2025" with "135.94" amount and expected disbursement date on "13 June 2025"
    And Admin successfully disburse the loan on "13 June 2025" with "135.94" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |           | 135.94          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 13 July 2025      |           | 113.81          | 22.13         | 1.28     | 0.0  | 0.0       | 23.41 | 0.0  | 0.0        | 0.0  | 23.41       |
      | 2  | 31   | 13 August 2025    |           | 91.47           | 22.34         | 1.07     | 0.0  | 0.0       | 23.41 | 0.0  | 0.0        | 0.0  | 23.41       |
      | 3  | 31   | 13 September 2025 |           | 68.92           | 22.55         | 0.86     | 0.0  | 0.0       | 23.41 | 0.0  | 0.0        | 0.0  | 23.41       |
      | 4  | 30   | 13 October 2025   |           | 46.16           | 22.76         | 0.65     | 0.0  | 0.0       | 23.41 | 0.0  | 0.0        | 0.0  | 23.41       |
      | 5  | 31   | 13 November 2025  |           | 23.19           | 22.97         | 0.44     | 0.0  | 0.0       | 23.41 | 0.0  | 0.0        | 0.0  | 23.41       |
      | 6  | 30   | 13 December 2025  |           | 0.0             | 23.19         | 0.22     | 0.0  | 0.0       | 23.41 | 0.0  | 0.0        | 0.0  | 23.41       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 135.94        | 4.52     | 0.0  | 0.0       | 140.46 | 0.0  | 0.0        | 0.0  | 140.46      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
#    --- First repayment on 22 June 2025 ---
    When Admin sets the business date to "22 June 2025"
    And Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "22 June 2025" with 25.00 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    |              | 91.29           | 21.62         | 1.79     | 0.0  | 0.0       | 23.41 | 1.59  | 1.59       | 0.0  | 21.82       |
      | 3  | 31   | 13 September 2025 |              | 68.74           | 22.55         | 0.86     | 0.0  | 0.0       | 23.41 | 0.0   | 0.0        | 0.0  | 23.41       |
      | 4  | 30   | 13 October 2025   |              | 45.98           | 22.76         | 0.65     | 0.0  | 0.0       | 23.41 | 0.0   | 0.0        | 0.0  | 23.41       |
      | 5  | 31   | 13 November 2025  |              | 23.0            | 22.98         | 0.43     | 0.0  | 0.0       | 23.41 | 0.0   | 0.0        | 0.0  | 23.41       |
      | 6  | 30   | 13 December 2025  |              | 0.0             | 23.0          | 0.22     | 0.0  | 0.0       | 23.22 | 0.0   | 0.0        | 0.0  | 23.22       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 135.94        | 4.33     | 0.0  | 0.0       | 140.27 | 25.0 | 25.0       | 0.0  | 115.27      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment        | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
#    --- Second repayment on 13 July 2025 ---
    When Admin sets the business date to "13 July 2025"
    And Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "13 July 2025" with 23.41 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    | 13 July 2025 | 90.24           | 22.67         | 0.74     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 3  | 31   | 13 September 2025 |              | 68.51           | 21.73         | 1.68     | 0.0  | 0.0       | 23.41 | 1.59  | 1.59       | 0.0  | 21.82       |
      | 4  | 30   | 13 October 2025   |              | 45.75           | 22.76         | 0.65     | 0.0  | 0.0       | 23.41 | 0.0   | 0.0        | 0.0  | 23.41       |
      | 5  | 31   | 13 November 2025  |              | 22.77           | 22.98         | 0.43     | 0.0  | 0.0       | 23.41 | 0.0   | 0.0        | 0.0  | 23.41       |
      | 6  | 30   | 13 December 2025  |              | 0.0             |22.77          | 0.21     | 0.0  | 0.0       | 22.98 | 0.0   | 0.0        | 0.0  | 22.98       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 135.94        | 4.09     | 0.0  | 0.0       | 140.03 | 48.41 | 48.41      | 0.0  | 91.62       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment        | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
      | 13 July 2025     | Repayment        | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | false    | false    |
#    --- Merchant issued refund ---
    When Admin sets the business date to "16 July 2025"
    And Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "16 July 2025" with 135.94 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    | 13 July 2025 | 90.24           | 22.67         | 0.74     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 3  | 31   | 13 September 2025 | 16 July 2025 | 66.91           | 23.33         | 0.08     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 4  | 30   | 13 October 2025   | 16 July 2025 | 43.5            | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 5  | 31   | 13 November 2025  | 16 July 2025 | 20.09           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 6  | 30   | 13 December 2025  | 16 July 2025 | 0.0             | 20.09         | 0.0      | 0.0  | 0.0       | 20.09 | 20.09 | 20.09      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 1.2      | 0.0  | 0.0       | 137.14 | 137.14 | 137.14     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement           | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment              | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
      | 13 July 2025     | Repayment              | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | false    | false    |
      | 13 July 2025     | Accrual Activity       | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Merchant Issued Refund | 135.94 | 88.65     | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Interest Refund        | 1.2    | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Accrual                | 1.2    | 0.0       | 1.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Accrual Activity       | 0.82   | 0.0       | 0.82     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan status will be "OVERPAID"
    And Loan has 48.41 overpaid amount
#    --- Undo repayment made on 13 July 2025 on 18 July 2025 ---
    When Admin sets the business date to "18 July 2025"
    And Customer undo "1"th "Repayment" transaction made on "13 July 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    | 16 July 2025 | 90.34           | 22.57         | 0.84     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 3  | 31   | 13 September 2025 | 16 July 2025 | 66.93           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 4  | 30   | 13 October 2025   | 16 July 2025 | 43.52           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 5  | 31   | 13 November 2025  | 16 July 2025 | 20.11           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 6  | 30   | 13 December 2025  | 16 July 2025 | 0.0             | 20.11         | 0.0      | 0.0  | 0.0       | 20.11 | 20.11 | 20.11      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 1.22     | 0.0  | 0.0       | 137.16 | 137.16 | 137.16     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement           | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment              | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
      | 13 July 2025     | Repayment              | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | true     | false    |
      | 13 July 2025     | Accrual Activity       | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Accrual                | 1.2    | 0.0       | 1.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Merchant Issued Refund | 135.94 | 111.32    | 0.84     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Interest Refund        | 1.22   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Accrual Activity       | 0.84   | 0.0       | 0.84     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 18 July 2025     | Accrual                | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan status will be "OVERPAID"
    And Loan has 25 overpaid amount
#    --- Add NSF penalty on 18 July 2025 ---
    When Admin adds "LOAN_NSF_FEE" due date charge with "18 July 2025" due date and 2.8 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    | 16 July 2025 | 90.34           | 22.57         | 0.84     | 0.0  | 2.8       | 26.21 | 26.21 | 26.21      | 0.0  | 0.0         |
      | 3  | 31   | 13 September 2025 | 16 July 2025 | 66.93           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 4  | 30   | 13 October 2025   | 16 July 2025 | 43.52           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 5  | 31   | 13 November 2025  | 16 July 2025 | 20.11           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 6  | 30   | 13 December 2025  | 16 July 2025 | 0.0             | 20.11         | 0.0      | 0.0  | 0.0       | 20.11 | 20.11 | 20.11      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 1.22     | 0.0  | 2.8       | 139.96 | 139.96 | 139.96     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement           | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment              | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
      | 13 July 2025     | Repayment              | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | true     | false    |
      | 13 July 2025     | Accrual Activity       | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Accrual                | 1.2    | 0.0       | 1.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Merchant Issued Refund | 135.94 | 111.32    | 0.84     | 0.0  | 2.8       | 0.0          | false    | true     |
      | 16 July 2025     | Interest Refund        | 1.22   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Accrual Activity       | 3.64   | 0.0       | 0.84     | 0.0  | 2.8       | 0.0          | false    | true     |
      | 18 July 2025     | Accrual                | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 July 2025     | Accrual                | 2.8    | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |
    And Loan status will be "OVERPAID"
    And Loan has 22.2 overpaid amount
#    --- Reprocess the loan on 18 July 2025 ---
    When Admin runs loan reprocess for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    | 16 July 2025 | 90.34           | 22.57         | 0.84     | 0.0  | 2.8       | 26.21 | 26.21 | 26.21      | 0.0  | 0.0         |
      | 3  | 31   | 13 September 2025 | 16 July 2025 | 66.93           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 4  | 30   | 13 October 2025   | 16 July 2025 | 43.52           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 5  | 31   | 13 November 2025  | 16 July 2025 | 20.11           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 6  | 30   | 13 December 2025  | 16 July 2025 | 0.0             | 20.11         | 0.0      | 0.0  | 0.0       | 20.11 | 20.11 | 20.11      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 1.22     | 0.0  | 2.8       | 139.96 | 139.96 | 139.96     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement           | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment              | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
      | 13 July 2025     | Repayment              | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | true     | false    |
      | 13 July 2025     | Accrual Activity       | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Accrual                | 1.2    | 0.0       | 1.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Merchant Issued Refund | 135.94 | 111.32    | 0.84     | 0.0  | 2.8       | 0.0          | false    | true     |
      | 16 July 2025     | Interest Refund        | 1.22   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Accrual Activity       | 3.64   | 0.0       | 0.84     | 0.0  | 2.8       | 0.0          | false    | true     |
      | 18 July 2025     | Accrual                | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 July 2025     | Accrual                | 2.8    | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |
    And Loan status will be "OVERPAID"
    And Loan has 22.2 overpaid amount
# --- add one more repayment - 13 July 2025 ---#
    And Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "14 July 2025" with 23.41 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    | 16 July 2025 | 90.33           | 22.58         | 0.83     | 0.0  | 2.8       | 26.21 | 26.21 | 26.21      | 0.0  | 0.0         |
      | 3  | 31   | 13 September 2025 | 16 July 2025 | 66.92           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 4  | 30   | 13 October 2025   | 16 July 2025 | 43.51           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 5  | 31   | 13 November 2025  | 16 July 2025 | 20.1            | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 6  | 30   | 13 December 2025  | 16 July 2025 | 0.0             | 20.1          | 0.0      | 0.0  | 0.0       | 20.1  | 20.1  | 20.1       | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 1.21     | 0.0  | 2.8       | 139.95 | 139.95 | 139.95     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement           | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment              | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
      | 13 July 2025     | Repayment              | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | true     | false    |
      | 13 July 2025     | Accrual Activity       | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 July 2025     | Repayment              | 23.41  | 20.61     | 0.0      | 0.0  | 2.8       | 90.71        | false    | false    |
      | 16 July 2025     | Accrual                | 1.2    | 0.0       | 1.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Merchant Issued Refund | 135.94 | 90.71     | 0.83     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Interest Refund        | 1.21   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Accrual Activity       | 3.63   | 0.0       | 0.83     | 0.0  | 2.8       | 0.0          | false    | true     |
      | 18 July 2025     | Accrual                | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 July 2025     | Accrual                | 2.8    | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |
      | 18 July 2025     | Accrual Adjustment     | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- undo repayment --- #
    And Customer undo "1"th "Repayment" transaction made on "14 July 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    | 16 July 2025 | 90.34           | 22.57         | 0.84     | 0.0  | 2.8       | 26.21 | 26.21 | 26.21      | 0.0  | 0.0         |
      | 3  | 31   | 13 September 2025 | 16 July 2025 | 66.93           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 4  | 30   | 13 October 2025   | 16 July 2025 | 43.52           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 5  | 31   | 13 November 2025  | 16 July 2025 | 20.11           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 6  | 30   | 13 December 2025  | 16 July 2025 | 0.0             | 20.11         | 0.0      | 0.0  | 0.0       | 20.11 | 20.11 | 20.11      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 1.22     | 0.0  | 2.8       | 139.96 | 139.96 | 139.96     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement           | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment              | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
      | 13 July 2025     | Repayment              | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | true     | false    |
      | 13 July 2025     | Accrual Activity       | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 July 2025     | Repayment              | 23.41  | 20.61     | 0.0      | 0.0  | 2.8       | 90.71        | true     | false    |
      | 16 July 2025     | Accrual                | 1.2    | 0.0       | 1.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Merchant Issued Refund | 135.94 | 111.32    | 0.84     | 0.0  | 2.8       | 0.0          | false    | true     |
      | 16 July 2025     | Interest Refund        | 1.22   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Accrual Activity       | 3.64   | 0.0       | 0.84     | 0.0  | 2.8       | 0.0          | false    | true     |
      | 18 July 2025     | Accrual                | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 July 2025     | Accrual                | 2.8    | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |
      | 18 July 2025     | Accrual Adjustment     | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 July 2025     | Accrual                | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |

    When Admin makes Credit Balance Refund transaction on "18 July 2025" with 22.2 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4054
  Scenario: Verify that no extra accrual activity will be created upon loan reprocessing with merchant issued refund and SNOOZE fee
    When Admin sets the business date to "13 June 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_ACCRUAL_ACTIVITY | 13 June 2025      | 135.94         | 11.32                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                | MONTHS                  | 1             | MONTHS                   | 6                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "13 June 2025" with "135.94" amount and expected disbursement date on "13 June 2025"
    And Admin successfully disburse the loan on "13 June 2025" with "135.94" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |           | 135.94          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 13 July 2025      |           | 113.81          | 22.13         | 1.28     | 0.0  | 0.0       | 23.41 | 0.0  | 0.0        | 0.0  | 23.41       |
      | 2  | 31   | 13 August 2025    |           | 91.47           | 22.34         | 1.07     | 0.0  | 0.0       | 23.41 | 0.0  | 0.0        | 0.0  | 23.41       |
      | 3  | 31   | 13 September 2025 |           | 68.92           | 22.55         | 0.86     | 0.0  | 0.0       | 23.41 | 0.0  | 0.0        | 0.0  | 23.41       |
      | 4  | 30   | 13 October 2025   |           | 46.16           | 22.76         | 0.65     | 0.0  | 0.0       | 23.41 | 0.0  | 0.0        | 0.0  | 23.41       |
      | 5  | 31   | 13 November 2025  |           | 23.19           | 22.97         | 0.44     | 0.0  | 0.0       | 23.41 | 0.0  | 0.0        | 0.0  | 23.41       |
      | 6  | 30   | 13 December 2025  |           | 0.0             | 23.19         | 0.22     | 0.0  | 0.0       | 23.41 | 0.0  | 0.0        | 0.0  | 23.41       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 135.94        | 4.52     | 0.0  | 0.0       | 140.46 | 0.0  | 0.0        | 0.0  | 140.46      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
#    --- First repayment on 22 June 2025 ---
    When Admin sets the business date to "22 June 2025"
    And Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "22 June 2025" with 25.00 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    |              | 91.29           | 21.62         | 1.79     | 0.0  | 0.0       | 23.41 | 1.59  | 1.59       | 0.0  | 21.82       |
      | 3  | 31   | 13 September 2025 |              | 68.74           | 22.55         | 0.86     | 0.0  | 0.0       | 23.41 | 0.0   | 0.0        | 0.0  | 23.41       |
      | 4  | 30   | 13 October 2025   |              | 45.98           | 22.76         | 0.65     | 0.0  | 0.0       | 23.41 | 0.0   | 0.0        | 0.0  | 23.41       |
      | 5  | 31   | 13 November 2025  |              | 23.0            | 22.98         | 0.43     | 0.0  | 0.0       | 23.41 | 0.0   | 0.0        | 0.0  | 23.41       |
      | 6  | 30   | 13 December 2025  |              | 0.0             | 23.0          | 0.22     | 0.0  | 0.0       | 23.22 | 0.0   | 0.0        | 0.0  | 23.22       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 135.94        | 4.33     | 0.0  | 0.0       | 140.27 | 25.0 | 25.0       | 0.0  | 115.27      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment        | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
#    --- Second repayment on 13 July 2025 ---
    When Admin sets the business date to "13 July 2025"
    And Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "13 July 2025" with 23.41 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    | 13 July 2025 | 90.24           | 22.67         | 0.74     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 3  | 31   | 13 September 2025 |              | 68.51           | 21.73         | 1.68     | 0.0  | 0.0       | 23.41 | 1.59  | 1.59       | 0.0  | 21.82       |
      | 4  | 30   | 13 October 2025   |              | 45.75           | 22.76         | 0.65     | 0.0  | 0.0       | 23.41 | 0.0   | 0.0        | 0.0  | 23.41       |
      | 5  | 31   | 13 November 2025  |              | 22.77           | 22.98         | 0.43     | 0.0  | 0.0       | 23.41 | 0.0   | 0.0        | 0.0  | 23.41       |
      | 6  | 30   | 13 December 2025  |              | 0.0             |22.77          | 0.21     | 0.0  | 0.0       | 22.98 | 0.0   | 0.0        | 0.0  | 22.98       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 135.94        | 4.09     | 0.0  | 0.0       | 140.03 | 48.41 | 48.41      | 0.0  | 91.62       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment        | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
      | 13 July 2025     | Repayment        | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | false    | false    |
#    --- Merchant issued refund ---
    When Admin sets the business date to "16 July 2025"
    And Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "16 July 2025" with 135.94 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    | 13 July 2025 | 90.24           | 22.67         | 0.74     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 3  | 31   | 13 September 2025 | 16 July 2025 | 66.91           | 23.33         | 0.08     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 4  | 30   | 13 October 2025   | 16 July 2025 | 43.5            | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 5  | 31   | 13 November 2025  | 16 July 2025 | 20.09           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 6  | 30   | 13 December 2025  | 16 July 2025 | 0.0             | 20.09         | 0.0      | 0.0  | 0.0       | 20.09 | 20.09 | 20.09      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 1.2      | 0.0  | 0.0       | 137.14 | 137.14 | 137.14     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement           | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment              | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
      | 13 July 2025     | Repayment              | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | false    | false    |
      | 13 July 2025     | Accrual Activity       | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Merchant Issued Refund | 135.94 | 88.65     | 0.08     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Interest Refund        | 1.2    | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Accrual                | 1.2    | 0.0       | 1.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Accrual Activity       | 0.82   | 0.0       | 0.82     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan status will be "OVERPAID"
    And Loan has 48.41 overpaid amount
#    --- Undo repayment made on 13 July 2025 on 18 July 2025 ---
    When Admin sets the business date to "18 July 2025"
    And Customer undo "1"th "Repayment" transaction made on "13 July 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    | 16 July 2025 | 90.34           | 22.57         | 0.84     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 3  | 31   | 13 September 2025 | 16 July 2025 | 66.93           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 4  | 30   | 13 October 2025   | 16 July 2025 | 43.52           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 5  | 31   | 13 November 2025  | 16 July 2025 | 20.11           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 6  | 30   | 13 December 2025  | 16 July 2025 | 0.0             | 20.11         | 0.0      | 0.0  | 0.0       | 20.11 | 20.11 | 20.11      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 1.22     | 0.0  | 0.0       | 137.16 | 137.16 | 137.16     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement           | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment              | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
      | 13 July 2025     | Repayment              | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | true     | false    |
      | 13 July 2025     | Accrual Activity       | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Accrual                | 1.2    | 0.0       | 1.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Merchant Issued Refund | 135.94 | 111.32    | 0.84     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Interest Refund        | 1.22   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Accrual Activity       | 0.84   | 0.0       | 0.84     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 18 July 2025     | Accrual                | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Loan status will be "OVERPAID"
    And Loan has 25 overpaid amount
#    --- Add SNOOZE fee on 18 July 2025 ---
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "18 July 2025" due date and 2.8 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    | 16 July 2025 | 90.34           | 22.57         | 0.84     | 2.8  | 0.0       | 26.21 | 26.21 | 26.21      | 0.0  | 0.0         |
      | 3  | 31   | 13 September 2025 | 16 July 2025 | 66.93           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 4  | 30   | 13 October 2025   | 16 July 2025 | 43.52           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 5  | 31   | 13 November 2025  | 16 July 2025 | 20.11           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 6  | 30   | 13 December 2025  | 16 July 2025 | 0.0             | 20.11         | 0.0      | 0.0  | 0.0       | 20.11 | 20.11 | 20.11      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 1.22     | 2.8  | 0.0       | 139.96 | 139.96 | 139.96     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement           | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment              | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
      | 13 July 2025     | Repayment              | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | true     | false    |
      | 13 July 2025     | Accrual Activity       | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Accrual                | 1.2    | 0.0       | 1.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Merchant Issued Refund | 135.94 | 111.32    | 0.84     | 2.8  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Interest Refund        | 1.22   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Accrual Activity       | 3.64   | 0.0       | 0.84     | 2.8  | 0.0       | 0.0          | false    | true     |
      | 18 July 2025     | Accrual                | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 July 2025     | Accrual                | 2.8    | 0.0       | 0.0      | 2.8  | 0.0       | 0.0          | false    | false    |
    And Loan status will be "OVERPAID"
    And Loan has 22.2 overpaid amount
#    --- Reprocess the loan on 18 July 2025 ---
    When Admin runs loan reprocess for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    | 16 July 2025 | 90.34           | 22.57         | 0.84     | 2.8  | 0.0       | 26.21 | 26.21 | 26.21      | 0.0  | 0.0         |
      | 3  | 31   | 13 September 2025 | 16 July 2025 | 66.93           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 4  | 30   | 13 October 2025   | 16 July 2025 | 43.52           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 5  | 31   | 13 November 2025  | 16 July 2025 | 20.11           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 6  | 30   | 13 December 2025  | 16 July 2025 | 0.0             | 20.11         | 0.0      | 0.0  | 0.0       | 20.11 | 20.11 | 20.11      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 1.22     | 2.8  | 0.0       | 139.96 | 139.96 | 139.96     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement           | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment              | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
      | 13 July 2025     | Repayment              | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | true     | false    |
      | 13 July 2025     | Accrual Activity       | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Accrual                | 1.2    | 0.0       | 1.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Merchant Issued Refund | 135.94 | 111.32    | 0.84     | 2.8  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Interest Refund        | 1.22   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Accrual Activity       | 3.64   | 0.0       | 0.84     | 2.8  | 0.0       | 0.0          | false    | true     |
      | 18 July 2025     | Accrual                | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 July 2025     | Accrual                | 2.8    | 0.0       | 0.0      | 2.8  | 0.0       | 0.0          | false    | false    |
    And Loan status will be "OVERPAID"
    And Loan has 22.2 overpaid amount
    # --- add one more repayment - 13 July 2025 ---#
    And Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "13 July 2025" with 23.41 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    | 16 July 2025 | 90.24           | 22.67         | 0.74     | 2.8  | 0.0       | 26.21 | 26.21 | 26.21      | 0.0  | 0.0         |
      | 3  | 31   | 13 September 2025 | 16 July 2025 | 66.91           | 23.33         | 0.08     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 4  | 30   | 13 October 2025   | 16 July 2025 | 43.5            | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 5  | 31   | 13 November 2025  | 16 July 2025 | 20.09           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 6  | 30   | 13 December 2025  | 16 July 2025 | 0.0             | 20.09         | 0.0      | 0.0  | 0.0       | 20.09 | 20.09 | 20.09      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 1.2      | 2.8  | 0.0       | 139.94 | 139.94 | 139.94     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement           | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment              | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
      | 13 July 2025     | Repayment              | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | true     | false    |
      | 13 July 2025     | Accrual Activity       | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 July 2025     | Repayment              | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | false    | false    |
      | 16 July 2025     | Accrual                | 1.2    | 0.0       | 1.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Merchant Issued Refund | 135.94 | 88.65     | 0.08     | 2.8  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Interest Refund        | 1.2    | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Accrual Activity       | 3.62   | 0.0       | 0.82     | 2.8  | 0.0       | 0.0          | false    | true     |
      | 18 July 2025     | Accrual                | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 July 2025     | Accrual                | 2.8    | 0.0       | 0.0      | 2.8  | 0.0       | 0.0          | false    | false    |
      | 18 July 2025     | Accrual Adjustment     | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- undo repayment --- #
    And Customer undo "2"th "Repayment" transaction made on "13 July 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 13 June 2025      |              | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 13 July 2025      | 22 June 2025 | 112.91          | 23.03         | 0.38     | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 2  | 31   | 13 August 2025    | 16 July 2025 | 90.34           | 22.57         | 0.84     | 2.8  | 0.0       | 26.21 | 26.21 | 26.21      | 0.0  | 0.0         |
      | 3  | 31   | 13 September 2025 | 16 July 2025 | 66.93           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 4  | 30   | 13 October 2025   | 16 July 2025 | 43.52           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 5  | 31   | 13 November 2025  | 16 July 2025 | 20.11           | 23.41         | 0.0      | 0.0  | 0.0       | 23.41 | 23.41 | 23.41      | 0.0  | 0.0         |
      | 6  | 30   | 13 December 2025  | 16 July 2025 | 0.0             | 20.11         | 0.0      | 0.0  | 0.0       | 20.11 | 20.11 | 20.11      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 1.22     | 2.8  | 0.0       | 139.96 | 139.96 | 139.96     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 13 June 2025     | Disbursement           | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 22 June 2025     | Repayment              | 25.0   | 24.62     | 0.38     | 0.0  | 0.0       | 111.32       | false    | false    |
      | 13 July 2025     | Repayment              | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | true     | false    |
      | 13 July 2025     | Accrual Activity       | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 July 2025     | Repayment              | 23.41  | 22.67     | 0.74     | 0.0  | 0.0       | 88.65        | true     | false    |
      | 16 July 2025     | Accrual                | 1.2    | 0.0       | 1.2      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 July 2025     | Merchant Issued Refund | 135.94 | 111.32    | 0.84     | 2.8  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Interest Refund        | 1.22   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | true     |
      | 16 July 2025     | Accrual Activity       | 3.64   | 0.0       | 0.84     | 2.8  | 0.0       | 0.0          | false    | true     |
      | 18 July 2025     | Accrual                | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 July 2025     | Accrual                | 2.8    | 0.0       | 0.0      | 2.8  | 0.0       | 0.0          | false    | false    |
      | 18 July 2025     | Accrual Adjustment     | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 July 2025     | Accrual                | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin makes Credit Balance Refund transaction on "18 July 2025" with 22.2 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3955
  Scenario: Verify accrual activity trn just reversed but nt replayed with backdated repayment that overpays loan - UC1
    When Admin sets the business date to "01 August 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_ACCRUAL_ACTIVITY | 01 August 2025    | 135.94         | 11.32                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1             | MONTHS                  | 4                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 August 2025" with "135.94" amount and expected disbursement date on "01 August 2025"
    And Admin successfully disburse the loan on "01 August 2025" with "135.94" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 August 2025    |           | 135.94          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 September 2025 |           | 102.43          | 33.51         | 1.28     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 2  | 30   | 01 October 2025   |           | 68.61           | 33.82         | 0.97     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 3  | 31   | 01 November 2025  |           | 34.47           | 34.14         | 0.65     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 4  | 30   | 01 December 2025  |           |  0.0            | 34.47         | 0.33     | 0.0  | 0.0       | 34.8  | 0.0  | 0.0        | 0.0  | 34.8       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 135.94        | 3.23     | 0.0  | 0.0       | 139.17 | 0.0  | 0.0        | 0.0  | 139.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2025   | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
    When Admin sets the business date to "02 August 2025"
    When Admin runs inline COB job for Loan

    When Admin sets the business date to "06 October 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 August 2025    |           | 135.94          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 September 2025 |           | 102.43          | 33.51         | 1.28     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 2  | 30   | 01 October 2025   |           | 68.92           | 33.51         | 1.28     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 3  | 31   | 01 November 2025  |           | 34.88           | 34.04         | 0.75     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 4  | 30   | 01 December 2025  |           |  0.0            | 34.88         | 0.33     | 0.0  | 0.0       | 35.21 | 0.0  | 0.0        | 0.0  | 35.21      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 135.94        | 3.64     | 0.0  | 0.0       | 139.58 | 0.0  | 0.0        | 0.0  | 139.58      |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2025    | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 02 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 September 2025 | Accrual Activity | 1.28   | 0.0       | 1.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 October 2025   | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 October 2025   | Accrual Activity | 1.28   | 0.0       | 1.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 October 2025   | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 October 2025   | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 October 2025   | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2025   | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Store "Accrual Activity" transaction created on "01 September 2025" date as "1"th transaction
    And Store "Accrual Activity" transaction created on "01 October 2025" date as "2"th transaction

#    --- backdated repayment on 01 August 2025 ---
    And Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 August 2025" with 140 EUR transaction amount
    And Loan status will be "OVERPAID"
    And Loan has 4.06 overpaid amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 August 2025    |                | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 September 2025 | 01 August 2025 | 101.15          | 34.79         | 0.0      | 0.0  | 0.0       | 34.79 | 34.79 | 34.79      | 0.0  | 0.0         |
      | 2  | 30   | 01 October 2025   | 01 August 2025 | 66.36           | 34.79         | 0.0      | 0.0  | 0.0       | 34.79 | 34.79 | 34.79      | 0.0  | 0.0         |
      | 3  | 31   | 01 November 2025  | 01 August 2025 | 31.57           | 34.79         | 0.0      | 0.0  | 0.0       | 34.79 | 34.79 | 34.79      | 0.0  | 0.0         |
      | 4  | 30   | 01 December 2025  | 01 August 2025 |  0.0            | 31.57         | 0.0      | 0.0  | 0.0       | 31.57 | 31.57 | 31.57      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 0.0      | 0.0  | 0.0       | 135.94 | 135.94 | 135.94     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2025    | Disbursement       | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 01 August 2025    | Repayment          | 140.0  | 135.94    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 October 2025   | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 October 2025   | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 October 2025   | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 October 2025   | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2025   | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 October 2025   | Accrual Adjustment | 2.73   | 0.0       | 2.73     | 0.0  | 0.0       | 0.0          | false    | false    |

    And LoanAdjustTransactionBusinessEvent is raised with transaction on "01 September 2025" got reversed on "06 October 2025"
    And LoanAdjustTransactionBusinessEvent is raised with transaction on "01 October 2025" got reversed on "06 October 2025"

   And Check required "1"th transaction for non-null eternal-id
   And Check required "2"th transaction for non-null eternal-id
   And In Loan Transactions all transactions have non-null external-id

    When Admin makes Credit Balance Refund transaction on "06 October 2025" with 4.06 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3956
  Scenario: Verify accrual activity trn just reversed but not replayed with backdated repayment that fully pays loan and charge - UC2
    When Admin sets the business date to "01 August 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_ACCRUAL_ACTIVITY | 01 August 2025    | 135.94         | 11.32                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1             | MONTHS                  | 4                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 August 2025" with "135.94" amount and expected disbursement date on "01 August 2025"
    And Admin successfully disburse the loan on "01 August 2025" with "135.94" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 August 2025    |           | 135.94          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 September 2025 |           | 102.43          | 33.51         | 1.28     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 2  | 30   | 01 October 2025   |           | 68.61           | 33.82         | 0.97     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 3  | 31   | 01 November 2025  |           | 34.47           | 34.14         | 0.65     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 4  | 30   | 01 December 2025  |           |  0.0            | 34.47         | 0.33     | 0.0  | 0.0       | 34.8  | 0.0  | 0.0        | 0.0  | 34.8       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 135.94        | 3.23     | 0.0  | 0.0       | 139.17 | 0.0  | 0.0        | 0.0  | 139.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2025   | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
    When Admin sets the business date to "02 August 2025"
    When Admin runs inline COB job for Loan

    When Admin sets the business date to "06 October 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 August 2025    |           | 135.94          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 September 2025 |           | 102.43          | 33.51         | 1.28     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 2  | 30   | 01 October 2025   |           | 68.92           | 33.51         | 1.28     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 3  | 31   | 01 November 2025  |           | 34.88           | 34.04         | 0.75     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 4  | 30   | 01 December 2025  |           |  0.0            | 34.88         | 0.33     | 0.0  | 0.0       | 35.21 | 0.0  | 0.0        | 0.0  | 35.21      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 135.94        | 3.64     | 0.0  | 0.0       | 139.58 | 0.0  | 0.0        | 0.0  | 139.58      |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2025    | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 02 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 September 2025 | Accrual Activity | 1.28   | 0.0       | 1.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 October 2025   | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 October 2025   | Accrual Activity | 1.28   | 0.0       | 1.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 October 2025   | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 October 2025   | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 October 2025   | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2025   | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Store "Accrual Activity" transaction created on "01 September 2025" date as "1"th transaction
    And Store "Accrual Activity" transaction created on "01 October 2025" date as "2"th transaction

    When Admin adds "LOAN_NSF_FEE" due date charge with "06 October 2025" due date and 9.8 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of       | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 06 October 2025 | Flat             | 9.8  | 0.0  | 0.0    | 9.8         |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 August 2025    |           | 135.94          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 September 2025 |           | 102.43          | 33.51         | 1.28     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 2  | 30   | 01 October 2025   |           | 68.92           | 33.51         | 1.28     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 3  | 31   | 01 November 2025  |           | 34.88           | 34.04         | 0.75     | 0.0  | 9.8       | 44.59 | 0.0  | 0.0        | 0.0  | 44.59       |
      | 4  | 30   | 01 December 2025  |           |  0.0            | 34.88         | 0.33     | 0.0  | 0.0       | 35.21 | 0.0  | 0.0        | 0.0  | 35.21      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 135.94        | 3.64     | 0.0  | 9.8       | 149.38 | 0.0  | 0.0        | 0.0  | 149.38      |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2025    | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 02 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 September 2025 | Accrual Activity | 1.28   | 0.0       | 1.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 October 2025   | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 October 2025   | Accrual Activity | 1.28   | 0.0       | 1.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 October 2025   | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 October 2025   | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 October 2025   | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2025   | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |

    When Admin sets the business date to "07 October 2025"
    When Admin runs inline COB job for Loan
#  --- backdated repayment on 01 August 2025 ---
    And Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 August 2025" with 145.74 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 August 2025    |                | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 September 2025 | 01 August 2025 | 101.15          | 34.79         | 0.0      | 0.0  | 0.0       | 34.79 | 34.79 | 34.79      | 0.0  | 0.0         |
      | 2  | 30   | 01 October 2025   | 01 August 2025 | 66.36           | 34.79         | 0.0      | 0.0  | 0.0       | 34.79 | 34.79 | 34.79      | 0.0  | 0.0         |
      | 3  | 31   | 01 November 2025  | 01 August 2025 | 31.57           | 34.79         | 0.0      | 0.0  | 9.8       | 44.59 | 44.59 | 44.59      | 0.0  | 0.0         |
      | 4  | 30   | 01 December 2025  | 01 August 2025 |  0.0            | 31.57         | 0.0      | 0.0  | 0.0       | 31.57 | 31.57 | 31.57      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 0.0      | 0.0  | 9.8       | 145.74 | 145.74 | 145.74     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2025    | Disbursement       | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 01 August 2025    | Repayment          | 145.74 | 135.94    | 0.0      | 0.0  | 9.8       |   0.0        | false    | false    |
      | 01 August 2025    | Accrual Activity   | 9.8    | 0.0       | 0.0      | 0.0  | 9.8       |   0.0        | false    | false    |
      | 02 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 October 2025   | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 October 2025   | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 October 2025   | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 October 2025   | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 October 2025   | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 October 2025   | Accrual            | 9.84   | 0.0       | 0.04     | 0.0  | 9.8       | 0.0          | false    | false    |
      | 07 October 2025   | Accrual Adjustment | 2.77   | 0.0       | 2.77     | 0.0  | 0.0       | 0.0          | false    | false    |

    And LoanAdjustTransactionBusinessEvent is raised with transaction on "01 September 2025" got reversed on "07 October 2025"
    And LoanAdjustTransactionBusinessEvent is raised with transaction on "01 October 2025" got reversed on "07 October 2025"

    And Check required "1"th transaction for non-null eternal-id
    And Check required "2"th transaction for non-null eternal-id
    And In Loan Transactions all transactions have non-null external-id

  @TestRailId:C3957
  Scenario: Verify accrual activity trn just reversed but not replayed with backdated repayment that fully pays loan - UC3
    When Admin sets the business date to "01 August 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_ACCRUAL_ACTIVITY | 01 August 2025    | 135.94         | 11.32                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1             | MONTHS                  | 4                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 August 2025" with "135.94" amount and expected disbursement date on "01 August 2025"
    And Admin successfully disburse the loan on "01 August 2025" with "135.94" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 August 2025    |           | 135.94          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 September 2025 |           | 102.43          | 33.51         | 1.28     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 2  | 30   | 01 October 2025   |           | 68.61           | 33.82         | 0.97     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 3  | 31   | 01 November 2025  |           | 34.47           | 34.14         | 0.65     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 4  | 30   | 01 December 2025  |           |  0.0            | 34.47         | 0.33     | 0.0  | 0.0       | 34.8  | 0.0  | 0.0        | 0.0  | 34.8       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 135.94        | 3.23     | 0.0  | 0.0       | 139.17 | 0.0  | 0.0        | 0.0  | 139.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2025   | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
    When Admin sets the business date to "02 August 2025"
    When Admin runs inline COB job for Loan

    When Admin sets the business date to "06 September 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 August 2025    |           | 135.94          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 September 2025 |           | 102.43          | 33.51         | 1.28     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 2  | 30   | 01 October 2025   |           | 68.66           | 33.77         | 1.02     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 3  | 31   | 01 November 2025  |           | 34.52           | 34.14         | 0.65     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 4  | 30   | 01 December 2025  |           |  0.0            | 34.52         | 0.33     | 0.0  | 0.0       | 34.85 | 0.0  | 0.0        | 0.0  | 34.85      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 135.94        | 3.28     | 0.0  | 0.0       | 139.22 | 0.0  | 0.0        | 0.0  | 139.22      |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2025    | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 02 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 September 2025 | Accrual Activity | 1.28   | 0.0       | 1.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Store "Accrual Activity" transaction created on "01 September 2025" date as "1"th transaction

#    --- backdated repayment on 01 August 2025 ---
    And Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 August 2025" with 135.94 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 August 2025    |                | 135.94          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 September 2025 | 01 August 2025 | 101.15          | 34.79         | 0.0      | 0.0  | 0.0       | 34.79 | 34.79 | 34.79      | 0.0  | 0.0         |
      | 2  | 30   | 01 October 2025   | 01 August 2025 | 66.36           | 34.79         | 0.0      | 0.0  | 0.0       | 34.79 | 34.79 | 34.79      | 0.0  | 0.0         |
      | 3  | 31   | 01 November 2025  | 01 August 2025 | 31.57           | 34.79         | 0.0      | 0.0  | 0.0       | 34.79 | 34.79 | 34.79      | 0.0  | 0.0         |
      | 4  | 30   | 01 December 2025  | 01 August 2025 |  0.0            | 31.57         | 0.0      | 0.0  | 0.0       | 31.57 | 31.57 | 31.57      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 0.0      | 0.0  | 0.0       | 135.94 | 135.94 | 135.94     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2025    | Disbursement       | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 01 August 2025    | Repayment          | 135.94 | 135.94    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 September 2025 | Accrual Adjustment | 1.45   | 0.0       | 1.45     | 0.0  | 0.0       | 0.0          | false    | false    |

    And LoanAdjustTransactionBusinessEvent is raised with transaction got reversed on "06 September 2025"
    And LoanAccrualAdjustmentTransactionBusinessEvent is raised on "06 September 2025"

    And Check required "1"th transaction for non-null eternal-id
    And In Loan Transactions all transactions have non-null external-id

  @TestRailId:C3953
  Scenario: Verify accrual activity trn just reversed but not replayed with backdated repayment that overpays loan and Snooze fee charge - UC4
    When Admin sets the business date to "01 August 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_ACCRUAL_ACTIVITY | 01 August 2025    | 135.94         | 11.32                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1             | MONTHS                  | 4                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 August 2025" with "135.94" amount and expected disbursement date on "01 August 2025"
    And Admin successfully disburse the loan on "01 August 2025" with "135.94" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 August 2025    |           | 135.94          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 September 2025 |           | 102.43          | 33.51         | 1.28     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 2  | 30   | 01 October 2025   |           | 68.61           | 33.82         | 0.97     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 3  | 31   | 01 November 2025  |           | 34.47           | 34.14         | 0.65     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 4  | 30   | 01 December 2025  |           |  0.0            | 34.47         | 0.33     | 0.0  | 0.0       | 34.8  | 0.0  | 0.0        | 0.0  | 34.8       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 135.94        | 3.23     | 0.0  | 0.0       | 139.17 | 0.0  | 0.0        | 0.0  | 139.17      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2025   | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
    When Admin sets the business date to "02 August 2025"
    When Admin runs inline COB job for Loan

    When Admin sets the business date to "06 September 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 August 2025    |           | 135.94          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 September 2025 |           | 102.43          | 33.51         | 1.28     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 2  | 30   | 01 October 2025   |           | 68.66           | 33.77         | 1.02     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 3  | 31   | 01 November 2025  |           | 34.52           | 34.14         | 0.65     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 4  | 30   | 01 December 2025  |           |  0.0            | 34.52         | 0.33     | 0.0  | 0.0       | 34.85 | 0.0  | 0.0        | 0.0  | 34.85      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 135.94        | 3.28     | 0.0  | 0.0       | 139.22 | 0.0  | 0.0        | 0.0  | 139.22      |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2025    | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 02 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 September 2025 | Accrual Activity | 1.28   | 0.0       | 1.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Store "Accrual Activity" transaction created on "01 September 2025" date as "1"th transaction

  # --- NSF fee charge on current date --- #
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "06 September 2025" due date and 10.15 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of         | Calculation type | Due   | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 06 September 2025 | Flat             | 10.15 | 0.0  | 0.0    | 10.15       |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 August 2025    |           | 135.94          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 September 2025 |           | 102.43          | 33.51         | 1.28     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 2  | 30   | 01 October 2025   |           | 68.66           | 33.77         | 1.02     | 10.15| 0.0       | 44.94 | 0.0  | 0.0        | 0.0  | 44.94       |
      | 3  | 31   | 01 November 2025  |           | 34.52           | 34.14         | 0.65     | 0.0  | 0.0       | 34.79 | 0.0  | 0.0        | 0.0  | 34.79       |
      | 4  | 30   | 01 December 2025  |           |  0.0            | 34.52         | 0.33     | 0.0  | 0.0       | 34.85 | 0.0  | 0.0        | 0.0  | 34.85      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 135.94        | 3.28     | 10.15 | 0.0       | 149.37 | 0.0  | 0.0        | 0.0  | 149.37      |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2025    | Disbursement     | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 02 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 August 2025    | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 August 2025    | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 September 2025 | Accrual Activity | 1.28   | 0.0       | 1.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 September 2025 | Accrual          | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2025 | Accrual          | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

#  --- backdated repayment on 01 August 2025 ---
    And Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "01 August 2025" with 150 EUR transaction amount
    And Loan status will be "OVERPAID"
    And Loan has 3.91 overpaid amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 August 2025    |                | 135.94          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 September 2025 | 01 August 2025 | 101.15          | 34.79         | 0.0      | 0.0  | 0.0       | 34.79 | 34.79| 34.79      | 0.0  | 0.0         |
      | 2  | 30   | 01 October 2025   | 01 August 2025 | 66.36           | 34.79         | 0.0      | 10.15| 0.0       | 44.94 | 44.94| 44.94      | 0.0  | 0.0         |
      | 3  | 31   | 01 November 2025  | 01 August 2025 | 31.57           | 34.79         | 0.0      | 0.0  | 0.0       | 34.79 | 34.79| 34.79      | 0.0  | 0.0         |
      | 4  | 30   | 01 December 2025  | 01 August 2025 |  0.0            | 31.57         | 0.0      | 0.0  | 0.0       | 31.57 | 31.57| 31.57      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees  | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 135.94        | 0.0      | 10.15 | 0.0       | 146.09 | 146.09 | 146.09     | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 August 2025    | Disbursement       | 135.94 | 0.0       | 0.0      | 0.0  | 0.0       | 135.94       | false    | false    |
      | 01 August 2025    | Repayment          | 150.0  | 135.94    | 0.0      | 10.15| 0.0       | 0.0          | false    | false    |
      | 01 August 2025    | Accrual Activity   | 10.15  | 0.0       | 0.0      | 10.15| 0.0       | 0.0          | false    | false    |
      | 02 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 August 2025    | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 August 2025    | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |

      | 01 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 September 2025 | Accrual            | 0.05   | 0.0       | 0.05     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 September 2025 | Accrual            | 0.04   | 0.0       | 0.04     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 September 2025 | Accrual            | 10.15  | 0.0       | 0.0      | 10.15| 0.0       | 0.0          | false    | false    |
      | 06 September 2025 | Accrual Adjustment | 1.45   | 0.0       | 1.45     | 0.0  | 0.0       | 0.0          | false    | false    |

    And LoanAdjustTransactionBusinessEvent is raised with transaction got reversed on "06 September 2025"
    And LoanAccrualAdjustmentTransactionBusinessEvent is raised on "06 September 2025"

    And Check required "1"th transaction for non-null eternal-id
    And In Loan Transactions all transactions have non-null external-id

    When Admin makes Credit Balance Refund transaction on "06 September 2025" with 3.91 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C3960
  Scenario: Verify MIR trn is processed after repayment for progressive loan with custom payment allocation rules - UC1
    When Admin sets the business date to "16 August 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY | 16 August 2025    | 516.06         | 19.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 24                | MONTHS                | 1              | MONTHS                 | 24                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "16 August 2025" with "516.06" amount and expected disbursement date on "16 August 2025"
    And Admin successfully disburse the loan on "16 August 2025" with "516.06" EUR transaction amount
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date                 | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 16 August 2025       |                  | 516.06          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 16 September 2025    |                  | 498.4           | 17.66         | 8.6      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 2  | 30   | 16 October 2025      |                  | 480.44          | 17.96         | 8.3      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 3  | 31   | 16 November 2025     |                  | 462.18          | 18.26         | 8.0      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 4  | 30   | 16 December 2025     |                  | 443.62          | 18.56         | 7.7      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 5  | 31   | 16 January 2026      |                  | 424.75          | 18.87         | 7.39     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 6  | 31   | 16 February 2026     |                  | 405.57          | 19.18         | 7.08     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 7  | 28   | 16 March 2026        |                  | 386.07          | 19.5          | 6.76     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 8  | 31   | 16 April 2026        |                  | 366.24          | 19.83         | 6.43     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 9  | 30   | 16 May 2026          |                  | 346.08          | 20.16         | 6.1      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 10 | 31   | 16 June 2026         |                  | 325.59          | 20.49         | 5.77     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 11 | 30   | 16 July 2026         |                  | 304.75          | 20.84         | 5.42     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 12 | 31   | 16 August 2026       |                  | 283.57          | 21.18         | 5.08     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 13 | 31   | 16 September 2026    |                  | 262.03          | 21.54         | 4.72     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 14 | 30   | 16 October 2026      |                  | 240.13          | 21.9          | 4.36     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 15 | 31   | 16 November 2026     |                  | 217.87          | 22.26         | 4.0      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 16 | 30   | 16 December 2026     |                  | 195.24          | 22.63         | 3.63     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 17 | 31   | 16 January 2027      |                  | 172.23          | 23.01         | 3.25     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 18 | 31   | 16 February 2027     |                  | 148.84          | 23.39         | 2.87     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 19 | 28   | 16 March 2027        |                  | 125.06          | 23.78         | 2.48     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 20 | 31   | 16 April 2027        |                  | 100.88          | 24.18         | 2.08     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 21 | 30   | 16 May 2027          |                  |  76.3           | 24.58         | 1.68     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 22 | 31   | 16 June 2027         |                  |  51.31          | 24.99         | 1.27     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 23 | 30   | 16 July 2027         |                  |  25.9           | 25.41         | 0.85     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 24 | 31   | 16 August 2027       |                  |   0.0           | 25.9          | 0.43     | 0.0  | 0.0       | 26.33 | 0.0   | 0.0        | 0.0  | 26.33       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 516.06        | 114.25   | 0.0  | 0.0       | 630.31  |  0.0 | 0.0        | 0.0  | 630.31      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance  | Reverted | Replayed |
      | 16 August 2025   | Disbursement     | 516.06  | 0.0       | 0.0      | 0.0  | 0.0       | 516.06        | false    | false    |

    When Admin sets the business date to "08 September 2025"
    And Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "08 September 2025" with 50 EUR transaction amount
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date                 | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 16 August 2025       |                   | 516.06          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 16 September 2025    | 08 September 2025 | 496.18          | 19.88         | 6.38     | 0.0  | 0.0       | 26.26 | 26.26 | 26.26      | 0.0  |  0.0        |
      | 2  | 30   | 16 October 2025      |                   | 469.92          | 26.26         | 0.0      | 0.0  | 0.0       | 26.26 | 23.74 | 23.74      | 0.0  |  2.52       |
      | 3  | 31   | 16 November 2025     |                   | 461.39          |  8.53         | 17.73    | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 4  | 30   | 16 December 2025     |                   | 442.82          | 18.57         | 7.69     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 5  | 31   | 16 January 2026      |                   | 423.94          | 18.88         | 7.38     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 6  | 31   | 16 February 2026     |                   | 404.74          | 19.2          | 7.06     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 7  | 28   | 16 March 2026        |                   | 385.22          | 19.52         | 6.74     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 8  | 31   | 16 April 2026        |                   | 365.38          | 19.84         | 6.42     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 9  | 30   | 16 May 2026          |                   | 345.21          | 20.17         | 6.09     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 10 | 31   | 16 June 2026         |                   | 324.7           | 20.51         | 5.75     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 11 | 30   | 16 July 2026         |                   | 303.85          | 20.85         | 5.41     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 12 | 31   | 16 August 2026       |                   | 282.65          | 21.2          | 5.06     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 13 | 31   | 16 September 2026    |                   | 261.1           | 21.55         | 4.71     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 14 | 30   | 16 October 2026      |                   | 239.19          | 21.91         | 4.35     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 15 | 31   | 16 November 2026     |                   | 216.91          | 22.28         | 3.98     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 16 | 30   | 16 December 2026     |                   | 194.26          | 22.65         | 3.61     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 17 | 31   | 16 January 2027      |                   | 171.24          | 23.02         | 3.24     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 18 | 31   | 16 February 2027     |                   | 147.83          | 23.41         | 2.85     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 19 | 28   | 16 March 2027        |                   | 124.03          | 23.8          | 2.46     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 20 | 31   | 16 April 2027        |                   |  99.84          | 24.19         | 2.07     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 21 | 30   | 16 May 2027          |                   |  75.24          | 24.6          | 1.66     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 22 | 31   | 16 June 2027         |                   |  50.23          | 25.01         | 1.25     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 23 | 30   | 16 July 2027         |                   |  24.81          | 25.42         | 0.84     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 24 | 31   | 16 August 2027       |                   |   0.0           | 24.81         | 0.41     | 0.0  | 0.0       | 25.22 | 0.0   | 0.0        | 0.0  | 25.22       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 516.06        | 113.14   | 0.0  | 0.0       | 629.2   | 50.0 | 50.0       | 0.0  | 579.2       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance  | Reverted | Replayed |
      | 16 August 2025    | Disbursement           | 516.06  | 0.0       | 0.0      | 0.0  | 0.0       | 516.06        | false    | false    |
      | 08 September 2025 | Repayment              | 50.0    | 43.62     | 6.38     | 0.0  | 0.0       | 472.44        | false    | false    |

    When Admin sets the business date to "06 October 2025"
    When Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "06 October 2025" with 516.06 EUR transaction amount

    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance  | Reverted | Replayed |
      | 16 August 2025    | Disbursement           | 516.06  | 0.0       | 0.0      | 0.0  | 0.0       | 516.06        | false    | false    |
      | 08 September 2025 | Repayment              | 50.0    | 43.62     | 6.38     | 0.0  | 0.0       | 472.44        | false    | false    |
      | 16 September 2025 | Accrual Activity       | 6.38    | 0.0       | 6.38     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 06 October 2025   | Merchant Issued Refund | 516.06  | 472.44    | 7.28     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 06 October 2025   | Interest Refund        | 13.66   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0           | false    | false    |
      | 06 October 2025   | Accrual                | 13.66   | 0.0       | 13.66    | 0.0  | 0.0       | 0.0           | false    | false    |
      | 06 October 2025   | Accrual Activity       | 7.28    | 0.0       | 7.28     | 0.0  | 0.0       | 0.0           | false    | false    |

    And Loan status will be "OVERPAID"
    And Loan has 50 overpaid amount

    When Admin makes Credit Balance Refund transaction on "06 October 2025" with 50 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4128
  Scenario: Verify Payout Refund trn is processed after repayment for progressive loan with custom payment allocation rules - UC2
    When Admin sets the business date to "16 August 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY | 16 August 2025    | 516.06         | 19.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 24                | MONTHS                | 1              | MONTHS                 | 24                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "16 August 2025" with "516.06" amount and expected disbursement date on "16 August 2025"
    And Admin successfully disburse the loan on "16 August 2025" with "516.06" EUR transaction amount
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date                 | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 16 August 2025       |                  | 516.06          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 16 September 2025    |                  | 498.4           | 17.66         | 8.6      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 2  | 30   | 16 October 2025      |                  | 480.44          | 17.96         | 8.3      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 3  | 31   | 16 November 2025     |                  | 462.18          | 18.26         | 8.0      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 4  | 30   | 16 December 2025     |                  | 443.62          | 18.56         | 7.7      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 5  | 31   | 16 January 2026      |                  | 424.75          | 18.87         | 7.39     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 6  | 31   | 16 February 2026     |                  | 405.57          | 19.18         | 7.08     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 7  | 28   | 16 March 2026        |                  | 386.07          | 19.5          | 6.76     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 8  | 31   | 16 April 2026        |                  | 366.24          | 19.83         | 6.43     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 9  | 30   | 16 May 2026          |                  | 346.08          | 20.16         | 6.1      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 10 | 31   | 16 June 2026         |                  | 325.59          | 20.49         | 5.77     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 11 | 30   | 16 July 2026         |                  | 304.75          | 20.84         | 5.42     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 12 | 31   | 16 August 2026       |                  | 283.57          | 21.18         | 5.08     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 13 | 31   | 16 September 2026    |                  | 262.03          | 21.54         | 4.72     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 14 | 30   | 16 October 2026      |                  | 240.13          | 21.9          | 4.36     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 15 | 31   | 16 November 2026     |                  | 217.87          | 22.26         | 4.0      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 16 | 30   | 16 December 2026     |                  | 195.24          | 22.63         | 3.63     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 17 | 31   | 16 January 2027      |                  | 172.23          | 23.01         | 3.25     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 18 | 31   | 16 February 2027     |                  | 148.84          | 23.39         | 2.87     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 19 | 28   | 16 March 2027        |                  | 125.06          | 23.78         | 2.48     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 20 | 31   | 16 April 2027        |                  | 100.88          | 24.18         | 2.08     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 21 | 30   | 16 May 2027          |                  |  76.3           | 24.58         | 1.68     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 22 | 31   | 16 June 2027         |                  |  51.31          | 24.99         | 1.27     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 23 | 30   | 16 July 2027         |                  |  25.9           | 25.41         | 0.85     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 24 | 31   | 16 August 2027       |                  |   0.0           | 25.9          | 0.43     | 0.0  | 0.0       | 26.33 | 0.0   | 0.0        | 0.0  | 26.33       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 516.06        | 114.25   | 0.0  | 0.0       | 630.31  |  0.0 | 0.0        | 0.0  | 630.31      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance  | Reverted | Replayed |
      | 16 August 2025   | Disbursement     | 516.06  | 0.0       | 0.0      | 0.0  | 0.0       | 516.06        | false    | false    |

    When Admin sets the business date to "08 September 2025"
    And Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "08 September 2025" with 50 EUR transaction amount
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date                 | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 16 August 2025       |                   | 516.06          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 16 September 2025    | 08 September 2025 | 496.18          | 19.88         | 6.38     | 0.0  | 0.0       | 26.26 | 26.26 | 26.26      | 0.0  |  0.0        |
      | 2  | 30   | 16 October 2025      |                   | 469.92          | 26.26         | 0.0      | 0.0  | 0.0       | 26.26 | 23.74 | 23.74      | 0.0  |  2.52       |
      | 3  | 31   | 16 November 2025     |                   | 461.39          |  8.53         | 17.73    | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 4  | 30   | 16 December 2025     |                   | 442.82          | 18.57         | 7.69     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 5  | 31   | 16 January 2026      |                   | 423.94          | 18.88         | 7.38     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 6  | 31   | 16 February 2026     |                   | 404.74          | 19.2          | 7.06     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 7  | 28   | 16 March 2026        |                   | 385.22          | 19.52         | 6.74     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 8  | 31   | 16 April 2026        |                   | 365.38          | 19.84         | 6.42     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 9  | 30   | 16 May 2026          |                   | 345.21          | 20.17         | 6.09     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 10 | 31   | 16 June 2026         |                   | 324.7           | 20.51         | 5.75     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 11 | 30   | 16 July 2026         |                   | 303.85          | 20.85         | 5.41     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 12 | 31   | 16 August 2026       |                   | 282.65          | 21.2          | 5.06     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 13 | 31   | 16 September 2026    |                   | 261.1           | 21.55         | 4.71     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 14 | 30   | 16 October 2026      |                   | 239.19          | 21.91         | 4.35     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 15 | 31   | 16 November 2026     |                   | 216.91          | 22.28         | 3.98     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 16 | 30   | 16 December 2026     |                   | 194.26          | 22.65         | 3.61     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 17 | 31   | 16 January 2027      |                   | 171.24          | 23.02         | 3.24     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 18 | 31   | 16 February 2027     |                   | 147.83          | 23.41         | 2.85     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 19 | 28   | 16 March 2027        |                   | 124.03          | 23.8          | 2.46     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 20 | 31   | 16 April 2027        |                   |  99.84          | 24.19         | 2.07     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 21 | 30   | 16 May 2027          |                   |  75.24          | 24.6          | 1.66     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 22 | 31   | 16 June 2027         |                   |  50.23          | 25.01         | 1.25     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 23 | 30   | 16 July 2027         |                   |  24.81          | 25.42         | 0.84     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 24 | 31   | 16 August 2027       |                   |   0.0           | 24.81         | 0.41     | 0.0  | 0.0       | 25.22 | 0.0   | 0.0        | 0.0  | 25.22       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 516.06        | 113.14   | 0.0  | 0.0       | 629.2   | 50.0 | 50.0       | 0.0  | 579.2       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance  | Reverted | Replayed |
      | 16 August 2025    | Disbursement           | 516.06  | 0.0       | 0.0      | 0.0  | 0.0       | 516.06        | false    | false    |
      | 08 September 2025 | Repayment              | 50.0    | 43.62     | 6.38     | 0.0  | 0.0       | 472.44        | false    | false    |

    When Admin sets the business date to "06 October 2025"
   When Admin makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "06 October 2025" with 516.06 EUR transaction amount

    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type   | Amount  | Principal | Interest | Fees | Penalties | Loan Balance  | Reverted | Replayed |
      | 16 August 2025    | Disbursement       | 516.06  | 0.0       | 0.0      | 0.0  | 0.0       | 516.06        | false    | false    |
      | 08 September 2025 | Repayment          | 50.0    | 43.62     | 6.38     | 0.0  | 0.0       | 472.44        | false    | false    |
      | 16 September 2025 | Accrual Activity   | 6.38    | 0.0       | 6.38     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 06 October 2025   | Payout Refund      | 516.06  | 472.44    | 7.28     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 06 October 2025   | Interest Refund    | 13.66   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0           | false    | false    |
      | 06 October 2025   | Accrual            | 13.66   | 0.0       | 13.66    | 0.0  | 0.0       | 0.0           | false    | false    |
      | 06 October 2025   | Accrual Activity   | 7.28    | 0.0       | 7.28     | 0.0  | 0.0       | 0.0           | false    | false    |

    And Loan status will be "OVERPAID"
    And Loan has 50 overpaid amount

    When Admin makes Credit Balance Refund transaction on "06 October 2025" with 50 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4129
  Scenario: Verify Goodwill Credit trn is processed after repayment for progressive loan with custom payment allocation rules - UC3
    When Admin sets the business date to "16 August 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY | 16 August 2025    | 516.06         | 19.99                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 24                | MONTHS                | 1              | MONTHS                 | 24                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "16 August 2025" with "516.06" amount and expected disbursement date on "16 August 2025"
    And Admin successfully disburse the loan on "16 August 2025" with "516.06" EUR transaction amount
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date                 | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 16 August 2025       |                  | 516.06          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 16 September 2025    |                  | 498.4           | 17.66         | 8.6      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 2  | 30   | 16 October 2025      |                  | 480.44          | 17.96         | 8.3      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 3  | 31   | 16 November 2025     |                  | 462.18          | 18.26         | 8.0      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 4  | 30   | 16 December 2025     |                  | 443.62          | 18.56         | 7.7      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 5  | 31   | 16 January 2026      |                  | 424.75          | 18.87         | 7.39     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 6  | 31   | 16 February 2026     |                  | 405.57          | 19.18         | 7.08     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 7  | 28   | 16 March 2026        |                  | 386.07          | 19.5          | 6.76     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 8  | 31   | 16 April 2026        |                  | 366.24          | 19.83         | 6.43     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 9  | 30   | 16 May 2026          |                  | 346.08          | 20.16         | 6.1      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 10 | 31   | 16 June 2026         |                  | 325.59          | 20.49         | 5.77     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 11 | 30   | 16 July 2026         |                  | 304.75          | 20.84         | 5.42     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 12 | 31   | 16 August 2026       |                  | 283.57          | 21.18         | 5.08     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 13 | 31   | 16 September 2026    |                  | 262.03          | 21.54         | 4.72     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 14 | 30   | 16 October 2026      |                  | 240.13          | 21.9          | 4.36     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 15 | 31   | 16 November 2026     |                  | 217.87          | 22.26         | 4.0      | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 16 | 30   | 16 December 2026     |                  | 195.24          | 22.63         | 3.63     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 17 | 31   | 16 January 2027      |                  | 172.23          | 23.01         | 3.25     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 18 | 31   | 16 February 2027     |                  | 148.84          | 23.39         | 2.87     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 19 | 28   | 16 March 2027        |                  | 125.06          | 23.78         | 2.48     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 20 | 31   | 16 April 2027        |                  | 100.88          | 24.18         | 2.08     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 21 | 30   | 16 May 2027          |                  |  76.3           | 24.58         | 1.68     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 22 | 31   | 16 June 2027         |                  |  51.31          | 24.99         | 1.27     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 23 | 30   | 16 July 2027         |                  |  25.9           | 25.41         | 0.85     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 24 | 31   | 16 August 2027       |                  |   0.0           | 25.9          | 0.43     | 0.0  | 0.0       | 26.33 | 0.0   | 0.0        | 0.0  | 26.33       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 516.06        | 114.25   | 0.0  | 0.0       | 630.31  |  0.0 | 0.0        | 0.0  | 630.31      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance  | Reverted | Replayed |
      | 16 August 2025   | Disbursement     | 516.06  | 0.0       | 0.0      | 0.0  | 0.0       | 516.06        | false    | false    |

    When Admin sets the business date to "08 September 2025"
    And Admin makes "REPAYMENT" transaction with "AUTOPAY" payment type on "08 September 2025" with 50 EUR transaction amount
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date                 | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 16 August 2025       |                   | 516.06          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 16 September 2025    | 08 September 2025 | 496.18          | 19.88         | 6.38     | 0.0  | 0.0       | 26.26 | 26.26 | 26.26      | 0.0  |  0.0        |
      | 2  | 30   | 16 October 2025      |                   | 469.92          | 26.26         | 0.0      | 0.0  | 0.0       | 26.26 | 23.74 | 23.74      | 0.0  |  2.52       |
      | 3  | 31   | 16 November 2025     |                   | 461.39          |  8.53         | 17.73    | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 4  | 30   | 16 December 2025     |                   | 442.82          | 18.57         | 7.69     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 5  | 31   | 16 January 2026      |                   | 423.94          | 18.88         | 7.38     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 6  | 31   | 16 February 2026     |                   | 404.74          | 19.2          | 7.06     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 7  | 28   | 16 March 2026        |                   | 385.22          | 19.52         | 6.74     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 8  | 31   | 16 April 2026        |                   | 365.38          | 19.84         | 6.42     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 9  | 30   | 16 May 2026          |                   | 345.21          | 20.17         | 6.09     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 10 | 31   | 16 June 2026         |                   | 324.7           | 20.51         | 5.75     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 11 | 30   | 16 July 2026         |                   | 303.85          | 20.85         | 5.41     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 12 | 31   | 16 August 2026       |                   | 282.65          | 21.2          | 5.06     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 13 | 31   | 16 September 2026    |                   | 261.1           | 21.55         | 4.71     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 14 | 30   | 16 October 2026      |                   | 239.19          | 21.91         | 4.35     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 15 | 31   | 16 November 2026     |                   | 216.91          | 22.28         | 3.98     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 16 | 30   | 16 December 2026     |                   | 194.26          | 22.65         | 3.61     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 17 | 31   | 16 January 2027      |                   | 171.24          | 23.02         | 3.24     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 18 | 31   | 16 February 2027     |                   | 147.83          | 23.41         | 2.85     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 19 | 28   | 16 March 2027        |                   | 124.03          | 23.8          | 2.46     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 20 | 31   | 16 April 2027        |                   |  99.84          | 24.19         | 2.07     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 21 | 30   | 16 May 2027          |                   |  75.24          | 24.6          | 1.66     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 22 | 31   | 16 June 2027         |                   |  50.23          | 25.01         | 1.25     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 23 | 30   | 16 July 2027         |                   |  24.81          | 25.42         | 0.84     | 0.0  | 0.0       | 26.26 | 0.0   | 0.0        | 0.0  | 26.26       |
      | 24 | 31   | 16 August 2027       |                   |   0.0           | 24.81         | 0.41     | 0.0  | 0.0       | 25.22 | 0.0   | 0.0        | 0.0  | 25.22       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 516.06        | 113.14   | 0.0  | 0.0       | 629.2   | 50.0 | 50.0       | 0.0  | 579.2       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance  | Reverted | Replayed |
      | 16 August 2025    | Disbursement           | 516.06  | 0.0       | 0.0      | 0.0  | 0.0       | 516.06        | false    | false    |
      | 08 September 2025 | Repayment              | 50.0    | 43.62     | 6.38     | 0.0  | 0.0       | 472.44        | false    | false    |

    When Admin sets the business date to "06 October 2025"
    When Admin makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "06 October 2025" with 516.06 EUR transaction amount

    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type  | Amount  | Principal | Interest | Fees | Penalties | Loan Balance  | Reverted | Replayed |
      | 16 August 2025    | Disbursement      | 516.06  | 0.0       | 0.0      | 0.0  | 0.0       | 516.06        | false    | false    |
      | 08 September 2025 | Repayment         | 50.0    | 43.62     | 6.38     | 0.0  | 0.0       | 472.44        | false    | false    |
      | 16 September 2025 | Accrual Activity  | 6.38    | 0.0       | 6.38     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 06 October 2025   | Goodwill Credit   | 516.06  | 472.44    | 7.28     | 0.0  | 0.0       | 0.0           | false    | false    |
      | 06 October 2025   | Accrual           | 13.66   | 0.0       | 13.66    | 0.0  | 0.0       | 0.0           | false    | false    |
      | 06 October 2025   | Accrual Activity  | 7.28    | 0.0       | 7.28     | 0.0  | 0.0       | 0.0           | false    | false    |

    And Loan status will be "OVERPAID"
    And Loan has 36.34 overpaid amount

    When Admin makes Credit Balance Refund transaction on "06 October 2025" with 36.34 EUR transaction amount
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4517
  Scenario: Verify that accrual and accrual activity amounts are correct in case of early paid last installment, overdue last-1 installment on each days around due date
    When Admin sets the business date to "21 April 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_360_30_USD | 21 April 2025     | 218.54         | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 April 2025" with "218.54" amount and expected disbursement date on "21 April 2025"
    And Admin successfully disburse the loan on "21 April 2025" with "218.54" EUR transaction amount
    When Admin sets the business date to "02 May 2025"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "02 May 2025" with 37.49 EUR transaction amount and system-generated Idempotency key
    When Admin sets the business date to "21 May 2025"
    And Customer makes "AUTOPAY" repayment on "21 May 2025" with 37.49 EUR transaction amount
    When Admin sets the business date to "21 June 2025"
    And Customer makes "AUTOPAY" repayment on "21 June 2025" with 37.49 EUR transaction amount
    When Admin sets the business date to "21 July 2025"
    And Customer makes "AUTOPAY" repayment on "21 July 2025" with 37.49 EUR transaction amount
    When Admin sets the business date to "21 August 2025"
    And Customer makes "AUTOPAY" repayment on "21 August 2025" with 37.49 EUR transaction amount
#  --- Check on maturity date - 1 ---
    When Admin sets the business date to "20 October 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 21 April 2025     |                | 218.54          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 21 May 2025       | 21 May 2025    | 182.67          | 35.87         | 1.62     | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 2  | 31   | 21 June 2025      | 21 June 2025   | 146.39          | 36.28         | 1.21     | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 3  | 30   | 21 July 2025      | 21 July 2025   | 109.81          | 36.58         | 0.91     | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 4  | 31   | 21 August 2025    | 21 August 2025 | 72.92           | 36.89         | 0.6      | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 5  | 31   | 21 September 2025 |                | 37.49           | 35.43         | 0.57     | 0.0  | 0.0       | 36.0  | 0.12  | 0.12       | 0.0  | 35.88       |
      | 6  | 30   | 21 October 2025   | 02 May 2025    | 0.0             | 37.49         | 0.0      | 0.0  | 0.0       | 37.49 | 37.49 | 37.49      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 218.54        | 4.91     | 0.0  | 0.0       | 223.45 | 187.57 | 38.09      | 0.0  | 35.88       |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 April 2025     | Disbursement           | 218.54 | 0.0       | 0.0      | 0.0  | 0.0       | 218.54       | false    | false    |
      | 02 May 2025       | Merchant Issued Refund | 37.49  | 37.49     | 0.0      | 0.0  | 0.0       | 181.05       | false    | false    |
      | 02 May 2025       | Interest Refund        | 0.12   | 0.12      | 0.0      | 0.0  | 0.0       | 180.93       | false    | false    |
      | 21 May 2025       | Repayment              | 37.49  | 35.87     | 1.62     | 0.0  | 0.0       | 145.06       | false    | false    |
      | 21 May 2025       | Accrual Activity       | 1.62   | 0.0       | 1.62     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 June 2025      | Repayment              | 37.49  | 36.28     | 1.21     | 0.0  | 0.0       | 108.78       | false    | false    |
      | 21 June 2025      | Accrual Activity       | 1.21   | 0.0       | 1.21     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 July 2025      | Repayment              | 37.49  | 36.58     | 0.91     | 0.0  | 0.0       | 72.2         | false    | false    |
      | 21 July 2025      | Accrual Activity       | 0.91   | 0.0       | 0.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 August 2025    | Repayment              | 37.49  | 36.89     | 0.6      | 0.0  | 0.0       | 35.31        | false    | false    |
      | 21 August 2025    | Accrual Activity       | 0.6    | 0.0       | 0.6      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 September 2025 | Accrual Activity       | 0.56   | 0.0       | 0.56     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 October 2025   | Accrual                | 4.63   | 0.0       | 4.63     | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Check on maturity date ---
    When Admin sets the business date to "21 October 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 21 April 2025     |                | 218.54          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 21 May 2025       | 21 May 2025    | 182.67          | 35.87         | 1.62     | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 2  | 31   | 21 June 2025      | 21 June 2025   | 146.39          | 36.28         | 1.21     | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 3  | 30   | 21 July 2025      | 21 July 2025   | 109.81          | 36.58         | 0.91     | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 4  | 31   | 21 August 2025    | 21 August 2025 | 72.92           | 36.89         | 0.6      | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 5  | 31   | 21 September 2025 |                | 37.49           | 35.43         | 0.58     | 0.0  | 0.0       | 36.01 | 0.12  | 0.12       | 0.0  | 35.89       |
      | 6  | 30   | 21 October 2025   | 02 May 2025    | 0.0             | 37.49         | 0.0      | 0.0  | 0.0       | 37.49 | 37.49 | 37.49      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 218.54        | 4.92     | 0.0  | 0.0       | 223.46 | 187.57 | 38.09      | 0.0  | 35.89       |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 April 2025     | Disbursement           | 218.54 | 0.0       | 0.0      | 0.0  | 0.0       | 218.54       | false    | false    |
      | 02 May 2025       | Merchant Issued Refund | 37.49  | 37.49     | 0.0      | 0.0  | 0.0       | 181.05       | false    | false    |
      | 02 May 2025       | Interest Refund        | 0.12   | 0.12      | 0.0      | 0.0  | 0.0       | 180.93       | false    | false    |
      | 21 May 2025       | Repayment              | 37.49  | 35.87     | 1.62     | 0.0  | 0.0       | 145.06       | false    | false    |
      | 21 May 2025       | Accrual Activity       | 1.62   | 0.0       | 1.62     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 June 2025      | Repayment              | 37.49  | 36.28     | 1.21     | 0.0  | 0.0       | 108.78       | false    | false    |
      | 21 June 2025      | Accrual Activity       | 1.21   | 0.0       | 1.21     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 July 2025      | Repayment              | 37.49  | 36.58     | 0.91     | 0.0  | 0.0       | 72.2         | false    | false    |
      | 21 July 2025      | Accrual Activity       | 0.91   | 0.0       | 0.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 August 2025    | Repayment              | 37.49  | 36.89     | 0.6      | 0.0  | 0.0       | 35.31        | false    | false    |
      | 21 August 2025    | Accrual Activity       | 0.6    | 0.0       | 0.6      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 September 2025 | Accrual Activity       | 0.57   | 0.0       | 0.57     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 19 October 2025   | Accrual                | 4.63   | 0.0       | 4.63     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 October 2025   | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Check on maturity date + 1 ---
    When Admin sets the business date to "22 October 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 21 April 2025     |                | 218.54          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 21 May 2025       | 21 May 2025    | 182.67          | 35.87         | 1.62     | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 2  | 31   | 21 June 2025      | 21 June 2025   | 146.39          | 36.28         | 1.21     | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 3  | 30   | 21 July 2025      | 21 July 2025   | 109.81          | 36.58         | 0.91     | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 4  | 31   | 21 August 2025    | 21 August 2025 | 72.92           | 36.89         | 0.6      | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 5  | 31   | 21 September 2025 |                | 37.49           | 35.43         | 0.58     | 0.0  | 0.0       | 36.01 | 0.12  | 0.12       | 0.0  | 35.89       |
      | 6  | 30   | 21 October 2025   | 02 May 2025    | 0.0             | 37.49         | 0.0      | 0.0  | 0.0       | 37.49 | 37.49 | 37.49      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 218.54        | 4.92     | 0.0  | 0.0       | 223.46 | 187.57 | 38.09      | 0.0  | 35.89       |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 April 2025     | Disbursement           | 218.54 | 0.0       | 0.0      | 0.0  | 0.0       | 218.54       | false    | false    |
      | 02 May 2025       | Merchant Issued Refund | 37.49  | 37.49     | 0.0      | 0.0  | 0.0       | 181.05       | false    | false    |
      | 02 May 2025       | Interest Refund        | 0.12   | 0.12      | 0.0      | 0.0  | 0.0       | 180.93       | false    | false    |
      | 21 May 2025       | Repayment              | 37.49  | 35.87     | 1.62     | 0.0  | 0.0       | 145.06       | false    | false    |
      | 21 May 2025       | Accrual Activity       | 1.62   | 0.0       | 1.62     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 June 2025      | Repayment              | 37.49  | 36.28     | 1.21     | 0.0  | 0.0       | 108.78       | false    | false    |
      | 21 June 2025      | Accrual Activity       | 1.21   | 0.0       | 1.21     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 July 2025      | Repayment              | 37.49  | 36.58     | 0.91     | 0.0  | 0.0       | 72.2         | false    | false    |
      | 21 July 2025      | Accrual Activity       | 0.91   | 0.0       | 0.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 August 2025    | Repayment              | 37.49  | 36.89     | 0.6      | 0.0  | 0.0       | 35.31        | false    | false    |
      | 21 August 2025    | Accrual Activity       | 0.6    | 0.0       | 0.6      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 September 2025 | Accrual Activity       | 0.58   | 0.0       | 0.58     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 19 October 2025   | Accrual                | 4.63   | 0.0       | 4.63     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 October 2025   | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 October 2025   | Accrual                | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
#   --- Check on maturity date + 2 ---
    When Admin sets the business date to "23 October 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 21 April 2025     |                | 218.54          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 21 May 2025       | 21 May 2025    | 182.67          | 35.87         | 1.62     | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 2  | 31   | 21 June 2025      | 21 June 2025   | 146.39          | 36.28         | 1.21     | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 3  | 30   | 21 July 2025      | 21 July 2025   | 109.81          | 36.58         | 0.91     | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 4  | 31   | 21 August 2025    | 21 August 2025 | 72.92           | 36.89         | 0.6      | 0.0  | 0.0       | 37.49 | 37.49 | 0.12       | 0.0  | 0.0         |
      | 5  | 31   | 21 September 2025 |                | 37.49           | 35.43         | 0.58     | 0.0  | 0.0       | 36.01 | 0.12  | 0.12       | 0.0  | 35.89       |
      | 6  | 30   | 21 October 2025   | 02 May 2025    | 0.0             | 37.49         | 0.0      | 0.0  | 0.0       | 37.49 | 37.49 | 37.49      | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 218.54        | 4.92     | 0.0  | 0.0       | 223.46 | 187.57 | 38.09      | 0.0  | 35.89       |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 April 2025     | Disbursement           | 218.54 | 0.0       | 0.0      | 0.0  | 0.0       | 218.54       | false    | false    |
      | 02 May 2025       | Merchant Issued Refund | 37.49  | 37.49     | 0.0      | 0.0  | 0.0       | 181.05       | false    | false    |
      | 02 May 2025       | Interest Refund        | 0.12   | 0.12      | 0.0      | 0.0  | 0.0       | 180.93       | false    | false    |
      | 21 May 2025       | Repayment              | 37.49  | 35.87     | 1.62     | 0.0  | 0.0       | 145.06       | false    | false    |
      | 21 May 2025       | Accrual Activity       | 1.62   | 0.0       | 1.62     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 June 2025      | Repayment              | 37.49  | 36.28     | 1.21     | 0.0  | 0.0       | 108.78       | false    | false    |
      | 21 June 2025      | Accrual Activity       | 1.21   | 0.0       | 1.21     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 July 2025      | Repayment              | 37.49  | 36.58     | 0.91     | 0.0  | 0.0       | 72.2         | false    | false    |
      | 21 July 2025      | Accrual Activity       | 0.91   | 0.0       | 0.91     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 August 2025    | Repayment              | 37.49  | 36.89     | 0.6      | 0.0  | 0.0       | 35.31        | false    | false    |
      | 21 August 2025    | Accrual Activity       | 0.6    | 0.0       | 0.6      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 September 2025 | Accrual Activity       | 0.58   | 0.0       | 0.58     | 0.0  | 0.0       | 0.0          | false    | true     |
      | 19 October 2025   | Accrual                | 4.63   | 0.0       | 4.63     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 October 2025   | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 October 2025   | Accrual                | 0.01   | 0.0       | 0.01     | 0.0  | 0.0       | 0.0          | false    | false    |
    #   --- Close loan ---
    When Loan Pay-off is made on "23 October 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
