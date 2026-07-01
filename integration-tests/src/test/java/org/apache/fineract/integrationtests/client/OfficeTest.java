/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.integrationtests.client;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.PostOfficesRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Integration Test for /offices API.
 *
 * @author Michael Vorburger.ch
 */
public class OfficeTest extends IntegrationTest {

    @Test
    @Order(1)
    void createOne() {
        // NB parentId(1) always exists (Head Office)
        // NB name random() because Office Names have to be unique
        // TODO requiring dateFormat(..).locale(..) is dumb :( see https://issues.apache.org/jira/browse/FINERACT-1233
        assertThat(ok(fineractClient().offices.createOffice(new PostOfficesRequest().name(Utils.randomStringGenerator("TestOffice_", 6))
                .parentId(1L).openingDate(LocalDate.now(ZoneId.of("UTC"))).dateFormat("yyyy-MM-dd").locale("en_US"))).getOfficeId())
                .isGreaterThan(0);
    }

    @Test
    @Order(2)
    void retrieveOneExistingInclDateFormat() { // see FINERACT-1220 re. what this tests re. Date Format
        List<GetOfficesResponse> response = ok(fineractClient().offices.retrieveAllOffices(true, null, null));
        assertThat(response.size()).isGreaterThanOrEqualTo(1);
        assertThat(response.get(0).getOpeningDate()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // ZDRES-035 — SQL Injection via Subquery in orderBy (ColumnValidator Bypass)
    //
    // Security regression tests for the vulnerability reported by Venkatraman
    // Kumar (Securin) on 2026-04-14. The ColumnValidator (introduced as the
    // CVE-2024-32838 fix) did not detect bare subqueries in ORDER BY position
    // because:
    // - No semicolons → inject-stacked-query regex skips
    // - No AND/OR → inject-timing regex skips
    // - No comparison operators → getOperand() returns empty set,
    // validateColumn() passes with an empty map
    //
    // The fix replaces regex-based validation with a strict column-name whitelist
    // defined in application.properties (fineract.input-validation.patterns[4],
    // profile office-order-by). Invalid input is rejected with HTTP 403.
    // -------------------------------------------------------------------------

    /**
     * Whitelist positive test — all accepted column names from the /api/v1/offices response payload must continue to
     * work after the fix.
     *
     * <p>
     * Covers: id, name, nameDecorated, externalId, hierarchy, openingDate (pattern:
     * ^(id|name(Decorated)?|externalId|hierarchy|openingDate)$)
     */
    @ParameterizedTest(name = "orderBy=''{0}'' is whitelisted — must succeed")
    @ValueSource(strings = { "id", "name", "nameDecorated", "externalId", "hierarchy", "openingDate" })
    @Order(3)
    void retrieveAllOffices_withWhitelistedOrderByColumn_succeeds(String column) throws Exception {
        var response = fineractClient().offices.retrieveAllOffices(false, column, null).execute();
        assertThat(response.isSuccessful())
                .as("GET /offices?orderBy=%s should return 2xx (whitelist over-blocks), got HTTP %d", column, response.code()).isTrue();
        assertThat(response.body()).isNotNull();
    }

    /**
     * Null and whitespace orderBy must be treated as absent (no ORDER BY clause) and accepted, consistent with the
     * behaviour documented in ClientSearchTest (testClientSearchOrderByRejectsEmptyAndWhitespace → isTrue()).
     */
    @ParameterizedTest(name = "orderBy=''{0}'' is blank — treated as absent, must succeed")
    @ValueSource(strings = { "   ", "\t" })
    @Order(4)
    void retrieveAllOffices_withBlankOrderBy_isAcceptedAsAbsent(String blank) throws Exception {
        var response = fineractClient().offices.retrieveAllOffices(false, blank, null).execute();
        assertThat(response.isSuccessful()).as("Blank orderBy [%s] should be treated as absent, not rejected", blank).isTrue();
    }

    /**
     * Core ZDRES-035 regression — the exact subquery payloads from the Securin advisory that previously bypassed
     * ColumnValidator and executed on the database. All must be rejected with HTTP 403.
     *
     * <p>
     * Payloads included:
     * <ul>
     * <li>Basic time-based blind: confirmed 3.05 s in Securin DAST environment</li>
     * <li>Scalable sleep: confirmed 10.05 s</li>
     * <li>Data extraction via conditional timing: extracts DB user char by char</li>
     * <li>DoS vector: holds one HikariCP connection for 5 minutes</li>
     * <li>Minimal subquery: confirms the subquery execution path, no timing</li>
     * <li>MySQL/MariaDB dual-table probe</li>
     * </ul>
     */
    @ParameterizedTest(name = "ZDRES-035 subquery ''{0}'' must be rejected with 403")
    @ValueSource(strings = { "(SELECT SLEEP(3))", "(SELECT SLEEP(10))", "(SELECT IF(SUBSTRING(user(),1,4)='root',SLEEP(2),0))",
            "(SELECT SLEEP(300))", "(SELECT 1)", "(SELECT 1 FROM dual)", })
    @Order(5)
    void retrieveAllOffices_zdres035SubqueryPayloads_areRejected(String payload) throws Exception {
        var response = fineractClient().offices.retrieveAllOffices(false, payload, null).execute();
        assertThat(response.isSuccessful()).as("ZDRES-035 subquery [%s] must be rejected — whitelist fix not effective", payload).isFalse();
        assertThat(response.code()).as("Expected HTTP 403 for payload [%s], got %d", payload, response.code()).isEqualTo(403);
    }

    /**
     * Timing regression — verifies that (SELECT SLEEP(3)) is rejected in application code BEFORE the SQL reaches the
     * database.
     *
     * <p>
     * Without the whitelist fix, orderBy input is concatenated into SQL before ColumnValidator runs, so MariaDB
     * executes the subquery and the response takes ≥ 3 seconds. With the fix, the 403 must arrive well under 1 second.
     *
     * <p>
     * The assertion threshold is 2 000 ms — generous for CI variance, but a 3-second SLEEP executing on the database
     * always exceeds it.
     */
    @Test
    @Order(6)
    void retrieveAllOffices_sleepSubquery_isRejectedBeforeDatabaseExecution() throws Exception {
        long start = System.currentTimeMillis();
        var response = fineractClient().offices.retrieveAllOffices(false, "(SELECT SLEEP(3))", null).execute();
        long elapsed = System.currentTimeMillis() - start;

        assertThat(response.code()).as("Expected HTTP 403 for SLEEP(3) subquery, got %d", response.code()).isEqualTo(403);
        assertThat(elapsed)
                .as("Response took %d ms — if >= 2000 ms the SLEEP(3) executed on the DB; whitelist fix is not active on this code path",
                        elapsed)
                .isLessThan(2000L);
    }

    /**
     * Classic SQL injection patterns that ColumnValidator already blocked before the ZDRES-035 fix. The stricter
     * whitelist must continue to reject them — no regression in pre-existing coverage.
     */
    @ParameterizedTest(name = "classic injection ''{0}'' must be rejected with 403")
    @ValueSource(strings = {
            // Stacked query — caught by inject-stacked-query regex pre-fix
            "id; SELECT 1--", "name; DROP TABLE m_office--",
            // Timing with AND prefix — caught by inject-timing regex pre-fix
            "id AND SLEEP(5)--",
            // CASE-based blind — caught by inject-blind regex pre-fix
            "CASE WHEN 1=1 THEN id ELSE name END",
            // UNION
            "id UNION SELECT 1,2,3,4,5--",
            // Comment truncation
            "id--", "id/*comment*/", })
    @Order(7)
    void retrieveAllOffices_classicInjectionPayloads_areRejected(String payload) throws Exception {
        var response = fineractClient().offices.retrieveAllOffices(false, payload, null).execute();
        assertThat(response.isSuccessful()).as("Injection payload [%s] must be rejected", payload).isFalse();
        assertThat(response.code()).isEqualTo(403);
    }

    /**
     * Non-whitelisted column names that are not injection payloads — internal schema columns, table-qualified forms,
     * numeric literals, and a percent-encoded subquery (passed as a literal string through retrofit; the server
     * receives the raw percent signs, which do not match any whitelisted column name and are therefore rejected).
     *
     * <p>
     * Note: blank / whitespace-only values are tested separately in
     * {@link #retrieveAllOffices_withBlankOrderBy_isAcceptedAsAbsent} because the framework treats them as absent
     * rather than as invalid input.
     */
    @ParameterizedTest(name = "non-whitelisted column ''{0}'' must be rejected with 403")
    @ValueSource(strings = {
            // Internal schema columns not present in the API response
            "parent_id", "created_date", "lastmodified_date", "createdby_id", "lastmodifiedby_id",
            // Table-qualified forms — rejected even for whitelisted column names
            "m_office.id", "o.name",
            // Numeric literals
            "1", "0",
            // Percent-encoded subquery passed as a literal string value;
            // retrofit re-encodes it, server receives the literal percent signs
            // → no whitelist match → 403
            "%28SELECT%20SLEEP%283%29%29", })
    @Order(8)
    void retrieveAllOffices_nonWhitelistedColumns_areRejected(String payload) throws Exception {
        var response = fineractClient().offices.retrieveAllOffices(false, payload, null).execute();
        assertThat(response.isSuccessful()).as("Non-whitelisted column [%s] must be rejected by the whitelist", payload).isFalse();
        assertThat(response.code()).isEqualTo(403);
    }
}
