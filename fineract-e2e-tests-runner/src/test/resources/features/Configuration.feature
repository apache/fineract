@ConfigurationFeature
Feature: Configuration

  @TestRailId:C3959
  Scenario: Verify update currency with empty value handled correct with accordance error message - UC1
    When Update currency with incorrect empty value outcomes with an error

  @TestRailId:C3961
  Scenario: Verify update currency as NULL value handled correct with accordance error message - UC2
    When Update currency as NULL value outcomes with an error

  @TestRailId:C3962
  Scenario Outline: Verify update currency with incorrect/null value handled correct with accordance error message - UC3
    When Update currency as <incorrect_config_value> value outcomes with an error

    Examples:
      | incorrect_config_value |
      | "string"               |
      | "null"                 |

  @TestRailId:C115353 @GlobalConfigAllowedValuesFeature
  Scenario: Verify allowed values validation for global configuration - UC1: rejects value outside allowed values
    When Update global config "wcl-charge-accrual-time" with stringValue "invalid-value" fails with error:
      | httpCode | errorMessage                                                          |
      | 400      | The parameter `stringValue` must be one of [ eod, real-time ] .       |

  @TestRailId:C115354 @GlobalConfigAllowedValuesFeature
  Scenario: Verify allowed values validation for global configuration - UC2: reports each invalid entry of a multi-value list separately
    When Update global config "account-mapping-for-charge" with stringValue "Income,Bogus1,Bogus2" fails with error:
      | httpCode | errorMessage                                                                    |
      | 400      | The parameter `stringValue` must be one of [ Income, Liability, Expense ] .     |
      | 400      | The parameter `stringValue` must be one of [ Income, Liability, Expense ] .     |

  @TestRailId:C115355 @GlobalConfigAllowedValuesFeature
  Scenario: Verify allowed values validation for global configuration - UC3: normalizes case-insensitive value on update
    When Global config "wcl-charge-accrual-time" value set to "EOD"
    Then Global config "wcl-charge-accrual-time" has stringValue "eod"
    And Global config "wcl-charge-accrual-time" value set to "eod"

  @TestRailId:C115356 @GlobalConfigAllowedValuesFeature
  Scenario: Verify allowed values validation for global configuration - UC4: allowedValues and multipleValuesAllowed exposed in API
    Then Global config "wcl-charge-accrual-time" has allowedValues "eod,real-time" and multipleValuesAllowed "false"
    And Global config "account-mapping-for-charge" has allowedValues "Income,Liability,Expense" and multipleValuesAllowed "true"
    And Global config "enable-business-date" has no allowedValues
