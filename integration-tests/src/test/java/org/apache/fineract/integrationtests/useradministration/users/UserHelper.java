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

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.List;
import org.apache.fineract.client.models.ChangePwdUsersUserIdRequest;
import org.apache.fineract.client.models.ChangePwdUsersUserIdResponse;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.GetUsersResponse;
import org.apache.fineract.client.models.GetUsersUserIdResponse;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.client.models.PutUsersUserIdRequest;
import org.apache.fineract.client.models.PutUsersUserIdResponse;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;

public final class UserHelper {

    public static final String SIMPLE_USER_NAME = Utils.uniqueRandomStringGenerator("TestUser_", 8);
    public static final String SIMPLE_USER_PASSWORD = "QwE!SrTy#9uP0";
    public static final long SUPER_USER_ROLE_ID = 1L;

    private UserHelper() {

    }

    /**
     * Factory method for backward compatibility. Even though it returns a new instance, Checkstyle is fine because the
     * constructor is private.
     */
    public static UserHelper create() {
        return new UserHelper();
    }

    public static PostUsersResponse createUser(final PostUsersRequest request) {
        return Calls.ok(FineractClientHelper.getFineractClient().users.createUser(request));
    }

    public static GetUsersUserIdResponse retrieveOneUser(final Long userId) {
        return Calls.ok(FineractClientHelper.getFineractClient().users.retrieveOneUser(userId));
    }

    public static List<GetUsersResponse> retrieveAllUsers() {
        return Calls.ok(FineractClientHelper.getFineractClient().users.retrieveAllUsers());
    }

    public static Long getUserIdByUsername(final String username) {
        return retrieveAllUsers().stream().filter(u -> u.getUsername().equals(username)).map(GetUsersResponse::getId).findFirst()
                .orElse(null);
    }

    public static PutUsersUserIdResponse updateUser(final Long userId, final PutUsersUserIdRequest request) {
        return Calls.ok(FineractClientHelper.getFineractClient().users.updateUser(userId, request));
    }

    public static ChangePwdUsersUserIdResponse changePasswordUser(final Long userId, final ChangePwdUsersUserIdRequest request) {
        return Calls.ok(FineractClientHelper.getFineractClient().users.changePasswordUser(userId, request));
    }

    public static void deleteUser(final Long userId) {
        Calls.ok(FineractClientHelper.getFineractClient().users.deleteUser(userId));
    }

    public static PostUsersRequest buildUserRequest(final String password, final Long roleId) {
        GetOfficesResponse office = OfficeHelper.getHeadOffice();
        return new PostUsersRequest().username(Utils.uniqueRandomStringGenerator("TestUser", 4)).firstname(Utils.randomFirstNameGenerator())
                .lastname(Utils.randomLastNameGenerator()).email("testuser@example.com").password(password).repeatPassword(password)
                .sendPasswordToEmail(false).officeId(office.getId()).roles(List.of(roleId));
    }

    public static PostUsersRequest buildUserRequest(ResponseSpecification res, RequestSpecification req, String password) {
        return buildUserRequest(password, SUPER_USER_ROLE_ID);
    }

    public static PostUsersResponse createUser(RequestSpecification req, ResponseSpecification res, PostUsersRequest request) {
        return createUser(request);
    }

    public static Object createUser(final RequestSpecification req, final ResponseSpecification res, final Object roleId,
            final Object staffId, final String username, final String password, final String attr) {
        PostUsersRequest request = new PostUsersRequest().username(username).firstname("Test").lastname("User")
                .email(username + "@mifos.org").officeId(1L).staffId(staffId == null ? null : Long.valueOf(staffId.toString()))
                .roles(List.of(Long.valueOf(roleId.toString()))).password(password).repeatPassword(password).sendPasswordToEmail(false);
        return createUser(request).getResourceId().intValue();
    }

    public static RequestSpecification getSimpleUserWithoutBypassPermission(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        String username = Utils.uniqueRandomStringGenerator("user", 8);
        createUser(requestSpec, responseSpec, 2L, null, username, SIMPLE_USER_PASSWORD, "resourceId");

        // Standard way
        return io.restassured.RestAssured.given().contentType(io.restassured.http.ContentType.JSON).header("Authorization",
                "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey(username, SIMPLE_USER_PASSWORD));
    }

    public static void deleteUser(RequestSpecification req, ResponseSpecification res, Integer userId) {
        deleteUser(userId.longValue());
    }

    public static GetUsersUserIdResponse getUser(final Long userId) {
        return retrieveOneUser(userId);
    }
}
