@PerformanceTesting
Feature: COB Performance Testing

  @Skip
  Scenario: Performance testing for progressive loans with daily interest calculation and COB catch-up to 1th day
    # Setup business date and create loan product
    When Admin sets the business date to "01 January 2024"

    # Create 1000 loans for performance testing
    When Admin creates performance test data with 1000 progressive loans with the following configuration:
      | loanProductName                                               | submittedOnDate | principal | interestRate | interestCalculationPeriod | interestType      | repaymentEvery | repaymentFrequencyType | numberOfRepayments | amortizationType   | fixedEmiAmount |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024 | 1000      | 12.0         | DAILY                     | DECLINING_BALANCE | 1              | MONTHS                 | 24                 | EQUAL_INSTALLMENTS | null           |

    # Approve and disburse all loans
    And Admin bulk approves all performance test loans on "01 January 2024" with expected disbursement date on "01 January 2024"
    And Admin bulk disburses all performance test loans on "01 January 2024" with full principal amount

    # Verify initial state
    Then Admin verifies that all performance test loans have status "ACTIVE"
    Then Admin checks that all performance test loans have last closed business date as "null"

    # Run COB on disbursement date
    When Admin runs inline COB job for loans
    Then Admin checks that all performance test loans have last closed business date as "31 December 2023"

    # Performance Test Scenario: COB catch-up to 1th day
    When Admin sets the business date to "03 January 2024"
    And Admin records performance metrics before action as "scenario_1_cob_catchup"
    Then Admin runs COB catch up
    And Admin is waiting for COB catch-up job to finish
    Then Admin records performance metrics after action as "scenario_1_cob_catchup"
    And Admin records performance metrics before action as "scenario_1_transactions"
    And Admin evaluates transaction count for all performance test loans as "scenario_1_transactions"
    Then Admin records performance metrics after action as "scenario_1_transactions"

  @Skip
  Scenario: Performance testing for progressive loans with daily interest calculation and COB catch-up to 7th day
    # Setup business date and create loan product
    When Admin sets the business date to "01 January 2024"
    
    # Create 1000 loans for performance testing
    When Admin creates performance test data with 1000 progressive loans with the following configuration:
      | loanProductName                                               | submittedOnDate | principal | interestRate | interestCalculationPeriod | interestType      | repaymentEvery | repaymentFrequencyType | numberOfRepayments | amortizationType   | fixedEmiAmount |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024 | 1000      | 12.0         | DAILY                     | DECLINING_BALANCE | 1              | MONTHS                 | 24                 | EQUAL_INSTALLMENTS | null           |
    
    # Approve and disburse all loans
    And Admin bulk approves all performance test loans on "01 January 2024" with expected disbursement date on "01 January 2024"
    And Admin bulk disburses all performance test loans on "01 January 2024" with full principal amount

    # Verify initial state
    Then Admin verifies that all performance test loans have status "ACTIVE"
    Then Admin checks that all performance test loans have last closed business date as "null"

    # Run COB on disbursement date
    When Admin runs inline COB job for loans
    Then Admin checks that all performance test loans have last closed business date as "31 December 2023"

    # Performance Test Scenario: COB catch-up to 7th day
    When Admin sets the business date to "08 January 2024"
    And Admin records performance metrics before action as "scenario_7_cob_catchup"
    Then Admin runs COB catch up
    And Admin is waiting for COB catch-up job to finish
    Then Admin records performance metrics after action as "scenario_7_cob_catchup"
    And Admin records performance metrics before action as "scenario_7_transactions"
    And Admin evaluates transaction count for all performance test loans as "scenario_7_transactions"
    Then Admin records performance metrics after action as "scenario_7_transactions"

  @Skip
  Scenario: Performance testing for progressive loans with daily interest calculation and COB catch-up to 30th day
    # Setup business date and create loan product
    When Admin sets the business date to "01 January 2024"
    
    # Create 1000 loans for performance testing
    When Admin creates performance test data with 1000 progressive loans with the following configuration:
      | loanProductName                                               | submittedOnDate | principal | interestRate | interestCalculationPeriod | interestType      | repaymentEvery | repaymentFrequencyType | numberOfRepayments | amortizationType   | fixedEmiAmount |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024 | 1000      | 12.0         | DAILY                     | DECLINING_BALANCE | 1              | MONTHS                 | 24                 | EQUAL_INSTALLMENTS | null           |
    
    # Approve and disburse all loans
    And Admin bulk approves all performance test loans on "01 January 2024" with expected disbursement date on "01 January 2024"
    And Admin bulk disburses all performance test loans on "01 January 2024" with full principal amount

    # Verify initial state
    Then Admin verifies that all performance test loans have status "ACTIVE"
    Then Admin checks that all performance test loans have last closed business date as "null"

    # Run COB on disbursement date
    When Admin runs inline COB job for loans
    Then Admin checks that all performance test loans have last closed business date as "31 December 2023"

    # Performance Test Scenario: COB catch-up to 30th day
    When Admin sets the business date to "31 January 2024"
    And Admin records performance metrics before action as "scenario_30_cob_catchup"
    Then Admin runs COB catch up
    And Admin is waiting for COB catch-up job to finish
    Then Admin records performance metrics after action as "scenario_30_cob_catchup"
    And Admin records performance metrics before action as "scenario_30_transactions"
    And Admin evaluates transaction count for all performance test loans as "scenario_30_transactions"
    Then Admin records performance metrics after action as "scenario_30_transactions"

  @Skip
  Scenario: Performance testing for progressive loans with daily interest calculation and COB catch-up to 100th day
    # Setup business date and create loan product
    When Admin sets the business date to "01 January 2024"

    # Create 1000 loans for performance testing
    When Admin creates performance test data with 1000 progressive loans with the following configuration:
      | loanProductName                                               | submittedOnDate | principal | interestRate | interestCalculationPeriod | interestType      | repaymentEvery | repaymentFrequencyType | numberOfRepayments | amortizationType   | fixedEmiAmount |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024 | 1000      | 12.0         | DAILY                     | DECLINING_BALANCE | 1              | MONTHS                 | 24                 | EQUAL_INSTALLMENTS | null           |

    # Approve and disburse all loans
    And Admin bulk approves all performance test loans on "01 January 2024" with expected disbursement date on "01 January 2024"
    And Admin bulk disburses all performance test loans on "01 January 2024" with full principal amount

    # Verify initial state
    Then Admin verifies that all performance test loans have status "ACTIVE"
    Then Admin checks that all performance test loans have last closed business date as "null"

    # Run COB on disbursement date
    When Admin runs inline COB job for loans
    Then Admin checks that all performance test loans have last closed business date as "31 December 2023"

#   Performance Test Scenario: COB catch-up to 100th day
    When Admin sets the business date to "11 April 2024"
    And Admin records performance metrics before action as "scenario_100_cob_catchup"
    Then Admin runs COB catch up
    And Admin is waiting for COB catch-up job to finish
    Then Admin records performance metrics after action as "scenario_100_cob_catchup"
    And Admin records performance metrics before action as "scenario_100_transactions"
    And Admin evaluates transaction count for all performance test loans as "scenario_100_transactions"
    Then Admin records performance metrics after action as "scenario_100_transactions"

  @Skip
  Scenario: Performance testing for progressive loans with daily interest calculation and COB catch-up to 300th day
    # Setup business date and create loan product
    When Admin sets the business date to "01 January 2024"

    # Create 1000 loans for performance testing
    When Admin creates performance test data with 1000 progressive loans with the following configuration:
      | loanProductName                                               | submittedOnDate | principal | interestRate | interestCalculationPeriod | interestType      | repaymentEvery | repaymentFrequencyType | numberOfRepayments | amortizationType   | fixedEmiAmount |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024 | 1000      | 12.0         | DAILY                     | DECLINING_BALANCE | 1              | MONTHS                 | 24                 | EQUAL_INSTALLMENTS | null           |

    # Approve and disburse all loans
    And Admin bulk approves all performance test loans on "01 January 2024" with expected disbursement date on "01 January 2024"
    And Admin bulk disburses all performance test loans on "01 January 2024" with full principal amount

    # Verify initial state
    Then Admin verifies that all performance test loans have status "ACTIVE"
    Then Admin checks that all performance test loans have last closed business date as "null"

    # Run COB on disbursement date
    When Admin runs inline COB job for loans
    Then Admin checks that all performance test loans have last closed business date as "31 December 2023"

    # Performance Test Scenario: COB catch-up to 300th day
    When Admin sets the business date to "27 October 2024"
    And Admin records performance metrics before action as "scenario_300_cob_catchup"
    Then Admin runs COB catch up
    And Admin is waiting for COB catch-up job to finish
    Then Admin records performance metrics after action as "scenario_300_cob_catchup"
    And Admin records performance metrics before action as "scenario_300_transactions"
    And Admin evaluates transaction count for all performance test loans as "scenario_300_transactions"
    Then Admin records performance metrics after action as "scenario_300_transactions"

  @Skip
  Scenario: Performance testing for progressive loans with daily interest calculation and COB catch-up to 700th day
    # Setup business date and create loan product
    When Admin sets the business date to "01 January 2024"

    # Create 1000 loans for performance testing
    When Admin creates performance test data with 1000 progressive loans with the following configuration:
      | loanProductName                                               | submittedOnDate | principal | interestRate | interestCalculationPeriod | interestType      | repaymentEvery | repaymentFrequencyType | numberOfRepayments | amortizationType   | fixedEmiAmount |
      | LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL | 01 January 2024 | 1000      | 12.0         | DAILY                     | DECLINING_BALANCE | 1              | MONTHS                 | 24                 | EQUAL_INSTALLMENTS | null           |

    # Approve and disburse all loans
    And Admin bulk approves all performance test loans on "01 January 2024" with expected disbursement date on "01 January 2024"
    And Admin bulk disburses all performance test loans on "01 January 2024" with full principal amount

    # Verify initial state
    Then Admin verifies that all performance test loans have status "ACTIVE"
    Then Admin checks that all performance test loans have last closed business date as "null"

    # Run COB on disbursement date
    When Admin runs inline COB job for loans
    Then Admin checks that all performance test loans have last closed business date as "31 December 2023"

    # Performance Test Scenario: COB catch-up to 700th day
    When Admin sets the business date to "27 December 2025"
    And Admin records performance metrics before action as "scenario_700_cob_catchup"
    Then Admin runs COB catch up
    And Admin is waiting for COB catch-up job to finish
    Then Admin records performance metrics after action as "scenario_700_cob_catchup"
    And Admin records performance metrics before action as "scenario_700_transactions"
    And Admin evaluates transaction count for all performance test loans as "scenario_700_transactions"
    Then Admin records performance metrics after action as "scenario_700_transactions"
