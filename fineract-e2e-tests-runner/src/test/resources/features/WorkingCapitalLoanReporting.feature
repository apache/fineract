@WorkingCapital
@WorkingCapitalLoanReportingFeature
Feature: Working Capital Loan Reporting

  # The journal entry aggregation job tracks the latest aggregated date tenant-wide. Each scenario uses its own month,
  # later than the regular loan Reporting.feature, so the aggregation of one scenario does not hide the entries of
  # another. Like Reporting.feature, a scenario cannot be rerun on the same database once its dates are aggregated.

  @TestRailId:C115365
  Scenario: Verify Trial Balance Summary Report for Working Capital Loans with originator before and after journal entry aggregation - UC1
    When Admin sets the business date to "01 May 2029"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a new loan originator with external ID and name "WC Trial Balance Originator"
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 May 2029     | 01 May 2029              | 1000            | 100000             | 18                | 0        |
    And Admin attaches the originator to the working capital loan
    And Admin successfully approves the working capital loan on "01 May 2029" with "1000" amount and expected disbursement date on "01 May 2029"
    And Admin successfully disburse the Working Capital loan on "01 May 2029" with "1000" EUR transaction amount
    When Admin sets the business date to "03 May 2029"
    Then Trial Balance Summary Report for Working Capital Loans for date "01 May 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-05-01  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 0.0              | 1000.0        | 0.0            | 1000.0        | originator_external_id  |
      | 2029-05-01  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | 0.0              | 0.0           | -1000.0        | -1000.0       | originator_external_id  |
    Then Trial Balance Summary Report for Working Capital Loans for date "03 May 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-05-03  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | 0.0            | 1000.0        | originator_external_id  |
      | 2029-05-03  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       | originator_external_id  |
    And Trial Balance Summary Report with Asset Owner for date "03 May 2029" has no rows
    When Admin runs the "JOURNAL_ENTRY_AGGREGATION" job
    Then Trial Balance Summary Report for Working Capital Loans for date "01 May 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-05-01  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 0.0              | 1000.0        | 0.0            | 1000.0        | originator_external_id  |
      | 2029-05-01  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | 0.0              | 0.0           | -1000.0        | -1000.0       | originator_external_id  |
    Then Trial Balance Summary Report for Working Capital Loans for date "03 May 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-05-03  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | 0.0            | 1000.0        | originator_external_id  |
      | 2029-05-03  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       | originator_external_id  |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "03 May 2029"

  @TestRailId:C115366
  Scenario: Verify Trial Balance Summary Report for Working Capital Loans with repayments on multiple days before and after journal entry aggregation - UC2
    When Admin sets the business date to "01 June 2029"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 June 2029    | 01 June 2029             | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 June 2029" with "1000" amount and expected disbursement date on "01 June 2029"
    And Admin successfully disburse the Working Capital loan on "01 June 2029" with "1000" EUR transaction amount
    When Admin sets the business date to "22 June 2029"
    And Customer makes repayment on "22 June 2029" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "26 June 2029"
    And Customer makes repayment on "26 June 2029" with 300.0 transaction amount on Working Capital loan
    Then Trial Balance Summary Report for Working Capital Loans for date "22 June 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-06-22  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | -200.0         | 800.0         |                         |
      | 2029-06-22  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 200.0         | 0.0            | -800.0        |                         |
    Then Trial Balance Summary Report for Working Capital Loans for date "26 June 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-06-26  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 800.0            | 0.0           | -300.0         | 500.0         |                         |
      | 2029-06-26  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -800.0           | 300.0         | 0.0            | -500.0        |                         |
    When Admin sets the business date to "28 June 2029"
    And Admin runs the "JOURNAL_ENTRY_AGGREGATION" job
    Then Trial Balance Summary Report for Working Capital Loans for date "26 June 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-06-26  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 800.0            | 0.0           | -300.0         | 500.0         |                         |
      | 2029-06-26  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -800.0           | 300.0         | 0.0            | -500.0        |                         |
    Then Trial Balance Summary Report for Working Capital Loans for date "28 June 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-06-28  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 500.0            | 0.0           | 0.0            | 500.0         |                         |
      | 2029-06-28  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -500.0           | 0.0           | 0.0            | -500.0        |                         |
    And Trial Balance Summary Report with Asset Owner for date "28 June 2029" has no rows
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "28 June 2029"

  @TestRailId:C115367
  Scenario: Verify Trial Balance Summary Report for Working Capital Loans with fee accrual and charge-off - UC3
    When Admin sets the business date to "01 July 2029"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 July 2029    | 01 July 2029             | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 July 2029" with "1000" amount and expected disbursement date on "01 July 2029"
    And Admin successfully disburse the Working Capital loan on "01 July 2029" with "1000" EUR transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "02 July 2029" due date and 50.0 transaction amount
    When Admin sets the business date to "03 July 2029"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Trial Balance Summary Report for Working Capital Loans for date "02 July 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-07-02  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
      | 2029-07-02  | WCLP_ACC_DEF_REV_AM | 112603 | Interest/Fee Receivable   | self       | 0.0              | 50.0          | 0.0            | 50.0          |                         |
      | 2029-07-02  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2029-07-02  | WCLP_ACC_DEF_REV_AM | 404007 | Fee Income                | self       | 0.0              | 0.0           | -50.0          | -50.0         |                         |
    When Admin sets the business date to "04 July 2029"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin charges off the Working Capital loan on "04 July 2029"
    Then Trial Balance Summary Report for Working Capital Loans for date "04 July 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-07-04  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | -1000.0        | 0.0           |                         |
      | 2029-07-04  | WCLP_ACC_DEF_REV_AM | 112603 | Interest/Fee Receivable   | self       | 50.0             | 0.0           | -50.0          | 0.0           |                         |
      | 2029-07-04  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2029-07-04  | WCLP_ACC_DEF_REV_AM | 404007 | Fee Income                | self       | -50.0            | 0.0           | 0.0            | -50.0         |                         |
      | 2029-07-04  | WCLP_ACC_DEF_REV_AM | 404008 | Fee Charge Off            | self       | 0.0              | 50.0          | 0.0            | 50.0          |                         |
      | 2029-07-04  | WCLP_ACC_DEF_REV_AM | 744007 | Credit Loss/Bad Debt      | self       | 0.0              | 1000.0        | 0.0            | 1000.0        |                         |
    Then Trial Balance Summary Report for Working Capital Loans for date "05 July 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-07-05  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2029-07-05  | WCLP_ACC_DEF_REV_AM | 404007 | Fee Income                | self       | -50.0            | 0.0           | 0.0            | -50.0         |                         |
      | 2029-07-05  | WCLP_ACC_DEF_REV_AM | 404008 | Fee Charge Off            | self       | 50.0             | 0.0           | 0.0            | 50.0          |                         |
      | 2029-07-05  | WCLP_ACC_DEF_REV_AM | 744007 | Credit Loss/Bad Debt      | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
    And Trial Balance Summary Report with Asset Owner for date "05 July 2029" has no rows
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "04 July 2029"

  @TestRailId:C115368
  Scenario: Verify Trial Balance Summary Report for Working Capital Loans keeps two working capital products apart before and after journal entry aggregation - UC4
    When Admin sets the business date to "01 August 2029"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 August 2029  | 01 August 2029           | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 August 2029" with "1000" amount and expected disbursement date on "01 August 2029"
    And Admin successfully disburse the Working Capital loan on "01 August 2029" with "1000" EUR transaction amount
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 August 2029  | 01 August 2029           | 2000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 August 2029" with "2000" amount and expected disbursement date on "01 August 2029"
    And Admin successfully disburse the Working Capital loan on "01 August 2029" with "2000" EUR transaction amount
    When Admin sets the business date to "10 August 2029"
    And Customer makes repayment on "10 August 2029" with 500.0 transaction amount on Working Capital loan
    Then Trial Balance Summary Report for Working Capital Loans for date "01 August 2029" has originatorId and the following data:
      | PostingDate | Product                  | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-08-01  | WCLP_ACC_DEF_REV_AM      | 112601 | Loans Receivable          | self       | 0.0              | 1000.0        | 0.0            | 1000.0        |                         |
      | 2029-08-01  | WCLP_ADVANCED_ACCOUNTING | 112601 | Loans Receivable          | self       | 0.0              | 2000.0        | 0.0            | 2000.0        |                         |
      | 2029-08-01  | WCLP_ACC_DEF_REV_AM      | 145023 | Suspense/Clearing account | self       | 0.0              | 0.0           | -1000.0        | -1000.0       |                         |
      | 2029-08-01  | WCLP_ADVANCED_ACCOUNTING | 145023 | Suspense/Clearing account | self       | 0.0              | 0.0           | -2000.0        | -2000.0       |                         |
    Then Trial Balance Summary Report for Working Capital Loans for date "10 August 2029" has originatorId and the following data:
      | PostingDate | Product                  | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-08-10  | WCLP_ACC_DEF_REV_AM      | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
      | 2029-08-10  | WCLP_ADVANCED_ACCOUNTING | 112601 | Loans Receivable          | self       | 2000.0           | 0.0           | -500.0         | 1500.0        |                         |
      | 2029-08-10  | WCLP_ACC_DEF_REV_AM      | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2029-08-10  | WCLP_ADVANCED_ACCOUNTING | 145023 | Suspense/Clearing account | self       | -2000.0          | 500.0         | 0.0            | -1500.0       |                         |
    When Admin sets the business date to "12 August 2029"
    And Admin runs the "JOURNAL_ENTRY_AGGREGATION" job
    Then Trial Balance Summary Report for Working Capital Loans for date "10 August 2029" has originatorId and the following data:
      | PostingDate | Product                  | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-08-10  | WCLP_ACC_DEF_REV_AM      | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
      | 2029-08-10  | WCLP_ADVANCED_ACCOUNTING | 112601 | Loans Receivable          | self       | 2000.0           | 0.0           | -500.0         | 1500.0        |                         |
      | 2029-08-10  | WCLP_ACC_DEF_REV_AM      | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2029-08-10  | WCLP_ADVANCED_ACCOUNTING | 145023 | Suspense/Clearing account | self       | -2000.0          | 500.0         | 0.0            | -1500.0       |                         |
    Then Trial Balance Summary Report for Working Capital Loans for date "12 August 2029" has originatorId and the following data:
      | PostingDate | Product                  | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-08-12  | WCLP_ACC_DEF_REV_AM      | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
      | 2029-08-12  | WCLP_ADVANCED_ACCOUNTING | 112601 | Loans Receivable          | self       | 1500.0           | 0.0           | 0.0            | 1500.0        |                         |
      | 2029-08-12  | WCLP_ACC_DEF_REV_AM      | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2029-08-12  | WCLP_ADVANCED_ACCOUNTING | 145023 | Suspense/Clearing account | self       | -1500.0          | 0.0           | 0.0            | -1500.0       |                         |
    And Trial Balance Summary Report with Asset Owner for date "12 August 2029" has no rows

  @TestRailId:C115372
  Scenario: Verify Trial Balance Summary Report for Working Capital Loans and Trial Balance Summary Report with Asset Owner give the same balances for matching regular and WC loans in one office - UC5
    When Admin sets the business date to "01 September 2029"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate   | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 September 2029 | 01 September 2029        | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 September 2029" with "1000" amount and expected disbursement date on "01 September 2029"
    And Admin successfully disburse the Working Capital loan on "01 September 2029" with "1000" EUR transaction amount
    When Admin sets the business date to "10 September 2029"
    And Customer makes repayment on "10 September 2029" with 200.0 transaction amount on Working Capital loan
    When Admin sets the business date to "01 September 2029"
    And Admin creates a new default Loan with date: "01 September 2029"
    And Admin successfully approves the loan on "01 September 2029" with "1000" amount and expected disbursement date on "01 September 2029"
    And Admin successfully disburse the loan on "01 September 2029" with "1000" EUR transaction amount
    When Admin sets the business date to "10 September 2029"
    And Customer makes "AUTOPAY" repayment on "10 September 2029" with 200 EUR transaction amount
    Then Trial Balance Summary Report for Working Capital Loans for date "10 September 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-09-10  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | -200.0         | 800.0         |                         |
      | 2029-09-10  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 200.0         | 0.0            | -800.0        |                         |
    Then Trial Balance Summary Report with Asset Owner for date "10 September 2029" has originatorId, asset owner externalId and the following data:
      | PostingDate | Product | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-09-10  | LP1     | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | -200.0         | 800.0         |                         |
      | 2029-09-10  | LP1     | 145023 | Suspense/Clearing account | self       | -1000.0          | 200.0         | 0.0            | -800.0        |                         |
    When Admin sets the business date to "12 September 2029"
    And Admin runs the "JOURNAL_ENTRY_AGGREGATION" job
    Then Trial Balance Summary Report for Working Capital Loans for date "12 September 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-09-12  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 800.0            | 0.0           | 0.0            | 800.0         |                         |
      | 2029-09-12  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -800.0           | 0.0           | 0.0            | -800.0        |                         |
    Then Trial Balance Summary Report with Asset Owner for date "12 September 2029" has originatorId, asset owner externalId and the following data:
      | PostingDate | Product | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-09-12  | LP1     | 112601 | Loans Receivable          | self       | 800.0            | 0.0           | 0.0            | 800.0         |                         |
      | 2029-09-12  | LP1     | 145023 | Suspense/Clearing account | self       | -800.0           | 0.0           | 0.0            | -800.0        |                         |

  @TestRailId:C115373
  Scenario: Verify Trial Balance Summary Report for Working Capital Loans only shows the selected office before and after journal entry aggregation - UC6
    When Admin sets the business date to "01 October 2029"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 October 2029 | 01 October 2029          | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 October 2029" with "1000" amount and expected disbursement date on "01 October 2029"
    And Admin successfully disburse the Working Capital loan on "01 October 2029" with "1000" EUR transaction amount
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 October 2029 | 01 October 2029          | 3000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 October 2029" with "3000" amount and expected disbursement date on "01 October 2029"
    And Admin successfully disburse the Working Capital loan on "01 October 2029" with "3000" EUR transaction amount
    Then Trial Balance Summary Report for Working Capital Loans for date "01 October 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-10-01  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 0.0              | 3000.0        | 0.0            | 3000.0        |                         |
      | 2029-10-01  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | 0.0              | 0.0           | -3000.0        | -3000.0       |                         |
    Then Trial Balance Summary Report for Working Capital Loans for date "03 October 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-10-03  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 3000.0           | 0.0           | 0.0            | 3000.0        |                         |
      | 2029-10-03  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -3000.0          | 0.0           | 0.0            | -3000.0       |                         |
    When Admin sets the business date to "03 October 2029"
    And Admin runs the "JOURNAL_ENTRY_AGGREGATION" job
    Then Trial Balance Summary Report for Working Capital Loans for date "01 October 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-10-01  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 0.0              | 3000.0        | 0.0            | 3000.0        |                         |
      | 2029-10-01  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | 0.0              | 0.0           | -3000.0        | -3000.0       |                         |
    Then Trial Balance Summary Report for Working Capital Loans for date "03 October 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-10-03  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 3000.0           | 0.0           | 0.0            | 3000.0        |                         |
      | 2029-10-03  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -3000.0          | 0.0           | 0.0            | -3000.0       |                         |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "03 October 2029"

  @TestRailId:C115374
  Scenario: Verify Trial Balance Summary Report for Working Capital Loans combines aggregated data with journal entries posted after the aggregation - UC7
    When Admin sets the business date to "01 November 2029"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 November 2029 | 01 November 2029         | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 November 2029" with "1000" amount and expected disbursement date on "01 November 2029"
    And Admin successfully disburse the Working Capital loan on "01 November 2029" with "1000" EUR transaction amount
    When Admin sets the business date to "05 November 2029"
    And Admin runs the "JOURNAL_ENTRY_AGGREGATION" job
    When Admin sets the business date to "10 November 2029"
    And Customer makes repayment on "10 November 2029" with 300.0 transaction amount on Working Capital loan
    Then Trial Balance Summary Report for Working Capital Loans for date "10 November 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-11-10  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | -300.0         | 700.0         |                         |
      | 2029-11-10  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 300.0         | 0.0            | -700.0        |                         |
    Then Trial Balance Summary Report for Working Capital Loans for date "12 November 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-11-12  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 700.0            | 0.0           | 0.0            | 700.0         |                         |
      | 2029-11-12  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -700.0           | 0.0           | 0.0            | -700.0        |                         |
    When Admin sets the business date to "15 November 2029"
    And Customer makes repayment on "15 November 2029" with 200.0 transaction amount on Working Capital loan
    Then Trial Balance Summary Report for Working Capital Loans for date "15 November 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-11-15  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 700.0            | 0.0           | -200.0         | 500.0         |                         |
      | 2029-11-15  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -700.0           | 200.0         | 0.0            | -500.0        |                         |
    When Admin sets the business date to "20 November 2029"
    And Admin runs the "JOURNAL_ENTRY_AGGREGATION" job
    Then Trial Balance Summary Report for Working Capital Loans for date "12 November 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-11-12  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 700.0            | 0.0           | 0.0            | 700.0         |                         |
      | 2029-11-12  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -700.0           | 0.0           | 0.0            | -700.0        |                         |
    Then Trial Balance Summary Report for Working Capital Loans for date "15 November 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-11-15  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 700.0            | 0.0           | -200.0         | 500.0         |                         |
      | 2029-11-15  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -700.0           | 200.0         | 0.0            | -500.0        |                         |
    Then Trial Balance Summary Report for Working Capital Loans for date "20 November 2029" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-11-20  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 500.0            | 0.0           | 0.0            | 500.0         |                         |
      | 2029-11-20  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -500.0           | 0.0           | 0.0            | -500.0        |                         |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "20 November 2029"

  @TestRailId:C115375
  Scenario: Verify Trial Balance Summary Report for Working Capital Loans with discount fee and discount fee amortization - UC8
    When Admin sets the business date to "01 December 2029"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 December 2029 | 01 December 2029         | 9000            | 100000             | 18                |          |
    And Admin successfully approves the working capital loan on "01 December 2029" with "9000" amount and expected disbursement date on "01 December 2029"
    And Admin successfully disburse the Working Capital loan on "01 December 2029" with "9000" EUR transaction amount
    And Admin successfully add discount with "1000" amount on Working Capital loan account
    Then Trial Balance Summary Report for Working Capital Loans for date "01 December 2029" has originatorId and the following data:
      | PostingDate | Product                  | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-12-01  | WCLP_ADVANCED_ACCOUNTING | 112601 | Loans Receivable          | self       | 0.0              | 10000.0       | 0.0            | 10000.0       |                         |
      | 2029-12-01  | WCLP_ADVANCED_ACCOUNTING | 145023 | Suspense/Clearing account | self       | 0.0              | 0.0           | -9000.0        | -9000.0       |                         |
      | 2029-12-01  | WCLP_ADVANCED_ACCOUNTING | 240005 | Deferred Interest Revenue | self       | 0.0              | 0.0           | -1000.0        | -1000.0       |                         |
    When Admin sets the business date to "05 December 2029"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "05 December 2029" with 150 transaction amount on Working Capital loan
    When Admin sets the business date to "08 December 2029"
    And Admin runs inline COB job for Working Capital Loan by loanId
    Then Trial Balance Summary Report for Working Capital Loans for date "05 December 2029" has originatorId and the following data:
      | PostingDate | Product                  | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-12-05  | WCLP_ADVANCED_ACCOUNTING | 112601 | Loans Receivable          | self       | 10000.0          | 0.0           | -150.0         | 9850.0        |                         |
      | 2029-12-05  | WCLP_ADVANCED_ACCOUNTING | 145023 | Suspense/Clearing account | self       | -9000.0          | 150.0         | 0.0            | -8850.0       |                         |
      | 2029-12-05  | WCLP_ADVANCED_ACCOUNTING | 240005 | Deferred Interest Revenue | self       | -1000.0          | 28.7          | 0.0            | -971.3        |                         |
      | 2029-12-05  | WCLP_ADVANCED_ACCOUNTING | 404000 | Interest Income           | self       | 0.0              | 0.0           | -28.7          | -28.7         |                         |
    Then Trial Balance Summary Report for Working Capital Loans for date "08 December 2029" has originatorId and the following data:
      | PostingDate | Product                  | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2029-12-08  | WCLP_ADVANCED_ACCOUNTING | 112601 | Loans Receivable          | self       | 9850.0           | 0.0           | 0.0            | 9850.0        |                         |
      | 2029-12-08  | WCLP_ADVANCED_ACCOUNTING | 145023 | Suspense/Clearing account | self       | -8850.0          | 0.0           | 0.0            | -8850.0       |                         |
      | 2029-12-08  | WCLP_ADVANCED_ACCOUNTING | 240005 | Deferred Interest Revenue | self       | -971.3           | 0.0           | 0.0            | -971.3        |                         |
      | 2029-12-08  | WCLP_ADVANCED_ACCOUNTING | 404000 | Interest Income           | self       | -28.7            | 0.0           | 0.0            | -28.7         |                         |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "08 December 2029"

  @TestRailId:C115376
  Scenario: Verify Trial Balance Summary Report for Working Capital Loans with write-off and undo write-off - UC9
    When Admin sets the business date to "01 January 2030"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 January 2030 | 01 January 2030          | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2030" with "1000" amount and expected disbursement date on "01 January 2030"
    And Admin successfully disburse the Working Capital loan on "01 January 2030" with "1000" EUR transaction amount
    When Admin sets the business date to "15 January 2030"
    And Admin writes off the Working Capital loan on "15 January 2030"
    Then Trial Balance Summary Report for Working Capital Loans for date "15 January 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-01-15  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | -1000.0        | 0.0           |                         |
      | 2030-01-15  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2030-01-15  | WCLP_ACC_DEF_REV_AM | e4     | Written off               | self       | 0.0              | 1000.0        | 0.0            | 1000.0        |                         |
    When Admin sets the business date to "16 January 2030"
    And Admin undoes the write-off on the Working Capital loan
    Then Trial Balance Summary Report for Working Capital Loans for date "15 January 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-01-15  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | -1000.0        | 0.0           |                         |
      | 2030-01-15  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2030-01-15  | WCLP_ACC_DEF_REV_AM | e4     | Written off               | self       | 0.0              | 1000.0        | 0.0            | 1000.0        |                         |
    Then Trial Balance Summary Report for Working Capital Loans for date "16 January 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-01-16  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 0.0              | 1000.0        | 0.0            | 1000.0        |                         |
      | 2030-01-16  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2030-01-16  | WCLP_ACC_DEF_REV_AM | e4     | Written off               | self       | 1000.0           | 0.0           | -1000.0        | 0.0           |                         |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "16 January 2030"

  @TestRailId:C115377
  Scenario: Verify Trial Balance Summary Report for Working Capital Loans with recovery payment after write-off - UC10
    When Admin sets the business date to "01 February 2030"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate  | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 February 2030 | 01 February 2030         | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 February 2030" with "1000" amount and expected disbursement date on "01 February 2030"
    And Admin successfully disburse the Working Capital loan on "01 February 2030" with "1000" EUR transaction amount
    When Admin sets the business date to "10 February 2030"
    And Admin writes off the Working Capital loan on "10 February 2030"
    When Admin sets the business date to "15 February 2030"
    And Admin makes a recovery payment of "400" on the Working Capital loan on "15 February 2030"
    Then Trial Balance Summary Report for Working Capital Loans for date "15 February 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-02-15  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 400.0         | 0.0            | -600.0        |                         |
      | 2030-02-15  | WCLP_ACC_DEF_REV_AM | 744008 | Recoveries                | self       | 0.0              | 0.0           | -400.0         | -400.0        |                         |
      | 2030-02-15  | WCLP_ACC_DEF_REV_AM | e4     | Written off               | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
    Then Trial Balance Summary Report for Working Capital Loans for date "16 February 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-02-16  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -600.0           | 0.0           | 0.0            | -600.0        |                         |
      | 2030-02-16  | WCLP_ACC_DEF_REV_AM | 744008 | Recoveries                | self       | -400.0           | 0.0           | 0.0            | -400.0        |                         |
      | 2030-02-16  | WCLP_ACC_DEF_REV_AM | e4     | Written off               | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |

  @TestRailId:C115378
  Scenario: Verify Trial Balance Summary Report for Working Capital Loans with overpayment and credit balance refund - UC11
    When Admin sets the business date to "01 March 2030"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 March 2030   | 01 March 2030            | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 March 2030" with "1000" amount and expected disbursement date on "01 March 2030"
    And Admin successfully disburse the Working Capital loan on "01 March 2030" with "1000" EUR transaction amount
    When Admin sets the business date to "02 March 2030"
    And Customer makes repayment on "02 March 2030" with 1100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "OVERPAID"
    Then Trial Balance Summary Report for Working Capital Loans for date "02 March 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-03-02  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | -1000.0        | 0.0           |                         |
      | 2030-03-02  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 1100.0        | 0.0            | 100.0         |                         |
      | 2030-03-02  | WCLP_ACC_DEF_REV_AM | 245000 | Other Credit Liability    | self       | 0.0              | 0.0           | -100.0         | -100.0        |                         |
    When Admin sets the business date to "03 March 2030"
    And Customer makes credit balance refund on "03 March 2030" with 100.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Trial Balance Summary Report for Working Capital Loans for date "03 March 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-03-03  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | 100.0            | 0.0           | -100.0         | 0.0           |                         |
      | 2030-03-03  | WCLP_ACC_DEF_REV_AM | 245000 | Other Credit Liability    | self       | -100.0           | 100.0         | 0.0            | 0.0           |                         |
    And Trial Balance Summary Report for Working Capital Loans for date "04 March 2030" has no rows

  @TestRailId:C115379
  Scenario: Verify Trial Balance Summary Report for Working Capital Loans with charge waiver, charge adjustment and goodwill credit - UC12
    When Admin sets the business date to "01 April 2030"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 April 2030   | 01 April 2030            | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 April 2030" with "1000" amount and expected disbursement date on "01 April 2030"
    And Admin successfully disburse the Working Capital loan on "01 April 2030" with "1000" EUR transaction amount
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "02 April 2030" due date and 50.0 transaction amount
    When Admin sets the business date to "03 April 2030"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin waives the last added charge on working capital loan
    Then Trial Balance Summary Report for Working Capital Loans for date "03 April 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-04-03  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
      | 2030-04-03  | WCLP_ACC_DEF_REV_AM | 112603 | Interest/Fee Receivable   | self       | 50.0             | 0.0           | -50.0          | 0.0           |                         |
      | 2030-04-03  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2030-04-03  | WCLP_ACC_DEF_REV_AM | 404007 | Fee Income                | self       | -50.0            | 0.0           | 0.0            | -50.0         |                         |
      | 2030-04-03  | WCLP_ACC_DEF_REV_AM | e4     | Written off               | self       | 0.0              | 50.0          | 0.0            | 50.0          |                         |
    And Admin adds "WORKING_CAPITAL_SPECIFIED_DUE_DATE_FEE" specified due date charge to working capital loan with "04 April 2030" due date and 40.0 transaction amount
    When Admin sets the business date to "05 April 2030"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin makes a charge adjustment for the last added charge with 40.0 amount on working capital loan
    Then Trial Balance Summary Report for Working Capital Loans for date "05 April 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-04-05  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | 0.0            | 1000.0        |                         |
      | 2030-04-05  | WCLP_ACC_DEF_REV_AM | 112603 | Interest/Fee Receivable   | self       | 40.0             | 0.0           | -40.0          | 0.0           |                         |
      | 2030-04-05  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2030-04-05  | WCLP_ACC_DEF_REV_AM | 404007 | Fee Income                | self       | -90.0            | 40.0          | 0.0            | -50.0         |                         |
      | 2030-04-05  | WCLP_ACC_DEF_REV_AM | e4     | Written off               | self       | 50.0             | 0.0           | 0.0            | 50.0          |                         |
    When Admin sets the business date to "10 April 2030"
    And Customer makes "GOODWILL_CREDIT" transaction on "10 April 2030" with 100.0 transaction amount on Working Capital loan
    Then Trial Balance Summary Report for Working Capital Loans for date "10 April 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-04-10  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | -100.0         | 900.0         |                         |
      | 2030-04-10  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 0.0           | 0.0            | -1000.0       |                         |
      | 2030-04-10  | WCLP_ACC_DEF_REV_AM | 404007 | Fee Income                | self       | -50.0            | 0.0           | 0.0            | -50.0         |                         |
      | 2030-04-10  | WCLP_ACC_DEF_REV_AM | 744003 | Goodwill Expense Account  | self       | 0.0              | 100.0         | 0.0            | 100.0         |                         |
      | 2030-04-10  | WCLP_ACC_DEF_REV_AM | e4     | Written off               | self       | 50.0             | 0.0           | 0.0            | 50.0          |                         |
    Then Admin closes the Working Capital loan with all obligations met with a full repayment on "05 April 2030"

  @TestRailId:C115380
  Scenario: Verify Trial Balance Summary Report for Working Capital Loans with undo repayment and undo disbursement - UC13
    When Admin sets the business date to "01 May 2030"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 May 2030     | 01 May 2030              | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 May 2030" with "1000" amount and expected disbursement date on "01 May 2030"
    And Admin successfully disburse the Working Capital loan on "01 May 2030" with "1000" EUR transaction amount
    When Admin sets the business date to "10 May 2030"
    And Customer makes repayment on "10 May 2030" with 200.0 transaction amount on Working Capital loan
    Then Trial Balance Summary Report for Working Capital Loans for date "10 May 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-05-10  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | -200.0         | 800.0         |                         |
      | 2030-05-10  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 200.0         | 0.0            | -800.0        |                         |
    When Admin sets the business date to "12 May 2030"
    And Customer undo "1"th working capital transaction made on "10 May 2030"
    Then Trial Balance Summary Report for Working Capital Loans for date "10 May 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-05-10  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | -200.0         | 800.0         |                         |
      | 2030-05-10  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 200.0         | 0.0            | -800.0        |                         |
    Then Trial Balance Summary Report for Working Capital Loans for date "12 May 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-05-12  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 800.0            | 200.0         | 0.0            | 1000.0        |                         |
      | 2030-05-12  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -800.0           | 0.0           | -200.0         | -1000.0       |                         |
    And Admin successfully undo Working Capital disbursal
    Then Trial Balance Summary Report for Working Capital Loans for date "12 May 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-05-12  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 800.0            | 200.0         | -1000.0        | 0.0           |                         |
      | 2030-05-12  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -800.0           | 1000.0        | -200.0         | 0.0           |                         |
    And Trial Balance Summary Report for Working Capital Loans for date "13 May 2030" has no rows
    When Admin makes undo approval on the working capital loan
    Then Working capital loan undo approval was successful
    Then Working Capital loan status will be "SUBMITTED_AND_PENDING_APPROVAL"
    When Admin rejects the working capital loan on "12 May 2030"
    Then Working capital loan rejection was successful

  @TestRailId:C115381
  Scenario: Verify Trial Balance Summary Report for Working Capital Loans leaves out zero rows after full repayment - UC14
    When Admin sets the business date to "01 June 2030"
    And Admin creates a new office
    And Admin creates a client with random data in the last created office
    And Admin creates a working capital loan with the following data:
      | LoanProduct         | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ACC_DEF_REV_AM | 01 June 2030    | 01 June 2030             | 1000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 June 2030" with "1000" amount and expected disbursement date on "01 June 2030"
    And Admin successfully disburse the Working Capital loan on "01 June 2030" with "1000" EUR transaction amount
    When Admin sets the business date to "10 June 2030"
    And Customer makes repayment on "10 June 2030" with 1000.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    Then Trial Balance Summary Report for Working Capital Loans for date "10 June 2030" has originatorId and the following data:
      | PostingDate | Product             | glAcct | Description               | AssetOwner | BeginningBalance | DebitMovement | CreditMovement | EndingBalance | originator_external_ids |
      | 2030-06-10  | WCLP_ACC_DEF_REV_AM | 112601 | Loans Receivable          | self       | 1000.0           | 0.0           | -1000.0        | 0.0           |                         |
      | 2030-06-10  | WCLP_ACC_DEF_REV_AM | 145023 | Suspense/Clearing account | self       | -1000.0          | 1000.0        | 0.0            | 0.0           |                         |
    And Trial Balance Summary Report for Working Capital Loans for date "11 June 2030" has no rows

