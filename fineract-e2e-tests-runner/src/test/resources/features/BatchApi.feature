@BatchApiFeature
Feature: Batch API

  @TestRailId:C63
  Scenario: As a user I would like to run a sample Batch API scenario
    When Batch API sample call ran
    Then Admin checks that all steps result 200OK

  @TestRailId:C2484 @idempotency
  Scenario: As admin I would like to verify that idempotency applies correctly in case of BatchAPI call with the same idempotency key on two repayments
    When Batch API call runs with idempotency key
    Then Admin checks that all steps result 200OK for Batch API idempotency request
    Then Batch API response has boolean value in header "x-served-from-cache": "true" in segment with requestId 6
    Then Batch API response has 200 EUR value for transaction amount in segment with requestId 6
    Then Batch API response has the same clientId and loanId in segment with requestId 6 as in segment with requestId 5
    Then Batch API response has the same idempotency key in segment with requestId 6 as in segment with requestId 5
    Then Loan has 1 "REPAYMENT" transactions on Transactions tab after Batch API run

  @TestRailId:C2640
  Scenario: Verify Batch API call in case of enclosing transaction is TRUE and all steps result 200OK
    When Batch API call with steps: createClient, createLoan, approveLoan, getLoanDetails runs with enclosingTransaction: "true"
    Then Admin checks that all steps result 200OK

  @TestRailId:C2641
  Scenario: Verify Batch API call in case of enclosing transaction is TRUE and one of the steps fails
    When Batch API call with steps: createClient, createLoan, approveLoan, getLoanDetails runs with enclosingTransaction: "true", with failed approve step
    Then Verify that step 3 throws an error with error code 404
    Then Nr. 1 Client creation was rolled back
    Then Nr. 1 Loan creation was rolled back

  @TestRailId:C2642
  Scenario: Verify Batch API call in case of enclosing transaction is FALSE and all steps result 200OK
    When Batch API call with steps: createClient, createLoan, approveLoan, getLoanDetails runs with enclosingTransaction: "false"
    Then Admin checks that all steps result 200OK

  @TestRailId:C2643
  Scenario: Verify Batch API call in case of enclosing transaction is FALSE, there is only one reference-tree and one of the steps fails
    When Batch API call with steps: createClient, createLoan, approveLoan, getLoanDetails runs with enclosingTransaction: "false", with failed approve step
    Then Verify that step Nr. 1 results 200
    Then Verify that step Nr. 2 results 200
    Then Verify that step 3 throws an error with error code 404
    Then Verify that step Nr. 4 results 200
    Then Nr. 1 Client was created
    Then Nr. 1 Loan was created

  @TestRailId:C2644
  Scenario: Verify Batch API call in case of enclosing transaction is FALSE, there are two reference-trees and all steps result 200
    When Batch API call with steps done twice: createClient, createLoan, approveLoan, getLoanDetails runs with enclosingTransaction: "false"
    Then Admin checks that all steps result 200OK

  @TestRailId:C3876
  Scenario: Create loan, approve, disburse and apply interest pause in a single Batch API call
    And Run Batch API with steps: createClient, createLoan, approveLoan, disburseLoan, applyInterestPause
    Then Admin checks that all steps result 200OK
    And Loan should have an active interest pause period starting on 1st day and ending on 2nd day

  @TestRailId:C3877
  Scenario: Create loan, approve, disburse and apply interest pause in a single Batch API call by external ids
    And Run Batch API with steps: createClient, createLoan, approveLoan, disburseLoan, applyInterestPause by external ids
    Then Admin checks that all steps result 200OK
    And Loan should have an active interest pause period starting on 1st day and ending on 2nd day

  @TestRailId:C2645
  Scenario: Verify Batch API call in case of enclosing transaction is FALSE, there are two reference-trees and one of the steps in second tree fails
    When Batch API call with steps done twice: createClient, createLoan, approveLoan, getLoanDetails runs with enclosingTransaction: "false", with failed approve step in second tree
    Then Verify that step Nr. 1 results 200
    Then Verify that step Nr. 2 results 200
    Then Verify that step Nr. 3 results 200
    Then Verify that step Nr. 4 results 200
    Then Verify that step Nr. 5 results 200
    Then Verify that step Nr. 6 results 200
    Then Verify that step 7 throws an error with error code 404
    Then Verify that step Nr. 8 results 200
    Then Nr. 1 Client was created
    Then Nr. 1 Loan was created
    Then Nr. 1 Loan was approved
    Then Nr. 2 Client was created
    Then Nr. 2 Loan was created

  @TestRailId:C2646
  Scenario: Verify Batch API call in case of enclosing transaction is FALSE and one of the steps is doubled
    When Batch API call with steps: createClient, createLoan, approveLoan, getLoanDetails runs with enclosingTransaction: "false", and approveLoan is doubled
    Then Admin checks that all steps result 200OK
    Then Batch API response has no "x-served-from-cache" field in segment with requestId 3
    Then Batch API response has boolean value in header "x-served-from-cache": "true" in segment with requestId 4

  @TestRailId:C2840
  Scenario: Verify datatable Batch API calls, when the second request relies on the first response, but the first response is empty
    When A datatable for "Loan" is created
    And Batch API call with steps: queryDatatable, updateDatatable runs, with empty queryDatatable response
    Then Verify that step Nr. 1 results 200
    Then Verify that step Nr. 2 results 400

  @TestRailId:C3509 @ChargeOffFeature
  Scenario: Verify Batch API call with steps to charge-off a loan
    When Admin sets the business date to "01 January 2024"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                            | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF | 01 January 2024   | 100            | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2024" with "100" amount and expected disbursement date on "01 January 2024"
    When Admin successfully disburse the loan on "01 January 2024" with "100" EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |           | 100.0           |               |          | 0.0  |           | 0.0   | 0.0  |            |      |             |
      | 1  | 31   | 01 February 2024 |           | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 2  | 29   | 01 March 2024    |           | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |           | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |           | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |           | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0  | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |           | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0  | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 0    | 0          | 0    | 102.05      |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type  | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement      | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
    When Admin sets the business date to "01 February 2024"
    And Customer makes "AUTOPAY" repayment on "01 February 2024" with 17.01 EUR transaction amount
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 67.05           | 16.52         | 0.49     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 50.43           | 16.62         | 0.39     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 33.71           | 16.72         | 0.29     | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 16.9            | 16.81         | 0.2      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 16.9          | 0.1      | 0.0  | 0.0       | 17.0  | 0.0   | 0.0        | 0.0  | 17.0        |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 2.05     | 0    | 0         | 102.05 | 17.01 | 0          | 0    | 85.04       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        |
    When Admin runs Batch API call with chargeOff command on "01 February 2024"
    Then Admin checks that all steps result 200OK
    Then Loan Repayment schedule has 6 periods, with the following data for periods:
      | Nr | Days | Date             | Paid date        | Balance of loan | Principal due | Interest | Fees | Penalties | Due   | Paid  | In advance | Late | Outstanding |
      |    |      | 01 January 2024  |                  | 100.0           |               |          | 0.0  |           | 0.0   | 0.0   |            |      |             |
      | 1  | 31   | 01 February 2024 | 01 February 2024 | 83.57           | 16.43         | 0.58     | 0.0  | 0.0       | 17.01 | 17.01 | 0.0        | 0.0  | 0.0         |
      | 2  | 29   | 01 March 2024    |                  | 66.56           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 3  | 31   | 01 April 2024    |                  | 49.55           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 4  | 30   | 01 May 2024      |                  | 32.54           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 5  | 31   | 01 June 2024     |                  | 15.53           | 17.01         | 0.0      | 0.0  | 0.0       | 17.01 | 0.0   | 0.0        | 0.0  | 17.01       |
      | 6  | 30   | 01 July 2024     |                  | 0.0             | 15.53         | 0.0      | 0.0  | 0.0       | 15.53 | 0.0   | 0.0        | 0.0  | 15.53       |
    Then Loan Repayment schedule has the following data in Total row:
      | Principal due | Interest | Fees | Penalties | Due    | Paid  | In advance | Late | Outstanding |
      | 100           | 0.58     | 0    | 0         | 100.58 | 17.01 | 0          | 0    | 83.57       |
    Then Loan Transactions tab has the following data:
      | Transaction date | Transaction Type | Amount | Principal | Interest | Fees | Penalties | Loan Balance |
      | 01 January 2024  | Disbursement     | 100.0  | 0.0       | 0.0      | 0.0  | 0.0       | 100.0        |
      | 01 February 2024 | Repayment        | 17.01  | 16.43     | 0.58     | 0.0  | 0.0       | 83.57        |
      | 01 February 2024 | Accrual          | 0.58   | 0.0       | 0.58     | 0.0  | 0.0       | 0.0          |
      | 01 February 2024 | Charge-off       | 83.57  | 83.57     | 0.0      | 0.0  | 0.0       | 0.0          |
    And Admin checks the loan has been charged-off on "01 February 2024"
