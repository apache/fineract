@Repayment
Feature: LoanRepayment - Part4

  @TestRailId:C4353
  Scenario: Verify the loan creation with total disbursement amount less then 1 for progressive loan - UC2
    When Admin sets the business date to "26 October 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | 26 October 2025   | 1              | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "26 October 2025" with "1" amount and expected disbursement date on "26 October 2025"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      |    |      | 26 October 2025  |           | 1.0             |               |          | 0.0  |           | 0.0 |      |            |      | 0.0         |
      | 1  | 31   | 26 November 2025 |           | 0.0             | 1.0           | 0.0      | 0.0  | 0.0       | 1.0 | 0.0  | 0.0        | 0.0  | 1.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      | 1.0           | 0.0      | 0.0  | 0.0       | 1.0 | 0.0  | 0.0        | 0.0  | 1.0         |
    When Admin successfully disburse the loan on "26 October 2025" with "0.4" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      |    |      | 26 October 2025  |           | 0.4             |               |          | 0.0  |           | 0.0 | 0.0  |            |      |             |
      | 1  | 31   | 26 November 2025 |           | 0.0             | 0.4           | 0.0      | 0.0  | 0.0       | 0.4 | 0.0  | 0.0        | 0.0  | 0.4         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      | 0.4           | 0.0      | 0.0  | 0.0       | 0.4 | 0.0  | 0.0        | 0.0  | 0.4         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 26 October 2025  | Disbursement     | 0.4    | 0.0       | 0.0      | 0.0  | 0.0       | 0.4          | false    | false    |
    When Admin sets the business date to "27 October 2025"
    When Loan Pay-off is made on "27 October 2025"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      |    |      | 26 October 2025  |                 | 0.4             |               |          | 0.0  |           | 0.0 | 0.0  |            |      |             |
      | 1  | 31   | 26 November 2025 | 27 October 2025 | 0.0             | 0.4           | 0.0      | 0.0  | 0.0       | 0.4 | 0.4  | 0.4        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      | 0.4           | 0.0      | 0.0  | 0.0       | 0.4 | 0.4  | 0.4        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 26 October 2025  | Disbursement     | 0.4    | 0.0       | 0.0      | 0.0  | 0.0       | 0.4          | false    | false    |
      | 27 October 2025  | Repayment        | 0.4    | 0.4       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4354
  Scenario: Verify the loan creation with total disbursement amount less then 1 for progressive loan - 2 repayments
    When Admin sets the business date to "26 October 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | 26 October 2025   | 1              | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 2                 | MONTHS                | 1              | MONTHS                 | 2                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "26 October 2025" with "1" amount and expected disbursement date on "26 October 2025"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      |    |      | 26 October 2025  |           | 1.0             |               |          | 0.0  |           | 0.0 |      |            |      | 0.0         |
      | 1  | 31   | 26 November 2025 |           | 0.0             | 1.0           | 0.0      | 0.0  | 0.0       | 1.0 | 0.0  | 0.0        | 0.0  | 1.0         |
      | 2  | 30   | 26 December 2025 |           | 0.0             | 0.0           | 0.0      | 0.0  | 0.0       | 0.0 | 0.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      | 1.0           | 0.0      | 0.0  | 0.0       | 1.0 | 0.0  | 0.0        | 0.0  | 1.0         |
    When Admin successfully disburse the loan on "26 October 2025" with "0.4" EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      |    |      | 26 October 2025  |                 | 0.4             |               |          | 0.0  |           | 0.0 | 0.0  |            |      |             |
      | 1  | 31   | 26 November 2025 |                 | 0.0             | 0.4           | 0.0      | 0.0  | 0.0       | 0.4 | 0.0  | 0.0        | 0.0  | 0.4         |
      | 2  | 30   | 26 December 2025 | 26 October 2025 | 0.0             | 0.0           | 0.0      | 0.0  | 0.0       | 0.0 | 0.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      | 0.4           | 0.0      | 0.0  | 0.0       | 0.4 | 0.0  | 0.0        | 0.0  | 0.4         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 26 October 2025  | Disbursement     | 0.4    | 0.0       | 0.0      | 0.0  | 0.0       | 0.4          | false    | false    |
    When Admin sets the business date to "27 October 2025"
    When Loan Pay-off is made on "27 October 2025"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      |    |      | 26 October 2025  |                 | 0.4             |               |          | 0.0  |           | 0.0 | 0.0  |            |      |             |
      | 1  | 31   | 26 November 2025 | 27 October 2025 | 0.0             | 0.4           | 0.0      | 0.0  | 0.0       | 0.4 | 0.4  | 0.4        | 0.0  | 0.0         |
      | 2  | 30   | 26 December 2025 | 26 October 2025 | 0.0             | 0.0           | 0.0      | 0.0  | 0.0       | 0.0 | 0.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due | Paid | In advance | Late | Outstanding |
      | 0.4           | 0.0      | 0.0  | 0.0       | 0.4 | 0.4  | 0.4        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 26 October 2025  | Disbursement     | 0.4    | 0.0       | 0.0      | 0.0  | 0.0       | 0.4          | false    | false    |
      | 27 October 2025  | Repayment        | 0.4    | 0.4       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4648
  Scenario: Verify repayment undo with linked chargeback fails with proper error
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_PENALTY_FEE_PRINCIPAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
    When Admin sets the business date to "15 March 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount
    Then Customer undo "1"th transaction made on "01 February 2024" results a 403 error and "update not allowed as loan transaction is linked to other transactions" error message
    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4683 @AdvancedPaymentAllocation @ProgressiveLoanSchedule
  Scenario: Verify AdvancedPaymentAllocation behaviour: loanScheduleProcessingType-vertical prepayment with NEXT_INSTALLMENT strategy
    When Admin sets the business date to "23 February 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC" loan product "REPAYMENT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC | 01 January 2026   | 25000000       | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "25000000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "25000000" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest  | Fees | Penalties | Due        | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |           | 25000000.0      |               |           | 0.0  |           | 0.0        | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2026  |           | 23034153.81     | 1965846.19    | 254794.52 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 2  | 28   | 01 March 2026     |           | 21039772.25     | 1994381.56    | 226259.15 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 3  | 31   | 01 April 2026     |           | 19033564.29     | 2006207.96    | 214432.75 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 4  | 30   | 01 May 2026       |           | 17000651.89     | 2032912.4     | 187728.31 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 5  | 31   | 01 June 2026      |           | 14953278.1      | 2047373.79    | 173266.92 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 6  | 30   | 01 July 2026      |           | 12880121.78     | 2073156.32    | 147484.39 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 7  | 31   | 01 August 2026    |           | 10790752.45     | 2089369.33    | 131271.38 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 8  | 31   | 01 September 2026 |           | 8680088.72      | 2110663.73    | 109976.98 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 9  | 30   | 01 October 2026   |           | 6545059.84      | 2135028.88    | 85611.83  | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 10 | 31   | 01 November 2026  |           | 4391124.95      | 2153934.89    | 66705.82  | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 11 | 30   | 01 December 2026  |           | 2213793.97      | 2177330.98    | 43309.73  | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 12 | 31   | 01 January 2027   |           | 0.0             | 2213793.97    | 22562.5   | 0.0  | 0.0       | 2236356.47 | 0.0  | 0.0        | 0.0  | 2236356.47  |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest   | Fees | Penalties | Due         | Paid | In advance | Late | Outstanding |
      | 25000000.0    | 1663404.28 | 0.0  | 0.0       | 26663404.28 | 0.0  | 0.0        | 0.0  | 26663404.28 |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount     | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 25000000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 25000000.0   |
    When Loan Pay-off is made on "23 February 2026"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest  | Fees | Penalties | Due        | Paid       | In advance | Late       | Outstanding |
      |    |      | 01 January 2026   |                  | 25000000.0      |               |           | 0.0  |           | 0.0        | 0.0        |            |            |             |
      | 1  | 31   | 01 February 2026  | 23 February 2026 | 23034153.81     | 1965846.19    | 254794.52 | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 0.0        | 2220640.71 | 0.0         |
      | 2  | 28   | 01 March 2026     | 23 February 2026 | 20813513.1      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 3  | 31   | 01 April 2026     | 23 February 2026 | 18592872.39     | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 4  | 30   | 01 May 2026       | 23 February 2026 | 16372231.68     | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 5  | 31   | 01 June 2026      | 23 February 2026 | 14151590.97     | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 6  | 30   | 01 July 2026      | 23 February 2026 | 11930950.26     | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 7  | 31   | 01 August 2026    | 23 February 2026 | 9710309.55      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 8  | 31   | 01 September 2026 | 23 February 2026 | 7489668.84      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 9  | 30   | 01 October 2026   | 23 February 2026 | 5269028.13      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 10 | 31   | 01 November 2026  | 23 February 2026 | 3048387.42      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 11 | 30   | 01 December 2026  | 23 February 2026 | 827746.71       | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 12 | 31   | 01 January 2027   | 23 February 2026 | 0.0             | 827746.71     | 180821.92 | 0.0  | 0.0       | 1008568.63 | 1008568.63 | 1008568.63 | 0.0        | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest  | Fees | Penalties | Due         | Paid        | In advance  | Late       | Outstanding |
      | 25000000.0    | 435616.44 | 0.0  | 0.0       | 25435616.44 | 25435616.44 | 23214975.73 | 2220640.71 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount      | Principal  | Interest  | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 25000000.0  | 0.0        | 0.0       | 0.0  | 0.0       | 25000000.0   |
      | 23 February 2026 | Repayment        | 25435616.44 | 25000000.0 | 435616.44 | 0.0  | 0.0       | 0.0          |
      | 23 February 2026 | Accrual          | 435616.44   | 0.0        | 435616.44 | 0.0  | 0.0       | 0.0          |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set "LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC" loan product "REPAYMENT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C4684 @AdvancedPaymentAllocation @ProgressiveLoanSchedule
  Scenario: Verify AdvancedPaymentAllocation behaviour: loanScheduleProcessingType-vertical prepayment with LAST_INSTALLMENT strategy
    When Admin sets the business date to "23 February 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC" loan product "REPAYMENT" transaction type to "LAST_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC | 01 January 2026   | 25000000       | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "25000000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "25000000" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest  | Fees | Penalties | Due        | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |           | 25000000.0      |               |           | 0.0  |           | 0.0        | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2026  |           | 23034153.81     | 1965846.19    | 254794.52 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 2  | 28   | 01 March 2026     |           | 21039772.25     | 1994381.56    | 226259.15 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 3  | 31   | 01 April 2026     |           | 19033564.29     | 2006207.96    | 214432.75 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 4  | 30   | 01 May 2026       |           | 17000651.89     | 2032912.4     | 187728.31 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 5  | 31   | 01 June 2026      |           | 14953278.1      | 2047373.79    | 173266.92 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 6  | 30   | 01 July 2026      |           | 12880121.78     | 2073156.32    | 147484.39 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 7  | 31   | 01 August 2026    |           | 10790752.45     | 2089369.33    | 131271.38 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 8  | 31   | 01 September 2026 |           | 8680088.72      | 2110663.73    | 109976.98 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 9  | 30   | 01 October 2026   |           | 6545059.84      | 2135028.88    | 85611.83  | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 10 | 31   | 01 November 2026  |           | 4391124.95      | 2153934.89    | 66705.82  | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 11 | 30   | 01 December 2026  |           | 2213793.97      | 2177330.98    | 43309.73  | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 12 | 31   | 01 January 2027   |           | 0.0             | 2213793.97    | 22562.5   | 0.0  | 0.0       | 2236356.47 | 0.0  | 0.0        | 0.0  | 2236356.47  |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest   | Fees | Penalties | Due         | Paid | In advance | Late | Outstanding |
      | 25000000.0    | 1663404.28 | 0.0  | 0.0       | 26663404.28 | 0.0  | 0.0        | 0.0  | 26663404.28 |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount     | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 25000000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 25000000.0   |
    When Loan Pay-off is made on "23 February 2026"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest  | Fees | Penalties | Due        | Paid       | In advance | Late       | Outstanding |
      |    |      | 01 January 2026   |                  | 25000000.0      |               |           | 0.0  |           | 0.0        | 0.0        |            |            |             |
      | 1  | 31   | 01 February 2026  | 23 February 2026 | 23034153.81     | 1965846.19    | 254794.52 | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 0.0        | 2220640.71 | 0.0         |
      | 2  | 28   | 01 March 2026     | 23 February 2026 | 22206407.14     | 827746.67     | 180821.92 | 0.0  | 0.0       | 1008568.59 | 1008568.59 | 1008568.59 | 0.0        | 0.0         |
      | 3  | 31   | 01 April 2026     | 23 February 2026 | 19985766.43     | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 4  | 30   | 01 May 2026       | 23 February 2026 | 17765125.72     | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 5  | 31   | 01 June 2026      | 23 February 2026 | 15544485.01     | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 6  | 30   | 01 July 2026      | 23 February 2026 | 13323844.3      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 7  | 31   | 01 August 2026    | 23 February 2026 | 11103203.59     | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 8  | 31   | 01 September 2026 | 23 February 2026 | 8882562.88      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 9  | 30   | 01 October 2026   | 23 February 2026 | 6661922.17      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 10 | 31   | 01 November 2026  | 23 February 2026 | 4441281.46      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 11 | 30   | 01 December 2026  | 23 February 2026 | 2220640.75      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 12 | 31   | 01 January 2027   | 23 February 2026 | 0.0             | 2220640.75    | 0.0       | 0.0  | 0.0       | 2220640.75 | 2220640.75 | 2220640.75 | 0.0        | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest  | Fees | Penalties | Due         | Paid        | In advance  | Late       | Outstanding |
      | 25000000.0    | 435616.44 | 0.0  | 0.0       | 25435616.44 | 25435616.44 | 23214975.73 | 2220640.71 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount      | Principal  | Interest  | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 25000000.0  | 0.0        | 0.0       | 0.0  | 0.0       | 25000000.0   |
      | 23 February 2026 | Repayment        | 25435616.44 | 25000000.0 | 435616.44 | 0.0  | 0.0       | 0.0          |
      | 23 February 2026 | Accrual          | 435616.44   | 0.0        | 435616.44 | 0.0  | 0.0       | 0.0          |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set "LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC" loan product "REPAYMENT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C4685 @AdvancedPaymentAllocation @ProgressiveLoanSchedule
  Scenario: Verify AdvancedPaymentAllocation behaviour: loanScheduleProcessingType-vertical prepayment with NEXT_LAST_INSTALLMENT strategy
    When Admin sets the business date to "23 February 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC" loan product "REPAYMENT" transaction type to "NEXT_LAST_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC | 01 January 2026   | 25000000       | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "25000000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "25000000" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest  | Fees | Penalties | Due        | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2026   |           | 25000000.0      |               |           | 0.0  |           | 0.0        | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2026  |           | 23034153.81     | 1965846.19    | 254794.52 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 2  | 28   | 01 March 2026     |           | 21039772.25     | 1994381.56    | 226259.15 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 3  | 31   | 01 April 2026     |           | 19033564.29     | 2006207.96    | 214432.75 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 4  | 30   | 01 May 2026       |           | 17000651.89     | 2032912.4     | 187728.31 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 5  | 31   | 01 June 2026      |           | 14953278.1      | 2047373.79    | 173266.92 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 6  | 30   | 01 July 2026      |           | 12880121.78     | 2073156.32    | 147484.39 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 7  | 31   | 01 August 2026    |           | 10790752.45     | 2089369.33    | 131271.38 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 8  | 31   | 01 September 2026 |           | 8680088.72      | 2110663.73    | 109976.98 | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 9  | 30   | 01 October 2026   |           | 6545059.84      | 2135028.88    | 85611.83  | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 10 | 31   | 01 November 2026  |           | 4391124.95      | 2153934.89    | 66705.82  | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 11 | 30   | 01 December 2026  |           | 2213793.97      | 2177330.98    | 43309.73  | 0.0  | 0.0       | 2220640.71 | 0.0  | 0.0        | 0.0  | 2220640.71  |
      | 12 | 31   | 01 January 2027   |           | 0.0             | 2213793.97    | 22562.5   | 0.0  | 0.0       | 2236356.47 | 0.0  | 0.0        | 0.0  | 2236356.47  |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest   | Fees | Penalties | Due         | Paid | In advance | Late | Outstanding |
      | 25000000.0    | 1663404.28 | 0.0  | 0.0       | 26663404.28 | 0.0  | 0.0        | 0.0  | 26663404.28 |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount     | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 25000000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 25000000.0   |
    When Loan Pay-off is made on "23 February 2026"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest  | Fees | Penalties | Due        | Paid       | In advance | Late       | Outstanding |
      |    |      | 01 January 2026   |                  | 25000000.0      |               |           | 0.0  |           | 0.0        | 0.0        |            |            |             |
      | 1  | 31   | 01 February 2026  | 23 February 2026 | 23034153.81     | 1965846.19    | 254794.52 | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 0.0        | 2220640.71 | 0.0         |
      | 2  | 28   | 01 March 2026     | 23 February 2026 | 20813513.1      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 3  | 31   | 01 April 2026     | 23 February 2026 | 19985766.43     | 827746.67     | 180821.92 | 0.0  | 0.0       | 1008568.59 | 1008568.59 | 1008568.59 | 0.0        | 0.0         |
      | 4  | 30   | 01 May 2026       | 23 February 2026 | 17765125.72     | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 5  | 31   | 01 June 2026      | 23 February 2026 | 15544485.01     | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 6  | 30   | 01 July 2026      | 23 February 2026 | 13323844.3      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 7  | 31   | 01 August 2026    | 23 February 2026 | 11103203.59     | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 8  | 31   | 01 September 2026 | 23 February 2026 | 8882562.88      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 9  | 30   | 01 October 2026   | 23 February 2026 | 6661922.17      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 10 | 31   | 01 November 2026  | 23 February 2026 | 4441281.46      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 11 | 30   | 01 December 2026  | 23 February 2026 | 2220640.75      | 2220640.71    | 0.0       | 0.0  | 0.0       | 2220640.71 | 2220640.71 | 2220640.71 | 0.0        | 0.0         |
      | 12 | 31   | 01 January 2027   | 23 February 2026 | 0.0             | 2220640.75    | 0.0       | 0.0  | 0.0       | 2220640.75 | 2220640.75 | 2220640.75 | 0.0        | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest  | Fees | Penalties | Due         | Paid        | In advance  | Late       | Outstanding |
      | 25000000.0    | 435616.44 | 0.0  | 0.0       | 25435616.44 | 25435616.44 | 23214975.73 | 2220640.71 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount      | Principal  | Interest  | Fees | Penalties | Loan Balance |
      | 01 January 2026  | Disbursement     | 25000000.0  | 0.0        | 0.0       | 0.0  | 0.0       | 25000000.0   |
      | 23 February 2026 | Repayment        | 25435616.44 | 25000000.0 | 435616.44 | 0.0  | 0.0       | 0.0          |
      | 23 February 2026 | Accrual          | 435616.44   | 0.0        | 435616.44 | 0.0  | 0.0       | 0.0          |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set "LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC" loan product "REPAYMENT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C78844 @AdvancedPaymentAllocation
  Scenario: Verify installment due date logic for monthly installments - repayment start date type overridden on loan level by submitted on date
    When Admin sets the business date to "31 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | expected disbursement date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | Repayment start date type |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR | 31 January 2024   | 10 February 2024           | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | SUBMITTED_ON_DATE         |
    Then LoanDetails has repaymentStartDateType field with value: "SUBMITTED_ON_DATE"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 10 February 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 29   | 29 February 2024 |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 31 March 2024    |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 30 April 2024    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 31 May 2024      |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    And Admin successfully approves the loan on "31 January 2024" with "1000" amount and expected disbursement date on "11 February 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 11 February 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 29   | 29 February 2024 |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 31 March 2024    |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 30 April 2024    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 31 May 2024      |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    When Admin sets the business date to "12 February 2024"
    When Admin successfully disburse the loan on "12 February 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 12 February 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 29   | 29 February 2024 |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 31 March 2024    |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 30   | 30 April 2024    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 31 May 2024      |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |

  @TestRailId:C78845 @AdvancedPaymentAllocation
  Scenario: Verify installment due date logic for monthly installments - repayment start date type overridden on loan level by disbursement date
    When Admin sets the business date to "31 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                | submitted on date | expected disbursement date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | Repayment start date type |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_REPAYMENT_START_SUBMITTED | 31 January 2024   | 10 February 2024           | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | DISBURSEMENT_DATE         |
    Then LoanDetails has repaymentStartDateType field with value: "DISBURSEMENT_DATE"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 10 February 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 10 February 2024 |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 29   | 10 March 2024    |           | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 10 April 2024    |           | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 10 May 2024      |           | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 10 June 2024     |           | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0  | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    And Admin successfully approves the loan on "31 January 2024" with "1000" amount and expected disbursement date on "11 February 2024"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 11 February 2024 |           | 1000.0          |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 0    | 11 February 2024 |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 29   | 11 March 2024    |           | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 11 April 2024    |           | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 11 May 2024      |           | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0  | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 11 June 2024     |           | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0  | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    When Admin sets the business date to "12 February 2024"
    When Admin successfully disburse the loan on "12 February 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 12 February 2024 |                  | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 12 February 2024 | 12 February 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 12 March 2024    |                  | 562.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 3  | 31   | 12 April 2024    |                  | 374.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 4  | 30   | 12 May 2024      |                  | 186.0           | 188.0         | 0.0      | 0.0  | 0.0       | 188.0 | 0.0   | 0.0        | 0.0  | 188.0       |
      | 5  | 31   | 12 June 2024     |                  | 0.0             | 186.0         | 0.0      | 0.0  | 0.0       | 186.0 | 0.0   | 0.0        | 0.0  | 186.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |

  @TestRailId:CXXXX @AdvancedPaymentAllocation @ProgressiveLoanSchedule
  Scenario: Verify AdvancedPaymentAllocation behaviour: loanScheduleProcessingType-vertical two repayment and prepayment with NEXT_INSTALLMENT strategy
    When Admin sets the business date to "23 February 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC" loan product "REPAYMENT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC | 31 December 2024  | 424036.08      | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 36                | MONTHS                | 1              | MONTHS                 | 36                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "31 December 2024" with "424036.08" amount and expected disbursement date on "31 December 2024"
    When Admin successfully disburse the loan on "31 December 2024" with "424036.08" EUR transaction amount
    Then Loan Repayment schedule has 36 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      |    |      | 31 December 2024  |           | 424036.08       |               |          | 0.0  |           | 0.0      | 0.0  |            |      |             |
      | 1  | 31   | 31 January 2025   |           | 414277.37       | 9758.71       | 4321.68  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 2  | 28   | 28 February 2025  |           | 404100.44       | 10176.93      | 3903.46  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 3  | 31   | 31 March 2025     |           | 394341.73       | 9758.71       | 4321.68  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 4  | 30   | 30 April 2025     |           | 384443.61       | 9898.12       | 4182.27  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 5  | 31   | 31 May 2025       |           | 374684.9        | 9758.71       | 4321.68  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 6  | 30   | 30 June 2025      |           | 364786.78       | 9898.12       | 4182.27  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 7  | 31   | 31 July 2025      |           | 355028.07       | 9758.71       | 4321.68  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 8  | 31   | 31 August 2025    |           | 345269.36       | 9758.71       | 4321.68  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 9  | 30   | 30 September 2025 |           | 335371.24       | 9898.12       | 4182.27  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 10 | 31   | 31 October 2025   |           | 325612.53       | 9758.71       | 4321.68  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 11 | 30   | 30 November 2025  |           | 315714.41       | 9898.12       | 4182.27  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 12 | 31   | 31 December 2025  |           | 305955.7        | 9758.71       | 4321.68  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 13 | 31   | 31 January 2026   |           | 296196.99       | 9758.71       | 4321.68  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 14 | 28   | 28 February 2026  |           | 285809.91       | 10387.08      | 3693.31  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 15 | 31   | 31 March 2026     |           | 274642.43       | 11167.48      | 2912.91  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 16 | 30   | 30 April 2026     |           | 263270.84       | 11371.59      | 2708.8   | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 17 | 31   | 31 May 2026       |           | 251873.65       | 11397.19      | 2683.2   | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 18 | 30   | 30 June 2026      |           | 240277.49       | 11596.16      | 2484.23  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 19 | 31   | 31 July 2026      |           | 228645.96       | 11631.53      | 2448.86  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 20 | 31   | 31 August 2026    |           | 216895.88       | 11750.08      | 2330.31  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 21 | 30   | 30 September 2026 |           | 204954.74       | 11941.14      | 2139.25  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 22 | 31   | 31 October 2026   |           | 192963.2        | 11991.54      | 2088.85  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 23 | 30   | 30 November 2026  |           | 180786.01       | 12177.19      | 1903.2   | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 24 | 31   | 31 December 2026  |           | 168548.15       | 12237.86      | 1842.53  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 25 | 31   | 31 January 2027   |           | 156185.57       | 12362.58      | 1717.81  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 26 | 28   | 28 February 2027  |           | 143542.94       | 12642.63      | 1437.76  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 27 | 31   | 31 March 2027     |           | 130925.51       | 12617.43      | 1462.96  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 28 | 30   | 30 April 2027     |           | 118136.44       | 12789.07      | 1291.32  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 29 | 31   | 31 May 2027       |           | 105260.07       | 12876.37      | 1204.02  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 30 | 30   | 30 June 2027      |           | 92217.86        | 13042.21      | 1038.18  | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 31 | 31   | 31 July 2027      |           | 79077.33        | 13140.53      | 939.86   | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 32 | 31   | 31 August 2027    |           | 65802.88        | 13274.45      | 805.94   | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 33 | 30   | 30 September 2027 |           | 52371.5         | 13431.38      | 649.01   | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 34 | 31   | 31 October 2027   |           | 38824.87        | 13546.63      | 533.76   | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 35 | 30   | 30 November 2027  |           | 25127.41        | 13697.46      | 382.93   | 0.0  | 0.0       | 14080.39 | 0.0  | 0.0        | 0.0  | 14080.39    |
      | 36 | 31   | 31 December 2027  |           | 0.0             | 25127.41      | 256.09   | 0.0  | 0.0       | 25383.5  | 0.0  | 0.0        | 0.0  | 25383.5     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due       | Paid | In advance | Late | Outstanding |
      | 424036.08     | 94161.07 | 0.0  | 0.0       | 518197.15 | 0.0  | 0.0        | 0.0  | 518197.15   |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount    | Principal | Interest | Fees | Penalties | Loan Balance |
      | 31 December 2024 | Disbursement     | 424036.08 | 0.0       | 0.0      | 0.0  | 0.0       | 424036.08    |
    And Customer makes "AUTOPAY" repayment on "12 February 2025" with 55284.0 EUR transaction amount
    Then Loan Repayment schedule has 36 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid     | In advance | Late     | Outstanding |
      |    |      | 31 December 2024  |                  | 424036.08       |               |          | 0.0  |           | 0.0      | 0.0      |            |          |             |
      | 1  | 31   | 31 January 2025   | 12 February 2025 | 414277.37       | 9758.71       | 4321.68  | 0.0  | 0.0       | 14080.39 | 14080.39 | 0.0        | 14080.39 | 0.0         |
      | 2  | 28   | 28 February 2025  | 12 February 2025 | 400196.98       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 3  | 31   | 31 March 2025     | 12 February 2025 | 386116.59       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 4  | 30   | 30 April 2025     |                  | 372036.2        | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 13042.83 | 13042.83   | 0.0      | 1037.56     |
      | 5  | 31   | 31 May 2025       |                  | 372036.2        | 0.0           | 14080.39 | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 6  | 30   | 30 June 2025      |                  | 362474.64       | 9561.56       | 4518.83  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 7  | 31   | 31 July 2025      |                  | 352196.54       | 10278.1       | 3802.29  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 8  | 31   | 31 August 2025    |                  | 341918.44       | 10278.1       | 3802.29  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 9  | 30   | 30 September 2025 |                  | 331517.68       | 10400.76      | 3679.63  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 10 | 31   | 31 October 2025   |                  | 321239.58       | 10278.1       | 3802.29  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 11 | 30   | 30 November 2025  |                  | 310838.82       | 10400.76      | 3679.63  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 12 | 31   | 31 December 2025  |                  | 300560.72       | 10278.1       | 3802.29  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 13 | 31   | 31 January 2026   |                  | 290282.62       | 10278.1       | 3802.29  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 14 | 28   | 28 February 2026  |                  | 279500.46       | 10782.16      | 3298.23  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 15 | 31   | 31 March 2026     |                  | 268268.68       | 11231.78      | 2848.61  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 16 | 30   | 30 April 2026     |                  | 256834.23       | 11434.45      | 2645.94  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 17 | 31   | 31 May 2026       |                  | 245371.44       | 11462.79      | 2617.6   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 18 | 30   | 30 June 2026      |                  | 233711.15       | 11660.29      | 2420.1   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 19 | 31   | 31 July 2026      |                  | 222012.69       | 11698.46      | 2381.93  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 20 | 31   | 31 August 2026    |                  | 210195.0        | 11817.69      | 2262.7   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 21 | 30   | 30 September 2026 |                  | 198187.77       | 12007.23      | 2073.16  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 22 | 31   | 31 October 2026   |                  | 186127.27       | 12060.5       | 2019.89  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 23 | 30   | 30 November 2026  |                  | 173882.66       | 12244.61      | 1835.78  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 24 | 31   | 31 December 2026  |                  | 161574.44       | 12308.22      | 1772.17  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 25 | 31   | 31 January 2027   |                  | 149140.78       | 12433.66      | 1646.73  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 26 | 28   | 28 February 2027  |                  | 136433.3        | 12707.48      | 1372.91  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 27 | 31   | 31 March 2027     |                  | 123743.41       | 12689.89      | 1390.5   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 28 | 30   | 30 April 2027     |                  | 110883.5        | 12859.91      | 1220.48  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 29 | 31   | 31 May 2027       |                  | 97933.21        | 12950.29      | 1130.1   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 30 | 30   | 30 June 2027      |                  | 84818.74        | 13114.47      | 965.92   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 31 | 31   | 31 July 2027      |                  | 71602.8         | 13215.94      | 864.45   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 32 | 31   | 31 August 2027    |                  | 58252.17        | 13350.63      | 729.76   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 33 | 30   | 30 September 2027 |                  | 44746.32        | 13505.85      | 574.54   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 34 | 31   | 31 October 2027   |                  | 31121.97        | 13624.35      | 456.04   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 35 | 30   | 30 November 2027  |                  | 17348.54        | 13773.43      | 306.96   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 36 | 31   | 31 December 2027  |                  | 0.0             | 17348.54      | 176.81   | 0.0  | 0.0       | 17525.35 | 0.0      | 0.0        | 0.0      | 17525.35    |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid    | In advance | Late     | Outstanding |
      | 424036.08     | 86302.92 | 0.0  | 0.0       | 510339.0 | 55284.0 | 41203.61   | 14080.39 | 455055.0    |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount    | Principal | Interest | Fees | Penalties | Loan Balance |
      | 31 December 2024 | Disbursement     | 424036.08 | 0.0       | 0.0      | 0.0  | 0.0       | 424036.08    |
      | 12 February 2025 | Repayment        | 55284.0   | 50962.32  | 4321.68  | 0.0  | 0.0       | 373073.76    |
    And Customer makes "AUTOPAY" repayment on "27 June 2025" with 66605.0 EUR transaction amount
    Then Loan Repayment schedule has 36 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid     | In advance | Late     | Outstanding |
      |    |      | 31 December 2024  |                  | 424036.08       |               |          | 0.0  |           | 0.0      | 0.0      |            |          |             |
      | 1  | 31   | 31 January 2025   | 12 February 2025 | 414277.37       | 9758.71       | 4321.68  | 0.0  | 0.0       | 14080.39 | 14080.39 | 0.0        | 14080.39 | 0.0         |
      | 2  | 28   | 28 February 2025  | 12 February 2025 | 400196.98       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 3  | 31   | 31 March 2025     | 12 February 2025 | 386116.59       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 4  | 30   | 30 April 2025     | 27 June 2025     | 372036.2        | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 13042.83   | 1037.56  | 0.0         |
      | 5  | 31   | 31 May 2025       | 27 June 2025     | 372036.2        | 0.0           | 14080.39 | 0.0  | 0.0       | 14080.39 | 14080.39 | 0.0        | 14080.39 | 0.0         |
      | 6  | 30   | 30 June 2025      | 27 June 2025     | 357955.81       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 7  | 31   | 31 July 2025      | 27 June 2025     | 343875.42       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 8  | 31   | 31 August 2025    | 27 June 2025     | 329795.03       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 9  | 30   | 30 September 2025 |                  | 329795.03       | 0.0           | 14080.39 | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 10 | 31   | 31 October 2025   |                  | 319063.77       | 10731.26      | 3349.13  | 0.0  | 0.0       | 14080.39 | 9245.88  | 9245.88    | 0.0      | 4834.51     |
      | 11 | 30   | 30 November 2025  |                  | 308144.96       | 10918.81      | 3161.58  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 12 | 31   | 31 December 2025  |                  | 297331.54       | 10813.42      | 3266.97  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 13 | 31   | 31 January 2026   |                  | 286518.12       | 10813.42      | 3266.97  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 14 | 28   | 28 February 2026  |                  | 275332.6        | 11185.52      | 2894.87  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 15 | 31   | 31 March 2026     |                  | 264058.34       | 11274.26      | 2806.13  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 16 | 30   | 30 April 2026     |                  | 252582.36       | 11475.98      | 2604.41  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 17 | 31   | 31 May 2026       |                  | 241076.23       | 11506.13      | 2574.26  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 18 | 30   | 30 June 2026      |                  | 229373.58       | 11702.65      | 2377.74  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 19 | 31   | 31 July 2026      |                  | 217630.92       | 11742.66      | 2337.73  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 20 | 31   | 31 August 2026    |                  | 205768.58       | 11862.34      | 2218.05  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 21 | 30   | 30 September 2026 |                  | 193717.69       | 12050.89      | 2029.5   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 22 | 31   | 31 October 2026   |                  | 181611.63       | 12106.06      | 1974.33  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 23 | 30   | 30 November 2026  |                  | 169322.48       | 12289.15      | 1791.24  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 24 | 31   | 31 December 2026  |                  | 156967.79       | 12354.69      | 1725.7   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 25 | 31   | 31 January 2027   |                  | 144487.18       | 12480.61      | 1599.78  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 26 | 28   | 28 February 2027  |                  | 131736.86       | 12750.32      | 1330.07  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 27 | 31   | 31 March 2027     |                  | 118999.1        | 12737.76      | 1342.63  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 28 | 30   | 30 April 2027     |                  | 106092.4        | 12906.7       | 1173.69  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 29 | 31   | 31 May 2027       |                  | 93093.28        | 12999.12      | 1081.27  | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 30 | 30   | 30 June 2027      |                  | 79931.07        | 13162.21      | 918.18   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 31 | 31   | 31 July 2027      |                  | 66665.32        | 13265.75      | 814.64   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 32 | 31   | 31 August 2027    |                  | 53264.37        | 13400.95      | 679.44   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 33 | 30   | 30 September 2027 |                  | 39709.33        | 13555.04      | 525.35   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 34 | 31   | 31 October 2027   |                  | 26033.65        | 13675.68      | 404.71   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 35 | 30   | 30 November 2027  |                  | 12210.03        | 13823.62      | 256.77   | 0.0  | 0.0       | 14080.39 | 0.0      | 0.0        | 0.0      | 14080.39    |
      | 36 | 31   | 31 December 2027  |                  | 0.0             | 12210.03      | 124.44   | 0.0  | 0.0       | 12334.47 | 0.0      | 0.0        | 0.0      | 12334.47    |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due       | Paid     | In advance | Late     | Outstanding |
      | 424036.08     | 81112.04 | 0.0  | 0.0       | 505148.12 | 121889.0 | 92690.66   | 29198.34 | 383259.12   |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount    | Principal | Interest | Fees | Penalties | Loan Balance |
      | 31 December 2024 | Disbursement     | 424036.08 | 0.0       | 0.0      | 0.0  | 0.0       | 424036.08    |
      | 12 February 2025 | Repayment        | 55284.0   | 50962.32  | 4321.68  | 0.0  | 0.0       | 373073.76    |
      | 27 June 2025     | Repayment        | 66605.0   | 52524.61  | 14080.39 | 0.0  | 0.0       | 320549.15    |
    When Loan Pay-off is made on "20 August 2025"
    Then Loan Repayment schedule has 36 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid     | In advance | Late     | Outstanding |
      |    |      | 31 December 2024  |                  | 424036.08       |               |          | 0.0  |           | 0.0      | 0.0      |            |          |             |
      | 1  | 31   | 31 January 2025   | 12 February 2025 | 414277.37       | 9758.71       | 4321.68  | 0.0  | 0.0       | 14080.39 | 14080.39 | 0.0        | 14080.39 | 0.0         |
      | 2  | 28   | 28 February 2025  | 12 February 2025 | 400196.98       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 3  | 31   | 31 March 2025     | 12 February 2025 | 386116.59       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 4  | 30   | 30 April 2025     | 27 June 2025     | 372036.2        | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 13042.83   | 1037.56  | 0.0         |
      | 5  | 31   | 31 May 2025       | 27 June 2025     | 372036.2        | 0.0           | 14080.39 | 0.0  | 0.0       | 14080.39 | 14080.39 | 0.0        | 14080.39 | 0.0         |
      | 6  | 30   | 30 June 2025      | 27 June 2025     | 357955.81       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 7  | 31   | 31 July 2025      | 27 June 2025     | 343875.42       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 8  | 31   | 31 August 2025    | 27 June 2025     | 329795.03       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 9  | 30   | 30 September 2025 | 20 August 2025   | 315714.64       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 10 | 31   | 31 October 2025   | 20 August 2025   | 301634.25       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 11 | 30   | 30 November 2025  | 20 August 2025   | 287553.86       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 12 | 31   | 31 December 2025  | 20 August 2025   | 273473.47       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 13 | 31   | 31 January 2026   | 20 August 2025   | 259393.08       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 14 | 28   | 28 February 2026  | 20 August 2025   | 245312.69       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 15 | 31   | 31 March 2026     | 20 August 2025   | 231232.3        | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 16 | 30   | 30 April 2026     | 20 August 2025   | 217151.91       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 17 | 31   | 31 May 2026       | 20 August 2025   | 203071.52       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 18 | 30   | 30 June 2026      | 20 August 2025   | 188991.13       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 19 | 31   | 31 July 2026      | 20 August 2025   | 174910.74       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 20 | 31   | 31 August 2026    | 20 August 2025   | 160830.35       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 21 | 30   | 30 September 2026 | 20 August 2025   | 146749.96       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 22 | 31   | 31 October 2026   | 20 August 2025   | 132669.57       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 23 | 30   | 30 November 2026  | 20 August 2025   | 118589.18       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 24 | 31   | 31 December 2026  | 20 August 2025   | 104508.79       | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 25 | 31   | 31 January 2027   | 20 August 2025   | 90428.4         | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 26 | 28   | 28 February 2027  | 20 August 2025   | 76348.01        | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 27 | 31   | 31 March 2027     | 20 August 2025   | 62267.62        | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 28 | 30   | 30 April 2027     | 20 August 2025   | 48187.23        | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 29 | 31   | 31 May 2027       | 20 August 2025   | 34106.84        | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 30 | 30   | 30 June 2027      | 20 August 2025   | 20026.45        | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 31 | 31   | 31 July 2027      | 20 August 2025   | 5946.06         | 14080.39      | 0.0      | 0.0  | 0.0       | 14080.39 | 14080.39 | 14080.39   | 0.0      | 0.0         |
      | 32 | 31   | 31 August 2027    | 20 August 2025   | 0.0             | 5946.06       | 0.0      | 0.0  | 0.0       | 5946.06  | 5946.06  | 5946.06    | 0.0      | 0.0         |
      | 33 | 30   | 30 September 2027 | 20 August 2025   | 0.0             | 0.0           | 9841.72  | 0.0  | 0.0       | 9841.72  | 9841.72  | 9841.72    | 0.0      | 0.0         |
      | 34 | 31   | 31 October 2027   | 20 August 2025   | 0.0             | 0.0           | 0.0      | 0.0  | 0.0       | 0.0      | 0.0      | 0.0        | 0.0      | 0.0         |
      | 35 | 30   | 30 November 2027  | 20 August 2025   | 0.0             | 0.0           | 0.0      | 0.0  | 0.0       | 0.0      | 0.0      | 0.0        | 0.0      | 0.0         |
      | 36 | 31   | 31 December 2027  | 20 August 2025   | 0.0             | 0.0           | 0.0      | 0.0  | 0.0       | 0.0      | 0.0      | 0.0        | 0.0      | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due       | Paid      | In advance | Late     | Outstanding |
      | 424036.08     | 28243.79 | 0.0  | 0.0       | 452279.87 | 452279.87 | 423081.53  | 29198.34 | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount    | Principal | Interest | Fees | Penalties | Loan Balance |
      | 31 December 2024 | Disbursement     | 424036.08 | 0.0       | 0.0      | 0.0  | 0.0       | 424036.08    |
      | 12 February 2025 | Repayment        | 55284.0   | 50962.32  | 4321.68  | 0.0  | 0.0       | 373073.76    |
      | 27 June 2025     | Repayment        | 66605.0   | 52524.61  | 14080.39 | 0.0  | 0.0       | 320549.15    |
      | 20 August 2025   | Repayment        | 330390.87 | 320549.15 | 9841.72  | 0.0  | 0.0       | 0.0          |
      | 23 February 2026 | Accrual          | 28243.79  | 0.0       | 28243.79 | 0.0  | 0.0       | 0.0          |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set "LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC" loan product "REPAYMENT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
