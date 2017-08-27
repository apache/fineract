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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/makercheckers")
@Component
@Scope("singleton")
@Api(value = "Maker Checker (or 4-eye) functionality")
public class MakercheckersApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "actionName", "entityName", "resourceId",
            "subresourceId", "maker", "madeOnDate", "checker", "checkedOnDate", "processingResult", "commandAsJson", "officeName",
            "groupLevelName", "groupName", "clientName", "loanAccountNo", "savingsAccountNo", "clientId", "loanId"));

    private final AuditReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializerAudit;
    private final DefaultToApiJsonSerializer<AuditSearchData> toApiJsonSerializerSearchTemplate;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService writePlatformService;

    @Autowired
    public MakercheckersApiResource(final AuditReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializerAudit,
            final DefaultToApiJsonSerializer<AuditSearchData> toApiJsonSerializerSearchTemplate,
            final ApiRequestParameterHelper apiRequestParameterHelper, final PortfolioCommandSourceWritePlatformService writePlatformService) {
        this.readPlatformService = readPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.toApiJsonSerializerAudit = toApiJsonSerializerAudit;
        this.toApiJsonSerializerSearchTemplate = toApiJsonSerializerSearchTemplate;
        this.writePlatformService = writePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Maker Checker Entries", notes = "Get a list of entries that can be checked by the requestor that match the criteria supplied.\n" + "\n" + "Example Requests:\n" + "\n" + "makercheckers\n" + "\n" + "makercheckers?fields=madeOnDate,maker,processingResult\n" + "\n" + "makercheckers?makerDateTimeFrom=2013-03-25 08:00:00&makerDateTimeTo=2013-04-04 18:00:00\n" + "\n" + "makercheckers?officeId=1\n" + "\n" + "makercheckers?officeId=1&includeJson=true")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = MakercheckersApiResourceSwagger.GetMakerCheckerResponse.class, responseContainer = "list")})
    public String retrieveCommands(@Context final UriInfo uriInfo, @QueryParam("actionName") @ApiParam(value = "actionName") final String actionName,
            @QueryParam("entityName") @ApiParam(value = "entityName") final String entityName, @QueryParam("resourceId") @ApiParam(value = "resourceId") final Long resourceId,
            @QueryParam("makerId") @ApiParam(value = "makerId") final Long makerId, @QueryParam("makerDateTimeFrom") @ApiParam(value = "makerDateTimeFrom") final String makerDateTimeFrom,
            @QueryParam("makerDateTimeTo") @ApiParam(value = "makerDateTimeTo") final String makerDateTimeTo, @QueryParam("officeId") @ApiParam(value = "officeId") final Integer officeId,
            @QueryParam("groupId") @ApiParam(value = "groupId") final Integer groupId, @QueryParam("clientId") @ApiParam(value = "clientId") final Integer clientId,
            @QueryParam("loanid") @ApiParam(value = "loanid") final Integer loanId, @QueryParam("savingsAccountId") @ApiParam(value = "savingsAccountId") final Integer savingsAccountId) {

        final String extraCriteria = getExtraCriteria(actionName, entityName, resourceId, makerId, makerDateTimeFrom, makerDateTimeTo,
                officeId, groupId, clientId, loanId, savingsAccountId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final Collection<AuditData> entries = this.readPlatformService.retrieveAllEntriesToBeChecked(extraCriteria,
                settings.isIncludeJson());

        return this.toApiJsonSerializerAudit.serialize(settings, entries, this.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("/searchtemplate")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Maker Checker Search Template", notes = "This is a convenience resource. It can be useful when building a Checker Inbox UI. \"appUsers\" are data scoped to the office/branch the requestor is associated with. \"actionNames\" and \"entityNames\" returned are those that the requestor has Checker approval permissions for.\n" + "\n" + "Example Requests:\n" + "\n" + "makercheckers/searchtemplate\n" + "makercheckers/searchtemplate?fields=entityNames")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = MakercheckersApiResourceSwagger.GetMakerCheckersSearchTemplateResponse.class)})
    public String retrieveAuditSearchTemplate(@Context final UriInfo uriInfo) {

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final AuditSearchData auditSearchData = this.readPlatformService.retrieveSearchTemplate("makerchecker");

        final Set<String> RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE = new HashSet<>(Arrays.asList("appUsers", "actionNames",
                "entityNames"));

        return this.toApiJsonSerializerSearchTemplate.serialize(settings, auditSearchData, RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE);
    }

    @POST
    @Path("{auditId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Approve Maker Checker Entry | Reject Maker Checker Entry")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = MakercheckersApiResourceSwagger.PostMakerCheckersResponse.class)})
    public String approveMakerCheckerEntry(@PathParam("auditId") @ApiParam(value = "auditId") final Long auditId, @QueryParam("command") @ApiParam(value = "command") final String commandParam) {

        CommandProcessingResult result = null;
        if (is(commandParam, "approve")) {
            result = this.writePlatformService.approveEntry(auditId);
        } else if (is(commandParam, "reject")) {
            final Long id = this.writePlatformService.rejectEntry(auditId);
            result = CommandProcessingResult.commandOnlyResult(id);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }
        return this.toApiJsonSerializerAudit.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @DELETE
    @Path("{auditId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete Maker Checker Entry")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = MakercheckersApiResourceSwagger.PostMakerCheckersResponse.class)})
    public String deleteMakerCheckerEntry(@PathParam("auditId") @ApiParam(value = "auditId") final Long auditId) {

        final Long id = this.writePlatformService.deleteEntry(auditId);

        return this.toApiJsonSerializerAudit.serialize(CommandProcessingResult.commandOnlyResult(id));
    }

    private String getExtraCriteria(final String actionName, final String entityName, final Long resourceId, final Long makerId,
            final String makerDateTimeFrom, final String makerDateTimeTo, final Integer officeId, final Integer groupId,
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
        if (makerDateTimeFrom != null) {
            extraCriteria += " and aud.made_on_date >= " + ApiParameterHelper.sqlEncodeString(makerDateTimeFrom);
        }
        if (makerDateTimeTo != null) {
            extraCriteria += " and aud.made_on_date <= " + ApiParameterHelper.sqlEncodeString(makerDateTimeTo);
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