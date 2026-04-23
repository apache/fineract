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

import org.apache.fineract.client.models.GetRolesRoleIdResponse;
import org.apache.fineract.integrationtests.useradministration.roles.RolesHelper;
import org.junit.jupiter.api.Test;

public class RolesTest {

    @Test
    public void testCreateAndFetchRole() {
        Long roleId = null;

        try {
            // Create Role using modernized static helper
            roleId = RolesHelper.createRole();
            assertNotNull(roleId, "Role ID should not be null after creation");

            // Fetch the created role
            GetRolesRoleIdResponse role = RolesHelper.getRole(roleId);

            // Stronger Assertions: Validate object and its internal state
            assertNotNull(role, "Retrieved role should not be null");
            assertNotNull(role.getId(), "Role ID in response should not be null");

        } finally {
            // Cleanup - Crucial for maintaining clean integration environment
            if (roleId != null) {
                RolesHelper.deleteRole(roleId);
            }
        }
    }
}
