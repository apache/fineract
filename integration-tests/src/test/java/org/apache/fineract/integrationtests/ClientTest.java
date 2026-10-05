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
import static org.apache.fineract.integrationtests.client.feign.modules.ClientTestData.LEGAL_FORM_PERSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AddressData;
import org.apache.fineract.client.models.ClientAddressRequest;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetClientsPageItemsResponse;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.PostClientClientIdAddressesResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PutClientsClientIdRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCodeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.ClientTestData;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.client.domain.ClientStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ClientTest extends FeignIntegrationTest {

    private static final SecureRandom rand = new SecureRandom();
    // TODO: figure out why Jakarta validation throws 403 instead of 400 (same note as StaffTest)
    private static final int BAD_REQUEST = 400;
    private static final String INVALID_MOBILE_NO = "validation.msg.client.mobileNo.does.not.match.regexp";
    private static final String COUNTRY_CODE = "COUNTRY";
    private static final String STATE_CODE = "STATE";
    private static final String ADDRESS_TYPE_CODE = "ADDRESS_TYPE";

    private FeignClientHelper clientHelper;
    private FeignCodeHelper codeHelper;
    private FeignGlobalConfigurationHelper globalConfigurationHelper;

    @BeforeAll
    public void setup() {
        clientHelper = new FeignClientHelper(fineractClient());
        codeHelper = new FeignCodeHelper(fineractClient());
        globalConfigurationHelper = new FeignGlobalConfigurationHelper(fineractClient());
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }

    @Test
    public void testClientCreateWithInvalidMobileNoValidationError() {
        // given
        PostClientsRequest request = ClientRequestBuilders.defaultClient().mobileNo("invalid-phone-###");

        // when/then: expects 400 (validation error), not 500 (would indicate the
        // FineractPhoneProperties NPE regression from FINERACT-405 has resurfaced)
        assertInvalidMobileNo(clientHelper.createClientExpectingError(request));
    }

    @Test
    public void testClientUpdateWithInvalidMobileNoValidationError() {
        // given
        final Long clientId = clientHelper.createClient(DEFAULT_ACTIVATION_DATE);
        PutClientsClientIdRequest request = new PutClientsClientIdRequest().mobileNo("invalid-phone-###");

        // when/then: expects 400 (validation error), not 500 (would indicate the
        // FineractPhoneProperties NPE regression from FINERACT-405 has resurfaced)
        assertInvalidMobileNo(clientHelper.updateClientExpectingError(clientId, request));
    }

    @Test
    public void testClientStatus() {
        final Long clientId = clientHelper.createClient(DEFAULT_ACTIVATION_DATE);
        verifyStatusTransitions(clientId);
    }

    @Test
    public void testClientAsPersonStatus() {
        final Long clientId = clientHelper.createClient(DEFAULT_ACTIVATION_DATE);
        verifyStatusTransitions(clientId);
    }

    @Test
    public void testClientAsEntityStatus() {
        final Long clientId = createEntityClient();
        verifyStatusTransitions(clientId);
    }

    @Test
    @SuppressFBWarnings(value = {
            "DMI_RANDOM_USED_ONLY_ONCE" }, justification = "False positive for random object created and used only once")
    public void testPendingOnlyClientRequest() {

        // Add a few clients to the server and activate a random amount of them
        for (int i = 0; i < 15; i++) {
            final Long clientId = createEntityClient();
            if (rand.nextInt(10) % 2 == 0) {
                // Takes Client to pending status
                clientHelper.closeClient(clientId, CREATED_DATE_PLUS_ONE);
                clientHelper.reactivateClient(clientId, ClientRequestBuilders.reactivateClient(CREATED_DATE_PLUS_ONE));
            }
            // Other clients stay in Active status
        }
        List<GetClientsPageItemsResponse> clientsReceived = retrieveClientsWithStatus("pending");
        assertNotEquals(0, clientsReceived.size());
        for (GetClientsPageItemsResponse client : clientsReceived) {
            assertClientStatus(client.getId(), ClientStatus.PENDING);
        }

        clientsReceived = retrieveClientsWithStatus("active");
        assertNotEquals(0, clientsReceived.size());
        for (GetClientsPageItemsResponse client : clientsReceived) {
            assertClientStatus(client.getId(), ClientStatus.ACTIVE);
        }
    }

    @Test
    public void testClientAddressCreationWorks() {
        // given
        enableAddress();

        Long addressTypeId = codeHelper.createCodeValue(ADDRESS_TYPE_CODE, Utils.randomStringGenerator("Residential address", 4), 0);
        Long countryId = codeHelper.createCodeValue(COUNTRY_CODE, Utils.randomStringGenerator("Hungary", 4), 0);
        Long stateId = codeHelper.createCodeValue(STATE_CODE, Utils.randomStringGenerator("Budapest", 4), 0);
        String city = "Budapest";
        boolean addressIsActive = true;
        String addressLine1 = "Pava Street 1";
        String postalCode = "1000";
        String street = "Pava Street";
        String townVillage = "Ferencvaros";
        String countyDistrict = "Pest County";

        // when
        ClientAddressRequest addressRequest = new ClientAddressRequest().street(street).townVillage(townVillage)
                .countyDistrict(countyDistrict).addressLine1(addressLine1).postalCode(postalCode).city(city).countryId(countryId)
                .stateProvinceId(stateId).addressTypeId(addressTypeId).isActive(addressIsActive);
        PostClientsRequest request = ClientRequestBuilders.defaultClient().address(List.of(addressRequest));
        final Long clientId = clientHelper.createClient(request).getClientId();

        // then
        assertEquals(clientId, clientHelper.getClient(clientId).getId());
        List<AddressData> clientAddresses = clientHelper.getClientAddresses(clientId);
        AddressData addressResponse = clientAddresses.get(0);
        assertThat(addressResponse.getCity()).isEqualTo(city);
        assertThat(addressResponse.getCountryId()).isEqualTo(countryId);
        assertThat(addressResponse.getStateProvinceId()).isEqualTo(stateId);
        assertThat(addressResponse.getAddressTypeId()).isEqualTo(addressTypeId);
        assertThat(addressResponse.getIsActive()).isEqualTo(addressIsActive);
        assertThat(addressResponse.getPostalCode()).isEqualTo(postalCode);
        assertThat(addressResponse.getStreet()).isEqualTo(street);
        assertThat(addressResponse.getTownVillage()).isEqualTo(townVillage);
        assertThat(addressResponse.getCountyDistrict()).isEqualTo(countyDistrict);
    }

    @Test
    public void testClientAddressCreationWorksAfterClientIsCreated() {
        // given
        enableAddress();

        Long addressTypeId = codeHelper.createCodeValue(ADDRESS_TYPE_CODE, Utils.randomStringGenerator("Residential address", 4), 0);
        Long countryId = codeHelper.createCodeValue(COUNTRY_CODE, Utils.randomStringGenerator("Hungary", 4), 0);
        Long stateId = codeHelper.createCodeValue(STATE_CODE, Utils.randomStringGenerator("Budapest", 4), 0);
        String city = "Budapest";
        boolean addressIsActive = true;
        String addressLine1 = "Rakoczi Street 1";
        String postalCode = "1000";
        String street = "Rakoczi Street";
        String townVillage = "Belvaros";
        String countyDistrict = "Buda District";

        final Long clientId = clientHelper.createClient(ClientRequestBuilders.defaultClient()).getClientId();
        // when
        ClientAddressRequest request = new ClientAddressRequest().street(street).townVillage(townVillage).countyDistrict(countyDistrict)
                .addressLine1(addressLine1).postalCode(postalCode).city(city).countryId(countryId).stateProvinceId(stateId)
                .isActive(addressIsActive);
        PostClientClientIdAddressesResponse response = clientHelper.createClientAddress(clientId, addressTypeId, request);
        // then
        assertThat(response.getResourceId()).isNotNull();
        List<AddressData> clientAddresses = clientHelper.getClientAddresses(clientId);
        AddressData addressResponse = clientAddresses.get(0);
        assertThat(addressResponse.getCity()).isEqualTo(city);
        assertThat(addressResponse.getCountryId()).isEqualTo(countryId);
        assertThat(addressResponse.getStateProvinceId()).isEqualTo(stateId);
        assertThat(addressResponse.getAddressTypeId()).isEqualTo(addressTypeId);
        assertThat(addressResponse.getIsActive()).isEqualTo(addressIsActive);
        assertThat(addressResponse.getPostalCode()).isEqualTo(postalCode);
        assertThat(addressResponse.getStreet()).isEqualTo(street);
        assertThat(addressResponse.getTownVillage()).isEqualTo(townVillage);
        assertThat(addressResponse.getCountyDistrict()).isEqualTo(countyDistrict);

        String updatedStreet = "Andrassy Avenue";
        String updatedTownVillage = "Terezvaros";
        String updatedCountyDistrict = "Central District";
        clientHelper.updateClientAddress(clientId, new ClientAddressRequest().addressId(addressResponse.getAddressId())
                .street(updatedStreet).townVillage(updatedTownVillage).countyDistrict(updatedCountyDistrict));

        List<AddressData> updatedClientAddresses = clientHelper.getClientAddresses(clientId);
        AddressData updatedAddressResponse = updatedClientAddresses.get(0);
        assertThat(updatedAddressResponse.getStreet()).isEqualTo(updatedStreet);
        assertThat(updatedAddressResponse.getTownVillage()).isEqualTo(updatedTownVillage);
        assertThat(updatedAddressResponse.getCountyDistrict()).isEqualTo(updatedCountyDistrict);
    }

    private void enableAddress() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_ADDRESS,
                new PutGlobalConfigurationsRequest().enabled(true));
        GlobalConfigurationPropertyData updatedAddressEnabledConfig = globalConfigurationHelper
                .getGlobalConfigurationByName(GlobalConfigurationConstants.ENABLE_ADDRESS);
        assertThat(updatedAddressEnabledConfig.getEnabled()).isTrue();
    }

    @Test
    public void testClientName() {
        String firstName = Utils.randomFirstNameGenerator();
        String middleName = Utils.randomFirstNameGenerator();
        String lastName = Utils.randomLastNameGenerator();
        String fullName = firstName + ' ' + middleName + ' ' + lastName;

        PostClientsRequest request = new PostClientsRequest().officeId(1L).legalFormId(LEGAL_FORM_PERSON).firstname(firstName)
                .middlename(middleName).lastname(lastName).externalId(UUID.randomUUID().toString()).dateFormat(Utils.DATE_FORMAT)
                .locale("en").active(true).activationDate(DEFAULT_ACTIVATION_DATE);
        Long clientId = clientHelper.createClient(request).getClientId();
        assertNotNull(clientId);

        GetClientsClientIdResponse client = clientHelper.getClient(clientId);
        assertNotNull(client);
        assertEquals(fullName, client.getDisplayName());

        request = new PostClientsRequest().officeId(1L).legalFormId(LEGAL_FORM_PERSON).fullname(fullName)
                .externalId(UUID.randomUUID().toString()).dateFormat(Utils.DATE_FORMAT).locale("en").active(true)
                .activationDate(DEFAULT_ACTIVATION_DATE);
        clientId = clientHelper.createClient(request).getClientId();
        assertNotNull(clientId);

        client = clientHelper.getClient(clientId);
        assertNotNull(client);
        assertEquals(fullName, client.getDisplayName());
    }

    private static void assertInvalidMobileNo(CallFailedRuntimeException error) {
        assertEquals(BAD_REQUEST, error.getStatus());
        assertEquals(INVALID_MOBILE_NO, FeignErrors.errorGlobalisationCode(error));
    }

    private Long createEntityClient() {
        Long constitutionId = codeHelper.retrieveOrCreateCodeValueId(ClientTestData.CONSTITUTION_CODE);
        return clientHelper.createClient(ClientRequestBuilders.activeEntityClient(constitutionId)).getClientId();
    }

    private void verifyStatusTransitions(Long clientId) {
        GetClientsClientIdResponse client = clientHelper.getClient(clientId);
        assertEquals(clientId, client.getId());
        ClientStatusChecker.verifyClientStatus(ClientStatus.ACTIVE, client);

        clientHelper.closeClient(clientId, CREATED_DATE_PLUS_ONE);
        assertClientStatus(clientId, ClientStatus.CLOSED);

        clientHelper.reactivateClient(clientId, ClientRequestBuilders.reactivateClient(CREATED_DATE_PLUS_ONE));
        assertClientStatus(clientId, ClientStatus.PENDING);

        clientHelper.rejectClient(clientId, CREATED_DATE_PLUS_ONE);
        assertClientStatus(clientId, ClientStatus.REJECTED);

        clientHelper.activateClient(clientId, ClientRequestBuilders.activateClient(CREATED_DATE_PLUS_ONE));
        assertClientStatus(clientId, ClientStatus.ACTIVE);

        clientHelper.closeClient(clientId, CREATED_DATE_PLUS_ONE);
        assertClientStatus(clientId, ClientStatus.CLOSED);

        clientHelper.reactivateClient(clientId, ClientRequestBuilders.reactivateClient(CREATED_DATE_PLUS_ONE));
        assertClientStatus(clientId, ClientStatus.PENDING);

        clientHelper.withdrawClient(clientId, CREATED_DATE_PLUS_ONE);
        assertClientStatus(clientId, ClientStatus.WITHDRAWN);
    }

    private List<GetClientsPageItemsResponse> retrieveClientsWithStatus(String status) {
        return clientHelper.retrieveClients(Map.of("paged", true, "status", status, "limit", 50)).getPageItems();
    }

    private void assertClientStatus(Long clientId, ClientStatus expected) {
        ClientStatusChecker.verifyClientStatus(expected, clientHelper.getClient(clientId));
    }
}
