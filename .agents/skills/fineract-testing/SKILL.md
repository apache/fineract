---
name: fineract-testing
description: Choose, write, run and verify tests for Apache Fineract changes, covering JUnit unit tests, the fineract-provider Cucumber tests, Feign-client integration tests, the two-factor and OAuth2 suites, and Cucumber E2E scenarios. Use when adding or changing tests, when a code change needs test coverage, when a test fails, or before claiming that a change works.
---

# Fineract Testing

Paths are relative to the repository root, and `.adoc` names refer to
`fineract-doc/src/docs/en/chapters/testing/`. Each rule below comes from the file or
check named next to it; if this skill and that source disagree, the source wins.

## Choose the suite

| Suite | Where the tests live | Needs a running server | Guide |
| --- | --- | --- | --- |
| Unit | `src/test/java` of the module you changed | No | `CONTRIBUTING.md`, "Unit tests" |
| Cucumber in `fineract-provider` | `fineract-provider/src/test/resources/features` | No | `.github/workflows/build-cucumber.yml` |
| Integration | `integration-tests` | Yes | `integration.adoc`, `integration-feign.adoc` |
| Two-factor, OAuth2 | `twofactor-tests`, `oauth2-tests` | Yes, started in that auth mode | `integration.adoc`, "Authentication modes" |
| Cucumber E2E | `fineract-e2e-tests-runner` (features), `fineract-e2e-tests-core` (steps) | Yes | `cucumber.adoc` |

The build never starts a server. Start one before running any suite marked "Yes",
or every API call fails (`CONTRIBUTING.md`, "How to run tests").

## Unit tests

1. Use JUnit Jupiter, with Mockito through `@ExtendWith(MockitoExtension.class)`, as the
   example in rule 2 does. The modules exclude the JUnit 4 vintage engine (for example
   `fineract-provider/dependencies.gradle`), so a JUnit 4 `@Test` never runs.
2. Code that reads the tenant or the business date needs the thread context. Set it in
   `@BeforeEach` and clear it with `ThreadLocalContextUtil.reset()` in `@AfterEach`.
   `Money` also needs the tenant's rounding mode, which sits in a static cache that
   `reset()` does not clear: set it with `MoneyHelper.initializeTenantRoundingMode` and
   remove it with `MoneyHelper.clearCacheForTenant`, as
   `fineract-working-capital-loan/src/test/java/org/apache/fineract/portfolio/workingcapitalloan/service/WorkingCapitalLoanAsOfBalanceReadServiceImplTest.java`
   does.
3. Let exceptions propagate instead of catching them. To test a failure, use
   `assertThrows`, not `@Test(expected = ...)` (`CONTRIBUTING.md`, "Error Handling").
4. Run one class with `./gradlew :<module>:test --tests <ClassName>`. The command for the
   whole fast suite is in `CONTRIBUTING.md`, "Unit tests". If you changed
   `fineract-provider`, also run `./gradlew :fineract-provider:cucumber`, because `test`
   does not run its features.

## Integration tests

New integration tests call the server through the generated Feign client. Read rules
IT-01 to IT-20 in `integration-feign.adoc` before writing one.

1. Extend the base class for the product under test: `FeignLoanTestBase`,
   `FeignSavingsTestBase`, `FeignWorkingCapitalTestBase` or `FeignDepositTestBase` in
   `integration-tests/src/test/java/org/apache/fineract/integrationtests/client/feign/`,
   otherwise `FeignIntegrationTest` one directory up.
2. The loan and savings bases register `LoanTestLifecycleExtension` and
   `FeignSavingsLifecycleExtension`. Before and after every test, these close, un-approve
   or reject every open loan or savings account in the tenant, so create accounts inside
   each test, not in `@BeforeAll`.
3. If your class needs its own `@BeforeAll`, give it a new name. Reusing the base
   method's name (`setupHelpers`, `setupSavingsHelpers`, `setupWorkingCapitalHelpers`,
   `setupDepositHelpers`) hides it, and the base helpers are never set up.
4. Make calls through `FeignCalls` (`ok`, `executeVoid`, `fail`, `failVoid`) from
   `fineract-client-feign/src/main/java/org/apache/fineract/client/feign/util/FeignCalls.java`,
   and assert the exact status and error code of a failure (IT-02, IT-10).
5. Never import `io.restassured`, and never add a file to
   `config/checkstyle/restassured-baseline.xml`: the `FeignNoRestAssured` check in
   `config/checkstyle/checkstyle.xml` fails the build. A test you migrate off REST
   Assured deletes its baseline entry in the same change (`integration-feign.adoc`,
   "The Baseline").
6. If the generated model lacks a field, add it to the matching `*ApiResourceSwagger`
   class and regenerate both clients. Do not work around it in the test (IT-05,
   "Fixing a Generated Model at Source").
7. For anything that depends on the date, run the steps inside `runAt(date, ...)`. The
   loan, savings and working capital bases provide it; elsewhere, call `runAt` on a
   `FeignBusinessDateHelper`
   (`integration-tests/src/test/java/org/apache/fineract/integrationtests/client/feign/helpers/FeignBusinessDateHelper.java`).
   It turns the business date on, sets it, and turns it off again afterwards.
8. Keep each test class self-contained, and undo any global configuration you change.
   CI splits test classes across shards on each of PostgreSQL, MySQL and MariaDB
   (`.github/workflows/build-postgresql.yml` and its MySQL and MariaDB variants), so no
   class can rely on another one having run.

## Keep the default lists in sync

Some tests compare the database with a list kept by hand. When your change adds one
of these, update the list in the same change:

| When you add | Also update | Otherwise this fails |
| --- | --- | --- |
| A global configuration (`c_configuration` row) | `GlobalConfigurationHelper.getAllDefaultGlobalConfigurations()` in `integration-tests/src/test/java/org/apache/fineract/integrationtests/common/GlobalConfigurationHelper.java` | Tests that reset global configurations, such as `GlobalConfigurationTest` |
| A scheduler job (`JobName` value) | Its `job` row, inserted by a Liquibase changeset | `SchedulerJobsTest`, which compares the two counts |
| An external business event type | `ExternalEventConfigurationTestData` in `integration-tests/src/test/java/org/apache/fineract/integrationtests/client/feign/modules/` | `ExternalEventConfigurationIntegrationTest` |

## Cucumber E2E scenarios

1. Put scenarios in a `.feature` file under
   `fineract-e2e-tests-runner/src/test/resources/features` and step definitions under
   `fineract-e2e-tests-core/src/test/java/org/apache/fineract/test/stepdef`
   (`cucumber.adoc`, "Writing New E2E Tests").
2. On a fresh database, run once with `INITIALIZATION_ENABLED=true` to create the
   products and charges the scenarios expect, and enable the business date
   (`cucumber.adoc`, "Test Data Initialization" and "Business Date Configuration").
3. From `fineract-e2e-tests-runner`, run one feature with
   `../gradlew cucumber -Pcucumber.features="src/test/resources/features/<Name>.feature"`,
   and filter with `-Pcucumber.tags`. A filter that matches nothing reports
   `0 scenarios` and still exits 0, so read the summary line (`integration.adoc`,
   "The WAR deployment").
4. Event assertions are skipped unless `EVENT_VERIFICATION_ENABLED=true`. E2E CI sets
   it and runs ActiveMQ (`.github/workflows/build-e2e-tests.yml`,
   `docker-compose-postgresql-test-activemq.yml`), so a local run without it does not
   check events.
5. Use `@TestRailId:C<number>` only for an existing TestRail case, because results are
   posted to that case
   (`fineract-e2e-tests-core/src/test/java/org/apache/fineract/test/testrail/TestRailClient.java`).
   If there is none, ask in the PR rather than guessing.

## Run and verify

1. Start a server first. For the integration suites, use the Docker stack in
   `CONTRIBUTING.md`, "Integration tests", which CI also uses; that section also covers
   running without Docker. For E2E, see the previous section.
2. Run the tests you added or changed against that server. Compiling and
   `spotlessCheck` are not verification (IT-18).
3. If a test fails, run it at the base commit before blaming your change (IT-20). If your
   change touches server code, rebuild and restart the server at the base commit too.
4. Check the report for skipped tests. Some `fineract-provider` tests are skipped, not
   failed, when Docker is not running (`@Testcontainers(disabledWithoutDocker = true)`).
5. If Gradle reports the test task as `UP-TO-DATE` or `FROM-CACHE`, the tests did not
   run. Add `--rerun-tasks` (`CONTRIBUTING.md`, "How to run tests").
6. Before pushing, run `./gradlew spotlessApply` and the checks in
   `.github/workflows/build-quality-checks.yml` that your change touches (Checkstyle,
   Spotless, SpotBugs, RAT, Javadoc).

## Flaky tests

Follow `CONTRIBUTING.md`, "Pull Requests". First rule out your own change ("Run and
verify", step 3). Search JIRA for the test; if there is no report, file one and link it
to FINERACT-850. Propose `@Disabled // TODO FINERACT-NNNN` only when comments show the
test failing in at least 3 past PRs, and only in a separate PR.
