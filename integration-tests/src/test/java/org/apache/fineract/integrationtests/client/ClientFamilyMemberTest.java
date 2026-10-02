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

import org.apache.fineract.client.feign.services.ClientFamilyMemberApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ClientFamilyMemberRequest;
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
        // m_family_members.relationship_cv_id is NOT NULL, so a RELATIONSHIP code value is required;
        // the code value id is returned as subResourceId (resourceId is the parent code's id)
        Long relationshipId = ok(() -> fineractClient().codeValues().createCodeValueByCodeName("RELATIONSHIP",
                new PostCodeValuesDataRequest().name(Utils.randomStringGenerator("Relative_", 4)).position(1).isActive(true)))
                .getSubResourceId();
        Long familyMemberId = ok(() -> familyMemberApi.createClientFamilyMember(ownerClientId,
                new ClientFamilyMemberRequest().firstName("Ada").lastName("Lovelace").relationshipId(relationshipId))).getResourceId();

        // sanity: reachable through the owning client
        assertEquals(familyMemberId, ok(() -> familyMemberApi.retrieveOneClientFamilyMember(familyMemberId, ownerClientId)).getId());

        CallFailedRuntimeException e = fail(() -> familyMemberApi.retrieveOneClientFamilyMember(familyMemberId, otherClientId));

        assertEquals(404, e.getStatus());
    }

    private Long createClient() {
        return ok(
                () -> fineractClient().clients()
                        .createClient(new PostClientsRequest().legalFormId(1L).officeId(1L)
                                .fullname(Utils.randomStringGenerator("TestClient", 6)).dateFormat(Utils.DATE_FORMAT).locale("en_US")))
                .getClientId();
    }
}
