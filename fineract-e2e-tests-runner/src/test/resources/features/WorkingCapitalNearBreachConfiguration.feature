@WorkingCapitalNearBreachManagementFeature
Feature: Working Capital Near Breach Configuration

  @TestRailId:C76697
  Scenario: Verify Working Capital Near Breach Configuration CRUD - UC1
    When Admin Calls Breach Template
    When Admin creates WC Near Breach With Values
    Then Check created Near Breach has the following values
    When Admin modifies WC Near Breach With Values
    Then Check updated Near Breach has the following values
    When Admin deletes WC Near Breach With Values

  @TestRailId:C76698
  Scenario Outline: Verify Working Capital Breach Configuration create with invalid data shall outcome with error - UC2
    Then Admin failed to create a new WC Near Breach for field "<wcb_field_name_invalid>" with invalid data <wcb_field_value_invalid> results with an error <wcb_error_message>

    Examples:
      | wcb_field_name_invalid       | wcb_field_value_invalid | wcb_error_message                                                                       |
      | nearBreachName               | "null"                  | The parameter `nearBreachName` is mandatory.                                            |
      | nearBreachName               | ""                      | The parameter `nearBreachName` is mandatory.                                            |
      | nearBreachFrequency          | "null"                  | The parameter `nearBreachFrequency` is mandatory.                                       |
      | nearBreachFrequency          | "0"                     | The parameter `nearBreachFrequency` must be greater than 0.                             |
      | nearBreachFrequencyType      | "null"                  | The parameter `nearBreachFrequencyType` is mandatory.                                   |
      | nearBreachFrequencyType      | "INVALID"               | The parameter `nearBreachFrequencyType` must be one of [ DAYS, WEEKS, MONTHS, YEARS ] . |
      | nearBreachThreshold          | "null"                  | The parameter `nearBreachThreshold` is mandatory.                                       |
      | nearBreachThreshold          | "0"                     | The parameter `nearBreachThreshold` must be greater than 0.                             |
      | nearBreachThreshold          | "100.01"                | The parameter `nearBreachThreshold` must be not greater than 100.                      |

  @TestRailId:C76699
  Scenario: Verify Working Capital Breach Configuration create validation with existing name outcomes with error - UC3
    When Admin creates WC Breach With Values for update
    Then Admin failed to create WC Breach With duplicated name
    When Admin deletes WC Breach With Values for update

  @TestRailId:C76700
  Scenario Outline: Verify Working Capital Near Breach Configuration update with invalid data shall outcome with error - UC4
    When Admin creates WC Near Breach With Values for update
    Then Admin failed to update WC Near Breach for field "<wcb_field_name_invalid>" with invalid data <wcb_field_value_invalid> results with an error <wcb_error_message>
    When Admin deletes WC Near Breach With Values for update

    Examples:
      | wcb_field_name_invalid       | wcb_field_value_invalid | wcb_error_message                                                                       |
      | nearBreachName               | "null"                  | The parameter `nearBreachName` is mandatory.                                            |
      | nearBreachName               | ""                      | The parameter `nearBreachName` is mandatory.                                            |
      | nearBreachFrequency          | "null"                  | The parameter `nearBreachFrequency` is mandatory.                                       |
      | nearBreachFrequency          | "0"                     | The parameter `nearBreachFrequency` must be greater than 0.                             |
      | nearBreachFrequencyType      | "null"                  | The parameter `nearBreachFrequencyType` is mandatory.                                   |
      | nearBreachFrequencyType      | "INVALID"               | The parameter `nearBreachFrequencyType` must be one of [ DAYS, WEEKS, MONTHS, YEARS ] . |
      | nearBreachThreshold          | "null"                  | The parameter `nearBreachThreshold` is mandatory.                                       |
      | nearBreachThreshold          | "0"                     | The parameter `nearBreachThreshold` must be greater than 0.                             |
      | nearBreachThreshold          | "110"                   | The parameter `nearBreachThreshold` must be not greater than 100.                       |

  @TestRailId:C76701
  Scenario: Verify Working Capital Near Breach update validation with existing name outcomes with error  - UC5
    When Admin creates WC Near Breach With Values
    When Admin creates WC Near Breach With Values for update
    Then Admin failed to update WC Near Breach With duplicated name
    When Admin deletes WC Near Breach With Values
    When Admin deletes WC Near Breach With Values for update

  @TestRailId:C76702
  Scenario: Verify deleting Working Capital Near Breach Configuration that is already deleted failure - UC6
    When Admin creates WC Near Breach With Values for update
    When Admin deletes WC Near Breach With Values for update
    Then Admin failed to delete WC Near Breach that is already deleted
    Then Admin failed to retrieve WC Near Breach that is already deleted

  @TestRailId:C76703
  Scenario Outline: Verify Working Capital Breach delete with invalid data shall outcome with error - validation check with id - UC7
    Then Admin failed to delete WC Near Breach with id <wcb_field_name_incorrect_value> that doesn't exist
    Examples:
      | wcb_field_name_incorrect_value |
      | 103284                         |
      | 0                              |

  @TestRailId:C76704
  Scenario Outline: Verify Working Capital Breach retrieve with invalid data shall outcome with error - validation check with id - UC8
    Then Admin failed to retrieve WC Near Breach with id <wcb_field_name_incorrect_value> that is not found
    Examples:
      | wcb_field_name_incorrect_value |
      | 565465                         |
      | 0                              |

  @TestRailId:C76705
  Scenario: Verify deleting Working Capital Near Breach Configuration that is still assigned to WCLP failure - UC9.1
    When Admin creates a new Working Capital Loan Product with breach and near breach
    Then Admin failed to delete WC Near Breach that is still assigned to WC loan product
    Then Admin deletes a Working Capital Loan Product
    Then Admin deletes WC Breach With Values
    Then Admin deletes WC Near Breach With Values

  @TestRailId:C76706
  Scenario: Verify deleting Working Capital Near Breach Configuration that is still assigned to WC loan account failure - UC9.2
    When Admin sets the business date to "01 January 2027"
    And Admin creates a client with random data
    And Admin creates working capital loan with with breach and near breach on "01 January 2027" date
    Then Admin failed to delete WC Near Breach that is still assigned to WC loan product



