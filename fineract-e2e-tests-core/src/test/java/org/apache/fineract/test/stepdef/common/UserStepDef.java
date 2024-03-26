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
package org.apache.fineract.test.stepdef.common;

import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.GetRolesResponse;
import org.apache.fineract.client.models.PostRolesRequest;
import org.apache.fineract.client.models.PostRolesResponse;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.client.models.PutRolesRoleIdPermissionsRequest;
import org.apache.fineract.client.models.PutRolesRoleIdPermissionsResponse;
import org.apache.fineract.client.services.RolesApi;
import org.apache.fineract.client.services.UsersApi;
import org.apache.fineract.test.api.ApiProperties;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

public class UserStepDef extends AbstractStepDef {

    private static final String EMAIL = "test@test.com";

    @Autowired
    private RolesApi rolesApi;

    @Autowired
    private UsersApi usersApi;

    @Autowired
    private ApiProperties apiProperties;

    @When("Admin creates new user with {string} username, {string} role name and given permissions:")
    public void createUserWithUsernameAndRoles(String username, String roleName, List<String> permissions) throws IOException {
        Response<List<GetRolesResponse>> retrieveAllRolesResponse = rolesApi.retrieveAllRoles().execute();
        ErrorHelper.checkSuccessfulApiCall(retrieveAllRolesResponse);
        PostRolesRequest newRoleRequest = new PostRolesRequest().name(Utils.randomNameGenerator(roleName, 8)).description(roleName);
        Response<PostRolesResponse> createNewRole = rolesApi.createRole(newRoleRequest).execute();
        ErrorHelper.checkSuccessfulApiCall(createNewRole);
        Long roleId = createNewRole.body().getResourceId();
        Map<String, Boolean> permissionMap = new HashMap<>();
        permissions.forEach(role -> permissionMap.put(role, true));
        PutRolesRoleIdPermissionsRequest putRolesRoleIdPermissionsRequest = new PutRolesRoleIdPermissionsRequest()
                .permissions(permissionMap);
        Response<PutRolesRoleIdPermissionsResponse> updateRolePermissionResponse = rolesApi
                .updateRolePermissions(roleId, putRolesRoleIdPermissionsRequest).execute();
        ErrorHelper.checkSuccessfulApiCall(updateRolePermissionResponse);

        PostUsersRequest postUsersRequest = new PostUsersRequest() //
                .username(Utils.randomNameGenerator(username, 8)) //
                .email(EMAIL) //
                .firstname(username) //
                .lastname(username) //
                .sendPasswordToEmail(Boolean.FALSE) //
                .officeId(1L) //
                .password(apiProperties.getPassword()) //
                .repeatPassword(apiProperties.getPassword()) //
                .roles(List.of(roleId));

        Response<PostUsersResponse> createUserResponse = usersApi.create15(postUsersRequest).execute();
        ErrorHelper.checkSuccessfulApiCall(createUserResponse);
        testContext().set(TestContextKey.CREATED_SIMPLE_USER_RESPONSE, createUserResponse);
    }
}
