@WorkingCapital
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
      | wc_db_field_name_invalid    | wc_db_field_value_invalid | wc_db_error_message                                                           |
      | name                        | "null"                    | The parameter `name` is mandatory.                                            |
      | name                        | ""                        | The parameter `name` is mandatory.                                            |
      | ranges                      | "[]"                      | The parameter `ranges` cannot be empty. You must select at least one.         |
      | bucketType                  | "INVALID"                 | The parameter `bucketType` must be one of [ REGULAR, WORKING_CAPITAL ] .      |
      | minimumPayment              | "-1"                      | The parameter `minimumPayment` must be greater than 0.                        |
      | minimumPaymentType          | "INVALID"                 | The parameter `minimumPaymentType` must be one of [ PERCENTAGE, FLAT ] .      |
      | frequencyType               | "INVALID"                 | The parameter `frequencyType` must be one of [ DAYS, WEEKS, MONTHS, YEARS ] . |
      | minimumPaymentPeriodAndRule | "null"                    | The parameter `minimumPaymentPeriodAndRule` is mandatory.                     |

  @TestRailId:C72331
  Scenario: Verify Working Capital Delinquency Configuration create validation with existing name outcomes with error - UC3
    When Admin creates WC Delinquency Bucket With Values for update
    Then Admin failed to create WC Delinquency Bucket With duplicated name

  @TestRailId:C72332
  Scenario Outline: Verify Working Capital Delinquency Configuration update with invalid data shall outcome with error - UC4
    Then Admin failed to update WC Delinquency Bucket for field "<wc_db_field_name_invalid>" with invalid data <wc_db_field_value_invalid> results with an error <wc_db_error_message>

    Examples:
      | wc_db_field_name_invalid | wc_db_field_value_invalid | wc_db_error_message                                                           |
      | ranges                   | "[]"                      | The parameter `ranges` cannot be empty. You must select at least one.         |
      | bucketType               | "INVALID"                 | The parameter `bucketType` must be one of [ REGULAR, WORKING_CAPITAL ] .      |
      | minimumPayment           | "-1"                      | The parameter `minimumPayment` must be greater than 0.                        |
      | minimumPaymentType       | "INVALID"                 | The parameter `minimumPaymentType` must be one of [ PERCENTAGE, FLAT ] .      |
      | frequencyType            | "INVALID"                 | The parameter `frequencyType` must be one of [ DAYS, WEEKS, MONTHS, YEARS ] . |

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

  @TestRailId:C78841
  Scenario: Verify deleting WC Delinquency Bucket assigned to a Working Capital Loan Product is rejected with entity-linked error
    When Admin creates WC Delinquency Bucket With Values
    When Admin creates a new Working Capital Loan Product with existing WC Delinquency Bucket
    Then Admin failed to delete WC Delinquency Bucket that is assigned to a Working Capital Loan Product
    Then Admin deletes a Working Capital Loan Product
    When Admin deletes WC Delinquency Bucket With Values

  @TestRailId:C106805
  Scenario: Verify updating WC Delinquency Bucket without bucketType keeps WORKING_CAPITAL type
    When Admin creates WC Delinquency Bucket With Values
    When Admin updates WC Delinquency Bucket name without bucketType
    Then Check Delinquency Bucket still has bucketType "WORKING_CAPITAL"
    When Admin deletes WC Delinquency Bucket With Values

  @TestRailId:C106806
  Scenario: Verify changing WC Delinquency Bucket type to REGULAR while assigned to a product is rejected
    When Admin creates WC Delinquency Bucket With Values
    When Admin creates a new Working Capital Loan Product with existing WC Delinquency Bucket
    Then Admin failed to update WC Delinquency Bucket bucketType to "REGULAR" while assigned to a Working Capital Loan Product
    Then Admin deletes a Working Capital Loan Product
    When Admin deletes WC Delinquency Bucket With Values

  @TestRailId:C106837
  Scenario: Verify changing WC Delinquency Bucket type to REGULAR while overridden on a Working Capital Loan is rejected
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a Working Capital Loan Product with custom breach config and overrides enabled:
      | breachFrequency | breachFrequencyType | breachAmountCalculationType | breachAmount | breachGraceDays |
      | 1               | MONTHS              | FLAT                        | 500          | 5               |
    And Admin creates WC Delinquency Bucket With Values:
      | frequency | frequencyType | minimumPaymentType | minimumPayment |
      | 2         | WEEKS         | FLAT               | 248            |
    And Admin creates a working capital loan using created product with breachGraceDays 11 and the following data:
      | submittedOnDate | expectedDisbursementDate | principalAmount | totalPaymentVolume | periodPaymentRate | discount | delinquencyBucketId | delinquencyGraceDays |
      | 01 January 2026 | 01 January 2026          | 9000            | 100000             | 18                | 0        |                     | 13                   |
    And Admin modifies the working capital loan with delinquency override data
    Then Admin failed to update WC Delinquency Bucket bucketType to "REGULAR" while overridden on a Working Capital Loan
