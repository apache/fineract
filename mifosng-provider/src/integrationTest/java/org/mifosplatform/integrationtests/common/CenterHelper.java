/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class CenterHelper {

    private static final String CENTERS_URL = "/mifosng-provider/api/v1/centers";

    public static CenterDomain retrieveByID(int id, final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String GET_CENTER_BY_ID_URL = CENTERS_URL + "/" + id + "?associations=groupMembers&" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING CENTER AT " + id + "-------------------------");
        final String jsonData = new Gson().toJson(Utils.performServerGet(requestSpec, responseSpec, GET_CENTER_BY_ID_URL, ""));
        return new Gson().fromJson(jsonData, new TypeToken<CenterDomain>() {}.getType());
    }

    public static ArrayList<CenterDomain> paginatedListCenters(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        final String GET_CENTER = CENTERS_URL + "?paged=true&limit=-1&" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING CENTERS-------------------------");
        final String jsonData = new Gson().toJson(Utils.performServerGet(requestSpec, responseSpec, GET_CENTER, "pageItems"));
        return new Gson().fromJson(jsonData, new TypeToken<ArrayList<CenterDomain>>() {}.getType());
    }

    public static ArrayList<CenterDomain> listCenters(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String GET_CENTER = CENTERS_URL + "?limit=-1&" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING CENTERS-------------------------");
        final String jsonData = new Gson().toJson(Utils.performServerGet(requestSpec, responseSpec, GET_CENTER, ""));
        return new Gson().fromJson(jsonData, new TypeToken<ArrayList<CenterDomain>>() {}.getType());
    }

    public static int createCenter(final String name, final int officeId, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        return createCenter(name, officeId, null, -1, null, null, requestSpec, responseSpec);
    }

    public static int createCenter(final String name, final int officeId, final String activationDate,
            final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return createCenter(name, officeId, null, -1, null, activationDate, requestSpec, responseSpec);
    }

    public static int createCenter(final String name, final int officeId, final String externalId, final int staffId,
            final int[] groupMembers, final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return createCenter(name, officeId, externalId, staffId, groupMembers, null, requestSpec, responseSpec);
    }

    public static int createCenter(final String name, final int officeId, final String externalId, final int staffId,
            final int[] groupMembers, final String activationDate, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        final String CREATE_CENTER_URL = CENTERS_URL + "?" + Utils.TENANT_IDENTIFIER;
        HashMap hm = new HashMap();
        hm.put("name", name);
        hm.put("officeId", officeId);
        hm.put("active", false);

        if (externalId != null) hm.put("externalId", externalId);
        if (staffId != -1) hm.put("staffId", staffId);
        if (groupMembers != null) hm.put("groupMembers", groupMembers);
        if (activationDate != null) {
            hm.put("active", true);
            hm.put("locale", "en");
            hm.put("dateFormat", "dd MMM yyyy");
            hm.put("activationDate", activationDate);
        }

        System.out.println("------------------------CREATING CENTER-------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CENTER_URL, new Gson().toJson(hm), "resourceId");
    }

    public static HashMap<String, String> updateCenter(final int id, HashMap request, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        final String UPDATE_CENTER_URL = CENTERS_URL + "/" + id + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("---------------------------------UPDATE CENTER AT " + id + "---------------------------------------------");
        HashMap<String, String> hash = Utils.performServerPut(requestSpec, responseSpec, UPDATE_CENTER_URL, new Gson().toJson(request),
                "changes");
        return hash;
    }

    public static int[] associateGroups(final int id, final int[] groupMembers, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        final String ASSOCIATE_GROUP_CENTER_URL = CENTERS_URL + "/" + id + "?command=associateGroups&" + Utils.TENANT_IDENTIFIER;
        HashMap groupMemberHashMap = new HashMap();
        groupMemberHashMap.put("groupMembers", groupMembers);
        System.out.println("---------------------------------ASSOCIATING GROUPS AT " + id + "--------------------------------------------");
        HashMap hash = Utils.performServerPost(requestSpec, responseSpec, ASSOCIATE_GROUP_CENTER_URL,
                new Gson().toJson(groupMemberHashMap), "changes");
        System.out.println(hash);
        ArrayList<String> arr = (ArrayList<String>) hash.get("groupMembers");
        int[] ret = new int[arr.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Integer.parseInt(arr.get(i));
        }
        return ret;
    }

    public static void deleteCenter(final int id, final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String DELETE_CENTER_URL = CENTERS_URL + "/" + id + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("---------------------------------DELETING CENTER AT " + id + "--------------------------------------------");
        Utils.performServerDelete(requestSpec, responseSpec, DELETE_CENTER_URL, "");
    }

}
