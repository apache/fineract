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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.PostOfficesResponse;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.useradministration.roles.RolesHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the EntityToEntityMapping office-scoping security fix.
 *
 * When fromId=0 (meaning "all offices") is passed to the GET /entitytoentitymapping/{mapId}/{fromId}/{toId} endpoint,
 * results must be scoped to the authenticated user's office hierarchy rather than returning every office's mappings.
 */
public class EntityToEntityMappingIntegrationTest {

    private static final String ENTITY_MAPPING_BASE_URL = "/fineract-provider/api/v1/entitytoentitymapping";
    private static final String CREATE_USER_URL = "/fineract-provider/api/v1/users?" + Utils.TENANT_IDENTIFIER;
    private static final String OFFICE_ACCESS_TO_LOAN_PRODUCTS = "office_access_to_loan_products";

    private RequestSpecification adminRequestSpec;
    private ResponseSpecification responseSpec;

    @BeforeEach
    public void setUp() {
        Utils.initializeRESTAssured();
        adminRequestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        adminRequestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testOfficeUserSeesOnlyOwnOfficeMappingsWhenFromIdIsZero() {
        OfficeHelper officeHelper = new OfficeHelper();
        PostOfficesResponse officeA = officeHelper.createOffice(LocalDate.of(2024, 1, 1));
        PostOfficesResponse officeB = officeHelper.createOffice(LocalDate.of(2024, 1, 1));
        assertNotNull(officeA.getOfficeId());
        assertNotNull(officeB.getOfficeId());

        LoanTransactionHelper loanHelper = new LoanTransactionHelper(adminRequestSpec, responseSpec);
        Integer loanProductId = loanHelper.getLoanProductId(new LoanProductTestBuilder().build());
        assertNotNull(loanProductId);

        Long relId = getOfficeToLoanProductRelationId();
        assertNotNull(relId, "office_access_to_loan_products relation must exist in the database");

        createEntityMapping(relId, officeA.getOfficeId(), loanProductId.longValue());
        createEntityMapping(relId, officeB.getOfficeId(), loanProductId.longValue());

        Integer roleId = RolesHelper.createRole(adminRequestSpec, responseSpec);
        grantReadOnlyPermission(roleId);
        String username = Utils.uniqueRandomStringGenerator("OfficeAUser", 6);
        // Must satisfy the "strong" password policy: 12-50 chars, upper/lower/digit/special, no consecutive repeats.
        String password = "Test@1234#XY";
        createUserAtOffice(username, password, roleId, officeA.getOfficeId());

        RequestSpecification officeARequestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        officeARequestSpec.header("Authorization",
                "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey(username, password));

        // fromId=0 means "all offices" — the fix scopes this to the user's office hierarchy
        String url = ENTITY_MAPPING_BASE_URL + "/" + relId + "/0/0?" + Utils.TENANT_IDENTIFIER;
        List<Map<String, Object>> mappings = Utils.performServerGet(officeARequestSpec, responseSpec, url, "$");

        assertNotNull(mappings);
        long officeAMappingCount = mappings.stream().filter(m -> officeA.getOfficeId().equals(toLong(m.get("fromId")))).count();
        long officeBMappingCount = mappings.stream().filter(m -> officeB.getOfficeId().equals(toLong(m.get("fromId")))).count();
        assertEquals(1, officeAMappingCount, "Office A user should see Office A's mapping");
        assertEquals(0, officeBMappingCount, "Office A user must NOT see Office B's mapping");
    }

    @Test
    public void testHeadOfficeUserSeesAllDescendantOfficeMappingsWhenFromIdIsZero() {
        OfficeHelper officeHelper = new OfficeHelper();
        PostOfficesResponse officeA = officeHelper.createOffice(LocalDate.of(2024, 1, 1));
        PostOfficesResponse officeB = officeHelper.createOffice(LocalDate.of(2024, 1, 1));

        LoanTransactionHelper loanHelper = new LoanTransactionHelper(adminRequestSpec, responseSpec);
        Integer loanProductId = loanHelper.getLoanProductId(new LoanProductTestBuilder().build());

        Long relId = getOfficeToLoanProductRelationId();
        assertNotNull(relId, "office_access_to_loan_products relation must exist in the database");

        createEntityMapping(relId, officeA.getOfficeId(), loanProductId.longValue());
        createEntityMapping(relId, officeB.getOfficeId(), loanProductId.longValue());

        // The superadmin belongs to Head Office, which is the parent of both Office A and B
        String url = ENTITY_MAPPING_BASE_URL + "/" + relId + "/0/0?" + Utils.TENANT_IDENTIFIER;
        List<Map<String, Object>> mappings = Utils.performServerGet(adminRequestSpec, responseSpec, url, "$");

        assertNotNull(mappings);
        long officeAMappingCount = mappings.stream().filter(m -> officeA.getOfficeId().equals(toLong(m.get("fromId")))).count();
        long officeBMappingCount = mappings.stream().filter(m -> officeB.getOfficeId().equals(toLong(m.get("fromId")))).count();
        assertEquals(1, officeAMappingCount, "Head office user should see Office A's mapping");
        assertEquals(1, officeBMappingCount, "Head office user should see Office B's mapping");
    }

    private Long getOfficeToLoanProductRelationId() {
        List<Map<String, Object>> mappingTypes = Utils.performServerGet(adminRequestSpec, responseSpec,
                ENTITY_MAPPING_BASE_URL + "?" + Utils.TENANT_IDENTIFIER, "$");
        if (mappingTypes == null) {
            return null;
        }
        return mappingTypes.stream().filter(t -> OFFICE_ACCESS_TO_LOAN_PRODUCTS.equals(t.get("mappingTypes"))).map(t -> toLong(t.get("id")))
                .findFirst().orElse(null);
    }

    private void createEntityMapping(Long relId, Long fromId, Long toId) {
        String body = "{\"fromId\":" + fromId + ",\"toId\":" + toId
                + ",\"startDate\":\"01 January 2024\",\"locale\":\"en\",\"dateFormat\":\"dd MMMM yyyy\"}";
        Utils.performServerPost(adminRequestSpec, responseSpec, ENTITY_MAPPING_BASE_URL + "/" + relId + "?" + Utils.TENANT_IDENTIFIER, body,
                "resourceId");
    }

    private void grantReadOnlyPermission(Integer roleId) {
        // READ_FINERACTENTITY has no corresponding seeded m_permission row, so it can never be granted to a role.
        // ALL_FUNCTIONS_READ is a real, seeded, read-only permission that satisfies validateHasReadPermission for
        // any resource, letting us exercise office-scoping with a non-superuser account.
        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put("ALL_FUNCTIONS_READ", true);
        RolesHelper.addPermissionsToRole(adminRequestSpec, responseSpec, roleId, permissions);
    }

    private void createUserAtOffice(String username, String password, Integer roleId, Long officeId) {
        String body = "{\"username\":\"" + username + "\",\"firstname\":\"Test\",\"lastname\":\"User\","
                + "\"email\":\"test@mifos.org\",\"officeId\":" + officeId + ",\"roles\":[" + roleId
                + "],\"sendPasswordToEmail\":false,\"password\":\"" + password + "\",\"repeatPassword\":\"" + password + "\"}";
        Utils.performServerPost(adminRequestSpec, responseSpec, CREATE_USER_URL, body, "resourceId");
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).longValue();
    }
}
