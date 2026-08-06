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
package org.apache.fineract.infrastructure.security.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.TenantOidcConfig;
import org.apache.fineract.infrastructure.core.exception.ResourceNotFoundException;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.data.TenantOidcConfigData;
import org.apache.fineract.infrastructure.security.domain.OidcFederationType;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.TenantOidcConfigService;
import org.springframework.stereotype.Component;

/**
 * REST resource for managing per-tenant OIDC/IdP configurations.
 *
 * <p>
 * All endpoints require the {@code MANAGE_TENANT_OIDC_CONFIG} permission (Super Admin only). The {@code clientSecret}
 * is accepted on write operations but is never returned in responses.
 */
@Path("/v1/tenants/{tenantId}/oidc-config")
@Component
@Tag(name = "Tenant OIDC Configuration", description = "Manage per-tenant OIDC/IdP configuration stored in the master database. "
        + "Each tenant can have exactly one IdP configured (Keycloak, Google, Azure AD, Okta, etc.). "
        + "The issuerUri is matched against the JWT 'iss' claim for automatic tenant resolution.")
@RequiredArgsConstructor
public class TenantOidcConfigApiResource {

    private static final String PERMISSION = "MANAGE_TENANT_OIDC_CONFIG";

    private final PlatformSecurityContext context;
    private final TenantOidcConfigService tenantOidcConfigService;
    private final ToApiJsonSerializer<TenantOidcConfigData> apiJsonSerializer;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve OIDC configuration for a tenant", description = "Returns the current OIDC/IdP configuration for the given tenant. "
            + "The clientSecret is never included in the response.")
    public String retrieve(@Parameter(description = "tenantId") @PathParam("tenantId") String tenantId) {
        context.authenticatedUser().validateHasPermissionTo(PERMISSION);

        TenantOidcConfig config = tenantOidcConfigService.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("tenantOidcConfig.not.found",
                        "No OIDC configuration found for tenant: " + tenantId, new Object[] { tenantId }));

        return apiJsonSerializer.serialize(TenantOidcConfigData.from(config));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create OIDC configuration for a tenant", description = "Creates a new OIDC/IdP configuration for the given tenant. "
            + "The clientSecret is encrypted at rest and never returned in responses. "
            + "The issuerUri must be globally unique across all tenants.")
    public String create(@Parameter(description = "tenantId") @PathParam("tenantId") String tenantId, String requestBody) {
        context.authenticatedUser().validateHasPermissionTo(PERMISSION);

        TenantOidcConfig config = parseRequest(tenantId, requestBody, null);
        TenantOidcConfig saved = tenantOidcConfigService.save(config);
        return apiJsonSerializer.serialize(TenantOidcConfigData.from(saved));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update OIDC configuration for a tenant", description = "Updates the existing OIDC/IdP configuration for the given tenant. "
            + "If clientSecret is omitted in the request body, the existing encrypted secret is preserved.")
    public String update(@Parameter(description = "tenantId") @PathParam("tenantId") String tenantId, String requestBody) {
        context.authenticatedUser().validateHasPermissionTo(PERMISSION);

        TenantOidcConfig existing = tenantOidcConfigService.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("tenantOidcConfig.not.found",
                        "No OIDC configuration found for tenant: " + tenantId, new Object[] { tenantId }));

        TenantOidcConfig updated = parseRequest(tenantId, requestBody, existing);
        TenantOidcConfig saved = tenantOidcConfigService.save(updated);
        return apiJsonSerializer.serialize(TenantOidcConfigData.from(saved));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete OIDC configuration for a tenant", description = "Removes the OIDC/IdP configuration for the given tenant. "
            + "After deletion, authentication via this tenant's IdP will no longer work.")
    public String delete(@Parameter(description = "tenantId") @PathParam("tenantId") String tenantId) {
        context.authenticatedUser().validateHasPermissionTo(PERMISSION);

        tenantOidcConfigService.findByTenantId(tenantId).orElseThrow(() -> new ResourceNotFoundException("tenantOidcConfig.not.found",
                "No OIDC configuration found for tenant: " + tenantId, new Object[] { tenantId }));

        tenantOidcConfigService.deleteByTenantId(tenantId);
        return "{}";
    }

    private TenantOidcConfig parseRequest(String tenantId, String body, TenantOidcConfig existing) {
        JsonElement json = JsonParser.parseString(body);
        var obj = json.getAsJsonObject();

        String providerTypeStr = getStringOrDefault(obj, "providerType",
                existing != null ? existing.getProviderType().name() : OidcFederationType.GENERIC.name());
        String issuerUri = getStringOrDefault(obj, "issuerUri", existing != null ? existing.getIssuerUri() : null);
        String clientId = getStringOrDefault(obj, "clientId", existing != null ? existing.getClientId() : null);
        // If clientSecret is absent from request, preserve the existing encrypted value (null for new records)
        String clientSecret = obj.has("clientSecret") && !obj.get("clientSecret").isJsonNull() ? obj.get("clientSecret").getAsString()
                : (existing != null ? existing.getClientSecret() : null);
        String jwksUri = getStringOrDefault(obj, "jwksUri", existing != null ? existing.getJwksUri() : null);
        String usernameClaim = getStringOrDefault(obj, "usernameClaim",
                existing != null ? existing.getUsernameClaim() : "preferred_username");
        String scopes = getStringOrDefault(obj, "scopes", existing != null ? existing.getScopes() : "openid,profile,email");
        String postLogoutRedirectUri = getStringOrDefault(obj, "postLogoutRedirectUri",
                existing != null ? existing.getPostLogoutRedirectUri() : null);
        boolean enabled = obj.has("enabled") ? obj.get("enabled").getAsBoolean() : (existing == null || existing.isEnabled());

        return TenantOidcConfig.builder().id(existing != null ? existing.getId() : null).tenantId(tenantId)
                .providerType(OidcFederationType.valueOf(providerTypeStr.toUpperCase(Locale.ROOT))).issuerUri(issuerUri).clientId(clientId)
                .clientSecret(clientSecret).jwksUri(jwksUri).usernameClaim(usernameClaim).scopes(scopes)
                .postLogoutRedirectUri(postLogoutRedirectUri).enabled(enabled).build();
    }

    private String getStringOrDefault(com.google.gson.JsonObject obj, String key, String defaultValue) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : defaultValue;
    }
}
