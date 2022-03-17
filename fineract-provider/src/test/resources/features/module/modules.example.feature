Feature: Example Modules

  @modules
  Scenario Outline: Verify that the dummy service returns the correct message
    Given A dummy service configuration <configurationClass>
    When The user gets the dummy service message
    Then The dummy service message should match <message>

    Examples:
      | configurationClass                                          | message               |
      | org.apache.fineract.module.example.TestDefaultConfiguration | Hello: DEFAULT DUMMY! |
      | org.apache.fineract.module.example.TestFooConfiguration     | Hello: FOO!           |