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
package org.apache.fineract.portfolio.group.api.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.group.api.GroupingTypesApiConstants;
import org.apache.fineract.portfolio.group.data.CenterData;
import org.springframework.stereotype.Component;

@Path("/v2/centers")
@Component
@Tag(name = "CentersV2", description = "V2 endpoint that always returns a paged response (totalFilteredRecords + pageItems) for centers, removing the ambiguous paged response typing of the v1 endpoint. The non-paged meeting-date lookup branch of the v1 endpoint is intentionally omitted.")
@RequiredArgsConstructor
public class CentersV2ApiResource {

    private final PlatformSecurityContext context;
    private final CentersV2ApiDelegate delegate;
    private final SqlValidator sqlValidator;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Centers", operationId = "retrieveAllCentersV2", description = "Get a paged list of centers that match the criteria supplied and sorted by hierarchy. Unlike the v1 endpoint, this endpoint always returns a paged response containing totalFilteredRecords and pageItems.\n\n"
            + "Example Requests:\n\n" + "centers\n\n" + "centers?fields=name,officeName,joinedDate\n\n" + "centers?offset=10&limit=50\n\n"
            + "centers?orderBy=name&sortOrder=DESC")
    public Page<CenterData> retrieveAllCenters(@QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @QueryParam("staffId") @Parameter(description = "staffId") final Long staffId,
            @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("name") @Parameter(description = "name") final String name,
            @QueryParam("underHierarchy") @Parameter(description = "underHierarchy") final String hierarchy,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.CENTER_RESOURCE_NAME);
        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        sqlValidator.validate(externalId);
        sqlValidator.validate(hierarchy);
        final PaginationParameters parameters = PaginationParameters.builder().paged(true).limit(limit).offset(offset).orderBy(orderBy)
                .sortOrder(sortOrder).build();
        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).officeId(officeId).externalId(externalId)
                .name(name).hierarchy(hierarchy).offset(offset).orderBy(orderBy).sortOrder(sortOrder).staffId(staffId).build();
        return delegate.retrieveAllCenters(searchParameters, parameters);
    }
}
