---
name: fineract-database
description: Add or change Apache Fineract database schema and seed data with Liquibase, covering where a changelog part goes and how it is included, the PostgreSQL and MySQL contexts, the DDL safety, registration and backward-compatibility checks, seed rows such as permissions and configurations, and how Java code reaches the database. Use when adding or editing a Liquibase changelog, a table, column, index or seed row, a JPA relationship or a native SQL query, or when a database check fails in CI.
---

# Fineract Database Changes

Paths are relative to the repository root, `db.changelog-master.xml` is
`fineract-provider/src/main/resources/db/changelog/db.changelog-master.xml`, and
`persistence.adoc` is `fineract-doc/src/docs/en/chapters/architecture/persistence.adoc`. Each
rule below comes from the file or check named next to it; if this skill and that source
disagree, the source wins.

## Where a change goes

Liquibase runs only the changelogs that `db.changelog-master.xml` reaches. Each tree has
its own `parts` directory, its own numbering and its own master changelog:

| Tables of | Parts directory | Included by |
| --- | --- | --- |
| The tenant database (core) | `fineract-provider/src/main/resources/db/changelog/tenant/parts` | `tenant/changelog-tenant.xml` |
| The tenant store (`fineract_tenants`) | `fineract-provider/src/main/resources/db/changelog/tenant-store/parts` | `tenant-store/changelog-tenant-store.xml` |
| A module with its own tree (loan, savings, progressive loan, working capital loan, loan origination, investor, command) | `<module>/src/main/resources/db/changelog/tenant/module/<name>/.../parts` | The `module-changelog-master.xml` that includes the other parts of that directory |

1. Put the change in the tree of the module that owns the table. A core part that alters a
   module's tables goes in `tenant/final-changelog-tenant.xml`, which runs after the modules
   (comment in `db.changelog-master.xml`).
2. Name the file with the next free number of that directory and what it does, like
   `0248_add_dates_to_client_identifier.xml` in the core tree, and add its `<include>` at the
   end of the master changelog. Another open PR may take the same number first; renumber when
   you rebase.
3. Run `./scripts/check-liquibase-changelog-registration.sh --base-ref origin/develop`. It
   fails on a part that no master includes, a master that `db.changelog-master.xml` does not
   reach, a file included twice or an include of a missing file, and warns when a new part
   reuses a number. CI runs it in `.github/workflows/verify-liquibase-changelog-registration.yml`.
   Run it again after resolving a conflict in a master changelog: a lost `<include>` breaks
   nothing visibly, the part just never runs.
4. Never edit, rename or move a changeset that is already on develop; add a new one.
   Liquibase identifies a changeset by its id, author and file path, so a moved file runs
   again, and it refuses to start when the checksum of a changeset it has run changes. The
   few old changesets that were edited carry `<validCheckSum>` entries, for example
   `fineract-provider/src/main/resources/db/changelog/tenant/parts/0015_add_business_date.xml`.

## Databases and contexts

1. PostgreSQL is the supported database; MySQL and MariaDB are deprecated but still
   supported (`persistence.adoc`, "Database support"). CI runs the integration tests on all
   three (`.github/workflows/build-postgresql.yml` and its MySQL and MariaDB variants).
2. The contexts are `mysql`, `postgresql`, `tenant_db`, `tenant_store_db` and the active
   Spring profiles. Never use `initial_switch` (`persistence.adoc`, "Database schema
   migration"). The includes in `db.changelog-master.xml` already set `tenant_db` or
   `tenant_store_db`, so a part only needs `context="mysql"` or `context="postgresql"` on
   the changesets whose types or SQL differ.
3. Where the types differ, write one changeset per database. For example,
   `fineract-loan-origination/src/main/resources/db/changelog/tenant/module/loanorigination/parts/0005_wc_loan_originator_mapping.xml`
   declares `created_on_utc` as `DATETIME(6)` on MySQL and `TIMESTAMP WITH TIME ZONE` on
   PostgreSQL, with `created_by` and `last_modified_by` as foreign keys to `m_appuser`.
4. For the current date, the current time or a UUID in a changeset, use the
   `${current_date}`, `${current_datetime}` and `${uuid}` properties defined at the top of
   `db.changelog-master.xml`.
5. In Java, put a database-specific function in a native query through
   `DatabaseSpecificSQLGenerator`, and extend it when a function is missing
   (`persistence.adoc`, "Data-access layer").
6. Map every new JPA relationship with `FetchType.LAZY`; a PR that adds an `EAGER` one is
   rejected (`persistence.adoc`, "Data-access layer").

## DDL safety

`.github/workflows/verify-liquibase-ddl-safety.yml` runs `scripts/check-liquibase-ddl-safety.sh`
on the changelog files a PR changes. Run it locally with
`./scripts/check-liquibase-ddl-safety.sh --base-ref origin/develop --head-ref HEAD`.

1. On MySQL, declare date-time columns as `DATETIME(6)`. A plain `DATETIME` blocks the
   build, because MySQL would drop the fractional seconds. On PostgreSQL, the `_utc` and
   audit columns are `TIMESTAMP WITH TIME ZONE`, so write one changeset per database for them
   (point 3 of "Databases and contexts").
2. An index on a table that the PR does not create must not lock it on PostgreSQL. Use raw
   `CREATE INDEX CONCURRENTLY` in a changeset with `runInTransaction="false"` and
   `context="postgresql"`, and a separate MySQL changeset, as
   `fineract-provider/src/main/resources/db/changelog/tenant/parts/0177_acc_journal_entry_index.xml`
   does. A `<createIndex>` on such a table blocks the build unless its changeset runs only
   on MySQL.
3. `dropColumn`, `dropTable`, `renameColumn`, `renameTable`, `modifyDataType`,
   `addNotNullConstraint`, and an `addColumn` with `nullable="false"` and no default block
   the build on the tables listed in `config/liquibase/critical-tables.txt`, and are
   warnings on other tables. `config/liquibase/dangerous-operations.txt` gives the reason for
   each. The check treats tables that the same PR creates as safe.
4. The `ddl-safety-override` label makes the check report without blocking. Only a
   maintainer can add it, so explain in the PR why the operation is safe.

## Backward compatibility

During a rolling upgrade the old code runs against the new schema, so add columns as
nullable or with a default, and drop or rename a column only after a release in which no
code uses it (`config/liquibase/dangerous-operations.txt`). These workflows, called from
`.github/workflows/full-build-ci.yml`, check the migrations:

1. `verify-liquibase-backward-compatibility.yml` starts the base commit's backend on a fresh
   PostgreSQL database, then starts the PR's backend on the same database, so every new
   changeset must run as an upgrade.
2. `regression-safety-db-changes.yml` runs on PRs that change a changelog. It runs the
   `@RegressionSafety` E2E scenarios three times: old code with the old schema, old code with
   the new schema, and new code with the new schema. A failure only in the second run means
   the old code cannot work with the new schema.
3. `liquibase-only-postgresql.yml` runs the migrations alone, with the `liquibase-only`
   Spring profile, on a fresh PostgreSQL database.

## Seed rows

Some changes need a row that the code expects to exist. Insert it in the same change:

| When you add | Insert a row into | Example |
| --- | --- | --- |
| A command or another action that checks a permission | `m_permission`, and for a write command also its `<CODE>_CHECKER` row | `fineract-working-capital-loan/src/main/resources/db/changelog/tenant/module/workingcapitalloan/parts/0084_wc_loan_missing_command_permissions.xml` |
| A global configuration | `c_configuration` | `fineract-provider/src/main/resources/db/changelog/tenant/parts/0267_add_retained_earning_wc_report_global_configuration.xml` |
| A scheduler job (`JobName` value) | `job` | `fineract-savings/src/main/resources/db/changelog/tenant/module/savings/parts/parts/2001_add_savings_accrual_job.xml` |
| An external business event type | `m_external_event_configuration`, with `enabled` false | `fineract-provider/src/main/resources/db/changelog/tenant/parts/0243_add_client_lifecycle_event_configuration.xml` |

1. Tests that run as the superuser do not notice a missing `m_permission` row. It fails only
   when maker-checker is enabled, where
   `ConfigurationDomainServiceJpa.isMakerCheckerEnabledForTask` throws
   `PermissionNotFoundException`, or for a role that needs to be granted the permission.
   Without the `<CODE>_CHECKER` row (`action_name` `<ACTION>_CHECKER`), only
   `CHECKER_SUPER_USER` can approve the command, because
   `PortfolioCommandSourceWritePlatformServiceImpl` checks `taskPermission + "_CHECKER"`. The
   0084 example adds such rows that were missing.
2. Guard an insert with `<preConditions onFail="MARK_RAN">` and a `sqlCheck` for the row, as
   the `m_permission` and `c_configuration` examples above do, so that a database that
   already has the row skips the changeset.
3. Integration tests compare some of these tables with lists kept by hand:
   `GlobalConfigurationHelper`, `SchedulerJobsTest` and `ExternalEventConfigurationTestData`
   in `integration-tests`. Update them in the same change.

## Verify

1. Run the registration and DDL safety scripts above.
2. Start the backend on a fresh database (`CONTRIBUTING.md`, "Cucumber E2E tests", shows
   `createPGDB` and `bootRun`) and read the log for the changesets that ran. Then start it on
   a database that develop created, to check the upgrade.
3. If you wrote a changeset for one database only, check the other one too, or watch the
   MySQL and MariaDB jobs in CI: the PostgreSQL run cannot catch it.
