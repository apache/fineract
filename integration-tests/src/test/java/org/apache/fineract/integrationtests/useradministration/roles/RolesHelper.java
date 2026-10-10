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

import static org.apache.fineract.client.util.Calls.ok;

import java.util.Map;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.DeleteRolesRoleIdResponse;
import org.apache.fineract.client.models.GetRolesRoleIdResponse;
import org.apache.fineract.client.models.PostRolesRequest;
import org.apache.fineract.client.models.PostRolesResponse;
import org.apache.fineract.client.models.PostRolesRoleIdResponse;
import org.apache.fineract.client.models.PutPermissionsRequest;
import org.apache.fineract.client.models.PutRolesRoleIdPermissionsRequest;
import org.apache.fineract.client.models.PutRolesRoleIdPermissionsResponse;
import org.apache.fineract.integrationtests.common.FineractClientHelper;

public final class RolesHelper {

    public static final long SUPER_USER_ROLE_ID = 1L; // This is hardcoded into the initial Liquibase migration

    private static final String DISABLE_ROLE_COMMAND = "disable";
    private static final String ENABLE_ROLE_COMMAND = "enable";
    private static final String DEFAULT_ROLE_NAME = "default_role_name";

    private RolesHelper() {}

    public static PostRolesResponse createRole() {
        final PostRolesRequest request = new PostRolesRequest().name(DEFAULT_ROLE_NAME);
        return ok(FineractClientHelper.getFineractClient().roles.createRole(request));
    }

    public static GetRolesRoleIdResponse getRoleDetails(final Long roleId) {
        return ok(FineractClientHelper.getFineractClient().roles.retrieveOneRole(roleId));
    }

    public static PostRolesRoleIdResponse disableRole(final Long roleId) {
        return ok(FineractClientHelper.getFineractClient().roles.handleCommandsRole(roleId, DISABLE_ROLE_COMMAND));
    }

    public static PostRolesRoleIdResponse enableRole(final Long roleId) {
        return ok(FineractClientHelper.getFineractClient().roles.handleCommandsRole(roleId, ENABLE_ROLE_COMMAND));
    }

    public static DeleteRolesRoleIdResponse deleteRole(final Long roleId) {
        return ok(FineractClientHelper.getFineractClient().roles.deleteRole(roleId));
    }

    public static PutRolesRoleIdPermissionsResponse addPermissionsToRole(final Long roleId, final Map<String, Boolean> permissionMap) {
        final PutRolesRoleIdPermissionsRequest request = new PutRolesRoleIdPermissionsRequest();
        request.setPermissions(permissionMap);
        return ok(FineractClientHelper.getFineractClient().roles.updateRolePermissions(roleId, request));
    }

    public static CommandProcessingResult updatePermissions(PutPermissionsRequest request) {
        return ok(FineractClientHelper.getFineractClient().permissions.updatePermissions(request));
    }
}
