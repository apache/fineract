@ClientFeature
Feature: Client

  @TestRailId:C14 @Smoke
  Scenario Outline: Client creation functionality for Fineract
    When Admin creates a client with Firstname <firstName> and Lastname <lastName>
    Then Client is created successfully
    Examples:
      | firstName    | lastName |
      | "FirstName1" | "Test1"  |


  @TestRailId:C15
  Scenario Outline: Client creation with address functionality for Fineract
    When Global configuration "enable-address" is enabled
    When Admin creates a client with Firstname <firstName> and Lastname <lastName> with address
    Then Client is created successfully
    Examples:
      | firstName    | lastName |
      | "FirstName2" | "Test2"  |
