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
package org.apache.fineract.infrastructure.openapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.swagger.v3.core.filter.SpecFilter;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.fineract.infrastructure.security.api.AuthenticationApiResource;
import org.apache.fineract.infrastructure.security.api.TenantOidcConfigApiResource;
import org.apache.fineract.infrastructure.security.api.TwoFactorApiResource;
import org.apache.fineract.infrastructure.security.api.TwoFactorConfigurationApiResource;
import org.apache.fineract.infrastructure.security.api.UserDetailsApiResource;
import org.apache.fineract.useradministration.api.PasswordPreferencesApiResource;
import org.apache.fineract.useradministration.api.PermissionsApiResource;
import org.apache.fineract.useradministration.api.RolesApiResource;
import org.apache.fineract.useradministration.api.UsersApiResource;
import org.junit.jupiter.api.Test;

class TypedSecurityApiOpenApiTest {

    @Test
    void preservesOperationIdsAndBothGeneratedClientNamingContracts() {
        OpenAPI api = snapshot();
        Map<String, String> actual = new LinkedHashMap<>();
        api.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
            Object alternative = operation.getExtensions() == null ? null
                    : operation.getExtensions().get(FineractOperationIdReader.ALTERNATIVE_OPERATION_ID_EXTENSION);
            actual.put(method.name() + " " + path, operation.getOperationId() + "|" + Objects.toString(alternative, ""));
        }));
        // Captured from these resources before changing their String return types (FINERACT-3184).
        Map<String, String> expected = Map.ofEntries(Map.entry("POST /v1/authentication", "authenticate|"),
                Map.entry("GET /v1/passwordpreferences", "retrieveAllPasswordPreferences|retrieve_1"),
                Map.entry("PUT /v1/passwordpreferences", "updatePasswordPreferences|update_25"),
                Map.entry("GET /v1/passwordpreferences/template", "retrieveTemplatePasswordPreferences|template_21"),
                Map.entry("GET /v1/permissions", "retrieveAllPermissions|"),
                Map.entry("PUT /v1/permissions", "updatePermissions|updatePermissionsDetails"),
                Map.entry("GET /v1/roles", "retrieveAllRoles|"), Map.entry("POST /v1/roles", "createRole|"),
                Map.entry("DELETE /v1/roles/{roleId}", "deleteRole|"), Map.entry("GET /v1/roles/{roleId}", "retrieveOneRole|retrieveRole"),
                Map.entry("POST /v1/roles/{roleId}", "handleCommandsRole|actionsOnRoles"),
                Map.entry("PUT /v1/roles/{roleId}", "updateRole|"),
                Map.entry("GET /v1/roles/{roleId}/permissions", "retrieveRolePermissions|"),
                Map.entry("PUT /v1/roles/{roleId}/permissions", "updateRolePermissions|"),
                Map.entry("DELETE /v1/tenants/{tenantId}/oidc-config", "delete|"),
                Map.entry("GET /v1/tenants/{tenantId}/oidc-config", "retrieve|"),
                Map.entry("POST /v1/tenants/{tenantId}/oidc-config", "create|"),
                Map.entry("PUT /v1/tenants/{tenantId}/oidc-config", "update|"), Map.entry("GET /v1/twofactor", "getOTPDeliveryMethods|"),
                Map.entry("POST /v1/twofactor", "requestToken|"), Map.entry("GET /v1/twofactor/configure", "retrieveAll_4|retrieveAll_9"),
                Map.entry("PUT /v1/twofactor/configure", "updateConfiguration_1|updateConfiguration_3"),
                Map.entry("POST /v1/twofactor/invalidate", "updateConfiguration|updateConfiguration_2"),
                Map.entry("POST /v1/twofactor/validate", "validate|"), Map.entry("GET /v1/userdetails", "fetchAuthenticatedUserData|"),
                Map.entry("GET /v1/users", "retrieveAllUsers|retrieveAll_41"), Map.entry("POST /v1/users", "createUser|create_15"),
                Map.entry("GET /v1/users/downloadtemplate", "getBulkTemplateUser|getUserTemplate"),
                Map.entry("GET /v1/users/template", "retrieveTemplateUser|template_22"),
                Map.entry("POST /v1/users/uploadtemplate", "postBulkTemplateUser|postUsersTemplate"),
                Map.entry("DELETE /v1/users/{userId}", "deleteUser|delete_23"),
                Map.entry("GET /v1/users/{userId}", "retrieveOneUser|retrieveOne_31"),
                Map.entry("PUT /v1/users/{userId}", "updateUser|update_26"),
                Map.entry("POST /v1/users/{userId}/pwd", "changePasswordUser|changePassword"));
        assertEquals(expected, actual);
    }

    @Test
    void preservesResponseDeclarationsAndRemovesUnreferencedRuntimeDtos() {
        OpenAPI api = snapshot();
        api.getPaths().get("/v1/tenants/{tenantId}/oidc-config").readOperations().forEach(operation -> {
            assertEquals(Set.of("default"), operation.getResponses().keySet());
            assertEquals("string", operation.getResponses().get("default").getContent().get("application/json").getSchema().getType());
        });
        assertEquals("string", api.getPaths().get("/v1/users/uploadtemplate").getPost().getResponses().get("default").getContent()
                .get("*/*").getSchema().getType());
        assertNull(api.getPaths().get("/v1/passwordpreferences").getPut().getResponses().get("200").getContent());
        for (String runtimeDto : Set.of("AppUserData", "AuthenticatedUserData", "AuthenticatedOauthUserData", "TenantOidcConfigData",
                "OTPMetadata", "AccessTokenData")) {
            assertFalse(api.getComponents().getSchemas().containsKey(runtimeDto), runtimeDto);
        }
    }

    private OpenAPI snapshot() {
        OpenAPI api = new FineractOperationIdReader()
                .read(Set.of(UsersApiResource.class, RolesApiResource.class, PermissionsApiResource.class,
                        PasswordPreferencesApiResource.class, TenantOidcConfigApiResource.class, TwoFactorApiResource.class,
                        TwoFactorConfigurationApiResource.class, AuthenticationApiResource.class, UserDetailsApiResource.class), Map.of());
        return new SpecFilter().filter(api, new FineractOpenApiSpecFilter(), Map.of(), Map.of(), Map.of());
    }
}
