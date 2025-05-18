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
package org.apache.fineract.commands.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.data.AuditData;
import org.apache.fineract.commands.data.AuditSearchData;
import org.apache.fineract.commands.data.request.AuditRequest;
import org.apache.fineract.commands.service.AuditReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.SQLBuilder;
import org.springframework.stereotype.Component;

@Path("/v1/audits")
@Component
@Tag(name = "Audits", description = "Every non-read Mifos API request is audited. A fully processed request can not be changed or deleted. See maker checker api for situations where an audit is not fully processed.\n"
        + "\n"
        + "Permissions: To search and look at audit entries a user needs to be attached to a role that has one of the ALL_FUNCTIONS, ALL_FUNCTIONS_READ or READ_AUDIT permissions.\n"
        + "\n"
        + "Data Scope: A user can only see audits that are within their data scope. However, 'head office' users can see all audits including those that aren't office/branch related e.g. Loan Product changes.\")")
@RequiredArgsConstructor
public class AuditsApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "AUDIT";

    private final PlatformSecurityContext context;
    private final AuditReadPlatformService auditReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final ToApiJsonSerializer<String> toApiJsonSerializer;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Audits", description = "Get a 200 list of audits that match the criteria supplied and sorted by audit id in descending order, and are within the requestors' data scope. Also it supports pagination and sorting\n"
            + "\n" + "Example Requests:\n" + "\n" + "audits\n" + "\n" + "audits?fields=madeOnDate,maker,processingResult\n" + "\n"
            + "audits?makerDateTimeFrom=2013-03-25 08:00:00&makerDateTimeTo=2013-04-04 18:00:00\n" + "\n" + "audits?officeId=1\n" + "\n"
            + "audits?officeId=1&includeJson=true")
    public String retrieveAuditEntries(@Context final UriInfo uriInfo, @BeanParam AuditRequest auditRequest,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("paged") @Parameter(description = "paged") final Boolean paged) {

        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final PaginationParameters parameters = PaginationParameters.builder().paged(Boolean.TRUE.equals(paged)).limit(limit).offset(offset)
                .orderBy(orderBy).sortOrder(sortOrder).build();
        final SQLBuilder extraCriteria = getExtraCriteria(auditRequest);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return toApiJsonSerializer.serialize(parameters.isPaged()
                ? auditReadPlatformService.retrievePaginatedAuditEntries(extraCriteria, settings.isIncludeJson(), parameters)
                : auditReadPlatformService.retrieveAuditEntries(extraCriteria, settings.isIncludeJson()));
    }

    @GET
    @Path("{auditId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve an Audit Entry", description = "Example Requests:\n" + "\n" + "audits/20\n"
            + "audits/20?fields=madeOnDate,maker,processingResult")
    public AuditData retrieveAuditEntry(@PathParam("auditId") @Parameter final Long auditId) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return auditReadPlatformService.retrieveAuditEntry(auditId);

    }

    @GET
    @Path("/searchtemplate")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Audit Search Template", description = "This is a convenience resource. It can be useful when building an Audit Search UI. \"appUsers\" are data scoped to the office/branch the requestor is associated with.\n"
            + "\n" + "Example Requests:\n" + "\n" + "audits/searchtemplate\n" + "audits/searchtemplate?fields=actionNames")
    public AuditSearchData retrieveAuditSearchTemplate() {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.auditReadPlatformService.retrieveSearchTemplate("audit");
    }

    private SQLBuilder getExtraCriteria(AuditRequest auditRequest) {

        SQLBuilder extraCriteria = new SQLBuilder();
        extraCriteria.addNonNullCriteria("aud.action_name = ", auditRequest.getActionName());
        if (auditRequest.getEntityName() != null) {
            extraCriteria.addCriteria("aud.entity_name like", auditRequest.getEntityName() + "%");
        }
        extraCriteria.addNonNullCriteria("aud.resource_id = ", auditRequest.getResourceId());
        extraCriteria.addNonNullCriteria("aud.maker_id = ", auditRequest.getMakerId());
        extraCriteria.addNonNullCriteria("aud.checker_id = ", auditRequest.getCheckerId());
        if (auditRequest.getMakerDateTimeFrom() != null) {
            extraCriteria.addSubOperation((SQLBuilder criteria) -> {
                criteria.addNonNullCriteria("aud.made_on_date >= ", auditRequest.getMakerDateTimeFrom(),
                        SQLBuilder.WhereLogicalOperator.NONE);
                criteria.addNonNullCriteria("aud.made_on_date_utc >= ", auditRequest.getMakerDateTimeFrom(),
                        SQLBuilder.WhereLogicalOperator.OR);
            });
        }
        if (auditRequest.getMakerDateTimeTo() != null) {
            extraCriteria.addSubOperation((SQLBuilder criteria) -> {
                criteria.addNonNullCriteria("aud.made_on_date <= ", auditRequest.getMakerDateTimeTo(),
                        SQLBuilder.WhereLogicalOperator.NONE);
                criteria.addNonNullCriteria("aud.made_on_date_utc <= ", auditRequest.getMakerDateTimeTo(),
                        SQLBuilder.WhereLogicalOperator.OR);
            });
        }
        if (auditRequest.getCheckerDateTimeFrom() != null) {
            extraCriteria.addSubOperation((SQLBuilder criteria) -> {
                criteria.addNonNullCriteria("aud.checked_on_date >= ", auditRequest.getCheckerDateTimeFrom(),
                        SQLBuilder.WhereLogicalOperator.NONE);
                criteria.addNonNullCriteria("aud.checked_on_date_utc >= ", auditRequest.getCheckerDateTimeFrom(),
                        SQLBuilder.WhereLogicalOperator.OR);
            });
        }
        if (auditRequest.getCheckerDateTimeTo() != null) {
            extraCriteria.addSubOperation((SQLBuilder criteria) -> {
                criteria.addNonNullCriteria("aud.checked_on_date <= ", auditRequest.getCheckerDateTimeTo(),
                        SQLBuilder.WhereLogicalOperator.NONE);
                criteria.addNonNullCriteria("aud.checked_on_date_utc <= ", auditRequest.getCheckerDateTimeTo(),
                        SQLBuilder.WhereLogicalOperator.OR);
            });
        }
        extraCriteria.addNonNullCriteria("aud.status = ", auditRequest.getStatus());
        extraCriteria.addNonNullCriteria("aud.office_id = ", auditRequest.getOfficeId());
        extraCriteria.addNonNullCriteria("aud.group_id = ", auditRequest.getGroupId());
        extraCriteria.addNonNullCriteria("aud.client_id = ", auditRequest.getClientId());
        extraCriteria.addNonNullCriteria("aud.loan_id = ", auditRequest.getLoanId());
        extraCriteria.addNonNullCriteria("aud.savings_account_id = ", auditRequest.getSavingsAccountId());

        return extraCriteria;
    }
}
