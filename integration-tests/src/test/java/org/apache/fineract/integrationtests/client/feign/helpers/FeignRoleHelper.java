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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.PostRolesRequest;
import org.apache.fineract.client.models.PostRolesResponse;
import org.apache.fineract.client.models.PutRolesRoleIdPermissionsRequest;
import org.apache.fineract.client.models.PutRolesRoleIdPermissionsResponse;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;

/** Typed Feign helper for role and role-permission operations. */
public final class FeignRoleHelper {

    private FeignRoleHelper() {}

    private static FineractFeignClient client() {
        return FineractFeignClientHelper.getFineractFeignClient();
    }

    /** Creates a role with a generated name and description, and returns its id. */
    public static Long createRole() {
        PostRolesResponse response = ok(
                () -> client().roles().createRole(new PostRolesRequest().name(Utils.uniqueRandomStringGenerator("Role_Name_", 5))
                        .description(Utils.randomStringGenerator("Role_Description_", 10))));
        return response.getResourceId();
    }

    /**
     * Grants or revokes the named permissions on a role. The endpoint merges the map into the role's existing
     * permissions, so a call only has to name the ones it changes.
     */
    public static PutRolesRoleIdPermissionsResponse addPermissionsToRole(Long roleId, Map<String, Boolean> permissions) {
        return ok(() -> client().roles().updateRolePermissions(roleId, new PutRolesRoleIdPermissionsRequest().permissions(permissions)));
    }
}
