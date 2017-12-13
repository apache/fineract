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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.integrationtests.common.system.CodeHelper;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

public class ClientHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String CREATE_CLIENT_URL = "/fineract-provider/api/v1/clients?" + Utils.TENANT_IDENTIFIER;
    private static final String CLIENT_URL = "/fineract-provider/api/v1/clients";
    private static final String CLOSE_CLIENT_COMMAND = "close";
    private static final String REACTIVATE_CLIENT_COMMAND = "reactivate";
    private static final String REJECT_CLIENT_COMMAND = "reject";
    private static final String ACTIVATE_CLIENT_COMMAND = "activate";
    private static final String WITHDRAW_CLIENT_COMMAND = "withdraw";
    private static final String UNDOREJECT_CLIENT_COMMAND = "undoRejection";
    private static final String UNDOWITHDRAWN_CLIENT_COMMAND = "undoWithdrawal";

    public static final String CREATED_DATE = "27 November 2014";
    public static final String CREATED_DATE_PLUS_ONE = "28 November 2014";
    public static final String CREATED_DATE_PLUS_TWO = "29 November 2014";

    public static final String CREATED_DATE_MINUS_ONE = "27 November 2014";
    public static final String TRANSACTION_DATE = "01 March 2013";
    public static final String LAST_TRANSACTION_DATE = "01 March 2013";
    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DATE_TIME_FORMAT = "dd MMMM yyyy HH:mm";

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
    
    
    
    public static Integer createClientPending(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return createClientPending(requestSpec, responseSpec, "04 March 2014");
    }

    public static Integer createClientPending(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String submittedOnDate) {
        return createClientPending(requestSpec, responseSpec, submittedOnDate, "1");
    }

    public static Integer createClientPending(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String submittedOnDate, final String officeId) {
        System.out.println("---------------------------------CREATING A CLIENT IN PENDING---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL, getTestClientAsJSONPending(submittedOnDate, officeId),
                "clientId");
    }

    public Object createClientPendingWithError(final String jsonAttributeToGetBack) {
        System.out.println("---------------------------------CREATING A CLIENT IN PENDING WITH ERROR---------------------------------------------");
        return Utils.performServerPost(this.requestSpec, this.responseSpec, CREATE_CLIENT_URL,
                getTestClientAsJSONPending("04 March 2014", "1"), jsonAttributeToGetBack);
    }

    public static Integer createClientPendingWithDatatable(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String registeredTableName) {
        System.out.println("-------------------------- CREATING A CLIENT IN PENDING WITH DATATABLES --------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL,
                getTestPendingClientWithDatatableAsJson(registeredTableName), "clientId");
    }

    public static Integer createClientAsPerson(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return createClientAsPerson(requestSpec, responseSpec, "04 March 2011");
    }

    public static Integer createClientAsPerson(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate) {
        return createClientAsPerson(requestSpec, responseSpec, activationDate, "1");
    }

    public static Integer createClientAsPerson(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate, final String officeId) {
    	
        System.out.println("---------------------------------CREATING A CLIENT NON PERSON(ORGANISATION)---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL, getTestPersonClientAsJSON(activationDate, officeId),
                "clientId");
    }
    
    public static Integer createClientAsEntity(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return createClientAsEntity(requestSpec, responseSpec, "04 March 2011");
    }

    public static Integer createClientAsEntity(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate) {
        return createClientAsEntity(requestSpec, responseSpec, activationDate, "1");
    }

    public static Integer createClientAsEntity(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate, final String officeId) {
    	
    	Integer constitutionCodeId = (Integer) CodeHelper.getCodeByName(requestSpec, responseSpec, "Constitution").get("id");
    	Integer soleProprietorCodeValueId = (Integer) CodeHelper.retrieveOrCreateCodeValue(constitutionCodeId, requestSpec, responseSpec).get("id");
    	
        System.out.println("---------------------------------CREATING A CLIENT NON PERSON(ORGANISATION)---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL, getTestEntityClientAsJSON(activationDate, officeId, soleProprietorCodeValueId),
                "clientId");
    }

    public static Integer createClientForAccountPreference(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer clientType, String jsonAttributeToGetBack) {
        final String activationDate = "04 March 2011";
        final String officeId = "1";
        System.out.println(
                "---------------------------------CREATING A CLIENT BASED ON ACCOUNT PREFERENCE---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL,
                getTestClientWithClientTypeAsJSON(activationDate, officeId, clientType.toString()), jsonAttributeToGetBack);
    }

    public static Object assignStaffToClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId, final String staffId) {
        final String CLIENT_ASSIGN_STAFF_URL = "/fineract-provider/api/v1/clients/" + clientId + "?" + Utils.TENANT_IDENTIFIER
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
        map.put("dateFormat", DATE_FORMAT);
		map.put("locale", "en");
		map.put("active", "true");
		map.put("activationDate", dateOfJoining);
		System.out.println("map : " + map);
		return new Gson().toJson(map);
    }
    
    public static String getTestClientAsJSONPending(final String submittedOnDate, final String officeId) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("officeId", officeId);
        map.put("firstname", Utils.randomNameGenerator("Client_FirstName_", 5));
        map.put("lastname", Utils.randomNameGenerator("Client_LastName_", 4));
        map.put("externalId", randomIDGenerator("ID_", 7));
        map.put("dateFormat", DATE_FORMAT);
		map.put("locale", "en");
		map.put("active", "false");
		map.put("submittedOnDate", submittedOnDate);
		System.out.println("map : " + map);
		return new Gson().toJson(map);
    }

    public static String getTestPendingClientWithDatatableAsJson(final String registeredTableName) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("officeId", "1");
        map.put("firstname", Utils.randomNameGenerator("Client_FirstName_", 5));
        map.put("lastname", Utils.randomNameGenerator("Client_LastName_", 4));
        map.put("externalId", randomIDGenerator("ID_", 7));
        map.put("dateFormat", DATE_FORMAT);
        map.put("locale", "en");
        map.put("active", "false");
        map.put("submittedOnDate", "04 March 2014");
        String requestJson = getTestDatatableAsJson(map, registeredTableName);
        System.out.println("map : " + requestJson);
        return requestJson;
    }

    public static String getTestDatatableAsJson(HashMap<String, Object> map, final String registeredTableName) {
        List<HashMap<String, Object>> datatablesListMap = new ArrayList<>();
        HashMap<String, Object> datatableMap = new HashMap<>();
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("locale", "en");
        dataMap.put("Spouse Name", Utils.randomNameGenerator("Spouse_name", 4));
        dataMap.put("Number of Dependents", 5);
        dataMap.put("Time of Visit", "01 December 2016 04:03");
        dataMap.put("dateFormat", DATE_TIME_FORMAT);
        dataMap.put("Date of Approval", "02 December 2016 00:00");
        datatableMap.put("registeredTableName", registeredTableName);
        datatableMap.put("data", dataMap);
        datatablesListMap.add(datatableMap);
        map.put("datatables", datatablesListMap);
        return new Gson().toJson(map);
    }

    public static String getTestPersonClientAsJSON(final String dateOfJoining, final String officeId) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("officeId", officeId);
        map.put("fullname", Utils.randomNameGenerator("Client_FullName_", 5));
        map.put("externalId", randomIDGenerator("ID_", 7));
        map.put("dateFormat", DATE_FORMAT);
        map.put("locale", "en");
        map.put("active", "true");
        map.put("activationDate", dateOfJoining);
        map.put("legalFormId", 1);
        
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }
    
    public static String getTestEntityClientAsJSON(final String dateOfJoining, final String officeId, final Integer soleProprietorCodeValueId) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("officeId", officeId);
        map.put("fullname", Utils.randomNameGenerator("Client_FullName_", 5));
        map.put("externalId", randomIDGenerator("ID_", 7));
        map.put("dateFormat", DATE_FORMAT);
        map.put("locale", "en");
        map.put("active", "true");
        map.put("activationDate", dateOfJoining);
        map.put("legalFormId", 2);
        
        final HashMap<String, Object> clientNonPersonMap = new HashMap<>();
        clientNonPersonMap.put("constitutionId", soleProprietorCodeValueId);
        map.put("clientNonPersonDetails", clientNonPersonMap);
        
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static String getTestClientWithClientTypeAsJSON(final String dateOfJoining, final String officeId, final String clientType) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("officeId", officeId);
        map.put("firstname", Utils.randomNameGenerator("Client_FirstName_", 5));
        map.put("lastname", Utils.randomNameGenerator("Client_LastName_", 4));
        map.put("externalId", randomIDGenerator("ID_", 7));
        map.put("dateFormat", DATE_FORMAT);
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
        final String CLIENT_URL = "/fineract-provider/api/v1/clients/" + generatedClientID + "?" + Utils.TENANT_IDENTIFIER;
        final Integer responseClientID = Utils.performServerGet(requestSpec, responseSpec, CLIENT_URL, "id");
        assertEquals("ERROR IN CREATING THE CLIENT", generatedClientID, responseClientID);
    }

    public static Object getClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final String clientId,
            final String jsonReturn) {
        final String GET_CLIENT_URL = "/fineract-provider/api/v1/clients/" + clientId + "?" + Utils.TENANT_IDENTIFIER;
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

	private String getUndoRejectClientAsJSON(final String date) {
		final HashMap<String, String> map = new HashMap<>();
		map.put("locale", CommonConstants.locale);
		map.put("dateFormat", CommonConstants.dateFormat);
		map.put("reopenedDate", date);
		String clientJson = new Gson().toJson(map);
		System.out.println(clientJson);
		return clientJson;

	}

	private String getUndoWithdrawnClientAsJSON(final String date) {
		final HashMap<String, String> map = new HashMap<>();
		map.put("locale", CommonConstants.locale);
		map.put("dateFormat", CommonConstants.dateFormat);
		map.put("reopenedDate", date);
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
		HashMap<String, Object> codeValue = CodeHelper.retrieveOrCreateCodeValue(clientRejectionReasonCodeId,
				this.requestSpec, this.responseSpec);
		Integer rejectionReasonId = (Integer) codeValue.get("id");

		map.put("locale", CommonConstants.locale);
		map.put("dateFormat", CommonConstants.dateFormat);
		map.put("rejectionDate", CREATED_DATE_PLUS_ONE);
		map.put("rejectionReasonId", rejectionReasonId.toString());
		String clientJson = new Gson().toJson(map);
		System.out.println(clientJson);
		return clientJson;

	}

    private String getActivateClientAsJSON(String date) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("activationDate", date);
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

    public static String getSpecifiedDueDateChargesClientAsJSON(final String chargeId, final String dueDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en_GB");
        map.put("dateFormat", DATE_FORMAT);
        map.put("dueDate", dueDate);
        map.put("chargeId", chargeId);
        map.put("amount", "200");
        String json = new Gson().toJson(map);
        return json;
    }

    public static String getPayChargeJSON(final String date, String amount) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en_GB");
        map.put("dateFormat", DATE_FORMAT);
        map.put("transactionDate", date);
        map.put("amount", amount);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public static String getWaiveChargeJSON(final String amount, String clientChargeId) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en_GB");
        map.put("amount", amount);
        map.put("clientChargeId", clientChargeId);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
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
        return performClientActions(createClientOperationURL(ACTIVATE_CLIENT_COMMAND, clientId), getActivateClientAsJSON(CREATED_DATE_PLUS_ONE), clientId);
    }

    public HashMap<String, Object> withdrawClient(final Integer clientId) {
        System.out.println("--------------------------------- WITHDRAWN CLIENT -------------------------------");
        return performClientActions(createClientOperationURL(WITHDRAW_CLIENT_COMMAND, clientId), getWithdrawClientAsJSON(), clientId);
    }

	private String createClientOperationURL(final String command, final Integer clientId) {
		return CLIENT_URL + "/" + clientId + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
	}

	public HashMap<String, Object> undoReject(final Integer clientId) {
		System.out.println("--------------------------------- UNDO REJECT CLIENT -------------------------------");
		return performClientActions(createClientOperationURL(UNDOREJECT_CLIENT_COMMAND, clientId),
				getUndoRejectClientAsJSON(CREATED_DATE_PLUS_TWO), clientId);
	}

	public HashMap<String, Object> undoWithdrawn(final Integer clientId) {
		System.out.println("--------------------------------- UNDO WITHDRAWN CLIENT -------------------------------");
		return performClientActions(createClientOperationURL(UNDOWITHDRAWN_CLIENT_COMMAND, clientId),
				getUndoWithdrawnClientAsJSON(CREATED_DATE_PLUS_TWO), clientId);
	}

	public Object undoRejectedclient(final Integer clientId, final String jsonAttributeToGetBack,
			final String rejectedDate) {
		System.out.println("----------------------------------UNDO REJECT CLIENT ----------------------------------");
		return performClientActionsWithValidationErrors(createClientOperationURL(UNDOREJECT_CLIENT_COMMAND, clientId),
				getUndoRejectClientAsJSON(rejectedDate), jsonAttributeToGetBack);
	}

	public Object undoWithdrawclient(final Integer clientId, final String jsonAttributeToGetBack,
			final String rejectedDate) {
		System.out.println("----------------------------------UNDO WITHDRAW CLIENT ----------------------------------");
		return performClientActionsWithValidationErrors(
				createClientOperationURL(UNDOWITHDRAWN_CLIENT_COMMAND, clientId),
				getUndoWithdrawnClientAsJSON(rejectedDate), jsonAttributeToGetBack);
	}

	public Object activateClient(final Integer clientId, final String jsonAttributeToGetBack) {
		System.out.println("--------------------------------- ACTIVATE CLIENT -------------------------------");
		return performClientActionsWithValidationErrors(createClientOperationURL(ACTIVATE_CLIENT_COMMAND, clientId),
				getActivateClientAsJSON(CREATED_DATE_PLUS_ONE), jsonAttributeToGetBack);
	}

	public HashMap<String, Object> activateClientWithDiffDateOption(final Integer clientId,
			final String activationDate) {
		System.out.println("--------------------------------- ACTIVATE CLIENT -------------------------------");
		return performClientActions(createClientOperationURL(ACTIVATE_CLIENT_COMMAND, clientId),
				getActivateClientAsJSON(activationDate), clientId);
	}

	private ArrayList<HashMap> performClientActionsWithValidationErrors(final String postURLForClient,
			final String jsonToBeSent, final String jsonAttributeToGetBack) {
		return Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForClient, jsonToBeSent,
				jsonAttributeToGetBack);
	}
    
    private HashMap<String, Object> performClientActions(final String postURLForClient, final String jsonToBeSent, final Integer clientId) {
        Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForClient, jsonToBeSent, CommonConstants.RESPONSE_STATUS);
        HashMap<String, Object> response = ClientHelper.getClientStatus(requestSpec, responseSpec, String.valueOf(clientId));

        return response;
    }

    public static Integer addChargesForClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer clientId, final String request) {
        System.out.println("--------------------------------- ADD CHARGES FOR Client --------------------------------");
        final String ADD_CHARGES_URL = "/fineract-provider/api/v1/clients/" + clientId + "/charges?" + Utils.TENANT_IDENTIFIER;
        final HashMap<?, ?> response = Utils.performServerPost(requestSpec, responseSpec, ADD_CHARGES_URL, request, "");
        return (Integer) response.get("resourceId");
    }

    public static String payChargesForClients(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer clientId, final Integer clientChargeId, final String json) {
        System.out.println("--------------------------------- PAY CHARGES FOR CLIENT --------------------------------");
        final String CHARGES_URL = "/fineract-provider/api/v1/clients/" + clientId + "/charges/" + clientChargeId + "?command=paycharge&"
                + Utils.TENANT_IDENTIFIER;
        final HashMap<?, ?> response = Utils.performServerPost(requestSpec, responseSpec, CHARGES_URL, json, "");
        return response.get("transactionId") != null ? response.get("transactionId").toString() : null;
    }

    public static String waiveChargesForClients(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer clientId, final Integer clientChargeId, final String json) {
        System.out.println("--------------------------------- WAIVE CHARGES FOR CLIENT --------------------------------");
        final String CHARGES_URL = "/fineract-provider/api/v1/clients/" + clientId + "/charges/" + clientChargeId + "?command=waive&"
                + Utils.TENANT_IDENTIFIER;

        final HashMap<?, ?> response = Utils.performServerPost(requestSpec, responseSpec, CHARGES_URL, json, "");
        return response.get("transactionId").toString();
    }

    public static Integer revertClientChargeTransaction(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId, String clientChargeId) {
        System.out.println("---------------------------------UNDO TRANSACTION---------------------------------------------");
        final String CHARGES_URL = "/fineract-provider/api/v1/clients/" + clientId + "/transactions/" + clientChargeId + "?command=undo&"
                + Utils.TENANT_IDENTIFIER;

        final HashMap<?, ?> response = Utils.performServerPost(requestSpec, responseSpec, CHARGES_URL, "", "");
        return (Integer) response.get("resourceId");

    }

    public static Object getClientCharge(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId, final String clientChargeId) {
        System.out.println("---------------------------------GET CLIENT CHARGE---------------------------------------------");
        final String CHARGES_URL = "/fineract-provider/api/v1/clients/" + clientId + "/charges/" + clientChargeId + "?"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, CHARGES_URL, "amountOutstanding");
    }

    public static Boolean getClientTransactions(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId, final String transactionId) {
        System.out.println("---------------------------------GET CLIENT CHARGE TRANSACTIONS---------------------------------------------");
        final String CHARGES_URL = "/fineract-provider/api/v1/clients/" + clientId + "/transactions/" + transactionId + "?"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, CHARGES_URL, "reversed");
    }

    public Workbook getClientEntityWorkbook(GlobalEntityType clientsEntity, String dateFormat) throws IOException {
        requestSpec.header(HttpHeaders.CONTENT_TYPE,"application/vnd.ms-excel");
        byte [] byteArray=Utils.performGetBinaryResponse(requestSpec,responseSpec,CLIENT_URL+"/downloadtemplate"+"?"+
                Utils.TENANT_IDENTIFIER+"&legalFormType="+clientsEntity+"&dateFormat="+dateFormat);
        InputStream inputStream= new ByteArrayInputStream(byteArray);
        Workbook workbook=new HSSFWorkbook(inputStream);
        return workbook;
    }

    public String getOutputTemplateLocation(final String importDocumentId){
        requestSpec.header(HttpHeaders.CONTENT_TYPE,MediaType.TEXT_PLAIN);
        return Utils.performServerOutputTemplateLocationGet(requestSpec,responseSpec,"/fineract-provider/api/v1/imports/getOutputTemplateLocation"+"?"
                +Utils.TENANT_IDENTIFIER,importDocumentId);
    }

    public String importClientEntityTemplate(File file){
        String locale="en";
        String dateFormat="dd MMMM yyyy";
        String legalFormType=GlobalEntityType.CLIENTS_ENTTTY.toString();
        requestSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA);
        return Utils.performServerTemplatePost(requestSpec,responseSpec,CLIENT_URL+"/uploadtemplate"+"?"+Utils.TENANT_IDENTIFIER,
                legalFormType,file,locale,dateFormat);
    }
}