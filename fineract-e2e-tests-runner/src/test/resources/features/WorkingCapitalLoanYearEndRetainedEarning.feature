@WorkingCapital
@WorkingCapitalLoanYearEndRetainedEarningFeature
@YearEndRetainedEarning
Feature: Working Capital Loan Year End Retained Earning

  Background:
    When Global config "income-expense-gl-accounts" value set to "400000-899999"
    And Global config "retained-gl-account" value set to "320000"
    And Global config "retained-earning-used-by-report-name" value set to "Trial Balance Summary Report with Asset Owner"
    And Global config "retained-earning-wc-used-by-report-name" value set to "Trial Balance Summary Report for Working Capital Loans"

  # TODO: add TestRail ID
  Scenario: Verify that year-end close zeroes working capital income accounts into retained earnings, is idempotent and next year starts clean
    Given any existing year-end retained earnings close for fiscal year ending "31 December 2028" is removed
    When Admin sets the business date to "01 December 2028"
    And Admin creates a new office
    And Admin points the Retained Earning Job at the last created office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 December 2028 | 01 December 2028         | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 December 2028" with "1000" amount and expected disbursement date on "01 December 2028"
    And Admin successfully disburse the Working Capital loan on "01 December 2028" with "1000" EUR transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "01 December 2028" due date and 50.0 transaction amount
    When Admin sets the business date to "02 December 2028"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Trial Balance Summary Report for Working Capital Loans for date "31 December 2028" has a row for GL account "404007" with non-zero ending balance
    And Trial Balance Summary Report for Working Capital Loans for date "31 December 2028" has a row for GL account "145023" with non-zero ending balance
    When Admin sets the business date to "02 January 2029"
    And Admin runs the Retained Earning Job
    Then Trial Balance Summary Report for Working Capital Loans for date "01 January 2029" shows GL account "404007" closed out
    And Trial Balance Summary Report for Working Capital Loans for date "01 January 2029" has a row for GL account "320000" with non-zero ending balance
    And Trial Balance Summary Report for Working Capital Loans for date "01 January 2029" has a row for GL account "145023" with non-zero ending balance
    And Trial Balance Summary Report with Asset Owner for date "01 January 2029" has no rows
    And The journal entry annual summary table contains 1 row for GL code "320000" with year end date "31 December 2028"
    When Admin runs the Retained Earning Job
    Then Trial Balance Summary Report for Working Capital Loans for date "01 January 2029" shows GL account "404007" closed out
    And The journal entry annual summary table contains 1 row for GL code "320000" with year end date "31 December 2028"
    When Admin sets the business date to "05 January 2029"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2029" due date and 20.0 transaction amount
    And Admin sets the business date to "06 January 2029"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Trial Balance Summary Report for Working Capital Loans for date "05 January 2029" has a row for GL account "404007" with non-zero ending balance
