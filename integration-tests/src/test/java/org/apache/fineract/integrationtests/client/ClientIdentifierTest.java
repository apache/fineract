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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientIdentifierTest extends IntegrationTest {

    private static final Gson GSON = new Gson();
    private static final Gson GSON_WITH_NULLS = new GsonBuilder().serializeNulls().create();
    private static final Long DOCUMENT_TYPE_ID = 1L;
    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String LOCALE = "en";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        clientHelper = new ClientHelper(requestSpec, responseSpec);
    }

    @Test
    public void testClientIdentifierIssuanceAndExpiryDatesCrudFlow() {
        PostClientsResponse client = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        String documentKey = Utils.randomStringGenerator("ID_DATES_", 10);
        Map<String, Object> createRequest = identifierRequest(documentKey, "01 January 2024", "01 January 2034", true);
        Long identifierId = ((Number) Utils.performServerPost(requestSpec, responseSpec, identifierUrl(client.getClientId()),
                GSON.toJson(createRequest), "resourceId")).longValue();

        JsonPath createdIdentifier = retrieveIdentifier(client.getClientId(), identifierId);
        assertThat(createdIdentifier.getLong("id")).isEqualTo(identifierId);
        assertThat(createdIdentifier.getLong("clientId")).isEqualTo(client.getClientId());
        assertThat(createdIdentifier.getString("documentKey")).isEqualTo(documentKey);
        assertThat(createdIdentifier.getString("description")).isEqualTo(createRequest.get("description"));
        assertThat(createdIdentifier.getList("issuanceDate", Integer.class)).isEqualTo(List.of(2024, 1, 1));
        assertThat(createdIdentifier.getList("expiryDate", Integer.class)).isEqualTo(List.of(2034, 1, 1));

        Map<String, Object> updateRequest = identifierRequest(documentKey, "01 February 2024", "01 February 2034", false);
        JsonPath updateResponse = JsonPath.from(Utils.performServerPut(requestSpec, responseSpec,
                identifierUrl(client.getClientId(), identifierId), GSON.toJson(updateRequest)));
        assertThat(updateResponse.getLong("resourceId")).isEqualTo(identifierId);
        assertThat(updateResponse.getList("changes.issuanceDate", Integer.class)).isEqualTo(List.of(2024, 2, 1));
        assertThat(updateResponse.getList("changes.expiryDate", Integer.class)).isEqualTo(List.of(2034, 2, 1));

        JsonPath updatedIdentifier = retrieveIdentifier(client.getClientId(), identifierId);
        assertThat(updatedIdentifier.getString("documentKey")).isEqualTo(documentKey);
        assertThat(updatedIdentifier.getString("description")).isEqualTo(updateRequest.get("description"));
        assertThat(updatedIdentifier.getList("issuanceDate", Integer.class)).isEqualTo(List.of(2024, 2, 1));
        assertThat(updatedIdentifier.getList("expiryDate", Integer.class)).isEqualTo(List.of(2034, 2, 1));

        Map<String, Object> clearDatesRequest = new LinkedHashMap<>();
        clearDatesRequest.put("issuanceDate", null);
        clearDatesRequest.put("expiryDate", null);
        JsonPath clearDatesResponse = JsonPath.from(Utils.performServerPut(requestSpec, responseSpec,
                identifierUrl(client.getClientId(), identifierId), GSON_WITH_NULLS.toJson(clearDatesRequest)));
        assertThat(clearDatesResponse.getLong("resourceId")).isEqualTo(identifierId);

        JsonPath clearedIdentifier = retrieveIdentifier(client.getClientId(), identifierId);
        assertThat(clearedIdentifier.getString("documentKey")).isEqualTo(documentKey);
        Assertions.assertThat((Object) clearedIdentifier.get("issuanceDate")).isNull();
        Assertions.assertThat((Object) clearedIdentifier.get("expiryDate")).isNull();
    }

    @Test
    public void testClientIdentifierWithoutIssuanceAndExpiryDatesRemainsValid() {
        PostClientsResponse client = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        String documentKey = Utils.randomStringGenerator("ID_NO_DATES_", 10);
        Map<String, Object> createRequest = new LinkedHashMap<>();
        createRequest.put("documentTypeId", DOCUMENT_TYPE_ID);
        createRequest.put("documentKey", documentKey);
        createRequest.put("description", "Document without date fields");
        createRequest.put("status", "Active");

        Long identifierId = ((Number) Utils.performServerPost(requestSpec, responseSpec, identifierUrl(client.getClientId()),
                GSON.toJson(createRequest), "resourceId")).longValue();

        JsonPath identifier = retrieveIdentifier(client.getClientId(), identifierId);
        assertThat(identifier.getString("documentKey")).isEqualTo(documentKey);
        assertThat(identifier.getString("description")).isEqualTo(createRequest.get("description"));
        Assertions.assertThat((Object) identifier.get("issuanceDate")).isNull();
        Assertions.assertThat((Object) identifier.get("expiryDate")).isNull();
    }

    private Map<String, Object> identifierRequest(final String documentKey, final String issuanceDate, final String expiryDate,
            final boolean includeStatus) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("documentTypeId", DOCUMENT_TYPE_ID);
        request.put("documentKey", documentKey);
        request.put("description", Utils.randomStringGenerator("Identifier Description ", 10));
        if (includeStatus) {
            request.put("status", "Active");
        }
        request.put("issuanceDate", issuanceDate);
        request.put("expiryDate", expiryDate);
        request.put("dateFormat", DATE_FORMAT);
        request.put("locale", LOCALE);
        return request;
    }

    private JsonPath retrieveIdentifier(final Long clientId, final Long identifierId) {
        String response = Utils.performServerGet(requestSpec, responseSpec, identifierUrl(clientId, identifierId), null);
        return JsonPath.from(response);
    }

    private String identifierUrl(final Long clientId) {
        return "/fineract-provider/api/v1/clients/" + clientId + "/identifiers?" + Utils.TENANT_IDENTIFIER;
    }

    private String identifierUrl(final Long clientId, final Long identifierId) {
        return "/fineract-provider/api/v1/clients/" + clientId + "/identifiers/" + identifierId + "?" + Utils.TENANT_IDENTIFIER;
    }
}
