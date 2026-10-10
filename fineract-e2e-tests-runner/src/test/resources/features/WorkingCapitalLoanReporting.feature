@WorkingCapital
@WorkingCapitalLoanReportingFeature
Feature: Working Capital Loan Reporting

  # The journal entry aggregation job tracks the latest aggregated date tenant-wide. Each scenario uses its own month,
  # later than the regular loan Reporting.feature, so the aggregation of one scenario does not hide the entries of
  # another. Like Reporting.feature, a scenario cannot be rerun on the same database once its dates are aggregated.

  # TODO: add TestRail ID
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

  # TODO: add TestRail ID
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

  # TODO: add TestRail ID
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
