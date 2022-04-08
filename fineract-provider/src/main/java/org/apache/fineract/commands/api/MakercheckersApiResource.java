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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.data.AuditData;
import org.apache.fineract.commands.data.AuditSearchData;
import org.apache.fineract.commands.service.AuditReadPlatformService;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.utils.SQLBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/makercheckers")
@Component
@Scope("singleton")
@Tag(name = "Maker Checker (or 4-eye) functionality")
@RequiredArgsConstructor
public class MakercheckersApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "actionName", "entityName", "resourceId",
            "subresourceId", "maker", "madeOnDate", "checker", "checkedOnDate", "processingResult", "commandAsJson", "officeName",
            "groupLevelName", "groupName", "clientName", "loanAccountNo", "savingsAccountNo", "clientId", "loanId"));

    private final AuditReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializerAudit;
    private final DefaultToApiJsonSerializer<AuditSearchData> toApiJsonSerializerSearchTemplate;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService writePlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Maker Checker Entries", description = "Get a list of entries that can be checked by the requestor that match the criteria supplied.\n"
            + "\n" + "Example Requests:\n" + "\n" + "makercheckers\n" + "\n" + "makercheckers?fields=madeOnDate,maker,processingResult\n"
            + "\n" + "makercheckers?makerDateTimeFrom=2013-03-25 08:00:00&makerDateTimeTo=2013-04-04 18:00:00\n" + "\n"
            + "makercheckers?officeId=1\n" + "\n" + "makercheckers?officeId=1&includeJson=true")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MakercheckersApiResourceSwagger.GetMakerCheckerResponse.class)))) })
    public String retrieveCommands(@Context final UriInfo uriInfo,
            @QueryParam("actionName") @Parameter(description = "actionName") final String actionName,
            @QueryParam("entityName") @Parameter(description = "entityName") final String entityName,
            @QueryParam("resourceId") @Parameter(description = "resourceId") final Long resourceId,
            @QueryParam("makerId") @Parameter(description = "makerId") final Long makerId,
            @QueryParam("makerDateTimeFrom") @Parameter(description = "makerDateTimeFrom") final String makerDateTimeFrom,
            @QueryParam("makerDateTimeTo") @Parameter(description = "makerDateTimeTo") final String makerDateTimeTo,
            @QueryParam("officeId") @Parameter(description = "officeId") final Integer officeId,
            @QueryParam("groupId") @Parameter(description = "groupId") final Integer groupId,
            @QueryParam("clientId") @Parameter(description = "clientId") final Integer clientId,
            @QueryParam("loanid") @Parameter(description = "loanid") final Integer loanId,
            @QueryParam("savingsAccountId") @Parameter(description = "savingsAccountId") final Integer savingsAccountId) {

        final SQLBuilder extraCriteria = getExtraCriteria(actionName, entityName, resourceId, makerId, makerDateTimeFrom, makerDateTimeTo,
                officeId, groupId, clientId, loanId, savingsAccountId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final Collection<AuditData> entries = this.readPlatformService.retrieveAllEntriesToBeChecked(extraCriteria,
                settings.isIncludeJson());

        return this.toApiJsonSerializerAudit.serialize(settings, entries, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("/searchtemplate")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Maker Checker Search Template", description = "This is a convenience resource. It can be useful when building a Checker Inbox UI. \"appUsers\" are data scoped to the office/branch the requestor is associated with. \"actionNames\" and \"entityNames\" returned are those that the requestor has Checker approval permissions for.\n"
            + "\n" + "Example Requests:\n" + "\n" + "makercheckers/searchtemplate\n" + "makercheckers/searchtemplate?fields=entityNames")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MakercheckersApiResourceSwagger.GetMakerCheckersSearchTemplateResponse.class))) })
    public String retrieveAuditSearchTemplate(@Context final UriInfo uriInfo) {

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final AuditSearchData auditSearchData = this.readPlatformService.retrieveSearchTemplate("makerchecker");

        final Set<String> RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE = new HashSet<>(Arrays.asList("appUsers", "actionNames", "entityNames"));

        return this.toApiJsonSerializerSearchTemplate.serialize(settings, auditSearchData, RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE);
    }

    @POST
    @Path("{auditId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Approve Maker Checker Entry | Reject Maker Checker Entry")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MakercheckersApiResourceSwagger.PostMakerCheckersResponse.class))) })
    public String approveMakerCheckerEntry(@PathParam("auditId") @Parameter(description = "auditId") final Long auditId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam) {

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
    @Operation(summary = "Delete Maker Checker Entry")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MakercheckersApiResourceSwagger.PostMakerCheckersResponse.class))) })
    public String deleteMakerCheckerEntry(@PathParam("auditId") @Parameter(description = "auditId") final Long auditId) {

        final Long id = this.writePlatformService.deleteEntry(auditId);

        return this.toApiJsonSerializerAudit.serialize(CommandProcessingResult.commandOnlyResult(id));
    }

    private SQLBuilder getExtraCriteria(final String actionName, final String entityName, final Long resourceId, final Long makerId,
            final String makerDateTimeFrom, final String makerDateTimeTo, final Integer officeId, final Integer groupId,
            final Integer clientId, final Integer loanId, final Integer savingsAccountId) {

        SQLBuilder extraCriteria = new SQLBuilder();
        extraCriteria.addNonNullCriteria("aud.action_name = ", actionName);
        if (entityName != null) {
            extraCriteria.addCriteria("aud.entity_name like ", entityName + "%");
        }
        extraCriteria.addNonNullCriteria("aud.resource_id = ", resourceId);
        extraCriteria.addNonNullCriteria("aud.maker_id = ", makerId);
        extraCriteria.addNonNullCriteria("aud.made_on_date >= ", makerDateTimeFrom);
        extraCriteria.addNonNullCriteria("aud.made_on_date <= ", makerDateTimeTo);
        extraCriteria.addNonNullCriteria("aud.office_id = ", officeId);
        extraCriteria.addNonNullCriteria("aud.group_id = ", groupId);
        extraCriteria.addNonNullCriteria("aud.client_id = ", clientId);
        extraCriteria.addNonNullCriteria("aud.loan_id = ", loanId);
        extraCriteria.addNonNullCriteria("aud.savings_account_id = ", savingsAccountId);

        return extraCriteria;
    }
}
