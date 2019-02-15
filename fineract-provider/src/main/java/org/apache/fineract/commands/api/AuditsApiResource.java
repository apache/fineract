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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.data.AuditData;
import org.apache.fineract.commands.data.AuditSearchData;
import org.apache.fineract.commands.service.AuditReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/audits")
@Component
@Scope("singleton")
@Api(value = "Audits",description = "Every non-read Mifos API request is audited. A fully processed request can not be changed or deleted. See maker checker api for situations where an audit is not fully processed.\n" + "\n" + "Permissions: To search and look at audit entries a user needs to be attached to a role that has one of the ALL_FUNCTIONS, ALL_FUNCTIONS_READ or READ_AUDIT permissions.\n" + "\n" + "Data Scope: A user can only see audits that are within their data scope. However, 'head office' users can see all audits including those that aren't office/branch related e.g. Loan Product changes.")
public class AuditsApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "actionName", "entityName", "resourceId",
            "subresourceId", "maker", "madeOnDate", "checker", "checkedOnDate", "processingResult", "commandAsJson", "officeName",
            "groupLevelName", "groupName", "clientName", "loanAccountNo", "savingsAccountNo", "clientId", "loanId", "url"));

    private final String resourceNameForPermissions = "AUDIT";

    private final PlatformSecurityContext context;
    private final AuditReadPlatformService auditReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<AuditSearchData> toApiJsonSerializerSearchTemplate;

    @Autowired
    public AuditsApiResource(final PlatformSecurityContext context, final AuditReadPlatformService auditReadPlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper, final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializer,
            final DefaultToApiJsonSerializer<AuditSearchData> toApiJsonSerializerSearchTemplate) {
        this.context = context;
        this.auditReadPlatformService = auditReadPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.toApiJsonSerializerSearchTemplate = toApiJsonSerializerSearchTemplate;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Audits", notes = "Get a 200 list of audits that match the criteria supplied and sorted by audit id in descending order, and are within the requestors' data scope. Also it supports pagination and sorting\n" + "\n" + "Example Requests:\n" + "\n" + "audits\n" + "\n" + "audits?fields=madeOnDate,maker,processingResult\n" + "\n" + "audits?makerDateTimeFrom=2013-03-25 08:00:00&makerDateTimeTo=2013-04-04 18:00:00\n" + "\n" + "audits?officeId=1\n" + "\n" + "audits?officeId=1&includeJson=true")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = MakercheckersApiResourceSwagger.GetMakerCheckerResponse.class, responseContainer = "list")})
    public String retrieveAuditEntries(@Context final UriInfo uriInfo, @QueryParam("actionName") @ApiParam(value = "actionName") final String actionName,
            @QueryParam("entityName") @ApiParam(value = "entityName") final String entityName, @QueryParam("resourceId") @ApiParam(value = "resourceId") final Long resourceId,
            @QueryParam("makerId") @ApiParam(value = "makerId") final Long makerId, @QueryParam("makerDateTimeFrom") @ApiParam(value = "makerDateTimeFrom") final String makerDateTimeFrom,
            @QueryParam("makerDateTimeTo") @ApiParam(value = "makerDateTimeTo") final String makerDateTimeTo, @QueryParam("checkerId") @ApiParam(value = "checkerId") final Long checkerId,
            @QueryParam("checkerDateTimeFrom") @ApiParam(value = "checkerDateTimeFrom") final String checkerDateTimeFrom,
            @QueryParam("checkerDateTimeTo") @ApiParam(value = "checkerDateTimeTo") final String checkerDateTimeTo,
            @QueryParam("processingResult") @ApiParam(value = "processingResult") final Integer processingResult, @QueryParam("officeId") @ApiParam(value = "officeId") final Integer officeId,
            @QueryParam("groupId") @ApiParam(value = "groupId") final Integer groupId, @QueryParam("clientId") @ApiParam(value = "clientId") final Integer clientId,
            @QueryParam("loanid") @ApiParam(value = "loanid") final Integer loanId, @QueryParam("savingsAccountId") @ApiParam(value = "savingsAccountId") final Integer savingsAccountId,
            @QueryParam("paged") @ApiParam(value = "paged") final Boolean paged, @QueryParam("offset") @ApiParam(value = "offset") final Integer offset, @QueryParam("limit") @ApiParam(value = "limit") final Integer limit,
            @QueryParam("orderBy") @ApiParam(value = "orderBy") final String orderBy, @QueryParam("sortOrder") @ApiParam(value = "sortOrder") final String sortOrder) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final PaginationParameters parameters = PaginationParameters.instance(paged, offset, limit, orderBy, sortOrder);
        final String extraCriteria = getExtraCriteria(actionName, entityName, resourceId, makerId, makerDateTimeFrom, makerDateTimeTo,
                checkerId, checkerDateTimeFrom, checkerDateTimeTo, processingResult, officeId, groupId, clientId, loanId, savingsAccountId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (parameters.isPaged()) {
            final Page<AuditData> auditEntries = this.auditReadPlatformService.retrievePaginatedAuditEntries(extraCriteria,
                    settings.isIncludeJson(), parameters);
            return this.toApiJsonSerializer.serialize(settings, auditEntries, this.RESPONSE_DATA_PARAMETERS);
        }

        final Collection<AuditData> auditEntries = this.auditReadPlatformService.retrieveAuditEntries(extraCriteria,
                settings.isIncludeJson());

        return this.toApiJsonSerializer.serialize(settings, auditEntries, this.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{auditId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve an Audit Entry", notes = "Example Requests:\n" + "\n" + "audits/20\n" + "audits/20?fields=madeOnDate,maker,processingResult")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = MakercheckersApiResourceSwagger.GetMakerCheckerResponse.class)})
    public String retrieveAuditEntry(@PathParam("auditId") @ApiParam(value = "auditId") final Long auditId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final AuditData auditEntry = this.auditReadPlatformService.retrieveAuditEntry(auditId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, auditEntry, this.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("/searchtemplate")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Audit Search Template", notes = "This is a convenience resource. It can be useful when building an Audit Search UI. \"appUsers\" are data scoped to the office/branch the requestor is associated with.\n" + "\n" + "Example Requests:\n" + "\n" + "audits/searchtemplate\n" + "audits/searchtemplate?fields=actionNames")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = MakercheckersApiResourceSwagger.GetMakerCheckersSearchTemplateResponse.class)})
    public String retrieveAuditSearchTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final AuditSearchData auditSearchData = this.auditReadPlatformService.retrieveSearchTemplate("audit");

        final Set<String> RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE = new HashSet<>(Arrays.asList("appUsers", "actionNames",
                "entityNames", "processingResults"));

        return this.toApiJsonSerializerSearchTemplate.serialize(settings, auditSearchData, RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE);
    }

    private String getExtraCriteria(final String actionName, final String entityName, final Long resourceId, final Long makerId,
            final String makerDateTimeFrom, final String makerDateTimeTo, final Long checkerId, final String checkerDateTimeFrom,
            final String checkerDateTimeTo, final Integer processingResult, final Integer officeId, final Integer groupId,
            final Integer clientId, final Integer loanId, final Integer savingsAccountId) {

        String extraCriteria = "";

        if (actionName != null) {
            extraCriteria += " and aud.action_name = " + ApiParameterHelper.sqlEncodeString(actionName);
        }
        if (entityName != null) {
            extraCriteria += " and aud.entity_name like " + ApiParameterHelper.sqlEncodeString(entityName + "%");
        }

        if (resourceId != null) {
            extraCriteria += " and aud.resource_id = " + resourceId;
        }
        if (makerId != null) {
            extraCriteria += " and aud.maker_id = " + makerId;
        }
        if (checkerId != null) {
            extraCriteria += " and aud.checker_id = " + checkerId;
        }
        if (makerDateTimeFrom != null) {
            extraCriteria += " and aud.made_on_date >= " + ApiParameterHelper.sqlEncodeString(makerDateTimeFrom);
        }
        if (makerDateTimeTo != null) {
            extraCriteria += " and aud.made_on_date <= " + ApiParameterHelper.sqlEncodeString(makerDateTimeTo);
        }
        if (checkerDateTimeFrom != null) {
            extraCriteria += " and aud.checked_on_date >= " + ApiParameterHelper.sqlEncodeString(checkerDateTimeFrom);
        }
        if (checkerDateTimeTo != null) {
            extraCriteria += " and aud.checked_on_date <= " + ApiParameterHelper.sqlEncodeString(checkerDateTimeTo);
        }

        if (processingResult != null) {
            extraCriteria += " and aud.processing_result_enum = " + processingResult;
        }

        if (officeId != null) {
            extraCriteria += " and aud.office_id = " + officeId;
        }

        if (groupId != null) {
            extraCriteria += " and aud.group_id = " + groupId;
        }

        if (clientId != null) {
            extraCriteria += " and aud.client_id = " + clientId;
        }

        if (loanId != null) {
            extraCriteria += " and aud.loan_id = " + loanId;
        }

        if (savingsAccountId != null) {
            extraCriteria += " and aud.savings_account_id = " + savingsAccountId;
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }

        return extraCriteria;
    }
}