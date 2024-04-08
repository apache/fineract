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
