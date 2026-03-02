@CurrencyFeature
Feature: Currency

  @TestRailId:C3963
  Scenario: Verify USD currency properties are correct
    When Admin retrieves currency configuration
    Then Currency "USD" has the following properties:
      | name      | symbol | decimalPlaces |
      | US Dollar | $      | 2             |

  @TestRailId:C3964
  Scenario: Verify updating selected currencies works correctly
    When Admin updates selected currencies to "EUR,KES,BND,LBP,GHC,USD,INR"
    Then The returned currency list matches "EUR,KES,BND,LBP,GHC,USD,INR"
    When Admin retrieves currency configuration
    Then The selected currencies contain "EUR,KES,BND,LBP,GHC,USD,INR"
    And Admin resets selected currencies to default
