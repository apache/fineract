/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.api;

import java.util.HashSet;

import javax.ws.rs.Consumes;
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
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans/{loanId}/schedule")
@Component
@Scope("singleton")
public class LoanScheduleApiResource {

    private final String resourceNameForPermissions = "LOAN";
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<LoanScheduleData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public LoanScheduleApiResource(final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<LoanScheduleData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final LoanScheduleCalculationPlatformService calculationPlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper) {
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.calculationPlatformService = calculationPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String calculateLoanScheduleOrSubmitVariableSchedule(@PathParam("loanId") final Long loanId,
            @QueryParam("command") final String commandParam, @Context final UriInfo uriInfo, final String apiRequestBodyAsJson) {

        CommandWrapper commandRequest = null;
        if (is(commandParam, "calculateLoanSchedule")) {
            this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
            final LoanScheduleData loanSchedule = this.calculationPlatformService.generateLoanScheduleForVariableInstallmentRequest(loanId,
                    apiRequestBodyAsJson);

            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, loanSchedule, new HashSet<String>());
        } else if (is(commandParam, "addVariations")) {
            commandRequest = new CommandWrapperBuilder().createScheduleExceptions(loanId).withJson(apiRequestBodyAsJson).build();
        } else if (is(commandParam, "deleteVariations")) {
            commandRequest = new CommandWrapperBuilder().deleteScheduleExceptions(loanId).build();
        }

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

}
