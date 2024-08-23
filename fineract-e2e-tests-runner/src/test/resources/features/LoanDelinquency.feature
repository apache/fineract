@LoanDelinquencyFeature
Feature: LoanDelinquency


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


  Scenario: Verify Loan delinquency pause API - RESUME without an active PAUSE period results an error
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
    Then Initiating a DELINQUENCY RESUME without an active PAUSE period results an error - startDate: "01 October 2023"


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

  @Skip
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


  Scenario: Verify Installment level loan delinquency - loan goes into delinquency pause
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
    When Admin sets the business date to "01 January 2024"
    When Admin runs inline COB job for Loan
    Then Installment level delinquency event has correct data
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 3       | RANGE_30 | 500.00 |
      | 4       | RANGE_60 | 500.00 |


  Scenario: Verify Installment level loan delinquency - loan goes into delinquency pause then will be resumed
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


  Scenario: Verify Loan delinquency pause E2E - PAUSE period with RESUME
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
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


  Scenario: Verify Loan delinquency pause E2E - PAUSE period with RESUME and second PAUSE
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
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
      | RANGE_30       | 1000.0           | 04 October 2023 | 31             | 60          |
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 2       | RANGE_3  | 500.00 |
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


  Scenario: Verify Loan delinquency pause E2E - full repayment (late/due date) during PAUSE period
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
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


  Scenario: Verify Loan delinquency pause E2E - partial repayment during PAUSE period
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
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


  Scenario: Verify Loan delinquency pause E2E - full repayment (only late) during PAUSE period then RESUME
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
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


  Scenario: Verify that in case of resume on end/start date of continous pause periods first period ends automatically, second period ended by resume
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 October 2023" with "1000" amount and expected disbursement date on "01 October 2023"
    When Admin successfully disburse the loan on "01 October 2023" with "1000" EUR transaction amount
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


  Scenario: Verify that creating a loan with Advanced payment allocation with product no Advanced payment allocation set results an error
    When Admin sets the business date to "01 October 2023"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with Advanced payment allocation and with product no Advanced payment allocation set results an error:
      | LoanProduct | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP1       | 01 October 2023   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |


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

   @AdvancedPaymentAllocation
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

#    TODO remove skip when LoanDelinquencyRangeChangeBusinessEvent has wrong values compared to LoanDetails is done and scenario below here does not fail
  @Skip
  Scenario: Verify that LoanDelinquencyRangeChangeBusinessEvent has the correct Delinquency range, date and amount on both loan- and installment-level
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 90                | DAYS                  | 30             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "05 March 2024"
    When Admin runs inline COB job for Loan
    Then Loan has the following LOAN level delinquency data:
      | classification | delinquentAmount | delinquentDate  | delinquentDays | pastDueDays |
      | RANGE_60       | 750.0            | 04 January 2024 | 61             | 64          |
    Then Loan has the following INSTALLMENT level delinquency data:
      | rangeId | Range    | Amount |
      | 2       | RANGE_3  | 250.00 |
      | 3       | RANGE_30 | 250.00 |
      | 4       | RANGE_60 | 250.00 |
    Then LoanDelinquencyRangeChangeBusinessEvent has the same Delinquency range, date and amount as in LoanDetails on both loan- and installment-level
