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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.apache.fineract.client.feign.services.ClientFamilyMemberApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.FamilyMemberCreateRequest;
import org.apache.fineract.client.models.FamilyMemberUpdateRequest;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@code /clients/{clientId}/familymembers} (FINERACT-2671).
 */
public class ClientFamilyMemberTest extends FeignIntegrationTest {

    private final ClientFamilyMemberApi familyMemberApi = fineractClient().clientFamilyMember();

    @Test
    public void retrieveFamilyMemberReturns404WhenNotExisting() {
        Long clientId = createClient();

        CallFailedRuntimeException e = fail(() -> familyMemberApi.retrieveOneClientFamilyMember(999999L, clientId));

        assertEquals(404, e.getStatus());
        assertTrue(e.getMessage().contains("error.msg.family.member.id.invalid"));
    }

    @Test
    public void retrieveFamilyMemberReturns404ForAnotherClientsMember() {
        Long ownerClientId = createClient();
        Long otherClientId = createClient();
        Long familyMemberId = createFamilyMember(ownerClientId, "Ada", "Lovelace");

        // sanity: reachable through the owning client
        assertEquals(familyMemberId, ok(() -> familyMemberApi.retrieveOneClientFamilyMember(familyMemberId, ownerClientId)).getId());

        CallFailedRuntimeException e = fail(() -> familyMemberApi.retrieveOneClientFamilyMember(familyMemberId, otherClientId));

        assertEquals(404, e.getStatus());
    }

    @Test
    public void createFamilyMemberAcceptsFormattedDateOfBirth() {
        Long clientId = createClient();
        Long relationshipId = createRelationship();

        Long familyMemberId = ok(() -> familyMemberApi.createClientFamilyMember(clientId, new FamilyMemberCreateRequest().firstName("Grace")
                .lastName("Hopper").relationshipId(relationshipId).dateOfBirth("01 January 2000").dateFormat("dd MMMM yyyy").locale("en")))
                .getResourceId();

        assertEquals(LocalDate.of(2000, 1, 1),
                ok(() -> familyMemberApi.retrieveOneClientFamilyMember(familyMemberId, clientId)).getDateOfBirth());
    }

    @Test
    public void createFamilyMemberRejectsBlankFirstName() {
        Long clientId = createClient();
        Long relationshipId = createRelationship();

        CallFailedRuntimeException e = fail(() -> familyMemberApi.createClientFamilyMember(clientId,
                new FamilyMemberCreateRequest().firstName(" ").lastName("Hopper").relationshipId(relationshipId)));

        assertEquals(400, e.getStatus());
        assertTrue(e.getMessage().contains("firstName"));
    }

    @Test
    public void updateFamilyMemberAcceptsFormattedDateOfBirthAndReportsChanges() {
        Long clientId = createClient();
        Long familyMemberId = createFamilyMember(clientId, "Ada", "Lovelace");

        var response = ok(() -> familyMemberApi.updateClientFamilyMember(familyMemberId, clientId, new FamilyMemberUpdateRequest()
                .firstName("Augusta").dateOfBirth("01 January 2000").dateFormat("dd MMMM yyyy").locale("en")));

        assertEquals("Augusta", response.getChanges().get("firstName"));
        var updated = ok(() -> familyMemberApi.retrieveOneClientFamilyMember(familyMemberId, clientId));
        assertEquals("Augusta", updated.getFirstName());
        assertEquals(LocalDate.of(2000, 1, 1), updated.getDateOfBirth());
    }

    @Test
    public void updateFamilyMemberAcceptsZeroCodeValueIdsAndZeroAge() {
        Long clientId = createClient();
        Long familyMemberId = createFamilyMember(clientId, "Ada", "Lovelace");
        Long relationshipId = createRelationship();

        // payload shape sent by existing clients: 0 for the optional code values means "not selected"
        var response = ok(() -> familyMemberApi.updateClientFamilyMember(familyMemberId, clientId,
                new FamilyMemberUpdateRequest().relationshipId(relationshipId).maritalStatusId(0L).genderId(0L).professionId(0L).age(0L)));

        assertEquals(relationshipId, ((Number) response.getChanges().get("relationshipId")).longValue());
        var updated = ok(() -> familyMemberApi.retrieveOneClientFamilyMember(familyMemberId, clientId));
        assertEquals(relationshipId, updated.getRelationshipId());
        assertEquals(0L, updated.getAge());
    }

    @Test
    public void updateFamilyMemberReturns404ForAnotherClientsMember() {
        Long ownerClientId = createClient();
        Long otherClientId = createClient();
        Long familyMemberId = createFamilyMember(ownerClientId, "Ada", "Lovelace");

        CallFailedRuntimeException e = fail(() -> familyMemberApi.updateClientFamilyMember(familyMemberId, otherClientId,
                new FamilyMemberUpdateRequest().firstName("Someone Else")));

        assertEquals(404, e.getStatus());
    }

    @Test
    public void deleteFamilyMemberRemovesIt() {
        Long clientId = createClient();
        Long familyMemberId = createFamilyMember(clientId, "Ada", "Lovelace");

        ok(() -> familyMemberApi.deleteClientFamilyMember(familyMemberId, clientId));

        CallFailedRuntimeException e = fail(() -> familyMemberApi.retrieveOneClientFamilyMember(familyMemberId, clientId));
        assertEquals(404, e.getStatus());
    }

    @Test
    public void deleteFamilyMemberReturns404ForAnotherClientsMember() {
        Long ownerClientId = createClient();
        Long otherClientId = createClient();
        Long familyMemberId = createFamilyMember(ownerClientId, "Ada", "Lovelace");

        CallFailedRuntimeException e = fail(() -> familyMemberApi.deleteClientFamilyMember(familyMemberId, otherClientId));
        assertEquals(404, e.getStatus());

        // sanity: still there for the owning client
        assertEquals(familyMemberId, ok(() -> familyMemberApi.retrieveOneClientFamilyMember(familyMemberId, ownerClientId)).getId());
    }

    private Long createFamilyMember(Long clientId, String firstName, String lastName) {
        Long relationshipId = createRelationship();
        return ok(() -> familyMemberApi.createClientFamilyMember(clientId,
                new FamilyMemberCreateRequest().firstName(firstName).lastName(lastName).relationshipId(relationshipId))).getResourceId();
    }

    // m_family_members.relationship_cv_id is NOT NULL, so a RELATIONSHIP code value is required;
    // the code value id is returned as subResourceId (resourceId is the parent code's id)
    private Long createRelationship() {
        return ok(() -> fineractClient().codeValues().createCodeValueByCodeName("RELATIONSHIP",
                new PostCodeValuesDataRequest().name(Utils.randomStringGenerator("Relative_", 4)).position(1).isActive(true)))
                .getSubResourceId();
    }

    private Long createClient() {
        return ok(
                () -> fineractClient().clients()
                        .createClient(new PostClientsRequest().legalFormId(1L).officeId(1L)
                                .fullname(Utils.randomStringGenerator("TestClient", 6)).dateFormat(Utils.DATE_FORMAT).locale("en_US")))
                .getClientId();
    }
}
