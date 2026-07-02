@WorkingCapital
@WorkingCapitalLoanOriginationFeature
Feature: Working Capital Loan Origination

  @TestRailId:C85377
  Scenario: Originator persists across the working capital loan lifecycle
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Merchant Alpha"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin attaches the originator to the working capital loan
    Then Working capital loan details has the originator attached
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan details has the originator attached
    When Admin makes undo approval on the working capital loan
    Then Working capital loan details has the originator attached
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan details has the originator attached
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working capital loan details has the originator attached

  @TestRailId:C85378
  Scenario: Capture originator inline at submit time by existing originator id
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Inline Originator"
    And Admin creates a working capital loan with originator attached inline and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Working capital loan details has the originator attached

  @TestRailId:C85379
  Scenario: Attach multiple originators and retrieve via the WC originators API
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC First Originator"
    And Admin creates a second loan originator with external ID and name "WC Second Originator"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin attaches the originator to the working capital loan
    And Admin attaches the second originator to the working capital loan
    Then Working capital loan details has 2 originators attached
    And Retrieving working capital loan originators returns 2 originators
    And Retrieving working capital loan originators by external id returns 2 originators

  @TestRailId:C85380
  Scenario: Detach originator before approval clears it from loan details
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Detach Originator"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin attaches the originator to the working capital loan
    Then Working capital loan details has the originator attached
    When Admin detaches the originator from the working capital loan
    Then Working capital loan details has no originator attached

  @TestRailId:C85381
  Scenario: Inactive originator cannot be attached
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID, name "WC Inactive Originator" and status "PENDING"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Attaching the originator to the working capital loan should fail with status 403

  @TestRailId:C85382
  Scenario: Same originator cannot be attached twice
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Duplicate Originator"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin attaches the originator to the working capital loan
    Then Attaching the originator to the working capital loan should fail with status 403

  @TestRailId:C85383
  Scenario: Originator cannot be attached to an approved WC loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Post Approval Originator"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Attaching the originator to the working capital loan should fail with status 403

  @TestRailId:C85405
  Scenario: Originator cannot be detached from an approved WC loan
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Pre Approval Detach Originator"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin attaches the originator to the working capital loan
    And Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Detaching the originator from the working capital loan should fail with status 403

  @TestRailId:C85384
  Scenario: Attaching a non-existent originator fails with 404
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Attaching non-existent originator to the working capital loan should fail with status 404

  @TestRailId:C85385
  Scenario: Attaching an originator to a non-existent WC loan fails with 404
    Then Attaching the originator to non-existent working capital loan should fail with status 404

  @TestRailId:C85386
  Scenario: Detaching a non-attached originator fails with 404
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Not Attached Originator"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Detaching the originator from the working capital loan should fail with status 404

  @TestRailId:C85387
  Scenario: Capture multiple originators inline at submit time
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Inline First Originator"
    And Admin creates a second loan originator with external ID and name "WC Inline Second Originator"
    And Admin creates a working capital loan with two originators attached inline and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Working capital loan details has 2 originators attached
    And Working capital loan details has the originator attached
    And Working capital loan details has the second originator attached

  @TestRailId:C85388
  Scenario: Capture inline originator created on the fly by external id at submit time
    When Admin sets the business date to "01 January 2026"
    And Global configuration "enable-originator-creation-during-loan-application" is enabled
    And Admin creates a client with random data
    And Admin creates a working capital loan with an inline originator created by a new external id and name "WC Created On The Fly" and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Working capital loan details has 1 originator attached
    And Working capital loan details has the originator attached
    And Working capital loan details has originator with name "WC Created On The Fly"

  @TestRailId:C85389
  Scenario: Capture inline originator referenced by an existing external id at submit time
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Existing External Id Originator"
    And Admin creates a working capital loan with an inline originator referenced by the existing originator external id and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Working capital loan details has 1 originator attached
    And Working capital loan details has the originator attached

  @TestRailId:C85390
  Scenario: Listing the same originator twice inline attaches it only once
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Inline Duplicate Originator"
    And Admin creates a working capital loan with the same originator listed twice inline and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Working capital loan details has 1 originator attached
    And Working capital loan details has the originator attached

  @TestRailId:C85391
  Scenario: Submitting with an inactive inline originator fails and rolls back loan creation
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID, name "WC Inline Inactive Originator" and status "PENDING"
    Then Creating a working capital loan with the inline originator should fail with status 403 and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |

  @TestRailId:C85392
  Scenario: Submitting with a non-existent inline originator fails and rolls back loan creation
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    Then Creating a working capital loan with a non-existent inline originator should fail with status 404 and the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |

  @TestRailId:C85393
  Scenario: Retrieving originators for a non-existent working capital loan fails with 404
    Then Retrieving working capital loan originators for a non-existent loan should fail with status 404

  @TestRailId:C85394
  Scenario: Retrieving originators by a non-existent working capital loan external id fails with 404
    Then Retrieving working capital loan originators by a non-existent external id should fail with status 404

  @TestRailId:C85395
  Scenario: Detaching an originator from a non-existent working capital loan fails with 404
    Then Detaching the originator from a non-existent working capital loan should fail with status 404

  @TestRailId:C85396
  Scenario: Detaching a non-existent originator from a working capital loan fails with 404
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    Then Detaching a non-existent originator from the working capital loan should fail with status 404

  @TestRailId:C85397
  Scenario: Attach originator by originator external id
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Attach By Originator External Id"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin attaches the originator to the working capital loan by originator external id
    Then Working capital loan details has the originator attached

  @TestRailId:C85398
  Scenario: Attach originator by loan external id
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Attach By Loan External Id"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin attaches the originator to the working capital loan by loan external id
    Then Working capital loan details has the originator attached

  @TestRailId:C85399
  Scenario: Attach originator by both external ids
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Attach By Both External Ids"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin attaches the originator to the working capital loan by both external ids
    Then Working capital loan details has the originator attached

  @TestRailId:C85400
  Scenario: Detach originator by originator external id
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Detach By Originator External Id"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin attaches the originator to the working capital loan
    Then Working capital loan details has the originator attached
    When Admin detaches the originator from the working capital loan by originator external id
    Then Working capital loan details has no originator attached

  @TestRailId:C85404
  Scenario: Detach originator by loan external id
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Detach By Loan External Id"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin attaches the originator to the working capital loan
    Then Working capital loan details has the originator attached
    When Admin detaches the originator from the working capital loan by loan external id
    Then Working capital loan details has no originator attached

  @TestRailId:C85401
  Scenario: Detach originator by both external ids
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Detach By Both External Ids"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin attaches the originator to the working capital loan
    Then Working capital loan details has the originator attached
    When Admin detaches the originator from the working capital loan by both external ids
    Then Working capital loan details has no originator attached

  @TestRailId:C85402
  Scenario: Loan details returns the full set of originator fields
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with all fields and name "WC Full Fields Originator"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin attaches the originator to the working capital loan
    Then Working capital loan details has the originator with all fields attached

  @TestRailId:C85403
  Scenario: Detaching one of two originators keeps the other attached
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a new loan originator with external ID and name "WC Selective First Originator"
    And Admin creates a second loan originator with external ID and name "WC Selective Second Originator"
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100.0           | 100.0              | 1.0               | 0.0      |
    Then Working capital loan creation was successful
    When Admin attaches the originator to the working capital loan
    And Admin attaches the second originator to the working capital loan
    Then Working capital loan details has 2 originators attached
    When Admin detaches the originator from the working capital loan
    Then Working capital loan details has 1 originator attached
    And Working capital loan details has the second originator attached
    And Working capital loan details does not have the originator attached
