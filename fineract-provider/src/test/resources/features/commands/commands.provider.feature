Feature: Commands Service

  @template
  Scenario Outline: Verify that command providers are injected
    Given A command handler for entity <entity> and action <action>
    When The user processes the command with ID <id>
    Then The command ID matches <id>

    Examples:
      | id  | entity | action |
      | 815 | HUMAN  | UPDATE |

  @template
  Scenario Outline: Verify that command no command handler is provided
    Given A missing command handler for entity <entity> and action <action>
    Then The system should throw an exception

    Examples:
      | entity   | action      |
      | WHATEVER | DOSOMETHING |
