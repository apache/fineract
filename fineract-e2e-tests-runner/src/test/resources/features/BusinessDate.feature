@BusinessDateFeature
Feature: BusinessDate


  Scenario: As a user I would like to enable the Business date configuration
    Given Global configuration "enable_business_date" is enabled


  Scenario: As a user I would like to disable the Business date configuration
    Given Global configuration "enable_business_date" is disabled


  Scenario: As a user I would like to set the business date
    When Admin sets the business date to "10 July 2022"
    Then Admin checks that the business date is correctly set to "10 July 2022"


  Scenario: As a user I would like to change the business date manually
    When Admin sets the business date to "10 July 2022"
    Then Admin checks that the business date is correctly set to "10 July 2022"
    When Admin sets the business date to "11 July 2022"
    Then Admin checks that the business date is correctly set to "11 July 2022"


  Scenario: As a user I would like to change the business date with scheduled job
    When Admin sets the business date to "10 July 2022"
    When Admin runs the Increase Business Date by 1 day job
    Then Admin checks that the business date is correctly set to "11 July 2022"
