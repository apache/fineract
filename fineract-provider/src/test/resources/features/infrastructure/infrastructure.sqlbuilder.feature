Feature: SQL Builder

  @sqlbuilder
  Scenario Outline: Verify that SQL builder has expected template and arguments
    Given A criteria <criteria1>, <criteria2>, <criteria3>, <criteria4> and arguments <argument1>, <argument2>, <argument3>, <argument4>
    Then The template should match expected template <template>
    Then The arguments should match expected arguments <arguments>
    Then The builder should match <expected>

    Examples:
      | criteria1  | argument1             | criteria2  | argument2             | criteria3 | argument3 | criteria4 | argument4 | template                                         | expected                                                                                           |
      |            |                       |            |                       |           |           |           |           |                                                  | SQLBuilder{}                                                                                                   |
      | name =     | Michael               | hobby LIKE | Mifos/Apache Fineract | age <     | 123       |           |           | WHERE  name = ?  AND  hobby LIKE ?  AND  age < ? | SQLBuilder{WHERE  name = ['Michael']  AND  hobby LIKE ['Mifos/Apache Fineract']  AND  age < [123]} |
      | ref =      | NULL                  |            |                       |           |           |           |           | WHERE  ref = ?                                   | SQLBuilder{WHERE  ref = [null]}                                                                    |
      | hobby LIKE | Mifos/Apache Fineract | hobby like | Mifos/Apache Fineract |           |           |           |           | WHERE  hobby LIKE ?  AND  hobby like ?           |                                                                                                    |

  @sqlbuilder
  Scenario Outline: Verify that SQL builder detects illegal criteria
    Given An illegal criteria <criteria> with argument <argument>
    Then The builder should throw an exception <exception> with message <message>

    Examples:
      | criteria    | argument | message                               | exception                          |
      | NULL        | argument | <NULL> criteria fragment              | java.lang.IllegalArgumentException |
      | EMPTY       | argument | <EMPTY> criteria fragment             | java.lang.IllegalArgumentException |
      | age<        | 123      | Space between column and operator     | java.lang.IllegalArgumentException |
      | age = ?     | 123      | Criteria with placeholder ?           | java.lang.IllegalArgumentException |
      | age         | 123      | Criteria missing operator             | java.lang.IllegalArgumentException |
      | and age = ? | 123      | Criteria starts with AND              | java.lang.IllegalArgumentException |
      | age = ? and | 123      | Criteria ends with AND                | java.lang.IllegalArgumentException |
      | or age = ?  | 123      | Criteria start with OR                | java.lang.IllegalArgumentException |
      | age = ? or  | 123      | Criteria ends with OR                 | java.lang.IllegalArgumentException |
      | (age =      | 123      | Criteria contains opening parenthesis | java.lang.IllegalArgumentException |
      | age = ?     | 123      | Criteria contains closing parenthesis | java.lang.IllegalArgumentException |
      | age< = ?    | 123      | Offset corner case                    | java.lang.IllegalArgumentException |
