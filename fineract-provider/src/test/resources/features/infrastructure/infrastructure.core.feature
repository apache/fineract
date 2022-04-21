Feature: Core Infrastructure

  @infrastructure
  Scenario: Verify that schema migration is not executed when set to disabled
    Given Liquibase is disabled with a default tenant
    When The database migration process is executed
    Then The database migration did not do anything

  @infrastructure
  Scenario: Verify that schema migration is not executed when the Fineract instance is a read instance
    Given Set every Fineract instance type to false
    Given Fineract instance is a read instance
    Given Liquibase is enabled with a default tenant
    When The database migration process is executed
    Then The database migration did not do anything, because it is not a write instance

  @infrastructure
  Scenario: Verify that schema migration is not executed when the Fineract instance is a batch instance
    Given Set every Fineract instance type to false
    Given Fineract instance is a batch instance
    Given Liquibase is enabled with a default tenant
    When The database migration process is executed
    Then The database migration did not do anything, because it is not a write instance

  @infrastructure
  Scenario: Verify that schema migration works from scratch
    Given Set every Fineract instance type to false
    Given Fineract instance is a write instance
    Given Liquibase is enabled with a default tenant
    Given Liquibase runs the very first time for the tenant store
    Given Liquibase runs the very first time for the default tenant
    When The database migration process is executed
    Then The tenant store and the default tenant gets upgraded from scratch

  @infrastructure
  Scenario: Verify that schema migration works with the latest Flyway migrated schemas
    Given Liquibase is enabled with a default tenant
    Given Liquibase runs the very first time for the tenant store
    Given A previously Flyway migrated tenant store database on the latest version
    Given Liquibase runs the very first time for the default tenant
    Given A previously Flyway migrated default tenant database on the latest version
    When The database migration process is executed
    Then The tenant store and the default tenant gets synced and then upgraded

  @infrastructure
  Scenario: Verify that schema migration fails when the tenant store is not on the latest Flyway migrated schemas
    Given Liquibase is enabled with a default tenant
    Given Liquibase runs the very first time for the tenant store
    Given A previously Flyway migrated tenant store database on an earlier version
    Given Liquibase runs the very first time for the default tenant
    Given A previously Flyway migrated default tenant database on the latest version
    When The database migration process is executed
    Then The tenant store upgrade fails with a schema upgrade needed

  @infrastructure
  Scenario: Verify that schema migration fails when the default tenant is not on the latest Flyway migrated schemas
    Given Liquibase is enabled with a default tenant
    Given Liquibase runs the very first time for the tenant store
    Given A previously Flyway migrated tenant store database on the latest version
    Given Liquibase runs the very first time for the default tenant
    Given A previously Flyway migrated default tenant database on an earlier version
    When The database migration process is executed
    Then The default tenant upgrade fails with a schema upgrade needed

  @infrastructure
  Scenario Outline: Verify empty multi exceptions
    Given A multi exception with exceptions <exception1> and <exception2>
    Then A <expected> should be thrown

    Examples:
      | exception1                         | exception2                      | expected                                                         |
      |                                    |                                 | java.lang.IllegalArgumentException                               |
      | java.lang.IllegalArgumentException | java.lang.IllegalStateException | org.apache.fineract.infrastructure.core.exception.MultiException |
