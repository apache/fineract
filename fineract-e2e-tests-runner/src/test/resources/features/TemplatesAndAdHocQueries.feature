@TemplatesAndAdHocQueriesFeature
Feature: Templates and ad hoc queries

  Scenario: A template with a plain placeholder is merged
    When Admin creates a template with text "Hello {{name}}"
    Then Merging the last created template with value "World" succeeds

  Scenario: A template that includes another template by name is rejected
    When Admin creates a template with text "Hello {{>other}}"
    Then Merging the last created template with value "World" is rejected

  Scenario: An ad hoc query is updated only with a valid SQL statement
    When Admin creates an ad hoc query
    Then Updating the last created ad hoc query with SQL "SELECT id FROM m_client WHERE 1 = 0" succeeds
    Then Updating the last created ad hoc query with SQL "SELECT id FROM m_client; drop table x" is rejected
