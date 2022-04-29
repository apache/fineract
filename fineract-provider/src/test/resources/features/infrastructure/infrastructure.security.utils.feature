Feature: Security Utils Infrastructure

  @security
  Scenario: Verify that log parameter escape util does not change strings that does not contain any special character
    Given A simple log message without any special character
    When Log parameter escape util escaping the special characters
    Then The log message stays as it is

  @security
  Scenario: Verify that log parameter escape util changes special characters in string
    Given A log message with new line, carriage return and tab characters
    When Log parameter escape util escaping the special characters
    Then The escape util changes the special characters to `_`
