@LoanRepaymentSchedule
Feature: Loan repayment schedule handling

  @TestRailId:C3901
  Scenario: Verify Loan repayment schedule for progressive loan, interest type: Declining balance, interest calculation petiod: same as repayment  - UC1: 2nd disbursement on due date, interest recalculation enabled, partial period interest calculation enabled
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_PARTIAL_PERIOD | 01 January 2025   | 2000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "2000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 2  | 28   | 01 March 2025    |           | 336.66          | 333.32        | 6.7      | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 336.66        | 3.37     | 0.0  | 0.0       | 340.03 | 0.0  | 0.0        | 0.0  | 340.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.07    | 0.0  | 0.0       | 1020.07 | 0.0  | 0.0        | 0.0  | 1020.07     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    #    --- 2nd Disbursement ---
    When Admin sets the business date to "01 February 2025"
    And Admin successfully disburse the loan on "01 February 2025" with "200" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 01 February 2025 |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 2  | 28   | 01 March 2025    |           | 437.15          | 432.83        | 8.7      | 0.0  | 0.0       | 441.53 | 0.0  | 0.0        | 0.0  | 441.53      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 437.15        | 4.37     | 0.0  | 0.0       | 441.52 | 0.0  | 0.0        | 0.0  | 441.52      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1200.0        | 23.07    | 0.0  | 0.0       | 1223.07 | 0.0  | 0.0        | 0.0  | 1223.07     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       | false    | false    |

  @TestRailId:C3902
  Scenario: Verify Loan repayment schedule for progressive loan, interest type: Declining balance, interest calculation period: same as repayment - UC2: 2nd disbursement on due date, interest recalculation disabled, partial period interest calculation enabled
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_NO_INT_RECALC_MULTIDISB_PARTIAL_PERIOD | 01 January 2025   | 2000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "2000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 2  | 28   | 01 March 2025    |           | 336.66          | 333.32        | 6.7      | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 336.66        | 3.37     | 0.0  | 0.0       | 340.03 | 0.0  | 0.0        | 0.0  | 340.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.07    | 0.0  | 0.0       | 1020.07 | 0.0  | 0.0        | 0.0  | 1020.07     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    #    --- 2nd Disbursement ---
    When Admin sets the business date to "01 February 2025"
    And Admin successfully disburse the loan on "01 February 2025" with "200" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 01 February 2025 |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 2  | 28   | 01 March 2025    |           | 437.15          | 432.83        | 8.7      | 0.0  | 0.0       | 441.53 | 0.0  | 0.0        | 0.0  | 441.53      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 437.15        | 4.37     | 0.0  | 0.0       | 441.52 | 0.0  | 0.0        | 0.0  | 441.52      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1200.0        | 23.07    | 0.0  | 0.0       | 1223.07 | 0.0  | 0.0        | 0.0  | 1223.07     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       | false    | false    |

  @TestRailId:C3903
  Scenario: Verify Loan repayment schedule for progressive loan, interest type: Declining balance, interest calculation period: same as repayment - UC3: 2nd disbursement on due date, interest recalculation disabled, partial period interest calculation disabled
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_NO_INT_RECALC_MULTIDISB_NO_PARTIAL_PERIOD | 01 January 2025   | 2000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "2000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 2  | 28   | 01 March 2025    |           | 336.66          | 333.32        | 6.7      | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 336.66        | 3.37     | 0.0  | 0.0       | 340.03 | 0.0  | 0.0        | 0.0  | 340.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.07    | 0.0  | 0.0       | 1020.07 | 0.0  | 0.0        | 0.0  | 1020.07     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    #    --- 2nd Disbursement ---
    When Admin sets the business date to "01 February 2025"
    And Admin successfully disburse the loan on "01 February 2025" with "200" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 01 February 2025 |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 2  | 28   | 01 March 2025    |           | 437.15          | 432.83        | 8.7      | 0.0  | 0.0       | 441.53 | 0.0  | 0.0        | 0.0  | 441.53      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 437.15        | 4.37     | 0.0  | 0.0       | 441.52 | 0.0  | 0.0        | 0.0  | 441.52      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1200.0        | 23.07    | 0.0  | 0.0       | 1223.07 | 0.0  | 0.0        | 0.0  | 1223.07     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       | false    | false    |

  @TestRailId:C3904
  Scenario: Verify Loan repayment schedule for progressive loan, interest type: Declining balance, interest calculation petiod: same as repayment  - UC4: 2nd disbursement NOT on due date, interest recalculation enabled, partial period interest calculation enabled
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_PARTIAL_PERIOD | 01 January 2025   | 2000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "2000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 2  | 28   | 01 March 2025    |           | 336.66          | 333.32        | 6.7      | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 336.66        | 3.37     | 0.0  | 0.0       | 340.03 | 0.0  | 0.0        | 0.0  | 340.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.07    | 0.0  | 0.0       | 1020.07 | 0.0  | 0.0        | 0.0  | 1020.07     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    #    --- 2nd Disbursement ---
    When Admin sets the business date to "15 January 2025"
    And Admin successfully disburse the loan on "15 January 2025" with "200" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2025  |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 803.38          | 396.62        | 11.1     | 0.0  | 0.0       | 407.72 | 0.0  | 0.0        | 0.0  | 407.72      |
      | 2  | 28   | 01 March 2025    |           | 403.69          | 399.69        | 8.03     | 0.0  | 0.0       | 407.72 | 0.0  | 0.0        | 0.0  | 407.72      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 403.69        | 4.04     | 0.0  | 0.0       | 407.73 | 0.0  | 0.0        | 0.0  | 407.73      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1200.0        | 23.17    | 0.0  | 0.0       | 1223.17 | 0.0  | 0.0        | 0.0  | 1223.17     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       | false    | false    |

  @TestRailId:C3905
  Scenario: Verify Loan repayment schedule for progressive loan, interest type: Declining balance, interest calculation period: same as repayment - UC5: 2nd disbursement NOT on due date, interest recalculation disabled, partial period interest calculation enabled
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_NO_INT_RECALC_MULTIDISB_PARTIAL_PERIOD | 01 January 2025   | 2000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "2000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 2  | 28   | 01 March 2025    |           | 336.66          | 333.32        | 6.7      | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 336.66        | 3.37     | 0.0  | 0.0       | 340.03 | 0.0  | 0.0        | 0.0  | 340.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.07    | 0.0  | 0.0       | 1020.07 | 0.0  | 0.0        | 0.0  | 1020.07     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    #    --- 2nd Disbursement ---
    When Admin sets the business date to "15 January 2025"
    And Admin successfully disburse the loan on "15 January 2025" with "200" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2025  |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 803.38          | 396.62        | 11.1     | 0.0  | 0.0       | 407.72 | 0.0  | 0.0        | 0.0  | 407.72      |
      | 2  | 28   | 01 March 2025    |           | 403.69          | 399.69        | 8.03     | 0.0  | 0.0       | 407.72 | 0.0  | 0.0        | 0.0  | 407.72      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 403.69        | 4.04     | 0.0  | 0.0       | 407.73 | 0.0  | 0.0        | 0.0  | 407.73      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1200.0        | 23.17    | 0.0  | 0.0       | 1223.17 | 0.0  | 0.0        | 0.0  | 1223.17     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       | false    | false    |

  @TestRailId:C3906
  Scenario: Verify Loan repayment schedule for progressive loan, interest type: Declining balance, interest calculation period: same as repayment - UC6: 2nd disbursement NOT on due date, interest recalculation disabled, partial period interest calculation disabled
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_NO_INT_RECALC_MULTIDISB_NO_PARTIAL_PERIOD | 01 January 2025   | 2000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "2000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 2  | 28   | 01 March 2025    |           | 336.66          | 333.32        | 6.7      | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 336.66        | 3.37     | 0.0  | 0.0       | 340.03 | 0.0  | 0.0        | 0.0  | 340.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.07    | 0.0  | 0.0       | 1020.07 | 0.0  | 0.0        | 0.0  | 1020.07     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    #    --- 2nd Disbursement ---
    When Admin sets the business date to "15 January 2025"
    And Admin successfully disburse the loan on "15 January 2025" with "200" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2025  |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 803.97          | 396.03        | 12.0     | 0.0  | 0.0       | 408.03 | 0.0  | 0.0        | 0.0  | 408.03      |
      | 2  | 28   | 01 March 2025    |           | 403.98          | 399.99        | 8.04     | 0.0  | 0.0       | 408.03 | 0.0  | 0.0        | 0.0  | 408.03      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 403.98        | 4.04     | 0.0  | 0.0       | 408.02 | 0.0  | 0.0        | 0.0  | 408.02      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1200          | 24.08    | 0.0  | 0.0       | 1224.08 | 0.0  | 0.0        | 0.0  | 1224.08     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2025  | Accrual          | 0.39   | 0.0       | 0.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.39   | 0.0       | 0.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.39   | 0.0       | 0.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.39   | 0.0       | 0.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.39   | 0.0       | 0.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.39   | 0.0       | 0.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.39   | 0.0       | 0.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.39   | 0.0       | 0.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.39   | 0.0       | 0.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.38   | 0.0       | 0.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       | false    | false    |

  @TestRailId:C3907
  Scenario: Verify Loan repayment schedule for progressive loan, interest type: Declining balance, interest calculation period: same as repayment - UC7: 2nd disbursement is in the middle of 2nd period + backdated repayment, interest recalculation enabled, partial period interest calculation enabled
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_PARTIAL_PERIOD | 01 January 2025   | 2000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "2000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 2  | 28   | 01 March 2025    |           | 336.66          | 333.32        | 6.7      | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 336.66        | 3.37     | 0.0  | 0.0       | 340.03 | 0.0  | 0.0        | 0.0  | 340.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.07    | 0.0  | 0.0       | 1020.07 | 0.0  | 0.0        | 0.0  | 1020.07     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "01 February 2025"
    And Admin runs inline COB job for Loan
#    --- 2nd Disbursement ---
    When Admin sets the business date to "15 February 2025"
    And Admin successfully disburse the loan on "15 February 2025" with "200" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      |    |      | 15 February 2025 |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 28   | 01 March 2025    |           | 438.31          | 431.67        | 9.35     | 0.0  | 0.0       | 441.02 | 0.0  | 0.0        | 0.0  | 441.02      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 438.31        | 4.38     | 0.0  | 0.0       | 442.69 | 0.0  | 0.0        | 0.0  | 442.69      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1200.0        | 23.73    | 0.0  | 0.0       | 1223.73 | 0.0  | 0.0        | 0.0  | 1223.73     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2025 | Accrual          | 0.35   | 0.0       | 0.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2025 | Accrual          | 0.35   | 0.0       | 0.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2025 | Accrual          | 0.35   | 0.0       | 0.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2025 | Accrual          | 0.35   | 0.0       | 0.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2025 | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       | false    | false    |
#    --- Backdated repayment ---
    When Customer makes "AUTOPAY" repayment on "01 February 2025" with 340.02 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 01 February 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 February 2025 |                  | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 2  | 28   | 01 March 2025    |                  | 436.66          | 433.32        | 7.7      | 0.0  | 0.0       | 441.02 | 0.0    | 0.0        | 0.0  | 441.02      |
      | 3  | 31   | 01 April 2025    |                  | 0.0             | 436.66        | 4.37     | 0.0  | 0.0       | 441.03 | 0.0    | 0.0        | 0.0  | 441.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1200.0        | 22.07    | 0.0  | 0.0       | 1222.07 | 340.02 | 0.0        | 0.0  | 882.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Repayment        | 340.02 | 330.02    | 10.0     | 0.0  | 0.0       | 669.98       | false    | false    |
      | 02 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2025 | Accrual          | 0.35   | 0.0       | 0.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2025 | Accrual          | 0.35   | 0.0       | 0.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2025 | Accrual          | 0.35   | 0.0       | 0.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2025 | Accrual          | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2025 | Accrual          | 0.35   | 0.0       | 0.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2025 | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 869.98       | false    | false    |

  @TestRailId:C3908
  Scenario: Verify Loan repayment schedule for progressive loan, interest type: Declining balance, interest calculation period: same as repayment - UC8: 2nd disbursement is in the middle of 2nd period + backdated repayment, interest recalculation disabled, partial period interest calculation enabled
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_NO_INT_RECALC_MULTIDISB_PARTIAL_PERIOD | 01 January 2025   | 2000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "2000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 2  | 28   | 01 March 2025    |           | 336.66          | 333.32        | 6.7      | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 336.66        | 3.37     | 0.0  | 0.0       | 340.03 | 0.0  | 0.0        | 0.0  | 340.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.07    | 0.0  | 0.0       | 1020.07 | 0.0  | 0.0        | 0.0  | 1020.07     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "01 February 2025"
    And Admin runs inline COB job for Loan
    #    --- 2nd Disbursement ---
    When Admin sets the business date to "15 February 2025"
    And Admin successfully disburse the loan on "15 February 2025" with "200" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      |    |      | 15 February 2025 |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 28   | 01 March 2025    |           | 436.66          | 433.32        | 7.7      | 0.0  | 0.0       | 441.02 | 0.0  | 0.0        | 0.0  | 441.02      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 436.66        | 4.37     | 0.0  | 0.0       | 441.03 | 0.0  | 0.0        | 0.0  | 441.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1200.0        | 22.07    | 0.0  | 0.0       | 1222.07 | 0.0  | 0.0        | 0.0  | 1222.07     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2025 | Accrual          | 0.23   | 0.0       | 0.23     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2025 | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       | false    | false    |
#    --- Backdated repayment ---
    When Customer makes "AUTOPAY" repayment on "01 February 2025" with 340.02 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 01 February 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 February 2025 |                  | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 2  | 28   | 01 March 2025    |                  | 436.66          | 433.32        | 7.7      | 0.0  | 0.0       | 441.02 | 0.0    | 0.0        | 0.0  | 441.02      |
      | 3  | 31   | 01 April 2025    |                  | 0.0             | 436.66        | 4.37     | 0.0  | 0.0       | 441.03 | 0.0    | 0.0        | 0.0  | 441.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1200.0        | 22.07    | 0.0  | 0.0       | 1222.07 | 340.02 | 0.0        | 0.0  | 882.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Repayment        | 340.02 | 330.02    | 10.0     | 0.0  | 0.0       | 669.98       | false    | false    |
      | 02 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2025 | Accrual          | 0.23   | 0.0       | 0.23     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2025 | Accrual          | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2025 | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 869.98       | false    | false    |

  @TestRailId:C3909
  Scenario: Verify Loan repayment schedule for progressive loan, interest type: Declining balance, interest calculation period: same as repayment - UC9: 2nd disbursement is in the middle of 2nd period + backdated repayment, interest recalculation disabled, partial period interest calculation disabled
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_NO_INT_RECALC_MULTIDISB_NO_PARTIAL_PERIOD | 01 January 2025   | 2000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "2000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 2  | 28   | 01 March 2025    |           | 336.66          | 333.32        | 6.7      | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 336.66        | 3.37     | 0.0  | 0.0       | 340.03 | 0.0  | 0.0        | 0.0  | 340.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.07    | 0.0  | 0.0       | 1020.07 | 0.0  | 0.0        | 0.0  | 1020.07     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "01 February 2025"
    And Admin runs inline COB job for Loan
#    --- 2nd Disbursement ---
    When Admin sets the business date to "15 February 2025"
    And Admin successfully disburse the loan on "15 February 2025" with "200" EUR transaction amount
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      |    |      | 15 February 2025 |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 28   | 01 March 2025    |           | 437.15          | 432.83        | 8.7      | 0.0  | 0.0       | 441.53 | 0.0  | 0.0        | 0.0  | 441.53      |
      | 3  | 31   | 01 April 2025    |           | 0.0             | 437.15        | 4.37     | 0.0  | 0.0       | 441.52 | 0.0  | 0.0        | 0.0  | 441.52      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1200.0        | 23.07    | 0.0  | 0.0       | 1223.07 | 0.0  | 0.0        | 0.0  | 1223.07     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2025 | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2025 | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       | false    | false    |
#    --- Backdated repayment ---
    When Customer makes "AUTOPAY" repayment on "01 February 2025" with 340.02 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 01 February 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 0.0        | 0.0  | 0.0         |
      |    |      | 15 February 2025 |                  | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 2  | 28   | 01 March 2025    |                  | 437.15          | 432.83        | 8.7      | 0.0  | 0.0       | 441.53 | 0.0    | 0.0        | 0.0  | 441.53      |
      | 3  | 31   | 01 April 2025    |                  | 0.0             | 437.15        | 4.37     | 0.0  | 0.0       | 441.52 | 0.0    | 0.0        | 0.0  | 441.52      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1200.0        | 23.07    | 0.0  | 0.0       | 1223.07 | 340.02 | 0.0        | 0.0  | 883.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual          | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Repayment        | 340.02 | 330.02    | 10.0     | 0.0  | 0.0       | 669.98       | false    | false    |
      | 02 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2025 | Accrual          | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2025 | Accrual          | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2025 | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 869.98       | false    | false    |

  @TestRailId:C3910
  Scenario: Verify Loan repayment schedule for progressive loan, interest type: Declining balance, interest calculation period: same as repayment - UC10: complex transactions, interest recalculation enabled, partial period interest calculation enabled
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_PARTIAL_PERIOD | 01 January 2025   | 2000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "2000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "01 February 2025"
    And Admin runs inline COB job for Loan
#    --- 2nd Disbursement ---
    When Admin sets the business date to "15 February 2025"
    And Admin runs inline COB job for Loan
    And Admin successfully disburse the loan on "15 February 2025" with "200" EUR transaction amount
#    --- Backdated repayment (early repayment) ---
    When Customer makes "AUTOPAY" repayment on "15 January 2025" with 340.02 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 664.5           | 335.5         | 4.52     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0  | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 2  | 28   | 01 March 2025    |                 | 434.76          | 429.74        | 11.28    | 0.0  | 0.0       | 441.02 | 0.0    | 0.0        | 0.0  | 441.02      |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 434.76        | 4.35     | 0.0  | 0.0       | 439.11 | 0.0    | 0.0        | 0.0  | 439.11      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1200.0        | 20.15    | 0.0  | 0.0       | 1220.15 | 340.02 | 340.02     | 0.0  | 880.13      |
#    --- Make full 2nd period repayment (late repayment) ---
    When Admin sets the business date to "15 March 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 March 2025" with 441.02 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 664.5           | 335.5         | 4.52     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 434.76          | 429.74        | 11.28    | 0.0  | 0.0       | 441.02 | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 434.76        | 6.29     | 0.0  | 0.0       | 441.05 | 0.0    | 0.0        | 0.0    | 441.05      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1200.0        | 22.09    | 0.0  | 0.0       | 1222.09 | 781.04 | 340.02     | 441.02 | 441.05      |
  #    --- Make merchant issued refund ---
    When Admin sets the business date to "16 March 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "16 March 2025" with 250 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 664.5           | 335.5         | 4.52     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 434.76          | 429.74        | 11.28    | 0.0  | 0.0       | 441.02 | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 434.76        | 5.0      | 0.0  | 0.0       | 439.76 | 250.0  | 250.0      | 0.0    | 189.76      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid    | In advance | Late   | Outstanding |
      | 1200.0        | 20.8     | 0.0  | 0.0       | 1220.8 | 1031.04 | 590.02     | 441.02 | 189.76      |
#    --- Create chargeback ---
    When Admin sets the business date to "17 March 2025"
    And Admin runs inline COB job for Loan
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 664.5           | 335.5         | 4.52     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 434.76          | 429.74        | 11.28    | 0.0  | 0.0       | 441.02 | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 684.76        | 6.21     | 0.0  | 0.0       | 690.97 | 250.0  | 250.0      | 0.0    | 440.97      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1450.0        | 22.01    | 0.0  | 0.0       | 1472.01 | 1031.04 | 590.02     | 441.02 | 440.97      |
#    --- Make repayment with amount to close the loan ---
    When Admin sets the business date to "18 March 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "18 March 2025" with 439 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan has 0 outstanding amount
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 664.5           | 335.5         | 4.52     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 434.76          | 429.74        | 11.28    | 0.0  | 0.0       | 441.02 | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    | 18 March 2025   | 0.0             | 684.76        | 4.24     | 0.0  | 0.0       | 689.0  | 689.0  | 689.0      | 0.0    | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1450.0        | 20.04    | 0.0  | 0.0       | 1470.04 | 1470.04 | 1029.02    | 441.02 | 0.0         |
#    --- Undo last payment ---
    When Admin sets the business date to "19 March 2025"
    And Admin runs inline COB job for Loan
    And Customer undo "1"th "Repayment" transaction made on "18 March 2025"
    Then Loan status will be "ACTIVE"
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 664.5           | 335.5         | 4.52     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 434.76          | 429.74        | 11.28    | 0.0  | 0.0       | 441.02 | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 684.76        | 6.21     | 0.0  | 0.0       | 690.97 | 250.0  | 250.0      | 0.0    | 440.97      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1450.0        | 22.01    | 0.0  | 0.0       | 1472.01 | 1031.04 | 590.02     | 441.02 | 440.97      |
#    --- Make repayment with amount that will overpay the loan with half of first repayment ---
    When Customer makes "AUTOPAY" repayment on "19 March 2025" with 609.15 EUR transaction amount
    Then Loan status will be "OVERPAID"
    And Loan has 170.01 overpaid amount
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 664.5           | 335.5         | 4.52     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 434.76          | 429.74        | 11.28    | 0.0  | 0.0       | 441.02 | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    | 19 March 2025   | 0.0             | 684.76        | 4.38     | 0.0  | 0.0       | 689.14 | 689.14 | 689.14     | 0.0    | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1450.0        | 20.18    | 0.0  | 0.0       | 1470.18 | 1470.18 | 1029.16    | 441.02 | 0.0         |
#    --- Make credit balance refund ---
    When Admin makes Credit Balance Refund transaction on "19 March 2025" with 170.01 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan has 0 outstanding amount
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 664.5           | 335.5         | 4.52     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 434.76          | 429.74        | 11.28    | 0.0  | 0.0       | 441.02 | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    | 19 March 2025   | 0.0             | 684.76        | 4.38     | 0.0  | 0.0       | 689.14 | 689.14 | 689.14     | 0.0    | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1450.0        | 20.18    | 0.0  | 0.0       | 1470.18 | 1470.18 | 1029.16    | 441.02 | 0.0         |
#    --- Make chargeback for first repayment ---
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 340.02 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 664.5           | 335.5         | 4.52     | 0.0  | 0.0       | 340.02  | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0     | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 434.76          | 429.74        | 11.28    | 0.0  | 0.0       | 441.02  | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 1024.78       | 5.81     | 0.0  | 0.0       | 1030.59 | 689.14 | 689.14     | 0.0    | 341.45      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1790.02       | 21.61    | 0.0  | 0.0       | 1811.63 | 1470.18 | 1029.16    | 441.02 | 341.45      |
#    --- Make repayment to close the loan ---
    When Customer makes "AUTOPAY" repayment on "19 March 2025" with 340.02 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan has 0 outstanding amount
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0     |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 664.5           | 335.5         | 4.52     | 0.0  | 0.0       | 340.02  | 340.02  | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0     | 0.0     |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 434.76          | 429.74        | 11.28    | 0.0  | 0.0       | 441.02  | 441.02  | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    | 19 March 2025   | 0.0             | 1024.78       | 4.38     | 0.0  | 0.0       | 1029.16 | 1029.16 | 1029.16    | 0.0    | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1790.02       | 20.18    | 0.0  | 0.0       | 1810.20 | 1810.20 | 1369.18    | 441.02 | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Repayment              | 340.02 | 335.5     | 4.52     | 0.0  | 0.0       | 664.5        | false    | false    |
      | 16 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2025 | Accrual                | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2025 | Accrual                | 0.35   | 0.0       | 0.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2025 | Accrual                | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2025 | Accrual                | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2025 | Accrual                | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2025 | Accrual                | 0.35   | 0.0       | 0.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2025 | Accrual                | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2025 | Accrual                | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2025 | Accrual                | 0.35   | 0.0       | 0.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2025 | Accrual                | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2025 | Accrual                | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2025 | Accrual                | 0.36   | 0.0       | 0.36     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2025 | Accrual                | 0.35   | 0.0       | 0.35     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2025 | Disbursement           | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 864.5        | false    | false    |
      | 15 February 2025 | Accrual Adjustment     | 3.16   | 0.0       | 3.16     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2025 | Accrual                | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Accrual                | 0.3    | 0.0       | 0.3      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2025    | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2025    | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2025    | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 March 2025    | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 March 2025    | Accrual                | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 March 2025    | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 March 2025    | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 March 2025    | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2025    | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 March 2025    | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 March 2025    | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 March 2025    | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 March 2025    | Accrual                | 0.28   | 0.0       | 0.28     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2025    | Repayment              | 441.02 | 429.74    | 11.28    | 0.0  | 0.0       | 434.76       | false    | false    |
      | 15 March 2025    | Accrual                | 0.27   | 0.0       | 0.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 March 2025    | Merchant Issued Refund | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 184.76       | false    | false    |
      | 16 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 March 2025    | Chargeback             | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 434.76       | false    | false    |
      | 17 March 2025    | Accrual                | 0.06   | 0.0       | 0.06     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 March 2025    | Repayment              | 439.0  | 434.76    | 4.24     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 18 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2025    | Repayment              | 609.15 | 434.76    | 4.38     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2025    | Credit Balance Refund  | 170.01 | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2025    | Chargeback             | 340.02 | 340.02    | 0.0      | 0.0  | 0.0       | 340.02       | false    | false    |
      | 19 March 2025    | Repayment              | 340.02 | 340.02    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3911
  Scenario: Verify Loan repayment schedule for progressive loan, interest type: Declining balance, interest calculation period: same as repayment - UC11: complex transactions, interest recalculation disabled, partial period interest calculation enabled
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_NO_INT_RECALC_MULTIDISB_PARTIAL_PERIOD | 01 January 2025   | 2000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "2000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "01 February 2025"
    And Admin runs inline COB job for Loan
#    --- 2nd Disbursement ---
    When Admin sets the business date to "15 February 2025"
    And Admin runs inline COB job for Loan
    And Admin successfully disburse the loan on "15 February 2025" with "200" EUR transaction amount
#    --- Backdated repayment (early repayment) ---
    When Customer makes "AUTOPAY" repayment on "15 January 2025" with 340.02 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0  | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 2  | 28   | 01 March 2025    |                 | 436.66          | 433.32        | 7.7      | 0.0  | 0.0       | 441.02 | 0.0    | 0.0        | 0.0  | 441.02      |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 436.66        | 4.37     | 0.0  | 0.0       | 441.03 | 0.0    | 0.0        | 0.0  | 441.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1200.0        | 22.07    | 0.0  | 0.0       | 1222.07 | 340.02 | 340.02     | 0.0  | 882.05      |
#    --- Make full 2nd period repayment (late repayment) ---
    When Admin sets the business date to "15 March 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 March 2025" with 441.02 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 436.66          | 433.32        | 7.7      | 0.0  | 0.0       | 441.02 | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 436.66        | 4.37     | 0.0  | 0.0       | 441.03 | 0.0    | 0.0        | 0.0    | 441.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1200.0        | 22.07    | 0.0  | 0.0       | 1222.07 | 781.04 | 340.02     | 441.02 | 441.03      |
  #    --- Make merchant issued refund ---
    When Admin sets the business date to "16 March 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "16 March 2025" with 250 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 436.66          | 433.32        | 7.7      | 0.0  | 0.0       | 441.02 | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 436.66        | 4.37     | 0.0  | 0.0       | 441.03 | 250.0  | 250.0      | 0.0    | 191.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1200.0        | 22.07    | 0.0  | 0.0       | 1222.07 | 1031.04 | 590.02     | 441.02 | 191.03      |
#    --- Create chargeback ---
    When Admin sets the business date to "17 March 2025"
    And Admin runs inline COB job for Loan
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 436.66          | 433.32        | 7.7      | 0.0  | 0.0       | 441.02 | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 686.66        | 4.37     | 0.0  | 0.0       | 691.03 | 250.0  | 250.0      | 0.0    | 441.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1450.0        | 22.07    | 0.0  | 0.0       | 1472.07 | 1031.04 | 590.02     | 441.02 | 441.03      |
#    --- Make repayment with amount to close the loan ---
    When Admin sets the business date to "18 March 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "18 March 2025" with 441.03 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan has 0 outstanding amount
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 436.66          | 433.32        | 7.7      | 0.0  | 0.0       | 441.02 | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    | 18 March 2025   | 0.0             | 686.66        | 4.37     | 0.0  | 0.0       | 691.03 | 691.03 | 691.03     | 0.0    | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1450.0        | 22.07    | 0.0  | 0.0       | 1472.07 | 1472.07 | 1031.05    | 441.02 | 0.0         |
#    --- Undo last payment ---
    When Admin sets the business date to "19 March 2025"
    And Admin runs inline COB job for Loan
    And Customer undo "1"th "Repayment" transaction made on "18 March 2025"
    Then Loan status will be "ACTIVE"
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 436.66          | 433.32        | 7.7      | 0.0  | 0.0       | 441.02 | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 686.66        | 4.37     | 0.0  | 0.0       | 691.03 | 250.0  | 250.0      | 0.0    | 441.03      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1450.0        | 22.07    | 0.0  | 0.0       | 1472.07 | 1031.04 | 590.02     | 441.02 | 441.03      |
#    --- Make repayment with amount that will overpay the loan with half of first repayment ---
    When Customer makes "AUTOPAY" repayment on "19 March 2025" with 611.04 EUR transaction amount
    Then Loan status will be "OVERPAID"
    And Loan has 170.01 overpaid amount
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 436.66          | 433.32        | 7.7      | 0.0  | 0.0       | 441.02 | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    | 19 March 2025   | 0.0             | 686.66        | 4.37     | 0.0  | 0.0       | 691.03 | 691.03 | 691.03     | 0.0    | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1450.0        | 22.07    | 0.0  | 0.0       | 1472.07 | 1472.07 | 1031.05    | 441.02 | 0.0         |
#    --- Make credit balance refund ---
    When Admin makes Credit Balance Refund transaction on "19 March 2025" with 170.01 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan has 0 outstanding amount
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 436.66          | 433.32        | 7.7      | 0.0  | 0.0       | 441.02 | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    | 19 March 2025   | 0.0             | 686.66        | 4.37     | 0.0  | 0.0       | 691.03 | 691.03 | 691.03     | 0.0    | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1450.0        | 22.07    | 0.0  | 0.0       | 1472.07 | 1472.07 | 1031.05    | 441.02 | 0.0         |
#    --- Make chargeback for first repayment ---
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 340.02 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02  | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0     | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 436.66          | 433.32        | 7.7      | 0.0  | 0.0       | 441.02  | 441.02 | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 1026.68       | 4.37     | 0.0  | 0.0       | 1031.05 | 691.03 | 691.03     | 0.0    | 340.02      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1790.02       | 22.07    | 0.0  | 0.0       | 1812.09 | 1472.07 | 1031.05    | 441.02 | 340.02      |
#    --- Make repayment to close the loan ---
    When Customer makes "AUTOPAY" repayment on "19 March 2025" with 340.02 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan has 0 outstanding amount
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0     |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02  | 340.02  | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0     | 0.0     |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 436.66          | 433.32        | 7.7      | 0.0  | 0.0       | 441.02  | 441.02  | 0.0        | 441.02 | 0.0         |
      | 3  | 31   | 01 April 2025    | 19 March 2025   | 0.0             | 1026.68       | 4.37     | 0.0  | 0.0       | 1031.05 | 1031.05 | 1031.05    | 0.0    | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1790.02       | 22.07    | 0.0  | 0.0       | 1812.09 | 1812.09 | 1371.07    | 441.02 | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Repayment              | 340.02 | 330.02    | 10.0     | 0.0  | 0.0       | 669.98       | false    | false    |
      | 16 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2025 | Accrual                | 0.23   | 0.0       | 0.23     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2025 | Disbursement           | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 869.98       | false    | false    |
      | 15 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2025 | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 March 2025    | Accrual                | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2025    | Repayment              | 441.02 | 433.32    | 7.7      | 0.0  | 0.0       | 436.66       | false    | false    |
      | 15 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 March 2025    | Merchant Issued Refund | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 186.66       | false    | false    |
      | 16 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 March 2025    | Chargeback             | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 436.66       | false    | false    |
      | 17 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 March 2025    | Repayment              | 441.03 | 436.66    | 4.37     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 18 March 2025    | Accrual                | 2.12   | 0.0       | 2.12     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2025    | Repayment              | 611.04 | 436.66    | 4.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2025    | Credit Balance Refund  | 170.01 | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2025    | Chargeback             | 340.02 | 340.02    | 0.0      | 0.0  | 0.0       | 340.02       | false    | false    |
      | 19 March 2025    | Repayment              | 340.02 | 340.02    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C3912
  Scenario: Verify Loan repayment schedule for progressive loan, interest type: Declining balance, interest calculation period: same as repayment - UC12: complex transactions, interest recalculation disabled, partial period interest calculation disabled
    When Admin sets the business date to "01 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_NO_INT_RECALC_MULTIDISB_NO_PARTIAL_PERIOD | 01 January 2025   | 2000           | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "2000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin runs inline COB job for Loan
    When Admin sets the business date to "01 February 2025"
    And Admin runs inline COB job for Loan
#    --- 2nd Disbursement ---
    When Admin sets the business date to "15 February 2025"
    And Admin runs inline COB job for Loan
    And Admin successfully disburse the loan on "15 February 2025" with "200" EUR transaction amount
#    --- Backdated repayment (early repayment) ---
    When Customer makes "AUTOPAY" repayment on "15 January 2025" with 340.02 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0  | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 2  | 28   | 01 March 2025    |                 | 437.15          | 432.83        | 8.7      | 0.0  | 0.0       | 441.53 | 0.0    | 0.0        | 0.0  | 441.53      |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 437.15        | 4.37     | 0.0  | 0.0       | 441.52 | 0.0    | 0.0        | 0.0  | 441.52      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1200.0        | 23.07    | 0.0  | 0.0       | 1223.07 | 340.02 | 340.02     | 0.0  | 883.05      |
#    --- Make full 2nd period repayment (late repayment) ---
    When Admin sets the business date to "15 March 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 March 2025" with 441.53 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 437.15          | 432.83        | 8.7      | 0.0  | 0.0       | 441.53 | 441.53 | 0.0        | 441.53 | 0.0         |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 437.15        | 4.37     | 0.0  | 0.0       | 441.52 | 0.0    | 0.0        | 0.0    | 441.52      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1200.0        | 23.07    | 0.0  | 0.0       | 1223.07 | 781.55 | 340.02     | 441.53 | 441.52      |
  #    --- Make merchant issued refund ---
    When Admin sets the business date to "16 March 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "16 March 2025" with 250 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 437.15          | 432.83        | 8.7      | 0.0  | 0.0       | 441.53 | 441.53 | 0.0        | 441.53 | 0.0         |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 437.15        | 4.37     | 0.0  | 0.0       | 441.52 | 250.0  | 250.0      | 0.0    | 191.52      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1200.0        | 23.07    | 0.0  | 0.0       | 1223.07 | 1031.55 | 590.02     | 441.53 | 191.52      |
#    --- Create chargeback ---
    When Admin sets the business date to "17 March 2025"
    And Admin runs inline COB job for Loan
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 250 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 437.15          | 432.83        | 8.7      | 0.0  | 0.0       | 441.53 | 441.53 | 0.0        | 441.53 | 0.0         |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 687.15        | 4.37     | 0.0  | 0.0       | 691.52 | 250.0  | 250.0      | 0.0    | 441.52      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1450.0        | 23.07    | 0.0  | 0.0       | 1473.07 | 1031.55 | 590.02     | 441.53 | 441.52      |
#    --- Make repayment with amount to close the loan ---
    When Admin sets the business date to "18 March 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "18 March 2025" with 441.52 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan has 0 outstanding amount
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 437.15          | 432.83        | 8.7      | 0.0  | 0.0       | 441.53 | 441.53 | 0.0        | 441.53 | 0.0         |
      | 3  | 31   | 01 April 2025    | 18 March 2025   | 0.0             | 687.15        | 4.37     | 0.0  | 0.0       | 691.52 | 691.52 | 691.52     | 0.0    | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1450.0        | 23.07    | 0.0  | 0.0       | 1473.07 | 1473.07 | 1031.54    | 441.53 | 0.0         |
#    --- Undo last payment ---
    When Admin sets the business date to "19 March 2025"
    And Admin runs inline COB job for Loan
    And Customer undo "1"th "Repayment" transaction made on "18 March 2025"
    Then Loan status will be "ACTIVE"
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 437.15          | 432.83        | 8.7      | 0.0  | 0.0       | 441.53 | 441.53 | 0.0        | 441.53 | 0.0         |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 687.15        | 4.37     | 0.0  | 0.0       | 691.52 | 250.0  | 250.0      | 0.0    | 441.52      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1450.0        | 23.07    | 0.0  | 0.0       | 1473.07 | 1031.55 | 590.02     | 441.53 | 441.52      |
#    --- Make repayment with amount that will overpay the loan with half of first repayment ---
    When Customer makes "AUTOPAY" repayment on "19 March 2025" with 611.53 EUR transaction amount
    Then Loan status will be "OVERPAID"
    And Loan has 170.01 overpaid amount
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 437.15          | 432.83        | 8.7      | 0.0  | 0.0       | 441.53 | 441.53 | 0.0        | 441.53 | 0.0         |
      | 3  | 31   | 01 April 2025    | 19 March 2025   | 0.0             | 687.15        | 4.37     | 0.0  | 0.0       | 691.52 | 691.52 | 691.52     | 0.0    | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1450.0        | 23.07    | 0.0  | 0.0       | 1473.07 | 1473.07 | 1031.54    | 441.53 | 0.0         |
#    --- Make credit balance refund ---
    When Admin makes Credit Balance Refund transaction on "19 March 2025" with 170.01 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan has 0 outstanding amount
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 437.15          | 432.83        | 8.7      | 0.0  | 0.0       | 441.53 | 441.53 | 0.0        | 441.53 | 0.0         |
      | 3  | 31   | 01 April 2025    | 19 March 2025   | 0.0             | 687.15        | 4.37     | 0.0  | 0.0       | 691.52 | 691.52 | 691.52     | 0.0    | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1450.0        | 23.07    | 0.0  | 0.0       | 1473.07 | 1473.07 | 1031.54    | 441.53 | 0.0         |
#    --- Make chargeback for first repayment ---
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 340.02 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0    |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02  | 340.02 | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0     | 0.0    |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 437.15          | 432.83        | 8.7      | 0.0  | 0.0       | 441.53  | 441.53 | 0.0        | 441.53 | 0.0         |
      | 3  | 31   | 01 April 2025    |                 | 0.0             | 1027.17       | 4.37     | 0.0  | 0.0       | 1031.54 | 691.52 | 691.52     | 0.0    | 340.02      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1790.02       | 23.07    | 0.0  | 0.0       | 1813.09 | 1473.07 | 1031.54    | 441.53 | 340.02      |
#    --- Make repayment to close the loan ---
    When Customer makes "AUTOPAY" repayment on "19 March 2025" with 340.02 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    And Loan has 0 outstanding amount
    And Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0     | 0.0     |            |        |             |
      | 1  | 31   | 01 February 2025 | 15 January 2025 | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02  | 340.02  | 340.02     | 0.0    | 0.0         |
      |    |      | 15 February 2025 |                 | 200.0           |               |          | 0.0  |           | 0.0     | 0.0     |            |        |             |
      | 2  | 28   | 01 March 2025    | 15 March 2025   | 437.15          | 432.83        | 8.7      | 0.0  | 0.0       | 441.53  | 441.53  | 0.0        | 441.53 | 0.0         |
      | 3  | 31   | 01 April 2025    | 19 March 2025   | 0.0             | 1027.17       | 4.37     | 0.0  | 0.0       | 1031.54 | 1031.54 | 1031.54    | 0.0    | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late   | Outstanding |
      | 1790.02       | 23.07    | 0.0  | 0.0       | 1813.09 | 1813.09 | 1371.56    | 441.53 | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement           | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 02 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 January 2025  | Repayment              | 340.02 | 330.02    | 10.0     | 0.0  | 0.0       | 669.98       | false    | false    |
      | 16 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 30 January 2025  | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 31 January 2025  | Accrual                | 0.33   | 0.0       | 0.33     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 February 2025 | Accrual                | 0.23   | 0.0       | 0.23     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 February 2025 | Accrual                | 0.24   | 0.0       | 0.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 February 2025 | Disbursement           | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 869.98       | false    | false    |
      | 15 February 2025 | Accrual                | 1.24   | 0.0       | 1.24     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 20 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 21 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 22 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 23 February 2025 | Accrual                | 0.32   | 0.0       | 0.32     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 27 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 28 February 2025 | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2025    | Accrual                | 0.31   | 0.0       | 0.31     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 02 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 03 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 04 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 05 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 06 March 2025    | Accrual                | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 09 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 11 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 12 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 13 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 14 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2025    | Repayment              | 441.53 | 432.83    | 8.7      | 0.0  | 0.0       | 437.15       | false    | false    |
      | 15 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 16 March 2025    | Merchant Issued Refund | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 187.15       | false    | false    |
      | 16 March 2025    | Accrual                | 0.15   | 0.0       | 0.15     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 17 March 2025    | Chargeback             | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 437.15       | false    | false    |
      | 17 March 2025    | Accrual                | 0.14   | 0.0       | 0.14     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 18 March 2025    | Repayment              | 441.52 | 437.15    | 4.37     | 0.0  | 0.0       | 0.0          | true     | false    |
      | 18 March 2025    | Accrual                | 2.11   | 0.0       | 2.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2025    | Repayment              | 611.53 | 437.15    | 4.37     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2025    | Credit Balance Refund  | 170.01 | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 19 March 2025    | Chargeback             | 340.02 | 340.02    | 0.0      | 0.0  | 0.0       | 340.02       | false    | false    |
      | 19 March 2025    | Repayment              | 340.02 | 340.02    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |

  @TestRailId:C115427 @InterestChargedFromIdealDisbursementDate
  Scenario: Verify first period interest of cumulative loan with clamped first due date - UC1: disbursed 31 January 2025, due 28 February 2025, 28 days of interest instead of 31
    Given Global configuration "interest-charged-from-date-same-as-disbursal-date" is disabled
    When Admin sets the business date to "31 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY | 31 January 2025   | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 28   | 28 February 2025 |           | 6693.05         | 3306.95       | 92.05    | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 2  | 31   | 31 March 2025    |           | 3362.26         | 3330.79       | 68.21    | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 3  | 30   | 30 April 2025    |           | 0.0             | 3362.26       | 33.16    | 0.0  | 0.0       | 3395.42 | 0.0  | 0.0        | 0.0  | 3395.42     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 193.42   | 0.0  | 0.0       | 10193.42 | 0.0  | 0.0        | 0.0  | 10193.42    |
    Then Global configuration "interest-charged-from-date-same-as-disbursal-date" is enabled

  @TestRailId:C115428 @InterestChargedFromIdealDisbursementDate
  Scenario: Verify first period interest of cumulative loan with clamped first due date - UC2: disbursed 30 January 2025, due 28 February 2025, 29 days of interest instead of 31
    Given Global configuration "interest-charged-from-date-same-as-disbursal-date" is disabled
    When Admin sets the business date to "30 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY | 30 January 2025   | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "30 January 2025" with "10000" amount and expected disbursement date on "30 January 2025"
    And Admin successfully disburse the loan on "30 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 30 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 29   | 28 February 2025 |           | 6696.34         | 3303.66       | 95.34    | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 2  | 30   | 30 March 2025    |           | 3363.39         | 3332.95       | 66.05    | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 3  | 31   | 30 April 2025    |           | 0.0             | 3363.39       | 34.28    | 0.0  | 0.0       | 3397.67 | 0.0  | 0.0        | 0.0  | 3397.67     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 195.67   | 0.0  | 0.0       | 10195.67 | 0.0  | 0.0        | 0.0  | 10195.67    |
    Then Global configuration "interest-charged-from-date-same-as-disbursal-date" is enabled

  @TestRailId:C115430 @InterestChargedFromIdealDisbursementDate
  Scenario: Verify first period interest of cumulative loan with clamped first due date - UC3: disbursed 29 January 2025, due 28 February 2025, 30 days of interest instead of 31
    Given Global configuration "interest-charged-from-date-same-as-disbursal-date" is disabled
    When Admin sets the business date to "29 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY | 29 January 2025   | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "29 January 2025" with "10000" amount and expected disbursement date on "29 January 2025"
    And Admin successfully disburse the loan on "29 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 29 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 28 February 2025 |           | 6699.63         | 3300.37       | 98.63    | 0.0  | 0.0       | 3399.0 | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 2  | 29   | 29 March 2025    |           | 3364.51         | 3335.12       | 63.88    | 0.0  | 0.0       | 3399.0 | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 3  | 31   | 29 April 2025    |           | 0.0             | 3364.51       | 34.29    | 0.0  | 0.0       | 3398.8 | 0.0  | 0.0        | 0.0  | 3398.8      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 10000.0       | 196.8    | 0.0  | 0.0       | 10196.8 | 0.0  | 0.0        | 0.0  | 10196.8     |
    Then Global configuration "interest-charged-from-date-same-as-disbursal-date" is enabled

  @TestRailId:C115431 @InterestChargedFromIdealDisbursementDate
  Scenario: Verify first period interest of cumulative loan with clamped first due date - UC4: disbursed 28 January 2025 (no clamp), 31 days of interest (control)
    Given Global configuration "interest-charged-from-date-same-as-disbursal-date" is disabled
    When Admin sets the business date to "28 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY | 28 January 2025   | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "28 January 2025" with "10000" amount and expected disbursement date on "28 January 2025"
    And Admin successfully disburse the loan on "28 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 28 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 31   | 28 February 2025 |           | 6702.92         | 3297.08       | 101.92   | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 2  | 28   | 28 March 2025    |           | 3365.62         | 3337.3        | 61.7     | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 3  | 31   | 28 April 2025    |           | 0.0             | 3365.62       | 34.3     | 0.0  | 0.0       | 3399.92 | 0.0  | 0.0        | 0.0  | 3399.92     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 197.92   | 0.0  | 0.0       | 10197.92 | 0.0  | 0.0        | 0.0  | 10197.92    |
    Then Global configuration "interest-charged-from-date-same-as-disbursal-date" is enabled

  @TestRailId:C115432 @InterestChargedFromIdealDisbursementDate
  Scenario: Verify first period interest of cumulative loan with clamped first due date - UC5: disbursed 31 December 2024, 2-month period, due 28 February 2025, 59 days of interest instead of 61
    Given Global configuration "interest-charged-from-date-same-as-disbursal-date" is disabled
    When Admin sets the business date to "31 December 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY | 31 December 2024  | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 2              | MONTHS                 | 2                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "31 December 2024" with "10000" amount and expected disbursement date on "31 December 2024"
    And Admin successfully disburse the loan on "31 December 2024" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 31 December 2024 |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 59   | 28 February 2025 |           | 5045.97         | 4954.03       | 193.97   | 0.0  | 0.0       | 5148.0  | 0.0  | 0.0        | 0.0  | 5148.0      |
      | 2  | 61   | 30 April 2025    |           | 0.0             | 5045.97       | 101.2    | 0.0  | 0.0       | 5147.17 | 0.0  | 0.0        | 0.0  | 5147.17     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 295.17   | 0.0  | 0.0       | 10295.17 | 0.0  | 0.0        | 0.0  | 10295.17    |
    Then Global configuration "interest-charged-from-date-same-as-disbursal-date" is enabled

  @TestRailId:C115433 @InterestChargedFromIdealDisbursementDate
  Scenario: Verify first period interest of cumulative loan with clamped first due date - UC6: disbursed 31 January 2024 (leap year), due 29 February 2024, 29 days of interest instead of 31
    Given Global configuration "interest-charged-from-date-same-as-disbursal-date" is disabled
    When Admin sets the business date to "31 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY | 31 January 2024   | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "31 January 2024" with "10000" amount and expected disbursement date on "31 January 2024"
    And Admin successfully disburse the loan on "31 January 2024" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2024  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 29   | 29 February 2024 |           | 6696.34         | 3303.66       | 95.34    | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 2  | 31   | 31 March 2024    |           | 3365.59         | 3330.75       | 68.25    | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 3  | 30   | 30 April 2024    |           | 0.0             | 3365.59       | 33.19    | 0.0  | 0.0       | 3398.78 | 0.0  | 0.0        | 0.0  | 3398.78     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 196.78   | 0.0  | 0.0       | 10196.78 | 0.0  | 0.0        | 0.0  | 10196.78    |
    Then Global configuration "interest-charged-from-date-same-as-disbursal-date" is enabled

  @TestRailId:C115434 @InterestChargedFromIdealDisbursementDate
  Scenario: Verify first period interest of cumulative loan with clamped first due date - UC7: expected disbursement 15 January 2025, actual 31 January 2025, regenerated schedule charges 28 days
    Given Global configuration "interest-charged-from-date-same-as-disbursal-date" is disabled
    When Admin sets the business date to "31 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY | 15 January 2025   | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "15 January 2025" with "10000" amount and expected disbursement date on "15 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 28   | 28 February 2025 |           | 6693.05         | 3306.95       | 92.05    | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 2  | 31   | 31 March 2025    |           | 3362.26         | 3330.79       | 68.21    | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 3  | 30   | 30 April 2025    |           | 0.0             | 3362.26       | 33.16    | 0.0  | 0.0       | 3395.42 | 0.0  | 0.0        | 0.0  | 3395.42     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 193.42   | 0.0  | 0.0       | 10193.42 | 0.0  | 0.0        | 0.0  | 10193.42    |
    Then Global configuration "interest-charged-from-date-same-as-disbursal-date" is enabled

  @TestRailId:C115435 @InterestChargedFromIdealDisbursementDate
  Scenario: Verify first period interest of cumulative loan with clamped first due date - UC8: disbursed 31 January 2025, undo disbursal and re-disburse, 28 days of interest kept
    Given Global configuration "interest-charged-from-date-same-as-disbursal-date" is disabled
    When Admin sets the business date to "31 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY | 31 January 2025   | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    And Admin successfully undo disbursal
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 28   | 28 February 2025 |           | 6693.05         | 3306.95       | 92.05    | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 2  | 31   | 31 March 2025    |           | 3362.26         | 3330.79       | 68.21    | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 3  | 30   | 30 April 2025    |           | 0.0             | 3362.26       | 33.16    | 0.0  | 0.0       | 3395.42 | 0.0  | 0.0        | 0.0  | 3395.42     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 193.42   | 0.0  | 0.0       | 10193.42 | 0.0  | 0.0        | 0.0  | 10193.42    |
    Then Global configuration "interest-charged-from-date-same-as-disbursal-date" is enabled

  @TestRailId:C115436 @InterestChargedFromIdealDisbursementDate
  Scenario: Verify first period interest of cumulative loan with clamped first due date - UC9: interest recalculation product disbursed 31 January 2025, 28 days of interest instead of 31
    Given Global configuration "interest-charged-from-date-same-as-disbursal-date" is disabled
    When Admin sets the business date to "31 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE | 31 January 2025   | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 28   | 28 February 2025 |           | 6693.05         | 3306.95       | 92.05    | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 2  | 31   | 31 March 2025    |           | 3362.26         | 3330.79       | 68.21    | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 3  | 30   | 30 April 2025    |           | 0.0             | 3362.26       | 33.16    | 0.0  | 0.0       | 3395.42 | 0.0  | 0.0        | 0.0  | 3395.42     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 193.42   | 0.0  | 0.0       | 10193.42 | 0.0  | 0.0        | 0.0  | 10193.42    |
    Then Global configuration "interest-charged-from-date-same-as-disbursal-date" is enabled

  @TestRailId:C115437 @InterestChargedFromIdealDisbursementDate
  Scenario: Verify first period interest of cumulative loan with clamped first due date - UC10: interest calculation period same as repayment, disbursed 31 January 2025, whole-period interest unaffected (control)
    Given Global configuration "interest-charged-from-date-same-as-disbursal-date" is disabled
    When Admin sets the business date to "31 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_SAME_AS_PAYMENT | 31 January 2025   | 10000          | 12                     | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 28   | 28 February 2025 |           | 6700.0          | 3300.0        | 100.0    | 0.0  | 0.0       | 3400.0  | 0.0  | 0.0        | 0.0  | 3400.0      |
      | 2  | 31   | 31 March 2025    |           | 3367.0          | 3333.0        | 67.0     | 0.0  | 0.0       | 3400.0  | 0.0  | 0.0        | 0.0  | 3400.0      |
      | 3  | 30   | 30 April 2025    |           | 0.0             | 3367.0        | 33.67    | 0.0  | 0.0       | 3400.67 | 0.0  | 0.0        | 0.0  | 3400.67     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 200.67   | 0.0  | 0.0       | 10200.67 | 0.0  | 0.0        | 0.0  | 10200.67    |
    Then Global configuration "interest-charged-from-date-same-as-disbursal-date" is enabled

  @TestRailId:C115438 @InterestChargedFromIdealDisbursementDate
  Scenario: Verify first period interest of cumulative loan with clamped first due date - UC11: 30/360 product disbursed 31 January 2025, whole-period interest unaffected (control)
    Given Global configuration "interest-charged-from-date-same-as-disbursal-date" is disabled
    When Admin sets the business date to "31 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_360_30_DECLINING_BALANCE_PERIOD_DAILY_INT_RECALC | 31 January 2025   | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 28   | 28 February 2025 |           | 6700.0          | 3300.0        | 100.0    | 0.0  | 0.0       | 3400.0  | 0.0  | 0.0        | 0.0  | 3400.0      |
      | 2  | 31   | 31 March 2025    |           | 3367.0          | 3333.0        | 67.0     | 0.0  | 0.0       | 3400.0  | 0.0  | 0.0        | 0.0  | 3400.0      |
      | 3  | 30   | 30 April 2025    |           | 0.0             | 3367.0        | 33.67    | 0.0  | 0.0       | 3400.67 | 0.0  | 0.0        | 0.0  | 3400.67     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 200.67   | 0.0  | 0.0       | 10200.67 | 0.0  | 0.0        | 0.0  | 10200.67    |
    Then Global configuration "interest-charged-from-date-same-as-disbursal-date" is enabled

  @TestRailId:C115439
  Scenario: Verify first period interest of cumulative loan with clamped first due date - UC12: global config interest-charged-from-date-same-as-disbursal-date enabled, disbursed 31 January 2025, 28 days of interest (control)
    When Admin sets the business date to "31 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY | 31 January 2025   | 10000          | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "31 January 2025" with "10000" amount and expected disbursement date on "31 January 2025"
    And Admin successfully disburse the loan on "31 January 2025" with "10000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      |    |      | 31 January 2025  |           | 10000.0         |               |          | 0.0  |           | 0.0     | 0.0  |            |      |             |
      | 1  | 28   | 28 February 2025 |           | 6693.05         | 3306.95       | 92.05    | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 2  | 31   | 31 March 2025    |           | 3362.26         | 3330.79       | 68.21    | 0.0  | 0.0       | 3399.0  | 0.0  | 0.0        | 0.0  | 3399.0      |
      | 3  | 30   | 30 April 2025    |           | 0.0             | 3362.26       | 33.16    | 0.0  | 0.0       | 3395.42 | 0.0  | 0.0        | 0.0  | 3395.42     |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 10000.0       | 193.42   | 0.0  | 0.0       | 10193.42 | 0.0  | 0.0        | 0.0  | 10193.42    |
