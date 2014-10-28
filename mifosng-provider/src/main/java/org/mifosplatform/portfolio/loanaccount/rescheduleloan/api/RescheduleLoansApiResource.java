/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.rescheduleloan.api;

import java.util.HashSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.RescheduleLoansApiConstants;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestData;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleModel;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.service.LoanReschedulePreviewPlatformService;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.service.LoanRescheduleRequestReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/rescheduleloans")
@Component
@Scope("singleton")
public class RescheduleLoansApiResource {

    private final DefaultToApiJsonSerializer<LoanRescheduleRequestData> loanRescheduleRequestToApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LoanScheduleData> loanRescheduleToApiJsonSerializer;
    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final LoanRescheduleRequestReadPlatformService loanRescheduleRequestReadPlatformService;
    private final LoanReschedulePreviewPlatformService loanReschedulePreviewPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public RescheduleLoansApiResource(final DefaultToApiJsonSerializer<LoanRescheduleRequestData> loanRescheduleRequestToApiJsonSerializer,
            final PlatformSecurityContext platformSecurityContext,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final LoanRescheduleRequestReadPlatformService loanRescheduleRequestReadPlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final DefaultToApiJsonSerializer<LoanScheduleData> loanRescheduleToApiJsonSerializer,
            final LoanReschedulePreviewPlatformService loanReschedulePreviewPlatformService) {
        this.loanRescheduleRequestToApiJsonSerializer = loanRescheduleRequestToApiJsonSerializer;
        this.platformSecurityContext = platformSecurityContext;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.loanRescheduleRequestReadPlatformService = loanRescheduleRequestReadPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.loanRescheduleToApiJsonSerializer = loanRescheduleToApiJsonSerializer;
        this.loanReschedulePreviewPlatformService = loanReschedulePreviewPlatformService;
    }

    @GET
    @Path("{requestId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String readLoanRescheduleRequest(@Context final UriInfo uriInfo, @PathParam("requestId") final Long requestId,
            @QueryParam("command") final String command) {
        this.platformSecurityContext.authenticatedUser().validateHasReadPermission(RescheduleLoansApiConstants.ENTITY_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (compareIgnoreCase(command, "previewLoanReschedule")) {
            final LoanRescheduleModel loanRescheduleModel = this.loanReschedulePreviewPlatformService.previewLoanReschedule(requestId);

            return this.loanRescheduleToApiJsonSerializer.serialize(settings, loanRescheduleModel.toData(), new HashSet<String>());
        }

        final LoanRescheduleRequestData loanRescheduleRequestData = this.loanRescheduleRequestReadPlatformService
                .readLoanRescheduleRequest(requestId);

        return this.loanRescheduleRequestToApiJsonSerializer.serialize(settings, loanRescheduleRequestData);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createLoanRescheduleRequest(final String apiRequestBodyAsJson) {
        final CommandWrapper commandWrapper = new CommandWrapperBuilder()
                .createLoanRescheduleRequest(RescheduleLoansApiConstants.ENTITY_NAME).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);

        return this.loanRescheduleRequestToApiJsonSerializer.serialize(commandProcessingResult);
    }

    @POST
    @Path("{requestId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateLoanRescheduleRequest(@PathParam("requestId") final Long requestId, @QueryParam("command") final String command,
            final String apiRequestBodyAsJson) {
        CommandWrapper commandWrapper = null;

        if (compareIgnoreCase(command, "approve")) {
            commandWrapper = new CommandWrapperBuilder().approveLoanRescheduleRequest(RescheduleLoansApiConstants.ENTITY_NAME, requestId)
                    .withJson(apiRequestBodyAsJson).build();
        }

        else if (compareIgnoreCase(command, "reject")) {
            commandWrapper = new CommandWrapperBuilder().rejectLoanRescheduleRequest(RescheduleLoansApiConstants.ENTITY_NAME, requestId)
                    .withJson(apiRequestBodyAsJson).build();
        }

        else {
            throw new UnrecognizedQueryParamException("command", command, new Object[] { "approve", "reject" });
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
}
