@WorkingCapital
@WorkingCapitalLoanYearEndRetainedEarningFeature
@YearEndRetainedEarning
Feature: Working Capital Loan Year End Retained Earning

  Background:
    When Global config "income-expense-gl-accounts" value set to "400000-899999"
    And Global config "retained-gl-account" value set to "320000"
    And Global config "retained-earning-used-by-report-name" value set to "Trial Balance Summary Report with Asset Owner"
    And Global config "retained-earning-wc-used-by-report-name" value set to "Trial Balance Summary Report for Working Capital Loans"

  @TestRailId:C115382
  Scenario: Verify that year-end close zeroes working capital income accounts into retained earnings, is idempotent and next year starts clean - UC1
    Given any existing year-end retained earnings close for fiscal year ending "31 December 2028" is removed
    When Admin sets the business date to "29 December 2028"
    And Admin creates a new office
    And Admin points the Retained Earning Job at the last created office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 29 December 2028 | 29 December 2028         | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "29 December 2028" with "1000" amount and expected disbursement date on "29 December 2028"
    And Admin successfully disburse the Working Capital loan on "29 December 2028" with "1000" EUR transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "29 December 2028" due date and 50.0 transaction amount
    When Admin sets the business date to "30 December 2028"
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
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "06 January 2029"

  @TestRailId:C115383
  Scenario: Verify working capital year-end close moves income into the Retained Earnings Prior Year row with exact balances and carries it into the next year - UC2
    Given any existing year-end retained earnings close for fiscal year ending "31 December 2030" is removed
    When Admin sets the business date to "29 December 2030"
    And Admin creates a new office
    And Admin points the Retained Earning Job at the last created office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 29 December 2030 | 29 December 2030         | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "29 December 2030" with "1000" amount and expected disbursement date on "29 December 2030"
    And Admin successfully disburse the Working Capital loan on "29 December 2030" with "1000" EUR transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "29 December 2030" due date and 50.0 transaction amount
    When Admin sets the business date to "30 December 2030"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Trial Balance Summary Report for Working Capital Loans for date "31 December 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-12-31  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
      | 2030-12-31  | WCLP_ACC_DEF_REV_AM | 112603 | Interest/Fee Receivable   | self       | 50.0             | 0.0           | 0.0            | 50.0          |                         |
      | 2030-12-31  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2030-12-31  | WCLP_ACC_DEF_REV_AM | 404007 | Fee Income                | self       | -50.0            | 0.0           | 0.0            | -50.0         |                         |
    When Admin sets the business date to "02 January 2031"
    And Admin runs the Retained Earning Job
    Then Trial Balance Summary Report for Working Capital Loans for date "01 January 2031" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description                  | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2031-01-01  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable             | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
      | 2031-01-01  | WCLP_ACC_DEF_REV_AM | 112603 | Interest/Fee Receivable      | self       | 50.0             | 0.0           | 0.0            | 50.0          |                         |
      | 2031-01-01  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account    | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2031-01-01  | WCLP_ACC_DEF_REV_AM | 320000 | Retained Earnings Prior Year | self       | -50.0            | 0.0           | 0.0            | -50.0         |                         |
    And The journal entry annual summary table contains 1 row for GL code "320000" with year end date "31 December 2030" and product type "WORKING_CAPITAL_LOAN"
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "05 January 2031" due date and 20.0 transaction amount
    When Admin sets the business date to "06 January 2031"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Trial Balance Summary Report for Working Capital Loans for date "05 January 2031" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description                  | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2031-01-05  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable             | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
      | 2031-01-05  | WCLP_ACC_DEF_REV_AM | 112603 | Interest/Fee Receivable      | self       | 50.0             | 20.0          | 0.0            | 70.0          |                         |
      | 2031-01-05  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account    | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2031-01-05  | WCLP_ACC_DEF_REV_AM | 320000 | Retained Earnings Prior Year | self       | -50.0            | 0.0           | 0.0            | -50.0         |                         |
      | 2031-01-05  | WCLP_ACC_DEF_REV_AM | 404007 | Fee Income                   | self       | 0.0              | 0.0           | -20.0          | -20.0         |                         |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "06 January 2031"

  @TestRailId:C115384
  Scenario: Verify year-end close of an office with a regular loan and a working capital loan closes each product type into its own report - UC3
    # Regular and WC product ids can be the same number, so the annual summary keeps the product type and each
    # report only reads the retained earnings of its own product type.
    Given any existing year-end retained earnings close for fiscal year ending "31 December 2031" is removed
    When Admin sets the business date to "01 December 2031"
    And Admin creates a new office
    And Admin points the Retained Earning Job at the last created office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 December 2031 | 01 December 2031         | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 December 2031" with "1000" amount and expected disbursement date on "01 December 2031"
    And Admin successfully disburse the Working Capital loan on "01 December 2031" with "1000" EUR transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "01 December 2031" due date and 50.0 transaction amount
    When Admin sets the business date to "02 December 2031"
    And Admin runs inline COB job for Working Capital Loan by loanId
    # The regular loan steps keep the last created loan in the same test context as the WC steps, so the regular loan
    # is created after the WC loan is done, with the business date moved back to the same disbursement date.
    When Admin sets the business date to "01 December 2031"
    And Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 December 2031  | 100            | 26                     | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 December 2031" with "100" amount and expected disbursement date on "01 December 2031"
    And Admin successfully disburse the loan on "01 December 2031" with "100" EUR transaction amount
    And Admin adds "LOAN_SNOOZE_FEE" due date charge with "15 December 2031" due date and 10 EUR transaction amount
    When Admin sets the business date to "31 December 2031"
    And Admin runs inline COB job for Loan
    Then Trial Balance Summary Report for Working Capital Loans for date "31 December 2031" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2031-12-31  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
      | 2031-12-31  | WCLP_ACC_DEF_REV_AM | 112603 | Interest/Fee Receivable   | self       | 50.0             | 0.0           | 0.0            | 50.0          |                         |
      | 2031-12-31  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2031-12-31  | WCLP_ACC_DEF_REV_AM | 404007 | Fee Income                | self       | -50.0            | 0.0           | 0.0            | -50.0         |                         |
    Then Trial Balance Summary Report with Asset Owner for date "31 December 2031" has originatorId, asset owner externalId and the following data:
      | PostingDate | Product                                                       | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2031-12-31  | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 112601 | Loans Receivable          | self       | 100.0            | 0.0           | 0.0            | 100.0         |                         |
      | 2031-12-31  | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 112603 | Interest/Fee Receivable   | self       | 12.03            | 0.0           | 0.0            | 12.03         |                         |
      | 2031-12-31  | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 145023 | Suspense/Clearing account | self       | -100.0           | 0.0           | 0.0            | -100.0        |                         |
      | 2031-12-31  | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 404000 | Interest Income           | self       | -2.03            | 0.0           | 0.0            | -2.03         |                         |
      | 2031-12-31  | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 404007 | Fee Income                | self       | -10.0            | 0.0           | 0.0            | -10.0         |                         |
    When Admin sets the business date to "02 January 2032"
    And Admin runs the Retained Earning Job
    Then Trial Balance Summary Report for Working Capital Loans for date "01 January 2032" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description                  | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2032-01-01  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable             | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
      | 2032-01-01  | WCLP_ACC_DEF_REV_AM | 112603 | Interest/Fee Receivable      | self       | 50.0             | 0.0           | 0.0            | 50.0          |                         |
      | 2032-01-01  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account    | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2032-01-01  | WCLP_ACC_DEF_REV_AM | 320000 | Retained Earnings Prior Year | self       | -50.0            | 0.0           | 0.0            | -50.0         |                         |
    Then Trial Balance Summary Report with Asset Owner for date "01 January 2032" has originatorId, asset owner externalId and the following data:
      | PostingDate | Product                                                       | glAcct | Description                  | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2032-01-01  | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 112601 | Loans Receivable             | self       | 100.0            | 0.0           | 0.0            | 100.0         |                         |
      | 2032-01-01  | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 112603 | Interest/Fee Receivable      | self       | 12.03            | 0.0           | 0.0            | 12.03         |                         |
      | 2032-01-01  | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 145023 | Suspense/Clearing account    | self       | -100.0           | 0.0           | 0.0            | -100.0        |                         |
      | 2032-01-01  | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 320000 | Retained Earnings Prior Year | self       | -12.03           | 0.0           | 0.0            | -12.03        |                         |
    And The journal entry annual summary table contains 1 row for GL code "320000" with year end date "31 December 2031" and product type "LOAN"
    And The journal entry annual summary table contains 1 row for GL code "320000" with year end date "31 December 2031" and product type "WORKING_CAPITAL_LOAN"
    When Admin runs the Retained Earning Job
    And The journal entry annual summary table contains 1 row for GL code "320000" with year end date "31 December 2031" and product type "LOAN"
    And The journal entry annual summary table contains 1 row for GL code "320000" with year end date "31 December 2031" and product type "WORKING_CAPITAL_LOAN"

  @TestRailId:C115385
  Scenario: Verify two consecutive working capital year-end closes accumulate the Retained Earnings Prior Year balance - UC4
    Given any existing year-end retained earnings close for fiscal year ending "31 December 2032" is removed
    Given any existing year-end retained earnings close for fiscal year ending "31 December 2033" is removed
    When Admin sets the business date to "29 December 2032"
    And Admin creates a new office
    And Admin points the Retained Earning Job at the last created office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 29 December 2032 | 29 December 2032         | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "29 December 2032" with "1000" amount and expected disbursement date on "29 December 2032"
    And Admin successfully disburse the Working Capital loan on "29 December 2032" with "1000" EUR transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "29 December 2032" due date and 50.0 transaction amount
    When Admin sets the business date to "30 December 2032"
    And Admin runs inline COB job for Working Capital Loan by loanId
    When Admin sets the business date to "02 January 2033"
    And Admin runs the Retained Earning Job
    Then Trial Balance Summary Report for Working Capital Loans for date "01 January 2033" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description                  | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2033-01-01  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable             | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
      | 2033-01-01  | WCLP_ACC_DEF_REV_AM | 112603 | Interest/Fee Receivable      | self       | 50.0             | 0.0           | 0.0            | 50.0          |                         |
      | 2033-01-01  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account    | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2033-01-01  | WCLP_ACC_DEF_REV_AM | 320000 | Retained Earnings Prior Year | self       | -50.0            | 0.0           | 0.0            | -50.0         |                         |
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "02 January 2033" due date and 30.0 transaction amount
    When Admin sets the business date to "03 January 2033"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Trial Balance Summary Report for Working Capital Loans for date "31 December 2033" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description                  | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2033-12-31  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable             | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
      | 2033-12-31  | WCLP_ACC_DEF_REV_AM | 112603 | Interest/Fee Receivable      | self       | 80.0             | 0.0           | 0.0            | 80.0          |                         |
      | 2033-12-31  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account    | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2033-12-31  | WCLP_ACC_DEF_REV_AM | 320000 | Retained Earnings Prior Year | self       | -50.0            | 0.0           | 0.0            | -50.0         |                         |
      | 2033-12-31  | WCLP_ACC_DEF_REV_AM | 404007 | Fee Income                   | self       | -30.0            | 0.0           | 0.0            | -30.0         |                         |
    When Admin sets the business date to "02 January 2034"
    And Admin runs the Retained Earning Job
    Then Trial Balance Summary Report for Working Capital Loans for date "01 January 2034" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description                  | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2034-01-01  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable             | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
      | 2034-01-01  | WCLP_ACC_DEF_REV_AM | 112603 | Interest/Fee Receivable      | self       | 80.0             | 0.0           | 0.0            | 80.0          |                         |
      | 2034-01-01  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account    | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2034-01-01  | WCLP_ACC_DEF_REV_AM | 320000 | Retained Earnings Prior Year | self       | -80.0            | 0.0           | 0.0            | -80.0         |                         |
    And The journal entry annual summary table contains 1 row for GL code "320000" with year end date "31 December 2032" and product type "WORKING_CAPITAL_LOAN"
    And The journal entry annual summary table contains 1 row for GL code "320000" with year end date "31 December 2033" and product type "WORKING_CAPITAL_LOAN"
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "02 January 2034"

  @TestRailId:C115386
  Scenario: Verify working capital year-end close also closes the expense accounts posted by a charge-off - UC5
    Given any existing year-end retained earnings close for fiscal year ending "31 December 2034" is removed
    When Admin sets the business date to "01 December 2034"
    And Admin creates a new office
    And Admin points the Retained Earning Job at the last created office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 December 2034 | 01 December 2034         | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 December 2034" with "1000" amount and expected disbursement date on "01 December 2034"
    And Admin successfully disburse the Working Capital loan on "01 December 2034" with "1000" EUR transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "02 December 2034" due date and 50.0 transaction amount
    When Admin sets the business date to "03 December 2034"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin charges off the Working Capital loan on "03 December 2034"
    Then Trial Balance Summary Report for Working Capital Loans for date "31 December 2034" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2034-12-31  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2034-12-31  | WCLP_ACC_DEF_REV_AM | 404007 | Fee Income                | self       | -50.0            | 0.0           | 0.0            | -50.0         |                         |
      | 2034-12-31  | WCLP_ACC_DEF_REV_AM | 404008 | Fee Charge Off            | self       | 50.0             | 0.0           | 0.0            | 50.0          |                         |
      | 2034-12-31  | WCLP_ACC_DEF_REV_AM | 744007 | Credit Loss/Bad Debt      | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
    When Admin sets the business date to "02 January 2035"
    And Admin runs the Retained Earning Job
    Then Trial Balance Summary Report for Working Capital Loans for date "01 January 2035" shows GL account "404007" closed out
    And Trial Balance Summary Report for Working Capital Loans for date "01 January 2035" shows GL account "404008" closed out
    And Trial Balance Summary Report for Working Capital Loans for date "01 January 2035" shows GL account "744007" closed out
    Then Trial Balance Summary Report for Working Capital Loans for date "01 January 2035" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description                  | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2035-01-01  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account    | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2035-01-01  | WCLP_ACC_DEF_REV_AM | 320000 | Retained Earnings Prior Year | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "02 January 2035"

  @TestRailId:C115387
  Scenario: Verify working capital year-end close keeps the originator of the loan on the retained earnings row - UC6
    Given any existing year-end retained earnings close for fiscal year ending "31 December 2035" is removed
    When Admin sets the business date to "01 December 2035"
    And Admin creates a new office
    And Admin points the Retained Earning Job at the last created office
    And Admin creates a client with random data in the last created office
    And Admin creates a new loan originator with external ID and name "WC Year End Originator"
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 December 2035 | 01 December 2035         | 1000            | 100000             | 18                | 0        |
    And Admin attaches the originator to the working capital loan
    And Admin successfully approves the working capital loan on "01 December 2035" with "1000" amount and expected disbursement date on "01 December 2035"
    And Admin successfully disburse the Working Capital loan on "01 December 2035" with "1000" EUR transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "01 December 2035" due date and 50.0 transaction amount
    When Admin sets the business date to "02 December 2035"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Trial Balance Summary Report for Working Capital Loans for date "31 December 2035" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2035-12-31  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | 0.0            | 1000.0        | originator_external_id  |
      | 2035-12-31  | WCLP_ACC_DEF_REV_AM | 112603 | Interest/Fee Receivable   | self       | 50.0             | 0.0           | 0.0            | 50.0          | originator_external_id  |
      | 2035-12-31  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       | originator_external_id  |
      | 2035-12-31  | WCLP_ACC_DEF_REV_AM | 404007 | Fee Income                | self       | -50.0            | 0.0           | 0.0            | -50.0         | originator_external_id  |
    When Admin sets the business date to "02 January 2036"
    And Admin runs the Retained Earning Job
    Then Trial Balance Summary Report for Working Capital Loans for date "01 January 2036" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description                  | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2036-01-01  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable             | self       | 1000.0           | 0.0           | 0.0            | 1000.0        | originator_external_id  |
      | 2036-01-01  | WCLP_ACC_DEF_REV_AM | 112603 | Interest/Fee Receivable      | self       | 50.0             | 0.0           | 0.0            | 50.0          | originator_external_id  |
      | 2036-01-01  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account    | self       | -1000.0          | 0.0           | 0.0            | -1000.0       | originator_external_id  |
      | 2036-01-01  | WCLP_ACC_DEF_REV_AM | 320000 | Retained Earnings Prior Year | self       | -50.0            | 0.0           | 0.0            | -50.0         | originator_external_id  |
    And The journal entry annual summary table contains 1 row for GL code "320000" with year end date "31 December 2035" and product type "WORKING_CAPITAL_LOAN"
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "02 January 2036"
