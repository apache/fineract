/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.useradministration.roles;

import java.util.HashMap;

import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class RolesHelper {

    private static final String CREATE_ROLE_URL = "/mifosng-provider/api/v1/roles?" + Utils.TENANT_IDENTIFIER;
    private static final String ROLE_URL = "/mifosng-provider/api/v1/roles";
    private static final String DISABLE_ROLE_COMMAND = "disable";
    private static final String ENABLE_ROLE_COMMAND = "enable";

    public static Integer createRole(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_ROLE_URL, getTestCreaRoleAsJSON(), "resourceId");
    }

    public static String getTestCreaRoleAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("name", Utils.randomNameGenerator("Role_Name_", 5));
        map.put("description", Utils.randomNameGenerator("Role_Description_", 10));
        return new Gson().toJson(map);
    }

    public static HashMap<String, Object> getRoleDetails(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer roleId) {
        final String GET_ROLE_URL = "/mifosng-provider/api/v1/roles/" + roleId + "?" + Utils.TENANT_IDENTIFIER;
        HashMap<String, Object> role = Utils.performServerGet(requestSpec, responseSpec, GET_ROLE_URL, "");
        return role;
    }

    public static Integer disableRole(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final Integer roleId) {
        return Utils.performServerPost(requestSpec, responseSpec, createRoleOperationURL(DISABLE_ROLE_COMMAND, roleId), "", "resourceId");
    }

    public static Integer enableRole(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final Integer roleId) {
        return Utils.performServerPost(requestSpec, responseSpec, createRoleOperationURL(ENABLE_ROLE_COMMAND, roleId), "", "resourceId");
    }

    public static Integer deleteRole(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final Integer roleId) {
        return Utils.performServerDelete(requestSpec, responseSpec, createRoleOperationURL(ENABLE_ROLE_COMMAND, roleId), "resourceId");
    }

    private static String createRoleOperationURL(final String command, final Integer roleId) {
        return ROLE_URL + "/" + roleId + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

}