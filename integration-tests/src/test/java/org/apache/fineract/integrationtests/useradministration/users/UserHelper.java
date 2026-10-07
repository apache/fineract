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

import static org.apache.fineract.client.util.Calls.ok;
import static org.apache.fineract.client.util.Calls.okR;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import org.apache.fineract.client.models.DeleteUsersUserIdResponse;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.GetUsersResponse;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.client.models.PutUsersUserIdRequest;
import org.apache.fineract.client.models.PutUsersUserIdResponse;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.useradministration.roles.RolesHelper;

public final class UserHelper {

    private UserHelper() {}

    public static PostUsersResponse createUser(Long roleId, Long staffId) {
        final PostUsersRequest request = new PostUsersRequest().staffId(staffId).addRolesItem(roleId);
        return ok(FineractClientHelper.getFineractClient().users.createUser(request));
    }

    public static PostUsersResponse createUser(final Long roleId, final Long staffId, final String username) {
        final PostUsersRequest request = new PostUsersRequest().staffId(staffId).addRolesItem(roleId).username(username);
        return okR(FineractClientHelper.getFineractClient().users.createUser(request)).body();
    }

    public static PostUsersResponse createUser(final Long roleId, final Long staffId, final String username, final String password) {
        final PostUsersRequest request = new PostUsersRequest().staffId(staffId).addRolesItem(roleId).username(username).password(password);
        return okR(FineractClientHelper.getFineractClient().users.createUser(request)).body();
    }

    public static PostUsersResponse createUser(PostUsersRequest request) {
        return ok(FineractClientHelper.getFineractClient().users.createUser(request));
    }

    public static JsonObject createUserWithJsonResponse(PostUsersRequest request) {
        return JsonParser.parseString(ok(FineractClientHelper.getFineractClient().users.createUser(request)).toString()).getAsJsonObject();
    }

    public static Long getUserId(String userName) {
        List<GetUsersResponse> userList = ok(FineractClientHelper.getFineractClient().users.retrieveAllUsers());
        for (GetUsersResponse user : userList) {
            if (user.getUsername() != null && user.getUsername().equals(userName)) {
                return user.getId();
            }
        }

        return null;
    }

    public static DeleteUsersUserIdResponse deleteUser(final Long userId) {
        return ok(FineractClientHelper.getFineractClient().users.deleteUser(userId));
    }

    public static PutUsersUserIdResponse updateUser(final Long userId) {
        final PutUsersUserIdRequest request = new PutUsersUserIdRequest().firstname("Test").lastname("User").email("whatever@mifos.org")
                .officeId(1L);
        return ok(FineractClientHelper.getFineractClient().users.updateUser(userId, request));
    }

    public static PostUsersRequest buildUserRequest(final String password) {
        final Long roleId = RolesHelper.createRole().getResourceId();
        String uniqueUsername = Utils.uniqueRandomStringGenerator("TestUser", 4);
        GetOfficesResponse headOffice = OfficeHelper.getHeadOffice();

        return new PostUsersRequest().username(uniqueUsername).firstname(Utils.randomFirstNameGenerator())
                .lastname(Utils.randomLastNameGenerator()).email("testuser@example.com").password(password).repeatPassword(password)
                .sendPasswordToEmail(false).officeId(headOffice.getId()).roles(List.of(roleId));
    }
}
