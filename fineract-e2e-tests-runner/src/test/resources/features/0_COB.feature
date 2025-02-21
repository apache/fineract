@COBFeature
Feature: COBFeature

  @TestRailId:C2501
  Scenario: As an admin I would like to see that last closed business date got updated after COB catch up job finished
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2022"
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Admin checks that last closed business date of loan is "null"
    When Admin sets the business date to "02 January 2022"
    When Admin runs COB job
    Then Admin checks that last closed business date of loan is "01 January 2022"
    When Admin sets the business date to "05 January 2022"
    When Admin runs COB job
    Then Admin checks that last closed business date of loan is "01 January 2022"
    When Admin runs COB catch up
    When Admin checks that Loan COB is running until the current business date
    Then Admin checks that last closed business date of loan is "04 January 2022"
    And Customer makes "AUTOPAY" repayment on "05 January 2022" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"


  @TestRailId:C2681
  Scenario: As an admin I would like to check that the Delinquency bucket set on the loan correctly when the arrears setting is 3 on the product
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2022"
    And Admin successfully approves the loan on "1 January 2022" with "1000" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "4 February 2022"
    When Admin runs COB job
    Then Admin checks that delinquency range is: "RANGE_1" and has delinquentDate "2022-02-03"

  @TestRailId:C2791
  Scenario: Verify that COB processes loans which are not closed/overpaid and has a last_closed_business_date exactly 1 day behind COB date
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 July 2023"
    And Admin successfully approves the loan on "01 July 2023" with "1000" amount and expected disbursement date on "01 July 2023"
    When Admin successfully disburse the loan on "01 July 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Admin checks that last closed business date of loan is "null"
    When Admin sets the business date to "02 July 2023"
    When Admin runs COB job
    Then Admin checks that last closed business date of loan is "01 July 2023"
    When Admin sets the business date to "03 July 2023"
    When Admin runs COB job
    Then Admin checks that last closed business date of loan is "02 July 2023"

  @TestRailId:C2792
  Scenario: Verify that COB doesn’t touch loans with last closed business date behind COB date
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 July 2023"
    And Admin successfully approves the loan on "01 July 2023" with "1000" amount and expected disbursement date on "01 July 2023"
    When Admin successfully disburse the loan on "01 July 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Admin checks that last closed business date of loan is "null"
    When Admin sets the business date to "10 August 2023"
    When Admin runs inline COB job for Loan
    Then Admin checks that last closed business date of loan is "09 August 2023"
    Then Admin checks that delinquency range is: "RANGE_3" and has delinquentDate "2023-08-03"
    Then Loan delinquency history has the following details:
      | Range (Classification) | Added on date  | Lifted on date |
      | RANGE_3                | 10 August 2023 |                |
      | RANGE_1                | 04 August 2023 | 09 August 2023 |
    When Admin sets the business date to "12 August 2023"
    When Admin runs COB job
    Then Admin checks that last closed business date of loan is "09 August 2023"
    Then Loan delinquency history has the following details:
      | Range (Classification) | Added on date  | Lifted on date |
      | RANGE_3                | 10 August 2023 |                |
      | RANGE_1                | 04 August 2023 | 09 August 2023 |

  @TestRailId:C2793
  Scenario: Verify that COB doesn’t touch CLOSED loans
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 July 2023"
    And Admin successfully approves the loan on "01 July 2023" with "1000" amount and expected disbursement date on "01 July 2023"
    When Admin successfully disburse the loan on "01 July 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Admin checks that last closed business date of loan is "null"
    When Admin sets the business date to "10 August 2023"
    When Admin runs inline COB job for Loan
    Then Admin checks that last closed business date of loan is "09 August 2023"
    Then Admin checks that delinquency range is: "RANGE_3" and has delinquentDate "2023-08-03"
    Then Loan delinquency history has the following details:
      | Range (Classification) | Added on date  | Lifted on date |
      | RANGE_3                | 10 August 2023 |                |
      | RANGE_1                | 04 August 2023 | 09 August 2023 |
    And Customer makes "AUTOPAY" repayment on "10 August 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Admin checks that delinquency range is: "NO_DELINQUENCY" and has delinquentDate ""
    Then Loan delinquency history has the following details:
      | Range (Classification) | Added on date  | Lifted on date |
      | RANGE_3                | 10 August 2023 | 10 August 2023 |
      | RANGE_1                | 04 August 2023 | 09 August 2023 |
    When Admin sets the business date to "11 August 2023"
    When Admin runs COB job
    Then Admin checks that last closed business date of loan is "09 August 2023"
    Then Loan delinquency history has the following details:
      | Range (Classification) | Added on date  | Lifted on date |
      | RANGE_3                | 10 August 2023 | 10 August 2023 |
      | RANGE_1                | 04 August 2023 | 09 August 2023 |

  @TestRailId:C2794
  Scenario: Verify that COB doesn’t touch OVERPAID loans
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 July 2023"
    And Admin successfully approves the loan on "01 July 2023" with "1000" amount and expected disbursement date on "01 July 2023"
    When Admin successfully disburse the loan on "01 July 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Admin checks that last closed business date of loan is "null"
    When Admin sets the business date to "10 August 2023"
    When Admin runs inline COB job for Loan
    Then Admin checks that last closed business date of loan is "09 August 2023"
    Then Admin checks that delinquency range is: "RANGE_3" and has delinquentDate "2023-08-03"
    Then Loan delinquency history has the following details:
      | Range (Classification) | Added on date  | Lifted on date |
      | RANGE_3                | 10 August 2023 |                |
      | RANGE_1                | 04 August 2023 | 09 August 2023 |
    And Customer makes "AUTOPAY" repayment on "10 August 2023" with 1200 EUR transaction amount
    Then Loan status will be "OVERPAID"
    Then Admin checks that delinquency range is: "NO_DELINQUENCY" and has delinquentDate ""
    Then Loan delinquency history has the following details:
      | Range (Classification) | Added on date  | Lifted on date |
      | RANGE_3                | 10 August 2023 | 10 August 2023 |
      | RANGE_1                | 04 August 2023 | 09 August 2023 |
    When Admin sets the business date to "11 August 2023"
    When Admin runs COB job
    Then Admin checks that last closed business date of loan is "09 August 2023"
    Then Loan delinquency history has the following details:
      | Range (Classification) | Added on date  | Lifted on date |
      | RANGE_3                | 10 August 2023 | 10 August 2023 |
      | RANGE_1                | 04 August 2023 | 09 August 2023 |

  @Skip @TestRailId:C2795
  Scenario: Verify that COB catch up runs properly on loan which is behind date because of locked with error
    When Admin sets the business date to "01 January 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 January 2022"
    And Admin successfully approves the loan on "01 January 2022" with "1000" amount and expected disbursement date on "01 January 2022"
    When Admin successfully disburse the loan on "01 January 2022" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Admin checks that last closed business date of loan is "null"
    When Admin sets the business date to "02 January 2022"
    When Admin runs COB job
    Then Admin checks that last closed business date of loan is "01 January 2022"
    When Admin sets the business date to "03 January 2022"
    Then Admin places a lock on loan account with an error message
    Then Admin checks that last closed business date of loan is "01 January 2022"
    When Admin sets the business date to "05 January 2022"
    Then Admin checks that last closed business date of loan is "01 January 2022"
    When Admin runs COB catch up
    When Admin checks that Loan COB is running until the current business date
    Then Admin checks that last closed business date of loan is "04 January 2022"
    And Customer makes "AUTOPAY" repayment on "05 January 2022" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"

  @TestRailId:C2796
  Scenario: Verify that after COB runs there are no unreleased loan locks
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 July 2023"
    And Admin successfully approves the loan on "01 July 2023" with "1000" amount and expected disbursement date on "01 July 2023"
    When Admin successfully disburse the loan on "01 July 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Admin checks that last closed business date of loan is "null"
    When Admin sets the business date to "02 July 2023"
    When Admin runs COB job
    Then The loan account is not locked

  @TestRailId:C2797
  Scenario: Verify that Inline COB is executed for stuck loans - when payment happened on a loan with last closed business date in the past, COB got executed before the repayment
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 July 2023"
    And Admin successfully approves the loan on "01 July 2023" with "1000" amount and expected disbursement date on "01 July 2023"
    When Admin successfully disburse the loan on "01 July 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Admin checks that last closed business date of loan is "null"
    When Admin sets the business date to "02 July 2023"
    When Admin runs COB job
    Then Admin checks that last closed business date of loan is "01 July 2023"
    When Admin sets the business date to "04 July 2023"
    When Admin creates new user with "NO_BYPASS_AUTOTEST" username, "NO_BYPASS_AUTOTEST_ROLE" role name and given permissions:
      | REPAYMENT_LOAN |
    When Created user makes externalID controlled "AUTOPAY" repayment on "04 July 2023" with 500 EUR transaction amount
    Then Admin checks that last closed business date of loan is "03 July 2023"

#  On a hard locked loan, in case of the lock has an error message, payment by a non-bypass user should trigger inlineCob and it should be executed
#  this functionality is not implemented yet
  @Skip @TestRailId:C2798
  Scenario: Verify that Inline COB is executed for stuck loans - when payment happened on a locked loan COB got executed before the repayment
    When Admin sets the business date to "01 July 2023"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "01 July 2023"
    And Admin successfully approves the loan on "01 July 2023" with "1000" amount and expected disbursement date on "01 July 2023"
    When Admin successfully disburse the loan on "01 July 2023" with "1000" EUR transaction amount
    Then Loan status will be "ACTIVE"
    Then Admin checks that last closed business date of loan is "null"
    When Admin sets the business date to "02 July 2023"
    When Admin runs COB job
    Then Admin checks that last closed business date of loan is "01 July 2023"
    When Admin places a lock on loan account with an error message
    When Admin sets the business date to "04 July 2023"
    When Admin creates new user with "NO_BYPASS_AUTOTEST" username, "NO_BYPASS_AUTOTEST_ROLE" role name and given permissions:
      | REPAYMENT_LOAN |
    When Created user makes externalID controlled "AUTOPAY" repayment on "04 July 2023" with 500 EUR transaction amount
    Then Admin checks that last closed business date of loan is "03 July 2023"

  @TestRailId:C3044 @AdvancedPaymentAllocation
  Scenario: Verify that LoanAccountCustomSnapshotBusinessEvent is created with proper business date when installment is due date and COB runs
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin set "LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION" loan product "DEFAULT" transaction type to "NEXT_INSTALLMENT" future installment allocation rule
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION | 01 January 2024   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 45                | DAYS                  | 15             | DAYS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "1000" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "1000" EUR transaction amount
    When Admin sets the business date to "17 January 2024"
    When Admin runs inline COB job for Loan
    Then LoanAccountCustomSnapshotBusinessEvent is created with business date "17 January 2024"


