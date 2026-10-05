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

import static org.apache.fineract.integrationtests.client.feign.modules.ClientTestData.CREATED_DATE_PLUS_ONE;
import static org.apache.fineract.integrationtests.client.feign.modules.ClientTestData.DEFAULT_ACTIVATION_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCodeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.ClientTestData;
import org.apache.fineract.portfolio.client.domain.ClientStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class ClientExternalIdTest extends FeignIntegrationTest {

    private FeignClientHelper clientHelper;
    private FeignCodeHelper codeHelper;
    private FeignGlobalConfigurationHelper globalConfigurationHelper;

    @BeforeAll
    public void setup() {
        clientHelper = new FeignClientHelper(fineractClient());
        codeHelper = new FeignCodeHelper(fineractClient());
        globalConfigurationHelper = new FeignGlobalConfigurationHelper(fineractClient());
    }

    @Test
    public void whenAutoExternalIdConfigIsOffCreateClient() {
        // given
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
        // when
        final PostClientsResponse clientResponse = addClientAsPerson(null);
        // then
        assertNotNull(clientResponse);
        assertNull(clientResponse.getResourceExternalId());
    }

    @Test
    public void whenAutoExternalIdConfigIsOffCreateClientWithValue() {
        // given
        final String externalId = UUID.randomUUID().toString();
        // when
        final PostClientsResponse clientResponse = addClientAsPerson(externalId);
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
        final PostClientsResponse clientResponse = addClientAsPerson(null);
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
        final PostClientsResponse clientResponse = addClientAsPerson(externalId);
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
        final PostClientsResponse addClientResponse = addClientAsPerson(null);
        final String clientExternalId = addClientResponse.getResourceExternalId();
        final Long clientId = addClientResponse.getClientId();
        assertNotNull(clientExternalId);
        log.info("Client data id {} and external Id {}", clientId, clientExternalId);

        GetClientsClientIdResponse clientResponse = clientHelper.getClient(clientExternalId);
        ClientStatusChecker.verifyClientStatus(ClientStatus.ACTIVE, clientResponse);
        log.info("Client data id {} and status {}", clientExternalId, clientResponse.getStatus().getCode());

        // Close Client action
        Long closureReasonId = codeHelper.retrieveOrCreateCodeValueId(ClientTestData.CLOSURE_REASON_CODE);
        PostClientsClientIdResponse commandResponse = clientHelper.closeClient(clientExternalId,
                ClientRequestBuilders.closeClient(closureReasonId, CREATED_DATE_PLUS_ONE));
        assertNotNull(commandResponse);
        assertNotNull(commandResponse.getResourceExternalId());
        assertEquals(clientExternalId, commandResponse.getResourceExternalId());
        log.info("Client data id {} and external Id {}", commandResponse.getResourceId(), clientExternalId);
        assertEquals(clientId, commandResponse.getResourceId());

        clientResponse = clientHelper.getClient(clientExternalId);
        ClientStatusChecker.verifyClientStatus(ClientStatus.CLOSED, clientResponse);
        log.info("Client data id {} and status {}", clientExternalId, clientResponse.getStatus().getCode());

        // Reactivate Client action
        commandResponse = clientHelper.reactivateClient(clientExternalId, ClientRequestBuilders.reactivateClient(CREATED_DATE_PLUS_ONE));
        assertNotNull(commandResponse);
        assertNotNull(commandResponse.getResourceExternalId());
        assertEquals(clientExternalId, commandResponse.getResourceExternalId());
        log.info("Client data id {} and external Id {}", commandResponse.getResourceId(), clientExternalId);
        assertEquals(clientId, commandResponse.getResourceId());

        clientResponse = clientHelper.getClient(clientExternalId);
        ClientStatusChecker.verifyClientStatus(ClientStatus.PENDING, clientResponse);
        log.info("Client data id {} and status {}", clientExternalId, clientResponse.getStatus().getCode());

        // Reject Client action
        Long rejectionReasonId = codeHelper.retrieveOrCreateCodeValueId(ClientTestData.REJECTION_REASON_CODE);
        commandResponse = clientHelper.rejectClient(clientExternalId,
                ClientRequestBuilders.rejectClient(rejectionReasonId, CREATED_DATE_PLUS_ONE));
        assertNotNull(commandResponse);
        assertNotNull(commandResponse.getResourceExternalId());
        assertEquals(clientExternalId, commandResponse.getResourceExternalId());
        log.info("Client data id {} and external Id {}", commandResponse.getResourceId(), clientExternalId);
        assertEquals(clientId, commandResponse.getResourceId());

        clientResponse = clientHelper.getClient(clientExternalId);
        ClientStatusChecker.verifyClientStatus(ClientStatus.REJECTED, clientResponse);
        log.info("Client data id {} and status {}", clientExternalId, clientResponse.getStatus().getCode());

        // Activate Client action
        commandResponse = clientHelper.activateClient(clientExternalId, ClientRequestBuilders.activateClient(DEFAULT_ACTIVATION_DATE));
        assertNotNull(commandResponse);
        assertNotNull(commandResponse.getResourceExternalId());
        assertEquals(clientExternalId, commandResponse.getResourceExternalId());
        log.info("Client data id {} and external Id {}", commandResponse.getResourceId(), clientExternalId);
        assertEquals(clientId, commandResponse.getResourceId());

        clientResponse = clientHelper.getClient(clientExternalId);
        ClientStatusChecker.verifyClientStatus(ClientStatus.ACTIVE, clientResponse);
        log.info("Client data id {} and status {}", clientExternalId, clientResponse.getStatus().getCode());

        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
    }

    @Test
    public void testUpdateClientUsingExternalId() {
        // given
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
        // when
        final PostClientsResponse clientResponse = addClientAsPerson(null);
        final String clientExternalId = clientResponse.getResourceExternalId();
        PutClientsClientIdRequest updateRequest = new PutClientsClientIdRequest().externalId(clientExternalId);
        final PutClientsClientIdResponse clientUpdateResponse = clientHelper.updateClient(clientExternalId, updateRequest);
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
        final PostClientsResponse clientResponse = addClientAsPerson(null);
        final String clientExternalId = clientResponse.getResourceExternalId();
        Long closureReasonId = codeHelper.retrieveOrCreateCodeValueId(ClientTestData.CLOSURE_REASON_CODE);
        clientHelper.closeClient(clientExternalId, ClientRequestBuilders.closeClient(closureReasonId, CREATED_DATE_PLUS_ONE));
        clientHelper.reactivateClient(clientExternalId, ClientRequestBuilders.reactivateClient(CREATED_DATE_PLUS_ONE));

        final DeleteClientsClientIdResponse clientDeleteResponse = clientHelper.deleteClient(clientExternalId);
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
        final PostClientsResponse clientResponse = addClientAsPerson(null);
        final String clientExternalId = clientResponse.getResourceExternalId();

        GetClientsClientIdAccountsResponse clientAccountsResponse = clientHelper.getClientAccounts(clientExternalId);

        // then
        assertNotNull(clientAccountsResponse);

        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
    }

    @Test
    public void testGetClientTransferProposalDate() {
        // given
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
        final PostClientsResponse clientResponse = addClientAsPerson(null);

        // when
        final String clientExternalId = clientResponse.getResourceExternalId();
        clientHelper.getProposedTransferDate(clientExternalId);

        fetchClientByExternalId(clientResponse.getResourceExternalId());

        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
    }

    @Test
    public void testGetClientObligeeData() {
        // given
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
        // when
        final PostClientsResponse clientResponse = addClientAsPerson(null);
        final String clientExternalId = clientResponse.getResourceExternalId();
        final List<GetObligeeData> obligeeDataResponse = clientHelper.getObligeeData(clientExternalId);

        // then
        assertNotNull(obligeeDataResponse);

        fetchClientByExternalId(clientResponse.getResourceExternalId());

        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
    }

    private PostClientsResponse addClientAsPerson(final String externalId) {
        return clientHelper.createClient(ClientRequestBuilders.defaultClient().externalId(externalId));
    }

    private void fetchClientByExternalId(final String externalId) {
        GetClientsClientIdResponse clientResponse = clientHelper.getClient(externalId);
        assertNotNull(clientResponse);
        assertEquals(externalId, clientResponse.getExternalId());
    }
}
