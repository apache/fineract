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
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ClientIdentifierCreateRequest;
import org.apache.fineract.client.models.ClientIdentifierUpdateChanges;
import org.apache.fineract.client.models.ClientIdentifierUpdateRequest;
import org.apache.fineract.client.models.ClientIdentifierUpdateResponse;
import org.apache.fineract.client.models.GetClientsClientIdIdentifiersResponse;
import org.apache.fineract.client.models.PostClientsResponse;
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
        ClientIdentifierCreateRequest createRequest = new ClientIdentifierCreateRequest().documentTypeId(DOCUMENT_TYPE_ID)
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

        ClientIdentifierUpdateRequest updateRequest = new ClientIdentifierUpdateRequest().documentTypeId(DOCUMENT_TYPE_ID)
                .documentKey(documentKey).description(Utils.randomStringGenerator("Identifier Description ", 10))
                .issuanceDate("01 February 2024").expiryDate("01 February 2034").dateFormat(DATE_FORMAT).locale(LOCALE);
        ClientIdentifierUpdateResponse updateResponse = clientHelper.updateClientIdentifier(client.getClientId(), identifierId,
                updateRequest);
        assertThat(updateResponse.getResourceId()).isEqualTo(identifierId);
        assertThat(updateResponse.getChanges().getIssuanceDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(updateResponse.getChanges().getExpiryDate()).isEqualTo(LocalDate.of(2034, 2, 1));

        GetClientsClientIdIdentifiersResponse updatedIdentifier = clientHelper.getClientIdentifier(client.getClientId(), identifierId);
        assertThat(updatedIdentifier.getDocumentKey()).isEqualTo(documentKey);
        assertThat(updatedIdentifier.getDescription()).isEqualTo(updateRequest.getDescription());
        assertThat(updatedIdentifier.getIssuanceDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(updatedIdentifier.getExpiryDate()).isEqualTo(LocalDate.of(2034, 2, 1));

        ClientIdentifierUpdateResponse clearDatesResponse = clientHelper.clearClientIdentifierDates(client.getClientId(), identifierId);
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
        ClientIdentifierCreateRequest createRequest = new ClientIdentifierCreateRequest().documentTypeId(DOCUMENT_TYPE_ID)
                .documentKey(documentKey).description("Document without date fields").status("Active");

        Long identifierId = clientHelper.createClientIdentifier(client.getClientId(), createRequest).getResourceId();

        GetClientsClientIdIdentifiersResponse identifier = clientHelper.getClientIdentifier(client.getClientId(), identifierId);
        assertThat(identifier.getDocumentKey()).isEqualTo(documentKey);
        assertThat(identifier.getDescription()).isEqualTo(createRequest.getDescription());
        assertThat(identifier.getIssuanceDate()).isNull();
        assertThat(identifier.getExpiryDate()).isNull();
    }

    @Test
    public void testClientIdentifierDatesKeptWhenNotSentAndClearedWithEmptyStrings() {
        PostClientsResponse client = clientHelper.createClient(ClientRequestBuilders.defaultClient());
        String documentKey = Utils.randomStringGenerator("ID_KEEP_DATES_", 10);
        ClientIdentifierCreateRequest createRequest = new ClientIdentifierCreateRequest().documentTypeId(DOCUMENT_TYPE_ID)
                .documentKey(documentKey).status("Active").issuanceDate("01 January 2024").expiryDate("01 January 2034")
                .dateFormat(DATE_FORMAT).locale(LOCALE);
        Long identifierId = clientHelper.createClientIdentifier(client.getClientId(), createRequest).getResourceId();

        // documentKey is unchanged, just along for the ride: a description-only update has no "real" field and is
        // rejected with 400, same as the legacy path (see testUpdateClientIdentifierWithNoRealFieldReturns400).
        clientHelper.updateClientIdentifier(client.getClientId(), identifierId, new ClientIdentifierUpdateRequest().documentKey(documentKey)
                .description(Utils.randomStringGenerator("Identifier Description ", 10)));

        GetClientsClientIdIdentifiersResponse keptIdentifier = clientHelper.getClientIdentifier(client.getClientId(), identifierId);
        assertThat(keptIdentifier.getIssuanceDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(keptIdentifier.getExpiryDate()).isEqualTo(LocalDate.of(2034, 1, 1));

        clientHelper.clearClientIdentifierDatesWithEmptyStrings(client.getClientId(), identifierId);

        GetClientsClientIdIdentifiersResponse clearedIdentifier = clientHelper.getClientIdentifier(client.getClientId(), identifierId);
        assertThat(clearedIdentifier.getIssuanceDate()).isNull();
        assertThat(clearedIdentifier.getExpiryDate()).isNull();
    }

    @Test
    public void testUpdateClientIdentifierChangesFields() {
        PostClientsResponse client = clientHelper.createClient(ClientRequestBuilders.defaultClient());
        Long identifierId = createIdentifier(client.getClientId());

        String newDocumentKey = Utils.randomStringGenerator("ID_UPDATED_", 10);
        ClientIdentifierUpdateResponse response = clientHelper.updateClientIdentifier(client.getClientId(), identifierId,
                new ClientIdentifierUpdateRequest().documentKey(newDocumentKey));

        assertThat(response.getResourceId()).isEqualTo(identifierId);
        assertThat(response.getChanges().getDocumentKey()).isEqualTo(newDocumentKey);
        assertThat(clientHelper.getClientIdentifier(client.getClientId(), identifierId).getDocumentKey()).isEqualTo(newDocumentKey);
    }

    @Test
    public void testUpdateClientIdentifierReturns404ForAnotherClientsIdentifier() {
        PostClientsResponse ownerClient = clientHelper.createClient(ClientRequestBuilders.defaultClient());
        PostClientsResponse otherClient = clientHelper.createClient(ClientRequestBuilders.defaultClient());
        Long identifierId = createIdentifier(ownerClient.getClientId());

        CallFailedRuntimeException e = clientHelper.updateClientIdentifierExpectingError(otherClient.getClientId(), identifierId,
                new ClientIdentifierUpdateRequest().documentKey(Utils.randomStringGenerator("ID_", 10)));

        assertThat(e.getStatus()).isEqualTo(404);
    }

    @Test
    public void testUpdateClientIdentifierWithNoRealFieldReturns400() {
        PostClientsResponse client = clientHelper.createClient(ClientRequestBuilders.defaultClient());
        Long identifierId = createIdentifier(client.getClientId());

        // status/description alone aren't "real" fields for an update, same as the legacy path.
        CallFailedRuntimeException e = clientHelper.updateClientIdentifierExpectingError(client.getClientId(), identifierId,
                new ClientIdentifierUpdateRequest().description(Utils.randomStringGenerator("Identifier Description ", 10))
                        .status("Inactive"));

        assertThat(e.getStatus()).isEqualTo(400);
    }

    @Test
    public void testUpdateClientIdentifierWithNonPositiveDocumentTypeIdReturns404() {
        PostClientsResponse client = clientHelper.createClient(ClientRequestBuilders.defaultClient());
        Long identifierId = createIdentifier(client.getClientId());

        CallFailedRuntimeException e = clientHelper.updateClientIdentifierExpectingError(client.getClientId(), identifierId,
                new ClientIdentifierUpdateRequest().documentTypeId(-1L));

        assertThat(e.getStatus()).isEqualTo(404);
    }

    @Test
    public void testClientIdentifierResponsesIncludeOfficeId() {
        PostClientsResponse client = clientHelper.createClient(ClientRequestBuilders.defaultClient());
        ClientIdentifierCreateRequest createRequest = new ClientIdentifierCreateRequest().documentTypeId(DOCUMENT_TYPE_ID)
                .documentKey(Utils.randomStringGenerator("ID_OFFICE_", 10)).status("Active");

        var createResponse = clientHelper.createClientIdentifier(client.getClientId(), createRequest);
        assertThat(createResponse.getOfficeId()).isEqualTo(client.getOfficeId());
        Long identifierId = createResponse.getResourceId();

        ClientIdentifierUpdateResponse updateResponse = clientHelper.updateClientIdentifier(client.getClientId(), identifierId,
                new ClientIdentifierUpdateRequest().documentKey(Utils.randomStringGenerator("ID_OFFICE_UPDATED_", 10)));
        assertThat(updateResponse.getOfficeId()).isEqualTo(client.getOfficeId());

        var deleteResponse = clientHelper.deleteClientIdentifier(client.getClientId(), identifierId);
        assertThat(deleteResponse.getOfficeId()).isEqualTo(client.getOfficeId());
    }

    @Test
    public void testDeleteClientIdentifierRemovesIt() {
        PostClientsResponse client = clientHelper.createClient(ClientRequestBuilders.defaultClient());
        Long identifierId = createIdentifier(client.getClientId());

        clientHelper.deleteClientIdentifier(client.getClientId(), identifierId);

        CallFailedRuntimeException e = clientHelper.getClientIdentifierExpectingError(client.getClientId(), identifierId);
        assertThat(e.getStatus()).isEqualTo(404);
    }

    @Test
    public void testDeleteClientIdentifierReturns404ForAnotherClientsIdentifier() {
        PostClientsResponse ownerClient = clientHelper.createClient(ClientRequestBuilders.defaultClient());
        PostClientsResponse otherClient = clientHelper.createClient(ClientRequestBuilders.defaultClient());
        Long identifierId = createIdentifier(ownerClient.getClientId());

        CallFailedRuntimeException e = clientHelper.deleteClientIdentifierExpectingError(otherClient.getClientId(), identifierId);
        assertThat(e.getStatus()).isEqualTo(404);

        // sanity: still there for the owning client
        assertThat(clientHelper.getClientIdentifier(ownerClient.getClientId(), identifierId).getId()).isEqualTo(identifierId);
    }

    @Test
    public void testCreateClientIdentifierWithoutDocumentTypeIdReturns400() {
        PostClientsResponse client = clientHelper.createClient(ClientRequestBuilders.defaultClient());

        CallFailedRuntimeException e = clientHelper.createClientIdentifierExpectingError(client.getClientId(),
                new ClientIdentifierCreateRequest().documentKey(Utils.randomStringGenerator("ID_", 10)).status("Active"));

        assertThat(e.getStatus()).isEqualTo(400);
        assertThat(e.getResponseBody()).contains("documentTypeId");
    }

    @Test
    public void testCreateClientIdentifierWithNonPositiveDocumentTypeIdReturns400() {
        PostClientsResponse client = clientHelper.createClient(ClientRequestBuilders.defaultClient());

        CallFailedRuntimeException e = clientHelper.createClientIdentifierExpectingError(client.getClientId(),
                new ClientIdentifierCreateRequest().documentTypeId(0L).documentKey(Utils.randomStringGenerator("ID_", 10))
                        .status("Active"));

        assertThat(e.getStatus()).isEqualTo(400);
        assertThat(e.getResponseBody()).contains("documentTypeId");
    }

    @Test
    public void testCreateClientIdentifierWithBlankDocumentKeyReturns400() {
        PostClientsResponse client = clientHelper.createClient(ClientRequestBuilders.defaultClient());

        CallFailedRuntimeException e = clientHelper.createClientIdentifierExpectingError(client.getClientId(),
                new ClientIdentifierCreateRequest().documentTypeId(DOCUMENT_TYPE_ID).documentKey("  ").status("Active"));

        assertThat(e.getStatus()).isEqualTo(400);
        assertThat(e.getResponseBody()).contains("documentKey");
    }

    @Test
    public void testCreateClientIdentifierWithTooLongValuesFailsAtTheDatabase() {
        PostClientsResponse client = clientHelper.createClient(ClientRequestBuilders.defaultClient());

        // No request-level length limit, same as the legacy path: the 1000-character columns reject it with the
        // generic data integrity error.
        CallFailedRuntimeException tooLongKey = clientHelper.createClientIdentifierExpectingError(client.getClientId(),
                new ClientIdentifierCreateRequest().documentTypeId(DOCUMENT_TYPE_ID).documentKey("K".repeat(1001)).status("Active"));
        assertThat(tooLongKey.getStatus()).isEqualTo(403);
        assertThat(tooLongKey.getResponseBody()).contains("error.msg.clientIdentifier.unknown.data.integrity.issue");

        CallFailedRuntimeException tooLongDescription = clientHelper.createClientIdentifierExpectingError(client.getClientId(),
                new ClientIdentifierCreateRequest().documentTypeId(DOCUMENT_TYPE_ID).documentKey(Utils.randomStringGenerator("ID_", 10))
                        .description("D".repeat(1001)).status("Active"));
        assertThat(tooLongDescription.getStatus()).isEqualTo(403);
        assertThat(tooLongDescription.getResponseBody()).contains("error.msg.clientIdentifier.unknown.data.integrity.issue");
    }

    @Test
    public void testCreateClientIdentifierWithoutStatusDefaultsToActive() {
        PostClientsResponse client = clientHelper.createClient(ClientRequestBuilders.defaultClient());
        clientHelper.createClientIdentifier(client.getClientId(),
                new ClientIdentifierCreateRequest().documentTypeId(DOCUMENT_TYPE_ID).documentKey(Utils.randomStringGenerator("ID_", 10)));

        // The GET response doesn't expose the status, so check it through unique_active_client_identifier: a second
        // active identifier of the same document type for the same client is only rejected if the first one is active.
        CallFailedRuntimeException e = clientHelper.createClientIdentifierExpectingError(client.getClientId(),
                new ClientIdentifierCreateRequest().documentTypeId(DOCUMENT_TYPE_ID).documentKey(Utils.randomStringGenerator("ID_", 10))
                        .status("Active"));
        assertThat(e.getStatus()).isEqualTo(403);
    }

    @Test
    public void testUpdateClientIdentifierWithoutActualChangeReturnsEmptyChanges() {
        PostClientsResponse client = clientHelper.createClient(ClientRequestBuilders.defaultClient());
        String documentKey = Utils.randomStringGenerator("ID_SAME_", 10);
        Long identifierId = clientHelper
                .createClientIdentifier(client.getClientId(),
                        new ClientIdentifierCreateRequest().documentTypeId(DOCUMENT_TYPE_ID).documentKey(documentKey).status("Active"))
                .getResourceId();

        ClientIdentifierUpdateResponse response = clientHelper.updateClientIdentifier(client.getClientId(), identifierId,
                new ClientIdentifierUpdateRequest().documentKey(documentKey));

        assertThat(response.getResourceId()).isEqualTo(identifierId);
        assertThat(response.getChanges()).isNotNull().isEqualTo(new ClientIdentifierUpdateChanges());
    }

    private Long createIdentifier(Long clientId) {
        ClientIdentifierCreateRequest request = new ClientIdentifierCreateRequest().documentTypeId(DOCUMENT_TYPE_ID)
                .documentKey(Utils.randomStringGenerator("ID_", 10)).status("Active");
        return clientHelper.createClientIdentifier(clientId, request).getResourceId();
    }
}
