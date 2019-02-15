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
package org.apache.fineract.portfolio.loanaccount.api;

import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.HashSet;

@Path("/loans/{loanId}/schedule")
@Component
@Scope("singleton")
@Api(value = "Loan Rescheduling", description = "Loan Term Variations provides the ability to change due dates, amounts and number of instalments before loan approval.")
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
    @ApiOperation(value = "Calculate loan repayment schedule based on Loan term variations | Updates loan repayment schedule based on Loan term variations | Updates loan repayment schedule by removing Loan term variations", httpMethod = "POST", notes = "Calculate loan repayment schedule based on Loan term variations:\n\n" + "Mandatory Fields: exceptions,locale,dateFormat\n\n" + "Updates loan repayment schedule based on Loan term variations:\n\n" + "Mandatory Fields: exceptions,locale,dateFormat\n\n" + "Updates loan repayment schedule by removing Loan term variations:\n\n" + "It updates the loan repayment schedule by removing Loan term variations\n\n" + "Showing request/response for 'Updates loan repayment schedule by removing Loan term variations'")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = LoanScheduleApiResourceSwagger.PostLoansLoanIdScheduleRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = LoanScheduleApiResourceSwagger.PostLoansLoanIdScheduleResponse.class)})
    public String calculateLoanScheduleOrSubmitVariableSchedule(@PathParam("loanId") @ApiParam(value = "loanId") final Long loanId,
            @QueryParam("command") @ApiParam(value = "command") final String commandParam, @Context final UriInfo uriInfo, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

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
