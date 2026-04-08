@LoanFeature
Feature: Loan - Part1

  @TestRailId:C16 @Smoke
  Scenario: Loan creation functionality in Fineract
    When Admin sets the business date to the actual date
    When Admin creates a client with random data
    When Admin creates a new Loan

  @TestRailId:C17
  Scenario: Loan creation functionality in Fineract
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24

  @TestRailId:C42
  Scenario: As a user I would like to see that the loan is not created if the loan submission date is after the business date
    When Admin sets the business date to "25 June 2022"
    When Admin creates a client with random data
    Then Admin fails to create a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24

  @TestRailId:C43
  Scenario: As a user I would like to see that the loan is created if the loan submission date is equal to business date
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24

  @TestRailId:C46
  Scenario: As a user I would like to see that the loan is approved at the business date
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "2 July 2022"

  @TestRailId:C30 @single
  Scenario: As a user I would like to see that the loan is cannot be approved with future approval date
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    Then Admin fails to approve the loan on "2 July 2022" with "5000" amount and expected disbursement date on "2 July 2022" because of wrong date

  @TestRailId:C47 @multi
  Scenario: As a user I would like to see that the loan can be disbursed at the business date
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "2 July 2022"
    When Admin successfully disburse the loan on "1 July 2022" with "5000" EUR transaction amount

  @TestRailId:C31
  Scenario: As a user I would like to see that the loan is cannot be disbursed with future disburse date
    When Admin sets the business date to "1 July 2022"
    When Admin creates a client with random data
    And Admin successfully creates a new customised Loan submitted on date: "1 July 2022", with Principal: "5000", a loanTermFrequency: 24 months, and numberOfRepayments: 24
    And Admin successfully approves the loan on "1 July 2022" with "5000" amount and expected disbursement date on "2 July 2022"
    Then Admin fails to disburse the loan on "2 July 2022" with "5000" EUR transaction amount because of wrong date

  @TestRailId:C64
  Scenario: As a user I would like to see that 50% over applied amount can be approved and disbursed on loan correctly
    When Admin sets the business date to "1 September 2022"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 September 2022", with Principal: "1000", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 September 2022" with "1500" amount and expected disbursement date on "1 September 2022"
    When Admin successfully disburse the loan on "1 September 2022" with "1500" EUR transaction amount

  @TestRailId:C65
  Scenario: As a user I would like to see that 50% over applied amount can be approved but more than 50% cannot be disbursed on loan
    When Admin sets the business date to "1 September 2022"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 September 2022", with Principal: "1000", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 September 2022" with "1500" amount and expected disbursement date on "1 September 2022"
    Then Admin fails to disburse the loan on "1 September 2022" with "1501" EUR transaction amount because of wrong amount

  @TestRailId:C66
  Scenario: As a user I would like to see that more than 50% over applied amount can not be approved on loan
    When Admin sets the business date to "1 September 2022"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 September 2022", with Principal: "1000", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    Then Admin fails to approve the loan on "1 September 2022" with "1501" amount and expected disbursement date on "1 September 2022" because of wrong amount

  @TestRailId:C2769
  Scenario: As a user I would like to see that more than 50% over applied amount in total can not be disbursed on loan
    When Admin sets the business date to "1 September 2022"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 September 2022", with Principal: "1000", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 September 2022" with "1500" amount and expected disbursement date on "1 September 2022"
    And Admin successfully disburse the loan on "1 September 2022" with "1400" EUR transaction amount
    Then Admin fails to disburse the loan on "1 September 2022" with "101" EUR transaction amount because of wrong amount

  @TestRailId:C3767
  Scenario: Verify disbursed amount exceeds approved over applied amount for progressive loan with percentage overAppliedCalculationType
    When Admin sets the business date to "1 September 2022"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 September 2022", with Principal: "1000", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 September 2022" with "1300" amount and expected disbursement date on "1 September 2022"
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 1500
    And Admin successfully disburse the loan on "1 September 2022" with "1200" EUR transaction amount
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 300
    Then Admin fails to disburse the loan on "1 September 2022" with "301" EUR transaction amount because of wrong amount

    When Loan Pay-off is made on "1 September 2022"
    Then Loan's all installments have obligations met

  @TestRailId:C3768
  Scenario: Verify approved amount exceeds approved over applied amount for progressive loan with flat overAppliedCalculationType
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                  | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_APPROVED_OVER_APPLIED_FLAT_CAPITALIZED_INCOME | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Admin fails to approve the loan on "1 January 2024" with "2001" amount and expected disbursement date on "1 January 2024" because of wrong amount

    And Admin successfully rejects the loan on "1 January 2024"
    Then Loan status will be "REJECTED"

  @TestRailId:C3769
  Scenario: Verify disbursed amount exceeds approved over applied amount for progressive loan with flat overAppliedCalculationType
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "1 January 2024" with "9000" amount and expected disbursement date on "1 January 2024"
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 11000
    And Admin successfully disburse the loan on "1 January 2024" with "9900" EUR transaction amount
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 1100
    Then Admin fails to disburse the loan on "1 January 2024" with "1200" EUR transaction amount because of wrong amount

    When Loan Pay-off is made on "1 January 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3895
  Scenario: Verify disbursed amount approved over applied amount for progressive loan that expects tranches with percentage overAppliedCalculationType - UC1
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with disbursement details and following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date |1st_tranche_disb_principal |
      | LP2_PROGRESSIVE_ADV_PYMNT_INTEREST_RECALC_360_30_MULTIDISB_OVER_APPLIED_EXPECTED_TRANCHES | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2024                | 1000.0                    |
    And Admin successfully approves the loan on "1 January 2024" with "1200" amount and expected disbursement date on "1 January 2024"
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 500
    Then Loan status will be "APPROVED"
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2024          |                 | 1000.0      |                      |
    When Admin sets the business date to "2 January 2024"
    And Admin successfully add disbursement detail to the loan on "5 January 2024" with 200 EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2024          |                 | 1000.0      |                      |
      | 05 January 2024          |                 | 200.0       | 1200.0               |
    And Admin checks available disbursement amount 0.0 EUR
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 300
    Then Admin fails to disburse the loan on "2 January 2024" with "1600" EUR transaction amount because of wrong amount
    And Admin successfully disburse the loan on "2 January 2024" with "1300" EUR transaction amount
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 0
    Then Loan status will be "ACTIVE"
    And Admin checks available disbursement amount 0.0 EUR
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2024          | 02 January 2024 | 1300.0      |                      |
      | 05 January 2024          |                 | 200.0       | 1200.0               |

    When Loan Pay-off is made on "2 January 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3896
  Scenario: Verify disbursed amount approved over applied amount for progressive loan that expects tranches with percentage overAppliedCalculationType - UC2
    When Admin sets the business date to "1 January 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with disbursement details and following data:
      | LoanProduct                                                                               | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date |1st_tranche_disb_principal |
      | LP2_PROGRESSIVE_ADV_PYMNT_INTEREST_RECALC_360_30_MULTIDISB_OVER_APPLIED_EXPECTED_TRANCHES | 01 January 2024   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2024                | 1000.0                    |
    And Admin successfully approves the loan on "1 January 2024" with "1200" amount and expected disbursement date on "1 January 2024"
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 500
    Then Loan status will be "APPROVED"
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2024          |                 | 1000.0      |                      |
    When Admin sets the business date to "2 January 2024"
    And Admin successfully add disbursement detail to the loan on "5 January 2024" with 200 EUR transaction amount
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2024          |                 | 1000.0      |                      |
      | 05 January 2024          |                 | 200.0       | 1200.0               |
    And Admin checks available disbursement amount 0.0 EUR
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 300
    Then Admin fails to disburse the loan on "2 January 2024" with "1600" EUR transaction amount because of wrong amount
    And Admin successfully disburse the loan on "2 January 2024" with "1100" EUR transaction amount
    Then Loan status will be "ACTIVE"
    And Admin checks available disbursement amount 100.0 EUR
    Then Loan has availableDisbursementAmountWithOverApplied field with value: 200
    Then Loan Tranche Details tab has the following data:
      | Expected Disbursement On | Disbursed On    | Principal   | Net Disbursal Amount |
      | 01 January 2024          | 02 January 2024 | 1100.0      |                      |
      | 05 January 2024          |                 | 200.0       | 1200.0               |

    When Loan Pay-off is made on "2 January 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C67
  Scenario: As admin I would like to check that amounts are distributed equally in loan repayment schedule
    When Admin sets the business date to "1 September 2022"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 September 2022", with Principal: "1000", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 September 2022" with "1000" amount and expected disbursement date on "1 September 2022"
    Then Amounts are distributed equally in loan repayment schedule in case of total amount 1000
    When Admin successfully disburse the loan on "1 September 2022" with "900" EUR transaction amount
    Then Amounts are distributed equally in loan repayment schedule in case of total amount 900

  @TestRailId:C68
  Scenario: As admin I would like to be sure that approval of on loan can be undone
    When Admin sets the business date to "1 September 2022"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 September 2022", with Principal: "1000", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 September 2022" with "1000" amount and expected disbursement date on "1 September 2022"
    Then Admin can successfully undone the loan approval

  @TestRailId:C69
  Scenario: As admin I would like to be sure that disbursal of on loan can be undone
    When Admin sets the business date to "1 September 2022"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 September 2022", with Principal: "1000", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 September 2022" with "1000" amount and expected disbursement date on "1 September 2022"
    When Admin successfully disburse the loan on "1 September 2022" with "1000" EUR transaction amount
    Then Admin can successfully undone the loan disbursal
    Then Admin can successfully undone the loan approval
    And Admin successfully approves the loan on "1 September 2022" with "1000" amount and expected disbursement date on "1 September 2022"

  @TestRailId:C70
  Scenario: As admin I would like to be sure that submitted on date can be edited on loan
    When Admin sets the business date to "1 September 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 September 2022"
    Then Admin can successfully modify the loan and changes the submitted on date to "31 August 2022"

  @TestRailId:C2454 @fraud
  Scenario: As admin I would like to set Fraud flag to a loan
    When Admin sets the business date to "1 September 2022"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 September 2022", with Principal: "1000", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 September 2022" with "1000" amount and expected disbursement date on "1 September 2022"
    When Admin successfully disburse the loan on "1 September 2022" with "1000" EUR transaction amount
    Then Admin can successfully set Fraud flag to the loan

  @TestRailId:C2455 @fraud
  Scenario: As admin I would like to unset Fraud flag to a loan
    When Admin sets the business date to "1 September 2022"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "1 September 2022", with Principal: "1000", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "1 September 2022" with "1000" amount and expected disbursement date on "1 September 2022"
    When Admin successfully disburse the loan on "1 September 2022" with "1000" EUR transaction amount
    Then Admin can successfully set Fraud flag to the loan
    Then Admin can successfully unset Fraud flag to the loan


  @TestRailId:C2456 @fraud
  Scenario: As admin I would like to try to add fraud flag on a not active loan
    When Admin sets the business date to "25 October 2022"
    When Admin creates a client with random data
    When Admin successfully creates a new customised Loan submitted on date: "25 October 2022", with Principal: "1000", a loanTermFrequency: 3 months, and numberOfRepayments: 3
    And Admin successfully approves the loan on "25 October 2022" with "1000" amount and expected disbursement date on "25 October 2022"
    Then Admin can successfully unset Fraud flag to the loan

  @TestRailId:C2473 @idempotency
  Scenario: As admin I would like to verify that idempotency APIs can be called with the Idempotency-Key header
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "15 November 2022"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "15 November 2022" with 200 EUR transaction amount and self-generated Idempotency key
    Then Loan has 1 "DISBURSEMENT" transactions on Transactions tab
    Then Loan has 1 "REPAYMENT" transactions on Transactions tab

  @TestRailId:C2474 @idempotency
  Scenario: As admin I would like to verify that idempotency APIs can be called without the Idempotency-Key header
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "15 November 2022"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "15 November 2022" with 200 EUR transaction amount and system-generated Idempotency key
    Then Loan has 1 "DISBURSEMENT" transactions on Transactions tab
    Then Loan has 1 "REPAYMENT" transactions on Transactions tab

  @TestRailId:C2475 @idempotency
  Scenario: As admin I would like to verify that idempotency applies correctly in a happy path scenario in case of REPAYMENT transaction
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "15 November 2022"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "15 November 2022" with 200 EUR transaction amount and self-generated Idempotency key
    And Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "15 November 2022" with 300 EUR transaction amount with the same Idempotency key as previous transaction
    Then Transaction response has boolean value in header "x-served-from-cache": "true"
    Then Transaction response has 200 EUR value for transaction amount
    Then Transaction response has the correct clientId and the loanId of the first transaction
    Then Loan has 1 "REPAYMENT" transactions on Transactions tab

  @TestRailId:C2476 @idempotency
  Scenario: As admin I would like to verify that idempotency applies correctly in a happy path scenario in case of GOODWILL_CREDIT transaction
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "15 November 2022"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "15 November 2022" with 1000 EUR transaction amount and system-generated Idempotency key
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "15 November 2022" with 200 EUR transaction amount and self-generated Idempotency key
    And Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "15 November 2022" with 300 EUR transaction amount with the same Idempotency key as previous transaction
    Then Transaction response has boolean value in header "x-served-from-cache": "true"
    Then Transaction response has 200 EUR value for transaction amount
    Then Transaction response has the correct clientId and the loanId of the first transaction
    Then Loan has 1 "GOODWILL_CREDIT" transactions on Transactions tab

  @TestRailId:C2477 @idempotency
  Scenario: As admin I would like to verify that idempotency applies correctly in a happy path scenario in case of PAYOUT_REFUND transaction
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "15 November 2022"
    When Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 November 2022" with 200 EUR transaction amount and self-generated Idempotency key
    And Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 November 2022" with 300 EUR transaction amount with the same Idempotency key as previous transaction
    Then Transaction response has boolean value in header "x-served-from-cache": "true"
    Then Transaction response has 200 EUR value for transaction amount
    Then Transaction response has the correct clientId and the loanId of the first transaction
    Then Loan has 1 "PAYOUT_REFUND" transactions on Transactions tab

  @TestRailId:C2478 @idempotency
  Scenario: As admin I would like to verify that idempotency applies correctly in a happy path scenario in case of MERCHANT_ISSUED_REFUND transaction
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "15 November 2022"
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 November 2022" with 200 EUR transaction amount and self-generated Idempotency key
    And Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 November 2022" with 300 EUR transaction amount with the same Idempotency key as previous transaction
    Then Transaction response has boolean value in header "x-served-from-cache": "true"
    Then Transaction response has 200 EUR value for transaction amount
    Then Transaction response has the correct clientId and the loanId of the first transaction
    Then Loan has 1 "MERCHANT_ISSUED_REFUND" transactions on Transactions tab

  @TestRailId:C2482 @idempotency
  Scenario: As admin I would like to verify that idempotency applies correctly in case of client calls the same idempotency key on a second loan
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin crates a second default loan with date: "1 November 2022"
    And Admin successfully approves the second loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the second loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "15 November 2022"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "15 November 2022" with 200 EUR transaction amount and self-generated Idempotency key
    And Customer makes "REPAYMENT" transaction on the second loan with "AUTOPAY" payment type on "15 November 2022" with 300 EUR transaction amount with the same Idempotency key as previous transaction
    Then Transaction response has boolean value in header "x-served-from-cache": "true"
    Then Transaction response has 200 EUR value for transaction amount
    Then Transaction response has the correct clientId and the loanId of the first transaction
    Then Loan has 1 "REPAYMENT" transactions on Transactions tab
    Then Second loan has 0 "REPAYMENT" transactions on Transactions tab

#  TODO unskip and check when PS-1106 is done
  @Skip @TestRailId:C2483 @idempotency
  Scenario: As admin I would like to verify that idempotency applies correctly in case of a second client calls the same idempotency key on a second loan
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin creates a second client with random data
    When Admin crates a second default loan for the second client with date: "1 November 2022"
    And Admin successfully approves the second loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the second loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "15 November 2022"
    When Customer makes "REPAYMENT" transaction with "AUTOPAY" payment type on "15 November 2022" with 200 EUR transaction amount and self-generated Idempotency key
    And Customer makes "REPAYMENT" transaction on the second loan with "AUTOPAY" payment type on "15 November 2022" with 300 EUR transaction amount with the same Idempotency key as previous transaction
    Then Transaction response has boolean value in header "x-served-from-cache": "true"
    Then Transaction response has 300 EUR value for transaction amount
    Then Transaction response has the clientId for the second client and the loanId of the second transaction
    Then Loan has 1 "REPAYMENT" transactions on Transactions tab
    Then Second loan has 1 "REPAYMENT" transactions on Transactions tab

  @TestRailId:C2479
  Scenario: As admin I would like to be sure that goodwill credit transaction is working properly
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "15 November 2022"
    And Customer makes "AUTOPAY" repayment on "15 November 2022" with 1000 EUR transaction amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "15 November 2022" with 200 EUR transaction amount and self-generated Idempotency key
    Then Loan has 1 "GOODWILL_CREDIT" transactions on Transactions tab

  @TestRailId:C2480
  Scenario: As admin I would like to be sure that payout refund transaction is working properly
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "15 November 2022"
    And Customer makes "AUTOPAY" repayment on "15 November 2022" with 1000 EUR transaction amount
    When Customer makes "PAYOUT_REFUND" transaction with "AUTOPAY" payment type on "15 November 2022" with 200 EUR transaction amount and self-generated Idempotency key
    Then Loan has 1 "PAYOUT_REFUND" transactions on Transactions tab

  @TestRailId:C2481
  Scenario: As admin I would like to be sure that  merchant issued refund transaction is working properly
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "15 November 2022"
    And Customer makes "AUTOPAY" repayment on "15 November 2022" with 1000 EUR transaction amount
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "15 November 2022" with 200 EUR transaction amount and self-generated Idempotency key
    Then Loan has 1 "MERCHANT_ISSUED_REFUND" transactions on Transactions tab

  @TestRailId:C2488
  Scenario: As admin I would like to be sure that no multiple status change event got raised during transaction replaying
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    Then Loan status has changed to "Approved"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    Then Loan status has changed to "Active"
    When Admin sets the business date to "2 November 2022"
    And Customer makes "AUTOPAY" repayment on "2 November 2022" with 500 EUR transaction amount
    When Admin sets the business date to "3 November 2022"
    And Customer makes "AUTOPAY" repayment on "3 November 2022" with 100 EUR transaction amount
    When Admin sets the business date to "4 November 2022"
    And Customer makes "AUTOPAY" repayment on "4 November 2022" with 600 EUR transaction amount
    Then Loan status has changed to "Overpaid"
    When Customer undo "2"th repayment on "4 November 2022"
    Then No new event with type "LoanStatusChangedEvent" has been raised for the loan
    When Customer undo "1"th repayment on "4 November 2022"
    Then Loan status has changed to "Active"

  @TestRailId:C2489
  Scenario: As admin I would like to charge-off a loan and be sure the event was triggered
    When Admin sets the business date to "1 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 November 2022"
    And Admin successfully approves the loan on "1 November 2022" with "1000" amount and expected disbursement date on "1 November 2022"
    When Admin successfully disburse the loan on "1 November 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "2 November 2022"
    And Customer makes "AUTOPAY" repayment on "2 November 2022" with 500 EUR transaction amount
    When Admin sets the business date to "3 November 2022"
    And Admin does charge-off the loan on "3 November 2022"
    Then Loan marked as charged-off on "03 November 2022"

  @TestRailId:C2491
  Scenario: As a user I would like to do multiple repayment, overpay the loan and reverse-replaying transactions and check outstanding balance
    When Admin sets the business date to "01 November 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 November 2022"
    And Admin successfully approves the loan on "01 November 2022" with "1000" amount and expected disbursement date on "01 November 2022"
    When Admin successfully disburse the loan on "01 November 2022" with "1000" EUR transaction amount
    Then Loan has 1000 outstanding amount
    When Admin sets the business date to "02 November 2022"
    And Customer makes "AUTOPAY" repayment on "02 November 2022" with 500 EUR transaction amount
    Then Loan Transactions tab has a transaction with date: "02 November 2022", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Repayment        | 500.0  | 500.0     | 0.0      | 0.0  | 0.0       | 500.0        |
    Then Loan has 500 outstanding amount
    When Admin sets the business date to "03 November 2022"
    And Customer makes "AUTOPAY" repayment on "03 November 2022" with 10 EUR transaction amount
    Then Loan Transactions tab has a transaction with date: "03 November 2022", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Repayment        | 10.0   | 10.0      | 0.0      | 0.0  | 0.0       | 490.0        |
    Then Loan has 490 outstanding amount
    When Admin sets the business date to "04 November 2022"
    And Customer makes "AUTOPAY" repayment on "04 November 2022" with 400 EUR transaction amount
    Then Loan Transactions tab has a transaction with date: "04 November 2022", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Repayment        | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 90.0         |
    Then Loan has 90 outstanding amount
    When Admin sets the business date to "05 November 2022"
    And Customer makes "AUTOPAY" repayment on "05 November 2022" with 390 EUR transaction amount
    Then Loan Transactions tab has a transaction with date: "05 November 2022", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Repayment        | 390.0  | 90.0      | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan has 300 overpaid amount
    When Customer undo "2"th repayment on "04 November 2022"
    Then In Loan Transactions the "3"th Transaction has Transaction type="Repayment" and is reverted
    Then Loan Transactions tab has a transaction with date: "04 November 2022", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Repayment        | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 100.0        |
    Then Loan Transactions tab has a transaction with date: "05 November 2022", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Repayment        | 390.0  | 100.0     | 0.0      | 0.0  | 0.0       | 0.0          |
    Then Loan has 290 overpaid amount
    When Customer undo "1"th repayment on "04 November 2022"
    Then In Loan Transactions the "2"th Transaction has Transaction type="Repayment" and is reverted
    Then In Loan Transactions the "3"th Transaction has Transaction type="Repayment" and is reverted
    Then Loan Transactions tab has a transaction with date: "04 November 2022", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Repayment        | 400.0  | 400.0     | 0.0      | 0.0  | 0.0       | 600.0        |
    Then Loan Transactions tab has a transaction with date: "05 November 2022", and with the following data:
      | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | Repayment        | 390.0  | 390.0     | 0.0      | 0.0  | 0.0       | 210.0        |
    Then Loan has 210 outstanding amount
    And Customer makes "AUTOPAY" repayment on "02 November 2022" with 500 EUR transaction amount
    Then Loan has 290 overpaid amount

  @TestRailId:C2502
  Scenario: Verify that Loan status goes from active to overpaid in case of Goodwill credit transaction when transaction amount is greater than balance
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "3 January 2023"
    And Customer makes "AUTOPAY" repayment on "3 January 2023" with 450 EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 250 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 300 outstanding amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "5 January 2023" with 400 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 100 overpaid amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 450.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 450.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name             | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable         |       | 300.0  |
      | LIABILITY | l1           | Overpayment account      |       | 100.0  |
      | EXPENSE   | 744003       | Goodwill Expense Account | 400.0 |        |

  @TestRailId:C2503
  Scenario: Verify that Loan status goes from active to overpaid in case of Backdated 3rd repayment when transaction amount is greater than balance
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "3 January 2023"
    And Customer makes "AUTOPAY" repayment on "3 January 2023" with 450 EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 250 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 300 outstanding amount
    And Customer makes "AUTOPAY" repayment on "2 January 2023" with 400 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 100 overpaid amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "02 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 400.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 400.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 450.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 450.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 150.0  |
      | LIABILITY | l1           | Overpayment account       |       | 100.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |


  @TestRailId:C2504
  Scenario: Verify that Loan status goes from overpaid to active in case of Chargeback transaction when transaction amount is greater than overpaid amount
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "3 January 2023"
    And Customer makes "AUTOPAY" repayment on "3 January 2023" with 450 EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 450 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 300 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 200 overpaid amount
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 300 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 100 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 450.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 450.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 450.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 450.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | LIABILITY | l1           | Overpayment account       |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 300.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 100.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 300.0  |
      | LIABILITY | l1           | Overpayment account       | 200.0 |        |

  @TestRailId:C2506
  Scenario: Verify that Loan status goes from overpaid to active in case of 1st repayment is undone
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "3 January 2023"
    And Customer makes "AUTOPAY" repayment on "3 January 2023" with 450 EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 450 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 300 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 200 overpaid amount
    When Customer undo "1"th repayment on "3 January 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 250 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 450.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 450.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 450.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 450.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 450.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 450.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 300.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 300.0 |        |

  @TestRailId:C2507
  Scenario: Verify that Loan status goes from active to closed in case of Goodwill credit transaction when transaction amount equals balance
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "3 January 2023"
    And Customer makes "AUTOPAY" repayment on "3 January 2023" with 450 EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 250 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 300 outstanding amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "5 January 2023" with 300 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 450.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 450.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |
    Then Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type    | Account code | Account name             | Debit | Credit |
      | ASSET   | 112601       | Loans Receivable         |       | 300.0  |
      | EXPENSE | 744003       | Goodwill Expense Account | 300.0 |        |

  @TestRailId:C2508
  Scenario: Verify that Loan status goes from active to closed in case of Backdated 3rd repayment when transaction amount equals balance
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "3 January 2023"
    And Customer makes "AUTOPAY" repayment on "3 January 2023" with 450 EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 250 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 300 outstanding amount
    And Customer makes "AUTOPAY" repayment on "2 January 2023" with 300 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "02 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 300.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 300.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 450.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 450.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 250.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 250.0 |        |

  @TestRailId:C2509
  Scenario: Verify that Loan status goes from closed to overpaid in case of Goodwill credit transaction
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "3 January 2023"
    And Customer makes "AUTOPAY" repayment on "3 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 300 EUR transaction amount
    When Admin sets the business date to "7 January 2023"
    And Customer makes "AUTOPAY" repayment on "7 January 2023" with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "7 January 2023" with 100 EUR transaction amount and system-generated Idempotency key
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 100 overpaid amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 300.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 300.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "07 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 200.0 |        |
    Then Loan Transactions tab has a "GOODWILL_CREDIT" transaction with date "07 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name             | Debit | Credit |
      | LIABILITY | l1           | Overpayment account      |       | 100.0  |
      | EXPENSE   | 744003       | Goodwill Expense Account | 100.0 |        |


  @TestRailId:C2510
  Scenario: Verify that Loan status goes from overpaid to closed in case of Chargeback transaction when transaction amount equals overpaid amount
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "3 January 2023"
    And Customer makes "AUTOPAY" repayment on "3 January 2023" with 450 EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 450 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 300 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 200 overpaid amount
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 450.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 450.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 450.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 450.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 100.0  |
      | LIABILITY | l1           | Overpayment account       |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 300.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | LIABILITY | l1           | Overpayment account       | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |


  @TestRailId:C2512
  Scenario: Verify that Loan status goes from overpaid to closed in case of 1st repayment is undone
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "3 January 2023"
    And Customer makes "AUTOPAY" repayment on "3 January 2023" with 200 EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 700 EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 300 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Loan has 0 outstanding amount
    Then Loan has 200 overpaid amount
    When Customer undo "1"th repayment on "3 January 2023"
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 200.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 700.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 700.0 |        |
      | ASSET     | 112601       | Loans Receivable          |       | 300.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 300.0 |        |

  @TestRailId:C2513
  Scenario: Verify that Loan status goes from closed to active in case of Chargeback transaction
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "3 January 2023"
    And Customer makes "AUTOPAY" repayment on "3 January 2023" with 500 EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 300 EUR transaction amount
    When Admin sets the business date to "7 January 2023"
    And Customer makes "AUTOPAY" repayment on "7 January 2023" with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 200 EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Loan has 200 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 500.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 500.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 300.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 300.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "07 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 200.0 |        |
    Then Loan Transactions tab has a "CHARGEBACK" transaction with date "07 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |

  @TestRailId:C2514
  Scenario: Verify that Loan status goes from closed to active in case of 1st repayment is undone
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "3 January 2023"
    And Customer makes "AUTOPAY" repayment on "3 January 2023" with 200 EUR transaction amount
    When Admin sets the business date to "5 January 2023"
    And Customer makes "AUTOPAY" repayment on "5 January 2023" with 600 EUR transaction amount
    When Admin sets the business date to "7 January 2023"
    And Customer makes "AUTOPAY" repayment on "7 January 2023" with 200 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    When Customer undo "1"th repayment on "3 January 2023"
    Then Loan status will be "ACTIVE"
    Then Loan has 200 outstanding amount
    Then Loan Transactions tab has a "DISBURSEMENT" transaction with date "01 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          | 1000.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |        | 1000.0 |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "03 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 200.0 |        |
      | ASSET     | 112601       | Loans Receivable          | 200.0 |        |
      | LIABILITY | 145023       | Suspense/Clearing account |       | 200.0  |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "05 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 600.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 600.0 |        |
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "07 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit | Credit |
      | ASSET     | 112601       | Loans Receivable          |       | 200.0  |
      | LIABILITY | 145023       | Suspense/Clearing account | 200.0 |        |


  @TestRailId:C2539
  Scenario: Verify that loan overdue calculation is updated upon Goodwill credit transaction
    When Admin sets the business date to "1 January 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2023"
    And Admin successfully approves the loan on "1 January 2023" with "1000" amount and expected disbursement date on "1 January 2023"
    When Admin successfully disburse the loan on "1 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "1 March 2023"
    When Admin runs inline COB job for Loan
    Then Admin checks that delinquency range is: "RANGE_3" and has delinquentDate "2023-02-03"
    Then Loan status will be "ACTIVE"
    Then Loan has 1000 outstanding amount
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "1 March 2023" with 1000 EUR transaction amount and system-generated Idempotency key
    Then Admin checks that delinquency range is: "NO_DELINQUENCY" and has delinquentDate ""
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
