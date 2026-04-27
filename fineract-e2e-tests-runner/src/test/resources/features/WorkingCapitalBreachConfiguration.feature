@WorkingCapitalBreachManagementFeature
Feature: Working Capital Breach Configuration

  @TestRailId:C74473
  Scenario: Verify Working Capital Breach Configuration CRUD - UC1
    When Admin Calls Breach Template
    When Admin creates WC Breach With Values
    Then Check created Breach has the following values
    When Admin modifies WC Breach With Values
    Then Check updated Breach has the following values
    When Admin deletes WC Breach With Values

  @TestRailId:C74474
  Scenario Outline: Verify Working Capital Breach Configuration create with invalid data shall outcome with error - UC2
    Then Admin failed to create a new WC Breach for field "<wcb_field_name_invalid>" with invalid data <wcb_field_value_invalid> results with an error <wcb_error_message>

    Examples:
      | wcb_field_name_invalid       | wcb_field_value_invalid | wcb_error_message                                                                 |
      | name                         | "null"                  | The parameter `name` is mandatory.                                                |
      | name                         | ""                      | The parameter `name` is mandatory.                                                |
      | breachFrequency              | "null"                  | The parameter `breachFrequency` is mandatory.                                     |
      | breachFrequency              | "0"                     | The parameter `breachFrequency` must be greater than 0.                           |
      | breachFrequencyType          | "null"                  | The parameter `breachFrequencyType` is mandatory.                                 |
      | breachFrequencyType          | "INVALID"               | The parameter `breachFrequencyType` must be one of [ DAYS, WEEKS, MONTHS, YEARS ] .      |
      | breachAmountCalculationType  | "null"                  | The parameter `breachAmountCalculationType` is mandatory.                         |
      | breachAmountCalculationType  | "INVALID"               | The parameter `breachAmountCalculationType` must be one of [ PERCENTAGE, FLAT ] . |
      | breachAmount                 | "null"                  | The parameter `breachAmount` is mandatory.                                        |
      | breachAmount                 | "-1"                    | The parameter `breachAmount` must be greater than or equal to 0.                  |

  @TestRailId:C74475
  Scenario: Verify Working Capital Breach Configuration create validation with existing name outcomes with error - UC3
    When Admin creates WC Breach With Values for update
    Then Admin failed to create WC Breach With duplicated name
    When Admin deletes WC Breach With Values for update

  @TestRailId:C74476
  Scenario Outline: Verify Working Capital Breach Configuration update with invalid data shall outcome with error - UC4
    When Admin creates WC Breach With Values for update
    Then Admin failed to update WC Breach for field "<wcb_field_name_invalid>" with invalid data <wcb_field_value_invalid> results with an error <wcb_error_message>
    When Admin deletes WC Breach With Values for update

    Examples:
      | wcb_field_name_invalid       | wcb_field_value_invalid | wcb_error_message                                                                 |
      | name                         | "null"                  | The parameter `name` is mandatory.                                                |
      | name                         | ""                      | The parameter `name` is mandatory.                                                |
      | breachFrequency              | "0"                     | The parameter `breachFrequency` must be greater than 0.                           |
      | breachFrequencyType          | "INVALID"               | The parameter `breachFrequencyType` must be one of [ DAYS, WEEKS, MONTHS, YEARS ] .      |
      | breachAmountCalculationType  | "INVALID"               | The parameter `breachAmountCalculationType` must be one of [ PERCENTAGE, FLAT ] . |
      | breachAmount                 | "-1"                    | The parameter `breachAmount` must be greater than or equal to 0.                  |

  @TestRailId:C74477
  Scenario: Verify Working Capital Breach update validation with existing name outcomes with error  - UC5
    When Admin creates WC Breach With Values
    When Admin creates WC Breach With Values for update
    Then Admin failed to update WC Breach With duplicated name
    When Admin deletes WC Breach With Values
    When Admin deletes WC Breach With Values for update

  @TestRailId:C74478
  Scenario: Verify deleting Working Capital Breach Configuration that is already deleted failure - UC6
    When Admin creates WC Breach With Values for update
    When Admin deletes WC Breach With Values for update
    Then Admin failed to delete WC Breach that is already deleted
    Then Admin failed to retrieve WC Breach that is already deleted

  @TestRailId:C74519
  Scenario Outline: Verify Working Capital Breach delete with invalid data shall outcome with error - validation check with id - UC7
    Then Admin failed to delete WC Breach with id <wcb_field_name_incorrect_value> that doesn't exist
    Examples:
      | wcb_field_name_incorrect_value |
      | 103284                         |
      | 0                              |

  @TestRailId:C74520
  Scenario Outline: Verify Working Capital Breach retrieve with invalid data shall outcome with error - validation check with id - UC8
    Then Admin failed to retrieve WC Breach with id <wcb_field_name_incorrect_value> that is not found
    Examples:
      | wcb_field_name_incorrect_value |
      | 565465                         |
      | 0                              |
