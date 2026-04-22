@LoanFeature
Feature: Loan - Part4

  @TestRailId:C3424
  Scenario: Verify that after maturity date with inline COB the outstanding interest is recognized on repayment schedule
    When Admin sets the business date to "23 December 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      |LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_ACTUAL  | 23 December 2024  | 100            | 4                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "23 December 2024" with "100" amount and expected disbursement date on "23 December 2024"
    When Admin successfully disburse the loan on "23 December 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 23 December 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 23 January 2025  |           | 66.78           | 33.22         | 0.34     | 0.0  | 0.0       | 33.56 | 0.0  | 0.0        | 0.0  | 33.56       |
      | 2  | 31   | 23 February 2025 |           | 33.45           | 33.33         | 0.23     | 0.0  | 0.0       | 33.56 | 0.0  | 0.0        | 0.0  | 33.56       |
      | 3  | 28   | 23 March 2025    |           | 0.0             | 33.45         | 0.1      | 0.0  | 0.0       | 33.55 | 0.0  | 0.0        | 0.0  | 33.55       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.67     | 0.0  | 0.0       | 100.67 | 0.0  | 0.0        | 0.0  | 100.67      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 December 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    And Admin sets the business date to "23 March 2025"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 23 December 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 23 January 2025  |           | 66.78           | 33.22         | 0.34     | 0.0  | 0.0       | 33.56 | 0.0  | 0.0        | 0.0  | 33.56       |
      | 2  | 31   | 23 February 2025 |           | 33.45           | 33.33         | 0.23     | 0.0  | 0.0       | 33.56 | 0.0  | 0.0        | 0.0  | 33.56       |
      | 3  | 28   | 23 March 2025    |           | 0.0             | 33.45         | 0.1      | 0.0  | 0.0       | 33.55 | 0.0  | 0.0        | 0.0  | 33.55       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.67     | 0.0  | 0.0       | 100.67 | 0.0  | 0.0        | 0.0  | 100.67      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 23 December 2024 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 22 March 2025    | Accrual          | 0.67   | 0.0       | 0.67     | 0.0  | 0.0       |  0.0         |

  @TestRailId:C3534 @AdvancedPaymentAllocation
  Scenario: Verify advanced payment allocation - future installments: LAST_INSTALLMENT, full Merchant issued Refund on the disbursement date, 2nd disbursement on the same date
    When Admin sets the business date to "11 March 2025"
    And Admin creates a client with random data
    When Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_FULL" loan product "MERCHANT_ISSUED_REFUND" transaction type to "LAST_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type              | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_FULL | 11 March 2025     | 1000           | 26                     | DECLINING_BALANCE          | DAILY                       | EQUAL_INSTALLMENTS | 12                | MONTHS                | 1              | MONTHS                 | 12                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "11 March 2025" with "1000" amount and expected disbursement date on "11 March 2025"
    And Admin successfully disburse the loan on "11 March 2025" with "200" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 11 March 2025     |                   | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 11 April 2025     |                   | 185.3           | 14.7          | 4.42     | 0.0  | 0.0       | 19.12 | 0.0   | 0.0        | 0.0  | 19.12       |
      | 2  | 30   | 11 May 2025       |                   | 170.14          | 15.16         | 3.96     | 0.0  | 0.0       | 19.12 | 0.0   | 0.0        | 0.0  | 19.12       |
      | 3  | 31   | 11 June 2025      |                   | 154.78          | 15.36         | 3.76     | 0.0  | 0.0       | 19.12 | 0.0   | 0.0        | 0.0  | 19.12       |
      | 4  | 30   | 11 July 2025      |                   | 138.97          | 15.81         | 3.31     | 0.0  | 0.0       | 19.12 | 0.0   | 0.0        | 0.0  | 19.12       |
      | 5  | 31   | 11 August 2025    |                   | 122.92          | 16.05         | 3.07     | 0.0  | 0.0       | 19.12 | 0.0   | 0.0        | 0.0  | 19.12       |
      | 6  | 31   | 11 September 2025 |                   | 106.51          | 16.41         | 2.71     | 0.0  | 0.0       | 19.12 | 0.0   | 0.0        | 0.0  | 19.12       |
      | 7  | 30   | 11 October 2025   |                   |  89.67          | 16.84         | 2.28     | 0.0  | 0.0       | 19.12 | 0.0   | 0.0        | 0.0  | 19.12       |
      | 8  | 31   | 11 November 2025  |                   |  72.53          | 17.14         | 1.98     | 0.0  | 0.0       | 19.12 | 0.0   | 0.0        | 0.0  | 19.12       |
      | 9  | 30   | 11 December 2025  |                   |  54.96          | 17.57         | 1.55     | 0.0  | 0.0       | 19.12 | 0.0   | 0.0        | 0.0  | 19.12       |
      | 10 | 31   | 11 January 2026   |                   |  37.05          | 17.91         | 1.21     | 0.0  | 0.0       | 19.12 | 0.0   | 0.0        | 0.0  | 19.12       |
      | 11 | 31   | 11 February 2026  |                   |  18.75          | 18.3          | 0.82     | 0.0  | 0.0       | 19.12 | 0.0   | 0.0        | 0.0  | 19.12       |
      | 12 | 28   | 11 March 2026     |                   |    0.0          | 18.75         | 0.37     | 0.0  | 0.0       | 19.12 | 0.0   | 0.0        | 0.0  | 19.12       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 200.0         | 29.44    | 0.0  | 0         | 229.44 | 0.0   | 0.0        | 0.0  | 229.44      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 11 March 2025     | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        |
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "11 March 2025" with 200 EUR transaction amount and system-generated Idempotency key
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 11 March 2025     |                   | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 11 April 2025     | 11 March 2025     | 200.0           |   0.0         | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 11 May 2025       | 11 March 2025     | 191.2           |   8.8         | 0.0      | 0.0  | 0.0       | 8.8   | 8.8   | 8.8        | 0.0  | 0.0         |
      | 3  | 31   | 11 June 2025      | 11 March 2025     | 172.08          | 19.12         | 0.0      | 0.0  | 0.0       | 19.12 | 19.12 | 19.12      | 0.0  | 0.0         |
      | 4  | 30   | 11 July 2025      | 11 March 2025     | 152.96          | 19.12         | 0.0      | 0.0  | 0.0       | 19.12 | 19.12 | 19.12      | 0.0  | 0.0         |
      | 5  | 31   | 11 August 2025    | 11 March 2025     | 133.84          | 19.12         | 0.0      | 0.0  | 0.0       | 19.12 | 19.12 | 19.12      | 0.0  | 0.0         |
      | 6  | 31   | 11 September 2025 | 11 March 2025     | 114.72          | 19.12         | 0.0      | 0.0  | 0.0       | 19.12 | 19.12 | 19.12      | 0.0  | 0.0         |
      | 7  | 30   | 11 October 2025   | 11 March 2025     |  95.6           | 19.12         | 0.0      | 0.0  | 0.0       | 19.12 | 19.12 | 19.12      | 0.0  | 0.0         |
      | 8  | 31   | 11 November 2025  | 11 March 2025     |  76.48          | 19.12         | 0.0      | 0.0  | 0.0       | 19.12 | 19.12 | 19.12      | 0.0  | 0.0         |
      | 9  | 30   | 11 December 2025  | 11 March 2025     |  57.36          | 19.12         | 0.0      | 0.0  | 0.0       | 19.12 | 19.12 | 19.12      | 0.0  | 0.0         |
      | 10 | 31   | 11 January 2026   | 11 March 2025     |  38.24          | 19.12         | 0.0      | 0.0  | 0.0       | 19.12 | 19.12 | 19.12      | 0.0  | 0.0         |
      | 11 | 31   | 11 February 2026  | 11 March 2025     |  19.12          | 19.12         | 0.0      | 0.0  | 0.0       | 19.12 | 19.12 | 19.12      | 0.0  | 0.0         |
      | 12 | 28   | 11 March 2026     | 11 March 2025     |    0.0          | 19.12         | 0.0      | 0.0  | 0.0       | 19.12 | 19.12 | 19.12      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 200.0         | 0        | 0.0  | 0         | 200.0 | 200.0 | 200.0      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 11 March 2025     | Disbursement              | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        |
      | 11 March 2025     | Merchant Issued Refund    | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    And Admin successfully disburse the loan on "11 March 2025" with "200" EUR transaction amount
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 11 March 2025     |                   | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 11 March 2025     |                   | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 11 April 2025     |                   | 366.18          | 33.82         | 4.42     | 0.0  | 0.0       | 38.24 | 0.0   | 0.0        | 0.0  | 38.24       |
      | 2  | 30   | 11 May 2025       |                   | 331.49          | 34.69         | 3.55     | 0.0  | 0.0       | 38.24 | 8.8   | 8.8        | 0.0  | 29.44       |
      | 3  | 31   | 11 June 2025      |                   | 296.35          | 35.14         | 3.1      | 0.0  | 0.0       | 38.24 | 19.12 | 19.12      | 0.0  | 19.12       |
      | 4  | 30   | 11 July 2025      |                   | 260.77          | 35.58         | 2.66     | 0.0  | 0.0       | 38.24 | 19.12 | 19.12      | 0.0  | 19.12       |
      | 5  | 31   | 11 August 2025    |                   | 224.91          | 35.86         | 2.38     | 0.0  | 0.0       | 38.24 | 19.12 | 19.12      | 0.0  | 19.12       |
      | 6  | 31   | 11 September 2025 |                   | 188.68          | 36.23         | 2.01     | 0.0  | 0.0       | 38.24 | 19.12 | 19.12      | 0.0  | 19.12       |
      | 7  | 30   | 11 October 2025   |                   | 152.02          | 36.66         | 1.58     | 0.0  | 0.0       | 38.24 | 19.12 | 19.12      | 0.0  | 19.12       |
      | 8  | 31   | 11 November 2025  |                   | 115.03          | 36.99         | 1.25     | 0.0  | 0.0       | 38.24 | 19.12 | 19.12      | 0.0  | 19.12       |
      | 9  | 30   | 11 December 2025  |                   |  77.61          | 37.42         | 0.82     | 0.0  | 0.0       | 38.24 | 19.12 | 19.12      | 0.0  | 19.12       |
      | 10 | 31   | 11 January 2026   |                   |  39.82          | 37.79         | 0.45     | 0.0  | 0.0       | 38.24 | 19.12 | 19.12      | 0.0  | 19.12       |
      | 11 | 31   | 11 February 2026  |                   |  19.12          | 20.7          | 0.03     | 0.0  | 0.0       | 20.73 | 19.12 | 19.12      | 0.0  | 1.61        |
      | 12 | 28   | 11 March 2026     | 11 March 2025     |    0.0          | 19.12         | 0.0      | 0.0  | 0.0       | 19.12 | 19.12 | 19.12      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 400.0         | 22.25    | 0.0  | 0         | 422.25 | 200.0 | 200.0      | 0.0  | 222.25      |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type          | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 11 March 2025     | Disbursement              | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        |
      | 11 March 2025     | Merchant Issued Refund    | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 0.0          |
      | 11 March 2025     | Disbursement              | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        |
    When Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_FULL" loan product "MERCHANT_ISSUED_REFUND" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C3570
  Scenario: Verify Loan is fully paid and closed after full Merchant issued refund 1 day after disbursement
    When Admin sets the business date to "29 March 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 28 March 2025     | 1383           | 12.23                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 24                | MONTHS                | 1              | MONTHS                 | 24                 | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "28 March 2025" with "1383" amount and expected disbursement date on "28 March 2025"
    When Admin successfully disburse the loan on "28 March 2025" with "1383" EUR transaction amount
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 28 March 2025       |                  | 1383.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 28 April 2025       |                  | 1331.85         | 51.15         | 14.1     | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 2  | 30   | 28 May 2025         |                  | 1280.17         | 51.68         | 13.57    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 3  | 31   | 28 June 2025        |                  | 1227.97         | 52.2          | 13.05    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 4  | 30   | 28 July 2025        |                  | 1175.24         | 52.73         | 12.52    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 5  | 31   | 28 August 2025      |                  | 1121.97         | 53.27         | 11.98    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 6  | 31   | 28 September 2025   |                  | 1068.15         | 53.82         | 11.43    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 7  | 30   | 28 October 2025     |                  | 1013.79         | 54.36         | 10.89    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 8  | 31   | 28 November 2025    |                  |  958.87         | 54.92         | 10.33    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 9  | 30   | 28 December 2025    |                  |  903.39         | 55.48         |  9.77    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 10 | 31   | 28 January 2026     |                  |  847.35         | 56.04         |  9.21    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 11 | 31   | 28 February 2026    |                  |  790.74         | 56.61         |  8.64    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 12 | 28   | 28 March 2026       |                  |  733.55         | 57.19         |  8.06    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 13 | 31   | 28 April 2026       |                  |  675.78         | 57.77         |  7.48    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 14 | 30   | 28 May 2026         |                  |  617.42         | 58.36         |  6.89    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 15 | 31   | 28 June 2026        |                  |  558.46         | 58.96         |  6.29    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 16 | 30   | 28 July 2026        |                  |  498.9          | 59.56         |  5.69    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 17 | 31   | 28 August 2026      |                  |  438.73         | 60.17         |  5.08    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 18 | 31   | 28 September 2026   |                  |  377.95         | 60.78         |  4.47    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 19 | 30   | 28 October 2026     |                  |  316.55         | 61.4          |  3.85    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 20 | 31   | 28 November 2026    |                  |  254.53         | 62.02         |  3.23    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 21 | 30   | 28 December 2026    |                  |  191.87         | 62.66         |  2.59    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 22 | 31   | 28 January 2027     |                  |  128.58         | 63.29         |  1.96    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 23 | 31   | 28 February 2027    |                  |   64.64         | 63.94         |  1.31    | 0.0  | 0.0       | 65.25 | 0.0   | 0.0        | 0.0  | 65.25       |
      | 24 | 28   | 28 March 2027       |                  |    0.0          | 64.64         |  0.66    | 0.0  | 0.0       | 65.3  | 0.0   | 0.0        | 0.0  | 65.3        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1383.0        | 183.05   | 0.0  | 0.0       | 1566.05 |  0.0 | 0.0        | 0.0  | 1566.05     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 28 March 2025    | Disbursement     | 1383.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1383.0       | false    | false    |
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "29 March 2025" with 1383 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 24 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 28 March 2025       |                  | 1383.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 28 April 2025       |  29 March 2025   | 1383.0          | 0.0           |  0.45    | 0.0  | 0.0       | 0.45  | 0.45  | 0.45       | 0.0  | 0.0         |
      | 2  | 30   | 28 May 2025         |  29 March 2025   | 1383.0          | 0.0           |  0.0     | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 28 June 2025        |  29 March 2025   | 1370.25         | 12.75         |  0.0     | 0.0  | 0.0       | 12.75 | 12.75 | 12.75      | 0.0  | 0.0         |
      | 4  | 30   | 28 July 2025        |  29 March 2025   | 1305.0          | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 5  | 31   | 28 August 2025      |  29 March 2025   | 1239.75         | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 6  | 31   | 28 September 2025   |  29 March 2025   | 1174.5          | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 7  | 30   | 28 October 2025     |  29 March 2025   | 1109.25         | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 8  | 31   | 28 November 2025    |  29 March 2025   | 1044.0          | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 9  | 30   | 28 December 2025    |  29 March 2025   |  978.75         | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 10 | 31   | 28 January 2026     |  29 March 2025   |  913.5          | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 11 | 31   | 28 February 2026    |  29 March 2025   |  848.25         | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 12 | 28   | 28 March 2026       |  29 March 2025   |  783.0          | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 13 | 31   | 28 April 2026       |  29 March 2025   |  717.75          | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 14 | 30   | 28 May 2026         |  29 March 2025   |  652.5          | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 15 | 31   | 28 June 2026        |  29 March 2025   |  587.25         | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 16 | 30   | 28 July 2026        |  29 March 2025   |  522.0          | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 17 | 31   | 28 August 2026      |  29 March 2025   |  456.75         | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 18 | 31   | 28 September 2026   |  29 March 2025   |  391.5          | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 19 | 30   | 28 October 2026     |  29 March 2025   |  326.25         | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 20 | 31   | 28 November 2026    |  29 March 2025   |  261.0          | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 21 | 30   | 28 December 2026    |  29 March 2025   |  195.75         | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 21 | 30   | 28 December 2026    |  29 March 2025   |  195.75         | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 22 | 31   | 28 January 2027     |  29 March 2025   |  130.5          | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 23 | 31   | 28 February 2027    |  29 March 2025   |   65.25         | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
      | 24 | 28   | 28 March 2027       |  29 March 2025   |    0.0          | 65.25         |  0.0     | 0.0  | 0.0       | 65.25 | 65.25 | 65.25      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid     | In advance | Late | Outstanding |
      | 1383.0        | 0.45     | 0.0  | 0.0       | 1383.45 |  1383.45 | 1383.45    | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type           | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 28 March 2025    | Disbursement               | 1383.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1383.0       | false    | false    |
      | 29 March 2025    | Merchant Issued Refund     | 1383.0  | 1383.0    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2025    | Interest Refund            | 0.45    | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2025    | Accrual Activity           | 0.45    | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 29 March 2025    | Accrual                    | 0.45    | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount

  @TestRailId:C3584
  Scenario: Verify 2nd disbursement after loan was fully paid and closed (2 MIR, 1 CBR) - No interest, No interest recalculation
    When Admin sets the business date to "14 March 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_DP_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 14 March 2024     | 1000.0         | 0.0                    | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "14 March 2024" with "1000.0" amount and expected disbursement date on "14 March 2024"
    When Admin successfully disburse the loan on "14 March 2024" with "487.58" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       |                  | 243.79          | 121.89        | 0.0      | 0.0  | 0.0       | 121.89 | 0.0   | 0.0        | 0.0  | 121.89      |
      | 3  | 15   | 13 April 2024       |                  | 121.9           | 121.89        | 0.0      | 0.0  | 0.0       | 121.89 | 0.0   | 0.0        | 0.0  | 121.89      |
      | 4  | 15   | 28 April 2024       |                  | 0.0             | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 0.0   | 0.0        | 0.0  | 121.9       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 487.58        | 0.0      | 0.0  | 0.0       | 487.58  | 121.9 | 0.0        | 0.0  | 365.68      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement     | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment     | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
    When Admin sets the business date to "24 March 2024"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "24 March 2024" with 201.39 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       |                  | 243.79          | 121.89        | 0.0      | 0.0  | 0.0       | 121.89 | 0.0   | 0.0        | 0.0  | 121.89      |
      | 3  | 15   | 13 April 2024       |                  | 121.9           | 121.89        | 0.0      | 0.0  | 0.0       | 121.89 | 79.49 | 79.49      | 0.0  | 42.4        |
      | 4  | 15   | 28 April 2024       | 24 March 2024    | 0.0             | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9 | 121.9      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid     | In advance | Late | Outstanding |
      | 487.58        | 0.0      | 0.0  | 0.0       | 487.58  | 323.29   | 201.39     | 0.0  | 164.29      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39  | 201.39    | 0.0      | 0.0  | 0.0       | 164.29       | false    | false    |
    Then Loan status will be "ACTIVE"
    Then Loan has 164.29 outstanding amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "24 March 2024" with 286.19 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid    | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0     |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9   | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       | 24 March 2024    | 243.79          | 121.89        | 0.0      | 0.0  | 0.0       | 121.89 | 121.89  | 121.89     | 0.0  | 0.0         |
      | 3  | 15   | 13 April 2024       | 24 March 2024    | 121.9           | 121.89        | 0.0      | 0.0  | 0.0       | 121.89 | 121.89  | 121.89     | 0.0  | 0.0         |
      | 4  | 15   | 28 April 2024       | 24 March 2024    | 0.0             | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9   | 121.9      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid     | In advance | Late | Outstanding |
      | 487.58        | 0.0      | 0.0  | 0.0       | 487.58  | 487.58   | 365.68     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39  | 201.39    | 0.0      | 0.0  | 0.0       | 164.29       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 286.19  | 164.29    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "OVERPAID"
    Then Loan has 121.9 overpaid amount
    When Admin sets the business date to "25 March 2024"
    When Admin makes Credit Balance Refund transaction on "25 March 2024" with 121.9 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid    | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0     |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9   | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       | 24 March 2024    | 243.79          | 121.89        | 0.0      | 0.0  | 0.0       | 121.89 | 121.89  | 121.89     | 0.0  | 0.0         |
      | 3  | 15   | 13 April 2024       | 24 March 2024    | 121.9           | 121.89        | 0.0      | 0.0  | 0.0       | 121.89 | 121.89  | 121.89     | 0.0  | 0.0         |
      | 4  | 15   | 28 April 2024       | 24 March 2024    | 0.0             | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9   | 121.9      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid     | In advance | Late | Outstanding |
      | 487.58        | 0.0      | 0.0  | 0.0       | 487.58  | 487.58   | 365.68     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39  | 201.39    | 0.0      | 0.0  | 0.0       | 164.29       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 286.19  | 164.29    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Credit Balance Refund  | 121.9   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin sets the business date to "01 April 2024"
    When Admin successfully disburse the loan on "01 April 2024" with "312.69" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid    | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0     |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9   | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       | 24 March 2024    | 243.79          | 121.89        | 0.0      | 0.0  | 0.0       | 121.89 | 121.89  | 121.89     | 0.0  | 0.0         |
      |    |      | 01 April 2024       |                  | 312.69          |               |          | 0.0  |           | 0.0    | 0.0     |            |      |             |
      | 3  | 0    | 01 April 2024       | 01 April 2024    | 478.31          | 78.17         | 0.0      | 0.0  | 0.0       | 78.17  | 78.17   | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 13 April 2024       |                  | 239.15          | 239.16        | 0.0      | 0.0  | 0.0       | 239.16 | 121.89  | 121.89     | 0.0  | 117.27      |
      | 5  | 15   | 28 April 2024       |                  | 0.0             | 239.15        | 0.0      | 0.0  | 0.0       | 239.15 | 121.9   | 121.9      | 0.0  | 117.25      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid     | In advance | Late | Outstanding |
      | 800.27        | 0.0      | 0.0  | 0.0       | 800.27  | 565.75   | 365.68     | 0.0  | 234.52      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39  | 201.39    | 0.0      | 0.0  | 0.0       | 164.29       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 286.19  | 164.29    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Credit Balance Refund  | 121.9   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Disbursement           | 312.69  | 0.0       | 0.0      | 0.0  | 0.0       | 312.69       | false    | false    |
      | 01 April 2024    | Down Payment           | 78.17   | 78.17     | 0.0      | 0.0  | 0.0       | 234.52       | false    | false    |
    Then Loan status will be "ACTIVE"
    Then Loan has 234.52 outstanding amount
    When Admin sets the business date to "10 April 2024"
    When Loan Pay-off is made on "10 April 2024"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid    | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0     |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9   | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       | 24 March 2024    | 243.79          | 121.89        | 0.0      | 0.0  | 0.0       | 121.89 | 121.89  | 121.89     | 0.0  | 0.0         |
      |    |      | 01 April 2024       |                  | 312.69          |               |          | 0.0  |           | 0.0    | 0.0     |            |      |             |
      | 3  | 0    | 01 April 2024       | 01 April 2024    | 478.31          | 78.17         | 0.0      | 0.0  | 0.0       | 78.17  | 78.17   | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 13 April 2024       | 10 April 2024    | 239.15          | 239.16        | 0.0      | 0.0  | 0.0       | 239.16 | 239.16  | 239.16     | 0.0  | 0.0         |
      | 5  | 15   | 28 April 2024       | 10 April 2024    | 0.0             | 239.15        | 0.0      | 0.0  | 0.0       | 239.15 | 239.15  | 239.15     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid     | In advance | Late | Outstanding |
      | 800.27        | 0.0      | 0.0  | 0.0       | 800.27  | 800.27   | 600.2      | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39  | 201.39    | 0.0      | 0.0  | 0.0       | 164.29       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 286.19  | 164.29    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Credit Balance Refund  | 121.9   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Disbursement           | 312.69  | 0.0       | 0.0      | 0.0  | 0.0       | 312.69       | false    | false    |
      | 01 April 2024    | Down Payment           | 78.17   | 78.17     | 0.0      | 0.0  | 0.0       | 234.52       | false    | false    |
      | 10 April 2024    | Repayment              | 234.52  | 234.52    | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount

  @TestRailId:C3585
  Scenario: Verify 2nd disbursement after loan was fully paid and closed (2 MIR, 1 CBR) - 10% interest, No interest recalculation
    When Admin sets the business date to "14 March 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_DP_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 14 March 2024     | 1000.0         | 10.0                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "14 March 2024" with "1000.0" amount and expected disbursement date on "14 March 2024"
    When Admin successfully disburse the loan on "14 March 2024" with "487.58" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       |                  | 244.29          | 121.39        | 1.5      | 0.0  | 0.0       | 122.89 | 0.0   | 0.0        | 0.0  | 122.89      |
      | 3  | 15   | 13 April 2024       |                  | 122.4           | 121.89        | 1.0      | 0.0  | 0.0       | 122.89 | 0.0   | 0.0        | 0.0  | 122.89      |
      | 4  | 15   | 28 April 2024       |                  | 0.0             | 122.4         | 0.5      | 0.0  | 0.0       | 122.9  | 0.0   | 0.0        | 0.0  | 122.9       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 487.58        | 3.0      | 0.0  | 0.0       | 490.58  | 121.9 | 0.0        | 0.0  | 368.68      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement     | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment     | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
    When Admin sets the business date to "24 March 2024"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "24 March 2024" with 201.39 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       |                  | 244.29          | 121.39        | 1.5      | 0.0  | 0.0       | 122.89 | 0.0   | 0.0        | 0.0  | 122.89      |
      | 3  | 15   | 13 April 2024       |                  | 122.4           | 121.89        | 1.0      | 0.0  | 0.0       | 122.89 | 78.49 | 78.49      | 0.0  | 44.4        |
      | 4  | 15   | 28 April 2024       | 24 March 2024    | 0.0             | 122.4         | 0.5      | 0.0  | 0.0       | 122.9  | 122.9 | 122.9      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid     | In advance | Late | Outstanding |
      | 487.58        | 3.0      | 0.0  | 0.0       | 490.58  | 323.29   | 201.39     | 0.0  | 167.29      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39  | 199.89    | 1.5      | 0.0  | 0.0       | 165.79       | false    | false    |
    Then Loan status will be "ACTIVE"
    Then Loan has 167.29 outstanding amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "24 March 2024" with 286.19 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9  | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       | 24 March 2024    | 244.29          | 121.39        | 1.5      | 0.0  | 0.0       | 122.89 | 122.89 | 122.89     | 0.0  | 0.0         |
      | 3  | 15   | 13 April 2024       | 24 March 2024    | 122.4           | 121.89        | 1.0      | 0.0  | 0.0       | 122.89 | 122.89 | 122.89     | 0.0  | 0.0         |
      | 4  | 15   | 28 April 2024       | 24 March 2024    | 0.0             | 122.4         | 0.5      | 0.0  | 0.0       | 122.9  | 122.9  | 122.9      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid     | In advance | Late | Outstanding |
      | 487.58        | 3.0      | 0.0  | 0.0       | 490.58  | 490.58   | 368.68     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39  | 199.89    | 1.5      | 0.0  | 0.0       | 165.79       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 286.19  | 165.79    | 1.5      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual                | 3.0     | 0.0       | 3.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "OVERPAID"
    Then Loan has 118.9 overpaid amount
    When Admin sets the business date to "25 March 2024"
    When Admin makes Credit Balance Refund transaction on "25 March 2024" with 118 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9  | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       | 24 March 2024    | 244.29          | 121.39        | 1.5      | 0.0  | 0.0       | 122.89 | 122.89 | 122.89     | 0.0  | 0.0         |
      | 3  | 15   | 13 April 2024       | 24 March 2024    | 122.4           | 121.89        | 1.0      | 0.0  | 0.0       | 122.89 | 122.89 | 122.89     | 0.0  | 0.0         |
      | 4  | 15   | 28 April 2024       | 24 March 2024    | 0.0             | 122.4         | 0.5      | 0.0  | 0.0       | 122.9  | 122.9  | 122.9      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid     | In advance | Late | Outstanding |
      | 487.58        | 3.0      | 0.0  | 0.0       | 490.58  | 490.58   | 368.68     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39  | 199.89    | 1.5      | 0.0  | 0.0       | 165.79       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 286.19  | 165.79    | 1.5      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual                | 3.0     | 0.0       | 3.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Credit Balance Refund  | 118.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "OVERPAID"
    Then Loan has 0.9 overpaid amount
    When Admin sets the business date to "01 April 2024"
    When Admin successfully disburse the loan on "01 April 2024" with "312.69" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid    | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9  | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       | 24 March 2024    | 244.29          | 121.39        | 1.5      | 0.0  | 0.0       | 122.89 | 122.89 | 122.89     | 0.0  | 0.0         |
      |    |      | 01 April 2024       |                  | 312.69          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 3  | 0    | 01 April 2024       | 01 April 2024    | 478.81          | 78.17         | 0.0      | 0.0  | 0.0       | 78.17  | 78.17  | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 13 April 2024       |                  | 239.8           | 239.01        | 1.77     | 0.0  | 0.0       | 240.78 | 122.89 | 122.89     | 0.0  | 117.89      |
      | 5  | 15   | 28 April 2024       |                  | 0.0             | 239.8         | 0.98     | 0.0  | 0.0       | 240.78 | 122.9  | 122.9      | 0.0  | 117.88      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid     | In advance | Late | Outstanding |
      | 800.27        | 4.25     | 0.0  | 0.0       | 804.52  | 568.75   | 368.68     | 0.0  | 235.77      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39  | 199.89    | 1.5      | 0.0  | 0.0       | 165.79       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 286.19  | 165.79    | 1.5      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual                | 3.0     | 0.0       | 3.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Credit Balance Refund  | 118.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Disbursement           | 312.69  | 0.0       | 0.0      | 0.0  | 0.0       | 311.79       | false    | false    |
      | 01 April 2024    | Down Payment           | 77.27   | 77.27     | 0.0      | 0.0  | 0.0       | 234.52       | false    | false    |
    Then Loan status will be "ACTIVE"
    Then Loan has 235.77 outstanding amount
    When Admin sets the business date to "10 April 2024"
    When Loan Pay-off is made on "10 April 2024"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid    | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9  | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       | 24 March 2024    | 244.29          | 121.39        | 1.5      | 0.0  | 0.0       | 122.89 | 122.89 | 122.89     | 0.0  | 0.0         |
      |    |      | 01 April 2024       |                  | 312.69          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 3  | 0    | 01 April 2024       | 01 April 2024    | 478.81          | 78.17         | 0.0      | 0.0  | 0.0       | 78.17  | 78.17  | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 13 April 2024       | 10 April 2024    | 239.8           | 239.01        | 1.77     | 0.0  | 0.0       | 240.78 | 240.78 | 240.78     | 0.0  | 0.0         |
      | 5  | 15   | 28 April 2024       | 10 April 2024    | 0.0             | 239.8         | 0.98     | 0.0  | 0.0       | 240.78 | 240.78 | 240.78     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid     | In advance | Late | Outstanding |
      | 800.27        | 4.25     | 0.0  | 0.0       | 804.52  | 804.52   | 604.45     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39  | 199.89    | 1.5      | 0.0  | 0.0       | 165.79       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 286.19  | 165.79    | 1.5      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual                | 3.0     | 0.0       | 3.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 25 March 2024    | Credit Balance Refund  | 118.0   | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Disbursement           | 312.69  | 0.0       | 0.0      | 0.0  | 0.0       | 311.79       | false    | false    |
      | 01 April 2024    | Down Payment           | 77.27   | 77.27     | 0.0      | 0.0  | 0.0       | 234.52       | false    | false    |
      | 10 April 2024    | Repayment              | 235.77  | 234.52    | 1.25     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2024    | Accrual                | 1.25    | 0.0       | 1.25     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount

  @TestRailId:C3586
  Scenario: Verify 2nd disbursement after loan was fully paid and closed (2 MIR, 1 CBR) - 33.33% interest with interest recalculation
    When Admin sets the business date to "14 March 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_DP_IR_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 14 March 2024     | 1000.0         | 33.33                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "14 March 2024" with "1000.0" amount and expected disbursement date on "14 March 2024"
    When Admin successfully disburse the loan on "14 March 2024" with "487.58" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       |                  | 245.46          | 120.22        | 5.08     | 0.0  | 0.0       | 125.3  | 0.0   | 0.0        | 0.0  | 125.3       |
      | 3  | 15   | 13 April 2024       |                  | 123.57          | 121.89        | 3.41     | 0.0  | 0.0       | 125.3  | 0.0   | 0.0        | 0.0  | 125.3       |
      | 4  | 15   | 28 April 2024       |                  | 0.0             | 123.57        | 1.72     | 0.0  | 0.0       | 125.29 | 0.0   | 0.0        | 0.0  | 125.29      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 487.58        | 10.21    | 0.0  | 0.0       | 497.79  | 121.9 | 0.0        | 0.0  | 375.89      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement     | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment     | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
    When Admin sets the business date to "24 March 2024"
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "24 March 2024" with 201.39 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9  | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       |                  | 244.53          | 121.15        | 4.15     | 0.0  | 0.0       | 125.3  | 0.0    | 0.0        | 0.0  | 125.3       |
      | 3  | 15   | 13 April 2024       |                  | 125.29          | 119.24        | 0.6      | 0.0  | 0.0       | 119.84 | 76.1   | 76.1       | 0.0  | 43.74       |
      | 4  | 15   | 28 April 2024       | 24 March 2024    | 0.0             | 125.29        | 0.0      | 0.0  | 0.0       | 125.29 | 125.29 | 125.29     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 487.58        | 4.75     | 0.0  | 0.0       | 492.33  | 323.29 | 201.39     | 0.0  | 169.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39  | 201.39    | 0.0      | 0.0  | 0.0       | 164.29       | false    | false    |
    Then Loan status will be "ACTIVE"
    Then Loan has 169.04 outstanding amount
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "24 March 2024" with 286.19 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid    | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0     |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9   | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       | 24 March 2024    | 250.59          | 115.09        | 3.39     | 0.0  | 0.0       | 118.48 | 118.48  | 118.48     | 0.0  | 0.0         |
      | 3  | 15   | 13 April 2024       | 24 March 2024    | 125.29          | 125.3         | 0.0      | 0.0  | 0.0       | 125.3  | 125.3   | 125.3      | 0.0  | 0.0         |
      | 4  | 15   | 28 April 2024       | 24 March 2024    | 0.0             | 125.29        | 0.0      | 0.0  | 0.0       | 125.29 | 125.29  | 125.29     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid     | In advance | Late | Outstanding |
      | 487.58        | 3.39     | 0.0  | 0.0       | 490.97  | 490.97   | 369.07     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39  | 201.39    | 0.0      | 0.0  | 0.0       | 164.29       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 286.19  | 164.29    | 3.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual                | 3.39    | 0.0       | 3.39     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "OVERPAID"
    Then Loan has 118.51 overpaid amount
    When Admin makes Credit Balance Refund transaction on "24 March 2024" with 11.9 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid    | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0     |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9   | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       | 24 March 2024    | 250.59          | 115.09        | 3.39     | 0.0  | 0.0       | 118.48 | 118.48  | 118.48     | 0.0  | 0.0         |
      | 3  | 15   | 13 April 2024       | 24 March 2024    | 125.29          | 125.3         | 0.0      | 0.0  | 0.0       | 125.3  | 125.3   | 125.3      | 0.0  | 0.0         |
      | 4  | 15   | 28 April 2024       | 24 March 2024    | 0.0             | 125.29        | 0.0      | 0.0  | 0.0       | 125.29 | 125.29  | 125.29     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid     | In advance | Late | Outstanding |
      | 487.58        | 3.39     | 0.0  | 0.0       | 490.97  | 490.97   | 369.07     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39  | 201.39    | 0.0      | 0.0  | 0.0       | 164.29       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 286.19  | 164.29    | 3.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual                | 3.39    | 0.0       | 3.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Credit Balance Refund  | 11.9    | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "OVERPAID"
    Then Loan has 106.61 overpaid amount
    When Admin sets the business date to "01 April 2024"
    When Admin successfully disburse the loan on "01 April 2024" with "312.69" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid    | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0     |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9   | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       | 24 March 2024    | 250.59          | 115.09        | 3.39     | 0.0  | 0.0       | 118.48 | 118.48  | 118.48     | 0.0  | 0.0         |
      |    |      | 01 April 2024       |                  | 312.69          |               |          | 0.0  |           | 0.0    | 0.0     |            |      |             |
      | 3  | 0    | 01 April 2024       | 01 April 2024    | 485.11          | 78.17         | 0.0      | 0.0  | 0.0       | 78.17  | 78.17   | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 13 April 2024       |                  | 239.25          | 245.86        | 2.29     | 0.0  | 0.0       | 248.15 | 139.52  | 139.52     | 0.0  | 108.63      |
      | 5  | 15   | 28 April 2024       |                  | 0.0             | 239.25        | 1.39     | 0.0  | 0.0       | 240.64 | 139.51  | 139.51     | 0.0  | 101.13      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid     | In advance | Late | Outstanding |
      | 800.27        | 7.07     | 0.0  | 0.0       | 807.34  | 597.58   | 397.51     | 0.0  | 209.76      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39  | 201.39    | 0.0      | 0.0  | 0.0       | 164.29       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 286.19  | 164.29    | 3.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual                | 3.39    | 0.0       | 3.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Credit Balance Refund  | 11.9    | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Disbursement           | 312.69  | 0.0       | 0.0      | 0.0  | 0.0       | 206.08       | false    | false    |
    Then Loan status will be "ACTIVE"
    Then Loan has 209.76 outstanding amount
    When Admin sets the business date to "10 April 2024"
    When Loan Pay-off is made on "10 April 2024"
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date                | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid    | In advance | Late | Outstanding |
      |    |      | 14 March 2024       |                  | 487.58          |               |          | 0.0  |           | 0.0    | 0.0     |            |      |             |
      | 1  | 0    | 14 March 2024       | 14 March 2024    | 365.68          | 121.9         | 0.0      | 0.0  | 0.0       | 121.9  | 121.9   | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 29 March 2024       | 24 March 2024    | 250.59          | 115.09        | 3.39     | 0.0  | 0.0       | 118.48 | 118.48  | 118.48     | 0.0  | 0.0         |
      |    |      | 01 April 2024       |                  | 312.69          |               |          | 0.0  |           | 0.0    | 0.0     |            |      |             |
      | 3  | 0    | 01 April 2024       | 01 April 2024    | 485.11          | 78.17         | 0.0      | 0.0  | 0.0       | 78.17  | 78.17   | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 13 April 2024       | 10 April 2024    | 241.69          | 243.42        | 1.72     | 0.0  | 0.0       | 245.14 | 245.14  | 245.14     | 0.0  | 0.0         |
      | 5  | 15   | 28 April 2024       | 10 April 2024    | 0.0             | 241.69        | 0.0      | 0.0  | 0.0       | 241.69 | 241.69  | 241.69     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid     | In advance | Late | Outstanding |
      | 800.27        | 5.11     | 0.0  | 0.0       | 805.38  | 805.38   | 605.31     | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 14 March 2024    | Disbursement           | 487.58  | 0.0       | 0.0      | 0.0  | 0.0       | 487.58       | false    | false    |
      | 14 March 2024    | Down Payment           | 121.9   | 121.9     | 0.0      | 0.0  | 0.0       | 365.68       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 201.39  | 201.39    | 0.0      | 0.0  | 0.0       | 164.29       | false    | false    |
      | 24 March 2024    | Merchant Issued Refund | 286.19  | 164.29    | 3.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Accrual                | 3.39    | 0.0       | 3.39     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 24 March 2024    | Credit Balance Refund  | 11.9    | 0.0       | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Disbursement           | 312.69  | 0.0       | 0.0      | 0.0  | 0.0       | 206.08       | false    | false    |
      | 10 April 2024    | Repayment              | 207.8   | 206.08    | 1.72     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 10 April 2024    | Accrual                | 1.72    | 0.0       | 1.72     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount

  @TestRailId:C3700
  Scenario: Verify repayment schedule and accrual transactions created after penalty is added for paid off loan on maturity date - UC1
    When Admin sets the business date to "08 May 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 08 May 2024       | 1000           | 12.19                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "08 May 2024" with "1000" amount and expected disbursement date on "08 May 2024"
    When Admin successfully disburse the loan on "08 May 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      |    |      | 08 May 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0      | 0.0  |            |      |             |
      | 1  | 31   | 08 June 2024     |           | 670.03          | 329.97        | 10.16    | 0.0  | 0.0       | 340.13   | 0.0  | 0.0        | 0.0  | 340.13      |
      | 2  | 30   | 08 July 2024     |           | 336.71          | 333.32        |  6.81    | 0.0  | 0.0       | 340.13   | 0.0  | 0.0        | 0.0  | 340.13      |
      | 3  | 31   | 08 August 2024   |           | 0.0             | 336.71        |  3.42    | 0.0  | 0.0       | 340.13   | 0.0  | 0.0        | 0.0  | 340.13      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.39    | 0.0  | 0.0       | 1020.39 | 0.0  | 0.0        | 0.0  | 1020.39     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 08 May 2024      | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "08 June 2024"
    And Customer makes "AUTOPAY" repayment on "08 June 2024" with 340.13 EUR transaction amount
    When Admin sets the business date to "08 July 2024"
    And Customer makes "AUTOPAY" repayment on "08 July 2024" with 340.13 EUR transaction amount
    When Admin sets the business date to "08 August 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "08 August 2024" with 340.13 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid   | In advance | Late | Outstanding |
      |    |      | 08 May 2024      |                | 1000.0          |               |          | 0.0  |           | 0.0      | 0.0    |            |      |             |
      | 1  | 31   | 08 June 2024     | 08 June 2024   | 670.03          | 329.97        | 10.16    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 08 July 2024     | 08 July 2024   | 336.71          | 333.32        |  6.81    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 08 August 2024   | 08 August 2024 | 0.0             | 336.71        |  3.42    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 20.39    | 0.0  | 0.0       | 1020.39 | 1020.39 | 0.0        | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 08 May 2024      | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 08 June 2024     | Repayment        | 340.13  | 329.97    | 10.16    | 0.0  | 0.0       | 670.03       | false    | false    |
      | 08 June 2024     | Accrual Activity | 10.16   | 0.0       | 10.16    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 July 2024     | Repayment        | 340.13  | 333.32    | 6.81     | 0.0  | 0.0       | 336.71       | false    | false    |
      | 08 July 2024     | Accrual Activity | 6.81    | 0.0       | 6.81     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2024   | Accrual          | 20.28   | 0.0       | 20.28    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2024   | Repayment        | 340.13  | 336.71    | 3.42     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2024   | Accrual          | 0.11    | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2024   | Accrual Activity | 3.42    | 0.0       | 3.42     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- add penalty for paid off loan on maturity date ---#
    When Admin adds "LOAN_NSF_FEE" due date charge with "08 August 2024" due date and 2.8 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid   | In advance | Late | Outstanding |
      |    |      | 08 May 2024      |                | 1000.0          |               |          | 0.0  |           | 0.0      | 0.0    |            |      |             |
      | 1  | 31   | 08 June 2024     | 08 June 2024   | 670.03          | 329.97        | 10.16    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 08 July 2024     | 08 July 2024   | 336.71          | 333.32        |  6.81    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 08 August 2024   |                | 0.0             | 336.71        |  3.42    | 0.0  | 2.8       | 342.93   | 340.13 | 0.0        | 0.0  | 2.8         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 20.39    | 0.0  | 2.8       | 1023.19 | 1020.39 | 0.0        | 0.0  | 2.8         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 08 May 2024      | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 08 June 2024     | Repayment        | 340.13  | 329.97    | 10.16    | 0.0  | 0.0       | 670.03       | false    | false    |
      | 08 June 2024     | Accrual Activity | 10.16   | 0.0       | 10.16    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 July 2024     | Repayment        | 340.13  | 333.32    | 6.81     | 0.0  | 0.0       | 336.71       | false    | false    |
      | 08 July 2024     | Accrual Activity | 6.81    | 0.0       | 6.81     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2024   | Accrual          | 20.28   | 0.0       | 20.28    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2024   | Repayment        | 340.13  | 336.71    | 3.42     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2024   | Accrual          | 0.11    | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "09 August 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid   | In advance | Late | Outstanding |
      |    |      | 08 May 2024      |                | 1000.0          |               |          | 0.0  |           | 0.0      | 0.0    |            |      |             |
      | 1  | 31   | 08 June 2024     | 08 June 2024   | 670.03          | 329.97        | 10.16    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 08 July 2024     | 08 July 2024   | 336.71          | 333.32        |  6.81    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 08 August 2024   |                | 0.0             | 336.71        |  3.42    | 0.0  | 2.8       | 342.93   | 340.13 | 0.0        | 0.0  | 2.8         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 20.39    | 0.0  | 2.8       | 1023.19 | 1020.39 | 0.0        | 0.0  | 2.8         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 08 May 2024      | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 08 June 2024     | Repayment        | 340.13  | 329.97    | 10.16    | 0.0  | 0.0       | 670.03       | false    | false    |
      | 08 June 2024     | Accrual Activity | 10.16   | 0.0       | 10.16    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 July 2024     | Repayment        | 340.13  | 333.32    | 6.81     | 0.0  | 0.0       | 336.71       | false    | false    |
      | 08 July 2024     | Accrual Activity | 6.81    | 0.0       | 6.81     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2024   | Accrual          | 20.28   | 0.0       | 20.28    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2024   | Repayment        | 340.13  | 336.71    | 3.42     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2024   | Accrual          | 0.11    | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2024   | Accrual          | 2.8     | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |
      | 08 August 2024   | Accrual Activity | 6.22    | 0.0       | 3.42     | 0.0  | 2.8       | 0.0          | false    | false    |
    When Admin sets the business date to "15 August 2024"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 August 2024" with 2.8 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid   | In advance | Late | Outstanding |
      |    |      | 08 May 2024      |                | 1000.0          |               |          | 0.0  |           | 0.0      | 0.0    |            |      |             |
      | 1  | 31   | 08 June 2024     | 08 June 2024   | 670.03          | 329.97        | 10.16    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 08 July 2024     | 08 July 2024   | 336.71          | 333.32        |  6.81    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 08 August 2024   | 15 August 2024 | 0.0             | 336.71        |  3.42    | 0.0  | 2.8       | 342.93   | 342.93 | 0.0        | 2.8  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 20.39    | 0.0  | 2.8       | 1023.19 | 1023.19 | 0.0        | 2.8  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 08 May 2024      | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 08 June 2024     | Repayment        | 340.13  | 329.97    | 10.16    | 0.0  | 0.0       | 670.03       | false    | false    |
      | 08 June 2024     | Accrual Activity | 10.16   | 0.0       | 10.16    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 July 2024     | Repayment        | 340.13  | 333.32    | 6.81     | 0.0  | 0.0       | 336.71       | false    | false    |
      | 08 July 2024     | Accrual Activity | 6.81    | 0.0       | 6.81     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2024   | Accrual          | 20.28   | 0.0       | 20.28    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2024   | Repayment        | 340.13  | 336.71    | 3.42     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2024   | Accrual          | 0.11    | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2024   | Accrual          | 2.8     | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |
      | 08 August 2024   | Accrual Activity | 6.22    | 0.0       | 3.42     | 0.0  | 2.8       | 0.0          | false    | false    |
      | 15 August 2024   | Repayment        | 2.8     | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |
    When Admin sets the business date to "16 August 2024"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid   | In advance | Late | Outstanding |
      |    |      | 08 May 2024      |                | 1000.0          |               |          | 0.0  |           | 0.0      | 0.0    |            |      |             |
      | 1  | 31   | 08 June 2024     | 08 June 2024   | 670.03          | 329.97        | 10.16    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 08 July 2024     | 08 July 2024   | 336.71          | 333.32        |  6.81    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 08 August 2024   | 15 August 2024 | 0.0             | 336.71        |  3.42    | 0.0  | 2.8       | 342.93   | 342.93 | 0.0        | 2.8  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 20.39    | 0.0  | 2.8       | 1023.19 | 1023.19 | 0.0        | 2.8  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 08 May 2024      | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 08 June 2024     | Repayment        | 340.13  | 329.97    | 10.16    | 0.0  | 0.0       | 670.03       | false    | false    |
      | 08 June 2024     | Accrual Activity | 10.16   | 0.0       | 10.16    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 July 2024     | Repayment        | 340.13  | 333.32    | 6.81     | 0.0  | 0.0       | 336.71       | false    | false    |
      | 08 July 2024     | Accrual Activity | 6.81    | 0.0       | 6.81     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2024   | Accrual          | 20.28   | 0.0       | 20.28    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2024   | Repayment        | 340.13  | 336.71    | 3.42     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2024   | Accrual          | 0.11    | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2024   | Accrual          | 2.8     | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |
      | 08 August 2024   | Accrual Activity | 6.22    | 0.0       | 3.42     | 0.0  | 2.8       | 0.0          | false    | false    |
      | 15 August 2024   | Repayment        | 2.8     | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |

  @TestRailId:C3701
  Scenario: Verify repayment schedule and accrual transactions created after penalty is added for paid off loan with accrual activity on maturity date - UC2
    When Admin sets the business date to "08 May 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_360_30_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF_ACCRUAL_ACTIVITY | 08 May 2025       | 1000           | 12.19                  | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "08 May 2025" with "1000" amount and expected disbursement date on "08 May 2025"
    When Admin successfully disburse the loan on "08 May 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      |    |      | 08 May 2025      |           | 1000.0          |               |          | 0.0  |           | 0.0      | 0.0  |            |      |             |
      | 1  | 31   | 08 June 2025     |           | 670.03          | 329.97        | 10.16    | 0.0  | 0.0       | 340.13   | 0.0  | 0.0        | 0.0  | 340.13      |
      | 2  | 30   | 08 July 2025     |           | 336.71          | 333.32        |  6.81    | 0.0  | 0.0       | 340.13   | 0.0  | 0.0        | 0.0  | 340.13      |
      | 3  | 31   | 08 August 2025   |           | 0.0             | 336.71        |  3.42    | 0.0  | 0.0       | 340.13   | 0.0  | 0.0        | 0.0  | 340.13      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.39    | 0.0  | 0.0       | 1020.39 | 0.0  | 0.0        | 0.0  | 1020.39     |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 08 May 2025      | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    When Admin sets the business date to "08 June 2025"
    And Customer makes "AUTOPAY" repayment on "08 June 2025" with 340.13 EUR transaction amount
    When Admin sets the business date to "08 July 2025"
    And Customer makes "AUTOPAY" repayment on "08 July 2025" with 340.13 EUR transaction amount
    When Admin sets the business date to "08 August 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "08 August 2025" with 340.13 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid   | In advance | Late | Outstanding |
      |    |      | 08 May 2025      |                | 1000.0          |               |          | 0.0  |           | 0.0      | 0.0    |            |      |             |
      | 1  | 31   | 08 June 2025     | 08 June 2025   | 670.03          | 329.97        | 10.16    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 08 July 2025     | 08 July 2025   | 336.71          | 333.32        |  6.81    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 08 August 2025   | 08 August 2025 | 0.0             | 336.71        |  3.42    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 20.39    | 0.0  | 0.0       | 1020.39 | 1020.39 | 0.0        | 0.0  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 08 May 2025      | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 08 June 2025     | Repayment        | 340.13  | 329.97    | 10.16    | 0.0  | 0.0       | 670.03       | false    | false    |
      | 08 June 2025     | Accrual Activity | 10.16   | 0.0       | 10.16    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 July 2025     | Repayment        | 340.13  | 333.32    | 6.81     | 0.0  | 0.0       | 336.71       | false    | false    |
      | 08 July 2025     | Accrual Activity | 6.81    | 0.0       | 6.81     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2025   | Accrual          | 20.28   | 0.0       | 20.28    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025   | Repayment        | 340.13  | 336.71    | 3.42     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025   | Accrual          | 0.11    | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025   | Accrual Activity | 3.42    | 0.0       | 3.42     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- add penalty for paid off loan on maturity date ---#
    When Admin adds "LOAN_NSF_FEE" due date charge with "08 August 2025" due date and 2.8 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid   | In advance | Late | Outstanding |
      |    |      | 08 May 2025      |                | 1000.0          |               |          | 0.0  |           | 0.0      | 0.0    |            |      |             |
      | 1  | 31   | 08 June 2025     | 08 June 2025   | 670.03          | 329.97        | 10.16    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 08 July 2025     | 08 July 2025   | 336.71          | 333.32        |  6.81    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 08 August 2025   |                | 0.0             | 336.71        |  3.42    | 0.0  | 2.8       | 342.93   | 340.13 | 0.0        | 0.0  | 2.8         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 20.39    | 0.0  | 2.8       | 1023.19 | 1020.39 | 0.0        | 0.0  | 2.8         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 08 May 2025      | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 08 June 2025     | Repayment        | 340.13  | 329.97    | 10.16    | 0.0  | 0.0       | 670.03       | false    | false    |
      | 08 June 2025     | Accrual Activity | 10.16   | 0.0       | 10.16    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 July 2025     | Repayment        | 340.13  | 333.32    | 6.81     | 0.0  | 0.0       | 336.71       | false    | false    |
      | 08 July 2025     | Accrual Activity | 6.81    | 0.0       | 6.81     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2025   | Accrual          | 20.28   | 0.0       | 20.28    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025   | Repayment        | 340.13  | 336.71    | 3.42     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025   | Accrual          | 0.11    | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "09 August 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid   | In advance | Late | Outstanding |
      |    |      | 08 May 2025      |                | 1000.0          |               |          | 0.0  |           | 0.0      | 0.0    |            |      |             |
      | 1  | 31   | 08 June 2025     | 08 June 2025   | 670.03          | 329.97        | 10.16    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 08 July 2025     | 08 July 2025   | 336.71          | 333.32        |  6.81    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 08 August 2025   |                | 0.0             | 336.71        |  3.42    | 0.0  | 2.8       | 342.93   | 340.13 | 0.0        | 0.0  | 2.8         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 20.39    | 0.0  | 2.8       | 1023.19 | 1020.39 | 0.0        | 0.0  | 2.8         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 08 May 2025      | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 08 June 2025     | Repayment        | 340.13  | 329.97    | 10.16    | 0.0  | 0.0       | 670.03       | false    | false    |
      | 08 June 2025     | Accrual Activity | 10.16   | 0.0       | 10.16    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 July 2025     | Repayment        | 340.13  | 333.32    | 6.81     | 0.0  | 0.0       | 336.71       | false    | false    |
      | 08 July 2025     | Accrual Activity | 6.81    | 0.0       | 6.81     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2025   | Accrual          | 20.28   | 0.0       | 20.28    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025   | Repayment        | 340.13  | 336.71    | 3.42     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025   | Accrual          | 0.11    | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025   | Accrual          | 2.8     | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |
      | 08 August 2025   | Accrual Activity | 6.22    | 0.0       | 3.42     | 0.0  | 2.8       | 0.0          | false    | false    |
    When Admin sets the business date to "15 August 2025"
    And Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 August 2025" with 2.8 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid   | In advance | Late | Outstanding |
      |    |      | 08 May 2025      |                | 1000.0          |               |          | 0.0  |           | 0.0      | 0.0    |            |      |             |
      | 1  | 31   | 08 June 2025     | 08 June 2025   | 670.03          | 329.97        | 10.16    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 08 July 2025     | 08 July 2025   | 336.71          | 333.32        |  6.81    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 08 August 2025   | 15 August 2025 | 0.0             | 336.71        |  3.42    | 0.0  | 2.8       | 342.93   | 342.93 | 0.0        | 2.8  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 20.39    | 0.0  | 2.8       | 1023.19 | 1023.19 | 0.0        | 2.8  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 08 May 2025      | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 08 June 2025     | Repayment        | 340.13  | 329.97    | 10.16    | 0.0  | 0.0       | 670.03       | false    | false    |
      | 08 June 2025     | Accrual Activity | 10.16   | 0.0       | 10.16    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 July 2025     | Repayment        | 340.13  | 333.32    | 6.81     | 0.0  | 0.0       | 336.71       | false    | false    |
      | 08 July 2025     | Accrual Activity | 6.81    | 0.0       | 6.81     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2025   | Accrual          | 20.28   | 0.0       | 20.28    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025   | Repayment        | 340.13  | 336.71    | 3.42     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025   | Accrual          | 0.11    | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025   | Accrual          | 2.8     | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |
      | 08 August 2025   | Accrual Activity | 6.22    | 0.0       | 3.42     | 0.0  | 2.8       | 0.0          | false    | false    |
      | 08 August 2025   | Accrual          | 2.8     | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |
    When Admin sets the business date to "16 August 2025"
    And Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due      | Paid   | In advance | Late | Outstanding |
      |    |      | 08 May 2025      |                | 1000.0          |               |          | 0.0  |           | 0.0      | 0.0    |            |      |             |
      | 1  | 31   | 08 June 2025     | 08 June 2025   | 670.03          | 329.97        | 10.16    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 08 July 2025     | 08 July 2025   | 336.71          | 333.32        |  6.81    | 0.0  | 0.0       | 340.13   | 340.13 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 08 August 2025   | 15 August 2025 | 0.0             | 336.71        |  3.42    | 0.0  | 2.8       | 342.93   | 342.93 | 0.0        | 2.8  | 0.0         |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000.0        | 20.39    | 0.0  | 2.8       | 1023.19 | 1023.19 | 0.0        | 2.8  | 0.0         |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 08 May 2025      | Disbursement     | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 08 June 2025     | Repayment        | 340.13  | 329.97    | 10.16    | 0.0  | 0.0       | 670.03       | false    | false    |
      | 08 June 2025     | Accrual Activity | 10.16   | 0.0       | 10.16    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 July 2025     | Repayment        | 340.13  | 333.32    | 6.81     | 0.0  | 0.0       | 336.71       | false    | false    |
      | 08 July 2025     | Accrual Activity | 6.81    | 0.0       | 6.81     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 07 August 2025   | Accrual          | 20.28   | 0.0       | 20.28    | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025   | Repayment        | 340.13  | 336.71    | 3.42     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025   | Accrual          | 0.11    | 0.0       | 0.11     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 08 August 2025   | Accrual          | 2.8     | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |
      | 08 August 2025   | Accrual Activity | 6.22    | 0.0       | 3.42     | 0.0  | 2.8       | 0.0          | false    | false    |
      | 15 August 2025   | Repayment        | 2.8     | 0.0       | 0.0      | 0.0  | 2.8       | 0.0          | false    | false    |



  @TestRailId:C3963
  Scenario: Verify Progressive Loan reschedule by extending repayment period: Basic scenario without downpayment, flat interest type
    When Admin sets the business date to "01 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 June 2024      | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    When Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 667.0           | 333.0         | 0.0      | 0.0  | 0.0       | 333.0 | 0.0  | 0.0        | 0.0  | 333.0       |
      | 2  | 31   | 01 August 2024    |           | 334.0           | 333.0         | 0.0      | 0.0  | 0.0       | 333.0 | 0.0  | 0.0        | 0.0  | 333.0       |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 334.0         | 0.0      | 0.0  | 0.0       | 334.0 | 0.0  | 0.0        | 0.0  | 334.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "01 July 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 August 2024     | 01 July 2024    |                 |                  |                 | 1          |                 |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 667.0           | 333.0         | 0.0      | 0.0  | 0.0       | 333.0 | 0.0  | 0.0        | 0.0  | 333.0       |
      | 2  | 31   | 01 August 2024    |           | 445.0           | 222.0         | 0.0      | 0.0  | 0.0       | 222.0 | 0.0  | 0.0        | 0.0  | 222.0       |
      | 3  | 31   | 01 September 2024 |           | 223.0           | 222.0         | 0.0      | 0.0  | 0.0       | 222.0 | 0.0  | 0.0        | 0.0  | 222.0       |
      | 4  | 30   | 01 October 2024   |           | 0.0             | 223.0         | 0.0      | 0.0  | 0.0       | 223.0 | 0.0  | 0.0        | 0.0  | 223.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |

  @TestRailId:C3964
  Scenario: Verify Progressive Loan reschedule by extending repayment period with downpayment installment, flat interest type
    When Admin sets the business date to "01 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 June 2024      | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    When Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 June 2024      |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 30   | 01 July 2024      |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 31   | 01 August 2024    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 September 2024 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "01 July 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 July 2024       | 01 July 2024    |                 |                  |                 | 1          |                 |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 June 2024      |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 30   | 01 July 2024      |           | 562.5           | 187.5         | 0.0      | 0.0  | 0.0       | 187.5 | 0.0  | 0.0        | 0.0  | 187.5       |
      | 3  | 31   | 01 August 2024    |           | 375.0           | 187.5         | 0.0      | 0.0  | 0.0       | 187.5 | 0.0  | 0.0        | 0.0  | 187.5       |
      | 4  | 31   | 01 September 2024 |           | 187.5           | 187.5         | 0.0      | 0.0  | 0.0       | 187.5 | 0.0  | 0.0        | 0.0  | 187.5       |
      | 5  | 30   | 01 October 2024   |           | 0.0             | 187.5         | 0.0      | 0.0  | 0.0       | 187.5 | 0.0  | 0.0        | 0.0  | 187.5       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |

  @TestRailId:C3965
  Scenario: Verify Progressive Loan reschedule by extending repayment period - multiple extra terms, flat interest type
    When Admin sets the business date to "01 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 June 2024      | 1500           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1500" amount and expected disbursement date on "01 June 2024"
    When Admin successfully disburse the loan on "01 June 2024" with "1500" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1500.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 1000.0          | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
      | 2  | 31   | 01 August 2024    |           | 500.0           | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 500.0         | 0.0      | 0.0  | 0.0       | 500.0 | 0.0  | 0.0        | 0.0  | 500.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1500.0        | 0.0      | 0.0  | 0.0       | 1500.0 | 0.0  | 0.0        | 0.0  | 1500.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       |
    When Admin sets the business date to "01 July 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 July 2024       | 01 July 2024    |                 |                  |                 | 2          |                 |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1500.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 1200.0          | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 2  | 31   | 01 August 2024    |           | 900.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 3  | 31   | 01 September 2024 |           | 600.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 4  | 30   | 01 October 2024   |           | 300.0           | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
      | 5  | 31   | 01 November 2024  |           | 0.0             | 300.0         | 0.0      | 0.0  | 0.0       | 300.0 | 0.0  | 0.0        | 0.0  | 300.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1500.0        | 0.0      | 0.0  | 0.0       | 1500.0 | 0.0  | 0.0        | 0.0  | 1500.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       |

  @TestRailId:C3966
  Scenario: Verify Progressive Loan reschedule by extending repayment period after partial repayment, flat interest type
    When Admin sets the business date to "01 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 June 2024      | 1200           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1200" amount and expected disbursement date on "01 June 2024"
    When Admin successfully disburse the loan on "01 June 2024" with "1200" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1200.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 800.0           | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 0.0  | 0.0        | 0.0  | 400.0       |
      | 2  | 31   | 01 August 2024    |           | 400.0           | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 0.0  | 0.0        | 0.0  | 400.0       |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 0.0  | 0.0        | 0.0  | 400.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1200.0        | 0.0      | 0.0  | 0.0       | 1200.0 | 0.0  | 0.0        | 0.0  | 1200.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1200.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       |
    When Admin sets the business date to "01 July 2024"
    And Customer makes "AUTOPAY" repayment on "01 July 2024" with 400 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1200.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 01 July 2024      | 01 July 2024 | 800.0           | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 400.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 400.0           | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 0.0   | 0.0        | 0.0  | 400.0       |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 0.0   | 0.0        | 0.0  | 400.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1200.0        | 0.0      | 0.0  | 0.0       | 1200.0 | 400.0 | 0.0        | 0.0  | 800.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1200.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       |
      | 01 July 2024     | Repayment        | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 800.0        |
    When Admin sets the business date to "15 July 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 August 2024     | 15 July 2024    |                 |                  |                 | 1          |                 |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1200.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 01 July 2024      | 01 July 2024 | 800.0           | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 400.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 533.0           | 267.0         | 0.0      | 0.0  | 0.0       | 267.0 | 0.0   | 0.0        | 0.0  | 267.0       |
      | 3  | 31   | 01 September 2024 |              | 266.0           | 267.0         | 0.0      | 0.0  | 0.0       | 267.0 | 0.0   | 0.0        | 0.0  | 267.0       |
      | 4  | 30   | 01 October 2024   |              | 0.0             | 266.0         | 0.0      | 0.0  | 0.0       | 266.0 | 0.0   | 0.0        | 0.0  | 266.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1200.0        | 0.0      | 0.0  | 0.0       | 1200.0 | 400.0 | 0.0        | 0.0  | 800.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1200.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       |
      | 01 July 2024     | Repayment        | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 800.0        |

  @TestRailId:C3967
  Scenario: Verify Progressive Loan reschedule by extending repayment periods after partial repayment and then backdated repayment occurs, flat interest type
    When Admin sets the business date to "01 June 2024"
    When Admin creates a client with random data
    When Admin set "LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "LAST_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                            | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 June 2024      | 1200           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1200" amount and expected disbursement date on "01 June 2024"
    When Admin successfully disburse the loan on "01 June 2024" with "1200" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1200.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 800.0           | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 0.0  | 0.0        | 0.0  | 400.0       |
      | 2  | 31   | 01 August 2024    |           | 400.0           | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 0.0  | 0.0        | 0.0  | 400.0       |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 0.0  | 0.0        | 0.0  | 400.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1200.0        | 0.0      | 0.0  | 0.0       | 1200.0 | 0.0  | 0.0        | 0.0  | 1200.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1200.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       |
    When Admin sets the business date to "01 July 2024"
    And Customer makes "AUTOPAY" repayment on "01 July 2024" with 400 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1200.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 01 July 2024      | 01 July 2024 | 800.0           | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 400.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 400.0           | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 0.0   | 0.0        | 0.0  | 400.0       |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 0.0   | 0.0        | 0.0  | 400.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1200.0        | 0.0      | 0.0  | 0.0       | 1200.0 | 400.0 | 0.0        | 0.0  | 800.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1200.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       |
      | 01 July 2024     | Repayment        | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 800.0        |
    When Admin sets the business date to "15 July 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 September 2024  | 15 July 2024    |                 |                  |                 | 2          |                 |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1200.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 01 July 2024      | 01 July 2024 | 800.0           | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 400.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 400.0           | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 0.0   | 0.0        | 0.0  | 400.0       |
      | 3  | 31   | 01 September 2024 |              | 267.0           | 133.0         | 0.0      | 0.0  | 0.0       | 133.0 | 0.0   | 0.0        | 0.0  | 133.0       |
      | 4  | 30   | 01 October 2024   |              | 134.0           | 133.0         | 0.0      | 0.0  | 0.0       | 133.0 | 0.0   | 0.0        | 0.0  | 133.0       |
      | 5  | 31   | 01 November 2024  |              | 0.0             | 134.0         | 0.0      | 0.0  | 0.0       | 134.0 | 0.0   | 0.0        | 0.0  | 134.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1200.0        | 0.0      | 0.0  | 0.0       | 1200.0 | 400.0 | 0.0        | 0.0  | 800.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1200.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       |
      | 01 July 2024     | Repayment        | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 800.0        |
    When Admin sets the business date to "15 October 2024"
    And Customer makes "AUTOPAY" repayment on "01 August 2024" with 500 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |                | 1200.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 30   | 01 July 2024      | 01 July 2024   | 800.0           | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 400.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    | 01 August 2024 | 400.0           | 400.0         | 0.0      | 0.0  | 0.0       | 400.0 | 400.0 | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 September 2024 |                | 267.0           | 133.0         | 0.0      | 0.0  | 0.0       | 133.0 | 0.0   | 0.0        | 0.0  | 133.0       |
      | 4  | 30   | 01 October 2024   |                | 134.0           | 133.0         | 0.0      | 0.0  | 0.0       | 133.0 | 0.0   | 0.0        | 0.0  | 133.0       |
      | 5  | 31   | 01 November 2024  |                | 0.0             | 134.0         | 0.0      | 0.0  | 0.0       | 134.0 | 100.0 | 100.0      | 0.0  | 34.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1200.0        | 0.0      | 0.0  | 0.0       | 1200.0 | 900.0 | 100.0      | 0.0  | 300.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1200.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       |
      | 01 July 2024     | Repayment        | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 800.0        |
      | 01 August 2024   | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 300.0        |
    When Admin set "LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

  @TestRailId:C3987
  Scenario: Verify Progressive Loan reschedule by extending repayment period: Basic scenario without downpayment, declining balance interest type
    When Admin sets the business date to "01 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 June 2024      | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    When Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 2  | 31   | 01 August 2024    |           | 336.66          | 333.32        | 6.7      | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 336.66        | 3.37     | 0.0  | 0.0       | 340.03 | 0.0  | 0.0        | 0.0  | 340.03      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.07    | 0.0  | 0.0       | 1020.07 | 0.0  | 0.0        | 0.0  | 1020.07     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "01 July 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 August 2024     | 01 July 2024    |                 |                  |                 | 1          |                 |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 669.98          | 330.02        | 10.0     | 0.0  | 0.0       | 340.02 | 0.0  | 0.0        | 0.0  | 340.02      |
      | 2  | 31   | 01 August 2024    |           | 448.87          | 221.11        | 6.7      | 0.0  | 0.0       | 227.81 | 0.0  | 0.0        | 0.0  | 227.81      |
      | 3  | 31   | 01 September 2024 |           | 225.55          | 223.32        | 4.49     | 0.0  | 0.0       | 227.81 | 0.0  | 0.0        | 0.0  | 227.81      |
      | 4  | 30   | 01 October 2024   |           | 0.0             | 225.55        | 2.26     | 0.0  | 0.0       | 227.81 | 0.0  | 0.0        | 0.0  | 227.81      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 23.45    | 0.0  | 0.0       | 1023.45 | 0.0  | 0.0        | 0.0  | 1023.45     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |

  @TestRailId:C3989
  Scenario: Verify Progressive Loan reschedule by extending repayment period with downpayment installment, declining balance interest type
    When Admin sets the business date to "01 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP2_DOWNPAYMENT_INTEREST | 01 June 2024      | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    When Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 0    | 01 June 2024      |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 30   | 01 July 2024      |           | 502.4           | 247.6         | 7.4      | 0.0  | 0.0       | 255.0  | 0.0  | 0.0        | 0.0  | 255.0       |
      | 3  | 31   | 01 August 2024    |           | 252.52          | 249.88        | 5.12     | 0.0  | 0.0       | 255.0  | 0.0  | 0.0        | 0.0  | 255.0       |
      | 4  | 31   | 01 September 2024 |           | 0.0             | 252.52        | 2.57     | 0.0  | 0.0       | 255.09 | 0.0  | 0.0        | 0.0  | 255.09      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 15.09    | 0.0  | 0.0       | 1015.09 | 0.0  | 0.0        | 0.0  | 1015.09     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    When Admin sets the business date to "01 July 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 July 2024       | 01 July 2024    |                 |                  |                 | 1          |                 |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 0    | 01 June 2024      |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 30   | 01 July 2024      |           | 629.4           | 120.6         | 7.4      | 0.0  | 0.0       | 128.0  | 0.0  | 0.0        | 0.0  | 128.0       |
      | 3  | 31   | 01 August 2024    |           | 507.81          | 121.59        | 6.41     | 0.0  | 0.0       | 128.0  | 0.0  | 0.0        | 0.0  | 128.0       |
      | 4  | 31   | 01 September 2024 |           | 384.99          | 122.82        | 5.18     | 0.0  | 0.0       | 128.0  | 0.0  | 0.0        | 0.0  | 128.0       |
      | 5  | 30   | 01 October 2024   |           | 0.0             | 384.99        | 3.8      | 0.0  | 0.0       | 388.79 | 0.0  | 0.0        | 0.0  | 388.79      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 22.79    | 0.0  | 0.0       | 1022.79 | 0.0  | 0.0        | 0.0  | 1022.79     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |

  @TestRailId:C3990
  Scenario: Verify Progressive Loan reschedule by extending repayment period - multiple extra terms, declining balance interest type
    When Admin sets the business date to "01 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 June 2024      | 1500           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1500" amount and expected disbursement date on "01 June 2024"
    When Admin successfully disburse the loan on "01 June 2024" with "1500" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1500.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 1004.97         | 495.03        | 15.0     | 0.0  | 0.0       | 510.03 | 0.0  | 0.0        | 0.0  | 510.03      |
      | 2  | 31   | 01 August 2024    |           | 504.99          | 499.98        | 10.05    | 0.0  | 0.0       | 510.03 | 0.0  | 0.0        | 0.0  | 510.03      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 504.99        | 5.05     | 0.0  | 0.0       | 510.04 | 0.0  | 0.0        | 0.0  | 510.04      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 30.1     | 0.0  | 0.0       | 1530.1  | 0.0  | 0.0        | 0.0  | 1530.1      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       |
    When Admin sets the business date to "01 July 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 July 2024       | 01 July 2024    |                 |                  |                 | 2          |                 |
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1500.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 1205.94         | 294.06        | 15.0     | 0.0  | 0.0       | 309.06 | 0.0  | 0.0        | 0.0  | 309.06      |
      | 2  | 31   | 01 August 2024    |           | 908.94          | 297.0         | 12.06    | 0.0  | 0.0       | 309.06 | 0.0  | 0.0        | 0.0  | 309.06      |
      | 3  | 31   | 01 September 2024 |           | 608.97          | 299.97        | 9.09     | 0.0  | 0.0       | 309.06 | 0.0  | 0.0        | 0.0  | 309.06      |
      | 4  | 30   | 01 October 2024   |           | 306.0           | 302.97        | 6.09     | 0.0  | 0.0       | 309.06 | 0.0  | 0.0        | 0.0  | 309.06      |
      | 5  | 31   | 01 November 2024  |           | 0.0             | 306.0         | 3.06     | 0.0  | 0.0       | 309.06 | 0.0  | 0.0        | 0.0  | 309.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 45.3     | 0.0  | 0.0       | 1545.3  | 0.0  | 0.0        | 0.0  | 1545.3      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       |

  @TestRailId:C3994
  Scenario: Verify Progressive Loan reschedule by extending repayment period after partial repayment, declining balance interest type
    When Admin sets the business date to "01 June 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 June 2024      | 1200           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1200" amount and expected disbursement date on "01 June 2024"
    When Admin successfully disburse the loan on "01 June 2024" with "1200" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1200.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 803.97          | 396.03        | 12.0     | 0.0  | 0.0       | 408.03 | 0.0  | 0.0        | 0.0  | 408.03      |
      | 2  | 31   | 01 August 2024    |           | 403.98          | 399.99        | 8.04     | 0.0  | 0.0       | 408.03 | 0.0  | 0.0        | 0.0  | 408.03      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 403.98        | 4.04     | 0.0  | 0.0       | 408.02 | 0.0  | 0.0        | 0.0  | 408.02      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1200.0        | 24.08    | 0.0  | 0.0       | 1224.08 | 0.0  | 0.0        | 0.0  | 1224.08     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1200.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       |
    When Admin sets the business date to "01 July 2024"
    And Customer makes "AUTOPAY" repayment on "01 July 2024" with 417.03 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1200.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 July 2024      | 01 July 2024 | 803.97          | 396.03        | 12.0     | 0.0  | 0.0       | 408.03 | 408.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 403.89          | 400.08        | 7.95     | 0.0  | 0.0       | 408.03 | 9.0    | 9.0        | 0.0  | 399.03      |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 403.89        | 4.04     | 0.0  | 0.0       | 407.93 | 0.0    | 0.0        | 0.0  | 407.93      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1200.0        | 23.99    | 0.0  | 0.0       | 1223.99 | 417.03 | 9.0        | 0.0  | 806.96      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1200.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       |
      | 01 July 2024     | Repayment        | 417.03 | 405.03    | 12.0     | 0.0  | 0.0       | 794.97       |
    When Admin sets the business date to "15 July 2024"
    When Admin creates and approves Loan reschedule with the following data:
      | rescheduleFromDate | submittedOnDate | adjustedDueDate | graceOnPrincipal | graceOnInterest | extraTerms | newInterestRate |
      | 01 August 2024     | 15 July 2024    |                 |                  |                 | 1          |                 |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1200.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 July 2024      | 01 July 2024 | 803.97          | 396.03        | 12.0     | 0.0  | 0.0       | 408.03 | 408.03 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 538.58          | 265.39        | 7.95     | 0.0  | 0.0       | 273.34 | 9.0    | 9.0        | 0.0  | 264.34      |
      | 3  | 31   | 01 September 2024 |              | 270.63          | 267.95        | 5.39     | 0.0  | 0.0       | 273.34 | 0.0    | 0.0        | 0.0  | 273.34      |
      | 4  | 30   | 01 October 2024   |              | 0.0             | 270.63        | 2.71     | 0.0  | 0.0       | 273.34 | 0.0    | 0.0        | 0.0  | 273.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1200.0        | 28.05    | 0.0  | 0.0       | 1228.05 | 417.03 | 9.0        | 0.0  | 811.02      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement     | 1200.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       |
      | 01 July 2024     | Repayment        | 417.03 | 405.03    | 12.0     | 0.0  | 0.0       | 794.97       |

  @TestRailId:C4028
  Scenario: Verify tranche interest bearing progressive loan that expects two tranches at the same date with exact disb amount in expected order - UC1
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2025                | 700.0                      | 01 January 2025                | 200.0                      |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      |    |      | 01 January 2025  |           | 200.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 752.17          | 147.83        | 5.25     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 2  | 28   | 01 March 2025    |           | 603.48          | 148.69        | 4.39     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 3  | 31   | 01 April 2025    |           | 453.92          | 149.56        | 3.52     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 4  | 30   | 01 May 2025      |           | 303.49          | 150.43        | 2.65     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 5  | 31   | 01 June 2025     |           | 152.18          | 151.31        | 1.77     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 152.18        | 0.89     | 0.0  | 0.0       | 153.07 | 0.0  | 0.0        | 0.0  | 153.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 900.0         | 18.47    | 0.0  | 0.0       | 918.47  | 0.0  | 0.0        | 0.0  | 918.47      |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          |                 | 700.0       |                      |
      | 01 January 2025          |                 | 200.0       |                      |
#    --- 1st disbursement - 1 January, 2025  ---
    When Admin successfully disburse the loan on "01 January 2025" with "700" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 585.02          | 114.98        | 4.08     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 2  | 28   | 01 March 2025    |           | 469.37          | 115.65        | 3.41     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 3  | 31   | 01 April 2025    |           | 353.05          | 116.32        | 2.74     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 4  | 30   | 01 May 2025      |           | 236.05          | 117.0         | 2.06     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 5  | 31   | 01 June 2025     |           | 118.37          | 117.68        | 1.38     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 118.37        | 0.69     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 700.0         | 14.36    | 0.0  | 0.0       | 714.36  | 0.0  | 0.0        | 0.0  | 714.36      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 700.0       |                      |
      | 01 January 2025          |                 | 200.0       |                      |
#    --- 2nd disbursement - 1 January, 2025  ---
    When Admin successfully disburse the loan on "01 January 2025" with "200" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 01 January 2025  |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 752.17          | 147.83        | 5.25     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 2  | 28   | 01 March 2025    |           | 603.48          | 148.69        | 4.39     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 3  | 31   | 01 April 2025    |           | 453.92          | 149.56        | 3.52     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 4  | 30   | 01 May 2025      |           | 303.49          | 150.43        | 2.65     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 5  | 31   | 01 June 2025     |           | 152.18          | 151.31        | 1.77     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 152.18        | 0.89     | 0.0  | 0.0       | 153.07 | 0.0  | 0.0        | 0.0  | 153.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 900.0         | 18.47    | 0.0  | 0.0       | 918.47  | 0.0  | 0.0        | 0.0  | 918.47      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 01 January 2025  | Disbursement       | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 700.0       |                      |
      | 01 January 2025          | 01 January 2025 | 200.0       |                      |

    When Loan Pay-off is made on "01 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4029
  Scenario: Verify tranche interest bearing progressive loan that expects two tranches at the same date with over expected disb amount in expected order  - UC2
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2025                | 700.0                      | 01 January 2025                | 200.0                      |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      |    |      | 01 January 2025  |           | 200.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 752.17          | 147.83        | 5.25     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 2  | 28   | 01 March 2025    |           | 603.48          | 148.69        | 4.39     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 3  | 31   | 01 April 2025    |           | 453.92          | 149.56        | 3.52     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 4  | 30   | 01 May 2025      |           | 303.49          | 150.43        | 2.65     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 5  | 31   | 01 June 2025     |           | 152.18          | 151.31        | 1.77     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 152.18        | 0.89     | 0.0  | 0.0       | 153.07 | 0.0  | 0.0        | 0.0  | 153.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 900.0         | 18.47    | 0.0  | 0.0       | 918.47  | 0.0  | 0.0        | 0.0  | 918.47      |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          |                 | 700.0       |                      |
      | 01 January 2025          |                 | 200.0       |                      |
#    --- 1st disbursement - 1 January, 2025  ---
    When Admin successfully disburse the loan on "01 January 2025" with "750" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 750.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 626.81          | 123.19        | 4.37     | 0.0  | 0.0       | 127.56 | 0.0  | 0.0        | 0.0  | 127.56      |
      | 2  | 28   | 01 March 2025    |           | 502.91          | 123.9         | 3.66     | 0.0  | 0.0       | 127.56 | 0.0  | 0.0        | 0.0  | 127.56      |
      | 3  | 31   | 01 April 2025    |           | 378.28          | 124.63        | 2.93     | 0.0  | 0.0       | 127.56 | 0.0  | 0.0        | 0.0  | 127.56      |
      | 4  | 30   | 01 May 2025      |           | 252.93          | 125.35        | 2.21     | 0.0  | 0.0       | 127.56 | 0.0  | 0.0        | 0.0  | 127.56      |
      | 5  | 31   | 01 June 2025     |           | 126.85          | 126.08        | 1.48     | 0.0  | 0.0       | 127.56 | 0.0  | 0.0        | 0.0  | 127.56      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 126.85        | 0.74     | 0.0  | 0.0       | 127.59 | 0.0  | 0.0        | 0.0  | 127.59      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 750.0         | 15.39    | 0.0  | 0.0       | 765.39  | 0.0  | 0.0        | 0.0  | 765.39      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 750.0  | 0.0       | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 750.0       |                      |
      | 01 January 2025          |                 | 200.0       |                      |
#    --- 2nd disbursement - 1 January, 2025  ---
    When Admin successfully disburse the loan on "01 January 2025" with "250" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 750.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 01 January 2025  |           | 250.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 835.74          | 164.26        | 5.83     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
      | 2  | 28   | 01 March 2025    |           | 670.53          | 165.21        | 4.88     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
      | 3  | 31   | 01 April 2025    |           | 504.35          | 166.18        | 3.91     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
      | 4  | 30   | 01 May 2025      |           | 337.2           | 167.15        | 2.94     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
      | 5  | 31   | 01 June 2025     |           | 169.08          | 168.12        | 1.97     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 169.08        | 0.99     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.52    | 0.0  | 0.0       | 1020.52 | 0.0  | 0.0        | 0.0  | 1020.52     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 750.0  | 0.0       | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 01 January 2025  | Disbursement       | 250.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 750.0       |                      |
      | 01 January 2025          | 01 January 2025 | 250.0       |                      |

    When Loan Pay-off is made on "1 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4030
  Scenario: Verify tranche interest bearing progressive loan that expects two tranches at the same date with over expected disb amount in not expected order  - UC3
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2025                | 200.0                      | 01 January 2025                | 500.0                      |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 200.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      |    |      | 01 January 2025  |           | 500.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 585.02          | 114.98        | 4.08     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 2  | 28   | 01 March 2025    |           | 469.37          | 115.65        | 3.41     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 3  | 31   | 01 April 2025    |           | 353.05          | 116.32        | 2.74     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 4  | 30   | 01 May 2025      |           | 236.05          | 117.0         | 2.06     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 5  | 31   | 01 June 2025     |           | 118.37          | 117.68        | 1.38     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 118.37        | 0.69     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 700.0         | 14.36    | 0.0  | 0.0       | 714.36  | 0.0  | 0.0        | 0.0  | 714.36      |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          |                 | 200.0       |                      |
      | 01 January 2025          |                 | 500.0       |                      |
#    --- 1st disbursement - 1 January, 2025  ---
    Then Admin fails to disburse the loan on "1 January 2025" with "801" EUR transaction amount due to exceed approved amount
    When Admin successfully disburse the loan on "01 January 2025" with "300" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 250.72          | 49.28         | 1.75     | 0.0  | 0.0       | 51.03  | 0.0  | 0.0        | 0.0  | 51.03       |
      | 2  | 28   | 01 March 2025    |           | 201.15          | 49.57         | 1.46     | 0.0  | 0.0       | 51.03  | 0.0  | 0.0        | 0.0  | 51.03       |
      | 3  | 31   | 01 April 2025    |           | 151.29          | 49.86         | 1.17     | 0.0  | 0.0       | 51.03  | 0.0  | 0.0        | 0.0  | 51.03       |
      | 4  | 30   | 01 May 2025      |           | 101.14          | 50.15         | 0.88     | 0.0  | 0.0       | 51.03  | 0.0  | 0.0        | 0.0  | 51.03       |
      | 5  | 31   | 01 June 2025     |           |  50.7           | 50.44         | 0.59     | 0.0  | 0.0       | 51.03  | 0.0  | 0.0        | 0.0  | 51.03       |
      | 6  | 30   | 01 July 2025     |           |   0.0           | 50.7          | 0.3      | 0.0  | 0.0       | 51.0   | 0.0  | 0.0        | 0.0  | 51.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 300.0         | 6.15     | 0.0  | 0.0       | 306.15  | 0.0  | 0.0        | 0.0  | 306.15      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 300.0        | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 300.0       |                      |
      | 01 January 2025          |                 | 200.0       |                      |
#    --- 2nd disbursement - 1 January, 2025  ---
    Then Admin fails to disburse the loan on "1 January 2025" with "701" EUR transaction amount due to exceed approved amount
    When Admin successfully disburse the loan on "01 January 2025" with "600" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 01 January 2025  |           | 600.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 752.17          | 147.83        | 5.25     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 2  | 28   | 01 March 2025    |           | 603.48          | 148.69        | 4.39     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 3  | 31   | 01 April 2025    |           | 453.92          | 149.56        | 3.52     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 4  | 30   | 01 May 2025      |           | 303.49          | 150.43        | 2.65     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 5  | 31   | 01 June 2025     |           | 152.18          | 151.31        | 1.77     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 6  | 30   | 01 July 2025     |           |   0.0           | 152.18        | 0.89     | 0.0  | 0.0       | 153.07 | 0.0  | 0.0        | 0.0  | 153.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 900.0         | 18.47    | 0.0  | 0.0       | 918.47  | 0.0  | 0.0        | 0.0  | 918.47      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 300.0        | false    | false    |
      | 01 January 2025  | Disbursement       | 600.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 300.0       |                      |
      | 01 January 2025          | 01 January 2025 | 600.0       |                      |

    When Loan Pay-off is made on "01 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4031
  Scenario: Verify tranche interest bearing progressive loan that expects two tranches at the same date with diff expected disb amounts in diff order - UC4
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2025                | 200.0                      | 01 January 2025                | 700.0                      |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      |    |      | 01 January 2025  |           | 200.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 752.17          | 147.83        | 5.25     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 2  | 28   | 01 March 2025    |           | 603.48          | 148.69        | 4.39     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 3  | 31   | 01 April 2025    |           | 453.92          | 149.56        | 3.52     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 4  | 30   | 01 May 2025      |           | 303.49          | 150.43        | 2.65     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 5  | 31   | 01 June 2025     |           | 152.18          | 151.31        | 1.77     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 152.18        | 0.89     | 0.0  | 0.0       | 153.07 | 0.0  | 0.0        | 0.0  | 153.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 900.0         | 18.47    | 0.0  | 0.0       | 918.47  | 0.0  | 0.0        | 0.0  | 918.47      |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          |                 | 200.0       |                      |
      | 01 January 2025          |                 | 700.0       |                      |
#    --- 1st disbursement - 1 January, 2025  ---
    Then Admin fails to disburse the loan on "1 January 2025" with "900" EUR transaction amount due to exceed approved amount
    When Admin successfully disburse the loan on "01 January 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 417.88          | 82.12         | 2.92     | 0.0  | 0.0       | 85.04  | 0.0  | 0.0        | 0.0  | 85.04       |
      | 2  | 28   | 01 March 2025    |           | 335.28          | 82.6          | 2.44     | 0.0  | 0.0       | 85.04  | 0.0  | 0.0        | 0.0  | 85.04       |
      | 3  | 31   | 01 April 2025    |           | 252.2           | 83.08         | 1.96     | 0.0  | 0.0       | 85.04  | 0.0  | 0.0        | 0.0  | 85.04       |
      | 4  | 30   | 01 May 2025      |           | 168.63          | 83.57         | 1.47     | 0.0  | 0.0       | 85.04  | 0.0  | 0.0        | 0.0  | 85.04       |
      | 5  | 31   | 01 June 2025     |           |  84.57          | 84.06         | 0.98     | 0.0  | 0.0       | 85.04  | 0.0  | 0.0        | 0.0  | 85.04       |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 84.57         | 0.49     | 0.0  | 0.0       | 85.06  | 0.0  | 0.0        | 0.0  | 85.06       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 500.0         | 10.26    | 0.0  | 0.0       | 510.26  | 0.0  | 0.0        | 0.0  | 510.26      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 500.0       |                      |
      | 01 January 2025          |                 | 200.0       |                      |
#    --- 2nd disbursement - 1 January, 2025  ---
    When Admin successfully disburse the loan on "01 January 2025" with "300" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 01 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 668.6           | 131.4         | 4.67     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 2  | 28   | 01 March 2025    |           | 536.43          | 132.17        | 3.9      | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 3  | 31   | 01 April 2025    |           | 403.49          | 132.94        | 3.13     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 4  | 30   | 01 May 2025      |           | 269.77          | 133.72        | 2.35     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 5  | 31   | 01 June 2025     |           | 135.27          | 134.5         | 1.57     | 0.0  | 0.0       | 136.07 | 0.0  | 0.0        | 0.0  | 136.07      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 135.27        | 0.79     | 0.0  | 0.0       | 136.06 | 0.0  | 0.0        | 0.0  | 136.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 800.0         | 16.41    | 0.0  | 0.0       | 816.41  | 0.0  | 0.0        | 0.0  | 816.41      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 01 January 2025  | Disbursement       | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 500.0       |                      |
      | 01 January 2025          | 01 January 2025 | 300.0       |                      |

    When Loan Pay-off is made on "1 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4032
  Scenario: Verify tranche interest bearing progressive loan that expects two tranches at the same date in defined order with over expected 2nd disb amount  - UC5
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2025                | 200.0                      | 01 January 2025                | 500.0                      |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 200.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      |    |      | 01 January 2025  |           | 500.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 585.02          | 114.98        | 4.08     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 2  | 28   | 01 March 2025    |           | 469.37          | 115.65        | 3.41     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 3  | 31   | 01 April 2025    |           | 353.05          | 116.32        | 2.74     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 4  | 30   | 01 May 2025      |           | 236.05          | 117.0         | 2.06     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 5  | 31   | 01 June 2025     |           | 118.37          | 117.68        | 1.38     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 118.37        | 0.69     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 700.0         | 14.36    | 0.0  | 0.0       | 714.36  | 0.0  | 0.0        | 0.0  | 714.36      |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          |                 | 200.0       |                      |
      | 01 January 2025          |                 | 500.0       |                      |
#    --- 1st disbursement - 1 January, 2025  ---
    When Admin successfully disburse the loan on "01 January 2025" with "200" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 167.15          | 32.85         | 1.17     | 0.0  | 0.0       | 34.02  | 0.0  | 0.0        | 0.0  | 34.02       |
      | 2  | 28   | 01 March 2025    |           | 134.11          | 33.04         | 0.98     | 0.0  | 0.0       | 34.02  | 0.0  | 0.0        | 0.0  | 34.02       |
      | 3  | 31   | 01 April 2025    |           | 100.87          | 33.24         | 0.78     | 0.0  | 0.0       | 34.02  | 0.0  | 0.0        | 0.0  | 34.02       |
      | 4  | 30   | 01 May 2025      |           |  67.44          | 33.43         | 0.59     | 0.0  | 0.0       | 34.02  | 0.0  | 0.0        | 0.0  | 34.02       |
      | 5  | 31   | 01 June 2025     |           |  33.81          | 33.63         | 0.39     | 0.0  | 0.0       | 34.02  | 0.0  | 0.0        | 0.0  | 34.02       |
      | 6  | 30   | 01 July 2025     |           |   0.0           | 33.81         | 0.2      | 0.0  | 0.0       | 34.01  | 0.0  | 0.0        | 0.0  | 34.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 200.0         | 4.11     | 0.0  | 0.0       | 204.11  | 0.0  | 0.0        | 0.0  | 204.11      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 200.0       |                      |
      | 01 January 2025          |                 | 500.0       |                      |
#    --- 2nd disbursement - 1 January, 2025  ---
    When Admin successfully disburse the loan on "01 January 2025" with "800" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 01 January 2025  |           | 800.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 835.74          | 164.26        | 5.83     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
      | 2  | 28   | 01 March 2025    |           | 670.53          | 165.21        | 4.88     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
      | 3  | 31   | 01 April 2025    |           | 504.35          | 166.18        | 3.91     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
      | 4  | 30   | 01 May 2025      |           | 337.2           | 167.15        | 2.94     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
      | 5  | 31   | 01 June 2025     |           | 169.08          | 168.12        | 1.97     | 0.0  | 0.0       | 170.09 | 0.0  | 0.0        | 0.0  | 170.09      |
      | 6  | 30   | 01 July 2025     |           |   0.0           | 169.08        | 0.99     | 0.0  | 0.0       | 170.07 | 0.0  | 0.0        | 0.0  | 170.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 20.52    | 0.0  | 0.0       | 1020.52 | 0.0  | 0.0        | 0.0  | 1020.52     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 200.0        | false    | false    |
      | 01 January 2025  | Disbursement       | 800.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 200.0       |                      |
      | 01 January 2025          | 01 January 2025 | 800.0       |                      |

    When Loan Pay-off is made on "1 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4033
  Scenario: Verify tranche interest bearing progressive loan that expects tranche with added 2nd tranche at the same date and undo disbursement - UC6
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date |1st_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2025                | 700.0                     |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 585.02          | 114.98        | 4.08     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 2  | 28   | 01 March 2025    |           | 469.37          | 115.65        | 3.41     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 3  | 31   | 01 April 2025    |           | 353.05          | 116.32        | 2.74     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 4  | 30   | 01 May 2025      |           | 236.05          | 117.0         | 2.06     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 5  | 31   | 01 June 2025     |           | 118.37          | 117.68        | 1.38     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 118.37        | 0.69     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 700.0         | 14.36    | 0.0  | 0.0       | 714.36  | 0.0  | 0.0        | 0.0  | 714.36      |
    When Admin successfully disburse the loan on "01 January 2025" with "700" EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 700.0       |                      |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 585.02          | 114.98        | 4.08     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 2  | 28   | 01 March 2025    |           | 469.37          | 115.65        | 3.41     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 3  | 31   | 01 April 2025    |           | 353.05          | 116.32        | 2.74     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 4  | 30   | 01 May 2025      |           | 236.05          | 117.0         | 2.06     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 5  | 31   | 01 June 2025     |           | 118.37          | 117.68        | 1.38     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 118.37        | 0.69     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 700.0         | 14.36     | 0.0  | 0.0       | 714.36  | 0.0  | 0.0        | 0.0  | 714.36      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
    And Admin successfully add disbursement detail to the loan on "01 January 2025" with 300 EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 700.0       |                      |
      | 01 January 2025          |                 | 300.0       | 700.0                |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 585.02          | 114.98        | 4.08     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 2  | 28   | 01 March 2025    |           | 469.37          | 115.65        | 3.41     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 3  | 31   | 01 April 2025    |           | 353.05          | 116.32        | 2.74     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 4  | 30   | 01 May 2025      |           | 236.05          | 117.0         | 2.06     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 5  | 31   | 01 June 2025     |           | 118.37          | 117.68        | 1.38     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 118.37        | 0.69     | 0.0  | 0.0       | 119.06 | 0.0  | 0.0        | 0.0  | 119.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 700.0         | 14.36     | 0.0  | 0.0       | 714.36  | 0.0  | 0.0        | 0.0  | 714.36      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
#    --- 2nd disbursement - 1 Jan, 2025  ---
    When Admin successfully disburse the loan on "01 January 2025" with "200" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 01 January 2025  |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 752.17          | 147.83        | 5.25     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 2  | 28   | 01 March 2025    |           | 603.48          | 148.69        | 4.39     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 3  | 31   | 01 April 2025    |           | 453.92          | 149.56        | 3.52     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 4  | 30   | 01 May 2025      |           | 303.49          | 150.43        | 2.65     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 5  | 31   | 01 June 2025     |           | 152.18          | 151.31        | 1.77     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 152.18        | 0.89     | 0.0  | 0.0       | 153.07 | 0.0  | 0.0        | 0.0  | 153.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 900.0         | 18.47    | 0.0  | 0.0       | 918.47  | 0.0  | 0.0        | 0.0  | 918.47      |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On     | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025  | 700.0       |                      |
      | 01 January 2025          | 01 January 2025  | 200.0       | 700.0                |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 01 January 2025  | Disbursement      | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    | false    |
    Then Admin fails to disburse the loan on "01 January 2025" with "100" amount
# -- undo disbursement ----
    When Admin successfully undo disbursal
    Then Loan status has changed to "Approved"
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On     | Principal   | Net Disbursal Amount |
      | 01 January 2025          |                  | 700.0       |                      |
      | 01 January 2025          |                  | 200.0       | 700.0                |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      |    |      | 01 January 2025  |           | 200.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 752.17          | 147.83        | 5.25     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 2  | 28   | 01 March 2025    |           | 603.48          | 148.69        | 4.39     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 3  | 31   | 01 April 2025    |           | 453.92          | 149.56        | 3.52     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 4  | 30   | 01 May 2025      |           | 303.49          | 150.43        | 2.65     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 5  | 31   | 01 June 2025     |           | 152.18          | 151.31        | 1.77     | 0.0  | 0.0       | 153.08 | 0.0  | 0.0        | 0.0  | 153.08      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 152.18        | 0.89     | 0.0  | 0.0       | 153.07 | 0.0  | 0.0        | 0.0  | 153.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 900.0         | 18.47    | 0.0  | 0.0       | 918.47 | 0.0    | 0.0        | 0.0  | 918.47      |
    Then Loan Transactions tab has none transaction
#---- make two disbursements on Jan1 , 2025 ---#
    When Admin successfully disburse the loan on "01 January 2025" with "750" EUR transaction amount
    When Admin successfully disburse the loan on "01 January 2025" with "200" EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On     | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025  | 750.0       |                      |
      | 01 January 2025          | 01 January 2025  | 200.0       | 700.0                |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 750.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 01 January 2025  |           | 200.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 793.96          | 156.04        | 5.54     | 0.0  | 0.0       | 161.58 | 0.0  | 0.0        | 0.0  | 161.58      |
      | 2  | 28   | 01 March 2025    |           | 637.01          | 156.95        | 4.63     | 0.0  | 0.0       | 161.58 | 0.0  | 0.0        | 0.0  | 161.58      |
      | 3  | 31   | 01 April 2025    |           | 479.15          | 157.86        | 3.72     | 0.0  | 0.0       | 161.58 | 0.0  | 0.0        | 0.0  | 161.58      |
      | 4  | 30   | 01 May 2025      |           | 320.37          | 158.78        | 2.8      | 0.0  | 0.0       | 161.58 | 0.0  | 0.0        | 0.0  | 161.58      |
      | 5  | 31   | 01 June 2025     |           | 160.66          | 159.71        | 1.87     | 0.0  | 0.0       | 161.58 | 0.0  | 0.0        | 0.0  | 161.58      |
      | 6  | 30   | 01 July 2025     |           | 0.0             | 160.66        | 0.94     | 0.0  | 0.0       | 161.6  | 0.0  | 0.0        | 0.0  | 161.6       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 950.0         | 19.5     | 0.0  | 0.0       | 969.5  | 0.0    | 0.0        | 0.0  | 969.5       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025 | Disbursement      | 750.0  | 0.0       | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 01 January 2025 | Disbursement      | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 950.0        | false    | false    |
    When Admin sets the business date to "01 February 2025"
    When Admin runs inline COB job for Loan
    Then Admin fails to disburse the loan on "01 February 2025" with "50" amount

    When Loan Pay-off is made on "1 February 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4034
  Scenario: Verify tranche interest bearing progressive loan that expects tranches at the same date with repayment and undo last disbursement - UC7
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date |1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 90                | DAYS                  | 15             | DAYS                   | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2025                | 700.0                     | 01 January 2025                | 300.0                      |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      |    |      | 01 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 15   | 16 January 2025  |           | 834.55          | 165.45        | 2.92     | 0.0  | 0.0       | 168.37 | 0.0  | 0.0        | 0.0  | 168.37      |
      | 2  | 15   | 31 January 2025  |           | 668.61          | 165.94        | 2.43     | 0.0  | 0.0       | 168.37 | 0.0  | 0.0        | 0.0  | 168.37      |
      | 3  | 15   | 15 February 2025 |           | 502.19          | 166.42        | 1.95     | 0.0  | 0.0       | 168.37 | 0.0  | 0.0        | 0.0  | 168.37      |
      | 4  | 15   | 02 March 2025    |           | 335.28          | 166.91        | 1.46     | 0.0  | 0.0       | 168.37 | 0.0  | 0.0        | 0.0  | 168.37      |
      | 5  | 15   | 17 March 2025    |           | 167.89          | 167.39        | 0.98     | 0.0  | 0.0       | 168.37 | 0.0  | 0.0        | 0.0  | 168.37      |
      | 6  | 15   | 01 April 2025    |           |   0.0           | 167.89        | 0.49     | 0.0  | 0.0       | 168.38 | 0.0  | 0.0        | 0.0  | 168.38      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 10.23    | 0.0  | 0.0       | 1010.23 | 0.0  | 0.0        | 0.0  | 1010.23     |
#    --- 1st disbursement - 1 January, 2025  ---
    When Admin successfully disburse the loan on "01 January 2025" with "700" EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 700.0       |                      |
      | 01 January 2025          |                 | 300.0       |                      |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 700.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 15   | 16 January 2025  |           | 584.18          | 115.82        | 2.04     | 0.0  | 0.0       | 117.86 | 0.0  | 0.0        | 0.0  | 117.86      |
      | 2  | 15   | 31 January 2025  |           | 468.02          | 116.16        | 1.7      | 0.0  | 0.0       | 117.86 | 0.0  | 0.0        | 0.0  | 117.86      |
      | 3  | 15   | 15 February 2025 |           | 351.53          | 116.49        | 1.37     | 0.0  | 0.0       | 117.86 | 0.0  | 0.0        | 0.0  | 117.86      |
      | 4  | 15   | 02 March 2025    |           | 234.7           | 116.83        | 1.03     | 0.0  | 0.0       | 117.86 | 0.0  | 0.0        | 0.0  | 117.86      |
      | 5  | 15   | 17 March 2025    |           | 117.52          | 117.18        | 0.68     | 0.0  | 0.0       | 117.86 | 0.0  | 0.0        | 0.0  | 117.86      |
      | 6  | 15   | 01 April 2025    |           | 0.0             | 117.52        | 0.34     | 0.0  | 0.0       | 117.86 | 0.0  | 0.0        | 0.0  | 117.86      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 700.0         | 7.16     | 0.0  | 0.0       | 707.16 | 0.0    | 0.0        | 0.0  | 707.16      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement       | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
#    --- 1st repayment - 1 January, 2025  ---
    And Customer makes "AUTOPAY" repayment on "01 January 2025" with 117.86 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 700.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 15   | 16 January 2025  | 01 January 2025 | 582.14          | 117.86        | 0.0      | 0.0  | 0.0       | 117.86 | 117.86 | 117.86     | 0.0  | 0.0         |
      | 2  | 15   | 31 January 2025  |                 | 467.68          | 114.46        | 3.4      | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 3  | 15   | 15 February 2025 |                 | 351.18          | 116.5         | 1.36     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 4  | 15   | 02 March 2025    |                 | 234.34          | 116.84        | 1.02     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 5  | 15   | 17 March 2025    |                 | 117.16          | 117.18        | 0.68     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 6  | 15   | 01 April 2025    |                 | 0.0             | 117.16        | 0.34     | 0.0  | 0.0       | 117.5  | 0.0    | 0.0        | 0.0  | 117.5       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 700.0         | 6.8      | 0.0  | 0.0       | 706.8  | 117.86 | 117.86     | 0.0  | 588.94      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 01 January 2025  | Repayment         | 117.86 | 117.86    | 0.0      | 0.0  | 0.0       | 582.14       | false    | false    |
#    --- 2nd disbursement - 1 January, 2025  ---
    When Admin successfully disburse the loan on "01 January 2025" with "300" EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 700.0       |                      |
      | 01 January 2025          | 01 January 2025 | 300.0       |                      |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 700.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      |    |      | 01 January 2025  |                 | 300.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 15   | 16 January 2025  |                 | 834.2           | 165.8         | 2.57     | 0.0  | 0.0       | 168.37 | 117.86 | 117.86     | 0.0  | 50.51       |
      | 2  | 15   | 31 January 2025  |                 | 668.26          | 165.94        | 2.43     | 0.0  | 0.0       | 168.37 | 0.0    | 0.0        | 0.0  | 168.37      |
      | 3  | 15   | 15 February 2025 |                 | 501.84          | 166.42        | 1.95     | 0.0  | 0.0       | 168.37 | 0.0    | 0.0        | 0.0  | 168.37      |
      | 4  | 15   | 02 March 2025    |                 | 334.93          | 166.91        | 1.46     | 0.0  | 0.0       | 168.37 | 0.0    | 0.0        | 0.0  | 168.37      |
      | 5  | 15   | 17 March 2025    |                 | 167.54          | 167.39        | 0.98     | 0.0  | 0.0       | 168.37 | 0.0    | 0.0        | 0.0  | 168.37      |
      | 6  | 15   | 01 April 2025    |                 |   0.0           | 167.54        | 0.49     | 0.0  | 0.0       | 168.03 | 0.0    | 0.0        | 0.0  | 168.03      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 9.88     | 0.0  | 0.0       | 1009.88 | 117.86 | 117.86     | 0.0  | 892.02      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 01 January 2025  | Repayment         | 117.86 | 117.86    | 0.0      | 0.0  | 0.0       | 582.14       | false    | false    |
      | 01 January 2025  | Disbursement      | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 882.14       | false    | false    |
    Then Admin fails to disburse the loan on "01 January 2025" with "100" amount due to exceed approved amount
#    --- undo disbursement --- #
    When Admin successfully undo last disbursal
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On     | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025  | 700.0       |                      |
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 700.0           |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 15   | 16 January 2025  | 01 January 2025 | 582.14          | 117.86        | 0.0      | 0.0  | 0.0       | 117.86 | 117.86 | 117.86     | 0.0  | 0.0         |
      | 2  | 15   | 31 January 2025  |                 | 467.68          | 114.46        | 3.4      | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 3  | 15   | 15 February 2025 |                 | 351.18          | 116.5         | 1.36     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 4  | 15   | 02 March 2025    |                 | 234.34          | 116.84        | 1.02     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 5  | 15   | 17 March 2025    |                 | 117.16          | 117.18        | 0.68     | 0.0  | 0.0       | 117.86 | 0.0    | 0.0        | 0.0  | 117.86      |
      | 6  | 15   | 01 April 2025    |                 | 0.0             | 117.16        | 0.34     | 0.0  | 0.0       | 117.5  | 0.0    | 0.0        | 0.0  | 117.5       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      | 700.0         | 6.8      | 0.0  | 0.0       | 706.8  | 117.86 | 117.86     | 0.0  | 588.94      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 700.0  | 0.0       | 0.0      | 0.0  | 0.0       | 700.0        | false    | false    |
      | 01 January 2025  | Repayment         | 117.86 | 117.86    | 0.0      | 0.0  | 0.0       | 582.14       | false    | false    |
    Then Admin fails to disburse the loan on "01 January 2025" with "200" amount

    When Loan Pay-off is made on "1 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4035
  Scenario: Verify tranche interest bearing progressive loan that expects tranche with added 2 tranches at the same date - UC8
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with disbursement details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date |1st_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 90                | DAYS                  | 15             | DAYS                   | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2025                | 300.0                     |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 15   | 16 January 2025  |           | 250.37          | 49.63         | 0.88     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      | 2  | 15   | 31 January 2025  |           | 200.59          | 49.78         | 0.73     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      | 3  | 15   | 15 February 2025 |           | 150.67          | 49.92         | 0.59     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      | 4  | 15   | 02 March 2025    |           | 100.6           | 50.07         | 0.44     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      | 5  | 15   | 17 March 2025    |           |  50.38          | 50.22         | 0.29     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      | 6  | 15   | 01 April 2025    |           |   0.0           | 50.38         | 0.15     | 0.0  | 0.0       | 50.53  | 0.0  | 0.0        | 0.0  | 50.53       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 300.0         | 3.08     | 0.0  | 0.0       | 303.08  | 0.0  | 0.0        | 0.0  | 303.08      |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          |                 | 300.0       |                      |
    And Admin successfully add disbursement detail to the loan on "01 February 2025" with 500 EUR transaction amount
    And Admin successfully add disbursement detail to the loan on "01 February 2025" with 200 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 15   | 16 January 2025  |           | 250.37          | 49.63         | 0.88     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      | 2  | 15   | 31 January 2025  |           | 200.59          | 49.78         | 0.73     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      |    |      | 01 February 2025 |           | 500.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      |    |      | 01 February 2025 |           | 200.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 3  | 15   | 15 February 2025 |           | 676.32          | 224.27        | 2.49     | 0.0  | 0.0       | 226.76 | 0.0  | 0.0        | 0.0  | 226.76      |
      | 4  | 15   | 02 March 2025    |           | 451.53          | 224.79        | 1.97     | 0.0  | 0.0       | 226.76 | 0.0  | 0.0        | 0.0  | 226.76      |
      | 5  | 15   | 17 March 2025    |           | 226.09          | 225.44        | 1.32     | 0.0  | 0.0       | 226.76 | 0.0  | 0.0        | 0.0  | 226.76      |
      | 6  | 15   | 01 April 2025    |           |   0.0           | 226.09        | 0.66     | 0.0  | 0.0       | 226.75 | 0.0  | 0.0        | 0.0  | 226.75      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due      | Paid | In advance | Late | Outstanding |
      | 1000.00       | 8.05     | 0.0  | 0.0       | 1008.05  | 0.0  | 0.0        | 0.0  | 1008.05     |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          |                 | 300.0       |                      |
      | 01 February 2025         |                 | 500.0       | 1000.0               |
      | 01 February 2025         |                 | 200.0       | 1000.0               |
#    --- 1st disbursement - 1 January, 2025  ---
    When Admin successfully disburse the loan on "01 January 2025" with "300" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 15   | 16 January 2025  |           | 250.37          | 49.63         | 0.88     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      | 2  | 15   | 31 January 2025  |           | 200.59          | 49.78         | 0.73     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      |    |      | 01 February 2025 |           | 500.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      |    |      | 01 February 2025 |           | 200.0           |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 3  | 15   | 15 February 2025 |           | 850.67          | 49.92         | 0.59     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      | 4  | 15   | 02 March 2025    |           | 800.6           | 50.07         | 0.44     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      | 5  | 15   | 17 March 2025    |           | 750.38          | 50.22         | 0.29     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      | 6  | 15   | 01 April 2025    |           | 700.0           | 50.38         | 0.15     | 0.0  | 0.0       | 50.53  | 0.0  | 0.0        | 0.0  | 50.53       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 300.0         | 3.08     | 0.0  | 0.0       | 303.08  | 0.0  | 0.0        | 0.0  | 303.08      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 300.0        | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 300.0       |                      |
      | 01 February 2025         |                 | 500.0       | 1000.0               |
      | 01 February 2025         |                 | 200.0       | 1000.0               |
#    --- 2nd disbursement - 1 February, 2025  ---
    When Admin sets the business date to "01 February 2025"
    When Admin runs inline COB job for Loan
    When Admin successfully disburse the loan on "01 February 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 15   | 16 January 2025  |           | 250.37          | 49.63         | 0.88     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      | 2  | 15   | 31 January 2025  |           | 200.74          | 49.63         | 0.88     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      |    |      | 01 February 2025 |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 3  | 15   | 15 February 2025 |           | 526.31          | 174.43        | 1.97     | 0.0  | 0.0       | 176.4  | 0.0  | 0.0        | 0.0  | 176.4       |
      | 4  | 15   | 02 March 2025    |           | 351.45          | 174.86        | 1.54     | 0.0  | 0.0       | 176.4  | 0.0  | 0.0        | 0.0  | 176.4       |
      | 5  | 15   | 17 March 2025    |           | 176.08          | 175.37        | 1.03     | 0.0  | 0.0       | 176.4  | 0.0  | 0.0        | 0.0  | 176.4       |
      | 6  | 15   | 01 April 2025    |           |   0.0           | 176.08        | 0.51     | 0.0  | 0.0       | 176.59 | 0.0  | 0.0        | 0.0  | 176.59      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 800.0         | 6.81     | 0.0  | 0.0       | 806.81  | 0.0  | 0.0        | 0.0  | 806.81      |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On     | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025  | 300.0       |                      |
      | 01 February 2025         | 01 February 2025 | 500.0       | 1000.0               |
      | 01 February 2025         |                  | 200.0       | 1000.0               |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 300.0        | false    | false    |
      | 31 January 2025  | Accrual           | 1.76   | 0.0       | 1.76     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Disbursement      | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |

#    ---  3rd disbursement - 1 February, 2025  ---
    When Admin successfully disburse the loan on "01 February 2025" with "150" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 300.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 15   | 16 January 2025  |           | 250.37          | 49.63         | 0.88     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      | 2  | 15   | 31 January 2025  |           | 200.74          | 49.63         | 0.88     | 0.0  | 0.0       | 50.51  | 0.0  | 0.0        | 0.0  | 50.51       |
      |    |      | 01 February 2025 |           | 500.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 01 February 2025 |           | 150.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 3  | 15   | 15 February 2025 |           | 638.95          | 211.79        | 2.37     | 0.0  | 0.0       | 214.16 | 0.0  | 0.0        | 0.0  | 214.16      |
      | 4  | 15   | 02 March 2025    |           | 426.65          | 212.3         | 1.86     | 0.0  | 0.0       | 214.16 | 0.0  | 0.0        | 0.0  | 214.16      |
      | 5  | 15   | 17 March 2025    |           | 213.73          | 212.92        | 1.24     | 0.0  | 0.0       | 214.16 | 0.0  | 0.0        | 0.0  | 214.16      |
      | 6  | 15   | 01 April 2025    |           |   0.0           | 213.73        | 0.62     | 0.0  | 0.0       | 214.35 | 0.0  | 0.0        | 0.0  | 214.35      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 950.0         | 7.85     | 0.0  | 0.0       | 957.85  | 0.0  | 0.0        | 0.0  | 957.85      |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On     | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025  | 300.0       |                      |
      | 01 February 2025         | 01 February 2025 | 500.0       | 1000.0               |
      | 01 February 2025         | 01 February 2025 | 150.0       | 1000.0               |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 300.0  | 0.0       | 0.0      | 0.0  | 0.0       | 300.0        | false    | false    |
      | 31 January 2025  | Accrual           | 1.76   | 0.0       | 1.76     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 February 2025 | Disbursement      | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 800.0        | false    | false    |
      | 01 February 2025 | Disbursement      | 150.0  | 0.0       | 0.0      | 0.0  | 0.0       | 950.0        | false    | false    |
    Then Admin fails to disburse the loan on "01 February 2025" with "50" amount

    When Loan Pay-off is made on "1 February 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4118
  Scenario: Verify cumulative multidisb loan with 2nd disb at 1st installment with flat interest type and same_as_repeyment interest calculation period - UC1
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type    | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT_SAR_RECALCULATION_SAME_AS_REPAYMENT_ACTUAL_ACTUAL_MULTIDISB | 01 January 2025   | 1500           | 7                      | FLAT             | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2025" with "1500" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 666.67          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 0.0  | 0.0        | 0.0  | 339.16      |
      | 2  | 28   | 01 March 2025    |           | 333.34          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 0.0  | 0.0        | 0.0  | 339.16      |
      | 3  | 31   | 01 April 2025    |           |   0.0           | 333.34        | 5.84     | 0.0  | 0.0       | 339.18 | 0.0  | 0.0        | 0.0  | 339.18      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 17.5    | 0.0  | 0.0       | 1017.5  | 0.0  | 0.0        | 0.0  | 1017.5      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
# -- 2nd disb - on Jan, 15, 2025 --#
    When Admin sets the business date to "15 January 2025"
    When Admin successfully disburse the loan on "15 January 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2025  |           |  500.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 1000.0          | 500.0         | 8.75     | 0.0  | 0.0       | 508.75 | 0.0  | 0.0        | 0.0  | 508.75      |
      | 2  | 28   | 01 March 2025    |           |  500.0          | 500.0         | 8.75     | 0.0  | 0.0       | 508.75 | 0.0  | 0.0        | 0.0  | 508.75      |
      | 3  | 31   | 01 April 2025    |           |    0.0          | 500.0         | 8.75     | 0.0  | 0.0       | 508.75 | 0.0  | 0.0        | 0.0  | 508.75      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 26.25    | 0.0  | 0.0       | 1526.25 | 0.0  | 0.0        | 0.0  | 1526.25     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2025  | Disbursement      |  500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    | false    |
#    --- undo last disbursement --- #
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 666.67          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 0.0  | 0.0        | 0.0  | 339.16      |
      | 2  | 28   | 01 March 2025    |           | 333.34          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 0.0  | 0.0        | 0.0  | 339.16      |
      | 3  | 31   | 01 April 2025    |           |   0.0           | 333.34        | 5.84     | 0.0  | 0.0       | 339.18 | 0.0  | 0.0        | 0.0  | 339.18      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 17.5    | 0.0  | 0.0       | 1017.5  | 0.0  | 0.0        | 0.0  | 1017.5      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |

    When Loan Pay-off is made on "15 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4119
  Scenario: Verify cumulative multidisbursal loan with 2nd disb at 2nd installment with flat interest type and same_as_repeyment interest calculation period - UC2
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type    | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT_SAR_RECALCULATION_SAME_AS_REPAYMENT_ACTUAL_ACTUAL_MULTIDISB | 01 January 2025   | 1500           | 7                      | FLAT             | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2025" with "1500" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 666.67          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 0.0  | 0.0        | 0.0  | 339.16      |
      | 2  | 28   | 01 March 2025    |           | 333.34          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 0.0  | 0.0        | 0.0  | 339.16      |
      | 3  | 31   | 01 April 2025    |           |   0.0           | 333.34        | 5.84     | 0.0  | 0.0       | 339.18 | 0.0  | 0.0        | 0.0  | 339.18      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 17.5     | 0.0  | 0.0       | 1017.5  | 0.0  | 0.0        | 0.0  | 1017.5      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
# -- 2nd disb - on Feb, 15, 2025 --#
    When Admin sets the business date to "15 February 2025"
    When Admin successfully disburse the loan on "15 February 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           |  666.67         | 333.33        | 8.75     | 0.0  | 0.0       | 342.08 | 0.0  | 0.0        | 0.0  | 342.08      |
      |    |      | 15 February 2025 |           |  500.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 2  | 28   | 01 March 2025    |           |  666.67         | 500.0         | 8.75     | 0.0  | 0.0       | 508.75 | 0.0  | 0.0        | 0.0  | 508.75      |
      | 3  | 31   | 01 April 2025    |           |    0.0          | 666.67        | 8.75     | 0.0  | 0.0       | 675.42 | 0.0  | 0.0        | 0.0  | 675.42      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 26.25    | 0.0  | 0.0       | 1526.25 | 0.0  | 0.0        | 0.0  | 1526.25    |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 February 2025 | Disbursement      |  500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    | false    |
#    --- undo disbursement --- #
    When Admin successfully undo disbursal
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1500.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 1000.0          | 500.0         | 8.75     | 0.0  | 0.0       | 508.75 | 0.0  | 0.0        | 0.0  | 508.75      |
      | 2  | 28   | 01 March 2025    |           |  500.0          | 500.0         | 8.75     | 0.0  | 0.0       | 508.75 | 0.0  | 0.0        | 0.0  | 508.75      |
      | 3  | 31   | 01 April 2025    |           |   0.0           | 500.0         | 8.75     | 0.0  | 0.0       | 508.75 | 0.0  | 0.0        | 0.0  | 508.75      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 26.25    | 0.0  | 0.0       | 1526.25 | 0.0  | 0.0        | 0.0  | 1526.25    |
    Then Loan Transactions tab has none transaction

    Then Admin can successfully undone the loan approval
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"
    And Admin successfully rejects the loan on "15 February 2025"
    Then Loan status will be "REJECTED"

  @TestRailId:C4120
  Scenario: Verify cumulative multidisbursal loan with repayment between disbursements with flat interest type and same_as_repeyment interest calculation period - UC3
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type    | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT_SAR_RECALCULATION_SAME_AS_REPAYMENT_ACTUAL_ACTUAL_MULTIDISB | 01 January 2025   | 1500           | 7                      | FLAT             | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "01 January 2025" with "1500" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 666.67          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 0.0  | 0.0        | 0.0  | 339.16      |
      | 2  | 28   | 01 March 2025    |           | 333.34          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 0.0  | 0.0        | 0.0  | 339.16      |
      | 3  | 31   | 01 April 2025    |           |   0.0           | 333.34        | 5.84     | 0.0  | 0.0       | 339.18 | 0.0  | 0.0        | 0.0  | 339.18      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 17.5     | 0.0  | 0.0       | 1017.5  | 0.0  | 0.0        | 0.0  | 1017.5      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
# -- repayment on Feb, 1, 2025 ---#
    When Admin sets the business date to "01 February 2025"
    And Customer makes "AUTOPAY" repayment on "01 February 2025" with 339.16 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 01 February 2025 | 666.67          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 339.16 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    |                  | 333.34          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 0.0    | 0.0        | 0.0  | 339.16      |
      | 3  | 31   | 01 April 2025    |                  |   0.0           | 333.34        | 5.84     | 0.0  | 0.0       | 339.18 | 0.0    | 0.0        | 0.0  | 339.18      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000.0        | 17.5     | 0.0  | 0.0       | 1017.5  | 339.16 | 0.0        | 0.0  | 678.34      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2025 | Repayment         | 339.16 | 333.33    | 5.83     | 0.0  | 0.0       | 666.67       | false    | false    |
# -- 2nd disb - on Feb, 15, 2025 --#
    When Admin sets the business date to "15 February 2025"
    When Admin successfully disburse the loan on "15 February 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 |                  | 666.67          | 333.33        | 8.75     | 0.0  | 0.0       | 342.08 | 339.16 | 0.0        | 0.0  | 2.92        |
      |    |      | 15 February 2025 |                  |  500.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 2  | 28   | 01 March 2025    |                  |  666.67         | 500.0         | 8.75     | 0.0  | 0.0       | 508.75 | 0.0    | 0.0        | 0.0  | 508.75      |
      | 3  | 31   | 01 April 2025    |                  |    0.0          | 666.67        | 8.75     | 0.0  | 0.0       | 675.42 | 0.0    | 0.0        | 0.0  | 675.42      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1500.0        | 26.25    | 0.0  | 0.0       | 1526.25 | 339.16 | 0.0        | 0.0  | 1187.09     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2025 | Repayment         | 339.16 | 330.41    | 8.75     | 0.0  | 0.0       | 669.59       | false    | true     |
      | 15 February 2025 | Disbursement      |  500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1169.59      | false    | false    |

    When Loan Pay-off is made on "15 February 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4121
  Scenario: Verify cumulative multidisbursal loan with flat interest type and same_as_repeyment interest calculation period with down payment - UC4
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type    | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        | 1st_tranche_disb_expected_date |1st_tranche_disb_principal | 2nd_tranche_disb_expected_date |2nd_tranche_disb_principal |
      | LP1_INTEREST_FLAT_SAR_RECALCULATION_SAME_AS_REPAYMENT_MULTIDISB_AUTO_DOWNPAYMENT | 01 January 2025   | 1500           | 7                      | FLAT             | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER | 01 January 2025                | 1000.0                    | 15 January 2025                | 500.0                     |
    And Admin successfully approves the loan on "01 January 2025" with "1500" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2025  | 01 January 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  |   0.0       |
      | 2  | 31   | 01 February 2025 |                 | 500.0           | 250.0         | 5.75     | 0.0  | 0.0       | 255.75 | 0.0   | 0.0        | 0.0  | 255.75      |
      | 3  | 28   | 01 March 2025    |                 | 250.0           | 250.0         | 5.75     | 0.0  | 0.0       | 255.75 | 0.0   | 0.0        | 0.0  | 255.75      |
      | 4  | 31   | 01 April 2025    |                 |   0.0           | 250.0         | 5.76     | 0.0  | 0.0       | 255.76 | 0.0   | 0.0        | 0.0  | 255.76      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 17.26    | 0.0  | 0.0       | 1017.26 | 250.0 | 0.0        | 0.0  | 767.26      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2025  | Down Payment      | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
# -- 2nd disb - on Jan, 15, 2025 --#
    When Admin sets the business date to "15 January 2025"
    When Admin successfully disburse the loan on "15 January 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2025  | 01 January 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  |   0.0       |
      |    |      | 15 January 2025  |                 | 500.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 2  | 0    | 15 January 2025  | 15 January 2025 | 1125.0          | 125.0         | 0.0      | 0.0  | 0.0       | 125.0  | 125.0 | 0.0        | 0.0  |   0.0       |
      | 3  | 31   | 01 February 2025 |                 | 750.0           | 375.0         | 8.63     | 0.0  | 0.0       | 383.63 | 0.0   | 0.0        | 0.0  | 383.63      |
      | 4  | 28   | 01 March 2025    |                 | 375.0           | 375.0         | 8.63     | 0.0  | 0.0       | 383.63 | 0.0   | 0.0        | 0.0  | 383.63      |
      | 5  | 31   | 01 April 2025    |                 |   0.0           | 375.0         | 8.63     | 0.0  | 0.0       | 383.63 | 0.0   | 0.0        | 0.0  | 383.63      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1500.0        | 25.89    | 0.0  | 0.0       | 1525.89 | 375.0 | 0.0        | 0.0  | 1150.89     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2025  | Down Payment      | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 15 January 2025  | Disbursement      | 500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1250.0       | false    | false    |
      | 15 January 2025  | Down Payment      | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 1125.0       | false    | false    |

    When Loan Pay-off is made on "15 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4122
  Scenario: Verify cumulative multidisbursal loan with flat interest type and same_as_repeyment interest calculation period with approved over applied amount - UC5
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type    | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        | 1st_tranche_disb_expected_date |1st_tranche_disb_principal | 2nd_tranche_disb_expected_date |2nd_tranche_disb_principal |
      | LP1_INTEREST_FLAT_SAR_RECALCULATION_DAILY_360_30_APPROVED_OVER_APPLIED_MULTIDISB | 01 January 2025   | 1000           | 7                      | FLAT             | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER | 01 January 2025                | 1000.0                    | 15 January 2025                | 500.0                     |
    And Admin successfully approves the loan on "01 January 2025" with "1200" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 666.67          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 0.0  | 0.0        | 0.0  | 339.16      |
      | 2  | 28   | 01 March 2025    |           | 333.34          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 0.0  | 0.0        | 0.0  | 339.16      |
      | 3  | 31   | 01 April 2025    |           |   0.0           | 333.34        | 5.84     | 0.0  | 0.0       | 339.18 | 0.0  | 0.0        | 0.0  | 339.18      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 17.5     | 0.0  | 0.0       | 1017.5  | 0.0  | 0.0        | 0.0  | 1017.5      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 1000.0      |                      |
# -- 2nd disb - on Feb, 15, 2025 --#
    When Admin sets the business date to "15 January 2025"
    When Admin successfully disburse the loan on "15 January 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2025  |           |  500.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 1000.0          | 500.0         | 8.75     | 0.0  | 0.0       | 508.75 | 0.0  | 0.0        | 0.0  | 508.75      |
      | 2  | 28   | 01 March 2025    |           |  500.0          | 500.0         | 8.75     | 0.0  | 0.0       | 508.75 | 0.0  | 0.0        | 0.0  | 508.75      |
      | 3  | 31   | 01 April 2025    |           |    0.0          | 500.0         | 8.75     | 0.0  | 0.0       | 508.75 | 0.0  | 0.0        | 0.0  | 508.75      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 26.25    | 0.0  | 0.0       |1526.25  | 0.0  | 0.0        | 0.0  | 1526.25     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2025  | Disbursement      |  500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 1000.0      |                      |
      | 01 January 2025          | 15 January 2025 |  500.0      |                      |

    When Loan Pay-off is made on "15 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4123
  Scenario: Verify cumulative multidisbursal loan with undo last disb with flat interest type and daily interest calculation period - UC6
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type    | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        | 1st_tranche_disb_expected_date |1st_tranche_disb_principal | 2nd_tranche_disb_expected_date |2nd_tranche_disb_principal |
      | LP1_INTEREST_FLAT_DAILY_RECALCULATION_DAILY_360_30_MULTIDISB | 01 January 2025   | 1500           | 7                      | FLAT             | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER | 01 January 2025                | 1000.0                    | 15 January 2025                | 500.0                     |
    And Admin successfully approves the loan on "01 January 2025" with "1500" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 666.67          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 0.0  | 0.0        | 0.0  | 339.16      |
      | 2  | 28   | 01 March 2025    |           | 333.34          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 0.0  | 0.0        | 0.0  | 339.16      |
      | 3  | 31   | 01 April 2025    |           |   0.0           | 333.34        | 5.84     | 0.0  | 0.0       | 339.18 | 0.0  | 0.0        | 0.0  | 339.18      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 17.5     | 0.0  | 0.0       | 1017.5  | 0.0  | 0.0        | 0.0  | 1017.5      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 1000.0      |                      |
# -- 2nd disb - on Feb, 15, 2025 --#
    When Admin sets the business date to "15 January 2025"
    When Admin successfully disburse the loan on "15 January 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2025  |           |  500.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 1000.0          | 500.0         | 8.75     | 0.0  | 0.0       | 508.75 | 0.0  | 0.0        | 0.0  | 508.75      |
      | 2  | 28   | 01 March 2025    |           |  500.0          | 500.0         | 8.75     | 0.0  | 0.0       | 508.75 | 0.0  | 0.0        | 0.0  | 508.75      |
      | 3  | 31   | 01 April 2025    |           |    0.0          | 500.0         | 8.75     | 0.0  | 0.0       | 508.75 | 0.0  | 0.0        | 0.0  | 508.75      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 26.25    | 0.0  | 0.0       | 1526.25 | 0.0  | 0.0        | 0.0  | 1526.25     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2025  | Disbursement      |  500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 1000.0      |                      |
      | 01 January 2025          | 15 January 2025 |  500.0      |                      |
#    --- undo last disbursement --- #
    When Admin successfully undo last disbursal
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 666.67          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 0.0  | 0.0        | 0.0  | 339.16      |
      | 2  | 28   | 01 March 2025    |           | 333.34          | 333.33        | 5.83     | 0.0  | 0.0       | 339.16 | 0.0  | 0.0        | 0.0  | 339.16      |
      | 3  | 31   | 01 April 2025    |           |   0.0           | 333.34        | 5.84     | 0.0  | 0.0       | 339.18 | 0.0  | 0.0        | 0.0  | 339.18      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000.0        | 17.5     | 0.0  | 0.0       | 1017.5  | 0.0  | 0.0        | 0.0  | 1017.5      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 1000.0      |                      |

    When Loan Pay-off is made on "15 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4201
  Scenario: Verify repayment reversal after adding NSF fee charge with transaction reprocessing
    When Admin sets the business date to "06 November 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_360_30_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF_ACCRUAL_ACTIVITY | 21 August 2025    | 102.47         | 11.3                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "21 August 2025" with "102.47" amount and expected disbursement date on "21 August 2025"
    And Admin successfully disburse the loan on "21 August 2025" with "102.47" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "21 September 2025" with 34.80 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "21 September 2025"
    And Customer makes "AUTOPAY" repayment on "26 September 2025" with 34.79 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "29 September 2025" with 0.01 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "30 September 2025" with 71.63 EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "30 September 2025" due date and 2.8 EUR transaction amount
    When Customer undo "1"th "Repayment" transaction made on "26 September 2025"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date         | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 21 August 2025    |                   | 102.47          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 21 September 2025 | 30 September 2025 | 68.63           | 33.84         | 0.96     | 0.0  | 0.0       | 34.8  | 34.8  | 0.0        | 34.8 | 0.0         |
      | 2  | 30   | 21 October 2025   |                   | 34.35           | 34.28         | 0.52     | 0.0  | 2.8       | 37.6  | 36.84 | 36.84      | 0.0  | 0.76        |
      | 3  | 31   | 21 November 2025  |                   | 0.0             | 34.35         | 0.32     | 0.0  | 0.0       | 34.67 | 0.0   | 0.0        | 0.0  | 34.67       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 102.47        | 1.8      | 0.0  | 2.8       | 107.07 | 71.64 | 36.84      | 34.8 | 35.43       |
    Then Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 21 August 2025    | Disbursement     | 102.47 | 0.0       | 0.0      | 0.0  | 0.0       | 102.47       | false    | false    |
      | 21 September 2025 | Repayment        | 34.8   | 33.84     | 0.96     | 0.0  | 0.0       | 68.63        | true     | false    |
      | 21 September 2025 | Accrual Activity | 0.96   | 0.0       | 0.96     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 26 September 2025 | Repayment        | 34.79  | 33.84     | 0.95     | 0.0  | 0.0       | 68.63        | true     | false    |
      | 29 September 2025 | Repayment        | 0.01   | 0.01      | 0.0      | 0.0  | 0.0       | 102.46       | false    | true     |
      | 30 September 2025 | Repayment        | 71.63  | 67.87     | 0.96     | 0.0  | 2.8       | 34.59        | false    | true     |
      | 21 October 2025   | Accrual Activity | 3.32   | 0.0       | 0.52     | 0.0  | 2.8       | 0.0          | false    | true     |
      | 06 November 2025  | Accrual          | 1.21   | 0.0       | 1.21     | 0.0  | 0.0       | 0.0          | false    | false    |
    And Customer makes "AUTOPAY" repayment on "06 November 2025" with 35.28 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C4124
  Scenario: Verify cumulative multidisbursal loan that expects tranches with flat interest type and daily interest calculation period - UC7
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with disbursements details and following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type    | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        | 1st_tranche_disb_expected_date |1st_tranche_disb_principal | 2nd_tranche_disb_expected_date |2nd_tranche_disb_principal |
      | LP1_INTEREST_FLAT_DAILY_RECALCULATION_SAR_MULTIDISB_EXPECT_TRANCHES | 01 January 2025   | 1500           | 7                      | FLAT             | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER | 01 January 2025                | 1000.0                    | 15 January 2025                | 500.0                     |
    And Admin successfully approves the loan on "01 January 2025" with "1500" amount and expected disbursement date on "01 January 2025"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      |    |      | 15 January 2025  |           |  500.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 1000.0          | 500.0         | 8.63     | 0.0  | 0.0       | 508.63 | 0.0  | 0.0        | 0.0  | 508.63      |
      | 2  | 28   | 01 March 2025    |           |  500.0          | 500.0         | 8.63     | 0.0  | 0.0       | 508.63 | 0.0  | 0.0        | 0.0  | 508.63      |
      | 3  | 31   | 01 April 2025    |           |    0.0          | 500.0         | 8.63     | 0.0  | 0.0       | 508.63 | 0.0  | 0.0        | 0.0  | 508.63      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 25.89    | 0.0  | 0.0       | 1525.89 | 0.0  | 0.0        | 0.0  | 1525.89     |
    Then Loan Transactions tab has none transaction
    When Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2025  |           |  500.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 1000.0          | 500.0         | 5.75     | 0.0  | 0.0       | 505.75 | 0.0  | 0.0        | 0.0  | 505.75      |
      | 2  | 28   | 01 March 2025    |           |  500.0          | 500.0         | 5.75     | 0.0  | 0.0       | 505.75 | 0.0  | 0.0        | 0.0  | 505.75      |
      | 3  | 31   | 01 April 2025    |           |    0.0          | 500.0         | 5.76     | 0.0  | 0.0       | 505.76 | 0.0  | 0.0        | 0.0  | 505.76      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 17.26    | 0.0  | 0.0       | 1517.26 | 0.0  | 0.0        | 0.0  | 1517.26     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 1000.0      |                      |
      | 15 January 2025          |                 |  500.0      |                      |
# -- 2nd disb - on Jan, 15, 2025 --#
     When Admin sets the business date to "15 January 2025"
    When Admin successfully disburse the loan on "15 January 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2025  |           |  500.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 1000.0          | 500.0         | 8.63     | 0.0  | 0.0       | 508.63 | 0.0  | 0.0        | 0.0  | 508.63      |
      | 2  | 28   | 01 March 2025    |           |  500.0          | 500.0         | 8.63     | 0.0  | 0.0       | 508.63 | 0.0  | 0.0        | 0.0  | 508.63      |
      | 3  | 31   | 01 April 2025    |           |    0.0          | 500.0         | 8.63     | 0.0  | 0.0       | 508.63 | 0.0  | 0.0        | 0.0  | 508.63      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 25.89    | 0.0  | 0.0       | 1525.89 | 0.0  | 0.0        | 0.0  | 1525.89     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 15 January 2025  | Disbursement      |  500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 1000.0      |                      |
      | 15 January 2025          | 15 January 2025 |  500.0      |                      |
    When Loan Pay-off is made on "15 January 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C4227
  Scenario: Verify cumulative multidisbursal loan that expects tranches with flat interest type and no interest calculation period - UC7.1
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with disbursements details and following data:
      | LoanProduct                                                     | submitted on date | with Principal | ANNUAL interest rate % | interest type    | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        | 1st_tranche_disb_expected_date |1st_tranche_disb_principal | 2nd_tranche_disb_expected_date |2nd_tranche_disb_principal |
      | LP1_INTEREST_FLAT_DAILY_ACTUAL_ACTUAL_MULTIDISB_EXPECT_TRANCHES | 01 January 2025   | 1500           | 7                      | FLAT             | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER | 01 January 2025                | 1000.0                    | 15 January 2025                | 500.0                     |
    And Admin successfully approves the loan on "01 January 2025" with "1500" amount and expected disbursement date on "01 January 2025"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      |    |      | 15 January 2025  |           |  500.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 1000.0          | 500.0         | 8.63     | 0.0  | 0.0       | 508.63 | 0.0  | 0.0        | 0.0  | 508.63      |
      | 2  | 28   | 01 March 2025    |           |  500.0          | 500.0         | 8.63     | 0.0  | 0.0       | 508.63 | 0.0  | 0.0        | 0.0  | 508.63      |
      | 3  | 31   | 01 April 2025    |           |    0.0          | 500.0         | 8.63     | 0.0  | 0.0       | 508.63 | 0.0  | 0.0        | 0.0  | 508.63      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 25.89    | 0.0  | 0.0       | 1525.89 | 0.0  | 0.0        | 0.0  | 1525.89     |
    Then Loan Transactions tab has none transaction
    When Admin disburses the loan on "01 January 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2025  |           |  500.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 1000.0          | 500.0         | 5.75     | 0.0  | 0.0       | 505.75 | 0.0  | 0.0        | 0.0  | 505.75      |
      | 2  | 28   | 01 March 2025    |           |  500.0          | 500.0         | 5.75     | 0.0  | 0.0       | 505.75 | 0.0  | 0.0        | 0.0  | 505.75      |
      | 3  | 31   | 01 April 2025    |           |    0.0          | 500.0         | 5.76     | 0.0  | 0.0       | 505.76 | 0.0  | 0.0        | 0.0  | 505.76      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 17.26    | 0.0  | 0.0       | 1517.26 | 0.0  | 0.0        | 0.0  | 1517.26     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    Then LoanDisbursalTransactionBusinessEvent has changedTerms "false"
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 1000.0      |                      |
      | 15 January 2025          |                 |  500.0      |                      |
    When Admin sets the business date to "16 January 2025"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2025  |           |  500.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 1000.0          | 500.0         | 5.75     | 0.0  | 0.0       | 505.75 | 0.0  | 0.0        | 0.0  | 505.75      |
      | 2  | 28   | 01 March 2025    |           |  500.0          | 500.0         | 5.75     | 0.0  | 0.0       | 505.75 | 0.0  | 0.0        | 0.0  | 505.75      |
      | 3  | 31   | 01 April 2025    |           |    0.0          | 500.0         | 5.76     | 0.0  | 0.0       | 505.76 | 0.0  | 0.0        | 0.0  | 505.76      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 17.26    | 0.0  | 0.0       | 1517.26 | 0.0  | 0.0        | 0.0  | 1517.26     |
    When Admin sets the business date to "01 February 2025"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 15 January 2025  |           |  500.0          |               |          | 0.0  |           | 0.0    |      |            |      | 0.0         |
      | 1  | 31   | 01 February 2025 |           | 1000.0          | 500.0         | 5.75     | 0.0  | 0.0       | 505.75 | 0.0  | 0.0        | 0.0  | 505.75      |
      | 2  | 28   | 01 March 2025    |           |  500.0          | 500.0         | 5.75     | 0.0  | 0.0       | 505.75 | 0.0  | 0.0        | 0.0  | 505.75      |
      | 3  | 31   | 01 April 2025    |           |    0.0          | 500.0         | 5.76     | 0.0  | 0.0       | 505.76 | 0.0  | 0.0        | 0.0  | 505.76      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 17.26    | 0.0  | 0.0       | 1517.26 | 0.0  | 0.0        | 0.0  | 1517.26     |
# -- 2nd disbursement - on Feb, 1, 2025 --#
    When Admin disburses the loan on "01 February 2025" with "500" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      |    |      | 01 February 2025 |           |  500.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 1000.0          | 500.0         | 8.63     | 0.0  | 0.0       | 508.63 | 0.0  | 0.0        | 0.0  | 508.63      |
      | 2  | 28   | 01 March 2025    |           |  500.0          | 500.0         | 8.63     | 0.0  | 0.0       | 508.63 | 0.0  | 0.0        | 0.0  | 508.63      |
      | 3  | 31   | 01 April 2025    |           |    0.0          | 500.0         | 8.63     | 0.0  | 0.0       | 508.63 | 0.0  | 0.0        | 0.0  | 508.63      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1500.0        | 25.89    | 0.0  | 0.0       | 1525.89 | 0.0  | 0.0        | 0.0  | 1525.89     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2025 | Disbursement      |  500.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On     | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025  | 1000.0      |                      |
      | 15 January 2025          | 01 February 2025 |  500.0      |                      |
    Then LoanDisbursalTransactionBusinessEvent has changedTerms "false"
    When Loan Pay-off is made on "01 February 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      |    |      | 01 February 2025 |                  |  500.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 31   | 01 February 2025 | 01 February 2025 | 1000.0          | 500.0         | 8.63     | 0.0  | 0.0       | 508.63 | 508.63 | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 01 March 2025    | 01 February 2025 |  500.0          | 500.0         | 8.63     | 0.0  | 0.0       | 508.63 | 508.63 | 508.63     | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2025    | 01 February 2025 |    0.0          | 500.0         | 8.63     | 0.0  | 0.0       | 508.63 | 508.63 | 508.63     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1500.0        | 25.89    | 0.0  | 0.0       | 1525.89 | 1525.89 | 1017.26    | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement      | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 February 2025 | Disbursement      |  500.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    | false    |
      | 01 February 2025 | Repayment         | 1525.89 | 1500.0    | 25.89    | 0.0  | 0.0       |    0.0       | false    | false    |
      | 01 February 2025 | Accrual           |  25.89  | 0.0       | 25.89    | 0.0  | 0.0       |    0.0       | false    | false    |

  @TestRailId:C4643
  Scenario: Verify that changedTerms is false in LoanDisbursalTransactionBusinessEvent for initial disbursement
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin disburses the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 15   | 16 January 2024  |                  | 667.64          | 332.36        | 2.92     | 0.0  | 0.0       | 335.28 | 0.0   | 0.0        | 0.0  | 335.28      |
      | 2  | 15   | 31 January 2024  |                  | 334.31          | 333.33        | 1.95     | 0.0  | 0.0       | 335.28 | 0.0   | 0.0        | 0.0  | 335.28      |
      | 3  | 15   | 15 February 2024 |                  | 0.0             | 334.31        | 0.98     | 0.0  | 0.0       | 335.29 | 0.0   | 0.0        | 0.0  | 335.29      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 5.85     | 0.0  | 0.0       | 1005.85 | 0.0   | 0.0        | 0.0  | 1005.85     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
    Then LoanDisbursalTransactionBusinessEvent has changedTerms "false"

  @TestRailId:C4645
  Scenario: Verify that changedTerms is false in LoanDisbursalTransactionBusinessEvent when additional disbursement does not change terms
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_MULTIDISBURSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                           | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_MULTIDISBURSE | 01 January 2024   | 300            | 9.4822                 | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "300" amount and expected disbursement date on "01 January 2024"
    When Admin disburses the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |                  | 83.66           | 16.34         | 0.79     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 2  | 29   | 01 March 2024    |                  | 67.19           | 16.47         | 0.66     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 3  | 31   | 01 April 2024    |                  | 50.59           | 16.6          | 0.53     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 4  | 30   | 01 May 2024      |                  | 33.86           | 16.73         | 0.4      | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 5  | 31   | 01 June 2024     |                  | 17.0            | 16.86         | 0.27     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 17.0          | 0.13     | 0.0  | 0.0       | 17.13 | 0.0   | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.78     | 0.0  | 0.0       | 102.78 | 0.0   | 0.0        | 0.0  | 102.78      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
    Then LoanDisbursalTransactionBusinessEvent has changedTerms "false"
    When Admin sets the business date to "08 January 2024"
    When Admin disburses the loan on "08 January 2024" with "200" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      |    |      | 08 January 2024  |                  | 200.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |                  | 250.68          | 49.32         | 2.01     | 0.0  | 0.0       | 51.33 | 0.0   | 0.0        | 0.0  | 51.33       |
      | 2  | 29   | 01 March 2024    |                  | 201.33          | 49.35         | 1.98     | 0.0  | 0.0       | 51.33 | 0.0   | 0.0        | 0.0  | 51.33       |
      | 3  | 31   | 01 April 2024    |                  | 151.59          | 49.74         | 1.59     | 0.0  | 0.0       | 51.33 | 0.0   | 0.0        | 0.0  | 51.33       |
      | 4  | 30   | 01 May 2024      |                  | 101.46          | 50.13         | 1.2      | 0.0  | 0.0       | 51.33 | 0.0   | 0.0        | 0.0  | 51.33       |
      | 5  | 31   | 01 June 2024     |                  | 50.93           | 50.53         | 0.8      | 0.0  | 0.0       | 51.33 | 0.0   | 0.0        | 0.0  | 51.33       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 50.93         | 0.4      | 0.0  | 0.0       | 51.33 | 0.0   | 0.0        | 0.0  | 51.33       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 300.0         | 7.98     | 0.0  | 0.0       | 307.98 | 0.0   | 0.0        | 0.0  | 307.98      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 08 January 2024  | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 300.0        | false    | false    |
    Then LoanDisbursalTransactionBusinessEvent has changedTerms "false"
    When Loan Pay-off is made on "08 January 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C70224
  Scenario: Verify max disb amount validation in case multidisb loan that expect tranches with overapplied setting enabled - UC1
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with disbursement details and following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date |1st_tranche_disb_principal |
      | LP2_PROGRESSIVE_ADV_PYMNT_INTEREST_RECALC_360_30_MULTIDISB_OVER_APPLIED_EXPECTED_TRANCHES | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2024                | 1000.0                    |
    And Admin successfully approves the loan on "1 January 2024" with "1200" amount and expected disbursement date on "1 January 2024"
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 500
    Then Loan status will be "APPROVED"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0    |       |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |                  | 835.74          | 164.26        | 5.83     | 0.0  | 0.0       | 170.09 | 0.0   | 0.0        | 0.0  | 170.09      |
      | 2  | 29   | 01 March 2024    |                  | 670.53          | 165.21        | 4.88     | 0.0  | 0.0       | 170.09 | 0.0   | 0.0        | 0.0  | 170.09      |
      | 3  | 31   | 01 April 2024    |                  | 504.35          | 166.18        | 3.91     | 0.0  | 0.0       | 170.09 | 0.0   | 0.0        | 0.0  | 170.09      |
      | 4  | 30   | 01 May 2024      |                  | 337.2           | 167.15        | 2.94     | 0.0  | 0.0       | 170.09 | 0.0   | 0.0        | 0.0  | 170.09      |
      | 5  | 31   | 01 June 2024     |                  | 169.08          | 168.12        | 1.97     | 0.0  | 0.0       | 170.09 | 0.0   | 0.0        | 0.0  | 170.09      |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 169.08        | 0.99     | 0.0  | 0.0       | 170.07 | 0.0   | 0.0        | 0.0  | 170.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 20.52    | 0.0  | 0.0       | 1020.52 | 0.0   | 0.0        | 0.0  | 1020.52     |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2024          |                 | 1000.0      |                      |
    And Admin successfully add disbursement detail to the loan on "5 January 2024" with 200 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0    |       |            |      | 0.0         |
      |    |      | 05 January 2024  |                  | 200.0           |               |          | 0.0  |           | 0.0    |       |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |                  | 1002.77         | 197.23        | 6.85     | 0.0  | 0.0       | 204.08 | 0.0   | 0.0        | 0.0  | 204.08      |
      | 2  | 29   | 01 March 2024    |                  | 804.54          | 198.23        | 5.85     | 0.0  | 0.0       | 204.08 | 0.0   | 0.0        | 0.0  | 204.08      |
      | 3  | 31   | 01 April 2024    |                  | 605.15          | 199.39        | 4.69     | 0.0  | 0.0       | 204.08 | 0.0   | 0.0        | 0.0  | 204.08      |
      | 4  | 30   | 01 May 2024      |                  | 404.6           | 200.55        | 3.53     | 0.0  | 0.0       | 204.08 | 0.0   | 0.0        | 0.0  | 204.08      |
      | 5  | 31   | 01 June 2024     |                  | 202.88          | 201.72        | 2.36     | 0.0  | 0.0       | 204.08 | 0.0   | 0.0        | 0.0  | 204.08      |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 202.88        | 1.18     | 0.0  | 0.0       | 204.06 | 0.0   | 0.0        | 0.0  | 204.06      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1200.0        | 24.46    | 0.0  | 0.0       | 1224.46 | 0.0   | 0.0        | 0.0  | 1224.46     |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2024          |                 | 1000.0      |                      |
      | 05 January 2024          |                 | 200.0       | 1200.0               |
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 300
    Then Admin fails to disburse the loan on "1 January 2024" with "1600" EUR transaction amount because of wrong amount
    Then Admin fails to disburse the loan on "1 January 2024" with "1500" EUR transaction amount because of wrong amount
    And Admin successfully disburse the loan on "1 January 2024" with "1300" EUR transaction amount
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 0
    Then Loan status will be "ACTIVE"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1300.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      |    |      | 05 January 2024  |                  | 200.0           |               |          | 0.0  |           | 0.0    |       |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |                  | 1286.47         | 213.53        | 7.58     | 0.0  | 0.0       | 221.11 | 0.0   | 0.0        | 0.0  | 221.11      |
      | 2  | 29   | 01 March 2024    |                  | 1071.7          | 214.77        | 6.34     | 0.0  | 0.0       | 221.11 | 0.0   | 0.0        | 0.0  | 221.11      |
      | 3  | 31   | 01 April 2024    |                  | 855.67          | 216.03        | 5.08     | 0.0  | 0.0       | 221.11 | 0.0   | 0.0        | 0.0  | 221.11      |
      | 4  | 30   | 01 May 2024      |                  | 638.38          | 217.29        | 3.82     | 0.0  | 0.0       | 221.11 | 0.0   | 0.0        | 0.0  | 221.11      |
      | 5  | 31   | 01 June 2024     |                  | 419.83          | 218.55        | 2.56     | 0.0  | 0.0       | 221.11 | 0.0   | 0.0        | 0.0  | 221.11      |
      | 6  | 30   | 01 July 2024     |                  | 200.0           | 219.83        | 1.28     | 0.0  | 0.0       | 221.11 | 0.0   | 0.0        | 0.0  | 221.11      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1300.0        | 26.66    | 0.0  | 0.0       | 1326.66 | 0.0   | 0.0        | 0.0  | 1326.66     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1300.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1300.0       | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2024          | 01 January 2024 | 1300.0      |                      |
      | 05 January 2024          |                 | 200.0       | 1200.0               |
    When Admin sets the business date to "5 January 2024"
    Then Admin fails to disburse the loan on "5 January 2024" with "300" EUR transaction amount because of wrong amount
    And Admin successfully disburse the loan on "5 January 2024" with "200" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1300.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      |    |      | 05 January 2024  |                  | 200.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |                  | 1253.5          | 246.5         | 8.6      | 0.0  | 0.0       | 255.1  | 0.0   | 0.0        | 0.0  | 255.1       |
      | 2  | 29   | 01 March 2024    |                  | 1005.71         | 247.79        | 7.31     | 0.0  | 0.0       | 255.1  | 0.0   | 0.0        | 0.0  | 255.1       |
      | 3  | 31   | 01 April 2024    |                  | 756.48          | 249.23        | 5.87     | 0.0  | 0.0       | 255.1  | 0.0   | 0.0        | 0.0  | 255.1       |
      | 4  | 30   | 01 May 2024      |                  | 505.79          | 250.69        | 4.41     | 0.0  | 0.0       | 255.1  | 0.0   | 0.0        | 0.0  | 255.1       |
      | 5  | 31   | 01 June 2024     |                  | 253.64          | 252.15        | 2.95     | 0.0  | 0.0       | 255.1  | 0.0   | 0.0        | 0.0  | 255.1       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 253.64        | 1.48     | 0.0  | 0.0       | 255.12 | 0.0   | 0.0        | 0.0  | 255.12      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1500.0        | 30.62    | 0.0  | 0.0       | 1530.62 | 0.0   | 0.0        | 0.0  | 1530.62     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1300.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1300.0       | false    | false    |
      | 05 January 2024  | Disbursement     | 200.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    | false    |
    # --- close loan --- #
    When Loan Pay-off is made on "5 January 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C70225
  Scenario: Verify max disb amount validation in case multidisb loan that expect tranches with overapplied setting enabled - UC2
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with disbursement details and following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date |1st_tranche_disb_principal |
      | LP2_PROGRESSIVE_ADV_PYMNT_INTEREST_RECALC_360_30_MULTIDISB_OVER_APPLIED_EXPECTED_TRANCHES | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2024                | 1000.0                    |
    And Admin successfully approves the loan on "1 January 2024" with "1200" amount and expected disbursement date on "1 January 2024"
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 500
    Then Loan status will be "APPROVED"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          |               |          | 0.0  |           | 0.0    |       |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |                  | 835.74          | 164.26        | 5.83     | 0.0  | 0.0       | 170.09 | 0.0   | 0.0        | 0.0  | 170.09      |
      | 2  | 29   | 01 March 2024    |                  | 670.53          | 165.21        | 4.88     | 0.0  | 0.0       | 170.09 | 0.0   | 0.0        | 0.0  | 170.09      |
      | 3  | 31   | 01 April 2024    |                  | 504.35          | 166.18        | 3.91     | 0.0  | 0.0       | 170.09 | 0.0   | 0.0        | 0.0  | 170.09      |
      | 4  | 30   | 01 May 2024      |                  | 337.2           | 167.15        | 2.94     | 0.0  | 0.0       | 170.09 | 0.0   | 0.0        | 0.0  | 170.09      |
      | 5  | 31   | 01 June 2024     |                  | 169.08          | 168.12        | 1.97     | 0.0  | 0.0       | 170.09 | 0.0   | 0.0        | 0.0  | 170.09      |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 169.08        | 0.99     | 0.0  | 0.0       | 170.07 | 0.0   | 0.0        | 0.0  | 170.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 20.52    | 0.0  | 0.0       | 1020.52 | 0.0   | 0.0        | 0.0  | 1020.52     |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2024          |                 | 1000.0      |                      |
    And Admin successfully add disbursement detail to the loan on "5 January 2024" with 200 EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2024          |                 | 1000.0      |                      |
      | 05 January 2024          |                 | 200.0       | 1200.0               |
    And Admin checks available disbursement amount 0.0 EUR
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 300
    Then Admin fails to disburse the loan on "1 January 2024" with "1600" EUR transaction amount because of wrong amount
    And Admin successfully disburse the loan on "1 January 2024" with "1100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 200
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1100.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      |    |      | 05 January 2024  |                  | 200.0           |               |          | 0.0  |           | 0.0    |       |            |      | 0.0         |
      | 1  | 31   | 01 February 2024 |                  | 1119.33         | 180.67        | 6.42     | 0.0  | 0.0       | 187.09 | 0.0   | 0.0        | 0.0  | 187.09      |
      | 2  | 29   | 01 March 2024    |                  | 937.6           | 181.73        | 5.36     | 0.0  | 0.0       | 187.09 | 0.0   | 0.0        | 0.0  | 187.09      |
      | 3  | 31   | 01 April 2024    |                  | 754.81          | 182.79        | 4.3      | 0.0  | 0.0       | 187.09 | 0.0   | 0.0        | 0.0  | 187.09      |
      | 4  | 30   | 01 May 2024      |                  | 570.96          | 183.85        | 3.24     | 0.0  | 0.0       | 187.09 | 0.0   | 0.0        | 0.0  | 187.09      |
      | 5  | 31   | 01 June 2024     |                  | 386.03          | 184.93        | 2.16     | 0.0  | 0.0       | 187.09 | 0.0   | 0.0        | 0.0  | 187.09      |
      | 6  | 30   | 01 July 2024     |                  | 200.0           | 186.03        | 1.09     | 0.0  | 0.0       | 187.12 | 0.0   | 0.0        | 0.0  | 187.12      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1100.0        | 22.57    | 0.0  | 0.0       | 1122.57 | 0.0   | 0.0        | 0.0  | 1122.57     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1100.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1100.0       | false    | false    |
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2024          | 01 January 2024 | 1100.0      |                      |
      | 05 January 2024          |                 | 200.0       | 1200.0               |
    When Admin sets the business date to "5 January 2024"
    Then Admin fails to disburse the loan on "5 January 2024" with "800" EUR transaction amount because of wrong amount
    And Admin successfully disburse the loan on "5 January 2024" with "400" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1100.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      |    |      | 05 January 2024  |                  | 400.0           |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 |                  | 1253.37         | 246.63        | 8.45     | 0.0  | 0.0       | 255.08 | 0.0   | 0.0        | 0.0  | 255.08      |
      | 2  | 29   | 01 March 2024    |                  | 1005.6          | 247.77        | 7.31     | 0.0  | 0.0       | 255.08 | 0.0   | 0.0        | 0.0  | 255.08      |
      | 3  | 31   | 01 April 2024    |                  | 756.39          | 249.21        | 5.87     | 0.0  | 0.0       | 255.08 | 0.0   | 0.0        | 0.0  | 255.08      |
      | 4  | 30   | 01 May 2024      |                  | 505.72          | 250.67        | 4.41     | 0.0  | 0.0       | 255.08 | 0.0   | 0.0        | 0.0  | 255.08      |
      | 5  | 31   | 01 June 2024     |                  | 253.59          | 252.13        | 2.95     | 0.0  | 0.0       | 255.08 | 0.0   | 0.0        | 0.0  | 255.08      |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 253.59        | 1.48     | 0.0  | 0.0       | 255.07 | 0.0   | 0.0        | 0.0  | 255.07      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1500.0        | 30.47    | 0.0  | 0.0       | 1530.47 | 0.0   | 0.0        | 0.0  | 1530.47     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 1100.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1100.0       | false    | false    |
      | 05 January 2024  | Disbursement     | 400.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1500.0       | false    | false    |
    # --- close loan --- #
    When Loan Pay-off is made on "5 January 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
