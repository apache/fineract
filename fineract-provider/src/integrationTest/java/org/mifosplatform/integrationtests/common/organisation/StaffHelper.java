/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.organisation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class StaffHelper {

    private static final String TRANSFER_STAFF_URL = "/mifosng-provider/api/v1/groups";

    private static final String CREATE_STAFF_URL = "/mifosng-provider/api/v1/staff";

    private static final String RESOURCE_ID = "resourceId";

    public static final String GROUP_ID = "groupId";


    public static Integer transferStaffToGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
                                               final Integer groupId,final Integer staffToTransfer ,final String note){
        final String url = TRANSFER_STAFF_URL + "/" + groupId + "?command=transferStaff&"+ Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, url, transferStaffToGroupAsJSON(staffToTransfer, note), GROUP_ID);
    }

    public static String transferStaffToGroupAsJSON(final Integer staffToTransferId,final String note) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("staffId", staffToTransferId);
        map.put("note", note);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static Integer createStaff(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return (Integer) createStaffWithJson(requestSpec, responseSpec, createStaffAsJSON()).get("resourceId");
    }

    public static HashMap createStaffMap(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return createStaffWithJson(requestSpec, responseSpec, createStaffAsJSON());
    }

    public static HashMap createStaffWithJson(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String json) {
        final String url = CREATE_STAFF_URL + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, url, json, "");
    }

    public static HashMap getStaff(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer staffId) {
        final String url = CREATE_STAFF_URL + "/" + staffId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, url, "");
    }

    public static List<HashMap> getStaffList(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String url = CREATE_STAFF_URL + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, url, "");
    }

    public static Object getStaffListWithState(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String status) {
        final String url = CREATE_STAFF_URL + "?" + Utils.TENANT_IDENTIFIER + "&status=" + status;
        return Utils.performServerGet(requestSpec, responseSpec, url, "");
    }

    public static Object updateStaff(final RequestSpecification requestSpec,
             final ResponseSpecification responseSpec, final Integer staffId, final HashMap<String, Object> changes) {
        final String url = CREATE_STAFF_URL + "/" + staffId + "?" + Utils.TENANT_IDENTIFIER;
        final String json = new Gson().toJson(changes);
        return Utils.performServerPut(requestSpec, responseSpec, url, json, "");
    }

    public static String createStaffAsJSON(){
        final HashMap<String, Object> map = new HashMap<>();
        map.put("officeId", 1);
        map.put("firstname", Utils.randomNameGenerator("michael_", 5));
        map.put("lastname", Utils.randomNameGenerator("Doe_", 4));
        map.put("isLoanOfficer", true);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static String createStaffWithJSONFields(String... fields) {
        final HashMap<String, Object> map = new HashMap<>();
        final List<String> fieldList = Arrays.asList(fields);
        if(fieldList.contains("officeId")) {
            map.put("officeId", 1);
        }
        if(fieldList.contains("firstname")) {
            map.put("firstname", Utils.randomNameGenerator("michael_", 5));
        }
        if(fieldList.contains("lastname")) {
            map.put("lastname", Utils.randomNameGenerator("Doe_", 4));
        }
        if(fieldList.contains("isLoanOfficer")) {
            map.put("isLoanOfficer", true);
        }
        if(fieldList.contains("mobileNo")) {
            map.put("mobileNo", "+123515198");
        }
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }
}
