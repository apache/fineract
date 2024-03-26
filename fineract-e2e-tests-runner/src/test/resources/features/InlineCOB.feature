@InlineCOBFeature
Feature: InlineCOBFeature

  @TestRailId:C2457
  Scenario: As an admin I would like to see that inline COB Happy Path scenario is working properly
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2022"
    And Admin successfully approves the loan on "1 January 2022" with "1000" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "1000" EUR transaction amount
    When Admin sets the business date to "1 March 2022"
    When Admin runs inline COB job for Loan
    Then Admin checks that delinquency range is: "RANGE_3" and has delinquentDate "2022-02-03"

  @TestRailId:C2473
  Scenario: As an admin I would like to see that loan was catching up with inline COB
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2022"
    And Admin successfully approves the loan on "1 January 2022" with "1000" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "1000" EUR transaction amount
    Then Admin checks that last closed business date of loan is "null"
    When Admin sets the business date to "2 January 2022"
    When Admin runs inline COB job for Loan
    Then Admin checks that last closed business date of loan is "01 January 2022"
    When Admin sets the business date to "5 January 2022"
    When Admin runs inline COB job for Loan
    Then Admin checks that last closed business date of loan is "04 January 2022"

  @TestRailId:C2554
  Scenario: As an admin I would like to see that loan was catching up with inline COB on a repayment
    When Admin sets the business date to "1 January 2022"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2022"
    And Admin successfully approves the loan on "1 January 2022" with "1000" amount and expected disbursement date on "1 January 2022"
    When Admin successfully disburse the loan on "1 January 2022" with "1000" EUR transaction amount
    Then Admin checks that last closed business date of loan is "null"
    When Admin sets the business date to "2 January 2022"
    When Admin runs inline COB job for Loan
    Then Admin checks that last closed business date of loan is "01 January 2022"
    When Admin sets the business date to "5 January 2022"
    When Admin creates new user with "NO_BYPASS_AUTOTEST" username, "NO_BYPASS_AUTOTEST_ROLE" role name and given permissions:
      | REPAYMENT_LOAN |
    And Created user makes "AUTOPAY" repayment on "5 January 2022" with 100 EUR transaction amount
    Then Admin checks that last closed business date of loan is "04 January 2022"

  @TestRailId:C2602
  Scenario: Verify that LoanCOBApiFilter works fine in the background and loanId controlled transaction does not trigger inlineCOB if COB had not been applied on loan before
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new Loan with date: "01 January 2023" and with 1 day loan term and repayment
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Admin checks that last closed business date of loan is "null"
    When Admin sets the business date to "07 January 2023"
    When Admin creates new user with "NO_BYPASS_AUTOTEST" username, "NO_BYPASS_AUTOTEST_ROLE" role name and given permissions:
      | REPAYMENT_LOAN |
    And Created user makes "AUTOPAY" repayment on "07 January 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "07 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
    Then Loan delinquency history has the following details:
      | Range (Classification) | Added on date   | Lifted on date  |
      | RANGE_1                | 06 January 2023 | 07 January 2023 |
    Then Admin checks that last closed business date of loan is "null"

  @TestRailId:C2603
  Scenario: Verify that LoanCOBApiFilter works fine in the background and externalId controlled transaction does not trigger inlineCOB if COB had not been applied on loan before
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new Loan with date: "01 January 2023" and with 1 day loan term and repayment
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Admin checks that last closed business date of loan is "null"
    When Admin sets the business date to "07 January 2023"
    When Admin creates new user with "NO_BYPASS_AUTOTEST" username, "NO_BYPASS_AUTOTEST_ROLE" role name and given permissions:
      | REPAYMENT_LOAN |
    When Created user makes externalID controlled "AUTOPAY" repayment on "07 January 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "07 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
    Then Loan delinquency history has the following details:
      | Range (Classification) | Added on date   | Lifted on date  |
      | RANGE_1                | 06 January 2023 | 07 January 2023 |
    Then Admin checks that last closed business date of loan is "null"

  @TestRailId:C2604
  Scenario: Verify that LoanCOBApiFilter works fine in the background and loanId controlled transaction triggers inlineCOB if COB had been applied on loan before
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new Loan with date: "01 January 2023" and with 1 day loan term and repayment
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Admin checks that last closed business date of loan is "null"
    When Admin sets the business date to "2 January 2023"
    When Admin runs inline COB job for Loan
    Then Admin checks that last closed business date of loan is "01 January 2023"
    When Admin sets the business date to "07 January 2023"
    When Admin creates new user with "NO_BYPASS_AUTOTEST" username, "NO_BYPASS_AUTOTEST_ROLE" role name and given permissions:
      | REPAYMENT_LOAN |
    And Created user makes "AUTOPAY" repayment on "07 January 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "07 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
    Then Loan delinquency history has the following details:
      | Range (Classification) | Added on date   | Lifted on date  |
      | RANGE_1                | 06 January 2023 | 07 January 2023 |
    Then Admin checks that last closed business date of loan is "06 January 2023"

  @TestRailId:C2605
  Scenario: Verify that LoanCOBApiFilter works fine in the background and externalId controlled transaction triggers inlineCOB if COB had been applied on loan before
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new Loan with date: "01 January 2023" and with 1 day loan term and repayment
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    Then Admin checks that last closed business date of loan is "null"
    When Admin sets the business date to "2 January 2023"
    When Admin runs inline COB job for Loan
    Then Admin checks that last closed business date of loan is "01 January 2023"
    When Admin sets the business date to "07 January 2023"
    When Admin creates new user with "NO_BYPASS_AUTOTEST" username, "NO_BYPASS_AUTOTEST_ROLE" role name and given permissions:
      | REPAYMENT_LOAN |
    When Created user makes externalID controlled "AUTOPAY" repayment on "07 January 2023" with 1000 EUR transaction amount
    Then Loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Loan has 0 outstanding amount
    Then Loan Transactions tab has a "REPAYMENT" transaction with date "07 January 2023" which has the following Journal entries:
      | Type      | Account code | Account name              | Debit  | Credit |
      | ASSET     | 112601       | Loans Receivable          |        | 1000.0 |
      | LIABILITY | 145023       | Suspense/Clearing account | 1000.0 |        |
    Then Loan delinquency history has the following details:
      | Range (Classification) | Added on date   | Lifted on date  |
      | RANGE_1                | 06 January 2023 | 07 January 2023 |
    Then Admin checks that last closed business date of loan is "06 January 2023"
