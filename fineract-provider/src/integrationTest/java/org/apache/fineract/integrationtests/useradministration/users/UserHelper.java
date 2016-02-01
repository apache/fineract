/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.useradministration.users;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.mifosplatform.integrationtests.common.Utils;


public class UserHelper {
    private static final String CREATE_USER_URL = "/mifosng-provider/api/v1/users?" + Utils.TENANT_IDENTIFIER;
    private static final String USER_URL = "/mifosng-provider/api/v1/users";

    public static Integer createUser(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, int roleId, int staffId) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_USER_URL, getTestCreateUserAsJSON(roleId, staffId), "resourceId");
    }

    public static String getTestCreateUserAsJSON(int roleId, int staffId) {
        String json = "{ \"username\": \"" + Utils.randomNameGenerator("User_Name_", 3)
                + "\", \"firstname\": \"Test\", \"lastname\": \"User\", \"email\": \"whatever@mifos.org\","
                + " \"officeId\": \"1\", \"staffId\": " + "\""
                + Integer.toString(staffId)+"\",\"roles\": [\""
                + Integer.toString(roleId) + "\"], \"sendPasswordToEmail\": false}";
        System.out.println(json);
        return json;

    }

    public static Integer deleteUser(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final Integer userId) {
        return Utils.performServerDelete(requestSpec, responseSpec, createRoleOperationURL(userId), "resourceId");
    }

    private static String createRoleOperationURL(final Integer userId) {
        return USER_URL + "/" + userId + "?" + Utils.TENANT_IDENTIFIER;
    }
}