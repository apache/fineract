@LoanUpdateApprovedAmount
Feature: LoanUpdateApprovedAmount

  @TestRailId:C3858
  Scenario: Verify update approved amount for progressive loan - UC1
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                    | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_PMT_ALLOC_1 | 01 January 2025   | 1000            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    Then Update loan approved amount is forbidden with amount "0" due to min allowed amount
    When Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount

    When Loan Pay-off is made on "1 January 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3859
  Scenario: Verify update approved amount after undo disbursement for single disb progressive loan - UC3
    When Admin sets the business date to "1 January 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    And Admin successfully disburse the loan on "1 January 2025" with "100" EUR transaction amount
    Then Update loan approved amount with new amount "600" value
    When Admin successfully undo disbursal
    Then Admin fails to disburse the loan on "1 January 2025" with "700" EUR transaction amount due to exceed approved amount
    When Admin successfully disburse the loan on "01 January 2025" with "600" EUR transaction amount

    When Loan Pay-off is made on "1 January 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3860
  Scenario: Verify update approved amount with approved over applied amount for progressive multidisbursal loan with percentage overAppliedCalculationType - UC4
    When Admin sets the business date to "1 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_MULTIDISB_APPROVED_OVER_APPLIED_CAPITALIZED_INCOME | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 1500
    And Admin successfully disburse the loan on "1 January 2025" with "1100" EUR transaction amount
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 400
    Then Update loan approved amount is forbidden with amount "1600" due to exceed applied amount
    Then Update loan approved amount with new amount "1300" value
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 400
    And Admin successfully disburse the loan on "1 January 2025" with "400" EUR transaction amount

    When Loan Pay-off is made on "1 January 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3861
  Scenario: Verify update approved amount with approved over applied amount and capitalized income for progressive loan with percentage overAppliedCalculationType - UC8_1
    When Admin sets the business date to "1 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_APPROVED_OVER_APPLIED_PERCENTAGE_CAPITALIZED_INCOME | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"

    Then Loan has availableDisbursementAmountWithOverApplied field with value: 1500
    And Admin successfully disburse the loan on "1 January 2025" with "1100" EUR transaction amount
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 400
    Then Update loan approved amount is forbidden with amount "1600" due to exceed applied amount
    Then Update loan approved amount with new amount "1400" value
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 400
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "1 January 2025" with "400" EUR transaction amount

    When Loan Pay-off is made on "1 January 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3862
  Scenario: Verify update approved amount with capitalized income for progressive loan - UC8_2
    When Admin sets the business date to "1 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                      | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2025" with "900" amount and expected disbursement date on "1 January 2025"
    And Admin successfully disburse the loan on "1 January 2025" with "600" EUR transaction amount
    Then Update loan approved amount is forbidden with amount "500" due to higher principal amount on loan
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "1 January 2025" with "200" EUR transaction amount
    Then Update loan approved amount with new amount "1000" value
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "1 January 2025" with "200" EUR transaction amount

    When Loan Pay-off is made on "1 January 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3863
  Scenario: Verify update approved amount with capitalized income for progressive multidisbursal loan - UC8_3
    When Admin sets the business date to "1 January 2025"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_MULTIDISB_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2025" with "900" amount and expected disbursement date on "1 January 2025"
    And Admin successfully disburse the loan on "1 January 2025" with "600" EUR transaction amount
    Then Update loan approved amount is forbidden with amount "500" due to higher principal amount on loan
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "1 January 2025" with "200" EUR transaction amount
    Then Update loan approved amount with new amount "1000" value
    And Admin successfully disburse the loan on "1 January 2025" with "200" EUR transaction amount

    When Loan Pay-off is made on "1 January 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3864
  Scenario: Verify update approved amount before disbursement for single disb cumulative loan - UC5_1
    When Admin sets the business date to "1 January 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy                        |
      | LP1_INTEREST_FLAT | 1 January 2025    | 1000           | 0                      | DECLINING_BALANCE | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER |
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    Then Update loan approved amount with new amount "900" value
    And Admin successfully disburse the loan on "1 January 2025" with "100" EUR transaction amount

    When Loan Pay-off is made on "1 January 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3865
  Scenario: Verify update approved amount before disbursement for single disb progressive loan - UC5_2
    When Admin sets the business date to "1 January 2025"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    Then Update loan approved amount with new amount "900" value
    And Admin successfully disburse the loan on "1 January 2025" with "100" EUR transaction amount

    When Loan Pay-off is made on "1 January 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3866
  Scenario: Verify approved amount change for progressive multidisbursal loan that doesn't expect tranches - UC6
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                 | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "200" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2025  |           | 200.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2025 |           | 167.15          | 32.85         | 1.17     | 0.0  | 0.0       | 34.02 | 0.0  | 0.0        | 0.0  | 34.02       |
      | 2  | 28   | 01 March 2025    |           | 134.11          | 33.04         | 0.98     | 0.0  | 0.0       | 34.02 | 0.0  | 0.0        | 0.0  | 34.02       |
      | 3  | 31   | 01 April 2025    |           | 100.87          | 33.24         | 0.78     | 0.0  | 0.0       | 34.02 | 0.0  | 0.0        | 0.0  | 34.02       |
      | 4  | 30   | 01 May 2025      |           |  67.44          | 33.43         | 0.59     | 0.0  | 0.0       | 34.02 | 0.0  | 0.0        | 0.0  | 34.02       |
      | 5  | 31   | 01 June 2025     |           |  33.81          | 33.63         | 0.39     | 0.0  | 0.0       | 34.02 | 0.0  | 0.0        | 0.0  | 34.02       |
      | 6  | 30   | 01 July 2025     |           |   0.0           | 33.81         | 0.2      | 0.0  | 0.0       | 34.01 | 0.0  | 0.0        | 0.0  | 34.01       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 200.0         | 4.11     | 0.0  | 0.0       | 204.11 | 0.0  | 0.0        | 0.0  | 204.11      |
    Then Update loan approved amount with new amount "600" value
    When Admin successfully disburse the loan on "01 January 2025" with "400" EUR transaction amount
    Then Update loan approved amount is forbidden with amount "500" due to higher principal amount on loan

    When Loan Pay-off is made on "1 January 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3867
  Scenario: Verify approved amount change with lower value for progressive multidisbursal loan that expects two tranches - UC7_1
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with three expected disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal | 3rd_tranche_disb_expected_date | 3rd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2025   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2025                | 300.0                      | 02 January 2025                | 200.0                      | 03 January 2025                | 500.0                      |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 0
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          |                 | 300.0       |                      |
      | 02 January 2025          |                 | 200.0       |                      |
      | 03 January 2025          |                 | 500.0       |                      |
#    --- disbursement - 1 January, 2025  ---
    When Admin successfully disburse the loan on "01 January 2025" with "300" EUR transaction amount
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 0
    When Admin sets the business date to "03 January 2025"
    Then Update loan approved amount is forbidden with amount "800" due to higher principal amount on loan

    When Admin successfully disburse the loan on "02 January 2025" with "200" EUR transaction amount
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 0
    Then Admin fails to disburse the loan on "03 January 2025" with "700" EUR transaction amount due to exceed approved amount
    When Admin successfully disburse the loan on "03 January 2025" with "500" EUR transaction amount
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 0
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 300.0       |                      |
      | 02 January 2025          | 02 January 2025 | 200.0       |                      |
      | 03 January 2025          | 03 January 2025 | 500.0       |                      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 300.0   | 0.0       | 0.0      | 0.0  | 0.0       | 300.0        | false    | false    |
      | 02 January 2025  | Disbursement     | 200.0   | 0.0       | 0.0      | 0.0  | 0.0       | 500.0        | false    | false    |
      | 03 January 2025  | Disbursement     | 500.0   | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       | false    | false    |

    When Loan Pay-off is made on "3 January 2025"
    Then Loan's all installments have obligations met

  @TestRailId:C3868
  Scenario: Verify approved amount change with greater value for progressive multidisbursal loan that expects two tranches - UC7_2
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with three expected disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal | 3rd_tranche_disb_expected_date | 3rd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2025   | 1200           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2025                | 300.0                      | 02 January 2025                | 200.0                      | 03 January 2025                | 500.0                      |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 0
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          |                 | 300.0       |                      |
      | 02 January 2025          |                 | 200.0       |                      |
      | 03 January 2025          |                 | 500.0       |                      |
#    --- disbursement - 1 January, 2025  ---
    When Admin successfully disburse the loan on "01 January 2025" with "300" EUR transaction amount
    When Admin sets the business date to "03 January 2025"
    Then Update loan approved amount with new amount "1200" value
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 200

    When Admin successfully disburse the loan on "02 January 2025" with "300" EUR transaction amount
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 100
    Then Admin fails to disburse the loan on "03 January 2025" with "700" EUR transaction amount due to exceed approved amount
    When Admin successfully disburse the loan on "03 January 2025" with "600" EUR transaction amount
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 0
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2025          | 01 January 2025 | 300.0       |                      |
      | 02 January 2025          | 02 January 2025 | 300.0       |                      |
      | 03 January 2025          | 03 January 2025 | 600.0       |                      |
    And Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount  | Principal | Interest | Fees | Penalties | Loan Balance | Reverted | Replayed |
      | 01 January 2025  | Disbursement     | 300.0   | 0.0       | 0.0      | 0.0  | 0.0       | 300.0        | false    | false    |
      | 02 January 2025  | Disbursement     | 300.0   | 0.0       | 0.0      | 0.0  | 0.0       | 600.0        | false    | false    |
      | 03 January 2025  | Disbursement     | 600.0   | 0.0       | 0.0      | 0.0  | 0.0       | 1200.0       | false    | false    |

    When Loan Pay-off is made on "3 January 2025"
    Then Loan's all installments have obligations met