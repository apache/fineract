@LoanOriginationFeature
Feature: Loan Origination

  @TestRailId:C4649
  Scenario: Verify loan originator registration, attachment to loan, and persistence through approval and disbursal
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Merchant Partner Alpha"
    Then Loan originator is created successfully with status "ACTIVE"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    Then Loan details with association "originators" has the originator attached
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    When Admin successfully disburse the loan on "1 January 2025" with "1000" EUR transaction amount
    Then Loan details with association "originators" has the originator attached

  @TestRailId:C4650
  Scenario: Verify loan originator inline attachment with auto-creation during loan application
    Given Global configuration "enable-originator-creation-during-loan-application" is enabled
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new Loan with originator inline submitted on date: "1 January 2025"
    Then Loan details with association "originators" has the originator attached
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    When Admin successfully disburse the loan on "1 January 2025" with "1000" EUR transaction amount
    Then Loan details with association "originators" has the originator attached
    Given Global configuration "enable-originator-creation-during-loan-application" is disabled

  @TestRailId:C4651
  Scenario: Verify loan originator details in external business event after loan approval
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Event Test Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    When Admin approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    Then LoanApprovedBusinessEvent is created with originator details

  @TestRailId:C4652
  Scenario: Verify loan originator detachment from loan before approval
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Detach Test Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    Then Loan details with association "originators" has the originator attached
    When Admin detaches the originator from the loan
    Then Loan details with association "originators" has no originator attached

  @TestRailId:C4653
  Scenario: Verify that inactive originator cannot be attached to loan
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID, name "Inactive Originator" and status "PENDING"
    When Admin creates a new default Loan with date: "1 January 2025"
    Then Attaching the originator to the loan should fail

  @TestRailId:C4654
  Scenario: Verify loan originator creation with all fields and persistence in loan details
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with all fields and name "Full Fields Originator"
    Then Loan originator is created successfully with all fields populated
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    Then Loan details with association "originators" has the originator with all fields attached

  @TestRailId:C4655
  Scenario: Verify that originator cannot be attached to approved loan
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Post Approval Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    Then Attaching the originator to the loan should fail with status 403

  @TestRailId:C4656
  Scenario: Verify that originator cannot be detached from approved loan
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Pre Approval Detach Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    Then Detaching the originator from the loan should fail with status 403

  @TestRailId:C4657
  Scenario: Verify that same originator cannot be attached to loan twice
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Duplicate Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    Then Attaching the originator to the loan should fail with status 403

  @TestRailId:C4658
  Scenario: Verify that non-attached originator cannot be detached from loan
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Not Attached Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    Then Detaching the originator from the loan should fail with status 404

  @TestRailId:C4659
  Scenario: Verify that originator cannot be attached to non-existent loan
    When Admin creates a new loan originator with external ID and name "Orphan Originator"
    Then Attaching the originator to non-existent loan should fail with status 404

  @TestRailId:C4660
  Scenario: Verify that non-existent originator cannot be attached to loan
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2025"
    Then Attaching non-existent originator to the loan should fail with status 404

  @TestRailId:C4661
  Scenario: Verify loan originator creation without name succeeds with default handling
    Then Creating a loan originator without name succeeds

  @TestRailId:C4662
  Scenario: Verify that user without ATTACH_LOAN_ORIGINATOR permission cannot attach originator
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Permission Test Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin creates new user with "ORIGINATOR_NO_ATTACH" username, "ORIGINATOR_NO_ATTACH_ROLE" role name and given permissions:
      | READ_LOAN |
    Then Created user without ATTACH_LOAN_ORIGINATOR permission fails to attach originator to the loan

  @TestRailId:C4663
  Scenario: Verify that user without DETACH_LOAN_ORIGINATOR permission cannot detach originator
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Permission Detach Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    When Admin creates new user with "ORIGINATOR_NO_DETACH" username, "ORIGINATOR_NO_DETACH_ROLE" role name and given permissions:
      | READ_LOAN |
    Then Created user without DETACH_LOAN_ORIGINATOR permission fails to detach originator from the loan

  @TestRailId:C4664
  Scenario: Verify loan originator persistence through full loan lifecycle with repayments
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with all fields and name "Lifecycle Originator"
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                   | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2025   | 1000           | 12                     | DECLINING_BALANCE | DAILY                      | EQUAL_INSTALLMENTS | 3                 | MONTHS                | 1              | MONTHS                 | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    When Admin attaches the originator to the loan
    Then Loan details with association "originators" has the originator with all fields attached
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    When Admin successfully disburse the loan on "1 January 2025" with "1000" EUR transaction amount
    Then Loan details with association "originators" has the originator with all fields attached
    When Admin sets the business date to "1 February 2025"
    And Customer makes "AUTOPAY" repayment on "1 February 2025" with 340 EUR transaction amount
    Then Loan details with association "originators" has the originator with all fields attached
    When Admin sets the business date to "1 March 2025"
    And Customer makes "AUTOPAY" repayment on "1 March 2025" with 340 EUR transaction amount
    Then Loan details with association "originators" has the originator with all fields attached

  @TestRailId:C4665
  Scenario: Verify multiple originators on a loan with add, update, and detach operations
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "First Originator"
    When Admin creates a second loan originator with external ID and name "Second Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    When Admin attaches the second originator to the loan
    Then Loan details with association "originators" has 2 originators attached
    When Admin updates the originator name to "First Originator Updated" and status to "ACTIVE"
    Then Loan details with association "originators" has originator with name "First Originator Updated"
    When Admin detaches the originator from the loan
    Then Loan details with association "originators" has 1 originator attached
    And Loan details with association "originators" has the second originator attached

  @TestRailId:C4666
  Scenario: Verify loan originator persistence through undo approval
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Undo Approval Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    Then Loan details with association "originators" has the originator attached
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    Then Loan details with association "originators" has the originator attached
    Then Admin can successfully undone the loan approval
    Then Loan details with association "originators" has the originator attached

  @TestRailId:C4667
  Scenario: Verify loan originator persistence through undo disbursal
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Undo Disbursal Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    When Admin successfully disburse the loan on "1 January 2025" with "1000" EUR transaction amount
    Then Loan details with association "originators" has the originator attached
    When Admin successfully undo disbursal
    Then Loan details with association "originators" has the originator attached

  @TestRailId:C4668
  Scenario: Verify loan originator persistence through loan charge-off
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Charge Off Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    When Admin successfully disburse the loan on "1 January 2025" with "1000" EUR transaction amount
    Then Loan details with association "originators" has the originator attached
    And Admin does charge-off the loan on "1 January 2025"
    Then Loan details with association "originators" has the originator attached

  @TestRailId:C4669
  Scenario: Verify that originator cannot be attached or detached from disbursed loan
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Disbursed Loan Originator"
    When Admin creates a second loan originator with external ID and name "Disbursed Loan Extra Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    When Admin successfully disburse the loan on "1 January 2025" with "1000" EUR transaction amount
    Then Attaching the second originator to the loan should fail with status 403
    And Detaching the originator from the loan should fail with status 403

  @TestRailId:C4670
  Scenario: Verify loan originator read operations with retrieve by external ID, list all, and template
    When Admin creates a new loan originator with all fields and name "CRUD Read Originator"
    Then Loan originator is retrieved successfully by external ID with all fields
    And Loan originator list contains the created originator
    And Loan originator template contains status options, originator type options and channel type options

  @TestRailId:C4671
  Scenario: Verify loan originator update operations by ID and by external ID
    When Admin creates a new loan originator with all fields and name "CRUD Update Originator"
    Then Loan originator is created successfully with status "ACTIVE"
    When Admin updates the originator name to "Updated Name" and status to "INACTIVE"
    Then Loan originator has name "Updated Name" and status "INACTIVE"
    When Admin updates the originator by external ID with name "ExtId Updated Name"
    Then Loan originator retrieved by external ID has name "ExtId Updated Name"

  @TestRailId:C4672
  Scenario: Verify loan originator delete operations by ID, by external ID, and deletion prevention when mapped to loan
    When Admin creates a new loan originator with external ID and name "Delete By ID Originator"
    When Admin deletes the originator by ID
    Then Retrieving the deleted originator by ID should fail with status 404
    When Admin creates a new loan originator with external ID and name "Delete By ExtId Originator"
    When Admin deletes the originator by external ID
    Then Retrieving the deleted originator by external ID should fail with status 404
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Mapped Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    Then Deleting the originator should fail with status 403

  @TestRailId:C4673
  Scenario: Verify loan originator CRUD permission checks with create, update, and delete denied without permissions
    When Admin creates a new loan originator with external ID and name "Permission CRUD Originator"
    When Admin creates new user with "ORIGINATOR_NO_CREATE" username, "ORIGINATOR_NO_CREATE_ROLE" role name and given permissions:
      | READ_LOAN |
    Then Created user without CREATE_LOAN_ORIGINATOR permission fails to create an originator
    When Admin creates new user with "ORIGINATOR_NO_UPDATE" username, "ORIGINATOR_NO_UPDATE_ROLE" role name and given permissions:
      | READ_LOAN_ORIGINATOR |
    Then Created user without UPDATE_LOAN_ORIGINATOR permission fails to update the originator
    When Admin creates new user with "ORIGINATOR_NO_DELETE" username, "ORIGINATOR_NO_DELETE_ROLE" role name and given permissions:
      | READ_LOAN_ORIGINATOR |
    Then Created user without DELETE_LOAN_ORIGINATOR permission fails to delete the originator
