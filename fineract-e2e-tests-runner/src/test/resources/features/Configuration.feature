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
