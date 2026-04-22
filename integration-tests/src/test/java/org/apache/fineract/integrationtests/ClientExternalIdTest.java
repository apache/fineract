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
import static org.junit.jupiter.api.Assertions.assertNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.DeleteClientsClientIdResponse;
import org.apache.fineract.client.models.GetClientsClientIdAccountsResponse;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetObligeeData;
import org.apache.fineract.client.models.PostClientsClientIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PutClientsClientIdRequest;
import org.apache.fineract.client.models.PutClientsClientIdResponse;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.apache.fineract.portfolio.client.domain.ClientStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class ClientExternalIdTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private GlobalConfigurationHelper globalConfigurationHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        globalConfigurationHelper = new GlobalConfigurationHelper();
    }

    @Test
    public void whenAutoExternalIdConfigIsOffCreateClient() {
        // given
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
        // when
        final PostClientsResponse clientResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, null);
        // then
        assertNotNull(clientResponse);
        assertNull(clientResponse.getResourceExternalId());
    }

    @Test
    public void whenAutoExternalIdConfigIsOffCreateClientWithValue() {
        // given
        final String externalId = UUID.randomUUID().toString();
        // when
        final PostClientsResponse clientResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, externalId);
        // then
        assertNotNull(clientResponse);
        assertNotNull(clientResponse.getResourceExternalId());
        assertEquals(externalId, clientResponse.getResourceExternalId());

        fetchClientByExternalId(clientResponse.getResourceExternalId());
    }

    @Test
    public void whenAutoExternalIdConfigIsOnCreateClient() {
        // given
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
        // when
        final PostClientsResponse clientResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, null);
        // then
        assertNotNull(clientResponse);
        assertNotNull(clientResponse.getResourceExternalId());
        assertEquals(36, clientResponse.getResourceExternalId().length());

        fetchClientByExternalId(clientResponse.getResourceExternalId());

        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
    }

    @Test
    public void whenAutoExternalIdConfigIsOnCreateClientWithValue() {
        // given
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
        final String externalId = UUID.randomUUID().toString();
        // when
        final PostClientsResponse clientResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, externalId);
        // then
        assertNotNull(clientResponse);
        assertNotNull(clientResponse.getResourceExternalId());
        assertEquals(externalId, clientResponse.getResourceExternalId());

        fetchClientByExternalId(clientResponse.getResourceExternalId());

        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
    }

    @Test
    public void testClientStatusUsingExternalId() {
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
        final PostClientsResponse addClientResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, null);
        final String clientExternalId = addClientResponse.getResourceExternalId();
        final Long clientId = addClientResponse.getClientId();
        assertNotNull(clientExternalId);
        log.info("Client data id {} and external Id {}", clientId, clientExternalId);

        GetClientsClientIdResponse clientResponse = ClientHelper.getClientByExternalId(clientExternalId);
        ClientStatusChecker.verifyClientStatus(ClientStatus.ACTIVE, clientResponse);
        log.info("Client data id {} and status {}", clientExternalId, clientResponse.getStatus().getCode());

        // Close Client action
        String codeName = "ClientClosureReason";
        HashMap<String, Object> code = CodeHelper.getCodeByName(requestSpec, responseSpec, codeName);
        Integer closureReasonId = (Integer) CodeHelper.retrieveOrCreateCodeValue((Integer) code.get("id"), requestSpec, responseSpec)
                .get("id");
        PostClientsClientIdResponse commandResponse = ClientHelper.closeClient(clientExternalId, closureReasonId);
        assertNotNull(commandResponse);
        assertNotNull(commandResponse.getResourceExternalId());
        assertEquals(clientExternalId, commandResponse.getResourceExternalId());
        log.info("Client data id {} and external Id {}", commandResponse.getResourceId(), clientExternalId);
        assertEquals(clientId.intValue(), commandResponse.getResourceId());

        clientResponse = ClientHelper.getClientByExternalId(clientExternalId);
        ClientStatusChecker.verifyClientStatus(ClientStatus.CLOSED, clientResponse);
        log.info("Client data id {} and status {}", clientExternalId, clientResponse.getStatus().getCode());

        // Reactivate Client action
        commandResponse = ClientHelper.reactivateClient(clientExternalId);
        assertNotNull(commandResponse);
        assertNotNull(commandResponse.getResourceExternalId());
        assertEquals(clientExternalId, commandResponse.getResourceExternalId());
        log.info("Client data id {} and external Id {}", commandResponse.getResourceId(), clientExternalId);
        assertEquals(clientId.intValue(), commandResponse.getResourceId());

        clientResponse = ClientHelper.getClientByExternalId(clientExternalId);
        ClientStatusChecker.verifyClientStatus(ClientStatus.PENDING, clientResponse);
        log.info("Client data id {} and status {}", clientExternalId, clientResponse.getStatus().getCode());

        // Reject Client action
        codeName = "ClientRejectReason";
        code = CodeHelper.getCodeByName(requestSpec, responseSpec, codeName);
        Integer rejectionReasonId = (Integer) CodeHelper.retrieveOrCreateCodeValue((Integer) code.get("id"), requestSpec, responseSpec)
                .get("id");
        commandResponse = ClientHelper.rejectClient(clientExternalId, rejectionReasonId);
        assertNotNull(commandResponse);
        assertNotNull(commandResponse.getResourceExternalId());
        assertEquals(clientExternalId, commandResponse.getResourceExternalId());
        log.info("Client data id {} and external Id {}", commandResponse.getResourceId(), clientExternalId);
        assertEquals(clientId.intValue(), commandResponse.getResourceId());

        clientResponse = ClientHelper.getClientByExternalId(clientExternalId);
        ClientStatusChecker.verifyClientStatus(ClientStatus.REJECTED, clientResponse);
        log.info("Client data id {} and status {}", clientExternalId, clientResponse.getStatus().getCode());

        // Activate Client action
        commandResponse = ClientHelper.activateClient(clientExternalId, ClientHelper.DEFAULT_DATE);
        assertNotNull(commandResponse);
        assertNotNull(commandResponse.getResourceExternalId());
        assertEquals(clientExternalId, commandResponse.getResourceExternalId());
        log.info("Client data id {} and external Id {}", commandResponse.getResourceId(), clientExternalId);
        assertEquals(clientId.intValue(), commandResponse.getResourceId());

        clientResponse = ClientHelper.getClientByExternalId(clientExternalId);
        ClientStatusChecker.verifyClientStatus(ClientStatus.ACTIVE, clientResponse);
        log.info("Client data id {} and status {}", clientExternalId, clientResponse.getStatus().getCode());

        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
    }

    @Test
    public void testUpdateClientUsingExternalId() {
        // given
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
        // when
        final PostClientsResponse clientResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, null);
        final String clientExternalId = clientResponse.getResourceExternalId();
        PutClientsClientIdRequest updateRequest = new PutClientsClientIdRequest().externalId(clientExternalId);
        final PutClientsClientIdResponse clientUpdateResponse = ClientHelper.updateClientByExternalId(clientExternalId, updateRequest);
        // then
        assertNotNull(clientUpdateResponse);
        assertNotNull(clientUpdateResponse.getResourceExternalId());
        assertEquals(clientExternalId, clientUpdateResponse.getResourceExternalId());

        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
    }

    @Test
    public void testDeleteClientUsingExternalId() {
        // given
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
        // when
        final PostClientsResponse clientResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, null);
        final String clientExternalId = clientResponse.getResourceExternalId();
        String codeName = "ClientClosureReason";
        HashMap<String, Object> code = CodeHelper.getCodeByName(requestSpec, responseSpec, codeName);
        Integer closureReasonId = (Integer) CodeHelper.retrieveOrCreateCodeValue((Integer) code.get("id"), requestSpec, responseSpec)
                .get("id");
        ClientHelper.closeClient(clientExternalId, closureReasonId);
        ClientHelper.reactivateClient(clientExternalId);

        final DeleteClientsClientIdResponse clientDeleteResponse = ClientHelper.deleteClientByExternalId(clientExternalId);
        assertNotNull(clientDeleteResponse);
        assertNotNull(clientDeleteResponse.getResourceExternalId());
        assertEquals(clientExternalId, clientDeleteResponse.getResourceExternalId());

        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
    }

    @Test
    public void testGetClientAccountsUsingExternalId() {
        // given
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
        // when
        final PostClientsResponse clientResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, null);
        final String clientExternalId = clientResponse.getResourceExternalId();

        GetClientsClientIdAccountsResponse clientAccountsResponse = ClientHelper.getClientAccounts(clientExternalId);

        // then
        assertNotNull(clientAccountsResponse);

        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
    }

    @Test
    public void testGetClientTransferProposalDate() {
        // given
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
        final PostClientsResponse clientResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, null);

        // when
        final String clientExternalId = clientResponse.getResourceExternalId();
        ClientHelper.getProposedTransferDate(clientExternalId);

        fetchClientByExternalId(clientResponse.getResourceExternalId());

        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
    }

    @Test
    public void testGetClientObligeeData() {
        // given
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
        // when
        final PostClientsResponse clientResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, null);
        final String clientExternalId = clientResponse.getResourceExternalId();
        final List<GetObligeeData> obligeeDataResponse = ClientHelper.getObligeeData(clientExternalId);

        // then
        assertNotNull(obligeeDataResponse);

        fetchClientByExternalId(clientResponse.getResourceExternalId());

        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
    }

    private void fetchClientByExternalId(final String externalId) {
        GetClientsClientIdResponse clientResponse = ClientHelper.getClientByExternalId(externalId);
        assertNotNull(clientResponse);
        assertEquals(externalId, clientResponse.getExternalId());
    }
}
