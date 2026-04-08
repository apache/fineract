@Repayment
Feature: LoanRepayment - Part4

  @TestRailId:C4353
  Scenario: Verify the loan creation with total disbursement amount less then 1 for progressive loan - UC2
    When Admin sets the business date to "26 October 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR   | 26 October 2025   | 1              | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 1                 | MONTHS                | 1              | MONTHS                 | 1                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "26 October 2025" with "1" amount and expected disbursement date on "26 October 2025"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 26 October 2025  |           | 1.0             |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 26 November 2025 |           | 0.0             | 1.0           | 0.0      | 0.0  | 0.0       | 1.0   | 0.0  | 0.0        | 0.0  | 1.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1.0           | 0.0      | 0.0  | 0.0       | 1.0    | 0.0  | 0.0        | 0.0  | 1.0         |
    When Admin successfully disburse the loan on "26 October 2025" with "0.4" EUR transaction amount
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 26 October 2025  |           | 0.4             |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 26 November 2025 |           | 0.0             | 0.4           | 0.0      | 0.0  | 0.0       | 0.4   | 0.0  | 0.0        | 0.0  | 0.4         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 0.4           | 0.0      | 0.0  | 0.0       | 0.4    | 0.0  | 0.0        | 0.0  | 0.4         |
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type | Amount  | Principal | Interest | Fees | Penalties  | Loan Balance | Reverted | Replayed |
      | 26 October 2025    | Disbursement     | 0.4     | 0.0       |  0.0      | 0.0  | 0.0       | 0.4          | false    | false    |
    When Admin sets the business date to "27 October 2025"
    When Loan Pay-off is made on "27 October 2025"
    Then Loan Repayment schedule has 1 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 26 October 2025  |                 | 0.4             |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 26 November 2025 | 27 October 2025 | 0.0             | 0.4           | 0.0      | 0.0  | 0.0       | 0.4   | 0.4  | 0.4        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 0.4           | 0.0      | 0.0  | 0.0       | 0.4    | 0.4  | 0.4        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type | Amount  | Principal | Interest | Fees | Penalties  | Loan Balance | Reverted | Replayed |
      | 26 October 2025    | Disbursement     | 0.4     | 0.0       |  0.0      | 0.0  | 0.0       | 0.4          | false    | false    |
      | 27 October 2025    | Repayment        | 0.4     | 0.4       |  0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4354
  Scenario: Verify the loan creation with total disbursement amount less then 1 for progressive loan - 2 repayments
    When Admin sets the business date to "26 October 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with loan product`s charges and following data:
      | LoanProduct                                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR                                            | 26 October 2025   | 1              | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 2                 | MONTHS                | 1              | MONTHS                 | 2                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "26 October 2025" with "1" amount and expected disbursement date on "26 October 2025"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 26 October 2025  |           | 1.0             |               |          | 0.0  |           | 0.0   |      |            |      | 0.0         |
      | 1  | 31   | 26 November 2025 |           | 0.0             | 1.0           | 0.0      | 0.0  | 0.0       | 1.0   | 0.0  | 0.0        | 0.0  | 1.0         |
      | 2  | 30   | 26 December 2025 |           | 0.0             | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1.0           | 0.0      | 0.0  | 0.0       | 1.0    | 0.0  | 0.0        | 0.0  | 1.0         |
    When Admin successfully disburse the loan on "26 October 2025" with "0.4" EUR transaction amount
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 26 October 2025  |                 | 0.4             |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 26 November 2025 |                 | 0.0             | 0.4           | 0.0      | 0.0  | 0.0       | 0.4   | 0.0  | 0.0        | 0.0  | 0.4         |
      | 2  | 30   | 26 December 2025 | 26 October 2025 | 0.0             | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 0.4           | 0.0      | 0.0  | 0.0       | 0.4    | 0.0  | 0.0        | 0.0  | 0.4         |
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type | Amount  | Principal | Interest | Fees | Penalties  | Loan Balance | Reverted | Replayed |
      | 26 October 2025    | Disbursement     | 0.4     | 0.0       |  0.0      | 0.0  | 0.0       | 0.4          | false    | false    |
    When Admin sets the business date to "27 October 2025"
    When Loan Pay-off is made on "27 October 2025"
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 26 October 2025  |                 | 0.4             |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 26 November 2025 | 27 October 2025 | 0.0             | 0.4           | 0.0      | 0.0  | 0.0       | 0.4   | 0.4  | 0.4        | 0.0  | 0.0         |
      | 2  | 30   | 26 December 2025 | 26 October 2025 | 0.0             | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 0.4           | 0.0      | 0.0  | 0.0       | 0.4    | 0.4  | 0.4        | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date   | Transaction Type | Amount  | Principal | Interest | Fees | Penalties  | Loan Balance | Reverted | Replayed |
      | 26 October 2025    | Disbursement     | 0.4     | 0.0       |  0.0      | 0.0  | 0.0       | 0.4          | false    | false    |
      | 27 October 2025    | Repayment        | 0.4     | 0.4       |  0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
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
  Scenario: Verify AdvancedPaymentAllocation behaviour: loanScheduleProcessingType-vertical, prepayment with NEXT_INSTALLMENT strategy
    When Admin sets the business date to "23 February 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC" loan product "REPAYMENT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type      | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC | 01 January 2026   | 25000000       | 12                     | DECLINING_BALANCE  | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                  | 0                       | 0                      | 0                   | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "25000000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "25000000" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest    | Fees | Penalties | Due          | Paid           | In advance       | Late    | Outstanding    |
      |    |      | 01 January 2026   |           | 25000000.0      |               |             | 0.0  |           | 0.0          | 0.0            |                  |         |                |
      | 1  | 31   | 01 February 2026  |           | 23034153.81     | 1965846.19    | 254794.52   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 2  | 28   | 01 March 2026     |           | 21039772.25     | 1994381.56    | 226259.15   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 3  | 31   | 01 April 2026     |           | 19033564.29     | 2006207.96    | 214432.75   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 4  | 30   | 01 May 2026       |           | 17000651.89     | 2032912.4     | 187728.31   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 5  | 31   | 01 June 2026      |           | 14953278.1      | 2047373.79    | 173266.92   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 6  | 30   | 01 July 2026      |           | 12880121.78     | 2073156.32    | 147484.39   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 7  | 31   | 01 August 2026    |           | 10790752.45     | 2089369.33    | 131271.38   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 8  | 31   | 01 September 2026 |           |  8680088.72     | 2110663.73    | 109976.98   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 9  | 30   | 01 October 2026   |           |  6545059.84     | 2135028.88    |  85611.83   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 10 | 31   | 01 November 2026  |           |  4391124.95     | 2153934.89    |  66705.82   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 11 | 30   | 01 December 2026  |           |  2213793.97     | 2177330.98    |  43309.73   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 12 | 31   | 01 January 2027   |           |        0.0      | 2213793.97    |  22562.5    | 0.0  | 0.0       | 2236356.47   | 0.0            | 0.0              | 0.0     | 2236356.47     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due     | Interest      | Fees | Penalties | Due           | Paid | In advance | Late | Outstanding    |
      | 25000000.0        | 1663404.28    | 0.0  | 0.0       | 26663404.28   | 0.0  | 0.0        | 0.0  | 26663404.28    |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount     | Principal | Interest | Fees | Penalties | Loan Balance     |
      | 01 January 2026   | Disbursement     | 25000000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 25000000.0       |
    When Loan Pay-off is made on "23 February 2026"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest    | Fees | Penalties | Due          | Paid           | In advance       | Late         | Outstanding    |
      |    |      | 01 January 2026   |                  | 25000000.0      |               |             | 0.0  |           | 0.0          | 0.0            |                  |              |                |
      | 1  | 31   | 01 February 2026  | 23 February 2026 | 23034153.81     | 1965846.19    | 254794.52   | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 0.0              | 2220640.71   | 0.0            |
      | 2  | 28   | 01 March 2026     | 23 February 2026 | 20813513.1      | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 3  | 31   | 01 April 2026     | 23 February 2026 | 18592872.39     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 4  | 30   | 01 May 2026       | 23 February 2026 | 16372231.68     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 5  | 31   | 01 June 2026      | 23 February 2026 | 14151590.97     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 6  | 30   | 01 July 2026      | 23 February 2026 | 11930950.26     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 7  | 31   | 01 August 2026    | 23 February 2026 |  9710309.55     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 8  | 31   | 01 September 2026 | 23 February 2026 |  7489668.84     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 9  | 30   | 01 October 2026   | 23 February 2026 |  5269028.13     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 10 | 31   | 01 November 2026  | 23 February 2026 |  3048387.42     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 11 | 30   | 01 December 2026  | 23 February 2026 |   827746.71     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 12 | 31   | 01 January 2027   | 23 February 2026 |        0.0      |  827746.71    | 180821.92   | 0.0  | 0.0       | 1008568.63   | 1008568.63     | 1008568.63       | 0.0          | 0.0            |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due     | Interest      | Fees | Penalties | Due           | Paid        | In advance   | Late        | Outstanding    |
      | 25000000.0        | 435616.44     | 0.0  | 0.0       | 25435616.44   | 25435616.44 | 23214975.73  | 2220640.71  | 0.0            |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount      | Principal  | Interest    | Fees | Penalties | Loan Balance     |
      | 01 January 2026   | Disbursement     | 25000000.0  | 0.0        | 0.0         | 0.0  | 0.0       | 25000000.0       |
      | 23 February 2026  | Repayment        | 25435616.44 | 25000000.0 | 435616.44   | 0.0  | 0.0       |        0.0       |
      | 23 February 2026  | Accrual          |   435616.44 |        0.0 | 435616.44   | 0.0  | 0.0       |        0.0       |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set "LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC" loan product "REPAYMENT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C4684 @AdvancedPaymentAllocation @ProgressiveLoanSchedule
  Scenario: Verify AdvancedPaymentAllocation behaviour: loanScheduleProcessingType-vertical, prepayment with LAST_INSTALLMENT strategy
    When Admin sets the business date to "23 February 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC" loan product "REPAYMENT" transaction type to "LAST_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type      | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC | 01 January 2026   | 25000000       | 12                     | DECLINING_BALANCE  | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                  | 0                       | 0                      | 0                   | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "25000000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "25000000" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest    | Fees | Penalties | Due          | Paid           | In advance       | Late    | Outstanding    |
      |    |      | 01 January 2026   |           | 25000000.0      |               |             | 0.0  |           | 0.0          | 0.0            |                  |         |                |
      | 1  | 31   | 01 February 2026  |           | 23034153.81     | 1965846.19    | 254794.52   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 2  | 28   | 01 March 2026     |           | 21039772.25     | 1994381.56    | 226259.15   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 3  | 31   | 01 April 2026     |           | 19033564.29     | 2006207.96    | 214432.75   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 4  | 30   | 01 May 2026       |           | 17000651.89     | 2032912.4     | 187728.31   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 5  | 31   | 01 June 2026      |           | 14953278.1      | 2047373.79    | 173266.92   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 6  | 30   | 01 July 2026      |           | 12880121.78     | 2073156.32    | 147484.39   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 7  | 31   | 01 August 2026    |           | 10790752.45     | 2089369.33    | 131271.38   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 8  | 31   | 01 September 2026 |           |  8680088.72     | 2110663.73    | 109976.98   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 9  | 30   | 01 October 2026   |           |  6545059.84     | 2135028.88    |  85611.83   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 10 | 31   | 01 November 2026  |           |  4391124.95     | 2153934.89    |  66705.82   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 11 | 30   | 01 December 2026  |           |  2213793.97     | 2177330.98    |  43309.73   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 12 | 31   | 01 January 2027   |           |        0.0      | 2213793.97    |  22562.5    | 0.0  | 0.0       | 2236356.47   | 0.0            | 0.0              | 0.0     | 2236356.47     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due     | Interest      | Fees | Penalties | Due           | Paid | In advance | Late | Outstanding    |
      | 25000000.0        | 1663404.28    | 0.0  | 0.0       | 26663404.28   | 0.0  | 0.0        | 0.0  | 26663404.28    |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount     | Principal | Interest | Fees | Penalties | Loan Balance     |
      | 01 January 2026   | Disbursement     | 25000000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 25000000.0       |
    When Loan Pay-off is made on "23 February 2026"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest    | Fees | Penalties | Due          | Paid           | In advance       | Late         | Outstanding    |
      |    |      | 01 January 2026   |                  | 25000000.0      |               |             | 0.0  |           | 0.0          | 0.0            |                  |              |                |
      | 1  | 31   | 01 February 2026  | 23 February 2026 | 23034153.81     | 1965846.19    | 254794.52   | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 0.0              | 2220640.71   | 0.0            |
      | 2  | 28   | 01 March 2026     | 23 February 2026 | 22206407.14     |  827746.67    | 180821.92   | 0.0  | 0.0       | 1008568.59   | 1008568.59     | 1008568.59       | 0.0          | 0.0            |
      | 3  | 31   | 01 April 2026     | 23 February 2026 | 19985766.43     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 4  | 30   | 01 May 2026       | 23 February 2026 | 17765125.72     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 5  | 31   | 01 June 2026      | 23 February 2026 | 15544485.01     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 6  | 30   | 01 July 2026      | 23 February 2026 | 13323844.3      | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 7  | 31   | 01 August 2026    | 23 February 2026 | 11103203.59     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 8  | 31   | 01 September 2026 | 23 February 2026 |  8882562.88     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 9  | 30   | 01 October 2026   | 23 February 2026 |  6661922.17     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 10 | 31   | 01 November 2026  | 23 February 2026 |  4441281.46     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 11 | 30   | 01 December 2026  | 23 February 2026 |  2220640.75     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 12 | 31   | 01 January 2027   | 23 February 2026 |        0.0      | 2220640.75    |      0.0    | 0.0  | 0.0       | 2220640.75   | 2220640.75     | 2220640.75       | 0.0          | 0.0            |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due     | Interest      | Fees | Penalties | Due           | Paid        | In advance   | Late        | Outstanding    |
      | 25000000.0        | 435616.44     | 0.0  | 0.0       | 25435616.44   | 25435616.44 | 23214975.73  | 2220640.71  | 0.0            |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount      | Principal  | Interest    | Fees | Penalties | Loan Balance     |
      | 01 January 2026   | Disbursement     | 25000000.0  | 0.0        | 0.0         | 0.0  | 0.0       | 25000000.0       |
      | 23 February 2026  | Repayment        | 25435616.44 | 25000000.0 | 435616.44   | 0.0  | 0.0       |        0.0       |
      | 23 February 2026  | Accrual          |   435616.44 |        0.0 | 435616.44   | 0.0  | 0.0       |        0.0       |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set "LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC" loan product "REPAYMENT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C4685 @AdvancedPaymentAllocation @ProgressiveLoanSchedule
  Scenario: Verify AdvancedPaymentAllocation behaviour: loanScheduleProcessingType-vertical, prepayment with NEXT_LAST_INSTALLMENT strategy
    When Admin sets the business date to "23 February 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC" loan product "REPAYMENT" transaction type to "NEXT_LAST_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type      | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC | 01 January 2026   | 25000000       | 12                     | DECLINING_BALANCE  | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                  | 0                       | 0                      | 0                   | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "25000000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "25000000" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest    | Fees | Penalties | Due          | Paid           | In advance       | Late    | Outstanding    |
      |    |      | 01 January 2026   |           | 25000000.0      |               |             | 0.0  |           | 0.0          | 0.0            |                  |         |                |
      | 1  | 31   | 01 February 2026  |           | 23034153.81     | 1965846.19    | 254794.52   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 2  | 28   | 01 March 2026     |           | 21039772.25     | 1994381.56    | 226259.15   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 3  | 31   | 01 April 2026     |           | 19033564.29     | 2006207.96    | 214432.75   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 4  | 30   | 01 May 2026       |           | 17000651.89     | 2032912.4     | 187728.31   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 5  | 31   | 01 June 2026      |           | 14953278.1      | 2047373.79    | 173266.92   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 6  | 30   | 01 July 2026      |           | 12880121.78     | 2073156.32    | 147484.39   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 7  | 31   | 01 August 2026    |           | 10790752.45     | 2089369.33    | 131271.38   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 8  | 31   | 01 September 2026 |           |  8680088.72     | 2110663.73    | 109976.98   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 9  | 30   | 01 October 2026   |           |  6545059.84     | 2135028.88    |  85611.83   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 10 | 31   | 01 November 2026  |           |  4391124.95     | 2153934.89    |  66705.82   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 11 | 30   | 01 December 2026  |           |  2213793.97     | 2177330.98    |  43309.73   | 0.0  | 0.0       | 2220640.71   | 0.0            | 0.0              | 0.0     | 2220640.71     |
      | 12 | 31   | 01 January 2027   |           |        0.0      | 2213793.97    |  22562.5    | 0.0  | 0.0       | 2236356.47   | 0.0            | 0.0              | 0.0     | 2236356.47     |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due     | Interest      | Fees | Penalties | Due           | Paid | In advance | Late | Outstanding    |
      | 25000000.0        | 1663404.28    | 0.0  | 0.0       | 26663404.28   | 0.0  | 0.0        | 0.0  | 26663404.28    |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount     | Principal | Interest | Fees | Penalties | Loan Balance     |
      | 01 January 2026   | Disbursement     | 25000000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 25000000.0       |
    When Loan Pay-off is made on "23 February 2026"
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest    | Fees | Penalties | Due          | Paid           | In advance       | Late         | Outstanding    |
      |    |      | 01 January 2026   |                  | 25000000.0      |               |             | 0.0  |           | 0.0          | 0.0            |                  |              |                |
      | 1  | 31   | 01 February 2026  | 23 February 2026 | 23034153.81     | 1965846.19    | 254794.52   | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 0.0              | 2220640.71   | 0.0            |
      | 2  | 28   | 01 March 2026     | 23 February 2026 | 20813513.1      | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 3  | 31   | 01 April 2026     | 23 February 2026 | 19985766.43     |  827746.67    | 180821.92   | 0.0  | 0.0       | 1008568.59   | 1008568.59     | 1008568.59       | 0.0          | 0.0            |
      | 4  | 30   | 01 May 2026       | 23 February 2026 | 17765125.72     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 5  | 31   | 01 June 2026      | 23 February 2026 | 15544485.01     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 6  | 30   | 01 July 2026      | 23 February 2026 | 13323844.3      | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 7  | 31   | 01 August 2026    | 23 February 2026 | 11103203.59     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 8  | 31   | 01 September 2026 | 23 February 2026 |  8882562.88     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 9  | 30   | 01 October 2026   | 23 February 2026 |  6661922.17     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 10 | 31   | 01 November 2026  | 23 February 2026 |  4441281.46     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 11 | 30   | 01 December 2026  | 23 February 2026 |  2220640.75     | 2220640.71    |      0.0    | 0.0  | 0.0       | 2220640.71   | 2220640.71     | 2220640.71       | 0.0          | 0.0            |
      | 12 | 31   | 01 January 2027   | 23 February 2026 |        0.0      | 2220640.75    |      0.0    | 0.0  | 0.0       | 2220640.75   | 2220640.75     | 2220640.75       | 0.0          | 0.0            |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due     | Interest      | Fees | Penalties | Due           | Paid        | In advance   | Late        | Outstanding    |
      | 25000000.0        | 435616.44     | 0.0  | 0.0       | 25435616.44   | 25435616.44 | 23214975.73  | 2220640.71  | 0.0            |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount      | Principal  | Interest    | Fees | Penalties | Loan Balance     |
      | 01 January 2026   | Disbursement     | 25000000.0  | 0.0        | 0.0         | 0.0  | 0.0       | 25000000.0       |
      | 23 February 2026  | Repayment        | 25435616.44 | 25000000.0 | 435616.44   | 0.0  | 0.0       |        0.0       |
      | 23 February 2026  | Accrual          |   435616.44 |        0.0 | 435616.44   | 0.0  | 0.0       |        0.0       |
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    When Admin set "LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL_INTEREST_RECALC" loan product "REPAYMENT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

