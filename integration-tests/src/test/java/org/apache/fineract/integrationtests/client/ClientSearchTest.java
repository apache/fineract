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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetClientsResponse;
import org.apache.fineract.client.models.PageClientSearchData;
import org.apache.fineract.client.models.PostClientsClientIdIdentifiersRequest;
import org.apache.fineract.client.models.PostClientsClientIdIdentifiersResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostOfficesResponse;
import org.apache.fineract.client.models.SortOrder;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCodeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ClientSearchTest extends FeignIntegrationTest {

    private static final int FORBIDDEN = 403;
    private static final String ALLOW_LIST_REJECTION = "error.msg.input.validation";
    private static final String SQL_VALIDATOR_REJECTION = "error.msg.sql.validation";

    private FeignClientHelper clientHelper;
    private FeignCodeHelper codeHelper;
    private FeignOfficeHelper officeHelper;

    @BeforeAll
    public void setup() {
        clientHelper = new FeignClientHelper(fineractClient());
        codeHelper = new FeignCodeHelper(fineractClient());
        officeHelper = new FeignOfficeHelper(fineractClient());
    }

    @Test
    public void testClientSearchWorks_WithLastnameText_WithPaging() {
        // given
        String lastname = Utils.randomStringGenerator("Client_LastName_", 5);
        PostClientsRequest request1 = ClientRequestBuilders.defaultClient();
        request1.setLastname(lastname);
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientRequestBuilders.defaultClient();
        request2.setLastname(lastname);
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientRequestBuilders.defaultClient();
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
        PostClientsRequest request1 = ClientRequestBuilders.defaultClient();
        request1.setExternalId(null);
        request1.setLastname(lastname);
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientRequestBuilders.defaultClient();
        request2.setExternalId(null);
        request2.setLastname(lastname);
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientRequestBuilders.defaultClient();
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
        PostClientsRequest request1 = ClientRequestBuilders.defaultClient();
        request1.setLastname(lastname);
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientRequestBuilders.defaultClient();
        request2.setLastname(lastname);
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientRequestBuilders.defaultClient();
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
        PostClientsRequest request1 = ClientRequestBuilders.defaultClient();
        request1.setLastname(lastname);
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientRequestBuilders.defaultClient();
        request2.setLastname(lastname);
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientRequestBuilders.defaultClient();
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
        PostClientsRequest request1 = ClientRequestBuilders.defaultClient();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientRequestBuilders.defaultClient();
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientRequestBuilders.defaultClient();
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
        PostClientsRequest request1 = ClientRequestBuilders.defaultClient();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientRequestBuilders.defaultClient();
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientRequestBuilders.defaultClient();
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
        PostClientsRequest request1 = ClientRequestBuilders.defaultClient();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientRequestBuilders.defaultClient();
        PostClientsResponse response2 = clientHelper.createClient(request2);
        GetClientsClientIdResponse client2Data = clientHelper.getClient(response2.getClientId());

        PostClientsRequest request3 = ClientRequestBuilders.defaultClient();
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
        PostClientsRequest request1 = ClientRequestBuilders.defaultClient();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientRequestBuilders.defaultClient();
        String uniqueFirstName = Utils.randomStringGenerator("FN_", 10);
        String uniqueLastName = Utils.randomStringGenerator("LN_", 10);
        request2.setFirstname(uniqueFirstName);
        request2.setLastname(uniqueLastName);
        clientHelper.createClient(request2);
        String client2DisplayName = "%s %s".formatted(uniqueFirstName, uniqueLastName);

        PostClientsRequest request3 = ClientRequestBuilders.defaultClient();
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
        PostClientsRequest request1 = ClientRequestBuilders.defaultClient();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientRequestBuilders.defaultClient();
        String uniqueFirstName = Utils.randomStringGenerator("FN_", 10);
        String uniqueLastName = Utils.randomStringGenerator("LN_", 10);
        request2.setFirstname(uniqueFirstName);
        request2.setLastname(uniqueLastName);
        clientHelper.createClient(request2);
        String client2DisplayName = "%s %s".formatted(uniqueFirstName, uniqueLastName);

        PostClientsRequest request3 = ClientRequestBuilders.defaultClient();
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
        PostClientsRequest request1 = ClientRequestBuilders.defaultClient();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientRequestBuilders.defaultClient();
        // request2.setMobileNo(Utils.randomNumberGenerator(8).toString());
        request2.setMobileNo(Utils.randomStringGenerator("", 8, Utils.SOURCE_SET_NUMBERS));
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientRequestBuilders.defaultClient();
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
        PostClientsRequest request1 = ClientRequestBuilders.defaultClient();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientRequestBuilders.defaultClient();
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientRequestBuilders.defaultClient();
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
        PostClientsRequest request1 = ClientRequestBuilders.defaultClient();
        // request1.setMobileNo(Utils.randomNumberGenerator(8).toString());
        request1.setMobileNo(Utils.randomStringGenerator("", 8, Utils.SOURCE_SET_NUMBERS));
        PostClientsResponse clientResponse = clientHelper.createClient(request1);
        final Long documentType = 1L;
        PostClientsClientIdIdentifiersRequest identifierRequest = clientIdentifier(documentType);
        final String documentKey = identifierRequest.getDocumentKey();
        PostClientsClientIdIdentifiersResponse clientIdentifierResponse = clientHelper.createClientIdentifier(clientResponse.getClientId(),
                identifierRequest);

        PostClientsRequest request2 = ClientRequestBuilders.defaultClient();
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientRequestBuilders.defaultClient();
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
        PostClientsRequest request1 = ClientRequestBuilders.defaultClient();
        request1.setMobileNo(Utils.randomStringGenerator("", 8, Utils.SOURCE_SET_NUMBERS));
        PostClientsResponse clientResponse = clientHelper.createClient(request1);
        final Long documentType = 1L;
        PostClientsClientIdIdentifiersRequest identifierRequest = clientIdentifier(documentType);
        final String documentKey = identifierRequest.getDocumentKey();
        clientHelper.createClientIdentifier(clientResponse.getClientId(), identifierRequest);

        PostClientsRequest request2 = ClientRequestBuilders.defaultClient();
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientRequestBuilders.defaultClient();
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
        PostClientsRequest request = ClientRequestBuilders.defaultClient();
        PostClientsResponse clientResponse = clientHelper.createClient(request);

        Long codeId = codeHelper.createCode(Utils.randomStringGenerator("ClientIdentifierTest_", 6));
        Long documentTypeIdOne = codeHelper.createCodeValue(codeId, Utils.randomStringGenerator("DocType_", 6), 1);
        Long documentTypeIdTwo = codeHelper.createCodeValue(codeId, Utils.randomStringGenerator("DocType_", 6), 2);

        String documentKeyToken = Utils.randomStringGenerator("DUP_ID_", 6);
        PostClientsClientIdIdentifiersRequest identifierOne = new PostClientsClientIdIdentifiersRequest().documentTypeId(documentTypeIdOne)
                .documentKey(documentKeyToken + "_A").description("Test").status("Active");
        PostClientsClientIdIdentifiersRequest identifierTwo = new PostClientsClientIdIdentifiersRequest().documentTypeId(documentTypeIdTwo)
                .documentKey(documentKeyToken + "_B").description("Test").status("Active");
        clientHelper.createClientIdentifier(clientResponse.getClientId(), identifierOne);
        clientHelper.createClientIdentifier(clientResponse.getClientId(), identifierTwo);

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
        PostOfficesResponse newOffice = officeHelper.createOffice(LocalDate.of(1970, 1, 1));
        PostClientsRequest individualClientRequest = ClientRequestBuilders.defaultClient();
        individualClientRequest.setLegalFormId(1L);
        individualClientRequest.setOfficeId(newOffice.getOfficeId());
        PostClientsResponse individualClientResponse = clientHelper.createClient(individualClientRequest);

        PostClientsRequest entityClientRequest = ClientRequestBuilders.defaultClient();
        entityClientRequest.setOfficeId(newOffice.getOfficeId());
        entityClientRequest.setLegalFormId(2L);
        PostClientsResponse entityClientResponse = clientHelper.createClient(entityClientRequest);

        PostClientsRequest secondEntityClientRequest = ClientRequestBuilders.defaultClient();
        secondEntityClientRequest.setOfficeId(newOffice.getOfficeId());
        secondEntityClientRequest.setLegalFormId(2L);
        PostClientsResponse secondEntityClientResponse = clientHelper.createClient(secondEntityClientRequest);
        // when
        GetClientsResponse individualClients = clientHelper.retrieveClients(Map.of("officeId", newOffice.getOfficeId(), "legalForm", 1));
        GetClientsResponse entityClients = clientHelper
                .retrieveClients(Map.of("officeId", newOffice.getOfficeId(), "orderBy", "id", "legalForm", 2));
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
    // ------------------------------------------------------------------

    private static Map<String, Object> ordering(String orderBy, String sortOrder) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("orderBy", orderBy);
        if (sortOrder != null) {
            queryParams.put("sortOrder", sortOrder);
        }
        return queryParams;
    }

    private void assertRejected(String expectedCode, String orderBy, String sortOrder) {
        CallFailedRuntimeException error = clientHelper.retrieveClientsExpectingError(ordering(orderBy, sortOrder));
        assertThat(error.getStatus()).isEqualTo(FORBIDDEN);
        assertThat(FeignErrors.errorGlobalisationCode(error)).isEqualTo(expectedCode);
    }

    @Test
    public void testClientSearchOrderByRejectsSqlInjectionPoc() {
        // given
        String maliciousOrderBy = "c.office_id, (CASE WHEN (ASCII(SUBSTRING((SELECT table_name FROM "
                + "information_schema.tables WHERE table_schema REGEXP database() LIMIT 0,1),1,1)) - 109) "
                + "THEN c.id ELSE (c.id*-1) END)";
        // when/then
        assertRejected(ALLOW_LIST_REJECTION, maliciousOrderBy, null);
    }

    @Test
    public void testClientSearchOrderByRejectsSubstringBypassAttempt() {
        // given - "officeId" appears as a substring; confirms regex anchors hold
        // when/then
        assertRejected(SQL_VALIDATOR_REJECTION, "officeId, (CASE WHEN (1=1) THEN 1 END)", null);
    }

    @Test
    public void testClientSearchOrderByRejectsCaseMismatch() {
        // given - documented value is "displayName", not "DisplayName" or "DISPLAYNAME"
        // when/then
        assertRejected(ALLOW_LIST_REJECTION, "DisplayName", null);
        assertRejected(ALLOW_LIST_REJECTION, "DISPLAYNAME", null);
    }

    @Test
    public void testClientSearchOrderByRejectsSnakeCaseColumnName() {
        // given - undocumented internal SQL column form should no longer be accepted
        // directly
        // when/then
        assertRejected(ALLOW_LIST_REJECTION, "c.display_name", null); // for generic validation this should succeed
    }

    @Test
    public void testClientSearchOrderByRejectsCommaSeparatedList() {
        // given - multi-column orderBy is out of scope for this allowlist
        // when/then
        assertRejected(ALLOW_LIST_REJECTION, "displayName,accountNo", null);
    }

    @Test
    public void testClientSearchOrderByRejectsEmptyAndWhitespace() {
        // when
        GetClientsResponse response = clientHelper.retrieveClients(ordering("   ", null));
        // then
        assertThat(response).isNotNull();
    }

    @Test
    public void testClientSearchOrderByRejectsSqlKeyword() {
        // given - sanity check against trivial payloads, not just the sophisticated PoC
        // when/then
        assertRejected(SQL_VALIDATOR_REJECTION, "id; DROP TABLE m_client", null);
    }

    @Test
    public void testClientSearchSortOrderRejectsArbitraryValue() {
        // given - direction value outside ASC/DESC should be rejected
        // when/then
        assertRejected(ALLOW_LIST_REJECTION, "displayName", "RANDOM");
    }

    @Test
    public void testClientSearchSortOrderRejectsInjectionAttempt() {
        // when/then
        assertRejected(SQL_VALIDATOR_REJECTION, "displayName", "ASC; DROP TABLE m_client--");
    }

    @Test
    public void testClientSearchOrderByAcceptsAllDocumentedValues() {
        // given - the 4 documented allowlist values from the API docs
        for (String validOrderBy : new String[] { "displayName", "accountNo", "officeId", "officeName" }) {
            // when
            GetClientsResponse response = clientHelper.retrieveClients(ordering(validOrderBy, "ASC"));
            // then
            assertThat(response).isNotNull();
        }
    }

    private static PostClientsClientIdIdentifiersRequest clientIdentifier(final Long documentType) {
        return new PostClientsClientIdIdentifiersRequest().documentTypeId(documentType).documentKey(Utils.randomStringGenerator("ID_", 10))
                .description(Utils.randomStringGenerator("Desc_", 50)).status("Active");
    }

}
