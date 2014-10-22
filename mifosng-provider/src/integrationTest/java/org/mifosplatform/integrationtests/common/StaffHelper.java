/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import java.util.HashMap;

public class StaffHelper {

    private static final String TRANSFER_STAFF_URL = "/mifosng-provider/api/v1/groups";

    private static final String CREATE_STAFF_URL = "/mifosng-provider/api/v1/staff";

    private static final String RESOURCE_ID = "resourceId";

    public static final String GROUP_ID = "groupId";



    public static Integer transferStaffToGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
                                               final Integer groupId,final Integer staffToTransfer ,final String note){
        final String url = TRANSFER_STAFF_URL + "/" + groupId + "?command=transferStaff&"+Utils.TENANT_IDENTIFIER;
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
        final String url = CREATE_STAFF_URL +"?"+ Utils.TENANT_IDENTIFIER ;
        return Utils.performServerPost(requestSpec, responseSpec, url, createStaffAsJSON(), RESOURCE_ID);
    }


    public static String createStaffAsJSON(){
        final HashMap<String, Object> map = new HashMap<>();
        map.put("officeId",1);
        map.put("firstname", Utils.randomNameGenerator("michael_",5));
        map.put("lastname", Utils.randomNameGenerator("Doe_",4));
        map.put("isLoanOfficer", true);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }


}
