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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.RescheduleLoansApiConstants;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestData;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.service.LoanReschedulePreviewPlatformService;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.service.LoanRescheduleRequestReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/rescheduleloans")
@Component
@Tag(name = "Reschedule Loans", description = "")
@RequiredArgsConstructor
public class RescheduleLoansApiResource {

    private final DefaultToApiJsonSerializer<LoanRescheduleRequestData> loanRescheduleRequestToApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LoanScheduleData> loanRescheduleToApiJsonSerializer;
    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final LoanRescheduleRequestReadPlatformService loanRescheduleRequestReadPlatformService;
    private final LoanReschedulePreviewPlatformService loanReschedulePreviewPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve all reschedule loan reasons", description = "Retrieve all reschedule loan reasons as a template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RescheduleLoansApiResourceSwagger.GetRescheduleReasonsTemplateResponse.class))) })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.platformSecurityContext.authenticatedUser().validateHasReadPermission(RescheduleLoansApiConstants.ENTITY_NAME);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        LoanRescheduleRequestData loanRescheduleReasons = this.loanRescheduleRequestReadPlatformService
                .retrieveAllRescheduleReasons(RescheduleLoansApiConstants.LOAN_RESCHEDULE_REASON);

        return this.loanRescheduleRequestToApiJsonSerializer.serialize(settings, loanRescheduleReasons);
    }

    @GET
    @Path("{scheduleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve loan reschedule request by schedule id", description = "Retrieve loan reschedule request by schedule id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RescheduleLoansApiResourceSwagger.GetLoanRescheduleRequestResponse.class))) })
    public String readLoanRescheduleRequest(@Context final UriInfo uriInfo, @PathParam("scheduleId") final Long scheduleId,
            @QueryParam("command") final String command) {
        this.platformSecurityContext.authenticatedUser().validateHasReadPermission(RescheduleLoansApiConstants.ENTITY_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (compareIgnoreCase(command, "previewLoanReschedule")) {
            final LoanScheduleModel loanRescheduleModel = this.loanReschedulePreviewPlatformService.previewLoanReschedule(scheduleId);

            return this.loanRescheduleToApiJsonSerializer.serialize(settings, loanRescheduleModel.toData(), new HashSet<String>());
        }

        final LoanRescheduleRequestData loanRescheduleRequestData = this.loanRescheduleRequestReadPlatformService
                .readLoanRescheduleRequest(scheduleId);

        return this.loanRescheduleRequestToApiJsonSerializer.serialize(settings, loanRescheduleRequestData);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create loan reschedule request", description = "Create a loan reschedule request.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RescheduleLoansApiResourceSwagger.PostCreateRescheduleLoansRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RescheduleLoansApiResourceSwagger.PostCreateRescheduleLoansResponse.class))) })
    public String createLoanRescheduleRequest(final String apiRequestBodyAsJson) {
        final CommandWrapper commandWrapper = new CommandWrapperBuilder()
                .createLoanRescheduleRequest(RescheduleLoansApiConstants.ENTITY_NAME).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);

        return this.loanRescheduleRequestToApiJsonSerializer.serialize(commandProcessingResult);
    }

    @POST
    @Path("{scheduleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update loan reschedule request", description = "Update a loan reschedule request by either approving/rejecting it.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RescheduleLoansApiResourceSwagger.PostUpdateRescheduleLoansRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RescheduleLoansApiResourceSwagger.PostUpdateRescheduleLoansResponse.class))) })
    public String updateLoanRescheduleRequest(@PathParam("scheduleId") final Long scheduleId, @QueryParam("command") final String command,
            final String apiRequestBodyAsJson) {
        CommandWrapper commandWrapper;

        if (compareIgnoreCase(command, "approve")) {
            commandWrapper = new CommandWrapperBuilder().approveLoanRescheduleRequest(RescheduleLoansApiConstants.ENTITY_NAME, scheduleId)
                    .withJson(apiRequestBodyAsJson).build();
        }

        else if (compareIgnoreCase(command, "reject")) {
            commandWrapper = new CommandWrapperBuilder().rejectLoanRescheduleRequest(RescheduleLoansApiConstants.ENTITY_NAME, scheduleId)
                    .withJson(apiRequestBodyAsJson).build();
        }

        else {
            throw new UnrecognizedQueryParamException("command", command, "approve", "reject");
        }

        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);

        return this.loanRescheduleRequestToApiJsonSerializer.serialize(commandProcessingResult);
    }

    /**
     * Compares two strings, ignoring differences in case
     *
     * @param firstString
     *            the first string
     * @param secondString
     *            the second string
     * @return true if the two strings are equal, else false
     **/
    private boolean compareIgnoreCase(String firstString, String secondString) {
        return StringUtils.isNotBlank(firstString) && firstString.trim().equalsIgnoreCase(secondString);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve all reschedule requests", description = "Retrieve all reschedule requests.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = RescheduleLoansApiResourceSwagger.GetLoanRescheduleRequestResponse.class)))) })
    public String retrieveAllRescheduleRequest(@Context final UriInfo uriInfo, @QueryParam("command") final String command,
            @QueryParam("loanId") Long loanId) {

        this.platformSecurityContext.authenticatedUser().validateHasReadPermission(RescheduleLoansApiConstants.ENTITY_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (StringUtils.isNotBlank(command) && !RescheduleLoansApiConstants.commandParams.contains(command.toLowerCase())) {
            throw new UnrecognizedQueryParamException("command", command, RescheduleLoansApiConstants.allCommandParamName,
                    RescheduleLoansApiConstants.pendingCommandParamName, RescheduleLoansApiConstants.approveCommandParamName,
                    RescheduleLoansApiConstants.rejectCommandParamName);
        }
        final List<LoanRescheduleRequestData> loanRescheduleRequestsData = this.loanRescheduleRequestReadPlatformService
                .retrieveAllRescheduleRequests(command, loanId);

        return this.loanRescheduleRequestToApiJsonSerializer.serialize(settings, loanRescheduleRequestsData);
    }
}
