@WorkingCapital
@WorkingCapitalSubmittedOnDateBusinessDateDisabledFeature
@BusinessDateDisabledCheck
Feature: Working Capital submitted on date when business date is disabled

  @TestRailId:C98202
  Scenario: Verify Working Capital period payment rate change submitted on date falls back to the system date when business date is disabled - UC25
    Given Global configuration "enable-business-date" is enabled
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP        | 01 January 2026 | 01 January 2026          | 100             | 100                | 1                 | 0        |
    Then Working capital loan creation was successful
    Then Admin successfully approves the working capital loan on "01 January 2026" with "100" amount and expected disbursement date on "01 January 2026"
    Then Admin successfully disburse the Working Capital loan on "01 January 2026" with "100" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    #--- with the config off the stamp must be the machine date, not the stored business date ---#
    Given Global configuration "enable-business-date" is disabled
    And Admin captures the current tenant date for the Working Capital loan
    When Admin update Working Capital period payment rate with "12.5" value
    Then Working Capital Loan latest period payment rate change was submitted on the current tenant date

  @TestRailId:C98200
  Scenario: Verify near breach action submitted on date falls back to the system date when business date is disabled
    Given Global configuration "enable-business-date" is enabled
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with breach and near breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | nearBreachFrequency | nearBreachFrequencyType | nearBreachThreshold | delinquencyGraceDays |
      | 9               | DAYS                | FLAT                        | 90           | 3                   | DAYS                    | 33.33               |                      |
    And Admin creates a working capital loan using created product with the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    #--- with the config off the stamp must be the machine date, not the stored business date ---#
    Given Global configuration "enable-business-date" is disabled
    And Admin captures the current tenant date for the Working Capital loan
    When Admin creates a near breach reschedule action with threshold "40" frequency 4 frequencyType "DAYS"
    Then Latest near breach action was submitted on the current tenant date
