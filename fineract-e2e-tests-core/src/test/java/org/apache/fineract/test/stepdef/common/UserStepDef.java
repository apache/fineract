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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import io.cucumber.java.en.When;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.PostRolesRequest;
import org.apache.fineract.client.models.PostRolesResponse;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.client.models.PutRolesRoleIdPermissionsRequest;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

public class UserStepDef extends AbstractStepDef {

    private static final String EMAIL = "test@test.com";

    @Autowired
    private FineractFeignClient fineractClient;

    private static final String PWD_USER_WITH_ROLE = "1234567890Aa!";

    @When("Admin creates new user with {string} username, {string} role name and given permissions:")
    public void createUserWithUsernameAndRoles(String username, String roleName, List<String> permissions) {
        ok(() -> fineractClient.roles().retrieveAllRoles());
        PostRolesRequest newRoleRequest = new PostRolesRequest().name(Utils.randomNameGenerator(roleName, 8)).description(roleName);
        PostRolesResponse createNewRole = ok(() -> fineractClient.roles().createRole(newRoleRequest));
        Long roleId = createNewRole.getResourceId();
        Map<String, Boolean> permissionMap = new HashMap<>();
        permissions.forEach(role -> permissionMap.put(role, true));
        PutRolesRoleIdPermissionsRequest putRolesRoleIdPermissionsRequest = new PutRolesRoleIdPermissionsRequest()
                .permissions(permissionMap);
        ok(() -> fineractClient.roles().updateRolePermissions(roleId, putRolesRoleIdPermissionsRequest));

        String generatedUsername = Utils.randomNameGenerator(username, 8);
        PostUsersRequest postUsersRequest = new PostUsersRequest() //
                .username(generatedUsername) //
                .email(EMAIL) //
                .firstname(username) //
                .lastname(username) //
                .sendPasswordToEmail(Boolean.FALSE) //
                .officeId(1L) //
                .password(PWD_USER_WITH_ROLE) //
                .repeatPassword(PWD_USER_WITH_ROLE) //
                .roles(List.of(roleId));

        PostUsersResponse createUserResponse = ok(() -> fineractClient.users().create15(postUsersRequest));
        testContext().set(TestContextKey.CREATED_SIMPLE_USER_RESPONSE, createUserResponse);
        testContext().set(TestContextKey.CREATED_SIMPLE_USER_USERNAME, generatedUsername);
        testContext().set(TestContextKey.CREATED_SIMPLE_USER_PASSWORD, PWD_USER_WITH_ROLE);
    }
}
