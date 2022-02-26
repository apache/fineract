Feature: Core Infrastructure

  @infrastructure
  Scenario: Verify that tenant migration service is properly executed
    Given Liquibase is disabled with a default tenant
    Given Liquibase runs the very first time for the tenant store
    Given A previously Flyway migrated tenant store database
    Given A previously Flyway migrated tenant store database on an earlier version than 1.6
    Given Liquibase runs the very first time for the default tenant
    Given A previously Flyway migrated default tenant database
    Given A previously Flyway migrated default tenant database on the 1.6 version
    When The database migration process is executed
    Then The tenant store upgrade fails with a schema upgrade needed

  @infrastructure
  Scenario Outline: Verify empty multi exceptions
    Given A multi exception with exceptions <exception1> and <exception2>
    Then A <expected> should be thrown

    Examples:
      | exception1                         | exception2                      | expected                                                         |
      |                                    |                                 | java.lang.IllegalArgumentException                               |
      | java.lang.IllegalArgumentException | java.lang.IllegalStateException | org.apache.fineract.infrastructure.core.exception.MultiException |
