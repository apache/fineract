@LoanReAgingPreviewFeature
Feature: LoanReAgingPreview

  @TestRailId:C4098
  Scenario: Basic verification of the loan re-aging preview schedule
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    When Admin sets the business date to "15 April 2025"
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 15 April 2025 | 3                    |
    Then Loan Repayment schedule preview has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |               | 1000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2025  | 15 April 2025 | 1000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2025 | 15 April 2025 | 1000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2025    | 15 April 2025 | 1000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2025    | 15 April 2025 | 1000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 5  | 14   | 15 April 2025    |               | 666.67          | 333.33        | 0.0      | 0.0  | 0.0       | 333.33 | 0.0  | 0.0        | 0.0  | 333.33      |
      | 6  | 30   | 15 May 2025      |               | 333.34          | 333.33        | 0.0      | 0.0  | 0.0       | 333.33 | 0.0  | 0.0        | 0.0  | 333.33      |
      | 7  | 31   | 15 June 2025     |               | 0.0             | 333.34        | 0.0      | 0.0  | 0.0       | 333.34 | 0.0  | 0.0        | 0.0  | 333.34      |
    Then Loan Repayment schedule preview has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2025  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2025 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2025    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2025    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |

    When Loan Pay-off is made on "15 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4099
  Scenario: Verify Loan re-aging preview with chargeback
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 15   | 16 January 2024  |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2024  |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2024 |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    When Admin sets the business date to "01 January 2024"
    And Customer makes "AUTOPAY" repayment on "01 January 2024" with 250 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2024  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2024  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2024 |                 | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
    When Admin sets the business date to "02 February 2024"
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2024  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2024  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2024 |                 | 0.0             | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0  | 375.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1125.0        | 0.0      | 0.0  | 0.0       | 1125.0 | 250.0 | 0.0        | 0.0  | 875.0       |
    When Admin sets the business date to "21 February 2024"
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 2               | MONTHS        | 10 March 2024 | 3                    |
    Then Loan Repayment schedule preview has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 1000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2024  | 21 February 2024 | 750.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 15   | 31 January 2024  | 21 February 2024 | 750.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 15   | 15 February 2024 | 21 February 2024 | 875.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0   | 0.0        | 0.0  | 0.0         |
      | 5  | 24   | 10 March 2024    |                  | 583.33          | 291.67        | 0.0      | 0.0  | 0.0       | 291.67 | 0.0   | 0.0        | 0.0  | 291.67      |
      | 6  | 61   | 10 May 2024      |                  | 291.66          | 291.67        | 0.0      | 0.0  | 0.0       | 291.67 | 0.0   | 0.0        | 0.0  | 291.67      |
      | 7  | 61   | 10 July 2024     |                  | 0.0             | 291.66        | 0.0      | 0.0  | 0.0       | 291.66 | 0.0   | 0.0        | 0.0  | 291.66      |
    And Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 15   | 16 January 2024  |                 | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 3  | 15   | 31 January 2024  |                 | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0  | 250.0       |
      | 4  | 15   | 15 February 2024 |                 | 0.0             | 375.0         | 0.0      | 0.0  | 0.0       | 375.0 | 0.0   | 0.0        | 0.0  | 375.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 1125.0        | 0.0      | 0.0  | 0.0       | 1125.0 | 250.0 | 0.0        | 0.0  | 875.0       |

    When Loan Pay-off is made on "02 February 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4100
  Scenario: Verify Loan re-aging preview with charge N+1 installment after maturity date
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    When Admin sets the business date to "3 May 2025"
    And Admin adds "LOAN_NSF_FEE" due date charge with "3 May 2025" due date and 10 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2025  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2025 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2025    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2025    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 5  | 32   | 03 May 2025      |           | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0  | 0.0  | 0.0        | 0.0  | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 10.0      | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | WEEKS         | 01 April 2025 | 1                    |
    Then Loan Repayment schedule preview has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |               | 1000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2025  | 01 April 2025 | 1000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2025 | 01 April 2025 | 1000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 3  | 28   | 01 March 2025    | 01 April 2025 | 1000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0    | 0.0  | 0.0        | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2025    |               | 0.0             | 1000.0        | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
      | 5  | 32   | 03 May 2025      |               | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0   | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule preview has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 10.0      | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |
    And Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2025  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 February 2025 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 28   | 01 March 2025    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 31   | 01 April 2025    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 5  | 32   | 03 May 2025      |           | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0  | 0.0  | 0.0        | 0.0  | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 10.0      | 1010.0 | 0.0  | 0.0        | 0.0  | 1010.0      |

    When Loan Pay-off is made on "03 May 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4101
  Scenario: Verify Loan re-aging preview with backdated repayment, charge and N+1 installment after maturity date
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    When Admin sets the business date to "3 May 2025"
    And Customer makes "AUTOPAY" repayment on "01 March 2025" with 250 EUR transaction amount
    And Admin adds "LOAN_NSF_FEE" due date charge with "3 May 2025" due date and 10 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |               | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  | 01 March 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 31   | 01 February 2025 |               | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 3  | 28   | 01 March 2025    |               | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 31   | 01 April 2025    |               | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 5  | 32   | 03 May 2025      |               | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0  | 0.0   | 0.0        | 0.0   | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 10.0      | 1010.0 | 250.0 | 0.0        | 250.0 | 760.0       |
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | WEEKS         | 01 April 2025 | 1                    |
    Then Loan Repayment schedule preview has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |               | 1000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  | 01 March 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 31   | 01 February 2025 | 01 April 2025 | 750.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0   | 0.0         |
      | 3  | 28   | 01 March 2025    | 01 April 2025 | 750.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0   | 0.0         |
      | 4  | 31   | 01 April 2025    |               | 0.0             | 750.0         | 0.0      | 0.0  | 0.0       | 750.0 | 0.0   | 0.0        | 0.0   | 750.0       |
      | 5  | 32   | 03 May 2025      |               | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0  | 0.0   | 0.0        | 0.0   | 10.0        |
    And Loan Repayment schedule preview has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 10.0      | 1010.0 | 250.0 | 0.0        | 250.0 | 760.0       |
    And Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |               | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  | 01 March 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 31   | 01 February 2025 |               | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 3  | 28   | 01 March 2025    |               | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 31   | 01 April 2025    |               | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 5  | 32   | 03 May 2025      |               | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0  | 0.0   | 0.0        | 0.0   | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 10.0      | 1010.0 | 250.0 | 0.0        | 250.0 | 760.0       |

    When Loan Pay-off is made on "03 May 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4102
  Scenario: Verify Loan re-aging preview with downpayment, payoff and charge - N+1 installment after maturity date
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2025   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    When Admin sets the business date to "20 March 2025"
    When Loan Pay-off is made on "20 March 2025"
    And Admin adds "LOAN_NSF_FEE" due date charge with "20 March 2025" due date and 10 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  | 01 January 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 15   | 16 January 2025  | 20 March 2025   | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 3  | 15   | 31 January 2025  | 20 March 2025   | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 4  | 15   | 15 February 2025 | 20 March 2025   | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 5  | 33   | 20 March 2025    |                 | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0  | 0.0   | 0.0        | 0.0   | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 10.0      | 1010.0 | 1000.0 | 0.0        | 750.0 | 10.0        |
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate        | numberOfInstallments |
      | 1               | WEEKS         | 15 February 2025 | 1                    |
    Then Loan Repayment schedule preview has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |                  | 1000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  | 01 January 2025  | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 15   | 16 January 2025  | 15 February 2025 | 750.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0   | 0.0         |
      | 3  | 15   | 31 January 2025  | 15 February 2025 | 750.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0   | 0.0         |
      | 4  | 15   | 15 February 2025 | 20 March 2025    | 0.0             | 750.0         | 0.0      | 0.0  | 0.0       | 750.0 | 750.0 | 0.0        | 750.0 | 0.0         |
      | 5  | 33   | 20 March 2025    |                  | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0  | 0.0   | 0.0        | 0.0   | 10.0        |
    And Loan Repayment schedule preview has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 10.0      | 1010.0 | 1000.0 | 0.0        | 750.0 | 10.0        |
    And Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |                 | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  | 01 January 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 0.0   | 0.0         |
      | 2  | 15   | 16 January 2025  | 20 March 2025   | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 3  | 15   | 31 January 2025  | 20 March 2025   | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 4  | 15   | 15 February 2025 | 20 March 2025   | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 5  | 33   | 20 March 2025    |                 | 0.0             | 0.0           | 0.0      | 0.0  | 10.0      | 10.0  | 0.0   | 0.0        | 0.0   | 10.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 10.0      | 1010.0 | 1000.0 | 0.0        | 750.0 | 10.0        |

    When Loan Pay-off is made on "20 March 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4103
  Scenario: Verify that Loan re-aging preview with repayment, chargeback and charge - N+1 installment after maturity date
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    When Admin sets the business date to "3 May 2025"
    And Customer makes "AUTOPAY" repayment on "01 March 2025" with 250 EUR transaction amount
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 125 EUR transaction amount
    And Admin adds "LOAN_NSF_FEE" due date charge with "3 May 2025" due date and 10 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |               | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  | 01 March 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 31   | 01 February 2025 |               | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 3  | 28   | 01 March 2025    |               | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 31   | 01 April 2025    |               | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 5  | 32   | 03 May 2025      |               | 0.0             | 125.0         | 0.0      | 0.0  | 10.0      | 135.0 | 0.0   | 0.0        | 0.0   | 135.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1125.0        | 0.0      | 0.0  | 10.0      | 1135.0 | 250.0 | 0.0        | 250.0 | 885.0       |
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | WEEKS         | 01 April 2025 | 1                    |
    Then Loan Repayment schedule preview has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |               | 1000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  | 01 March 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 31   | 01 February 2025 | 01 April 2025 | 750.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0   | 0.0         |
      | 3  | 28   | 01 March 2025    | 01 April 2025 | 750.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0   | 0.0         |
      | 4  | 31   | 01 April 2025    |               | 0.0             | 750.0         | 0.0      | 0.0  | 0.0       | 750.0 | 0.0   | 0.0        | 0.0   | 750.0       |
      | 5  | 32   | 03 May 2025      |               | 0.0             | 125.0         | 0.0      | 0.0  | 10.0      | 135.0 | 0.0   | 0.0        | 0.0   | 135.0       |
    And Loan Repayment schedule preview has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1125.0        | 0.0      | 0.0  | 10.0      | 1135.0 | 250.0 | 0.0        | 250.0 | 885.0       |
    And Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |               | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  | 01 March 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 31   | 01 February 2025 |               | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 3  | 28   | 01 March 2025    |               | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 31   | 01 April 2025    |               | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 5  | 32   | 03 May 2025      |               | 0.0             | 125.0         | 0.0      | 0.0  | 10.0      | 135.0 | 0.0   | 0.0        | 0.0   | 135.0       |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1125.0        | 0.0      | 0.0  | 10.0      | 1135.0 | 250.0 | 0.0        | 250.0 | 885.0       |

    When Loan Pay-off is made on "03 May 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4104
  Scenario: Verify that Loan re-aging preview with repayment, charge and charge adjustment - N+1 installment after maturity date
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    When Admin sets the business date to "3 May 2025"
    And Customer makes "AUTOPAY" repayment on "01 March 2025" with 250 EUR transaction amount
    And Admin adds "LOAN_NSF_FEE" due date charge with "03 May 2025" due date and 20 EUR transaction amount
    When Admin sets the business date to "04 May 2025"
    When Admin makes a charge adjustment for the last "LOAN_NSF_FEE" type charge which is due on "03 May 2025" with 20 EUR transaction amount and externalId ""
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |               | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  | 01 March 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 31   | 01 February 2025 |               | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 20.0  | 0.0        | 20.0  | 230.0       |
      | 3  | 28   | 01 March 2025    |               | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 31   | 01 April 2025    |               | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 5  | 32   | 03 May 2025      |               | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0   | 0.0        | 0.0   | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 270.0 | 0.0        | 270.0 | 750.0       |
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | WEEKS         | 01 April 2025 | 1                    |
    Then Loan Repayment schedule preview has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |               | 1000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  | 01 March 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 31   | 01 February 2025 | 01 April 2025 | 750.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0   | 0.0         |
      | 3  | 28   | 01 March 2025    | 01 April 2025 | 750.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0   | 0.0         |
      | 4  | 31   | 01 April 2025    |               | 0.0             | 750.0         | 0.0      | 0.0  | 0.0       | 750.0 | 20.0  | 0.0        | 20.0  | 730.0       |
      | 5  | 32   | 03 May 2025      |               | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0   | 0.0        | 0.0   | 20.0        |
    Then Loan Repayment schedule preview has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 270.0 | 0.0        | 270.0 | 750.0       |
    And Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |               | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  | 01 March 2025 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 250.0 | 0.0        | 250.0 | 0.0         |
      | 2  | 31   | 01 February 2025 |               | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 20.0  | 0.0        | 20.0  | 230.0       |
      | 3  | 28   | 01 March 2025    |               | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 31   | 01 April 2025    |               | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 5  | 32   | 03 May 2025      |               | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0   | 0.0        | 0.0   | 20.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 270.0 | 0.0        | 270.0 | 750.0       |

    When Loan Pay-off is made on "04 May 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4105
  Scenario: Verify that Loan re-aging transaction with MIR and charge - N+1 installment after maturity date
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    And Admin sets the business date to "03 May 2025"
    And Admin makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "01 April 2025" with 100 EUR transaction amount
    And Admin adds "LOAN_NSF_FEE" due date charge with "03 May 2025" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 100.0 | 0.0        | 100.0 | 150.0       |
      | 2  | 31   | 01 February 2025 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 3  | 28   | 01 March 2025    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 31   | 01 April 2025    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 5  | 32   | 03 May 2025      |           | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0   | 0.0        | 0.0   | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 100.0 | 0.0        | 100.0 | 920.0       |
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | WEEKS         | 01 April 2025 | 1                    |
    Then Loan Repayment schedule preview has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |               | 1000.0          | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  | 01 April 2025 | 900.0           | 100.0         | 0.0      | 0.0  | 0.0       | 100.0 | 100.0 | 0.0        | 100.0 | 0.0         |
      | 2  | 31   | 01 February 2025 | 01 April 2025 | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0   | 0.0         |
      | 3  | 28   | 01 March 2025    | 01 April 2025 | 900.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0   | 0.0         |
      | 4  | 31   | 01 April 2025    |               | 0.0             | 900.0         | 0.0      | 0.0  | 0.0       | 900.0 | 0.0   | 0.0        | 0.0   | 900.0       |
      | 5  | 32   | 03 May 2025      |               | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0   | 0.0        | 0.0   | 20.0        |
    And Loan Repayment schedule preview has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 100.0 | 0.0        | 100.0 | 920.0       |
    And Loan Repayment schedule has 5 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2025  |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2025  |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 100.0 | 0.0        | 100.0 | 150.0       |
      | 2  | 31   | 01 February 2025 |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 3  | 28   | 01 March 2025    |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 4  | 31   | 01 April 2025    |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0   | 0.0        | 0.0   | 250.0       |
      | 5  | 32   | 03 May 2025      |           | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0   | 0.0        | 0.0   | 20.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 1000.0        | 0.0      | 0.0  | 20.0      | 1020.0 | 100.0 | 0.0        | 100.0 | 920.0       |

    When Loan Pay-off is made on "03 May 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4106
  Scenario: Verify that Loan re-aging preview with 2nd disbursement and charge - N+1 installment after maturity date
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "500" amount and expected disbursement date on "01 January 2025"
    And Admin successfully disburse the loan on "01 January 2025" with "500" EUR transaction amount
    And Admin sets the business date to "01 May 2025"
    And Admin successfully disburse the loan on "16 January 2025" with "100" EUR transaction amount
    And Admin adds "LOAN_NSF_FEE" due date charge with "01 May 2025" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2025  |           | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 0.0  | 0.0        | 0.0  | 125.0       |
      |    |      | 16 January 2025  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 0    | 16 January 2025  |           | 450.0           | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 0.0  | 0.0        | 0.0  | 25.0        |
      | 3  | 31   | 01 February 2025 |           | 300.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
      | 4  | 28   | 01 March 2025    |           | 150.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
      | 5  | 31   | 01 April 2025    |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
      | 6  | 30   | 01 May 2025      |           | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0  | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 600.0         | 0.0      | 0.0  | 20.0      | 620.0 | 0.0  | 0.0        | 0.0  | 620.0       |
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | WEEKS         | 01 April 2025 | 1                    |
    Then Loan Repayment schedule preview has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date     | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |               | 500.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2025  | 01 April 2025 | 500.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      |    |      | 16 January 2025  |               | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  |            |      |             |
      | 2  | 0    | 16 January 2025  | 01 April 2025 | 600.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 February 2025 | 01 April 2025 | 600.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 4  | 28   | 01 March 2025    | 01 April 2025 | 600.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0  | 0.0        | 0.0  | 0.0         |
      | 5  | 31   | 01 April 2025    |               | 0.0             | 600.0         | 0.0      | 0.0  | 0.0       | 600.0 | 0.0  | 0.0        | 0.0  | 600.0       |
      | 6  | 30   | 01 May 2025      |               | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0  | 0.0        | 0.0  | 20.0        |
    Then Loan Repayment schedule preview has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 600.0         | 0.0      | 0.0  | 20.0      | 620.0 | 0.0  | 0.0        | 0.0  | 620.0       |
    And Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 500.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2025  |           | 375.0           | 125.0         | 0.0      | 0.0  | 0.0       | 125.0 | 0.0  | 0.0        | 0.0  | 125.0       |
      |    |      | 16 January 2025  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 2  | 0    | 16 January 2025  |           | 450.0           | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 0.0  | 0.0        | 0.0  | 25.0        |
      | 3  | 31   | 01 February 2025 |           | 300.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
      | 4  | 28   | 01 March 2025    |           | 150.0           | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
      | 5  | 31   | 01 April 2025    |           | 0.0             | 150.0         | 0.0      | 0.0  | 0.0       | 150.0 | 0.0  | 0.0        | 0.0  | 150.0       |
      | 6  | 30   | 01 May 2025      |           | 0.0             | 0.0           | 0.0      | 0.0  | 20.0      | 20.0  | 0.0  | 0.0        | 0.0  | 20.0        |
    And Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 600.0         | 0.0      | 0.0  | 20.0      | 620.0 | 0.0  | 0.0        | 0.0  | 620.0       |

    When Loan Pay-off is made on "01 May 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4209 @AdvancedPaymentAllocation
  Scenario: Verify Re-aging preview on interest bearing loan - Interest calculation: Default Behavior - Interest Split scenario - UC1
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL  | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "15 March 2024"
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024| 6                    |
    Then Loan Repayment schedule preview has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 4  | 30   | 01 May 2024      |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 31   | 01 June 2024     |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 30   | 01 July 2024     |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 31   | 01 August 2024   |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 September 2024|                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    Then Loan Repayment schedule preview has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8 | 17.01 | 0.0        | 0.0  | 85.79       |

    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4210 @AdvancedPaymentAllocation
  Scenario: Verify Re-aging preview on interest bearing loan - Interest calculation: Default Behavior - Fees and Interest Split before re-aging - UC2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL  | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "15 February 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 February 2024" due date and 10 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 17.01 | 0.0        | 0.0  | 95.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
    And Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at     | Due as of        | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date | 15 February 2024 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    When Admin sets the business date to "15 March 2024"
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024| 6                    |
    Then Loan Repayment schedule preview has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 83.57           | 0.0           | 0.0      | 10.0 | 0.0       | 10.0  | 0.0   | 0.0        | 0.0  | 10.0        |
      | 3  | 31   | 01 April 2024    |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 4  | 30   | 01 May 2024      |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 31   | 01 June 2024     |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 30   | 01 July 2024     |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 31   | 01 August 2024   |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 September 2024|                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    Then Loan Repayment schedule preview has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 10.0 | 0.0       | 112.8 | 17.01 | 0.0        | 0.0  | 95.79       |

    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4211 @AdvancedPaymentAllocation
  Scenario: Verify allowing Re-aging preview on interest bearing loan - Interest calculation: Default Behavior - N+1 Scenario - UC4
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL  | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    When Admin sets the business date to "15 July 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 July 2024" due date and 10 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
      | 7  | 14   | 15 July 2024     |                  | 0.0             | 0.0           | 0.0      | 10.0 | 0.0       | 10.0  | 0.0   | 0.0        | 0.0  | 10.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 17.01 | 0.0        | 0.0  | 95.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
    When Admin sets the business date to "15 March 2024"
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024| 6                    |
    Then Loan Repayment schedule preview has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 4  | 30   | 01 May 2024      |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 31   | 01 June 2024     |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 30   | 01 July 2024     |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 31   | 01 August 2024   |                  | 14.21           | 14.13         | 0.17     | 10.0 | 0.0       | 24.3  | 0.0   | 0.0        | 0.0  | 24.3        |
      | 8  | 31   | 01 September 2024|                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    Then Loan Repayment schedule preview has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 10.0 | 0.0       | 112.8 | 17.01 | 0.0        | 0.0  | 95.79       |

    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4212 @AdvancedPaymentAllocation
  Scenario: Verify Re-aging preview on interest bearing loan - Interest calculation: Default Behavior - Partially paid installment - UC5
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL  | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
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
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 25.0 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.0            | 16.57         | 0.44     | 0.0  | 0.0       | 17.01 | 7.99  | 7.99       | 0.0  | 9.02        |
      | 3  | 31   | 01 April 2024    |                  | 50.38           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.66           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.85           | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.85         | 0.1      | 0.0  | 0.0       | 16.95 | 0.0   | 0.0        | 0.0  | 16.95       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.0      | 0.0  | 0.0       | 102.0 | 25.0 | 7.99       | 0.0  | 77.0        |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 25.0   | 24.42     | 0.58     | 0.0  | 0.0       | 75.58        | false    |
    When Admin sets the business date to "15 March 2024"
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate    | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024| 6                    |
    Then Loan Repayment schedule preview has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 March 2024    | 75.58           | 7.99          | 0.0      | 0.0  | 0.0       | 7.99  | 7.99  | 7.99       | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 63.53           | 12.05         | 0.88     | 0.0  | 0.0       | 12.93 | 0.0   | 0.0        | 0.0  | 12.93       |
      | 4  | 30   | 01 May 2024      |                  | 50.97           | 12.56         | 0.37     | 0.0  | 0.0       | 12.93 | 0.0   | 0.0        | 0.0  | 12.93       |
      | 5  | 31   | 01 June 2024     |                  | 38.34           | 12.63         | 0.3      | 0.0  | 0.0       | 12.93 | 0.0   | 0.0        | 0.0  | 12.93       |
      | 6  | 30   | 01 July 2024     |                  | 25.63           | 12.71         | 0.22     | 0.0  | 0.0       | 12.93 | 0.0   | 0.0        | 0.0  | 12.93       |
      | 7  | 31   | 01 August 2024   |                  | 12.85           | 12.78         | 0.15     | 0.0  | 0.0       | 12.93 | 0.0   | 0.0        | 0.0  | 12.93       |
      | 8  | 31   | 01 September 2024|                  | 0.0             | 12.85         | 0.07     | 0.0  | 0.0       | 12.92 | 0.0   | 0.0        | 0.0  | 12.92       |
    Then Loan Repayment schedule preview has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.57     | 0.0  | 0.0       | 102.57 | 25.0 | 7.99       | 0.0  | 77.57       |

    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C_4213 @AdvancedPaymentAllocation
  Scenario: Verify Re-aging preview on interest bearing loan - Interest calculation: Default Behavior - DownPayment Scenarios - UC7
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_AUTO_DOWNPAYMENT" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_AUTO_DOWNPAYMENT | 01 January 2024   | 100            | 7                       | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date       | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                 | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 0    | 01 January 2024  | 01 January 2024 | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024 |                 | 62.68           | 12.32         | 0.44     | 0.0  | 0.0       | 12.76 | 0.0  | 0.0        | 0.0  | 12.76       |
      | 3  | 29   | 01 March 2024    |                 | 50.29           | 12.39         | 0.37     | 0.0  | 0.0       | 12.76 | 0.0  | 0.0        | 0.0  | 12.76       |
      | 4  | 31   | 01 April 2024    |                 | 37.82           | 12.47         | 0.29     | 0.0  | 0.0       | 12.76 | 0.0  | 0.0        | 0.0  | 12.76       |
      | 5  | 30   | 01 May 2024      |                 | 25.28           | 12.54         | 0.22     | 0.0  | 0.0       | 12.76 | 0.0  | 0.0        | 0.0  | 12.76       |
      | 6  | 31   | 01 June 2024     |                 | 12.67           | 12.61         | 0.15     | 0.0  | 0.0       | 12.76 | 0.0  | 0.0        | 0.0  | 12.76       |
      | 7  | 30   | 01 July 2024     |                 | 0.0             | 12.67         | 0.07     | 0.0  | 0.0       | 12.74 | 0.0  | 0.0        | 0.0  | 12.74       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 1.54     | 0.0  | 0.0       | 101.54 | 25.0 | 0.0        | 0.0  | 76.54       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 12.76 EUR transaction amount
    Then Loan Repayment schedule has 7 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late  | Outstanding |
      |    |      | 01 January 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |       |             |
      | 1  | 0    | 01 January 2024 | 01 January 2024  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0  | 0.0        | 0.0   | 0.0         |
      | 2  | 31   | 01 February 2024| 01 February 2024 | 62.68           | 12.32         | 0.44     | 0.0  | 0.0       | 12.76 | 12.76 | 0.0        | 0.0   | 0.0         |
      | 3  | 29   | 01 March 2024   |                  | 50.29           | 12.39         | 0.37     | 0.0  | 0.0       | 12.76 | 0.0   | 0.0        | 0.0   | 12.76       |
      | 4  | 31   | 01 April 2024   |                  | 37.82           | 12.47         | 0.29     | 0.0  | 0.0       | 12.76 | 0.0   | 0.0        | 0.0   | 12.76       |
      | 5  | 30   | 01 May 2024     |                  | 25.28           | 12.54         | 0.22     | 0.0  | 0.0       | 12.76 | 0.0   | 0.0        | 0.0   | 12.76       |
      | 6  | 31   | 01 June 2024    |                  | 12.67           | 12.61         | 0.15     | 0.0  | 0.0       | 12.76 | 0.0   | 0.0        | 0.0   | 12.76       |
      | 7  | 30   | 01 July 2024    |                  | 0.0             | 12.67         | 0.07     | 0.0  | 0.0       | 12.74 | 0.0   | 0.0        | 0.0   | 12.74       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late  | Outstanding |
      | 100.0         | 1.54     | 0.0  | 0.0       | 101.54 | 37.76 | 0.0        | 0.0   | 63.78       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 January 2024  | Down Payment     | 25.0   | 25.0      | 0.0      | 0.0  | 0.0       | 75.0         | false    |
      | 01 February 2024 | Repayment        | 12.76  | 12.32     | 0.44     | 0.0  | 0.0       | 62.68        | false    |
    When Admin sets the business date to "15 March 2024"
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024 | 6                    |
    Then Loan Repayment schedule preview has 9 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024   |                  | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   |            |      |             |
      | 1  | 0    | 01 January 2024   | 01 January 2024  | 75.0            | 25.0          | 0.0      | 0.0  | 0.0       | 25.0  | 25.0  | 0.0        | 0.0  | 0.0         |
      | 2  | 31   | 01 February 2024  | 01 February 2024 | 62.68           | 12.32         | 0.44     | 0.0  | 0.0       | 12.76 | 12.76 | 0.0        | 0.0  | 0.0         |
      | 3  | 29   | 01 March 2024     | 15 March 2024    | 62.68           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 4  | 31   | 01 April 2024     |                  | 52.69           | 9.99          | 0.74     | 0.0  | 0.0       | 10.73 | 0.0   | 0.0        | 0.0  | 10.73       |
      | 5  | 30   | 01 May 2024       |                  | 42.27           | 10.42         | 0.31     | 0.0  | 0.0       | 10.73 | 0.0   | 0.0        | 0.0  | 10.73       |
      | 6  | 31   | 01 June 2024      |                  | 31.79           | 10.48         | 0.25     | 0.0  | 0.0       | 10.73 | 0.0   | 0.0        | 0.0  | 10.73       |
      | 7  | 30   | 01 July 2024      |                  | 21.25           | 10.54         | 0.19     | 0.0  | 0.0       | 10.73 | 0.0   | 0.0        | 0.0  | 10.73       |
      | 8  | 31   | 01 August 2024    |                  | 10.64           | 10.61         | 0.12     | 0.0  | 0.0       | 10.73 | 0.0   | 0.0        | 0.0  | 10.73       |
      | 9  | 31   | 01 September 2024 |                  | 0.0             | 10.64         | 0.06     | 0.0  | 0.0       | 10.7  | 0.0   | 0.0        | 0.0  | 10.7        |
    Then Loan Repayment schedule preview has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.11     | 0.0  | 0.0       | 102.11 | 37.76 | 0.0        | 0.0  | 64.35       |

    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4214 @AdvancedPaymentAllocation
  Scenario: Verify Re-aging preview on interest bearing loan - Interest calculation: Default Behavior - reversal of Re-aging - UC9
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                       | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024|           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024   |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024   |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024     |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024    |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024    |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024| 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024   |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024   |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024     |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024    |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024    |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
    When Admin sets the business date to "15 March 2024"
    When Admin creates a Loan re-aging transaction with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024 | 6                    |
    Then Loan Repayment schedule has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 4  | 30   | 01 May 2024      |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 31   | 01 June 2024     |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 30   | 01 July 2024     |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 31   | 01 August 2024   |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 September 2024|                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8 | 17.01 | 0.0        | 0.0  | 85.79       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | false    |
    When Admin sets the business date to "01 April 2024"
    When Admin successfully undo Loan re-aging transaction
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024| 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024   |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024   |                  | 50.53           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024     |                  | 33.81           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024    |                  | 17.0            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024    |                  | 0.0             | 17.0          | 0.1      | 0.0  | 0.0       | 17.1  | 0.0   | 0.0        | 0.0  | 17.1        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.15     | 0.0  | 0.0       | 102.15 | 17.01 | 0.0        | 0.0  | 85.14       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
      | 15 March 2024    | Re-age           | 84.28  | 83.57     | 0.71     | 0.0  | 0.0       | 0.0          | true     |

    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024 | 6                    |
    Then Loan Repayment schedule preview has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 01 April 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 4  | 30   | 01 May 2024      |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 31   | 01 June 2024     |                  | 42.39           | 13.97         | 0.33     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 6  | 30   | 01 July 2024     |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 31   | 01 August 2024   |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 September 2024|                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    Then Loan Repayment schedule preview has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 0.0  | 0.0       | 102.8 | 17.01 | 0.0        | 0.0  | 85.79       |

    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4215 @AdvancedPaymentAllocation
  Scenario: Verify that Re-aging preview is forbidden on charged-off loan, interest bearing loan, Interest calculation: Default Behavior, Charge-off scenario (zero interest) - UC10
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                       | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024|           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024   |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024   |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024     |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024    |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024    |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024| 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024   |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024   |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024     |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024    |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024    |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
    When Admin sets the business date to "01 March 2024"
    And Admin does charge-off the loan on "01 March 2024"
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024| 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024   |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024   |                  | 50.04           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024     |                  | 33.03           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024    |                  | 16.02           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024    |                  | 0.0             | 16.02         | 0.0      | 0.0  | 0.0       | 16.02 | 0.0   | 0.0        | 0.0  | 16.02       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 0.0  | 0.0       | 101.07 | 17.01 | 0.0        | 0.0  | 84.06       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
      | 01 March 2024    | Accrual          | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    |
      | 01 March 2024    | Charge-off       | 84.06  | 83.57     | 0.49     | 0.0  | 0.0       | 0.0          | false    |
    When Admin sets the business date to "15 March 2024"
    Then Admin fails to create a Loan re-aging preview with the following data because loan was charged-off:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024 | 6                    |

    When Loan Pay-off is made on "01 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4216 @AdvancedPaymentAllocation
  Scenario: Verify that Re-aging preview is forbidden on contract terminated loan, interest bearing loan, Interest calculation: Default Behavior - UC10.2
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_CONTRACT_TERMINATION | 01 January 2024   | 100            | 7                       | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024|           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024   |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024   |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024     |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024    |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024    |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024| 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024   |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024   |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024     |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024    |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024    |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
    When Admin sets the business date to "1 March 2024"
    And Admin successfully terminates loan contract
    Then Loan Repayment schedule has 2 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 0.0             | 83.57         | 0.49     | 0.0  | 0.0       | 84.06 | 0.0   | 0.0        | 0.0  | 84.06       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 1.07     | 0.0  | 0.0       | 101.07 | 17.01 | 0.0        | 0.0  | 84.06       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type     | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2024  | Disbursement         | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    | false    |
      | 01 February 2024 | Repayment            | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    | false    |
      | 01 March 2024    | Accrual              | 1.07   | 0.0       | 1.07     | 0.0  | 0.0       | 0.0          | false    | false    |
      | 01 March 2024    | Contract Termination | 84.06  | 83.57     | 0.49     | 0.0  | 0.0       | 0.0          | false    | false    |
    When Admin sets the business date to "15 April 2024"
    Then Admin fails to create a Loan re-aging preview with the following data because loan was contract terminated:
      | frequencyNumber | frequencyType | startDate   | numberOfInstallments |
      | 1               | MONTHS        | 01 May 2024 | 6                    |

  @TestRailId:C4217 @AdvancedPaymentAllocation
  Scenario: Verify Re-aging preview on interest bearing loan - Interest calculation: Default Behavior - Fees and Interest Split after re-aging - UC12
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 100            | 7                       | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024|           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024   |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024   |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024     |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024    |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024    |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 0.0  | 0.0        | 0.0  | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024| 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024   |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024   |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024     |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024    |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024    |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 0.0  | 0.0       | 102.05 | 17.01 | 0.0        | 0.0  | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
    When Admin sets the business date to "15 February 2024"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 May 2024" due date and 10 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date            | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024 |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024| 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024   |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024   |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024     |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024    |                  | 16.9            | 16.81         | 0.2      | 10.0 | 0.0       | 27.01 | 0.0   | 0.0        | 0.0  | 27.01       |
      | 6  | 30   | 01 July 2024    |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.05     | 10.0 | 0.0       | 112.05 | 17.01 | 0.0        | 0.0  | 95.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance | Reverted |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        | false    |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        | false    |
    Then Loan Charges tab has the following data:
      | Name       | isPenalty | Payment due at       | Due as of   | Calculation type | Due  | Paid | Waived | Outstanding |
      | Snooze fee | false     | Specified due date   | 15 May 2024 | Flat             | 10.0 | 0.0  | 0.0    | 10.0        |
    When Admin sets the business date to "15 March 2024"
    When Admin creates a Loan re-aging preview by Loan external ID with the following data:
      | frequencyNumber | frequencyType | startDate     | numberOfInstallments |
      | 1               | MONTHS        | 01 April 2024 | 6                    |
    Then Loan Repayment schedule preview has 8 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    | 15 March 2024    | 83.57           | 0.0           | 0.0      | 0.0  | 0.0       | 0.0   | 0.0   | 0.0        | 0.0  | 0.0         |
      | 3  | 31   | 01 April 2024    |                  | 70.25           | 13.32         | 0.98     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 4  | 30   | 01 May 2024      |                  | 56.36           | 13.89         | 0.41     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 5  | 31   | 01 June 2024     |                  | 42.39           | 13.97         | 0.33     | 10.0 | 0.0       | 24.3  | 0.0   | 0.0        | 0.0  | 24.3        |
      | 6  | 30   | 01 July 2024     |                  | 28.34           | 14.05         | 0.25     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 7  | 31   | 01 August 2024   |                  | 14.21           | 14.13         | 0.17     | 0.0  | 0.0       | 14.3  | 0.0   | 0.0        | 0.0  | 14.3        |
      | 8  | 31   | 01 September 2024|                  | 0.0             | 14.21         | 0.08     | 0.0  | 0.0       | 14.29 | 0.0   | 0.0        | 0.0  | 14.29       |
    Then Loan Repayment schedule preview has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      | 100.0         | 2.8      | 10.0 | 0.0       | 112.8 | 17.01 | 0.0        | 0.0  | 95.79       |

    When Loan Pay-off is made on "15 March 2024"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
