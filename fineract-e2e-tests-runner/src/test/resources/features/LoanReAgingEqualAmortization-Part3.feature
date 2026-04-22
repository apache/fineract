@LoanReAgingEqualAmortizationFeature
Feature: LoanReAgingEqualAmortization - Part3

  @TestRailId:C4339 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging on 1st installment after repay and charge on 1st due with partially paid installment; adjust to last - interest bearing loan with equal amortization; outstanding full interest - UC4.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_IR_DAILY_TILL_PRECLOSE_LAST_INSTALLMENT_STRATEGY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |

    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 34.02 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 66.95           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.23           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.41           | 16.82         | 0.19     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 17.01           | 16.4          | 0.1      | 0.0  | 0.0       | 16.5  | 0.0   | 0.0        | 0.0  | 16.5        |
      | 6  | 30   | 01 July 2024     | 01 February 2024 | 0.0             | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.55     | 0.0  | 0.0       | 101.55 | 34.02 | 17.01      | 0.0  | 67.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 34.02  | 33.44     | 0.58     | 0.0  | 0.0       | 66.56        | false    | false    |

    When Admin sets the business date to "21 February 2024"
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling            |
      | 1               | MONTHS        | 01 April 2024 | 6                    | EQUAL_AMORTIZATION_FULL_INTEREST |
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 20   | 21 February 2024  | 21 February 2024 | 66.56           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 17.01 | 17.01      | 0.0  | 0.0         |
      | 3  | 40   | 01 April 2024     |                  | 55.46           | 11.1          | 0.16     | 0.0  | 0.0       | 11.26 | 0.0   | 0.0        | 0.0  | 11.26       |
      | 4  | 30   | 01 May 2024       |                  | 44.36           | 11.1          | 0.16     | 0.0  | 0.0       | 11.26 | 0.0   | 0.0        | 0.0  | 11.26       |
      | 5  | 31   | 01 June 2024      |                  | 33.26           | 11.1          | 0.16     | 0.0  | 0.0       | 11.26 | 0.0   | 0.0        | 0.0  | 11.26       |
      | 6  | 30   | 01 July 2024      |                  | 22.16           | 11.1          | 0.16     | 0.0  | 0.0       | 11.26 | 0.0   | 0.0        | 0.0  | 11.26       |
      | 7  | 31   | 01 August 2024    |                  | 11.06           | 11.1          | 0.16     | 0.0  | 0.0       | 11.26 | 0.0   | 0.0        | 0.0  | 11.26       |
      | 8  | 31   | 01 September 2024 |                  |  0.0            | 11.06         | 0.17     | 0.0  | 0.0       | 11.23 | 0.0   | 0.0        | 0.0  | 11.23       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.55     | 0.0  | 0.0       | 101.55 | 34.02 | 17.01      | 0.0  | 67.53       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 34.02  | 33.44     | 0.58     | 0.0  | 0.0       | 66.56        | false    | false    |
      | 21 February 2024 | Re-age           | 67.53  | 66.56     | 0.97     | 0.0  |  0.0      | 0.0          | false    | false    |

    When Loan Pay-off is made on "21 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4340 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging on date after maturity date after repay and chargeback on 1st due with partially paid installment; adjust to last - interest bearing loan with equal amortization; outstanding full interest - UC4.3
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_IR_DAILY_TILL_PRECLOSE_LAST_INSTALLMENT_STRATEGY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |

    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 25 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.0            | 16.57         | 0.44     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.33           | 16.67         | 0.34     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.57           | 16.76         | 0.25     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.71           | 16.86         | 0.15     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.71         | 0.05     | 0.0  | 0.0       | 16.76 | 7.99  | 7.99       | 0.0  | 8.77        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.81     | 0.0  | 0.0       | 101.81 | 25.0 | 7.99       | 0.0  | 76.81       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 25.0   | 24.42     | 0.58     | 0.0  | 0.0       | 75.58        | false    | false    |

    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.1            | 33.48         | 0.54     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.67         | 0.34     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.67           | 16.76         | 0.25     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.81           | 16.86         | 0.15     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.81         | 0.05     | 0.0  | 0.0       | 16.86 | 7.99  | 7.99       | 0.0  | 8.87        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 117.01        | 1.91     | 0.0  | 0.0       | 118.92 | 25.0 | 7.99       | 0.0  | 93.92       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 25.0   | 24.42     | 0.58     | 0.0  | 0.0       | 75.58        | false    | false    |
      | 01 February 2024 | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 92.59        | false    | false    |

    When Admin sets the business date to "01 March 2024"
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate      | numberOfInstallments | reAgeInterestHandling             |
      | 1               | MONTHS        | 01 August 2024 | 6                    | EQUAL_AMORTIZATION_FULL_INTEREST |
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 01 March 2024    | 92.59           |  7.99         | 0.0      | 0.0  | 0.0       | 7.99  | 7.99  | 7.99       | 0.0  | 0.0         |
      | 3  | 153  | 01 August 2024    |                  | 77.16           | 15.43         | 0.22     | 0.0  | 0.0       | 15.65 | 0.0   | 0.0        | 0.0  | 15.65       |
      | 4  | 31   | 01 September 2024 |                  | 61.73           | 15.43         | 0.22     | 0.0  | 0.0       | 15.65 | 0.0   | 0.0        | 0.0  | 15.65       |
      | 5  | 30   | 01 October 2024   |                  | 46.3            | 15.43         | 0.22     | 0.0  | 0.0       | 15.65 | 0.0   | 0.0        | 0.0  | 15.65       |
      | 6  | 31   | 01 November 2024  |                  | 30.87           | 15.43         | 0.22     | 0.0  | 0.0       | 15.65 | 0.0   | 0.0        | 0.0  | 15.65       |
      | 7  | 30   | 01 December 2024  |                  | 15.44           | 15.43         | 0.22     | 0.0  | 0.0       | 15.65 | 0.0   | 0.0        | 0.0  | 15.65       |
      | 8  | 31   | 01 January 2025   |                  |  0.0            | 15.44         | 0.23     | 0.0  | 0.0       | 15.67 | 0.0   | 0.0        | 0.0  | 15.67       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 117.01        | 1.91     | 0.0  | 0.0       | 118.92 | 25.0  | 7.99       | 0.0  | 93.92       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 25.0   | 24.42     | 0.58     | 0.0  | 0.0       | 75.58        | false    | false    |
      | 01 February 2024 | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 92.59        | false    | false    |
      | 01 March 2024    | Re-age           | 93.92  | 92.59     | 1.33     | 0.0  | 0.0       | 0.0          | false    | false    |

    When Loan Pay-off is made on "01 MArch 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4341 @AdvancedPaymentAllocation
  Scenario: Verify Loan backdated re-aging on 1st installment after repay and chargeback on 1st due with partially paid installment; adjust to next - interest bearing loan with equal amortization; outstanding full interest - UC4.4
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_IR_DAILY_TILL_PRECLOSE_LAST_INSTALLMENT_STRATEGY | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |

    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 25 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.0            | 16.57         | 0.44     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.33           | 16.67         | 0.34     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.57           | 16.76         | 0.25     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.71           | 16.86         | 0.15     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.71         | 0.05     | 0.0  | 0.0       | 16.76 | 7.99  | 7.99       | 0.0  | 8.77        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.81     | 0.0  | 0.0       | 101.81 | 25.0 | 7.99       | 0.0  | 76.81       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 25.0   | 24.42     | 0.58     | 0.0  | 0.0       | 75.58        | false    | false    |

    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.1            | 33.48         | 0.54     | 0.0  | 0.0       | 34.02 | 0.0   | 0.0        | 0.0  | 34.02       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.67         | 0.34     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.67           | 16.76         | 0.25     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.81           | 16.86         | 0.15     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.81         | 0.05     | 0.0  | 0.0       | 16.86 | 7.99  | 7.99       | 0.0  | 8.87        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 117.01        | 1.91     | 0.0  | 0.0       | 118.92 | 25.0 | 7.99       | 0.0  | 93.92       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 25.0   | 24.42     | 0.58     | 0.0  | 0.0       | 75.58        | false    | false    |
      | 01 February 2024 | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 92.59        | false    | false    |

    When Admin sets the business date to "01 March 2024"
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling            |
      | 1               | MONTHS        | 01 March 2024 | 6                    | EQUAL_AMORTIZATION_FULL_INTEREST |
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 March 2024    | 92.59           | 7.99          | 0.0      | 0.0  | 0.0       |  7.99 | 7.99  | 7.99       | 0.0  | 0.0         |
      | 3  |  0   | 01 March 2024    |                  | 77.16           | 15.43         | 0.22     | 0.0  | 0.0       | 15.65 | 0.0   | 0.0        | 0.0  | 15.65       |
      | 4  | 31   | 01 April 2024    |                  | 61.73           | 15.43         | 0.22     | 0.0  | 0.0       | 15.65 | 0.0   | 0.0        | 0.0  | 15.65       |
      | 5  | 30   | 01 May 2024      |                  | 46.3            | 15.43         | 0.22     | 0.0  | 0.0       | 15.65 | 0.0   | 0.0        | 0.0  | 15.65       |
      | 6  | 31   | 01 June 2024     |                  | 30.87           | 15.43         | 0.22     | 0.0  | 0.0       | 15.65 | 0.0   | 0.0        | 0.0  | 15.65       |
      | 7  | 30   | 01 July 2024     |                  | 15.44           | 15.43         | 0.22     | 0.0  | 0.0       | 15.65 | 0.0   | 0.0        | 0.0  | 15.65       |
      | 8  | 31   | 01 August 2024   |                  |  0.0            | 15.44         | 0.23     | 0.0  | 0.0       | 15.67 | 0.0   | 0.0        | 0.0  | 15.67       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 117.01        | 1.91     | 0.0  | 0.0       | 118.92 | 25.0  | 7.99       | 0.0  | 93.92       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 25.0   | 24.42     | 0.58     | 0.0  | 0.0       | 75.58        | false    | false    |
      | 01 February 2024 | Chargeback       | 17.01  | 17.01     | 0.0      | 0.0  | 0.0       | 92.59        | false    | false    |
      | 01 March 2024    | Re-age           | 93.92  | 92.59     | 1.33     | 0.0  | 0.0       | 0.0          | false    | false    |

    When Loan Pay-off is made on "1 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4524 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging transaction with charge-off with zero interest after re-age - interest bearing loan with equal amortization; outstanding payable interest - UC1.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
# --- re-aging transaction --- #
    When Admin sets the business date to "15 March 2024"
    When Admin runs inline COB job for Loan
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 01 April 2024 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.64           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 5  | 30   | 01 May 2024       |                  | 55.71           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 6  | 31   | 01 June 2024      |                  | 41.78           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 7  | 30   | 01 July 2024      |                  | 27.85           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 8  | 31   | 01 August 2024    |                  | 13.92           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.92         | 0.11     | 0.0  | 0.0       | 14.03 | 0.0   | 0.0        | 0.0  | 14.03       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.29     | 0.0  | 0.0       | 101.29 | 17.01 | 0.0        | 0.0  | 84.28       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- charge-off with zero interest behaviour --- #
    When Admin sets the business date to "01 April 2024"
    And Admin does charge-off the loan on "01 April 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 April 2024"
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.64           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 5  | 30   | 01 May 2024       |                  | 55.71           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 6  | 31   | 01 June 2024      |                  | 41.78           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 7  | 30   | 01 July 2024      |                  | 27.85           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 8  | 31   | 01 August 2024    |                  | 13.92           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.92         | 0.11     | 0.0  | 0.0       | 14.03 | 0.0   | 0.0        | 0.0  | 14.03       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.29     | 0.0  | 0.0       | 101.29 | 17.01 | 0.0        | 0.0  | 84.28       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual            | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age             | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Adjustment | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off         | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4537 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging transaction with charge-off with zero interest after re-age - interest bearing loan with equal amortization; outstanding full interest - UC1.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
# --- re-aging transaction --- #
    When Admin sets the business date to "15 March 2024"
    When Admin runs inline COB job for Loan
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling            |
      | 1               | MONTHS        | 01 April 2024 | 6                    | EQUAL_AMORTIZATION_FULL_INTEREST |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.64           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 5  | 30   | 01 May 2024       |                  | 55.71           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 6  | 31   | 01 June 2024      |                  | 41.78           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 7  | 30   | 01 July 2024      |                  | 27.85           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 8  | 31   | 01 August 2024    |                  | 13.92           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.92         | 0.26     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.09     | 0.0  | 0.0       | 102.09 | 17.01 | 0.0        | 0.0  | 85.08       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 85.08  | 83.57     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- charge-off with zero interest behaviour --- #
    When Admin sets the business date to "01 April 2024"
    And Admin does charge-off the loan on "01 April 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 April 2024"
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.64           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 5  | 30   | 01 May 2024       |                  | 55.71           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 6  | 31   | 01 June 2024      |                  | 41.78           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 7  | 30   | 01 July 2024      |                  | 27.85           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 8  | 31   | 01 August 2024    |                  | 13.92           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.92         | 0.26     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.09     | 0.0  | 0.0       | 102.09 | 17.01 | 0.0        | 0.0  | 85.08       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual            | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age             | 85.08  | 83.57     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Adjustment | 0.19   | 0.0       | 0.19     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off         | 85.08  | 83.57     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4538 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging transaction with charge-off with accelerate maturity after re-age - interest bearing loan with equal amortization; outstanding payable interest - UC2.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                       | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
# --- re-aging transaction --- #
    When Admin sets the business date to "15 March 2024"
    When Admin runs inline COB job for Loan
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 01 April 2024 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.64           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 5  | 30   | 01 May 2024       |                  | 55.71           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 6  | 31   | 01 June 2024      |                  | 41.78           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 7  | 30   | 01 July 2024      |                  | 27.85           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 8  | 31   | 01 August 2024    |                  | 13.92           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.92         | 0.11     | 0.0  | 0.0       | 14.03 | 0.0   | 0.0        | 0.0  | 14.03       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.29     | 0.0  | 0.0       | 101.29 | 17.01 | 0.0        | 0.0  | 84.28       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- charge-off with accelerate maturity behaviour --- #
    When Admin sets the business date to "01 April 2024"
    And Admin does charge-off the loan on "01 April 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 April 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024    |                  | 0.0             | 83.57         | 0.71     | 0.0  | 0.0       | 84.28 | 0.0   | 0.0        | 0.0  | 84.28       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.29     | 0.0  | 0.0       | 101.29 | 17.01 | 0.0        | 0.0  | 84.28       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual          | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off       | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4525 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging transaction with charge-off with accelerate maturity after re-age - interest bearing loan with equal amortization; outstanding full interest - UC2.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR | 01 January 2024   | 100            | 7                       | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
# --- re-aging transaction --- #
    When Admin sets the business date to "15 March 2024"
    When Admin runs inline COB job for Loan
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling            |
      | 1               | MONTHS        | 01 April 2024 | 6                    | EQUAL_AMORTIZATION_FULL_INTEREST |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.64           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 5  | 30   | 01 May 2024       |                  | 55.71           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 6  | 31   | 01 June 2024      |                  | 41.78           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 7  | 30   | 01 July 2024      |                  | 27.85           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 8  | 31   | 01 August 2024    |                  | 13.92           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.92         | 0.26     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.09     | 0.0  | 0.0       | 102.09 | 17.01 | 0.0        | 0.0  | 85.08       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 85.08  | 83.57     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- charge-off with accelerate maturity behaviour --- #
    When Admin sets the business date to "01 April 2024"
    And Admin does charge-off the loan on "01 April 2024"
    Then LoanBalanceChangedBusinessEvent is created on "01 April 2024"
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024    |                  | 0.0             | 83.57         | 1.51     | 0.0  | 0.0       | 85.08 | 0.0   | 0.0        | 0.0  | 85.08       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.09     | 0.0  | 0.0       | 102.09 | 17.01 | 0.0        | 0.0  | 85.08       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 85.08  | 83.57     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual          | 0.82   | 0.0       | 0.82     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off       | 85.08  | 83.57     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4526 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging trn with charge-off after re-aging - interest bearing loan with equal amortization; outstanding payable interest - UC3.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
# --- re-aging transaction with default behaviour --- #
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
# --- re-aging transaction --- #
    When Admin sets the business date to "15 March 2024"
    When Admin runs inline COB job for Loan
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 01 April 2024 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.64           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 5  | 30   | 01 May 2024       |                  | 55.71           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 6  | 31   | 01 June 2024      |                  | 41.78           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 7  | 30   | 01 July 2024      |                  | 27.85           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 8  | 31   | 01 August 2024    |                  | 13.92           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.92         | 0.11     | 0.0  | 0.0       | 14.03 | 0.0   | 0.0        | 0.0  | 14.03       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.29     | 0.0  | 0.0       | 101.29 | 17.01 | 0.0        | 0.0  | 84.28       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- charge-off regular --- #
    When Admin sets the business date to "01 April 2024"
    And Admin does charge-off the loan on "01 April 2024"
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.64           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 5  | 30   | 01 May 2024       |                  | 55.71           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 6  | 31   | 01 June 2024      |                  | 41.78           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 7  | 30   | 01 July 2024      |                  | 27.85           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 8  | 31   | 01 August 2024    |                  | 13.92           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.92         | 0.11     | 0.0  | 0.0       | 14.03 | 0.0   | 0.0        | 0.0  | 14.03       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.29     | 0.0  | 0.0       | 101.29 | 17.01 | 0.0        | 0.0  | 84.28       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual            | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age             | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual Adjustment | 0.45   | 0.0       | 0.45     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Charge-off         | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4539 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging trn with charge-off after re-aging - interest bearing loan with equal amortization; outstanding full interest - UC3.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
# --- repayment transaction --- #
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
# --- re-aging transaction --- #
    When Admin sets the business date to "15 March 2024"
    When Admin runs inline COB job for Loan
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling            |
      | 1               | MONTHS        | 01 April 2024 | 6                    | EQUAL_AMORTIZATION_FULL_INTEREST |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.64           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 5  | 30   | 01 May 2024       |                  | 55.71           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 6  | 31   | 01 June 2024      |                  | 41.78           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 7  | 30   | 01 July 2024      |                  | 27.85           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 8  | 31   | 01 August 2024    |                  | 13.92           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.92         | 0.26     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.09     | 0.0  | 0.0       | 102.09 | 17.01 | 0.0        | 0.0  | 85.08       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 85.08  | 83.57     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
# --- charge-off regular --- #
    And Admin does charge-off the loan on "15 March 2024"
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.64           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 5  | 30   | 01 May 2024       |                  | 55.71           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 6  | 31   | 01 June 2024      |                  | 41.78           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 7  | 30   | 01 July 2024      |                  | 27.85           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 8  | 31   | 01 August 2024    |                  | 13.92           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.92         | 0.26     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.09     | 0.0  | 0.0       | 102.09 | 17.01 | 0.0        | 0.0  | 85.08       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type   | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement       | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment          | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual            | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age             | 85.08  | 83.57     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Accrual Adjustment | 0.44   | 0.0       | 0.44     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Charge-off         | 85.08  | 83.57     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4527 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging transaction with Contract Termination after re-age - interest bearing loan with equal amortization; outstanding payable interest - UC4.1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_CONTRACT_TERMINATION | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
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
# --- re-aging transaction --- #
    When Admin sets the business date to "15 March 2024"
    When Admin runs inline COB job for Loan
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 01 April 2024 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.64           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 5  | 30   | 01 May 2024       |                  | 55.71           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 6  | 31   | 01 June 2024      |                  | 41.78           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 7  | 30   | 01 July 2024      |                  | 27.85           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 8  | 31   | 01 August 2024    |                  | 13.92           | 13.93         | 0.12     | 0.0  | 0.0       | 14.05 | 0.0   | 0.0        | 0.0  | 14.05       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.92         | 0.11     | 0.0  | 0.0       | 14.03 | 0.0   | 0.0        | 0.0  | 14.03       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.29     | 0.0  | 0.0       | 101.29 | 17.01 | 0.0        | 0.0  | 84.28       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual           | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "01 April 2024"
    And Admin successfully terminates loan contract
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024    |                  | 0.0             | 83.57         | 0.71     | 0.0  | 0.0       | 84.28 | 0.0   | 0.0        | 0.0  | 84.28       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.29     | 0.0  | 0.0       | 101.29 | 17.01 | 0.0        | 0.0  | 84.28       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type     | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement         | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment            | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual              | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age               | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual              | 0.02   | 0.0       | 0.02     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Contract Termination | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4540 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging transaction with Contract Termination after re-age - interest bearing loan with equal amortization; outstanding full interest - UC4.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_CONTRACT_TERMINATION | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
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
# --- re-aging transaction --- #
    When Admin sets the business date to "15 March 2024"
    When Admin runs inline COB job for Loan
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling            |
      | 1               | MONTHS        | 01 April 2024 | 6                    | EQUAL_AMORTIZATION_FULL_INTEREST |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.64           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 5  | 30   | 01 May 2024       |                  | 55.71           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 6  | 31   | 01 June 2024      |                  | 41.78           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 7  | 30   | 01 July 2024      |                  | 27.85           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 8  | 31   | 01 August 2024    |                  | 13.92           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.92         | 0.26     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.09     | 0.0  | 0.0       | 102.09 | 17.01 | 0.0        | 0.0  | 85.08       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual          | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age           | 85.08  | 83.57     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "01 April 2024"
    And Admin successfully terminates loan contract
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024    |                  | 0.0             | 83.57         | 1.51     | 0.0  | 0.0       | 85.08 | 0.0   | 0.0        | 0.0  | 85.08       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.09     | 0.0  | 0.0       | 102.09 | 17.01 | 0.0        | 0.0  | 85.08       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type     | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement         | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment            | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 14 March 2024    | Accrual              | 1.27   | 0.0       | 1.27     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 15 March 2024    | Re-age               | 85.08  | 83.57     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Accrual              | 0.82   | 0.0       | 0.82     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Contract Termination | 85.08  | 83.57     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "01 April 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4528 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging transaction with Write-Off after re-age - interest bearing loan with equal amortization; outstanding full interest - UC5
    When Admin sets the business date to "01 January 2024"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    And Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
# --- 1st installment paid --- #
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
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
# --- re-age transaction --- #
    When Admin sets the business date to "15 March 2024"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling            |
      | 1               | MONTHS        | 01 April 2024 | 6                    | EQUAL_AMORTIZATION_FULL_INTEREST |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024  | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 14   | 15 March 2024     | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 17   | 01 April 2024     |                  | 69.64           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 5  | 30   | 01 May 2024       |                  | 55.71           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 6  | 31   | 01 June 2024      |                  | 41.78           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 7  | 30   | 01 July 2024      |                  | 27.85           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 8  | 31   | 01 August 2024    |                  | 13.92           | 13.93         | 0.25     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 13.92         | 0.26     | 0.0  | 0.0       | 14.18 | 0.0   | 0.0        | 0.0  | 14.18       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.09     | 0.0  | 0.0       | 102.09 | 17.01 | 0.0        | 0.0  | 85.08       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age           | 85.08  | 83.57     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "01 April 2024"
    And Admin does write-off the loan on "01 April 2024"
    Then Loan status will be "CLOSED_WRITTEN_OFF"
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type       | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement           | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment              | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 15 March 2024    | Re-age                 | 85.08  | 83.57     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 April 2024    | Close (as written-off) | 85.08  | 83.57     | 1.51     | 0.0  | 0.0       | 0.0          | false    | false    |
    Then Loan has 0 outstanding amount
    Then Loan's all installments have obligations met


  @TestRailId:C70226 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging transaction with after maturity date with charge n+1 - interest bearing loan with equal amortization; outstanding full interest - UC1
    When Admin sets the business date to "10 March 2026"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_AUTO_DOWNPAYMENT | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 2                 | MONTHS                | 1              | MONTHS                 | 2                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2026 |                 | 375.0           | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0  | 375.0       |
      | 3  | 28   | 01 March 2026    |                 | 0.0             | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0  | 375.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
# --- repayment at current date --- #
    And Customer makes "AUTOPAY" repayment on "10 March 2026" with 100 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 01 February 2026 |                 | 375.0           | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 100.0 | 0.0        | 100.0 | 275.0       |
      | 3  | 28   | 01 March 2026    |                 | 0.0             | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0   | 375.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 350.0 | 0.0        | 100.0 | 650.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 10 March 2026    | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 650.0        | false    | false    |
    And Admin adds "LOAN_NSF_FEE" due date charge with "10 March 2026" due date and 12 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 01 February 2026 |                 | 375.0           | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 100.0 | 0.0        | 100.0 | 275.0       |
      | 3  | 28   | 01 March 2026    |                 | 0.0             | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0   | 375.0       |
      | 4  | 9    | 10 March 2026    |                 | 0.0             | 0.0           | 0.0      | 0.0  | 12.0      | 12.0  | 0.0   | 0.0        | 0.0   | 12.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 12.0      | 1012.0 | 350.0 | 0.0        | 100.0 | 662.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 10 March 2026    | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 650.0        | false    | false    |
    Then Loan Charges tab has the following data:
      | Name    | isPenalty | Payment due at     | Due as of     | Calculation type | Due  | Paid | Waived | Outstanding |
      | NSF fee | true      | Specified due date | 10 March 2026 | Flat             | 12.0 | 0.0  | 0.0    | 12.0        |
# --- re-age transaction --- #
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling            |
      | 1               | MONTHS        | 10 April 2026 | 6                    | EQUAL_AMORTIZATION_FULL_INTEREST |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2026   |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 01 February 2026  | 10 March 2026   | 650.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0  | 100.0 | 0.0        | 100.0 | 0.0         |
      | 3  | 28   | 01 March 2026     | 10 March 2026   | 650.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   | 0.0        | 0.0   | 0.0         |
      | 4  | 40   | 10 April 2026     |                 | 541.67          | 108.33        | 0.0      | 0.0  | 2.0       | 110.33 | 0.0   | 0.0        | 0.0   | 110.33      |
      | 5  | 30   | 10 May 2026       |                 | 433.34          | 108.33        | 0.0      | 0.0  | 2.0       | 110.33 | 0.0   | 0.0        | 0.0   | 110.33      |
      | 6  | 31   | 10 June 2026      |                 | 325.01          | 108.33        | 0.0      | 0.0  | 2.0       | 110.33 | 0.0   | 0.0        | 0.0   | 110.33      |
      | 7  | 30   | 10 July 2026      |                 | 216.68          | 108.33        | 0.0      | 0.0  | 2.0       | 110.33 | 0.0   | 0.0        | 0.0   | 110.33      |
      | 8  | 31   | 10 August 2026    |                 | 108.35          | 108.33        | 0.0      | 0.0  | 2.0       | 110.33 | 0.0   | 0.0        | 0.0   | 110.33      |
      | 9  | 31   | 10 September 2026 |                 | 0.0             | 108.35        | 0.0      | 0.0  | 2.0       | 110.35 | 0.0   | 0.0        | 0.0   | 110.35      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 12.0      | 1012.0 | 350.0 | 0.0        | 100.0 | 662.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 10 March 2026    | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 650.0        | false    | false    |
      | 10 March 2026    | Re-age           | 662.0  | 650.0     | 0.0      | 0.0  | 12.0      | 0.0          | false    | false    |
    When Loan Pay-off is made on "10 March 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C70227 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging transaction with after maturity date with charge n+1 - interest bearing loan with equal amortization; outstanding payable interest - UC2
    When Admin sets the business date to "10 March 2026"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_AUTO_DOWNPAYMENT | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 2                 | MONTHS                | 1              | MONTHS                 | 2                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2026 |                 | 375.0           | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0  | 375.0       |
      | 3  | 28   | 01 March 2026    |                 | 0.0             | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0  | 375.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
# --- repayment at current date --- #
    And Customer makes "AUTOPAY" repayment on "10 March 2026" with 100 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 01 February 2026 |                 | 375.0           | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 100.0 | 0.0        | 100.0 | 275.0       |
      | 3  | 28   | 01 March 2026    |                 | 0.0             | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0   | 375.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 350.0 | 0.0        | 100.0 | 650.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 10 March 2026    | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 650.0        | false    | false    |
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "10 March 2026" due date and 12 EUR transaction amount
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 10 March 2026 | Flat             | 12.0 | 0.0  | 0.0    | 12.0        |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 01 February 2026 |                 | 375.0           | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 100.0 | 0.0        | 100.0 | 275.0       |
      | 3  | 28   | 01 March 2026    |                 | 0.0             | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0   | 375.0       |
      | 4  | 9    | 10 March 2026    |                 | 0.0             | 0.0           | 0.0      | 12.0 | 0.0       | 12.0  | 0.0   | 0.0        | 0.0   | 12.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 12.0 | 0.0       | 1012.0 | 350.0 | 0.0        | 100.0 | 662.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 10 March 2026    | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 650.0        | false    | false    |
# --- re-age transaction --- #
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 10 April 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2026   |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 01 February 2026  | 10 March 2026   | 650.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0  | 100.0 | 0.0        | 100.0 | 0.0         |
      | 3  | 28   | 01 March 2026     | 10 March 2026   | 650.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   | 0.0        | 0.0   | 0.0         |
      | 4  | 40   | 10 April 2026     |                 | 541.67          | 108.33        | 0.0      | 2.0  | 0.0       | 110.33 | 0.0   | 0.0        | 0.0   | 110.33      |
      | 5  | 30   | 10 May 2026       |                 | 433.34          | 108.33        | 0.0      | 2.0  | 0.0       | 110.33 | 0.0   | 0.0        | 0.0   | 110.33      |
      | 6  | 31   | 10 June 2026      |                 | 325.01          | 108.33        | 0.0      | 2.0  | 0.0       | 110.33 | 0.0   | 0.0        | 0.0   | 110.33      |
      | 7  | 30   | 10 July 2026      |                 | 216.68          | 108.33        | 0.0      | 2.0  | 0.0       | 110.33 | 0.0   | 0.0        | 0.0   | 110.33      |
      | 8  | 31   | 10 August 2026    |                 | 108.35          | 108.33        | 0.0      | 2.0  | 0.0       | 110.33 | 0.0   | 0.0        | 0.0   | 110.33      |
      | 9  | 31   | 10 September 2026 |                 | 0.0             | 108.35        | 0.0      | 2.0  | 0.0       | 110.35 | 0.0   | 0.0        | 0.0   | 110.35      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 12.0 | 0.0       | 1012.0 | 350.0 | 0.0        | 100.0 | 662.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 10 March 2026    | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 650.0        | false    | false    |
      | 10 March 2026    | Re-age           | 662.0  | 650.0     | 0.0      | 12.0 | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "10 March 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C70228 @AdvancedPaymentAllocation
  Scenario: Verify Loan re-aging transaction with after maturity date with chargeback n+1 - interest bearing loan with equal amortization; outstanding payable interest - UC3
    When Admin sets the business date to "10 March 2026"
    And Admin creates a client with random data
    And Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_AUTO_DOWNPAYMENT | 01 January 2026   | 1000           | 0                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 2                 | MONTHS                | 1              | MONTHS                 | 2                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2026 |                 | 375.0           | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0  | 375.0       |
      | 3  | 28   | 01 March 2026    |                 | 0.0             | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0  | 375.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 250.0 | 0.0        | 0.0  | 750.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
# --- repayment at current date --- #
    And Customer makes "AUTOPAY" repayment on "10 March 2026" with 100 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 01 February 2026 |                 | 375.0           | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 100.0 | 0.0        | 100.0 | 275.0       |
      | 3  | 28   | 01 March 2026    |                 | 0.0             | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0   | 375.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 350.0 | 0.0        | 100.0 | 650.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 10 March 2026    | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 650.0        | false    | false    |
   # And Admin adds "LOAN_NSF_FEE" due date charge with "10 March 2026" due date and 12 EUR transaction amount
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 12 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2026  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2026  | 01 January 2026 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 01 February 2026 |                 | 375.0           | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 100.0 | 0.0        | 100.0 | 275.0       |
      | 3  | 28   | 01 March 2026    |                 | 0.0             | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0   | 375.0       |
      | 4  | 9    | 10 March 2026    |                 | 0.0             | 12.0          | 0.0      | 0.0  | 0.0       | 12.0  | 0.0   | 0.0        | 0.0   | 12.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1012.0        | 0.0      | 0.0  | 0.0       | 1012.0 | 350.0 | 0.0        | 100.0 | 662.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 10 March 2026    | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 650.0        | false    | false    |
      | 10 March 2026    | Chargeback       | 12.0   | 12.0      | 0.0      | 0.0  | 0.0       | 662.0        | false    | false    |
# --- re-age transaction --- #
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 10 April 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan Repayment schedule has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2026   |                 | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2026   | 01 January 2026 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 01 February 2026  | 10 March 2026   | 650.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0  | 100.0 | 0.0        | 100.0 | 0.0         |
      | 3  | 28   | 01 March 2026     | 10 March 2026   | 650.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   | 0.0        | 0.0   | 0.0         |
      | 4  | 40   | 10 April 2026     |                 | 551.67          | 110.33        | 0.0      | 0.0  | 0.0       | 110.33 | 0.0   | 0.0        | 0.0   | 110.33      |
      | 5  | 30   | 10 May 2026       |                 | 441.34          | 110.33        | 0.0      | 0.0  | 0.0       | 110.33 | 0.0   | 0.0        | 0.0   | 110.33      |
      | 6  | 31   | 10 June 2026      |                 | 331.01          | 110.33        | 0.0      | 0.0  | 0.0       | 110.33 | 0.0   | 0.0        | 0.0   | 110.33      |
      | 7  | 30   | 10 July 2026      |                 | 220.68          | 110.33        | 0.0      | 0.0  | 0.0       | 110.33 | 0.0   | 0.0        | 0.0   | 110.33      |
      | 8  | 31   | 10 August 2026    |                 | 110.35          | 110.33        | 0.0      | 0.0  | 0.0       | 110.33 | 0.0   | 0.0        | 0.0   | 110.33      |
      | 9  | 31   | 10 September 2026 |                 | 0.0             | 110.35        | 0.0      | 0.0  | 0.0       | 110.35 | 0.0   | 0.0        | 0.0   | 110.35      |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1012.0        | 0.0      | 0.0  | 0.0       | 1012.0 | 350.0 | 0.0        | 100.0 | 662.0       |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2026  | Disbursement     | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |
      | 01 January 2026  | Down Payment     | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    | false    |
      | 10 March 2026    | Repayment        | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 650.0        | false    | false    |
      | 10 March 2026    | Chargeback       | 12.0   | 12.0      | 0.0      | 0.0  | 0.0       | 662.0        | false    | false    |
      | 10 March 2026    | Re-age           | 662.0  | 662.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    | false    |
    When Loan Pay-off is made on "10 March 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C70229 @AdvancedPaymentAllocation
  Scenario: Verify that Loan re-aging trn with backdated repayment and chargeback - N+1 installment after maturity date overlaps with re-aging with equal amortization; outstanding full interest - UC4
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                 | 1             | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2025  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2025 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2025    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2025    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0   | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
# --- add charge a month later --- #
    When Admin sets the business date to "3 May 2025"
    And Customer makes "AUTOPAY" repayment on "01 March 2025" with 250 EUR transaction amount
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |               | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2025  | 01 March 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0| 0.0        | 250.0|   0.0       |
      | 2  | 31   | 01 February 2025 |               | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2025    |               | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2025    |               | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 5  | 32   | 03 May 2025      |               | 0.0             | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 0.0  | 0.0        | 0.0  | 125.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1125.0        | 0.0      | 0.0  | 0.0       | 1125.0 | 250.0 | 0.0        | 250.0 | 875.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 March 2025    | Repayment         | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        | false    |
      | 03 May 2025      | Chargeback        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       | 875.0        | false    |
# --- add re-aging trn with start date as maturity date --- #
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments | reAgeInterestHandling            |
      | 1               | WEEKS         | 01 April 2025 | 6                    | EQUAL_AMORTIZATION_FULL_INTEREST |
    Then Loan Repayment schedule has 10 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |               | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  | 01 March 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 31   | 01 February 2025 | 01 April 2025 | 750.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0   | 0.0         |
      | 3  | 28   | 01 March 2025    | 01 April 2025 | 750.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0   | 0.0         |
      | 4  | 31   | 01 April 2025    | 01 April 2025 | 750.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0   | 0.0         |
      | 5  | 0    | 01 April 2025    |               | 625.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 0.0   | 0.0        | 0.0   | 125.0       |
      | 6  | 7    | 08 April 2025    |               | 500.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 0.0   | 0.0        | 0.0   | 125.0       |
      | 7  | 7    | 15 April 2025    |               | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 0.0   | 0.0        | 0.0   | 125.0       |
      | 8  | 7    | 22 April 2025    |               | 250.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 0.0   | 0.0        | 0.0   | 125.0       |
      | 9  | 7    | 29 April 2025    |               | 125.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 0.0   | 0.0        | 0.0   | 125.0       |
      | 10 | 7    | 06 May 2025      |               | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1125.0        | 0.0      | 0.0  | 0.0       | 1125.0 | 250.0 | 0.0        | 250.0| 875.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2025  | Disbursement      | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    |
      | 01 March 2025    | Repayment         | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       |  750.0       | false    |
      | 03 May 2025      | Chargeback        | 125.0  | 125.0     | 0.0      | 0.0  | 0.0       |  875.0       | false    |
      | 01 April 2025    | Re-age            | 750.0  | 750.0     | 0.0      | 0.0  | 0.0       |    0.0       | false    |
    When Loan Pay-off is made on "03 May 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C72348 @AdvancedPaymentAllocation
  Scenario: Verify that post-maturity Re-aging with EQUAL_AMORTIZATION_PAYABLE_INTEREST succeeds on interest bearing progressive loan with no prior payments
    When Admin sets the business date to "07 September 2025"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date  | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 07 September 2025  | 900            | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "07 September 2025" with "900" amount and expected disbursement date on "07 September 2025"
    And Admin successfully disburse the loan on "07 September 2025" with "900" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date               | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 07 September 2025  |           | 900.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 07 October 2025    |           | 753.09          | 146.91        | 7.49     | 0.0  | 0.0       | 154.4  | 0.0  | 0.0        | 0.0  | 154.4       |
      | 2  | 31   | 07 November 2025   |           | 604.96          | 148.13        | 6.27     | 0.0  | 0.0       | 154.4  | 0.0  | 0.0        | 0.0  | 154.4       |
      | 3  | 30   | 07 December 2025   |           | 455.6           | 149.36        | 5.04     | 0.0  | 0.0       | 154.4  | 0.0  | 0.0        | 0.0  | 154.4       |
      | 4  | 31   | 07 January 2026    |           | 304.99          | 150.61        | 3.79     | 0.0  | 0.0       | 154.4  | 0.0  | 0.0        | 0.0  | 154.4       |
      | 5  | 31   | 07 February 2026   |           | 153.13          | 151.86        | 2.54     | 0.0  | 0.0       | 154.4  | 0.0  | 0.0        | 0.0  | 154.4       |
      | 6  | 28   | 07 March 2026      |           | 0.0             | 153.13        | 1.27     | 0.0  | 0.0       | 154.4  | 0.0  | 0.0        | 0.0  | 154.4       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 900.0         | 26.4     | 0.0  | 0.0       | 926.4 | 0.0  | 0.0        | 0.0  | 926.4       |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 07 September 2025 | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
# --- No payments made. Loan matures on 07 March 2026. Move date past maturity. --- #
    When Admin sets the business date to "17 April 2026"
    When Admin runs inline COB job for Loan
# --- Re-age post maturity with EQUAL_AMORTIZATION_PAYABLE_INTEREST --- #
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate      | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 17 May 2026    | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date               | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 07 September 2025  |                 | 900.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 07 October 2025    | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 07 November 2025   | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 3  | 30   | 07 December 2025   | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 4  | 31   | 07 January 2026    | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 07 February 2026   | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 6  | 28   | 07 March 2026      | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 7  | 71   | 17 May 2026        |                 | 750.0           | 150.0         | 7.49     | 0.0  | 0.0       | 157.49 | 0.0  | 0.0        | 0.0  | 157.49      |
      | 8  | 31   | 17 June 2026       |                 | 600.0           | 150.0         | 7.49     | 0.0  | 0.0       | 157.49 | 0.0  | 0.0        | 0.0  | 157.49      |
      | 9  | 30   | 17 July 2026       |                 | 450.0           | 150.0         | 7.49     | 0.0  | 0.0       | 157.49 | 0.0  | 0.0        | 0.0  | 157.49      |
      | 10 | 31   | 17 August 2026     |                 | 300.0           | 150.0         | 7.49     | 0.0  | 0.0       | 157.49 | 0.0  | 0.0        | 0.0  | 157.49      |
      | 11 | 31   | 17 September 2026  |                 | 150.0           | 150.0         | 7.49     | 0.0  | 0.0       | 157.49 | 0.0  | 0.0        | 0.0  | 157.49      |
      | 12 | 30   | 17 October 2026    |                 | 0.0             | 150.0         | 7.49     | 0.0  | 0.0       | 157.49 | 0.0  | 0.0        | 0.0  | 157.49      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 900.0         | 44.94    | 0.0  | 0.0       | 944.94 | 0.0  | 0.0        | 0.0  | 944.94      |
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 07 September 2025 | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 17 April 2026     | Re-age           | 944.94 | 900.0     | 44.94    | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C72369 @AdvancedPaymentAllocation
  Scenario: Verify that post-maturity Re-aging with EQUAL_AMORTIZATION_PAYABLE_INTEREST succeeds and loan can be paid off on interest bearing progressive loan
    When Admin sets the business date to "07 September 2025"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date  | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 07 September 2025  | 100            | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "07 September 2025" with "100" amount and expected disbursement date on "07 September 2025"
    And Admin successfully disburse the loan on "07 September 2025" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date               | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 07 September 2025  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 07 October 2025    |           | 83.67           | 16.33         | 0.83     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 2  | 31   | 07 November 2025   |           | 67.21           | 16.46         | 0.7      | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 3  | 30   | 07 December 2025   |           | 50.61           | 16.6          | 0.56     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 4  | 31   | 07 January 2026    |           | 33.87           | 16.74         | 0.42     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 5  | 31   | 07 February 2026   |           | 16.99           | 16.88         | 0.28     | 0.0  | 0.0       | 17.16 | 0.0  | 0.0        | 0.0  | 17.16       |
      | 6  | 28   | 07 March 2026      |           | 0.0             | 16.99         | 0.14     | 0.0  | 0.0       | 17.13 | 0.0  | 0.0        | 0.0  | 17.13       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.93     | 0.0  | 0.0       | 102.93 | 0.0  | 0.0        | 0.0  | 102.93      |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 07 September 2025 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
# --- No payments made. Re-age 3 days after maturity (07 March 2026). --- #
    When Admin sets the business date to "10 March 2026"
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate      | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 17 April 2026  | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date               | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 07 September 2025  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 07 October 2025    | 10 March 2026   | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 07 November 2025   | 10 March 2026   | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 3  | 30   | 07 December 2025   | 10 March 2026   | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 4  | 31   | 07 January 2026    | 10 March 2026   | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 07 February 2026   | 10 March 2026   | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 6  | 28   | 07 March 2026      | 10 March 2026   | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 7  | 41   | 17 April 2026      |                 | 83.33           | 16.67         | 0.83     | 0.0  | 0.0       | 17.5  | 0.0  | 0.0        | 0.0  | 17.5        |
      | 8  | 30   | 17 May 2026        |                 | 66.66           | 16.67         | 0.83     | 0.0  | 0.0       | 17.5  | 0.0  | 0.0        | 0.0  | 17.5        |
      | 9  | 31   | 17 June 2026       |                 | 49.99           | 16.67         | 0.83     | 0.0  | 0.0       | 17.5  | 0.0  | 0.0        | 0.0  | 17.5        |
      | 10 | 30   | 17 July 2026       |                 | 33.32           | 16.67         | 0.83     | 0.0  | 0.0       | 17.5  | 0.0  | 0.0        | 0.0  | 17.5        |
      | 11 | 31   | 17 August 2026     |                 | 16.65           | 16.67         | 0.83     | 0.0  | 0.0       | 17.5  | 0.0  | 0.0        | 0.0  | 17.5        |
      | 12 | 31   | 17 September 2026  |                 | 0.0             | 16.65         | 0.83     | 0.0  | 0.0       | 17.48 | 0.0  | 0.0        | 0.0  | 17.48       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 4.98     | 0.0  | 0.0       | 104.98 | 0.0  | 0.0        | 0.0  | 104.98      |
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 07 September 2025 | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 10 March 2026     | Re-age           | 104.98 | 100.0     | 4.98     | 0.0  | 0.0       | 0.0          | false    |
# --- Pay-off and verify loan closes cleanly --- #
    When Admin sets the business date to "01 April 2026"
    When Loan Pay-off is made on "01 April 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C72370 @AdvancedPaymentAllocation
  Scenario: Verify that post-maturity Re-aging with EQUAL_AMORTIZATION_FULL_INTEREST succeeds on interest bearing progressive loan with no prior payments
    When Admin sets the business date to "07 September 2025"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date  | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 07 September 2025  | 900            | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "07 September 2025" with "900" amount and expected disbursement date on "07 September 2025"
    And Admin successfully disburse the loan on "07 September 2025" with "900" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date               | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 07 September 2025  |           | 900.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 07 October 2025    |           | 753.09          | 146.91        | 7.49     | 0.0  | 0.0       | 154.4  | 0.0  | 0.0        | 0.0  | 154.4       |
      | 2  | 31   | 07 November 2025   |           | 604.96          | 148.13        | 6.27     | 0.0  | 0.0       | 154.4  | 0.0  | 0.0        | 0.0  | 154.4       |
      | 3  | 30   | 07 December 2025   |           | 455.6           | 149.36        | 5.04     | 0.0  | 0.0       | 154.4  | 0.0  | 0.0        | 0.0  | 154.4       |
      | 4  | 31   | 07 January 2026    |           | 304.99          | 150.61        | 3.79     | 0.0  | 0.0       | 154.4  | 0.0  | 0.0        | 0.0  | 154.4       |
      | 5  | 31   | 07 February 2026   |           | 153.13          | 151.86        | 2.54     | 0.0  | 0.0       | 154.4  | 0.0  | 0.0        | 0.0  | 154.4       |
      | 6  | 28   | 07 March 2026      |           | 0.0             | 153.13        | 1.27     | 0.0  | 0.0       | 154.4  | 0.0  | 0.0        | 0.0  | 154.4       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 900.0         | 26.4     | 0.0  | 0.0       | 926.4 | 0.0  | 0.0        | 0.0  | 926.4       |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 07 September 2025 | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
# --- No payments made. Loan matures on 07 March 2026. Move date past maturity. --- #
    When Admin sets the business date to "17 April 2026"
    When Admin runs inline COB job for Loan
# --- Re-age post maturity with EQUAL_AMORTIZATION_FULL_INTEREST --- #
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate      | numberOfInstallments | reAgeInterestHandling            |
      | 1               | MONTHS        | 17 May 2026    | 6                    | EQUAL_AMORTIZATION_FULL_INTEREST |
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date               | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 07 September 2025  |                 | 900.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 07 October 2025    | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 07 November 2025   | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 3  | 30   | 07 December 2025   | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 4  | 31   | 07 January 2026    | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 07 February 2026   | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 6  | 28   | 07 March 2026      | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 7  | 71   | 17 May 2026        |                 | 750.0           | 150.0         | 7.49     | 0.0  | 0.0       | 157.49 | 0.0  | 0.0        | 0.0  | 157.49      |
      | 8  | 31   | 17 June 2026       |                 | 600.0           | 150.0         | 7.49     | 0.0  | 0.0       | 157.49 | 0.0  | 0.0        | 0.0  | 157.49      |
      | 9  | 30   | 17 July 2026       |                 | 450.0           | 150.0         | 7.49     | 0.0  | 0.0       | 157.49 | 0.0  | 0.0        | 0.0  | 157.49      |
      | 10 | 31   | 17 August 2026     |                 | 300.0           | 150.0         | 7.49     | 0.0  | 0.0       | 157.49 | 0.0  | 0.0        | 0.0  | 157.49      |
      | 11 | 31   | 17 September 2026  |                 | 150.0           | 150.0         | 7.49     | 0.0  | 0.0       | 157.49 | 0.0  | 0.0        | 0.0  | 157.49      |
      | 12 | 30   | 17 October 2026    |                 | 0.0             | 150.0         | 7.49     | 0.0  | 0.0       | 157.49 | 0.0  | 0.0        | 0.0  | 157.49      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 900.0         | 44.94    | 0.0  | 0.0       | 944.94 | 0.0  | 0.0        | 0.0  | 944.94      |
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 07 September 2025 | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 17 April 2026     | Re-age           | 944.94 | 900.0     | 44.94    | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C72349 @AdvancedPaymentAllocation
  Scenario: Verify repeated re-aging with EQUAL_AMORTIZATION_FULL_INTEREST does not accumulate stub periods on interest bearing progressive loan with interest recalculation
    When Admin sets the business date to "28 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_CONTRACT_TERMINATION | 28 January 2026   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "28 January 2026" with "100" amount and expected disbursement date on "28 January 2026"
    When Admin successfully disburse the loan on "28 January 2026" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 28 January 2026   |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 28 February 2026  |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 28   | 28 March 2026     |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 28 April 2026     |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 28 May 2026       |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 28 June 2026      |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 28 July 2026      |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 28 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
# --- first re-age on disbursement date --- #
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling            |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_FULL_INTEREST |
# --- second re-age on 29 January 2026 --- #
    When Admin sets the business date to "29 January 2026"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling            |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_FULL_INTEREST |
# --- third re-age on 30 January 2026 --- #
    When Admin sets the business date to "30 January 2026"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling            |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_FULL_INTEREST |
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 28 January 2026   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 2    | 30 January 2026   | 30 January 2026  | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 28 February 2026  |                  | 83.33           | 16.67         | 0.34     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 28   | 28 March 2026     |                  | 66.66           | 16.67         | 0.34     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 31   | 28 April 2026     |                  | 49.99           | 16.67         | 0.34     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 30   | 28 May 2026       |                  | 33.32           | 16.67         | 0.34     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 31   | 28 June 2026      |                  | 16.65           | 16.67         | 0.34     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 7  | 30   | 28 July 2026      |                  | 0.0             | 16.65         | 0.35     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 28 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 28 January 2026  | Re-age           | 102.05 | 100.0     | 2.05     | 0.0  | 0.0       | 0.0          | false    |
      | 29 January 2026  | Re-age           | 102.05 | 100.0     | 2.05     | 0.0  | 0.0       | 0.0          | false    |
      | 30 January 2026  | Re-age           | 102.05 | 100.0     | 2.05     | 0.0  | 0.0       | 0.0          | false    |
# --- pay-off and verify loan closes cleanly --- #
    When Admin sets the business date to "01 February 2026"
    When Loan Pay-off is made on "01 February 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C72350 @AdvancedPaymentAllocation
  Scenario: Verify repeated re-aging with EQUAL_AMORTIZATION_PAYABLE_INTEREST does not accumulate stub periods on interest bearing progressive loan with interest recalculation
    When Admin sets the business date to "28 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_CONTRACT_TERMINATION | 28 January 2026   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "28 January 2026" with "100" amount and expected disbursement date on "28 January 2026"
    When Admin successfully disburse the loan on "28 January 2026" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 28 January 2026   |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 28 February 2026  |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 28   | 28 March 2026     |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 28 April 2026     |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 28 May 2026       |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 28 June 2026      |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 28 July 2026      |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 28 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
# --- first re-age on disbursement date --- #
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
# --- second re-age on 29 January 2026 --- #
    When Admin sets the business date to "29 January 2026"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
# --- third re-age on 30 January 2026 --- #
    When Admin sets the business date to "30 January 2026"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 28 January 2026   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 2    | 30 January 2026   | 30 January 2026  | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 28 February 2026  |                  | 83.33           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 3  | 28   | 28 March 2026     |                  | 66.66           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 4  | 31   | 28 April 2026     |                  | 49.99           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 5  | 30   | 28 May 2026       |                  | 33.32           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 6  | 31   | 28 June 2026      |                  | 16.65           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 7  | 30   | 28 July 2026      |                  | 0.0             | 16.65         | 0.0      | 0.0  | 0.0       | 16.65 | 0.0  | 0.0        | 0.0  | 16.65       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 28 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 28 January 2026  | Re-age           | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 29 January 2026  | Re-age           | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 30 January 2026  | Re-age           | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
# --- pay-off and verify loan closes cleanly --- #
    When Admin sets the business date to "01 February 2026"
    When Loan Pay-off is made on "01 February 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C72351 @AdvancedPaymentAllocation
  Scenario: Verify repeated re-aging with EQUAL_AMORTIZATION_PAYABLE_INTEREST does not fail on interest bearing progressive loan without interest recalculation
    When Admin sets the business date to "28 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR  | 28 January 2026   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "28 January 2026" with "100" amount and expected disbursement date on "28 January 2026"
    When Admin successfully disburse the loan on "28 January 2026" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 28 January 2026   |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 28 February 2026  |           | 83.59           | 16.41         | 0.59     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 2  | 28   | 28 March 2026     |           | 67.04           | 16.55         | 0.45     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 31   | 28 April 2026     |           | 50.44           | 16.6          | 0.4      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 30   | 28 May 2026       |           | 33.73           | 16.71         | 0.29     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 31   | 28 June 2026      |           | 16.93           | 16.8          | 0.2      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 30   | 28 July 2026      |           | 0.0             | 16.93         | 0.1      | 0.0  | 0.0       | 17.03 | 0.0  | 0.0        | 0.0  | 17.03       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.03     | 0.0  | 0.0       | 102.03 | 0.0  | 0.0        | 0.0  | 102.03      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 28 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
# --- first re-age on disbursement date --- #
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
# --- second re-age on 29 January 2026 --- #
    When Admin sets the business date to "29 January 2026"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
# --- third re-age on 30 January 2026 --- #
    When Admin sets the business date to "30 January 2026"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 28 January 2026   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 2    | 30 January 2026   | 30 January 2026  | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 28 February 2026  |                  | 83.01           | 16.99         | 0.01     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 3  | 28   | 28 March 2026     |                  | 66.02           | 16.99         | 0.01     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 4  | 31   | 28 April 2026     |                  | 49.03           | 16.99         | 0.01     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 5  | 30   | 28 May 2026       |                  | 32.04           | 16.99         | 0.01     | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
      | 6  | 31   | 28 June 2026      |                  | 15.05           | 16.99         | 0.0      | 0.0  | 0.0       | 16.99 | 0.0  | 0.0        | 0.0  | 16.99       |
      | 7  | 30   | 28 July 2026      |                  | 0.0             | 15.05         | 0.0      | 0.0  | 0.0       | 15.05 | 0.0  | 0.0        | 0.0  | 15.05       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.04     | 0.0  | 0.0       | 100.04 | 0.0  | 0.0        | 0.0  | 100.04      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 28 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 28 January 2026  | Re-age           | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 29 January 2026  | Re-age           | 100.02 | 100.0     | 0.02     | 0.0  | 0.0       | 0.0          | false    |
      | 30 January 2026  | Re-age           | 100.04 | 100.0     | 0.04     | 0.0  | 0.0       | 0.0          | false    |
# --- pay-off and verify loan closes cleanly --- #
    When Admin sets the business date to "01 February 2026"
    When Loan Pay-off is made on "01 February 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C72352 @AdvancedPaymentAllocation
  Scenario: Verify repeated re-aging with EQUAL_AMORTIZATION_PAYABLE_INTEREST does not fail on zero interest progressive loan
    When Admin sets the business date to "28 January 2026"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 28 January 2026   | 100            | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "28 January 2026" with "100" amount and expected disbursement date on "28 January 2026"
    When Admin successfully disburse the loan on "28 January 2026" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 28 January 2026   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 28 January 2026   | 28 January 2026  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 28 February 2026  |                  | 63.0            | 12.0          | 0.0      | 0.0  | 0.0       | 12.0  | 0.0  | 0.0        | 0.0  | 12.0        |
      | 3  | 28   | 28 March 2026     |                  | 51.0            | 12.0          | 0.0      | 0.0  | 0.0       | 12.0  | 0.0  | 0.0        | 0.0  | 12.0        |
      | 4  | 31   | 28 April 2026     |                  | 39.0            | 12.0          | 0.0      | 0.0  | 0.0       | 12.0  | 0.0  | 0.0        | 0.0  | 12.0        |
      | 5  | 30   | 28 May 2026       |                  | 27.0            | 12.0          | 0.0      | 0.0  | 0.0       | 12.0  | 0.0  | 0.0        | 0.0  | 12.0        |
      | 6  | 31   | 28 June 2026      |                  | 15.0            | 12.0          | 0.0      | 0.0  | 0.0       | 12.0  | 0.0  | 0.0        | 0.0  | 12.0        |
      | 7  | 30   | 28 July 2026      |                  | 0.0             | 15.0          | 0.0      | 0.0  | 0.0       | 15.0  | 0.0  | 0.0        | 0.0  | 15.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 25.0 | 0.0        | 0.0  | 75.0        |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 28 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 28 January 2026  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    |
# --- first re-age on disbursement date --- #
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
# --- second re-age on 29 January 2026 --- #
    When Admin sets the business date to "29 January 2026"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
# --- third re-age on 30 January 2026 --- #
    When Admin sets the business date to "30 January 2026"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 28 January 2026   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 28 January 2026   | 28 January 2026  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 2    | 30 January 2026   | 30 January 2026  | 75.0            | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 28 February 2026  |                  | 63.0            | 12.0          | 0.0      | 0.0  | 0.0       | 12.0  | 0.0  | 0.0        | 0.0  | 12.0        |
      | 4  | 28   | 28 March 2026     |                  | 51.0            | 12.0          | 0.0      | 0.0  | 0.0       | 12.0  | 0.0  | 0.0        | 0.0  | 12.0        |
      | 5  | 31   | 28 April 2026     |                  | 39.0            | 12.0          | 0.0      | 0.0  | 0.0       | 12.0  | 0.0  | 0.0        | 0.0  | 12.0        |
      | 6  | 30   | 28 May 2026       |                  | 27.0            | 12.0          | 0.0      | 0.0  | 0.0       | 12.0  | 0.0  | 0.0        | 0.0  | 12.0        |
      | 7  | 31   | 28 June 2026      |                  | 15.0            | 12.0          | 0.0      | 0.0  | 0.0       | 12.0  | 0.0  | 0.0        | 0.0  | 12.0        |
      | 8  | 30   | 28 July 2026      |                  | 0.0             | 15.0          | 0.0      | 0.0  | 0.0       | 15.0  | 0.0  | 0.0        | 0.0  | 15.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 25.0 | 0.0        | 0.0  | 75.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 28 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 28 January 2026  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    |
      | 28 January 2026  | Re-age           | 75.0   | 75.0      | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 29 January 2026  | Re-age           | 75.0   | 75.0      | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 30 January 2026  | Re-age           | 75.0   | 75.0      | 0.0      | 0.0  | 0.0       | 0.0          | false    |
# --- pay-off and verify loan closes cleanly --- #
    When Admin sets the business date to "01 February 2026"
    When Loan Pay-off is made on "01 February 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C72353 @AdvancedPaymentAllocation
  Scenario: Verify that post-maturity Re-aging with EQUAL_AMORTIZATION_PAYABLE_INTEREST succeeds on interest bearing progressive loan without interest recalculation
    When Admin sets the business date to "07 September 2025"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date  | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR  | 07 September 2025  | 900            | 9.99                   | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "07 September 2025" with "900" amount and expected disbursement date on "07 September 2025"
    And Admin successfully disburse the loan on "07 September 2025" with "900" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date               | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 07 September 2025  |           | 900.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 07 October 2025    |           | 753.39          | 146.61        | 7.39     | 0.0  | 0.0       | 154.0  | 0.0  | 0.0        | 0.0  | 154.0       |
      | 2  | 31   | 07 November 2025   |           | 605.78          | 147.61        | 6.39     | 0.0  | 0.0       | 154.0  | 0.0  | 0.0        | 0.0  | 154.0       |
      | 3  | 30   | 07 December 2025   |           | 456.75          | 149.03        | 4.97     | 0.0  | 0.0       | 154.0  | 0.0  | 0.0        | 0.0  | 154.0       |
      | 4  | 31   | 07 January 2026    |           | 306.63          | 150.12        | 3.88     | 0.0  | 0.0       | 154.0  | 0.0  | 0.0        | 0.0  | 154.0       |
      | 5  | 31   | 07 February 2026   |           | 155.23          | 151.4         | 2.6      | 0.0  | 0.0       | 154.0  | 0.0  | 0.0        | 0.0  | 154.0       |
      | 6  | 28   | 07 March 2026      |           | 0.0             | 155.23        | 1.19     | 0.0  | 0.0       | 156.42 | 0.0  | 0.0        | 0.0  | 156.42      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 900.0         | 26.42    | 0.0  | 0.0       | 926.42 | 0.0  | 0.0        | 0.0  | 926.42      |
    And Loan Transactions tab has the following data:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 07 September 2025 | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
# --- No payments made. Loan matures on 07 March 2026. Move date past maturity. --- #
    When Admin sets the business date to "17 April 2026"
    When Admin runs inline COB job for Loan
# --- Re-age post maturity with EQUAL_AMORTIZATION_PAYABLE_INTEREST (no interest recalc) --- #
    When Admin creates a Loan re-aging transaction by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate      | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 17 May 2026    | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan Repayment schedule has 12 periods, with the following data for periods:
      | Nr | Days | Date               | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 07 September 2025  |                 | 900.0           |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 07 October 2025    | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 07 November 2025   | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 3  | 30   | 07 December 2025   | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 4  | 31   | 07 January 2026    | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 07 February 2026   | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 6  | 28   | 07 March 2026      | 17 April 2026   | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 7  | 71   | 17 May 2026        |                 | 750.4           | 149.6         | 4.4      | 0.0  | 0.0       | 154.0  | 0.0  | 0.0        | 0.0  | 154.0       |
      | 8  | 31   | 17 June 2026       |                 | 600.8           | 149.6         | 4.4      | 0.0  | 0.0       | 154.0  | 0.0  | 0.0        | 0.0  | 154.0       |
      | 9  | 30   | 17 July 2026       |                 | 451.2           | 149.6         | 4.4      | 0.0  | 0.0       | 154.0  | 0.0  | 0.0        | 0.0  | 154.0       |
      | 10 | 31   | 17 August 2026     |                 | 301.6           | 149.6         | 4.4      | 0.0  | 0.0       | 154.0  | 0.0  | 0.0        | 0.0  | 154.0       |
      | 11 | 31   | 17 September 2026  |                 | 152.0           | 149.6         | 4.4      | 0.0  | 0.0       | 154.0  | 0.0  | 0.0        | 0.0  | 154.0       |
      | 12 | 30   | 17 October 2026    |                 | 0.0             | 152.0         | 4.42     | 0.0  | 0.0       | 156.42 | 0.0  | 0.0        | 0.0  | 156.42      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 900.0         | 26.42    | 0.0  | 0.0       | 926.42 | 0.0  | 0.0        | 0.0  | 926.42      |
    Then Loan Transactions tab has the following data without accruals:
      | Transaction date  | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 07 September 2025 | Disbursement     | 900.0  | 0.0       | 0.0      | 0.0  | 0.0       | 900.0        | false    |
      | 17 April 2026     | Re-age           | 926.42 | 900.0     | 26.42    | 0.0  | 0.0       | 0.0          | false    |

  @TestRailId:C72354 @AdvancedPaymentAllocation
  Scenario: Verify re-aging preview does not accumulate 1-day stub periods after repeated re-aging with EQUAL_AMORTIZATION_PAYABLE_INTEREST across month-end
    When Admin sets the business date to "28 January 2026"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_CONTRACT_TERMINATION | 28 January 2026   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "28 January 2026" with "100" amount and expected disbursement date on "28 January 2026"
    When Admin successfully disburse the loan on "28 January 2026" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 28 January 2026   |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 28 February 2026  |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 28   | 28 March 2026     |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 28 April 2026     |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 28 May 2026       |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 28 June 2026      |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 28 July 2026      |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 28 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
# --- first re-age on disbursement date 28 January 2026 --- #
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
# --- second re-age on 29 January 2026 --- #
    When Admin sets the business date to "29 January 2026"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
# --- third re-age on 30 January 2026 --- #
    When Admin sets the business date to "30 January 2026"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
# --- fourth re-age on 31 January 2026 --- #
    When Admin sets the business date to "31 January 2026"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 28 January 2026   |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 3    | 31 January 2026   | 31 January 2026  | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 28   | 28 February 2026  |                  | 83.33           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 3  | 28   | 28 March 2026     |                  | 66.66           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 4  | 31   | 28 April 2026     |                  | 49.99           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 5  | 30   | 28 May 2026       |                  | 33.32           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 6  | 31   | 28 June 2026      |                  | 16.65           | 16.67         | 0.0      | 0.0  | 0.0       | 16.67 | 0.0  | 0.0        | 0.0  | 16.67       |
      | 7  | 30   | 28 July 2026      |                  | 0.0             | 16.65         | 0.0      | 0.0  | 0.0       | 16.65 | 0.0  | 0.0        | 0.0  | 16.65       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 0.0  | 0.0        | 0.0  | 100.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 28 January 2026  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 28 January 2026  | Re-age           | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 29 January 2026  | Re-age           | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 30 January 2026  | Re-age           | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
      | 31 January 2026  | Re-age           | 100.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          | false    |
# --- re-age preview on 01 February 2026: must not accumulate 1-day stub periods --- #
    When Admin sets the business date to "01 February 2026"
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments | reAgeInterestHandling               |
      | 1               | MONTHS        | 28 February 2026 | 6                    | EQUAL_AMORTIZATION_PAYABLE_INTEREST |
# --- pay-off and verify loan closes cleanly --- #
    When Loan Pay-off is made on "01 February 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
