@WorkingCapitalDelinquencyManagementFeature
Feature: Working Capital Delinquency Configuration

  @TestRailId:C72329
  Scenario: Verify Working Capital Delinquency Configuration CRUD - UC1
    When Admin Calls Delinquency Template
    When Admin creates WC Delinquency Bucket With Values
    Then Check created Delinquency Bucket has the following values
    Then Get Delinquency Bucket With Template has the following values
    When Admin modifies WC Delinquency Bucket With Values
    Then Check updated Delinquency Bucket has the following values
    When Admin deletes WC Delinquency Bucket With Values

  @TestRailId:C72330
  Scenario Outline: Verify Working Capital Delinquency Configuration create with invalid data shall outcome with error - UC2
    Then Admin failed to create a new WC Delinquency Bucket for field "<wc_db_field_name_invalid>" with invalid data <wc_db_field_value_invalid> results with an error <wc_db_error_message>

    Examples:
      | wc_db_field_name_invalid    | wc_db_field_value_invalid | wc_db_error_message                                                   |
      | name                        | "null"                    | The parameter `name` is mandatory.                                    |
      | name                        | ""                        | The parameter `name` is mandatory.                                    |
      | ranges                      | "[]"                      | The parameter `ranges` cannot be empty. You must select at least one. |
      | bucketType                  | "0"                       | The parameter `bucketType` must be one of [ 1, 2 ] .                  |
      | minimumPayment              | "-1"                      | The parameter `minimumPayment` must be greater than or equal to 0.    |
      | minimumPaymentType          | "9"                       | The parameter `minimumPaymentType` must be between 1 and 2.           |
      | frequencyType               | "5"                       | The parameter `frequencyType` must be between 0 and 3.                |
      | minimumPaymentPeriodAndRule | "null"                    | The parameter `minimumPaymentPeriodAndRule` is mandatory.             |

  @TestRailId:C72331
  Scenario: Verify Working Capital Delinquency Configuration create validation with existing name outcomes with error - UC3
    When Admin creates WC Delinquency Bucket With Values for update
    Then Admin failed to create WC Delinquency Bucket With duplicated name

  @TestRailId:C72332
  Scenario Outline: Verify Working Capital Delinquency Configuration update with invalid data shall outcome with error - UC4
    Then Admin failed to update WC Delinquency Bucket for field "<wc_db_field_name_invalid>" with invalid data <wc_db_field_value_invalid> results with an error <wc_db_error_message>

    Examples:
      | wc_db_field_name_invalid  | wc_db_field_value_invalid | wc_db_error_message                                                   |
      | ranges                    | "[]"                      | The parameter `ranges` cannot be empty. You must select at least one. |
      | bucketType                | "9"                       | The parameter `bucketType` must be one of [ 1, 2 ] .                  |
      | minimumPayment            | "-1"                      | The parameter `minimumPayment` must be greater than or equal to 0.    |
      | minimumPaymentType        | "0"                       | The parameter `minimumPaymentType` must be between 1 and 2.           |
      | frequencyType             | "8"                       | The parameter `frequencyType` must be between 0 and 3.                |

  @TestRailId:C72333
  Scenario: Verify Working Capital Delinquency Configuration update validation with existing name outcomes with error  - UC5
    When Admin creates WC Delinquency Bucket With Values
    Then Admin failed to update WC Delinquency Bucket With duplicated name
    When Admin deletes WC Delinquency Bucket With Values

  @TestRailId:C72334
  Scenario: Verify deleting Working Capital Delinquency Configuration that is already deleted failure - UC6
    When Admin deletes WC Delinquency Bucket With Values for update
    Then Admin failed to delete WC Delinquency Bucket that is already deleted
    Then Admin failed to retrieve WC Delinquency Bucket that is already deleted

  @TestRailId:C72335
  Scenario Outline: Verify Working capital Delinquency Bucket delete with invalid data shall outcome with error - validation check with id - UC7
    Then Admin failed to delete WC Delinquency Bucket with id <wcp_field_name_incorrect_value> that doesn't exist
    Examples:
      | wcp_field_name_incorrect_value |
      | 103284                         |
      | 0                              |

  @TestRailId:C72336
  Scenario Outline: Verify Working capital Delinquency Bucket retrieve with invalid data shall outcome with error - validation check with id - UC8
    Then Admin failed to retrieve WC Delinquency Bucket with id <wcp_field_name_incorrect_value> that is not found
    Examples:
      | wcp_field_name_incorrect_value |
      | 565465                         |
      | 0                              |
