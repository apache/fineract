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
    When Admin creates a new loan originator with external ID and name "Waiver Reversal Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan

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
    When Loan Pay-off is made on "01 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C4651
  Scenario: Verify loan originator details in external business event after loan approval
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Event Test Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    When Admin approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    Then LoanApprovedBusinessEvent is created with originator details
    Then Admin can successfully undone the loan approval
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"

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
    And Admin successfully rejects the loan on "01 January 2025"
    Then Loan status will be "REJECTED"

  @TestRailId:C4653
  Scenario: Verify that inactive originator cannot be attached to loan
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID, name "Inactive Originator" and status "PENDING"
    When Admin creates a new default Loan with date: "1 January 2025"
    Then Attaching the originator to the loan should fail
    And Admin successfully rejects the loan on "01 January 2025"
    Then Loan status will be "REJECTED"

  @TestRailId:C4654
  Scenario: Verify loan originator creation with all fields and persistence in loan details
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with all fields and name "Full Fields Originator"
    Then Loan originator is created successfully with all fields populated
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    Then Loan details with association "originators" has the originator with all fields attached
    And Admin successfully rejects the loan on "01 January 2025"
    Then Loan status will be "REJECTED"

  @TestRailId:C4655
  Scenario: Verify that originator cannot be attached to approved loan
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Post Approval Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    Then Attaching the originator to the loan should fail with status 403
    Then Admin can successfully undone the loan approval
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"

  @TestRailId:C4656
  Scenario: Verify that originator cannot be detached from approved loan
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Pre Approval Detach Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    Then Detaching the originator from the loan should fail with status 403
    Then Admin can successfully undone the loan approval
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"

  @TestRailId:C4657
  Scenario: Verify that same originator cannot be attached to loan twice
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Duplicate Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    Then Attaching the originator to the loan should fail with status 403
    And Admin successfully rejects the loan on "01 January 2025"
    Then Loan status will be "REJECTED"

  @TestRailId:C4658
  Scenario: Verify that non-attached originator cannot be detached from loan
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Not Attached Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    Then Detaching the originator from the loan should fail with status 404
    And Admin successfully rejects the loan on "01 January 2025"
    Then Loan status will be "REJECTED"

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
    And Admin successfully rejects the loan on "01 January 2025"
    Then Loan status will be "REJECTED"

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
    And Admin successfully rejects the loan on "01 January 2025"
    Then Loan status will be "REJECTED"

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
    And Admin successfully rejects the loan on "01 January 2025"
    Then Loan status will be "REJECTED"

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
    When Loan Pay-off is made on "01 March 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

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
    And Admin successfully rejects the loan on "01 January 2025"
    Then Loan status will be "REJECTED"

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
    And Admin successfully rejects the loan on "01 January 2025"
    Then Loan status will be "REJECTED"

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
    Then Admin can successfully undone the loan approval
    Then Loan status will be "SUBMITTED_AND_PENDING_APPROVAL"

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
    When Loan Pay-off is made on "01 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

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
    When Loan Pay-off is made on "01 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

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
    And Admin successfully rejects the loan on "01 January 2025"
    Then Loan status will be "REJECTED"

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

  @TestRailId:C74521
  Scenario: Verify that originator details are present in LoanAdjustTransactionBusinessEvent after repayment reversal
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Adjust Event Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    When Admin successfully disburse the loan on "1 January 2025" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "1 January 2025" with 500 EUR transaction amount
    When Customer makes a repayment undo on "1 January 2025" without event check
    Then LoanAdjustTransactionBusinessEvent is created with originator details in "transactionToAdjust"
    When Loan Pay-off is made on "01 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C74522
  Scenario: Verify that originator details are present in LoanAccrualTransactionCreatedBusinessEvent after COB runs
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Accrual Event Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    When Admin successfully disburse the loan on "1 January 2025" with "1000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "1 January 2025" due date and 10 EUR transaction amount
    When Admin sets the business date to "2 January 2025"
    When Admin runs inline COB job for Loan
    Then LoanAccrualTransactionCreatedBusinessEvent is created with originator details on "01 January 2025"
    When Loan Pay-off is made on "02 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C74523
  Scenario: Verify that originator details are present in LoanAdjustTransactionBusinessEvent after charge waiver reversal
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Waiver Reversal Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    When Admin successfully disburse the loan on "1 January 2025" with "1000" EUR transaction amount
    When Admin adds "LOAN_SNOOZE_FEE" due date charge with "1 January 2025" due date and 10 EUR transaction amount
    And Admin waives due date charge
    When Customer reverses the waiver transaction on "1 January 2025"
    Then LoanAdjustTransactionBusinessEvent is created with originator details in "transactionToAdjust"
    When Loan Pay-off is made on "01 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C74524
  Scenario: Verify that no originator details are present in LoanAdjustTransactionBusinessEvent when loan has no originator attached
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new default Loan with date: "1 January 2025"
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    When Admin successfully disburse the loan on "1 January 2025" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "1 January 2025" with 500 EUR transaction amount
    When Customer makes a repayment undo on "1 January 2025" without event check
    Then LoanAdjustTransactionBusinessEvent is created without originator details in "transactionToAdjust"

  @TestRailId:C74538
  Scenario: Verify that originator details are present in LoanAdjustTransactionBusinessEvent new transaction detail after repayment adjustment
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Adjustment Replacement Originator"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    When Admin successfully disburse the loan on "1 January 2025" with "1000" EUR transaction amount
    And Customer makes "AUTOPAY" repayment on "1 January 2025" with 500 EUR transaction amount
    When Customer adjusts the repayment on "1 January 2025" to 300 EUR without event check
    Then LoanAdjustTransactionBusinessEvent is created with originator details in "newTransactionDetail"
    When Loan Pay-off is made on "01 January 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C78846
  Scenario: Verify that LoanAccountDelinquencyRangeDataV1 and LoanRepaymentDueDataV1 has the correct data for Originators - UC1: no originator attached
    When Admin sets the business date to "01 January 2025"
    When Admin creates a client with random data
    When Admin creates a fully customized loan with the following data:
      | LoanProduct                                                                                       | submitted on date | with Principal | ANNUAL interest rate % | interest type | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            |
      | LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY | 01 January 2025   | 1000           | 0                      | FLAT          | SAME_AS_REPAYMENT_PERIOD    | EQUAL_INSTALLMENTS | 3                | MONTHS                  | 1             | MONTHS                   | 3                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION |
    And Admin successfully approves the loan on "01 January 2025" with "1000" amount and expected disbursement date on "01 January 2025"
    When Admin successfully disburse the loan on "01 January 2025" with "1000" EUR transaction amount
    When Admin sets the business date to "01 February 2025"
    When Admin runs inline COB job for Loan
    Then LoanRepaymentDueDataV1 has the same data for Originators as in loanDetails
    When Admin sets the business date to "10 April 2025"
    When Admin runs inline COB job for Loan
    Then LoanAccountDelinquencyRangeDataV1 has the same data for Originators as in loanDetails
    When Loan Pay-off is made on "10 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C78847
  Scenario: Verify that LoanAccountDelinquencyRangeDataV1 and LoanRepaymentDueDataV1 has the correct data for Originators - UC2: originator attached
    When Admin sets the business date to "1 January 2025"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Test Originator"
    Then Loan originator is created successfully with status "ACTIVE"
    When Admin creates a new default Loan with date: "1 January 2025"
    When Admin attaches the originator to the loan
    Then Loan details with association "originators" has the originator attached
    And Admin successfully approves the loan on "1 January 2025" with "1000" amount and expected disbursement date on "1 January 2025"
    When Admin successfully disburse the loan on "1 January 2025" with "1000" EUR transaction amount
    Then Loan details with association "originators" has the originator attached
    When Admin sets the business date to "31 January 2025"
    When Admin runs inline COB job for Loan
    Then LoanRepaymentDueDataV1 has the same data for Originators as in loanDetails
    When Admin sets the business date to "01 April 2025"
    When Admin runs inline COB job for Loan
    Then LoanAccountDelinquencyRangeDataV1 has the same data for Originators as in loanDetails
    When Loan Pay-off is made on "01 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C78848
  Scenario: Verify that LoanAccountDelinquencyRangeDataV1 and LoanRepaymentDueDataV1 has the correct data for Originators - UC3: originator attached, fully customized loan
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
    When Admin sets the business date to "01 February 2025"
    When Admin runs inline COB job for Loan
    Then LoanRepaymentDueDataV1 has the same data for Originators as in loanDetails
    When Admin sets the business date to "10 April 2025"
    When Admin runs inline COB job for Loan
    Then LoanAccountDelinquencyRangeDataV1 has the same data for Originators as in loanDetails
    When Loan Pay-off is made on "10 April 2025"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C85196
  Scenario: Verify that originator details are present in LoanRepaymentDueBusinessEvent after inline COB runs
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Repayment Due Originator"
    When Admin creates a new Loan with date: "01 January 2023" and with 1 day loan term and repayment
    When Admin attaches the originator to the loan
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2023"
    When Admin runs inline COB job for Loan
    Then LoanRepaymentDueBusinessEvent is created with originator details
    When Loan Pay-off is made on "02 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C85197
  Scenario: Verify no originator details in LoanRepaymentDueBusinessEvent when loan has no originator attached
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new Loan with date: "01 January 2023" and with 1 day loan term and repayment
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2023"
    When Admin runs inline COB job for Loan
    Then LoanRepaymentDueBusinessEvent is created without originator details
    When Loan Pay-off is made on "02 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C85198
  Scenario: Verify multiple originator details in LoanRepaymentDueBusinessEvent after inline COB runs
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Repayment Due First Originator"
    When Admin creates a second loan originator with external ID and name "Repayment Due Second Originator"
    When Admin creates a new Loan with date: "01 January 2023" and with 1 day loan term and repayment
    When Admin attaches the originator to the loan
    When Admin attaches the second originator to the loan
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    And Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "02 January 2023"
    When Admin runs inline COB job for Loan
    Then LoanRepaymentDueBusinessEvent is created with 2 originator details
    When Loan Pay-off is made on "02 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C85199
  Scenario: Verify that originator details are present in LoanDelinquencyRangeChangeEvent when loan becomes delinquent
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Delinquency Originator"
    When Admin creates a new Loan with date: "01 January 2023" and with 1 day loan term and repayment
    When Admin attaches the originator to the loan
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "07 January 2023"
    When Admin runs inline COB job for Loan
    Then LoanDelinquencyRangeChangeEvent is created with originator details
    When Loan Pay-off is made on "07 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C85200
  Scenario: Verify no originator details in LoanDelinquencyRangeChangeEvent when loan has no originator attached
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new Loan with date: "01 January 2023" and with 1 day loan term and repayment
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "07 January 2023"
    When Admin runs inline COB job for Loan
    Then LoanDelinquencyRangeChangeEvent is created without originator details
    When Loan Pay-off is made on "07 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C85201
  Scenario: Verify multiple originator details in LoanDelinquencyRangeChangeEvent when loan becomes delinquent
    When Admin sets the business date to "01 January 2023"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Delinquency First Originator"
    When Admin creates a second loan originator with external ID and name "Delinquency Second Originator"
    When Admin creates a new Loan with date: "01 January 2023" and with 1 day loan term and repayment
    When Admin attaches the originator to the loan
    When Admin attaches the second originator to the loan
    And Admin successfully approves the loan on "01 January 2023" with "1000" amount and expected disbursement date on "01 January 2023"
    When Admin successfully disburse the loan on "01 January 2023" with "1000" EUR transaction amount
    When Admin sets the business date to "07 January 2023"
    When Admin runs inline COB job for Loan
    Then LoanDelinquencyRangeChangeEvent is created with 2 originator details
    When Loan Pay-off is made on "07 January 2023"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C89810
  Scenario: Verify loan originators can be reconciled during tranche disbursements
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "First Disbursement Originator"
    When Admin creates a second loan originator with external ID and name "Later Disbursement Originator"
    When Admin creates a fully customized loan with three expected disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal | 3rd_tranche_disb_expected_date | 3rd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2026   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2026                | 300.0                      | 02 January 2026                | 200.0                      | 03 January 2026                | 500.0                      |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "300" EUR transaction amount and the originator
    Then Loan details with association "originators" has 1 originator attached
    And Loan details with association "originators" has the originator attached
    When Admin sets the business date to "02 January 2026"
    And Admin successfully disburse the loan on "02 January 2026" with "200" EUR transaction amount and the second originator
    Then Loan details with association "originators" has 1 originator attached
    And Loan details with association "originators" has the second originator attached
    When Admin sets the business date to "03 January 2026"
    And Admin successfully disburse the loan on "03 January 2026" with "500" EUR transaction amount and empty originators
    Then Loan details with association "originators" has no originator attached
    When Loan Pay-off is made on "03 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met

  @TestRailId:C89811
  Scenario: Verify null originators on a later tranche disbursement leave existing originator mappings untouched
    When Admin sets the business date to "01 January 2026"
    When Admin creates a client with random data
    When Admin creates a new loan originator with external ID and name "Preserved Disbursement Originator"
    When Admin creates a fully customized loan with three expected disbursements details and following data:
      | LoanProduct                                                                                | submitted on date | with Principal | ANNUAL interest rate % | interest type     | interest calculation period | amortization type  | loanTermFrequency | loanTermFrequencyType | repaymentEvery | repaymentFrequencyType | numberOfRepayments | graceOnPrincipalPayment | graceOnInterestPayment | interest free period | Payment strategy            | 1st_tranche_disb_expected_date | 1st_tranche_disb_principal | 2nd_tranche_disb_expected_date | 2nd_tranche_disb_principal | 3rd_tranche_disb_expected_date | 3rd_tranche_disb_principal |
      | LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE | 01 January 2026   | 1000           | 7                      | DECLINING_BALANCE | DAILY                       | EQUAL_INSTALLMENTS | 6                 | MONTHS                | 1              | MONTHS                 | 6                  | 0                       | 0                      | 0                    | ADVANCED_PAYMENT_ALLOCATION | 01 January 2026                | 300.0                      | 02 January 2026                | 200.0                      | 03 January 2026                | 500.0                      |
    And Admin successfully approves the loan on "01 January 2026" with "1000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the loan on "01 January 2026" with "300" EUR transaction amount and the originator
    Then Loan details with association "originators" has 1 originator attached
    And Loan details with association "originators" has the originator attached
    When Admin sets the business date to "02 January 2026"
    And Admin successfully disburse the loan on "02 January 2026" with "200" EUR transaction amount and null originators
    Then Loan details with association "originators" has 1 originator attached
    And Loan details with association "originators" has the originator attached
    When Admin sets the business date to "03 January 2026"
    And Admin successfully disburse the loan on "03 January 2026" with "500" EUR transaction amount
    Then Loan details with association "originators" has 1 originator attached
    And Loan details with association "originators" has the originator attached
    When Loan Pay-off is made on "03 January 2026"
    Then Loan is closed with zero outstanding balance and it's all installments have obligations met
