@WorkingCapital
@WorkingCapitalLoanProductAdvancedAccountingFeature
Feature: WorkingCapitalLoanProductAdvancedAccounting

  @TestRailId:C76759
  Scenario: Verify WC Loan Product template includes advanced accounting options
    When Admin retrieves the Working Capital Loan Product template
    Then Working Capital Loan Product template has advanced accounting options

  @TestRailId:C76760
  Scenario: Verify WC Loan Product create persists advanced accounting mappings
    When Admin creates a new Working Capital Loan Product with Accrual with deferred revenue amortization accounting and advanced mappings
    Then Working Capital Loan Product has advanced accounting mappings

  @TestRailId:C76761
  Scenario: Verify WC Loan Product update persists advanced accounting mappings
    When Admin creates a new Working Capital Loan Product with accounting rule "ACC_DEF_REV_AM"
    And Admin updates Working Capital Loan Product with advanced mappings
    Then Working Capital Loan Product has advanced accounting mappings

  @TestRailId:C76762
  Scenario: Verify WC Loan Product second update overrides existing advanced mappings
    When Admin creates a new Working Capital Loan Product with accounting rule "ACC_DEF_REV_AM"
    And Admin updates Working Capital Loan Product with advanced mappings twice
    Then Working Capital Loan Product has latest advanced accounting mappings after second update

  @TestRailId:C78827
  Scenario: Verify validation error when paymentTypeId is null in payment channel mappings
    When Admin attempts to create Working Capital Loan Product with null paymentTypeId in payment channel mappings
    Then Admin gets validation error with status code 400 and message "paymentTypeId is mandatory"

  @TestRailId:C78828
  Scenario: Verify validation error when fundSourceAccountId is null in payment channel mappings
    When Admin attempts to create Working Capital Loan Product with null fundSourceAccountId in payment channel mappings
    Then Admin gets validation error with status code 400 and message "fundSourceAccountId is mandatory"

  @TestRailId:C78829
  Scenario: Verify validation error when chargeOffReasonCodeValueId is null in charge-off mappings
    When Admin attempts to create Working Capital Loan Product with null chargeOffReasonCodeValueId in charge-off mappings
    Then Admin gets validation error with status code 400 and message "chargeOffReasonCodeValueId is mandatory"

  @TestRailId:C78830
  Scenario: Verify validation error when expenseAccountId is null in charge-off mappings
    When Admin attempts to create Working Capital Loan Product with null expenseAccountId in charge-off mappings
    Then Admin gets validation error with status code 400 and message "expenseGlAccountId is mandatory"

  @TestRailId:C78831
  Scenario: Verify validation error when writeOffReasonCodeValueId is null in write-off mappings
    When Admin attempts to create Working Capital Loan Product with null writeOffReasonCodeValueId in write-off mappings
    Then Admin gets validation error with status code 400 and message "writeOffReasonCodeValueId is mandatory"

  @TestRailId:C78832
  Scenario: Verify validation error when expenseAccountId is null in write-off mappings
    When Admin attempts to create Working Capital Loan Product with null expenseAccountId in write-off mappings
    Then Admin gets validation error with status code 400 and message "expenseGlAccountId is mandatory"

  @TestRailId:C78833
  Scenario: Verify validation error when duplicate paymentTypeId exists in payment channel mappings
    When Admin attempts to create Working Capital Loan Product with duplicate paymentTypeId in payment channel mappings
    Then Admin gets validation error with status code 400 and message "Duplicated entry for paymentChannelToFundSourceMappings.paymentTypeId"

  @TestRailId:C78835
  Scenario: Verify successful creation with unique payment channel mappings, multiple elements
    When Admin creates Working Capital Loan Product with unique payment channel mappings
    Then Working Capital Loan Product is created successfully with two payment channel mappings
