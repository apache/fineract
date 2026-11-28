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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostRolesRequest;
import org.apache.fineract.client.models.PutRolesRoleIdPermissionsRequest;
import org.apache.fineract.test.api.FineractClientConfiguration;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@RequiredArgsConstructor
public class RolePermissionsStepDef extends AbstractStepDef {

    private static final String ROLE_ID = "rolePermissionsRoleId";
    private static final long SUPER_USER_ROLE_ID = 1L;

    private final FineractFeignClient fineractClient;
    private final FineractClientConfiguration fineractClientConfiguration;

    @When("Admin creates a role named {string}")
    public void createRole(final String name) {
        final PostRolesRequest request = new PostRolesRequest().name(Utils.randomStringGenerator(name, 8)).description(name);
        final Long roleId = ok(() -> fineractClient.roles().createRole(request)).getResourceId();
        testContext().set(ROLE_ID, roleId);
    }

    @Then("Created user cannot grant permission {string} to the last created role")
    public void createdUserCannotGrant(final String permission) {
        final Long roleId = testContext().get(ROLE_ID);
        final CallFailedRuntimeException exception = fail(
                () -> createdUserClient().roles().updateRolePermissions(roleId, permissionRequest(permission)));
        assertThat(exception.getStatus()).isEqualTo(403);
    }

    @Then("Created user can grant permission {string} to the last created role")
    public void createdUserCanGrant(final String permission) {
        final Long roleId = testContext().get(ROLE_ID);
        ok(() -> createdUserClient().roles().updateRolePermissions(roleId, permissionRequest(permission)));
    }

    @Then("Created user cannot modify the permissions of the super user role")
    public void createdUserCannotModifySuperUserRole() {
        final CallFailedRuntimeException exception = fail(
                () -> createdUserClient().roles().updateRolePermissions(SUPER_USER_ROLE_ID, permissionRequest("READ_ROLE")));
        assertThat(exception.getStatus()).isEqualTo(403);
    }

    @Then("Admin can modify the permissions of the super user role")
    public void adminCanModifySuperUserRole() {
        ok(() -> fineractClient.roles().updateRolePermissions(SUPER_USER_ROLE_ID, permissionRequest("READ_ROLE")));
    }

    private static PutRolesRoleIdPermissionsRequest permissionRequest(final String permission) {
        return new PutRolesRoleIdPermissionsRequest().permissions(Map.of(permission, true));
    }

    private FineractFeignClient createdUserClient() {
        final String username = testContext().get(TestContextKey.CREATED_SIMPLE_USER_USERNAME);
        final String password = testContext().get(TestContextKey.CREATED_SIMPLE_USER_PASSWORD);
        return fineractClientConfiguration.fineractFeignClientForUser(username, password);
    }
}
