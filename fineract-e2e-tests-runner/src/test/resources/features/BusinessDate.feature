@BusinessDateFeature
Feature: BusinessDate

  @TestRailId:C38
  Scenario: As a user I would like to enable the Business date configuration
    Given Global configuration "enable-business-date" is enabled

  @TestRailId:C39
  Scenario: As a user I would like to disable the Business date configuration
    Given Global configuration "enable-business-date" is disabled

  @TestRailId:C40
  Scenario: As a user I would like to set the business date
    When Admin sets the business date to "10 July 2022"
    Then Admin checks that the business date is correctly set to "10 July 2022"

  @TestRailId:C41
  Scenario: As a user I would like to change the business date manually
    When Admin sets the business date to "10 July 2022"
    Then Admin checks that the business date is correctly set to "10 July 2022"
    When Admin sets the business date to "11 July 2022"
    Then Admin checks that the business date is correctly set to "11 July 2022"

  @TestRailId:C27
  Scenario: As a user I would like to change the business date with scheduled job
    When Admin sets the business date to "10 July 2022"
    When Admin runs the Increase Business Date by 1 day job
    Then Admin checks that the business date is correctly set to "11 July 2022"

  @TestRailId:C3953
  Scenario: Verify set incorrect business date with empty value handled correct with accordance error message - UC1
    When Set incorrect business date with empty value "null" outcomes with an error

  @TestRailId:C3954
  Scenario: Verify set incorrect business date with null value handled correct with accordance error message - UC2
    When Set incorrect business date with empty value "" outcomes with an error

  @TestRailId:C3955
  Scenario: Verify set incorrect business date value handled correct with accordance error message - UC3
    When Set incorrect business date value "15" outcomes with an error

  @TestRailId:C3956
  Scenario: Verify set incorrect business date value handled correct with accordance error message - UC4
    When Set incorrect business date value "11 15 2025" outcomes with an error

  @TestRailId:C_3957
  Scenario: Verify set incorrect business date value handled correct with accordance error message - UC5
    When Set incorrect business date value "August 12 2025" outcomes with an error

  @TestRailId:C3958
  Scenario: Verify set incorrect business date value handled correct with accordance error message - UC6
    When Set incorrect business date value "33 August 2025" outcomes with an error

