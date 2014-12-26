/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.mifosplatform.integrationtests.common.system.CodeHelper;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class ClientHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String CREATE_CLIENT_URL = "/mifosng-provider/api/v1/clients?" + Utils.TENANT_IDENTIFIER;
    private static final String CLIENT_URL = "/mifosng-provider/api/v1/clients";
    private static final String CLOSE_CLIENT_COMMAND = "close";
    private static final String REACTIVATE_CLIENT_COMMAND = "reactivate";
    private static final String REJECT_CLIENT_COMMAND = "reject";
    private static final String ACTIVATE_CLIENT_COMMAND = "activate";
    private static final String WITHDRAW_CLIENT_COMMAND = "withdraw";

    public static final String CREATED_DATE = "27 November 2014";
    public static final String CREATED_DATE_PLUS_ONE = "28 November 2014";
    public static final String CREATED_DATE_MINUS_ONE = "27 November 2014";
    public static final String TRANSACTION_DATE = "01 March 2013";
    public static final String LAST_TRANSACTION_DATE = "01 March 2013";

    public ClientHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public static Integer createClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return createClient(requestSpec, responseSpec, "04 March 2011");
    }

    public static Integer createClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate) {
        return createClient(requestSpec, responseSpec, activationDate, "1");
    }

    public static Integer createClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate, final String officeId) {
        System.out.println("---------------------------------CREATING A CLIENT---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL, getTestClientAsJSON(activationDate, officeId),
                "clientId");
    }

    public static Integer createClientForAccountPreference(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer clientType, String jsonAttributeToGetBack) {
        final String activationDate = "04 March 2011";
        final String officeId = "1";
        System.out
                .println("---------------------------------CREATING A CLIENT BASED ON ACCOUNT PREFERENCE---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL,
                getTestClientWithClientTypeAsJSON(activationDate, officeId, clientType.toString()), jsonAttributeToGetBack);
    }

    public static Object assignStaffToClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId, final String staffId) {
        final String CLIENT_ASSIGN_STAFF_URL = "/mifosng-provider/api/v1/clients/" + clientId + "?" + Utils.TENANT_IDENTIFIER
                + "&command=assignStaff";

        System.out.println("---------------------------------CREATING A CLIENT---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CLIENT_ASSIGN_STAFF_URL, assignStaffToClientAsJson(staffId), "changes");
    }

    public static Integer getClientsStaffId(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId) {
        return (Integer) getClient(requestSpec, responseSpec, clientId, "staffId");
    }

    public static String getTestClientAsJSON(final String dateOfJoining, final String officeId) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("officeId", officeId);
        map.put("firstname", Utils.randomNameGenerator("Client_FirstName_", 5));
        map.put("lastname", Utils.randomNameGenerator("Client_LastName_", 4));
        map.put("externalId", randomIDGenerator("ID_", 7));
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("active", "true");
        map.put("activationDate", dateOfJoining);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static String getTestClientWithClientTypeAsJSON(final String dateOfJoining, final String officeId, final String clientType) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("officeId", officeId);
        map.put("firstname", Utils.randomNameGenerator("Client_FirstName_", 5));
        map.put("lastname", Utils.randomNameGenerator("Client_LastName_", 4));
        map.put("externalId", randomIDGenerator("ID_", 7));
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("active", "true");
        map.put("activationDate", dateOfJoining);
        map.put("clientTypeId", clientType);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static String assignStaffToClientAsJson(final String staffId) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("staffId", staffId);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static void verifyClientCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedClientID) {
        System.out.println("------------------------------CHECK CLIENT DETAILS------------------------------------\n");
        final String CLIENT_URL = "/mifosng-provider/api/v1/clients/" + generatedClientID + "?" + Utils.TENANT_IDENTIFIER;
        final Integer responseClientID = Utils.performServerGet(requestSpec, responseSpec, CLIENT_URL, "id");
        assertEquals("ERROR IN CREATING THE CLIENT", generatedClientID, responseClientID);
    }

    public static Object getClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final String clientId,
            final String jsonReturn) {
        final String GET_CLIENT_URL = "/mifosng-provider/api/v1/clients/" + clientId + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("---------------------------------GET A CLIENT---------------------------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, GET_CLIENT_URL, jsonReturn);

    }

    /* Client status is a map.So adding SuppressWarnings */
    @SuppressWarnings("unchecked")
    public static HashMap<String, Object> getClientStatus(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId) {
        return (HashMap<String, Object>) getClient(requestSpec, responseSpec, clientId, "status");
    }

    private static String randomIDGenerator(final String prefix, final int lenOfRandomSuffix) {
        return Utils.randomStringGenerator(prefix, lenOfRandomSuffix, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    private String getCloseClientAsJSON() {
        final HashMap<String, String> map = new HashMap<>();

        /* Retrieve Code id for the Code "ClientClosureReason" */
        String codeName = "ClientClosureReason";
        HashMap<String, Object> code = CodeHelper.getCodeByName(this.requestSpec, this.responseSpec, codeName);
        Integer clientClosureCodeId = (Integer) code.get("id");

        /* Retrieve/Create Code Values for the Code "ClientClosureReason" */
        HashMap<String, Object> codeValue = CodeHelper.retrieveOrCreateCodeValue(clientClosureCodeId, this.requestSpec, this.responseSpec);
        Integer closureReasonId = (Integer) codeValue.get("id");

        map.put("closureReasonId", closureReasonId.toString());
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("closureDate", CREATED_DATE_PLUS_ONE);

        String clientJson = new Gson().toJson(map);
        System.out.println(clientJson);
        return clientJson;

    }

    private String getReactivateClientAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("reactivationDate", CREATED_DATE_PLUS_ONE);
        String clientJson = new Gson().toJson(map);
        System.out.println(clientJson);
        return clientJson;

    }

    private String getRejectClientAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        /* Retrieve Code id for the Code "ClientRejectReason" */
        String codeName = "ClientRejectReason";
        HashMap<String, Object> code = CodeHelper.getCodeByName(this.requestSpec, this.responseSpec, codeName);
        Integer clientRejectionReasonCodeId = (Integer) code.get("id");

        /* Retrieve/Create Code Values for the Code "ClientRejectReason" */
        HashMap<String, Object> codeValue = CodeHelper.retrieveOrCreateCodeValue(clientRejectionReasonCodeId, this.requestSpec,
                this.responseSpec);
        Integer rejectionReasonId = (Integer) codeValue.get("id");

        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("rejectionDate", CREATED_DATE_PLUS_ONE);
        map.put("rejectionReasonId", rejectionReasonId.toString());
        String clientJson = new Gson().toJson(map);
        System.out.println(clientJson);
        return clientJson;

    }

    private String getActivateClientAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("activationDate", CREATED_DATE_PLUS_ONE);
        String clientJson = new Gson().toJson(map);
        System.out.println(clientJson);
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

        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("withdrawalDate", CREATED_DATE_PLUS_ONE);
        map.put("withdrawalReasonId", withdrawalReasonId.toString());
        String clientJson = new Gson().toJson(map);
        System.out.println(clientJson);
        return clientJson;

    }

    public HashMap<String, Object> closeClient(final Integer clientId) {
        System.out.println("--------------------------------- CLOSE CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(CLOSE_CLIENT_COMMAND, clientId), getCloseClientAsJSON(), clientId);
    }

    public HashMap<String, Object> reactivateClient(final Integer clientId) {
        System.out.println("--------------------------------- REACTIVATE CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(REACTIVATE_CLIENT_COMMAND, clientId), getReactivateClientAsJSON(), clientId);
    }

    public HashMap<String, Object> rejectClient(final Integer clientId) {
        System.out.println("--------------------------------- REJECT CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(REJECT_CLIENT_COMMAND, clientId), getRejectClientAsJSON(), clientId);
    }

    public HashMap<String, Object> activateClient(final Integer clientId) {
        System.out.println("--------------------------------- ACTIVATE CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(ACTIVATE_CLIENT_COMMAND, clientId), getActivateClientAsJSON(), clientId);
    }

    public HashMap<String, Object> withdrawClient(final Integer clientId) {
        System.out.println("--------------------------------- WITHDRAWN CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(WITHDRAW_CLIENT_COMMAND, clientId), getWithdrawClientAsJSON(), clientId);
    }

    private String createClientOperationURL(final String command, final Integer clientId) {
        return CLIENT_URL + "/" + clientId + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    private HashMap<String, Object> performClientActions(final String postURLForClient, final String jsonToBeSent, final Integer clientId) {
        Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForClient, jsonToBeSent, CommonConstants.RESPONSE_STATUS);
        HashMap<String, Object> response = ClientHelper.getClientStatus(requestSpec, responseSpec, String.valueOf(clientId));

        return response;
    }

}