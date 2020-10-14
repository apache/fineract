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
package org.apache.fineract.infrastructure.report.util;

import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.fineract.api.ReportingProcessService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;

public final class ParameterUtil {

    private ParameterUtil() {}

    public static Map<String, String> toSingleValueMap(MultivaluedMap<String, String> parameters) {
        return parameters.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));
    }

    public static void addMissingDefaults(Map<String, String> parameters, PlatformSecurityContext context) {
        // TODO: @vidakovic need to fix this to make reporting service implementation completely independent of internal
        // Fineract implementation classes
        AppUser currentUser = context.authenticatedUser();
        parameters.put(ReportingProcessService.PARAMETER_PREFIX + ReportingProcessService.PARAM_USER_ID, currentUser.getId() + "");

        parameters.put(ReportingProcessService.PARAMETER_PREFIX + ReportingProcessService.PARAM_USER_HIERARCHIE,
                currentUser.getOffice().getHierarchy());

        FineractPlatformTenantConnection tenantConnection = ThreadLocalContextUtil.getTenant().getConnection();
        String tenantUrl = ""; // TODO: fix this // driverConfig.constructProtocol(tenantConnection.getSchemaServer(),
                               // tenantConnection.getSchemaServerPort(), tenantConnection.getSchemaName());
        parameters.put(ReportingProcessService.PARAMETER_PREFIX + ReportingProcessService.PARAM_TENANT_URL, tenantUrl);
        parameters.put(ReportingProcessService.PARAMETER_PREFIX + ReportingProcessService.PARAM_USERNAME,
                tenantConnection.getSchemaUsername());
        parameters.put(ReportingProcessService.PARAMETER_PREFIX + ReportingProcessService.PARAM_PASSWORD,
                tenantConnection.getSchemaPassword());
    }
}
