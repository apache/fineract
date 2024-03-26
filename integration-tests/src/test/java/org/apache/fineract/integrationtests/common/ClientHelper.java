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
package org.apache.fineract.integrationtests.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.ClientTextSearch;
import org.apache.fineract.client.models.DeleteClientsClientIdResponse;
import org.apache.fineract.client.models.GetClientClientIdAddressesResponse;
import org.apache.fineract.client.models.GetClientTransferProposalDateResponse;
import org.apache.fineract.client.models.GetClientsClientIdAccountsResponse;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetClientsClientIdTransactionsResponse;
import org.apache.fineract.client.models.GetClientsClientIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.GetLoanAccountLockResponse;
import org.apache.fineract.client.models.GetObligeeData;
import org.apache.fineract.client.models.PageClientSearchData;
import org.apache.fineract.client.models.PagedRequestClientTextSearch;
import org.apache.fineract.client.models.PostClientClientIdAddressesRequest;
import org.apache.fineract.client.models.PostClientClientIdAddressesResponse;
import org.apache.fineract.client.models.PostClientsClientIdIdentifiersRequest;
import org.apache.fineract.client.models.PostClientsClientIdIdentifiersResponse;
import org.apache.fineract.client.models.PostClientsClientIdResponse;
import org.apache.fineract.client.models.PostClientsClientIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PutClientsClientIdResponse;
import org.apache.fineract.client.models.SortOrder;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

@Slf4j
@RequiredArgsConstructor
public class ClientHelper extends IntegrationTest {

    private static final String CLIENT_URL = "/fineract-provider/api/v1/clients";
    private static final String CREATE_CLIENT_URL = CLIENT_URL + "?" + Utils.TENANT_IDENTIFIER;
    private static final String CLIENT_EXTERNALID_URL = CLIENT_URL + "/external-id";

    public static final String CLOSE_CLIENT_COMMAND = "close";
    public static final String REACTIVATE_CLIENT_COMMAND = "reactivate";
    public static final String REJECT_CLIENT_COMMAND = "reject";
    public static final String ACTIVATE_CLIENT_COMMAND = "activate";
    public static final String WITHDRAW_CLIENT_COMMAND = "withdraw";
    public static final String UNDOREJECT_CLIENT_COMMAND = "undoRejection";
    public static final String UNDOWITHDRAWN_CLIENT_COMMAND = "undoWithdrawal";
    public static final String DEFAULT_OFFICE_ID = "1";
    public static final Long LEGALFORM_ID_PERSON = 1L;
    public static final Long LEGALFORM_ID_ENTITY = 2L;
    public static final String CREATED_DATE = Utils.getLocalDateOfTenant().minusDays(5).format(Utils.dateFormatter);
    public static final String CREATED_DATE_PLUS_ONE = Utils.getLocalDateOfTenant().minusDays(4).format(Utils.dateFormatter);
    public static final String CREATED_DATE_PLUS_TWO = Utils.getLocalDateOfTenant().minusDays(3).format(Utils.dateFormatter);

    public static final String DEFAULT_DATE = "04 March 2011";

    private static final Gson GSON = new JSON().getGson();

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public static Integer createClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            PostClientsRequest request) {
        log.info("---------------------------------CREATING A CLIENT---------------------------------------------");
        String requestBody = GSON.toJson(request);
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL, requestBody, "clientId");
    }

    public PostClientsResponse createClient(final PostClientsRequest request) {
        return ok(fineract().clients.create6(request));
    }

    public PostClientsClientIdIdentifiersResponse createClientIdentifer(final Long clientId,
            final PostClientsClientIdIdentifiersRequest request) {
        return ok(fineract().clientIdentifiers.createClientIdentifier(clientId, request));
    }

    public PageClientSearchData searchClients(String text) {
        ClientTextSearch clientTextSearch = new ClientTextSearch();
        clientTextSearch.setText(text);
        PagedRequestClientTextSearch request = new PagedRequestClientTextSearch();
        request.setRequest(clientTextSearch);
        return searchClients(request);
    }

    public PageClientSearchData searchClients(String text, int page, int pageSize) {
        ClientTextSearch clientTextSearch = new ClientTextSearch();
        clientTextSearch.setText(text);
        PagedRequestClientTextSearch request = new PagedRequestClientTextSearch();
        request.setRequest(clientTextSearch);
        request.setPage(page);
        request.setSize(pageSize);
        return searchClients(request);
    }

    public PageClientSearchData searchClients(String text, SortOrder sortOrder) {
        ClientTextSearch clientTextSearch = new ClientTextSearch();
        clientTextSearch.setText(text);
        PagedRequestClientTextSearch request = new PagedRequestClientTextSearch();
        request.setRequest(clientTextSearch);
        request.setSorts(List.of(sortOrder));
        return searchClients(request);
    }

    public PageClientSearchData searchClients(PagedRequestClientTextSearch request) {
        return ok(fineract().clientSearchV2.searchByText(request));
    }

    public static PostClientsResponse addClientAsPerson(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String jsonPayload) {
        final String response = Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL, jsonPayload);
        log.info("{}", response);
        return GSON.fromJson(response, PostClientsResponse.class);
    }

    public static PutClientsClientIdResponse updateClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String externalId, final String jsonPayload) {
        final String url = CLIENT_EXTERNALID_URL + "/" + externalId + "?" + Utils.TENANT_IDENTIFIER;
        final String response = Utils.performServerPut(requestSpec, responseSpec, url, jsonPayload);
        log.info("{}", response);
        return GSON.fromJson(response, PutClientsClientIdResponse.class);
    }

    public static DeleteClientsClientIdResponse deleteClient(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String externalId) {
        final String url = CLIENT_EXTERNALID_URL + "/" + externalId + "?" + Utils.TENANT_IDENTIFIER;
        final String response = Utils.performServerDelete(requestSpec, responseSpec, url, Utils.emptyJson(), null);
        log.info("{}", response);
        return GSON.fromJson(response, DeleteClientsClientIdResponse.class);
    }

    public static GetClientsClientIdAccountsResponse getClientAccounts(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String externalId) {
        final String url = CLIENT_EXTERNALID_URL + "/" + externalId + "/accounts?" + Utils.TENANT_IDENTIFIER;
        final String response = Utils.performServerGet(requestSpec, responseSpec, url);
        log.info("{}", response);
        return GSON.fromJson(response, GetClientsClientIdAccountsResponse.class);
    }

    public static GetClientTransferProposalDateResponse getProposedTransferDate(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String externalId) {
        final String url = CLIENT_EXTERNALID_URL + "/" + externalId + "/transferproposaldate?" + Utils.TENANT_IDENTIFIER;
        final String response = Utils.performServerGet(requestSpec, responseSpec, url);
        log.info("{}", response);
        return GSON.fromJson(response, GetClientTransferProposalDateResponse.class);
    }

    public static List<GetObligeeData> getObligeeData(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String externalId) {
        final String url = CLIENT_EXTERNALID_URL + "/" + externalId + "/obligeedetails?" + Utils.TENANT_IDENTIFIER;
        final String response = Utils.performServerGet(requestSpec, responseSpec, url);
        log.info("{}", response);
        return GSON.fromJson(response, new TypeToken<List<GetObligeeData>>() {}.getType());
    }

    public static Integer createClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return createClient(requestSpec, responseSpec, DEFAULT_DATE);
    }

    public static Integer createClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate) {
        return createClient(requestSpec, responseSpec, activationDate, "1");
    }

    public static Integer createClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate, final String officeId) {
        log.info("---------------------------------CREATING A CLIENT---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL, getTestClientAsJSON(activationDate, officeId),
                "clientId");
    }

    public static PostClientsResponse createClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate, final String officeId, final String externalId) {
        log.info("---------------------------------CREATING A CLIENT---------------------------------------------");
        final String response = Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL,
                getTestClientAsJSON(activationDate, officeId));
        return GSON.fromJson(response, PostClientsResponse.class);
    }

    public static PostClientClientIdAddressesResponse createClientAddress(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, long clientId, long addressTypeId, PostClientClientIdAddressesRequest request) {
        final String CREATE_CLIENT_ADDRESS_URL = "/fineract-provider/api/v1/client/" + clientId + "/addresses?type=" + addressTypeId + "&"
                + Utils.TENANT_IDENTIFIER;
        log.info("---------------------------------CREATING A CLIENT ADDRESS ---------------------------------------------");
        String requestBody = GSON.toJson(request);
        String response = Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_ADDRESS_URL, requestBody);
        return GSON.fromJson(response, PostClientClientIdAddressesResponse.class);
    }

    public static Integer createClientPending(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return createClientPending(requestSpec, responseSpec, "04 March 2014");
    }

    public static Integer createClientPending(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String submittedOnDate) {
        return createClientPending(requestSpec, responseSpec, submittedOnDate, "1");
    }

    public static Integer createClientPending(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String submittedOnDate, final String officeId) {
        log.info("---------------------------------CREATING A CLIENT IN PENDING---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL, getTestClientAsJSONPending(submittedOnDate, officeId),
                "clientId");
    }

    public Object createClientPendingWithError(final String jsonAttributeToGetBack) {
        log.info("---------------------------------CREATING A CLIENT IN PENDING WITH ERROR---------------------------------------------");
        return Utils.performServerPost(this.requestSpec, this.responseSpec, CREATE_CLIENT_URL,
                getTestClientAsJSONPending("04 March 2014", "1"), jsonAttributeToGetBack);
    }

    public static Integer createClientPendingWithDatatable(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String registeredTableName) {
        log.info("-------------------------- CREATING A CLIENT IN PENDING WITH DATATABLES --------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL,
                getTestPendingClientWithDatatableAsJson(registeredTableName), "clientId");
    }

    public static Integer createClientAsPerson(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return createClientAsPerson(requestSpec, responseSpec, DEFAULT_DATE);
    }

    public static Integer createClientAsPerson(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate) {
        return createClientAsPerson(requestSpec, responseSpec, activationDate, "1");
    }

    public static Integer createClientAsPerson(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate, final String officeId) {

        log.info(
                "---------------------------------CREATING A CLIENT NON PERSON(ORGANISATION)---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL, getTestClientAsJSON(activationDate, officeId),
                "clientId");
    }

    public static PostClientsResponse createClientAsPersonWithDatatable(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String activationDate, final String officeId,
            final HashMap<String, Object> datatables) {
        log.info("---------------------------------CREATING A CLIENT PERSON WITH DATATABLE---------------------------------------------");
        final String response = Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL,
                getTestPersonClientAsJSON(activationDate, officeId, datatables), null);
        return GSON.fromJson(response, PostClientsResponse.class);
    }

    public static Integer createClientAsEntity(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return createClientAsEntity(requestSpec, responseSpec, DEFAULT_DATE);
    }

    public static Integer createClientAsEntity(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate) {
        return createClientAsEntity(requestSpec, responseSpec, activationDate, "1");
    }

    public static Integer createClientAsEntity(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate, final String officeId) {

        Integer constitutionCodeId = (Integer) CodeHelper.getCodeByName(requestSpec, responseSpec, "Constitution").get("id");
        Integer soleProprietorCodeValueId = (Integer) CodeHelper.retrieveOrCreateCodeValue(constitutionCodeId, requestSpec, responseSpec)
                .get("id");

        log.info(
                "---------------------------------CREATING A CLIENT NON PERSON(ORGANISATION)---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL,
                getTestEntityClientAsJSON(activationDate, officeId, soleProprietorCodeValueId), "clientId");
    }

    public static Integer createClientForAccountPreference(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer clientType, String jsonAttributeToGetBack) {
        final String activationDate = DEFAULT_DATE;
        final String officeId = "1";
        log.info(
                "---------------------------------CREATING A CLIENT BASED ON ACCOUNT PREFERENCE---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL,
                getTestClientWithClientTypeAsJSON(activationDate, officeId, clientType.toString()), jsonAttributeToGetBack);
    }

    public static Object assignStaffToClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId, final String staffId) {
        final String CLIENT_ASSIGN_STAFF_URL = "/fineract-provider/api/v1/clients/" + clientId + "?" + Utils.TENANT_IDENTIFIER
                + "&command=assignStaff";

        log.info("---------------------------------CREATING A CLIENT---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CLIENT_ASSIGN_STAFF_URL, assignStaffToClientAsJson(staffId), "changes");
    }

    public static Integer getClientsStaffId(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId) {
        return (Integer) getClient(requestSpec, responseSpec, clientId, "staffId");
    }

    public static HashMap<String, Object> setInitialClientValues(final String officeId, final Long legalFormId) {
        return setInitialClientValues(officeId, legalFormId, UUID.randomUUID().toString());
    }

    public static HashMap<String, Object> setInitialClientValues(final String officeId, final Long legalFormId, final String externalId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("officeId", officeId);
        map.put("legalFormId", legalFormId);
        map.put("firstname", Utils.randomStringGenerator("Client_FirstName_", 5));
        map.put("lastname", Utils.randomStringGenerator("Client_LastName_", 4));
        if (externalId != null) {
            map.put("externalId", externalId);
        }
        map.put("dateFormat", Utils.DATE_FORMAT);
        map.put("locale", "en");
        return map;
    }

    public static String getBasicClientAsJSON(final String officeId, final Long legalFormId, final String externalId) {
        HashMap<String, Object> map = setInitialClientValues(officeId, legalFormId, externalId);
        map.put("active", "true");
        map.put("activationDate", DEFAULT_DATE);
        final String basicClientAsJson = GSON.toJson(map);
        log.info("Client JSON :  {}", basicClientAsJson);
        return basicClientAsJson;
    }

    public static String getTestClientAsJSON(final String dateOfJoining, final String officeId) {
        HashMap<String, Object> map = setInitialClientValues(officeId, LEGALFORM_ID_PERSON);
        map.put("active", "true");
        map.put("activationDate", dateOfJoining);
        final String testClientAsJson = GSON.toJson(map);
        log.info("TestClient Request :  {}", testClientAsJson);
        return testClientAsJson;
    }

    public static String getTestClientAsJSONPending(final String submittedOnDate, final String officeId) {
        HashMap<String, Object> map = setInitialClientValues(officeId, LEGALFORM_ID_PERSON);
        map.put("active", "false");
        map.put("submittedOnDate", submittedOnDate);
        log.info("map :  {}", map);
        return GSON.toJson(map);
    }

    public static String getTestPendingClientWithDatatableAsJson(final String registeredTableName) {
        HashMap<String, Object> map = setInitialClientValues("1", LEGALFORM_ID_PERSON);
        map.put("active", "false");
        map.put("submittedOnDate", "04 March 2014");
        String requestJson = getTestDatatableAsJson(map, registeredTableName);
        log.info("map : {}", requestJson);
        return requestJson;
    }

    public static String getTestDatatableAsJson(HashMap<String, Object> map, final String registeredTableName) {
        List<HashMap<String, Object>> datatablesListMap = new ArrayList<>();
        HashMap<String, Object> datatableMap = new HashMap<>();
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("locale", "en");
        dataMap.put("Spouse Name", Utils.randomStringGenerator("Spouse_name", 4));
        dataMap.put("Number of Dependents", 5);
        dataMap.put("Time of Visit", "01 December 2016 04:03");
        dataMap.put("dateFormat", Utils.DATE_TIME_FORMAT);
        dataMap.put("Date of Approval", "02 December 2016 00:00");
        datatableMap.put("registeredTableName", registeredTableName);
        datatableMap.put("data", dataMap);
        datatablesListMap.add(datatableMap);
        map.put("datatables", datatablesListMap);
        return GSON.toJson(map);
    }

    public static String getTestPersonClientAsJSON(final String dateOfJoining, final String officeId) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("officeId", officeId);
        map.put("fullname", Utils.randomStringGenerator("Client_FullName_", 5));
        map.put("externalId", UUID.randomUUID().toString());
        map.put("dateFormat", Utils.DATE_FORMAT);
        map.put("locale", "en");
        map.put("active", "true");
        map.put("activationDate", dateOfJoining);
        map.put("legalFormId", 1);

        log.info("map :  {}", map);
        return GSON.toJson(map);
    }

    public static String getTestPersonClientAsJSON(final String dateOfJoining, final String officeId,
            final HashMap<String, Object> datatables) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("officeId", officeId);
        map.put("fullname", Utils.randomStringGenerator("Client_FullName_", 5));
        map.put("externalId", UUID.randomUUID().toString());
        map.put("dateFormat", Utils.DATE_FORMAT);
        map.put("locale", "en");
        map.put("active", "true");
        map.put("activationDate", dateOfJoining);
        map.put("legalFormId", 1);
        if (datatables != null) {
            map.put("datatables", Arrays.asList(datatables));
        }

        log.info("map :  {}", map);
        return GSON.toJson(map);
    }

    public static String getTestEntityClientAsJSON(final String dateOfJoining, final String officeId,
            final Integer soleProprietorCodeValueId) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("officeId", officeId);
        map.put("fullname", Utils.randomStringGenerator("Client_FullName_", 5));
        map.put("externalId", UUID.randomUUID().toString());
        map.put("dateFormat", Utils.DATE_FORMAT);
        map.put("locale", "en");
        map.put("active", "true");
        map.put("activationDate", dateOfJoining);
        map.put("legalFormId", LEGALFORM_ID_ENTITY);

        final HashMap<String, Object> clientNonPersonMap = new HashMap<>();
        clientNonPersonMap.put("constitutionId", soleProprietorCodeValueId);
        map.put("clientNonPersonDetails", clientNonPersonMap);

        log.info("map :  {}", map);
        return GSON.toJson(map);
    }

    public static String getTestClientWithClientTypeAsJSON(final String dateOfJoining, final String officeId, final String clientType) {
        HashMap<String, Object> map = setInitialClientValues("1", LEGALFORM_ID_PERSON);
        map.put("active", "true");
        map.put("activationDate", dateOfJoining);
        map.put("clientTypeId", clientType);
        log.info("map :  {}", map);
        return GSON.toJson(map);
    }

    public static String assignStaffToClientAsJson(final String staffId) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("staffId", staffId);
        log.info("map :  {}", map);
        return GSON.toJson(map);
    }

    public static void verifyClientCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer clientId) {
        log.info("------------------------------CHECK CLIENT DETAILS------------------------------------\n");
        final String CLIENT_URL = "/fineract-provider/api/v1/clients/" + clientId + "?" + Utils.TENANT_IDENTIFIER;
        final Integer responseClientID = Utils.performServerGet(requestSpec, responseSpec, CLIENT_URL, "id");
        assertEquals(clientId, responseClientID, "ERROR IN CREATING THE CLIENT");
    }

    public static GetClientsClientIdResponse getClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final int clientId) {
        String clientResponseStr = (String) getClient(requestSpec, responseSpec, Integer.toString(clientId), null);
        return GSON.fromJson(clientResponseStr, GetClientsClientIdResponse.class);
    }

    public static GetClientsClientIdResponse getClientByExternalId(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String externalId) {
        final String url = CLIENT_EXTERNALID_URL + "/" + externalId + "?" + Utils.TENANT_IDENTIFIER;
        final String response = Utils.performServerGet(requestSpec, responseSpec, url);
        return GSON.fromJson(response, GetClientsClientIdResponse.class);
    }

    public static List<GetClientClientIdAddressesResponse> getClientAddresses(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final int clientId) {
        final String GET_CLIENT_ADDRESSES_URL = "/fineract-provider/api/v1/client/" + clientId + "/addresses?" + Utils.TENANT_IDENTIFIER;
        log.info("---------------------------------GET A CLIENT'S ADDRESSES ---------------------------------------------");
        String clientResponseStr = Utils.performServerGet(requestSpec, responseSpec, GET_CLIENT_ADDRESSES_URL);
        return GSON.fromJson(clientResponseStr, new TypeToken<List<GetClientClientIdAddressesResponse>>() {}.getType());
    }

    public static Object getClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final String clientId,
            final String jsonReturn) {
        final String GET_CLIENT_URL = "/fineract-provider/api/v1/clients/" + clientId + "?" + Utils.TENANT_IDENTIFIER;
        log.info("---------------------------------GET A CLIENT---------------------------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, GET_CLIENT_URL, jsonReturn);
    }

    public static HashMap<String, Object> getClientAuditFields(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer clientId, final String jsonReturn) {
        final String GET_CLIENT_URL = "/fineract-provider/api/v1/internal/client/" + clientId + "/audit?" + Utils.TENANT_IDENTIFIER;
        log.info("---------------------------------GET A CLIENT ENTITY AUDIT FIELDS---------------------------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, GET_CLIENT_URL, jsonReturn);
    }

    /* Client status is a map.So adding SuppressWarnings */
    @SuppressWarnings("unchecked")
    public static HashMap<String, Object> getClientStatus(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId) {
        return (HashMap<String, Object>) getClient(requestSpec, responseSpec, clientId, "status");
    }

    public String getCloseClientAsJSON() {
        final HashMap<String, String> map = new HashMap<>();

        /* Retrieve Code id for the Code "ClientClosureReason" */
        String codeName = "ClientClosureReason";
        HashMap<String, Object> code = CodeHelper.getCodeByName(this.requestSpec, this.responseSpec, codeName);
        Integer clientClosureCodeId = (Integer) code.get("id");

        /* Retrieve/Create Code Values for the Code "ClientClosureReason" */
        HashMap<String, Object> codeValue = CodeHelper.retrieveOrCreateCodeValue(clientClosureCodeId, this.requestSpec, this.responseSpec);
        Integer closureReasonId = (Integer) codeValue.get("id");

        map.put("closureReasonId", closureReasonId.toString());
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("closureDate", CREATED_DATE_PLUS_ONE);

        String clientJson = GSON.toJson(map);
        log.info("{}", clientJson);
        return clientJson;
    }

    public String getReactivateClientAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("reactivationDate", CREATED_DATE_PLUS_ONE);
        String clientJson = GSON.toJson(map);
        log.info("{}", clientJson);
        return clientJson;
    }

    private String getUndoRejectClientAsJSON(final String date) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("reopenedDate", date);
        String clientJson = GSON.toJson(map);
        log.info("{}", clientJson);
        return clientJson;

    }

    private String getUndoWithdrawnClientAsJSON(final String date) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("reopenedDate", date);
        String clientJson = GSON.toJson(map);
        log.info("{}", clientJson);
        return clientJson;

    }

    public String getRejectClientAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        /* Retrieve Code id for the Code "ClientRejectReason" */
        String codeName = "ClientRejectReason";
        HashMap<String, Object> code = CodeHelper.getCodeByName(this.requestSpec, this.responseSpec, codeName);
        Integer clientRejectionReasonCodeId = (Integer) code.get("id");

        /* Retrieve/Create Code Values for the Code "ClientRejectReason" */
        HashMap<String, Object> codeValue = CodeHelper.retrieveOrCreateCodeValue(clientRejectionReasonCodeId, this.requestSpec,
                this.responseSpec);
        Integer rejectionReasonId = (Integer) codeValue.get("id");

        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("rejectionDate", CREATED_DATE_PLUS_ONE);
        map.put("rejectionReasonId", rejectionReasonId.toString());
        String clientJson = GSON.toJson(map);
        log.info("{}", clientJson);
        return clientJson;

    }

    public static String getActivateClientAsJSON(String date) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("activationDate", date);
        String clientJson = GSON.toJson(map);
        log.info("{}", clientJson);
        return clientJson;
    }

    private String getWithdrawClientAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        /* Retrieve Code id for the Code "ClientWithdrawReason" */
        String codeName = "ClientWithdrawReason";
        HashMap<String, Object> code = CodeHelper.getCodeByName(this.requestSpec, this.responseSpec, codeName);
        Integer clientWithdrawReasonCodeId = (Integer) code.get("id");

        /* Retrieve/Create Code Values for the Code "ClientWithdrawReason" */
        HashMap<String, Object> codeValue = CodeHelper.retrieveOrCreateCodeValue(clientWithdrawReasonCodeId, this.requestSpec,
                this.responseSpec);
        Integer withdrawalReasonId = (Integer) codeValue.get("id");

        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("withdrawalDate", CREATED_DATE_PLUS_ONE);
        map.put("withdrawalReasonId", withdrawalReasonId.toString());
        String clientJson = GSON.toJson(map);
        log.info(clientJson);
        return clientJson;
    }

    public static String getSpecifiedDueDateChargesClientAsJSON(final String chargeId, final String dueDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en_GB");
        map.put("dateFormat", Utils.DATE_FORMAT);
        map.put("dueDate", dueDate);
        map.put("chargeId", chargeId);
        map.put("amount", "200");
        String json = GSON.toJson(map);
        return json;
    }

    public static String getPayChargeJSON(final String date, String amount) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en_GB");
        map.put("dateFormat", Utils.DATE_FORMAT);
        map.put("transactionDate", date);
        map.put("amount", amount);
        String json = GSON.toJson(map);
        log.info("{}", json);
        return json;
    }

    public static String getPayChargeJSONWithExternalId(final String date, String amount, String externalId) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en_GB");
        map.put("dateFormat", Utils.DATE_FORMAT);
        map.put("transactionDate", date);
        map.put("amount", amount);
        map.put("externalId", externalId);
        String json = GSON.toJson(map);
        log.info("{}", json);
        return json;
    }

    public static String getWaiveChargeJSON(final String amount, String clientChargeId) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en_GB");
        map.put("amount", amount);
        map.put("clientChargeId", clientChargeId);
        String json = GSON.toJson(map);
        log.info("{}", json);
        return json;
    }

    public HashMap<String, Object> closeClient(final Integer clientId) {
        log.info("--------------------------------- CLOSE CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(CLOSE_CLIENT_COMMAND, clientId), getCloseClientAsJSON(), clientId);
    }

    public HashMap<String, Object> reactivateClient(final Integer clientId) {
        log.info("--------------------------------- REACTIVATE CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(REACTIVATE_CLIENT_COMMAND, clientId), getReactivateClientAsJSON(), clientId);
    }

    public HashMap<String, Object> rejectClient(final Integer clientId) {
        log.info("--------------------------------- REJECT CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(REJECT_CLIENT_COMMAND, clientId), getRejectClientAsJSON(), clientId);
    }

    public HashMap<String, Object> activateClient(final Integer clientId) {
        log.info("--------------------------------- ACTIVATE CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(ACTIVATE_CLIENT_COMMAND, clientId),
                getActivateClientAsJSON(CREATED_DATE_PLUS_ONE), clientId);
    }

    public HashMap<String, Object> withdrawClient(final Integer clientId) {
        log.info("--------------------------------- WITHDRAWN CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(WITHDRAW_CLIENT_COMMAND, clientId), getWithdrawClientAsJSON(), clientId);
    }

    private String createClientOperationURL(final String command, final Integer clientId) {
        return CLIENT_URL + "/" + clientId + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    public HashMap<String, Object> undoReject(final Integer clientId) {
        log.info("--------------------------------- UNDO REJECT CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(UNDOREJECT_CLIENT_COMMAND, clientId),
                getUndoRejectClientAsJSON(CREATED_DATE_PLUS_TWO), clientId);
    }

    public HashMap<String, Object> undoWithdrawn(final Integer clientId) {
        log.info("--------------------------------- UNDO WITHDRAWN CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(UNDOWITHDRAWN_CLIENT_COMMAND, clientId),
                getUndoWithdrawnClientAsJSON(CREATED_DATE_PLUS_TWO), clientId);
    }

    public ArrayList<HashMap<String, Object>> undoRejectedclient(final Integer clientId, final String jsonAttributeToGetBack,
            final String rejectedDate) {
        log.info("----------------------------------UNDO REJECT CLIENT ----------------------------------");
        return performClientActionsWithValidationErrors(createClientOperationURL(UNDOREJECT_CLIENT_COMMAND, clientId),
                getUndoRejectClientAsJSON(rejectedDate), jsonAttributeToGetBack);
    }

    public ArrayList<HashMap<String, Object>> undoWithdrawclient(final Integer clientId, final String jsonAttributeToGetBack,
            final String rejectedDate) {
        log.info("----------------------------------UNDO WITHDRAW CLIENT ----------------------------------");
        return performClientActionsWithValidationErrors(createClientOperationURL(UNDOWITHDRAWN_CLIENT_COMMAND, clientId),
                getUndoWithdrawnClientAsJSON(rejectedDate), jsonAttributeToGetBack);
    }

    public ArrayList<HashMap<String, Object>> activateClient(final Integer clientId, final String jsonAttributeToGetBack) {
        log.info("--------------------------------- ACTIVATE CLIENT -------------------------------");
        return performClientActionsWithValidationErrors(createClientOperationURL(ACTIVATE_CLIENT_COMMAND, clientId),
                getActivateClientAsJSON(CREATED_DATE_PLUS_ONE), jsonAttributeToGetBack);
    }

    public HashMap<String, Object> activateClientWithDiffDateOption(final Integer clientId, final String activationDate) {
        log.info("--------------------------------- ACTIVATE CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(ACTIVATE_CLIENT_COMMAND, clientId), getActivateClientAsJSON(activationDate),
                clientId);
    }

    private ArrayList<HashMap<String, Object>> performClientActionsWithValidationErrors(final String postURLForClient,
            final String jsonToBeSent, final String jsonAttributeToGetBack) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForClient, jsonToBeSent, jsonAttributeToGetBack);
    }

    private HashMap<String, Object> performClientActions(final String postURLForClient, final String jsonToBeSent, final Integer clientId) {
        Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForClient, jsonToBeSent, CommonConstants.RESPONSE_STATUS);
        HashMap<String, Object> response = ClientHelper.getClientStatus(requestSpec, responseSpec, String.valueOf(clientId));

        return response;
    }

    public static PostClientsClientIdResponse performClientActionUsingExternalId(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String externalId, final String command, final String jsonPayload) {
        final String url = CLIENT_EXTERNALID_URL + "/" + externalId + "?" + Utils.TENANT_IDENTIFIER + "&command=" + command;
        final String response = Utils.performServerPost(requestSpec, responseSpec, url, jsonPayload);
        return GSON.fromJson(response, PostClientsClientIdResponse.class);
    }

    public static Integer addChargesForClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer clientId, final String request) {
        log.info("--------------------------------- ADD CHARGES FOR Client --------------------------------");
        final String ADD_CHARGES_URL = "/fineract-provider/api/v1/clients/" + clientId + "/charges?" + Utils.TENANT_IDENTIFIER;
        final HashMap<?, ?> response = Utils.performServerPost(requestSpec, responseSpec, ADD_CHARGES_URL, request, "");
        return (Integer) response.get("resourceId");
    }

    public static String payChargesForClients(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer clientId, final Integer clientChargeId, final String json) {
        log.info("--------------------------------- PAY CHARGES FOR CLIENT --------------------------------");
        final String CHARGES_URL = "/fineract-provider/api/v1/clients/" + clientId + "/charges/" + clientChargeId + "?command=paycharge&"
                + Utils.TENANT_IDENTIFIER;
        final HashMap<?, ?> response = Utils.performServerPost(requestSpec, responseSpec, CHARGES_URL, json, "");
        return response.get("transactionId") != null ? response.get("transactionId").toString() : null;
    }

    public static String payChargesForClientsTransactionExternalId(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer clientId, final Integer clientChargeId, final String json) {
        log.info("--------------------------------- PAY CHARGES FOR CLIENT --------------------------------");
        final String CHARGES_URL = "/fineract-provider/api/v1/clients/" + clientId + "/charges/" + clientChargeId + "?command=paycharge&"
                + Utils.TENANT_IDENTIFIER;
        final HashMap<?, ?> response = Utils.performServerPost(requestSpec, responseSpec, CHARGES_URL, json, "");
        return response.get("subResourceExternalId") != null ? response.get("subResourceExternalId").toString() : null;
    }

    public static String waiveChargesForClients(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer clientId, final Integer clientChargeId, final String json) {
        log.info("--------------------------------- WAIVE CHARGES FOR CLIENT --------------------------------");
        final String CHARGES_URL = "/fineract-provider/api/v1/clients/" + clientId + "/charges/" + clientChargeId + "?command=waive&"
                + Utils.TENANT_IDENTIFIER;

        final HashMap<?, ?> response = Utils.performServerPost(requestSpec, responseSpec, CHARGES_URL, json, "");
        return response.get("transactionId").toString();
    }

    public static Integer revertClientChargeTransaction(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId, String clientChargeId) {
        log.info("---------------------------------UNDO TRANSACTION---------------------------------------------");
        final String CHARGES_URL = "/fineract-provider/api/v1/clients/" + clientId + "/transactions/" + clientChargeId + "?command=undo&"
                + Utils.TENANT_IDENTIFIER;

        final HashMap<?, ?> response = Utils.performServerPost(requestSpec, responseSpec, CHARGES_URL, "", "");
        return (Integer) response.get("resourceId");

    }

    public static Object getClientCharge(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId, final String clientChargeId) {
        log.info("---------------------------------GET CLIENT CHARGE---------------------------------------------");
        final String CHARGES_URL = "/fineract-provider/api/v1/clients/" + clientId + "/charges/" + clientChargeId + "?"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, CHARGES_URL, "amountOutstanding");
    }

    public static Boolean getClientTransactions(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId, final String transactionId) {
        log.info("---------------------------------GET CLIENT CHARGE TRANSACTIONS---------------------------------------------");
        final String CHARGES_URL = "/fineract-provider/api/v1/clients/" + clientId + "/transactions/" + transactionId + "?"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, CHARGES_URL, "reversed");
    }

    public GetClientsClientIdTransactionsResponse getAllClientTransactionsByExternalId(final String externalId) {
        return ok(fineract().clientTransactions.retrieveAllClientTransactions1(externalId, 0, 100));
    }

    public GetClientsClientIdTransactionsTransactionIdResponse getClientTransactionByExternalId(final String externalId,
            final String transactionId) {
        return ok(fineract().clientTransactions.retrieveClientTransaction2(externalId, Long.parseLong(transactionId)));
    }

    public GetClientsClientIdTransactionsTransactionIdResponse getClientTransactionByTransactionExternalId(final Long clientId,
            final String transactionExternalId) {
        return ok(fineract().clientTransactions.retrieveClientTransaction1(clientId, transactionExternalId));
    }

    public PostClientsClientIdTransactionsTransactionIdResponse undoClientTransactionByExternalId(final String externalId,
            final String transactionId) {
        return ok(fineract().clientTransactions.undoClientTransaction2(externalId, Long.parseLong(transactionId), "undo"));
    }

    public PostClientsClientIdTransactionsTransactionIdResponse undoClientTransactionByTransactionExternalId(final Long clientId,
            final String transactionExternalId) {
        return ok(fineract().clientTransactions.undoClientTransaction1(clientId, transactionExternalId, "undo"));
    }

    public Workbook getClientEntityWorkbook(GlobalEntityType clientsEntity, String dateFormat) throws IOException {
        requestSpec.header(HttpHeaders.CONTENT_TYPE, "application/vnd.ms-excel");
        byte[] byteArray = Utils.performGetBinaryResponse(requestSpec, responseSpec, CLIENT_URL + "/downloadtemplate" + "?"
                + Utils.TENANT_IDENTIFIER + "&legalFormType=" + clientsEntity + "&dateFormat=" + dateFormat);
        InputStream inputStream = new ByteArrayInputStream(byteArray);
        Workbook workbook = new HSSFWorkbook(inputStream);
        return workbook;
    }

    public String getOutputTemplateLocation(final String importDocumentId) {
        requestSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
        return Utils.performServerOutputTemplateLocationGet(requestSpec, responseSpec,
                "/fineract-provider/api/v1/imports/getOutputTemplateLocation" + "?" + Utils.TENANT_IDENTIFIER, importDocumentId);
    }

    public String importClientEntityTemplate(File file) {
        String locale = "en";
        String dateFormat = "dd MMMM yyyy";
        String legalFormType = GlobalEntityType.CLIENTS_ENTITY.toString();
        requestSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA);
        return Utils.performServerTemplatePost(requestSpec, responseSpec, CLIENT_URL + "/uploadtemplate" + "?" + Utils.TENANT_IDENTIFIER,
                legalFormType, file, locale, dateFormat);
    }

    public List getClientWithStatus(final int limit, final String status) {
        final String URL = "/fineract-provider/api/v1/clients?paged=true&status=" + status + "&limit=" + Integer.toString(limit) + "&"
                + Utils.TENANT_IDENTIFIER;
        LinkedHashMap responseClients = Utils.performServerGet(requestSpec, responseSpec, URL, "");
        return (List) responseClients.get("pageItems");
    }

    public static PostClientsRequest defaultClientCreationRequest() {
        return new PostClientsRequest().officeId(1L).legalFormId(LEGALFORM_ID_PERSON)
                .firstname(Utils.randomStringGenerator("Client_FirstName_", 5)).lastname(Utils.randomStringGenerator("Client_LastName_", 5))
                .externalId(UUID.randomUUID().toString()).dateFormat(Utils.DATE_FORMAT).locale("en").active(true)
                .activationDate(DEFAULT_DATE);
    }

    public GetLoanAccountLockResponse retrieveLockedAccounts(int page, int limit) {
        return ok(fineract().loanAccountLockApi.retrieveLockedAccounts(page, limit));
    }

    public static PostClientsClientIdIdentifiersRequest createClientIdentifer(final Long documentType) {
        return new PostClientsClientIdIdentifiersRequest().documentTypeId(documentType).documentKey(Utils.randomStringGenerator("ID_", 10))
                .description(Utils.randomStringGenerator("Desc_", 50)).status("Active");
    }

}
