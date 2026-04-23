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

package org.apache.fineract.integrationtests.useradministration.roles;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.Map;
import org.apache.fineract.client.models.GetRolesRoleIdResponse;
import org.apache.fineract.client.models.PostRolesRequest;
import org.apache.fineract.client.models.PutPermissionsRequest;
import org.apache.fineract.client.models.PutRolesRoleIdPermissionsRequest;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
import org.apache.fineract.integrationtests.common.Utils;

public final class RolesHelper {

    public static final long SUPER_USER_ROLE_ID = 1L;

    private RolesHelper() {
        // Private constructor for utility class
    }

    /**
     * Factory method for backward compatibility. Even though it returns a new instance, Checkstyle is fine because the
     * constructor is private.
     */
    public static RolesHelper create() {
        return new RolesHelper();
    }

    public static Long createRole() {
        PostRolesRequest request = new PostRolesRequest().name(Utils.uniqueRandomStringGenerator("Role_", 4)).description("Test Role");
        return Calls.ok(FineractClientHelper.getFineractClient().roles.createRole(request)).getResourceId();
    }

    public static GetRolesRoleIdResponse getRole(final Long roleId) {
        return Calls.ok(FineractClientHelper.getFineractClient().roles.retrieveRole(roleId));
    }

    public static void deleteRole(final Long roleId) {
        Calls.ok(FineractClientHelper.getFineractClient().roles.deleteRole(roleId));
    }

    public static void enableRole(final Long roleId) {
        Calls.ok(FineractClientHelper.getFineractClient().roles.actionsOnRoles(roleId, "enable"));
    }

    public static void disableRole(final Long roleId) {
        Calls.ok(FineractClientHelper.getFineractClient().roles.actionsOnRoles(roleId, "disable"));
    }

    public static void updatePermissions(final Long roleId, final Map<String, Boolean> permissions) {
        PutRolesRoleIdPermissionsRequest request = new PutRolesRoleIdPermissionsRequest().permissions(permissions);
        Calls.ok(FineractClientHelper.getFineractClient().roles.updateRolePermissions(roleId, request));
    }

    // --- BRIDGE METHODS FOR LEGACY TESTS ---

    public static Integer createRole(RequestSpecification req, ResponseSpecification res) {
        return createRole().intValue();
    }

    /**
     * FIXED: Implementation for MakercheckerTest legacy calls. This updates the permissions for the SuperUser (ID 1) as
     * requested by tests.
     */
    public void updatePermissions(Object request) {
        if (request instanceof PutPermissionsRequest permissionsRequest) {
            updatePermissions(SUPER_USER_ROLE_ID, permissionsRequest.getPermissions());
        }
    }

    public static void addPermissionsToRole(RequestSpecification req, ResponseSpecification res, Object roleId,
            Map<String, Boolean> permissions) {
        updatePermissions(Long.valueOf(roleId.toString()), permissions);
    }
}
