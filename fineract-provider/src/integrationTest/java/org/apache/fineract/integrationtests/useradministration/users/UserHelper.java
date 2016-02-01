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
package org.apache.fineract.integrationtests.useradministration.users;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import org.apache.fineract.integrationtests.common.Utils;


public class UserHelper {
    private static final String CREATE_USER_URL = "/fineract-provider/api/v1/users?" + Utils.TENANT_IDENTIFIER;
    private static final String USER_URL = "/fineract-provider/api/v1/users";

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