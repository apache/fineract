Feature: Template Service

  @template
  Scenario Outline: Verify that mustache templates have expected results
    Given A mustache template file <template>
    Given A JSON data file <json>
    When The user merges the template with data
    Then The result should match the content of file <result>

    Examples:
      | template             | json       | result          |
      | hello.mustache       | hello.json | hello.txt       |
      | loan.mustache        | loan.json  | loan.html       |
      | array.loop.mustache  | array.json | array.loop.txt  |
      | array.index.mustache | array.json | array.index.txt |
