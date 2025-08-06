@LoanDelinquencyFeature
Feature: LoanDelinquency

  @TestRailId:C2963
  Scenario: Verify Loan delinquency pause API - PAUSE and RESUME by loanId
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "16 October 2023" and endDate: "30 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 16 October 2023 | 30 October 2023 |
    When Admin sets the business date to "20 October 2023"
    When Admin initiate a DELINQUENCY RESUME with startDate: "20 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 16 October 2023 | 30 October 2023 |
      | RESUME | 20 October 2023 |                 |

  @TestRailId:C2964
  Scenario: Verify Loan delinquency pause API - PAUSE and RESUME by loanExternalId
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    When Admin initiate a DELINQUENCY PAUSE by loanExternalId with startDate: "16 October 2023" and endDate: "30 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 16 October 2023 | 30 October 2023 |
    When Admin sets the business date to "20 October 2023"
    When Admin initiate a DELINQUENCY RESUME by loanExternalId with startDate: "20 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 16 October 2023 | 30 October 2023 |
      | RESUME | 20 October 2023 |                 |

  @TestRailId:C2965
  Scenario: Verify Loan delinquency pause API - PAUSE and RESUME actions supported only
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    Then Initiating a delinquency-action other than PAUSE or RESUME in action field results an error - startDate: "16 October 2023", endDate: "30 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "16 October 2023" and endDate: "30 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 16 October 2023 | 30 October 2023 |
    When Admin sets the business date to "20 October 2023"
    When Admin initiate a DELINQUENCY RESUME by loanExternalId with startDate: "20 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 16 October 2023 | 30 October 2023 |
      | RESUME | 20 October 2023 |                 |

  @TestRailId:C2966
  Scenario: Verify Loan delinquency pause API - PAUSE with start date on actual business date
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "16 October 2023" and endDate: "30 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 16 October 2023 | 30 October 2023 |

  @TestRailId:C2967
  Scenario: Verify Loan delinquency pause API - PAUSE with start date later than actual business date
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "25 October 2023" and endDate: "30 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 25 October 2023 | 30 October 2023 |

  @TestRailId:C2968
  Scenario: Verify Loan delinquency pause API - PAUSE with start date before than actual business date is possible
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "14 October 2023" and endDate: "30 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 14 October 2023 | 30 October 2023 |

  @TestRailId:C2969
  Scenario: Verify Loan delinquency pause API - PAUSE action on non-active loan result an error
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
#    pending approval
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"
    Then Initiating a DELINQUENCY PAUSE on a non-active loan results an error - startDate: "16 October 2023", endDate: "30 October 2023"
#    approved
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    Then Loan status will be "APPROVED"
    Then Initiating a DELINQUENCY PAUSE on a non-active loan results an error - startDate: "16 October 2023", endDate: "30 October 2023"
#    overpaid
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 1000 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Initiating a DELINQUENCY PAUSE on a non-active loan results an error - startDate: "16 October 2023", endDate: "30 October 2023"
#   closed
    And Admin makes Credit Balance Refund transaction on "01 October 2023" with 250 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    When Admin sets the business date to "16 October 2023"
    Then Initiating a DELINQUENCY PAUSE on a non-active loan results an error - startDate: "16 October 2023", endDate: "30 October 2023"

  @TestRailId:C2970
  Scenario: Verify Loan delinquency pause API - RESUME action on non-active loan result an error
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin initiate a DELINQUENCY PAUSE with startDate: "16 October 2023" and endDate: "30 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 16 October 2023 | 30 October 2023 |
#    overpaid
    When Admin sets the business date to "16 October 2023"
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 1000 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Initiating a DELINQUENCY RESUME on a non-active loan results an error - startDate: "16 October 2023"
#   closed
    And Admin makes Credit Balance Refund transaction on "16 October 2023" with 250 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Initiating a DELINQUENCY RESUME on a non-active loan results an error - startDate: "16 October 2023"

  @TestRailId:C2971
  Scenario: Verify Loan delinquency pause API - Overlapping PAUSE periods result an error
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "16 October 2023" and endDate: "30 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 16 October 2023 | 30 October 2023 |
    When Admin sets the business date to "20 October 2023"
    Then Overlapping PAUSE periods result an error - startDate: "20 October 2023", endDate: "30 October 2023"

  @TestRailId:C2972
  Scenario: Verify Loan delinquency pause API - RESUME without an active PAUSE period results an error
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    Then Initiating a DELINQUENCY RESUME without an active PAUSE period results an error - startDate: "01 October 2023"

  @TestRailId:C2973
  Scenario: Verify Loan delinquency pause API - RESUME with start date before than actual business date results an error
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "16 October 2023" and endDate: "30 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 16 October 2023 | 30 October 2023 |
    When Admin sets the business date to "20 October 2023"
    Then Initiating a DELINQUENCY RESUME with start date other than actual business date results an error - startDate: "01 October 2023"

  @TestRailId:C2974
  Scenario: Verify Loan delinquency pause API - RESUME with start date later than actual business date results an error
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "16 October 2023" and endDate: "30 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 16 October 2023 | 30 October 2023 |
    When Admin sets the business date to "20 October 2023"
    Then Initiating a DELINQUENCY RESUME with start date other than actual business date results an error - startDate: "21 October 2023"

  @TestRailId:C2975
  Scenario: Verify Loan delinquency pause API - RESUME with end date results an error
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "16 October 2023" and endDate: "30 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 16 October 2023 | 30 October 2023 |
    When Admin sets the business date to "20 October 2023"
    Then Initiating a DELINQUENCY RESUME with an endDate results an error - startDate: "20 October 2023", endDate: "30 October 2023"

  @TestRailId:C2992
  Scenario: Verify Loan level loan delinquency - loan goes into delinquency pause then will be resumed
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "05 October 2023"
    When Admin runs inline COB job for Loan
    Then Admin checks that delinquency range is: "RANGE_1" and has delinquentDate "2023-10-04"
    When Admin sets the business date to "17 November 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "17 November 2023" and endDate: "30 December 2023"
    Then Delinquency-actions have the following data:
      | action | startDate        | endDate          |
      | PAUSE  | 17 November 2023 | 30 December 2023 |
    When Admin runs inline COB job for Loan
    Then Admin checks that delinquency range is: "RANGE_30" and has delinquentDate "2023-10-04"
    When Admin sets the business date to "01 December 2023"
    When Admin initiate a DELINQUENCY RESUME with startDate: "01 December 2023"
    Then Delinquency-actions have the following data:
      | action | startDate        | endDate          |
      | PAUSE  | 17 November 2023 | 30 December 2023 |
      | RESUME | 01 December 2023 |                  |
    When Admin runs inline COB job for Loan
    Then Admin checks that delinquency range is: "RANGE_30" and has delinquentDate "2023-10-04"

  @TestRailId:C2979
  Scenario: Verify Installment level loan delinquency - loan goes into delinquency bucket
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 December 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 3       | RANGE_30 | 500.00 |
      | 4       | RANGE_60 | 500.00 |
    Then Installment level delinquency event has correct data

  @TestRailId:C2980
  Scenario: Verify Installment level loan delinquency - loan goes from one delinquency bucket to an other
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 November 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 1       | RANGE_1  | 250.00 |
      | 2       | RANGE_3  | 250.00 |
      | 3       | RANGE_30 | 500.00 |
    Then Installment level delinquency event has correct data
    When Admin sets the business date to "16 December 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 3       | RANGE_30 | 500.00 |
      | 4       | RANGE_60 | 500.00 |
    Then Installment level delinquency event has correct data

  @TestRailId:C2981
  Scenario: Verify Installment level loan delinquency - loan goes out from delinquency by late repayment
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 December 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 3       | RANGE_30 | 500.00 |
      | 4       | RANGE_60 | 500.00 |
    Then Installment level delinquency event has correct data
    When Admin sets the business date to "17 December 2023"
    And Customer makes "AUTOPAY" repayment on "17 December 2023" with 1000 EUR transaction amount
    Then Installment level delinquency event has correct data
    Then INSTALLMENT level delinquency is null

  @TestRailId:C2982
  Scenario: Verify Installment level loan delinquency - some of the installments go out from delinquency by late repayment
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "25 October 2023"
    When Admin runs inline COB job for Loan
    Then Installment level delinquency event has correct data
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 500.00 |
    When Admin sets the business date to "26 October 2023"
    And Customer makes "AUTOPAY" repayment on "26 October 2023" with 250 EUR transaction amount
    Then Installment level delinquency event has correct data
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |

  @TestRailId:C2983
  Scenario: Verify Installment level loan delinquency - loan goes out from delinquency by Goodwill credit transaction
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 December 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 3       | RANGE_30 | 500.00 |
      | 4       | RANGE_60 | 500.00 |
    Then Installment level delinquency event has correct data
    When Admin sets the business date to "17 December 2023"
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "17 December 2023" with 1000 EUR transaction amount and system-generated Idempotency key
    Then Installment level delinquency event has correct data
    Then INSTALLMENT level delinquency is null

  @TestRailId:C2984
  Scenario: Verify Installment level loan delinquency - some of the installments go out from delinquency by Goodwill credit transaction
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "25 October 2023"
    When Admin runs inline COB job for Loan
    Then Installment level delinquency event has correct data
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 500.00 |
    When Admin sets the business date to "26 October 2023"
    When Customer makes "GOODWILL_CREDIT" transaction with "AUTOPAY" payment type on "26 October 2023" with 250 EUR transaction amount and system-generated Idempotency key
    Then Installment level delinquency event has correct data
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |

  @TestRailId:C2985
  Scenario: Verify Installment level loan delinquency - loan with charges goes into delinquency bucket
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin adds "LOAN_NSF_FEE" due date charge with "05 October 2023" due date and 20 EUR transaction amount
    When Admin sets the business date to "20 October 2023"
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "20 October 2023" due date and 20 EUR transaction amount
    When Admin sets the business date to "16 December 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 3       | RANGE_30 | 520.00 |
      | 4       | RANGE_60 | 520.00 |
    Then Installment level delinquency event has correct data

  @TestRailId:C2987
  Scenario: Verify Installment level loan delinquency - loan goes into delinquency pause
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "30 October 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "16 November 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 1       | RANGE_1  | 250.00 |
      | 2       | RANGE_3  | 250.00 |
      | 3       | RANGE_30 | 500.00 |
    Then Installment level delinquency event has correct data
    When Admin sets the business date to "17 November 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "17 November 2023" and endDate: "30 November 2023"
    Then Delinquency-actions have the following data:
      | action | startDate        | endDate          |
      | PAUSE  | 17 November 2023 | 30 November 2023 |
    When Admin runs inline COB job for Loan
    Then Installment level delinquency event has correct data
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 1       | RANGE_1  | 250.00 |
      | 2       | RANGE_3  | 250.00 |
      | 3       | RANGE_30 | 500.00 |
    When Admin sets the business date to "30 November 2023"
    When Admin runs inline COB job for Loan
    Then Installment level delinquency event has correct data
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 1       | RANGE_1  | 250.00 |
      | 2       | RANGE_3  | 250.00 |
      | 3       | RANGE_30 | 500.00 |
    When Admin sets the business date to "20 December 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "01 January 2024"
    When Admin runs inline COB job for Loan
    Then Installment level delinquency event has correct data
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 3       | RANGE_30 | 500.00 |
      | 4       | RANGE_60 | 500.00 |

  @TestRailId:C2988
  Scenario: Verify Installment level loan delinquency - loan goes into delinquency pause then will be resumed
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "30 October 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "16 November 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 1       | RANGE_1  | 250.00 |
      | 2       | RANGE_3  | 250.00 |
      | 3       | RANGE_30 | 500.00 |
    Then Installment level delinquency event has correct data
    When Admin sets the business date to "17 November 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "17 November 2023" and endDate: "30 December 2023"
    Then Delinquency-actions have the following data:
      | action | startDate        | endDate          |
      | PAUSE  | 17 November 2023 | 30 December 2023 |
    When Admin runs inline COB job for Loan
    Then Installment level delinquency event has correct data
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 1       | RANGE_1  | 250.00 |
      | 2       | RANGE_3  | 250.00 |
      | 3       | RANGE_30 | 500.00 |
    When Admin sets the business date to "01 December 2023"
    When Admin initiate a DELINQUENCY RESUME with startDate: "01 December 2023"
    Then Delinquency-actions have the following data:
      | action | startDate        | endDate          |
      | PAUSE  | 17 November 2023 | 30 December 2023 |
      | RESUME | 01 December 2023 |                  |
    When Admin runs inline COB job for Loan
    Then Installment level delinquency event has correct data
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 1       | RANGE_1  | 250.00 |
      | 2       | RANGE_3  | 250.00 |
      | 3       | RANGE_30 | 500.00 |

  @TestRailId:C2990
  Scenario: Verify that a non-super user with CREATE_DELINQUENCY_ACTION permission can initiate a DELINQUENCY PAUSE
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    When Admin creates new user with "CREATE_DELINQUENCY_ACTION_USER" username, "CREATE_DELINQUENCY_ACTION_ROLE" role name and given permissions:
      | CREATE_DELINQUENCY_ACTION |
      | REPAYMENT_LOAN            |
    When Created user with CREATE_DELINQUENCY_ACTION permission initiate a DELINQUENCY PAUSE with startDate: "16 October 2023" and endDate: "30 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 16 October 2023 | 30 October 2023 |

  @TestRailId:C2991
  Scenario: Verify that a non-super user with no CREATE_DELINQUENCY_ACTION permission gets an error when initiate a DELINQUENCY PAUSE
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 October 2023"
    When Admin creates new user with "NO_CREATE_DELINQUENCY_ACTION_USER" username, "NO_CREATE_DELINQUENCY_ACTION_ROLE" role name and given permissions:
      | REPAYMENT_LOAN |
    Then Created user with no CREATE_DELINQUENCY_ACTION permission gets an error when initiate a DELINQUENCY PAUSE with startDate: "16 October 2023" and endDate: "30 October 2023"

  @TestRailId:C2999
  Scenario: Verify Loan delinquency pause E2E - full PAUSE period
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "02 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 250.0            | 04 October 2023 | 0              | 1           |
    Then INSTALLMENT level delinquency is null
    When Admin sets the business date to "04 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 250.0            | 04 October 2023 | 0              | 3           |
    Then INSTALLMENT level delinquency is null
    When Admin sets the business date to "05 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_1        | 250.0            | 04 October 2023 | 1              | 4           |
#   --- Grace period applied only on Loan level, not on installment level ---
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |
    When Admin sets the business date to "06 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "06 October 2023" and endDate: "30 October 2023"
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 06 October 2023  | 30 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 06 October 2023 | 30 October 2023 |
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_1        | 250.0            | 04 October 2023 | 2              | 5           |
#    --- Grace period applied only on Loan level, not on installment level ---
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |
    When Admin sets the business date to "30 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_1        | 500.0            | 04 October 2023 | 2              | 29          |
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 06 October 2023  | 30 October 2023 |
#    --- Grace period applied only on Loan level, not on installment level ---
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |
    When Admin sets the business date to "31 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_1        | 500.0            | 04 October 2023 | 3              | 30          |
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | false  | 06 October 2023  | 30 October 2023 |
#    --- Grace period applied only on Loan level, not on installment level ---
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |

  @TestRailId:C3000
  Scenario: Verify Loan delinquency pause E2E - PAUSE period with RESUME
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
#    --- Delinquency pause ---
    When Admin sets the business date to "15 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "15 October 2023" and endDate: "30 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 15 October 2023  | 30 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 15 October 2023 | 30 October 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_3        | 250.0            | 04 October 2023 | 11             | 14          |
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |
    Then Installment level delinquency event has correct data
#    --- Delinquency resume ---
    When Admin sets the business date to "25 October 2023"
    When Admin initiate a DELINQUENCY RESUME with startDate: "25 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 15 October 2023  | 25 October 2023 |
    When Admin sets the business date to "26 October 2023"
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | false  | 15 October 2023  | 25 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 15 October 2023 | 30 October 2023 |
      | RESUME | 25 October 2023 |                 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_3        | 500.0            | 04 October 2023 | 12             | 25          |
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |
    Then Installment level delinquency event has correct data

  @TestRailId:C3001
  Scenario: Verify Loan delinquency pause E2E - PAUSE period with RESUME and second PAUSE
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
#    --- Delinquency pause ---
    When Admin sets the business date to "15 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "15 October 2023" and endDate: "30 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 15 October 2023  | 30 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 15 October 2023 | 30 October 2023 |
    When Admin sets the business date to "16 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_3        | 250.0            | 04 October 2023 | 11             | 15          |
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |
    Then Installment level delinquency event has correct data
#    --- Delinquency resume ---
    When Admin sets the business date to "25 October 2023"
    When Admin initiate a DELINQUENCY RESUME with startDate: "25 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 15 October 2023  | 25 October 2023 |
    When Admin sets the business date to "26 October 2023"
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | false  | 15 October 2023  | 25 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 15 October 2023 | 30 October 2023 |
      | RESUME | 25 October 2023 |                 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_3        | 500.0            | 04 October 2023 | 12             | 25          |
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |
    Then Installment level delinquency event has correct data
#   --- Delinquency runs ---
    When Admin sets the business date to "13 November 2023"
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | false  | 15 October 2023  | 25 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 15 October 2023 | 30 October 2023 |
      | RESUME | 25 October 2023 |                 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_3        | 750.0            | 04 October 2023 | 30             | 43          |
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 1       | RANGE_1  | 250.00 |
      | 2       | RANGE_3  | 250.00 |
      | 3       | RANGE_30 | 250.00 |
#    --- Second delinquency pause ---
    When Admin sets the business date to "14 November 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "14 November 2023" and endDate: "30 November 2023"
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd   |
      | false  | 15 October 2023  | 25 October 2023  |
      | true   | 14 November 2023 | 30 November 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate        | endDate          |
      | PAUSE  | 15 October 2023  | 30 October 2023  |
      | RESUME | 25 October 2023  |                  |
      | PAUSE  | 14 November 2023 | 30 November 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_3        | 750.0            | 04 October 2023 | 31             | 44          |
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 1       | RANGE_1  | 250.00 |
      | 2       | RANGE_3  | 250.00 |
      | 3       | RANGE_30 | 250.00 |
    Then Installment level delinquency event has correct data
#    --- Second delinquency ends ---
    When Admin sets the business date to "30 November 2023"
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd   |
      | false  | 15 October 2023  | 25 October 2023  |
      | true   | 14 November 2023 | 30 November 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate        | endDate          |
      | PAUSE  | 15 October 2023  | 30 October 2023  |
      | RESUME | 25 October 2023  |                  |
      | PAUSE  | 14 November 2023 | 30 November 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_3       | 1000.0           | 04 October 2023 | 31             | 60          |
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 1       | RANGE_1  | 250.00 |
      | 2       | RANGE_3  | 250.00 |
      | 3       | RANGE_30 | 250.00 |
#    --- Delinquency runs again ---
    When Admin sets the business date to "01 December 2023"
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd   |
      | false  | 15 October 2023  | 25 October 2023  |
      | false  | 14 November 2023 | 30 November 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate        | endDate          |
      | PAUSE  | 15 October 2023  | 30 October 2023  |
      | RESUME | 25 October 2023  |                  |
      | PAUSE  | 14 November 2023 | 30 November 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_30       | 1000.0           | 04 October 2023 | 32             | 61          |
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 2       | RANGE_3  | 500.00 |
      | 3       | RANGE_30 | 250.00 |
    Then Installment level delinquency event has correct data

  @TestRailId:C3002
  Scenario: Verify Loan delinquency pause E2E - full repayment (late/due date) during PAUSE period
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
#    --- Delinquency pause ---
    When Admin sets the business date to "06 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "06 October 2023" and endDate: "30 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 06 October 2023  | 30 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 06 October 2023 | 30 October 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_1        | 250.0            | 04 October 2023 | 2              | 5           |
#    --- Grace period applied only on Loan level, not on installment level ---
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |
    Then Installment level delinquency event has correct data
#    --- Full repayment for late/due date installments ---
    When Admin sets the business date to "16 October 2023"
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 06 October 2023  | 30 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 06 October 2023 | 30 October 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_1        | 250.0            | 04 October 2023 | 2              | 15          |
#    --- Grace period applied only on Loan level, not on installment level ---
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 500 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 06 October 2023  | 30 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 06 October 2023 | 30 October 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | 0              | 0           |
    Then INSTALLMENT level delinquency is null

  @TestRailId:C3003
  Scenario: Verify Loan delinquency pause E2E - partial repayment during PAUSE period
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
#    --- Delinquency pause ---
    When Admin sets the business date to "06 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "06 October 2023" and endDate: "30 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 06 October 2023  | 30 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 06 October 2023 | 30 October 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_1        | 250.0            | 04 October 2023 | 2              | 5           |
#    --- Grace period applied only on Loan level, not on installment level ---
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |
    Then Installment level delinquency event has correct data
#    --- Full repayment for late/due date installments ---
    When Admin sets the business date to "16 October 2023"
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 06 October 2023  | 30 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 06 October 2023 | 30 October 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_1        | 250.0            | 04 October 2023 | 2              | 15          |
#    --- Grace period applied only on Loan level, not on installment level ---
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 150 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 06 October 2023  | 30 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 06 October 2023 | 30 October 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_1        | 100.0            | 04 October 2023 | 2              | 15          |
#    --- Grace period applied only on Loan level, not on installment level ---
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 100.00 |

  @TestRailId:C3004
  Scenario: Verify Loan delinquency pause E2E - full repayment (only late) during PAUSE period then RESUME
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
#    --- Delinquency pause ---
    When Admin sets the business date to "06 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "06 October 2023" and endDate: "30 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 06 October 2023  | 30 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 06 October 2023 | 30 October 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_1        | 250.0            | 04 October 2023 | 2              | 5           |
#    --- Grace period applied only on Loan level, not on installment level ---
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |
    Then Installment level delinquency event has correct data
#    --- Full repayment for late/due date installments ---
    When Admin sets the business date to "16 October 2023"
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 06 October 2023  | 30 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 06 October 2023 | 30 October 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_1        | 250.0            | 04 October 2023 | 2              | 15          |
#    --- Grace period applied only on Loan level, not on installment level ---
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |
    And Customer makes "AUTOPAY" repayment on "16 October 2023" with 250 EUR transaction amount
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 06 October 2023  | 30 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 06 October 2023 | 30 October 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | 0              | 0           |
    Then INSTALLMENT level delinquency is null
#   --- Delinquency resume ---
    When Admin sets the business date to "25 October 2023"
    When Admin initiate a DELINQUENCY RESUME with startDate: "25 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 06 October 2023  | 25 October 2023 |
    When Admin sets the business date to "26 October 2023"
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | false  | 06 October 2023  | 25 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 06 October 2023 | 30 October 2023 |
      | RESUME | 25 October 2023 |                 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 250.0            | 19 October 2023 | 0              | 10          |
    Then INSTALLMENT level delinquency is null
#   --- Delinquency runs ---
    When Admin sets the business date to "15 November 2023"
    When Admin runs inline COB job for Loan
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | false  | 06 October 2023  | 25 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 06 October 2023 | 30 October 2023 |
      | RESUME | 25 October 2023 |                 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_3        | 500.0            | 19 October 2023 | 8              | 30          |
#    --- Grace period applied only on Loan level, not on installment level ---
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |
    Then Installment level delinquency event has correct data

  @TestRailId:C3013
  Scenario: Verify that in case of resume on end/start date of continous pause periods first period ends automatically, second period ended by resume
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "25 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "25 October 2023" and endDate: "30 October 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 25 October 2023 | 30 October 2023 |
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 25 October 2023  | 30 October 2023 |
    When Admin sets the business date to "30 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "30 October 2023" and endDate: "15 November 2023"
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate          |
      | PAUSE  | 25 October 2023 | 30 October 2023  |
      | PAUSE  | 30 October 2023 | 15 November 2023 |
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd   |
      | true   | 25 October 2023  | 30 October 2023  |
      | true   | 30 October 2023  | 15 November 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 500.0            | 04 October 2023 | 21             | 29          |
    Then INSTALLMENT level delinquency is null
    When Admin initiate a DELINQUENCY RESUME with startDate: "30 October 2023"
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 500.0            | 04 October 2023 | 21             | 29          |
    Then INSTALLMENT level delinquency is null
    When Admin runs inline COB job for Loan
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate          |
      | PAUSE  | 25 October 2023 | 30 October 2023  |
      | PAUSE  | 30 October 2023 | 15 November 2023 |
      | RESUME | 30 October 2023 |                  |
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | true   | 25 October 2023  | 30 October 2023 |
      | true   | 30 October 2023  | 30 October 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_3        | 500.0            | 04 October 2023 | 21             | 29          |
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 500.00 |
    When Admin sets the business date to "31 October 2023"
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | false  | 25 October 2023  | 30 October 2023 |
      | false  | 30 October 2023  | 30 October 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_3        | 500.0            | 04 October 2023 | 22             | 30          |
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 500.00 |

  @TestRailId:C3014
  Scenario: Verify that creating a loan with Advanced payment allocation with product no Advanced payment allocation set results an error
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with Advanced payment allocation and with product no Advanced payment allocation set results an error:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1       | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |

  @TestRailId:C3015
  Scenario: Verify Backdated Pause Delinquency - Event Trigger: LoanDelinquencyRangeChangeBusinessEvent, LoanAccountDelinquencyPauseChangedBusinessEvent check
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "25 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_3        | 500.0            | 04 October 2023 | 21             | 24          |
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 500.00 |
    When Admin sets the business date to "27 October 2023"
#    event checks included in next steps
    When Admin initiate a DELINQUENCY PAUSE with startDate: "25 October 2023" and endDate: "15 November 2023"
    Then Admin checks that delinquency range is: "RANGE_3" and has delinquentDate "2023-10-04"
    Then Installment level delinquency event has correct data
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate          |
      | PAUSE  | 25 October 2023 | 15 November 2023 |
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd   |
      | true   | 25 October 2023  | 15 November 2023 |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_3        | 500.0            | 04 October 2023 | 21             | 26          |
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 500.00 |

  @TestRailId:C3016
  Scenario: Verify that for pause period calculations business date is being used instead of COB date
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "01 October 2023" with 250 EUR transaction amount
#    --- Delinquency pause ---
    When Admin sets the business date to "05 October 2023"
    When Admin initiate a DELINQUENCY PAUSE with startDate: "16 October 2023" and endDate: "25 October 2023"
    When Admin runs inline COB job for Loan
    When Admin sets the business date to "25 October 2023"
    When Admin runs inline COB job for Loan
#    --- Because of grace period 3 days delinguency won't start ---
    When Admin sets the business date to "26 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 250.0            | 19 October 2023 | 0              | 10          |
    Then INSTALLMENT level delinquency is null
#    -----------
    When Admin sets the business date to "27 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 250.0            | 19 October 2023 | 0              | 11          |
    Then INSTALLMENT level delinquency is null
#    -----------
    When Admin sets the business date to "28 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 250.0            | 19 October 2023 | 0              | 12          |
    Then INSTALLMENT level delinquency is null
#    --- After grace period ends delinquency starts ---
    When Admin sets the business date to "29 October 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_1        | 250.0            | 19 October 2023 | 1              | 13          |
#    --- Grace period applied only on Loan level, not on installment level ---
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range   | Amount |
      | 2       | RANGE_3 | 250.00 |
    Then Installment level delinquency event has correct data
    Then Loan Delinquency pause periods has the following data:
      | active | pausePeriodStart | pausePeriodEnd  |
      | false  | 16 October 2023  | 25 October 2023 |
    Then Delinquency-actions have the following data:
      | action | startDate       | endDate         |
      | PAUSE  | 16 October 2023 | 25 October 2023 |

  @TestRailId:C3018
  Scenario: Verify that if Global configuration: next-payment-due-date is set to: earliest-unpaid-date then in Loan details delinquent.nextPaymentDueDate will be the first unpaid installment date
    When Global config "next-payment-due-date" value set to "earliest-unpaid-date"
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "11 October 2023"
    Then Loan details delinquent.nextPaymentDueDate will be "01 October 2023"
    When Admin sets the business date to "21 October 2023"
    Then Loan details delinquent.nextPaymentDueDate will be "01 October 2023"
    When Global config "next-payment-due-date" value set to "earliest-unpaid-date"

  @TestRailId:C3019
  Scenario: Verify that if Global configuration: next-payment-due-date is set to: next-unpaid-due-date then in Loan details delinquent.nextPaymentDueDate will be the next unpaid installment date regardless of the status of previous installments
    When Global config "next-payment-due-date" value set to "next-unpaid-due-date"
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "11 October 2023"
    Then Loan details delinquent.nextPaymentDueDate will be "16 October 2023"
    When Admin sets the business date to "21 October 2023"
    Then Loan details delinquent.nextPaymentDueDate will be "31 October 2023"
    When Global config "next-payment-due-date" value set to "earliest-unpaid-date"

  @TestRailId:C3032
  Scenario: Verify that delinquencyRange field in LoanAccountDelinquencyRangeDataV1 is not null in case of delinquent Loan
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "01 December 2023"
    When Admin runs inline COB job for Loan
    Then LoanAccountDelinquencyRangeDataV1 has delinquencyRange field with value "RANGE_30"
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_30       | 1000.0           | 04 October 2023 | 58             | 61          |

  @TestRailId:C3035
  Scenario: Verify that delinquency is NOT applied after loan submitted and approved
    When Admin sets the business date to "30 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | 0              | 0           |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | 0              | 0           |

  @TestRailId:C3047
  Scenario: Verify that delinquent.lastRepaymentAmount is calculated correctly in case of auto downpayment
    When Admin sets the business date to "01 February 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 February 2024  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 February 2024" with "1000" amount and expected disbursement date on "01 February 2024"
    When Admin successfully disburse the loan on "01 February 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "01 April 2024"
    Then In Loan details delinquent.lastRepaymentAmount is 250 EUR with lastRepaymentDate "01 February 2024"

  @TestRailId:C3066 @AdvancedPaymentAllocation
  Scenario: Verify that on Loans in SUBMITTED_AND_PENDING_APPROVAL or APPROVED status delinquency is not applied
    When Admin sets the business date to "01 February 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 February 2024  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    When Admin sets the business date to "01 March 2024"
    When Admin runs inline COB job for Loan
    Then Admin checks that delinquency range is: "NO_DELINQUENCY" and has delinquentDate ""
    And Admin successfully approves the loan on "01 March 2024" with "1000" amount and expected disbursement date on "01 March 2024"
    Then Admin checks that delinquency range is: "NO_DELINQUENCY" and has delinquentDate ""

  @TestRailId:C3135
  Scenario: Verify that the delinquency is not applied on Loan with Rejected status
    When Admin sets the business date to "01 February 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 February 2024  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    When Admin sets the business date to "01 March 2024"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | 0              | 0           |
    And Admin successfully approves the loan on "01 March 2024" with "1000" amount and expected disbursement date on "01 March 2024"
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | 0              | 0           |
    When Admin sets the business date to "25 March 2024"
    And Admin can successfully undone the loan approval
    And Admin successfully rejects the loan on "25 March 2024"
    Then Loan status will be "REJECTED"
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | 0              | 0           |

  @TestRailId:C3136
  Scenario: Verify that the delinquency is not applied on Loan with Withdrawn status
    When Admin sets the business date to "01 February 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 February 2024  | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    When Admin sets the business date to "01 March 2024"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | 0              | 0           |
    And Admin successfully approves the loan on "01 March 2024" with "1000" amount and expected disbursement date on "01 March 2024"
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | 0              | 0           |
    When Admin sets the business date to "25 March 2024"
    And Admin can successfully undone the loan approval
    And Admin successfully withdrawn the loan on "25 March 2024"
    Then Loan status will be "WITHDRAWN"
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate | delinquentDays | pastDueDays |
      | NO_DELINQUENCY | 0.0              | null           | 0              | 0           |

  @TestRailId:C3137
  Scenario: Verify Installment level loan delinquency can be applied on loan account level in case of non-installment level delinquency loan product
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with installment level delinquency and with the following data:
      | LoanProduct                                                         | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "16 December 2023"
    When Admin runs inline COB job for Loan
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 3       | RANGE_30 | 500.00 |
      | 4       | RANGE_60 | 500.00 |
    Then Installment level delinquency event has correct data

  @TestRailId:C3930
  Scenario: Verify nextPaymentAmount value with repayment on first installment - progressive loan, no interest recalculation, zero interest rate - UC1
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 1 June 2024       | 1000             | 0                    | DECLINING_BALANCE | DAILY                   | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 2  | 31   | 01 August 2024    |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 31   | 01 September 2024 |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 30   | 01 October 2024   |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0.0      | 0.0  | 0.0       | 1000.0 | 0.0  | 0.0        | 0.0  | 1000.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 250.0             |

    When Admin sets the business date to "15 June 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 June 2024" with 50 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 50.0 | 50.0       | 0.0  | 200.0       |
      | 2  | 31   | 01 August 2024    |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 31   | 01 September 2024 |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 30   | 01 October 2024   |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0.0      | 0.0  | 0.0       | 1000.0 | 50.0 | 50.0       | 0.0  | 950.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 June 2024     | Repayment               | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 950.0        |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 200.0             |

    When Admin sets the business date to "01 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 50.0 | 50.0       | 0.0  | 200.0       |
      | 2  | 31   | 01 August 2024    |           | 500.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 3  | 31   | 01 September 2024 |           | 250.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
      | 4  | 30   | 01 October 2024   |           | 0.0             | 250.0         | 0.0      | 0.0  | 0.0       | 250.0 | 0.0  | 0.0        | 0.0  | 250.0       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 0.0      | 0.0  | 0.0       | 1000.0 | 50.0 | 50.0       | 0.0  | 950.0       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 15 June 2024     | Repayment               | 50.0   | 50.0      | 0.0      | 0.0  | 0.0       | 950.0        |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 01 July 2024       | 200.0             |

    When Loan Pay-off is made on "1 August 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3931
  Scenario: Verify nextPaymentAmount value with penalty on first installment - progressive loan, no interest recalculation, non-zero interest rate - UC2
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                              | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
       | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30 | 1 June 2024       | 1000           | 12                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 4                 | MONTHS                | 1              | MONTHS                 | 4                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 753.72          | 246.28        | 10.0     | 0.0  | 0.0       | 256.28 | 0.0  | 0.0        | 0.0  | 256.28      |
      | 2  | 31   | 01 August 2024    |           | 504.98          | 248.74        | 7.54     | 0.0  | 0.0       | 256.28 | 0.0  | 0.0        | 0.0  | 256.28      |
      | 3  | 31   | 01 September 2024 |           | 253.75          | 251.23        | 5.05     | 0.0  | 0.0       | 256.28 | 0.0  | 0.0        | 0.0  | 256.28      |
      | 4  | 30   | 01 October 2024   |           | 0.0             | 253.75        | 2.54     | 0.0  | 0.0       | 256.29 | 0.0  | 0.0        | 0.0  | 256.29      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 25.13    | 0.0  | 0.0       | 1025.13 | 0.0  | 0.0        | 0.0  | 1025.13     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 256.28             |

    When Admin sets the business date to "20 June 2024"
    When Admin runs inline COB job for Loan
    And Admin adds "LOAN_NSF_FEE" due date charge with "20 June 2024" due date and 20 EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 753.72          | 246.28        | 10.0     | 0.0  | 20.0      | 276.28 | 0.0  | 0.0        | 0.0  | 276.28      |
      | 2  | 31   | 01 August 2024    |           | 504.98          | 248.74        | 7.54     | 0.0  | 0.0       | 256.28 | 0.0  | 0.0        | 0.0  | 256.28      |
      | 3  | 31   | 01 September 2024 |           | 253.75          | 251.23        | 5.05     | 0.0  | 0.0       | 256.28 | 0.0  | 0.0        | 0.0  | 256.28      |
      | 4  | 30   | 01 October 2024   |           | 0.0             | 253.75        | 2.54     | 0.0  | 0.0       | 256.29 | 0.0  | 0.0        | 0.0  | 256.29      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 25.13    | 0.0  | 20.0      | 1045.13 | 0.0  | 0.0        | 0.0  | 1045.13     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 19 June 2024     | Accrual                 | 6.0    | 0.0       | 6.0      | 0.0  | 0.0       | 0.0          |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 276.28            |

    When Admin sets the business date to "01 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 753.72          | 246.28        | 10.0     | 0.0  | 20.0      | 276.28 | 0.0  | 0.0        | 0.0  | 276.28      |
      | 2  | 31   | 01 August 2024    |           | 504.98          | 248.74        | 7.54     | 0.0  | 0.0       | 256.28 | 0.0  | 0.0        | 0.0  | 256.28      |
      | 3  | 31   | 01 September 2024 |           | 253.75          | 251.23        | 5.05     | 0.0  | 0.0       | 256.28 | 0.0  | 0.0        | 0.0  | 256.28      |
      | 4  | 30   | 01 October 2024   |           | 0.0             | 253.75        | 2.54     | 0.0  | 0.0       | 256.29 | 0.0  | 0.0        | 0.0  | 256.29      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 25.13    | 0.0  | 20.0      | 1045.13 | 0.0  | 0.0        | 0.0  | 1045.13     |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 01 July 2024       | 276.28            |

    When Loan Pay-off is made on "1 August 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3932
  Scenario: Verify nextPaymentAmount value with repayment at 2nd installment - progressive loan, no interest recalculation, the same as repayment period - UC3
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE | 01 June 2024      | 1000           | 12                     | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 2  | 31   | 01 August 2024    |           | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0  | 0.0        | 0.0  | 343.33      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0  | 0.0        | 0.0  | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 0.0  | 0.0        | 0.0  | 1030.0      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 343.33            |

    When Admin sets the business date to "15 July 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 July 2024" with 343.33 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 15 July 2024 | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 343.33 | 0.0        | 343.33 | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0    | 0.0        | 0.0    | 343.33      |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0    | 0.0        | 0.0    | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 343.33 | 0.0        | 343.33 | 686.67      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 14 July 2024     | Accrual                 | 10.0   | 0.0       | 10.0     | 0.0  | 0.0       | 0.0          |
      | 15 July 2024     | Repayment               | 343.33 | 333.33    | 10.0     | 0.0  | 0.0       | 666.67        |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 August 2024     | 343.33            |

    When Admin sets the business date to "01 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 15 July 2024 | 666.67          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 343.33 | 0.0        | 343.33 | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 333.34          | 333.33        | 10.0     | 0.0  | 0.0       | 343.33 | 0.0    | 0.0        | 0.0    | 343.33      |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 333.34        | 10.0     | 0.0  | 0.0       | 343.34 | 0.0    | 0.0        | 0.0    | 343.34      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      | 1000          | 30.0     | 0.0  | 0.0       | 1030.0 | 343.33 | 0.0        | 343.33 | 686.67      |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 August 2024     | 343.33            |

    When Loan Pay-off is made on "1 August 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3933
  Scenario: Verify nextPaymentAmount value - progressive loan, interest recalculation daily - UC4
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 June 2024      | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 2  | 31   | 01 August 2024    |           | 335.27          | 333.33        | 3.9      | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 335.27        | 1.96     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 11.69    | 0.0  | 0.0       | 1011.69 | 0.0  | 0.0        | 0.0  | 1011.69     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 337.23            |

    When Admin sets the business date to "01 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 2  | 31   | 01 August 2024    |           | 337.2           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 337.2         | 1.97     | 0.0  | 0.0       | 339.17 | 0.0  | 0.0        | 0.0  | 339.17      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 13.63    | 0.0  | 0.0       | 1013.63 | 0.0  | 0.0        | 0.0  | 1013.63     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 31 July 2024     | Accrual                 | 11.48  | 0.0       | 11.48    | 0.0  | 0.0       | 0.0          |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 01 July 2024       | 337.23            |

    When Admin sets the business date to "05 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 2  | 31   | 01 August 2024    |           | 337.2           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 337.2         | 2.47     | 0.0  | 0.0       | 339.67 | 0.0  | 0.0        | 0.0  | 339.67      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 14.13    | 0.0  | 0.0       | 1014.13 | 0.0  | 0.0        | 0.0  | 1014.13     |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_30       | 01 July 2024       | 337.23            |

    When Loan Pay-off is made on "5 August 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3934
  Scenario: Verify nextPaymentAmount value with chargeback - progressive loan, interest recalculation daily - UC5
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 June 2024      | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 2  | 31   | 01 August 2024    |           | 335.27          | 333.33        | 3.9      | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 335.27        | 1.96     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 11.69    | 0.0  | 0.0       | 1011.69 | 0.0  | 0.0        | 0.0  | 1011.69     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 337.23            |

    When Admin sets the business date to "25 June 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "25 June 2024" with 55 EUR transaction amount
    And Admin makes "REPAYMENT_ADJUSTMENT_CHARGEBACK" chargeback with 12 EUR transaction amount for Payment nr. 1
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 668.55          | 343.45        | 5.78     | 0.0  | 0.0       | 349.23 | 55.0 | 55.0       | 0.0  | 294.23      |
      | 2  | 31   | 01 August 2024    |           | 335.22          | 333.33        | 3.9      | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 335.22        | 1.96     | 0.0  | 0.0       | 337.18 | 0.0  | 0.0        | 0.0  | 337.18      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1012.0        | 11.64    | 0.0  | 0.0       | 1023.64 | 55.0 | 55.0       | 0.0  | 968.64      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 24 June 2024     | Accrual                 | 4.47   | 0.0       | 4.47     | 0.0  | 0.0       | 0.0          |
      | 25 June 2024     | Repayment               | 55.0   | 55.0      | 0.0      | 0.0  | 0.0       | 945.0        |
      | 25 June 2024     | Chargeback              | 12.0   | 12.0      | 0.0      | 0.0  | 0.0       | 957.0        |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 294.23            |

    When Admin sets the business date to "01 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 668.55          | 343.45        | 5.78     | 0.0  | 0.0       | 349.23 | 55.0 | 55.0       | 0.0  | 294.23      |
      | 2  | 31   | 01 August 2024    |           | 336.9           | 331.65        | 5.58     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 336.9         | 1.97     | 0.0  | 0.0       | 338.87 | 0.0  | 0.0        | 0.0  | 338.87      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1012.0        | 13.33    | 0.0  | 0.0       | 1025.33 | 55.0 | 55.0       | 0.0  | 970.33     |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 01 July 2024       | 294.23            |

    When Admin sets the business date to "05 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_30       | 01 July 2024       | 294.23            |

    When Loan Pay-off is made on "5 August 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3935
  Scenario: Verify nextPaymentAmount value with full repayment on first installment - progressive loan, interest recalculation daily - UC6
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 June 2024      | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 2  | 31   | 01 August 2024    |           | 335.27          | 333.33        | 3.9      | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 335.27        | 1.96     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 11.69    | 0.0  | 0.0       | 1011.69 | 0.0  | 0.0        | 0.0  | 1011.69     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 337.23            |

    When Admin sets the business date to "25 June 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "25 June 2024" with 337.23 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 July 2024      | 25 June 2024 | 667.44          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 334.88          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 0.0    | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 334.88        | 1.95     | 0.0  | 0.0       | 336.83 | 0.0    | 0.0        | 0.0  | 336.83      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000          | 11.29    | 0.0  | 0.0       | 1011.29 | 337.23 | 337.23     | 0.0  | 674.06      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 24 June 2024     | Accrual                 | 4.47   | 0.0       | 4.47     | 0.0  | 0.0       | 0.0          |
      | 25 June 2024     | Repayment               | 337.23 | 332.56    | 4.67     | 0.0  | 0.0       | 667.44       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 August 2024       | 337.23            |

    When Admin sets the business date to "01 July 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 July 2024      | 25 June 2024 | 667.44          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 334.88          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 0.0    | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 334.88        | 1.95     | 0.0  | 0.0       | 336.83 | 0.0    | 0.0        | 0.0  | 336.83      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000          | 11.29    | 0.0  | 0.0       | 1011.29 | 337.23 | 337.23     | 0.0  | 674.06      |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 August 2024     | 337.23            |

    When Admin sets the business date to "03 July 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 July 2024      | 25 June 2024 | 667.44          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 334.88          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 0.0    | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 334.88        | 1.95     | 0.0  | 0.0       | 336.83 | 0.0    | 0.0        | 0.0  | 336.83      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000          | 11.29    | 0.0  | 0.0       | 1011.29 | 337.23 | 337.23     | 0.0  | 674.06      |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 August 2024     | 337.23            |

    When Loan Pay-off is made on "1 July 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3936
  Scenario: Verify nextPaymentAmount value overpayment first installment - progressive loan, interest recalculation daily - UC7
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 June 2024      | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 2  | 31   | 01 August 2024    |           | 335.27          | 333.33        | 3.9      | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 335.27        | 1.96     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 11.69    | 0.0  | 0.0       | 1011.69 | 0.0  | 0.0        | 0.0  | 1011.69     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 337.23            |

    When Admin sets the business date to "25 June 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "25 June 2024" with 400 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 July 2024      | 25 June 2024 | 667.44          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 334.44          | 333.0         | 4.23     | 0.0  | 0.0       | 337.23 | 62.77  | 62.77      | 0.0  | 274.46      |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 334.44        | 1.95     | 0.0  | 0.0       | 336.39 | 0.0    | 0.0        | 0.0  | 336.39      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000          | 10.85    | 0.0  | 0.0       | 1010.85 | 400.0  | 400.0      | 0.0  | 610.85      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 24 June 2024     | Accrual                 | 4.47   | 0.0       | 4.47     | 0.0  | 0.0       | 0.0          |
      | 25 June 2024     | Repayment               | 400.0  | 395.33    | 4.67     | 0.0  | 0.0       | 604.67       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 August 2024     | 274.46            |

    When Admin sets the business date to "01 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 July 2024      | 25 June 2024 | 667.44          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 334.44          | 333.0         | 4.23     | 0.0  | 0.0       | 337.23 | 62.77  | 62.77      | 0.0  | 274.46      |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 334.44        | 1.95     | 0.0  | 0.0       | 336.39 | 0.0    | 0.0        | 0.0  | 336.39      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000          | 10.85    | 0.0  | 0.0       | 1010.85 | 400.0  | 400.0      | 0.0  | 610.85      |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 August 2024     | 274.46            |

    When Admin sets the business date to "03 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 July 2024      | 25 June 2024 | 667.44          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    |              | 334.44          | 333.0         | 4.23     | 0.0  | 0.0       | 337.23 | 62.77  | 62.77      | 0.0  | 274.46      |
      | 3  | 31   | 01 September 2024 |              | 0.0             | 334.44        | 2.05     | 0.0  | 0.0       | 336.49 | 0.0    | 0.0        | 0.0  | 336.49      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late | Outstanding |
      | 1000          | 10.95    | 0.0  | 0.0       | 1010.95 | 400.0  | 400.0      | 0.0  | 610.95      |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 August 2024     | 274.46            |

    When Loan Pay-off is made on "3 August 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3937
  Scenario: Verify nextPaymentAmount value for the last installment - progressive loan, interest recalculation daily, next installment - UC8
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 June 2024      | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 2  | 31   | 01 August 2024    |           | 335.27          | 333.33        | 3.9      | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 335.27        | 1.96     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 11.69    | 0.0  | 0.0       | 1011.69 | 0.0  | 0.0        | 0.0  | 1011.69     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 337.23            |

    When Admin sets the business date to "15 August 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 August 2024" with 337.23 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |                | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 15 August 2024 | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 337.23 | 0.0        | 337.23 | 0.0         |
      | 2  | 31   | 01 August 2024    |                | 337.2           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0    | 0.0        | 0.0    | 337.23      |
      | 3  | 31   | 01 September 2024 |                | 0.0             | 337.2         | 3.71     | 0.0  | 0.0       | 340.91 | 0.0    | 0.0        | 0.0    | 340.91      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1000          | 15.37    | 0.0  | 0.0       | 1015.37 | 337.23 | 0.0        | 337.23 | 678.14      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 14 August 2024   | Accrual                 | 12.18  | 0.0       | 12.18    | 0.0  | 0.0       | 0.0          |
      | 15 August 2024   | Repayment               | 337.23 | 331.4     | 5.83     | 0.0  | 0.0       | 668.6        |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 15 August 2024     | 340.91            |

    When Admin sets the business date to "01 September 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |                | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 15 August 2024 | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 337.23 | 0.0        | 337.23 | 0.0         |
      | 2  | 31   | 01 August 2024    |                | 337.2           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0    | 0.0        | 0.0    | 337.23      |
      | 3  | 31   | 01 September 2024 |                | 0.0             | 337.2         | 4.77     | 0.0  | 0.0       | 341.97 | 0.0    | 0.0        | 0.0    | 341.97      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1000          | 16.43    | 0.0  | 0.0       | 1016.43 | 337.23 | 0.0        | 337.23 | 679.2       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 15 August 2024     | 341.97            |

    When Admin sets the business date to "03 September 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date      | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |                | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 30   | 01 July 2024      | 15 August 2024 | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 337.23 | 0.0        | 337.23 | 0.0         |
      | 2  | 31   | 01 August 2024    |                | 337.2           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0    | 0.0        | 0.0    | 337.23      |
      | 3  | 31   | 01 September 2024 |                | 0.0             | 337.2         | 4.77     | 0.0  | 0.0       | 341.97 | 0.0    | 0.0        | 0.0    | 341.97      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1000          | 16.43    | 0.0  | 0.0       | 1016.43 | 337.23 | 0.0        | 337.23 | 679.2       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 15 August 2024     | 341.97            |

    When Loan Pay-off is made on "3 September 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3938
  Scenario: Verify nextPaymentAmount value for the last installment - progressive loan, interest recalculation daily, last installment - UC9
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "LAST_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                             | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 June 2024      | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 2  | 31   | 01 August 2024    |           | 335.27          | 333.33        | 3.9      | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 335.27        | 1.96     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 11.69    | 0.0  | 0.0       | 1011.69 | 0.0  | 0.0        | 0.0  | 1011.69     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |

    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 337.23            |

    When Admin sets the business date to "15 June 2024"
    When Admin runs inline COB job for Loan
    And Customer makes "AUTOPAY" repayment on "15 June 2024" with 337.23 EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |       |             |
      | 1  | 30   | 01 July 2024      |              | 667.55          | 332.45        | 4.78     | 0.0  | 0.0       | 337.23 | 0.0    | 0.0        | 0.0   | 337.23      |
      | 2  | 31   | 01 August 2024    |              | 337.23          | 330.32        | 1.93     | 0.0  | 0.0       | 332.25 | 0.0    | 0.0        | 0.0   | 332.25      |
      | 3  | 31   | 01 September 2024 | 15 June 2024 | 0.0             | 337.23        | 0.0      | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late  | Outstanding |
      | 1000          | 6.71     | 0.0  | 0.0       | 1006.71 | 337.23 | 337.23     | 0.0   | 669.48      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 14 June 2024     | Accrual                 | 2.53   | 0.0       | 2.53     | 0.0  | 0.0       | 0.0          |
      | 15 June 2024     | Repayment               | 337.23 | 337.23    | 0.0      | 0.0  | 0.0       | 662.77       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 337.23            |

    When Admin sets the business date to "15 July 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |       |             |
      | 1  | 30   | 01 July 2024      |              | 667.55          | 332.45        | 4.78     | 0.0  | 0.0       | 337.23 | 0.0    | 0.0        | 0.0   | 337.23      |
      | 2  | 31   | 01 August 2024    |              | 337.23          | 330.32        | 2.8      | 0.0  | 0.0       | 333.12 | 0.0    | 0.0        | 0.0   | 333.12      |
      | 3  | 31   | 01 September 2024 | 15 June 2024 | 0.0             | 337.23        | 0.0      | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late  | Outstanding |
      | 1000          | 7.58     | 0.0  | 0.0       | 1007.58 | 337.23 | 337.23     | 0.0   | 670.35      |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 01 July 2024       | 337.23            |

    When Admin sets the business date to "01 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late  | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |       |             |
      | 1  | 30   | 01 July 2024      |              | 667.55          | 332.45        | 4.78     | 0.0  | 0.0       | 337.23 | 0.0    | 0.0        | 0.0   | 337.23      |
      | 2  | 31   | 01 August 2024    |              | 337.23          | 330.32        | 3.87     | 0.0  | 0.0       | 334.19 | 0.0    | 0.0        | 0.0   | 334.19      |
      | 3  | 31   | 01 September 2024 | 15 June 2024 | 0.0             | 337.23        | 0.0      | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0   | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late  | Outstanding |
      | 1000          | 8.65     | 0.0  | 0.0       | 1008.65 | 337.23 | 337.23     | 0.0   | 671.42      |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 01 July 2024       | 337.23            |
    When Admin set "LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule

    When Loan Pay-off is made on "1 August 2024"
    Then Loan's all installments have obligations met

  @TestRailId:C3939
  Scenario: Verify nextPaymentAmount value with loan pay-off on first installment - progressive loan, interest recalculation daily - UC10
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                        | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE | 01 June 2024      | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |           | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0  |            |      |             |
      | 1  | 30   | 01 July 2024      |           | 668.6           | 331.4         | 5.83     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 2  | 31   | 01 August 2024    |           | 335.27          | 333.33        | 3.9      | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
      | 3  | 31   | 01 September 2024 |           | 0.0             | 335.27        | 1.96     | 0.0  | 0.0       | 337.23 | 0.0  | 0.0        | 0.0  | 337.23      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid | In advance | Late | Outstanding |
      | 1000          | 11.69    | 0.0  | 0.0       | 1011.69 | 0.0  | 0.0        | 0.0  | 1011.69     |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 337.23            |

    When Admin sets the business date to "25 June 2024"
    When Admin runs inline COB job for Loan
    When Loan Pay-off is made on "25 June 2024"
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 July 2024      | 25 June 2024 | 667.44          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    | 25 June 2024 | 330.21          | 337.23        | 0.0      | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 3  | 31   | 01 September 2024 | 25 June 2024 | 0.0             | 330.21        | 0.0      | 0.0  | 0.0       | 330.21 | 330.21 | 330.21     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000          | 4.67     | 0.0  | 0.0       | 1004.67 | 1004.67 | 1004.67    | 0.0  | 0.0         |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount  | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0  | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 24 June 2024     | Accrual                 | 4.47    | 0.0       | 4.47     | 0.0  | 0.0       | 0.0          |
      | 25 June 2024     | Repayment               | 1004.67 | 1000.0    | 4.67     | 0.0  | 0.0       | 0.0          |
      | 25 June 2024     | Accrual                 | 0.2     | 0.0       | 0.2      | 0.0  | 0.0       | 0.0          |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 25 June 2024       | 0.0               |

    When Admin sets the business date to "01 August 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 3 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |      |             |
      | 1  | 30   | 01 July 2024      | 25 June 2024 | 667.44          | 332.56        | 4.67     | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 2  | 31   | 01 August 2024    | 25 June 2024 | 330.21          | 337.23        | 0.0      | 0.0  | 0.0       | 337.23 | 337.23 | 337.23     | 0.0  | 0.0         |
      | 3  | 31   | 01 September 2024 | 25 June 2024 | 0.0             | 330.21        | 0.0      | 0.0  | 0.0       | 330.21 | 330.21 | 330.21     | 0.0  | 0.0         |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid    | In advance | Late | Outstanding |
      | 1000          | 4.67     | 0.0  | 0.0       | 1004.67 | 1004.67 | 1004.67    | 0.0  | 0.0         |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 August 2024     | 0.0               |

  @TestRailId:C3940
  Scenario: Verify nextPaymentAmount value with downpayment and interest refund - progressive loan, interest recalculation daily - UC11
    When Admin sets the business date to "01 June 2024"
    And Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                          | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY | 01 June 2024      | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 June 2024" with "1000" amount and expected disbursement date on "01 June 2024"
    And Admin successfully disburse the loan on "01 June 2024" with "1000" EUR transaction amount
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0   |            |      |             |
      | 1  | 0    | 01 June 2024      | 01 June 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0 | 0.0        | 0.0  | 0.0         |
      | 2  | 30   | 01 July 2024      |              | 501.45          | 248.55        | 4.37     | 0.0  | 0.0       | 252.92 | 0.0   | 0.0        | 0.0  | 252.92      |
      | 3  | 31   | 01 August 2024    |              | 251.46          | 249.99        | 2.93     | 0.0  | 0.0       | 252.92 | 0.0   | 0.0        | 0.0  | 252.92      |
      | 4  | 31   | 01 September 2024 |              | 0.0             | 251.46        | 1.47     | 0.0  | 0.0       | 252.93 | 0.0   | 0.0        | 0.0  | 252.93      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid  | In advance | Late | Outstanding |
      | 1000          | 8.77     | 0.0  | 0.0       | 1008.77 | 250.0 | 0.0        | 0.0  | 758.77      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 June 2024     | Down Payment            | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | NO_DELINQUENCY | 01 July 2024       | 252.92            |

    When Admin sets the business date to "01 August 2024"
    When Admin runs inline COB job for Loan
    When Customer makes "MERCHANT_ISSUED_REFUND" transaction with "AUTOPAY" payment type on "01 August 2024" with 200 EUR transaction amount and self-generated Idempotency key
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 0    | 01 June 2024      | 01 June 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0  | 0.0        | 0.0    | 0.0         |
      | 2  | 30   | 01 July 2024      |              | 501.45          | 248.55        | 4.37     | 0.0  | 0.0       | 252.92 | 202.32 | 0.0        | 202.32 | 50.6        |
      | 3  | 31   | 01 August 2024    |              | 252.9           | 248.55        | 4.37     | 0.0  | 0.0       | 252.92 | 0.0    | 0.0        | 0.0    | 252.92      |
      | 4  | 31   | 01 September 2024 |              | 0.0             | 252.9         | 1.48     | 0.0  | 0.0       | 254.38 | 0.0    | 0.0        | 0.0    | 254.38      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1000          | 10.22    | 0.0  | 0.0       | 1010.22 | 452.32 | 0.0        | 202.32 | 557.9       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type        | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 June 2024     | Disbursement            | 1000.0 | 0.0       | 0.0      | 0.0  | 0.0       | 1000.0       |
      | 01 June 2024     | Down Payment            | 250.0  | 250.0     | 0.0      | 0.0  | 0.0       | 750.0        |
      | 01 July 2024     | Accrual Activity        | 4.37   | 0.0       | 4.37     | 0.0  | 0.0       | 0.0          |
      | 31 July 2024     | Accrual                 | 8.6    | 0.0       | 8.6      | 0.0  | 0.0       | 0.0          |
      | 01 August 2024   | Merchant Issued Refund  | 200.0  | 200.0     | 0.0      | 0.0  | 0.0       | 550.0        |
      | 01 August 2024   | Interest Refund         | 2.32   | 2.32      | 0.0      | 0.0  | 0.0       | 547.68       |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_3        | 01 August 2024     | 252.92            |

    When Admin sets the business date to "01 September 2024"
    When Admin runs inline COB job for Loan
    Then Loan Repayment schedule has 4 periods, with the following data for periods:
      | Nr | Days | Date              | Paid date    | Balance of loan | Principal due | Interest | Fees | Penalties | Due    | Paid   | In advance | Late   | Outstanding |
      |    |      | 01 June 2024      |              | 1000.0          |               |          | 0.0  |           | 0.0    | 0.0    |            |        |             |
      | 1  | 0    | 01 June 2024      | 01 June 2024 | 750.0           | 250.0         | 0.0      | 0.0  | 0.0       | 250.0  | 250.0  | 0.0        | 0.0    | 0.0         |
      | 2  | 30   | 01 July 2024      |              | 501.45          | 248.55        | 4.37     | 0.0  | 0.0       | 252.92 | 202.32 | 0.0        | 202.32 | 50.6        |
      | 3  | 31   | 01 August 2024    |              | 252.9           | 248.55        | 4.37     | 0.0  | 0.0       | 252.92 | 0.0    | 0.0        | 0.0    | 252.92      |
      | 4  | 31   | 01 September 2024 |              | 0.0             | 252.9         | 3.19     | 0.0  | 0.0       | 256.09 | 0.0    | 0.0        | 0.0    | 256.09      |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due     | Paid   | In advance | Late   | Outstanding |
      | 1000          | 11.93    | 0.0  | 0.0       | 1011.93 | 452.32 | 0.0        | 202.32 | 559.61      |
    Then Loan has the following LOAN level next payment due data:
      | classification | nextPaymentDueDate | nextPaymentAmount |
      | RANGE_30       | 01 August 2024     | 252.92            |

    When Loan Pay-off is made on "1 September 2024"
    Then Loan's all installments have obligations met
