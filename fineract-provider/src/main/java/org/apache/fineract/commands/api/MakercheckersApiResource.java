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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.data.AuditData;
import org.apache.fineract.commands.data.AuditSearchData;
import org.apache.fineract.commands.data.request.MakerCheckerRequest;
import org.apache.fineract.commands.service.AuditReadPlatformService;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.security.utils.SQLBuilder;
import org.springframework.stereotype.Component;

@Path("/v1/makercheckers")
@Component
@Tag(name = "Maker Checker (or 4-eye) functionality")
@RequiredArgsConstructor
public class MakercheckersApiResource {

    private static final String COMMAND_APPROVE = "approve";
    private static final String COMMAND_REJECT = "reject";

    private final AuditReadPlatformService readPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService writePlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Maker Checker Entries", description = "Get a list of entries that can be checked by the requestor that match the criteria supplied.\n"
            + "\n" + "Example Requests:\n" + "\n" + "makercheckers\n" + "\n" + "makercheckers?fields=madeOnDate,maker,processingResult\n"
            + "\n" + "makercheckers?makerDateTimeFrom=2013-03-25 08:00:00&makerDateTimeTo=2013-04-04 18:00:00\n" + "\n"
            + "makercheckers?officeId=1\n" + "\n" + "makercheckers?officeId=1&includeJson=true")
    public List<AuditData> retrieveCommands(@Context final UriInfo uriInfo, @BeanParam MakerCheckerRequest makerCheckerRequest) {
        final SQLBuilder extraCriteria = getExtraCriteria(makerCheckerRequest);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return readPlatformService.retrieveAllEntriesToBeChecked(extraCriteria, settings.isIncludeJson());

    }

    @GET
    @Path("/searchtemplate")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Maker Checker Search Template", description = "This is a convenience resource. It can be useful when building a Checker Inbox UI. \"appUsers\" are data scoped to the office/branch the requestor is associated with. \"actionNames\" and \"entityNames\" returned are those that the requestor has Checker approval permissions for.\n"
            + "\n" + "Example Requests:\n" + "\n" + "makercheckers/searchtemplate\n" + "makercheckers/searchtemplate?fields=entityNames")
    public AuditSearchData retrieveAuditSearchTemplate() {
        return readPlatformService.retrieveSearchTemplate("makerchecker");
    }

    @POST
    @Path("{auditId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Approve Maker Checker Entry | Reject Maker Checker Entry")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MakercheckersApiResourceSwagger.PostMakerCheckersResponse.class))) })
    public CommandProcessingResult approveMakerCheckerEntry(@PathParam("auditId") @Parameter(description = "auditId") final Long auditId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam) {

        CommandProcessingResult result = null;
        if (is(commandParam, COMMAND_APPROVE)) {
            result = writePlatformService.approveEntry(auditId);
        } else if (is(commandParam, COMMAND_REJECT)) {
            final Long id = writePlatformService.rejectEntry(auditId);
            result = CommandProcessingResult.commandOnlyResult(id);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }
        return result;
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
    public CommandProcessingResult deleteMakerCheckerEntry(@PathParam("auditId") @Parameter(description = "auditId") final Long auditId) {
        final Long id = writePlatformService.deleteEntry(auditId);
        return CommandProcessingResult.commandOnlyResult(id);
    }

    private SQLBuilder getExtraCriteria(MakerCheckerRequest makerCheckerRequest) {

        SQLBuilder extraCriteria = new SQLBuilder();
        extraCriteria.addNonNullCriteria("aud.action_name = ", makerCheckerRequest.getActionName());
        if (makerCheckerRequest.getEntityName() != null) {
            extraCriteria.addCriteria("aud.entity_name like ", makerCheckerRequest.getEntityName() + "%");
        }
        extraCriteria.addNonNullCriteria("aud.resource_id = ", makerCheckerRequest.getResourceId());
        extraCriteria.addNonNullCriteria("aud.maker_id = ", makerCheckerRequest.getMakerId());
        extraCriteria.addNonNullCriteria("aud.made_on_date >= ", makerCheckerRequest.getMakerDateTimeFrom());
        extraCriteria.addNonNullCriteria("aud.made_on_date <= ", makerCheckerRequest.getMakerDateTimeTo());
        extraCriteria.addNonNullCriteria("aud.office_id = ", makerCheckerRequest.getOfficeId());
        extraCriteria.addNonNullCriteria("aud.group_id = ", makerCheckerRequest.getGroupId());
        extraCriteria.addNonNullCriteria("aud.client_id = ", makerCheckerRequest.getClientId());
        extraCriteria.addNonNullCriteria("aud.loan_id = ", makerCheckerRequest.getLoanId());
        extraCriteria.addNonNullCriteria("aud.savings_account_id = ", makerCheckerRequest.getSavingsAccountId());

        return extraCriteria;
    }
}
