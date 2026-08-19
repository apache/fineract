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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * Office-based mappings returned by GET /entitytoentitymapping/{mapId}/{fromId}/{toId} must be scoped to the
 * authenticated user's office hierarchy, both for fromId=0 ("all offices") and for an explicit office outside it.
 */
public class EntityToEntityMappingIntegrationTest extends FeignLoanTestBase {

    private static final String OFFICE_ACCESS_TO_LOAN_PRODUCTS = "office_access_to_loan_products";
    private static final String PASSWORD = "Test@1234#XY";

    @Test
    public void testOfficeUserSeesOnlyOwnOfficeMappings() {
        FeignOfficeHelper officeHelper = new FeignOfficeHelper(fineractClient());
        Long officeA = officeHelper.createOffice(LocalDate.of(2024, 1, 1)).getOfficeId();
        Long officeB = officeHelper.createOffice(LocalDate.of(2024, 1, 1)).getOfficeId();
        Long loanProductId = createLoanProduct(onePeriod30DaysNoInterest());
        Long relId = getOfficeToLoanProductRelationId();

        createEntityMapping(relId, officeA, loanProductId);
        createEntityMapping(relId, officeB, loanProductId);

        FineractFeignClient officeAClient = createReadOnlyUserClient(officeA);

        List<Long> allOffices = fromIdsOf(officeAClient.fineractEntity().getEntityToEntityMappings(relId, 0L, loanProductId));
        assertEquals(List.of(officeA), allOffices, "Office A user must only see Office A's mapping");

        List<Long> officeBOnly = fromIdsOf(officeAClient.fineractEntity().getEntityToEntityMappings(relId, officeB, loanProductId));
        assertEquals(List.of(), officeBOnly, "Office A user must not see Office B's mapping when asking for Office B directly");
    }

    @Test
    public void testHeadOfficeUserSeesAllDescendantOfficeMappings() {
        FeignOfficeHelper officeHelper = new FeignOfficeHelper(fineractClient());
        Long officeA = officeHelper.createOffice(LocalDate.of(2024, 1, 1)).getOfficeId();
        Long officeB = officeHelper.createOffice(LocalDate.of(2024, 1, 1)).getOfficeId();
        Long loanProductId = createLoanProduct(onePeriod30DaysNoInterest());
        Long relId = getOfficeToLoanProductRelationId();

        createEntityMapping(relId, officeA, loanProductId);
        createEntityMapping(relId, officeB, loanProductId);

        List<Long> mappings = fromIdsOf(fineractClient().fineractEntity().getEntityToEntityMappings(relId, 0L, loanProductId));
        assertEquals(List.of(officeA, officeB), mappings.stream().sorted().toList(), "Head office user must see both mappings");
    }

    private Long getOfficeToLoanProductRelationId() {
        JsonNode mappingTypes = readTree(ok(() -> fineractClient().fineractEntity().retrieveAll3()));
        Long relId = StreamSupport.stream(mappingTypes.spliterator(), false)
                .filter(type -> OFFICE_ACCESS_TO_LOAN_PRODUCTS.equals(type.path("mappingTypes").asText()))
                .map(type -> type.get("id").asLong()).findFirst().orElse(null);
        assertNotNull(relId, "office_access_to_loan_products relation must exist");
        return relId;
    }

    private void createEntityMapping(Long relId, Long fromId, Long toId) {
        String body = "{\"fromId\":" + fromId + ",\"toId\":" + toId
                + ",\"startDate\":\"01 January 2024\",\"locale\":\"en\",\"dateFormat\":\"dd MMMM yyyy\"}";
        ok(() -> fineractClient().fineractEntity().createMap(relId, body));
    }

    private FineractFeignClient createReadOnlyUserClient(Long officeId) {
        Long roleId = FeignRoleHelper.createRole();
        FeignRoleHelper.addPermissionsToRole(roleId, Map.of("ALL_FUNCTIONS_READ", true));
        String username = Utils.uniqueRandomStringGenerator("OfficeUser", 6);
        FeignUserHelper.createUser(new PostUsersRequest().username(username).firstname("Test").lastname("User").email("test@localhost")
                .officeId(officeId).roles(List.of(roleId)).password(PASSWORD).repeatPassword(PASSWORD).sendPasswordToEmail(false));
        return FineractFeignClientHelper.createNewFineractFeignClient(username, PASSWORD);
    }

    private List<Long> fromIdsOf(String json) {
        return StreamSupport.stream(readTree(json).spliterator(), false).map(mapping -> mapping.get("fromId").asLong()).toList();
    }

    private static JsonNode readTree(String json) {
        try {
            return ObjectMapperFactory.getShared().readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse entity mapping response", e);
        }
    }
}
