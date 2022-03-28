Feature: Accounting Service

  @accounting
  Scenario: Verify financial activities constants
    Given All financial activities
    Then The they should not be empty
