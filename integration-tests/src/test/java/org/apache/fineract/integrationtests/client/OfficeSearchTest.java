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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.util.Calls;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

/**
 * Covers orderBy / sortOrder input validation on GET /api/v1/offices (OfficesApiResource#retrieveOffices), the endpoint
 * targeted by FINERACT-2662. The orderBy parameter is concatenated into an ORDER BY clause, so it must be restricted to
 * a strict allowlist of known column names and sortOrder to ASC/DESC.
 *
 * retrieveOffices param order (from OfficesApi.java): includeAllOffices, orderBy, sortOrder
 */
public class OfficeSearchTest extends IntegrationTest {

    private Response<List<GetOfficesResponse>> callRetrieveOffices(String orderBy, String sortOrder) {
        return Calls.executeU(fineractClient().offices.retrieveOffices(null, orderBy, sortOrder));
    }

    @Test
    public void testOfficeSearchOrderByRejectsSqlInjectionPoc() {
        // given - the same style of blind boolean-based payload reported against the search endpoints
        String maliciousOrderBy = "o.id, (CASE WHEN (ASCII(SUBSTRING((SELECT table_name FROM "
                + "information_schema.tables WHERE table_schema REGEXP database() LIMIT 0,1),1,1)) - 109) "
                + "THEN o.id ELSE (o.id*-1) END)";
        // when
        Response<List<GetOfficesResponse>> response = callRetrieveOffices(maliciousOrderBy, null);
        // then
        assertThat(response.isSuccessful()).isFalse();
    }

    @Test
    public void testOfficeSearchOrderByRejectsSubstringBypassAttempt() {
        // given - "name" appears as a substring; confirms regex anchors hold
        // when
        Response<List<GetOfficesResponse>> response = callRetrieveOffices("name, (CASE WHEN (1=1) THEN 1 END)", null);
        // then
        assertThat(response.isSuccessful()).isFalse();
    }

    @Test
    public void testOfficeSearchOrderByRejectsCaseMismatch() {
        // given - documented value is "name", not "Name" or "NAME"
        // when
        Response<List<GetOfficesResponse>> response1 = callRetrieveOffices("Name", null);
        Response<List<GetOfficesResponse>> response2 = callRetrieveOffices("NAME", null);
        // then
        assertThat(response1.isSuccessful()).isFalse();
        assertThat(response2.isSuccessful()).isFalse();
    }

    @Test
    public void testOfficeSearchOrderByRejectsSnakeCaseColumnName() {
        // given - undocumented internal SQL column form should no longer be accepted directly
        // when
        Response<List<GetOfficesResponse>> response = callRetrieveOffices("o.opening_date", null);
        // then
        assertThat(response.isSuccessful()).isFalse();
    }

    @Test
    public void testOfficeSearchOrderByRejectsCommaSeparatedList() {
        // given - multi-column orderBy is out of scope for this allowlist
        // when
        Response<List<GetOfficesResponse>> response = callRetrieveOffices("name,hierarchy", null);
        // then
        assertThat(response.isSuccessful()).isFalse();
    }

    @Test
    public void testOfficeSearchOrderByAllowsEmpty() {
        // given - blank orderBy falls through to the default ordering
        // when
        Response<List<GetOfficesResponse>> response = callRetrieveOffices("   ", null);
        // then
        assertThat(response.isSuccessful()).isTrue();
    }

    @Test
    public void testOfficeSearchOrderByRejectsSqlKeyword() {
        // when
        Response<List<GetOfficesResponse>> response = callRetrieveOffices("id; DROP TABLE m_office", null);
        // then
        assertThat(response.isSuccessful()).isFalse();
    }

    @Test
    public void testOfficeSearchSortOrderRejectsArbitraryValue() {
        // given - direction value outside ASC/DESC should be rejected
        // when
        Response<List<GetOfficesResponse>> response = callRetrieveOffices("name", "RANDOM");
        // then
        assertThat(response.isSuccessful()).isFalse();
    }

    @Test
    public void testOfficeSearchSortOrderRejectsInjectionAttempt() {
        // when
        Response<List<GetOfficesResponse>> response = callRetrieveOffices("name", "ASC; DROP TABLE m_office--");
        // then
        assertThat(response.isSuccessful()).isFalse();
    }

    @Test
    public void testOfficeSearchOrderByAcceptsAllDocumentedValues() {
        // given - the allowlisted columns exposed by the office search response
        for (String validOrderBy : new String[] { "id", "name", "nameDecorated", "externalId", "openingDate", "hierarchy", "parentId",
                "parentName" }) {
            // when
            Response<List<GetOfficesResponse>> response = callRetrieveOffices(validOrderBy, "ASC");
            // then
            assertThat(response.isSuccessful()).isTrue();
        }
    }
}
