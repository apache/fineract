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
package org.apache.fineract.commands.api.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.data.AuditData;
import org.apache.fineract.commands.data.request.AuditRequest;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@Path("/v2/audits")
@Component
@Tag(name = "AuditsV2", description = "V2 endpoint that always returns a paged response (totalFilteredRecords + pageItems) for audits, removing the ambiguous paged response typing of the v1 endpoint.")
@RequiredArgsConstructor
public class AuditsV2ApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "AUDIT";

    private final PlatformSecurityContext context;
    private final AuditsV2ApiDelegate delegate;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Audits", operationId = "retrieveAllAuditsV2", description = "Get a paged list of audits that match the criteria supplied and sorted by audit id in descending order, and are within the requestors' data scope. Unlike the v1 endpoint, this endpoint always returns a paged response containing totalFilteredRecords and pageItems.\n\n"
            + "Example Requests:\n\n" + "audits\n\n" + "audits?fields=madeOnDate,maker,processingResult\n\n" + "audits?officeId=1\n\n"
            + "audits?officeId=1&includeJson=true")
    public Page<AuditData> retrieveAllAudits(@Context final UriInfo uriInfo, @BeanParam AuditRequest auditRequest,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final boolean includeJson = apiRequestParameterHelper.process(uriInfo.getQueryParameters()).isIncludeJson();
        return delegate.retrieveAllAudits(uriInfo, auditRequest, offset, limit, orderBy, sortOrder, includeJson);
    }
}
