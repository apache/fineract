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
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.GetClientsClientIdAccountsResponse;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.LoanAccountLockResponseDTO;
import org.apache.fineract.client.models.PostClientsClientIdResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

@Slf4j
@RequiredArgsConstructor
public class ClientHelper {

    private static final String CLIENT_URL = "/fineract-provider/api/v1/clients";
    private static final String CREATE_CLIENT_URL = CLIENT_URL + "?" + Utils.TENANT_IDENTIFIER;
    private static final String CLIENT_EXTERNALID_URL = CLIENT_URL + "/external-id";

    public static final String CLOSE_CLIENT_COMMAND = "close";
    public static final String REACTIVATE_CLIENT_COMMAND = "reactivate";
    public static final String DEFAULT_OFFICE_ID = "1";
    public static final Long LEGALFORM_ID_PERSON = 1L;
    public static final String CREATED_DATE = Utils.getLocalDateOfTenant().minusDays(5).format(Utils.dateFormatter);
    public static final String CREATED_DATE_PLUS_ONE = Utils.getLocalDateOfTenant().minusDays(4).format(Utils.dateFormatter);
    public static final String CREATED_DATE_PLUS_TWO = Utils.getLocalDateOfTenant().minusDays(3).format(Utils.dateFormatter);

    public static final String DEFAULT_DATE = "04 March 2011";

    private static final Gson GSON = new JSON().getGson();

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            PostClientsRequest request) {
        log.info("---------------------------------CREATING A CLIENT---------------------------------------------");
        String requestBody = GSON.toJson(request);
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL, requestBody, "clientId");
    }

    public static PostClientsResponse createClient(final PostClientsRequest request) {
        return Calls.ok(FineractClientHelper.getFineractClient().clients.createClient(request));
    }

    public static GetClientsClientIdAccountsResponse getClientAccounts(final String externalId) {
        return Calls.ok(FineractClientHelper.getFineractClient().clients.retrieveAllClientAccountsByExternalId(externalId));
    }

    public static GetClientsClientIdAccountsResponse getClientAccounts(final long clientId) {
        return FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().clients().retrieveAllClientAccounts(clientId));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return createClient(requestSpec, responseSpec, DEFAULT_DATE);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate) {
        return createClient(requestSpec, responseSpec, activationDate, "1");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate, final String officeId) {
        log.info("---------------------------------CREATING A CLIENT---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL, getTestClientAsJSON(activationDate, officeId),
                "clientId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static PostClientsResponse createClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate, final String officeId, final String externalId) {
        log.info("---------------------------------CREATING A CLIENT---------------------------------------------");
        final String response = Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL,
                getTestClientAsJSON(activationDate, officeId));
        return GSON.fromJson(response, PostClientsResponse.class);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Object assignStaffToClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId, final String staffId) {
        final String CLIENT_ASSIGN_STAFF_URL = "/fineract-provider/api/v1/clients/" + clientId + "?" + Utils.TENANT_IDENTIFIER
                + "&command=assignStaff";

        log.info("---------------------------------CREATING A CLIENT---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CLIENT_ASSIGN_STAFF_URL, assignStaffToClientAsJson(staffId), "changes");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer getClientsStaffId(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId) {
        return (Integer) getClient(requestSpec, responseSpec, clientId, "staffId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static HashMap<String, Object> setInitialClientValues(final String officeId, final Long legalFormId) {
        return setInitialClientValues(officeId, legalFormId, UUID.randomUUID().toString());
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static HashMap<String, Object> setInitialClientValues(final String officeId, final Long legalFormId, final String externalId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("officeId", officeId);
        map.put("legalFormId", legalFormId);
        map.put("firstname", Utils.randomFirstNameGenerator());
        map.put("lastname", Utils.randomLastNameGenerator());
        if (externalId != null) {
            map.put("externalId", externalId);
        }
        map.put("dateFormat", Utils.DATE_FORMAT);
        map.put("locale", "en");
        return map;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getTestClientAsJSON(final String dateOfJoining, final String officeId) {
        HashMap<String, Object> map = setInitialClientValues(officeId, LEGALFORM_ID_PERSON);
        map.put("active", "true");
        map.put("activationDate", dateOfJoining);
        final String testClientAsJson = GSON.toJson(map);
        log.info("TestClient Request :  {}", testClientAsJson);
        return testClientAsJson;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getTestClientAsJSONPending(final String submittedOnDate, final String officeId) {
        HashMap<String, Object> map = setInitialClientValues(officeId, LEGALFORM_ID_PERSON);
        map.put("active", "false");
        map.put("submittedOnDate", submittedOnDate);
        log.info("map :  {}", map);
        return GSON.toJson(map);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getTestPendingClientWithDatatableAsJson(final String registeredTableName) {
        HashMap<String, Object> map = setInitialClientValues("1", LEGALFORM_ID_PERSON);
        map.put("active", "false");
        map.put("submittedOnDate", "04 March 2014");
        String requestJson = getTestDatatableAsJson(map, registeredTableName);
        log.info("map : {}", requestJson);
        return requestJson;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
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

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
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

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
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

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getTestClientWithClientTypeAsJSON(final String dateOfJoining, final String officeId, final String clientType) {
        HashMap<String, Object> map = setInitialClientValues("1", LEGALFORM_ID_PERSON);
        map.put("active", "true");
        map.put("activationDate", dateOfJoining);
        map.put("clientTypeId", clientType);
        log.info("map :  {}", map);
        return GSON.toJson(map);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String assignStaffToClientAsJson(final String staffId) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("staffId", staffId);
        log.info("map :  {}", map);
        return GSON.toJson(map);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static void verifyClientCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer clientId) {
        log.info("------------------------------CHECK CLIENT DETAILS------------------------------------\n");
        final String CLIENT_URL = "/fineract-provider/api/v1/clients/" + clientId + "?" + Utils.TENANT_IDENTIFIER;
        final Integer responseClientID = Utils.performServerGet(requestSpec, responseSpec, CLIENT_URL, "id");
        assertEquals(clientId, responseClientID, "ERROR IN CREATING THE CLIENT");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static GetClientsClientIdResponse getClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final int clientId) {
        String clientResponseStr = (String) getClient(requestSpec, responseSpec, Integer.toString(clientId), null);
        return GSON.fromJson(clientResponseStr, GetClientsClientIdResponse.class);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Object getClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final String clientId,
            final String jsonReturn) {
        final String GET_CLIENT_URL = "/fineract-provider/api/v1/clients/" + clientId + "?" + Utils.TENANT_IDENTIFIER;
        log.info("---------------------------------GET A CLIENT---------------------------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, GET_CLIENT_URL, jsonReturn);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    /* Client status is a map.So adding SuppressWarnings */
    @SuppressWarnings("unchecked")
    public static HashMap<String, Object> getClientStatus(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId) {
        return (HashMap<String, Object>) getClient(requestSpec, responseSpec, clientId, "status");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
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

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public String getReactivateClientAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("reactivationDate", CREATED_DATE_PLUS_ONE);
        String clientJson = GSON.toJson(map);
        log.info("{}", clientJson);
        return clientJson;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public HashMap<String, Object> closeClient(final Integer clientId) {
        log.info("--------------------------------- CLOSE CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(CLOSE_CLIENT_COMMAND, clientId), getCloseClientAsJSON(), clientId);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public HashMap<String, Object> reactivateClient(final Integer clientId) {
        log.info("--------------------------------- REACTIVATE CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(REACTIVATE_CLIENT_COMMAND, clientId), getReactivateClientAsJSON(), clientId);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private String createClientOperationURL(final String command, final Integer clientId) {
        return CLIENT_URL + "/" + clientId + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private HashMap<String, Object> performClientActions(final String postURLForClient, final String jsonToBeSent, final Integer clientId) {
        Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForClient, jsonToBeSent, CommonConstants.RESPONSE_STATUS);
        HashMap<String, Object> response = ClientHelper.getClientStatus(requestSpec, responseSpec, String.valueOf(clientId));

        return response;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static PostClientsClientIdResponse performClientActionUsingExternalId(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String externalId, final String command, final String jsonPayload) {
        final String url = CLIENT_EXTERNALID_URL + "/" + externalId + "?" + Utils.TENANT_IDENTIFIER + "&command=" + command;
        final String response = Utils.performServerPost(requestSpec, responseSpec, url, jsonPayload);
        return GSON.fromJson(response, PostClientsClientIdResponse.class);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Workbook getClientEntityWorkbook(GlobalEntityType clientsEntity, String dateFormat) throws IOException {
        requestSpec.header(HttpHeaders.CONTENT_TYPE, "application/vnd.ms-excel");
        byte[] byteArray = Utils.performGetBinaryResponse(requestSpec, responseSpec, CLIENT_URL + "/downloadtemplate" + "?"
                + Utils.TENANT_IDENTIFIER + "&legalFormType=" + clientsEntity + "&dateFormat=" + dateFormat);
        InputStream inputStream = new ByteArrayInputStream(byteArray);
        Workbook workbook = new HSSFWorkbook(inputStream);
        return workbook;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public String getOutputTemplateLocation(final String importDocumentId) {
        requestSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
        return Utils.performServerOutputTemplateLocationGet(requestSpec, responseSpec,
                "/fineract-provider/api/v1/imports/getOutputTemplateLocation" + "?" + Utils.TENANT_IDENTIFIER, importDocumentId);
    }

    @Deprecated(forRemoval = true)
    public byte[] downloadOutputTemplate(final String importDocumentId) {
        requestSpec.header(HttpHeaders.CONTENT_TYPE, "application/vnd.ms-excel");
        return Utils.performServerOutputTemplateDownloadGet(requestSpec, responseSpec,
                "/fineract-provider/api/v1/imports/downloadOutputTemplate" + "?" + Utils.TENANT_IDENTIFIER, importDocumentId);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public String importClientEntityTemplate(File file) {
        String locale = "en";
        String dateFormat = "dd MMMM yyyy";
        String legalFormType = GlobalEntityType.CLIENTS_ENTITY.toString();
        requestSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA);
        return Utils.performServerTemplatePost(requestSpec, responseSpec, CLIENT_URL + "/uploadtemplate" + "?" + Utils.TENANT_IDENTIFIER,
                legalFormType, file, locale, dateFormat);
    }

    public static PostClientsRequest defaultClientCreationRequest() {
        return new PostClientsRequest().officeId(1L).legalFormId(LEGALFORM_ID_PERSON).firstname(Utils.randomFirstNameGenerator())
                .lastname(Utils.randomLastNameGenerator()).externalId(UUID.randomUUID().toString()).dateFormat(Utils.DATE_FORMAT)
                .locale("en").active(true).activationDate(DEFAULT_DATE);
    }

    public LoanAccountLockResponseDTO retrieveLockedAccounts(int page, int limit) {
        return Calls.ok(FineractClientHelper.getFineractClient().loanAccountLockApi.retrieveLockedAccounts(page, limit));
    }

}
