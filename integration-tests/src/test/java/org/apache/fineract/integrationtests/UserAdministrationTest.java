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

package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.fineract.client.models.GetUsersUserIdResponse;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.integrationtests.useradministration.roles.RolesHelper;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.junit.jupiter.api.Test;

public class UserAdministrationTest {

    @Test
    public void testCreateAndFetchUser() {
        Long roleId = null;
        Long userId = null;

        try {
            // Create Role
            roleId = RolesHelper.createRole();
            assertNotNull(roleId, "Role ID should not be null");

            // Extra Safety: Explicitly enable role to prevent flaky failures
            RolesHelper.enableRole(roleId);

            // Build User Request
            PostUsersRequest request = UserHelper.buildUserRequest("Password@123", roleId);

            // Create User
            PostUsersResponse response = UserHelper.createUser(request);
            userId = response.getResourceId();
            assertNotNull(userId, "User ID should not be null");

            // Fetch and Validate User
            GetUsersUserIdResponse user = UserHelper.getUser(userId);
            assertNotNull(user, "Retrieved user should not be null");

        } finally {
            // Cleanup - Essential for shared integration environments
            if (userId != null) {
                UserHelper.deleteUser(userId);
            }
            if (roleId != null) {
                RolesHelper.deleteRole(roleId);
            }
        }
    }
}
