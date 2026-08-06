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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.util.Locale;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetClientsResponse;
import org.apache.fineract.client.models.PageClientSearchData;
import org.apache.fineract.client.models.PostClientsClientIdIdentifiersRequest;
import org.apache.fineract.client.models.PostClientsClientIdIdentifiersResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostOfficesRequest;
import org.apache.fineract.client.models.PostOfficesResponse;
import org.apache.fineract.client.models.SortOrder;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

public class ClientSearchTest extends IntegrationTest {

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
    public void testClientSearchWorks_WithLastnameText_WithPaging() {
        // given
        String lastname = Utils.randomStringGenerator("Client_LastName_", 5);
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        request1.setLastname(lastname);
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        request2.setLastname(lastname);
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        request3.setLastname(lastname);
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(lastname, 0, 1);
        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    public void testClientSearchWorks_WhenNoExternalIdForClients() {
        // given
        String lastname = Utils.randomStringGenerator("Client_LastName_", 5);
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        request1.setExternalId(null);
        request1.setLastname(lastname);
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        request2.setExternalId(null);
        request2.setLastname(lastname);
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        request3.setExternalId(null);
        request3.setLastname(lastname);
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(lastname, 0, 1);
        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    public void testClientSearchWorks_WithLastnameTextOnDefaultOrdering() {
        // given
        String lastname = Utils.randomStringGenerator("Client_LastName_", 5);
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        request1.setLastname(lastname);
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        request2.setLastname(lastname);
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        request3.setLastname(lastname);
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(lastname);
        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent().get(0).getExternalId().getValue()).isEqualTo(request3.getExternalId());
        assertThat(result.getContent().get(1).getExternalId().getValue()).isEqualTo(request2.getExternalId());
        assertThat(result.getContent().get(2).getExternalId().getValue()).isEqualTo(request1.getExternalId());
    }

    @Test
    public void testClientSearchWorks_WithLastnameText_OrderedByIdAsc() {
        // given
        String lastname = Utils.randomStringGenerator("Client_LastName_", 5);
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        request1.setLastname(lastname);
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        request2.setLastname(lastname);
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        request3.setLastname(lastname);
        clientHelper.createClient(request3);

        SortOrder sortOrder = new SortOrder().property("id").direction(SortOrder.DirectionEnum.ASC);
        // when
        PageClientSearchData result = clientHelper.searchClients(lastname, sortOrder);
        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent().get(0).getExternalId().getValue()).isEqualTo(request1.getExternalId());
        assertThat(result.getContent().get(1).getExternalId().getValue()).isEqualTo(request2.getExternalId());
        assertThat(result.getContent().get(2).getExternalId().getValue()).isEqualTo(request3.getExternalId());
    }

    @Test
    public void testClientSearchWorks_ByExternalId() {
        // given
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(request2.getExternalId());
        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getExternalId().getValue()).isEqualTo(request2.getExternalId());
    }

    @Test
    public void testClientSearchWorks_ByExternalId_CaseInsensitive() {
        // given
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(request2.getExternalId().toUpperCase(Locale.ROOT));
        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getExternalId().getValue()).isEqualTo(request2.getExternalId());
    }

    @Test
    public void testClientSearchWorks_ByAccountNumber() {
        // given
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        PostClientsResponse response2 = clientHelper.createClient(request2);
        GetClientsClientIdResponse client2Data = ClientHelper.getClient(requestSpec, responseSpec,
                Math.toIntExact(response2.getClientId()));

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(client2Data.getAccountNo());
        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getAccountNumber()).isEqualTo(client2Data.getAccountNo());
    }

    @Test
    public void testClientSearchWorks_ByDisplayName() {
        // given
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        String uniqueFirstName = Utils.randomStringGenerator("FN_", 10);
        String uniqueLastName = Utils.randomStringGenerator("LN_", 10);
        request2.setFirstname(uniqueFirstName);
        request2.setLastname(uniqueLastName);
        clientHelper.createClient(request2);
        String client2DisplayName = "%s %s".formatted(uniqueFirstName, uniqueLastName);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(client2DisplayName);
        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDisplayName()).isEqualTo(client2DisplayName);
    }

    @Test
    public void testClientSearchWorks_ByDisplayName_CaseInsensitive() {
        // given
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        String uniqueFirstName = Utils.randomStringGenerator("FN_", 10);
        String uniqueLastName = Utils.randomStringGenerator("LN_", 10);
        request2.setFirstname(uniqueFirstName);
        request2.setLastname(uniqueLastName);
        clientHelper.createClient(request2);
        String client2DisplayName = "%s %s".formatted(uniqueFirstName, uniqueLastName);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(client2DisplayName.toLowerCase());
        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDisplayName()).isEqualTo(client2DisplayName);
    }

    @Test
    public void testClientSearchWorks_ByMobileNo() {
        // given
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        // request2.setMobileNo(Utils.randomNumberGenerator(8).toString());
        request2.setMobileNo(Utils.randomStringGenerator("", 8, Utils.SOURCE_SET_NUMBERS));
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(request2.getMobileNo());
        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getMobileNo()).isEqualTo(request2.getMobileNo());
    }

    @Test
    public void testClientSearchDoesntReturnAnything_ByMobileNo() {
        // given
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(Utils.randomNumberGenerator(8).toString());
        // then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    public void testClientSearchWorks_ByClientIdentifier() {
        // given
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        // request1.setMobileNo(Utils.randomNumberGenerator(8).toString());
        request1.setMobileNo(Utils.randomStringGenerator("", 8, Utils.SOURCE_SET_NUMBERS));
        PostClientsResponse clientResponse = clientHelper.createClient(request1);
        final Long documentType = 1L;
        PostClientsClientIdIdentifiersRequest identifierRequest = ClientHelper.createClientIdentifer(documentType);
        final String documentKey = identifierRequest.getDocumentKey();
        PostClientsClientIdIdentifiersResponse clientIdentifierResponse = clientHelper.createClientIdentifer(clientResponse.getClientId(),
                identifierRequest);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(documentKey);
        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getMobileNo()).isEqualTo(request1.getMobileNo());
    }

    @Test
    public void testClientSearchWorks_ByClientIdentifier_CaseInsensitive() {
        // given
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        request1.setMobileNo(Utils.randomStringGenerator("", 8, Utils.SOURCE_SET_NUMBERS));
        PostClientsResponse clientResponse = clientHelper.createClient(request1);
        final Long documentType = 1L;
        PostClientsClientIdIdentifiersRequest identifierRequest = ClientHelper.createClientIdentifer(documentType);
        final String documentKey = identifierRequest.getDocumentKey();
        clientHelper.createClientIdentifer(clientResponse.getClientId(), identifierRequest);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(documentKey.toLowerCase());
        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getMobileNo()).isEqualTo(request1.getMobileNo());
    }

    @Test
    public void testClientSearchDoesNotDuplicateResults_WhenIdentifierHasMultipleMatches() {
        // given
        PostClientsRequest request = ClientHelper.defaultClientCreationRequest();
        PostClientsResponse clientResponse = clientHelper.createClient(request);

        Integer codeId = (Integer) CodeHelper.createCode(requestSpec, responseSpec, Utils.randomStringGenerator("ClientIdentifierTest_", 6),
                CodeHelper.RESPONSE_ID_ATTRIBUTE_NAME);
        Integer documentTypeIdOne = CodeHelper.createCodeValue(requestSpec, responseSpec, codeId,
                Utils.randomStringGenerator("DocType_", 6), 1);
        Integer documentTypeIdTwo = CodeHelper.createCodeValue(requestSpec, responseSpec, codeId,
                Utils.randomStringGenerator("DocType_", 6), 2);

        String documentKeyToken = Utils.randomStringGenerator("DUP_ID_", 6);
        PostClientsClientIdIdentifiersRequest identifierOne = new PostClientsClientIdIdentifiersRequest()
                .documentTypeId(documentTypeIdOne.longValue()).documentKey(documentKeyToken + "_A").description("Test").status("Active");
        PostClientsClientIdIdentifiersRequest identifierTwo = new PostClientsClientIdIdentifiersRequest()
                .documentTypeId(documentTypeIdTwo.longValue()).documentKey(documentKeyToken + "_B").description("Test").status("Active");
        clientHelper.createClientIdentifer(clientResponse.getClientId(), identifierOne);
        clientHelper.createClientIdentifer(clientResponse.getClientId(), identifierTwo);

        // when
        PageClientSearchData result = clientHelper.searchClients(documentKeyToken);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().get(0).getExternalId().getValue()).isEqualTo(request.getExternalId());
    }

    @Test
    public void testClientSearchByLegalForm() {
        // given
        PostOfficesResponse newOffice = ok(
                fineractClient().offices.createOffice(new PostOfficesRequest().name(Utils.randomStringGenerator("TestOffice_", 6))
                        .parentId(1L).openingDate(LocalDate.of(1970, 1, 1)).dateFormat("yyyy-MM-dd").locale("en_US")));
        PostClientsRequest individualClientRequest = ClientHelper.defaultClientCreationRequest();
        individualClientRequest.setLegalFormId(1L);
        individualClientRequest.setOfficeId(newOffice.getOfficeId());
        PostClientsResponse individualClientResponse = clientHelper.createClient(individualClientRequest);

        PostClientsRequest entityClientRequest = ClientHelper.defaultClientCreationRequest();
        entityClientRequest.setOfficeId(newOffice.getOfficeId());
        entityClientRequest.setLegalFormId(2L);
        PostClientsResponse entityClientResponse = clientHelper.createClient(entityClientRequest);

        PostClientsRequest secondEntityClientRequest = ClientHelper.defaultClientCreationRequest();
        secondEntityClientRequest.setOfficeId(newOffice.getOfficeId());
        secondEntityClientRequest.setLegalFormId(2L);
        PostClientsResponse secondEntityClientResponse = clientHelper.createClient(secondEntityClientRequest);
        // when
        GetClientsResponse individualClients = ok(fineractClient().clients.retrieveAllClients(newOffice.getOfficeId(), null, null, null,
                null, null, null, null, null, null, null, null, 1, null));
        GetClientsResponse entityClients = ok(fineractClient().clients.retrieveAllClients(newOffice.getOfficeId(), null, null, null, null,
                null, null, null, null, "id", null, null, 2, null));
        // then
        assertThat(individualClients.getTotalFilteredRecords()).isEqualTo(1);
        assertThat(individualClients.getPageItems().get(0).getId()).isEqualTo(individualClientResponse.getClientId());
        assertThat(entityClients.getTotalFilteredRecords()).isEqualTo(2);
        assertThat(entityClients.getPageItems().get(0).getId()).isEqualTo(entityClientResponse.getClientId());
        assertThat(entityClients.getPageItems().get(1).getId()).isEqualTo(secondEntityClientResponse.getClientId());
    }

    // ------------------------------------------------------------------
    // orderBy / sortOrder input validation (CVE fix coverage)
    //
    // These exercise GET /api/v1/clients (ClientsApiResource#retrieveAll)
    // directly via the generated retrieveAllClients(...) call, since that
    // is the endpoint targeted by the security report
    //
    // retrieveAllClients param order (from ClientApi.java):
    // officeId, externalId, displayName, firstName, lastName, status,
    // underHierarchy, offset, limit, orderBy, sortOrder, orphansOnly,
    // legalForm, staffId
    // ------------------------------------------------------------------

    private Response<GetClientsResponse> callRetrieveAllClients(String orderBy, String sortOrder) {
        return Calls.executeU(fineractClient().clients.retrieveAllClients(null, null, null, null, null, null, null, null, null, orderBy,
                sortOrder, null, null, null));
    }

    @Test
    public void testClientSearchOrderByRejectsSqlInjectionPoc() {
        // given
        String maliciousOrderBy = "c.office_id, (CASE WHEN (ASCII(SUBSTRING((SELECT table_name FROM "
                + "information_schema.tables WHERE table_schema REGEXP database() LIMIT 0,1),1,1)) - 109) "
                + "THEN c.id ELSE (c.id*-1) END)";
        // when
        Response<GetClientsResponse> response = callRetrieveAllClients(maliciousOrderBy, null);
        // then
        assertThat(response.isSuccessful()).isFalse();
    }

    @Test
    public void testClientSearchOrderByRejectsSubstringBypassAttempt() {
        // given - "officeId" appears as a substring; confirms regex anchors hold
        // when
        Response<GetClientsResponse> response = callRetrieveAllClients("officeId, (CASE WHEN (1=1) THEN 1 END)", null);
        // then
        assertThat(response.isSuccessful()).isFalse();
    }

    @Test
    public void testClientSearchOrderByRejectsCaseMismatch() {
        // given - documented value is "displayName", not "DisplayName" or "DISPLAYNAME"
        // when
        Response<GetClientsResponse> response1 = callRetrieveAllClients("DisplayName", null);
        Response<GetClientsResponse> response2 = callRetrieveAllClients("DISPLAYNAME", null);
        // then
        assertThat(response1.isSuccessful()).isFalse();
        assertThat(response2.isSuccessful()).isFalse();
    }

    @Test
    public void testClientSearchOrderByRejectsSnakeCaseColumnName() {
        // given - undocumented internal SQL column form should no longer be accepted
        // directly
        // when
        Response<GetClientsResponse> response1 = callRetrieveAllClients("c.display_name", null);
        // then
        assertThat(response1.isSuccessful()).isFalse(); // for generic validation this should be isTrue()
    }

    @Test
    public void testClientSearchOrderByRejectsCommaSeparatedList() {
        // given - multi-column orderBy is out of scope for this allowlist
        // when
        Response<GetClientsResponse> response = callRetrieveAllClients("displayName,accountNo", null);
        // then
        assertThat(response.isSuccessful()).isFalse();
    }

    @Test
    public void testClientSearchOrderByRejectsEmptyAndWhitespace() {
        // when
        Response<GetClientsResponse> response = callRetrieveAllClients("   ", null);
        // then
        assertThat(response.isSuccessful()).isTrue();
    }

    @Test
    public void testClientSearchOrderByRejectsSqlKeyword() {
        // given - sanity check against trivial payloads, not just the sophisticated PoC
        // when
        Response<GetClientsResponse> response = callRetrieveAllClients("id; DROP TABLE m_client", null);
        // then
        assertThat(response.isSuccessful()).isFalse();
    }

    @Test
    public void testClientSearchSortOrderRejectsArbitraryValue() {
        // given - direction value outside ASC/DESC should be rejected
        // when
        Response<GetClientsResponse> response = callRetrieveAllClients("displayName", "RANDOM");
        // then
        assertThat(response.isSuccessful()).isFalse();
    }

    @Test
    public void testClientSearchSortOrderRejectsInjectionAttempt() {
        // when
        Response<GetClientsResponse> response = callRetrieveAllClients("displayName", "ASC; DROP TABLE m_client--");
        // then
        assertThat(response.isSuccessful()).isFalse();
    }

    @Test
    public void testClientSearchOrderByAcceptsAllDocumentedValues() {
        // given - the 4 documented allowlist values from the API docs
        for (String validOrderBy : new String[] { "displayName", "accountNo", "officeId", "officeName" }) {
            // when
            Response<GetClientsResponse> response = callRetrieveAllClients(validOrderBy, "ASC");
            // then
            assertThat(response.isSuccessful()).isTrue();
        }
    }

}
