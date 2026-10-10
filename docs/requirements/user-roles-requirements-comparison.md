Continue the existing User and Roles Phase 1 implementation on branch `feat/user-role-administration`.

Do not create another branch. Do not commit, push, merge or rebase.

The current work is an acceptable intermediate checkpoint, but it is not ready for review or commitment.

First perform these checks:

1. Show `git status --short`.
2. Show `git diff --stat`.
3. List all untracked files.
4. Confirm the exact paths of:

   * the implementation report;
   * the CSV change log;
   * the database migration;
   * every new Java file.
5. Check whether the branch was created from the intended local `develop` commit.
6. Fetch remote references and report whether local `develop` is behind, ahead of or diverged from `origin/develop`.
7. Do not change the branch base without my approval.

Requirements document:

I will provide the previously generated requirements comparison separately.

Save its exact content as:

`docs/requirements/user-roles-requirements-comparison.md`

Do not reconstruct or summarize missing requirements from memory. Once supplied, compare the implementation against every listed User and Roles field and add a traceability table to the implementation report with these columns:

`Requirement | Existing Fineract Capability | Nsimbi Extension | Implementation Status | Files | Tests | Deferred Reason`

Immediate correction:

Add the repository-required full Apache licence header to every newly created Java source file. Do not alter existing licence, NOTICE, attribution or copyright files.

Continue Phase 1 with the following priorities:

1. Complete persistence mappings and database migration validation.
2. Complete validation for:

   * administrative suspension/reactivation;
   * additional branch assignments;
   * role operating hours;
   * monetary authority ranges;
   * ISO currency codes;
   * minimum amount not exceeding maximum amount;
   * duplicate records and assignments.
3. Complete request and response DTOs.
4. Add management service and command-handler operations.
5. Add properly authorized REST API endpoints.
6. Ensure every modifying operation uses Fineract’s command/audit conventions.
7. Add unit and integration tests.
8. Update the CSV and Markdown implementation report after each logical change.

Suspension requirements:

* A suspended user must be rejected during authentication.
* Failed authentication must not update `lastLoginAt`.
* Successful authentication must update `lastLoginAt`.
* Suspension and reactivation must require their respective permissions.
* Suspension must not delete roles, office assignments or monetary policies.
* Determine how authentication sessions or tokens work in this Fineract revision.
* Propose the safest compatible method for invalidating existing access after suspension.
* If immediate session invalidation cannot be completed safely in this logical unit, document the exact technical limitation and ensure subsequent authenticated requests still reject the suspended user where possible.
* Add tests covering all these cases.

Branch-assignment requirements:

* Primary office remains the existing required Fineract office.
* Additional offices are explicit assignments.
* Reject duplicate additional-office assignments.
* Decide and document whether assigning the primary office again as an additional office is rejected or ignored.
* Check tenant ownership and office existence.
* Require `MANAGE_USER_BRANCH_ASSIGNMENTS` for management operations.
* Do not yet modify every banking endpoint.
* Provide a reusable authorization/policy service for future endpoints.
* Test assigned and unassigned-office decisions.

Role operating-hours requirements:

* Use `Africa/Kampala` as the initial policy timezone.
* Do not hard-code the server’s system timezone.
* Validate opening and closing times.
* Define behaviour for overnight time ranges instead of accidentally accepting them.
* Make clock/time access injectable for deterministic tests.
* Require `MANAGE_ROLE_OPERATING_HOURS`.
* Add boundary tests for exactly opening time, exactly closing time, before opening and after closing.
* Do not yet terminate existing sessions at closing time.

Monetary-authority requirements:

* Keep all ten required operation types.
* Use `BigDecimal`.
* Store an explicit ISO currency.
* Comparisons are inclusive.
* Reject a minimum greater than the maximum.
* An absent or unset authority must not silently mean unlimited.
* Existing users must not be locked out merely because the migration has run.
* Keep actual transaction-handler enforcement deferred until we approve a backward-compatible rollout.
* Add policy-service tests for:

  * exact minimum;
  * exact maximum;
  * below minimum;
  * above maximum;
  * inside range;
  * missing authority;
  * wrong currency;
  * invalid configuration.

Role disablement:

Do not change core Fineract’s assigned-role disablement guard yet.

Instead:

1. Analyze its security and compatibility consequences.
2. Propose the smallest safe change.
3. Identify required regression tests.
4. Record it as a deferred decision requiring approval.

Till and Chart Accounts:

Keep these requirements in the traceability matrix but do not implement them yet. Do not assume Teller, Cashier, Till and GL Account are equivalent.

Testing environment:

The previous run found Java 21, while the repository instructions require Java 25.

Before installing or modifying system Java:

1. Inspect Gradle toolchain configuration.
2. Inspect repository scripts, containers, devcontainers and CI workflows for the intended Java 25 setup.
3. Check whether a compatible Java 25 installation already exists.
4. Prefer the repository-supported environment.
5. Use a writable project-specific Gradle cache if required.
6. Do not alter global Java or Gradle configuration without approval.

Run the narrowest relevant tests first. If Java 25 remains unavailable, still inspect and write the tests, but do not claim they pass.

Documentation:

Continue updating:

* `docs/changes/user-roles-change-log.csv`
* `docs/changes/user-roles-implementation-report.md`

The report must remain understandable to someone learning Java. For each logical feature, explain the path from API request through validation, command/service, entity/repository and database.

Do not claim a feature is complete merely because its data can be stored. Distinguish:

* persisted;
* validated;
* exposed through API;
* authorized;
* enforced;
* tested.

At the end of this continuation, report:

1. completed functionality;
2. incomplete functionality;
3. changed and untracked files;
4. migrations;
5. API endpoints;
6. permission checks;
7. exact tests executed and results;
8. Java/Gradle environment status;
9. requirements traceability status;
10. risks and approval decisions;
11. suggested next logical unit;
12. confirmation that the CSV and Markdown reports are current.

Do not commit until I explicitly approve it.
