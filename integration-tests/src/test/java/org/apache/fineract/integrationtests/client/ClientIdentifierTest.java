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
import org.apache.fineract.client.models.ClientIdentifierRequest;
import org.apache.fineract.client.models.GetClientsClientIdIdentifiersResponse;
import org.apache.fineract.client.models.PostClientsClientIdIdentifiersRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PutClientsClientIdIdentifiersIdentifierIdResponse;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ClientIdentifierTest extends FeignIntegrationTest {

    private static final Long DOCUMENT_TYPE_ID = 1L;
    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String LOCALE = "en";

    private FeignClientHelper clientHelper;

    @BeforeAll
    public void setup() {
        clientHelper = new FeignClientHelper(fineractClient());
    }

    @Test
    public void testClientIdentifierIssuanceAndExpiryDatesCrudFlow() {
        PostClientsResponse client = clientHelper.createClient(ClientRequestBuilders.defaultClient());

        String documentKey = Utils.randomStringGenerator("ID_DATES_", 10);
        PostClientsClientIdIdentifiersRequest createRequest = new PostClientsClientIdIdentifiersRequest().documentTypeId(DOCUMENT_TYPE_ID)
                .documentKey(documentKey).description(Utils.randomStringGenerator("Identifier Description ", 10)).status("Active")
                .issuanceDate("01 January 2024").expiryDate("01 January 2034").dateFormat(DATE_FORMAT).locale(LOCALE);
        Long identifierId = clientHelper.createClientIdentifier(client.getClientId(), createRequest).getResourceId();

        GetClientsClientIdIdentifiersResponse createdIdentifier = clientHelper.getClientIdentifier(client.getClientId(), identifierId);
        assertThat(createdIdentifier.getId()).isEqualTo(identifierId);
        assertThat(createdIdentifier.getClientId()).isEqualTo(client.getClientId());
        assertThat(createdIdentifier.getDocumentKey()).isEqualTo(documentKey);
        assertThat(createdIdentifier.getDescription()).isEqualTo(createRequest.getDescription());
        assertThat(createdIdentifier.getIssuanceDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(createdIdentifier.getExpiryDate()).isEqualTo(LocalDate.of(2034, 1, 1));

        ClientIdentifierRequest updateRequest = new ClientIdentifierRequest().documentTypeId(DOCUMENT_TYPE_ID).documentKey(documentKey)
                .description(Utils.randomStringGenerator("Identifier Description ", 10)).issuanceDate("01 February 2024")
                .expiryDate("01 February 2034").dateFormat(DATE_FORMAT).locale(LOCALE);
        PutClientsClientIdIdentifiersIdentifierIdResponse updateResponse = clientHelper.updateClientIdentifier(client.getClientId(),
                identifierId, updateRequest);
        assertThat(updateResponse.getResourceId()).isEqualTo(identifierId);
        assertThat(updateResponse.getChanges().getIssuanceDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(updateResponse.getChanges().getExpiryDate()).isEqualTo(LocalDate.of(2034, 2, 1));

        GetClientsClientIdIdentifiersResponse updatedIdentifier = clientHelper.getClientIdentifier(client.getClientId(), identifierId);
        assertThat(updatedIdentifier.getDocumentKey()).isEqualTo(documentKey);
        assertThat(updatedIdentifier.getDescription()).isEqualTo(updateRequest.getDescription());
        assertThat(updatedIdentifier.getIssuanceDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(updatedIdentifier.getExpiryDate()).isEqualTo(LocalDate.of(2034, 2, 1));

        PutClientsClientIdIdentifiersIdentifierIdResponse clearDatesResponse = clientHelper.clearClientIdentifierDates(client.getClientId(),
                identifierId);
        assertThat(clearDatesResponse.getResourceId()).isEqualTo(identifierId);

        GetClientsClientIdIdentifiersResponse clearedIdentifier = clientHelper.getClientIdentifier(client.getClientId(), identifierId);
        assertThat(clearedIdentifier.getDocumentKey()).isEqualTo(documentKey);
        assertThat(clearedIdentifier.getIssuanceDate()).isNull();
        assertThat(clearedIdentifier.getExpiryDate()).isNull();
    }

    @Test
    public void testClientIdentifierWithoutIssuanceAndExpiryDatesRemainsValid() {
        PostClientsResponse client = clientHelper.createClient(ClientRequestBuilders.defaultClient());

        String documentKey = Utils.randomStringGenerator("ID_NO_DATES_", 10);
        PostClientsClientIdIdentifiersRequest createRequest = new PostClientsClientIdIdentifiersRequest().documentTypeId(DOCUMENT_TYPE_ID)
                .documentKey(documentKey).description("Document without date fields").status("Active");

        Long identifierId = clientHelper.createClientIdentifier(client.getClientId(), createRequest).getResourceId();

        GetClientsClientIdIdentifiersResponse identifier = clientHelper.getClientIdentifier(client.getClientId(), identifierId);
        assertThat(identifier.getDocumentKey()).isEqualTo(documentKey);
        assertThat(identifier.getDescription()).isEqualTo(createRequest.getDescription());
        assertThat(identifier.getIssuanceDate()).isNull();
        assertThat(identifier.getExpiryDate()).isNull();
    }
}
