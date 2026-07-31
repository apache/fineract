@SavingsProduct
Feature: SavingsProduct

  @SavingsProductOfficeRestrictionFeature
  Scenario: As a user I would like to retrieve savings products when office-specific product restriction is enabled and my office has no explicit product mapping
    Given Global configuration "office-specific-products-enabled" is enabled
    And Global configuration "restrict-products-to-user-office" is enabled
    When Savings products are retrieved successfully
