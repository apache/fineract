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

import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.AuditData;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.GetPermissionsResponse;
import org.apache.fineract.client.models.PutPermissionsRequest;

/** Typed Feign helper for maker-checker permissions, the pending command queue and audit entries. */
public class FeignMakerCheckerHelper {

    private final FineractFeignClient fineractClient;

    public FeignMakerCheckerHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public boolean isMakerCheckerEnabled(String permissionCode) {
        return ok(() -> fineractClient.permissions().retrieveAllPermissionsUniversal(Map.of("makerCheckerable", true))).stream()
                .filter(permission -> permissionCode.equals(permission.getCode())).findFirst().map(GetPermissionsResponse::getSelected)
                .orElseThrow(() -> new IllegalStateException("No maker-checkerable permission " + permissionCode));
    }

    public CommandProcessingResult setMakerCheckerEnabled(String permissionCode, boolean enabled) {
        return ok(() -> fineractClient.permissions()
                .updatePermissionsDetails(new PutPermissionsRequest().putPermissionsItem(permissionCode, enabled)));
    }

    public List<AuditData> pendingCommands(String actionName, String entityName, Long resourceId, Long makerId) {
        return ok(() -> fineractClient.makerCheckerOr4EyeFunctionality().retrieveCommandsUniversal(
                Map.of("actionName", actionName, "entityName", entityName, "resourceId", resourceId, "makerId", makerId), Map.of()));
    }

    public CommandProcessingResult approve(Long auditId) {
        return ok(() -> fineractClient.makerCheckerOr4EyeFunctionality().approveMakerCheckerEntry(auditId, "approve"));
    }

    public AuditData retrieveAuditEntry(Long auditId) {
        return ok(() -> fineractClient.audits().retrieveAuditEntry(auditId));
    }
}
