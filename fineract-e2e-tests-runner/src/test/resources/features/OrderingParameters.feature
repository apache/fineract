@OrderingParametersFeature
Feature: Ordering parameters

  Scenario Outline: List endpoints accept a column name as orderBy
    Then Listing "<list>" ordered by "id" with sort order "DESC" is accepted
    Then Listing "<list>" ordered by "id" with sort order "asc" is accepted

    Examples:
      | list                      |
      | journal entries           |
      | scheduler job run history |
      | loans                     |

  Scenario Outline: List endpoints reject an orderBy that is not a column name
    Then Listing "<list>" ordered by "(select 1)" with sort order "ASC" is rejected
    Then Listing "<list>" ordered by "id; select 1" with sort order "ASC" is rejected

    Examples:
      | list                      |
      | journal entries           |
      | scheduler job run history |
      | loans                     |

  Scenario Outline: List endpoints reject a sort order other than ASC or DESC
    Then Listing "<list>" ordered by "id" with sort order "id" is rejected
    Then Listing "<list>" ordered by "id" with sort order "ASC, id" is rejected

    Examples:
      | list                      |
      | journal entries           |
      | scheduler job run history |
      | loans                     |
