@Product
Feature: LoanProduct

  @Scenario1 @TestRailId:C52
  Scenario: As a user I would like to fully repay the loan in time
    When Admin sets the business date to "12 December 2021"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "12 December 2021", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "12 December 2021" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "12 December 2021" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "30 December 2021"
    And Customer makes "AUTOPAY" repayment on "30 December 2021" with 1000 EUR transaction amount
    Then Repayment transaction is created with 1000 amount and "AUTOPAY" type
    Then Loan has 0 outstanding amount

  @Scenario2 @TestRailId:C53
  Scenario: As a user I would like to fully repay a loan which was disbursed 2 times
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "5 June 2022"
    And Admin successfully disburse the loan on "5 June 2022" with "500" EUR transaction amount
    Then Loan has 1500 outstanding amount
    When Admin sets the business date to "1 July 2022"
    And Customer makes "AUTOPAY" repayment on "1 July 2022" with 1500 EUR transaction amount
    Then Repayment transaction is created with 1500 amount and "AUTOPAY" type
    Then Loan has 0 outstanding amount

  @Scenario3 @TestRailId:C54
  Scenario: As a user I would like to fully repay a multi disbursed loan with 2 repayments
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "15 June 2022"
    And Customer makes "AUTOPAY" repayment on "15 June 2022" with 500 EUR transaction amount
    Then Repayment transaction is created with 500 amount and "AUTOPAY" type
    Then Loan has 500 outstanding amount
    When Admin runs the Increase Business Date by 1 day job
    And Admin successfully disburse the loan on "16 June 2022" with "500" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "15 July 2022"
    And Customer makes "AUTOPAY" repayment on "15 July 2022" with 1000 EUR transaction amount
    Then Loan has 0 outstanding amount

  @Scenario4 @TestRailId:C55
  Scenario: As a user I would like to multi disburse a loan which was previously fully paid
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "15 June 2022"
    And Customer makes "AUTOPAY" repayment on "15 June 2022" with 1000 EUR transaction amount
    Then Repayment transaction is created with 1000 amount and "AUTOPAY" type
    Then Loan has 0 outstanding amount
    When Admin runs the Increase Business Date by 1 day job
    And Admin successfully disburse the loan on "16 June 2022" with "500" EUR transaction amount
    Then Loan has 500 outstanding amount
    When Admin sets the business date to "15 July 2022"
    And Customer makes "AUTOPAY" repayment on "15 July 2022" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount

  @Scenario5 @TestRailId:C57
  Scenario: As a user I would like to fully repay a loan and check a repayment reversal with NSF fee
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "15 June 2022"
    And Customer makes "AUTOPAY" repayment on "15 June 2022" with 1000 EUR transaction amount
    Then Repayment transaction is created with 1000 amount and "AUTOPAY" type
    Then Loan has 0 outstanding amount
    When Customer makes a repayment undo on "15 June 2022"
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "18 June 2022"
    And Admin adds an NSF fee because of payment bounce with "18 June 2022" transaction date
    Then Loan has 1010 outstanding amount
    And Customer makes "AUTOPAY" repayment on "18 June 2022" with 1010 EUR transaction amount
    Then Loan has 0 outstanding amount

  @Scenario6 @TestRailId:C58
  Scenario: As a user I would like to repay the half amount of the loan and check a repayment reversal with NSF fee
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "15 June 2022"
    And Customer makes "AUTOPAY" repayment on "15 June 2022" with 500 EUR transaction amount
    Then Repayment transaction is created with 500 amount and "AUTOPAY" type
    Then Loan has 500 outstanding amount
    When Customer makes a repayment undo on "15 June 2022"
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "18 June 2022"
    And Admin adds an NSF fee because of payment bounce with "18 June 2022" transaction date
    Then Loan has 1010 outstanding amount
    And Customer makes "AUTOPAY" repayment on "18 June 2022" with 1010 EUR transaction amount
    Then Loan has 0 outstanding amount

  @Scenario7 @TestRailId:C59
  Scenario: As a user I would like to fully repay a loan then reverse the repayment + add an NSF fee after the 1 month period
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "1 July 2022"
    And Customer makes "AUTOPAY" repayment on "1 July 2022" with 1000 EUR transaction amount
    Then Repayment transaction is created with 1000 amount and "AUTOPAY" type
    Then Loan has 0 outstanding amount
    When Customer makes a repayment undo on "1 July 2022"
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "5 July 2022"
    And Admin adds an NSF fee because of payment bounce with "5 July 2022" transaction date
    Then Loan has 1010 outstanding amount
    And Customer makes "AUTOPAY" repayment on "5 July 2022" with 1010 EUR transaction amount
    Then Loan has 0 outstanding amount

  @Scenario8 @TestRailId:C60
  Scenario: As a user I would like to repay the half amount of the loan and do a refund
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "25 June 2022"
    And Customer makes "AUTOPAY" repayment on "25 June 2022" with 500 EUR transaction amount
    Then Repayment transaction is created with 500 amount and "AUTOPAY" type
    Then Loan has 500 outstanding amount
    When Admin sets the business date to "1 July 2022"
    When Refund happens on "1 July 2022" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount

  @Scenario9 @TestRailId:C61
  Scenario: As a user I would like to repay the half amount of the loan and do a refund + repayment reversal on the repayment
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    And Admin sets the business date to "25 June 2022"
    And Customer makes "AUTOPAY" repayment on "25 June 2022" with 500 EUR transaction amount
    Then Repayment transaction is created with 500 amount and "AUTOPAY" type
    Then Loan has 500 outstanding amount
    When Admin sets the business date to "1 July 2022"
    When Refund happens on "1 July 2022" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Customer makes a repayment undo on "25 June 2022"
    Then Loan has 500 outstanding amount

  @Scenario10 @TestRailId:C62
  Scenario: As a user I would like to repay the half amount of the loan and do a refund + repayment reversal + refund reversal
    When Admin sets the business date to "1 June 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 June 2022", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    And Admin successfully approves the loan on "1 June 2022" with "1000" amount and expected disbursement date on "1 July 2022"
    And Admin successfully disburse the loan on "1 June 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "25 June 2022"
    And Customer makes "AUTOPAY" repayment on "25 June 2022" with 500 EUR transaction amount
    Then Repayment transaction is created with 500 amount and "AUTOPAY" type
    Then Loan has 500 outstanding amount
    When Admin sets the business date to "1 July 2022"
    When Refund happens on "1 July 2022" with 500 EUR transaction amount
    Then Loan has 0 outstanding amount
    When Customer makes a repayment undo on "25 June 2022"
    Then Loan has 500 outstanding amount
    When Refund undo happens on "1 July 2022"
    Then Loan has 1000 outstanding amount

  @TestRailId:C3481
  Scenario: As a user I would like to verify Charge-Off reasons options in loan product template response
    When Admin sets the business date to "12 December 2021"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "12 December 2021", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    Then Loan Product Charge-Off reasons options from loan product template have 5 options, with the following data:
      | Charge-Off Reason Name | Description | Position | Is Active | Is Mandatory |
      | debit_card             |             | 0        | true      | false        |
      | credit_card            |             | 1        | true      | false        |
      | Fraud                  |             | 2        | true      | false        |
      | Delinquent             |             | 3        | true      | false        |
      | Other                  |             | 4        | true      | false        |

  @TestRailId:C3482
  Scenario: As a user I would like to verify Charge-Off reasons options in specific loan product response
    When Admin sets the business date to "12 December 2021"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "12 December 2021", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    Then Loan Product "LP1" Charge-Off reasons options from specific loan product have 5 options, with the following data:
      | Charge-Off Reason Name | Description | Position | Is Active | Is Mandatory |
      | debit_card             |             | 0        | true      | false        |
      | credit_card            |             | 1        | true      | false        |
      | Fraud                  |             | 2        | true      | false        |
      | Delinquent             |             | 3        | true      | false        |
      | Other                  |             | 4        | true      | false        |

  @TestRailId:C3587
  Scenario: As a user I would like to verify interestRecognitionOnDisbursementDate=false flag in loan product response
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "01 January 2025", with Principal: "1000", a loanTermFrequency: 1 months, and numberOfRepayments: 1
    Then Loan Product response contains interestRecognitionOnDisbursementDate flag with value "false"

  @TestRailId:C3588
  Scenario: As a user I would like to verify interestRecognitionOnDisbursementDate=true flag in loan product response
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECOGNITION_DISBURSEMENT_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2025   | 1000           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Product response contains interestRecognitionOnDisbursementDate flag with value "true"

  @TestRailId:C3780
  Scenario: As a user I would like to verify BuyDownFees enabled in loan product response
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES | 01 January 2025   | 1000           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Product response contains Buy Down Fees flag "true" with data:
      | buyDownFeeCalculationType | buyDownFeeStrategy    | buyDownFeeIncomeType |
      | Flat                      | Equal amortization    | Interest             |
    Then Loan Details response contains Buy Down Fees flag "true" and data:
      | buyDownFeeCalculationType | buyDownFeeStrategy    | buyDownFeeIncomeType |
      | Flat                      | Equal amortization    | Interest             |

  @TestRailId:C3781
  Scenario: As a user I would like to verify BuyDownFees disabled in loan product response
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_RECOGNITION_DISBURSEMENT_DAILY_EMI_360_30_ACCRUAL_ACTIVITY | 01 January 2025   | 1000           | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan Product response contains Buy Down Fees flag "false"
    Then Loan Details response contains Buy Down Fees flag "false"

  @TestRailId:C3884
  Scenario: As a user I would like to verify multi-disburse loan product with over-applied amount and expected tranches can be created
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with disbursements details and following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date |1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal |
      | LP2_PROGRESSIVE_ADV_PYMNT_INTEREST_RECALC_360_30_MULTIDISB_OVER_APPLIED_EXPECTED_TRANCHES | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2024                | 700.0                     | 02 January 2024                | 300.0                      |
    And Admin successfully approves the loan on "1 January 2024" with "1000" amount and expected disbursement date on "1 January 2024"
    And Admin successfully disburse the loan on "1 January 2024" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    When Admin sets the business date to "2 January 2024"
    And Admin successfully disburse the loan on "2 January 2024" with "300" EUR transaction amount
    And Admin adds capitalized income with "AUTOPAY" payment type to the loan on "02 January 2024" with "200" EUR transaction amount

