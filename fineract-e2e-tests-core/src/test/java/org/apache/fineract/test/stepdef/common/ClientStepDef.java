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
package org.apache.fineract.test.stepdef.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.Arrays;
import org.apache.fineract.client.models.PostClientsAddressRequest;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.services.ClientApi;
import org.apache.fineract.test.factory.ClientRequestFactory;
import org.apache.fineract.test.helper.CodeHelper;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

public class ClientStepDef extends AbstractStepDef {

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private CodeHelper codeHelper;

    @Autowired
    private ClientRequestFactory clientRequestFactory;

    @Autowired
    private EventCheckHelper eventCheckHelper;

    @When("Admin creates a client with random data")
    public void createClientRandomFirstNameLastName() throws IOException {
        PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest();

        Response<PostClientsResponse> response = clientApi.create6(clientsRequest).execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        testContext().set(TestContextKey.CLIENT_CREATE_RESPONSE, response);

        eventCheckHelper.clientEventCheck(response);
    }

    @When("Admin creates a second client with random data")
    public void createSecondClientRandomFirstNameLastName() throws IOException {
        PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest();

        Response<PostClientsResponse> response = clientApi.create6(clientsRequest).execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        testContext().set(TestContextKey.CLIENT_CREATE_SECOND_CLIENT_RESPONSE, response);

        eventCheckHelper.clientEventCheck(response);
    }

    @When("Admin creates a client with Firstname {string} and Lastname {string}")
    public void createClient(String firstName, String lastName) throws IOException {
        PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest().firstname(firstName).lastname(lastName);

        Response<PostClientsResponse> response = clientApi.create6(clientsRequest).execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        testContext().set(TestContextKey.CLIENT_CREATE_RESPONSE, response);
    }

    @When("Admin creates a client with Firstname {string} and Lastname {string} with address")
    public void createClientWithAddress(String firstName, String lastName) throws IOException {
        Long addressTypeId = codeHelper.createAddressTypeCodeValue(Utils.randomNameGenerator("Residential address", 4)).body()
                .getResourceId();
        Long countryId = codeHelper.createCountryCodeValue(Utils.randomNameGenerator("Hungary", 4)).body().getResourceId();
        Long stateId = codeHelper.createStateCodeValue(Utils.randomNameGenerator("Budapest", 4)).body().getResourceId();
        String city = "Budapest";
        boolean addressIsActive = true;
        long postalCode = 1000L;

        PostClientsAddressRequest addressRequest = new PostClientsAddressRequest().postalCode(postalCode).city(city).countryId(countryId)
                .stateProvinceId(stateId).addressTypeId(addressTypeId).isActive(addressIsActive);

        PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest().firstname(firstName).lastname(lastName)
                .address(Arrays.asList(addressRequest));

        Response<PostClientsResponse> response = clientApi.create6(clientsRequest).execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        testContext().set(TestContextKey.CLIENT_CREATE_RESPONSE, response);

    }

    @When("Admin creates a client with Firstname {string} and Lastname {string} with {string} activation date")
    public void createClientWithSpecifiedDates(String firstName, String lastName, String activationDate) throws IOException {

        PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest().firstname(firstName).lastname(lastName)
                .activationDate(activationDate);

        Response<PostClientsResponse> response = clientApi.create6(clientsRequest).execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        testContext().set(TestContextKey.CLIENT_CREATE_RESPONSE, response);
    }

    @Then("Client is created successfully")
    public void checkClientCreatedSuccessfully() throws IOException {
        Response<PostClientsResponse> response = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);

        assertThat(response.isSuccessful()).as(ErrorMessageHelper.requestFailed(response)).isTrue();

        eventCheckHelper.clientEventCheck(response);
    }
}
