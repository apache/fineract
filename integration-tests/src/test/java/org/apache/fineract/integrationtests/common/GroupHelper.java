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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;
    private static final Logger LOG = LoggerFactory.getLogger(GroupHelper.class);

    private static final String CREATE_GROUP_URL = "/fineract-provider/api/v1/groups?" + Utils.TENANT_IDENTIFIER;
    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DATE_TIME_FORMAT = "dd MMMM yyyy HH:mm";

    public GroupHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public static Integer createGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            @SuppressWarnings("unused") final boolean active) {
        LOG.info("---------------------------------CREATING A GROUP---------------------------------------------");
        return createGroup(requestSpec, responseSpec, "04 March 2011");
    }

    public static Integer createGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate) {
        LOG.info("---------------------------------CREATING A GROUP---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_GROUP_URL, getTestGroupAsJSON(true, activationDate), "groupId");
    }

    public static Integer createGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        LOG.info("---------------------------------CREATING A GROUP---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_GROUP_URL, getTestGroupAsJSON(false, ""), "groupId");
    }

    public Object createGroupWithError(final String jsonAttributeToGetBack) {
        LOG.info("---------------------------------CREATING A GROUP WITH ERROR---------------------------------------------");
        return Utils.performServerPost(this.requestSpec, this.responseSpec, CREATE_GROUP_URL, getTestGroupAsJSON(false, ""),
                jsonAttributeToGetBack);
    }

    public static Integer createGroupPendingWithDatatable(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String registeredTableName) {
        LOG.info("-------------------------- CREATING A GROUP WITH DATATABLES --------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_GROUP_URL, getTestGroupWithDatatableAsJson(registeredTableName),
                "groupId");
    }

    public static Integer associateClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String groupId, final String clientMember) {
        final String GROUP_ASSOCIATE_URL = "/fineract-provider/api/v1/groups/" + groupId + "?command=associateClients&"
                + Utils.TENANT_IDENTIFIER;
        LOG.info("---------------------------------Associate Client To A GROUP---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, GROUP_ASSOCIATE_URL, associateClientAsJSON(clientMember), "groupId");
    }

    public static Integer disAssociateClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String groupId, final String clientMember) {
        final String GROUP_ASSOCIATE_URL = "/fineract-provider/api/v1/groups/" + groupId + "?command=disassociateClients&"
                + Utils.TENANT_IDENTIFIER;
        LOG.info("---------------------------------Disassociate Client To A GROUP---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, GROUP_ASSOCIATE_URL, associateClientAsJSON(clientMember), "groupId");
    }

    public static Integer activateGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String groupId) {
        final String GROUP_ASSOCIATE_URL = "/fineract-provider/api/v1/groups/" + groupId + "?command=activate&" + Utils.TENANT_IDENTIFIER;
        LOG.info("---------------------------------Activate A GROUP---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, GROUP_ASSOCIATE_URL, activateGroupAsJSON(""), "groupId");
    }

    public static Integer updateGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final String name,
            final String groupId) {
        final String GROUP_ASSOCIATE_URL = "/fineract-provider/api/v1/groups/" + groupId + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("---------------------------------UPDATE GROUP---------------------------------------------");
        return Utils.performServerPut(requestSpec, responseSpec, GROUP_ASSOCIATE_URL, updateGroupAsJSON(name), "groupId");
    }

    public static Integer deleteGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String groupId) {
        final String GROUP_ASSOCIATE_URL = "/fineract-provider/api/v1/groups/" + groupId + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("---------------------------------DELETE GROUP---------------------------------------------");
        return Utils.performServerDelete(requestSpec, responseSpec, GROUP_ASSOCIATE_URL, "groupId");
    }

    public static Object assignStaff(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final String groupId,
            final Long staffId) {
        final String GROUP_ASSIGN_STAFF_URL = "/fineract-provider/api/v1/groups/" + groupId + "?" + Utils.TENANT_IDENTIFIER
                + "&command=assignStaff";
        LOG.info("---------------------------------DELETE GROUP---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, GROUP_ASSIGN_STAFF_URL, assignStaffAsJSON(staffId), "changes");
    }

    public static Object assignStaffInheritStaffForClientAccounts(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String groupId, final String staffId) {
        final String GROUP_ASSIGN_STAFF_URL = "/fineract-provider/api/v1/groups/" + groupId + "?" + Utils.TENANT_IDENTIFIER
                + "&command=assignStaff";
        LOG.info("---------------------------------DELETE GROUP---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, GROUP_ASSIGN_STAFF_URL,
                assignStaffAndInheritStaffForClientAccountsAsJSON(staffId), "changes");
    }

    public static String getTestGroupAsJSON(final boolean active, final String activationDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("officeId", "1");
        map.put("name", randomNameGenerator("Group_Name_", 5));
        map.put("externalId", randomIDGenerator("ID_", 7));
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        if (active) {
            map.put("active", "true");
            map.put("activationDate", activationDate);
        } else {
            map.put("active", "false");
            map.put("submittedOnDate", "04 March 2011");
            LOG.info("defaulting to inactive group: 04 March 2011");
        }
        LOG.debug("map : {} ", map);
        return new Gson().toJson(map);
    }

    public static String associateClientAsJSON(final String clientMember) {
        final HashMap<String, List<String>> map = new HashMap<>();
        final List<String> list = new ArrayList<>();
        list.add(clientMember);
        map.put("clientMembers", list);
        LOG.debug("map : {}", map);
        return new Gson().toJson(map);
    }

    public static String activateGroupAsJSON(final String activationDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        if (!Strings.isNullOrEmpty(activationDate)) {
            map.put("activationDate", activationDate);
        } else {
            map.put("activationDate", "04 March 2011");
            LOG.info("defaulting to fixed date: 04 March 2011");
        }
        LOG.debug("map : {}", map);
        return new Gson().toJson(map);
    }

    public static String updateGroupAsJSON(final String name) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("name", name);
        LOG.debug("map : {}", map);
        return new Gson().toJson(map);
    }

    public static String assignStaffAsJSON(final Long staffId) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("staffId", staffId);
        LOG.debug("map : {}", map);
        return new Gson().toJson(map);
    }

    public static String assignStaffAndInheritStaffForClientAccountsAsJSON(final String staffId) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("staffId", staffId);
        map.put("inheritStaffForClientAccounts", "true");
        LOG.debug("map : {}", map);
        return new Gson().toJson(map);
    }

    public static void verifyGroupCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedGroupID) {
        LOG.info("------------------------------CHECK GROUP DETAILS------------------------------------\n");
        final String GROUP_URL = "/fineract-provider/api/v1/groups/" + generatedGroupID + "?" + Utils.TENANT_IDENTIFIER;
        final Integer responseGroupID = Utils.performServerGet(requestSpec, responseSpec, GROUP_URL, "id");
        assertEquals(generatedGroupID, responseGroupID, "ERROR IN CREATING THE GROUP");
    }

    public static void verifyGroupDetails(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedGroupID, final String field, final String expectedValue) {
        LOG.info("------------------------------CHECK GROUP DETAILS------------------------------------\n");
        final String GROUP_URL = "/fineract-provider/api/v1/groups/" + generatedGroupID + "?" + Utils.TENANT_IDENTIFIER;
        final String responseValue = Utils.performServerGet(requestSpec, responseSpec, GROUP_URL, field);
        assertEquals(expectedValue, responseValue, "ERROR IN CREATING THE GROUP");
    }

    public static void verifyGroupActivatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedGroupID, final boolean generatedGroupStatus) {
        LOG.info("------------------------------CHECK GROUP STATUS------------------------------------\n");
        final String GROUP_URL = "/fineract-provider/api/v1/groups/" + generatedGroupID + "?" + Utils.TENANT_IDENTIFIER;
        final Boolean responseGroupStatus = Utils.performServerGet(requestSpec, responseSpec, GROUP_URL, "active");
        assertEquals(generatedGroupStatus, responseGroupStatus, "ERROR IN ACTIVATING THE GROUP");
    }

    public static void verifyGroupMembers(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedGroupID, final Integer groupMember) {
        List<String> list = new ArrayList<>();
        LOG.info("------------------------------CHECK GROUP MEMBERS------------------------------------\n");
        final String GROUP_URL = "/fineract-provider/api/v1/groups/" + generatedGroupID + "?associations=clientMembers&"
                + Utils.TENANT_IDENTIFIER;
        list = Utils.performServerGet(requestSpec, responseSpec, GROUP_URL, "clientMembers");
        LOG.debug("the list of verifyEmptyGroupMembers : {} ", list);
        assertTrue(list.toString().contains("id=" + groupMember.toString()), "ERROR IN GROUP MEMBER");
    }

    public static void verifyEmptyGroupMembers(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedGroupID) {
        List<String> list = new ArrayList<>();
        LOG.info("------------------------------CHECK EMPTY GROUP MEMBER LIST------------------------------------\n");
        final String GROUP_URL = "/fineract-provider/api/v1/groups/" + generatedGroupID + "?associations=clientMembers&"
                + Utils.TENANT_IDENTIFIER;
        list = Utils.performServerGet(requestSpec, responseSpec, GROUP_URL, "clientMembers");
        LOG.debug("the list of verifyEmptyGroupMembers : {} ", list);
        assertEquals(list, null, "GROUP MEMBER LIST NOT EMPTY");
    }

    public static void verifyGroupDeleted(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedGroupID) {
        List<String> list = new ArrayList<>();
        LOG.info("------------------------------CHECK GROUP DELETED------------------------------------\n");
        final String GROUP_URL = "/fineract-provider/api/v1/groups/?" + Utils.TENANT_IDENTIFIER;
        list = Utils.performServerGet(requestSpec, responseSpec, GROUP_URL, "pageItems");
        assertFalse(list.toString().contains("id=" + generatedGroupID.toString()), "GROUP NOT DELETED");
    }

    // Glim_Gsim_testing
    public static List<String> verifyRetrieveGlimAccountsByGroupId(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer groupID) {
        List<String> list = new ArrayList<>();
        LOG.info("------------------------------CHECK GROUP Retrieve Accounts------------------------------------\n");
        final String GROUP_URL = "/fineract-provider/api/v1/groups/" + groupID + "/glimaccounts?" + Utils.TENANT_IDENTIFIER;
        list = Utils.performServerGet(requestSpec, responseSpec, GROUP_URL, "glimId");
        LOG.debug("GlimId of Retrieved Account : {} ", list);
        return list;
    }

    public static List<String> verifyRetrieveGlimAccountsByGlimId(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer glimId) {
        List<String> list = new ArrayList<>();
        LOG.info("------------------------------CHECK GROUP Retrieve Accounts------------------------------------\n");
        final String GROUP_URL = "/fineract-provider/api/v1/loans/glimAccount/" + glimId + "?" + Utils.TENANT_IDENTIFIER;
        list = Utils.performServerGet(requestSpec, responseSpec, GROUP_URL, "glimId");
        LOG.debug("GlimId of Retrieved Account: {} ", list);
        return list;
    }

    public static List<String> verifyRetrieveGsimAccounts(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer groupID) {
        List<String> list = new ArrayList<>();
        LOG.info("------------------------------CHECK GROUP Retrieve Accounts------------------------------------\n");
        final String GROUP_URL = "/fineract-provider/api/v1/groups/" + groupID + "/gsimaccounts?" + Utils.TENANT_IDENTIFIER;
        list = Utils.performServerGet(requestSpec, responseSpec, GROUP_URL, "gsimId");
        LOG.debug("GsimId Retrieved Accounts: {} ", list);
        return list;
    }

    public static String randomNameGenerator(final String prefix, final int lenOfRandomSuffix) {
        return Utils.randomStringGenerator(prefix, lenOfRandomSuffix);
    }

    private static String randomIDGenerator(final String prefix, final int lenOfRandomSuffix) {
        return Utils.randomStringGenerator(prefix, lenOfRandomSuffix, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    public static String getTestGroupWithDatatableAsJson(final String registeredTableName) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("officeId", "1");
        map.put("name", randomNameGenerator("Group_Name_", 5));
        map.put("externalId", randomIDGenerator("ID_", 7));
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("active", "false");
        map.put("submittedOnDate", "04 March 2011");
        String requestJson = getTestDatatableAsJson(map, registeredTableName);
        LOG.debug("map : {} ", requestJson);
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
}
